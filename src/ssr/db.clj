(ns ssr.db
  (:require [clojure.java.jdbc :as j]))

(def pg-db {:dbtype "postgresql"
            :dbname "postgres"
            :host "localhost"
            :user "postgres"
            :password "example"
            :ssl false})

(defn insert-user
  [username pw-hash]
  (j/insert! pg-db
             :users
             {:username username
              :hash pw-hash}))

(defn get-user
  [username]
  (first (j/query pg-db
                  ["select * from users where username = ?" username])))

(defn get-session
  [session-hash]
  (first (j/query pg-db
                  ["select * from sessions where hash = ?" session-hash])))

(defn get-user-by-session
  [session-hash]
  (first (j/query pg-db
                  [(str "select u.* from sessions s "
                        "join users u on s.user_id = u.user_id "
                        "where s.hash = ?")
                   session-hash])))

(defn insert-session
  [session-hash user-id]
  (first (j/insert! pg-db
                    :sessions
                    {:hash session-hash
                     :user_id user-id})))
