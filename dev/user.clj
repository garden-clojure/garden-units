(ns ^:no-doc user
  (:require
   [cljs.repl]
   [cljs.repl.node]
   [cemerick.piggieback]))

(defn node-repl []
  (cemerick.piggieback/cljs-repl (cljs.repl.node/repl-env)))
