(ns tennis.xrpc
  (:require [clj-http.client :as client]
            [cheshire.core :refer [parse-string generate-string]]
            [tennis.lexicon.schemas :as lexicon]
            [tennis.lexicon.validation :as lex]))

(defn- http-method [schema]
  (if (= :query (lex/schema-type schema))
    client/get
    client/post))

(defn- construct-headers [schema headers payload]
  (if (and (= :procedure (lex/schema-type schema))
           (= "application/json" (lex/schema-encoding :procedure-input schema)))
    (-> (assoc payload :headers headers)
        (assoc-in [:headers :content-type] "application/json"))
    headers))

(defn- construct-query-params [schema params payload]
  (if (= :query (lex/schema-type schema))
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
  (if (= :procedure (lex/schema-type schema))
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
  (if-let [schema (get lexicon/schemas nsid)]
    (let [uri    (str service "/xrpc/" nsid)
          method (http-method schema)
          res    (method uri (->> {}
                                  (construct-headers schema headers)
                                  (construct-query-params schema params)
                                  (construct-body schema body)))]
      {:data (parse-string (:body res) true)
       :headers (:headers res)})
    (throw (Exception. "No schema found for that method/nsid"))))
