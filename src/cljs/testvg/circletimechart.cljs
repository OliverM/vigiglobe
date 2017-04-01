(ns testvg.circletimechart
  (:require [reagent.core :as r]
            [cljsjs.d3 :as d3]
            [testvg.chartutil :as c]))

(def chartdata (r/atom nil))

(defn chart []
  [:div
   [:h3 "The custom chart"]
   [:div [:a {:href "/"} "Go back to the home page."]]])

