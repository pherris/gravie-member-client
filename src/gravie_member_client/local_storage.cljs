(ns gravie-member-client.local-storage
  (:require [gravie-member-client.utils :as utils :refer [mlog]]))

(defn store [k obj]
  (.setItem js/localStorage k (js/JSON.stringify (clj->js obj))))

(defn keywordify [m]
  (cond
    (map? m) (into {} (for [[k v] m] [(keyword k) (keywordify v)]))
    (coll? m) (vec (map keywordify m))
    :else m))

(defn fetch [k default]
  (let [item (.getItem js/localStorage k)]
    (if item
      (-> (.getItem js/localStorage k)
          (or (js-obj))
          (js/JSON.parse)
          (js->clj)
          (keywordify))
      default)))

(defn limited-state-revisions [state]
  (take-last 50 state))

(defn store-state [state]
  "Adds the state to the localStorage states array"
  (let [states (fetch :state [])]
    (store :state (limited-state-revisions (conj states state)))))

(defn fetch-state [& {:keys [index] :or {index -1}}]
  "Gets whatever index you pass in (if it exists) and nil if not"
  (let [states (fetch :state [])
        idx (if (and
                  (= index -1)
                  (count states))
              (- (count states) 1)
              index)]
      (nth states idx nil)))
