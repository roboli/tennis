(ns tennis.lexicon.schemas
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]))

(def schemas (->> "lexicons.edn"
                  io/resource
                  slurp
                  edn/read-string))
