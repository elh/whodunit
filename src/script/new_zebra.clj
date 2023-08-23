(ns script.new-zebra
  (:gen-class)
  (:refer-clojure :exclude [==])
  (:require [whodunit.core :refer :all]
            [whodunit.zebra :as zebra]
            [clojure.pprint :as pp]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Generate a new variation of the original Zebra Puzzle
;;
;; At the moment, this usually completes in a few seconds; rarely, it creates an inefficient set of rules that hangs.
;; TODO: make this consistently fast by optimizing rule order in `puzzle`
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(let [res (time (puzzle zebra/config))]
  (println "\nConfig:\n" zebra/config)
  (println "\nRules:")
  (doseq [[idx item] (map-indexed vector (rules-text (:rules res)))]
    (println (str (inc idx) ".") item))
  (println "\nSolution:")
  (pp/pprint (:soln res)))

