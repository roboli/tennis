(ns tennis.core
  (:require [tennis.api :as api]))

(defn resolve-handle
  "Test resolving handle"
  [host handle]
  (let [agent (api/agent {:service host})]
    (api/resolve-handle agent handle)))

(defn get-post-thread
  "Test getting post thread"
  [host uri]
  (let [agent    (api/agent {:service host})
        response (api/get-post-thread agent uri)
        post     (get-in response [:data :thread :post])]
    {:author (get-in post [:author :handle])
     :name   (get-in post [:author :displayName])
     :text   (get-in post [:record :text])}))
