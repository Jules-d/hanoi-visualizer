(ns hanoi-visualizer.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [ring.middleware.stacktrace :as trace]
            [ring.middleware.session :as session]
            [ring.middleware.session.cookie :as cookie]
            [ring.middleware.params :as params]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.basic-authentication :as basic]
            [cemerick.drawbridge :as drawbridge]
            [environ.core :refer [env]]
            [hanoi-visualizer.hanoi :as hanoi]
            [hiccup.core :as hc]
            [hiccup.page :as hp]))

(defn- authenticated? [user pass]
  ;; TODO: heroku config:add REPL_USER=[...] REPL_PASSWORD=[...]
  (= [user pass] [(env :repl-user false) (env :repl-password false)]))

(def ^:private drawbridge
  (-> (drawbridge/ring-handler)
      (session/wrap-session)
      (basic/wrap-basic-authentication authenticated?)))

(defn simple-link [link]
  [:a {:href link} link])

(defn my-handler [req]
  (let [params (:params req)
        solution-div (try (if params
                            (let [hanoi-input (val (last params))
                                  hanoi-result (read-string hanoi-input)
                                  answer (hanoi/read-fn hanoi-result)
                                  num-rings (hanoi/infer-rings answer)
                                  strings (map (partial hanoi/state->string num-rings) (hanoi/answer->history answer))]
                              [:table
                               (for [s strings]
                                 [:tr
                                  (for [peg s]
                                    [:td peg])])])
                            [])
                       (catch Exception e [:div "Exception: " (.getMessage e)]))
        example2  "[(\"a\",\"c\"), (\"a\",\"b\"), (\"c\",\"b\")]"
        example3 "[(\"a\",\"b\"),(\"a\",\"c\"),(\"b\",\"c\"),(\"a\",\"b\"),(\"c\",\"a\"),(\"c\",\"b\"),(\"a\",\"b\")]"
        example-extra-pegs "[(%22a%22,%22b%22),(%22b%22,%22c%22),(%22c%22,%22d%22)(%22d%22,%22e%22),(%22e%22,%22b%22)]"
        req-port (:server-port req)
        server (:server-name req)
        url (str "http://" server (if req-port ":" req-port) req-port "/")]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body  (hp/html5 [:head]
                      [:body [:h3 "Hello BFPG!"]
                       [:div "This is a visualizer for solutions in haskel from the Yorgey lectures "
                        (simple-link "http://www.seas.upenn.edu/~cis194/lectures/01-intro.html")
                        [:p]
                        "Source is available at " (simple-link "https://github.com/Jules-d/hanoi-visualizer")
                        "Use the url to pass in your haskell solution, e.g.:"]
                       [:div (simple-link (str url example2))]
                       [:div "or " (simple-link (str url example3))]
                       [:div "It should also work with " [:a {:href (str url example-extra-pegs)} "more pegs"]]
                       [:div "Each line is one step in your solution, and it's supposed to be ascii art as though you're looking down on the pegs from above."]
                       ; [:div (pr-str :req req)]
                       solution-div])}))

(defn wrap-error-page [handler]
  (fn [req]
    (try (handler req)
         (catch Exception e
           {:status 500
            :headers {"Content-Type" "text/html"}
            :body (slurp (io/resource "500.html"))}))))

(defroutes app
  (ANY "/repl" {:as req}
       (drawbridge req))
  (GET "/*" [req]
       (ring.middleware.params/wrap-params my-handler req))
  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))
        ;; TODO: heroku config:add SESSION_SECRET=$RANDOM_16_CHARS
        store (cookie/cookie-store {:key (env :session-secret)})]
    (jetty/run-jetty app {:port port})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
