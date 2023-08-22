(ns script.zebra
  (:gen-class)
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic :refer :all]
            [clojure.core.logic.fd :as fd]
            [whodunit.core :refer :all]
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



;;;;;;;; Identify performance issues of puzzle-fast-fixed-order
;; the zebrao rules fast when run through run+ but super slow through puzzle-fast-fixed-order...
;;
;; TODO: figure out which added rule is causing this

(def config {:values {:house-idx [1 2 3 4 5]
                      :house-color ["blue" "green" "ivory" "red" "yellow"]
                      :nationality ["englishman" "japanese" "norwegian" "spaniard" "ukrainian"]
                      :drinks ["coffee" "milk" "orange-juice" "tea" "water"]
                      :smokes ["chesterfields" "kools" "lucky-strike" "old-gold" "parliaments"]
                      :pet ["dog" "fox" "horse" "snail" "zebra"]}})

(defn righto [x y]
  (fd/+ x 1 y))

(defn nexto [x y]
  (conde [(fd/+ x 1 y)]
         [(fd/- x 1 y)]))

(defn ordero [config idx-rel key x y hs]
  (fresh [x-idx y-idx]
         (membero x hs)
         (membero y hs)
         (== x (new-rec config {key x-idx}))
         (== y (new-rec config {key y-idx}))
         (idx-rel x-idx y-idx)))

(defn zebra-puzzle-rules [q]
  [;; this ordering from core.logic benchmark v. original problem statement improves latency from 1200ms to 23ms!!!
   (membero (new-rec config {:house-idx 3
                             :drinks "milk"}) q)
   (membero (new-rec config {:house-idx 1
                             :nationality "norwegian"}) q)
   (ordero config nexto :house-idx (new-rec config {:nationality "norwegian"}) (new-rec config {:house-color "blue"}) q)
   (ordero config righto :house-idx (new-rec config {:house-color "ivory"}) (new-rec config {:house-color "green"}) q)
   (membero (new-rec config {:house-color "red"
                             :nationality "englishman"}) q)
   (membero (new-rec config {:house-color "yellow"
                             :smokes "kools"}) q)
   (membero (new-rec config {:nationality "spaniard"
                             :pet "dog"}) q)
   (membero (new-rec config {:house-color "green"
                             :drinks "coffee"}) q)
   (membero (new-rec config {:nationality "ukrainian"
                             :drinks "tea"}) q)
   (membero (new-rec config {:drinks "orange-juice"
                             :smokes "lucky-strike"}) q)
   (membero (new-rec config {:nationality "japanese"
                             :smokes "parliaments"}) q)
   (membero (new-rec config {:smokes "old-gold"
                             :pet "snail"}) q)
   (ordero config nexto :house-idx (new-rec config {:smokes "kools"}) (new-rec config {:pet "horse"}) q)
   (ordero config nexto :house-idx (new-rec config {:smokes "chesterfields"}) (new-rec config {:pet "fox"}) q)
               ;; implied by the questions
   (membero (new-rec config {:drinks "water"}) q)
   (membero (new-rec config {:pet "zebra"}) q)])

(println "")
(println "=======================================================")
(println "========== Debugging puzzle-fast-fixed-order ==========")
(println "=======================================================")

(println "\n---------- Run zebra rules directly with run+ ----------")
(time (run+ (fn [q] (and* (zebra-puzzle-rules q)))))

(println "\n---------- Run zebra with puzzle-fast-fixed-order ----------")
(let [hs (lvar)]
  (time (puzzle-fast-fixed-order config hs (map (fn [x] {:goal x}) (zebra-puzzle-rules hs)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; core.logic benchmark implementation
;; https://github.com/clojure/core.logic/blob/master/src/main/clojure/clojure/core/logic/bench.clj
;;
;; very simple approach that uses vector order to encoded house order. does not use core.logic.fd
;; 13 ms
