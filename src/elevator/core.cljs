(ns elevator.core
  (:require
    [reagent.core :as r]
    [elevator.views.app :refer [app-view]]
    [elevator.test.state]))

(enable-console-print!)

(defn render []
  (r/render-component [app-view]
    (.. js/document (getElementById "app"))))

(defn run-tests []
  (cljs.test/run-tests 'elevator.test.state))

(defn ^:export init []
  (render))

(defn ^:export reload []
  (render)
  (run-tests))



