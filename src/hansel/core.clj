(ns hansel.core
  (:require [clojure.string :as str]
            [hansel.grid :as grid]
            [hansel.racetrack :as racetrack])
  (:use [hansel.server :only [start-server]]
        [hansel.text-interface :only [graph-from-text]]
        [clojure.pprint]
        [hansel.util :only [path]]
        [hansel.astar :only [astar]]))

(defn -main
  "Start the web app"
  [& args]
  (start-server 8080))

(defn solve [the-map]
  (let [graph (graph-from-text the-map)
        ; steps (astar graph)
        steps (astar (assoc graph
                            :neighbors (partial racetrack/available-moves (:nodes graph))
                            :start [(:start graph) [0 0]]
                            :dest [(:dest graph) [0 0]]
                            :g-score (constantly 1)
                            :h-score (racetrack/cost grid/chebychev)))
        ]
    (pprint (path (last steps)))
    ))

; (solve ". . . . . # . z
;        . . . . . # . .
;        . . # . . # . .
;        . . # . . . . .
;        a . # . . . . .")
; (solve ". . # . .
;        a . # . z
;        . . . . .")
; (solve "a # z")
; (solve "a . z")
; (solve "a . . . . . . . . z")
; (solve ". . . . .
;        a . . . z
;        . . . . .")
