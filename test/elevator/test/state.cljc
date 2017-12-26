(ns elevator.test.state
  (:require
    [clojure.test :refer [deftest is testing]]
    [elevator.state :as state]))

(defmacro is-sub= [expected actual]
  (list 'is (list '= expected (list 'select-keys actual (list 'keys expected)) )))

(defn meta-people-generator [queue]
  (let [queue (atom queue)]
    (fn [floor-count]
      (let [result (first @queue)]
        (swap! queue rest)
        result))))

(deftest tick
  (testing "Initialize with no people"
    (is (= {:people []
            :floor-count 2
            :time 1
            :elevators [{:index 0
                         :floor 0}
                        {:index 1
                         :floor 0}]}
           (-> (state/run {:floor-count 2
                           :elevator-count 2
                           :ticks 1
                           :people-generator (fn [_])})))))

  (testing "Tick uses generator to generate people"
    (is-sub= {:people [{:location {:floor 1}
                        :target-floor 0
                        :start-time 1}]}
             (-> (state/run {:floor-count 2
                             :elevator-count 2
                             :ticks 1
                             :people-generator (meta-people-generator
                                                 [[{:floor 1
                                                    :target-floor 0}]])})))

    (is-sub= {:people [{:location {:floor 1} 
                        :target-floor 0
                        :start-time 1}
                       {:location {:floor 1}
                        :target-floor 0
                        :start-time 2}]}
             (-> (state/run {:floor-count 2
                             :elevator-count 2
                             :ticks 2
                             :people-generator (meta-people-generator
                                                 [[{:floor 1
                                                    :target-floor 0}]
                                                  [{:floor 1
                                                    :target-floor 0}]])}))))

  (testing "Person on same floor as elevator assigned to elevator in next tick"
    (is-sub= {:people [{:location {:elevator 0} 
                        :target-floor 1
                        :start-time 1}]
              :elevators [{:index 0
                           :floor 0}]}
             (-> (state/run {:floor-count 2
                             :elevator-count 1
                             :ticks 2
                             :people-generator (meta-people-generator
                                                 [[{:floor 0
                                                    :target-floor 1}]])})))))
