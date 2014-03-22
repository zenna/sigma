(ns ^{:doc "The actual interpreter"
      :author "Zenna Tavares"}
  relax.interpret
  (:require [relax.construct :refer :all])
  (:require [clozen.helpers :as clzn])
  (:require [clojure.core.match :refer [match]])
  (:require [fipp.edn :refer (pprint) :rename {pprint fipp}])
  (import [java.io PushbackReader FileReader]))

(defn parse-clj [fname]
  (with-open [reader (PushbackReader. (FileReader. fname))]
    (loop [exprs []]
      (if-let [expr (read reader nil nil)]
        (recur (conj exprs expr))
        exprs))))

(defn eval-expr-seq
  "Load a clj file and interpret line by line using rules"
  [exprs]
  (clzn/loop-until-fn #(sigma-rewrite % transformer) exprs))

(defn interpret-file
  "Load a file and interpret files in sequence"
  [fname]
  (eval-expr-seq (parse-clj fname)))

(defmulti get-command :cmd)

(defmethod get-command 'load
  [{fname :fname}] 
  (do
    (println "Trying to load" fname)
    (interpret-file fname)
    true))

; Exit
(defmethod get-command 'exit
  [_] (println "See you, space cowboy!"))

; Print the global environment
(defmethod get-command 'print-env
  [_] (do (fipp global-env) true))

; Evaluate the command
(defmethod get-command :default
  [exp] (sigma-rewrite exp transformer))

(defn sigma-repl
  "Very Simple Repl"
  []
  (println "===== Welcome σ REPL, type exit to exit ========")
  (loop [ip (read)]
    (let 
      [x (match ip
         (['load fname] :seq) {:cmd 'load :fname fname}
         'env {:cmd 'print-env}
         'exit {:cmd 'exit}
         :else ip)]
      (println x)
      (if (get-command x)
          (recur (read))
          nil))))