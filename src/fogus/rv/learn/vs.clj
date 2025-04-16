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
  (-init [basis] [basis arity]))

(defn- specializable? [g neg-example s]
  (and (= g ?G) (not= s neg-example)))

(defn- maybe-generalize [g s]
  (cond (= g ?S) s
        (= s ?S) g
        (= g s)  g
        :default ?G))

(defn- specialization-indexes [g neg s]
  (util/positions-of specializable? g neg s))

(defn- generalize-sequential [g s]
  (map maybe-generalize g s))

(defn- specialize-with [f g neg s]
  (map f (specialization-indexes g neg s)))

(extend-protocol S&G
  clojure.lang.PersistentVector
  (-generalize [g s]
    (vec (generalize-sequential g s)))
  (-specialize [g neg s]
    (vec (specialize-with #(assoc g % (get s %)) g neg s)))
  (-init [tmpl]
    (let [d (count tmpl)]
      {:S [(vec (repeat d ?S))]
       :G [(vec (repeat d ?G))]
       :arity d})))

(defn- more-general? [patt data]
  (util/pairwise-every? #(or (= %1 %2) (= %1 ?G)) patt data))

(defn collapsed?
  "Returns if a version space vs or a most-general hypothesis g and a
  most-specific hypothesis s have collapsed. That is, training has
  caused the hypotheses to become inconsistent, making further classification
  impossible."
  ([vs] (collapsed? (:G vs) (:S vs)))
  ([g s]
   (and (empty? g) (empty? s))))

(defn converged?
  "Returns if a version space vs or a most-general hypothesis g and a
  most-specific hypothesis s have converged. That is, training has
  caused the hypotheses to ground to a single legal case."
  ([vs] (converged? (:G vs) (:S vs)))
  ([g s]
   (and (= 1 (bounded-count 2 g) (bounded-count 2 s)) (= g s))))

(defn- positive [{:keys [S G arity]} example]
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

     :arity arity}))

(defn- negative [{:keys [S G arity]} example]
  {:G (reduce (fn [acc g]
                (if (not (more-general? g example))
                  (conj acc g)
                  (into acc
                        (reduce (fn [acc g']
                                  (if (and (not (more-general? g' example))
                                           (every? #(more-general? g' %) S)
                                           (not-any? #(and (not= g %) (more-general? % g')) G))
                                    (conj acc g')
                                    acc))
                                  []
                                  (-specialize g example (first S))))))
              []
              G)

   :S (filter #(not (more-general? %1 example)) S)

   :arity arity})

(defn arity
  "Returns a vector template for arity n."
  [n]
  (vec (repeat n '?)))

(defn refine
  "Given a version space vs and an example, returns a new version space
  with hypotheses adjusted according to the given example's elements and
  its classification. An example is classified by attaching a metadata mapping
  :positive? -> boolean or by passing a boolean as the last argument. The
  explicit classification argument will always dominate the metadata
  classification."
  ([vs example]
   (refine vs example (-> example meta :positive)))
  ([vs example positive?]
   (if positive?
     (positive vs example)
     (negative vs example))))
