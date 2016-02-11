(ns gravie-member-client.dom-utils
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]])
  (:refer-clojure :exclude [uuid]))

(defn get-error [path state]
  (vec (get-in state (into [:errors] path))))

(defn include-error-class [current-classes error-object]
  "if the collection is not empty and any items in the collection are not nil, return a new string with ' has-error' appended to 'current-classes'"
  (let [errors (vec error-object)]
    (str current-classes (if (and (not-any? nil? errors) (not-empty errors)) " has-error" ""))))

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
      (html
       [:span.has-error-icon.glyphicon.glyphicon-exclamation-sign]))))

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
  (reify om/IRender
    (render [_]
      (let [options (:options data)
            value (:value data)
            on-change (:on-change data)]
        (dom/select {:name "form-container" :className "form-control form-66 angular ng-pristine ng-valid ng-touched" :value value :on-change on-change }
          (for [option options]
            (let [value (:value option)
                  display (:display option)]
              (dom/option {:value value} display))))))))

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
    (dom/div {:className "error-content"} error-message))))

(defn form-binary [config owner]
  (let [option-one (:option-one config)
        option-two (:option-two config)]
    (reify om/IRender (render [_]
      (dom/div {:className "form-binary form-control angular"}
        (dom/label {:className (if (= (:value option-one) (:value config)) "active" "")}
          (om/build input-radio {
                                :name "gender"
                                :value (:value option-one)
                                :className "ng-pristine ng-untouched ng-valid ng-valid-required"
                                :on-click (:on-click option-one)}) (:display option-one))
        (dom/label {:className (if (= (:value config) (:value option-two)) "active" "")}
          (om/build input-radio {
                                :name "gender"
                                :value (:value option-two)
                                :className "ng-pristine ng-untouched ng-valid ng-valid-required"
                                :on-click (:on-click option-two)}) (:display option-two)))))))
