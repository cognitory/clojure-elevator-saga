(defproject clj-elevator-saga "0.0.1"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 
                 [org.clojure/clojurescript "1.9.946"]
                 [re-frame "0.10.2"]]
  
  :plugins [[lein-figwheel "0.5.14"]]

  :figwheel {:server-port 6124}

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src" "test"]
                        :figwheel {:on-jsload "elevator.core/reload"}
                        :compiler {:main "elevator.core"
                                   :asset-path "/js/out"
                                   :output-to "resources/public/js/elevator.js"
                                   :output-dir "resources/public/js/out"}}]})
