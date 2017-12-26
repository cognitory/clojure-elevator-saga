(ns elevator.views.world)

(defn world-view [world-state]
  (let [floor-height 50
        waiting-area-width 100
        elevator-width 35
        elevator-gap 5
        total-height (* floor-height (world-state :floor-count))] 
    [:svg {:height total-height}
     (for [floor-index (range (world-state :floor-count))]
       ^{:key floor-index}
       [:g {:transform (str "translate(" 0 "," (- total-height (* floor-height (inc floor-index))) ")")}
        [:rect.floor 
         {:width (+ waiting-area-width (* (count (world-state :elevators)) (+ elevator-width elevator-gap))) 
          :height floor-height 
          :fill "#999" 
          :stroke "black" 
          :stroke-width "2px"}]
        [:g
         [:rect.waiting-area
          {:width waiting-area-width
           :height floor-height
           :fill "#666"}]
         (map-indexed 
           (fn [index person]
             ^{:key index}
             [:circle
              {:cx (- waiting-area-width 20 (* index 25))
               :cy 20
               :r 10
               :fill "yellow"
               :stroke "black"
               :stroke-width "1px"}]) 
           (->> (world-state :people)
                (filter (fn [person]
                          (= {:floor floor-index} (person :location))))))]])
     (for [elevator (world-state :elevators)]
       ^{:key (elevator :index)}
       [:g {:style {:transition "transform 1s ease-in-out"
                    :transform (str "translate(" (+ waiting-area-width (* (elevator :index) (+ elevator-width elevator-gap))) "px ,"
                                    (- total-height (* floor-height (inc (elevator :floor)))) "px)")}}
        [:rect.elevator 
         {:width elevator-width 
          :height floor-height 
          :fill (if (elevator :open?) "black" "gray") 
          :stroke "black" 
          :stroke-width "2px"}]
        (map-indexed 
          (fn [index person]
            ^{:key index}
            [:circle
             {:cx (+ 20 (* index 5))
              :cy 20
              :r 10
              :fill "yellow"
              :stroke "black"
              :stroke-width "1px"}]) 
          (->> (world-state :people)
               (filter (fn [person]
                         (= {:elevator (elevator :index)} (person :location))))))])]))
