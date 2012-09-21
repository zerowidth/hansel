(ns hansel.astar
  (:require [hansel.grid :as grid])
  (:use [clojure.data.priority-map :only [priority-map]]))


(defn- calculate-new-costs
  [{:keys [neighbors parents g-scores closed current dest f-score g-score h-score] :as state}]
  (assoc state
         :updates
         (for [node (remove closed (neighbors current parents))
               :let [node-g (g-scores node)
                     new-g (+ (g-scores current) (g-score current node))]
               :when (or (nil? node-g) (< new-g node-g))]
           {:node node :g new-g :f (f-score new-g (h-score node dest))})))

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
  "Return a lazy sequence of states for the A* algorithm.

  Required keys are:

    :start         - the starting node
    :dest          - the destination node
    :neighbors     - (fn [node]...) to retrieve neighbors of a given node

  Optional keys:

    :f-score       - (fn [g h]...) heuristic function, defaults to (+ g h)
    :g-score       - (fn [a b]...) distance cost for paths,
                     defaults to grid/chebychev
    :h-score       - (fn [a b]...) distance cost for paths,
                     defaults to grid/chebychev"
  [{:keys [start dest neighbors f-score g-score h-score]
    :or {f-score (fn [g h] (+ g h))
         g-score grid/chebychev
         h-score grid/chebychev}}]
  (let [init {:start start
              :dest dest
              :neighbors neighbors
              :f-score f-score
              :g-score g-score
              :h-score h-score
              :current start
              :f-scores (priority-map start (f-score (g-score start dest) (h-score start dest)))
              :g-scores {start 0}
              :open #{start}
              :closed #{}
              :parents {}}]
    (take-while identity (iterate step init))))

(defn dijkstra
  "Return a lazy sequence of states for Dijkstra's algorithm"
  [args]
  (astar (assoc args :f-score (fn [g h] g))))

(defn greedy
  "Return a lazy sequence of states for greedy best-first algorithm"
  [args]
  (astar (assoc args :f-score (fn [g h] h))))

