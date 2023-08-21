(defproject whodunit "0.1.0-SNAPSHOT"
  :description "Logic Puzzles Experiments"
  :url "https://github.com/elh/whodunit"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/core.logic "1.0.1"]
                 [org.clojure/tools.macro "0.1.5"]]
  ;; :main ^:skip-aot whodunit.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}
             :dev {:dependencies [[lambdaisland/kaocha "1.85.1342"]]}}
  :plugins [[lein-exec "0.3.7"]]
  :aliases {"kaocha" ["run" "-m" "kaocha.runner"]})
