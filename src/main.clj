(ns main
  (:use greeting repl-server))
(defn main []
  (loop []
    (print-message)
    (Thread/sleep 1000)
    (recur)))
(main)