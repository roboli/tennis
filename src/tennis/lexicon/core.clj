(ns tennis.lexicon.core
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]))

(def lexicons (->> "lexicons.edn"
                   io/resource
                   slurp
                   edn/read-string))

(def encoding-paths
  {:procedure-input [:defs :main :input :encoding]})

(defn encoding-def [lexicon path-name]
  (get-in lexicon (path-name encoding-paths)))

(defn type-def
  ([lexicon] (type-def lexicon :main))
  ([lexicon def]
   (keyword (get-in lexicon [:defs def :type]))))
