(ns gravie-member-client.api
  (:require [ajax.core :refer [GET POST PUT]]
            [cljs.core.async :as async :refer [put!]]
            [gravie-member-client.utils :as utils :refer [mlog]]
            [goog.net.XhrLike :as xhr-like]))

(defmulti api-event
  (fn [message status data state] [message status]))

(defmethod api-event :default
  [message status data state]
  (mlog "default api event for message" message status))

(defn medical-coverage-details [channel coverage-needs]
  (POST "/interview/api/medicalCoverageNeeds"
        {:response-format :json
         :format :json
         :keywords? true
         :params coverage-needs
         :handler #(put! channel [:medical-coverage-needs :success %])}))

(defn get-counties-for-zip-code [channel zip-code]
  (GET "/county/list"
        {:response-format :json
         :keywords? true
         :params {:zipCode zip-code}
         :handler #(put! channel [:get-counties-for-zip-code :success %])}))
