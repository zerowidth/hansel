(ns hansel.astar
  (:use [clojure.data.priority-map :only [priority-map]]))

(defn- chebychev [[ax ay] [bx by]]
  (Math/max (Math/abs (- bx ax)) (Math/abs (- by ay))))

(defn- init [transitions start dest]
  {:transitions transitions
   :start start
   :dest dest
   :current start
   :f-scores (priority-map start (chebychev start dest))
   :g-scores {start 0}
   :open #{start}
   :closed #{}
   :paths {}})

(defn- step
  [{:keys [transitions current dest closed f-scores g-scores paths] :as state}]
  (if current
    (let [new-g (inc (g-scores current))
          lower-costs (for [node (remove closed (transitions current))
                            :let [node-g (g-scores node)]
                            :when (or (nil? node-g) (< new-g node-g))]
                        {:node node :g new-g :f (+ new-g (* 1.001 (chebychev node dest)))})
          updated-paths (into paths (zipmap (map :node lower-costs) (repeat current)))
          updated-g (into g-scores (zipmap (map :node lower-costs) (map :g lower-costs)))
          updated-f (into f-scores (zipmap (map :node lower-costs) (map :f lower-costs)))
          visited (conj closed current)
          updated-f (dissoc updated-f current)
          next-closest (if (or (= current dest) (empty? updated-f))
                         nil
                         (first (peek updated-f)))]
      (assoc state
             :f-scores (if (empty? updated-f) {} (pop updated-f))
             :g-scores updated-g
             :paths updated-paths
             :open (apply disj (set (keys updated-g)) visited)
             :closed visited
             :current next-closest))))

(defn astar
  "Return a lazy sequence of states for the A* algorithm, given a set of node
  transitions, a starting node, and a destination node."
  [{:keys [transitions start dest]}]
  (take-while identity (iterate step (init transitions start dest))))

