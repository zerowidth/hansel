(ns hansel.dijkstra
  (:use [clojure.data.priority-map :only [priority-map]]))

; dijkstra's:
;
; distance to c is 0
; current node c = start
; loop
;   for all unvisited neighbors n of c
;     distance to n is distance to c + cost of edge to n
;     if distance < value in n, update n
;   mark c as visited
;   if destination was marked visited or no unvisited nodes left, terminate
;   mark c as node with next smallest tentative distance
; if destination visited, walk backward to roll up the path to the node
;

(defn- dijkstra-init [transitions start dest]
  {:transitions transitions
   :start start
   :dest dest
   :current start
   :costs (priority-map start 0)
   :open #{}
   :closed #{}
   :paths {}})

(defn- dijkstra-step
  [{:keys [transitions dest current closed costs paths] :as state}]
   (if current
     (let [neighbors (remove closed (transitions current))
           new-cost (inc (costs current))
           lower-costs (for [node neighbors
                             :let [node-cost (costs node)]
                             :when (or (nil? node-cost) (< new-cost node-cost))]
                         [node new-cost])
           updated-paths (merge paths (zipmap
                                        (map first lower-costs)
                                        (repeat current)))
           updated-costs (into costs lower-costs)
           visited (conj closed current)
           next-closest (if (visited dest)
                          nil
                          (first (first (apply dissoc updated-costs visited))))]
       (assoc state
              :costs updated-costs
              :paths updated-paths
              :closed visited
              :current next-closest))))

(defn path [state]
  "Return the calculated path given a state. Returns a sequence of nodes if
  there is a path from start to dest, nil if no path exists."
  (let [steps (reverse (take-while
                         identity
                         (iterate (:paths state) (:dest state))))]
    (if ((set steps) (:start state))
      steps)))

(defn dijkstra
  "Return a lazy sequence of states for dijkstra's algorithm, given a set of
  node transitions, a starting node, and a destination node."
  [{:keys [transitions start dest]}]
  (take-while identity (iterate dijkstra-step (dijkstra-init transitions start dest))))

