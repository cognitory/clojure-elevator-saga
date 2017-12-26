(ns elevator.core
  (:require
    [reagent.core :as r]
    [elevator.views.app :refer [app-view]]))

(enable-console-print!)

(defn render []
  (r/render-component [app-view]
    (.. js/document (getElementById "app"))))

(defn ^:export init []
  (render))

(defn ^:export reload []
  (render))



