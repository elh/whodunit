(ns whodunit.core
  (:gen-class)
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic :refer :all]))

(def DEBUG false)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Logic Puzzle Generation
;; Given a user-defined solution space, generate a set of core.logic rules that arrives at a single solution.
;; The solution space is defined as a set of uniquely `:name`-ed map records.
;;
;; Example:
;; The goal is to generate a rule set to answer "who killed dave?"
;; * 3 suspects last night: alice, bob, and carol
;; * 3 colors they were wearing: red, blue, green
;; * 3 locations they were at: park, pier, and palace
;; * 2 of them are innocent. 1 is guilty!
;;
;; Logic puzzles scale (n!)^m where n is the number of values for each key and m is the number of keys. This is the base
;; case where each key has a unique value that will be assigned to a single value.
;;
;; See `run+` and `puzzle`.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Returns true if input is grounded as defined by not containing any core.logic symbols like `'_0`.
;; Replace this with a built-in core.logic solution if it exists.
(defn grounded? [x]
  (not-any? (fn [x] (and (instance? clojure.lang.Symbol x)
                         (boolean (re-matches #"_\d+" (name x)))))
            (tree-seq coll? seq x)))

;; Runs the goal returning the first solution, if it's grounded, and if there are more solutions.
;; If :soln-count? option is true, we will find all results and return the total result count.
(defn run+
  ([goal] (run+ goal {}))
  ([goal opts]
   (let [res (if (:soln-count? opts)
               (run* [q] (goal q))
               (run 2 [q] (goal q)))
         soln-count (count res)
         soln (first res)
         out {:soln soln
              :grounded? (grounded? soln)
              :has-more? (> soln-count 1)}]
     (if (:soln-count? opts)
       (assoc out :soln-count soln-count)
       out))))

;; Sets up lvars based on a whodunit puzzle config. This is useful for constructing relations.
;;
;; :values is a map of keys to a list of lvars representing the possible values for that key
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

;; Take a partial map and hydrate it for all unspecified keys. This is useful for unification because featurec does not
;; seem to work with additional fresh variables.
(defn new-rec [config partial]
  (merge (reduce-kv (fn [m k _] (assoc m k (lvar)))
                    {}
                    (get config :values)) partial))

;; Generate a new candidate rule for a puzzle being created.
;;
;; TODO: support more rule (hint) types. I am starting very simple but this is easily extensible. Take inspiration from
;;       logic puzzles and add rules thematic to murder mysteries.
;; TODO: add some user-defined constraints. e.g. never generate a rule that on its own gives away who is guilty
(defn generate-rule [config q]
  (let [ks (keys (get config :values))
        k1 (rand-nth ks)
        k2 (first (shuffle (remove #{k1} ks)))
        v1 (rand-nth (get-in config [:values k1]))
        v2 (rand-nth (get-in config [:values k2]))
        kvs {k1 v1
             k2 v2}]
    (when DEBUG (println "DEBUG - generate-rule: type = membero, kvs =" kvs))
    {:data {:type :membero
            :kvs kvs}
     :code (format "(membero (new-rec config {%s \"%s\" %s \"%s\"}) q)", k1, v1, k2, v2)
     :goal (membero (new-rec config kvs) q)}))

;; Generate a new candidate rule for a puzzle but directly against a target solution.
(defn generate-rule-from-soln [soln config q]
  (let [record (rand-nth soln)
        ks (keys record)
        k1 (rand-nth ks)
        k2 (first (shuffle (remove #{k1} ks)))
        kvs (select-keys record [k1 k2])]
    (when DEBUG (println "DEBUG - generate-rule: type = membero, kvs =" kvs))
    {:data {:type :membero
            :kvs kvs}
     :code (format "(membero (new-rec config {%s \"%s\" %s \"%s\"}) q)", k1, (get record k1), k2, (get record k2))
     :goal (membero (new-rec config kvs) q)}))

;; Given a config (and an optional set of starting rules), generate a logic puzzle. We incrementally generate and
;; include rules that narrow the solution space until we arrive at a single solution.
;;
;; Config :values define the set of possible records key-values. unique :name values are required.
;; Returns the solution and a list of rules with a :goal function, structured :data map, and :code text.
;;
;; TODO: support constraining the solution that are not presented as rules. e.g. "the solution must be that dave is
;;       guilty, but that should not be directly given away by a rule"
;; TODO: make the additional details on rules optional. probably only the actual goal is strictly required.
;; TODO: intelligently sort rules to optimize solving
;; TODO: stop based on a user-defined condition. e.g. "we know who is guilty"
;; TODO: shuffle config on each iteration so real solution isn't actually pre-defined
;; TODO: support an interactive mode of puzzle generation where all rules are not all created at once
;; TODO: steer generation to produce "good" puzzles. e.g. at a tunable level of difficulty
;; TODO: benchmark
(defn puzzle
  ([config] (puzzle config (lvar) []))
  ;; hs is an lvar defined outside of run so we can inject rules
  ;; rules as a vector of goals relative to hs. A vector enables introspection and reorderings which could not
  ;; be done if just handed an opaque `all` goal.
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
                 res (run+ (fn [q]
                             (and*
                              ;; rules are ordered to limit the solution space as quickly as possible
                              (into [] (concat
                                        [(== hs q)
                                         (== q (get lvars :records))
                                         ;; pin order of :name to prevent redundant solutions. do not use shuffled!
                                         (== (get-in config [:values :name]) (get-in lvars [:values :name]))]
                                        ;; puzzle-specific rules
                                        (mapv #(:goal %) new-rules)
                                        ;; defining solution space given the config. this could be made more flexible
                                        [(everyg (fn [k] (permuteo (get-in config [:values k]) (get-in lvars [:values k])))
                                                 (keys (get lvars :values)))])))))]
             ;; (println ".")
             (when DEBUG (println "DEBUG - run+: grounded? =" (:grounded? res) ", has-more? =" (:has-more? res)))
             (if (nil? (:soln res))
               (if (some? new-rule)
                 (recur rules last-soln)
                 (throw (Exception. "Initial rules provided to puzzle-fast have no valid solution")))
               (do
                 (when (and DEBUG (some? new-rule)) (println "DEBUG - added rule:" (count new-rules) "rules\nDEBUG - rule code:" (:code new-rule)))
                 (if (and (:grounded? res) (not (:has-more? res)))
                   (do
                     (when DEBUG (println "DEBUG - done: soln =" (:soln res)))
                     {:rules new-rules
                      :soln (:soln res)})
                   (recur new-rules (:soln res))))))))))))

;; Jank text generation. Note that when presenting, you will probably want to randomize rule order.
;; TODO: improve this...
(defn rules-text [rules]
  (map (fn [r]
         (let [kvs (:kvs (:data r))]
           (str (name (ffirst kvs)) " is " (second (first kvs)) " and "
                (name (first (second kvs))) " is " (second (second kvs)))))
       rules))
