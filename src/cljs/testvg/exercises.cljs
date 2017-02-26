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

(def shared-timer
  "A namespace-level atom shared between multiple components. Should really be
  managed by Component or Mount or Integrant or a similar state management
  library, but implemented as a bare atom for the purposes of this exercise."
  (r/atom 0))

;; start the shared timer
(js/setInterval #(swap! shared-timer inc) 1000)

(defn shared-timer-view
  "Return a component displaying the current shared-timer value."
  []
  [:div "Seconds since starting: " @shared-timer])

(defn test2 []
  [:div
   [shared-timer-view]
   [shared-timer-view]])

(defn test3 [])

(defn bonus [])
