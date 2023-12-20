(ns tennis.xrpc
  (:require [clj-http.client :as client]
            [cheshire.core :refer [parse-string generate-string]]
            [tennis.lexicon :as lex]))

(defn process-request
  [service nsid {:keys [headers params body]
                 :or {headers {}
                      params {}
                      body {}}}]
  (if-let [schema (get lex/schemas nsid)]
    (let [type (get-in schema [:defs :main :type])
          url  (str service "/xrpc/" nsid)
          res  (if (= "query" type)
                 (client/get url {:headers headers
                                  :query-params params})
                 (client/post url {:content-type :json
                                   :headers headers
                                   :query-params params
                                   :body (generate-string body)}))]
      {:data (parse-string (:body res) true)
       :headers (:headers res)})
    (throw (Exception. "No schema found for that method/nsid"))))
