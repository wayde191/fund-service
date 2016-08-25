(ns utils.http
  (:require [clj-time.format :as dtf]
            [clojure.walk :as walk]
            [clj-http.client :as client]))

(defn json-dates->dates
  [m req]
  (let [{:keys [json-dates]} req]
    (defn f [[k v]]
      (if (some (partial = k) json-dates) [k (dtf/parse v)] [k v]))
    (walk/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))

(defn hyphenate-keys
  [m _]
  (defn underscore->hyphen [k]
    (-> k
      name
      (clojure.string/replace #"_" "-")
      keyword))
  (let [f (fn [[k v]] (if (keyword? k) [(underscore->hyphen k) v] [k v]))]
    (walk/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))

(defn wrap-json-post-parser [client]
  (fn [req]
    (let [{:keys [as json-post-parsers]} req
          {:keys [body] :as resp} (client req)]
      (if (and body (= as :json))
        (assoc resp :body ((apply comp json-post-parsers) body req))
        resp))))

(defn http-atom [request]
  (client/with-middleware client/default-middleware
    (:body (client/get (request :url)
             request))))