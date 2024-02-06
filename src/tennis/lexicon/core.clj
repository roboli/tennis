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
(declare visit-lexicon)

(defn to-concrete [[k def] required value]
  (condp = (:type def)
        "array"
        (if (seq? value)
          (reduce
           (fn [acc val]
             (let [results (recur-defs [[k (:items def)]]
                                       required
                                       {k value}
                                       [])]
               (if (seq? results)
                 (conj acc results)
                 acc)))
           []
           value)
          [{k "Must be an array"}])

        "ref"
        (let [lexicon (find-def (:ref def))]
          (visit-lexicon lexicon {k value}))))

(defn recur-defs [defs required data results]
  (if (empty? defs)
    results
    (let [[k def] (first defs)]
      (if (concrete-types (:type def))
        (let [value  (get data k)
              result (validate def value (required k))]
          (if-not result
            (recur-defs (rest defs)
                        required
                        data
                        (conj results {k (explain def value (required k))}))
            (recur-defs (rest defs)
                        required
                        data
                        results)))
        (if (and (not (required k))
                 (empty? (get data k)))
          (recur-defs (rest defs)
                      required
                      data
                      results)
          (recur-defs (rest defs)
                      required
                      data
                      (conj results
                            (to-concrete [k def] required data))))))))

(defn visit-lexicon [lexicon data]
  (let [defs     (map (fn [[k v]] [k v]) (:properties lexicon))
        required (set (map keyword (:required lexicon)))]
    (recur-defs defs required data [])))

(defmulti assert-valid-xrpc :type)

(defmethod assert-valid-xrpc :query-input [{:keys [def data]}]
  (visit-lexicon (:parameters def) data))

(defmethod assert-valid-xrpc :procedure-input [{:keys [def data]}]
  (visit-lexicon (get-in def [:input :schema]) data))

(defmethod assert-valid-xrpc :output [{:keys [def data]}]
  (visit-lexicon (get-in def [:output :schema]) data))
