(defproject tennis "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [clj-http "3.12.3"]
                 [cheshire "5.12.0"]
                 [metosin/malli "0.13.0"]]
  :aliases {"init" ["run" "-m" "tennis.lexicon.init"]}
  :repl-options {:init-ns tennis.core})
