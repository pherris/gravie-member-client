(ns ^:figwheel-always gravie-member-client.core
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [gravie-member-client.async :refer [raise!]]
            [gravie-member-client.utils :as utils :refer [mlog]]
            [gravie-member-client.user-events :as user-events]
            [cljs.core.async :as async :refer [<! chan put!]]
            [clojure.string :as string])
  (:require-macros [cljs.core.async.macros :as am :refer [go alt!]]
            [gravie-member-client.utils :as utils :refer [swallow-errors]]))

(enable-console-print!)

(def app-state
  (atom
    { :comms {:nav (chan)
                 :api (chan)
                 :user-event (chan)}
      :coverage-details {
                         :plan-coverage-date {
                                               :available-dates ["Select..." "3/1/2016" "4/1/2014" "5/1/2014"]
                                               :selected "3/1/2016"
                                               :errors ["some error"]}
                         :zip-code {
                                     :selected "55104"
                                     :errors nil}
                         :county {
                                   :selected "Ramsey"
                                   :available-counties ["Ramsey" "Hennepin"]
                                   :errors nil}
                         }
     :participants {:people [{
                       :member true
                       :first-name "Henry"
                       :last-name "Wingbanger"
                       :birth-date "8/13/1955"
                       :gender nil
                       :tobacco true
                       :errors nil }
                      {
                       :member false
                       :first-name "Mary"
                       :last-name "Wingbanger"
                       :birth-date "9/21/1955"
                       :gender "Female"
                       :tobacco false
                       :errors nil }
                      {
                       :member false
                       :first-name "Child"
                       :last-name "Wingbanger"
                       :birth-date nil
                       :gender nil
                       :tobacco nil
                       :errors nil }]
                    :errors nil ;;["Please provide only 1 spouse or domestic partner"]
                    }}))


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




(defn get-error [field app-state]
  (get-in app-state [field :errors]))

(defn include-error-class [current-classes error-object]
  (str current-classes " " (if error-object "has-error" "")))

(defn glossary-term [data owner]
  (reify
    om/IRender
    (render [_]
      (dom/a
        #js {:className "query"}
        data))))

(defn years-ago [from-date]
  (-> (.moment js/window) (.diff (.moment js/window from-date "MM/DD/YYYY") "years")))

(defn form-field-required-icon [data owner]
  (reify
    om/IRender
    (render [_]
      (dom/span {:className "has-error-icon glyphicon glyphicon-exclamation-sign"} ""))))

(defn form-checkbox-sprite [_ owner]
  (reify
    om/IRender
    (render [_]
      (dom/span {:className "form-checkbox-sprite"} ""))))

(defn form-checkbox [field owner]
  (reify
    om/IRender
    (render [_]
      (dom/input {:type "checkbox" :className "ng-pristine ng-untouched ng-valid" :disabled (:disabled field) :checked (:checked field)}))))

(defn select-box [data owner]
  (let [options (:options data)
        selected (:selected data)]
    (reify
      om/IRender
      (render [_]
        (dom/select {:name "form-container" :className "form-control form-66 angular ng-pristine ng-valid ng-touched"}
          (for [option options]
            (dom/option (when (= selected option) {:selected true}) option)))))))

(defn input-text [data owner]
  (reify
    om/IRender
    (render [_]
      (dom/input {
                   :type "text"
                   :className "form-control form-33 angular ng-pristine ng-untouched ng-valid ng-valid-maxlength"
                   :id (:id data)
                   :value (:value data)
                   :on-change (:on-change data)}))))

(defn coverage-add [app-data owner]
  (reify om/IRender
    (render [_]
      (dom/div {:className "coverage-add"}
        (dom/button {:className "btn-bigadd" :type "button"} "Add a Dependant")))))

(defn coverage-participant-heading [participant owner]
  (let [{:keys [:member :first-name :last-name :birth-date :gender :tobacco :errors]} participant]
    (reify om/IRender
      (render [_]
        (dom/div {:className (include-error-class "panel-heading clearfix" errors)}
          (dom/label {:className "panel-title checkbox active"} ;; handle toggling active
            (om/build form-checkbox-sprite "")
            (om/build form-checkbox {:checked true :disabled true })
            (dom/span [first-name " " last-name] ))
          (dom/div {:className "panel-subtitle text-center pl0 pr0"} "") ;;need to handle male/female icon
          (dom/div {:className "col-xs-12 col-sm-7"}
            (dom/div {:className "col-md-6"}
              (dom/ul {:className "list-unstyled"}
                (dom/li
                  (dom/span birth-date)
                  (dom/span " (" (years-ago birth-date) ")"))
                (dom/li
                  (dom/span {:className "grey"} (if (= member true) "You!" "Other!"))))) ;;add relationship to the person object
            (dom/div {:className "col-md-6"}
              (dom/ul {:className "list-unstyled"}
                (dom/li
                  (dom/span "Tobacco:" " " (str tobacco)))))))))))

(defn coverage-participant [participant owner]
  (let [{:keys [:member :first-name :last-name :birth-date :gender :tobacco :errors]} participant] ;;may not need
    (reify
      om/IRender
        (render [_]
          (dom/div {:className "panel-list coverage-list"}
            (dom/div {:className "panel-list coverage-list"}
              (dom/div {:className "panel"}
                (om/build coverage-participant-heading participant))))))))

(defn coverage-participants [app-state owner]
  (let [participants (:participants app-state)
        participants-error (get-error :participants app-state)]
    (reify
      om/IRender
        (render [_]
          (dom/div {:className (include-error-class "" participants-error)}
            (dom/label {:className "control-label control-label-lg"} "Tell us who needs coverage")
            (dom/div {:className "alert alert-danger error-content"} participants-error)
            (om/build-all coverage-participant (:people participants))
            (om/build coverage-add app-state))))))

(defn zip-and-county [app-state owner]
  (let [zip-code-error (get-error :zip-code (:coverage-details app-state))
        county-error (get-error :county (:coverage-details app-state))
        coverage-details (:coverage-details app-state)]
    (reify
      om/IRender
      (render [_]
        (dom/div
          (dom/hr)
          (dom/div {:className (include-error-class "form-group" zip-code-error)}
            (dom/label {:className "control-label col-sm-4" :for "zipCode"}
              (dom/span "ZIP Code")
              (om/build form-field-required-icon ""))
            (dom/div {:className "col-sm-8" }
              (om/build input-text {
                                     :id "zipCode"
                                     :value (:selected (:zip-code coverage-details))
                                     :on-change #(utils/edit-input owner [:coverage-details :zip-code :selected] %)})
              (dom/span {:className "error-content"} zip-code-error)))
          (dom/div {:className (include-error-class "form-group" county-error)}
              (dom/label {:className "control-label col-sm-4"}
                (dom/span "County"))
              (dom/div {:className "col-sm-8"}
                (om/build select-box {
                                       :options (:available-counties (:county coverage-details))
                                       :selected (:selected (:county coverage-details))})
                (dom/span {:className "error-content"} county-error)))
          (dom/hr))))))

(defn coverage-details [app-state owner]
  (let [plan-coverage-date-error (get-error :plan-coverage-date (:coverage-details app-state))]
    (reify
      om/IRender
      (render [_]
        (dom/div {:className "form-horizontal"}
          (dom/div {:className (include-error-class "form-group" plan-coverage-date-error)}
            (dom/label {:className "control-label col-sm-4"}
              (om/build glossary-term ["Requested Start Date"])
              (om/build form-field-required-icon [""])) ;;no-arg component?
            (dom/div {:className "col-sm-8"}
              (om/build select-box {
                                     :name "planCoverageDate"
                                     :options (get-in app-state [:coverage-details :plan-coverage-date :available-dates])
                                     :selected (get-in app-state [:coverage-details :plan-coverage-date :selected])})
              (dom/span {:className "error-content"} plan-coverage-date-error)))
          (om/build zip-and-county app-state))))))

(om/root coverage-details app-state
         {:target (. js/document (getElementById "coverageDetails"))
          :shared {:comms (-> @app-state :comms)}})

(om/root coverage-participants app-state
         {:target (. js/document (getElementById "coverageParticipants"))})

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
