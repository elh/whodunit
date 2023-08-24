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
(def same-soln? true)

(defn generate []
  (if same-soln?
    (let [hs (lvar)]
      (puzzle zebra/config
              hs
              ;; bootstrap some rules
              [{:data {:type :membero
                       :kvs {:house-color "red" :house-idx 3}}
                :goal (membero (new-rec zebra/config {:house-color "red" :house-idx 3}) hs)}]
              ;; add some constraints on the solution that are hidden from the generated rules
              [{:data {:type :membero
                       :kvs {:drinks "water" :name "norwegian"}}
                :goal (membero (new-rec zebra/config {:drinks "water" :name "norwegian"}) hs)}
               {:data {:type :membero
                       :kvs {:pet "zebra" :name "japanese"}}
                :goal (membero (new-rec zebra/config {:pet "zebra" :name "japanese"}) hs)}]))
      ;; alternatively, without structure
      ;; (puzzle zebra/config
      ;;         hs
      ;;         ;; bootstrap some rules
      ;;         [(membero (new-rec zebra/config {:house-color "red" :house-idx 3}) hs)]
      ;;         ;; add some constraints on the solution that are hidden from the generated rules
      ;;         [(membero (new-rec zebra/config {:drinks "water" :name "norwegian"}) hs)
      ;;          (membero (new-rec zebra/config {:pet "zebra" :name "japanese"}) hs)]))
    (puzzle zebra/config)))

(let [res (time (generate))]
  (println "\nConfig:")
  (pp/pprint zebra/config)
  (println "\nRules:")
  (doseq [[idx item] (map-indexed vector (rules-text (:rules res)))]
    (println (str (inc idx) ".") item))
  (println "\nSolution:")
  (pp/pprint (:soln res)))
