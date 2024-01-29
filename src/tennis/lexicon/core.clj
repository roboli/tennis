(ns tennis.lexicon.core
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [tennis.lexicon.validation :refer [validate]]))

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
    (if (= 1 (count vals))
      ["" (first vals)]
      values)))

(defn find-def [nsid]
  (let [[id name-def] (nsid->path nsid)]
    (if (not (empty? id))
      (get-in lexicons [id (keyword name-def)])
      (get-in lexicons [(keyword name-def) :main]))))

(defn to-concrete [lexicon data]
  (condp (:type lexicon)
      "array" ))
;; HOW TO TREAT array concrete types VS array ref types ??

(defn recur-defs [defs required data results]
  (if (empty? defs)
    results
    (let [[k def] (first defs)]
      (if (concrete-types (:type def))
        (let [value  (get data k)
              result (validate def value (required k))]
          (if-not result
            (recur-defs (rest defs)
                        (conj results (explain def value (required k))))
            (recur-defs (rest defs) results)))
        (if (and (not (required k))
                 (seq (get data k)))
          (to-concrete def required data results)
          (recur-defs (rest defs) results))))))

(defn visit-lexicon [lexicon data]
  (let [properties (map (fn [[k v] [k v]])(:properties lexicon))
        required   (set (map keyword (:required lexicon)))]
    (recur-defs properties required data [])
    
    (reduce
     (fn [acc [k def]]
       (if (concrete-types (:type def))
         (let [value  (get data k)
               result (validate def value (required k))]
           (if-not result
             (conj acc (explain def value (required k)))
             acc))
         (if (and (not (required k))
                  (not (get data k)))
           (to-concrete lexicon data)
           acc)))
     []
     (:properties lexicon))))

(defmulti assert-valid-xrpc :type)

(defmethod assert-valid-xrpc :query-input [def path data]
  (visit-lexicon (:parameters def) data))

(defmethod assert-valid-xrpc :procedure-input [{:keys [def data]}]
  (visit-lexicon (get-in def [:input :schema]) data))

(defmethod assert-valid-xrpc :output [{:keys [def data]}]
  (visit-lexicon (get-in def [:output :schema]) data))
