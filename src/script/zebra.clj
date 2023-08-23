(ns script.zebra
  (:gen-class)
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic :refer :all]
            [whodunit.core :refer :all]
            [whodunit.zebra :refer :all]
            [clojure.pprint :as pp]))

;; 13 ms.
;;
;; Context:
;; adds one more dimension with explicit house-idx but it is pinned
;; uses core.logic.fd
;; 650 ms. 50x slower
;;
;; reordering to match core.logic benchmark brings it down to 13 ms!!! parity with core.logic benchmark
;; removing fd lowers it to 400 ms
(println "---------- Zebra Puzzle - using vectors ----------")
(dotimes [_ 4]
  (time (run+ zebrao-vec)))
(pp/pprint (time (run+ zebrao-vec)))

;; 23 ms
;;
;; Context:
;; adds one more dimension with explicit house-idx but it is pinned
;; adds creates a lot of maps and merges
;; uses core.logic.fd
;; 1200 ms. 92x slower
;;
;; reordering to match core.logic benchmark brings it down to 23 ms!!! 80% slower than core.logic benchmark
(println "\n---------- Zebra Puzzle - using maps ----------")
(dotimes [_ 4]
  (time (run+ zebrao)))
(pp/pprint (time (run+ zebrao)))

(println "")
(println "=======================================================")
(println "========== Debugging puzzle-fast-fixed-order ==========")
(println "=======================================================")

;; 24 ms
;;
;; same speed as before.
(println "\n---------- Run zebra rules directly with run+ ----------")
(dotimes [_ 4]
  (time (run+ zebrao-whodunit)))
(pp/pprint (time (run+ zebrao-whodunit)))

;; ;; note: this is super slow and never completes! issues with the order of the permuto rule
;; (println "\n---------- Run zebra rules directly with puzzle-fast-fixed-order setup manually ----------")
;; (let [hs (lvar)
;;       lvars (init-lvars config)]
;;   (pp/pprint (time (run+ (fn [q] (and* (into [] (concat
;;                                                  [(== hs q)
;;                                                   (== q (get lvars :records))
;;                                                   (== (get-in config [:values :name]) (get-in lvars [:values :name]))
;;                                                   (everyg (fn [k] (permuteo (get-in config [:values k]) (get-in lvars [:values k])))
;;                                                           (keys (get lvars :values)))]
;;                                                  [(== q (map #(new-rec config {:house-idx %}) (range 1 6)))]
;;                                                  (zebra-goals hs)))))))))

;; 300 ms
;;
;; after we made the fix
(println "\n---------- Run zebra with puzzle-fast-permuto-last ----------")
(let [hs (lvar)]
  (pp/pprint (time (puzzle config
                           hs
                           (map (fn [x] {:goal x}) (zebra-goals hs))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; core.logic benchmark implementation
;; https://github.com/clojure/core.logic/blob/master/src/main/clojure/clojure/core/logic/bench.clj
;;
;; very simple approach that uses vector order to encoded house order. does not use core.logic.fd
;; 13 ms
