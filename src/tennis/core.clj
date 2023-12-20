(ns tennis.core
  (:require [tennis.api :as api]))

(defn resolve-handle
  "Test resolving handle"
  [service handle]
  (let [agent (api/agent {:service service})]
    (api/resolve-handle agent handle)))

(defn get-post-thread
  "Test getting post thread"
  [service uri]
  (let [agent    (api/agent {:service service})
        response (api/get-post-thread agent uri)
        post     (get-in response [:data :thread :post])]
    {:author (get-in post [:author :handle])
     :name   (get-in post [:author :displayName])
     :text   (get-in post [:record :text])}))

(defn post
  "Test creating a post"
  [service handle pwd text]
  (let [agent   (api/agent {:service service})
        session (api/create-session agent handle pwd)]
    (api/post session text)))
