(ns testvg.circletimechart
  (:require [reagent.core :as r]
            [cljsjs.d3 :as d3]
            [testvg.chartutil :as c]))

(def chartdata (r/atom nil))

(def base-chart-dim {:width 500 :height 500 :margin 30 :inner-radius 75})
(def chart-dim (-> base-chart-dim
                   (assoc :outer-radius (-> (.min js/Math (:width base-chart-dim)
                                            (:height base-chart-dim))
                                      (- (:margin base-chart-dim))
                                      (/ 2)))))

(def period-matchings
  "A map from period durations to quads of period durations in
  seconds,comparison period durations, granularities, and English captions.
  Takes no account of irregular month lengths (here defined as four weeks, e.g.
  28 days) or leap years (all years are 365 days)."
  (let [min-msecs (* 1000 60)
        hour-msecs (* min-msecs 60)
        day-msecs (* hour-msecs 24)
        week-msecs (* day-msecs 7)
        month-msecs (* week-msecs 4)
        year-msecs (* day-msecs 365)]
    {:minute [min-msecs hour-msecs :second
              "Compare relative intensity of the previous minute to the same minute an hour ago, in seconds."]
     :hour [hour-msecs day-msecs :minute
            "Compare relative intensity of the previous hour to the same hour yesterday, in minutes."]
     :day [day-msecs week-msecs :hour
           "Compare relative intensity of the previous day to the same day a week ago, in hours."]
     :week [week-msecs month-msecs :hour
            "Compare relative intensity of the previous week to the same week a month ago, in hours."]
     :month [month-msecs year-msecs :day
             "Compare relative intensity of the previous month to the same month a year ago, in days."]}))

(def appstate
  "The full application state."
  (r/atom
   {:current-data []
    :historical-data []
    :current-period :hour
    :ascale (-> (.scaleTime js/d3) (.range (array 0 (* 2 (.-PI js/Math)))))
    :rscale (-> (.scaleLinear js/d3) (.range (array (:inner-radius chart-dim)
                                                    (:outer-radius chart-dim))))}))

(defn update-scale
  "Adjust the selected scale to reflect the new domain of data values recieved."
  [new-domain scale-kw]
  (let [scale (.domain (scale-kw @appstate) new-domain)]
    (swap! appstate assoc scale-kw scale)
    scale))

(defn gen-data-handler
  "Generate a data-handler function to update the correct part of the state
  map."
  [state-map-key]
  (fn [response]
    (swap! appstate assoc state-map-key
           (map
            (fn [[timestamp value]]
              [(js/Date. timestamp) value])
            (get-in response [:data "messages"])))))

(defn refresh
  "Update the chart data based on the provided period."
  [period]
  (let [[msecs comparison granularity _] (period period-matchings)]
    (c/get-period-data (c/last-period-timestamp msecs)
                       (name granularity)
                       (gen-data-handler :current-data))
    (c/get-period-data (c/last-period-timestamp comparison)
                       (c/last-period-timestamp (- comparison msecs))
                       (name granularity)
                       (gen-data-handler :historical-data))))

(defn select-change-handler
  "Update the model when a new period is selected by the user."
  [e]
  (let [new-period (keyword (.. e -target -value))]
    (refresh new-period)
    (swap! appstate assoc :current-period new-period)))

(defn grid
  []
  (let [_ @appstate
        inner-r (:inner-radius chart-dim)
        outer-r (:outer-radius chart-dim)]
    [:g
     [:circle {:style {:fill "whitesmoke" :stroke "silver"} :cx 0 :cy 0
               :r outer-r}]
     [:circle {:style {:fill "white" :stroke "silver"} :cx 0 :cy 0
               :r inner-r}]
     [:line {:style {:stroke "silver"}
             :x1 0 :x2 0
             :y1 (- inner-r) :y2 (- outer-r)}]
     [:text {:text-anchor "middle" :y (- (- outer-r) 10)} "Start"]
     [:text {:x (- inner-r 10) :text-anchor "end"} "0"]
     [:text {:x (+ outer-r 10) :text-anchor "start"} "Max"]
     [:g {:transform (str "translate(0," (+ outer-r 25) ")")}
      [:g
       [:text {:text-anchor "end"} "Current"]
       [:line {:stroke "red" :x1 10 :x2 40 :y1 -6 :y2 -6}]]
      [:g {:transform "translate(0, 20)"}
       [:text {:text-anchor "end"} "Historical"]
       [:line {:stroke "grey" :x1 10 :x2 40 :y1 -6 :y2 -6}]]]]))

(defn dataline
  "Given a data key and a colour, look up the data under that key in the
  appstate and draw a line using that data, coloured by the line-colour."
  [data-key line-colour]
  (let [data (data-key @appstate)
        time-start (-> data first first)
        time-end (-> data last first)
        magnitudes (map second data)
        rscale (update-scale (.extent js/d3 (clj->js magnitudes)) :rscale)
        ascale (update-scale (array time-start time-end) :ascale)
        line (-> (.radialLine js/d3)
                 (.angle (fn [[timestamp _] _ _] (ascale timestamp)))
                 (.radius (fn [[_ value] _ _] (rscale value))))
        path-data (line (clj->js data))]
    [:g [:path {:fill "none" :stroke line-colour :d path-data}]]))

(defn historical-dataline [] (dataline :historical-data "grey"))
(defn current-dataline [] (dataline :current-data "red"))

(defn circletimechart
  "Generate the circular time chart."
  []
  (let [margin (:margin chart-dim)
        full-width (+ (* 2 margin) (:width chart-dim))
        full-height (+ (* 2 margin) (:height chart-dim))]
    [:svg {:viewBox (str "0 0 " full-width " " full-height) :width full-width}
     [:g {:transform (str "translate(" (/ full-width 2)
                          "," (/ full-height 2) ")")}
      [grid]
      [historical-dataline]
      [current-dataline]]]))

(defn controls
  "A UI to control the dataset being visualised."
  []
  (let [current-period (:current-period @appstate)]
    [:select {:value current-period
              :on-change select-change-handler}
     (for [[key [_ _ _ caption]] (seq period-matchings)]
       [:option {:key key :value key} caption])]))

(defn chart []
  [:div
   [:h3 "The custom chart"]
   [circletimechart]
   [controls]
   [:div [:a {:href "/"} "Go back to the home page."]]])

(refresh (:current-period @appstate))
