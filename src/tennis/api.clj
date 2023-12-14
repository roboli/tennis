(ns tennis.api
  (:require [tennis.xrpc :as xrpc]))

(defn service [host]
  (fn [method & args]
    (if (= method :resolve-handle)
      (xrpc/process-request host "com.atproto.identity.resolveHandle" {"handle" (first args)}))))
