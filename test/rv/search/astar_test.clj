(ns rv.search.astar-test
  (:require [clojure.test :refer :all]
            [fogus.rv.search.astar :as search]))

(defn- neighbors
  ([size yx]
   (neighbors [[-1 0] [1 0] [0 -1] [0 1]] size yx))
  ([deltas size yx]
   (filter (fn [new-yx] (every? #(< -1 % size)
                                new-yx))
           (map #(vec (map + yx %)) deltas))))

(defn- init-routes [yxcosts]
  (let [size (count yxcosts)]
    (vec (repeat size (vec (repeat size nil))))))

(deftype SimpleAsciiGraph [start end step-est yxcosts routes]
  search/GraphSearch
  (start-node [_]
    start)
  (goal-node [_]
    end)
  (neighbors-of [_ yx]
    (neighbors (count yxcosts) yx))
  (report-route [_ node new-path]
    (SimpleAsciiGraph. start end step-est yxcosts (assoc-in routes node new-path)))
  (route-of [_ node]
    (get-in routes node))
  (best-route [_]
    (peek (peek routes)))
  (step-estimate [_]
    step-est)
  (cost [_ yx]
    (get-in yxcosts yx))

  clojure.lang.Counted
  (count [_]
    (count yxcosts)))

(deftest test-astar
  (let [z-world [[  1   1   1   1   1]
                 [999 999 999 999   1]
                 [  1   1   1   1   1]
                 [  1 999 999 999 999]
                 [  1   1   1   1   1]]
        res (search/astar (SimpleAsciiGraph. [0 0] [4 4]
                                             900
                                             z-world
                                             (init-routes z-world)))]
    (is (= 17 (:cost res))))

  (let [shrub-world [[1 1 1 2   1]
                     [1 1 1 999 1]
                     [1 1 1 999 1]
                     [1 1 1 999 1]
                     [1 1 1 1   1]]
        res (search/astar (SimpleAsciiGraph. [0 0] [4 4]
                                             900
                                             shrub-world
                                             (init-routes shrub-world)))]
    (is (= 9 (:cost res)))))

