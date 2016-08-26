(def version (if-let [version (System/getenv "GO_PIPELINE_COUNTER")]
               (str version "." (System/getenv "GO_STAGE_COUNTER"))
               "0.1.0-SNAPSHOT"))

(defproject fund-service version
  :description "Fund service app"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [dk.ative/docjure "1.6.0"]
                 [me.raynes/fs "1.4.4"]
                 [clj-time "0.8.0"]
                 [clj-http "0.7.6"]
                 [digest "1.4.5"]
                 [org.clojure/data.json "0.2.3"]
                 [environ "0.4.0"]
                 [me.raynes/fs "1.4.4"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.clojure/core.incubator "0.1.3"]
                 [overtone/at-at "1.2.0"]
                 [org.clojure/java.jdbc "0.0.6"]
                 [mysql/mysql-connector-java "5.1.6"]
                 [clj-tagsoup/clj-tagsoup "0.3.0" :exclusions [org.clojure/clojure]]
                 [pjstadig/humane-test-output "0.6.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jdmk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 ]
  :injections [(require 'pjstadig.humane-test-output)
               (pjstadig.humane-test-output/activate!)]
  :plugins [[lein-midje "3.0.0"]
            [lein-ring "0.8.10"]
            [lein-rpm "0.0.5"]
            [lein-shell "0.4.0"]
            [lein-localrepo "0.5.3"]
            [jonase/eastwood "0.1.4"]]
  :main ^:skip-aot fund-service.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[midje "1.5.1"]
                                  [ring-mock "0.1.5"]
                                  [clj-http-fake "0.7.8"]
                                  [lein-environ "0.4.0"]
                                  [org.hsqldb/hsqldb "2.3.2"]
                                  [java-jdbc/dsl "0.1.1"]]}}

  :rpm {:name "fund-service"
        :summary "Fund service"
        :copyright "iHakula Inc"
        :workarea "target/rpm"
        :requires ["java-1.7.0-openjdk"]
        :preinstall {:scriptFile "src/rpm/pre-install"}
        :preremove {:scriptFile "src/rpm/pre-uninstall"}
        :mappings [{:directory "/usr/lib/fund-service"
                    :sources {:source
                              [{:location ~(str "target/fund-service-" version "-standalone.jar")
                                :destination "fund-service-standalone.jar"}]}}
                   {:directory "/usr/bin"
                    :filemode "755"
                    :directoryIncluded false
                    :sources {:source [{:location "src/rpm/fund-service"}]}}
                   {:directory "/etc/systemd/system"
                    :filemode "755"
                    :directoryIncluded false
                    :sources {:source [{:location "src/scripts/fund-service.service"}]}}
                   ]}

  :aliases {"clean-test" ["do" ["clean"] ["compile"] ["test"]]
            "clean-package" ["do" ["clean"] ["package"]]
            "package" ["do" ["compile"] ["uberjar"] ["rpm"]]
            "production" ["shell" "src/scripts/deploy" ~version]
            "deploy-production" ["do" ["clean-package"] ["production"]]
            })
