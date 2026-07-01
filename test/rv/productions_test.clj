(ns rv.productions-test
  (:require [clojure.test :refer :all]
            [fogus.rv.productions :as p]
            [fogus.rv.productions.naive :as naive]
            [fogus.rv.datalog :as d]))

(def productions
  '[{:antecedent   [[?id   :emergency/type :emergency.type/fire]]
     :consequent [[-1000 :response/type  :response.type/activate-sprinklers]
                  [-1000 :response/to    ?id]]}
    {:antecedent   [[?id   :emergency/type :emergency.type/flood]]
     :consequent [[-1002 :response/type  :response.type/kill-electricity]
                  [-1002 :response/to    ?id]]}])

(def all-facts #{[-50 :emergency/type :emergency.type/fire]
                 [-51 :emergency/type :emergency.type/flood]})

(def KB {:productions productions
         :facts all-facts})

(deftest test-unifications
  (testing "that the context seq is built with a single antecedent pattern."
    (is (= '[{?id -50}]
           (p/unifications '[[?id :emergency/type :emergency.type/fire]]
                              (:facts KB)
                              {})))

    (is (= '[{?id -50} {?id -1000000}]
           (p/unifications '[[?id :emergency/type :emergency.type/fire]]
                              (conj all-facts [-1000000 :emergency/type :emergency.type/fire])
                              {}))))
  
  (testing "that the context is built with multiple antecedent patterns"
    (is (= '[{?id -50, ?rid -5000000}]
           (p/unifications '[[?id :emergency/type :emergency.type/fire]
                                [?rid :response/to ?id]]
                              (conj all-facts [-5000000 :response/to -50])
                              {})))))

(deftest test-select-production
  (testing "that productions are selected as expected"
    (let [first-matching-production (comp first identity)]
      (is (= (first (:productions KB))
             (first (p/select-production first-matching-production KB)))))))

(deftest test-apply-production
  (testing "that a production applied to a KB causes expected assertions"
    (let [first-matching-production (comp first identity)
          [production binds]        (p/select-production first-matching-production KB)]
      (is (= #{[-51 :emergency/type :emergency.type/flood]
               [-50 :emergency/type :emergency.type/fire]
               [-1000 :response/type :response.type/activate-sprinklers]
               [-1000 :response/to -50]}
             (p/apply-production production (:facts KB) binds))))))

(deftest test-step
  (testing "that a single step occurs as expected"
    (let [first-matching-production (comp first identity)
          results (p/step first-matching-production KB)]
      (is (= #{[-1000 -50]}
             (d/q '[:find ?from ?to
                    :where 
                    [?to :emergency/type :emergency.type/fire]
                    [?from :response/to ?to]] 
                  results)))

      (is (= #{[:response.type/activate-sprinklers]}
             (d/q '[:find ?response
                    :where 
                    [_ :response/type ?response]] 
                  results))))))

(deftest test-cycle
  (testing "that the whole run occurs as expected"
    (let [naive-engine (p/make-engine :rule-choice naive/random-choice
                                      :state-xform naive/noop
                                      :quiesce     (naive/qf 256))
          socrates  (p/run naive-engine
                      '{:productions [{:antecedent [[?id :person/name ?n]
                                                    [?id :isa/human? true]]
                                       :consequent [[?id :isa/mortal? true]]}]
                        :facts #{[42 :person/name "Socrates"]
                                 [42 :isa/human? true]}})
          emergency (p/run naive-engine KB)]
      (is (= #{[42 :isa/mortal? true] [42 :isa/human? true] [42 :person/name "Socrates"]}
             socrates))
      (is (= #{[:response.type/kill-electricity] [:response.type/activate-sprinklers]}
             (d/q '[:find ?response
                    :where
                    [_ :response/type ?response]]
                  emergency)))
      (is (= #{[:emergency.type/fire   :response.type/activate-sprinklers]
               [:emergency.type/flood  :response.type/kill-electricity]}
             (d/q '[:find ?problem ?response
                    :where
                    [?id :response/type   ?response]
                    [?id :response/to     ?pid]
                    [?pid :emergency/type ?problem]]
                  emergency))))))

;; A sliding-window transducer of size n.
(defn slide [n]
  (fn [rf]
    (let [buf (volatile! [])]
      (fn
        ([] (rf))
        ([acc] (rf acc))
        ([acc x]
         (let [b (vswap! buf #(vec (take-last n (conj % x))))]
           (if (= n (count b))
             (rf acc b)
             acc)))))))

(defn fixed-point-qf
  ([] nil)
  ([acc] acc)
  ([_ [prev cur]] (if (= prev cur) (reduced cur) cur)))

(deftest test-cycle-fixed-point
  (testing "that run short-circuits once successive states stop changing"
    (let [fixed-point-system (p/make-engine :rule-choice first 
                                            :state-xform (slide 2)
                                            :quiesce fixed-point-qf)
          results (p/run fixed-point-system
                    {:productions '[{:antecedent [[?id :person/name ?n]
                                                  [?id :isa/human? true]]
                                     :consequent [[?id :isa/mortal? true]]}]
                     :facts       #{[42 :person/name "Socrates"]
                                    [42 :isa/human? true]}})]
      (is (= #{[42 :person/name "Socrates"]
               [42 :isa/human? true]
               [42 :isa/mortal? true]}
             results)))))
