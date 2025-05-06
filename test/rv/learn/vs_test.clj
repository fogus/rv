(ns rv.learn.vs-test
  (:require [clojure.test :refer :all]
            [fogus.rv.learn :as proto]
            [fogus.rv.learn.vs :as vs])
  (:refer-clojure :exclude [*]))

(def ^:const _ vs/?S)
(def ^:const * vs/?G)

(deftest generalization-test
  (is (empty? (proto/-generalize [] [:a :b])))
  (is (= [:a] (proto/-generalize [:a] [:a])))
  (is (= [:a] (proto/-generalize [_] [:a])))
  (is (= [*] (proto/-generalize [:a] [:b])))
  (is (= [:a :b] (proto/-generalize [:a _] [:a :b])))
  (is (= [:b] (proto/-generalize [:b] [_])))
  (is (= [* :b] (proto/-generalize [:a :b] [:z :b]))))

(deftest specialization-test
  (is (= [[:small :blue]] (proto/-specialize [:small *] [:small :yellow] [:small :blue])))
  (is (= [[:small *]] (proto/-specialize [* *] [:mid :blue] [:small :blue])))
  (is (= [[:small * *] [* * :laying]] (proto/-specialize [* * *] [:mid :blue :standing] [:small :blue :laying]))))

(deftest collapsed-test
  (is (vs/collapsed? (-> (proto/-init (vs/arity-vec 2))
                         (vs/refine '(1 2) true)
                         (vs/refine '(:a :b) true)
                         (vs/refine '("c" "d") false)
                         (vs/refine '([] [1]) false)))))

(deftest s&g-tests
  (let [{:keys [S G]} (-> (proto/-init (vs/arity-vec 3))
                          (vs/refine [:vocal :jazz 50] true)
                          (vs/refine [:band :pop  70] false)
                          (vs/refine [:band :pop  80] false)
                          (vs/refine [:solo :jazz 40] false)
                          (vs/refine [:vocal :jazz 50] true)
                          (vs/refine [:orchestra :classical 100] false)
                          (vs/refine [:vocal :jazz 70] true))]
    (is (= [[:vocal * *]] G))
    (is (= [[:vocal :jazz *]] S)))

  (let [{:keys [S G]} (-> (proto/-init (vs/arity-vec 11))
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

  (let [{:keys [S G]} (-> (proto/-init (vs/arity-vec 6))
                          (vs/refine [:sunny :warm :normal :strong :warm :same] true)
                          (vs/refine [:sunny :warm :high   :strong :warm :same] true)
                          (vs/refine [:rainy :cold :high   :strong :warm :change] false)
                          (vs/refine [:sunny :warm :high   :strong :cool :change] true))]
    (is (= G [[:sunny * * * * *] [* :warm * * * *]]))
    (is (= S [[:sunny :warm * :strong * *]]))))

(deftest convergence-test
  (let [example [:japan "Honda"    :blue  1980 :economy]
        {:keys [S G] :as V} (-> (proto/-init (vs/arity-vec 5))
                                (vs/refine example true))]
    (testing "CONVERGENCE TEST STEP 1"
      (is (= G [[* * * * *]]))
      (is (= S [[:japan "Honda" :blue 1980 :economy]]))
      (is (not (vs/converged? V)))
      (is (= :fogus.rv.learn.vs/positive (vs/classify V example))))

    (testing "CONVERGENCE TEST STEP 2"
      (let [example [:japan "Toyota"   :green 1970 :sports]
            {:keys [S G] :as V} (-> V (vs/refine example false))]
        (is (= G [[* "Honda" * * *]
                  [* * :blue * *]
                  [* * * 1980 *]
                  [* * * * :economy]]))
        (is (= S [[:japan "Honda" :blue 1980 :economy]]))
        (is (not (vs/converged? V)))
        (is (= :fogus.rv.learn.vs/negative (vs/classify V example)))

        (testing "CONVERGENCE TEST STEP 3"
          (let [example [:japan "Toyota"   :blue  1990 :economy]
                {:keys [S G] :as V} (-> V (vs/refine example true))]
            (is (= G [[* * :blue * *]
                      [* * * * :economy]]))
            (is (= S [[:japan * :blue * :economy]]))
            (is (not (vs/converged? V)))
            (is (= :fogus.rv.learn.vs/positive (vs/classify V example)))

            (testing "CONVERGENCE TEST STEP 4"
              (let [example [:usa   "Chrysler" :red   1980 :economy]
                    {:keys [S G] :as V} (-> V (vs/refine example false))]
                (is (= G [[* * :blue * *]
                          [:japan * * * :economy]]))
                (is (= S [[:japan * :blue * :economy]]))
                (is (not (vs/converged? V)))
                (is (= :fogus.rv.learn.vs/negative (vs/classify V example)))

                (testing "CONVERGENCE TEST STEP 5 - LAST"
                  (let [example [:japan "Honda"    :white 1980 :economy]]
                    (is (vs/applicable? V example true))
                    (let [{:keys [S G] :as V} (-> V (vs/refine example true))]
                      (is (= :fogus.rv.learn.vs/positive (vs/classify V example)))
                      (is (vs/converged? V)))))))))))))

(deftest applicable?-test
    (let [vs {:S '[[Small Red Soft]]
              :G [[* * *]]}]
      (is (vs/applicable? vs ['Small 'Red 'Soft] true))
      (is (not (vs/applicable? vs ['Large 'Blue 'Hard] false)))
      (is (not (vs/applicable? vs ['Small 'Red 'Soft] false)))))

(deftest consistent?-test
    (let [vs {:S '[[Small Red Soft]]
              :G [[* * *]]}]
      (is (vs/consistent? vs ['Small 'Red 'Soft] true))  
      (is (not (vs/consistent? vs ['Large 'Blue 'Hard] false)))
      (is (not (vs/consistent? vs ['Small 'Red 'Soft] false)))))

(deftest classify-test
  (let [vs {:S [['Small 'Red 'Soft]]
            :G [[* 'Red *]]}]
    (is (= :fogus.rv.learn.vs/positive (vs/classify vs '[Small Red Soft])))
    (is (= :fogus.rv.learn.vs/negative (vs/classify vs '[Large Blue Hard])))
    (is (= :fogus.rv.learn.vs/ambiguous  (vs/classify vs '[Large Red Hard])))
    (is true?  (vs/covers? (:G vs) '[Large Red Hard]))
    (is false? (vs/covers? (:S vs) '[Large Red Hard]))))

(deftest explain-test
  (let [vs (-> (proto/-init (vs/arity-vec 6))
               (vs/refine [:sunny :warm :normal :strong :warm :same] true)
               (vs/refine [:sunny :warm :high   :strong :warm :same] true)
               (vs/refine [:rainy :cold :high   :strong :warm :change] false))
        example [:sunny :warm :high :strong :cool :change]]
    (testing "explain"
      (let [res (vs/explain vs example)]
        (is (= :fogus.rv.learn.vs/ambiguous (:explain/classification res)))
        (is (= [2/3] (map :similarity (:explain/S res))))
        (is (= [1 1 5/6] (map :similarity (:explain/G res))))))
    (is (= 1 (:similarity (vs/best-fit vs example))))))
