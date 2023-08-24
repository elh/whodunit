(ns script.rule-order
  (:gen-class)
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic :refer :all]
            [whodunit.core :refer :all]
            [whodunit.zebra :as z]))

(defmacro with-time
  "Evaluates expr returns the time it as millis. Does not return the value of expr."
  [expr]
  `(let [start# (. System (nanoTime))
         ret# ~expr]
     {:result ret#
      :millis (/ (double (- (. System (nanoTime)) start#)) 1000000.0)}))

;; Approach
;; randomly sort rules
;; rotate them until best perf and then pin it.
;; name the rules

(defn time-puzzle [config hs goals]
  (:millis (with-time (puzzle config
                              hs
                              (vec (map-indexed (fn [idx x] {:idx idx :goal x}) goals))))))

(defn avg [coll]
  (/ (reduce + 0 coll) (count coll)))

(defn rotate [v]
  (let [v (vec v)
        size (count v)
        rotation-mod (mod 1 size)]
    (vec (concat (subvec v rotation-mod)
                 (subvec v 0 rotation-mod)))))

;; (defn rotations [v]
;;   (take (count v) (iterate rotate v)))

(println (map (fn [i]
                (let [hs (lvar)
                      times (take-last 2 (repeatedly 3 #(time-puzzle z/config hs (nth (iterate rotate (z/zebra-goals hs)) i))))]
                  (println (avg times))))
              (range 15)))
