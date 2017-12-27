(ns elevator.solutions)

(defn elevator-logic [state]
  (let [people (state :people)
        people-in-elevators (dissoc (group-by (fn [p] (-> p :location :elevator))  people) nil)
        people-on-floors (dissoc (group-by (fn [p] (-> p :location :floor))  people) nil)]
    (into {}
          (for [elevator (state :elevators)]
            (if-let [people-in-elevator (people-in-elevators (elevator :index))]
              (let [target-floors (map :target-floor people-in-elevator)
                    elevator-floor (elevator :floor)
                    closest-floor (first (sort-by (fn [f] (Math/abs (- f elevator-floor))) target-floors))
                    command (cond
                              (= closest-floor elevator-floor)
                              :open
                              (< closest-floor elevator-floor)
                              :down
                              (> closest-floor elevator-floor)
                              :up)]
                [(elevator :index) command])
              (let [request-floors (keys people-on-floors)
                    elevator-floor (elevator :floor)
                    closest-floor (first (sort-by (fn [f] (Math/abs (- f elevator-floor))) request-floors))
                    command (cond
                              (nil? request-floors)
                              :open
                              (= closest-floor elevator-floor)
                              :open
                              (< closest-floor elevator-floor)
                              :down
                              (> closest-floor elevator-floor)
                              :up)]
                [(elevator :index) command]))))))

