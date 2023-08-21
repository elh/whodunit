(ns whodunit.core
  (:gen-class)
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic :refer :all]))

(def DEBUG false)

;; Evaluates if input is fully grounded. Returns true if input contains no core.logic symbols like `'_0`.
;;
;; TODO: built-in core.logic approach?
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Logic Puzzle Generation
;; The solution space is defined as a set of records identified by a unique :name values.
;;
;; Example:
;; 3 people: alice, bob, and carol
;; 3 clothing colors: red, blue, green
;; 3 locations: park, pier, and palace
;; 1 person is guilty; the other 2 are innocent
;; Question: Who killed dave?

;; Sets up lvars based on config
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

;; Take partial map and hydrate it for all unspecified keys. useful for unification.
(defn new-rec [config partial]
  (merge (reduce-kv (fn [m k _] (assoc m k (lvar)))
                    {}
                    (get config :values)) partial))

;; Only simple membero rules supported
;;
;; TODO: add some user-defined constraints. e.g. never generate a rule that just gives away who is guilty
(defn generate-rule [config q]
  (let [ks (keys (get config :values))
        k1 (rand-nth ks)
        k2 (first (shuffle (remove #{k1} ks)))
        v1 (rand-nth (get-in config [:values k1]))
        v2 (rand-nth (get-in config [:values k2]))
        kvs #{[k1 v1]
              [k2 v2]}]
    (when DEBUG (println "DEBUG - generate-rule: type = membero, kvs =" kvs))
    {:data {:type :membero
            :kvs kvs}
     :goal (membero (new-rec config {k1 v1
                                     k2 v2}) q)}))

;; Generates a (janky) logic puzzle! It generates a full set of rules up front using techniques that are not suitable to
;; large solution spaces. The number of possible solutions is the product of the number of unique orderings for each
;; non :name field. In the default case where all values are unique this is (n!)^m. yikes!
;;
;; Config :values define the set of possible records key-values. unique :name values are required.
;; Returns a list of rules with a :goal function and structured :data map
;; No rules are added that do not add new information to the solution space.
;;
;; TODO: stop based on a user-defined condition. e.g. "we know who is guilty"
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
          (if (and (:grounded? res) (= (:soln-count res) 1))
            new-rules
            (do
              (when DEBUG
                (println "DEBUG - added rule:" (count new-rules) "rules," (:soln-count res) "possible solutions"))
              (recur new-rules (:soln-count res)))))))))

(defn puzzle-base-count [config]
  (let [lvars (init-lvars config)
        res (run+ (fn [q]
                    (and*
                     (list
                      (== q (get lvars :records))
                      ;; pin order of :name to prevent redundant solutions
                      (== (get-in config [:values :name]) (get-in lvars [:values :name]))
                      ;; defining solution space given the config. this could be made more flexible
                      (everyg (fn [k] (permuteo (get-in config [:values k]) (get-in lvars [:values k])))
                              (keys (get lvars :values)))))) {:soln-count? true})]
    (:soln-count res)))

;; Jank text generation
(defn rules-text [rules]
  (map (fn [r]
         (let [kvs (:kvs (:data r))]
           (str (name (ffirst kvs)) " is " (second (first kvs)) " and "
                (name (first (second kvs))) " is " (second (second kvs)))))
       rules))
