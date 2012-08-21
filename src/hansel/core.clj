(ns hansel.core
  (:require [clojure.string :as str])
  (:use [hansel.server :only [start-server]]
        [hansel.text-interface :only [graph-from-text]]
        [hansel.dijkstra :only [path dijkstra]]
        [hansel.astar :only [astar]]))

(defn -main
  "I don't do a whole lot."
  [& args]
  (start-server 8080))

(defn solve [the-map]
  (let [graph (graph-from-text the-map)
    (prn (count steps))
    (prn (path (last steps)))))
        steps (astar graph)]

; (solve ". . . . . # . z
;        . . . . . # . .
;        . . # . . # . .
;        . . # . . . . .
;        a . # . . . . .")
; (solve ". . # . .
;        a . # . z
;        . . . . .")
; (solve "a # z")
