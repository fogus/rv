(ns fogus.rv.search
  "")

(defprotocol GraphSearch
  ""
  (cost-of [_ node] "")
  (neighbors-of [_ node] "")
  (report-route [_ node new-route] "")
  (route-of [_ node] ""))

(defprotocol HeuristicSearch
  ""
  (estimate-cost [_ node goal] ""))
