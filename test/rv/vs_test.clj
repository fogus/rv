(ns rv.vs-test
  (:require [clojure.test :refer :all]
            [fogus.rv.vs :as vs]))

(deftest generalization-test
  (is (empty? (vs/-generalize [] [:a :b])))
  (is (= [:a] (vs/-generalize [:a] [:a])))
  (is (= [:a] (vs/-generalize [vs/S?] [:a])))
  (is (= [vs/G?] (vs/-generalize [:a] [:b])))
  (is (= [:a :b] (vs/-generalize [:a vs/S?] [:a :b])))
  (is (= [:b] (vs/-generalize [:b] [vs/S?])))
  (is (= [vs/G? :b] (vs/-generalize [:a :b] [:z :b]))))

(deftest specialization-test
  (is (= [[:round :blue]] (vs/-specialize [:round vs/G?] [:round :yellow] [:round :blue])))
  (is (= [[:round vs/G?]] (vs/-specialize [vs/G? vs/G?] [:square :blue] [:round :blue])))
  (is (= [[:round vs/G? vs/G?] [vs/G? vs/G? :small]] (vs/-specialize [vs/G? vs/G? vs/G?] [:square :blue :large] [:round :blue :small]))))

(deftest termination-test
  (is (vs/terminated? (-> (vs/-init '[? ?])
                          (#'vs/positive '("rund" "schwarzrot"))
                          (#'vs/positive '("rund" "schwarzweiss"))
                          (#'vs/negative '("rund" "blau"))))))

(deftest s&g-tests
  (let [{:keys [S G]} (-> (vs/-init '[? ? ?])
                          (#'vs/positive [:vocal :jazz 50])
                          (#'vs/negative [:band :pop  70])
                          (#'vs/negative [:band :pop  80])
                          (#'vs/negative [:solo :jazz 40])
                          (#'vs/positive [:vocal :jazz 50])
                          (#'vs/negative [:orchestra :classical 100])
                          (#'vs/positive [:vocal :jazz 70]))]
    (is (= [[:vocal vs/G? vs/G?]] G))
    (is (= [[:vocal :jazz vs/G?]] S)))

  (let [{:keys [S G]} (-> (vs/-init '[? ? ? ? ? ? ? ? ? ? ?])
                          (#'vs/positive '("neu" "VW" "90-120" "< 2 l" "< 180" "Diesel" "< 6 l" "Minivan" "8" "silber/grau" "< 25000"))
                          (#'vs/positive '("< 2 Jahre" "VW" "90-120" "< 2 l" "< 180" "Diesel" "< 6 l" "Minivan" "8" "grün" "< 20000"))
                          (#'vs/negative '("2-5 Jahre" "Peugeot" "75-90" "< 2 l" "< 180" "Super" "< 8 l" "kompakt" "5" "silber/grau" "< 7500")))]
    (is (= G
           [[vs/G? "VW" vs/G? vs/G? vs/G? vs/G? vs/G? vs/G? vs/G? vs/G? vs/G?]
            [vs/G? vs/G? "90-120" vs/G? vs/G? vs/G? vs/G? vs/G? vs/G? vs/G? vs/G?]
            [vs/G? vs/G? vs/G? vs/G? vs/G? "Diesel" vs/G? vs/G? vs/G? vs/G? vs/G?]
            [vs/G? vs/G? vs/G? vs/G? vs/G? vs/G? "< 6 l" vs/G? vs/G? vs/G? vs/G?]
            [vs/G? vs/G? vs/G? vs/G? vs/G? vs/G? vs/G? "Minivan" vs/G? vs/G? vs/G?]
            [vs/G? vs/G? vs/G? vs/G? vs/G? vs/G? vs/G? vs/G? "8" vs/G? vs/G?]]))
    (is (= S
           [[vs/G? "VW" "90-120" "< 2 l" "< 180" "Diesel" "< 6 l" "Minivan" "8" vs/G? vs/G?]]
           )))
  
)
