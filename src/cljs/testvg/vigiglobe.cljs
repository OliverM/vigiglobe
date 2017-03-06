(ns testvg.vigiglobe
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

(def chart-width 500)
(def chart-height 400)

(defn default-handler [response]
  "Generic successful API response handler."
  (.log js/console (str response)))

(defn default-error-handler [{:keys [status status-text]}]
  "Generic failed API response handler."
  (.log js/console (str "API error: " status " " status-text)))

(defn data-received-handler
  [response]
  (let [data (get-in response [:data "messages"])
        time-start (->> data first first (.parse js/Date))
        time-end (->> data last first (.parse js/Date))
        xscale (-> (.scaleTime js/d3)
                   (.domain (array time-start time-end))
                   (.range (array 0 chart-width)))]
    (.log js/console (xscale time-end))
    ))

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
                 :handler data-received-handler
                 :error-handler default-error-handler}))


(defn chart []
  [:div
   [:h3 "The test chart"]
   [:div [:a {:href "/"} "Go back to the home page."]]])
