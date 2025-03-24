(ns rv.search.astar-test
  (:require [clojure.test :refer :all]
            [fogus.rv.search :as search]
            [fogus.rv.search.graph :as graph]))

(def ^:private ^:const ORTHO-DIRS [[-1 0] [1 0] [0 -1] [0 1]])

(defn- neighbors
  [deltas size yx]
  (filter (fn [new-yx] (every? #(< -1 % size) new-yx))
          (map #(vec (map + yx %)) deltas)))

(defn- init-routes [yxcosts]
  (let [size (count yxcosts)]
    (vec (repeat size (vec (repeat size nil))))))

(deftype SimpleAsciiGraph [dirs step-est yxcosts routes]
  search/GraphSearch
  (neighbors-of [_ yx]
    (neighbors dirs (count yxcosts) yx))
  (add-route [_ node new-path]
    (SimpleAsciiGraph. dirs step-est yxcosts (assoc-in routes node new-path)))
  (route-of [_ node]
    (get-in routes node))
  (cost-of [_ yx]
    (get-in yxcosts yx))
  search/HeuristicSearch
  (estimate-cost [_ yx _]
    (let [[y x] yx
          sz (count yxcosts)]
      (* step-est (- (+ sz sz) y x 2)))))

(deftest test-astar
  (let [z-world [[  1   1   1   1   1]
                 [999 999 999 999   1]
                 [  1   1   1   1   1]
                 [  1 999 999 999 999]
                 [  1   1   1   1   1]]
        z-graph (SimpleAsciiGraph. ORTHO-DIRS 900 z-world (init-routes z-world))
        res (graph/astar z-graph [0 0] [4 4])]
    (is (= 17 (:cost res)))
    (is (= [[0 0] [0 1] [0 2] [0 3] [0 4] [1 4] [2 4] [2 3] [2 2] [2 1] [2 0] [3 0] [4 0] [4 1] [4 2] [4 3] [4 4]]
           (:path res))))

  (let [down-path [[1 1 1 2   1]
                   [1 1 1 999 1]
                   [1 1 1 999 1]
                   [1 1 1 999 1]
                   [1 1 1 1   1]]
        down-graph (SimpleAsciiGraph. ORTHO-DIRS 900 down-path (init-routes down-path))
        res (graph/astar down-graph [0 0] [4 4])]
    (is (= 9 (:cost res)))
    (is (= [[0 0] [0 1] [0 2] [1 2] [2 2] [3 2] [4 2] [4 3] [4 4]]
           (:path res))))

  (let [up-path [[1 1 1 2   1]
                 [1 1 1 999 1]
                 [1 1 1 999 1]
                 [1 1 1 999 1]
                 [1 1 1 3   1]]
        up-graph (SimpleAsciiGraph. ORTHO-DIRS 900 up-path (init-routes up-path))
        res (graph/astar up-graph [0 0] [4 4])]
    (is (= 10 (:cost res)))
    (is (= [[0 0] [0 1] [0 2] [0 3] [0 4] [1 4] [2 4] [3 4] [4 4]]
           (:path res))))

  (let [l-path [[1 2 1 2   1]
                [1 2 1 999 1]
                [1 2 1 999 1]
                [1 2 2 999 1]
                [1 1 1 2   1]]
        l-graph (SimpleAsciiGraph. ORTHO-DIRS 900 l-path (init-routes l-path))
        res (graph/astar l-graph [0 0] [4 4])]
    (is (= 10 (:cost res)))
    (is (= [[0 0] [1 0] [2 0] [3 0] [4 0] [4 1] [4 2] [4 3] [4 4]]
           (:path res))))

  (let [short-path [[1 2 1 2   1]
                    [1 0 1 999 1]
                    [1 1 1 999 1]
                    [1 1 1 999 1]
                    [1 1 1 1   1]]
        short-graph (SimpleAsciiGraph. ORTHO-DIRS 900 short-path (init-routes short-path))
        res (graph/astar short-graph [0 0] [1 1])]
    (is (= 2 (:cost res)))
    (is (= [[0 0] [1 0] [1 1]]
           (:path res)))))

