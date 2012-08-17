(ns hansel.grid-test
  (:use midje.sweet
        hansel.grid))

(fact
  "neighbors on an open grid returns everything next to it"
  (let [nodes #{[-1 -1] [0 -1] [1 -1]
                [-1 0] [0 0] [1 0]
                [-1 1] [0 1] [1 1]}]
    (neighbors [0 0] nodes) => #{[-1 -1] [0 -1] [1 -1]
                                 [-1 0] [1 0]
                                 [-1 1] [0 1] [1 1]}
    (neighbors [-1 -1] nodes) => #{[0 -1] [-1 0] [0 0]}
    (neighbors [0 1] nodes) => #{[-1 1] [-1 0] [0 0] [1 0] [1 1]}))

(fact
  "neighbors rejects nodes that aren't available"
  (let [nodes #{[-1 -1]      [1 -1]
                [-1 0] [0 0] [1 0]}]
    (neighbors [0 0] nodes) => (disj nodes [0 0])))

(fact
  "neighbors rejects blocked diagonals"
  (let [nodes #{[-1 -1] [1 -1]
                [0 0]
                [-1 1] [1 1]}]
    (neighbors [0 0] nodes) => #{}))

(fact
  "neighbors includes a diagonal if an adjacent is open"
  (let [nodes #{[-1 -1] [0 -1] [0 0]}]
    (neighbors [0 0] nodes) => #{[-1 -1] [0 -1]}))

