(ns ^:figwheel-always gravie-member-client.core
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [clojure.string :as string]))

(enable-console-print!)

(def app-state
  (atom
    {:coverage-details {
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
     :participants [{
                      :member true
                      :first-name "Henry"
                      :last-name "Wingbanger"
                      :birth-date "8/13/1955"
                      :gender nil
                      :tobacco nil
                      }]}))

(defn glossary-term [data owner]
  (reify
    om/IRender
    (render [_]
      (dom/a
        #js {:className "query"}
        data))))

(defn form-field-required-icon [data owner]
  (reify
    om/IRender
    (render [_]
      (dom/span {:className "has-error-icon glyphicon glyphicon-exclamation-sign"} ""))))

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
                   :value (:value data)}))))

(defn get-error [field app-state]
  (get-in app-state [field :errors]))

(defn include-error-class [current-classes error-object]
  (str current-classes " " (if error-object "has-error" "")))

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
                                     :value (:selected (:zip-code coverage-details))})
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
        (dom/div {:className "form-container"}
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
            (om/build zip-and-county app-state)))))))

(om/root coverage-details app-state
         {:target (. js/document (getElementById "coverageDetails"))})

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
