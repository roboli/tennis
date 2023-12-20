(ns tennis.api
  (:require [tennis.xrpc :as xrpc]))

(defn- session [opts]
  (fn [method]
    (condp = method
      :post
      (fn [text]
        (xrpc/process-request (get-in opts [:agent :service])
                              "com.atproto.repo.createRecord"
                              {:authorization (str "Bearer " (:access-jwt opts))}
                              nil
                              {"repo" (:did opts)
                               "collection" "app.bsky.feed.post"
                               "record" {"text" text
                                         "createdAt" (java.util.Date.)
                                         "$type" "app.bsky.feed.post"}})))))

(defn post [ssn text]
  ((ssn :post) text))

(defn agent [opts]
  (fn [method]
    (condp = method
      :create-session
      (fn [handle password]
        (let [response (xrpc/process-request (:service opts)
                                             "com.atproto.server.createSession"
                                             nil
                                             {"identifier" handle
                                              "password" password})
              data     (:data response)]
          (session {:access-jwt (:accessJwt data)
                    :refresh-jwt (:accessJwt data)
                    :handle (:handle data)
                    :did (:did data)
                    :email (:email data)
                    :email-confirmed (:emailConfirmed data)
                    :agent opts})))

      :get-post-thread
      (fn [uri]
        (xrpc/process-request (:service opts) "app.bsky.feed.getPostThread" {"uri" uri}))

      :resolve-handle
      (fn [handle]
        (xrpc/process-request (:service opts) "com.atproto.identity.resolveHandle" {"handle" handle})))))

(defn create-session [svc handle password]
  ((svc :create-session) handle password))

(defn resolve-handle [svc handle]
  ((svc :resolve-handle) handle))

(defn get-post-thread [svc uri]
  ((svc :get-post-thread) uri))
