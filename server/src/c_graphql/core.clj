(ns c-graphql.core
  (:use ring.util.response)
  (:require [compojure.core :refer :all]
            [compojure.handler :as h]
            [compojure.route :as route]
            [ring.middleware.json :as m-json]
            [ring.middleware.cors :refer [wrap-cors]]
            [graphql-clj.executor :as executor]
            [clojure.core.match :as match]))

(def users [
    { :id "1" :name "John Gang", :email "gang@gmail.com" :age 14 }
    { :id "2" :name "John Kim", :email "kim@gmail.com" :age 15 }
    { :id "3" :name "John Min", :email "min@gmail.com" :age 18 }
    { :id "4" :name "John Yu", :email "yu@gmail.com" :age 18 }          
])

(def schema "
  type User {
    id: String
    name: String
    email: String
    age: Int
  }
  type RootQuery {
                  user(id: ID): User
                  users : [User]
                  }
  schema {
    query: RootQuery
  }")

(defn get-user [id]
  (first
   (filter (fn [user]
             (= (:id user) id))
           users)))

(defn get-users [] users)

(defn resolver-fn [type-name field-name]
  (match/match [type-name field-name]
               ["RootQuery" "user"] (fn [context parent args] ; { user (id : "2") { id, name, email, age } }
                                      (get-user (get args "id")))
              ["RootQuery" "users"] (fn [context parent args] ; { users { id, name, email, age } }
                                     (get-users))
               :else nil))

(defroutes handler
           (POST "/graphql" [query]
                 (response (executor/execute nil schema resolver-fn 
                                             query)))
           (route/not-found (response
                             {:message "not found"})))

(def app
  (-> (h/api handler)
      (m-json/wrap-json-params)
      (m-json/wrap-json-response)
      (wrap-cors :access-control-allow-origin [#"http://127.0.0.1:5500"] ; #".*"
                 :access-control-allow-methods [:get :put :post :delete])))