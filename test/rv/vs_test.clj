(ns rv.vs-test
  (:require [clojure.test :refer :all]
            [fogus.rv.vs :as vs]))

(deftest generalization-test
  (is (empty? (vs/-generalize [] [:a :b])))
  (is (= [:a] (vs/-generalize [:a] [:a])))
  (is (= [:a] (vs/-generalize [vs/?S] [:a])))
  (is (= [vs/?G] (vs/-generalize [:a] [:b])))
  (is (= [:a :b] (vs/-generalize [:a vs/?S] [:a :b])))
  (is (= [:b] (vs/-generalize [:b] [vs/?S])))
  (is (= [vs/?G :b] (vs/-generalize [:a :b] [:z :b]))))

(deftest specialization-test
  (is (= [[:round :blue]] (vs/-specialize [:round vs/?G] [:round :yellow] [:round :blue])))
  (is (= [[:round vs/?G]] (vs/-specialize [vs/?G vs/?G] [:square :blue] [:round :blue])))
  (is (= [[:round vs/?G vs/?G] [vs/?G vs/?G :small]] (vs/-specialize [vs/?G vs/?G vs/?G] [:square :blue :large] [:round :blue :small]))))

(deftest termination-test
  (is (vs/terminated? (-> (vs/-init (vs/arity 2))
                          (#'vs/positive '(1 2))
                          (#'vs/positive '(:a :b))
                          (#'vs/negative '("c" "d"))
                          (#'vs/negative '([] [1]))))))

(deftest s&g-tests
  (let [{:keys [S G]} (-> (vs/-init (vs/arity 3))
                          (#'vs/positive [:vocal :jazz 50])
                          (#'vs/negative [:band :pop  70])
                          (#'vs/negative [:band :pop  80])
                          (#'vs/negative [:solo :jazz 40])
                          (#'vs/positive [:vocal :jazz 50])
                          (#'vs/negative [:orchestra :classical 100])
                          (#'vs/positive [:vocal :jazz 70]))]
    (is (= [[:vocal vs/?G vs/?G]] G))
    (is (= [[:vocal :jazz vs/?G]] S)))

  (let [{:keys [S G]} (-> (vs/-init (vs/arity 11))
                          (#'vs/positive '("rookie"  "P"  "R" "MLB" "Active" "AL" "East" "Orioles" "Active" 19 "Mike"))
                          (#'vs/positive '("veteran" "P"  "R" "MLB" "Active" "AL" "East" "Orioles" "Active" 23 "Jeff"))
                          (#'vs/negative '("ace"     "LF" "L" "MLB" "Active" "NL" "West" "Giants"  "IL"     19 "Jamie")))]
    (is (= G
           [[vs/?G "P" vs/?G vs/?G vs/?G vs/?G vs/?G vs/?G vs/?G vs/?G vs/?G]
            [vs/?G vs/?G "R" vs/?G vs/?G vs/?G vs/?G vs/?G vs/?G vs/?G vs/?G]
            [vs/?G vs/?G vs/?G vs/?G vs/?G "AL" vs/?G vs/?G vs/?G vs/?G vs/?G]
            [vs/?G vs/?G vs/?G vs/?G vs/?G vs/?G "East" vs/?G vs/?G vs/?G vs/?G]
            [vs/?G vs/?G vs/?G vs/?G vs/?G vs/?G vs/?G "Orioles" vs/?G vs/?G vs/?G]
            [vs/?G vs/?G vs/?G vs/?G vs/?G vs/?G vs/?G vs/?G "Active" vs/?G vs/?G]]))
    (is (= S
           [[vs/?G "P" "R" "MLB" "Active" "AL" "East" "Orioles" "Active" vs/?G vs/?G]]
           )))

  (let [{:keys [S G]} (-> (vs/-init (vs/arity 6))
                          (#'vs/positive [:sunny :warm :normal :strong :warm :same])
                          (#'vs/positive [:sunny :warm :high   :strong :warm :same])
                          (#'vs/negative [:rainy :cold :high   :strong :warm :change])
                          (#'vs/positive [:sunny :warm :high   :strong :cool :change]))]
    (is (= G [[:sunny vs/?G vs/?G vs/?G vs/?G vs/?G] [vs/?G :warm vs/?G vs/?G vs/?G vs/?G]]))
    (is (= S [[:sunny :warm vs/?G :strong vs/?G vs/?G]])))

  (let [{:keys [S G]} (-> (vs/-init (vs/arity 5))
                          (#'vs/positive [:japan "Honda"    :blue  1980 :economy])
                          (#'vs/negative [:japan "Toyota"   :green 1970 :sports])
                          (#'vs/positive [:japan "Toyota"   :blue  1990 :economy])
                          (#'vs/negative [:usa   "Chrysler" :red   1980 :economy])
                          (#'vs/positive [:japan "Honda"    :white 1980 :economy]))]
    (is (= G [[:japan vs/?G vs/?G vs/?G :economy]]))
    (is (= G S)))
  
)
