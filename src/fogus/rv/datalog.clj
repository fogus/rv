(ns fogus.rv.datalog
  "A minimal implementation of Datalog.")

;;  Implementation is a modified version of Christophe Grand's 39loc Datalog implementation
;;  adding more operators and allowing a Datomic-style query function. To
;;  understand the core implementation I recommend reading Christophe's posts:
;;
;;  - https://buttondown.com/tensegritics-curiosities/archive/writing-the-worst-datalog-ever-in-26loc
;;  - https://buttondown.com/tensegritics-curiosities/archive/half-dumb-datalog-in-30-loc/
;;  - https://buttondown.com/tensegritics-curiosities/archive/restrained-datalog-in-39loc/
;;
;;  While this implementation may diverge over time, the articles above are
;;  a master class in simplicity and emergent behavior.

(defn- lookup-op [op]
  (case op
    not= not=
    = =
    < <
    > >
    <= <=
    >= >=))

(defn- constrain [env [op & args]]
  (let [args (map #(let [v (env % %)] (if (set? v) % v)) args)]
    (if-some [free-var (->> args (filter symbol?) first)]
      (update env free-var (fnil conj #{}) (cons op args))
      (when (apply (lookup-op op) args) env))))

(defn- bind [env p v]
  (let [p-or-v (env p p)]
    (cond
      (= p '_) env
      (= p-or-v v) env
      (symbol? p-or-v) (assoc env p v)
      (set? p-or-v) (reduce constrain (assoc env p v) p-or-v))))

(defn- match [pattern fact env]
  (assert (= (count pattern) (count fact) 3) (str "[e a v] pattern expected, got: " pattern " - " fact))
  (reduce (fn [env [p v]] (or (bind env p v) (reduced nil)))
          env
          (map vector pattern fact)))

(defn- match-patterns [patterns dfacts facts]
  (reduce
    (fn [[envs denvs] pattern]
      (if (seq? pattern)
        [(->> @envs (keep #(constrain % pattern)) set delay)
         (->> denvs (keep #(constrain % pattern)) set)]
        [(-> #{} (into (for [fact facts env @envs] (match pattern fact env))) (disj nil) delay)
         (-> #{}
           (into (for [fact facts env denvs] (match pattern fact env)))
           (into (for [fact dfacts env denvs] (match pattern fact env)))
           (into (for [fact dfacts env @envs] (match pattern fact env)))
           (disj nil))]))
    [(delay #{{}}) #{}] patterns))

(defn- match-rule [dfacts facts [head & patterns]]
  (for [env (second (match-patterns patterns dfacts facts))]
    (into [] (map #(env % %)) head)))

(defn- saturate [facts rules]
  (loop [dfacts facts, facts #{}]
    (let [facts' (into facts dfacts)
          dfacts' (into #{} (comp (mapcat #(match-rule dfacts facts %)) (remove facts')) rules)]
      (cond->> facts' (seq dfacts') (recur dfacts')))))

(defn- q*
  "Underlying query impl.

   - facts: a set of tuples
   - query: a seq of ([..binds..] tuple*)
   - rules: a seq of ((head-tuple tuple*)*)

  [..binds..] can contain lvars and grounds to form output tuples
  "
  [facts query rules]
  (-> facts (saturate rules) (match-rule #{} query) set))

(def linked-list-rules '[([?h :cell/linked ?t] [?h :cell/head _] [?t :cell/head _] (= ?h ?t))
                         ([?h :cell/linked ?t] [?h :cell/linked ?x] [?x :cell/tail ?t])])

(defn query->map
  "Accepts the vector form of a Datalog query and outputs a map
  of the component sections as keyword->seq mappings."
  [query]
  (letfn [(q->pairs [qq]
            (let [q (partition-by keyword? qq)]
              (map #(conj (vec %1) %2)
                   (take-nth 2 q)
                   (take-nth 2 (rest q)))))]
    (into {} (q->pairs query))))

(defn q
  "Queries a knowledge base or a set of relations given a vector
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
  derived from previous rule applications."
  ([query kb] (q query kb '()))
  ([query kb rules]
   (let [{:keys [find where]} (query->map query)
         find (if (vector? (first find)) (first find) find)
         facts (cond (map? kb) (:facts kb)
                     (set? kb) kb
                     :else (throw (ex-info "Cannot derive facts from KB"
                                           {:kb/type (type kb)})))]
     (q* facts
         (list* find where)
         rules))))

(comment
  (def fkb
    {:facts
     #{[-1002 :response/to -51]
       [-51 :emergency/type :emergency.type/flood]
       [-50 :emergency/type :emergency.type/fire]
       [-1002 :response/type :response.type/kill-electricity]
       [-1000 :response/to -50]
       [-1000 :response/type :response.type/activate-sprinklers]}})

  (q* fkb
     '([?response] [_ :response/type ?response])
     '())

  (q* fkb
     '([?problem ?response]
       [?id :response/type   ?response]
       [?id :response/to     ?pid]
       [?pid :emergency/type ?problem])
     '())

  (q '[:find ?problem ?response
       :where
       [?id :response/type   ?response]
       [?id :response/to     ?pid]
       [?pid :emergency/type ?problem]]
     fkb)

  

  (query->map '[:find ?problem ?response
                :where
                [?id :response/type   ?response]
                [?id :response/to     ?pid]
                [?pid :emergency/type ?problem]])

  (query->map '[:find [?response :response/to ?problem]
                :where
                [?id :response/type   ?response]
                [?id :response/to     ?pid]
                [?pid :emergency/type ?problem]])

  (def vkb #{[:primes :num/primes 100]
             [100 :sequence/items 101]
             [100 :sequence/indexed? true]
             [101 :cell/head 1] [101 :cell/i 0] [101 :cell/tail 103]
             [103 :cell/head 2] [103 :cell/i 1] [103 :cell/tail 105]
             [105 :cell/head 3] [105 :cell/i 2] [105 :cell/tail 107]
             [107 :cell/head 5] [107 :cell/i 3] [107 :cell/tail 109]
             [109 :cell/head 7] [109 :cell/i 4]})

  (q '[:find ?h2
       :where
       [:primes :num/primes ?primes]
       [?primes :sequence/items ?e]
       [?e :cell/head _]
       [?e :cell/linked ?t]
       [?t :cell/head ?h2]]
     vkb
     linked-list-rules
     )

  (q '[:find ?h2
       :where
       [?e :cell/head 1]
       [?e :cell/linked ?t]
       [?t :cell/head ?h2]]
     vkb
     rvrules
     )

  (q '[:find ?s ?h
       :where
       [?s :sequence/items ?h]
       [?h :cell/i 0]]
     vkb
     ;;vrules
     )

    (q '[:find ?s ?h
       :where
       [?s :sequence/items ?h]
       [?h :cell/i 0]]
     vkb
     ;;vrules
     )
  
  )
