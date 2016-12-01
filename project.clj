(defproject garden/garden-units "1.0.0-RC2"
  :description "Utilities for working with units"
  :url "http://github.com/garden-clojure/garden-units"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies
  [[org.clojure/clojure "1.9.0-alpha13" :scope "provided"]
   [org.clojure/clojurescript "1.9.293" :scope "provided"]]

  :source-paths
  ["src"]

  :profiles
  {:dev
   {:dependencies
    [[com.cemerick/piggieback "0.2.1"]
     [org.clojure/test.check "0.9.0"]]

    :source-paths
    ["src" "dev"]

    :plugins
    [[com.jakemccrary/lein-test-refresh "0.17.0"]
     [lein-cljsbuild "1.1.4"]
     [lein-codox "0.10.2"]
     [lein-doo "0.1.7"]]

    :repl-options
    {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}}

  :aliases
  {"cljs-test"
   ["doo" "node" "test" "once"]}

  :cljsbuild
  {:builds [{:id "test"
             :source-paths ["src" "test"]
             :compiler {:main garden.test-runner
                        :optimizations :none
                        :output-dir "target/js/out"
                        :output-to "target/js/garden-units.test.js"
                        :source-map true
                        :target :nodejs}}]})
