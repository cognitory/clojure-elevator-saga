(ns elevator.state)

(defn- initialize [{:keys [floor-count elevator-count people-generator
                           elevator-logic]}]
  {:time 0
   :people []
   :floor-count floor-count
   :elevators (for [index (range elevator-count)] 
                {:index index
                 :floor 0
                 :open? true})
   :people-generator (partial people-generator floor-count)
   :elevator-logic elevator-logic})

(defn- to-player-state [world-state]
  (-> world-state
      (select-keys [:time :people :floor-count :elevators])))

(defn- add-people [world-state]
  (let [partial-people ((world-state :people-generator))]
    (-> world-state
        (update :people concat (map
                                 (fn [partial-person]
                                   {:location {:floor (partial-person :floor)} 
                                    :target-floor (partial-person :target-floor)
                                    :start-time (world-state :time)})
                                 partial-people)))))

(defn- increment-time [world-state]
  (update world-state :time inc))

(defn- move-people-into-elevator [world-state]
  (let [elevator-floors (set (map :floor (world-state :elevators)))]
    (update world-state :people 
            (fn [people]
              (for [person people]
                (if-let [elevator (->> (world-state :elevators)
                                       (filter (fn [elevator] 
                                                 (= (elevator :floor)
                                                    (-> person :location :floor))))
                                       first)]
                  (assoc person :location
                    {:elevator (elevator :index)})
                  person))))))

(defn- move-elevators [world-state]
  (update world-state :elevators 
          (fn [elevators]
            (for [elevator elevators]
              (let [commands ((world-state :elevator-logic) 
                              (to-player-state world-state))
                    command (get commands (elevator :index))]
                (case command
                  :up
                  (-> elevator
                      (assoc :open? false)
                      (update :floor inc))

                  :down
                  (-> elevator
                      (assoc :open? false)
                      (update :floor dec))

                  ; :open & default
                  (-> elevator
                      (assoc :open? true))))))))

(defn- tick [world-state]
  (-> world-state
      move-people-into-elevator
      move-elevators
      increment-time
      add-people))

(defn run [options]
  (-> (reduce (fn [world-state _]
                (tick world-state)) 
              (initialize options) 
              (range (options :ticks)))
      to-player-state))
