(ns elevator.ui.views.app
  (:require
    [elevator.ui.state :as state]
    [elevator.ui.views.world :refer [world-view]]))

(defn slider-view []
  [:div
   [:input {:type "range"
            :min 0
            :max (dec @state/world-states-count)
            :value @state/tick
            :on-change (fn [e]
                         (state/set-tick! (js/parseInt (.. e -target -value) 10)))}]

   [:button {:on-click (fn [_]
                         (state/dec-tick!))} 
    "<"]

   [:button {:on-click (fn [_]
                         (state/inc-tick!))} 
    ">"]

   @state/tick])

(defn debug-view [world-state]
  [:pre
   (with-out-str
     (cljs.pprint/pprint world-state))])

(defn app-view []
  (let [world-state @state/current-state]
    [:div
     [world-view world-state]
     
     [slider-view]
     [debug-view world-state]
     [:button {:on-click (fn [_]
                           (state/eval!))}
      "Eval"]]))
