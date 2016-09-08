(ns utils.date
  (:require [clj-time.core :as time]
            [clj-time.periodic :as tp]
            [clj-time.format :refer [parse formatter unparse]]
            [clj-time.predicates :as predicates]))

(defn unparse-date [format date]
  (unparse (formatter format) date))

(defn day-of-week [date day]
  (.withDayOfWeek date day))

(defn sunday [date] (day-of-week date 7))

(defn last-days [days]
  (let [the-date (time/minus (time/now) (time/days days))]
  (time/date-time (time/year the-date) (time/month the-date) (time/day the-date) 23 59 59)))

(defn last-sunday [date]
  (if (predicates/sunday? date)
    (time/minus date (time/days 7))
    (time/minus (sunday date) (time/days 7))))

(defn weeks [end-date]
  (let [last-end-date (if (predicates/sunday? end-date)
                        end-date
                        (last-sunday end-date))]
    last-end-date))

(defn to-local-date
  ([date] (to-local-date "dd-MM-yyyy" date))
  ([format date] (let [date-time (parse (formatter format) date)]
    (time/local-date (time/year date-time) (time/month date-time) (time/day date-time))
    ))
  )

(defn is-years-before? [years date]
  (time/before? (to-local-date "yyyy-MM-dd" date) (time/minus (time/today) (time/years years)))
  )

(defn is-date-before-current-year? [date]
  (time/before? (to-local-date "yyyy-MM-dd" date) (time/local-date (time/year (time/today)) 01 01)
  ))

(defn all-days-between [start-date end-date]
  (take-while #(not (time/after? % end-date))
              (tp/periodic-seq start-date (time/days 1))))
