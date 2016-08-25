(ns fund-service.fund
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.string :as string]
            [utils.file :refer :all]
            [utils.http :as middleware]
            [environ.core  :refer [env]]
            [fund-service.config :as config]
            [clojure.tools.logging :as log]
            [pl.danieljanus.tagsoup :as html-parser]
            ))

(defn http-atom [request]
  (client/with-middleware client/default-middleware
    (:body (client/get (request :url)
             request))))

(defn process-fund-company []
  (try
    (let [html-str (http-atom {:url config/fund-company-url})
          compaines (json/read-str (string/replace html-str #"﻿var FundCommpanyInfos=" "") :key-fn keyword)]
      (println (count (map #(get % :_id) compaines))))
    (catch Exception e
      (log/error (str "caught exception: " (.getMessage e) " with process-fund-company")))))

(defn start []
  (log/info "Starting the fund service ... ")
  (process-fund-company))



;  (println (get-in (html-parser/parse config/fund-company-url) [3 2])))
