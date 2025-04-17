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

(defn- covers? [patt data]
  (util/pairwise-every? covers-elem? patt data))

(declare collapsed? converged?)

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

(defn arity
  "Returns a vector template for arity n."
  [n]
  (vec (repeat n ??)))

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

(defn consistent?
  "Returns true if all hypotheses in the version space are consistent with the labeled example."
  [vs example label]
  (letfn [(hyp-consistent? [h]
            (let [matches? (covers? h example)]
              (if label
                matches?
                (not matches?))))]
    (and
      (every? hyp-consistent? (:S vs))
      (every? hyp-consistent? (:G vs)))))

(defn applicable?
  "Returns true if the version space can accommodate this example (i.e., at least one hypothesis is consistent)."
  [vs example label]
  (letfn [(hyp-consistent? [h]
            (let [matches? (covers? h example)]
              (if label
                matches?
                (not matches?))))]
    (boolean
     (or
      (some hyp-consistent? (:S vs))
      (some hyp-consistent? (:G vs))))))

(comment

  (def vs
    {:S [['Small 'Red 'Soft]]
     :G [[?G ?G ?G]]})

  (applicable? vs ['Small 'Red 'Soft] true)
  (consistent? vs ['Small 'Red 'Soft] true)  
  ;; => true x2

  (applicable? vs ['Large 'Blue 'Hard] false)
  (consistent? vs ['Large 'Blue 'Hard] false)
  ;; => true, false

  (applicable? vs ['Small 'Red 'Soft] false)
  (consistent? vs ['Small 'Red 'Soft] false)
  ;; => false x2

)

(defn classify
  "Attempts to classify an example using the current version space.
   Returns :positive, :negative, or :unknown if G and S disagree."
  [vs example]
  (let [s-covers? (boolean (some #(covers? % example) (:S vs)))
        g-covers? (every? #(covers? % example) (:G vs))]

    (cond
      ;; All general hypotheses cover AND at least one specific hypothesis agrees
      (and g-covers? s-covers?) :positive

      ;; None of the specific hypotheses agree AND no general hypothesis does either
      (and (not s-covers?) (not (some #(covers? % example) (:G vs)))) :negative

      ;; Otherwise ambiguous
      :else :unknown)))

(comment

  (def vs
  {:S [['Small 'Red 'Soft]]
   :G [[?G 'Red ?G]]})

  (covers? [?? 'Red ??] '[Small Red Soft])
  (covers? '[Small Red Soft] '[Small Red Soft])
  

(classify vs ['Small 'Red 'Soft])
;; => :positive

(classify vs ['Large 'Blue 'Hard])
;; => :negative

(classify vs ['Large 'Red 'Hard])
;; => :unknown  ;; G covers, S rejects

)
