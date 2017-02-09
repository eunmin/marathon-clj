# marathon-clj

Clojure client library for Mesos Marathon

https://mesosphere.github.io/marathon/docs/rest-api.html

## Usage

```clojure
(require 'marathon-clj.core)

(def client {:host "http://localhost:8080"})

(def app {:constraints [["hostname" "UNIQUE"]]
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

(create-app client "my-app" app)

(get-app client "my-app")

(update-app client "my-app" {:cmd "sleep 55"
                             :constraints [["hostname" "UNIQUE" ""]]
                             :cpus "0.3"
                             :instances "2"
                             :mem "9"
                             :ports [9000]})

;; Example version rollback
(update-app client "my-app" {:version "2014-03-01T23:17:50.295Z"})

(restart-app client "my-app")
```

## License

Copyright © 2017 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
