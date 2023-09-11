(ns whodunit.zebra
  (:gen-class)
  (:refer-clojure :exclude [==])
  (:require [whodunit.core :refer :all]
            [clojure.core.logic :refer :all]
            [clojure.core.logic.fd :as fd]
            [clojure.tools.macro :as macro]))

;; prevent linting issues with macros.
(declare x-idx y-idx)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;; The Zebra Puzzle. see https://en.wikipedia.org/wiki/Zebra_Puzzle
;;
;; holding house order constant, there are five house attributes and five houses -> (5!)^5 or 24.9B possibilities
;; see script/zebra.clj for benchmarking and performance thoughts.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; zebrao-vec is a the conventional approach using vectors
;; Result binding is a vec of [house-idx house-color nationality drinks smokes pet] vecs
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
        ;; this ordering from core.logic benchmark v. original problem statement improves latency from 650ms to 13ms!!!
        (membero [3 _ _ "milk" _ _] q)
        (membero [1 _ "norwegian" _ _ _] q)
        (house-ordero nexto [_ _ "norwegian" _ _ _] [_ "blue" _ _ _ _] q)
        (house-ordero righto [_ "ivory" _ _ _ _] [_ "green" _ _ _ _] q)
        (membero [_ "red" "englishman" _ _ _] q)
        (membero [_ "yellow" _ _ "kools" _] q)
        (membero [_ _ "spaniard" _ _ "dog"] q)
        (membero [_ "green" _ "coffee" _ _] q)
        (membero [_ _ "ukrainian" "tea" _ _] q)
        (membero [_ _ _ "orange-juice" "lucky-strike" _] q)
        (membero [_ _ "japanese" _ "parliaments" _] q)
        (membero [_ _ _ _ "old-gold" "snail"] q)
        (house-ordero nexto [_ _ _ _ "kools" _] [_ _ _ _ _ "horse"] q)
        (house-ordero nexto [_ _ _ _ "chesterfields" _] [_ _ _ _ _ "fox"] q)
        ;; implied by the questions
        (membero [_ _ _ "water" _ _] q)
        (membero [_ _ _ _ _ "zebra"] q))))))

;; zebrao is a more extensible approach using maps
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
           ;; note: I had some issue getting featurec to work with fresh variables so just unifying full maps instead
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
     ;; where house-idx's are 1-5 in order
     (let [answers (map #(new-rec {:house-idx %}) (range 1 6))]
       (all
        (== answers q)
        (membero (new-rec {:house-idx 3
                           :drinks "milk"}) q)
        (membero (new-rec {:house-idx 1
                           :nationality "norwegian"}) q)
        (ordero nexto :house-idx (new-rec {:nationality "norwegian"}) (new-rec {:house-color "blue"}) q)
        (ordero righto :house-idx (new-rec {:house-color "ivory"}) (new-rec {:house-color "green"}) q)
        (membero (new-rec {:house-color "red"
                           :nationality "englishman"}) q)
        (membero (new-rec {:house-color "yellow"
                           :smokes "kools"}) q)
        (membero (new-rec {:nationality "spaniard"
                           :pet "dog"}) q)
        (membero (new-rec {:house-color "green"
                           :drinks "coffee"}) q)
        (membero (new-rec {:nationality "ukrainian"
                           :drinks "tea"}) q)
        (membero (new-rec {:drinks "orange-juice"
                           :smokes "lucky-strike"}) q)
        (membero (new-rec {:nationality "japanese"
                           :smokes "parliaments"}) q)
        (membero (new-rec {:smokes "old-gold"
                           :pet "snail"}) q)
        (ordero nexto :house-idx (new-rec {:smokes "kools"}) (new-rec {:pet "horse"}) q)
        (ordero nexto :house-idx (new-rec {:smokes "chesterfields"}) (new-rec {:pet "fox"}) q)
        ;; implied by the questions
        (membero (new-rec {:drinks "water"}) q)
        (membero (new-rec {:pet "zebra"}) q))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;; As whodunit puzzle generation configs
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; whoduinit puzzle generation config for the zebra puzzle
;; note: name renamed from nationality in order to work with the puzzle generation functionality
(def config {:values {:name ["englishman" "japanese" "norwegian" "spaniard" "ukrainian"]
                      :house-idx [1 2 3 4 5]
                      :house-color ["blue" "green" "ivory" "red" "yellow"]
                      :drinks ["coffee" "milk" "orange-juice" "tea" "water"]
                      :smokes ["chesterfields" "kools" "lucky-strike" "old-gold" "parliaments"]
                      :pet ["dog" "fox" "horse" "snail" "zebra"]}})

;; zebra-goals returns the puzzle rules as a vector of goals. This enables introspection and reorderings which could not
;; be done if just handled as an opaque (all ...) goal.
(defn zebra-goals [q]
  (letfn [(righto [x y]
                  (fd/+ x 1 y))
          (nexto [x y]
                 (conde [(fd/+ x 1 y)]
                        [(fd/- x 1 y)]))
          (ordero [config idx-rel key x y hs]
                  (fresh [x-idx y-idx]
                         (membero x hs)
                         (membero y hs)
                         (== x (new-rec config {key x-idx}))
                         (== y (new-rec config {key y-idx}))
                         (idx-rel x-idx y-idx)))]
    [;; this ordering from core.logic benchmark v. original problem statement improves latency from 1200ms to 23ms!!!
     (membero (new-rec config {:house-idx 3
                               :drinks "milk"}) q)
     (membero (new-rec config {:house-idx 1
                               :name "norwegian"}) q)
     (ordero config nexto :house-idx (new-rec config {:name "norwegian"}) (new-rec config {:house-color "blue"}) q)
     (ordero config righto :house-idx (new-rec config {:house-color "ivory"}) (new-rec config {:house-color "green"}) q)
     (membero (new-rec config {:house-color "red"
                               :name "englishman"}) q)
     (membero (new-rec config {:house-color "yellow"
                               :smokes "kools"}) q)
     (membero (new-rec config {:name "spaniard"
                               :pet "dog"}) q)
     (membero (new-rec config {:house-color "green"
                               :drinks "coffee"}) q)
     (membero (new-rec config {:name "ukrainian"
                               :drinks "tea"}) q)
     (membero (new-rec config {:drinks "orange-juice"
                               :smokes "lucky-strike"}) q)
     (membero (new-rec config {:name "japanese"
                               :smokes "parliaments"}) q)
     (membero (new-rec config {:smokes "old-gold"
                               :pet "snail"}) q)
     (ordero config nexto :house-idx (new-rec config {:smokes "kools"}) (new-rec config {:pet "horse"}) q)
     (ordero config nexto :house-idx (new-rec config {:smokes "chesterfields"}) (new-rec config {:pet "fox"}) q)
     ;; implied by the questions
     (membero (new-rec config {:drinks "water"}) q)
     (membero (new-rec config {:pet "zebra"}) q)]))

;; zebrao-whodunit implements a zebrao goal using the zebra-goals vector.
;; NOTE: current zebra rules actually allow for some degenerate solutions where some values are duplicated. We handle
;; this correctly in puzzle but not in these original zebra examples. They happen to force a unique solution in when
;; at least the house idx's are forced to be unique; not so if we just do that for nationality.
(defn zebrao-whodunit [q]
  (and* (concat [(== q (map #(new-rec config {:house-idx %}) (range 1 6)))]
                (zebra-goals q))))
