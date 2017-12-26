(ns elevator.test.state
  (:require
    [clojure.test :refer [deftest is testing]]
    [elevator.state :as state]))

(defmacro is-sub= [expected actual]
  (list 'is (list '= (list 'select-keys expected (list 'keys actual)) actual)))

(defn meta-people-generator [queue]
  (let [queue (atom queue)]
    (fn [floor-count]
      (let [result (first @queue)]
        (swap! queue rest)
        result))))

(deftest tick
  (testing "Initialize with no people"
    (is (= (-> (state/run {:floor-count 2
                           :elevator-count 2
                           :ticks 1
                           :people-generator (fn [_])}))
           {:people []
            :floor-count 2
            :time 1
            :elevators [{:location [:floor 0]}
                        {:location [:floor 0]}]})))

  (testing "Tick uses generator to generate people"
    (is-sub= (-> (state/run {:floor-count 2
                             :elevator-count 2
                             :ticks 1
                             :people-generator (meta-people-generator
                                                 [[{:floor 0
                                                    :target-floor 1}]])}))
             {:people [{:location [:floor 0]
                        :target-floor 1
                        :start-time 1}]})


    (is-sub= (-> (state/run {:floor-count 2
                             :elevator-count 2
                             :ticks 2
                             :people-generator (meta-people-generator
                                                 [[{:floor 0
                                                    :target-floor 1}]
                                                  [{:floor 1
                                                    :target-floor 0}]])}))
             {:people [{:location [:floor 0]
                        :target-floor 1
                        :start-time 1}
                       {:location [:floor 1]
                        :target-floor 0
                        :start-time 2}]})))
