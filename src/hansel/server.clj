(ns hansel.server
  (:use [noir.core]
        [dieter.core :only [asset-pipeline]]
        [ring.middleware.json-params :only [wrap-json-params]])
  (:require [hansel.grid :as grid]
            [hansel.dijkstra :as dijkstra]
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

(defn- reverse-paths
  "reverse the given map of paths into a sequence of [val, key]"
  [paths]
  (map (fn [[k v]] [v k]) paths))

(defn- final-state
  "adds a final state with a paths filtered to only include final path nodes,
  if the path exists"
  [steps]
  (let [final (last steps)
        path (dijkstra/path final)
        final-path (reverse-paths (partition 2 1 path))]
    (concat steps (list (assoc final
                               :paths final-path
                               :open #{}
                               :closed #{})))))

(defpage [:post, "/paths"] {:strs [start dest nodes]}
         (let [transitions (grid/transitions-for (set nodes))
               steps (astar/astar {:start start
                                         :dest dest
                                         :transitions transitions})
               with-final (final-state steps)
               filtered (map #(select-keys % [:open :closed :current :costs :paths])
                             with-final)
               munged (map #(assoc %
                                   :paths (reverse-paths (:paths %))
                                   :costs (seq (:costs %))) filtered)]
           (response/json munged)))
