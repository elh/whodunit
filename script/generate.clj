(ns script.generate
  (:gen-class)
  (:refer-clojure :exclude [==])
  (:require [whodunit.core :refer :all]))

(println "---------- Logic Puzzle Generation ----------")
(println "Generating...")
(let [example-config {:values {:name ["alice" "bob" "carol"]
                               :guilty [true false false]
                               :color ["red" "blue" "green"]
                               :location ["park" "pier" "palace"]}}
      rules (time (puzzle-exhaustive example-config))]
  (println "\nConfig:\n" example-config)
  (println "\nRules:")
  (doseq [[idx item] (map-indexed vector (rules-text rules))]
    (println (str (inc idx) ".") item)))

;; some example configs. testing complexity
;; {:values {:name ["alice" "bob"]
;;           :guilty [true false]
;;           :color ["red" "blue"]
;;           :location ["park" "pier"]}}
;;
;; {:values {:name ["alice" "bob" "carol"]
;;           :guilty [true false false]
;;           :color ["red" "blue" "green"]
;;           :location ["park" "pier" "palace"]}}
;;
;; {:values {:name ["alice" "bob" "carol" "dave"]
;;           :guilty [true false false false]
;;           :color ["red" "blue" "green" "white"]
;;           :location ["park" "pier" "palace" "plaza"]}}
;;
;; ;; solution space is too large for a full state space search...
;; {:values {:name ["alice" "bob" "carol" "dave" "eve"]
;;           :guilty [true false false false false]
;;           :color ["red" "red" "green" "yellow" "blue"]       ;; 2 reds
;;           :location ["park" "park" "pier" "pier" "palace"]}} ;; 2 parks, 2 piers
