(ns tennis.lexicon.core
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [tennis.lexicon.validation :refer [validate explain]]))

(def concrete-types #{"boolean" "integer" "string" "bytes" "cid-link" "blob"})

(def lexicons (->> "lexicons.edn"
                   io/resource
                   slurp
                   edn/read-string))

(def encoding-paths
  {:procedure-input [:defs :main :input :encoding]})

(defn encoding-def [def type] (get-in def [type :encoding]))

(defn type-def [def] (keyword (:type def)))

(defn- nsid->path [nsid]
  (let [values (clojure.string/split nsid #"#")]
    (if (= 1 (count values))
      ["" (first values)]
      values)))

(defn find-def [nsid]
  (let [[id name-def] (nsid->path nsid)]
    (if (seq id)
      (get-in lexicons [id (keyword :defs name-def)])
      (get-in lexicons [name-def :defs :main]))))

(declare recur-defs)
(declare visit-def)

(defn to-concrete [[k def] required value]
  (condp = (:type def)
        "array"
        (if (coll? value)
          (reduce-kv
           (fn [acc idx val]
             (let [results (recur-defs [[k (:items def)]]
                                       required
                                       {k val}
                                       [])]
               (if (seq results)
                 (let [result (first results)]
                   (conj acc {k {idx (get result k)}}))
                 acc)))
           []
           value)
          [{k ["Must be an array"]}])

        "ref"
        (let [lexicon-def (find-def (:ref def))]
          (visit-def lexicon-def {k value}))))

(defn recur-defs [defs required data results]
  (if (empty? defs)
    results
    (let [[k def] (first defs)
          value   (get data k)]
      (if (concrete-types (:type def))
        (let [result (validate k def value (required k))]
          (if-not result
            (recur-defs (rest defs)
                        required
                        data
                        (conj results (explain k def value (required k))))
            (recur-defs (rest defs)
                        required
                        data
                        results)))
        (if (and (not (required k))
                 (empty? value))
          (recur-defs (rest defs)
                      required
                      data
                      results)
          (recur-defs (rest defs)
                      required
                      data
                      (let [rs (to-concrete [k def] required value)]
                        (if (seq rs)
                          (into results rs)
                          results))))))))

(defn visit-def [def data]
  (let [defs     (map (fn [[k v]] [k v]) (:properties def))
        required (set (map keyword (:required def)))]
    (recur-defs defs required data [])))

(defmulti assert-valid-xrpc :type)

(defmethod assert-valid-xrpc :query-input [{:keys [def data]}]
  (visit-def (:parameters def) data))

(defmethod assert-valid-xrpc :procedure-input [{:keys [def data]}]
  (visit-def (get-in def [:input :schema]) data))

(defmethod assert-valid-xrpc :output [{:keys [def data]}]
  (visit-def (get-in def [:output :schema]) data))
