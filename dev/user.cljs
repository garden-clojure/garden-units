(ns ^:no-doc user
  (:require
   [weasel.repl :as ws-repl]))

(ws-repl/connect "ws://localhost:9123" :verbose true)
