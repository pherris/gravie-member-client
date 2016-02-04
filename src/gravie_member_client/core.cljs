(ns ^:figwheel-always gravie-member-client.core
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [clojure.string :as string]))

(enable-console-print!)

(def app-state
  (atom
    {:coverage-details {
                         :available-dates ["Select..." "3/1/2016" "4/1/2014" "5/1/2014"]
                         :zip-code "55104"
                         :counties ["Ramsey" "Hennepin"]
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
  (reify
    om/IRender
    (render [_]
      (dom/select {:name "form-container" :className "form-control form-66 angular ng-pristine ng-valid ng-touched"}
        (for [option (:options data)]
          (dom/option option))))))

(defn input-text [data owner]
  (reify
    om/IRender
    (render [_]
      (dom/input {
                   :type "text"
                   :className "form-control form-33 angular ng-pristine ng-untouched ng-valid ng-valid-maxlength"
                   :id (:id data)
                   :value (:value data)}))))

(defn zip-and-county [data owner]
  (reify
    om/IRender
    (render [_]
      (dom/div
        (dom/hr)
        (dom/div {:className "form-group"}
          (dom/label {:className "control-label col-sm-4" :for "zipCode"}
            (dom/span "ZIP Code")
            (om/build form-field-required-icon ""))
          (dom/div {:className "col-sm-8" }
            (om/build input-text {
                                   :id "zipCode"
                                   :value (:zip-code (:coverage-details data))})))
        (dom/div {:className "form-group"}
            (dom/label {:className "control-label col-sm-4"}
              (dom/span "County"))
            (dom/div {:className "col-sm-8"}
              (om/build select-box {:options ["Hennepin" "Ramsey"]})))
        (dom/hr)))))

(defn coverage-details [data owner]
  (reify
    om/IRender
    (render [_]
      (dom/div {:className "form-container"}
        (dom/div {:className "form-horizontal"}
          (dom/div {:className "form-group"}
            (dom/label {:className "control-label col-sm-4"}
              (om/build glossary-term ["Requested Start Date"])
              (om/build form-field-required-icon [""])) ;;can I pass no arg to a component?
            (dom/div {:className "col-sm-8"}
              (om/build select-box {
                                     :name "planCoverageDate"
                                     :options (:available-dates (:coverage-details data))})))
          (om/build zip-and-county data))))))

(om/root coverage-details app-state
         {:target (. js/document (getElementById "coverageDetails"))})

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
