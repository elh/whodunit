(ns script.config)

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
