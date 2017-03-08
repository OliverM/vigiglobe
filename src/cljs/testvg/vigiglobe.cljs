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

(def chart-dim {:width 500
                :height 400})

(defn line-chart
  "Generate a linechart using the supplied data."
  [data]
  (let [data (map (fn [[timestamp value]]
                    [(.parse js/Date timestamp) value])
                  data)
        time-start (-> data first first)
        time-end (-> data last first)
        xscale (-> (.scaleTime js/d3)
                   (.domain (array time-start time-end))
                   (.range (array 0 (:width chart-dim))))
        v-extent (.extent js/d3 (clj->js (map second data)))
        yscale (-> (.scaleLinear js/d3)
                   (.domain v-extent)
                   (.range (array (:height chart-dim) 0)))
        line (-> (.line js/d3)
                 (.x (fn [[timestamp _] _ _] (xscale timestamp)))
                 (.y (fn [[_ value] _ _] (yscale value))))]
    (.log js/console (line (clj->js data)))
    ))

(defn chart []
  [:div
   [:h3 "The test chart"]
   [:div [:a {:href "/"} "Go back to the home page."]]])

(defn default-handler [response]
  "Generic successful API response handler."
  (.log js/console (str response)))

(defn default-error-handler [{:keys [status status-text]}]
  "Generic failed API response handler."
  (.log js/console (str "API error: " status " " status-text)))

(defn linechart-data-received
  [response]
  (line-chart (get-in response [:data "messages"])))

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
