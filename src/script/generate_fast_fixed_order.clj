(ns script.generate-fast-fixed-order
  (:gen-class)
  (:refer-clojure :exclude [==])
  (:require [whodunit.core :refer :all]
            [script.config :as c]))

(println "---------- Logic Puzzle Generation ----------")
(println "Generating...")
(let [config-size (if (>= (count *command-line-args*) 2)
                    (Integer/parseInt (second *command-line-args*))
                    3)
      config (get c/configs config-size)
      rules (time (puzzle-fast-fixed-order config))]
  (println "\nConfig:\n" config)
  (println "\nRules:")
  (doseq [[idx item] (map-indexed vector (rules-text rules))]
    (println (str (inc idx) ".") item)))
