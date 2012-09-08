(ns hansel.astar
  (:require [hansel.grid :as grid])
  (:use [clojure.data.priority-map :only [priority-map]]))

(defn- init [nodes start dest cost-fn f-score]
  {:neighbors (partial grid/neighbors (set nodes))
   :cost-fn cost-fn
   :f-score f-score
   :start start
   :dest dest
   :current start
   :f-scores (priority-map start (cost-fn start dest))
   :g-scores {start 0}
   :open #{start}
   :closed #{}
   :parents {}})

(defn- calculate-new-costs
  [{:keys [neighbors g-scores closed current dest cost-fn f-score] :as state}]
  (assoc state
         :updates
         (for [node (remove closed (neighbors current))
               :let [node-g (g-scores node)
                     new-g (+ (g-scores current) (cost-fn current node))]
               :when (or (nil? node-g) (< new-g node-g))]
           {:node node :g new-g :f (f-score new-g (cost-fn node dest))})))

(defn- update-parents
  [{:keys [parents current updates] :as state}]
  (assoc state :parents (into parents (zipmap (map :node updates) (repeat current)))))

(defn- update-g-scores
  [{:keys [g-scores updates] :as state}]
  (assoc state :g-scores (into g-scores (zipmap (map :node updates) (map :g updates)))))

(defn- update-f-scores
  [{:keys [f-scores current updates] :as state}]
  (assoc state
         :f-scores
         (-> f-scores
           (into (zipmap (map :node updates) (map :f updates)))
           (dissoc current)
           (or {}))))

(defn- update-closed
  [{:keys [closed current] :as state}]
  (assoc state :closed (conj closed current)))

(defn- update-open
  [{:keys [open g-scores closed] :as state}]
  (assoc state :open (apply disj (set (keys g-scores)) closed)))

(defn- next-closest-as-current
  [{:keys [f-scores current dest] :as state}]
  (assoc state :current (if (or (= current dest) (empty? f-scores))
                          nil
                          (first (peek f-scores)))))

(defn- step [state]
  (if (:current state)
    (-> state
      calculate-new-costs
      update-parents
      update-g-scores
      update-f-scores
      update-closed
      update-open
      next-closest-as-current)))

(defn astar
  "Return a lazy sequence of states for the A* algorithm, given a set of nodes,
  a starting node, and a destination node."
  [{:keys [nodes start dest cost-fn f-score]
    :or {cost-fn grid/chebychev
         f-score (fn [g h] (+ g h))}}]
  (take-while identity (iterate step (init nodes start dest cost-fn f-score))))

(defn dijkstra
  "Return a lazy sequence of states for Dijkstra's algorithm"
  [args]
  (astar (assoc args :f-score (fn [g h] g))))

(defn greedy
  "Return a lazy sequence of states for greedy best-first algorithm"
  [args]
  (astar (assoc args :f-score (fn [g h] h))))

