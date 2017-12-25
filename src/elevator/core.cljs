(ns elevator.core
  (:require
    [reagent.core :as r]
    [elevator.views :as views]
    [elevator.test.state]))

(enable-console-print!)

(defn render []
  (r/render-component [views/app-view]
    (.. js/document (getElementById "app"))))

(defn run-tests []
  (cljs.test/run-tests 'elevator.test.state))

(defn ^:export init []
  (render))

(defn ^:export reload []
  (render)
  (run-tests))



