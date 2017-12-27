(ns elevator.ui.views.world)

(defn person-view [index]
  (let [icon (cond 
               (= 0 (mod index 5)) 
               "child"
               (even? index) 
               "male" 
               :else
               "female")]
    [:use {:href (str "icons.svg#icon-" icon) 
           :width "15px" 
           :height "20px"}]))

(defn world-view [world-state]
  (let [floor-height 50
        person-width 15
        person-height 20
        waiting-area-width 100
        destination-area-width 100
        elevator-gap 5
        total-height (* floor-height (world-state :floor-count))
        elevator-width (fn [elevator]
                         (* person-width (elevator :capacity)))
        elevator-widths-up-to-index (fn [index]
                                      (reduce (fn [sum elevator]
                                                (+ sum (elevator-width elevator) elevator-gap)) 
                                              0 
                                              (take index (world-state :elevators))))
        elevator-x (fn [elevator]
                     (+ waiting-area-width (elevator-widths-up-to-index (elevator :index))))
        elevator-y (fn [elevator]
                     (- total-height (* floor-height (inc (elevator :floor)))))

        total-width (+ waiting-area-width (elevator-widths-up-to-index (count (world-state :elevators))) destination-area-width)
        floor-x (fn [floor]
                  0)
        floor-y (fn [floor]
                  (- total-height (* floor-height (inc floor))))
        floor-color (fn [floor]
                      (str "hsl(" (* 360 (/ floor (world-state :floor-count))) ",80%,50%)"))
        index-in-location (fn [person-index location]
                            (->> (world-state :people)
                                 (map-indexed vector)
                                 (filter (fn [[i person]]
                                           (= (person :location) location)))
                                 (take-while (fn [[i _]]
                                               (not= i person-index)))
                                 count))] 

    [:svg {:height total-height :width total-width}

     [:g.floors
      (for [floor-index (range (world-state :floor-count))]
        ^{:key floor-index}
        [:g {:transform (str "translate(" (floor-x floor-index) "," (floor-y floor-index) ")")}
         [:rect.floor 
          {:width total-width 
           :height floor-height 
           :fill "#999" 
           :stroke "black" 
           :stroke-width "2px"}]
         [:rect.waiting-area
          {:width waiting-area-width
           :height floor-height
           :fill "#666"}]
         [:rect.destination-area
          {:width destination-area-width
           :height floor-height
           :fill (floor-color floor-index)
           :x (- total-width destination-area-width)}]])]

     [:g.elevators
      (for [elevator (world-state :elevators)]
        ^{:key (elevator :index)}
        [:g {:style {:transition "transform 1s ease-in-out"
                     :transform (str "translate(" (elevator-x elevator) "px ," (elevator-y elevator) "px)")}}
         [:rect.elevator 
          {:width (elevator-width elevator) 
           :height floor-height 
           :fill (if (elevator :open?) "black" "gray") 
           :stroke "black" 
           :stroke-width "2px"}]])]
     
     [:g.people
      (map-indexed 
        (fn [index person]
          (let [{:keys [x y]} (case (first (keys (person :location)))
                                :elevator
                                (let [elevator (get-in world-state [:elevators (-> person :location :elevator)])
                                      position-in-elevator (index-in-location index {:elevator (elevator :index)})]
                                  {:x (+ (elevator-x elevator) (* person-width position-in-elevator))  
                                   :y (elevator-y elevator)})

                                :floor
                                (let [floor (-> person :location :floor)
                                      position-in-floor (index-in-location index {:floor floor})]
                                  {:x (+ (floor-x floor) (* person-width position-in-floor))
                                   :y (floor-y floor)})

                                :destination
                                (let [floor (-> person :location :destination)]
                                  {:x total-width
                                   :y (floor-y floor)}))]
            ^{:key index}
            [:g 
             {:style {:transition "transform 1s ease-in-out"
                      :transform (str "translate(" x "px ," (+ (- floor-height person-height) y) "px)")}
              :fill (floor-color (person :target-floor))
              :stroke "black"
              :stroke-width "1px"}
             [person-view index]])) 
        (world-state :people))]]))
