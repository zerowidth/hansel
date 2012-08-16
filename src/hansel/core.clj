(ns hansel.core
  (:require [clojure.string :as str])
  (:use [clojure.pprint :only [pprint]]
        [clojure.set :only [union]]))

(defn -main
  "I don't do a whole lot."
  [& args]
  (println "Hello, World!"))
; given a graph representation in text, find the shortest path.
; e.g.
;
; . . . . . # . z
; . . . . . # . .
; . . # . . # . .
; . . # . . . . .
; a . # . . . . .
;
; a = start, z = finish

(defn neighbors [[x y]]
  (for [dx [-1 0 1]
        dy [-1 0 1]
        :when (not= 0 dx dy)]
    [(+ dx x) (+ dy y)]))

(defn neighbors-in [node nodes]
  (filter nodes (neighbors node)))

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

(defn dijkstra-init [edges start dest]
  {:edges edges
   :start start
   :dest dest
   :current start
   :costs { start 0 }
   :open #{}
   :closed #{}
   :paths {}})

(defn dijkstra
  [{:keys [edges dest current closed costs paths] :as state}]
   (if current
     (let [neighbors (remove closed (edges current))
           new-cost (inc (costs current))
           lower-costs (apply merge (map (fn [node]
                                               (if-let [node-cost (costs node)]
                                                 (if (< new-cost node-cost)
                                                   {node new-cost})
                                                 {node new-cost}))
                                             neighbors))
           updated-paths (merge paths (zipmap (keys lower-costs) (repeat current)))
           updated-costs (merge costs lower-costs)
           visited (conj closed current)
           next-available (select-keys
                            updated-costs
                            (remove visited (keys updated-costs)))
           next-closest (first (first (sort-by (fn [[k v]] v) next-available)))]
       (assoc state
              :costs updated-costs
              :paths updated-paths
              :closed visited
              :current next-closest)
       )))

(defn path [state]
  (let [steps (reverse (take-while
                         (complement nil?)
                         (iterate (:paths state) (:dest state))))]
    (if ((set steps) (:start state))
      steps)))

(let [the-map ". . . . . # . z
              . . . . . # . .
              . . # . . # . .
              . . # . . . . .
              a . # . . . . ."
; (let [the-map ". . # . .
;               a . # . z
;               . . . . ."
; (let [the-map "a # z"
      lines (->> the-map str/split-lines (map #(remove #{\space} %)))
      width (count (first lines))
      height (count lines)
      rows (zipmap (range (count lines)) lines)
      map-nodes (apply merge-with into (for [y (range height)
                                             x (range width)
                                             :let [c ((vec (rows y)) x)]
                                             :when (#{\. \a \z} c)]
                                         {c [[x y]]}))
      start (first (map-nodes \a))
      dest (first (map-nodes \z))
      nodes (set (apply concat (vals map-nodes)))
      edges (set (for [node nodes
                       neighbor (neighbors-in node nodes)]
                   #{node neighbor}))
      paths (reduce (fn [index [a b]]
                      (merge-with into index {a [b] b [a]}))
                    {}
                    (map seq edges))
      dj (iterate dijkstra (dijkstra-init paths start dest))
      steps (take-while (complement nil?) dj)
      ]
  ; (pprint (last steps))
  (pprint (path (last steps)))
  )
