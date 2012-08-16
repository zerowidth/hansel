(ns hansel.grid)

(defn- neighbors-for
  "Generate a list of potential neighbors for a given node on a grid"
  [[x y]]
  (for [dx [-1 0 1]
        dy [-1 0 1]
        :when (not= 0 dx dy)]
    [(+ dx x) (+ dy y)]))

(defn neighbors
  "Retrieve the neighbors of a given node in nodes, based on a grid."
  [node nodes]
  (filter nodes (neighbors-for node)))


