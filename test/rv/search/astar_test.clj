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

(deftype SimpleAsciiGraph [step-est yxcosts routes]
  search/GraphSearch
  (neighbors-of [_ yx]
    (neighbors (count yxcosts) yx))
  (report-route [_ node new-path]
    (SimpleAsciiGraph. step-est yxcosts (assoc-in routes node new-path)))
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
        res (search/astar (SimpleAsciiGraph. 900
                                             z-world
                                             (init-routes z-world))
                          [0 0] [4 4])]
    (is (= 17 (:cost res)))
    (is (= [[0 0] [0 1] [0 2] [0 3] [0 4] [1 4] [2 4] [2 3] [2 2] [2 1] [2 0] [3 0] [4 0] [4 1] [4 2] [4 3] [4 4]]
           (:path res))))

  (let [down-path [[1 1 1 2   1]
                   [1 1 1 999 1]
                   [1 1 1 999 1]
                   [1 1 1 999 1]
                   [1 1 1 1   1]]
        res (search/astar (SimpleAsciiGraph. 900
                                             down-path
                                             (init-routes down-path))
                          [0 0] [4 4])]
    (is (= 9 (:cost res)))
    (is (= [[0 0] [0 1] [0 2] [1 2] [2 2] [3 2] [4 2] [4 3] [4 4]]
           (:path res))))

  (let [up-path [[1 1 1 2   1]
                 [1 1 1 999 1]
                 [1 1 1 999 1]
                 [1 1 1 999 1]
                 [1 1 1 3   1]]
        res (search/astar (SimpleAsciiGraph. 900
                                             up-path
                                             (init-routes up-path))
                          [0 0] [4 4])]
    (is (= 10 (:cost res)))
    (is (= [[0 0] [0 1] [0 2] [0 3] [0 4] [1 4] [2 4] [3 4] [4 4]]
           (:path res))))

  (let [l-path [[1 2 1 2   1]
                [1 2 1 999 1]
                [1 2 1 999 1]
                [1 2 2 999 1]
                [1 1 1 3   1]]
        res (search/astar (SimpleAsciiGraph. 900
                                             l-path
                                             (init-routes l-path))
                          [0 0] [4 4])]
    (is (= 11 (:cost res)))
    (is (= [[0 0] [1 0] [2 0] [3 0] [4 0] [4 1] [4 2] [4 3] [4 4]]
           (:path res)))))

