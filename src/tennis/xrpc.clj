(ns tennis.xrpc
  (:require [clj-http.client :as client]
            [cheshire.core :refer [parse-string generate-string]]
            [tennis.lexicon.core :as lex]
            [tennis.lexicon.validation :refer [validate explain]]))

(defn- http-method [lexicon]
  (if (= :query (lex/type-def lexicon))
    client/get
    client/post))

(defn- construct-headers [lexicon headers payload]
  (if (and (= :procedure (lex/type-def lexicon))
           (= "application/json" (lex/encoding-def lexicon :procedure-input)))
    (-> (assoc payload :headers headers)
        (assoc-in [:headers :content-type] "application/json"))
    headers))

(defn- construct-query-params [lexicon params payload]
  (if (= :query (lex/type-def lexicon))
    (if (validate :query-parameters
                  lexicon
                  params)
      (assoc payload :query-params params)
      (throw (Exception. (str "Invalid query param found: "
                              (explain :query-parameters
                                       lexicon
                                       params)))))
    payload))

(defn- construct-body [lexicon body payload]
  (if (= :procedure (lex/type-def lexicon))
    (if (validate :procedure-input
                  lexicon
                  body)
      (assoc payload :body (generate-string body))
      (throw (Exception. (str "Invalid property in body found: "
                              (explain :procedure-input
                                       lexicon
                                       body)))))
    payload))

(defn process-request
  [service nsid {:keys [headers params body]
                 :or {headers {}
                      params {}
                      body {}}}]
  (if-let [lexicon (get lex/lexicons nsid)]
    (let [uri    (str service "/xrpc/" nsid)
          method (http-method lexicon)
          res    (method uri (->> {}
                                  (construct-headers lexicon headers)
                                  (construct-query-params lexicon params)
                                  (construct-body lexicon body)))]
      {:data (parse-string (:body res) true)
       :headers (:headers res)})
    (throw (Exception. "No lexicon found for that method/nsid"))))
