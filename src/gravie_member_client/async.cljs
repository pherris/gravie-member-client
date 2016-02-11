(ns gravie-member-client.async
  (:require [om.core :as om :include-macros true]
            [cljs.core.async :refer [put!]]))

(defn raise! [owner message]
  (let [chan (om/get-shared owner [:comms :user-event])]
    (put! chan message))
  ;;react does not like returning boolean false from event handlers
  nil)

