(ns script.generate-fast-fixed-order
  (:gen-class)
  (:refer-clojure :exclude [==])
  (:require [whodunit.core :refer :all]))

;; when first adding this, generating for config 4...
;; generate_exhaustive.clj            24000 ms
;; generate.clj                       17000 ms
;; generate_fast.clj                   1600 ms
;; generate_fast_fixed_order.clj        310 ms

;; important to consider if config can even be solved with currently supported rules. e.g. duplicate values make membero
;; relations far less effective.
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
              5 {:values {:name ["alice" "bob" "carol" "dave" "eve"]
                          ;; :guilty [true false false false false]
                          ;; :color ["red" "green" "blue" "yellow" "white"]
                          :location ["park" "pier" "palace" "plaza" "parlor"]
                          :item ["comb" "cowl" "coin" "cap" "crowbar"]}}})

(println "---------- Logic Puzzle Generation ----------")
(println "Generating...")
(let [config-size (if (>= (count *command-line-args*) 2)
                    (Integer/parseInt (second *command-line-args*))
                    3)
      config (get configs config-size)
      rules (time (puzzle-fast-fixed-order config))]
  (println "\nConfig:\n" config)
  (println "\nRules:")
  (doseq [[idx item] (map-indexed vector (rules-text rules))]
    (println (str (inc idx) ".") item)))