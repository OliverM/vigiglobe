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

(def period-matchings
  "A map from period durations to quads of period durations in seconds,
  comparison period differences, granularities, and English captions."
  {:minute [60 :hour :minute "Compare incidence of the previous minute to the same minute an hour ago, in seconds."]
   :hour [(* 60 60) :day :hour "Compare incidence of the previous hour to the same hour yesterday, in minutes."]
   :day [(* 60 60 24) :week :hour "Compare incidence of the previous day to the same day a week ago, in hours."]
   :week [(* 60 60 24 7) :month :hour "Compare incidence of the previous week to the same week a month ago, in hours."]
   :month [(* 60 60 24 7 30) :year :day "Compare incidence of the previous month to the same month a year ago, in days."]})

(def appstate
  "The full application state."
  (r/atom
   {:current-data []
    :current-period :hour
    :data-refresh-seconds 0}))

(defn select-change-handler
  "Update the model when a new period is selected by the user."
  [e]
  (let [new-period (keyword (.. e -target -value))]
    (swap! appstate assoc :current-period new-period)))

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
   [controls]
   [:div [:a {:href "/"} "Go back to the home page."]]])
