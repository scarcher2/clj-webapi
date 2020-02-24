(ns api.main
  (:require
   [ring.middleware.json :refer [wrap-json-params wrap-json-response]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.adapter.jetty :as jetty]
   [compojure.core :refer :all]
   [compojure.route :as route]
   [compojure.handler :as handler]))

(defn content-type-response [response content-type]
  (assoc-in response [:headers "Content-Type"] content-type))

(defn wrap-content-type [handler content-type]
  (fn
    ([request]
     (-> (handler request) (content-type-response content-type)))
    ([request respond raise]
     (handler request #(respond (content-type-response % content-type)) raise))))

(defn what-is-my-ip
  ([request]
   {:status 200
    :headers {}
    :body (:remote-addr request)})
  
  ([request respond raise]
   
   (respond (what-is-my-ip request))))


 
(defn echo-handler
  ([request]
   {:status 200
    :headers {}
    :body (assoc request :body (slurp (:body request)))
   }
  )
  ([request respond raise]
   (respond (echo-handler request))))


  

(defroutes main-routes
  (GET "/" []  'echo-handler)
  (route/not-found "<h1>Page not found</h1>"))


(defroutes api-routes
  (context "/api" []
    (GET "/" request
      (let [base-uri (get-base-uri request)
            hal-links {:_links {:self {:href base-uri} :greet {:href (str base-uri "/greet{?name}") :templated true}}}]
        (json-response hal-links)))
    (GET "/greet" [name :as request]
      (let [base-uri (get-base-uri request)]
        (json-response {:greeting (str "Greetings " name) :_links {:self {:href (str base-uri "/greet?name=" name)}}})))
    (ANY "*" []
      (route/not-found (slurp (io/resource "404.html"))))))

(def rest-api
  (handler/api api-routes))


(def my-app
  (-> handler/site main-routes
      ))

(defn -main [&args]
  (jetty/run-jetty #'my-app {:port 3000 :join? true}))

(comment
  (defonce server (jetty/run-jetty #'my-app {:port 3000 :join? false}))
  (.stop server)
  (.start server)
  
  
  wrap-json-response
wrap-keyword-params
wrap-json-params
  )

