(ns testvg.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [testvg.exercises :as ex]
              [testvg.linechart :as lc]
              [testvg.circletimechart :as ctc]))

;; -------------------------
;; Views

(defn home-page []
  [:div
   [:h3 "The Reagent test pages."]
   [:div [:a {:href "/test1"} "Go to the first test page."]]
   [:div [:a {:href "/test2"} "Go to the second test page."]]
   [:div [:a {:href "/test3"} "Go to the third test page."]]
   [:div [:a {:href "/bonus"} "Go to the bonus test page."]]
   [:h3 "The chart test pages."]
   [:div [:a {:href "/linechart"} "Go to the linechart page."]]
   [:div [:a {:href "/circletimechart"} "Go to the custom chart page."]]
   [:h3 "Notes"]
   [:div [:a {:href "/about"} "Notes about the experience."]]])

(defn about-page []
  [:div [:h2 "About the experience"]
   [:h3 "On the Reagent tests"]
   [:p "It was interesting to use Reagent as I've mainly used " [:a {:href "https://github.com/tonsky/rum"} "Rum"] " in the past. Rum is less declarative than Reagent but it does mean you can compose your state management abstractions to best suit your own taste, whereas Reagent uses its own atoms and component conventions; for example, Reagent evaluating the function returned by a component immediately mean that I couldn't return an invokable function for the parent view to apply as needed, which I've done in Rum previously. I'm not suggesting it's a good idea to do this; it's just that Reagent's choices constrain you more than Rum's do, by design of both libraries."]
   [:p "The multiple counters bonus example was in some ways very easy as a straight re-purposing of Reagent's state for their todo example meant that the plumbing involved was straightforward. The pausable solution I have in place works but contains a memory leak: the handle returned by setInterval is not retained, and so when the timer is deleted the interval function is not disconnected. A better solution would be to use the value returned by setInterval as the unique identifier for each pausable timer (since those values are guaranteed to be unique by the browser)."]
   [:h3 "On the Vigiglobe Line Chart tests"]
   [:p "I'd assumed initially that Vigiglobe would offer a charting API to integrate with the data API (especially after reading the WIZR promotional material) but this doesn't seem to be the case, at least for this test. Examining the output of the swagger interface to your API for the supplied vgteam-TV_Shows project-id also raised issues: the granularity and time period settings seemed to have an effect, but in each case the response reported zero entries for each time bucket! For example, I thought that " [:a {:href "http://api.vigiglobe.com/api/statistics/v1/volume?project_id=vgteam-TV_Shows&timeFrom=2010-02-27T20%3A52%3A12.050Z&granularity=day"}  "this URL"] " would supply a daily count of shows for each day since the 27th of February 2010. Instead, only results for March 2016 onwards were returned, and they showed zero shows for each date. Limiting myself to the past hour, as the test suggests, had the same issue."]
   [:p "One swift email to Arnaud later and we're back on track, using a different data source (the public earthquake dataset Vigiglobe allow clients to use for integration tests.)"]
   [:p "I'm using " [:a {:href "http://d3js.org"} "D3"] " to implement a basic linechart. D3 is slightly awkward to use directly from clojurescript as it assumes a shared global space between the data and the visualisation functions. A more component orientated approach (like " [:a {:href "http://highcharts.com"} "Highcharts" ]", or a D3-based library like " [:a {:href "http://nvd3.org"} "NVD3.js"] ") would be quicker in the short term, but not at all as useful for a custom implementation of a graph."]
   [:p "The linechart shown has a basic mouse-over detailing the closest datapoint, and an average magnitude line (ignoring the practical utility of such a line), but nothing else. This is certainly less than what the linechart component in one of the above libraries gives, but this implementation should show an understanding of understanding chart creation challenges, including translation from data-space to screen-space, transformation of SVG elements using D3 in clojurescript, and knowledge of associated tasks."]
   [:h3 "On the Vigiglobe Custom Chart tests"]
   [:p "The main idea here was to construct a circular line chart for particular line periods, comparing the intensity of earthquakes over the most recent period against a historical one. for example, the default option shows the current day's earthquake data vs the same day last week."]
   [:p "This wasn't hugely successful. The data available from the Vigiglobe API often didn't suit the requests (visible in the selection box of available comparisons). In fact, the current day versus the same day last week is the best example as both datasets are available."]
   [:p "I wouldn't call this visualisation finished, as there's no mouse-over showing details of individual data points, and more grid lines could be presented to show the difference in periods between the datasets. But the underlying paucity of data argues against proceeding."]
   [:p "More generally, the idea behind the chart is that you could compare social media activity over specific periods, to see at a glance when your normal busy times are (for example, Tuesday afternoons), especially if an average of the previous weeks data was used as a comparison point. I did think while examining the Vigiglobe API docs that one thing that was missing was the ability to request data above the atomic level - e.g. data that had been cumulatively analysed or prepared in some way."]
   [:p "In summary this was an enjoyable exercise, and it was good to use Reagent outside of toy exercises (and to remember how awkward integrating D3 directly can be, even though version 4.0 broke itself up into independent modules). Experimenting with data visualisation is a passion of mine (as evidenced by " [:a {:href="https://getbulb.com"}  "GetBulb"] ") and it's work I'd love to do under Vigiglobe's auspices."]
   [:p "Oliver Mooney" [:br] [:a {:href "mailto:oliver.mooney@gmail.com"} "oliver.mooney@gmail.com"]]
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

(secretary/defroute "/linechart" []
  (session/put! :current-page #'lc/chart))

(secretary/defroute "/circletimechart" []
  (session/put! :current-page #'ctc/chart))

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
