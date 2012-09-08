(ns hansel.server
  (:use [noir.core]
        [dieter.core :only [asset-pipeline]]
        [ring.middleware.json-params :only [wrap-json-params]])
  (:require [hansel.grid :as grid]
            [hansel.util :as util]
            [hansel.astar :as astar]
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

(defn- filter-keys-for-presentation
  [steps]
  (map #(select-keys % [:open :closed :current :paths]) steps))

(defpage [:post, "/paths"] {:strs [start dest nodes alg cost]}
         (let [algorithm (case alg
                           "astar" astar/astar
                           "dijkstra" astar/dijkstra
                           "greedy" astar/greedy
                           astar/astar)
               cost-fn (case cost
                         "chebychev" grid/chebychev
                         "tweaked" grid/weighted-chebychev
                         "euclidean" grid/euclidean
                         grid/chebychev)]
           (prn "using alg" algorithm "cost-fn" cost-fn)
           (response/json (->>
                            {:start start
                             :dest dest
                             :nodes nodes
                             :cost-fn cost-fn}
                            algorithm
                            add-final-state
                            (map calculate-paths)
                            filter-keys-for-presentation))))
