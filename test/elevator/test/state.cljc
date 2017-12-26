(ns elevator.test.state
  (:require
    [clojure.test :refer [deftest is testing]]
    [elevator.state :as state]))

(defmacro is-sub= [expected actual]
  (list 'is (list '= expected (list 'select-keys actual (list 'keys expected)) )))

(defn result-queue [queue]
  (let [queue (atom queue)]
    (fn [_]
      (let [result (first @queue)]
        (swap! queue rest)
        result))))

(deftest tick
  (testing "Initialize with no people"
    (is (= [{:people []
             :floor-count 2
             :time 0
             :elevators [{:index 0
                          :floor 0
                          :open? true}
                         {:index 1
                          :floor 0
                          :open? true}]}
            {:people []
             :floor-count 2
             :time 1
             :elevators [{:index 0
                          :floor 0
                          :open? true}
                         {:index 1
                          :floor 0
                          :open? true}]}]
           (state/run {:floor-count 2
                       :elevator-count 2
                       :ticks 1
                       :people-generator (fn [_])
                       :elevator-logic (fn [_] {})}))))

  (testing "Tick uses generator to generate people"
    (is-sub= {:people [{:location {:floor 1}
                        :target-floor 0
                        :start-time 1}]}
             (-> (state/run {:floor-count 2
                             :elevator-count 2
                             :ticks 1
                             :people-generator (result-queue
                                                 [[{:floor 1
                                                    :target-floor 0}]])
                             :elevator-logic (fn [_] {})})
                 last))

    (is-sub= {:people [{:location {:floor 1} 
                        :target-floor 0
                        :start-time 1}
                       {:location {:floor 1}
                        :target-floor 0
                        :start-time 2}]}
             (-> (state/run {:floor-count 2
                             :elevator-count 2
                             :ticks 2
                             :people-generator (result-queue
                                                 [[{:floor 1
                                                    :target-floor 0}]
                                                  [{:floor 1
                                                    :target-floor 0}]])
                             :elevator-logic (fn [_] {})})
                 last)))

  (testing "Person on same floor as elevator assigned to elevator in next tick"
    (is-sub= {:people [{:location {:elevator 0} 
                        :target-floor 1
                        :start-time 1}]
              :elevators [{:index 0
                           :floor 0
                           :open? true}]}
             (-> (state/run {:floor-count 2
                             :elevator-count 1
                             :ticks 2
                             :people-generator (result-queue
                                                 [[{:floor 0
                                                    :target-floor 1}]])
                             :elevator-logic (fn [_] {})})
                 last)))

  (testing "Elevators move according to commands"
    (is-sub= {:people []
              :elevators [{:index 0
                           :floor 1
                           :open? false}
                          {:index 1
                           :floor 0
                           :open? true}]}
             (-> (state/run {:floor-count 2
                             :elevator-count 2
                             :ticks 1
                             :people-generator (fn [_])
                             :elevator-logic (fn [_] 
                                               {0 :up
                                                1 :open})})
                 last)))

  (testing "Elevators don't move past max or minimum floor"
    (is-sub= {:people []
              :elevators [{:index 0
                           :floor 0
                           :open? true}
                          {:index 1
                           :floor 0
                           :open? true}]}
             (-> (state/run {:floor-count 1
                             :elevator-count 2
                             :ticks 1
                             :people-generator (fn [_])
                             :elevator-logic (fn [_]
                                               {0 :up
                                                1 :down})})
                 last)))

  (testing "People get off elevator when it is at target floor"
    (is-sub= {:people [{:location {:destination 1}
                        :target-floor 1
                        :start-time 1
                        :end-time 4}]
              :elevators [{:index 0
                           :floor 1
                           :open? true}]}
             (-> (state/run {:floor-count 2
                             :elevator-count 1
                             :ticks 4
                             :people-generator (result-queue
                                                 [[{:floor 0
                                                    :target-floor 1}]])
                             :elevator-logic (result-queue
                                               [{0 :open} 
                                                {0 :open}
                                                {0 :up}])})
                 last))))
