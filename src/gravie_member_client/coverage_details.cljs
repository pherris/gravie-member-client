(ns gravie-member-client.coverage_details
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [gravie-member-client.async :refer [raise!]]
            [gravie-member-client.utils :as utils :refer [mlog]]
            [gravie-member-client.user-events :as user-events]
            [gravie-member-client.dom-utils :as dom-utils]
            [cljs.core.async :as async :refer [<! chan put!]]
            [clojure.string :as string])
  (:require-macros [cljs.core.async.macros :as am :refer [go alt!]]
            [gravie-member-client.utils :as utils :refer [swallow-errors]]))

(defn years-ago [from-date]
  (-> (.moment js/window) (.diff (.moment js/window from-date "MM/DD/YYYY") "years")))

(defn coverage-add [app-data owner]
  (reify om/IRender
    (render [_]
      (dom/div {:className "coverage-add"}
        (dom/button {:className "btn-bigadd" :type "button"} "Add a Dependant")))))

(defn coverage-participant-heading [participant owner]
  (let [{:keys [:member :first-name :last-name :birth-date :gender :tobacco :errors]} participant]
    (reify om/IRender
      (render [_]
        (dom/div {:className (dom-utils/include-error-class "panel-heading clearfix" errors)}
          (dom/label {:className "panel-title checkbox active"} ;; handle toggling active
            (om/build dom-utils/form-checkbox-sprite "")
            (om/build dom-utils/form-checkbox {:checked true :disabled true })
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
        participants-error (dom-utils/get-error :participants app-state)]
    (reify
      om/IRender
        (render [_]
          (dom/div {:className (dom-utils/include-error-class "" participants-error)}
            (dom/label {:className "control-label control-label-lg"} "Tell us who needs coverage")
            (dom/div {:className "alert alert-danger error-content"} participants-error)
            (om/build-all coverage-participant (:people participants))
            (om/build coverage-add app-state))))))

(defn zip-and-county [app-state owner]
  (let [zip-code-error (dom-utils/get-error :zip-code (:coverage-details app-state))
        county-error (dom-utils/get-error :county (:coverage-details app-state))
        coverage-details (:coverage-details app-state)]
    (reify
      om/IRender
      (render [_]
        (dom/div
          (dom/hr)
          (dom/div {:className (dom-utils/include-error-class "form-group" zip-code-error)}
            (dom/label {:className "control-label col-sm-4" :for "zipCode"}
              (dom/span "ZIP Code")
              (om/build dom-utils/form-field-required-icon ""))
            (dom/div {:className "col-sm-8" }
              (om/build dom-utils/input-text {
                                     :id "zipCode"
                                     :value (:selected (:zip-code coverage-details))
                                     :on-change #(utils/edit-input owner [:coverage-details :zip-code :selected] %)})
              (dom/span {:className "error-content"} zip-code-error)))
          (dom/div {:className (dom-utils/include-error-class "form-group" county-error)}
              (dom/label {:className "control-label col-sm-4"}
                (dom/span "County"))
              (dom/div {:className "col-sm-8"}
                (om/build dom-utils/select-box {
                                       :options (:available-counties (:county coverage-details))
                                       :selected (:selected (:county coverage-details))})
                (dom/span {:className "error-content"} county-error)))
          (dom/hr))))))

(defn coverage-details [app-state owner]
  (let [plan-coverage-date-error (dom-utils/get-error :plan-coverage-date (:coverage-details app-state))]
    (reify
      om/IRender
      (render [_]
        (dom/div {:className "form-horizontal"}
          (dom/div {:className (dom-utils/include-error-class "form-group" plan-coverage-date-error)}
            (dom/label {:className "control-label col-sm-4"}
              (om/build dom-utils/glossary-term ["Requested Start Date"])
              (om/build dom-utils/form-field-required-icon [""])) ;;no-arg component?
            (dom/div {:className "col-sm-8"}
              (om/build dom-utils/select-box {
                                     :name "planCoverageDate"
                                     :options (get-in app-state [:coverage-details :plan-coverage-date :available-dates])
                                     :selected (get-in app-state [:coverage-details :plan-coverage-date :selected])})
              (dom/span {:className "error-content"} plan-coverage-date-error)))
          (om/build zip-and-county app-state))))))
