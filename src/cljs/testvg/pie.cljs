(ns testvg.pie
  "Prep work for the C&AG exploded chart."
  (:require [reagent.core :as r]
            [cljsjs.d3 :as d3]
            [testvg.chartutil :as c]))


(def old-pie-data
  (r/atom [{:value 19168970 :caption "Income Tax"}
           {:value 12420480 :caption "VAT"}
           {:value 7351108 :caption "Corporation Tax"}
           {:value 5711486 :caption "Excise Duty"}
           {:value 3212437 :caption "Other tax revenue"}
           {:value 1795212 :caption "Central Bank surplus"}
           {:value 317900 :caption "Receipts from local government fund"}
           {:value 268802 :caption "Dividends"}
           {:value 722469 :caption "Other non-tax current revenue"}
           {:value 68661 :caption "EU receipts"}
           {:value 2143790 :caption "Loan repayment"}
           {:value 1870559 :caption "Banking stabilisation measure"}
           {:value 6534 :caption "Other capital receipts"}
           {:value 6838974 :caption "Servicing of national debt"}
           {:value 2546000 :caption "Net borrowing/(repayment) of debt"}
           {:value 43985910 :caption "Voted expenditure"}
           {:value 3021219.25 :caption "Social Insurance Fund (SIF)"}
           {:value 3021219.25 :caption "Local Government Fund (LGF)"}
           {:value 3021219.25 :caption "European Agricultural Guidance and Guarantee Fund (EAGGF)"}
           {:value 3021219.25 :caption "Other Departmental expenditure and Departmental funds"}
           {:value 1370000 :caption "Loan to Social Insurance Fund"}
           {:value 770000 :caption "EAGGF guarantee agriculture"}
           {:value 371465 :caption "Other capital expenditure"}
           {:value 2022828 :caption "Contribution to EU budget"}
           {:value 711610 :caption "Other non-voted current expenditure"}
           ]))

(def pie-data
  (r/atom [
           {:value 2500000 :caption "Capital expenditure"}
           {:value 700000 :caption "Other non-voted current expenditure"}
           {:value 2000000 :caption "Contribution to EU budget"}
           {:value 43985910 :caption "Voted expenditure"}

           {:value 6800000 :caption "Servicing of national debt"}
           {:value 1000000 :caption "Deficit for the year"}

           {:value 1900000 :caption "Banking stabilisation measure"}
           {:value 2100000 :caption "Loan repayment and other capital receipts"}
           {:value 68000 :caption "EU receipts"}
           {:value 1300000 :caption "Other non-tax current revenue (including Dividends and Receipts from Local Government)"}
           {:value 1800000 :caption "Central Bank surplus"}
           {:value 2700000 :caption "Other tax revenue"}
           {:value 5700000 :caption "Excise Duty"}
           {:value 7400000 :caption "Corporation Tax"}
           {:value 12400000 :caption "VAT"}
           {:value 19200000 :caption "Income Tax"}
           ]))

(def chart-dim {:width 500 :height 500 :margin 30})

(defn chart
  ""
  []
  (let [margin (:margin chart-dim)
        radius (/ (:width chart-dim) 2)
        full-width (+ (* 2 margin) (:width chart-dim))
        full-height (+ (* 2 margin) (:height chart-dim))
        data @pie-data
        arcs ((-> (.pie js/d3)
                  (.sort nil)
                  (.value #(aget % "value")))
              (clj->js data))
        arcfn (-> (.arc js/d3)
                  (.outerRadius radius)
                  (.innerRadius 100))
        ]
    [:svg {:viewBox (str "0 0 " full-width " " full-height)
           :width full-width}
     [:g 
      (map-indexed
       (fn [index segment]
         ^{:key index}
         [:g {:transform (str "translate(" (/ full-height 2) "," (/ full-height 2) ")")}
          [:path {:fill  "green" :stroke "white" :d (arcfn segment)}]
          (let [coords (.centroid arcfn segment)] 
            [:text {:fill "blue" :x (aget coords 0) :y (aget coords 1)}
             (:caption (nth data index))])])
       arcs)]]))
