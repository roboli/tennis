(ns tennis.xrpc
  (:require [clj-http.client :as client]
            [cheshire.core :refer [parse-string generate-string]]
            [tennis.lexicon.core :as lex]
            [tennis.lexicon.validation :refer [validate explain]]))

(defn- http-method [lexicon-def]
  (if (= :query (lex/type-def lexicon-def))
    client/get
    client/post))

(defn- construct-headers [lexicon-def headers payload]
  (if (and (= :procedure (lex/type-def lexicon-def))
           (= "application/json" (lex/encoding-def lexicon-def :input)))
    (-> (assoc payload :headers headers)
        (assoc-in [:headers :content-type] "application/json"))
    headers))

(defn- construct-query-params [lexicon-def params payload]
  (if (= :query (lex/type-def lexicon-def))
    (if (lex/assert-valid-xrpc {:type :query-input
                                :def lexicon-def
                                :data params})
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
  (if-let [lexicon-def (lex/find-def nsid)]
    (let [uri    (str service "/xrpc/" nsid)
          method (http-method lexicon-def)
          res    (method uri (->> {}
                                  (construct-headers lexicon-def headers)
                                  (construct-query-params lexicon-def params)
                                  (construct-body lexicon-def body)))]
      {:data (parse-string (:body res) true)
       :headers (:headers res)})
    (throw (Exception. "No lexicon found for that method/nsid"))))
