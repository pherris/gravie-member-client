(ns gravie-member-client.async
  (:require [om.core :as om :include-macros true]
            [cljs.core.async :refer [put!]]))

(defn raise! [owner message]
  (let [chan (om/get-shared owner [:comms :user-event])]
    (println "I made it to raise!" chan message)
    (put! chan message))
  ;;react does not like returning boolean false from event handlers
  nil)

