(ns tennis.api
  (:require [tennis.xrpc :as xrpc]))

(defn get-post-thread [host uri]
  (xrpc/process-request host "app.bsky.feed.getPostThread" {"uri" uri}))

(defn resolve-handle [host handle]
  (xrpc/process-request host "com.atproto.identity.resolveHandle" {"handle" handle}))

(defn service [host]
  (fn [method & args]
    (condp = method
      :get-post-thread (get-post-thread host (first args))
      :resolve-handle  (resolve-handle host (first args)))))
