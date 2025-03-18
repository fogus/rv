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

(deftype SimpleAsciiGraph [start end step-est yxcosts]
  search/GraphSearch
  (start-node [_]
    start)
  (goal-node [_]
    end)
  (neighbors-of [_ yx]
    (neighbors (count yxcosts) yx))
  (step-estimate [_]
    step-est)
  (cost [_ yx]
    (get-in yxcosts yx))

  clojure.lang.Counted
  (count [_]
    (count yxcosts)))

(deftest test-astar
  (let [res (search/astar (SimpleAsciiGraph. [0 0] [4 4]
                                             900
                                             [[  1   1   1   1   1]
                                              [999 999 999 999   1]
                                              [  1   1   1   1   1]
                                              [  1 999 999 999 999]
                                              [  1   1   1   1   1]]))]
    (is (= 17 (:cost res))))

  (let [res (search/astar (SimpleAsciiGraph. [0 0] [4 4]
                                             900
                                             [[1 1 1 2   1]
                                              [1 1 1 999 1]
                                              [1 1 1 999 1]
                                              [1 1 1 999 1]
                                              [1 1 1 1   1]]))]
    (is (= 9 (:cost res)))))

