;   Copyright (c) Fogus. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns fogus.rv.learn.vs
  (:require [fogus.rv.core :as core]
            [fogus.rv.learn :as proto]
            [fogus.rv.util :as util]))

(def ^:const ?S (core/->IgnoreT))
(def ^:const ?G (core/->AnyT))
(def ^:const ?? (core/->AskT))

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

(extend-protocol proto/S&G
  clojure.lang.PersistentVector
  (proto/-generalize [g s]
    (vec (generalize-sequential g s)))
  (proto/-specialize [g neg s]
    (vec (specialize-with #(assoc g % (get s %)) g neg s)))
  (proto/-init [tmpl]
    (let [d (count tmpl)]
      {:S [(vec (repeat d ?S))]
       :G [(vec (repeat d ?G))]
       :arity d})))

(defn- covers-elem? [h e]
  (or (= h e) (= h ?G)))

(declare covers? collapsed? converged?)

(defn- positive [{:keys [S G arity]} example]
  (let [g' (filter #(covers? %1 example) G)]
    {:G g'
     
     :S (let [s' (map (fn [s]
                        (if (not (covers? s example))
                          (proto/-generalize s example)
                          s))
                      S)]
          (if (converged? g' s')
            s'
            (filter
             (fn [s] (not-any? #(covers? s %1) G))
             s')))

     :arity arity}))

(defn- negative [{:keys [S G arity]} example]
  {:G (reduce (fn [acc g]
                (if (not (covers? g example))
                  (conj acc g)
                  (into acc
                        (reduce (fn [acc g']
                                  (if (and (not (covers? g' example))
                                           (every? #(covers? g' %) S)
                                           (not-any? #(and (not= g %) (covers? % g')) G))
                                    (conj acc g')
                                    acc))
                                  []
                                  (proto/-specialize g example (first S))))))
              []
              G)

   :S (filter #(not (covers? %1 example)) S)

   :arity arity})

(defn arity-vec
  "Returns a vector template for arity n."
  [n]
  (vec (repeat n ??)))

(defn covers?
  "Takes a hypothesis from a version space and returns if the example is
  consistent with it."
  [hypothesis example]
  (util/pairwise-every? covers-elem? hypothesis example))

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

(defn- classification-for [example]
  (-> example meta ::positive))

(defn refine
  "Given a version space vs and an example, returns a new version space
  with hypotheses adjusted according to the given example's elements and
  its classification. An example is classified by attaching a metadata mapping
  :positive? -> boolean or by passing a boolean as the last argument. The
  explicit classification argument will always dominate the metadata
  classification."
  ([vs example]
   (refine vs example (classification-for example)))
  ([vs example positive?]
   (if positive?
     (positive vs example)
     (negative vs example))))

(defn consistent?
  "Returns true if all hypotheses in the version space are consistent with
  the labeled example."
  ([vs example]
   (consistent? vs example (classification-for example)))
  ([vs example positive?]
   (and
    (every? #(and positive? (covers? % example)) (:S vs))
    (every? #(and positive? (covers? % example)) (:G vs)))))

(defn applicable?
  "Returns true if at least one hypothesis in the version space is consistent
  with the example."
  ([vs example]
   (applicable? vs example (classification-for example)))
  ([vs example positive?]
   (boolean
    (or
     (some #(and positive? (covers? % example)) (:S vs))
     (some #(and positive? (covers? % example)) (:G vs))))))

(defn classify
  "Attempts to classify an example using the current version space.
   Returns ::positive, ::negative, or :ambiguous if G and S disagree."
  [vs example]
  (let [at-least-one-s? (boolean (some #(covers? % example) (:S vs)))
        all-g? (every? #(covers? % example) (:G vs))]
    (cond
      (and all-g? at-least-one-s?) ::positive
      (and (not at-least-one-s?) (not (some #(covers? % example) (:G vs)))) ::negative
      :otherwise ::ambiguous)))
