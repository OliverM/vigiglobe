(ns testvg.pie
  "Prep work for the C&AG exploded chart."
  (:require [reagent.core :as r]
            [cljsjs.d3 :as d3]
            [testvg.chartutil :as c]))


(def pie-data
  (r/atom [{:value 19168970 :caption "!"}
           {:value 12420480 :caption "!"}
           {:value 7351108 :caption "!"}
           {:value 5711486 :caption "!"}
           {:value 3212437 :caption "!"}
           {:value 1795212 :caption "!"}
           {:value 317900 :caption "!"}
           {:value 268802 :caption "!"}
           {:value 722469 :caption "!"}
           {:value 68661 :caption "!"}
           {:value 2143790 :caption "!"}
           {:value 1870559 :caption "!"}
           {:value 6534 :caption "!"}
           {:value 6838974 :caption "!"}
           {:value 2546000 :caption "!"}
           {:value 43985910 :caption "!"}
           {:value 3021219.25 :caption "!"}
           {:value 3021219.25 :caption "!"}
           {:value 3021219.25 :caption "!"}
           {:value 3021219.25 :caption "!"}
           {:value 1370000 :caption "!"}
           {:value 770000 :caption "!"}
           {:value 371465 :caption "!"}
           {:value 2022828 :caption "!"}
           {:value 711610 :caption "!"}
           ]))

(def chart-dim {:width 500 :height 500 :margin 30})

(defn chart
  ""
  []
  (let [margin (:margin chart-dim)
        radius (/ (:width chart-dim) 2)
        full-width (+ (* 2 margin) (:width chart-dim))
        full-height (+ (* 2 margin) (:height chart-dim))
        arcs ((-> (.pie js/d3)
                  (.sort nil)
                  (.value #(aget % "value")))
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
