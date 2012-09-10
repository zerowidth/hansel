(ns hansel.racetrack-test
  (:use midje.sweet
        hansel.racetrack))

(defn empty-grid []
  (apply sorted-set (for [x (range 4) y (range 4)] [x y])))

(fact
  "raytrace includes all nodes touched between two grid squares"
  (raytrace [0 0] [3 0]) => [[0 0] [1 0] [2 0] [3 0]]
  (raytrace [0 0] [2 3]) => [[0 0] [0 1] [1 1] [1 2] [2 2] [2 3]]
  (raytrace [2 3] [0 0]) => [[2 3] [2 2] [1 2] [1 1] [0 1] [0 0]]
  ; go through the corners for perfectly diagonal paths:
  (raytrace [0 0] [2 2]) => [[0 0] [1 1] [2 2]])

(fact
  "clear-path? tests raytracing against a grid"
  (clear-path? (empty-grid) [0 0] [0 0]) => true
  (clear-path? (empty-grid) [0 0] [0 3]) => true
  (clear-path? (empty-grid) [0 0] [3 3]) => true
  (clear-path? (empty-grid) [0 0] [0 4]) => false
  (let [grid (disj (empty-grid) [1 0])]
    (clear-path? grid [0 0] [2 0]) => false)
  ; including diagonal checks
  (let [grid (disj (empty-grid) [1 0] [0 1])]
    (clear-path? grid [0 0] [1 1]) => false))

(fact
  "next-reachable returns next open neighbors given current position/velocity"

  ; basic position tests
  (set (next-reachable (empty-grid) [[0 0] [0 0]])) => #{[0 0] [1 0] [1 1] [0 1]}

  ; includes all nodes, since staying still is a potential move
  (set (next-reachable (empty-grid) [[1 1] [0 0]])) => #{[0 0] [0 1] [0 2]
                                                         [1 0] [1 1] [1 2]
                                                         [2 0] [2 1] [2 2]}

  (set (next-reachable (empty-grid) [[0 0] [1 1]])) => #{[0 0] [0 1] [0 2]
                                                         [1 0] [1 1] [1 2]
                                                         [2 0] [2 1] [2 2]}

  (set (next-reachable (empty-grid) [[1 1] [1 0]])) => #{[1 0] [1 1] [1 2]
                                                         [2 0] [2 1] [2 2]
                                                         [3 0] [3 1] [3 2]}

  (let [grid (disj (empty-grid) [2 0])]
    (set (next-reachable grid [[0 0] [1 0]])) => #{[0 0] [1 0]
                                                   [0 1] [1 1] [2 1]})

  ; raytracing test, [1 0] blocks the way to [2 0] and [2 1]
  (let [grid (disj (empty-grid) [1 0])]
    (set (next-reachable grid [[0 0] [1 0]])) => #{[0 0] [0 1] [1 1]})

  ; raytracing test, [1 1] blocks the way to [2 1]
  (let [grid (disj (empty-grid) [1 1])]
    (set (next-reachable grid [[0 0] [1 0]])) => #{[0 0] [1 0] [2 0] [0 1] })

  ; more raytracing
  (let [grid (disj (empty-grid) [2 0] [1 1])]
    (set (next-reachable grid [[0 0] [1 0]])) => #{[0 0] [1 0] [0 1]})

  ; can't traverse diagonally if both sides are blocked
  (let [grid (disj (empty-grid) [1 0] [0 1])]
    (set (next-reachable grid [[0 0] [1 1]])) => #{[0 0]}))

(fact
  "available-moves returns a list of position/velocity moves available for a
  given grid, position, and velocity"

  (set (available-moves (empty-grid) [[0 0] [0 0]])) => #{[[0 0] [0 0]]
                                                          [[1 0] [1 0]]
                                                          [[0 1] [0 1]]
                                                          [[1 1] [1 1]]}

  (let [grid #{ [0 0] [1 0] [2 0] [3 0] }]
    (set (available-moves grid [[0 0] [2 0]])) => #{[[1 0] [1 0]]
                                                    [[2 0] [2 0]]
                                                    [[3 0] [3 0]]})

  (let [grid (disj (empty-grid) [2 1])]
    (set (available-moves grid [[0 0] [2 2]])) => #{[[1 1] [1 1]]
                                                    [[1 2] [1 2]]
                                                    [[1 3] [1 3]]
                                                    [[2 2] [2 2]]
                                                    [[2 3] [2 3]]
                                                    [[3 3] [3 3]]})

  (let [grid (disj (empty-grid) [1 1] [1 2])]
    (set (available-moves grid [[3 2] [-2 -1]])) => #{[[1 0] [-2 -2]]
                                                      [[2 0] [-1 -2]]
                                                      [[2 1] [-1 -1]]
                                                      [[2 2] [-1 0]]}))
