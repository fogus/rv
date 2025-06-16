;   Copyright (c) Fogus. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns fogus.rv.core
  "Most functions in rv work off of one or more of the following core
  concepts:

  - Entity: a hashmap with a :kb/id key mapped to a unique value and namespaced keys
  - Table: a set of hashmaps or Entities
  - Fact: a vector triple in the form [entity-id attribute value]
  - Relation: a set of Facts pertaining to a particular Entity
  - LVar: a logic variable that can bind to any value in its :range
  - Ground: a concrete value
  - Query: a set of Facts containing a mix of LVars and Grounds  
  - Rules: a set of Facts describing synthetic relations
  - Production: a pair of: antecedent query and consequent Facts
  - KB: a set of Relations about many Entities and possibly containing Productions  
  - Constraint Description: a set of LVars and a Formula describing the domain of their values
  - Formula: a list describing a predicate expression of mixed LVars and clojure functions"
  (:import java.io.Writer))

;; Logic variables

(defrecord LVar [domain range]
  Object
  (toString [this]
    (if range
      (str "?" domain "::" range)
      (str "?" domain))))

(def lv? #(instance? LVar %))

(deftype IgnoreT []
  Object
  (toString [_] "_")
  (equals [_ o] (instance? IgnoreT o)))

(deftype AnyT []
  Object
  (toString [_] "*")
  (equals [_ o] (instance? AnyT o)))

(deftype AskT []
  Object
  (toString [_] "?")
  (equals [_ o] true))

(defmethod print-method LVar [lvar ^Writer writer]
  (.write writer (str lvar)))

(defmethod print-method IgnoreT [i ^Writer writer]
  (.write writer (str i)))

(defmethod print-method AnyT [a ^Writer writer]
  (.write writer (str a)))

(def ID_KEY :kb/id)

(def ^:private use-or-gen-id
  (let [next-id (atom 0)]
    (fn [entity]
      (if-let [id (get entity ID_KEY)]
        id
        (swap! next-id inc)))))

(defn- set->tuples
  [id k s]
  (for [v s] [id k v]))

(defn- vector->tuples
  [idfn eid k v]
  (let [vid (idfn v)
        sid (idfn v)
        pre [[eid k vid]
             [vid :sequence/items sid]
             [vid :sequence/indexed? true]]]
    (loop [elems v
           i 0
           cid sid
           tuples pre]
      (if (seq elems)
        (let [head [cid :cell/head (first elems)]
              index [cid :cell/i i]
              tid (when (next elems)
                    (idfn (inc i)))]
          (recur (rest elems)
                 (inc i)
                 tid
                 (into tuples (if tid
                                [head index [cid :cell/tail tid]]
                                [head index]))))
        tuples))))

(comment
  (map->relation {:kb/id :primes
                 :num/primes [1 2 3 5 7]})

  (vector->tuples use-or-gen-id :primes :num/primes [1 2 3 5 7])
  
  ;; becomes

  [:primes :num/primes 100]
  [100 :sequence/items 101]
  [100 :sequence/indexed? true]
  [101 :cell/head 1] [101 :cell/i 0] [101 :cell/tail 103]
  [103 :cell/head 2] [103 :cell/i 1] [103 :cell/tail 105]
  [105 :cell/head 3] [105 :cell/i 2] [105 :cell/tail 107]
  [107 :cell/head 5] [107 :cell/i 3] [107 :cell/tail 109]
  [109 :cell/head 7] [109 :cell/i 4]

)

(defn map->relation
  "Converts a map to a set of tuples for that map, applying a unique
  :kb/id if the map doesn't already have a value mapped for that key.

  Relation values that are sets are expanded into individual tuples
  per item in the set with the same :kb/id as the entity and the
  attribute that the whole set was mapped to.  

  An idfn is a function of map -> id and if provided is used to
  override the default entity id generation and any existing :kb/id
  values."
  ([entity]
   (map->relation use-or-gen-id entity))
  ([idfn entity]
   (let [id (idfn entity)]
     (reduce (fn [acc [k v]]
               (if (= k ID_KEY)
                 acc
                 (cond (set? v) (concat acc (set->tuples id k v))
                       (vector? v) (concat acc (vector->tuples idfn id k v))
                       :default (conj acc [id k v]))))
             []
             (seq entity)))))

(defn table->kb
  "Converts a Table into a KB, applying unique :kb/id to maps without a
  mapped identity value.

  See map->relation for more information about how the entities in the
  table are converted to relations.

  An idfn is a function of map -> id and if provided is used to
  override the default entity id generation and any existing :kb/id
  values."
  ([table] (table->kb use-or-gen-id table))
  ([idfn table]
   {:facts (set (mapcat #(map->relation idfn %) table))}))

