(ns script.rule-order
  (:gen-class)
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic :refer :all]
            [whodunit.core :refer :all]
            [whodunit.zebra :as z]))

(def timeout-ms 1000)

(defmacro with-time
  "Evaluates expr returns the time it as millis. Does not return the value of expr."
  [expr]
  `(let [start# (. System (nanoTime))
         ret# ~expr]
     {:result ret#
      :millis (/ (double (- (. System (nanoTime)) start#)) 1000000.0)}))

(defn with-timeout
  "Evaluate unary function f with timeout in millis. Returns :timed-out if timeout is exceeded."
  [f timeout-ms]
  (let [executor (java.util.concurrent.Executors/newSingleThreadExecutor)
        future (java.util.concurrent.FutureTask. f)]
    (.submit executor future)
    (try
      (.get future timeout-ms java.util.concurrent.TimeUnit/MILLISECONDS)
      (catch java.util.concurrent.TimeoutException e
        (do
          (.cancel future true)
          :timed-out))
      (finally
        (.shutdown executor)))))

;; Approach
;; randomly sort rules
;; rotate them until best perf and then pin it.
;; name the rules

(defn time-puzzle [config hs goals]
  (let [res (with-timeout
              #(with-time (puzzle config
                                  hs
                                  (vec (map-indexed (fn [idx x] {:idx idx :goal x}) goals))))
              timeout-ms)]
    (if (= :timed-out res)
      timeout-ms
      (:millis res))))

;; TODO: should not be generating puzzle. just run but we need to thread the lvar through
(defn time-run+ [goal]
  (let [res (with-timeout
              #(with-time (run+ goal))
              timeout-ms)]
    (if (= :timed-out res)
      timeout-ms
      (:millis res))))

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

;; TODO: fix bug. this fails to properly terminate after run via lein exec

;; UPDATE: put this on pause. managing building of the incrementally sorted order seems a bit annoying i guess you could
;; track a record of all the indices and then rebuild it on each outer loop?

;; (let [res (doall (map (fn [i]
;;                         (let [hs (lvar)
;;                               ;; take the last n of m runs so that it is warmed up
;;                               times (take-last 2 (repeatedly 3 #(time-puzzle z/config hs (nth (iterate rotate (z/zebra-goals hs)) i))))]
;;                           (println (avg times))
;;                           (avg times)))
;;                       (range (dec (count (z/zebra-goals (lvar)))))))]
;;   (println res))

(let [res (doall (map (fn [i]
                        (let [;; take the last n of m runs so that it is warmed up
                              times (take-last 2 (repeatedly 3 #(time-run+ (fn [q]
                                                                             (and*
                                                                              (nth (iterate rotate (z/zebra-goals q)) i))))))]
                          (println (avg times))
                          (avg times)))
                      (range (dec (count (z/zebra-goals (lvar)))))))]
  (println res))
