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
   :parents {}})

(defn- calculate-new-costs
  [{:keys [transitions g-scores closed current dest] :as state}]
  (let [new-g (inc (g-scores current))]
    (assoc state
           :updates
           (for [node (remove closed (transitions current))
                 :let [node-g (g-scores node)]
                 :when (or (nil? node-g) (< new-g node-g))]
             {:node node :g new-g :f (+ new-g (* 1.001 (chebychev node dest)))}))))

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
  "Return a lazy sequence of states for the A* algorithm, given a set of node
  transitions, a starting node, and a destination node."
  [{:keys [transitions start dest]}]
  (take-while identity (iterate step (init transitions start dest))))

