(ns status-im.contexts.wallet.edit-account.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im.contexts.wallet.common.screen-base.create-or-edit-account.view
             :as create-or-edit-account]
            [status-im.contexts.wallet.common.sheets.network-preferences.view
             :as network-preferences]
            [status-im.contexts.wallet.edit-account.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- show-save-account-toast
  [updated-key]
  (let [message (case updated-key
                  :name                     :t/edit-wallet-account-name-updated-message
                  :color                    :t/edit-wallet-account-colour-updated-message
                  :emoji                    :t/edit-wallet-account-emoji-updated-message
                  :prod-preferred-chain-ids :t/edit-wallet-network-preferences-updated-message
                  nil)]
    (rf/dispatch [:toasts/upsert
                  {:id   :edit-account
                   :type :positive
                   :text (i18n/label message)}])))

(defn- save-account
  [{:keys [account updated-key new-value]}]
  (let [edited-account-data (assoc account updated-key new-value)]
    (rf/dispatch [:wallet/save-account
                  {:account    edited-account-data
                   :on-success #(show-save-account-toast updated-key)}])))

(def view
  (let [edited-account-name  (reagent/atom nil)
        show-confirm-button? (reagent/atom false)
        on-change-color      (fn [edited-color {:keys [color] :as account}]
                               (when (not= edited-color color)
                                 (save-account {:account     account
                                                :updated-key :color
                                                :new-value   edited-color})))
        on-change-emoji      (fn [edited-emoji {:keys [emoji] :as account}]
                               (when (not= edited-emoji emoji)
                                 (save-account {:account     account
                                                :updated-key :emoji
                                                :new-value   edited-emoji})))
        on-confirm-name      (fn [account]
                               (rn/dismiss-keyboard!)
                               (save-account {:account     account
                                              :updated-key :name
                                              :new-value   @edited-account-name}))]
    (fn []
      (let [{:keys [name emoji address color watch-only?]
             :as   account}  (rf/sub [:wallet/current-viewing-account])
            network-details  (rf/sub [:wallet/network-preference-details])
            account-name     (or @edited-account-name name)
            button-disabled? (or (nil? @edited-account-name)
                                 (= name @edited-account-name))]
        [create-or-edit-account/view
         {:page-nav-right-side [{:icon-name :i/delete
                                 :on-press  #(js/alert "Delete account: to be implemented")}]
          :account-name        account-name
          :account-emoji       emoji
          :account-color       color
          :on-change-name      #(reset! edited-account-name %)
          :on-change-color     #(on-change-color % account)
          :on-change-emoji     #(on-change-emoji % account)
          :section-label       :t/account-info
          :on-focus            #(reset! show-confirm-button? true)
          :on-blur             #(reset! show-confirm-button? false)
          :bottom-action?      @show-confirm-button?
          :bottom-action-label :t/update-account-name
          :bottom-action-props {:customization-color color
                                :disabled?           button-disabled?
                                :on-press            #(on-confirm-name account)}}
         [quo/data-item
          {:status          :default
           :size            :default
           :subtitle-type   :default
           :label           :none
           :blur?           false
           :icon-right?     true
           :right-icon      :i/advanced
           :card?           true
           :title           (i18n/label :t/address)
           :custom-subtitle (fn [] [quo/address-text
                                    {:networks network-details
                                     :address  address
                                     :format   :long}])
           :on-press        (fn []
                              (rf/dispatch [:show-bottom-sheet
                                            {:content
                                             (fn []
                                               [network-preferences/view
                                                {:on-save     (fn [chain-ids]
                                                                (rf/dispatch [:hide-bottom-sheet])
                                                                (save-account
                                                                 {:account     account
                                                                  :updated-key :prod-preferred-chain-ids
                                                                  :new-value   chain-ids}))
                                                 :watch-only? watch-only?}])}]))
           :container-style style/data-item}]]))))
