(ns fogus.rv.learn
  "Common learning-related functions and protocols.")

(defprotocol S&G
  (-generalize [lhs rhs])
  (-specialize [lhs neg rhs])
  (-init [basis] [basis arity]))
