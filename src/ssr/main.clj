(ns ssr.main
  (:require [buddy.hashers :as hashers]
            [clojure.data.json :as json]
            [ring.util.response :as resp]
            [ssr.db :as db]
            [ssr.crypt :as crypt])
  (:use [hiccup.core]
        [hiccup.page]
        [ring.adapter.jetty]
        [ring.middleware.resource]
        [ring.middleware.params]
        [ring.middleware.cookies]
        [ring.util.request]
        [ssr.components]))

(defn html-result
  ([body extra]
   (merge {:status 200
           :headers {"Content-Type" "text/html"}
           :body body}
          extra))
  ([body]
   (html-result body {})))

(defn login
  [request]
  (case (:request-method request)
    :post (let [params (:form-params request)
                username (get params "username")
                password (get params "password")
                user (db/get-user username)
                success? (:valid (hashers/verify password (:hash user)))
                session-hash (crypt/random-session-hash)
                session (db/insert-session session-hash (:user_id user))]
            (-> (resp/redirect "/")
                (resp/set-cookie "session" (:hash session))))
    :get (html-result (html5 (login-page)))))

(defn logout
  [request]
  (let [user-id (get-in request [:auth :user_id])]
    (db/remove-session-for-user user-id)
    (-> (resp/redirect "/")
        (resp/set-cookie "session" "" {:max-age 0}))))

(defn register
  [request]
  (case (:request-method request)
    :post (let [params (:form-params request)
                username (get params "username")
                password (get params "password")
                encrypted-p (hashers/derive password {:alg :bcrypt+sha512})
                user (db/insert-user username encrypted-p)
                session (db/insert-session (crypt/random-session-hash)
                                           (:user_id user))]
            (-> (resp/redirect "/")
                (resp/set-cookie "session" (:hash session))))
    :get (html-result (html5 (register-page)))))

(defn handler
  [request]
  (case (:uri request)
    "/register" (register request)
    "/login" (login request)
    "/logout" (logout request)
    (-> (greeting-page request)
        html5
        html-result)))

(defn wrap-db-token-auth
  [handler]
  (fn [request]
    (if-let [session-hash (get-in request [:cookies "session" :value])]
      (if-let [auth-user (db/get-user-by-session session-hash)]
        (handler
         (merge request
                {:auth auth-user}))
        (handler request))
      (handler request))))

(def app
  (-> handler
      (wrap-db-token-auth)
      (wrap-params)
      (wrap-cookies)
      (wrap-resource "public")))

(defn -main
  []
  (run-jetty app {:port 80
                  :join? false}))
