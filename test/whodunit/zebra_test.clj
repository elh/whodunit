(ns whodunit.zebra-test
  (:require [clojure.test :refer :all]
            [whodunit.zebra :refer :all]
            [whodunit.core :refer :all]))

(deftest zebrao-vec-test
  (is (= (run+ zebrao-vec)
         {:soln '((1 "yellow" "norwegian" "water" "kools" "fox")
                 (2 "blue" "ukrainian" "tea" "chesterfields" "horse")
                 (3 "red" "englishman" "milk" "old-gold" "snail")
                 (4 "ivory" "spaniard" "orange-juice" "lucky-strike" "dog")
                 (5 "green" "japanese" "coffee" "parliaments" "zebra")),
          :grounded? true,
          :has-more? false})))

(deftest zebrao-test
  (is (= (run+ zebrao)
         {:soln '({:house-idx 1, :house-color "yellow", :nationality "norwegian", :drinks "water", :smokes "kools", :pet "fox"}
                  {:house-idx 2, :house-color "blue", :nationality "ukrainian", :drinks "tea", :smokes "chesterfields", :pet "horse"}
                  {:house-idx 3, :house-color "red", :nationality "englishman", :drinks "milk", :smokes "old-gold", :pet "snail"}
                  {:house-idx 4, :house-color "ivory", :nationality "spaniard", :drinks "orange-juice", :smokes "lucky-strike", :pet "dog"}
                  {:house-idx 5, :house-color "green", :nationality "japanese", :drinks "coffee", :smokes "parliaments", :pet "zebra"}),
          :grounded? true,
          :has-more? false})))

(deftest zebrao-whodunit-test
  (is (= (run+ zebrao-whodunit)
         {:soln '({:house-idx 1, :house-color "yellow", :name "norwegian", :drinks "water", :smokes "kools", :pet "fox"}
                  {:house-idx 2, :house-color "blue", :name "ukrainian", :drinks "tea", :smokes "chesterfields", :pet "horse"}
                  {:house-idx 3, :house-color "red", :name "englishman", :drinks "milk", :smokes "old-gold", :pet "snail"}
                  {:house-idx 4, :house-color "ivory", :name "spaniard", :drinks "orange-juice", :smokes "lucky-strike", :pet "dog"}
                  {:house-idx 5, :house-color "green", :name "japanese", :drinks "coffee", :smokes "parliaments", :pet "zebra"}),
          :grounded? true,
          :has-more? false})))
