(ns hansel.text-interface
  (:require [clojure.string :as str])
  (:use [hansel.grid :only [neighbors]]))

(defn graph-from-text
  "Convert a text represenation of a graph to its node transitions, start point,
  and dest point.

  text - the text representation of a graph: a = start, z = dest, . = open.
      . . . . . # . z
      . . . . . # . .
      . . # . . # . .
      . . # . . . . .
      a . # . . . . .

  Returns a map with :start, :dest, and :transtions"
  [text]
  (let [lines (->> text str/split-lines (map #(remove #{\space} %)))
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
        nodes (set (apply concat (vals map-nodes)))]
      {:start start
       :dest dest
       :nodes nodes
       :neighbors (partial neighbors (set nodes))}))
