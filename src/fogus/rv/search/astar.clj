(ns fogus.rv.search.astar
  "A* search implementation."
  (:require [fogus.rv.util :as util]))

(defprotocol GraphSearch
  (cost-of [_ node])
  (estimate-cost [_ node goal])
  (neighbors-of [_ node])
  (report-route [_ node new-route])
  (route-of [_ node]))

(defn- path-cost [node-cost cheapest-nbr]
  (+ node-cost (or cheapest-nbr 0)))

(defn- total-cost [graph newcost node goal]
  (+ newcost (estimate-cost graph node goal)))

(defn astar
  "Implements a lazy A* graph traversal algorithm."
  [graph start-node goal-node]
  (loop [steps 0
         graph graph
         work-queue (sorted-set [0 start-node])]
    (if (empty? work-queue)
      (with-meta (route-of graph goal-node) {:steps steps})
      (let [[_ node :as work-item] (first work-queue)
            rest-work-queue (disj work-queue work-item)
            neighbors (neighbors-of graph node)
            cheapest-nbr (util/f-by min-key :cost (keep #(route-of graph %) neighbors))
            newcost (path-cost (cost-of graph node) (:cost cheapest-nbr))
            oldcost (:cost (route-of graph node))]
        (if (= node goal-node)
          (recur (inc steps)
                 (report-route graph node
                               {:cost newcost 
                                :path (conj (:path cheapest-nbr []) node)})
                 rest-work-queue)
          (if (and oldcost (>= newcost oldcost))
            (recur (inc steps) graph rest-work-queue)
            (recur (inc steps)
                   (report-route graph node
                                 {:cost newcost 
                                  :path (conj (:path cheapest-nbr []) node)})
                   (into rest-work-queue
                         (map #(vector (total-cost graph newcost % goal-node) %) neighbors)))))))))



