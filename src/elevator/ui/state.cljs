(ns elevator.ui.state
  (:require
    [reagent.core :as r]
    [reagent.ratom :as ratom]
    [elevator.engine.core :as engine]
    [elevator.solutions :as solutions]))

(defonce state 
  (r/atom {:playing-interval nil 
           :tick 0
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

(def playing?
  (ratom/make-reaction
    (fn []
      (boolean (@state :playing-interval)))))

; transactions

(defn eval! []
  (swap! state assoc 
         :tick 0
         :world-states
         (vec (engine/run {:floor-count 5
                           :elevators [{:capacity 2}]
                           :ticks 100
                           :people-generator engine/standard-people-generator
                           :elevator-logic solutions/elevator-logic}))))

(defn set-tick! [tick]
  (swap! state assoc :tick tick))

(defn dec-tick! []
  (when (> @tick 0)
    (swap! state update :tick dec)))

(defn inc-tick! []
  (when (< @tick (dec @world-states-count))
    (swap! state update :tick inc)))

(defn stop! []
  (js/clearInterval (@state :playing-interval))
  (swap! state assoc :playing-interval nil))

(defn play! []
  (inc-tick!)
  (swap! state assoc :playing-interval 
         (js/setInterval (fn []
                           (if (= @tick (dec @world-states-count))
                             (stop!)
                             (inc-tick!)))
                         1000)))
