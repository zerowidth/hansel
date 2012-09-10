(ns hansel.grid)

(defn- neighbors-for
  "Generate a list of potential neighbors for a given node on a grid"
  [[x y]]
  (for [dx [-1 0 1]
        dy [-1 0 1]
        :when (not= 0 dx dy)]
    [(+ dx x) (+ dy y)]))

(defn- reachable [nodes [x y] [new-x new-y]]
  "Determine if a new node is reachable as a neighbor of node [x y] in nodes."
  (if (nodes [new-x new-y])
    (if (or (= 0 (- new-x x)) (= 0 (- new-y y)))
      true
      ; filter out unreachable diagonals:
      (or (nodes [new-x y]) (nodes [x new-y])))))

(defn neighbors
  "Retrieve the neighbors of a given node in nodes, based on a grid."
  [nodes node]
  (filter (partial reachable nodes node) (neighbors-for node)))

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

(defn chebychev
  "Calculate the chebychev (diagonal) distance between two points"
  [[ax ay] [bx by]]
  (Math/max (Math/abs (- bx ax)) (Math/abs (- by ay))))

(defn weighted-chebychev
  "Tweaked chebychev, with diagonal movement cost a tiny bit more than straight
  line movement so pathfinding prefers straight lines first"
  [[ax ay] [bx by]]
  (let [dx (Math/abs (- bx ax))
        dy (Math/abs (- by ay))]
    (+ (- (max dx dy) (min dx dy)) (* (min dx dy) 1.001))))

(defn dot
  "Dot product of two vectors"
  [p q]
  (reduce + (map * p q)))

(defn euclidean
  "Calulcate the euclidian distance between two points"
  [p q]
  (let [v (map - q p)]
    (Math/sqrt (dot v v))))
