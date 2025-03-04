;   Copyright (c) Fogus. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

;; WIP

(ns fogus.rv.constraints
  "A simple constraints solver."
  (:require [fogus.rv.core :as core]
            [fogus.rv.util :as util]
            [clojure.core.unify :as unify]
            [fogus.evalive :as live]))

(defrecord cpair      [domain value])

(defn get-all-pairs [c]
  (let [vars     (:variables c)
        ;;varnames (map :domain vars)
        tuples   (util/cart (map :range vars))]
    (map #(map ->cpair
               vars ;;varnames
               %) tuples)))

(def ^:private subst (clojure.core.unify/make-occurs-subst-fn core/lv?))

(defn test-pair [f p]
  (cond (= p []) (live/evil {} f)
        :else (let [current-pair    (first p)
                    remaining-pairs (rest p)]
                (test-pair (subst f {(:domain current-pair) (:value current-pair)})
                           remaining-pairs))))

(defn- descend [f ps]
  (cond (nil? ps) []
        (test-pair f (first ps)) (first ps)
        :else (recur f (rest ps))))

(defn find-sat [c]
  (let [formula (:formula c)
        pairs   (get-all-pairs c)]
    (descend formula pairs)))

(comment

  (let [?x (core/->LVar 'x [0 1])
        ?y (core/->LVar 'y [0 1])
        ?z (core/->LVar 'z [0 1])
        c1 {:variables [?x ?y ?z]
            :formula `(= (+ ~?x ~?y) ~?z)}]
    (find-sat c1))

)
