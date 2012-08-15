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

(defn points-where [pred rows x y]
  (pred ((vec (rows y)) x)))

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

(defn dijkstra
  ([paths start dest]
    (dijkstra {:paths paths
               :start start
               :dest dest
               :current start
               :costs { start 0 }
               :open #{}
               :closed #{}}))
  ([{:keys [paths dest current open closed costs] :as state}]
   (let [neighbors (remove closed (paths current))
         new-cost (inc (costs current))
         updated-costs (merge
                         costs
                         (apply merge (map (fn [node]
                                           (if-let [node-cost (costs node)]
                                             (if (< new-cost node-cost)
                                               {node new-cost})
                                             {node new-cost}))
                                         neighbors)))
         visited (conj closed current)
         next-available (select-keys
                          updated-costs
                          (remove visited (keys updated-costs)))
         next-closest (first (first (sort-by (fn [[k v]] v) next-available)))]
     (assoc state
            :costs updated-costs
            :closed visited
            :current next-closest))))

; let  game ". . . . . # . z
;            . . . . . # . .
;            . . # . . # . .
;            . . # . . . . .
;            a . # . . . . ."
(let [game ". . # . .
           a . # . z
           . . . . ."
      lines (->> game str/split-lines (map #(remove #{\space} %)))
      width (count (first lines))
      height (count lines)
      rows (zipmap (range (count lines)) lines)
      nodes (set (for [y (range height)
                       x (range width)
                       :when (points-where #{\. \a \z} rows x y)]
                   [x y]))
      start (first (for [x (range width)
                         y (range height)
                         :when (points-where #{\a} rows x y)
                         ] [x y]))
      dest (first (for [x (range width)
                        y (range height)
                        :when (points-where #{\z} rows x y)
                        ] [x y]))
      edges (set (for [node nodes
                       neighbor (neighbors-in node nodes)]
                   #{node neighbor}))
      paths (reduce (fn [index [a b]]
                    (merge-with into index {a [b] b [a]}))
                  {}
                  (map seq edges))
      dj (iterate dijkstra [paths start dest])
      ]
  (pprint (take 1 dj))
  (pprint (take 2 dj))
  )
