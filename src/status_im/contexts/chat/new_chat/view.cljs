(ns status-im.contexts.chat.new-chat.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [re-frame.core :as re-frame]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [status-im.common.contact-list-item.view :as contact-list-item]
    [status-im.common.contact-list.view :as contact-list]
    [status-im.common.resources :as resources]
    [status-im.constants :as constants]
    [status-im.contexts.chat.new-chat.styles :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- no-contacts-view
  [{:keys [theme]}]
  [rn/view
   {:style (style/no-contacts)}
   [rn/image {:source (resources/get-themed-image :no-contacts theme)}]
   [quo/text
    {:weight :semi-bold
     :size   :paragraph-1
     :style  {:margin-bottom 2
              :margin-top    12}}
    (i18n/label :t/you-have-no-contacts)]
   [quo/text
    {:weight :regular
     :size   :label}
    (i18n/label :t/dont-yell-at-me)]
   [quo/button
    {:type            :primary
     :size            32
     :container-style {:margin-top    20
                       :margin-bottom 12}
     :on-press        #(rf/dispatch [:invite.events/share-link])}
    (i18n/label :t/invite-friends)]
   [quo/button
    {:type     :grey
     :size     32
     :on-press #(do
                  (rf/dispatch [:navigate-back])
                  (rf/dispatch [:open-modal :new-contact]))}
    (i18n/label :t/add-a-contact)]])

(defn contact-item-render
  [_]
  (fn [{:keys [public-key] :as item}]
    (let [user-selected? (rf/sub [:is-contact-selected? public-key])
          on-toggle      #(if user-selected?
                            (re-frame/dispatch [:deselect-contact public-key])
                            (re-frame/dispatch [:select-contact public-key]))]
      [contact-list-item/contact-list-item
       {:on-press  on-toggle
        :accessory {:type     :checkbox
                    :checked? user-selected?
                    :on-check on-toggle}}
       item])))

(defn- view-internal
  [{:keys [scroll-enabled on-scroll close theme]}]
  (let [contacts                          (rf/sub [:contacts/sorted-and-grouped-by-first-letter])
        selected-contacts-count           (rf/sub [:selected-contacts-count])
        selected-contacts                 (rf/sub [:group/selected-contacts])
        one-contact-selected?             (= selected-contacts-count 1)
        contacts-selected?                (pos? selected-contacts-count)
        {:keys [primary-name public-key]} (when one-contact-selected?
                                            (rf/sub [:contacts/contact-by-identity
                                                     (first selected-contacts)]))]
    [rn/view {:flex 1}
     [rn/view {:padding-horizontal 20}
      [quo/button
       {:type       :grey
        :size       32
        :icon-only? true
        :on-press   close} :i/close]
      [rn/view {:style style/contact-selection-heading}
       [quo/text
        {:weight :semi-bold
         :size   :heading-1
         :style  {:color (colors/theme-colors colors/neutral-100 colors/white theme)}}
        (i18n/label :t/new-chat)]
       (when (seq contacts)
         [quo/text
          {:size   :paragraph-2
           :weight :regular
           :style  {:margin-bottom 2
                    :color         (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)}}
          (i18n/label :t/selected-count-from-max
                      {:selected selected-contacts-count
                       :max      constants/max-group-chat-participants})])]]
     (if (empty? contacts)
       [no-contacts-view {:theme theme}]
       [gesture/section-list
        {:key-fn                         :title
         :sticky-section-headers-enabled false
         :sections                       (rf/sub [:contacts/filtered-active-sections])
         :render-section-header-fn       contact-list/contacts-section-header
         :content-container-style        {:padding-bottom 70}
         :render-fn                      contact-item-render
         :scroll-enabled                 @scroll-enabled
         :on-scroll                      on-scroll}])
     (when contacts-selected?
       [quo/button
        {:type                :primary
         :accessibility-label :next-button
         :container-style     style/chat-button
         :on-press            (fn []
                                (if one-contact-selected?
                                  (rf/dispatch [:chat.ui/start-chat public-key])
                                  (rf/dispatch [:navigate-to :new-group])))}
        (if one-contact-selected?
          (i18n/label :t/chat-with {:selected-user primary-name})
          (i18n/label :t/setup-group-chat))])]))

(def view (quo.theme/with-theme view-internal))
