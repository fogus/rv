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
                          (#'vs/positive '(3 4))
                          (#'vs/negative '(5 6))))))

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
                          (#'vs/positive '("neu" "VW" "90-120" "< 2 l" "< 180" "Diesel" "< 6 l" "Minivan" "8" "silber/grau" "< 25000"))
                          (#'vs/positive '("< 2 Jahre" "VW" "90-120" "< 2 l" "< 180" "Diesel" "< 6 l" "Minivan" "8" "grün" "< 20000"))
                          (#'vs/negative '("2-5 Jahre" "Peugeot" "75-90" "< 2 l" "< 180" "Super" "< 8 l" "kompakt" "5" "silber/grau" "< 7500")))]
    (is (= G
           [[vs/?G "VW" vs/?G vs/?G vs/?G vs/?G vs/?G vs/?G vs/?G vs/?G vs/?G]
            [vs/?G vs/?G "90-120" vs/?G vs/?G vs/?G vs/?G vs/?G vs/?G vs/?G vs/?G]
            [vs/?G vs/?G vs/?G vs/?G vs/?G "Diesel" vs/?G vs/?G vs/?G vs/?G vs/?G]
            [vs/?G vs/?G vs/?G vs/?G vs/?G vs/?G "< 6 l" vs/?G vs/?G vs/?G vs/?G]
            [vs/?G vs/?G vs/?G vs/?G vs/?G vs/?G vs/?G "Minivan" vs/?G vs/?G vs/?G]
            [vs/?G vs/?G vs/?G vs/?G vs/?G vs/?G vs/?G vs/?G "8" vs/?G vs/?G]]))
    (is (= S
           [[vs/?G "VW" "90-120" "< 2 l" "< 180" "Diesel" "< 6 l" "Minivan" "8" vs/?G vs/?G]]
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
