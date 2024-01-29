(ns tennis.lexicon.validation
  (:require [malli.core :as m]
            [malli.error :as me]))

(defn- coerce-type [type]
  (condp = type
    "integer" :int
    "unknown" :any
    (keyword type)))

(defn- schema->malli [def required]
  (let [rule (if (not required)
               [k {:optional true}]
               [k])]
    (m/schema (conj rule (coerce-type (:type v))))))

(defn validate
  ([concrete-def value] (validate concrete-def value true))
  ([concrete-def value required]
   (let [valid-schema (schema->malli concrete-def required)]
     (m/validate valid-schema value))))

(defn explain
  ([concrete-def value] (explain concrete-def value true))
  ([concrete-def value required]
   (let [valid-schema (schema->malli concrete-def required)]
     (-> valid-schema
         (m/explain data)
         (me/humanize data)))))
