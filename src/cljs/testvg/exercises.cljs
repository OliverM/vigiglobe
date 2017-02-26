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

(defn counter-gen
  "Instantiate a counter that can be shared among any number of views."
  []
  (let [seconds-since (r/atom 0)
        _ (js/setInterval #(swap! seconds-since inc) 1000)]
    seconds-since))

(defn counter-view
  "Given a seconds counter, return a view on that counter."
  [counter]
  [:div "Seconds since starting: " @counter])

(defn test2 []
  (let [counter (counter-gen)]
    [:div
     [ counter-view counter ]
     [ counter-view counter ]
     [:div [:a {:href "/"} "Go back to the home page."]]]))

(defn pausable-counter-gen
  []
  (let [seconds-since (r/atom 0)
        update (r/atom true)
        _ (js/setInterval #(when @update
                             (swap! seconds-since inc))
                          1000)]
    [seconds-since update]))

(defn pausable-counter-view
  "Given a pair of atoms containing a seconds counter and an update flag, return
  a view that displays the counter value and a control pausing that value."
  [[counter update]]
  [:div
   "Seconds since starting: " @counter
   [:button {:on-click #(swap! update not)}
    (if @update "Pause" "Resume")]])

(defn test3 []
  (let [pausable-counter (pausable-counter-gen)]
    [:div
     [pausable-counter-view pausable-counter]
     [:div [:a {:href "/"} "Go back to the home page."]]]))

(defn bonus [])
