(ns rv.learn.vs-test
  (:require [clojure.test :refer :all]
            [fogus.rv.learn.vs :as vs])
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
  (is (= [[:small :blue]] (vs/-specialize [:small *] [:small :yellow] [:small :blue])))
  (is (= [[:small *]] (vs/-specialize [* *] [:mid :blue] [:small :blue])))
  (is (= [[:small * *] [* * :laying]] (vs/-specialize [* * *] [:mid :blue :standing] [:small :blue :laying]))))

(deftest termination-test
  (is (vs/terminated? (-> (vs/-init (vs/arity 2))
                          (vs/refine '(1 2) true)
                          (vs/refine '(:a :b) true)
                          (vs/refine '("c" "d") false)
                          (vs/refine '([] [1]) false)))))

(deftest s&g-tests
  (let [{:keys [S G]} (-> (vs/-init (vs/arity 3))
                          (vs/refine [:vocal :jazz 50] true)
                          (vs/refine [:band :pop  70] false)
                          (vs/refine [:band :pop  80] false)
                          (vs/refine [:solo :jazz 40] false)
                          (vs/refine [:vocal :jazz 50] true)
                          (vs/refine [:orchestra :classical 100] false)
                          (vs/refine [:vocal :jazz 70] true))]
    (is (= [[:vocal * *]] G))
    (is (= [[:vocal :jazz *]] S)))

  (let [{:keys [S G]} (-> (vs/-init (vs/arity 11))
                          (vs/refine '("rookie"  "P"  "R" "MLB" "Active" "AL" "East" "Orioles" "Active" 19 "Mike") true)
                          (vs/refine '("veteran" "P"  "R" "MLB" "Active" "AL" "East" "Orioles" "Active" 23 "Jeff") true)
                          (vs/refine '("ace"     "LF" "L" "MLB" "Active" "NL" "West" "Giants"  "IL"     19 "Jamie") false))]
    (is (= G
           [[* "P" * * * * * * * * *]
            [* * "R" * * * * * * * *]
            [* * * * * "AL" * * * * *]
            [* * * * * * "East" * * * *]
            [* * * * * * * "Orioles" * * *]
            [* * * * * * * * "Active" * *]]))
    (is (= S
           [[* "P" "R" "MLB" "Active" "AL" "East" "Orioles" "Active" * *]])))

  (let [{:keys [S G]} (-> (vs/-init (vs/arity 6))
                          (vs/refine [:sunny :warm :normal :strong :warm :same] true)
                          (vs/refine [:sunny :warm :high   :strong :warm :same] true)
                          (vs/refine [:rainy :cold :high   :strong :warm :change] false)
                          (vs/refine [:sunny :warm :high   :strong :cool :change] true))]
    (is (= G [[:sunny * * * * *] [* :warm * * * *]]))
    (is (= S [[:sunny :warm * :strong * *]]))))

(deftest convergence-test
  (let [{:keys [S G] :as V} (-> (vs/-init (vs/arity 5))
                                (vs/refine [:japan "Honda"    :blue  1980 :economy] true))]
    (testing "CONVERGENCE TEST STEP 1"
      (is (= G [[* * * * *]]))
      (is (= S [[:japan "Honda" :blue 1980 :economy]]))
      (is (not (vs/converged? V))))

    (testing "CONVERGENCE TEST STEP 2"
      (let [{:keys [S G] :as V} (-> V (vs/refine [:japan "Toyota"   :green 1970 :sports] false))]
        (is (= G [[* "Honda" * * *]
                  [* * :blue * *]
                  [* * * 1980 *]
                  [* * * * :economy]]))
        (is (= S [[:japan "Honda" :blue 1980 :economy]]))
        (is (not (vs/converged? V)))

        (testing "CONVERGENCE TEST STEP 3"
          (let [{:keys [S G] :as V} (-> V (vs/refine [:japan "Toyota"   :blue  1990 :economy] true))]
            (is (= G [[* * :blue * *]
                      [* * * * :economy]]))
            (is (= S [[:japan * :blue * :economy]]))
            (is (not (vs/converged? V)))

            (testing "CONVERGENCE TEST STEP 4"
              (let [{:keys [S G] :as V} (-> V (vs/refine [:usa   "Chrysler" :red   1980 :economy] false))]
                (is (= G [[* * :blue * *]
                          [:japan * * * :economy]]))
                (is (= S [[:japan * :blue * :economy]]))
                (is (not (vs/converged? V)))

                (testing "CONVERGENCE TEST STEP 5 - LAST"
                  (let [{:keys [S G] :as V} (-> V (vs/refine [:japan "Honda"    :white 1980 :economy] true))]
                    (is (vs/converged? V))))))))))))

