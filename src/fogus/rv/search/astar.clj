(ns fogus.rv.search.astar
  "A* search implementation."
  (:require [fogus.rv.util :as util]))

(defprotocol GraphSearch
  (start-node [_])
  (goal-node [_])
  (neighbors-of [_ node])
  (step-estimate [_])
  (cost [_ node]))

(defn- estimate-cost [step-cost-est sz y x]
  (* step-cost-est 
     (- (+ sz sz) y x 2)))

(defn- path-cost [node-cost cheapest-nbr]
  (+ node-cost
     (or (:cost cheapest-nbr) 0)))

(defn- total-cost [newcost step-cost-est size y x]
  (+ newcost 
     (estimate-cost step-cost-est size y x)))

(defn astar
  "Implements a lazy A* graph traversal algorithm."
  [graph]
  (let [size (count graph)
        start (start-node graph)
        step-est (step-estimate graph)]
    (loop [steps 0
           routes (vec (repeat size (vec (repeat size nil))))
           work-todo (sorted-set [0 start])]
      (if (empty? work-todo)                             ;; Check done
        (assoc (peek (peek routes)) :steps steps)        ;; Grab the first route
        (let [[_ node :as work-item] (first work-todo)     ;; Get next work item
              rest-work-todo (disj work-todo work-item)  ;; Clear from todo
              neighbors (neighbors-of graph node)            ;; Get neighbors
              cheapest-nbr (util/f-by min-key :cost            ;; Calc least-cost
                                      (keep #(get-in routes %) 
                                            neighbors))
              newcost (path-cost (cost graph node) ;; Calc path so-far
                                 cheapest-nbr)
              oldcost (:cost (get-in routes node))]
          (if (and oldcost (>= newcost oldcost))         ;; Check if new is worse
            (recur (inc steps) routes rest-work-todo)
            (recur (inc steps)                           ;; Place new path in the routes
                   (assoc-in routes node
                             {:cost newcost 
                              :path (conj (:path cheapest-nbr []) node)})
                   (into rest-work-todo                  ;; Add the estimated path to the todo and recur
                         (map 
                          (fn [w] 
                            (let [[y x] w]
                              [(total-cost newcost step-est size y x) w]))
                          neighbors)))))))))



