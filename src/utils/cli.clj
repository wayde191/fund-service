(ns utils.cli)

(defmacro defoperation [op bindings & body]
  `(defmethod operation ~op [op# args#]
     (let [~(first bindings) args#]
       (println (str "Start " op#))
       ~@body
       (println (str "Stop " op#)))))

(defmulti operation (fn[op args] op))
