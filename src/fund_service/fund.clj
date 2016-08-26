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
            [digest :as digest]))

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
          html-md5 (digest/md5 html-str)
          db-md5 (mysql/get-resource-md5-by-name "fund_company")
          compaines (json/read-str (string/replace html-str #"﻿var FundCommpanyInfos=" "") :key-fn keyword)]
      (if (nil? db-md5)
        (mysql/insert-resource-md5-with-name html-md5 "fund_company")
        (if (= html-md5 db-md5)
          (log/info (str "Same data for fund company: " html-md5))
          (do
            (count (map #(update-fund-company %) compaines))
            (mysql/update-resource-md5-with-name html-md5 "fund_company")))))
    (catch Exception e
      (log/error (str "caught exception: " (.getMessage e) " with process-fund-company")))))

(defn start []
  (log/info "Starting the fund service ... ")
  (process-fund-company))



;  (println (get-in (html-parser/parse config/fund-company-url) [3 2])))
