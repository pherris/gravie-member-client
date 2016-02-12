(ns gravie-member-client.coverage-details
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [gravie-member-client.async :refer [raise!]]
            [gravie-member-client.api :as api :refer [api-event]]
            [gravie-member-client.utils :as utils :refer [mlog]]
            [gravie-member-client.user-events :as user-events :refer [user-action-event!]]
            [gravie-member-client.dom-utils :as dom-utils]
            [cljs.core.async :as async :refer [<! chan put!]]
            [clojure.string :as string]
            [cljs-time.core :as t]
            [cljs-time.coerce :as coerce]
            [cljs-time.format :as format]
            [sablono.core :as html :refer-macros [html]]
            [schema.core :as s]
            [schema.spec.core :as spec :include-macros true]
            [schema.utils :as s-utils])
  (:require-macros [cljs.core.async.macros :as am :refer [go alt!]]
            [gravie-member-client.util :as util :refer [swallow-errors]]))

(defmethod user-action-event! :continue-clicked
  [action {destination :destination :as message} previous-state current-state history]
  (mlog "Continue clicked")
  (api/medical-coverage-details (-> current-state :comms :api) {:foo "bar"}))

(defmethod api-event [:medical-coverage-needs :success]
  [message status data state]
  (mlog "medical-coverage-needs api event")
  (println data)
  state)

(defmethod user-action-event! :continue-clicked
  [action {destination :destination :as message} previous-state current-state history]
  (mlog "Continue clicked")
  (api/medical-coverage-details (-> current-state :comms :api) {:foo "bar"}))

(def Participant-schema
  "A schema for a person"
  {:first-name s/Str
   :last-name s/Str
   :birth-date s/Str
   :gender s/Str
   :is-tobacco-user s/Bool
   :relationship-type (s/maybe s/Str)
   (s/optional-key :existing-interview-products) [(s/maybe s/Str)]
   (s/optional-key :existing-product-types) [(s/maybe s/Str)]
   (s/optional-key :is-removable) s/Bool
   (s/optional-key :id) s/Int
   (s/optional-key :ssn-last-four) (s/maybe s/Str)
   :index s/Int
   (s/optional-key :ssn) (s/maybe s/Str)
   (s/optional-key :is-member) s/Bool
   (s/optional-key :is-editing) (s/maybe s/Bool)
   (s/optional-key :errors) (s/maybe s/Str)})

(defn clear-errors [owner participant]
  "clears all errors on the participant"
  (utils/edit-input owner [:errors :participants :people (:index participant)] nil :value {}))

(defn done-editing! [owner participant errorObject]
  "Takes the results of a plumatic schema s/check and adds errors to the participant"
  (println "calling done-editing" errorObject)
  (let [errors (keys (dissoc errorObject :errors))]
    (println errors)
    (if (nil? errors)
      (do
        (utils/edit-input owner [:participants :people (:index participant) :is-editing] nil :value false) ;need to clear all errors here too
        (clear-errors owner participant))
      (doseq [error-key errors]
        (utils/edit-input owner [:errors :participants :people (:index participant) error-key] nil :value (str "error: " error-key))))))

(defn years-ago [from-date]
  (let [from (coerce/from-string from-date)]
    (if (nil? from)
      "0"
      (-> (t/interval from (t/now))
        (t/in-years)))))

(defn format-date [date]
  (if-let [d (coerce/from-string date)]
    (format/unparse (format/formatter "MM/d/yyyy") d)
    date))

(defn coverage-add [app-state owner]
  (reify om/IRender
    (render [_]
      (let [next-participant (count (get-in app-state [:participants :people]))]
        (println "next participant is: " next-participant (get-in app-state [:participants :people]))
        (dom/div {:className "coverage-add"}
          (dom/button {
                        :className "btn-bigadd"
                        :type "button"
                        :value {:first-name "test"}
                        :on-click #(utils/edit-input owner [:participants :people next-participant] % :value {:is-member false})} "Add a Dependant"))))))

(defn coverage-participant-heading [participant owner]
  (reify om/IRender
    (render [_]
      (let [{:keys [:is-member :first-name :last-name :birth-date :gender :is-tobacco-user :errors]} participant]
        (dom/div
          (dom/div {:className (dom-utils/include-error-class "panel-heading clearfix" (:errors errors))}
            (dom/label {:className "panel-title checkbox active"} ;; handle toggling active
              (om/build dom-utils/form-checkbox-sprite "")
              (om/build dom-utils/form-checkbox {:checked true :disabled true })
                (dom/span (str first-name " " last-name) ))
            (dom/div {:className "panel-subtitle text-center pl0 pr0"} "") ;;need to handle male/female icon
            (dom/div {:className "col-xs-12 col-sm-7"}
              (dom/div {:className "col-md-6"}
                (dom/ul {:className "list-unstyled"}
                  (dom/li
                    (dom/span birth-date)
                    (dom/span " (" (years-ago birth-date) ")"))
                  (dom/li
                    (dom/span {:className "grey"} (if (= is-member true) "You!" "Other!"))))) ;;add relationship to the person object
              (dom/div {:className "col-md-6"}
                (dom/ul {:className "list-unstyled"}
                  (dom/li
                    (dom/span "Tobacco:" " " (str is-tobacco-user)))))))
            (dom/div {:className (dom-utils/include-error-class "mb0" errors)}
              (om/build-all dom-utils/component-error errors)))))))

(defn coverage-participant-form [participant owner]
  (reify om/IRender
    (render [_]
      (let [first-name-error (:first-name (:errors participant))
            last-name-error (:last-name (:errors participant))
            birth-date-error (:birth-date (:errors participant))
            gender-error (:gender (:errors participant))
            relationship-type-error (:relationship-type (:errors participant))
            tobacco-error (:is-tobacco-user (:errors participant))
            birth-date (format-date (:birth-date participant))
            is-editing (:is-editing participant)
            {:keys [:is-member :first-name :last-name :gender :is-tobacco-user :errors :index :relationship-type]} participant]
          (dom/div {:className (str "panel-body pb0" (if (or (nil? is-editing) is-editing) "" " hide"))}
            (dom/form {:className "form-horizontal ng-pristine ng-valid ng-valid-required ng-valid-maxlength ng-valid-invalid"}
              (dom/div {:className (dom-utils/include-error-class "form-group" [first-name-error last-name-error])}
                (dom/label {:className "control-label col-sm-2"} "Name *")
                (dom/div {:className "col-sm-8 p0"}
                  (dom/div {:className (dom-utils/include-error-class "col-sm-6 mb0" first-name-error)}
                    (dom/label {:className "sr-only" :for "firstName"} "First Name")
                    (om/build dom-utils/input-text {
                                           :name "firstName"
                                           :id "firstName"
                                           :className "form-control angular ng-pristine ng-untouched ng-valid ng-valid-required ng-valid-maxlength"
                                           :placeholder "First"
                                           :maxLength 50
                                           :required true
                                           :value first-name
                                           :on-change #(utils/edit-input owner [:participants :people index :first-name] %)}) ;how to identify the individual person?
                    (om/build dom-utils/field-error first-name-error))
                  (dom/div {:className (dom-utils/include-error-class "col-sm-6 p0 mb0" last-name-error)}
                    (dom/label {:className "sr-only" :for "lastName"} "Last Name")
                    (om/build dom-utils/input-text {
                                           :name "lastName"
                                           :id "lastName"
                                           :className "form-control angular ng-pristine ng-untouched ng-valid ng-valid-required ng-valid-maxlength"
                                           :placeholder "Last"
                                           :maxLength 50
                                           :required true
                                           :value last-name
                                           :on-change #(utils/edit-input owner [:participants :people index :last-name] %)})
                    (om/build dom-utils/field-error last-name-error))))
                (dom/div {:className (dom-utils/include-error-class "form-group" birth-date-error)}
                  (dom/span
                    (dom/label {:className "control-label col-sm-2 whs-nw" :for "birthDate"} "Birth Date *")
                    (dom/div {:className "col-sm-4"}
                      (om/build dom-utils/input-text {
                                           :name "birthDate"
                                           :id "birthDate"
                                           :className "form-control angular ng-pristine ng-untouched ng-valid ng-valid-required ng-valid-maxlength"
                                           :placeholder "mm/dd/yyyy"
                                           :maxLength 10
                                           :required true
                                           :value (format-date birth-date)
                                           :on-change #(utils/edit-input owner [:participants :people index :birth-date] %)})
                      (om/build dom-utils/field-error birth-date-error)))
                  (if (= is-member false)
                    (dom/div {:className "d-i"}
                      (dom/label {:className "control-label col-sm-2" :for "relationshipType"} "Relation *")
                      (dom/div {:className "col-sm-4"}
                        (om/build dom-utils/select-box {
                                       :options [{ :value "?" }
                                                 { :value "SPOUSE" :display "Spouse"}
                                                 { :value "DOMESTIC_PARTNER" :display "Domestic Partner"}
                                                 { :value "CHILD" :display "Child"}]
                                       :value relationship-type
                                       :on-change #(utils/edit-input owner [:participants :people index :relationship-type] %)})
                        (om/build dom-utils/field-error relationship-type-error)))
                    (dom/span)))
                (dom/div {:className "form-group"}
                  (dom/div {:className (dom-utils/include-error-class "d-i" gender-error)}
                    (dom/label {:className "control-label col-sm-2" :for "gender"} "Gender *")
                    (dom/div {:className "col-sm-4"}
                      (om/build dom-utils/form-binary {
                                            :value gender
                                            :option-one {
                                                          :name "gender"
                                                          :on-click #(utils/edit-input owner [:participants :people index :gender] %)
                                                          :display "Male"
                                                          :value "MALE" }
                                            :option-two {
                                                          :name "gender"
                                                          :on-click #(utils/edit-input owner [:participants :people index :gender] %)
                                                          :display "Female"
                                                          :value "FEMALE" }})
                      (om/build dom-utils/field-error gender-error)))
                  (dom/div {:className (dom-utils/include-error-class "d-i" tobacco-error)}
                    (dom/label {:className "control-label col-sm-2" :for "gender"} "Tobacco *")
                    (dom/div {:className "col-sm-4"}
                      (om/build dom-utils/form-binary {
                                            :value is-tobacco-user
                                            :option-one {
                                                          :name "tobacco"
                                                          :on-click #(utils/edit-input owner [:participants :people index :is-tobacco-user] nil :value true)
                                                          :display "Yes"
                                                          :value true
                                                          }
                                            :option-two {
                                                          :name "tobacco"
                                                          :on-click #(utils/edit-input owner [:participants :people index :is-tobacco-user] nil :value false)
                                                          :display "No"
                                                          :value false
                                                          }})
                      (om/build dom-utils/field-error tobacco-error))))
                (dom/hr)
                (dom/div {:className "row"}
                  (dom/div {:className "col-sm-12"}
                    (dom/button {:name "delete" :type "button" :className "btn btn-link red ng-hide"} "Remove")
                    (dom/a { :name "finish"
                             :className "btn btn-primary pull-right"
                             :on-click #(->> (s/check Participant-schema participant)
                                            (done-editing! owner participant))} "Done")))))))))

(defn coverage-participant [participant owner]
  (reify
    om/IRender
    (render [_]
      (dom/div {:className "panel-list coverage-list"}
        (dom/div {:className "panel"}
          (om/build coverage-participant-heading participant)
          (om/build coverage-participant-form participant))))))

(defn coverage-participants [app-state owner]
  (reify
    om/IRender
    (render [_]
      (let [participants (:participants app-state)
            participants-errors (dom-utils/get-error [:participants :errors] app-state)
            people-errors (get-in app-state [:errors :participants :people])
            ; note that people and their errors are combined here as well as being associated with the index they hold in the vector
            people-with-errors (map-indexed (fn [index participant]
                                              ;TODO combine into one assoc
                                              (let [pwe (assoc participant :errors (get people-errors index))]
                                                (println pwe)
                                                (assoc pwe :index index :relationship-type (if (= (:is-member pwe) true) "MEMBER" (:relationship-type pwe))))) (:people participants))]
          (dom/div
            (dom/div {:className (dom-utils/include-error-class "" participants-errors)}
              (dom/label {:className "control-label control-label-lg"} "Tell us who needs coverage")
              (dom/div {:className "alert alert-danger error"} participants-errors))
            (om/build-all coverage-participant people-with-errors)
            (om/build coverage-add app-state))))))

(defn zip-and-county [app-state owner]
  (reify
    om/IRender
    (render [_]
      (let [zip-code-path [:coverage-details :zip-code]
            county-path [:coverage-details :county]
            zip-code-error (dom-utils/get-error zip-code-path app-state)
            county-error (dom-utils/get-error county-path app-state)
            county (get-in app-state county-path)
            zip-code (get-in app-state zip-code-path)
            coverage-details (:coverage-details app-state)]
        (html
         [:div
          [:hr]
          [:div.form-group
           [:label.control-label.col-sm-4 {:for "zipCode"}
            [:span "ZIP Code"]
            (om/build dom-utils/form-field-required-icon "")]
           [:div.col-sm-8
            [:input.form-control.form-33#zipCode {:type "text"
                             :value zip-code
                             :on-change #(utils/edit-input owner [:coverage-details :zip-code] %)
                             }]]]
          [:div.form-group
           [:label.control-label.col-sm-4 {:for "county"}
            [:span "County"]
            (om/build dom-utils/form-field-required-icon "")]
           [:div.col-sm-8
            [:select.form-control.form-66 {:value (-> app-state :coverage-details :county-fips-code)
                                           :on-change #(utils/edit-input owner [:coverage-details :plan-coverage-date] %)}
                  (for [available-county (-> coverage-details :available-counties)]
                    [:option {:value (:fips-code available-county)
                              :key (:fips-code available-county)}
                     (:name available-county)])]
            ]]])))))

(defn coverage-details [app-state owner]
  (reify
    om/IRender
    (render [_]
      (let [plan-coverage-date-path [:coverage-details :plan-coverage-date]
            plan-coverage-date-error (dom-utils/get-error plan-coverage-date-path app-state)
            available-plan-coverage-dates (-> app-state :coverage-details :available-plan-coverage-dates)
            display-date-format (format/formatter "M/d/yyyy")]
        (html [:div.form-horizontal
               [:div.form-group
                [:label.control-label.col-sm-4
                 (om/build dom-utils/glossary-term ["Requested Start Date"])
                 (om/build dom-utils/form-field-required-icon nil)]
                [:div.col-sm-8
                 [:select.form-control.form-66 {:value (-> app-state :coverage-details :plan-coverage-date)
                                                :on-change #(utils/edit-input owner [:coverage-details :plan-coverage-date] %)}
                  [:option {:value ""} ""]
                  (for [coverage-date available-plan-coverage-dates]
                    [:option {:value coverage-date :key coverage-date} (->> coverage-date
                                                                            (coerce/from-string)
                                                                            (format/unparse display-date-format))])]

                 [:span.error-content]]]
               (om/build zip-and-county app-state)])))))
