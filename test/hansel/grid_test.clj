(ns hansel.grid-test
  (:use midje.sweet
        hansel.grid))

(fact
  "neighbors on an open grid returns everything next to it"
  (let [nodes #{[-1 -1] [0 -1] [1 -1]
                [-1 0] [0 0] [1 0]
                [-1 1] [0 1] [1 1]}]
    (set (neighbors nodes [0 0])) => #{[-1 -1] [0 -1] [1 -1]
                                       [-1 0] [1 0]
                                       [-1 1] [0 1] [1 1]}
    (set (neighbors nodes [-1 -1])) => #{[0 -1] [-1 0] [0 0]}
    (set (neighbors nodes [0 1])) => #{[-1 1] [-1 0] [0 0] [1 0] [1 1]}))

(fact
  "neighbors rejects nodes that aren't available"
  (let [nodes #{[-1 -1]      [1 -1]
                [-1 0] [0 0] [1 0]}]
    (set (neighbors nodes [0 0])) => (disj nodes [0 0])))

(fact
  "neighbors rejects blocked diagonals"
  (let [nodes #{[-1 -1] [1 -1]
                [0 0]
                [-1 1] [1 1]}]
    (set (neighbors nodes [0 0])) => #{}))

(fact
  "neighbors includes a diagonal if an adjacent is open"
  (let [nodes #{[-1 -1] [0 -1] [0 0]}]
    (set (neighbors nodes [0 0])) => #{[-1 -1] [0 -1]}))

