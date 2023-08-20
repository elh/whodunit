(ns whodunit.core
  (:gen-class)
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic :refer :all]
            [clojure.core.logic.fd :as fd]
            [clojure.tools.macro :as macro]
            [clojure.pprint :as pp]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; The Zebra Puzzle. see https://en.wikipedia.org/wiki/Zebra_Puzzle

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

;; my preferred, more extensible map approach
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

;; Evaluates if input is fully grounded. Returns true if input contains no core.logic symbols like `'_0`.
;; TODO: If built-in core.logic approach is found, replace this.
(defn grounded? [x]
  (not-any? (fn [x] (and (instance? clojure.lang.Symbol x)
                         (boolean (re-matches #"_\d+" (name x)))))
            (tree-seq coll? seq x)))

;; Runs the goal with timing and returns the first solution, if it's grounded, and if there are more solutions.
;; note that this function takes a lot longer than the underlying call to run*. removing timing for now.
;; TODO: optionally return all solutions. useful for analysis. e.g. ">1 solution but A=true in all of them"
(defn run-with-context [goal]
  (let [res (run* [q] (goal q))
        soln (first res)
        is-grounded (grounded? soln)]
    {:soln soln
     :grounded? is-grounded
     :has-more? (> (count res) 1)
     :soln-count (count res)}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Generation of logic puzzle hints
;; Example
;; 3 people: alice, bob, and carol
;; 3 clothing colors: red, blue, green
;; 3 locations: park, pier, and palace
;; 1 person is guilty; the other 2 are innocent
;;
;; Question: Who killed dave?
;;
;; Facts: a concrete arrangement. however, there is also an approach where we don't actually bind the values,
;; but that may make rule generation more difficult

;; actually create a fully grounded set of records
;; this might help a lot with rule generation because brute force might be very slow. when a lot is already
;; specified, it might be hard to blindly generate the final rules.
(defn init-grounded-records [config]
  (let [shuffled (reduce-kv (fn [m k v] (assoc m k (shuffle v)))
                            {}
                            (get config :values))]
    (apply map
           (fn [& args] (apply hash-map (interleave (keys shuffled) args)))
           (vals shuffled))))

;; :values is a map of keys to a list of lvars
;; :records is those lvars zipped into records
(defn init-lvars [config]
  (let [values (reduce-kv (fn [m k v] (assoc m k (repeatedly (count v) lvar)))
                          {}
                          (get config :values))
        records (apply map
                       (fn [& args] (apply hash-map (interleave (keys values) args)))
                       (vals values))]
    {:values values
     :records records}))

;; take partial and hydrate it for all unspecified keys. useful for unification.
(defn new-rec [config partial]
  (merge (reduce-kv (fn [m k _] (assoc m k (lvar)))
                    {}
                    (get config :values)) partial))

;; only simple membero rules supported
;; TODO: add some constraints. e.g. never generate a rule that just gives away who is guilty
(defn generate-rule [config q]
  (let [ks (keys (get config :values))
        k1 (rand-nth ks)
        k2 (first (shuffle (remove #{k1} ks)))
        v1 (rand-nth (get-in config [:values k1]))
        v2 (rand-nth (get-in config [:values k2]))]
    (println "DEBUG - generate-rule:" k1 "=" v1 "," k2 "=" v2)
    (membero (new-rec config {k1 v1
                              k2 v2}) q)))

;; config values must always contain :name and :guilty
;; TODO: prevent redundant rules
;; TODO: only generate discriminating rules? ones that reduce the solution space?
;; TODO: stop based on a condition. e.g. "we know who is guilty"
(defn puzzle [config]
  (let [hs (lvar)                            ;; so rules can be declared outside of run
        lvars (init-lvars config)]
    (loop [rules '()]
      (let [new-rules (cons (generate-rule config hs) rules)
            res (run-with-context (fn [q]
                                    (and*
                                     (conj new-rules
                                           (== hs q)
                                           (== q (get lvars :records))
                                           ;; pin order of :name to prevent redundant solutions
                                           (== (get-in config [:values :name]) (get-in lvars [:values :name]))
                                           ;; defining solution space given the config. this could be made more flexible
                                           (everyg (fn [k] (permuteo (get-in config [:values k]) (get-in lvars [:values k])))
                                                   (keys (get lvars :values)))))))]
        (if (nil? (:soln res))
          (recur rules)
          (if (and (:grounded? res) (= (:soln-count res) 1))
            new-rules
            (do
              (println "DEBUG - added rule: (count new-rules) =" (count new-rules) "(:soln-count res) =" (:soln-count res))
              (recur new-rules))))))))

(defn -main []
  (let [example-config {:values {:name ["alice" "bob" "carol"]
                                 :guilty [true false false]
                                 :color ["red" "blue" "green"]
                                 :location ["park" "pier" "palace"]}}]
    (pp/pprint (puzzle example-config)))

  ;; (println "---------- Zebra Puzzle - using vectors ----------")
  ;; (pp/pprint (run-with-context zebrao-vec))
  ;; (println "\n---------- Zebra Puzzle - using maps ----------")
  ;; (pp/pprint (run-with-context zebrao))
  )
