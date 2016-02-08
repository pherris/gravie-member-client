(ns gravie-member-client.util)

(defmacro swallow-errors
  [& body]
  `(try ~@body
        (catch :default e#
          (println "Exception:")
          (println e#))))
