(ns gravie-member-client.utils)

(defmacro swallow-errors
  [& body]
  `(try ~@body
        (catch :default e#
          (println "Exception:")
          (println e#))))
