(ns script.debug-order
  (:gen-class)
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic :refer :all]
            [whodunit.core :refer :all]))

;;;;;;;; Testing ordering of rules for puzzle-fast
;; Learning:
;; all of the time goes into the final run 2 call that is looking for the second nonexistent solution.
;; every incremental run up until the final solution is very fast for 5 records w/ 3 attributes. ~3ms
;; There is no difference between run 2 and run*.
;;
;; no perf difference between original and custom

(def config {:values {:name ["alice" "bob" "carol" "dave" "eve"]
                      :location ["park" "pier" "palace" "plaza" "parlor"]
                      :item ["comb" "cowl" "coin" "cap" "crowbar"]}})

(defn original [q]
  (let [goals [(membero (new-rec config {:name "alice" :location "pier"}) q)
               ;; (membero (new-rec config {:location "x" :item "crowbar"}) q) ;; bad rule. testing if it fails fast. does not
               (membero (new-rec config {:name "eve" :location "plaza"}) q)
               (membero (new-rec config {:name "bob" :location "parlor"}) q)
               (membero (new-rec config {:item "comb" :location "plaza"}) q)
               (membero (new-rec config {:name "eve" :item "comb"}) q)
               (membero (new-rec config {:item "crowbar" :name "alice"}) q)
               (membero (new-rec config {:item "cap" :name "bob"}) q)
               (membero (new-rec config {:item "cowl" :name "carol"}) q)
               (membero (new-rec config {:location "pier" :item "crowbar"}) q)
               (membero (new-rec config {:location "park" :name "carol"}) q)]]
    (map (fn [x] {:goal x}) goals)))

(defn custom [q]
  (let [goals [(membero (new-rec config {:name "alice" :location "pier"}) q)
               (membero (new-rec config {:item "crowbar" :name "alice"}) q)
               (membero (new-rec config {:location "pier" :item "crowbar"}) q)
               (membero (new-rec config {:name "eve" :location "plaza"}) q)
               (membero (new-rec config {:name "eve" :item "comb"}) q)
               ;; (membero (new-rec config {:item "comb" :location "plaza"}) q) ;; redundant
               (membero (new-rec config {:name "bob" :location "parlor"}) q) 
               (membero (new-rec config {:item "cap" :name "bob"}) q)
               (membero (new-rec config {:item "cowl" :name "carol"}) q)
               (membero (new-rec config {:location "park" :name "carol"}) q)]]
    (map (fn [x] {:goal x}) goals)))


(println "5 records with :name and 2 other attributes. all attributes are unique. (5!)^3 = 1,728,000 possibilities\n")
(println "---------- Original order ----------")
(let [hs (lvar)]
  (time (puzzle-fast-fixed-order config hs (original hs))))

(println "---------- Custom order ----------")
(let [hs (lvar)]
  (time (puzzle-fast-fixed-order config hs (custom hs))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;; Original Generation
;; 31 seconds for the final run+ call. 32 seconds for total puzzle generation
;;
;;;; Config:
;; 5 records with :name and 2 other attributes. all attributes are unique
;; (5!)^3 = 1,728,000 possibilities
;;
;;;; Logs:
;; ---------- Logic Puzzle Generation ----------
;; Generating...
;; DEBUG - generate-rule: type = membero, kvs = {:name alice, :location pier}
;; "Elapsed time: 16.460583 msecs"
;; DEBUG - run+: grounded? = true , has-more? = true
;; DEBUG - added rule: 1 rules
;; DEBUG - rule code: (membero (new-rec config {:name "alice" :location "pier"}) q)
;; DEBUG - generate-rule: type = membero, kvs = {:name eve, :location plaza}
;; "Elapsed time: 4.35225 msecs"
;; DEBUG - run+: grounded? = true , has-more? = true
;; DEBUG - added rule: 2 rules
;; DEBUG - rule code: (membero (new-rec config {:name "eve" :location "plaza"}) q)
;; DEBUG - generate-rule: type = membero, kvs = {:name bob, :location parlor}
;; "Elapsed time: 3.560625 msecs"
;; DEBUG - run+: grounded? = true , has-more? = true
;; DEBUG - added rule: 3 rules
;; DEBUG - rule code: (membero (new-rec config {:name "bob" :location "parlor"}) q)
;; DEBUG - generate-rule: type = membero, kvs = {:item comb, :location plaza}
;; "Elapsed time: 4.157333 msecs"
;; DEBUG - run+: grounded? = true , has-more? = true
;; DEBUG - added rule: 4 rules
;; DEBUG - rule code: (membero (new-rec config {:item "comb" :location "plaza"}) q)
;; DEBUG - generate-rule: type = membero, kvs = {:name eve, :item comb}
;; "Elapsed time: 3.643083 msecs"
;; DEBUG - run+: grounded? = true , has-more? = true
;; DEBUG - added rule: 5 rules
;; DEBUG - rule code: (membero (new-rec config {:name "eve" :item "comb"}) q)
;; DEBUG - generate-rule: type = membero, kvs = {:location pier, :name alice}
;; DEBUG - duplicate rule
;; DEBUG - generate-rule: type = membero, kvs = {:item crowbar, :name alice}
;; "Elapsed time: 7.76575 msecs"
;; DEBUG - run+: grounded? = true , has-more? = true
;; DEBUG - added rule: 6 rules
;; DEBUG - rule code: (membero (new-rec config {:item "crowbar" :name "alice"}) q)
;; DEBUG - generate-rule: type = membero, kvs = {:item cap, :name bob}
;; "Elapsed time: 7.565625 msecs"
;; DEBUG - run+: grounded? = true , has-more? = true
;; DEBUG - added rule: 7 rules
;; DEBUG - rule code: (membero (new-rec config {:item "cap" :name "bob"}) q)
;; DEBUG - generate-rule: type = membero, kvs = {:name eve, :item comb}
;; DEBUG - duplicate rule
;; DEBUG - generate-rule: type = membero, kvs = {:name alice, :item crowbar}
;; DEBUG - duplicate rule
;; DEBUG - generate-rule: type = membero, kvs = {:item cowl, :name carol}
;; "Elapsed time: 79.071875 msecs"
;; DEBUG - run+: grounded? = true , has-more? = true
;; DEBUG - added rule: 8 rules
;; DEBUG - rule code: (membero (new-rec config {:item "cowl" :name "carol"}) q)
;; DEBUG - generate-rule: type = membero, kvs = {:item cap, :name bob}
;; DEBUG - duplicate rule
;; DEBUG - generate-rule: type = membero, kvs = {:name alice, :item crowbar}
;; DEBUG - duplicate rule
;; DEBUG - generate-rule: type = membero, kvs = {:location plaza, :item comb}
;; DEBUG - duplicate rule
;; DEBUG - generate-rule: type = membero, kvs = {:location pier, :item crowbar}
;; "Elapsed time: 75.38575 msecs"
;; DEBUG - run+: grounded? = true , has-more? = true
;; DEBUG - added rule: 9 rules
;; DEBUG - rule code: (membero (new-rec config {:location "pier" :item "crowbar"}) q)
;; DEBUG - generate-rule: type = membero, kvs = {:name alice, :item crowbar}
;; DEBUG - duplicate rule
;; DEBUG - generate-rule: type = membero, kvs = {:location park, :name carol}
;; "Elapsed time: 31809.727833 msecs"
;; DEBUG - run+: grounded? = true , has-more? = false
;; DEBUG - added rule: 10 rules
;; DEBUG - rule code: (membero (new-rec config {:location "park" :name "carol"}) q)
;; DEBUG - done: soln = ({:name dave, :item coin, :location palace} {:name alice, :item crowbar, :location pier} {:name carol, :item cowl, :location park} {:name eve, :item comb, :location plaza} {:name bob, :item cap, :location parlor})
;; "Elapsed time: 32018.757375 msecs"
;;
;; Config:
;;  {:values {:name [alice bob carol dave eve], :location [park pier palace plaza parlor], :item [comb cowl coin cap crowbar]}}
;;
;; Rules:
;; 1. name is alice and location is pier
;; 2. name is eve and location is plaza
;; 3. name is bob and location is parlor
;; 4. item is comb and location is plaza
;; 5. name is eve and item is comb
;; 6. item is crowbar and name is alice
;; 7. item is cap and name is bob
;; 8. item is cowl and name is carol
;; 9. location is pier and item is crowbar
;; 10. location is park and name is carol
