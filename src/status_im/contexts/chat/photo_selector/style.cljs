(ns status-im.contexts.chat.photo-selector.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.platform :as platform]))

(defn gradient-container
  [bottom-inset]
  {:left     0
   :right    0
   :height   (+ bottom-inset (if platform/ios? 65 85))
   :position :absolute
   :bottom   0})

(def buttons-container
  {:position        :absolute
   :flex-direction  :row
   :left            0
   :right           0
   :top             20
   :justify-content :center
   :z-index         1})

(def clear-container
  {:position :absolute
   :right    20})

(defn close-button-container
  []
  {:background-color (colors/theme-colors colors/neutral-10 colors/neutral-80)
   :width            32
   :height           32
   :border-radius    10
   :justify-content  :center
   :align-items      :center
   :position         :absolute
   :left             20})

(defn title-container
  []
  {:flex-direction     :row
   :background-color   (colors/theme-colors colors/neutral-10 colors/neutral-80)
   :border-radius      10
   :padding-horizontal 12
   :padding-vertical   5
   :align-self         :center})

(defn chevron-container
  []
  {:background-color (colors/theme-colors colors/neutral-30 colors/neutral-100)
   :width            14
   :height           14
   :border-radius    7
   :justify-content  :center
   :align-items      :center
   :margin-left      7
   :margin-top       4})

(defn image
  [window-width index]
  {:width                   (- (/ window-width 3) 0.67)
   :height                  (/ window-width 3)
   :margin-left             (when (not= (mod index 3) 0) 1)
   :margin-bottom           1
   :border-top-left-radius  (when (= index 0) 20)
   :border-top-right-radius (when (= index 2) 20)})

(defn overlay
  [window-width]
  {:position         :absolute
   :width            (- (/ window-width 3) 0.67)
   :height           (/ window-width 3)
   :background-color (colors/theme-colors colors/white-opa-40 colors/neutral-80-opa-40)})

(def image-count
  {:position :absolute
   :top      8
   :right    8})
