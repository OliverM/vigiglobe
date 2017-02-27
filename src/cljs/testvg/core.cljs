(ns testvg.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [testvg.exercises :as ex]
              [testvg.vigiglobe :as vg]))

;; -------------------------
;; Views

(defn home-page []
  [:div
   [:h3 "The Reagent test pages."]
   [:div [:a {:href "/test1"} "Go to the first test page."]]
   [:div [:a {:href "/test2"} "Go to the second test page."]]
   [:div [:a {:href "/test3"} "Go to the third test page."]]
   [:div [:a {:href "/bonus"} "Go to the bonus test page."]]
   [:h3 "The Vigiglobe chart test."]
   [:div [:a {:href "/chart"} "Go to the chart page."]]
   [:h3 "Notes"]
   [:div [:a {:href "/about"} "Notes about the experience."]]])

(defn about-page []
  [:div [:h2 "About the experience"]
   [:h3 "On the Reagent tests"]
   [:p "It was interesting to use Reagent as I've mainly used " [:a {:href "https://github.com/tonsky/rum"} "Rum"] " in the past. Rum is less declarative than Reagent but it does mean you can compose your state management abstractions to best suit your own taste, whereas Reagent uses its own atoms and component conventions; for example, Reagent evaluating the function returned by a component immediately mean that I couldn't return an invokable function for the parent view to apply as needed, which I've done in Rum previously. I'm not suggesting it's a good idea to do this; it's just that Reagent's choices constrain you more than Rum's do, by design of both libraries."]
   [:p "The multiple counters bonus example was in some ways very easy as a straight re-purposing of Reagent's state for their todo example meant that the plumbing involved was straightforward. The pausable solution I have in place works but contains a memory leak: the handle returned by setInterval is not retained, and so when the timer is deleted the interval function is not disconnected. A better solution would be to use the value returned by setInterval as the unique identifier for each pausable timer (since those values are guaranteed to be unique by the browser)."]
   [:h3 "On the Vigiglobe Chart tests"]
   [:p "I'd assumed initially that Vigiglobe would offer a charting API to integrate with the data API (especially after reading the WIZR promotional material) but this doesn't seem to be the case, at least for this test. Examining the output of the swagger interface to your API for the supplied vgteam-TV_Shows project-id also raised issues: the granularity and time period settings seemed to have an effect, but in each case the response reported zero entries for each time bucket! For example, I thought that " [:a {:href "http://api.vigiglobe.com/api/statistics/v1/volume?project_id=vgteam-TV_Shows&timeFrom=2010-02-27T20%3A52%3A12.050Z&granularity=day"}  "this URL"] " would supply a daily count of shows for each day since the 27th of February 2010. Instead, only results for March 2016 onwards were returned, and they showed zero shows for each date. Limiting myself to the past hour, as the test suggests, had the same issue."]
   [:p "One swift email to Arnaud later and...?"]
   [:div [:a {:href "/"} "Go back to the home page."]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

(secretary/defroute "/test1" []
  (session/put! :current-page #'ex/test1))

(secretary/defroute "/test2" []
  (session/put! :current-page #'ex/test2))

(secretary/defroute "/test3" []
  (session/put! :current-page #'ex/test3))

(secretary/defroute "/bonus" []
  (session/put! :current-page #'ex/bonus))

(secretary/defroute "/chart" []
  (session/put! :current-page #'vg/chart))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
