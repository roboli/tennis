(ns tennis.lexicon.validation
  (:require [malli.core :as m]
            [malli.error :as me]))

(defn- coerce-type [type]
  (condp = type
    "integer" :int
    "unknown" :any
    (keyword type)))

(defn- schema->mallin [constraints]
  (let [required (set (map keyword (:required constraints)))]
    (reduce
     (fn [acc [k v]]
       (let [rule (if (not (required k))
                    [k {:optional true}]
                    [k])]
         (conj acc (conj rule (coerce-type (:type v))))))
     [:map]
     (:properties constraints))))

(def constraints-paths
  {:query-parameters [:defs :main :parameters]
   :procedure-input [:defs :main :input :schema]})

(defn validate [constraint-type schema data]
  (let [constraints  (get-in schema (constraint-type constraints-paths))
        valid-schema (schema->mallin constraints)]
    (m/validate valid-schema data)))

(defn explain [constraint-type schema data]
  (let [constraints  (get-in schema (constraint-type constraints-paths))
        valid-schema (schema->mallin constraints)]
    (-> valid-schema
        (m/explain data)
        (me/humanize data))))

(defn schema-type [schema]
  (keyword (get-in schema [:defs :main :type])))

(def encoding-paths
  {:procedure-input [:defs :main :input :encoding]})

(defn schema-encoding [encoding-type schema]
  (get-in schema (encoding-type encoding-paths)))
