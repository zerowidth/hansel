(ns hansel.grid)

(defn- neighbors-for
  "Generate a list of potential neighbors for a given node on a grid"
  [[x y]]
  (for [dx [-1 0 1]
        dy [-1 0 1]
        :when (not= 0 dx dy)]
    [(+ dx x) (+ dy y)]))

(defn- reachable [[x y] nodes]
  "Return a predicate function that determines if a given node is reachable as a
  neighbor of node [x y] in nodes."
  (fn [[new-x new-y]]
    (if (nodes [new-x new-y])
      (if (or (= 0 (- new-x x)) (= 0 (- new-y y)))
        true
        ; filter out unreachable diagonals:
        (or (nodes [new-x y]) (nodes [x new-y]))))))

(defn neighbors
  "Retrieve the neighbors of a given node in nodes, based on a grid."
  [node nodes]
  (set (filter (reachable node nodes) (neighbors-for node))))

(defn transitions-for
  "Retrieve the grid-based transitions map for the given list of nodes"
  [nodes]
  (let [edges (set (for [node nodes
                         neighbor (neighbors node nodes)]
                     #{node neighbor}))]
    (reduce (fn [index [a b]]
              (merge-with into index {a [b] b [a]}))
            {}
            (map seq edges))))
