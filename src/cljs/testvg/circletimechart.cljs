(ns testvg.circletimechart
  (:require [reagent.core :as r]
            [testvg.linechart :as lc]
            [cljsjs.d3 :as d3]))

(def chartdata (r/atom nil))

(defn chart []
  [:div
   [:h3 "The custom chart"]
   [:div [:a {:href "/"} "Go back to the home page."]]])
