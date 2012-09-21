(ns hansel.server
  (:use [noir.core]
        [dieter.core :only [asset-pipeline]]
        [ring.middleware.json-params :only [wrap-json-params]])
  (:require [hansel.grid :as grid]
            [hansel.util :as util]
            [hansel.astar :as astar]
            [hansel.racetrack :as racetrack]
            [hansel.jps :as jps]
            [noir.server :as server]
            [ring.util.response :as ring-response]
            [noir.response :as response]
            [noir.request :as request]))

(defn- log [msg & vals]
  (let [line (apply format msg vals)]
    (locking System/out (println line))))

(defn wrap-request-logging [handler]
  (fn [{:keys [request-method uri] :as req}]
    (let [resp (handler req)]
      (log "%s %s" request-method uri)
      resp)))

(server/add-middleware wrap-request-logging)
(server/add-middleware asset-pipeline)
(server/add-middleware wrap-json-params)

(defn start-server [port]
  (server/start port))

(defpage "/" [] (ring-response/resource-response "public/index.html"))

(defn- reverse-parents
  "reverse the given map of parents into a sequence of [val, key]"
  [parents]
  (map (fn [[k v]] [v k]) parents))

(defn- add-final-state
  "adds a final state with a paths filtered to only include final path nodes,
  if the path exists"
  [steps]
  (let [final (last steps)
        final-path (->> final util/path (partition 2 1) reverse-parents)]
    (concat steps (list (assoc final
                               :parents final-path
                               :open #{}
                               :closed #{})))))

(defn- calculate-paths [step]
  (assoc step :paths (reverse-parents (:parents step))))

(defn- expand-parents [step]
  (assoc step :paths (jps/expand-parents (:paths step))))

(defn filter-for-presentation
  [pos-fn {:keys [open closed current paths]}]
  {:open (map pos-fn open)
   :closed (map pos-fn closed)
   :current (pos-fn current)
   :paths (map #(map pos-fn %) paths)})

(defpage [:post, "/paths"] {:strs [start dest nodes alg cost jps racetrack]}
         (let [grid (set nodes)
               algorithm (case alg
                           "astar" astar/astar
                           "dijkstra" astar/dijkstra
                           "greedy" astar/greedy)
               start-pos (if racetrack [start [0 0]] start)
               end-pos (if racetrack [dest [0 0]] dest)
               neighbors (cond
                           racetrack (fn [n p] (racetrack/available-moves grid n))
                           jps (fn [n p] (jps/successors grid p n dest))
                           :else (fn [n p] (grid/neighbors grid n)))
               h-score (if racetrack
                         racetrack/guess-max-steps
                         (case cost
                           "chebychev" grid/chebychev
                           "tweaked" grid/weighted-chebychev
                           "euclidean" grid/euclidean))
               g-score (if racetrack (constantly 1) h-score)
               pos (if racetrack first identity)]
           (->>
             {:start start-pos
              :dest end-pos
              :neighbors neighbors
              :g-score g-score
              :h-score h-score}
             algorithm
             add-final-state
             ((if racetrack (partial take-last 1) identity))
             (map calculate-paths)
             ((if jps (partial map expand-parents) identity))
             (map (partial filter-for-presentation pos))
             response/json)))
