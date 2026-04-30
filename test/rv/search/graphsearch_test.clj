(ns rv.search.graphsearch-test
  (:require [clojure.test :refer :all]
            [fogus.rv.search :as search]))

(defn- apply-jug-action [capacities state [op i j]]
  (case op
    :fill  (assoc state i (nth capacities i))
    :empty (assoc state i 0)
    :pour  (let [amount (min (nth state i)
                             (- (nth capacities j) (nth state j)))]
             (-> state (update i - amount) (update j + amount)))))

(defn- label-jug-action [capacities [op i j]]
  (case op
    :fill  [:fill  (nth capacities i)]
    :empty [:empty (nth capacities i)]
    :pour  [:pour  (nth capacities i) (nth capacities j)]))

(defrecord JugGraph [capacities goal-amount routes]
  search/GraphSearch
  (cost-of [_ _] 1)
  (neighbors-of [_ state]
    (let [n (count capacities)
          actions (concat (for [i (range n)] [:fill i])
                          (for [i (range n)] [:empty i])
                          (for [i (range n) j (range n) :when (not= i j)] [:pour i j]))]
      (keep (fn [action]
              (let [nxt (apply-jug-action capacities state action)]
                (when (not= nxt state)
                  [(label-jug-action capacities action) nxt])))
            actions)))
  (add-route [_ state new-route]
    (JugGraph. capacities goal-amount (assoc routes state new-route)))
  (route-of [_ state]
    (get routes state))
  (goal? [_ state]
    (some #(= % goal-amount) state)))

(defn solve-jugs
  [goal capacities]
  (let [start (vec (repeat (count capacities) 0))
        graph (->JugGraph capacities goal {start []})]
    (if (search/goal? graph start)
      []
      (loop [queue   (conj clojure.lang.PersistentQueue/EMPTY start)
             visited #{start}
             graph   graph]
        (when-not (empty? queue)
          (let [state        (peek queue)
                current-path (search/route-of graph state)
                unvisited    (remove (fn [[_ nxt]] (visited nxt))
                                     (search/neighbors-of graph state))]
            (or (some (fn [[label nxt]]
                        (when (search/goal? graph nxt)
                          (conj current-path label)))
                      unvisited)
                (recur (reduce (fn [q [_ nxt]] (conj q nxt)) (pop queue) unvisited)
                       (into visited (map second unvisited))
                       (reduce (fn [g [label nxt]]
                                 (search/add-route g nxt (conj current-path label)))
                               graph
                               unvisited)))))))))

(deftest jugs-test
  (testing "that the search finds the shortest path"
    (is (= (solve-jugs 4 [5 3])
           [[:fill 5] [:pour 5 3] [:empty 3] [:pour 5 3] [:fill 5] [:pour 5 3]]))))

