(ns fund-service.fund
  (:require
            [clojure.data.json :as json]
            [clojure.string :as string]
            [utils.file :refer :all]
            [utils.http :as middleware]
            [utils.date :as date-utils]
            [clj-time.core :as time]
            [clj-time.coerce :as coerce]
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

(defn insert-fund-net-value [fund]
  (let [code (get fund :code)
        short-name (get fund :short_name)
        name (get fund :name)
        type (get fund :type)
        the-day (date-utils/unparse-date "YYYY-MM-dd" (time/now))
        log-info (str "insert " code " " short-name " " name " " type " at the day: " the-day)
        html (html-parser/parse (str config/east-fund-url code ".html"))
        body (get-in html [3 2 13])]
    (println html)
    (log/info log-info)
    (try
      (if (nil? (mysql/get-fund-net-value-by-code-date code the-day))
        (println "haha")
        (log/info "exist already! " log-info))
      (catch  Exception e
        (log/error (str "caught exception: " (.getMessage e) " with insert-fund-net-value"))))
    ))

(defn insert-net-value []
  (try
    (let [all-funds (first (partition 1 (mysql/get-all-funds)))]
      (count (map #(insert-fund-net-value %) all-funds)))
    (catch Exception e
      (println e)
      (log/error (str "caught exception: " (.getMessage e) " with update-net-value")))))


(defn update-funds-net-value [fund show-day]
  (let [code (get fund 0)
        name (get fund 1)
        net-value (get fund 3)
        acc-net-value (get fund 4)
        day-increase (get fund 7)
        day-increase-rate (get fund 8)
        buy-state (get fund 9)
        sell-state (get fund 10)
        commission (get fund 17)
        the-day (date-utils/unparse-date "YYYY-MM-dd" (time/now))
        log-info (str "update-funds-net-value " code " " name " " net-value " " commission " at the day: " the-day)
        ]
    (try
      (if (nil? (mysql/get-fund-net-value-by-code-date code show-day))
        (do
          (mysql/insert-fund-net-value code name net-value acc-net-value day-increase day-increase-rate buy-state sell-state commission show-day)
          (log/info log-info))
        (log/info "exist already! " log-info))
      (catch  Exception e
        (log/error (str "caught exception: " (.getMessage e) " with insert-fund-net-value"))))
    ))

(defn process-funds-net-value []
  (try
    (let [html-str (middleware/http-atom {:url (config/get-funds-data-url (coerce/to-long (time/now)))})
          trim-str (string/trim
                     (->
                       (string/replace html-str #"var db=" "")
                       (string/replace #"chars|datas|count|record|pages|curpage|showday|indexsy"  #(str "\"" %1 "\""))
                       (string/replace #";" "")
                       ))
          funds (json/read-str trim-str :key-fn keyword)
          datas (:datas funds)
          showday (first (:showday funds))
          the-day (date-utils/unparse-date "YYYY-MM-dd" (time/now))]
      (persist-as-json trim-str (str "/tmp/funds-net-value-" the-day ".json"))
      (count (map #(update-funds-net-value % showday) datas)))
    (catch Exception e
      (println e)
      (log/error (str "caught exception: " (.getMessage e) " with insert-funds-net-value")))))

(defn start []
  (log/info "Starting the fund service ... ")
;  (process-fund-company)
;  (process-funds)
  (process-funds-net-value))
