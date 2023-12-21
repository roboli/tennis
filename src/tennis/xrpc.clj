(ns tennis.xrpc
  (:require [clj-http.client :as client]
            [cheshire.core :refer [parse-string generate-string]]
            [tennis.lexicon :as lex]))

(defn- schema->mallin [schema] identity)

(defn- validate [schema data] true)

(defn- explain [schema data] "errors")

(defn- http-method [schema]
  (if (= "query" (get-in schema [:defs :main :type]))
    client/get
    client/post))

(defn- construct-headers [schema headers payload]
  (if (and (= "procedure" (get-in schema [:defs :main :type]))
           (= "application/json" (get-in schema [:defs :main :input :encoding])))
    (-> (assoc payload :headers headers)
        (assoc-in [:headers :content-type] "application/json"))
    headers))

(defn- construct-query-params [schema params payload]
  (if (= "query" (get-in schema [:defs :main :type]))
    (let [valid-schema (schema->mallin schema)]
      (if (validate valid-schema params)
        (assoc payload :query-params params)
        (throw (Exception. "Invalid query param found: "
                           (:errors (explain valid-schema params))))))
    payload))

(defn- construct-body [schema body payload]
  (if (= "procedure" (get-in schema [:defs :main :type]))
    (let [valid-schema (schema->mallin schema)]
      (if (validate valid-schema body)
        (assoc payload :body (generate-string body))
        (throw (Exception. "Invalid property in body found: "
                           (:errors (explain valid-schema body))))))
    payload))

(defn process-request
  [service nsid {:keys [headers params body]
                 :or {headers {}
                      params {}
                      body {}}}]
  (if-let [schema (get lex/schemas nsid)]
    (let [uri    (str service "/xrpc/" nsid)
          method (http-method schema)
          res    (method uri (->> {}
                                  (construct-headers schema headers)
                                  (construct-query-params schema params)
                                  (construct-body schema body)))]
      {:data (parse-string (:body res) true)
       :headers (:headers res)})
    (throw (Exception. "No schema found for that method/nsid"))))
