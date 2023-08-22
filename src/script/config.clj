(ns script.config)

;; important to consider if config can even be solved with currently supported rules. e.g. duplicate values make membero
;; relations far less effective.
(def configs {"2" {:values {:name ["alice" "bob"]
                            :guilty [true false]
                            :color ["red" "blue"]
                            :location ["park" "pier"]}}
              "3" {:values {:name ["alice" "bob" "carol"]
                            :guilty [true false false]
                            :color ["red" "blue" "green"]
                            :location ["park" "pier" "palace"]}}
              "4" {:values {:name ["alice" "bob" "carol" "dave"]
                            :guilty [true false false false]
                            :color ["red" "blue" "green" "white"]
                            :location ["park" "pier" "palace" "plaza"]}}
              "5-small" {:values {:name ["alice" "bob" "carol" "dave" "eve"]
                                  ;; :guilty [true false false false false]
                                  ;; :color ["red" "green" "blue" "yellow" "white"]
                                  :location ["park" "pier" "palace" "plaza" "parlor"]
                                  :item ["comb" "cowl" "coin" "cap" "crowbar"]}}
              "5" {:values {:name ["alice" "bob" "carol" "dave" "eve"]
                            :guilty [true false false false false]
                            :color ["red" "green" "blue" "yellow" "white"]
                            :color2 ["red" "green" "blue" "yellow" "white"]
                            :location ["park" "pier" "palace" "plaza" "parlor"]
                            :item ["comb" "cowl" "coin" "cap" "crowbar"]}}
              "zebra" {:values {:house-idx [1 2 3 4 5]
                                :house-color ["blue" "green" "ivory" "red" "yellow"]
                                ;; NOTE: renamed from nationality to use with puzzle fns
                                :name ["englishman" "japanese" "norwegian" "spaniard" "ukrainian"]
                                :drinks ["coffee" "milk" "orange-juice" "tea" "water"]
                                :smokes ["chesterfields" "kools" "lucky-strike" "old-gold" "parliaments"]
                                :pet ["dog" "fox" "horse" "snail" "zebra"]}}
              "7" {:values {:house-idx [1 2 3 4 5 6 7]
                            :house-color ["blue" "green" "ivory" "red" "yellow", "black", "white"]
                            ;; NOTE: renamed from nationality to use with puzzle fns
                            :name ["englishman" "japanese" "norwegian" "spaniard" "ukrainian", "irish", "nigerian"]
                            :drinks ["coffee" "milk" "orange-juice" "tea" "water", "beer", "wine"]
                            :smokes ["chesterfields" "kools" "lucky-strike" "old-gold" "parliaments", "marlboro", "camel"]
                            :pet ["dog" "fox" "horse" "snail" "zebra", "cat", "fish"]
                            :location ["park" "pier" "palace" "plaza" "parlor", "pub", "pool"]
                            :item ["comb" "cowl" "coin" "cap" "crowbar", "cane", "candle"]}}})
