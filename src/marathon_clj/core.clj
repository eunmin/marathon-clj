(ns marathon-clj.core
  (:require [org.httpkit.client :as http]
            [clojure.string :as string]
            [cheshire.core :refer [parse-string generate-string]]))

(def ^:private json-type "application/json")

(defn- ok? [{:keys [status]}]
  (= 200 status))

(defn- json? [{:keys [headers]}]
  (string/starts-with? (:content-type headers) json-type))

(defn- request [method client path & [body]]
  (let [options (if body
                  {:body (generate-string body)
                   :headers {"Content-Type" json-type}}
                  {})
        resp @(method (str (:url client) path) (merge options (dissoc client :url)))]
    (if (and (ok? resp) (json? resp))
      (parse-string (:body resp) true)
      resp)))

(def ^:private GET (partial request http/get))

(def ^:private POST (partial request http/post))

(def ^:private PUT (partial request http/put))

(def ^:private DELETE (partial request http/delete))

(defn- multi-param [param-name values]
  (string/join "&" (map #(str param-name "=" %) values)))

(defn- query-params [params]
  (string/join "&" (map (fn [[k v]]
                          (if (vector? v)
                            (multi-param (name k) v)
                            (str (name k) "=" v)))
                     params)))

;; Apps

(defn create-app
  "Create and start a new app"
  [client app-id app]
  (POST client "/v2/apps" (merge {:id app-id} app)))

(defn get-apps
  "List all running apps"
  [client & [{:keys [cmd id label embed] :as opts}]]
  (GET client (str "/v2/apps?" (query-params opts))))

(defn get-app
  "List the app app-id"
  [client app-id & [{:keys [embed] :as opts}]]
  (GET client (str "/v2/apps/" app-id "?" (query-params opts))))

(defn get-versions
  "List the versions of the application with id app-id"
  [client app-id]
  (GET client (str "/v2/apps/" app-id "/versions")))

(defn get-version
  "List the configuration of the application with id app-id at version version"
  [client app-id version]
  (GET client (str "/v2/apps/" app-id "/versions/" version)))

(defn update-app
  "Update or create an app with id app-id"
  [client app-id app & [{:keys [force] :as opts}]]
  (PUT client (str "/v2/apps" app-id "?" (query-params opts)) app))

(defn restart-app
  "Rolling restart of all tasks of the given app-id"
  [client app-id & [{:keys [force] :as opts}]]
  (POST client (str "/v2/apps/" app-id "/restart?" (query-params opts))))

(defn delete-app
  "Destroy app app-id"
  [client app-id]
  (DELETE client (str "/v2/apps/" app-id)))

(defn get-running-tasks-by-app
  "List running tasks for app appId"
  [client app-id]
  (GET client (str "/v2/apps/" app-id "/tasks")))

(defn kill-all-app-tasks
  "Kill tasks belonging to app app-id"
  [client app-id & [{:keys [host scale wipe] :as opts}]]
  (DELETE client (str "/v2/apps/" app-id "/tasks?" (query-params opts))))


;; Groups

(defn get-groups
  "List all groups"
  [client]
  (GET client (str "/v2/groups")))

(defn get-group
  "List the group with the specified ID"
  [client group-id]
  (GET client (str "/v2/groups/" group-id)))

(defn create-group
  "Create and start a new groups"
  ([client group-id]
   (create-group client group-id []))
  ([client group-id apps]
   (POST client (str "/v2/groups") {:id group-id :apps apps})))

(defn update-group
  "Change parameters of a deployed application group"
  [client group-id apps]
  (PUT client (str "/v2/groups" group-id) {:apps apps}))

(defn delete-group
  "Destroy a group"
  [client group-id]
  (DELETE client (str "/v2/groups" group-id)))


;; Tasks

(defn get-running-tasks
  "List running tasks"
  [client & [{:keys [status] :as opts}]]
  (GET client "/v2/tasks?" (query-params opts)))

(defn kill-all-tasks
  "Kill given list of tasks"
  [client task-ids & [{:keys [scale wipe] :as opts}]]
  (POST client (str "/v2/tasks/delete?" (query-params opts)) {:ids task-ids}))

(defn kill-task
  "Kill the task task-id that belongs to the application app-id"
  [client app-id task-id & [{:keys [scale wipe] :as opts}]]
  (DELETE client (str "/v2/apps/" app-id "/tasks/" task-id "?" (query-params opts))))


;; Deploymemnts

(defn get-running-deployments
  "List running deployments"
  [client]
  (GET client "/v2/deployments"))

(defn cancel-deployment
  "Revert or cancel the deployment with deploymentId"
  [client deployment-id & [{:keys [force] :as opts}]]
  (DELETE client (str "/v2/deployments/" deployment-id "?" (query-params opts))))

;; Event Stream

(defn get-events
  "Attach to the event stream"
  [client]
  (GET client "/v2/events"))

;; Event Subscriptions

(defn create-event-subscription
  "Register a callback URL as an event subscriber"
  [client callback-url]
  (POST client (str "/v2/eventSubscriptions?callbackUrl=" callback-url)))

(defn get-event-subscriptions
  "List all event subscriber callback URLs"
  [client]
  (GET client "/v2/eventSubscriptions"))

(defn delete-event-subscription
  "Unregister a callback URL from the event subscribers list"
  [client callback-url]
  (DELETE client "/v2/eventSubscriptions?callbackUrl=" callback-url))


;; Queue

(defn get-queue
  "Show content of the launch queue"
  [client]
  (GET client "/v2/queue"))

(defn delete-queue????
  "The application specific task launch delay can be reset by calling this endpoint"
  [client app-id]
  (DELETE client (str "/v2/queue/" app-id "/delay")))

;; Server Info

(defn get-info
  "Get info about the Marathon Instance"
  [client]
  (GET client "/v2/info"))

(defn get-leader
  "Returns the current leader. If no leader exists, Marathon will respond with a 404 error."
  [client]
  (GET client "/v2/leader"))

(defn delete-leader
  "Causes the current leader to abdicate, triggering a new election. 
   If no leader exists, Marathon will respond with a 404 error."
  [client]
  (DELETE client "/v2/leader"))


;; Miscellaneous

(defn ping [client]
  (GET client "/v2/ping"))

(defn logging [client]
  (GET client "/v2/logging"))

(defn help [client]
  (GET client "/v2/help"))

(defn metrics [client]
  (GET client "/v2/metrics"))
