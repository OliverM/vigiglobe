(ns testvg.chartutil
  (:require [ajax.core :refer [GET]]))

(def data-src
  "API root data-source URL."
  "http://api.vigiglobe.com/api/statistics/v1/volume")

(def project-id
  "Project ID string, used in the Vigiglobe API to indentify the dataset under
  interrogation."
  "vigiglobe-Earthquake")

(defn default-error-handler [{:keys [status status-text]}]
  "Generic failed API response handler."
  (.log js/console (str "API error: " status " " status-text)))

(defn get-period-data
  "Given a timestamp, a period, and a granularity, use the timestamp as the
  start point of a set of data for the supplied duration and granularity,
  interrogate the Vigiglobe API with those parameters, and call the supplied
  handler when data is received. When the three-arity version (excluding the
  period) is called, all available data from the current timestamp is returned."
  ([timestamp granularity handler]
   (GET data-src {:params {:project_id project-id
                             :timeFrom timestamp
                             :granularity granularity}
                    :response-format :transit
                    :handler handler
                    :error-handler default-error-handler}))
  ([timestamp period granularity handler]
   (GET data-src {:params {:project_id project-id
                             :timeFrom timestamp
                             :timeTo period
                             :granularity granularity}
                    :response-format :transit
                    :handler handler
                    :error-handler default-error-handler})))

(defn last-period-timestamp
  "Generate an ISO timestamp one period before the current system time, where a
  period is specified in milliseconds."
  [millis]
  (-> (js/Date.) .getTime (- millis) js/Date. .toISOString))

(defn last-hour-timestamp
  "Generate an ISO timestamp one hour before the current system time."
  []
  (last-period-timestamp (* 1000 60 60)))
