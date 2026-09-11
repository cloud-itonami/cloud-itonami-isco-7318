(ns handicraft.governor-test
  (:require [clojure.test :refer [deftest is testing]]
            [handicraft.store :as store]
            [handicraft.governor :as governor]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-client! st {:client-id "client-1" :name "Kobo Craft"})
    (store/register-order! st {:order-id "O-1" :client-id "client-1"
                               :name "leather-satchel"
                               :material-stock {"leather-hide" 5 "brass-buckle" 2}
                               :required-spec-items #{"body" "strap" "buckle"}})
    st))

(defn- craft [material qty]
  {:op :approve-craft-step :effect :propose :order-id "O-1"
   :material material :quantity qty :confidence 0.9 :stake :low})

(defn- deliver [items]
  {:op :approve-delivery :effect :propose :order-id "O-1"
   :delivered-items items :confidence 0.9 :stake :low})

(def ^:private req {:client-id "client-1"})
(def ^:private all-items #{"body" "strap" "buckle"})

(deftest ok-craft-within-stock
  (let [st (fresh-store)
        v (governor/check req {} (craft "leather-hide" 3) st)]
    (is (:ok? v))))

(deftest ok-craft-at-exact-stock
  (testing "using exactly the on-hand quantity is within margin"
    (let [st (fresh-store)
          v (governor/check req {} (craft "brass-buckle" 2) st)]
      (is (:ok? v)))))

(deftest hard-on-craft-stock-exceeded
  (testing "you cannot craft with material you don't have"
    (let [st (fresh-store)
          v (governor/check req {} (assoc (craft "leather-hide" 10) :confidence 0.99) st)]
      (is (:hard? v))
      (is (some #(= :stock-exceeded (:rule %)) (:violations v))))))

(deftest hard-on-unknown-material
  (let [st (fresh-store)
        v (governor/check req {} (craft "synthetic-vinyl" 1) st)]
    (is (:hard? v))
    (is (some #(= :unknown-material (:rule %)) (:violations v)))))

(deftest ok-delivery-with-all-spec-items
  (let [st (fresh-store)
        v (governor/check req {} (deliver all-items) st)]
    (is (:ok? v))))

(deftest ok-delivery-with-extra-items
  (testing "a superset of the required spec items still satisfies completeness"
    (let [st (fresh-store)
          v (governor/check req {} (deliver (conj all-items "care-card")) st)]
      (is (:ok? v)))))

(deftest hard-on-incomplete-delivery
  (testing "partial delivery is not delivery"
    (let [st (fresh-store)
          v (governor/check req {} (assoc (deliver #{"body" "strap"}) :confidence 0.99) st)]
      (is (:hard? v))
      (is (some #(= :incomplete-delivery (:rule %)) (:violations v))))))

(deftest hard-on-unknown-order
  (let [st (fresh-store)
        v (governor/check req {} (assoc (craft "leather-hide" 3) :order-id "O-ghost") st)]
    (is (:hard? v))
    (is (some #(= :unknown-order (:rule %)) (:violations v)))))

(deftest hard-on-foreign-order
  (let [st (fresh-store)]
    (store/register-client! st {:client-id "client-2" :name "Other"})
    (let [v (governor/check {:client-id "client-2"} {} (craft "leather-hide" 3) st)]
      (is (:hard? v))
      (is (some #(= :order-wrong-client (:rule %)) (:violations v))))))

(deftest hard-on-unregistered-client
  (let [st (fresh-store)
        v (governor/check {:client-id "nobody"} {} (craft "leather-hide" 3) st)]
    (is (:hard? v))
    (is (some #(= :no-client (:rule %)) (:violations v)))))

(deftest hard-on-no-actuation-violation
  (let [st (fresh-store)
        v (governor/check req {} (assoc (craft "leather-hide" 3) :effect :direct-write) st)]
    (is (:hard? v))
    (is (some #(= :no-actuation (:rule %)) (:violations v)))))

(deftest always-escalates-sharp-equipment-operation-even-at-high-confidence
  (testing "no sharp-equipment operation without the governor gate"
    (let [st (fresh-store)
          v (governor/check req {} {:op :approve-sharp-equipment-operation :effect :propose
                                    :order-id "O-1" :confidence 0.99 :stake :low} st)]
      (is (not (:hard? v)))
      (is (:escalate? v)))))

(deftest always-escalates-chemical-treatment-even-at-high-confidence
  (testing "chemical leather-treatment application always requires human sign-off"
    (let [st (fresh-store)
          v (governor/check req {} {:op :approve-chemical-treatment :effect :propose
                                    :order-id "O-1" :confidence 0.99 :stake :low} st)]
      (is (not (:hard? v)))
      (is (:escalate? v)))))

(deftest escalates-low-confidence
  (let [st (fresh-store)
        v (governor/check req {} (assoc (craft "leather-hide" 3) :confidence 0.3) st)]
    (is (not (:hard? v)))
    (is (:escalate? v))))
