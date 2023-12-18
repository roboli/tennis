(ns tennis.api
  (:require [tennis.xrpc :as xrpc]))

(defn agent [opts]
  (fn [method]
    (condp = method
      :get-post-thread
      (fn [uri]
        (xrpc/process-request (:service opts) "app.bsky.feed.getPostThread" {"uri" uri}))

      :resolve-handle
      (fn [handle]
        (xrpc/process-request (:service opts) "com.atproto.identity.resolveHandle" {"handle" handle})))))

(defn resolve-handle [svc handle]
  ((svc :resolve-handle) handle))

(defn get-post-thread [svc uri]
  ((svc :get-post-thread) uri))
