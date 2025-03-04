(ns rv.constraint-test
  (:require [clojure.test :refer :all]
            [fogus.rv.core :as core]
            [fogus.rv.constraints :as c]))

(deftest solve-tests1
  (let [?x (core/->LVar 'x [0 1])
        ?y (core/->LVar 'y [0 1])
        ?z (core/->LVar 'z [0 1])
        c1 {:variables [?x ?y ?z]
            :formula   `(= (+ ~?x ~?y) ~?z)}]
    (is (= [(c/map->cpair {:domain ?x :value 0})
            (c/map->cpair {:domain ?y :value 0})
            (c/map->cpair {:domain ?z :value 0})]
           (c/find-sat c1)))))

(deftest solve-tests2
  (let [?x (core/->LVar 'x [0 1])
        ?y (core/->LVar 'y [1 2])
        ?z (core/->LVar 'z [2 3])
        c1 {:variables [?x ?y ?z]
            :formula   `(= (+ ~?x ~?y) ~?z)}]
    (is (= [(c/map->cpair {:domain ?x :value 1})
            (c/map->cpair {:domain ?y :value 1})
            (c/map->cpair {:domain ?z :value 2})]
           (c/find-sat c1)))))

(deftest solve-tests3
  (let [?x (core/->LVar 'x [1 1])
        ?y (core/->LVar 'y [2 2])
        ?z (core/->LVar 'z [3 3])
        c1 {:variables [?x ?y ?z]
            :formula   `(= (+ ~?x ~?y) ~?z)}]
    (is (= [(c/map->cpair {:domain ?x :value 1})
            (c/map->cpair {:domain ?y :value 2})
            (c/map->cpair {:domain ?z :value 3})]
           (c/find-sat c1)))))
