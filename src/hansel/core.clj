(ns hansel.core
  (:require [clojure.string :as str])
  (:use [hansel.server :only [start-server]]
        [hansel.text-interface :only [graph-from-text]]
        [hansel.dijkstra :only [path dijkstra]]))

(defn -main
  "I don't do a whole lot."
  [& args]
  (start-server 8080))

(defn solve [the-map]
  (let [graph (graph-from-text the-map)
        steps (dijkstra graph)]
    (prn (count steps))
    (prn (path (last steps)))))

; (solve ". . . . . # . z
;        . . . . . # . .
;        . . # . . # . .
;        . . # . . . . .
;        a . # . . . . .")
; (solve ". . # . .
;        a . # . z
;        . . . . .")
; (solve "a # z")
