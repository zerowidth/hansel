(ns hansel.core
  (:require [clojure.string :as str])
  (:use [hansel.text-interface :only [graph-from-text]]
        [hansel.dijkstra :only [path dijkstra]]))

(defn -main
  "I don't do a whole lot."
  [& args]
  (println "Hello, World!"))

(defn solve [the-map]
  (let [graph (graph-from-text the-map)
        steps (dijkstra graph)]
    (prn (count steps))
    (prn (path (last steps)))))

(solve ". . . . . # . z
       . . . . . # . .
       . . # . . # . .
       . . # . . . . .
       a . # . . . . .")
(solve ". . # . .
       a . # . z
       . . . . .")
(solve "a # z")
