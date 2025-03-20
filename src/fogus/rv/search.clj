(ns fogus.rv.search
  "Common search-related functions and protocols.")

(defprotocol GraphSearch
  "Functions related to graph-search algorithms."
  (cost-of [_ node] "Returns the cost to visit `node`.")
  (neighbors-of [_ node] "Returns a seq of neighbors of the given `node`.")
  (add-route [_ node new-route]
    "Adds a route to a `node` as a seq of nodes. Implementors of this function
    should return an instance of the object implementing this protocol.")
  (route-of [_ node] "Given a `node`, returns the route associated with it."))

(defprotocol HeuristicSearch
  "Function(s) related to heuristic-guided search."
  (estimate-cost [_ node goal]
    "Returns an estimated cost of the route from `node` to `goal`."))
