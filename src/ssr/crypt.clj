(ns ssr.crypt
  (:require [buddy.hashers :as hashers])
  (:import java.security.SecureRandom
           java.util.Base64))

(defn random-session-hash
  []
  (let [bytes (byte-array 64)]
    (.nextBytes (SecureRandom.) bytes)
    (.encodeToString (Base64/getEncoder) bytes)))

