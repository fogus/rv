;   Copyright (c) Fogus. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns fogus.rv.productions.naive)

(def random-choice rand-nth)

(def noop identity)

(defn qf
  "Returns reducing function that terminates after n production firings."
  [n]
  (let [ct (atom 0)]
    (fn
      ([] (reset! ct 0) nil)
      ([state] state)
      ([_ s] (if (>= (swap! ct inc) n) (reduced s) s)))))
