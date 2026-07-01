
-----
# <a name="fogus.rv.amb">fogus.rv.amb</a>


Provides an implementation of McCarthy's [`amb`](#fogus.rv.amb/amb) operator with
   binding forms and acceptance test operator.




## <a name="fogus.rv.amb/accept">`accept`</a><a name="fogus.rv.amb/accept"></a>
``` clojure

(accept condition ret)
```
Function.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/amb.clj#L14-L18">Source</a></sub></p>

## <a name="fogus.rv.amb/amb">`amb`</a><a name="fogus.rv.amb/amb"></a>
``` clojure

(amb & [binds & body])
```
Function.

A macro that provides a non-deterministic way to traverse a space
   and find a single solution amongst potentially many. If the search
   space is exhausted then [[[`amb`](#fogus.rv.amb/amb)](#fogus.rv.amb/amb)](#fogus.rv.amb/amb) will return `nil`. The general form
   of [[[`amb`](#fogus.rv.amb/amb)](#fogus.rv.amb/amb)](#fogus.rv.amb/amb) is as follows:

      (amb <bindings> <execution body>)

   Where `<bindings>` is a typical Clojure bindings form:

      [<name1> <value1> ... <nameN> <valueN>]

   And `<execution body>` is one or more Clojure expressions.

   Within the execution body the `(accept <condition> <expression>)`
   form is used to test some combination of the bindings for adherence
   to a `<condition>` and return an `<expression>` that serves as the
   return value of the call to [[[`amb`](#fogus.rv.amb/amb)](#fogus.rv.amb/amb)](#fogus.rv.amb/amb).

   A call to `(amb)` (i.e. without bindings and body) will exhaust
   immediately and thus result in `nil` as its value.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/amb.clj#L20-L54">Source</a></sub></p>

-----
# <a name="fogus.rv.constraints">fogus.rv.constraints</a>


Constraints solving functions that operate on a Constraint Description
  which is a map describing a constraint description containing the mappings:
  - :variables -> seq of LVars
  - :formula -> list describing a predicate expression composed of a mix of
    the LVars in :variables and Clojure functions.




## <a name="fogus.rv.constraints/satisfy*">`satisfy*`</a><a name="fogus.rv.constraints/satisfy*"></a>
``` clojure

(satisfy* {:keys [variables formula :as c]})
```

Accepts a map describing a constraint description containing the mappings:
  - :variables -> seq of LVars
  - :formula -> list describing a predicate expression composed of a mix of
    the LVars in :variables and Clojure functions

  This function will use the constraint description to calculate the all of 
  the values for the LVars that satisfy the formula. The result is a seq of
  maps with mappings from LVar -> value. If there is no way to satisfy the
  formula then an empty seq is the result.

  The ordering of the results of this function is not guaranteed to be stable.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/constraints.clj#L47-L66">Source</a></sub></p>

## <a name="fogus.rv.constraints/satisfy1">`satisfy1`</a><a name="fogus.rv.constraints/satisfy1"></a>
``` clojure

(satisfy1 {:keys [variables formula :as c]})
```

Accepts a map describing a constraint description containing the mappings:
  - :variables -> seq of LVars
  - :formula -> list describing a predicate expression composed of a mix of
    the LVars in :variables and Clojure functions

  This function will use the constraint description to calculate the first
  set of values for the LVars that satisfy the formula. The result is a map
  with mappings from LVar -> value. If there is no way to satisfy the formula
  then an empty map is the result.

  The first found result of this function is not guaranteed to be stable.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/constraints.clj#L32-L45">Source</a></sub></p>

-----
# <a name="fogus.rv.core">fogus.rv.core</a>


Most functions in rv work off of one or more of the following core
  concepts:

  Entity: a hashmap with a :kb/id key mapped to a unique value and namespaced keys.

  {:kb/id       :person/john-doe
   :person/name "John Doe"
   :person/age  42}
  
  Table: a set of hashmaps or Entities. Tables represent unstructured or
  semi-structured collections of data.

  #{{:kb/id :city/blt :city/name "Baltimore"}
    {:kb/id :city/atl  :city/name "Atlanta"}}

  Fact: a vector triple in the form [entity-id attribute value] that describe that
  a given entity has an attribute with a specific value. You can tie facts together
  by referencing their :kb/ids.

  [:person/john-doe :person/age 42]
  [:city/blt :city/name "Baltimore"]

  Relation: a set of Facts pertaining to a particular Entity. You can tie facts
  together by referencing :kb/ids in value positions.

  #{[:person/john-doe :person/age 42]
    [:city/blt :city/name "Baltimore"]
    [:person/john-doe :address/city :city/blt]}

  LVar: a logic variable that can unify with any value in its :range

  (map->LVar {:domain 'x :range (range 0 5)})

  Ground: a concrete value, like a keyword, number, string, etc.

  42, "John Doe", :city/blt

  Query: a set of Facts containing a mix of LVars and Grounds used  to find
  bindings for the LVars that satisfy a set of Facts.

  Rules: a set of Facts describing derived or synthetic relations in terms of
  existing ones.

  Production: a map containing :antecedent -> query and :consequent -> Facts
  to be asserted if the query fires.

  KB: a set of Relations about many Entities and possibly containing Productions. It
  represents all the knowledge currently known or derivable.

  Engine: describes how a KB is processed, including: which matching Production fires
  next, how the stream of successive Facts are shaped, and how to know when to stop.

  Constraint Description: a set of LVars and a Formula describing the domain
  of their values.

  Formula: a list describing a predicate expression of mixed LVars and clojure functions.




## <a name="fogus.rv.core/->AnyT">`->AnyT`</a><a name="fogus.rv.core/->AnyT"></a>
``` clojure

(->AnyT)
```
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/core.clj#L83-L86">Source</a></sub></p>

## <a name="fogus.rv.core/->AskT">`->AskT`</a><a name="fogus.rv.core/->AskT"></a>
``` clojure

(->AskT)
```
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/core.clj#L88-L91">Source</a></sub></p>

## <a name="fogus.rv.core/->IgnoreT">`->IgnoreT`</a><a name="fogus.rv.core/->IgnoreT"></a>
``` clojure

(->IgnoreT)
```
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/core.clj#L78-L81">Source</a></sub></p>

## <a name="fogus.rv.core/AnyT">`AnyT`</a><a name="fogus.rv.core/AnyT"></a>



<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/core.clj#L83-L86">Source</a></sub></p>

## <a name="fogus.rv.core/AskT">`AskT`</a><a name="fogus.rv.core/AskT"></a>



<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/core.clj#L88-L91">Source</a></sub></p>

## <a name="fogus.rv.core/ID_KEY">`ID_KEY`</a><a name="fogus.rv.core/ID_KEY"></a>



<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/core.clj#L102-L102">Source</a></sub></p>

## <a name="fogus.rv.core/IgnoreT">`IgnoreT`</a><a name="fogus.rv.core/IgnoreT"></a>



<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/core.clj#L78-L81">Source</a></sub></p>

## <a name="fogus.rv.core/lv?">`lv?`</a><a name="fogus.rv.core/lv?"></a>
``` clojure

(lv? %1)
```
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/core.clj#L76-L76">Source</a></sub></p>

## <a name="fogus.rv.core/map->relation">`map->relation`</a><a name="fogus.rv.core/map->relation"></a>
``` clojure

(map->relation entity)
(map->relation idfn entity)
```

Converts a map to a set of tuples for that map, applying a unique
  :kb/id if the map doesn't already have a value mapped for that key.

  Relation values that are sets are expanded into individual tuples
  per item in the set with the same :kb/id as the entity and the
  attribute that the whole set was mapped to.  

  An idfn is a function of map -> id and if provided is used to
  override the default entity id generation and any existing :kb/id
  values.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/core.clj#L158-L180">Source</a></sub></p>

## <a name="fogus.rv.core/table->kb">`table->kb`</a><a name="fogus.rv.core/table->kb"></a>
``` clojure

(table->kb table)
(table->kb idfn table)
```

Converts a Table into a KB, applying unique :kb/id to maps without a
  mapped identity value.

  See map->relation for more information about how the entities in the
  table are converted to relations.

  An idfn is a function of map -> id and if provided is used to
  override the default entity id generation and any existing :kb/id
  values.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/core.clj#L182-L194">Source</a></sub></p>

-----
# <a name="fogus.rv.datalog">fogus.rv.datalog</a>


A minimal implementation of Datalog.




## <a name="fogus.rv.datalog/entity">`entity`</a><a name="fogus.rv.datalog/entity"></a>
``` clojure

(entity kb id)
```

Given a KB and an id within that KB, returns a map containing all of
  the attr->val mappings for that logical 'entity'.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/datalog.clj#L153-L164">Source</a></sub></p>

## <a name="fogus.rv.datalog/linked-list-rules">`linked-list-rules`</a><a name="fogus.rv.datalog/linked-list-rules"></a>



<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/datalog.clj#L82-L83">Source</a></sub></p>

## <a name="fogus.rv.datalog/q">`q`</a><a name="fogus.rv.datalog/q"></a>
``` clojure

(q query kb)
(q query kb rules)
```

Queries a knowledge base or a set of relations given a vector
  form of a query and an optional set of rules.

  A query takes the form:

  [:find find-spec :where clauses]

  A find-spec can be any number of lvars like:

  [:find ?e ?v :where ...]

  or a tuple containing a mix of lvars and grounds which is used to
  build output tuples from the query results:

  [:find [?e :an/attribute ?v] :where ...]

  The :where clauses are any number of tuples containing a mix of
  lvars and grounds:

  [:find ...
   :where
   [?e :an/attribute ?v]
   [?e :another/attr 42]]

  :where clauses may also contain filters defined as calls to predicates
  used to constrain the values that may bind to lvars:

  [:find ...
   :where
   [?e :an/attribute ?v]
   (= ?v 42)]

  The possible filter predicates are: =, not=, <, >, <=, >=

  rules are a vector of lists where each list defines a rule with a
  single head tuple followed by any number of rule clauses:

  ([?p :relationship/parent ?c] [?p :relationship/father ?c])
  
  The rule above defines a syntheic relation called
  `:relationship/parent` defined in terms of another relation
  `relationship/father`. Rules describe synthetic relations derived
  from real relations in the data or other synthetic relations
  derived from previous rule applications.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/datalog.clj#L96-L151">Source</a></sub></p>

## <a name="fogus.rv.datalog/query->map">`query->map`</a><a name="fogus.rv.datalog/query->map"></a>
``` clojure

(query->map query)
```

Accepts the vector form of a Datalog query and outputs a map
  of the component sections as keyword->seq mappings.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/datalog.clj#L85-L94">Source</a></sub></p>

-----
# <a name="fogus.rv.fuzzy.soundex">fogus.rv.fuzzy.soundex</a>


I came across the Soundex algorithm when researching the retro KAMAS outlining application.
  Soundex is a phonetic algorithm for indexing words by sound.




## <a name="fogus.rv.fuzzy.soundex/encode">`encode`</a><a name="fogus.rv.fuzzy.soundex/encode"></a>
``` clojure

(encode word & {:keys [numeric?], :as opts})
```

Soundex is an algorithm for creating indices for words based on their
  English pronunciation. Homophones are encoded such that words can be matched
  despite minor differences in spelling. Example, the words "Ashcraft" and
  "Ashcroft" are both encoded as the same soundex code "A261".

  This function accepts the following keyword arguments:

  :numeric? -> true numerically encodes the entire word rather than using
  the default soundex letter prefix.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/fuzzy/soundex.clj#L46-L62">Source</a></sub></p>

-----
# <a name="fogus.rv.impl.unification">fogus.rv.impl.unification</a>


Provides internal unification functions.
  DO NOT USE THIS NS.
  There is no guarantee that it will remain stable or at all.




## <a name="fogus.rv.impl.unification/subst">`subst`</a><a name="fogus.rv.impl.unification/subst"></a>



<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/impl/unification.clj#L16-L16">Source</a></sub></p>

-----
# <a name="fogus.rv.learn">fogus.rv.learn</a>


Common learning-related functions and protocols.




## <a name="fogus.rv.learn/-generalize">`-generalize`</a><a name="fogus.rv.learn/-generalize"></a>
``` clojure

(-generalize lhs rhs)
```
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/learn.clj#L5-L5">Source</a></sub></p>

## <a name="fogus.rv.learn/-init">`-init`</a><a name="fogus.rv.learn/-init"></a>
``` clojure

(-init basis)
(-init basis arity)
```
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/learn.clj#L7-L7">Source</a></sub></p>

## <a name="fogus.rv.learn/-specialize">`-specialize`</a><a name="fogus.rv.learn/-specialize"></a>
``` clojure

(-specialize lhs neg rhs)
```
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/learn.clj#L6-L6">Source</a></sub></p>

## <a name="fogus.rv.learn/S&G">`S&G`</a><a name="fogus.rv.learn/S&G"></a>



<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/learn.clj#L4-L7">Source</a></sub></p>

-----
# <a name="fogus.rv.learn.vs">fogus.rv.learn.vs</a>


Version spaces are a binary classification, empirical learning algorithm.
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
  in either boundary then they are classified as `::ambiguous` instead.




## <a name="fogus.rv.learn.vs/??">`??`</a><a name="fogus.rv.learn.vs/??"></a>



<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/learn/vs.clj#L29-L29">Source</a></sub></p>

## <a name="fogus.rv.learn.vs/?G">`?G`</a><a name="fogus.rv.learn.vs/?G"></a>



<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/learn/vs.clj#L28-L28">Source</a></sub></p>

## <a name="fogus.rv.learn.vs/?S">`?S`</a><a name="fogus.rv.learn.vs/?S"></a>



<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/learn/vs.clj#L27-L27">Source</a></sub></p>

## <a name="fogus.rv.learn.vs/applicable?">`applicable?`</a><a name="fogus.rv.learn.vs/applicable?"></a>
``` clojure

(applicable? vs example)
(applicable? vs example positive?)
```

Returns true if at least one hypothesis in the version space `vs` is consistent
  with the `example` and false otherwise.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/learn/vs.clj#L193-L202">Source</a></sub></p>

## <a name="fogus.rv.learn.vs/arity-vec">`arity-vec`</a><a name="fogus.rv.learn.vs/arity-vec"></a>
``` clojure

(arity-vec n)
```

Returns a vector template for arity n.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/learn/vs.clj#L103-L106">Source</a></sub></p>

## <a name="fogus.rv.learn.vs/best-fit">`best-fit`</a><a name="fogus.rv.learn.vs/best-fit"></a>
``` clojure

(best-fit vs example)
```

Returns the best-fit hypothesis coverage analysis (see [`explain`](#fogus.rv.learn.vs/explain)) for a given
  version space `vs` and compatible `example`. The metadata of the best fit return will
  have a mapping of `::fit-from` -> `:S` or `:G` pertaining to which boundary set the
  fit came from.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/learn/vs.clj#L272-L284">Source</a></sub></p>

## <a name="fogus.rv.learn.vs/classify">`classify`</a><a name="fogus.rv.learn.vs/classify"></a>
``` clojure

(classify vs example)
```

Attempts to classify an `example` using the given version space `vs`.
  Returns `::positive`, `::negative`, or `::ambiguous` if the boundaries
  G and S are incongruent.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/learn/vs.clj#L204-L214">Source</a></sub></p>

## <a name="fogus.rv.learn.vs/collapsed?">`collapsed?`</a><a name="fogus.rv.learn.vs/collapsed?"></a>
``` clojure

(collapsed? vs)
(collapsed? g s)
```

Returns if a version space `vs` or boundaries `g` and `s` have collapsed.
  That is, training data have caused the hypotheses to become inconsistent,
  making further classification impossible.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/learn/vs.clj#L114-L119">Source</a></sub></p>

## <a name="fogus.rv.learn.vs/consistent?">`consistent?`</a><a name="fogus.rv.learn.vs/consistent?"></a>
``` clojure

(consistent? vs example)
(consistent? vs example positive?)
```

Returns `true` if all hypotheses in the version space `vs`'s general and specific
  boundaries are consistent with the `example` features and classification.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/learn/vs.clj#L183-L191">Source</a></sub></p>

## <a name="fogus.rv.learn.vs/converged?">`converged?`</a><a name="fogus.rv.learn.vs/converged?"></a>
``` clojure

(converged? vs)
(converged? g s)
```

Returns if a version space `vs` or boundaries `g` and `s` have
  converged. That is, training has caused the boundaries to converge to a single
  case.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/learn/vs.clj#L121-L127">Source</a></sub></p>

## <a name="fogus.rv.learn.vs/covers?">`covers?`</a><a name="fogus.rv.learn.vs/covers?"></a>
``` clojure

(covers? hypothesis example)
```

Takes a `hypothesis` from a version space and returns if the `example` is
  consistent with it.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/learn/vs.clj#L108-L112">Source</a></sub></p>

## <a name="fogus.rv.learn.vs/explain">`explain`</a><a name="fogus.rv.learn.vs/explain"></a>
``` clojure

(explain vs example)
```

Returns a structure explaining how the classifier reaches a conclusion,
  given a version space `vs` and a compatible `example`.

  The map returned contains the mappings:

  - `:explain/classification` -> the result of the call to [`classify`](#fogus.rv.learn.vs/classify)
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
  interactions.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/learn/vs.clj#L241-L270">Source</a></sub></p>

## <a name="fogus.rv.learn.vs/refine">`refine`</a><a name="fogus.rv.learn.vs/refine"></a>
``` clojure

(refine vs example)
(refine vs example positive?)
```

Given a version space `vs` and an `example`, returns a new version space
  with boundaries adjusted according to the given example's features and
  classification. An example is marked as positive by attaching a metadata mapping
  `:positive?` -> boolean or by passing a boolean as the last argument. The
  explicit classification argument will always dominate the metadata
  classification.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/learn/vs.clj#L169-L181">Source</a></sub></p>

-----
# <a name="fogus.rv.productions">fogus.rv.productions</a>


The simplest possible production rules system. A run pairs two
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

     [[?id :person/name     "Fogus"]
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





## <a name="fogus.rv.productions/apply-production">`apply-production`</a><a name="fogus.rv.productions/apply-production"></a>
``` clojure

(apply-production production facts context)
```
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/productions.clj#L118-L121">Source</a></sub></p>

## <a name="fogus.rv.productions/make-engine">`make-engine`</a><a name="fogus.rv.productions/make-engine"></a>
``` clojure

(make-engine & {:as config})
```

Constructs a production rules engine from input functions:

   :rule-choice -> fn to pick which production fires next
   :state-xform -> transformer of fact-set states
   :quiesce     -> a completing reducing function that detects when to stop
       () initial accumulator
       (state) extract the final fact set
       (state s) new state, or (reduced acc)
  
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/productions.clj#L77-L89">Source</a></sub></p>

## <a name="fogus.rv.productions/run">`run`</a><a name="fogus.rv.productions/run"></a>
``` clojure

(run {:keys [rule-choice state-xform quiesce]} kb)
```

Runs a production rules engine against a knowledge base, driving the
   state stream through the engine's transducer and quiessence function
   until quiessence is reached, then returning the final fact set.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/productions.clj#L142-L147">Source</a></sub></p>

## <a name="fogus.rv.productions/select-production">`select-production`</a><a name="fogus.rv.productions/select-production"></a>
``` clojure

(select-production selection-strategy {:keys [productions facts]})
```

Builds a sequence of bindings paired with each production and then uses a selection 
   function to execute one of the productions that matched.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/productions.clj#L106-L114">Source</a></sub></p>

## <a name="fogus.rv.productions/states">`states`</a><a name="fogus.rv.productions/states"></a>
``` clojure

(states choice-fn kb)
```

Will apply the result of one production firing to the fact base and feed
   the result forward into the next firing. The production selection is
   driven by choice-fn.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/productions.clj#L134-L140">Source</a></sub></p>

## <a name="fogus.rv.productions/step">`step`</a><a name="fogus.rv.productions/step"></a>
``` clojure

(step kb)
(step choice-fn kb)
```

Takes a set of productions and facts and returns a new fact base based on the application of single production.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/productions.clj#L125-L130">Source</a></sub></p>

## <a name="fogus.rv.productions/unifications">`unifications`</a><a name="fogus.rv.productions/unifications"></a>
``` clojure

(unifications [clause & more :as clauses] facts context)
```

Walks through all of the clauses in an implied antecedent and matches 
   each against every fact provided. Returns a seq of contexts representing
   all of the bindings established by the antecedent unifications across all
   facts provided.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/productions.clj#L93-L102">Source</a></sub></p>

-----
# <a name="fogus.rv.productions.naive">fogus.rv.productions.naive</a>






## <a name="fogus.rv.productions.naive/noop">`noop`</a><a name="fogus.rv.productions.naive/noop"></a>



<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/productions/naive.clj#L13-L13">Source</a></sub></p>

## <a name="fogus.rv.productions.naive/qf">`qf`</a><a name="fogus.rv.productions.naive/qf"></a>
``` clojure

(qf n)
```

Returns reducing function that terminates after n production firings.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/productions/naive.clj#L15-L22">Source</a></sub></p>

## <a name="fogus.rv.productions.naive/random-choice">`random-choice`</a><a name="fogus.rv.productions.naive/random-choice"></a>



<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/productions/naive.clj#L11-L11">Source</a></sub></p>

-----
# <a name="fogus.rv.search">fogus.rv.search</a>


Common search-related functions and protocols.




## <a name="fogus.rv.search/GraphSearch">`GraphSearch`</a><a name="fogus.rv.search/GraphSearch"></a>




Functions related to graph-search algorithms.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/search.clj#L4-L12">Source</a></sub></p>

## <a name="fogus.rv.search/HeuristicSearch">`HeuristicSearch`</a><a name="fogus.rv.search/HeuristicSearch"></a>




Function(s) related to heuristic-guided search.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/search.clj#L14-L17">Source</a></sub></p>

## <a name="fogus.rv.search/add-route">`add-route`</a><a name="fogus.rv.search/add-route"></a>
``` clojure

(add-route _ node new-route)
```

Adds a route to a `node` as a seq of nodes. Implementors of this function
    should return an instance of the object implementing this protocol.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/search.clj#L8-L10">Source</a></sub></p>

## <a name="fogus.rv.search/cost-of">`cost-of`</a><a name="fogus.rv.search/cost-of"></a>
``` clojure

(cost-of _ node)
```

Returns the cost to visit `node`.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/search.clj#L6-L6">Source</a></sub></p>

## <a name="fogus.rv.search/estimate-cost">`estimate-cost`</a><a name="fogus.rv.search/estimate-cost"></a>
``` clojure

(estimate-cost _ node goal)
```

Returns an estimated cost of the route from `node` to `goal`.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/search.clj#L16-L17">Source</a></sub></p>

## <a name="fogus.rv.search/goal?">`goal?`</a><a name="fogus.rv.search/goal?"></a>
``` clojure

(goal? _ node)
```

Returns true if `node` satisfies the goal condition.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/search.clj#L12-L12">Source</a></sub></p>

## <a name="fogus.rv.search/neighbors-of">`neighbors-of`</a><a name="fogus.rv.search/neighbors-of"></a>
``` clojure

(neighbors-of _ node)
```

Returns a seq of neighbors of the given `node`.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/search.clj#L7-L7">Source</a></sub></p>

## <a name="fogus.rv.search/route-of">`route-of`</a><a name="fogus.rv.search/route-of"></a>
``` clojure

(route-of _ node)
```

Given a `node`, returns the route associated with it.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/search.clj#L11-L11">Source</a></sub></p>

-----
# <a name="fogus.rv.search.graph">fogus.rv.search.graph</a>


A* search implementation.




## <a name="fogus.rv.search.graph/astar">`astar`</a><a name="fogus.rv.search.graph/astar"></a>
``` clojure

(astar graph start-node goal-node)
```

Implements a lazy A* best-first graph traversal algorithm. Takes a
  `graph` object implementing both of the `fogus.rv.search.GraphSearch`
  and `fogus.rv.search.HeuristicSearch` protocols and a `start-node`
  and `goal-node` describing the bounds of the search. Returns of map
  with keys `:path` mapped to a sequence of nodes from `start-node` to
  `goal-node` and `:cost` describing the cost of the path. This search
  guarantees to return the lowest cost path as long as one exists.
  In the event that there is no path to the `goal-node` the current result
  is undefined.
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/search/graph.clj#L18-L53">Source</a></sub></p>

-----
# <a name="fogus.rv.util">fogus.rv.util</a>






## <a name="fogus.rv.util/cart">`cart`</a><a name="fogus.rv.util/cart"></a>
``` clojure

(cart colls)
```
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/util.clj#L15-L20">Source</a></sub></p>

## <a name="fogus.rv.util/f-by">`f-by`</a><a name="fogus.rv.util/f-by"></a>
``` clojure

(f-by f key coll)
```
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/util.clj#L22-L25">Source</a></sub></p>

## <a name="fogus.rv.util/pairwise-every?">`pairwise-every?`</a><a name="fogus.rv.util/pairwise-every?"></a>
``` clojure

(pairwise-every? pred xs ys)
```
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/util.clj#L27-L28">Source</a></sub></p>

## <a name="fogus.rv.util/positions-of">`positions-of`</a><a name="fogus.rv.util/positions-of"></a>
``` clojure

(positions-of pred & xs)
```
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/util.clj#L30-L31">Source</a></sub></p>

## <a name="fogus.rv.util/process-bindings">`process-bindings`</a><a name="fogus.rv.util/process-bindings"></a>
``` clojure

(process-bindings bindings)
```
<p><sub><a href="https://github.com/fogus/rv/blob/main/src/fogus/rv/util.clj#L11-L13">Source</a></sub></p>
