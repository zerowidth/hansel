(ns hansel.server
  (:use [noir.core]
        [ring.util.response :as response]
        [dieter.core :only [asset-pipeline]])
  (:require [noir.server :as server]
            [noir.response]))

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

(defn start-server [port]
  (server/start port))

(defpage "/" [] (response/resource-response "public/index.html"))
