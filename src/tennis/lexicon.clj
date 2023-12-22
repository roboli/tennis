(ns tennis.lexicon)

(def app-bsky-feed-getpostthread
  {:lexicon 1,
   :id "app.bsky.feed.getPostThread",
   :defs {:main {:type "query",
                 :description "Get posts in a thread.",
                 :parameters {:type "params",
                              :required [:uri],
                              :properties {:uri {:type "string", :format "at-uri"},
                                           :depth {:type "integer", :default 6, :minimum 0, :maximum 1000},
                                           :parentHeight
                                           {:type "integer", :default 80, :minimum 0, :maximum 1000}}},
                 :output {:encoding "application/json",
                          :schema {:type "object",
                                   :required [:thread],
                                   :properties
                                   {:thread {:type "union",
                                             :refs ["lex:app.bsky.feed.defs#threadViewPost"
                                                    "lex:app.bsky.feed.defs#notFoundPost"
                                                    "lex:app.bsky.feed.defs#blockedPost"]}}}},
                 :errors [{:name "NotFound"}]}}})

(def com-atproto-identity-resolvehandle
  {:lexicon 1,
   :id "com.atproto.identity.resolveHandle",
   :defs {:main {:type "query",
                 :description "Provides the DID of a repo.",
                 :parameters {:type "params",
                              :required [:handle],
                              :properties {:handle {:type "string",
                                                    :format "handle",
                                                    :description "The handle to resolve."}}},
                 :output {:encoding "application/json",
                          :schema {:type "object",
                                   :required [:did],
                                   :properties {:did {:type "string", :format "did"}}}}}}})

(def com-atproto-repo-createrecord
  {:lexicon 1,
   :id "com.atproto.repo.createRecord",
   :defs {:main {:type "procedure",
                 :description "Create a new record.",
                 :input {:encoding "application/json",
                         :schema {:type "object",
                                  :required [:repo :collection :record],
                                  :properties {:repo {:type "string",
                                                      :format "at-identifier",
                                                      :description "The handle or DID of the repo."},
                                               :collection {:type "string",
                                                            :format "nsid",
                                                            :description "The NSID of the record collection."},
                                               :rkey {:type "string",
                                                      :description "The key of the record.",
                                                      :maxLength 15},
                                               :validate {:type "boolean",
                                                          :default true,
                                                          :description "Flag for validating the record."},
                                               :record {:type "unknown", :description "The record to create."},
                                               :swapCommit {:type "string",
                                                            :format "cid",
                                                            :description
                                                            "Compare and swap with the previous commit by CID."}}}},
                 :output {:encoding "application/json",
                          :schema {:type "object",
                                   :required [:uri :cid],
                                   :properties {:uri {:type "string", :format "at-uri"},
                                                :cid {:type "string", :format "cid"}}}},
                 :errors [{:name "InvalidSwap"}]}}})

(def com-atproto-server-createsession
  {:lexicon 1,
   :id "com.atproto.server.createSession",
   :defs {:main {:type "procedure",
                 :description "Create an authentication session.",
                 :input {:encoding "application/json",
                         :schema {:type "object",
                                  :required [:identifier :password],
                                  :properties {:identifier {:type "string",
                                                            :description
                                                            "Handle or other identifier supported by the server for the authenticating user."},
                                               :password {:type "string"}}}},
                 :output {:encoding "application/json",
                          :schema {:type "object",
                                   :required [:accessJwt :refreshJwt :handle :did],
                                   :properties {:accessJwt {:type "string"},
                                                :refreshJwt {:type "string"},
                                                :handle {:type "string", :format "handle"},
                                                :did {:type "string", :format "did"},
                                                :didDoc {:type "unknown"},
                                                :email {:type "string"},
                                                :emailConfirmed {:type "boolean"}}}},
                 :errors [{:name "AccountTakedown"}]}}})

(def schemas {"app.bsky.feed.getPostThread" app-bsky-feed-getpostthread
              "com.atproto.identity.resolveHandle" com-atproto-identity-resolvehandle
              "com.atproto.repo.createRecord" com-atproto-repo-createrecord
              "com.atproto.server.createSession" com-atproto-server-createsession})
