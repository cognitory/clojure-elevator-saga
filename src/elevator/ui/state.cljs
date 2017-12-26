(ns elevator.ui.state
  (:require
    [reagent.core :as r]
    [reagent.ratom :as ratom]
    [elevator.engine.core :as engine]))

(defonce state 
  (r/atom {:tick 0
           :world-states []}))

; reactions

(def world-states
  (ratom/make-reaction 
    (fn []
      (@state :world-states))))

(def world-states-count
  (ratom/make-reaction
    (fn []
      (count @world-states))))

(def tick
  (ratom/make-reaction
    (fn []
      (@state :tick))))

(def current-state
  (ratom/make-reaction
    (fn []
      (get @world-states @tick))))

; transactions

(defn eval! []
  (swap! state assoc 
         :tick 0
         :world-states
         (vec (engine/run {:floor-count 5
                           :elevator-count 3
                           :ticks 100
                           :people-generator engine/standard-people-generator
                           :elevator-logic (fn [_] 
                                             {0 (rand-nth [:up :down :open])
                                              1 (rand-nth [:up :down :open])
                                              2 (rand-nth [:up :down :open])})}))))

(defn set-tick! [tick]
  (swap! state assoc :tick tick))

(defn inc-tick! []
  (when (> @tick 0)
    (swap! state update :tick dec)))

(defn dec-tick! []
  (when (< @tick (dec @world-states-count))
    (swap! state update :tick inc)))
