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
    ;; other non-core.logic symbols are still considered ground
    (is (= true (grounded? 'foo)))
  (testing "fresh"
    (is (= false (grounded? '_0)))
    (is (= false (grounded? ['_0])))
    (is (= false (grounded? {:a '_0})))
    (is (= false (grounded? {'_0 1})))
    (is (= false (grounded? {:a {:b '_0}}))))
  (testing "against core.logic run"
    (testing "ground"
      (is (= true (grounded? (first (l/run 1 [q] (l/== q 1))))))
      (is (= true (grounded? (first (l/run 1 [q] (l/== q 'foo)))))))
    (testing "fresh"
      (is (= false (grounded? (first (l/run 1 [q]))))))))

(deftest run+-test
  (is (= (run+ (fn [q] (l/== q 1)))
         {:soln 1,
          :grounded? true,
          :has-more? false}))
  (is (= (run+ (fn [q] (l/permuteo q [1 2 3])))
         {:soln [1 2 3],
          :grounded? true,
          :has-more? true}))
  (is (= (run+ (fn [q] l/succeed))
         {:soln '_0,
          :grounded? false,
          :has-more? false})))
