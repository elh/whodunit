(ns script.new-zebra
  (:gen-class)
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic :refer :all]
            [whodunit.core :refer :all]
            [whodunit.zebra :as zebra]
            [clojure.pprint :as pp]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Generate a new variation of the original Zebra Puzzle
;;
;; At the moment, this usually completes in a few seconds; rarely, it creates an inefficient set of rules that hangs.
;; TODO: make this consistently fast by optimizing rule order in `puzzle`
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; If true, let's create with the same answers to the final question as the original puzzle
;; "who drinks water? Who owns the zebra?"
(def same-soln? false)

(defn generate []
  (if same-soln?
    (let [hs (lvar)]
      (puzzle zebra/config hs [{:data {:type :membero
                                       :kvs {:drinks "water"
                                             :name "norwegian"}}
                                :goal (membero (new-rec zebra/config {:drinks "water"
                                                                      :name "norwegian"}) hs)}
                               {:data {:type :membero
                                       :kvs {:pet "zebra"
                                             :name "japanese"}}
                                :goal (membero (new-rec zebra/config {:pet "zebra"
                                                                      :name "japanese"}) hs)}]))
    (puzzle zebra/config)))

(let [res (time (generate))]
  (println "\nConfig:")
  (pp/pprint zebra/config)
  (println "\nRules:")
  (doseq [[idx item] (map-indexed vector (rules-text (:rules res)))]
    (println (str (inc idx) ".") item))
  (println "\nSolution:")
  (pp/pprint (:soln res)))
