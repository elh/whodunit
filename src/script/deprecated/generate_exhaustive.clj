(ns script.deprecated.generate-exhaustive
  (:gen-class)
  (:refer-clojure :exclude [==])
  (:require [whodunit.core :refer :all]
            [whodunit.deprecated.core :refer :all]
            [script.config :as c]
            [clojure.pprint :as pp]))

(println "Generating...")
(let [config-key (if (>= (count *command-line-args*) 2)
                   (second *command-line-args*)
                   "3")
      config (get c/configs config-key)
      res (time (puzzle-exhaustive config))
      rules (:rules res)]
  (when (nil? config)
    (throw (Exception. (str "No config found for key: " config-key))))
  (println "\nConfig:")
  (pp/pprint config)
  (println "\nRules:")
  (doseq [[idx item] (map-indexed vector (rules-text rules))]
    (println (str (inc idx) ".") item))
  (println "\nSolution:")
  (pp/pprint (:soln res)))
