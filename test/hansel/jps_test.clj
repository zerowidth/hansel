(ns hansel.jps-test
  (:use midje.sweet
        hansel.jps))

(def empty-grid
  (apply sorted-set (for [x (range 5) y (range 5)] [x y])))

(fact
  "forced-neighbors knows if a neighbor is forced,
  given a grid, a node and a direction"
  (let [grid (disj empty-grid [2 2])]
    ; . . . . .
    ; . . . . .
    ; . . # . .
    ; . . . . .
    ; . . . . .
    ; horizontal movement, +x
    (forced-neighbors grid [1 0] [1 0]) => nil
    (forced-neighbors grid [1 1] [1 0]) => nil
    (forced-neighbors grid [1 2] [1 0]) => nil
    (forced-neighbors grid [1 3] [1 0]) => nil
    (forced-neighbors grid [1 4] [1 0]) => nil

    (forced-neighbors grid [2 0] [1 0]) => nil
    (forced-neighbors grid [2 1] [1 0]) => #{ [3 2] }
    (forced-neighbors grid [2 2] [1 0]) => nil
    (forced-neighbors grid [2 3] [1 0]) => #{ [3 2] }
    (forced-neighbors grid [2 4] [1 0]) => nil

    ; horizontal, -x
    (forced-neighbors grid [3 0] [-1 0]) => nil
    (forced-neighbors grid [3 1] [-1 0]) => nil
    (forced-neighbors grid [3 2] [-1 0]) => nil
    (forced-neighbors grid [3 3] [-1 0]) => nil
    (forced-neighbors grid [3 4] [-1 0]) => nil

    (forced-neighbors grid [2 0] [-1 0]) => nil
    (forced-neighbors grid [2 1] [-1 0]) => #{ [1 2] }
    (forced-neighbors grid [2 2] [-1 0]) => nil
    (forced-neighbors grid [2 3] [-1 0]) => #{ [1 2] }
    (forced-neighbors grid [2 4] [-1 0]) => nil

    ; vertical, +y
    (forced-neighbors grid [0 1] [0 1]) => nil
    (forced-neighbors grid [1 1] [0 1]) => nil
    (forced-neighbors grid [2 1] [0 1]) => nil
    (forced-neighbors grid [3 1] [0 1]) => nil
    (forced-neighbors grid [4 1] [0 1]) => nil

    (forced-neighbors grid [0 2] [0 1]) => nil
    (forced-neighbors grid [1 2] [0 1]) => #{ [2 3] }
    (forced-neighbors grid [2 2] [0 1]) => nil
    (forced-neighbors grid [3 2] [0 1]) => #{ [2 3] }
    (forced-neighbors grid [4 2] [0 1]) => nil

    ; vertical, -y
    (forced-neighbors grid [0 3] [0 -1]) => nil
    (forced-neighbors grid [1 3] [0 -1]) => nil
    (forced-neighbors grid [2 3] [0 -1]) => nil
    (forced-neighbors grid [3 3] [0 -1]) => nil
    (forced-neighbors grid [4 3] [0 -1]) => nil

    (forced-neighbors grid [0 2] [0 -1]) => nil
    (forced-neighbors grid [1 2] [0 -1]) => #{ [2 1] }
    (forced-neighbors grid [2 2] [0 -1]) => nil
    (forced-neighbors grid [3 2] [0 -1]) => #{ [2 1] }
    (forced-neighbors grid [4 2] [0 -1]) => nil

    ; . . . . .
    ; . . . . .
    ; . . # . .
    ; . . . . .
    ; . . . . .
    ; diagonal, +x +y
    (forced-neighbors grid [1 1] [1 1]) => nil
    (forced-neighbors grid [2 1] [1 1]) => nil
    (forced-neighbors grid [3 1] [1 1]) => nil
    (forced-neighbors grid [1 2] [1 1]) => nil
    (forced-neighbors grid [3 2] [1 1]) => #{ [2 3] }
    (forced-neighbors grid [1 3] [1 1]) => nil
    (forced-neighbors grid [2 3] [1 1]) => #{ [3 2] }
    (forced-neighbors grid [3 3] [1 1]) => nil

    (forced-neighbors grid [0 2] [1 1]) => nil
    (forced-neighbors grid [2 0] [1 1]) => nil

    ; ; diagonal, +x -y
    (forced-neighbors grid [1 1] [1 -1]) => nil
    (forced-neighbors grid [2 1] [1 -1]) => #{ [3 2] }
    (forced-neighbors grid [3 1] [1 -1]) => nil
    (forced-neighbors grid [1 2] [1 -1]) => nil
    (forced-neighbors grid [3 2] [1 -1]) => #{ [2 1] }
    (forced-neighbors grid [1 3] [1 -1]) => nil
    (forced-neighbors grid [2 3] [1 -1]) => nil
    (forced-neighbors grid [3 3] [1 -1]) => nil

    (forced-neighbors grid [0 2] [1 1]) => nil
    (forced-neighbors grid [4 2] [1 1]) => nil

    ; ; diagonal, -x +y
    (forced-neighbors grid [1 1] [-1 1]) => nil
    (forced-neighbors grid [2 1] [-1 1]) => nil
    (forced-neighbors grid [3 1] [-1 1]) => nil
    (forced-neighbors grid [1 2] [-1 1]) => #{ [2 3] }
    (forced-neighbors grid [3 2] [-1 1]) => nil
    (forced-neighbors grid [1 3] [-1 1]) => nil
    (forced-neighbors grid [2 3] [-1 1]) => #{ [1 2] }
    (forced-neighbors grid [3 3] [-1 1]) => nil

    (forced-neighbors grid [2 0] [1 1]) => nil
    (forced-neighbors grid [4 2] [1 1]) => nil

    ; ; diagonal, -x -y
    (forced-neighbors grid [1 1] [-1 -1]) => nil
    (forced-neighbors grid [2 1] [-1 -1]) => #{ [1 2] }
    (forced-neighbors grid [3 1] [-1 -1]) => nil
    (forced-neighbors grid [1 2] [-1 -1]) => #{ [2 1] }
    (forced-neighbors grid [3 2] [-1 -1]) => nil
    (forced-neighbors grid [1 3] [-1 -1]) => nil
    (forced-neighbors grid [2 3] [-1 -1]) => nil
    (forced-neighbors grid [3 3] [-1 -1]) => nil

    (forced-neighbors grid [2 4] [1 1]) => nil
    (forced-neighbors grid [4 2] [1 1]) => nil

    ; misc test cases
    (forced-neighbors (disj empty-grid [0 1]) [1 1] [1 1]) => #{ [0 2] }
    (forced-neighbors (disj empty-grid [3 3]) [3 4] [1 1]) => #{ [4 3] }

    ; can't go through diagonals
    (forced-neighbors (disj empty-grid [1 0] [0 1]) [0 0] [1 1]) => nil
    (forced-neighbors (disj empty-grid [1 0] [0 1]) [0 0] [1 0]) => nil
    (forced-neighbors (disj empty-grid [1 0] [0 1]) [0 0] [0 1]) => nil))

(fact
  "jump finds the next jump points"
  (let [grid (disj empty-grid [2 2])]

    ; straight-line movement
    (jump grid [0 0] [1 0] [4 4]) => nil   ; bump into wall
    (jump grid [0 2] [1 0] [4 4]) => nil   ; bump into obstacle

    (jump grid [0 0] [1 0] [1 0]) => [1 0] ; find goal
    (jump grid [0 0] [1 0] [4 0]) => [4 0] ; find goal

    (jump grid [0 1] [1 0] [4 4]) => [2 1] ; next jump point
    (jump grid [0 3] [1 0] [4 4]) => [2 3] ; next jump point

    ; diagonal movement
    (jump empty-grid [1 0] [1 1] [0 4]) => nil   ; bump into wall
    (jump empty-grid [0 0] [1 1] [4 4]) => [4 4] ; find goal
    (jump grid [2 0] [1 1] [0 4]) => [3 1]         ; vertical jump point
    (jump grid [0 2] [1 1] [0 0]) => [1 3]         ; horizontal jump point
    (jump empty-grid [0 0] [1 1] [4 2]) => [2 2] ; horizontal to goal
    (jump empty-grid [0 0] [1 1] [2 4]) => [2 2] ; vertical to goal

    ; misc test cases
    (jump (disj empty-grid [0 1]) [0 0] [1 1] [0 4]) => [1 1]

    ; can't go through diagonals
    (jump (disj empty-grid [0 3] [1 2] [2 1] [3 0]) [0 0] [1 1] [4 4]) => nil))

(fact
  "direction determines a normalized movement vector"
  (direction [0 0] [1 1]) => [1 1]
  (direction [0 0] [1 0]) => [1 0]
  (direction [0 0] [4 0]) => [1 0]
  (direction [0 0] [2 2]) => [1 1]
  (direction [2 2] [0 0]) => [-1 -1])

(fact
  "neighbors uses parent transitions to prune directionally"

  ; no parent for the given node, return all neighbors
  (neighbors empty-grid {} [0 0]) => #{ [0 1] [1 1] [1 0] }
  (neighbors empty-grid {} [1 1]) =>
    #{ [0 0] [0 1] [0 2] [1 0] [1 2] [2 0] [2 1] [2 2] }

  (neighbors (disj empty-grid [2 2]) {} [1 1]) =>
    #{ [0 0] [0 1] [0 2] [1 0] [1 2] [2 0] [2 1] }

  (neighbors (disj empty-grid [2 1] [1 2]) {} [1 1]) =>
    #{ [0 0] [0 1] [0 2] [1 0] [2 0] }

  ; horizontal movement
  (neighbors empty-grid {[1 1] [0 1]} [1 1]) => #{ [2 1] }

  (neighbors (disj empty-grid [1 2]) {[1 1] [0 1]} [1 1]) =>
    #{ [2 1] [2 2] }

  (neighbors (disj empty-grid [1 2] [1 0]) {[1 1] [0 1]} [1 1]) =>
    #{ [2 0] [2 1] [2 2] }

  ; vertical movement
  (neighbors empty-grid {[1 1] [1 0]} [1 1]) => #{ [1 2] }

  (neighbors (disj empty-grid [2 1]) {[1 1] [1 0]} [1 1]) =>
    #{ [1 2] [2 2] }

  (neighbors (disj empty-grid [0 1] [2 1]) {[1 1] [1 0]} [1 1]) =>
    #{ [1 2] [2 2] [0 2] }

  ; diagonal movement
  (neighbors empty-grid {[1 1] [0 0]} [1 1]) => #{ [1 2] [2 2] [2 1] }

  (neighbors (disj empty-grid [0 1]) {[1 1] [0 0]} [1 1]) =>
    #{ [1 2] [2 2] [2 1] [0 2] }

  (neighbors (disj empty-grid [0 1] [1 0]) {[1 1] [0 0]} [1 1]) =>
    #{ [1 2] [2 2] [2 1] [0 2] [2 0] })

(fact
  "successors identifies successor nodes"

  ; jump point around obstacle
  (let [grid (disj empty-grid [2 2])]
    (successors grid {} [0 0] [4 4]) => #{[1 1]})

  ; find goal directly
  (successors empty-grid {} [0 0] [4 4]) => #{[4 4]}

  ; jump point towards goal
  (successors (disj empty-grid [0 1]) {} [0 0] [0 4]) => #{[1 1]}

  ; only directional successors (directional pruning)
  ;   . . . - .
  ;   . . . # .  moving [1 0] at x
  ;   . x . + .  - should not be a successor
  ;   # - . . .  + should be a successor
  ;   . . # . .
  (successors (disj empty-grid [0 1] [2 0] [3 3])
              { [1 2] [0 2] } ; set the direction via parents
              [1 2]
              [1 1]) => #{ [3 2] })

(fact
  "expand-pair expands two points"
  (expand-pair [1 1] [0 0]) => (list [1 1] [0 0])
  (expand-pair [2 2] [0 0]) => (list [2 2] [1 1] [0 0]))

(fact
  "expand-parents expands parent relationships"
  (expand-parents {[0 0] [2 0]}) => {[0 0] [1 0]
                                     [1 0] [2 0]}
  (expand-parents {[4 4] [0 0]
                   [0 0] [2 0]}) => {[0 0] [1 0]
                                     [1 0] [2 0]
                                     [1 1] [0 0]
                                     [2 2] [1 1]
                                     [3 3] [2 2]
                                     [4 4] [3 3]})

