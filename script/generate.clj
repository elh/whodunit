(ns script.generate
  (:gen-class)
  (:refer-clojure :exclude [==])
  (:require [whodunit.core :refer :all]))

(def configs {2 {:values {:name ["alice" "bob"]
                          :guilty [true false]
                          :color ["red" "blue"]
                          :location ["park" "pier"]}}
              3 {:values {:name ["alice" "bob" "carol"]
                          :guilty [true false false]
                          :color ["red" "blue" "green"]
                          :location ["park" "pier" "palace"]}}
              4 {:values {:name ["alice" "bob" "carol" "dave"]
                          :guilty [true false false false]
                          :color ["red" "blue" "green" "white"]
                          :location ["park" "pier" "palace" "plaza"]}}
              ;; note: 2 reds. 2 parks, 2 piers
              5 {:values {:name ["alice" "bob" "carol" "dave" "eve"]
                          :guilty [true false false false false]
                          :color ["red" "red" "green" "yellow" "blue"]
                          :location ["park" "park" "pier" "pier" "palace"]}}})

(println "---------- Logic Puzzle Generation ----------")
(println "Generating...")
(println *command-line-args*)
(let [config-size (if (>= (count *command-line-args*) 2)
                    (Integer/parseInt (second *command-line-args*))
                    3)
      config (get configs config-size)
      rules (time (puzzle-exhaustive config))]
  (println "\nConfig:\n" config)
  (println "\nRules:")
  (doseq [[idx item] (map-indexed vector (rules-text rules))]
    (println (str (inc idx) ".") item)))
