(ns elevator.state)

(defn- initialize [{:keys [floor-count elevator-count people-generator
                           elevator-logic]}]
  {:time 0
   :people []
   :floor-count floor-count
   :elevators (vec (for [index (range elevator-count)] 
                     {:index index
                      :floor 0
                      :open? true}))
   :people-generator (partial people-generator floor-count)
   :elevator-logic elevator-logic})

(defn- to-player-state [world-state]
  (-> world-state
      (select-keys [:time :people :floor-count :elevators])))

(defn- add-people [world-state]
  (let [partial-people ((world-state :people-generator))]
    (-> world-state
        (update :people concat (vec (for [partial-person partial-people]
                                      {:location {:floor (partial-person :floor)} 
                                       :target-floor (partial-person :target-floor)
                                       :start-time (world-state :time)}))))))

(defn- increment-time [world-state]
  (update world-state :time inc))

(defn- move-people-into-elevators [world-state]
  (update world-state :people 
          (fn [people]
            (vec (for [person people]
                   (if-let [elevator (->> (world-state :elevators)
                                          (filter (fn [elevator] 
                                                    (= (elevator :floor)
                                                       (-> person :location :floor))))
                                          first)]
                     (assoc person :location
                       {:elevator (elevator :index)})
                     person))))))

(defn- move-people-out-of-elevators [world-state]
  (update world-state :people
          (fn [people]
            (vec (for [person people]
                   (let [elevator-index (-> person :location :elevator)
                         elevator (get-in world-state [:elevators elevator-index])]
                     (if (and 
                           elevator
                           (elevator :open?)
                           (= (elevator :floor) (person :target-floor)))
                       (-> person
                           (assoc :location
                             {:destination (person :target-floor)})
                           (assoc :end-time (inc (world-state :time))))
                       person)))))))

(defn- move-elevators [world-state]
  (update world-state :elevators 
          (fn [elevators]
            (vec (for [elevator elevators]
                   (let [commands ((world-state :elevator-logic) 
                                   (to-player-state world-state))
                         command (get commands (elevator :index))]
                     (case command
                       :up
                       (if (< (elevator :floor) (dec (world-state :floor-count)))
                         (-> elevator
                             (assoc :open? false)
                             (update :floor inc))
                         (-> elevator
                             (assoc :open? true)))

                       :down
                       (if (> (elevator :floor) 0)
                         (-> elevator
                             (assoc :open? false)
                             (update :floor dec))
                         (-> elevator
                             (assoc :open? true)))

                       ; :open & default
                       (-> elevator
                           (assoc :open? true)))))))))

(defn- tick [world-state]
  (-> world-state
      move-people-out-of-elevators
      move-people-into-elevators
      move-elevators
      increment-time
      add-people))

(defn run [options]
  (->> (reduce (fn [world-states _]
                 (conj world-states (tick (last world-states)))) 
               [(initialize options)] 
               (range (options :ticks)))
       (map to-player-state)))

(defn standard-people-generator [floor-count]
  (when (< (rand) 0.5)
    (if (< (rand) 0.5)
      [{:floor 0 
        :target-floor (rand-nth (range 1 (dec floor-count)))}]
      [{:floor (rand-nth (range 1 (dec floor-count)))
        :target-floor 0}])))
