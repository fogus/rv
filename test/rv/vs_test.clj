(ns rv.vs-test
  (:require [clojure.test :refer :all]
            [fogus.rv.vs :as vs])
  (:refer-clojure :exclude [*]))

(def ^:const _ vs/?S)
(def ^:const * vs/?G)

(deftest generalization-test
  (is (empty? (vs/-generalize [] [:a :b])))
  (is (= [:a] (vs/-generalize [:a] [:a])))
  (is (= [:a] (vs/-generalize [_] [:a])))
  (is (= [*] (vs/-generalize [:a] [:b])))
  (is (= [:a :b] (vs/-generalize [:a _] [:a :b])))
  (is (= [:b] (vs/-generalize [:b] [_])))
  (is (= [* :b] (vs/-generalize [:a :b] [:z :b]))))

(deftest specialization-test
  (is (= [[:round :blue]] (vs/-specialize [:round *] [:round :yellow] [:round :blue])))
  (is (= [[:round *]] (vs/-specialize [* *] [:square :blue] [:round :blue])))
  (is (= [[:round * *] [* * :small]] (vs/-specialize [* * *] [:square :blue :large] [:round :blue :small]))))

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
    (is (= [[:vocal * *]] G))
    (is (= [[:vocal :jazz *]] S)))

  (let [{:keys [S G]} (-> (vs/-init (vs/arity 11))
                          (#'vs/positive '("rookie"  "P"  "R" "MLB" "Active" "AL" "East" "Orioles" "Active" 19 "Mike"))
                          (#'vs/positive '("veteran" "P"  "R" "MLB" "Active" "AL" "East" "Orioles" "Active" 23 "Jeff"))
                          (#'vs/negative '("ace"     "LF" "L" "MLB" "Active" "NL" "West" "Giants"  "IL"     19 "Jamie")))]
    (is (= G
           [[* "P" * * * * * * * * *]
            [* * "R" * * * * * * * *]
            [* * * * * "AL" * * * * *]
            [* * * * * * "East" * * * *]
            [* * * * * * * "Orioles" * * *]
            [* * * * * * * * "Active" * *]]))
    (is (= S
           [[* "P" "R" "MLB" "Active" "AL" "East" "Orioles" "Active" * *]]
           )))

  (let [{:keys [S G]} (-> (vs/-init (vs/arity 6))
                          (#'vs/positive [:sunny :warm :normal :strong :warm :same])
                          (#'vs/positive [:sunny :warm :high   :strong :warm :same])
                          (#'vs/negative [:rainy :cold :high   :strong :warm :change])
                          (#'vs/positive [:sunny :warm :high   :strong :cool :change]))]
    (is (= G [[:sunny * * * * *] [* :warm * * * *]]))
    (is (= S [[:sunny :warm * :strong * *]]))))

(deftest convergence-test
  (let [{:keys [S G] :as V} (-> (vs/-init (vs/arity 5))
                                (#'vs/positive [:japan "Honda"    :blue  1980 :economy]))]
    (testing "CONVERGENCE TEST STEP 1"
      (is (= G [[* * * * *]]))
      (is (= S [[:japan "Honda" :blue 1980 :economy]]))
      (is (not (vs/converged? V))))

    (testing "CONVERGENCE TEST STEP 2"
      (let [{:keys [S G] :as V} (-> V (#'vs/negative [:japan "Toyota"   :green 1970 :sports]))]
        (is (= G [[* "Honda" * * *]
                  [* * :blue * *]
                  [* * * 1980 *]
                  [* * * * :economy]]))
        (is (= S [[:japan "Honda" :blue 1980 :economy]]))
        (is (not (vs/converged? V)))

        (testing "CONVERGENCE TEST STEP 3"
          (let [{:keys [S G] :as V} (-> V (#'vs/positive [:japan "Toyota"   :blue  1990 :economy]))]
            (is (= G [[* * :blue * *]
                      [* * * * :economy]]))
            (is (= S [[:japan * :blue * :economy]]))
            (is (not (vs/converged? V)))

            (testing "CONVERGENCE TEST STEP 4"
              (let [{:keys [S G] :as V} (-> V (#'vs/negative [:usa   "Chrysler" :red   1980 :economy]))]
                (is (= G [[* * :blue * *]
                          [:japan * * * :economy]]))
                (is (= S [[:japan * :blue * :economy]]))
                (is (not (vs/converged? V)))

                (testing "CONVERGENCE TEST STEP 5 - LAST"
                  (let [{:keys [S G] :as V} (-> V (#'vs/positive [:japan "Honda"    :white 1980 :economy]))]
                    (is (vs/converged? V))))))))))))

