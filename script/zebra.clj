(ns script.zebra
  (:gen-class)
  (:refer-clojure :exclude [==])
  (:require [whodunit.core :refer :all]
            [whodunit.zebra :refer :all]
            [clojure.pprint :as pp]))

(println "---------- Zebra Puzzle - using vectors ----------")
(dotimes [_ 4]
  (time (run+ zebrao-vec)))
(pp/pprint (time (run+ zebrao-vec)))
;; 13 ms.
;;
;; Context:
;; adds one more dimension with explicit house-idx but it is pinned
;; uses core.logic.fd
;; 650 ms. 50x slower
;;
;; reordering to match core.logic benchmark brings it down to 13 ms!!! parity with core.logic benchmark
;; removing fd lowers it to 400 ms

(println "\n---------- Zebra Puzzle - using maps ----------")
(dotimes [_ 4]
  (time (run+ zebrao)))
(pp/pprint (time (run+ zebrao)))
;; 23 ms
;;
;; Context:
;; adds one more dimension with explicit house-idx but it is pinned
;; adds creates a lot of maps and merges
;; uses core.logic.fd
;; 1200 ms. 92x slower
;;
;; reordering to match core.logic benchmark brings it down to 23 ms!!! 80% slower than core.logic benchmark



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; core.logic benchmark implementation
;; https://github.com/clojure/core.logic/blob/master/src/main/clojure/clojure/core/logic/bench.clj
;;
;; very simple approach that uses vector order to encoded house order. does not use core.logic.fd
;; 13 ms
