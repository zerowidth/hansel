(ns hansel.jps
  (require [hansel.grid :as grid]
           [clojure.set :as set]))

(defn forced-neighbors
  "Return a set of the forced neighbors for a given node and direction"
  [grid [x y] [dx dy]]
  (let [all (set (grid/neighbors grid [x y]))]
    (->>
      (cond
        (= dy 0)
        (list
          (if (not (grid [x (inc y)])) [(+ x dx) (inc y)])
          (if (not (grid [x (dec y)])) [(+ x dx) (dec y)]))
        (= dx 0)
        (list
          (if (not (grid [(inc x) y])) [(inc x) (+ y dy)])
          (if (not (grid [(dec x) y])) [(dec x) (+ y dy)]))
        :diagonal
        (list
          (if (and
                (grid [x (+ y dy)]) ; can move diagonally
                (not (grid [(- x dx) y]))) [(- x dx) (+ y dy)])
          (if (and
                (grid [(+ x dx) y])
                (not (grid [x (- y dy)]))) [(+ x dx) (- y dy)])))
      set
      (set/intersection all)
      ((fn [s] (if (empty? s) nil s))))))

(defn normalize [d]
  (vec (map #(cond (> % 1) 1 (< % -1) -1 :else %) d)))

(defn direction [from to]
  (vec (normalize (map - to from))))

(defn neighbors
  "Return the pruned neighbors on a grid given parents and a node"
  [grid parents node]
  (let [all (set (grid/neighbors grid node))]
    (if-let [p (parents node)]
      (let [[x y] node
            [dx dy] (direction p node)]
        (->>
          (cond
            (= dy 0)
            (list [ (+ x dx) y ] )
            (= dx 0)
            (list [ x (+ y dy) ])
            :diagonal (list
                        [ x (+ y dy) ]
                        [ (+ x dx) y ]
                        [ (+ x dx) (+ y dy) ]))
          set
          (set/union (forced-neighbors grid [x y] [dx dy]))
          (set/intersection all)))
      all)))

(defn diagonal? [[dx dy]]
  (not (or (= dx 0) (= dy 0))))

; Require: x: initial node, d⃗: direction, s: start, g: goal
; n ← step(x, d⃗)
; if n is an obstacle or is outside the grid then
;   return null
; if n=g then
;   return n
; if ∃ n′ ∈ neighbours(n) s.t. n′ is forced then
;   return n
; if d⃗ is diagonal then
;   for all i ∈ {1, 2} do
;     if jump(n,d⃗i,s,g) is not null then return n
; return jump(n, d⃗, s, g)
(defn jump
  "Jump on grid, starting at node, in a direction"
  [grid node direction goal]
  (let [n (vec (map + node direction))
        [dx dy] direction
        diag? (diagonal? direction)]
    (if ((set (grid/neighbors grid node)) n)
      (cond
        (= n goal) n
        (forced-neighbors grid n direction) n
        (and diag?
             (or (jump grid n [dx 0] goal)
                 (jump grid n [0 dy] goal))) n
        ; not completely optimal, but partially tail-call:
        :else (recur grid n direction goal)))))

; Require: x: current node, s: start, g: goal
; successors(x) ← ∅
; neighbours(x)←prune(x,neighbours(x))
; for all n ∈ neighbours(x) do
;   n ← jump(x, direction(x, n), s, g)
;   add n to successors(x)
; return successors(x)
(defn successors
  "Identify the successors of a node, given its parents and the goal"
  [grid parents start goal]
  (letfn [(jump-to [x] (jump grid start (direction start x) goal))]
    (set (filter identity (map jump-to (neighbors grid parents start))))))

