# whodunit - Logic Puzzle Generation with [core.logic](https://github.com/clojure/core.logic)
Given a provided solution space, generate a set of core.logic rules that arrives at a single solution.
The solution space is defined as a set of uniquely `:name`-ed map records.

Example:<br>
We can generate a rule set to answer "who killed dave?"
* 3 suspects last night: alice, bob, and carol
* 3 colors they were wearing: red, blue, green
* 3 locations they were at: park, pier, and palace
* 2 of them are innocent. 1 is guilty!

Logic puzzles scale (n!)^m where n is the number of values for each key and m is the number of keys. This is the base case where each key has a unique value that will be assigned to a single value.

See [`run+` and `puzzle`](src/whodunit/core.clj).

### Generating Puzzles
Let's generate a new variation of the famous Zebra Puzzle. With 5 records and 5 attributes with unique values, there are (5!)^5 or 24,883,200,000 possible solutions! We can generate a new puzzle in ~1s.

```clojure
;; "nationality" is renamed to `:name` for `puzzle`
(def config {:values {:name ["englishman" "japanese" "norwegian" "spaniard" "ukrainian"]
                      :house-idx [1 2 3 4 5]
                      :house-color ["blue" "green" "ivory" "red" "yellow"]
                      :drinks ["coffee" "milk" "orange-juice" "tea" "water"]
                      :smokes ["chesterfields" "kools" "lucky-strike" "old-gold" "parliaments"]
                      :pet ["dog" "fox" "horse" "snail" "zebra"]}})

;; generate a puzzle rule set
(puzzle config)

;; generate a puzzle rule set with some starting rules
;; e.g. the first rule is "the red house is in the middle"
(let [hs (lvar)]
  (puzzle config hs [{:data {:type :membero
                             :kvs {:house-color "red" :house-idx 3}}
                      :goal (membero (new-rec zebra/config {:house-color "red" :house-idx 3}) hs)}]))

;; generate a puzzle rule set with some constraints on the solution that are hidden from the resulting rules
;; e.g. this puzzle will be generated with the same answer to the ultimate question of the original zebra puzzle
(let [hs (lvar)]
  (puzzle config hs [] [{:data {:type :membero
                                :kvs {:drinks "water" :name "norwegian"}}
                         :goal (membero (new-rec zebra/config {:drinks "water" :name "norwegian"}) hs)}
                        {:data {:type :membero
                                :kvs {:pet "zebra" :name "japanese"}}
                         :goal (membero (new-rec zebra/config {:pet "zebra" :name "japanese"}) hs)}]))
```

```bash
make new-zebra
# "Elapsed time: 952.507166 msecs"
#
# Config:
# {:values
#  {:name ["englishman" "japanese" "norwegian" "spaniard" "ukrainian"],
#   :house-idx [1 2 3 4 5],
#   :house-color ["blue" "green" "ivory" "red" "yellow"],
#   :drinks ["coffee" "milk" "orange-juice" "tea" "water"],
#   :smokes
#   ["chesterfields" "kools" "lucky-strike" "old-gold" "parliaments"],
#   :pet ["dog" "fox" "horse" "snail" "zebra"]}}
#
# Rules:
# 1. name is spaniard and house-color is yellow
# 2. name is ukrainian and pet is snail
# 3. house-idx is 4 and drinks is water
# 4. drinks is milk and house-idx is 1
# 5. smokes is old-gold and name is norwegian
# 6. name is japanese and pet is horse
# 7. house-idx is 4 and name is japanese
# 8. smokes is lucky-strike and drinks is milk
# 9. house-color is yellow and house-idx is 3
# 10. house-color is yellow and drinks is coffee
# 11. pet is fox and house-idx is 3
# 12. smokes is kools and name is japanese
# 13. pet is snail and house-color is blue
# 14. house-idx is 1 and pet is zebra
# 15. drinks is coffee and name is spaniard
# 16. name is japanese and house-color is ivory
# 17. name is ukrainian and house-idx is 2
# 18. house-color is green and drinks is milk
# 19. house-color is blue and smokes is parliaments
# 20. name is norwegian and drinks is tea
#
# Solution:
# ({:house-idx 4,
#   :house-color "ivory",
#   :name "japanese",
#   :drinks "water",
#   :smokes "kools",
#   :pet "horse"}
#  {:house-idx 1,
#   :house-color "green",
#   :name "englishman",
#   :drinks "milk",
#   :smokes "lucky-strike",
#   :pet "zebra"}
#  {:house-idx 5,
#   :house-color "red",
#   :name "norwegian",
#   :drinks "tea",
#   :smokes "old-gold",
#   :pet "dog"}
#  {:house-idx 2,
#   :house-color "blue",
#   :name "ukrainian",
#   :drinks "orange-juice",
#   :smokes "parliaments",
#   :pet "snail"}
#  {:house-idx 3,
#   :house-color "yellow",
#   :name "spaniard",
#   :drinks "coffee",
#   :smokes "chesterfields",
#   :pet "fox"})
```

Another example

```clojure
(puzzle {:values {:name ["alice" "bob" "carol"]
                  :guilty [true false false]
                  :color ["red" "blue" "green"]
                  :location ["park" "pier" "palace"]}})
```

```bash
make puzzle
```

### Context: Solving the Zebra Puzzle with core.logic

See solutions in [`zebra.clj`](src/whodunit/zebra.clj) that introduced me to core.logic and prototyped the map-based and introspectable rule approach.

```bash
make zebra
```

### Development
See [Makefile](Makefile) for dev commands. Clojure environment set up using Nix.
```bash
make lint
make test
make bench
```

### TODO:
* ✅ Support constraining the solution that are not presented as rules. e.g. "The solution must be that dave is guilty, but that should not be directly given away by a rule".
* ✅ Make the additional details on rules optional. puzzle accepts either structured goal records or just record expressions although some functionality will be limited.
* Support more rule types. I am starting very simple but this is easily extensible. Take inspiration from logic puzzles and add rules thematic to murder mysteries.
* Support an interactive mode of puzzle generation where all rules are not all created at once.
* Intelligently sort rules to optimize solving.
* Steer generation to produce "good" puzzles. e.g. at a tunable level of difficulty.
