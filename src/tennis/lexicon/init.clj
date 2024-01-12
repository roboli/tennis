(ns tennis.lexicon.init
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clj-http.client :as client]
            [cheshire.core :refer [parse-string generate-string]])
  (:import [java.io File]))

;; Initiating (retreiving and converting to edn) lexicons
;; See alias "init" in project.clj

(defn zip-url [repo package tag]
  (str "https://github.com/bluesky-social/"
       repo
       "/archive/refs/tags/@"
       repo
       "/"
       package
       "@"
       tag
       ".zip"))

;; Thanks to https://stackoverflow.com/a/51946836/2202143
(defn download-unzip
  "Download lexicons from bsky/atproto repo"
  [url]
  (-> (client/get url {:as :stream})
      (:body)
      (java.util.zip.ZipInputStream.)))

(defn save-lexicons
  "Save lexicons in resources/lexicons dir"
  [stream repo package tag]
  (let [package-name (str repo "--" repo "-" package "-" tag)
        lexicons-dir (str package-name "/lexicons/")]
    (loop [entry (.getNextEntry stream)]
      (if entry
        (let [entry-name (.getName entry)]
          (if (string/includes? entry-name lexicons-dir)
            (let [savePath (str "resources"
                                File/separatorChar
                                (string/replace entry-name
                                                (re-pattern (str
                                                             (java.util.regex.Pattern/quote package-name)
                                                             "/"))
                                                ""))
                  saveFile (File. savePath)]
              (if (.isDirectory entry)
                (if-not (.exists saveFile)
                  (.mkdirs saveFile))
                (let [parentDir (File. (.substring savePath 0 (.lastIndexOf savePath (int File/separatorChar))))]
                  (if-not (.exists parentDir) (.mkdirs parentDir))
                  (clojure.java.io/copy stream saveFile)))))
          (recur (.getNextEntry stream)))))))

(defn -main []
  (prn "Fetching...")
  (let [repo  "atproto"
        packg "bsky"
        tag   "0.0.22"]
    (with-open [stream (download-unzip (zip-url repo packg tag))]
      (prn "Saving...")
      (save-lexicons stream repo packg tag)))
  (prn "Done!"))


