(ns testvg.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [testvg.exercises :as ex]))

;; -------------------------
;; Views

(defn home-page []
  [:div [:h2 "The Vigiglobe test pages."]
   [:div [:a {:href "/about"} "Notes about the experience."]]
   [:div [:a {:href "/test1"} "Go to the first test page."]]
   [:div [:a {:href "/test2"} "Go to the second test page."]]
   [:div [:a {:href "/test3"} "Go to the third test page."]]
   [:div [:a {:href "/bonus"} "Go to the bonus test page."]]])

(defn about-page []
  [:div [:h2 "About the experience."]
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
