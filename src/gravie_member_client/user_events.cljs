(ns gravie-member-client.user-events
  (:require [gravie-member-client.utils :as utils :refer [mlog]]
            [gravie-member-client.local-storage :as local-storage]))

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
  (let [cleaned-state (dissoc current-state :comms)
        string-state (.stringify js/JSON cleaned-state nil 2)]
  ;(println "No user-action-event! for: " action)
    (local-storage/store-state cleaned-state)))

(defmethod user-action-state :edited-input
  [action {:keys [value path]} state]
  ;(mlog "Edited input" action  state)
  ;why do we have this assoc-in if this state is already swapped?
  (assoc-in state path value))
