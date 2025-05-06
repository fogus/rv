# v0.0.9
- Added functions to enable human-in-the-loop learning to `fogus.rv.learn.vs` namespace
  - `(explain [vs example])` returns a structure explaining how the classifier reaches a conclusion
  - `(best-fit [vs example])` returns the best-fit hypothesis for an example 
  - See [API.md](https://github.com/fogus/rv/blob/main/doc/API.md#fogusrvlearnvs) for details
- Cleaned up the docstrings in the `fogus.rv.learn.vs`

# v0.0.8
- Added (fogus.rv.util/pairwise-every? [pred xs ys]) combinator to build Clojure
  style `*-every?` functions of two arguments.
- Added (fogus.rv.util/positions-of [pred & xs]) function that returns the indices
  of values that match a predicate in any number of collections.
- Added fogus.rv.learn.vs containing an implementation of version space learning
  using an inductive candidate-elimination algorithm.
- Added docs/API.md documentation.

# v0.0.7
- Added (fogus.rv.util/f-by [f key coll]) combinator to build Clojure-style `*-by`
  functions.
- Added fogus.rv.search ns containing search-related protocols GraphSearch and
  HeuristicSearch.
- Refined fogus.rv.search.astar/astar to work in terms of the GraphSearch and
  HeuristicSearch protocols. Takes a graph object implementing those protocols
  plus a start node and goal node to find the lowest cost path between them.

# v0.0.6
- Added fogus.rv.constraints ns exposing two functions: satisfy1 and satisfy* that take
  a constraint description containing :variables and :formula mappings and return
  context(s) defining bindings for the variables constrained by formula.
- Added a bibtex file with the references for rv

# v0.0.5
- Added the ability to define entity enumerations by mapping a key to a set. These are
  expanded into [id k v] for each element in the set.
- Changed the name from reinen-vernunft to rv... please stop contacting me about the old name. ;)

# v0.0.4
- Added extra arity to both map->relation and table->kb to take id function, which can
  be used to override the default :kb/id gen or existing key.
- Added a fogus.reinen-vernunft.fuzzy.soundex/encode function that implements the
  American soundex algorithm. https://en.wikipedia.org/wiki/Soundex

# v0.0.3
- Added fogus.reinen-vernunft.datalog ns based on cgrand's 39loc datalog
- Added map->relation and table->kb conversion functions in fogus.reinen-vernunft.core

# v0.0.2
- me.fogus release


