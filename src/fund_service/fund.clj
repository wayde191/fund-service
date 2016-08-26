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
    (log/info (str "updating " company-code " " name " " search-field))
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

(defn update-funds [fund]
  (let [code (get fund 0)
        short-name (get fund 1)
        name (get fund 2)
        type (get fund 3)]
    (log/info (str "updating " code " " short-name " " name " " type))
    (try
      (if (nil? (mysql/get-fund-by-code code))
        (mysql/insert-fund code short-name name type)
        (mysql/update-fund code short-name name type))
      (catch  Exception e
        (log/error (str "caught exception: " (.getMessage e) " with update-funds"))))
    ))

(defn process-funds []
  (try
    (let [html-str (middleware/http-atom {:url config/fund-code-url})
          html-md5 (digest/md5 html-str)
          db-md5 (mysql/get-resource-md5-by-name "funds")
          trim-str (string/trim
                     (->
                       (string/replace html-str #"var r = " "")
                       (string/replace #";" "")
                       ))
          funds (json/read-str (subs trim-str 1 (.length trim-str)))]
      (if (nil? db-md5)
        (mysql/insert-resource-md5-with-name html-md5 "funds")
        (if (= html-md5 db-md5)
          (log/info (str "Same data for funds: " html-md5))
          (do
            (count (map #(update-funds %) funds))
            (mysql/update-resource-md5-with-name html-md5 "funds"))))
      )
    (catch Exception e
      (println e)
      (log/error (str "caught exception: " (.getMessage e) " with process-funds")))))

(defn start []
  (log/info "Starting the fund service ... ")
  (process-fund-company)
  (process-funds))



;  (println (get-in (html-parser/parse config/fund-company-url) [3 2])))
