(ns ^:figwheel-always gravie-member-client.core
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [gravie-member-client.async :refer [raise!]]
            [gravie-member-client.user-events :as user-events]
            [gravie-member-client.coverage-details :as coverage-details]
            [gravie-member-client.utils :as utils :refer [mlog]]
            [cljs.core.async :as async :refer [<! chan put!]]
            [clojure.string :as string])
  (:require-macros [cljs.core.async.macros :as am :refer [go alt!]]
            [gravie-member-client.util :as util :refer [swallow-errors]]))

(enable-console-print!)

(def app-state
  (atom
    { :comms {:nav (chan)
                 :api (chan)
                 :user-event (chan)}
      :coverage-details {
                         :plan-coverage-date "3/1/2016"
                         :available-dates ["Select..." "3/1/2016" "4/1/2014" "5/1/2014"]
                         :zip-code "55104"
                         :county "Ramsey"
                         :available-counties ["Ramsey" "Hennepin"]}
     :participants {:people [{
                       :member true
                       :first-name "Henry"
                       :last-name "Wingbanger"
                       :birth-date "8/13/1955"
                       :gender "MALE"
                       :tobacco true
                      :relationship-type nil} ;how to associate
                      {
                       :member false
                       :first-name "Mary"
                       :last-name "Wingbanger"
                       :birth-date "8/21/1954"
                       :gender "FEMALE"
                       :tobacco false
                       :relationship-type "SPOUSE"}
                      {
                       :member false
                       :first-name "child"
                       :last-name "Wingbanger"
                       :birth-date "1/03/1995"
                       :gender nil
                       :tobacco false
                       :relationship-type nil}]
                    }
      :errors {
                :coverage-details {
                                 :plan-coverage-date ["error one" "2" "basd"]
                                    }
                :participants { :errors ["BLA"] ;can we avoid this generic errors object? I think not, how else to show an error for all participants?
                                :people [{ :first-name ["so sad, your first name has an error"]
                                           :last-name ["oops, fill me in"]
                                           :birth-date ["not very good at typing are we?"]
                                           :gender ["kindof tough to get this one wrong isnt it?"]
                                           :tobacco ["these are the kinds of things you should know"]
                                           :relationship-type ["confusing in this day and age"]
                                           :errors ["bummer, you also have a generic error"]} ;'general' might be a better name so we can (get-in state [:participant :errors :general])
                                         {}
                                         { :errors ["the child has an error" "or two"]}]}}}))

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
  (mlog "User-action-handler called with action" action " message " message)
  (swallow-errors
   (let [previous-state @state]
     (swap! state (partial user-events/user-action-state action message))
     (user-events/user-action-event! action message previous-state @state history))))

(defn ^:export setup! [state]
  (let [nav-ch (-> @state :comms :nav)
        api-ch (-> @state :comms :api)
        user-event-ch (-> @state :comms :user-event)]
;;         history (routes/define-routes! state)]

    (go (while true
          (alt!
;;             nav-ch ([message] (nav-handler message state history))
;;             api-ch ([message] (api-handler message state))
            user-event-ch ([message] (user-action-handler message state nil)))))))


(om/root coverage-details/coverage-details app-state
         {:target (. js/document (getElementById "coverageDetails"))
          :shared {:comms (-> @app-state :comms)}})

(om/root coverage-details/coverage-participants app-state
         {:target (. js/document (getElementById "coverageParticipants"))})

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
