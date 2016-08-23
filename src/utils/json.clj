(ns utils.json
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clj-time.coerce :as coerce]))

(defmulti json-value (fn [k v] (class v)))
(defmethod json-value java.util.Date [k v] (.toString (coerce/from-date v)))
(defmethod json-value org.joda.time.DateTime [k v] (.toString v))
(defmethod json-value org.joda.time.LocalDate [k v] (.toString v))
(defmethod json-value :default [k v] v)

(defn to-file [location contents]
  (with-open [writer (io/writer location)]
    (json/write contents writer :value-fn json-value)))

(defn read-json-file [file-name]
  (slurp (io/resource file-name)))
;  (json/read-str (slurp (io/resource file-name))))
