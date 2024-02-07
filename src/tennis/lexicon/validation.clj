(ns tennis.lexicon.validation
  (:require [malli.core :as m]
            [malli.error :as me]))

(defn- coerce-type [type]
  (condp = type
    "integer" :int
    "unknown" :any
    (keyword type)))

(defn- schema->malli [k def required]
  (let [rule (if (not required)
               [k {:optional true}]
               [k])]
    [:map (conj rule (coerce-type (:type def)))]))

(defn validate
  ([k concrete-def value] (validate k concrete-def value true))
  ([k concrete-def value required]
   (let [valid-schema (schema->malli k concrete-def required)]
     (m/validate valid-schema (if-not value
                                {}
                                {k value})))))

(defn explain
  ([k concrete-def value] (explain concrete-def value true))
  ([k concrete-def value required]
   (let [valid-schema (schema->malli k concrete-def required)
         val          (if-not value {} {k value})]
     (-> valid-schema
         (m/explain val)
         (me/humanize val)))))
