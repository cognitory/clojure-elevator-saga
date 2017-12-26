(ns elevator.state)

(defn- initialize [{:keys [floor-count elevator-count people-generator]}]
  {:time 0
   :people []
   :floor-count floor-count
   :elevators (repeat elevator-count 
                      {:location [:floor 0]})
   :people-generator (partial people-generator floor-count)})

(defn- to-player-state [world-state]
  (-> world-state
      (select-keys [:time :people :floor-count :elevators])))

(defn- add-people [world-state]
  (let [partial-people ((world-state :people-generator))]
    (-> world-state
        (update :people concat (map
                                 (fn [partial-person]
                                   {:location [:floor (partial-person :floor)]
                                    :target-floor (partial-person :target-floor)
                                    :start-time (world-state :time)})
                                 partial-people)))))

(defn- increment-time [world-state]
  (update world-state :time inc))

(defn- tick [world-state]
  (-> world-state
      increment-time
      add-people))

(defn run [options]
  (-> (reduce (fn [world-state _]
                (tick world-state)) 
              (initialize options) 
              (range (options :ticks)))
      to-player-state))
