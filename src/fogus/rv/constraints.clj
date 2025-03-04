;   Copyright (c) Fogus. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns fogus.rv.constraints
  "A simple constraints solver."
  (:require [fogus.rv.core :as core]
            [fogus.rv.util :as util]
            [clojure.core.unify :as unify]
            [fogus.evalive :as live]))

(defn- pairwise [c]
  (let [vars (:variables c)
        tuples (util/cart (map :range vars))]
    (map #(map vector vars %) tuples)))

(def ^:private subst (clojure.core.unify/make-occurs-subst-fn core/lv?))

(defn- test-pair [f p]
  (cond (= p []) (live/evil {} f)
        :else (let [[domain value] (first p)
                    remaining-pairs (rest p)]
                (test-pair (subst f {domain value})
                           remaining-pairs))))

(defn- descend [f pairs]
  (cond (nil? pairs) []
        (test-pair f (first pairs)) (first pairs)
        :else (recur f (rest pairs))))

(defn satisfy1 [c]
  (let [formula (:formula c)
        pairs   (pairwise c)]
    (into {} (descend formula pairs))))

