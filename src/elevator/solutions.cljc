(ns elevator.solutions)

(defn- people-in-elevator [state index]
  (if-let [people (dissoc (group-by (fn [p] (-> p :location :elevator)) (state :people)) nil)]
    (people index)
    []))

(defn- requested-floors [state]
  (set (keys (dissoc (group-by (fn [p] (-> p :location :floor)) (state :people)) nil))))

(defn- requested-floor [state index]
  (contains? (set (requested-floors state)) index))

(defn- elevator-logic [state]
  (let [request-floors (atom (requested-floors state))]
    (into {}
          (for [elevator (state :elevators)]
            (if-let [people-in-elevator (people-in-elevator state (elevator :index))]
              (let [target-floors (map :target-floor people-in-elevator)
                    elevator-floor (elevator :floor)
                    closest-floor (first (sort-by (fn [f] (Math/abs (- f elevator-floor))) target-floors))
                    command (cond
                              (= closest-floor elevator-floor)
                              :open
                              (and
                                (requested-floor state elevator-floor)
                                (< (count people-in-elevator) (elevator :capacity))
                                (< closest-floor elevator-floor))
                              :open
                              (< closest-floor elevator-floor)
                              :down
                              (> closest-floor elevator-floor)
                              :up)]
                [(elevator :index) {:action     command
                                    :indicators #{:up :down}}])
              (let [elevator-floor (elevator :floor)
                    closest-floor (first (sort-by (fn [f] (Math/abs (- f elevator-floor))) @request-floors))
                    command (cond
                              (or
                                (nil? @request-floors)
                                (empty? @request-floors))
                              :open
                              (= closest-floor elevator-floor)
                              :open
                              (< closest-floor elevator-floor)
                              :down
                              (> closest-floor elevator-floor)
                              :up)]
                (swap! request-floors disj closest-floor)
                [(elevator :index) {:action     command
                                    :indicators #{:up :down}}]))))))

