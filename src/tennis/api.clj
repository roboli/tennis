(ns tennis.api
  (:require [tennis.xrpc :as xrpc]))

(defn- session [opts]
  "Create a session to make authenticated requests to a PDS service."
  (fn [method]
    (condp = method
      :post
      (fn [text]
        (xrpc/process-request (get-in opts [:agent :service])
                              "com.atproto.repo.createRecord"
                              {:headers {:authorization (str "Bearer " (:access-jwt opts))}
                               :body {:repo (:did opts)
                                      :collection "app.bsky.feed.post"
                                      :record {:text text
                                               :createdAt (java.util.Date.)
                                               :$type "app.bsky.feed.post"}}})))))

(defn post [session text]
  "Create a post using a session."
  ((session :post) text))

(defn agent [opts]
  "Create an agent to make unauthenticated requests to a PDS service."
  (fn [method]
    (condp = method
      :create-session
      (fn [handle password]
        (let [response (xrpc/process-request (:service opts)
                                             "com.atproto.server.createSession"
                                             {:body {:identifier handle
                                                     :password password}})
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
        (xrpc/process-request (:service opts)
                              "app.bsky.feed.getPostThread"
                              {:params {:uri uri}}))

      :resolve-handle
      (fn [handle]
        (xrpc/process-request (:service opts)
                              "com.atproto.identity.resolveHandle"
                              {:params {:handle handle}})))))

(defn create-session [agent handle password]
  "Creates a session for a service."
  ((agent :create-session) handle password))

(defn resolve-handle [agent handle]
  "Resolves handle in service."
  ((agent :resolve-handle) handle))

(defn get-post-thread [agent uri]
  "Gets thread from service."
  ((agent :get-post-thread) uri))
