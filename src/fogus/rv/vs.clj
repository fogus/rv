;   Copyright (c) Fogus. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns fogus.rv.vs
  (:require [fogus.rv.core :as core]))

(def ^:const ?S (core/->IgnoreT))
(def ^:const ?G (core/->AnyT))

(defprotocol S&G
  (-generalize [lhs rhs])
  (-specialize [lhs neg rhs])
  (-wrap [basis coll])
  (-init [basis] [basis domain]))

(defn- generalize-sequence [lhs rhs]
  (-wrap lhs
         (map (fn [a b]
                (cond
                  (= a ?S) b
                  (= b ?S) a
                  (= a b) a
                  :default ?G))
              lhs
              rhs)))

(defn- specialize-at-position [g s pos]
  (if (= pos 0)
    (cons (first s) (rest g))
    (cons (first g) (specialize-at-position (rest g) (rest s) (- pos 1)))))

(defn- position-can-be-specialized? [g neg s]
  (and (= g ?G) (not= s neg)))

(defn- get-potential-positions [g neg s]
  (keep-indexed
   (fn [index specializable]
     (when specializable index))
   (map position-can-be-specialized? g neg s)))

(defn- specialize-sequence [g neg s]
  (map 
   (fn [pos] (-wrap g (specialize-at-position g s pos)))
   (get-potential-positions g neg s)))

(extend-protocol S&G
  clojure.lang.PersistentVector
  (-wrap [_ coll] (into [] coll))
  (-generalize [lhs rhs] (generalize-sequence lhs rhs))
  (-specialize [lhs neg rhs] (specialize-sequence lhs neg rhs))
  (-init [tmpl]
    (let [d (count tmpl)]
      {:S [(into (-wrap [] []) (repeat d ?S))]
       :G [(into (-wrap [] []) (repeat d ?G))]
       :domain d})))

(defn- includes? [a b]
  (or (= a b) (= a ?G)))

(defn- more-general? [a b]
  (every? true? (map includes? a b)))

(defn terminated?
  ([vs] (terminated? (:G vs) (:S vs)))
  ([g s]
   (and (empty? g) (empty? s))))

(defn converged?
  ([vs] (converged? (:G vs) (:S vs)))
  ([g s]
   (and (= 1 (count g)) (= 1 (count s)) (= g s))))

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

