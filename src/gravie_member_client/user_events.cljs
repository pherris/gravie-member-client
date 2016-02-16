(ns gravie-member-client.user-events
  (:require [gravie-member-client.utils :as utils :refer [mlog]]
            [gravie-member-client.local-storage :as local-storage]))

(defn incremented-state-history [state]
  (let [cnt (+ 1 (count (local-storage/fetch :state [])))]
    (-> state
        (assoc-in [:history :revision] cnt))))

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
    (println "No user-action-event! for: " action)
    (local-storage/store-state cleaned-state)))

(defmethod user-action-state :edited-input
  [action {:keys [value path]} state]
  ;(mlog "Edited input" action  state)
  (assoc-in (incremented-state-history state) path value))

(defmethod user-action-state :history-change
  [action {:keys [value path]} state]
  "swaps out the history with an older version from local-storage"
  (mlog "Edited history" action state (- value 1))
  (local-storage/fetch-state :index (- value 1)))

(defmethod user-action-event! :history-change
  [action message previous-state current-state history]
  (println "No user-action-event! for: " action))
