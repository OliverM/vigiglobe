(ns testvg.linechart
  "Implement the Vigiglobe chart exercises using the API at
  http://api.vigiglobe.com/"
  (:require [reagent.core :as r]
            [ajax.core :refer [GET]]
            [cljsjs.d3 :as d3]
            [testvg.chartutil :as c]))

(def linechart-data (r/atom nil))

(defn linechart-data-received
  "Parse and store the supplied dataset."
  [response]
  (reset! linechart-data (map
                          (fn [[timestamp value]]
                            [(js/Date. timestamp) value])
                          (get-in response [:data "messages"]))))

(defn minute-data
  "Get the data since the supplied timestamp, in minute granularity."
  [timestamp]
  (GET c/data-src {:params {:project_id c/project-id
                          :timeFrom timestamp
                          :granularity "minute"}
                 :response-format :transit
                 :handler linechart-data-received
                 :error-handler c/default-error-handler}))

(def chart-dim {:width 500 :height 300 :margin 30})

(def chart-fns
  "A map of functions shared between the chart rendering points (e.g. the
  positioning of the overlay and the drawing of the chart data line)."
  (atom {:xscale (-> (.scaleTime js/d3)
                     (.range (array 0 (:width chart-dim))))
         :yscale (-> (.scaleLinear js/d3)
                     (.range (array (:height chart-dim) 0)))
         :xbisect (.-left (.bisector js/d3 first))}))

(defn update-scale
  "Adjust the selected scale to reflect the new domain of data values recieved."
  [new-domain scale-kw]
  (let [scale (.domain (scale-kw @chart-fns) new-domain)]
    (swap! chart-fns assoc scale-kw scale)
    scale))

(def overlay-metrics (r/atom {:x 0 :y 0 :vis "none" :caption ""}))

(defn overlay
  "A small detail view revealing the underlaying x-value on mouse-over."
  []
  (let [{:keys [vis caption x y]} @overlay-metrics]
    [:g#focus {:style {:display vis}
               :transform (str "translate(" x "," y ")")}
     [:circle {:style {:fill "none" :stroke "steelblue"} :r "4.5"}]
     [:text {:x "9" :dy "0.35em"} caption]]))

(defn closest-datapoint
  "Given a test-date, a position and the data points to the left and right of
  that position in an array, determine which of the datapoints is the closest to
  the test-date and return that datapoint. Here data points are pairs of dates
  and values."
  [test-date posn data]
  (let [d0 (nth data (dec posn))
        d1 (nth data posn)
        d0-diff (- (first d0) test-date)
        d1-diff (- test-date (first d1))]
    (if (> d0-diff d1-diff) d0 d1)))

(defn move-overlay
  "Update the overlay metrics with the mouse position when the mouse moves over
  the linechart."
  [e]
  (let [clientRect (-> e .-currentTarget .getBoundingClientRect)
        newX (- (.-pageX e) (.-left clientRect))
        newY (- (.-pageY e) (.-top clientRect))
        dataX (-> @chart-fns :xscale (.invert newX))
        xbisect (:xbisect @chart-fns)
        posn (xbisect (clj->js @linechart-data) dataX 1)
        datum (closest-datapoint dataX posn @linechart-data)]
    (swap! overlay-metrics assoc
           :x ((:xscale @chart-fns) (first datum))
           :y ((:yscale @chart-fns) (second datum))
           :caption (second datum))))

(defn dataline
  "The data line in the line chart."
  []
  (when-let [data @linechart-data]
    (let [time-start (-> data first first)
          time-end (-> data last first)
          magnitudes (map second data)
          xscale (update-scale (array time-start time-end) :xscale)
          yscale (update-scale (.extent js/d3 (clj->js magnitudes))
                               :yscale)
          av-mag (/ (reduce + magnitudes) (count magnitudes))
          scaled-av-mag (yscale av-mag)
          line (-> (.line js/d3)
                   (.x (fn [[timestamp _] _ _] (xscale timestamp)))
                   (.y (fn [[_ value] _ _] (yscale value))))
          path-data (line (clj->js data))]
      [:g [:line {:stroke "grey"
                  :x1 0
                  :x2 (:width chart-dim)
                  :y1 scaled-av-mag
                  :y2 scaled-av-mag}]
       [:text {:y (- scaled-av-mag 7) :font-size 12}
        (str "Average magnitude: " (.round js/Math av-mag))]
       [:path {:fill "none" :stroke "red" :d path-data}]])))

(defn line-chart
  "Generate a linechart using the supplied data."
  []
  (let [margin (:margin chart-dim)
        full-width (+ (* 2 margin) (:width chart-dim))
        full-height (+ (* 2 margin) (:height chart-dim))]
    [:svg {:viewBox (str "0 0 " full-width " " full-height)
           :width full-width}
     [:g {:transform (str "translate(" margin "," margin ")")}
      [dataline]
      [overlay]
      [:rect {:width (:width chart-dim)
              :height (:height chart-dim)
              :style {:fill "none" :pointer-events "all"}
              :on-mouse-over #(swap! overlay-metrics assoc :vis "block")
              :on-mouse-out #(swap! overlay-metrics assoc :vis "none")
              :on-mouse-move move-overlay}]]]))

(defn refresh-line-chart
  "Automatically refresh the linechart with data every 60 seconds, and provide a
  button that, when pressed, refreshes the data immediately, resetting the next
  refresh to 60 seconds. The button caption displays the time remaining until
  the next refresh."
  []
  (let [seconds-until (r/atom 0) ;; starting at zero triggers immediate update
        refresh #(do (reset! seconds-until 60)
                     (minute-data (c/last-hour-timestamp)))]
    (js/setInterval #(swap! seconds-until dec) 1000) ;; capture return to cleanup
    (fn []
      (when (<= @seconds-until 0) (refresh))
      [:button {:on-click refresh}
       (str "Automatic refresh in "
            @seconds-until
            " seconds, or click to update the chart with the last hour's data")])))

(defn chart []
  [:div
   [:h3 "The line-chart"]
   [line-chart]
   [refresh-line-chart]
   [:div [:a {:href "/"} "Go back to the home page."]]])
