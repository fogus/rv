(ns fogus.rv.search.astar
  "A* search implementation."
  (:require [fogus.rv.util :as util]))

(defprotocol GraphSearch
  (route-of [_ node])
  (report-route [_ node new-route])
  (best-route [_])
  (neighbors-of [_ node])
  (step-estimate [_])
  (cost [_ node]))

(defn- estimate-cost [step-cost-est sz y x]
  (* step-cost-est 
     (- (+ sz sz) y x 2)))

(defn- path-cost [node-cost cheapest-nbr]
  (+ node-cost
     (or cheapest-nbr 0)))

(defn- total-cost [newcost step-cost-est size [y x]]
  (+ newcost 
     (estimate-cost step-cost-est size y x)))

(defn astar
  "Implements a lazy A* graph traversal algorithm."
  [graph start-node goal-node]
  (let [size (count graph)
        step-est (step-estimate graph)]
    (loop [steps 0
           graph graph
           work-todo (sorted-set [0 start-node])]
      (if (empty? work-todo)                             ;; Check done
        (assoc (best-route graph) :steps steps)        ;; Grab the first route
        (let [[_ node :as work-item] (first work-todo)     ;; Get next work item
              rest-work-todo (disj work-todo work-item)  ;; Clear from todo
              neighbors (neighbors-of graph node)            ;; Get neighbors
              cheapest-nbr (util/f-by min-key :cost            ;; Calc least-cost
                                      (keep #(route-of graph %) neighbors))
              newcost (path-cost (cost graph node) ;; Calc path so-far
                                 (:cost cheapest-nbr))
              oldcost (:cost (route-of graph node))]
          (if (= node goal-node)
            (recur (inc steps)
                   (report-route graph node              ;; report new path to the goal
                                 {:cost newcost 
                                  :path (conj (:path cheapest-nbr []) node)})
                   rest-work-todo)
            (if (and oldcost (>= newcost oldcost))         ;; Check if new is worse
              (recur (inc steps) graph rest-work-todo)
              (recur (inc steps)
                     (report-route graph node              ;; report new path to the cheaper node
                                   {:cost newcost 
                                    :path (conj (:path cheapest-nbr []) node)})
                     (into rest-work-todo                  ;; Add the estimated path to the todo and recur
                           (map #(vector (total-cost newcost step-est size %) %) neighbors))))))))))



