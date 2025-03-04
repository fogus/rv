(ns rv.constraint-test
  (:require [clojure.test :refer :all]
            [fogus.rv.core :as core]
            [fogus.rv.constraints :as c]))

(deftest solve-tests1
  (let [c1 (c/->constraint [(core/->LVar '?x [0 1])
                            (core/->LVar '?y [0 1])
                            (core/->LVar '?z [0 1])]
                           '(= (+ ?x ?y) ?z))]
    (is (= [(c/map->cpair '{:domain ?x :value 0})
            (c/map->cpair '{:domain ?y :value 0})
            (c/map->cpair '{:domain ?z :value 0})]
           (c/find-sat c1)))))

(deftest solve-tests2
  (let [c1 (c/->constraint [(core/->LVar '?x [0 1])
                            (core/->LVar '?y [1 2])
                            (core/->LVar '?z [2 3])]
                         '(= (+ ?x ?y) ?z))]
    (is (= [(c/map->cpair '{:domain ?x :value 1})
            (c/map->cpair '{:domain ?y :value 1})
            (c/map->cpair '{:domain ?z :value 2})]
           (c/find-sat c1)))))

(deftest solve-tests3
  (let [c1 (c/->constraint [(core/->LVar '?x [1 1])
                            (core/->LVar '?y [2 2])
                            (core/->LVar '?z [3 3])]
                         '(= (+ ?x ?y) ?z))]
    (is (= [(c/map->cpair '{:domain ?x :value 1})
            (c/map->cpair '{:domain ?y :value 2})
            (c/map->cpair '{:domain ?z :value 3})]
           (c/find-sat c1)))))
