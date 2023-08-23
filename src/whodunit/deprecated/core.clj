(ns whodunit.deprecated.core
  (:gen-class)
  (:refer-clojure :exclude [==])
  (:require [whodunit.core :refer :all]
            [clojure.core.logic :refer :all]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;; Original implementations of `puzzle` that were improved upon and benchmarked
;;
;; In order they were added. Comments document the thinking.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Generates a (janky) logic puzzle! It generates a full set of rules up front using techniques that are not suitable to
;; large solution spaces. The number of possible solutions is the product of the number of unique orderings for each
;; non :name field. In the default case where all values are unique this is (n!)^m. yikes!
;;
;; Config :values define the set of possible records key-values. unique :name values are required.
;; Returns a list of rules with a :goal function and structured :data map
;; No rules are added that do not add new information to the solution space. This sets a decent ceiling on the number of
;; rules required.
(defn puzzle-exhaustive [config]
  (let [hs (lvar)                            ;; so rules can be declared outside of run
        lvars (init-lvars config)]
    (loop [rules [] soln-count nil]
      (let [new-rules (conj rules (generate-rule config hs))
            res (time (run+ (fn [q]
                              (and*
                               (conj (map #(:goal %) new-rules)
                                     (== hs q)
                                     (== q (get lvars :records))
                                     ;; pin order of :name to prevent redundant solutions
                                     (== (get-in config [:values :name]) (get-in lvars [:values :name]))
                                     ;; defining solution space given the config. this could be made more flexible
                                     (everyg (fn [k] (permuteo (get-in config [:values k]) (get-in lvars [:values k])))
                                             (keys (get lvars :values)))))) {:soln-count? true}))]
        (if (or (nil? (:soln res))
                (= soln-count (:soln-count res)))
          (recur rules soln-count)
          (do
            (when DEBUG
              (println "DEBUG - added rule:" (count new-rules) "rules," (:soln-count res) "possible solutions"))
            (if (and (:grounded? res) (= (:soln-count res) 1))
              {:rules new-rules
               :soln (:soln res)}
              (recur new-rules (:soln-count res)))))))))

;; Newer approach
;;
;; Generate new rules until we have a fully grounded solution. This does not exhaustively generate possible solutions
;; from run+ making individual iterations faster; however, that means we cannot avoid adding rules that do not reduce
;; the solution space. Some runs are still very slow even when only asking for 2 results and I think that is in the
;; case there are no results but it is hard to verify...
;;
;; Still not improve it much. The best way to generate the rule set would be to have a specific real expected solution
;; and derive rules against that. Originally, I didn't want to give up the flexibility of not being able to change
;; things as I went. This is because I actually expect to use this in an iterative way.
(defn puzzle-optimistic [config]
  (let [hs (lvar)                            ;; so rules can be declared outside of run
        lvars (init-lvars config)]
    (loop [rules []]
      (let [new-rule (generate-rule config hs)]
        ;; prevent identical duplicate rules
        (if (contains? (set (map #(:data %) rules)) (:data new-rule))
          (recur rules)
          (let [new-rules (conj rules new-rule)
                res (time (run+ (fn [q]
                                  (and*
                                   (conj (map #(:goal %) new-rules)
                                         (== hs q)
                                         (== q (get lvars :records))
                                         ;; pin order of :name to prevent redundant solutions
                                         (== (get-in config [:values :name]) (get-in lvars [:values :name]))
                                         ;; defining solution space given the config. this could be made more flexible
                                         (everyg (fn [k] (permuteo (get-in config [:values k]) (get-in lvars [:values k])))
                                                 (keys (get lvars :values))))))))]
            (when DEBUG
              (println "DEBUG - run+: grounded? =" (:grounded? res) ", has-more? =" (:has-more? res)))
            (if (nil? (:soln res))
              (recur rules)
              (do
                (when DEBUG
                  (println "DEBUG - added rule:" (count new-rules) "rules"))
                (if (and (:grounded? res) (not (:has-more? res)))
                  {:rules new-rules
                   :soln (:soln res)}
                  (recur new-rules))))))))))

;; A faster approach. Instead of generating a random rule and hoping it conforms to the solution space, instead use a
;; known good solution and build the rule from that. This trades off the flexibility of being able to change the
;; solution space as you go though.
;;
;; Next challenge will be avoiding creating redundant rules when dealing with large solution spaces and last rules.
;; The biggest issue seems to be the final run+ that will prove there is exactly one ground solution. this effectively
;; triggers a full scan.
;; We seem to also suffer in large solution spaces since we are running an arbitrary order of goals that is likely not
;; optimally ordered. When pre-processing some goals, we could try to derive that emperically but that does not help
;; us with 0-to-1 latency here.
(defn puzzle-fast [config]
  (let [hs (lvar)                             ;; so rules can be declared outside of run
        lvars (init-lvars config)
        ;; shuffled once up front
        config (assoc config :values (reduce-kv (fn [m k v] (assoc m k (shuffle v)))
                                                {}
                                                (get config :values)))]
    (loop [rules []
           last-soln nil]                     ;; last candidate solution from last run+
      (let [new-rule (if (nil? last-soln)
                       (generate-rule config hs)
                       (generate-rule-from-soln last-soln config hs))]
        ;; prevent identical duplicate rules
        (if (contains? (set (map #(:data %) rules)) (:data new-rule))
          (do
            (when DEBUG
              (println "DEBUG - duplicate rule"))
            (recur rules last-soln))
          (let [new-rules (conj rules new-rule)
                res (time (run+ (fn [q]
                                  (and*
                                   (conj (map #(:goal %) new-rules)
                                         (== hs q)
                                         (== q (get lvars :records))
                                         ;; pin order of :name to prevent redundant solutions. do not use shuffled!
                                         (== (get-in config [:values :name]) (get-in lvars [:values :name]))
                                         ;; defining solution space given the config. this could be made more flexible
                                         (everyg (fn [k] (permuteo (get-in config [:values k]) (get-in lvars [:values k])))
                                                 (keys (get lvars :values))))))))]
            (when DEBUG
              (println "DEBUG - run+: grounded? =" (:grounded? res) ", has-more? =" (:has-more? res)))
            (if (nil? (:soln res))
              (recur rules last-soln)
              (do
                (when DEBUG
                  (println "DEBUG - added rule:" (count new-rules) "rules")
                  (println "DEBUG - rule code:" (:code new-rule)))
                (if (and (:grounded? res) (not (:has-more? res)))
                  (do
                    (when DEBUG
                      (println "DEBUG - done: soln =" (:soln res)))
                    {:rules new-rules
                     :soln (:soln res)})
                  (recur new-rules (:soln res)))))))))))

;; Fixed a big error in past implementations. We need to make sure the order of rules are passed in correctly to run+.
;; It is important to have the permuteo rule early in the list of goals to constrain the search.
;; Supports taking in rules as an argument which can be useful for benchmarking.
;;
;; The big improvement we make after this is to run the super heavy weight permuteo rule last.
(defn puzzle-fast-fixed-order
  ([config] (puzzle-fast-fixed-order config (lvar) []))
  ;; hs is an lvar defined outside of run so we can inject rules
  ([config hs rules]
   (let [lvars (init-lvars config)
         ;; shuffled once up front
         config (assoc config :values (reduce-kv (fn [m k v] (assoc m k (shuffle v)))
                                                 {}
                                                 (get config :values)))]
     (loop [rules (vec rules)                  ;; rules as vector. we need conj to the end because goal order matters
            last-soln nil]                     ;; last candidate solution from last run+
       ;; don't add a new rule on first iteration. we want to terminate if initial rules are already complete
       (let [new-rule (when (some? last-soln)
                        (if (nil? last-soln)
                          (generate-rule config hs)
                          (generate-rule-from-soln last-soln config hs)))]
         ;; prevent identical duplicate rules
         (if (and (some? new-rule) (contains? (set (map #(:data %) rules)) (:data new-rule)))
           (do
             (when DEBUG (println "DEBUG - duplicate rule"))
             (recur rules last-soln))
           (let [new-rules (if (some? new-rule) (conj rules new-rule) rules)
                 res (time (run+ (fn [q]
                                   (and*
                                    (into [] (concat
                                              [(== hs q)
                                               (== q (get lvars :records))
                                               ;; pin order of :name to prevent redundant solutions. do not use shuffled!
                                               (== (get-in config [:values :name]) (get-in lvars [:values :name]))
                                               ;; defining solution space given the config. this could be made more flexible
                                               (everyg (fn [k] (permuteo (get-in config [:values k]) (get-in lvars [:values k])))
                                                       (keys (get lvars :values)))]
                                              (mapv #(:goal %) new-rules)))))))]
             (when DEBUG (println "DEBUG - run+: grounded? =" (:grounded? res) ", has-more? =" (:has-more? res)))
             (if (nil? (:soln res))
               (if (some? new-rule)
                 (recur rules last-soln)
                 ;; starting rules were bad
                 (throw (Exception. "Initial rules provided to puzzle-fast have no valid solution")))
               (do
                 (when (and DEBUG (some? new-rule))
                   (println "DEBUG - added rule:" (count new-rules) "rules")
                   (println "DEBUG - rule code:" (:code new-rule)))
                 (if (and (:grounded? res) (not (:has-more? res)))
                   (do
                     (when DEBUG (println "DEBUG - done: soln =" (:soln res)))
                     {:rules new-rules
                      :soln (:soln res)})
                   (recur new-rules (:soln res))))))))))))
