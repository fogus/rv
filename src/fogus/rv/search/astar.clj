(ns fogus.rv.search.astar
  "A* search implementation."
  (:require [fogus.rv.search :as search]
            [fogus.rv.util :as util]))

(defn- path-cost
  "The g(n) function calculating the cost of the path from the start
  to the current node."
  [node-cost cheapest-nbr]
  (+ node-cost (or cheapest-nbr 0)))

(defn- total-cost
  "The f(n) function built from g(n) + h(n) or
  total_cost = current_path_cost + estimated_remaining_cost"
  [graph newcost node goal]
  (+ newcost (search/estimate-cost graph node goal)))

(defn astar
  "Implements a lazy A* best-first graph traversal algorithm. Takes a
  `graph` object implementing both of the `fogus.rv.search.GraphSearch`
  and `fogus.rv.search.HeuristicSearch` protocols and a `start-node`
  and `goal-node` describing the bounds of the search. Returns of map
  with keys `:path` mapped to a sequence of nodes from `start-node` to
  `goal-node` and `:cost` describing the cost of the path. This search
  guarantees to return the lowest cost path as long as one exists.
  In the event that there is no path to the `goal-node` the current result
  is undefined."
  [graph start-node goal-node]
  (loop [steps 0
         graph graph
         work-queue (sorted-set [0 start-node])]
    (if (empty? work-queue)
      (with-meta (search/route-of graph goal-node) {:steps steps})
      (let [[_ node :as work-item] (first work-queue)
            rest-work-queue (disj work-queue work-item)
            neighbors (search/neighbors-of graph node)
            cheapest-nbr (util/f-by min-key :cost (keep #(search/route-of graph %) neighbors))
            newcost (path-cost (search/cost-of graph node) (:cost cheapest-nbr))
            oldcost (:cost (search/route-of graph node))]
        (if (= node goal-node)
          (recur (inc steps)
                 (search/add-route graph node
                                   {:cost newcost
                                    :path (conj (:path cheapest-nbr []) node)})
                 rest-work-queue)
          (if (and oldcost (>= newcost oldcost))
            (recur (inc steps) graph rest-work-queue)
            (recur (inc steps)
                   (search/add-route graph node
                                     {:cost newcost
                                      :path (conj (:path cheapest-nbr []) node)})
                   (into rest-work-queue
                         (map #(vector (total-cost graph newcost % goal-node) %) neighbors)))))))))



