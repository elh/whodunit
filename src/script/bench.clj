(ns script.bench
  (:gen-class)
  (:refer-clojure :exclude [==])
  (:require [criterium.core :as crit]
            [clojure.core.logic :refer :all]
            [whodunit.core :refer :all]
            [whodunit.zebra :refer :all]
            [script.config :as conf]))

;; Consider adding more benchmarks
;; Consider saving timing results. In a structured form?
;; Consider using bench instead of quick-bench

(println "////////////////////////////////////////////////////////////////////////////////")
(println "//////////////////////////// run+: the Zebra Puzzle ////////////////////////////")
(println "////////////////////////////////////////////////////////////////////////////////")
(crit/with-progress-reporting
  (crit/quick-bench
   (run+ zebrao-whodunit) :verbose))

(println "////////////////////////////////////////////////////////////////////////////////")
(println "///////////////////////////// puzzle: zebra-goals //////////////////////////////")
(println "////////////////////////////////////////////////////////////////////////////////")
(crit/with-progress-reporting
  (crit/quick-bench
   (let [hs (lvar)]
     (puzzle config
             hs
             (mapv (fn [x] {:goal x}) (zebra-goals hs)))) :verbose))

(println "////////////////////////////////////////////////////////////////////////////////")
(println "/////////////////////// puzzle: Generate a small puzzle ////////////////////////")
(println "////////////////////////////////////////////////////////////////////////////////")
(crit/with-progress-reporting
  (crit/quick-bench
   (puzzle (get conf/configs "4")) :verbose))
