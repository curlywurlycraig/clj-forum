(ns ssr.components
  (:require [clojure.string :as str]))

(defn breadcrumbs
  [uri]
  (let [bits (rest (str/split uri #"/"))]
    [:div#breadcrumbs-container
     (map-indexed (fn [i b]
                    (let [link [:a {:href (str "/" (str/join "/" (take (inc i) bits)))} b]
                          chevron [:span.breadcrumb-chevron ">"]]
                      (if (= i (dec (count bits)))
                        link
                        (list link chevron))))
                  bits)]))

(defn page
  [contents]
  (list
   [:head
    [:link {:rel "stylesheet" :href "/reset.css"}]
    [:link {:rel "stylesheet" :href "/main.css"}]]
   [:body
    contents]))

(defn page-header
  [user uri]
  [:nav
   (list
    (breadcrumbs uri)
    [:div#login-container
     (if user
       [:p (list
            (:username user)
            " ("
            [:a {:href "/logout"} "log out"]
            ")")]
       (list
        [:a {:href "/login"} "Login"]
        " "
        [:a {:href "/register"} "Register"]))])])

(defn greeting-page
  [request]
  (let [auth-user (:auth request)]
    (page (list
           (page-header auth-user (:uri request))
           [:div#content
            [:p "There are no posts."]]))))

(defn register-success-page
  [name]
  (page [:p (str "Welcome " name)]))

(defn register-page
  []
  (page [:form {:action "/register"
                :method "POST"
                :name "registrationForm"}
         (list
          [:label {:for "username"} "Username"]
          [:input#username {:name "username" :type "text"}]
          [:label {:for "password"} "Password"]
          [:input#password {:name "password" :type "password"}]
          [:button {:type "submit"} "Submit"])]))

(defn login-page
  []
  (page [:form {:action "/login"
                :method "POST"
                :name "loginForm"}
         (list
          [:label {:for "username"} "Username"]
          [:input#username {:name "username" :type "text"}]
          [:label {:for "password"} "Password"]
          [:input#password {:name "password" :type "password"}]
          [:button {:type "submit"} "Submit"])]))

(defn login-success-page
  [success?]
  (page [:p (if success? "welcome!" "wrong password.")]))
