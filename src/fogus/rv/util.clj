;   Copyright (c) Fogus. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns fogus.rv.util)

(defn process-bindings [bindings]
  {:names  (take-nth 2 bindings)
   :values (vec (take-nth 2 (rest bindings)))})

(defn cart [colls]
  (if (empty? colls)
    '(())
    (for [more (cart (rest colls))
          x (first colls)]
      (cons x more))))

(defn f-by [f key coll]
  (when (seq coll)
    (reduce #(f key %1 %2)
            coll)))

(defn pairwise-every? [pred xs ys]
  (every? identity (map pred xs ys)))

(defn positions-of [pred & xs]
  (keep-indexed #(when %2 %1) (apply map pred xs)))
