(ns tennis.core
  (:require [tennis.api :as api]))

(defn resolve-handle
  "Test resolving handle"
  [host handle]
  (let [agent (api/service host)]
    (agent :resolve-handle handle)))
