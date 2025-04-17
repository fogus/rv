
-----
# <a name="fogus.rv.amb">fogus.rv.amb</a>


Provides an implementation of McCarthy's [`amb`](#fogus.rv.amb/amb) operator with
   binding forms and acceptance test operator.




## <a name="fogus.rv.amb/accept">`accept`</a><a name="fogus.rv.amb/accept"></a>
``` clojure

(accept condition ret)
```
Function.
<p><sub><a href="/blob/main/src/fogus/rv/amb.clj#L14-L18">Source</a></sub></p>

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
<p><sub><a href="/blob/main/src/fogus/rv/amb.clj#L20-L54">Source</a></sub></p>

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
<p><sub><a href="/blob/main/src/fogus/rv/constraints.clj#L47-L66">Source</a></sub></p>

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
<p><sub><a href="/blob/main/src/fogus/rv/constraints.clj#L32-L45">Source</a></sub></p>

-----
# <a name="fogus.rv.core">fogus.rv.core</a>


Most functions in rv work off of one or more of the following root
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
  - Formula: a list describing a predicate expression of mixed LVars and clojure functions




## <a name="fogus.rv.core/->AnyT">`->AnyT`</a><a name="fogus.rv.core/->AnyT"></a>
``` clojure

(->AnyT)
```
<p><sub><a href="/blob/main/src/fogus/rv/core.clj#L43-L46">Source</a></sub></p>

## <a name="fogus.rv.core/->AskT">`->AskT`</a><a name="fogus.rv.core/->AskT"></a>
``` clojure

(->AskT)
```
<p><sub><a href="/blob/main/src/fogus/rv/core.clj#L48-L51">Source</a></sub></p>

## <a name="fogus.rv.core/->IgnoreT">`->IgnoreT`</a><a name="fogus.rv.core/->IgnoreT"></a>
``` clojure

(->IgnoreT)
```
<p><sub><a href="/blob/main/src/fogus/rv/core.clj#L38-L41">Source</a></sub></p>

## <a name="fogus.rv.core/AnyT">`AnyT`</a><a name="fogus.rv.core/AnyT"></a>



<p><sub><a href="/blob/main/src/fogus/rv/core.clj#L43-L46">Source</a></sub></p>

## <a name="fogus.rv.core/AskT">`AskT`</a><a name="fogus.rv.core/AskT"></a>



<p><sub><a href="/blob/main/src/fogus/rv/core.clj#L48-L51">Source</a></sub></p>

## <a name="fogus.rv.core/ID_KEY">`ID_KEY`</a><a name="fogus.rv.core/ID_KEY"></a>



<p><sub><a href="/blob/main/src/fogus/rv/core.clj#L62-L62">Source</a></sub></p>

## <a name="fogus.rv.core/IgnoreT">`IgnoreT`</a><a name="fogus.rv.core/IgnoreT"></a>



<p><sub><a href="/blob/main/src/fogus/rv/core.clj#L38-L41">Source</a></sub></p>

## <a name="fogus.rv.core/lv?">`lv?`</a><a name="fogus.rv.core/lv?"></a>
``` clojure

(lv? %1)
```
<p><sub><a href="/blob/main/src/fogus/rv/core.clj#L36-L36">Source</a></sub></p>

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
<p><sub><a href="/blob/main/src/fogus/rv/core.clj#L75-L97">Source</a></sub></p>

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
<p><sub><a href="/blob/main/src/fogus/rv/core.clj#L99-L111">Source</a></sub></p>

-----
# <a name="fogus.rv.datalog">fogus.rv.datalog</a>


A minimal implementation of Datalog.




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
<p><sub><a href="/blob/main/src/fogus/rv/datalog.clj#L91-L146">Source</a></sub></p>

## <a name="fogus.rv.datalog/query->map">`query->map`</a><a name="fogus.rv.datalog/query->map"></a>
``` clojure

(query->map query)
```

Accepts the vector form of a Datalog query and outputs a map
  of the component sections as keyword->seq mappings.
<p><sub><a href="/blob/main/src/fogus/rv/datalog.clj#L80-L89">Source</a></sub></p>

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
<p><sub><a href="/blob/main/src/fogus/rv/fuzzy/soundex.clj#L46-L62">Source</a></sub></p>

-----
# <a name="fogus.rv.impl.unification">fogus.rv.impl.unification</a>


Provides internal unification functions.
  DO NOT USE THIS NS.
  There is no guarantee that it will remain stable or at all.




## <a name="fogus.rv.impl.unification/subst">`subst`</a><a name="fogus.rv.impl.unification/subst"></a>



<p><sub><a href="/blob/main/src/fogus/rv/impl/unification.clj#L16-L16">Source</a></sub></p>

-----
# <a name="fogus.rv.learn">fogus.rv.learn</a>


Common learning-related functions and protocols.




## <a name="fogus.rv.learn/-generalize">`-generalize`</a><a name="fogus.rv.learn/-generalize"></a>
``` clojure

(-generalize lhs rhs)
```
<p><sub><a href="/blob/main/src/fogus/rv/learn.clj#L5-L5">Source</a></sub></p>

## <a name="fogus.rv.learn/-init">`-init`</a><a name="fogus.rv.learn/-init"></a>
``` clojure

(-init basis)
(-init basis arity)
```
<p><sub><a href="/blob/main/src/fogus/rv/learn.clj#L7-L7">Source</a></sub></p>

## <a name="fogus.rv.learn/-specialize">`-specialize`</a><a name="fogus.rv.learn/-specialize"></a>
``` clojure

(-specialize lhs neg rhs)
```
<p><sub><a href="/blob/main/src/fogus/rv/learn.clj#L6-L6">Source</a></sub></p>

## <a name="fogus.rv.learn/S&G">`S&G`</a><a name="fogus.rv.learn/S&G"></a>



<p><sub><a href="/blob/main/src/fogus/rv/learn.clj#L4-L7">Source</a></sub></p>

-----
# <a name="fogus.rv.learn.vs">fogus.rv.learn.vs</a>






## <a name="fogus.rv.learn.vs/??">`??`</a><a name="fogus.rv.learn.vs/??"></a>



<p><sub><a href="/blob/main/src/fogus/rv/learn/vs.clj#L16-L16">Source</a></sub></p>

## <a name="fogus.rv.learn.vs/?G">`?G`</a><a name="fogus.rv.learn.vs/?G"></a>



<p><sub><a href="/blob/main/src/fogus/rv/learn/vs.clj#L15-L15">Source</a></sub></p>

## <a name="fogus.rv.learn.vs/?S">`?S`</a><a name="fogus.rv.learn.vs/?S"></a>



<p><sub><a href="/blob/main/src/fogus/rv/learn/vs.clj#L14-L14">Source</a></sub></p>

## <a name="fogus.rv.learn.vs/applicable?">`applicable?`</a><a name="fogus.rv.learn.vs/applicable?"></a>
``` clojure

(applicable? vs example label)
```

Returns true if the version space can accommodate this example (i.e., at least one hypothesis is consistent).
<p><sub><a href="/blob/main/src/fogus/rv/learn/vs.clj#L141-L152">Source</a></sub></p>

## <a name="fogus.rv.learn.vs/arity">`arity`</a><a name="fogus.rv.learn.vs/arity"></a>
``` clojure

(arity n)
```

Returns a vector template for arity n.
<p><sub><a href="/blob/main/src/fogus/rv/learn/vs.clj#L93-L96">Source</a></sub></p>

## <a name="fogus.rv.learn.vs/classify">`classify`</a><a name="fogus.rv.learn.vs/classify"></a>
``` clojure

(classify vs example)
```

Attempts to classify an example using the current version space.
   Returns :positive, :negative, or :unknown if G and S disagree.
<p><sub><a href="/blob/main/src/fogus/rv/learn/vs.clj#L174-L189">Source</a></sub></p>

## <a name="fogus.rv.learn.vs/collapsed?">`collapsed?`</a><a name="fogus.rv.learn.vs/collapsed?"></a>
``` clojure

(collapsed? vs)
(collapsed? g s)
```

Returns if a version space vs or a most-general hypothesis g and a
  most-specific hypothesis s have collapsed. That is, training has
  caused the hypotheses to become inconsistent, making further classification
  impossible.
<p><sub><a href="/blob/main/src/fogus/rv/learn/vs.clj#L98-L105">Source</a></sub></p>

## <a name="fogus.rv.learn.vs/consistent?">`consistent?`</a><a name="fogus.rv.learn.vs/consistent?"></a>
``` clojure

(consistent? vs example label)
```

Returns true if all hypotheses in the version space are consistent with the labeled example.
<p><sub><a href="/blob/main/src/fogus/rv/learn/vs.clj#L129-L139">Source</a></sub></p>

## <a name="fogus.rv.learn.vs/converged?">`converged?`</a><a name="fogus.rv.learn.vs/converged?"></a>
``` clojure

(converged? vs)
(converged? g s)
```

Returns if a version space vs or a most-general hypothesis g and a
  most-specific hypothesis s have converged. That is, training has
  caused the hypotheses to ground to a single legal case.
<p><sub><a href="/blob/main/src/fogus/rv/learn/vs.clj#L107-L113">Source</a></sub></p>

## <a name="fogus.rv.learn.vs/refine">`refine`</a><a name="fogus.rv.learn.vs/refine"></a>
``` clojure

(refine vs example)
(refine vs example positive?)
```

Given a version space vs and an example, returns a new version space
  with hypotheses adjusted according to the given example's elements and
  its classification. An example is classified by attaching a metadata mapping
  :positive? -> boolean or by passing a boolean as the last argument. The
  explicit classification argument will always dominate the metadata
  classification.
<p><sub><a href="/blob/main/src/fogus/rv/learn/vs.clj#L115-L127">Source</a></sub></p>

-----
# <a name="fogus.rv.productions">fogus.rv.productions</a>


The simplest possible production rules system that uses a set
   of EAV tuples as its knowledge base.




## <a name="fogus.rv.productions/apply-production">`apply-production`</a><a name="fogus.rv.productions/apply-production"></a>
``` clojure

(apply-production production facts context)
```
<p><sub><a href="/blob/main/src/fogus/rv/productions.clj#L87-L90">Source</a></sub></p>

## <a name="fogus.rv.productions/cycle">`cycle`</a><a name="fogus.rv.productions/cycle"></a>
``` clojure

(cycle qf kb)
```

Feeds the results of states into a function qf that is responsible for 
   detecting when production firings have stopped and returns an augmented
   fact set.
<p><sub><a href="/blob/main/src/fogus/rv/productions.clj#L110-L115">Source</a></sub></p>

## <a name="fogus.rv.productions/naive-qf">`naive-qf`</a><a name="fogus.rv.productions/naive-qf"></a>
``` clojure

(naive-qf states)
```

Takes the last environment in a long sequence of states in the hope that 
   the sequence was long enough that all of the productions fired in creating it.
<p><sub><a href="/blob/main/src/fogus/rv/productions.clj#L119-L123">Source</a></sub></p>

## <a name="fogus.rv.productions/select-production">`select-production`</a><a name="fogus.rv.productions/select-production"></a>
``` clojure

(select-production selection-strategy {:keys [productions facts]})
```

Builds a sequence of bindings paired with each production and then uses a selection 
   function to execute one of the productions that matched.
<p><sub><a href="/blob/main/src/fogus/rv/productions.clj#L74-L82">Source</a></sub></p>

## <a name="fogus.rv.productions/states">`states`</a><a name="fogus.rv.productions/states"></a>
``` clojure

(states kb)
```

Will apply the result of one production firing to the fact base and feed 
   the result forward into the next firing.
<p><sub><a href="/blob/main/src/fogus/rv/productions.clj#L103-L108">Source</a></sub></p>

## <a name="fogus.rv.productions/step">`step`</a><a name="fogus.rv.productions/step"></a>
``` clojure

(step kb)
(step choice-fn kb)
```

Takes a set of productions and facts and returns a new fact base based on the application of single production.
<p><sub><a href="/blob/main/src/fogus/rv/productions.clj#L94-L99">Source</a></sub></p>

## <a name="fogus.rv.productions/unifications">`unifications`</a><a name="fogus.rv.productions/unifications"></a>
``` clojure

(unifications [clause & more :as clauses] facts context)
```

Walks through all of the clauses in an implied antecedent and matches 
   each against every fact provided.  Returns a seq of contexts representing
   all of the bindings established by the antecedent unifications across all
   facts provided.
<p><sub><a href="/blob/main/src/fogus/rv/productions.clj#L61-L70">Source</a></sub></p>

-----
# <a name="fogus.rv.search">fogus.rv.search</a>


Common search-related functions and protocols.




## <a name="fogus.rv.search/GraphSearch">`GraphSearch`</a><a name="fogus.rv.search/GraphSearch"></a>




Functions related to graph-search algorithms.
<p><sub><a href="/blob/main/src/fogus/rv/search.clj#L4-L11">Source</a></sub></p>

## <a name="fogus.rv.search/HeuristicSearch">`HeuristicSearch`</a><a name="fogus.rv.search/HeuristicSearch"></a>




Function(s) related to heuristic-guided search.
<p><sub><a href="/blob/main/src/fogus/rv/search.clj#L13-L16">Source</a></sub></p>

## <a name="fogus.rv.search/add-route">`add-route`</a><a name="fogus.rv.search/add-route"></a>
``` clojure

(add-route _ node new-route)
```

Adds a route to a `node` as a seq of nodes. Implementors of this function
    should return an instance of the object implementing this protocol.
<p><sub><a href="/blob/main/src/fogus/rv/search.clj#L8-L10">Source</a></sub></p>

## <a name="fogus.rv.search/cost-of">`cost-of`</a><a name="fogus.rv.search/cost-of"></a>
``` clojure

(cost-of _ node)
```

Returns the cost to visit `node`.
<p><sub><a href="/blob/main/src/fogus/rv/search.clj#L6-L6">Source</a></sub></p>

## <a name="fogus.rv.search/estimate-cost">`estimate-cost`</a><a name="fogus.rv.search/estimate-cost"></a>
``` clojure

(estimate-cost _ node goal)
```

Returns an estimated cost of the route from `node` to `goal`.
<p><sub><a href="/blob/main/src/fogus/rv/search.clj#L15-L16">Source</a></sub></p>

## <a name="fogus.rv.search/neighbors-of">`neighbors-of`</a><a name="fogus.rv.search/neighbors-of"></a>
``` clojure

(neighbors-of _ node)
```

Returns a seq of neighbors of the given `node`.
<p><sub><a href="/blob/main/src/fogus/rv/search.clj#L7-L7">Source</a></sub></p>

## <a name="fogus.rv.search/route-of">`route-of`</a><a name="fogus.rv.search/route-of"></a>
``` clojure

(route-of _ node)
```

Given a `node`, returns the route associated with it.
<p><sub><a href="/blob/main/src/fogus/rv/search.clj#L11-L11">Source</a></sub></p>

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
<p><sub><a href="/blob/main/src/fogus/rv/search/graph.clj#L18-L53">Source</a></sub></p>

-----
# <a name="fogus.rv.util">fogus.rv.util</a>






## <a name="fogus.rv.util/cart">`cart`</a><a name="fogus.rv.util/cart"></a>
``` clojure

(cart colls)
```
<p><sub><a href="/blob/main/src/fogus/rv/util.clj#L15-L20">Source</a></sub></p>

## <a name="fogus.rv.util/f-by">`f-by`</a><a name="fogus.rv.util/f-by"></a>
``` clojure

(f-by f key coll)
```
<p><sub><a href="/blob/main/src/fogus/rv/util.clj#L22-L25">Source</a></sub></p>

## <a name="fogus.rv.util/pairwise-every?">`pairwise-every?`</a><a name="fogus.rv.util/pairwise-every?"></a>
``` clojure

(pairwise-every? pred xs ys)
```
<p><sub><a href="/blob/main/src/fogus/rv/util.clj#L27-L28">Source</a></sub></p>

## <a name="fogus.rv.util/positions-of">`positions-of`</a><a name="fogus.rv.util/positions-of"></a>
``` clojure

(positions-of pred & xs)
```
<p><sub><a href="/blob/main/src/fogus/rv/util.clj#L30-L31">Source</a></sub></p>

## <a name="fogus.rv.util/process-bindings">`process-bindings`</a><a name="fogus.rv.util/process-bindings"></a>
``` clojure

(process-bindings bindings)
```
<p><sub><a href="/blob/main/src/fogus/rv/util.clj#L11-L13">Source</a></sub></p>
