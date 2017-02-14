(ns marathon-clj.core-test
  (:require [clojure.test :refer :all]
            [marathon-clj.core :refer :all]))

(def client {:url "http://localhost:8080"
             :basic-auth ["user" "password"]})

(def sample-app-id "/my-app")

(def sample-app {:constraints [["hostname" "UNIQUE"]]
                 :mem 50
                 :healthChecks
                 [{:gracePeriodSeconds 3
                   :intervalSeconds 10
                   :maxConsecutiveFailures 3
                   :path "/"
                   :portIndex 0
                   :protocol "HTTP"
                   :timeoutSeconds 5}]
                 :ports [0]
                 :instances 2
                 :upgradeStrategy
                 {:minimumHealthCapacity 0.5
                  :maximumOverCapacity 0.5}
                 :container
                 {:docker {:image "python:3"}, :type "DOCKER"}
                 :cmd "env && python3 -m http.server $PORT0"
                 :cpus 0.25})

(deftest core-test

  (testing "create app"
    (let [resp (create-app client sample-app-id sample-app)]
      (is (= (:status resp) 201))))

  (testing "get apps"
    (let [resp (get-apps client)]
      (is (contains? (set (map :id (:apps resp))) sample-app-id))))

  (testing "get app"
    (let [resp (get-app client sample-app-id)]
      (is (= sample-app-id (:id (:app resp))))))

  (testing "get versions"
    (let [resp (get-versions client sample-app-id)]
      (is (pos? (count (:versions resp))))))

  (testing "get version"
    (let [version (first (:versions (get-versions client sample-app-id)))
          resp (get-version client sample-app-id version)]
      (is (= (:id resp) sample-app-id))))

  (testing "update app"
    (let [version (first (:versions (get-versions client sample-app-id)))
          resp (update-app client sample-app-id {:version version})]
      (is (not (nil? (:version resp))))))

  (testing "get running tasks by app"
    (let [resp (get-running-tasks-by-app client sample-app-id)]
      (is (pos? (count (:tasks resp))))))

  (testing "delete app"
    (let [resp (delete-app client sample-app-id)])))





