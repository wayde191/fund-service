(ns fund-service.fund
  (:require
            [clojure.data.json :as json]
            [clojure.string :as string]
            [utils.file :refer :all]
            [utils.http :as middleware]
            [environ.core  :refer [env]]
            [fund-service.config :as config]
            [clojure.tools.logging :as log]
            [pl.danieljanus.tagsoup :as html-parser]
            [fund-service.mysql :as mysql]
            ))

(defn update-fund-company [company]
  (let [company-code (get company :COMPANYCODE)
        name (get company :SNAME)
        search-field (get company :SEARCHFIELD)]
    (try
      (if (nil? (mysql/get-fund-company-by-code company-code))
        (mysql/insert-fund-company company-code name search-field)
        (mysql/update-fund-company company-code name search-field))
      (catch  Exception e
        (log/error (str "caught exception: " (.getMessage e) " with update-fund-company"))))
    ))

(defn process-fund-company []
  (try
    (let [html-str (middleware/http-atom {:url config/fund-company-url})
          compaines (json/read-str (string/replace html-str #"﻿var FundCommpanyInfos=" "") :key-fn keyword)]
      (count (map #(update-fund-company %) compaines)))
    (catch Exception e
      (log/error (str "caught exception: " (.getMessage e) " with process-fund-company")))))

(defn start []
  (log/info "Starting the fund service ... ")
  (process-fund-company))



;  (println (get-in (html-parser/parse config/fund-company-url) [3 2])))
