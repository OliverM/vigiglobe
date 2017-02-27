(ns testvg.vigiglobe
  "Implement the Vigiglobe chart exercises using the API at
  http://api.vigiglobe.com/"
  (:require [reagent.core :as r]))

(defn chart []
  [:div
   [:h3 "The test chart"]
   [:div [:a {:href "/"} "Go back to the home page."]]])
