(defproject clj-code-poster "0.1.0-SNAPSHOT"
  :description "Code Poster Generator in Clojure"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [net.mikera/imagez "0.12.0"]
                 [dali "0.7.3"]
                 [com.taoensso/timbre "4.8.0"]
                 [org.clojure/tools.cli "0.3.5"]]
  :main ^:skip-aot clj-code-poster.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
