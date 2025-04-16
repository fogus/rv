;   Copyright (c) Fogus. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns fogus.rv.learn.vs
  (:require [fogus.rv.core :as core]
            [fogus.rv.util :as util]))

(def ^:const ?S (core/->IgnoreT))
(def ^:const ?G (core/->AnyT))

(defprotocol S&G
  (-generalize [lhs rhs])
  (-specialize [lhs neg rhs])
  (-wrap [basis coll])
  (-init [basis] [basis domain]))

(defn- specialize-at [g s k]
  (if (= k 0)
    (cons (first s) (rest g))
    (cons (first g) (specialize-at (rest g) (rest s) (- k 1)))))

(defn- specializable? [gen neg-example spec]
  (and (= gen ?G) (not= spec neg-example)))

(defn- generalizable? [l r]
  (cond (= l ?S) r
        (= r ?S) l
        (= l r)  l
        :default ?G))

(extend-protocol S&G
  clojure.lang.PersistentVector
  (-wrap [_ coll] (into [] coll))
  (-generalize [lhs rhs]
    (-wrap lhs (map generalizable? lhs rhs)))
  (-specialize [lhs neg rhs]
    (map #(-wrap lhs (specialize-at lhs rhs %))
         (util/positions-of specializable? lhs neg rhs)))
  (-init [tmpl]
    (let [d (count tmpl)]
      {:S [(into (-wrap [] []) (repeat d ?S))]
       :G [(into (-wrap [] []) (repeat d ?G))]
       :domain d})))

(def ^:private more-general?
  (fn [patt data]
    (util/pairwise-every? #(or (= %1 %2) (= %1 ?G)) patt data)))

(defn terminated?
  ([vs] (terminated? (:G vs) (:S vs)))
  ([g s]
   (and (empty? g) (empty? s))))

(defn converged?
  ([vs] (converged? (:G vs) (:S vs)))
  ([g s]
   (and (= 1 (bounded-count 2 g) (bounded-count 2 s)) (= g s))))

(defn- positive [{:keys [S G domain]} example]
  (let [g' (filter #(more-general? %1 example) G)]
    {:G g'
     
     :S (let [s' (map (fn [s]
                        (if (not (more-general? s example))
                          (-generalize s example)
                          s))
                      S)]
          (if (converged? g' s')
            s'
            (filter
             (fn [s] (not-any? #(more-general? s %1) G))
             s')))

     :domain domain}))

(defn- negative [{:keys [S G domain]} example]
  {:G (reduce (fn [acc g]
                (if (not (more-general? g example))
                  (concat acc (list g))
                  (concat acc
                          (reduce (fn [acc new_g]
                                    (if (and
                                         (not (more-general? new_g example))
                                         (every? (fn [pe] more-general? new_g pe) S)
                                         (not-any? (fn [other_g]
                                                     (and
                                                      (not (= g other_g))
                                                      (more-general? other_g new_g)))
                                                   G))
                                      (concat acc (list new_g))
                                      acc))
                                  '()
                                  (-specialize g example (first S))))))
              '()
              G)

   :S (filter #(not (more-general? %1 example)) S)

   :domain domain})

(defn arity [n]
  (vec (repeat n '?)))

(defn refine
  ([vs example]
   (refine vs example (-> example meta :positive)))
  ([vs example positive?]
   (if positive?
     (positive vs example)
     (negative vs example))))
