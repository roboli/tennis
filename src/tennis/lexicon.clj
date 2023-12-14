(ns tennis.lexicon)

(def app-bsky-feed-getpostthread
  {:lexicon 1,
   :id "app.bsky.feed.getPostThread",
   :defs {:main {:type "query",
                 :description "Get posts in a thread.",
                 :parameters {:type "params",
                              :required ["uri"],
                              :properties {:uri {:type "string", :format "at-uri"},
                                           :depth {:type "integer", :default 6, :minimum 0, :maximum 1000},
                                           :parentHeight
                                           {:type "integer", :default 80, :minimum 0, :maximum 1000}}},
                 :output {:encoding "application/json",
                          :schema {:type "object",
                                   :required ["thread"],
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
                              :required ["handle"],
                              :properties {:handle {:type "string",
                                                    :format "handle",
                                                    :description "The handle to resolve."}}},
                 :output {:encoding "application/json",
                          :schema {:type "object",
                                   :required ["did"],
                                   :properties {:did {:type "string", :format "did"}}}}}}})

(def schemas {"app.bsky.feed.getPostThread" app-bsky-feed-getpostthread
              "com.atproto.identity.resolveHandle" com-atproto-identity-resolvehandle})
