(ns c-graphql.core
  (:use ring.util.response)
  (:require [compojure.core :refer :all]
            [compojure.handler :as h]
            [compojure.route :as route]
            [ring.middleware.json :as m-json]
            [ring.middleware.cors :refer [wrap-cors]]))

(defroutes handler
           (POST "/graphql" [query]
                 (response
                  {:message query}))
           (route/not-found (response
                             {:message "not found"})))

(def app
  (-> (h/api handler)
      (m-json/wrap-json-params)
      (m-json/wrap-json-response)
      (wrap-cors :access-control-allow-origin [#"http://127.0.0.1:5500"] ; #".*"
                 :access-control-allow-methods [:get :put :post :delete])))