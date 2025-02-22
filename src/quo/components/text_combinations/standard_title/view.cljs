(ns quo.components.text-combinations.standard-title.view
  (:require [clojure.string :as string]
            [quo.components.buttons.button.view :as button]
            [quo.components.markdown.text :as text]
            [quo.components.tags.tag :as tag]
            [quo.components.text-combinations.standard-title.style :as style]
            [quo.theme]
            [react-native.core :as rn]
            [utils.number]))

(defn- get-counter-number
  [n]
  (let [parsed-number (utils.number/parse-int n)]
    (if (< n 10)
      (str "0" parsed-number)
      parsed-number)))

(defn- right-counter
  [{:keys [blur? theme counter-left counter-right]}]
  [rn/view {:style style/right-counter}
   [text/text
    {:size   :paragraph-2
     :weight :regular
     :style  (style/right-counter-text blur? theme)}
    (str (get-counter-number counter-left)
         "/"
         (get-counter-number counter-right))]])

(defn- right-action
  [{:keys [customization-color on-press icon]
    :or   {icon :i/placeholder}}]
  [button/button
   {:accessibility-label :standard-title-action
    :size                32
    :icon-only?          true
    :customization-color customization-color
    :on-press            on-press}
   icon])

(defn- right-tag
  [{:keys [theme blur? on-press icon label]
    :or   {icon :i/placeholder}}]
  (let [labelled? (not (string/blank? label))]
    [tag/tag
     {:accessibility-label :standard-title-tag
      :size                32
      :type                :icon
      :resource            icon
      :on-press            on-press
      :labelled?           labelled?
      :label               (when labelled? label)
      :blurred?            blur?
      :icon-color          (style/right-tag-icon-color blur? theme)}]))

(defn- view-internal
  [{:keys [title right] :as props}]
  [rn/view {:style style/container}
   [text/text {:size :heading-1 :weight :semi-bold}
    title]
   (when right
     [rn/view {:style style/right-container}
      (case right
        :counter [right-counter props]
        :action  [right-action props]
        :tag     [right-tag props]
        nil)])])

(def view (quo.theme/with-theme view-internal))
