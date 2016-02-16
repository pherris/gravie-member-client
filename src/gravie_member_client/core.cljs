(ns ^:figwheel-always gravie-member-client.core
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [gravie-member-client.async :refer [raise!]]
            [gravie-member-client.user-events :as user-events]
            [gravie-member-client.api :as api]
            [gravie-member-client.coverage-details :as coverage-details]
            [gravie-member-client.footer :as footer]
            [gravie-member-client.utils :as utils :refer [mlog]]
            [gravie-member-client.local-storage :as local-storage]
            [cljs.core.async :as async :refer [<! chan put!]]
            [clojure.string :as string]
            [camel-snake-kebab.core :as csk]
            [camel-snake-kebab.extras :refer [transform-keys]])
  (:require-macros [cljs.core.async.macros :as am :refer [go alt!]]
                   [gravie-member-client.util :as util :refer [swallow-errors]]))

(enable-console-print!)

(defonce init-page-state
  (-> js/gravie
      (aget "jsModel")
      (js->clj)
      (->> (transform-keys csk/->kebab-case-keyword))))

(defonce app-state
  (let [state (or
                (local-storage/fetch-state)
                init-page-state)]
    (atom
     (merge
       {:comms {:nav (chan)
             :api (chan)
             :user-event (chan)}}
        state))))

(defn api-handler
  [value state]
  (println "received api request")
  (swallow-errors
   (let [message (first value)
         status (second value)
         api-data (nth value 2)]
     (api/api-event message status api-data state))))

(defn user-action-handler
  [{:keys [action] :as message} state history]
  (mlog "User-action-handler called with action" action " message " message)
   (let [previous-state @state]
     (swap! state (partial user-events/user-action-state action message))
     (user-events/user-action-event! action message previous-state @state history)))

(defn ^:export setup! [state]
  (let [api-ch (-> @state :comms :api)
        user-event-ch (-> @state :comms :user-event)]

    (go (while true
          (alt!
            user-event-ch ([message] (user-action-handler message state nil))
            api-ch ([message] (api-handler message state)))))))

;look at the instrument method in root to log numbers of times things change
(println "jsModel:" (-> js/gravie
                        (aget "jsModel")
                        (js->clj)
                        (->> (transform-keys csk/->kebab-case-keyword))))

(om/root coverage-details/coverage-details app-state
         {:target (. js/document (getElementById "coverageDetails"))
          :shared {:comms (-> @app-state :comms)}})

(om/root coverage-details/coverage-participants app-state
         {:target (. js/document (getElementById "coverageParticipants"))
                    :shared {:comms (-> @app-state :comms)}})

(om/root footer/step-footer app-state
         {:target (. js/document (getElementById "stepFooter"))
                    :shared {:comms (-> @app-state :comms)}})


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

(setup! app-state)
