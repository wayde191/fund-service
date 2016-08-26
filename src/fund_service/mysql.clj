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

(defn get-fund-company-md5 []
  (let [md5 (sql/with-connection db
              (sql/with-query-results rows ["select md5 from resource where name = 'fund_company'"]
                (doall rows)))]
    (if (nil? md5)
      nil
      (:md5 (first md5)))))

(defn insert-fund-company-md5 [md5]
  (sql/with-connection db
    (sql/insert-records :resource
      {:name "fund_company"
       :md5 md5})))

(defn update-fund-company-md5 [md5]
  (sql/with-connection db
    (sql/update-values :resource
      ["name=?" "fund_company"]
      {:md5 md5})))