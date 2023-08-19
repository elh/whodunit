(ns whodunit.core
  (:gen-class)
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic :refer :all]
            [clojure.core.logic.fd :as fd]
            [clojure.tools.macro :as macro]
            [clojure.pprint :as pp]))

;; The Zebra Puzzle. see https://en.wikipedia.org/wiki/Zebra_Puzzle
;; result binding is a vec of [house-idx house-color nationality drinks smokes pet] vecs
(defn zebrao-vec [q]
  (macro/symbol-macrolet
   [_ (lvar)]
   (letfn [(righto [x y]
                   (fd/+ x 1 y))
           (nexto [x y]
                  (conde [(fd/+ x 1 y)]
                         [(fd/- x 1 y)]))
           (house-ordero [idx-rel x y hs]
                         (fresh [x-idx y-idx]
                                (membero x hs)
                                (membero y hs)
                                (== [x-idx _ _ _ _ _] x)
                                (== [y-idx _ _ _ _ _] y)
                                (idx-rel x-idx y-idx)))]
     ;; where house-idx's are 1-5 in order
     (let [answers (apply map list (cons (range 1 6)
                                         (repeatedly 5 #(repeatedly 5 lvar))))]
       (all
        (== answers q)
        (== [_ _ _ _ _] q)
        (membero [_ "red" "englishman" _ _ _] q)
        (membero [_ _ "spaniard" _ _ "dog"] q)
        (membero [_ "green" _ "coffee" _ _] q)
        (membero [_ _ "ukrainian" "tea" _ _] q)
        (house-ordero righto [_ "ivory" _ _ _ _] [_ "green" _ _ _ _] q)
        (membero [_ _ _ _ "old-gold" "snail"] q)
        (membero [_ "yellow" _ _ "kools" _] q)
        (membero [3 _ _ "milk" _ _] q)
        (membero [1 _ "norwegian" _ _ _] q)
        (house-ordero nexto [_ _ _ _ "chesterfields" _] [_ _ _ _ _ "fox"] q)
        (house-ordero nexto [_ _ _ _ "kools" _] [_ _ _ _ _ "horse"] q)
        (membero [_ _ _ "orange-juice" "lucky-strike" _] q)
        (membero [_ _ "japanese" _ "parliaments" _] q)
        (house-ordero nexto [_ _ "norwegian" _ _ _] [_ "blue" _ _ _ _] q)
        ;; implied by the questions
        (membero [_ _ _ "water" _ _] q)
        (membero [_ _ _ _ _ "zebra"] q))))))

;; The Zebra Puzzle. see https://en.wikipedia.org/wiki/Zebra_Puzzle
(defn zebrao [q]
  (macro/symbol-macrolet
   [_ (lvar)]
   (letfn [(new-rec ([] (new-rec nil))
                    ([p] (merge {:house-idx _
                                 :house-color _
                                 :nationality _
                                 :drinks _
                                 :smokes _
                                 :pet _} p)))
           ;; note: issues getting featurec to work
           (righto [x y]
                   (fd/+ x 1 y))
           (nexto [x y]
                  (conde [(fd/+ x 1 y)]
                         [(fd/- x 1 y)]))
           (ordero [idx-rel key x y hs]
                   (fresh [x-idx y-idx]
                          (membero x hs)
                          (membero y hs)
                          (== x (new-rec {key x-idx}))
                          (== y (new-rec {key y-idx}))
                          (idx-rel x-idx y-idx)))]
     (let [answers (map #(new-rec {:house-idx %}) (range 1 6))]
       (all
        (== answers q)
        (membero (new-rec {:house-color "red"
                           :nationality "englishman"}) q)
        (membero (new-rec {:nationality "spaniard"
                           :pet "dog"}) q)
        (membero (new-rec {:house-color "green"
                           :drinks "coffee"}) q)
        (membero (new-rec {:nationality "ukrainian"
                           :drinks "tea"}) q)
        (ordero righto :house-idx (new-rec {:house-color "ivory"}) (new-rec {:house-color "green"}) q)
        (membero (new-rec {:smokes "old-gold"
                           :pet "snail"}) q)
        (membero (new-rec {:house-color "yellow"
                           :smokes "kools"}) q)
        (membero (new-rec {:house-idx 3
                           :drinks "milk"}) q)
        (membero (new-rec {:house-idx 1
                           :nationality "norwegian"}) q)
        (ordero nexto :house-idx (new-rec {:smokes "chesterfields"}) (new-rec {:pet "fox"}) q)
        (ordero nexto :house-idx (new-rec {:smokes "kools"}) (new-rec {:pet "horse"}) q)
        (membero (new-rec {:drinks "orange-juice"
                           :smokes "lucky-strike"}) q)
        (membero (new-rec {:nationality "japanese"
                           :smokes "parliaments"}) q)
        (ordero nexto :house-idx (new-rec {:nationality "norwegian"}) (new-rec {:house-color "blue"}) q)
        ;; implied by the questions
        (membero (new-rec {:drinks "water"}) q)
        (membero (new-rec {:pet "zebra"}) q))))))

;; Evaluates if input is fully grounded. Returns true if input and all collection elements have no symbols.
;; This could be improved with a built-in core.logic approach or tighter check on the symbol names.
(defn grounded? [x]
  (not-any? #(instance? clojure.lang.Symbol %)
            (tree-seq coll? seq x)))

;; Runs the goal with timing and returns the first solution, if it's grounded, and if there are more solutions.
(defn run-with-context [goal]
  (let [res (run 2 [q] (time (goal q)))
        soln (first res)
        is-grounded (grounded? soln)]
    {:soln soln
     :grounded? is-grounded
     :has-more? (> (count res) 1)}))

(defn -main []
  (println "\n---------- Zebra Puzzle - using vectors ----------")
  (pp/pprint (run-with-context zebrao-vec))
  (println "\n---------- Zebra Puzzle - using maps ----------")
  (pp/pprint (run-with-context zebrao)))
