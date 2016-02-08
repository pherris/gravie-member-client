(ns gravie-member-client.dom-utils
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true])
  (:refer-clojure :exclude [uuid]))

(defn get-error [field app-state]
  (get-in app-state [field :errors]))

(defn include-error-class [current-classes error-object]
  (let [errors (if (list? error-object) error-object (list error-object))]
    (str current-classes (if (not-any? nil? errors) " has-error" ""))))

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
              (println selected options)
        (dom/select {:name "form-container" :className "form-control form-66 angular ng-pristine ng-valid ng-touched"}
          (for [option options]
            (dom/option (when (= selected option) {:selected true}) option)))))))

(defn input-text [data owner]
  (reify
    om/IRender
    (render [_]
      (dom/input (merge {
                   :type "text"
                   :className "form-control form-33 angular ng-pristine ng-untouched ng-valid ng-valid-maxlength"} data))))) ;default classes incorrect

(defn input-radio [data owner]
  (reify
    om/IRender
    (render [_]
      (dom/input (merge {
                   :type "radio"} data)))))

(defn error-div [error-message owner]
  (reify om/IRender (render [_]
    (dom/div {:className "error-content ng-hide"} error-message))))

(defn form-binary [config owner]
  (let [option-one (:option-one config)
        option-two (:option-two config)]
    (reify om/IRender (render [_]
      (dom/div {:className "form-binary form-control angular"}
        (dom/label {:className (if (= (:value option-one) (:value config)) "active" "")}
          (om/build input-radio {
                                :name "gender"
                                :value (:value option-one)
                                :className "ng-pristine ng-untouched ng-valid ng-valid-required" }) (:display option-one))
        (dom/label {:className (if (= (:value config) (:value option-two)) "active" "")}
          (om/build input-radio {
                                :name "gender"
                                :value (:value option-two)
                                :className "ng-pristine ng-untouched ng-valid ng-valid-required" }) (:display option-two)))))))
