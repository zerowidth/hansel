(ns hansel.grid)

(defn- neighbors-for
  "Generate a list of potential neighbors for a given node on a grid"
  [[x y]]
  (for [dx [-1 0 1]
        dy [-1 0 1]
        :when (not= 0 dx dy)]
    [(+ dx x) (+ dy y)]))

(defn- reachable [nodes [x y]]
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
  [nodes node]
  (filter (reachable nodes node) (neighbors-for node)))

(defn transitions-for
  "Retrieve the grid-based transitions map for the given list of nodes"
  [nodes]
  (let [edges (set (for [node nodes
                         neighbor (neighbors nodes node)]
                     #{node neighbor}))]
    (reduce (fn [index [a b]]
              (merge-with into index {a [b] b [a]}))
            {}
            (map seq edges))))

(defn- abs [n] (if (< n 0) (- n) n))

(defn chebychev
  "chebychev or diagonal distance, diagonal is just as cheap as linear movement"
  [[ax ay] [bx by]]
  (Math/max (Math/abs (- bx ax)) (Math/abs (- by ay))))

(defn weighted-chebychev
  "attempt to tweak chebychev by having diagonal movement cost just a bit more
  than straight line movement"
  [[ax ay] [bx by]]
  (let [dx (abs (- bx ax))
        dy (abs (- by ay))]
    (+ (- (max dx dy) (min dx dy)) (* (min dx dy) 1.001))))
