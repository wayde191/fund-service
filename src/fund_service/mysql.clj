(ns fund-service.mysql
  (:require [clojure.java.jdbc :as sql]))

(def db {:classname "com.mysql.jdbc.Driver"
         :subprotocol "mysql"
         :subname "//localhost:3306/ihakula_fund?characterEncoding=UTF-8"
         :user "root"
         :password "Wayde191!"})

(defn get-fund-company-by-code [code]
  (sql/with-connection db
    (sql/with-query-results rows [(str "select * from fund_company where company_code = '" code "'")]
      (doall rows))))

(defn insert-fund-company [company-code name search-field]
  (sql/with-connection db
    (sql/insert-records :fund_company
      {:company_code company-code
       :name name
       :search_field search-field})))

(defn update-fund-company [company-code name search-field]
  (sql/with-connection db
    (sql/update-values :fund_company
      ["company_code=?" company-code]
      {:company_code company-code
       :name name
       :search_field search-field})))

(defn get-resource-md5-by-name [name]
  (let [md5 (sql/with-connection db
              (sql/with-query-results rows [(str "select md5 from resource where name = '" name "'" )]
                (doall rows)))]
    (if (nil? md5)
      nil
      (:md5 (first md5)))))

(defn insert-resource-md5-with-name [md5 name]
  (sql/with-connection db
    (sql/insert-records :resource
      {:name name
       :md5 md5})))

(defn update-resource-md5-with-name [md5 name]
  (sql/with-connection db
    (sql/update-values :resource
      ["name=?" name]
      {:md5 md5})))

(defn get-fund-by-code [code]
  (sql/with-connection db
    (sql/with-query-results rows [(str "select * from fund where code = '" code "'")]
      (doall rows))))

(defn get-fund-net-value-by-code-date [code date]
  (sql/with-connection db
    (sql/with-query-results rows [(str "select * from net_value where code = '" code "'" " and date = '" date "'")]
      (doall rows))))

(defn insert-fund-net-value [code name net-value acc-net-value day-increase day-increase-rate buy-state sell-state commission show-day]
  (sql/with-connection db
    (sql/insert-records :net_value
      {:code code
       :name name
       :net_value net-value
       :acc_net_value acc-net-value
       :day_increase day-increase
       :day_increase_rate day-increase-rate
       :buy_state buy-state
       :sell_state sell-state
       :commission commission
       :date show-day})))

(defn get-all-funds []
  (sql/with-connection db
    (sql/with-query-results rows ["select * from fund"]
      (doall rows))))

(defn insert-fund [code short-name name type]
  (sql/with-connection db
    (sql/insert-records :fund
      {:code code
       :short_name short-name
       :name name
       :type type})))

(defn update-fund [code short-name name type]
  (sql/with-connection db
    (sql/update-values :fund
      ["code=?" code]
      {:code code
       :short_name short-name
       :name name
       :type type})))