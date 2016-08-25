(ns fund-service.config)

(defn- env [key] (System/getenv key))

(defn east-fund-service-url []
  (env "EAST_FUND_SERVICE_URL"))

(def fund-code-url "http://fund.eastmoney.com/js/fundcode_search.js")

(def fund-company-url "http://fund.eastmoney.com/api/static/FundCommpanyInfo.js")


