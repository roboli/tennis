(ns tennis.xrpc
  (:require [clj-http.client :as client]
            [cheshire.core :refer [parse-string]]
            [tennis.lexicon :as lex]))

(defn process-request
  ([host nsid params] (process-request host nsid params nil))
  ([host nsid params body]
   (if-let [schema (get lex/schemas nsid)]
     (let [type (get-in schema [:defs :main :type])
           url  (str "https://" host "/xrpc/" nsid)
           res  (if (= "query" type)
                  (client/get url {:query-params params})
                  (client/post url {:query-params params}
                               body))]
       {:data (parse-string (:body res) true)
        :headers (:headers res)}))))
