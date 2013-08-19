(ns ^{:doc "Axis Aligned Box (Orthorope) abstractions"
                :author "Zenna Tavares"}
  relax.box
  (:use relax.abstraction)
  (:use clozen.helpers)
  (:require [clojure.math.combinatorics :as combo]))

(defn lower-bound [interval]
  (first interval))

(defn upper-bound [interval]
  (second interval))

(defn num-dims
  "Dimensionality of box"
  [box]
  (count (:internals box)))

(defn nth-dim-interval
  "Get the interval at particular dimenison"
  [{box :internals} dim-n]
  (nth box dim-n))

(defn middle-split
  [{box :internals}]
  (map #(double (+ (lower-bound %) (/ (- (upper-bound %) (lower-bound %)) 2))) box))

(defn split
  "Split a box into 2^D other boxes"
  [box split-points]
  (map
    #(make-abstraction % (:formula box)) ; All subboxes have same formula as parent
    (for [dim-to-change (apply combo/cartesian-product (:internals box))]
      (mapv
        (fn [dim-to-replace min-max split-point]
          (vec (sort [(first (filter #(not= dim-to-replace %) min-max)) split-point])))
        dim-to-change (:internals box) split-points))))

(defn split-uniform
  "Split the box into equally sized boxes"
  [box]
  (split box (middle-split box)))

(defn abstraction-vertices
  "Get the vertices of an abstraction"
  [box]
  (apply combo/cartesian-product (:internals box)))

; THIS IS INCORRECT
(defn completely-within?
  [box vars]
  false)
  ; (every? #(satisfiable? % (:formula box) vars) (abstraction-vertices box)))

(defn on-boundary?
  [box vars]
  (not (completely-within? box vars)))

(defn not-intersect?
  [box1 box2]
  ; (println box1 "!!!" box2)
  (let [x
        (for [dim (range (num-dims box1))]
              (or (< (upper-bound (nth-dim-interval box1 dim))
                     (lower-bound (nth-dim-interval box2 dim)))
                  (> (lower-bound (nth-dim-interval box1 dim))
                     (upper-bound (nth-dim-interval box2 dim)))))]
        (some true? x)))

(defn intersect?
  [box1 box2]
  "Do they intersect?"
  (not (not-intersect? box1 box2)))

(defn point-in-abstraction?
  "Is a point within an abstraction"
  [box point]
  {:pre [(count= (:internals box) point)]}
  ; (println "PIA" point box (intersect? box (make-abstraction (mapv #(vector % %) point) 'no-formula)))
  ; Assume point is degenerate box
  (intersect? box (make-abstraction (mapv #(vector % %) point) 'no-formula)))

(defn abstraction-contains?
  "Does box-a fully contain box-b?
   This is true when for each dimenison lower-bound of A is lte
   and upper-bound is gte B"
  [box-a box-b]
  ; {:pre [(count= box-a box-b)]}
  (every?
    #(and (<= (lower-bound (nth-dim-interval box-a %))
              (lower-bound (nth-dim-interval box-b %)))
          (>= (upper-bound (nth-dim-interval box-a %))
              (upper-bound (nth-dim-interval box-b %))))
    (range (num-dims box-a))))

;TODO
(defn overlap
  "Compute overlapping hyperrectangle from two overlappign ones"
  [box1 box2]
  (if (intersect? box1 box2)
      {:formula #(and (apply (:formula box1) %) (apply (:formula box2) %))
       :internals
        (vec
          (for [[[low1 high1][low2 high2]]
                (partition 2 (interleave (:internals box1) (:internals box2)))]
            [(max low1 low2)(min high1 high2)]))}
      'empty-abstraction))

(defn volume
  "get the box volume"
  [box]
  (apply * (map #(- (upper-bound %) (lower-bound %)) (:internals box))))

(defn union-volume
  "Find the volume of the union of a set of boxes
   Recurse through boxes adding on volume of each new one seen
   and subtracting union volume of interection of new one and all
   previous ones"
  [& boxess]
  ; (println boxess)
  (if (or (empty? boxess) (nil? boxess))
    0.0
    (loop [boxes (rest boxess) seen-boxes (list (first boxess))
           vol (volume (first boxess))]
      ; (println "vol is" vol)
      ; (println "box is" (first boxess))
      (if (empty? boxes)
          vol
          (recur (rest boxes)
                 (conj seen-boxes (first boxes))
                 (- (+ vol (volume (first boxes)))
                    (apply union-volume
                           (filter has-volume?
                                   (map #(overlap % (first boxes))
                                        seen-boxes))))))))) 

;TODO Does this support negative intervals?
(defn interval-sample
  "Sample within box"
  [intervals]
  (for [interval intervals]
    (+ (lower-bound interval)
       (rand (- (upper-bound interval) (lower-bound interval))))))

(defn abstraction-sample
  "Sample with the abstraction"
  [box]
  (for [interval (:internals box)]
    (+ (lower-bound interval)
       (rand (- (upper-bound interval) (lower-bound interval))))))

;; Boolean Operations
(defn remove-dim
  "Remove a dimension from a box"
  [box dim]
  (assoc box :internals (vec-remove (:internals box) dim)))

; TODO
(defn edit-interval
  ""
  [box dim interval]
  (assoc-in box [:internals dim] interval))

; TODO
(defn edit-lower-bound
  [box dim new-bound]
  (assoc-in box [:internals dim 0] new-bound))

(defn edit-upper-bound
  [box dim new-bound]
  (assoc-in box [:internals dim 1] new-bound))

; FIXME, IM SAMPLING WITHIN THE ENTIRE EXPANDED BOX NOT JUST THE EXPANDED
;REGION
(defn valid-ext
  "LOLZ!"
  [box exp-box boxes]
  (println "ext" exp-box)
  (let [n-samples 100]
    (loop [n-samples n-samples]
      (println n-samples)
      (cond
        (zero? n-samples)
        true

        :else
        (let [sample (abstraction-sample exp-box)]
          (if (some #(point-in-abstraction? % sample) boxes)
              (recur (dec n-samples))
              false))))))

(defn expand-box
  [box boxes]
  (let [n-dims (num-dims box)]
  (loop [exp-box box sides (vec (range n-dims))]
    (cond
      (empty? sides)
      exp-box
      
      :else
      (let [[dim sides] (rand-vec-remove sides)
            interval (nth-dim-interval box dim)
            ; We only care about extending to places which intersect.
            good-boxes (filter #(intersect? (remove-dim exp-box dim)
                                            (remove-dim % dim))
                                boxes)
            pvar (println "good boxes" good-boxes)

            ext-points (sort (flatten (map #(nth-dim-interval % dim) 
                                            good-boxes)))

            pvar (println "ext points" ext-points)
            pvar (println "filtered" (filterv #(>= % (upper-bound interval)) ext-points))
            upper
            (max-pred #(valid-ext exp-box
                                  (edit-upper-bound exp-box dim %)
                                  boxes)
                            (filterv #(>= % (upper-bound interval)) ext-points))
            pvar (println "upper" upper)

            lower
            (min-pred #(valid-ext exp-box
                                  (edit-lower-bound exp-box dim %)
                                  boxes)
                            (filterv #(<= % (lower-bound interval)) ext-points))]
        (recur (edit-interval exp-box dim [lower upper]) sides))))))

; (defn select-init-box [boxes]
;   (first boxes))

; (defn can-extend?
;   [side])

; ; NO TEST
; (defn cover-abstr
;   "Given"
;   [boxes]
;   (loop [unvisited-sides (select-init-box boxes) covering [curr-box]]
;     (if (empty? unvisited-sides)
;         covering
;         (let [side (random-nth unvisited-sides)]
;           (if (can-extend? side)
;               (let [new-box (expand-box (extend-box side))]
;                 (recur (add-sides unvisited-sides new-box) (conj covering new-box)))
;               (recur (remove unvisited-sides sides) covering))))))

; ; I could check each face of each box every tiem but that's silly
; ; I could carry around a list of all the things I've alredy done
; ; Or I could carry around a list of ones available to carry from

; (defn cover-abstr-inner
;   [init boxes])

  ; Assume overlapping or divide into overlapping segments?
  ; Find feasible region?
  ; Start with any box
  ; - Start with overlapping region
  ; - Start with point sampled uniformly from volume
  ; Breadth first, so I'm going to try to expand the dimension of each box
  ; Choose a side uniformly and expand each dimension one
  ; Q- What is the set of lines to which I must consider expansion
  ; When that is done choose a side randomly, and see if extension is possible, if so extend
  ; 
  ;Expand box to


; (defn tile-abstr
;   "Box"
;   [& boxes]
;   ; {:pre [(true? (apply count= boxes))]} ; All boxes have same dim
;   (let [;boxes (map :internals boxes)
;         n-dim (num-dims (first boxes))
;         planes
;         (for [dim (range n-dim)]
;           (partition 2 1
;             (sort (distinct (reduce concat (map #(nth % dim) boxes))))))]
;     ; Retain cells which are contained by one of the orig Boxes
;     (filter
;       #(some true? (for [box boxes] (contains? box %)))
;       (apply combo/cartesian-product planes))))

(def b1 {:internals [[0 5][0 5]]})
(def b2 {:internals [[3 6][2 10]]})
(def b3 {:internals [[0 10][0 10]]})

(defn gen-random-boxes
  [n-dims n-boxes]
  (repeat n-boxes
    (vec (for [dim (range n-dims)]
         [[(rand) (rand)][(rand) (rand)]]))))

(defn -main []
  ; (union-volume b1 b2 b3))
  (expand-box (overlap b1 b2) [b1 b2]))