;   Copyright (c) Fogus. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns fogus.rv.learn.vs
  "Version spaces are a binary classification, empirical learning algorithm.
  The approach, as described in 'Version spaces: a candidate elimination approach
  to rule learning' by Tom Mitchel (1977) takes training examples (currently
  Tuples of a like-arity) and manages a 'version space'. A version space is a
  map containing two 'boundaries' `:S` and `:G`. The `:G` boundary contains 'hypotheses'
  corresponding to the most general versions of the training data that are consistent
  and `:S` is the most specific versions. When a version space is presented with a new
  example it runs a 'candidate elimination' algorithm to modify the boundaries `:S`
  and `:G` accordingly. Examples can be marked as 'positive' examples, meaning
  that they are preferred instances. Anything not marked as 'positive' are taken as
  negative examples. Once trained, a version space can  classify new examples as
  `::positive` or `::negative`. If new examples are not covered by the existing hypotheses
  in either boundary then they are classified as `::ambiguous` instead."
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
  "Takes a `hypothesis` from a version space and returns if the `example` is
  consistent with it."
  [hypothesis example]
  (util/pairwise-every? covers-elem? hypothesis example))

(defn collapsed?
  "Returns if a version space `vs` or boundaries `g` and `s` have collapsed.
  That is, training data have caused the hypotheses to become inconsistent,
  making further classification impossible."
  ([vs] (collapsed? (:G vs) (:S vs)))
  ([g s] (and (empty? g) (empty? s))))

(defn converged?
  "Returns if a version space `vs` or boundaries `g` and `s` have
  converged. That is, training has caused the boundaries to converge to a single
  case."
  ([vs] (converged? (:G vs) (:S vs)))
  ([g s]
   (and (= 1 (bounded-count 2 g) (bounded-count 2 s)) (= g s))))

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

(defn- classification-for [example]
  (-> example meta ::positive))

(defn refine
  "Given a version space `vs` and an `example`, returns a new version space
  with boundaries adjusted according to the given example's features and
  classification. An example is marked as positive by attaching a metadata mapping
  `:positive?` -> boolean or by passing a boolean as the last argument. The
  explicit classification argument will always dominate the metadata
  classification."
  ([vs example]
   (refine vs example (classification-for example)))
  ([vs example positive?]
   (if positive?
     (positive vs example)
     (negative vs example))))

(defn consistent?
  "Returns `true` if all hypotheses in the version space `vs`'s general and specific
  boundaries are consistent with the `example` features and classification."
  ([vs example]
   (consistent? vs example (classification-for example)))
  ([vs example positive?]
   (and
    (every? #(and positive? (covers? % example)) (:S vs))
    (every? #(and positive? (covers? % example)) (:G vs)))))

(defn applicable?
  "Returns true if at least one hypothesis in the version space `vs` is consistent
  with the `example` and false otherwise."
  ([vs example]
   (applicable? vs example (classification-for example)))
  ([vs example positive?]
   (boolean
    (or
     (some #(and positive? (covers? % example)) (:S vs))
     (some #(and positive? (covers? % example)) (:G vs))))))

(defn classify
  "Attempts to classify an `example` using the given version space `vs`.
  Returns `::positive`, `::negative`, or `::ambiguous` if the boundaries
  G and S are incongruent."
  [vs example]
  (let [at-least-one-s? (boolean (some #(covers? % example) (:S vs)))
        all-g? (every? #(covers? % example) (:G vs))]
    (cond
      (and all-g? at-least-one-s?) ::positive
      (and (not at-least-one-s?) (not (some #(covers? % example) (:G vs)))) ::negative
      :otherwise ::ambiguous)))

(defn- similarity
  "Computes a similarity score as the ratio of positions in which
   the hypothesis covers the example."
  [hypothesis example]
  (let [arity (count hypothesis)]
    (if (zero? arity)
      arity
      (/ (->> (map covers-elem? hypothesis example)
              (filter identity)
              count)
         arity))))

(defn- explain-hypothesis
  [hypothesis example]
  (let [mismatches (keep-indexed
                    (fn [i [h e]]
                      (when-not (covers-elem? h e)
                        {:position i
                         :constraint h}))
                    (map vector hypothesis example))]
    {:hypothesis hypothesis
     :covers? (covers? hypothesis example)
     :mismatched-features (vec mismatches)
     :similarity (similarity hypothesis example)}))

(defn explain
  "Returns a structure explaining how the classifier reaches a conclusion,
  given a version space `vs` and a compatible `example`.

  The map returned contains the mappings:

  - `:explain/classification` -> the result of the call to `classify`
  - `:explain/example` -> the example given
  - `explain/G` -> a sequence of hypotheses coverage analysis structures in the G boundary
  - `explain/S` -> a sequence of hypotheses coverage analysis structures in the S boundary

  The hypotheses coverage analyses contain the mappings:

  - `:hypothesis` -> The hypothesis inspected
  - `:covers?` -> true or false if the hypothesis covers the example
  - `:similarity` -> A ratio of hypothesis coverages over its arity
  - `:mismatched-features` -> a sequence of the features of the hypothesis that do not match the example

  A mismatched feature of a hypothesis has the mappings:

  - `:position` -> the position of the feature in the hypothesis
  - `:constraint` -> the value or wildcard at that position
  
  The information provided is sufficient for informing human-in-the-loop learning
  interactions."
  [vs example]
  {:explain/classification (classify vs example)
   :explain/example example
   :explain/S (vec (map #(explain-hypothesis % example) (:S vs)))
   :explain/G (vec (map #(explain-hypothesis % example) (:G vs)))})

(defn best-fit
  "Returns the best-fit hypothesis coverage analysis (see `explain`) for a given
  version space `vs` and compatible `example`. The metadata of the best fit return will
  have a mapping of `::fit-from` -> `:S` or `:G` pertaining to which boundary set the
  fit came from."
  [vs example]
  (let [explanation (explain vs example)
        best #(vec (sort-by (comp - :similarity) %))
        [best-s & _] (best (:explain/S explanation))
        [best-g & _] (best (:explain/G explanation))]
    (if (> (:similarity best-s) (:similarity best-g))
      (with-meta best-s {::fit-from :S})
      (with-meta best-g {::fit-from :G}))))
