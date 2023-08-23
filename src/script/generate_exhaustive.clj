(ns script.generate-exhaustive
  (:gen-class)
  (:refer-clojure :exclude [==])
  (:require [whodunit.core :refer :all]
            [script.config :as c]))

(println "---------- Logic Puzzle Generation ----------")
(println "Generating...")
(let [config-key (if (>= (count *command-line-args*) 2)
                   (second *command-line-args*)
                   "3")
      config (get c/configs config-key)
      rules (time (puzzle-exhaustive config))]
  (when (nil? config)
    (throw (Exception. (str "No config found for key: " config-key))))
  (println "\nConfig:\n" config)
  (println "\nRules:")
  (doseq [[idx item] (map-indexed vector (rules-text rules))]
    (println (str (inc idx) ".") item)))
