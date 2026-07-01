;   Copyright (c) Fogus. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns fogus.rv.productions
  "The simplest possible production rules system. A run pairs two
   abstractions: a knowledge base (facts + rules) with an engine (a
   rule-selection strategy paired with a quiessence-detection strategy).

   A production rules system is built from two abstractions:

     1. A knowledge base is the rules and the facts they operate on (the WHAT)
     2. An engine is the policy that applies the rule processing (the HOW)

   Productions are represented as maps having two privileged keys:
   {:antecedent ...
    :consequent ...}

   The :antecedent key in the production map contains a sequence of EAV
   3-tuples with logical variables at key locations for the purpose of
   pattern matching:

     [[?id :person/name     \"Fogus\"]
      [?id :language/speaks ?lang]]

   The antecedent describes the patterns that must be present in the EAV
   set in order for the production to activate. The antecedent is also
   known as the left-hand-side (LHS) of the production.

   When a production activates, the structure in the :consequent key in
   the production map is applied to the knowledge base to potentially
   create new facts. The consequent also contains a sequence of EAV
   3-tuples with logical variables at key locations. However, these tuples
   describe new facts with values bound to embedded logic variables as
   defined within the antecedent context of a production activation.

   A production set is a data structure defined as such:

     1. A production set is simply a vector of production definitions
     2. A production definition is a map containing :antecedent and :consequent keys
     3. An antecedent is a vector of EAV 3-tuples representing patterns in data
     4. An EAV 3-tuple is a vector of three elements: id, attribute, value
     5. A consequent is a vector of EAV 3-tuples representing new attribute assertions

   A fact base is a set of EAV 3-tuples.

   A knowledge base is a map of productions and facts, mapped to keys:

   {:productions #{...}
    :facts       #{...}}

   An engine describes how a knowledge base is processed: which
   matching production fires next, how the stream of successive fact sets
   is shaped, and how quiessence is detected:

   :rule-choice -> fn to pick which production fires next
   :state-xform -> transformer of fact-set states
   :quiesce     -> a completing reducing function that detects when to stop

   The production rules system implemented herein runs an engine against
   a knowledge base in four stages:

     1. Antecedent unifications over the KB's facts
     2. Production selection via the engine's :rule-choice
     3. Consequent substitution and assertion
     4. System quiessence via the engine's :quiesce, through :state-xform
"
  (:require [clojure.core.unify :as u]
            [clojure.set :as s]))

;; Stage 0: Engine definition

(defn make-engine
  "Constructs a production rules engine from input functions:

   :rule-choice -> fn to pick which production fires next
   :state-xform -> transformer of fact-set states
   :quiesce     -> a completing reducing function that detects when to stop
       () initial accumulator
       (state) extract the final fact set
       (state s) new state, or (reduced acc)
  "
  [& {:as config}]
  {:pre [(every? #(contains? config %) #{:rule-choice :state-xform :quiesce})]}
  config)

;; Stage 1: Unifications

(defn unifications 
  "Walks through all of the clauses in an implied antecedent and matches 
   each against every fact provided. Returns a seq of contexts representing
   all of the bindings established by the antecedent unifications across all
   facts provided."
  [[clause & more :as clauses] facts context]
  (if clause
    (let [bindings (keep #(u/unify clause % context) facts)]
      (mapcat #(unifications more facts %) bindings))
    [context]))

;; Stage 2: Production selection

(defn select-production
  "Builds a sequence of bindings paired with each production and then uses a selection 
   function to execute one of the productions that matched."
  [selection-strategy {:keys [productions facts]}]
  (let [possibilities 
        (for [production    productions
              bindings (unifications (:antecedent production) facts {})]
          [production bindings])]
    (selection-strategy possibilities)))

;; Stage 3: Consequent substitutions and assertion

(defn apply-production [production facts context]
  (let [new-facts (set (for [rhs (:consequent production)]
                         (u/subst rhs context)))]
    (s/union new-facts facts)))

;; Stage 3a: Single substitution and assertion

(defn step 
  "Takes a set of productions and facts and returns a new fact base based on the application of single production."
  ([kb] (step rand-nth kb))
  ([choice-fn kb]
   (when-let [[production binds] (select-production choice-fn kb)]
     (apply-production production (:facts kb) binds))))

;; Stage 3b: Repeated substitution and assertion

(defn states
  "Will apply the result of one production firing to the fact base and feed
   the result forward into the next firing. The production selection is
   driven by choice-fn."
  [choice-fn kb]
  (iterate #(step choice-fn (assoc kb :facts %))
           (set (:facts kb))))

(defn run
  "Runs a production rules engine against a knowledge base, driving the
   state stream through the engine's transducer and quiessence function
   until quiessence is reached, then returning the final fact set."
  [{:keys [rule-choice state-xform quiesce]} kb]
  (transduce state-xform quiesce (states rule-choice kb)))

