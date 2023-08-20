(ns whodunit.core
  (:gen-class)
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic :refer :all]
            [clojure.core.logic.fd :as fd]
            [clojure.tools.macro :as macro]
            [clojure.pprint :as pp]))

(def DEBUG false)
(def DEBUG-ALL-SOLNS false)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; The Zebra Puzzle. see https://en.wikipedia.org/wiki/Zebra_Puzzle

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

;; My preferred, more extensible map approach
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
;;
;; TODO: built-in core.logic approach?
(defn grounded? [x]
  (not-any? (fn [x] (and (instance? clojure.lang.Symbol x)
                         (boolean (re-matches #"_\d+" (name x)))))
            (tree-seq coll? seq x)))

;; Runs the goal returning the first solution, if it's grounded, and if there are more solutions.
;; Note that this function takes a lot longer than the underlying call to run*.
(defn run+
  ([goal] (run+ goal false))
  ([goal all-solns?]
   (let [res (run* [q] (goal q))
         soln (first res)
         out {:soln soln
              :grounded? (grounded? soln)
              :has-more? (> (count res) 1)
              :soln-count (count res)}]
     (if all-solns?
       (assoc out :solns res)
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

;; Generates a (janky) logic puzzle!
;; Config :values define the set of possible records key-values. unique :name values are required.
;; Returns a list of rules with :goal function and structured :data map
;; No fully redundant rules are included.
;;
;; The number of possible solutions is the product of the number of unique orderings for each non :name field
;; In the default case where all values are unique this is (n!)^m!
;;
;; TODO: stop based on a user-defined condition. e.g. "we know who is guilty"
(defn puzzle [config]
  (let [hs (lvar)                            ;; so rules can be declared outside of run
        lvars (init-lvars config)]
    (loop [rules [] soln-count nil]
      (let [new-rules (conj rules (generate-rule config hs))
            res (run+ (fn [q]
                        (and*
                         (conj (map #(:goal %) new-rules)
                               (== hs q)
                               (== q (get lvars :records))
                               ;; pin order of :name to prevent redundant solutions
                               (== (get-in config [:values :name]) (get-in lvars [:values :name]))
                               ;; defining solution space given the config. this could be made more flexible
                               (everyg (fn [k] (permuteo (get-in config [:values k]) (get-in lvars [:values k])))
                                       (keys (get lvars :values)))))) DEBUG-ALL-SOLNS)]
        (if (or (nil? (:soln res))
                (= soln-count (:soln-count res)))
          (recur rules soln-count)
          (if (and (:grounded? res) (= (:soln-count res) 1))
            new-rules
            (do
              (when DEBUG
                (println "DEBUG - added rule:" (count new-rules) "rules," (:soln-count res) "possible solutions")
                (when DEBUG-ALL-SOLNS
                  (println "DEBUG - solns:")
                  (pp/pprint (:solns res))))
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
                              (keys (get lvars :values)))))) DEBUG-ALL-SOLNS)]
    (:soln-count res)))

;; Jank text generation
(defn rules-text [rules]
  (map (fn [r]
         (let [kvs (:kvs (:data r))]
           (str (name (ffirst kvs)) " is " (second (first kvs)) " and "
                (name (first (second kvs))) " is " (second (second kvs)))))
       rules))

(defn -main []
  (println "---------- Logic Puzzle Generation ----------")
  (let [example-config {:values {:name ["alice" "bob" "carol"]
                                 :guilty [true false false]
                                 :color ["red" "blue" "green"]
                                 :location ["park" "pier" "palace"]}}
        rules (time (puzzle example-config))]
    (println "\nConfig:\n" example-config)
    (println "\nRules:")
    (doseq [[idx item] (map-indexed vector (rules-text rules))]
      (println (str (inc idx) ".") item)))

;;     (println (time (puzzle-base-count example-config))))
;;
;;   (println "\n---------- Zebra Puzzle - using vectors ----------")
;;   (pp/pprint (run+ zebrao-vec))
;;   (println "\n---------- Zebra Puzzle - using maps ----------")
;;   (pp/pprint (run+ zebrao))
  )

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
