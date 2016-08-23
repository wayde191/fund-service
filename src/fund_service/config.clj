(ns fund-service.config)

(defn- env [key] (System/getenv key))

(defn east-fund-service-url []
  (env "EAST_FUND_SERVICE_URL"))




