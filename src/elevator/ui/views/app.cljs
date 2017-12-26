(ns elevator.ui.views.app
  (:require
    [reagent.core :as r]
    [elevator.engine.core :as engine]
    [elevator.ui.views.world :refer [world-view]]))

(def state 
  (r/atom {:tick 0
           :world-states (vec (engine/run {:floor-count 5
                                           :elevator-count 3
                                           :ticks 190
                                           :people-generator engine/standard-people-generator
                                           :elevator-logic (fn [_] 
                                                             {0 (rand-nth [:up :down :open])
                                                              1 (rand-nth [:up :down :open])
                                                              2 (rand-nth [:up :down :open])})}))}))

(defn slider-view []
  [:div
   [:input {:type "range"
            :min 0
            :max (dec (count (@state :world-states)))
            :value (@state :tick)
            :on-change (fn [e]
                         (swap! state assoc :tick (js/parseInt (.. e -target -value) 10)))}]

   [:button {:on-click (fn [_]
                         (when (> (@state :tick) 0)
                           (swap! state update :tick dec)))} 
    "<"]

   [:button {:on-click (fn [_]
                         (when (< (@state :tick) (dec (count (@state :world-states))))
                           (swap! state update :tick inc)))} 
    ">"]

   (@state :tick)])

(defn debug-view [world-state]
  [:pre
   (with-out-str
     (cljs.pprint/pprint world-state))])

(defn app-view []
  (let [world-state (get-in @state [:world-states (@state :tick)])]
    [:div
     [world-view world-state]
     [slider-view]
     [debug-view world-state]]))

