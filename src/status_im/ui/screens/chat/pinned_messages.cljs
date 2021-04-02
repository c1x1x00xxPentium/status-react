(ns status-im.ui.screens.chat.pinned-messages
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.connectivity.view :as connectivity]
            [status-im.ui.components.react :as react]
            [quo.animated :as animated]
            [status-im.ui.screens.chat.styles.main :as style]
            [status-im.ui.screens.chat.components.accessory :as accessory]
            [status-im.utils.platform :as platform]
            [quo.react :as quo.react]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.screens.chat.views :as chat]
            [status-im.ui.components.list.views :as list]))

(defn pins-topbar []
  (let [{:keys [group-chat chat-id chat-name]}
        @(re-frame/subscribe [:chats/current-chat])
        pinned-messages @(re-frame/subscribe [:chats/pinned chat-id])
        [first-name _] (when-not group-chat @(re-frame.core/subscribe [:contacts/contact-two-names-by-identity chat-id]))]
    [topbar/topbar {:show-border? true
                    :title        (if group-chat chat-name first-name)
                    :subtitle     (if (= (count pinned-messages) 0)
                                    (i18n/label :t/no-pinned-messages)
                                    (i18n/label-pluralize (count pinned-messages) :t/pinned-messages-count))}]))

(defn get-space-keeper-ios [bottom-space panel-space active-panel text-input-ref]
  (fn [state]
    ;; NOTE: Only iOS now because we use soft input resize screen on android
    (when platform/ios?
      (cond
        (and state
             (< @bottom-space @panel-space)
             (not @active-panel))
        (reset! bottom-space @panel-space)

        (and (not state)
             (< @panel-space @bottom-space))
        (do
          (some-> ^js (quo.react/current-ref text-input-ref) .focus)
          (reset! panel-space @bottom-space)
          (reset! bottom-space 0))))))

(defn pinned-messages-empty []
  [react/view {:style {:flex 1
                       :align-items :center
                       :justify-content :center}}
   [react/text {:style style/intro-header-description}
    (i18n/label :t/pinned-messages-empty)]])

(defonce messages-list-ref (atom nil))

(def list-ref #(reset! messages-list-ref %))

(defn pinned-messages-view [{:keys [chat pan-responder space-keeper]}]
  (let [{:keys [group-chat chat-id public? community-id admins]} chat
        pinned-messages @(re-frame/subscribe [:chats/raw-chat-pin-messages-stream chat-id])]
    (if (= (count pinned-messages) 0)
      [pinned-messages-empty]
      ;;do not use anonymous functions for handlers
      [list/flat-list
       (merge
        pan-responder
        {:key-fn                  chat/list-key-fn
         :ref                     list-ref
         :data                    (reverse pinned-messages)
         :render-data             (chat/get-render-data {:group-chat      group-chat
                                                         :chat-id         chat-id
                                                         :public?         public?
                                                         :community-id    community-id
                                                         :admins          admins
                                                         :space-keeper    space-keeper
                                                         :show-input?     false
                                                         :edit-enabled    false
                                                         :in-pinned-view? true})
         :render-fn               chat/render-fn
         :content-container-style {:padding-top 16
                                   :padding-bottom 16}})])))

(defn pinned-messages []
  (let [bottom-space (reagent/atom 0)
        panel-space (reagent/atom 52)
        active-panel (reagent/atom nil)
        position-y (animated/value 0)
        pan-state (animated/value 0)
        text-input-ref (quo.react/create-ref)
        pan-responder (accessory/create-pan-responder position-y pan-state)
        space-keeper (get-space-keeper-ios bottom-space panel-space active-panel text-input-ref)
        chat @(re-frame/subscribe [:chats/current-chat-chat-view])]
    [:<>
     [pins-topbar]
     [connectivity/loading-indicator]
     [pinned-messages-view {:chat          chat
                            :pan-responder pan-responder
                            :space-keeper  space-keeper}]]))