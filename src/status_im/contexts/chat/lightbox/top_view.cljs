(ns status-im.contexts.chat.lightbox.top-view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.orientation :as orientation]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [status-im.contexts.chat.lightbox.animations :as anim]
    [status-im.contexts.chat.lightbox.constants :as c]
    [status-im.contexts.chat.lightbox.style :as style]
    [utils.datetime :as datetime]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.url :as url]))

(defn animate-rotation
  [result screen-width screen-height insets
   {:keys [rotate top-view-y top-view-x top-view-width top-view-bg]}]
  (let [top-x (+ (/ c/top-view-height 2) (:top insets))]
    (cond
      (= result orientation/landscape-left)
      (do
        (anim/animate rotate "90deg")
        (anim/animate top-view-y 60)
        (anim/animate top-view-x (- (/ screen-height 2) top-x))
        (anim/animate top-view-width screen-height)
        (anim/animate top-view-bg colors/neutral-100-opa-70))
      (= result orientation/landscape-right)
      (do
        (anim/animate rotate "-90deg")
        (anim/animate top-view-y (- (- screen-width) 4))
        (anim/animate top-view-x (+ (/ screen-height -2) top-x))
        (anim/animate top-view-width screen-height)
        (anim/animate top-view-bg colors/neutral-100-opa-70))
      (= result orientation/portrait)
      (do
        (anim/animate rotate "0deg")
        (anim/animate top-view-y 0)
        (anim/animate top-view-x 0)
        (anim/animate top-view-width screen-width)
        (anim/animate top-view-bg colors/neutral-100-opa-0)))))

(defn drawer
  [messages index]
  (let [{:keys [content]} (nth messages index)
        uri               (url/replace-port (:image content) (rf/sub [:mediaserver/port]))]
    [quo/action-drawer
     [[{:icon                :i/save
        :accessibility-label :save-image
        :label               (i18n/label :t/save-image-library)
        :on-press            (fn []
                               (rf/dispatch [:hide-bottom-sheet])
                               (rf/dispatch
                                [:chat.ui/save-image-to-gallery
                                 uri
                                 #(rf/dispatch [:toasts/upsert
                                                {:id              :random-id
                                                 :type            :positive
                                                 :container-style {:bottom (when platform/android? 20)}
                                                 :text            (i18n/label :t/photo-saved)}])]))}]]]))

(defn share-image
  [messages index]
  (let [{:keys [content]} (nth messages index)
        uri               (url/replace-port (:image content) (rf/sub [:mediaserver/port]))]
    (rf/dispatch [:chat.ui/share-image uri])))

(defn top-view
  [messages insets index animations derived landscape? screen-width]
  (let [{:keys [from timestamp]}  (first messages)
        [primary-name _]          (rf/sub [:contacts/contact-two-names-by-identity from])
        bg-color                  (if landscape?
                                    colors/neutral-100-opa-70
                                    colors/neutral-100-opa-0)
        {:keys [background-color opacity
                overlay-opacity]} animations]
    [reanimated/view
     {:style
      (style/top-view-container (:top insets) screen-width bg-color landscape? animations derived)}
     [reanimated/linear-gradient
      {:colors [(colors/alpha "#000000" 0.8) :transparent]
       :start  {:x 0 :y 0}
       :end    {:x 0 :y 1}
       :style  (style/top-gradient insets)}]
     [rn/view
      {:style {:flex-direction :row
               :align-items    :center}}
      [rn/touchable-opacity
       {:on-press (fn []
                    (anim/animate background-color :transparent)
                    (anim/animate opacity 0)
                    (anim/animate overlay-opacity 0)
                    (rf/dispatch (if platform/ios?
                                   [:chat.ui/exit-lightbox-signal @index]
                                   [:navigate-back])))
        :style    style/close-container}
       [quo/icon :close {:size 20 :color colors/white}]]
      [rn/view {:style {:margin-left 12}}
       [quo/text
        {:weight :semi-bold
         :size   :paragraph-1
         :style  {:color colors/white}} primary-name]
       [quo/text
        {:weight :medium
         :size   :paragraph-2
         :style  {:color colors/neutral-40}} (when timestamp (datetime/to-short-str timestamp))]]]
     [rn/view {:style style/top-right-buttons}
      [rn/touchable-opacity
       {:active-opacity      1
        :accessibility-label :share-image
        :on-press            #(share-image messages @index)
        :style               (merge style/close-container {:margin-right 12})}
       [quo/icon :share {:size 20 :color colors/white}]]
      [rn/touchable-opacity
       {:active-opacity      1
        :accessibility-label :image-options
        :on-press            #(rf/dispatch [:show-bottom-sheet
                                            {:content (fn [] [drawer messages @index])}])
        :style               style/close-container}
       [quo/icon :options {:size 20 :color colors/white}]]]]))
