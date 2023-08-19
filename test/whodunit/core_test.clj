(ns whodunit.core-test
  (:require [clojure.test :refer :all]
            [whodunit.core :refer :all]
            [clojure.core.logic :as l]))

(deftest grounded?-test
  (testing "ground"
    (is (= true (grounded? 1)))
    (is (= true (grounded? [])))
    (is (= true (grounded? ["a" 1])))
    (is (= true (grounded? {"a" 1})))
    (is (= true (grounded? {:a 1})))
    (is (= true (grounded? {:a {:b 1}}))))
  (testing "fresh"
    (is (= false (grounded? 'x)))
    (is (= false (grounded? ['x])))
    (is (= false (grounded? {:a 'x})))
    (is (= false (grounded? {'x 1})))
    (is (= false (grounded? {:a {:b 'x}}))))
  (testing "against core.logic run"
    (testing "ground"
      (is (= true (grounded? (first (l/run 1 [q] (l/== q 1)))))))
    (testing "fresh"
      (is (= false (grounded? (first (l/run 1 [q]))))))))
