(ns fund-service.fund
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.string :as string]
            [utils.file :refer :all]
            [utils.http :as middleware]
            [environ.core  :refer [env]]
            [fund-service.config :as config]
            [clojure.tools.logging :as log]
            ))

(defn start []
  (log/info "Starting the fund service ... "))
