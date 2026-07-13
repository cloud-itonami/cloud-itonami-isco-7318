(ns handicraft.actor-test
  (:require [clojure.test :refer [deftest is testing]]
            [handicraft.actor :as actor]
            [handicraft.store :as store]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-client! st {:client-id "client-1" :name "Kobo Craft"})
    (store/register-order! st {:order-id "O-1" :client-id "client-1"
                               :name "leather-satchel"
                               :material-stock {"leather-hide" 5}
                               :required-spec-items #{"body" "strap"}})
    st))

(deftest commits-an-in-stock-craft-step
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:client-id "client-1" :op :approve-craft-step :stake :low
                 :order-id "O-1" :material "leather-hide" :quantity 3}
        result (actor/run-request! graph request {} "thread-1")]
    (is (= :done (:status result)))
    (is (some? (get-in result [:state :record])))
    (is (= 1 (count (store/records-of st "client-1"))))))

(deftest holds-an-incomplete-delivery
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:client-id "client-1" :op :approve-delivery :stake :low
                 :order-id "O-1" :delivered-items #{"body"}}
        result (actor/run-request! graph request {} "thread-2")]
    (is (= :hold (:disposition (:state result))))
    (is (empty? (store/records-of st "client-1")))))

(deftest interrupts-then-approves-sharp-equipment-on-human-approval
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:client-id "client-1" :op :approve-sharp-equipment-operation :stake :low
                 :order-id "O-1"}
        interrupted (actor/run-request! graph request {} "thread-3")]
    (is (= :interrupted (:status interrupted)))
    (is (empty? (store/records-of st "client-1")))
    (let [resumed (actor/approve! graph "thread-3")]
      (is (= :done (:status resumed)))
      (is (= 1 (count (store/records-of st "client-1")))))))
