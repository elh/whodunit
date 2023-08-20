# whodunit

```plaintext
---------- Logic Puzzle Generation ----------
DEBUG - generate-rule: :name = bob , :location = palace
DEBUG - added rule: 1 rules, 36 possible solutions
DEBUG - generate-rule: :location = palace , :name = bob
DEBUG - added rule: 2 rules, 36 possible solutions
DEBUG - generate-rule: :location = palace , :guilty = true
DEBUG - added rule: 3 rules, 12 possible solutions
DEBUG - generate-rule: :name = carol , :color = red
DEBUG - added rule: 4 rules, 4 possible solutions
DEBUG - generate-rule: :color = red , :location = pier
DEBUG - added rule: 5 rules, 2 possible solutions
DEBUG - generate-rule: :name = alice , :color = blue
"Elapsed time: 155.089125 msecs"

Config:
 {:values {:name [alice bob carol], :guilty [true false false], :color [red blue green], :location [park pier palace]}}

Rules:
1. name is alice and color is blue
2. color is red and location is pier
3. name is carol and color is red
4. location is palace and guilty is true
5. location is palace and name is bob
6. name is bob and location is palace

---------- Zebra Puzzle - using vectors ----------
{:soln
 ((1 "yellow" "norwegian" "water" "kools" "fox")
  (2 "blue" "ukrainian" "tea" "chesterfields" "horse")
  (3 "red" "englishman" "milk" "old-gold" "snail")
  (4 "ivory" "spaniard" "orange-juice" "lucky-strike" "dog")
  (5 "green" "japanese" "coffee" "parliaments" "zebra")),
 :grounded? true,
 :has-more? false,
 :soln-count 1}

---------- Zebra Puzzle - using maps ----------
{:soln
 ({:house-idx 1,
   :house-color "yellow",
   :nationality "norwegian",
   :drinks "water",
   :smokes "kools",
   :pet "fox"}
  {:house-idx 2,
   :house-color "blue",
   :nationality "ukrainian",
   :drinks "tea",
   :smokes "chesterfields",
   :pet "horse"}
  {:house-idx 3,
   :house-color "red",
   :nationality "englishman",
   :drinks "milk",
   :smokes "old-gold",
   :pet "snail"}
  {:house-idx 4,
   :house-color "ivory",
   :nationality "spaniard",
   :drinks "orange-juice",
   :smokes "lucky-strike",
   :pet "dog"}
  {:house-idx 5,
   :house-color "green",
   :nationality "japanese",
   :drinks "coffee",
   :smokes "parliaments",
   :pet "zebra"}),
 :grounded? true,
 :has-more? false,
 :soln-count 1}
```
