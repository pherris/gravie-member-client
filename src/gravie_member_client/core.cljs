(ns ^:figwheel-always gravie-member-client.core
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [clojure.string :as string]))

(enable-console-print!)

(def app-state
  (atom
    {:people
     [{:type :student :first "Ben" :last "Bitdiddle" :email "benb@mit.edu"}
      {:type :student :first "Alyssa" :middle-initial "P" :last "Hacker"
       :email "aphacker@mit.edu"}
      {:type :professor :first "Gerald" :middle "Jay" :last "Sussman"
       :email "metacirc@mit.edu" :classes [:6001 :6946]}
      {:type :student :first "Eva" :middle "Lu" :last "Ator" :email "eval@mit.edu"}
      {:type :student :first "Louis" :last "Reasoner" :email "prolog@mit.edu"}
      {:type :professor :first "Hal" :last "Abelson" :email "evalapply@mit.edu"
       :classes [:6001]}]
     :classes
     {:6001 "The Structure and Interpretation of Computer Programs"
      :6946 "The Structure and Interpretation of Classical Mechanics"
      :1806 "Linear Algebra"}}))

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
            (om/build-all form-field-required-icon [""]))
          (dom/div {:className "col-sm-8" }
            (om/build-all input-text [{
                                        :id "zipCode"
                                        :value "55104"
                                        }])))
        (dom/div {:className "form-group"}
            (dom/label {:className "control-label col-sm-4"}
              (dom/span "County"))
            (dom/div {:className "col-sm-8"}
              (om/build-all select-box [{:options ["Hennepin" "Ramsey"]}])))))))

(defn coverage-details [data owner]
  (reify
    om/IRender
    (render [_]
      (dom/div {:className "form-container"}
        (dom/div {:className "form-group"}
          (dom/label {:className "control-label col-sm-4"}
            (om/build-all glossary-term ["Requested Start Date"])
            (om/build-all form-field-required-icon [""])) ;;can I pass no arg to a component?
          (dom/div {:className "col-sm-8"}
            (om/build-all select-box [{
                                        :name "planCoverageDate"
                                        :options ["Select..." "3/1/2016" "4/1/2014"]}])))
        (om/build-all zip-and-county [""])))))

(om/root coverage-details app-state
         {:target (. js/document (getElementById "coverageDetails"))})

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
