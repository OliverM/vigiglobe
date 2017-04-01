(ns testvg.linechart
  "Implement the Vigiglobe chart exercises using the API at
  http://api.vigiglobe.com/"
  (:require [reagent.core :as r]
            [ajax.core :refer [GET]]
            [cljsjs.d3 :as d3]))

(def data-src
  "API root data-source URL."
  "http://api.vigiglobe.com/api/statistics/v1/volume")

(def project-id
  "Project ID string, used in the Vigiglobe API to indentify the dataset under
  interrogation."
  "vigiglobe-Earthquake")

(def linechart-data (r/atom nil))

(defn default-error-handler [{:keys [status status-text]}]
  "Generic failed API response handler."
  (.log js/console (str "API error: " status " " status-text)))

(defn linechart-data-received
  "Parse and store the supplied dataset."
  [response]
  (reset! linechart-data (map
                          (fn [[timestamp value]]
                            [(js/Date. timestamp) value])
                          (get-in response [:data "messages"]))))

(defn last-hour-timestamp
  "Generate an ISO timestamp one hour before the current system time."
  []
  (let [now (js/Date.)
        hour (dec (.getHours now))]
    (.setHours now hour)
    (.toISOString now)))

(defn minute-data
  "Get the data since the supplied timestamp, in minute granularity."
  [timestamp]
  (GET data-src {:params {:project_id project-id
                          :timeFrom timestamp
                          :granularity "minute"}
                 :response-format :transit
                 :handler linechart-data-received
                 :error-handler default-error-handler}))

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
          xscale (update-scale (array time-start time-end) :xscale)
          yscale (update-scale (.extent js/d3 (clj->js (map second data)))
                               :yscale)
          line (-> (.line js/d3)
                   (.x (fn [[timestamp _] _ _] (xscale timestamp)))
                   (.y (fn [[_ value] _ _] (yscale value))))
          path-data (line (clj->js data))]
      [:g [:path {:fill "none" :stroke "red" :d path-data}]])))

(defn line-chart
  "Generate a linechart using the supplied data."
  []
  (let [margin (:margin chart-dim)
        full-width (+ (* 2 margin) (:width chart-dim))
        full-height (+ (* 2 margin) (:height chart-dim))]
    [:svg {:viewBox (str "0 0 " full-width " " full-height)
           :width full-width}
     [:g [:line {:stroke "grey"
                 :x1 0
                 :x2 full-width
                 :y1 (/ full-height 2)
                 :y2 (/ full-height 2)}]]
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
  "Generate a button that, when pressed, launches a network request for fresh data."
  []
  [:button {:on-click #(minute-data (last-hour-timestamp))}
   "Update the chart with the last hour's data"])

(defn chart []
  [:div
   [:h3 "The line-chart"]
   [line-chart]
   [refresh-line-chart]
   [:div [:a {:href "/"} "Go back to the home page."]]])
