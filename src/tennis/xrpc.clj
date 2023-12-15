(ns tennis.xrpc
  (:require [clj-http.client :as client]
            [tennis.lexicon :as lex]))

(defn process-request
  ([host nsid params] (process-request host nsid params nil))
  ([host nsid params body]
   (if-let [schema (get lex/schemas nsid)]
     (let [type (get-in schema [:defs :main :type])
           url  (str "https://" host "/xrpc/" nsid)]
       (if (= "query" type)
         (client/get url {:query-params params})
         (client/post url {:query-params params}
                      body))))))
