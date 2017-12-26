(ns elevator.ui.core
  (:require
    [reagent.core :as r]
    [elevator.ui.state :as state]
    [elevator.ui.views.app :refer [app-view]]))

(enable-console-print!)

(defn render []
  (r/render-component [app-view]
    (.. js/document (getElementById "app"))))

(defn ^:export init []
  (state/eval!)
  (render))

(defn ^:export reload []
  (render))
