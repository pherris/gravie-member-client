(ns ^:figwheel-always gravie-member-client.core
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [gravie-member-client.async :refer [raise!]]
            [gravie-member-client.user-events :as user-events]
            [gravie-member-client.coverage-details :as coverage-details]
            [gravie-member-client.footer :as footer]
            [gravie-member-client.utils :as utils :refer [mlog]]
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
  (atom
   (merge
    {:comms {:nav (chan)
             :api (chan)
             :user-event (chan)}}
    init-page-state)))

;; (defn api-handler
;;   [value state]
;;   (println "received api request")
;;   (swallow-errors
;;    (let [message (first value)
;;          status (second value)
;;          api-data (nth value 2)]
;;      (api/api-event message status api-data state))))

;; (defn nav-handler
;;   [navigation-point state history]
;;   (println "received nav message" navigation-point)
;;   (swallow-errors
;;    (let [args nil]
;;      (if (and (nil? (-> @state :auth :kat-token))
;;               (not= (:page navigation-point) :home))
;;        ;;todo: save args and deep redirect after auth
;;        (do (.setToken history ""))
;;        (do
;;          (swap! state #(assoc %
;;                               :page nil
;;                               :page-errors nil
;;                               :page-data nil))
;;          (swap! state (partial nav/navigated-to-state navigation-point args))
;;          (nav/navigated-to-action navigation-point args @state))))))

(defn user-action-handler
  [{:keys [action] :as message} state history]
  (println "TEST")
  (mlog "User-action-handler called with action" action " message " message)
  (swallow-errors
   (let [previous-state @state]
     (println "user-action-handler" previous-state)
     (swap! state (partial user-events/user-action-state action message))
     (user-events/user-action-event! action message previous-state @state history))))

(defn ^:export setup! [state]
  (let [api-ch (-> @state :comms :api)
        user-event-ch (-> @state :comms :user-event)]

    (go (while true
          (alt!
            user-event-ch ([message] (user-action-handler message state nil)))))))

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

(setup! app-state)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

(setup! app-state)
