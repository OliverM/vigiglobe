(ns testvg.circletimechart
  (:require [reagent.core :as r]
            [cljsjs.d3 :as d3]
            [testvg.chartutil :as c]))

(def chartdata (r/atom nil))

;; 0) UI to set period under inspection
;; 1) get current period's data
;; 2) get comparison period's data
;; 3) render data
;; 4) render time to next refresh graphically? Or set refresh to automatic
;; one-minute window and show that approaching compared to current time?

(def chart-dim {:width 500 :height 500 :margin 30})
(assoc chart-dim
       :radius (- (.min js/Math (:width chart-dim)
                        (:height chart-dim))
                  (:margin chart-dim)))

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
    {:minute [min-msecs hour-msecs :minute
              "Compare incidence of the previous minute to the same minute an hour ago, in seconds."]
     :hour [hour-msecs day-msecs :hour
            "Compare incidence of the previous hour to the same hour yesterday, in minutes."]
     :day [day-msecs week-msecs :hour
           "Compare incidence of the previous day to the same day a week ago, in hours."]
     :week [week-msecs month-msecs :hour
            "Compare incidence of the previous week to the same week a month ago, in hours."]
     :month [month-msecs year-msecs :day
             "Compare incidence of the previous month to the same month a year ago, in days."]}))

(def appstate
  "The full application state."
  (r/atom
   {:current-data []
    :historical-data []
    :current-period :hour
    :data-refresh-seconds 0}))

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

(defn historical-dataline
  []
  (let [data (:historical-data @appstate)
        time-start (-> data first first)
        time-end (-> data last first)
        magnitudes (map second data)
        max-radius (:radius chart-dims)
        rscale (-> (.scaleLinear js/d3)
                   (.domain magnitudes) 
                   (.range (array 100 max-radius))) ;; 100 is arbitrary
        line (-> (.radialLine js/d3)
                 (.angle (fn [[timestamp _] _ _] timestamp))
                 (.radius (fn [[_ value] _ _] (rscale value))))

        ]
    [:g]))

(defn current-dataline
  []
  (let [data (:current-data @appstate)
        _ (.log js/console (clj->js data))
        ]
    [:g]))

(defn circletimechart
  "Generate the circular time chart."
  []
  (let [margin (:margin chart-dim)
        full-width (+ (* 2 margin) (:width chart-dim))
        full-height (+ (* 2 margin) (:height chart-dim))]
    [:svg {:viewBox (str "0 0 " full-width " " full-height)
           :width full-width}
     [:g {:transform (str "translate(" margin "," margin ")")}
      [historical-dataline]
      [current-dataline]
      ;; [overlay]
      ;; [:rect {:width (:width chart-dim)
      ;;         :height (:height chart-dim)
      ;;         :style {:fill "none" :pointer-events "all"}
      ;;         :on-mouse-over #(swap! overlay-metrics assoc :vis "block")
      ;;         :on-mouse-out #(swap! overlay-metrics assoc :vis "none")
      ;;         :on-mouse-move move-overlay}]
      ]]))

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
