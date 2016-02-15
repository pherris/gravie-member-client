(ns gravie-member-client.sample-test
  (:require [cemerick.cljs.test :as t]
            [goog.dom]
            [sablono.core :as html :refer-macros [html]]
            [om.core :as om :include-macros true])
  (:require-macros [cemerick.cljs.test :refer (is deftest with-test run-tests testing test-var)]))

(defn text [elem]
  (or (.-textContent elem) (.-innerText elem)))

(defn sample-component [data owner]
  (reify
    om/IRender
    (render [_]
      (html [:div "Woot"]))))

(deftest sample
  (let [n (goog.dom/htmlToDocumentFragment "<div class='sample-node'></div>")]
    (om/root sample-component (atom {}) {:target n})
    (is (= "Woot" (text n)))))
