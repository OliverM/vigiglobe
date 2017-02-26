(ns testvg.exercises
  "The Vigiglobe reagent test exercises."
  (:require [reagent.core :as r]))

(defn timer
  "The reagent timer example."
  []
  (let [seconds-since (r/atom 0)]
    (fn []
      (js/setTimeout #(swap! seconds-since inc) 1000)
      [:div "Seconds since starting: " @seconds-since])))

(defn test1 []
  [:div
   [timer]
   [:div [:a {:href "/"} "Go back to the home page."]]])

(defn test2 []
  [timer])

(defn test3 [])

(defn bonus [])
