# whodunit

### Logic Puzzle Generation
Given a solution space defined via config, generate a set of rules that arrives at a single solution.
The solution space is defined as a set of records identified by a unique :name values.

Example:<br>
The goal is to generate a rule set to answer "who killed dave?"
* 3 people: alice, bob, and carol
* 3 clothing colors: red, blue, green
* 3 locations: park, pier, and palace
* 1 person is guilty; the other 2 are innocent



Logic puzzles scale (n!)^m where n is the number of values for each key and m is the number of keys. This is the base
case where each key has a unique value that will be assigned to a single value.

see `run+` and `puzzle`

```plaintext
> make generate
---------- Logic Puzzle Generation ----------
Generating...
"Elapsed time: 36.659625 msecs"

Config:
 {:values {:name [alice bob carol dave], :guilty [true false false false], :color [red blue green white], :location [park pier palace plaza]}}

Rules:
1. name is carol and guilty is false
2. name is alice and guilty is false
3. location is plaza and name is dave
4. color is green and name is alice
5. color is white and location is park
6. color is white and guilty is false
7. name is bob and guilty is false
8. name is bob and color is white
9. color is red and location is plaza
10. location is palace and color is blue

Solution:
({:color "green", :name "alice", :guilty false, :location "pier"}
 {:color "white", :name "bob", :guilty false, :location "park"}
 {:color "blue", :name "carol", :guilty false, :location "palace"}
 {:color "red", :name "dave", :guilty true, :location "plaza"})
```

### The Zebra Puzzle
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
