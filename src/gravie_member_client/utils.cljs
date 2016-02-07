(ns gravie-member-client.utils
  (:refer-clojure :exclude [uuid])
  (:require [gravie-member-client.async :refer [raise!]]
            [cljs.core.async :refer [<! >! put! chan]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn mlog [& messages]
  (.apply (.-log js/console) js/console (clj->js messages)))

(defn edit-input
  "Meant to be used in a react event handler, usually for the :on-change event on input.
  Path is the vector of keys you would pass to assoc-in to change the value in state,
  event is the Synthetic React event. Pulls the value out of the event.
  Optionally takes :value as a keyword arg to override the event's value"
  [owner path event & {:keys [value]
                       :or {value (.. event -target -value)}}]
  (println "Setting value for path" path "to" value)
  (raise! owner {:action :edited-input :path path :value value}))
