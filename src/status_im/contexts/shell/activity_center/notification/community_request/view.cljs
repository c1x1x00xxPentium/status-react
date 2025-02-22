(ns status-im.contexts.shell.activity-center.notification.community-request.view
  (:require
    [quo.core :as quo]
    [react-native.gesture :as gesture]
    [status-im.constants :as constants]
    [status-im.contexts.shell.activity-center.notification.common.style :as common-style]
    [status-im.contexts.shell.activity-center.notification.common.view :as common]
    [utils.datetime :as datetime]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- swipeable
  [{:keys [active-swipeable extra-fn]} child]
  [common/swipeable
   {:left-button      common/swipe-button-read-or-unread
    :left-on-press    common/swipe-on-press-toggle-read
    :right-button     common/swipe-button-delete
    :right-on-press   common/swipe-on-press-delete
    :active-swipeable active-swipeable
    :extra-fn         extra-fn}
   child])

(defn- get-header-text-and-context
  [community membership-status]
  (let [community-name        (:name community)
        permissions           (:permissions community)
        open?                 (not= 3 (:access permissions))
        community-image       (get-in community [:images :thumbnail :uri])
        community-context-tag [quo/context-tag
                               {:type           :community
                                :size           24
                                :blur?          true
                                :community-logo community-image
                                :community-name community-name}]]
    (cond
      (= membership-status constants/activity-center-membership-status-idle)
      {:header-text (i18n/label :t/community-request-not-accepted)
       :context     [[quo/text {:style common-style/user-avatar-tag-text}
                      (i18n/label :t/community-request-not-accepted-body-text-prefix)]
                     community-context-tag
                     [quo/text {:style common-style/user-avatar-tag-text}
                      (i18n/label :t/community-request-not-accepted-body-text-suffix)]]}

      (= membership-status constants/activity-center-membership-status-pending)
      {:header-text (i18n/label :t/community-request-pending)
       :context     [[quo/text {:style common-style/user-avatar-tag-text}
                      (i18n/label :t/community-request-pending-body-text)]
                     community-context-tag]}

      (= membership-status constants/activity-center-membership-status-accepted)
      {:header-text (i18n/label (if open?
                                  :t/join-open-community
                                  :t/community-request-accepted))
       :context     [[quo/text {:style common-style/user-avatar-tag-text}
                      (i18n/label (if open?
                                    :t/joined-community
                                    :t/community-request-accepted-body-text)
                                  (when open? {:community community-name}))]
                     community-context-tag]}

      :else nil)))

(defn view
  [{:keys [notification set-swipeable-height customization-color] :as props}]
  (let [{:keys [community-id membership-status read
                timestamp]}           notification
        community                     (rf/sub [:communities/community community-id])
        {:keys [header-text context]} (get-header-text-and-context community
                                                                   membership-status)]
    [swipeable props
     [gesture/touchable-without-feedback
      {:on-press (fn []
                   (rf/dispatch [:navigate-back])
                   (rf/dispatch [:navigate-to :community-overview community-id]))}
      [quo/activity-log
       {:title               header-text
        :customization-color customization-color
        :icon                :i/communities
        :on-layout           set-swipeable-height
        :timestamp           (datetime/timestamp->relative timestamp)
        :unread?             (not read)
        :context             context}]]]))
