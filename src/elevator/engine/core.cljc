(ns elevator.engine.core)

(defn- initialize [{:keys [floor-count elevators people-generator
                           elevator-logic]}]
  {:time 0
   :people []
   :floor-count floor-count
   :elevators (vec (map-indexed (fn [index elevator] 
                                  {:index index
                                   :capacity (elevator :capacity)
                                   :floor 0
                                   :open? true})
                                elevators))
   :people-generator (partial people-generator floor-count)
   :elevator-logic elevator-logic})

(defn- to-player-state [world-state]
  (-> world-state
      (select-keys [:time :people :floor-count :elevators])))

(defn- add-people [world-state]
  (let [partial-people ((world-state :people-generator))]
    (-> world-state
        (update :people concat (vec (map-indexed (fn [index partial-person]
                                                   {:index (+ index (count (world-state :people)))
                                                    :location {:floor (partial-person :floor)} 
                                                    :target-floor (partial-person :target-floor)
                                                    :start-time (world-state :time)})
                                                 partial-people))))))

(defn- increment-time [world-state]
  (update world-state :time inc))

(defn- move-people-into-elevators [world-state]
  (update world-state :people 
          (fn [people]
            (reduce (fn [people person]
                      (if-let [elevator (->> (world-state :elevators)
                                             (filter (fn [elevator] 
                                                       (and 
                                                         (elevator :open?)
                                                         (= (elevator :floor)
                                                            (-> person :location :floor))
                                                         (< (->> people
                                                                 (filter (fn [person]
                                                                           (= {:elevator (elevator :index)} 
                                                                              (person :location))))
                                                                 count)
                                                            (elevator :capacity)))))
                                             first)]
                        (assoc people (person :index) (assoc person :location
                                                        {:elevator (elevator :index)}))
                        (assoc people (person :index) person)))
                    people
                    people))))

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
      move-elevators
      move-people-out-of-elevators
      move-people-into-elevators
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
        :target-floor (rand-nth (range 1 floor-count))}]
      [{:floor (rand-nth (range 1 floor-count))
        :target-floor 0}])))
