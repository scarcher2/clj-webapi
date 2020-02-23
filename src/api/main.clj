(ns api.main
  (:require 
   [ring.middleware.json :refer [wrap-json-params wrap-json-response]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.adapter.jetty :as jetty]))

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
    :body {:type (type request)}})

  ([request respond raise]
   (respond (echo-handler request))))


  (def my-app
    (-> echo-handler
        wrap-json-response
        wrap-keyword-params
        wrap-json-params
        ))



(defn -main [&args]
  (jetty/run-jetty #'my-app {:port 3000 :join? true}))

(comment
  (defonce server (jetty/run-jetty #'my-app {:port 3000 :join? false}))
  (.stop server)
  (.start server)
  )

