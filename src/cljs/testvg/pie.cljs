(ns testvg.pie
  "Prep work for the C&AG exploded chart."
  (:require [reagent.core :as r]
            [cljsjs.d3 :as d3]
            [testvg.chartutil :as c]))


(def pie-data
  (r/atom
   [19168970 12420480 7351108 5711486 3212437 1795212 317900 268802 722469 68661
   2143790 1870559 6534 6838974 2546000 43985910 3021219.25 3021219.25
   3021219.25 3021219.25 1370000 770000 371465 2022828 711610]))

(def chart-dim {:width 500 :height 500 :margin 30})

(defn chart
  ""
  []
  (let [margin (:margin chart-dim)
        radius (/ (:width chart-dim) 2)
        full-width (+ (* 2 margin) (:width chart-dim))
        full-height (+ (* 2 margin) (:height chart-dim))
        arcs ((-> (.pie js/d3)
                  (.sort nil))
              (clj->js @pie-data))
        arcfn (-> (.arc js/d3)
                  (.outerRadius radius)
                  (.innerRadius 100))
        ]
    [:svg {:viewBox (str "0 0 " full-width " " full-height)
           :width full-width}
     [:g {:transform (str "translate(" (/ full-height 2) "," (/ full-height 2) ")")}
      (map-indexed
       (fn [index segment]
         ^{:key index}
         [:path {:fill "green" :stroke "white" :d (arcfn segment) }])
       arcs)]]))
