(ns tennis.xrpc
  (:require [clj-http.client :as client]
            [cheshire.core :refer [parse-string generate-string]]
            [tennis.lexicon :as lex]))

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
    (if (lex/validate :query-parameters
                      schema
                      params)
      (assoc payload :query-params params)
      (throw (Exception. (str "Invalid query param found: "
                              (lex/explain :query-parameters
                                           schema
                                           params)))))
    payload))

(defn- construct-body [schema body payload]
  (if (= "procedure" (get-in schema [:defs :main :type]))
    (if (lex/validate :procedure-input
                      schema
                      body)
      (assoc payload :body (generate-string body))
      (throw (Exception. (str "Invalid property in body found: "
                              (lex/explain :procedure-input
                                           schema
                                           body)))))
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
