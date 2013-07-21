(ns ^{:doc "A constrain a generative model"
      :author "Zenna Tavares"}
  relax.constrain
  (:use relax.evalcs)
  (:use relax.symbolic)
  (:use relax.common)
  (:use relax.examples)
  (:use relax.env)
  (:use relax.linprog)
  (:use clozen.helpers))

;; Inequality abstractions
(defn decompose-binary-exp
  "Takes a binary exp involving a symbol and a concrete number
   and extracts them into a map.

   Useful to find the symbol and/or number when an expression could be
   (+ x 2) or (+ 2 x) for instnace"
  [ineq]
  (cond
    (symbolic? (nth ineq 1))
    {:num (nth ineq 2) :symb (nth ineq 1)}

    (symbolic? (nth ineq 2))
    {:num (nth ineq 1) :symb (nth ineq 2)}

    :else
    (error "one of the values in inequality must be symbolic")))

(defn lower-bound [interval]
  (first interval))

(defn upper-bound [interval]
  (second interval))

(defn revise-interval
  "Constrain an interval with an inequality.
   e.g. (interval [0 10] '> 5) should eval to [0 5]"
  [interval ineq-pred ineq-val]
  {:pre [(>= (upper-bound interval) (lower-bound interval))]
   :post [(>= (upper-bound %) (lower-bound %))]}
  ; (println "INTERVAL" interval "ineq-pred" ineq-pred "ineq-val" ineq-val "\n")
  (let [lowerb (lower-bound interval)
        upperb (upper-bound interval)]
    (condp = ineq-pred
      '<  (cond
            (<= ineq-val lowerb)
            [0.0 0.0] ;(error "inequality unsatisfiable")

            (> upperb ineq-val)
            [lowerb ineq-val]

            :else
            interval)
      '<= (cond
            (< ineq-val lowerb)
            [0.0 0.0] ;(error "inequality unsatisfiable")

            (> upperb ineq-val)
            [lowerb ineq-val]

            :else
            interval)
      '>  (cond
            (>= ineq-val upperb)
            [0.0 0.0] ; (error "inequality unsatisfiable")

            (> ineq-val lowerb)
            [ineq-val upperb]

            :else
            interval)
      '>= (cond
            (> ineq-val upperb)
            [0.0 0.0] ;(error "inequality unsatisfiable")

            (> ineq-val lowerb)
            [ineq-val upperb]

            :else
            interval)
      (error "this predicate not supported"))))

(defn update-intervals
  "Compute a set of intervals with a set of inequalities"
  [ineqs variable-intervals]  
  (pass (fn [ineq var-intervals]
          ; (println "DEBUG ineq" ineq "var-intervals" var-intervals "\n") 
          (let [ineq (symbolic-value ineq)
                {num :num symb :symb} (decompose-binary-exp ineq)]
            ; (println "DEBUG num" num "symb" symb "\n")
            (update-in var-intervals [(symbolic-value symb)]
                       #(revise-interval % (first ineq) num))))
        variable-intervals
        ineqs))

;TODO
(defn compute-volume
  "Compute the volumes of the orthotope (hyperrectangle)"
  [intervals]
  (reduce * (map #(- (upper-bound %) (lower-bound %))
                  (vals intervals))))

;TODO Does this support negative intervals?
(defn interval-sample
  "Sample within box"
  [intervals]
  (for [interval intervals]
    (+ (lower-bound interval)
       (rand (- (upper-bound interval) (lower-bound interval))))))

(defn bound-polytope
  "Compute a suitable bounding volume of the polytope"
  [polytope])

(defn cover
  ""
  [ineqs]
  (map bound-polytope ineqs))

(defn enumerate-vertices
  [constraints])

(defn bounding-box
  "Expects data in poly-form form 
   [[x1 y1 z1][x2 y2 z2]]"
  [points]
  (pass
    (fn [point box]
      ; {:pre [(count= point box])}
      (println point "point-" "box" box)
      (for [[[lowerb upperb]  x] (map vector box point)]
        (cond
          (> x upperb)
          [lowerb x]

          (< x lowerb)
          [x upperb]

          :else
          [lowerb upperb])))
    (mapv #(vector % %) (first points)) ;bounding box
    points))

(defn box-volume
  "get the box volume"
  [box]
  (apply * (map #(- (first %) (second %)) box)))
; (symbolic (<= (symbolic (+ (symbolic x2) (symbolic (* -1 (symbolic x1))) 1)))) 


(defn ineq-to-matrix-form
  [exp vars]
  (let [var-id (zipmap vars (range (count vars)))
        exp (symbolic-value exp)
        second-arg (symbolic-value (second exp))
        add-sub (if (coll? second-arg)
                    (eval (operator second-arg))
                    +)
        arguments (if (coll? second-arg)
                      (rest second-arg)
                      [(make-symbolic second-arg)])]
    (println "second arg" second-arg "exp" exp "args" arguments "\n")
    [(pass
        (fn [term row]
          (cond
            (tagged-list? (symbolic-value term) '*)
            (let [{num :num symb :symb} (decompose-binary-exp (symbolic-value term))]
              (assoc row (var-id (symbolic-value symb)) (* (add-sub 1) num)))

            (symbolic? term)
            (assoc row (var-id (symbolic-value term)) (add-sub 1))

            :else
            (error "UNKOWN TERM" term)))

        (vec (zeros (count vars)))
        arguments)
    
    (range 1 (inc (count vars)))
    (operator exp)
    (last exp)]))

(defn satisfiable?
  [sample formula vars]
  ; (println "sample" sample "formula" formula "vars" vars)
  (let [extended-env (extend-environment vars sample the-pure-environment)]
    (every? true? (map #(evalcs % extended-env) formula))))

(defn disjoint-poly-box-sampling
  [feasible-boxes vars]
  (let [volumes (map first feasible-boxes)]
    #(loop [n-sampled 0 n-rejected 0]
      (let [[vol box formula] (categorical feasible-boxes volumes)
           sample (interval-sample box)
           pvar (println "box is" box " sample is " sample)]
       (if (satisfiable? sample formula vars)
           {:sample sample :n-sampled (inc n-sampled) :n-rejected n-rejected}
           (recur (inc n-sampled) (inc n-rejected)))))))

(defn unsymbolise
  [formula]
  "Remove symbols from something like this:
  (<= (symbolic (+ (symbolic (* -4 (symbolic x1))) (symbolic x2))) 10)"
  (map 
    #(let [value (if (symbolic? %)
                     (symbolic-value %)
                     %)]
      (if (coll? value)
          (unsymbolise value)
          value))
    formula))

(defn to-dnf
  "Takes a model represented as map of variables to intervals on uniform distribution"
  [variable-intervals pred]
  
  ; Add variables to environment
  (doall
    (for [variable (keys variable-intervals)]
      (define-symbolic! variable the-global-environment)))

  (println "the the-global-environment is" the-global-environment "\n")
  (println "the expanded predicate is" (andor-to-if pred)  "\n")
  (let [ineqs   (multivalues (all-possible-values 
                  (evalcs (andor-to-if pred) the-global-environment)))
        ineqs (filter #(true? (conditional-value %)) ineqs)]
        ineqs))

(defn make-lambda-args
  "Make a function from an expression with some args"
  [expr args]
  (eval (list 'fn args expr)))

(defn naive-rejection
  "Just sample and accept or reject
   Variables x: 10"
  [variable-intervals pred]
  (let [pred-fn (make-lambda-args pred (vec (keys variable-intervals)))]
  #(loop [n-sampled 0 n-rejected 0]
    (let [sample (interval-sample (vals variable-intervals))]
     (if (apply pred-fn sample)
         {:sample sample :n-sampled (inc n-sampled) :n-rejected n-rejected}
         (recur (inc n-sampled) (inc n-rejected)))))))

(defn disjoint-poly-box-sampling
  [feasible-boxes vars]
  (let [volumes (map first feasible-boxes)]
    #(loop [n-sampled 0 n-rejected 0]
      (let [[vol box formula] (categorical feasible-boxes volumes)
           sample (interval-sample box)
           pvar (println "box is" box " sample is " sample)]
       (if (satisfiable? sample formula vars)
           {:sample sample :n-sampled (inc n-sampled) :n-rejected n-rejected}
           (recur (inc n-sampled) (inc n-rejected)))))))

(defn constrain-uniform
  "Takes a model represented as map of variables to intervals on uniform distribution"
  [variable-intervals pred]
  
  ; Add variables to environment
  (doall
    (for [variable (keys variable-intervals)]
      (define-symbolic! variable the-global-environment)))

  (println "the the-global-environment is" the-global-environment "\n")
  (println "the expanded predicate is" (andor-to-if pred)  "\n")
  (let [ineqs   (multivalues (all-possible-values 
                  (evalcs (andor-to-if pred) the-global-environment)))
        ineqs (filter #(true? (conditional-value %)) ineqs)
        interval-constraints (map #(evalcs % the-global-environment)
                                   (vec 
                                    (reduce concat (vals variable-intervals))))
        pvar (println "INEQS" (map value-conditions ineqs) "\n" "constrs" interval-constraints)
        vars (keys variable-intervals)
        pvar (println "vars are " vars)
        matrix-form (map #(map (fn [x] (ineq-to-matrix-form x vars))
                                 (concat
                                  (value-conditions %)
                                  interval-constraints))
                          ineqs)
        pvar (println "The matrix form is" matrix-form "\n")
        pvar (println "The matrix form is" matrix-form "\n")
        ; Then for each disjuctive clause we need to compute an abstraction
        boxes (map #(bounding-box-lp % vars) matrix-form)
        feasible-boxes (filter #(not-any? nil? (first %))
                                (map vector boxes ineqs))
        feasible-boxes (map #(vector 
                              (box-volume (partition 2 (first %)))
                              (partition 2 (first %))
                              (map (fn [t] (unsymbolise (symbolic-value t)))
                                (value-conditions (second %))))
                            feasible-boxes)
        pvar (println "The feasible boxes are" feasible-boxes)
        pvar (println "The feasible boxes are" feasible-boxes)
        pvar (println "Volumes are" (map first feasible-boxes))]
    (disjoint-poly-box-sampling feasible-boxes vars)))


  ; (define-symbolic! 'x1 the-global-environment)
  ; (define-symbolic! 'x2 the-global-environment)
  ; (ineq-to-matrix-form (evalcs '(>= (+ (* 6 x1) x2) 5) the-global-environment) '[x1 x2])
  ; (ineq-to-matrix-form (evalcs '(>= x2 10) the-global-environment) '[x1 x2])

(use 'clojure.java.io)
(defn samples-to-file
  [fname samples]
  (let [num-dim (count (first samples))
          re-samples
        (for [i (range num-dim)]
          (map #(nth % i) samples))]
    (doall
      (for [sample samples]
        (with-open [wrtr (writer (str fname "-lines") :append true)]
          (.write wrtr  (str (clojure.string/join " " sample)))
                    ; (.write wrtr (apply str (doall sample)))

          (.write wrtr "\n"))))
    (doall
      (for [dim re-samples]
        (with-open [wrtr (writer fname :append true)]
          (.write wrtr (str (clojure.string/join " " dim) "\n")))))))

(defn -main[]
  (let [{vars :vars pred :pred} (gen-box-constraints 3)
        intervals (zipmap vars (map #(vector `(~'> ~% 0) `(~'< ~% 10)) vars))
        new-model (constrain-uniform intervals pred)
        data (repeatedly 10 new-model)
        samples (extract data :sample)
        n-sampled (sum (extract data :n-sampled))
        n-rejected (sum (extract data :n-rejected))
        ;; Rejection sampling
        srs-model (naive-rejection
          (zipmap vars (repeat (count vars) (vector 0 10))) pred)
        srs-data (repeatedly 10 srs-model)
        srs-samples (extract srs-data :sample)
        srs-n-sampled (sum (extract srs-data :n-sampled))
        srs-n-rejected (sum (extract srs-data :n-rejected))
        pvar (println "SRS SAMPLES ARE" srs-samples)
        pvar (println "SRS SAMPLES ARE" srs-samples)]
    (samples-to-file "op" srs-samples)
    (println "N-SAMPLES:" n-sampled " n-rejected: " n-rejected " ratio:" (double (/ n-rejected n-sampled)))
    (println "N-SAMPLES-SRS:" srs-n-sampled " n-rejected: " srs-n-rejected " ratio:" (double (/ srs-n-rejected srs-n-sampled)))
    samples))

; (defn -main[]
;   (let [new-model (constrain-uniform {'x1 ['(> x1 0) '(< x1 10)]
;                                         'x2 ['(> x2 0) '(< x2 10)]}
;                                         exp-abs)
;         samples (repeatedly 1000 new-model)]
;     (samples-to-file "op" samples)
;     samples))
