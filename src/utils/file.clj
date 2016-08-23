(ns utils.file
  (:require [utils.json :as json]
            [me.raynes.fs :as fs]))

(defn persist-as-json [contents location]
  (fs/mkdirs (fs/parent location))
  (json/to-file location contents)
  location)

(defn persist-resource [contents path & paths]
  (let [target (apply fs/file path paths)]
    (println "Creating resource " (.getName target) " at " (.getParent target))
    (persist-as-json contents target)))

(defn delete-directory [path]
  (if (fs/exists? path)
    (fs/delete-dir path)))

(defn read-file [file-name]
  (json/read-json-file file-name))