# whodunit

Generating Logic Puzzles
```plaintext
> make run
---------- Logic Puzzle Generation ----------
DEBUG - generate-rule: type = membero, kvs = #{[:guilty false] [:name carol]}
DEBUG - added rule: 1 rules, 72 possible solutions
DEBUG - generate-rule: type = membero, kvs = #{[:name carol] [:color green]}
DEBUG - added rule: 2 rules, 24 possible solutions
DEBUG - generate-rule: type = membero, kvs = #{[:color red] [:name carol]}
DEBUG - generate-rule: type = membero, kvs = #{[:location pier] [:color green]}
DEBUG - added rule: 3 rules, 8 possible solutions
DEBUG - generate-rule: type = membero, kvs = #{[:name carol] [:color green]}
DEBUG - generate-rule: type = membero, kvs = #{[:guilty true] [:color green]}
DEBUG - generate-rule: type = membero, kvs = #{[:location palace] [:color green]}
DEBUG - generate-rule: type = membero, kvs = #{[:color red] [:name alice]}
DEBUG - added rule: 4 rules, 4 possible solutions
DEBUG - generate-rule: type = membero, kvs = #{[:color red] [:name alice]}
DEBUG - generate-rule: type = membero, kvs = #{[:color blue] [:name carol]}
DEBUG - generate-rule: type = membero, kvs = #{[:guilty true] [:color green]}
DEBUG - generate-rule: type = membero, kvs = #{[:location palace] [:guilty false]}
DEBUG - added rule: 5 rules, 2 possible solutions
DEBUG - generate-rule: type = membero, kvs = #{[:color blue] [:name carol]}
DEBUG - generate-rule: type = membero, kvs = #{[:location palace] [:color blue]}
"Elapsed time: 350.056625 msecs"

Config:
 {:values {:name [alice bob carol], :guilty [true false false], :color [red blue green], :location [park pier palace]}}

Rules:
1. guilty is false and name is carol
2. name is carol and color is green
3. location is pier and color is green
4. color is red and name is alice
5. location is palace and guilty is false
6. location is palace and color is blue
```

The Zebra Puzzle
```plaintext
> make zebra
---------- Zebra Puzzle - using vectors ----------
"Elapsed time: 27.248042 msecs"
"Elapsed time: 15.686916 msecs"
"Elapsed time: 14.313708 msecs"
"Elapsed time: 13.928584 msecs"
"Elapsed time: 13.858833 msecs"
{:soln
 ((1 "yellow" "norwegian" "water" "kools" "fox")
  (2 "blue" "ukrainian" "tea" "chesterfields" "horse")
  (3 "red" "englishman" "milk" "old-gold" "snail")
  (4 "ivory" "spaniard" "orange-juice" "lucky-strike" "dog")
  (5 "green" "japanese" "coffee" "parliaments" "zebra")),
 :grounded? true,
 :has-more? false}

---------- Zebra Puzzle - using maps ----------
"Elapsed time: 28.097333 msecs"
"Elapsed time: 23.879833 msecs"
"Elapsed time: 26.963625 msecs"
"Elapsed time: 22.967208 msecs"
"Elapsed time: 22.87375 msecs"
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
 :has-more? false}
```
