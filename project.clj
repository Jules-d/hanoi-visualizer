(defproject hanoi-visualizer "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://hanoi-visualizer.herokuapp.com"
  :license {:name "FIXME: choose"
            :url "http://example.com/FIXME"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.1"]
                 [ring/ring-jetty-adapter "1.1.0"]
                 [ring/ring-devel "1.1.0"]
                 [ring/ring-core "1.2.1"]
                 [ring-basic-authentication "1.0.1"]
                 [environ "0.2.1"]
                 [com.cemerick/drawbridge "0.0.6"]
                 [hiccup "1.0.5"]]
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.2.1"]
            [lein-ring "0.8.10"]]
  :hooks [environ.leiningen.hooks]
  :profiles {:production {:env {:production true}}
             :dev {:ring {:handler hanoi-visualizer.web/app
                          :auto-reload? true
                          :auto-refresh? true}}})
