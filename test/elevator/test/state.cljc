(ns elevator.test.state
  (:require
    [clojure.test :refer [deftest is testing]]
    [elevator.state :as state]))

(deftest tick
  (testing "Initialize with no people"
    (is (= (-> (state/initialize {:floor-count 2
                                  :elevator-count 2
                                  :people-generator (fn [floor-count] nil)})
               state/tick
               state/to-player-state)
           {:people []
            :floor-count 2
            :time 1
            :elevators [{:location [:floor 0]}
                        {:location [:floor 0]}]})))
  
  
  (testing "Tick uses generator to generate people"
    (is (= (-> (state/initialize {:floor-count 2
                                  :elevator-count 2
                                  :people-generator (fn [floor-count] 
                                                      [{:floor 0
                                                        :target-floor 1}])})
               state/tick
               state/to-player-state)
           {:people [{:location [:floor 0]
                      :target-floor 1
                      :start-time 1}]
            :time 1
            :floor-count 2
            :elevators [{:location [:floor 0]}
                        {:location [:floor 0]}]}))))
