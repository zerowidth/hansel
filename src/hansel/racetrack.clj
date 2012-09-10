(ns hansel.racetrack
  (require [clojure.set :as set]))

(defn raytrace
  "Return a vector of grid squares intersected by the line between the two given
  squares. Assumes a path from the centers of the grid squares. Based on
  http://playtechs.blogspot.com/2007/03/raytracing-on-grid.html"
  [[x0 y0] [x1 y1]]
  (let [dx (* 2 (Math/abs (- x1 x0)))
        dy (* 2 (Math/abs (- y1 y0)))
        x-inc (if (> x1 x0) 1 -1)
        y-inc (if (> y1 y0) 1 -1)]
    (loop [points []
           n (+ 1 (/ (+ dx dy) 2))
           x x0
           y y0
           err (/ (- dx dy) 2)]
      (if (> n 0)
        (cond
          (= err 0) (recur (conj points [x y]) (- n 2) (+ x x-inc) (+ y y-inc) (+ (- err dy) dx))
          (> err 0) (recur (conj points [x y]) (dec n) (+ x x-inc) y (- err dy))
          :else     (recur (conj points [x y]) (dec n) x (+ y y-inc) (+ err dx)))
        points))))

(defn- and-surrounding [[x y]]
  "Return a list of the square and surrounding neighbors for a given square on a grid"
  [[x y]]
  (set (for [dx [-1 0 1] dy [-1 0 1]] [(+ dx x) (+ dy y)])))

(defn- available-points [grid pos]
  (set/intersection grid (and-surrounding pos)))

(defn- diagonal-open?
  "Given a grid and a path segment, check that at least one diagonal is open on
  the grid if the path is diagonal"
  [grid [[x0 y0] [x1 y1]]]
  (if (or (= 0 (- x1 x0)) (= 0 (- y1 y0)))
    true
    (or (grid [x1 y0]) (grid [x0 y1]))))

(defn clear-path?
  "Check if a path is clear on grid, given a start and end square"
  [grid from to]
  (let [path (raytrace from to)]
    (and (set/subset? (set path) grid)
         (every? (partial diagonal-open? grid) (partition 2 1 path)))))

(defn next-reachable
  "Return the next reachable points of a racetrack point/velocity pair"
  [grid [pos velocity]]
  (let [next-pos (vec (map + pos velocity))]
    (filter (partial clear-path? grid pos) (available-points grid next-pos))))

(defn available-moves
  [grid [pos v]]
  (map (fn [new-pos] [new-pos (vec (map - new-pos pos))]) (next-reachable grid [pos v])))

