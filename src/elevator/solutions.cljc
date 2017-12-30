(ns elevator.solutions)

(defn- people-in-elevator [state index]
  (if-let [people (dissoc (group-by (fn [p] (-> p :location :elevator)) (state :people)) nil)]
    (people index)
    []))

(defn- cost [state next-commands]
  (let [next-elevators-state (vec
                               (map
                                 (fn [elevator]
                                   (case (get next-commands (elevator :index))
                                     :up
                                     (assoc
                                       elevator
                                       :floor
                                       (min
                                         (+ (elevator :floor) 1)
                                         (dec (state :floor-count))))
                                     :down
                                     (assoc
                                       elevator
                                       :floor
                                       (max
                                         (- (elevator :floor) 1)
                                         0))
                                     ; default, open
                                     elevator))
                                 (state :elevators)))]
    (reduce
      (fn [memo person]
        (+
          memo
          (let [target-floor (-> person :target-floor)
                current-floor (-> person :location :floor)]
           (cond
             (-> person :location :destination)
             0

             (-> person :location :floor)
             (let [empty-elevators (filter
                                     (fn [elevator]
                                       (<
                                         (people-in-elevator state (elevator :index))
                                         (elevator :capacity)))
                                     next-elevators-state)
                   closest-elevator (first
                                      (sort-by
                                        (fn [elevator] (Math/abs (- (elevator :floor) current-floor)))
                                        empty-elevators))]

               (if closest-elevator
                 (+ (Math/abs (- current-floor (closest-elevator :floor)))
                    1
                    (Math/abs (- current-floor target-floor)))
                 0))

             (-> person :location :elevator)
             (let [elevator-floor ((get next-elevators-state (-> person :location :elevator)) :floor)]
               (Math/abs (- elevator-floor target-floor)))
             :else
             0))))
      0
      (state :people))))

(defn elevator-logic [state]
  (let [up (cost state [:up])
        down (cost state [:down])
        open (cost state [:open])
        best-action-cost (min up down open)]
    (println (state :time) "up" up "down" down "open" open)
    (cond
      (= best-action-cost open)
      {0 {:action :open
          :indicators #{:up :down}}}
      (= best-action-cost up)
      {0 {:action :up
          :indicators #{:up :down}}}
      (= best-action-cost down)
      {0 {:action :down
          :indicators #{:up :down}}}
      :else
      {0 {:action :open
          :indicators #{:up :down}}})))
