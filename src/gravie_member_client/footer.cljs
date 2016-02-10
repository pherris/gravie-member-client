(ns gravie-member-client.footer
  (:require [om.core :as om :include-macros true]
            [gravie-member-client.async :refer [raise!]]
            [gravie-member-client.utils :as utils :refer [mlog]]
            [gravie-member-client.user-events :as user-events]
            [gravie-member-client.dom-utils :as dom-utils]
            [cljs.core.async :as async :refer [<! chan put!]]
            [clojure.string :as string]
            [sablono.core :as html :refer-macros [html]])
  (:require-macros [cljs.core.async.macros :as am :refer [go alt!]]
            [gravie-member-client.util :as util :refer [swallow-errors]]))

(defn step-footer [app-state owner]
  (reify
    om/IDisplayName
    (display-name [_] "Step Footer")

    om/IRender
    (render [_]
      (html 
       [:div.row.form-submit
        [:div.col-xs-6.text-right.pull-right
         [:button.btn.btn-primary.pull-right#footerNext {:name "next"
                                                         :type "button"
                                                         :value "Next"
                                                         :on-click #(raise! owner {:action :continue-clicked})}
          "Continue"]]
        [:div.col-xs-6
         [:a.btn.btn-info.no-fill {:href "foo"}
          "BACK"]]]))))
