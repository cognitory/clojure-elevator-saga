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

   (if @state/playing?
     [:button {:on-click (fn [_]
                           (state/stop!))}
      "Stop"]
     [:button {:on-click (fn [_]
                           (state/play!))}
      "Play"])

   @state/tick])

(defn debug-view [world-state]
  [:pre
   (with-out-str
     (cljs.pprint/pprint world-state))])

(defn app-view []
  (let [world-state @state/current-state
        next-world-state @state/next-state]
    [:div
     [world-view world-state next-world-state]
     
     [slider-view]
     [:button {:on-click (fn [_]
                           (state/eval!))}
      "Eval"]
     [debug-view world-state]]))
