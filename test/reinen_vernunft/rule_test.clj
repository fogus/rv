(ns reinen-vernunft.rule-test
  (:require [clojure.test :refer :all]
            [fogus.reinen-vernunft.rules :as rule]
            [datascript.core :as d]))

(def rules
  '[{:antecedent   [[?id   :emergency/type :emergency.type/fire]]
     :consequent [[-1000 :response/type  :response.type/activate-sprinklers]
                  [-1000 :response/to    ?id]]}
    {:antecedent   [[?id   :emergency/type :emergency.type/flood]]
     :consequent [[-1002 :response/type  :response.type/kill-electricity]
                  [-1002 :response/to    ?id]]}])

(def all-facts #{[-50 :emergency/type :emergency.type/fire]
                 [-51 :emergency/type :emergency.type/flood]})

(def KB {:rules rules
         :facts all-facts})

(deftest test-unifications
  ""
  (testing "that the context seq is built with a single antecedent pattern."
    (is (= '[{?id -50}]
           (rule/unifications '[[?id :emergency/type :emergency.type/fire]]
                              (:facts KB)
                              {})))

    (is (= '[{?id -50} {?id -1000000}]
           (rule/unifications '[[?id :emergency/type :emergency.type/fire]]
                              (conj all-facts [-1000000 :emergency/type :emergency.type/fire])
                              {}))))
  
  (testing "that the context is built with multiple antecedent patterns"
    (is (= '[{?id -50, ?rid -5000000}]
           (rule/unifications '[[?id :emergency/type :emergency.type/fire]
                                [?rid :response/to ?id]]
                              (conj all-facts [-5000000 :response/to -50])
                              {})))))

(deftest test-select-rule
  (testing "that rules are selected as expected"
    (let [first-matching-rule (comp first identity)]
      (is (= (first (:rules KB))
             (first (rule/select-rule first-matching-rule KB)))))))

(deftest test-apply-rule
  (testing "that a rule applied to a KB causes expected assertions"
    (let [first-matching-rule (comp first identity)
          [rule binds]        (rule/select-rule first-matching-rule KB)]
      (is (= #{[-51 :emergency/type :emergency.type/flood]
               [-50 :emergency/type :emergency.type/fire]
               [-1000 :response/type :response.type/activate-sprinklers]
               [-1000 :response/to -50]}
             (rule/apply-rule rule (:facts KB) binds))))))

(deftest test-step
  (testing "that a single step occurs as expected"
    (let [first-matching-rule (comp first identity)
          ])))
