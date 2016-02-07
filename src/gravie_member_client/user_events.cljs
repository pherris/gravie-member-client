(ns gravie-member-client.user-events
  (:require [gravie-member-client.utils :as utils :refer [mlog]]))

(defmulti user-action-state
  (fn [action message state] action))

(defmethod user-action-state :default
  [action message state]
  (println "No user-action-state for: " action)
  state)

(defmulti user-action-event!
  (fn [action message previous-state current-state history] action))

(defmethod user-action-event! :default
  [action message previous-state current-state history]
  (println "No user-action-event! for: " action))

(defmethod user-action-state :edited-input
  [action {:keys [value path]} state]
  (mlog "Edited input")
  (mlog "Current state of path" (get-in state path))
  (assoc-in state path value))
