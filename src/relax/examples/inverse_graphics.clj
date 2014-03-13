; (ns ^{:doc "Example generative models and constraints"
; 	    :author "Zenna Tavares"}
;   relax.examples
;   (:use clozen.helpers))

; ;; Graphs
; (defn gen-multigraph-edge-list
;   "Generate a graph in edge list form:[[n1 n2],..,[n1 n2]]"
;   []
;   (let [num-points (rand-int 10)
;         num-edges (rand-int (* num-points num-points))]
;     (for [i (range num-edges)]
;       [(rand-int num-points) (rand-int num-points)])))

; ; NOTEST
; (defn gen-graph
;   "Generates a graph"
;   []
;   (let [num-points (rand-int 100)
;         num-edges (rand-int (* num-points num-points))
;         edges (vec (repeat num-points []))]
;     (pass
;       (fn [elem edges]
;         (let [new-edge
;               ; Generate an edge that doesnt already exist
;               (gen-until #(vector (rand-int num-points) (rand-int num-points))
;                          #(not (in? (nth edges (first %)) (second %))))
;               new-val (conj (nth edges (first new-edge)) (second new-edge))]
;           (assoc edges (first new-edge) new-val)))
;       edges
;       (range num-edges))))

; (defn num-nodes
;   [graph]
;   (count graph))

; (defn graph-is-empty?
;   [graph]
;   (every? empty? graph))

; ; NOTEST
; (defn acyclic?
;   "Check whether a DAG has no cycles by determining whether it can be
;    topologically sorted

;    Assumes graphs is in integer form"
;   [graph]
;   (let [sorted-elems []
;         n-nodes (num-nodes graph)
;         working-set (set (range n-nodes))
;         working-set (pass (fn [tail working-set]
;                               (disj working-set tail))
;                           working-set
;                           (reduce concat graph))]
;     (cond
;       (empty? working-set)
;       false

;       :else
;       (graph-is-empty?
;         (loop [working-set working-set sorted-elems [] graph graph]
;           (cond
;             (empty? working-set)
;             graph

;             :else
;             (let [n (first working-set)
;                   tails (nth graph n)
;                   graph (assoc graph n [])
;                   working-set (pass (fn [tail working-set]
;                                         (if (in? (reduce concat graph) tail)
;                                              working-set
;                                             (conj working-set tail)))
;                                     working-set
;                                     tails)]
;                   (recur (disj working-set n) (conj sorted-elems n)
;                                               graph))))))))
; (defn test-rejection-acyclic []
;   (counts (map acyclic? (repeatedly 1000 gen-graph))))

; ;; Planning
; (defn gen-path
;   "Generate a random path in X Y"
;   []
;   (let [num-points (rand-int 10)]
;     (repeatedly num-points #(vector (rand) (rand)))))

; (defn gen-path-parametric
;   "Generate a random path in X Y"
;   [num-dim num-points]
;   (repeatedly num-points #(vec (repeatedly num-dim rand))))

; ; (defn avoids-obstacles?
; ;   "Obstacles are of form [poly poly poly]"
; ;   [path obstacles]
; ;   (for [edge (partition 2 1 path)
; ;         :let [tundot (somecomp)
; ;               obs-edges ()]
; ;         obs-edges]
; ;     (let [t (dot-prod t-undot (edge-vec obs-edges))]
; ;       (or (> t 1) (< t 0)))))

; (defn perp-2d
;   [[x y :as vect]]
;   {:pre [(= 2 (count vect))]}
;   [(- y) x])

; ; (defn avoid-box-obs
; ;   "path is "
; ;   [path boxes]
; ;   (let [valid-point (map #(point-in-box? % boxes) path)]
; ;     (some true? valid-point)))

; ; (defn point-in-box?
; ;   [point box]
; ;   true)

; ; (defn avoids-obstacles?
; ;   "When evaluated on a path will return true
; ;    only if that path passes through no obstacles
; ;    obstacles is a vector of points = [[x1-min x1-max][x2-min x2-max]]"
; ;   [path start target obstacles max-step]
; ;   (let [p-start (first path)
; ;         p-end (last path)]

; ;     (and
; ;       (in-box? p-start start)     ; First point must be in start
; ;       (in-box? p-end target)      ; Last point must be in target

; ;                                   ; Points must be at most max-distance apart
; ;       (apply and
; ;         (for [p path]
; ;           (in-box? p (square-around-point point step))))

; ;                                   ; Points must not be within obstacles
; ;       (apply or
; ;         (for [p path              ; Consider all combinations of points
; ;               o obstacles]        ;   and obstacles
; ;           (not (in-box? p o)))))))   ; Check point is not in obstacle 

; (defn avoid-orthotope-obs
;   "Creates a program which when evaluated on a path will return true
;    only if that path passes through no obstacles
;    obstacles is a vector of points = [[x1-min x1-max][x2-min x2-max]]"
;   [n-points [sx sy :as start] [ex ey :as end] obstacles max-step]
;   (let [pos-delta 0.1
;         vars
;         (for [i (range n-points)]
;           [(symbol (str "x" i)) (symbol (str "y" i))])
;         [svx svy] (first vars)
;         [evx evy] (last vars)]
;     {:vars (vec (reduce concat vars))
;      :pred
;   `(~'and

;     ; First point must be in start box
;     (~'>= ~svx ~(- sx pos-delta))
;     (~'<= ~svx ~(+ sx pos-delta))
;     (~'>= ~svy ~(- sy pos-delta))
;     (~'<= ~svy ~(+ sy pos-delta))

;     ; Last point must be in target box
;     (~'>= ~evx ~(- ex pos-delta))
;     (~'<= ~evx ~(+ ex pos-delta))
;     (~'>= ~evy ~(- ey pos-delta))
;     (~'<= ~evy ~(+ ey pos-delta))

;     ; Points must be certain distane apart
;     ~@(reduce concat
;         (for [[[path-x0 path-y0] [path-x1 path-y1]] (partition 2 1 vars)]
;           `[(~'>= (~'+ ~path-x1 (~'* -1 ~path-x0)) 0)
;             (~'<= (~'+ ~path-x1 (~'* -1 ~path-x0)) ~max-step)
;             (~'>= (~'+ ~path-y1 (~'* -1 ~path-y0)) 0)
;             (~'<= (~'+ ~path-y1 (~'* -1 ~path-y0)) ~max-step)]))

;     ; Points must not be within obstacles
;     ~@(for [[x y] (subvec (vec vars) 1 (dec (count vars)))
;               [[x-min x-max][y-min y-max]] obstacles]
;           `(~'or
;             (~'<= ~x ~x-min)
;             (~'>= ~x ~x-max)
;             (~'<= ~y ~y-min)
;             (~'>= ~y ~y-max))))}))

; (defn point-avoid-orthotope-obs
;   "Creates a program which when evaluated on a path will return true
;    only if that path passes through no obstacles
;    obstacles is a vector of points = [[x1-min x1-max][x2-min x2-max]]"
;   [n-points obstacles]
;   (let [vars
;         (for [i (range n-points)]
;           [(symbol (str "x" i)) (symbol (str "y" i))])]
;     {:vars (vec (reduce concat vars))
;      :pred
;   `(~'and

;     ; Points must not be within obstacles
;     ~@(for [[x y] vars
;             [[x-min x-max][y-min y-max]] obstacles]
;           `(~'or
;             (~'<= ~x ~x-min)
;             (~'>= ~x ~x-max)
;             (~'<= ~y ~y-min)
;             (~'>= ~y ~y-max))))}))

; (defn long-args-to-let
;   [pred vars]
;   (let [let-args (interleave vars (map #(list 'nth 'sample %)
;                                         (range (count vars))))]
;     `(let [~@let-args]
;       ~pred)))

; ;; Non-linear planning constraint
; (defn point-in-box?
;   "Is a point inside a box?"
;   [[[px py] :as point] [[[low-x up-x][low-y up-y]] :as box]]
;   (and
;     (>= px low-x)
;     (<= px up-x)
;     (>= py low-y)
;     (<= py up-x)))

; (defn points-to-vec
;   [[p1x p1y] [p2x p2y]]
;   [(- p1x p2x)(- p1y p2y)])

; (def obstacle-eg
;   [[[3 9][7 9]]
;    [[7 9][5 7]]
;    [[5 7][3 9]]])

; (def path-eg
;   [[3,3][9,5]])

; (defn intersection-point
;   "Find the intersection point of two vectors"
;   [[a0 a1] [b0 b1]]
;   (let [[u1 u2] (points-to-vec a1 a0)
;         [v1 v2] (points-to-vec b0 b1)
;         [w1 w2] (points-to-vec b0 a1)]
;     (/ (- (* v2 w1) (* v1 w2))
;        (- (* v1 u2) (* v2 u1)))))

; (defn path-avoids-obstacles?
;   "Does the path avoid (not pass through) any of the obstacles?

;    Path is set of vertices, e.g. [[3,3][9,5]]
;    Obstacles is set of edges [e1,..,en]
;    where ei is pair of vertices.
;    e.g. obstacles = 
;    [[[3 9][7 9]]
;     [[7 9][5 7]]
;     [[5 7][3 9]]]"
;   [obstacles path]
;   (every?
;     (for [[p0 p1] (partition 2 1 path)
;           [o0 o1] obstacles
;            :let [[u1 u2] (points-to-vec p1 p0)
;                  [v1 v2] (points-to-vec o0 o1)
;                  [w1 w2] (points-to-vec o0 p1)
;                  s (/ (- (* v2 w1) (* v1 w2))
;                       (- (* v1 u2) (* v2 u1)))]]
;       (or (> s 1.0)
;           (< s 1.0)))))

; (defn valid-path?
;   [start target obstacles path]
;   (and
;     (point-in-box? (first path) start)
;     (point-in-box? (last path) target)
;     (path-avoids-obstacles? obstacles path)))

; ;; Inverse Graphics
; ; (defn gen-poly [])

; ; (defn simple? [])

; ; (defn gen-mesh [])

; ; (defn self-intersections? [])

; ; (defn -main []
; ;   (avoid-orthotope-obs 3 [1 1] [9 9] [[[2 5][5 7]][[5 8][4 6]]]))


; ;; Box packing
; (defn gen-box-constraints-overlap
;   [n-boxes]
;   (let [vars
;        (for [i (range n-boxes)]
;           [(symbol (str "x" i)) (symbol (str "y" i)) (symbol (str "r" i))])]
;     {:vars (reduce concat vars)
;      :pred
;     `(~'and
;       ~@(reduce concat
;         (for [[[ax ay ar][bx by br]] (unique-pairs vars)]
;         `((~'< (~'+ ~ax (~'* -1 ~ar) (~'* -1 ~bx) (~'* -1 ~br)) 0)
;           (~'> (~'+ ~ax ~ar (~'* -1 ~bx) ~br ) 0)
;           (~'> (~'+ ~ay ~ar (~'* -1 ~by) ~br ) 0)
;           (~'< (~'+ ~ay (~'* -1 ~ar) (~'* -1 ~by) (~'* -1 ~br)) 0)))))}))

; (defn gen-box-non-overlap
;   [n-boxes]
;   (let [vars
;        (for [i (range n-boxes)]
;           [(symbol (str "x" i)) (symbol (str "y" i)) (symbol (str "r" i))])]
;     {:vars (reduce concat vars)
;      :pred
;     `(~'and
;       ~@(for [[[ax ay ar][bx by br]] (unique-pairs vars)]
;         `(~'or 
;           (~'> (~'+ ~ax (~'* -1 ~ar) (~'* -1 ~bx) (~'* -1 ~br)) 0)
;           (~'< (~'+ ~ax ~ar (~'* -1 ~bx) ~br ) 0)
;           (~'> (~'+ ~ay (~'* -1 ~ar) (~'* -1 ~by) (~'* -1 ~br)) 0)
;           (~'< (~'+ ~ay ~ar (~'* -1 ~by) ~br ) 0))))}))

; (defn gen-box-non-overlap-close
;   [n-boxes]
;   (let [proximity-thresh 1.0 ; How close the boxes must be
;         vars
;        (for [i (range n-boxes)]
;           [(symbol (str "x" i)) (symbol (str "y" i)) (symbol (str "r" i))])]
;     {:vars (reduce concat vars)
;      :pred
;     `(~'and
;       ~@(for [[[ax ay ar][bx by br]] (unique-pairs vars)]
;         `(~'or 
;           (~'and
;             (~'> (~'+ ~ax (~'* -1 ~ar) (~'* -1 ~bx) (~'* -1 ~br)) 0)
;             (~'< (~'+ ~ax (~'* -1 ~ar) (~'* -1 ~bx) (~'* -1 ~br)) ~proximity-thresh))

;           (~'and
;             (~'< (~'+ ~ax ~ar (~'* -1 ~bx) ~br ) 0)
;             (~'> (~'+ ~ax ~ar (~'* -1 ~bx) ~br ) ~(* -1 proximity-thresh)))

;           (~'and
;             (~'> (~'+ ~ay (~'* -1 ~ar) (~'* -1 ~by) (~'* -1 ~br)) 0)
;             (~'< (~'+ ~ay (~'* -1 ~ar) (~'* -1 ~by) (~'* -1 ~br)) ~proximity-thresh))

;           (~'and
;             (~'< (~'+ ~ay ~ar (~'* -1 ~by) ~br ) 0)
;             (~'> (~'+ ~ay ~ar (~'* -1 ~by) ~br ) ~proximity-thresh)))))}))

; ; the predicate is 

; ; (and 
; ;   (or 
; ;     (and 
; ;       (> (+ x0 (* -1 r0) (* -1 x1) (* -1 r1)) 0) 
; ;       (< (+ x0 (* -1 r0) (* -1 x1) (* -1 r1)) 1.0))
; ;     (and (< (+ x0 r0 (* -1 x1) r1) 0)
; ;          (> (+ x0 r0 (* -1 x1) r1) -1.0))
; ;     (and (> (+ y0 (* -1 r0) (* -1 y1) (* -1 r1)) 0)
; ;          (< (+ y0 (* -1 r0) (* -1 y1) (* -1 r1)) 1.0))
; ;     (and (< (+ y0 r0 (* -1 y1) r1) 0) (> (+ y0 r0 (* -1 y1) r1) 1.0)))

; ;   (or (and (> (+ x0 (* -1 r0) (* -1 x2) (* -1 r2)) 0)
; ;            (< (+ x0 (* -1 r0) (* -1 x2) (* -1 r2)) 1.0))
; ;       (and (< (+ x0 r0 (* -1 x2) r2) 0)
; ;            (> (+ x0 r0 (* -1 x2) r2) -1.0))
; ;       (and (> (+ y0 (* -1 r0) (* -1 y2) (* -1 r2)) 0)
; ;            (< (+ y0 (* -1 r0) (* -1 y2) (* -1 r2)) 1.0))
; ;       (and (< (+ y0 r0 (* -1 y2) r2) 0)
; ;         (> (+ y0 r0 (* -1 y2) r2) 1.0)))
  
; ;   (or 
; ;     (and (> (+ x1 (* -1 r1) (* -1 x2) (* -1 r2)) 0)
; ;          (< (+ x1 (* -1 r1) (* -1 x2) (* -1 r2)) 1.0))
; ;     (and (< (+ x1 r1 (* -1 x2) r2) 0)
; ;          (> (+ x1 r1 (* -1 x2) r2) -1.0))
; ;     (and (> (+ y1 (* -1 r1) (* -1 y2) (* -1 r2)) 0)
; ;          (< (+ y1 (* -1 r1) (* -1 y2) (* -1 r2)) 1.0))
; ;     (and (< (+ y1 r1 (* -1 y2) r2) 0)
; ;          (> (+ y1 r1 (* -1 y2) r2) 1.0))))


; ;; Random examples
; (def exp 
;   '(if (> x1 9)
;       (or (> x2 10)
;           (< x2 1))
;       (if (> x2 8)
;           true
;           false)))

; (def exp2
;   '(if (> x1 8)
;       (or (> x2 10)
;           (< x2 1))
;       (if (> x2 5)
;           (or (> x2 7)
;               (< x1 9))
;           false)))

; (def exp3
;   '(or (and (> x1 7) (> x2 7) (< x1 9) (< x2 10))
;        (and (> x1 3) (> x2 3) (< x1 5) (< x2 5))
;        (< x1 1)))

; (def exp-line
;   '(if (>= (+ x2 (* -1 x1)) 0)
;         true
;         false))

; (def exp-linear
;   '(or

;     (and (>= x2 9) (<= x2 10))
;     (and (>= x1 3) (>= x2 3) (<= x1 5) (<= x2 5))
;     (and 
;       (>= x1 0)
;       (>= x2 0)
;       (<= (+ x2 (* (- 1) x1)) 1)
;       (<= (+ x1 (* 6 x2)) 15)
;       (<= (+ (* 4 x1) (* (- 1) x2)) 10))))

; (def exp-linear-overlap
;   '(or

;     (> (+ x2 (* -1 x1)) 0)
;     (and (> x1 8) (> x2 2) (< x1 10) (< x2 4))))

; (def exp7
;   '(if (> x1 2)
;         (if (> x2 2)
;             true
;             false)
;         false))

; (def exp10
;   '(if (> x1 2)
;         true
;         false))

; (def exp4
;   '(if (if (> x1 3)
;             true
;             false)
;       false
;       true))

; (def exp5
;   '(if (if (> x1 3)
;            true
;            false) 
;        (if (< x2 4) true false  )
;        true))


; (defn qual-example
;   []
;   {:vars '[x y]
;    :pred
;    '(or (and (< x 5) (> (+ y (* -1 x)) 2.5))
;         (and (>= x 5) (< (+ y (* -0.5 x)) 2)
;                       (> (+ y (* 0.5 x)) 10.5)))})

; (def exp5
;   '(and
;     (or (> x1 1) (< x2 2) (> x1 3) (> x2 4))
;     (or (> x1 5) (< x2 6) (> x1 7) (> x2 8))
;     (or (> x1 9) (< x2 10) (> x1 11) (> x2 12))
;     (or (> x1 13) (< x2 14) (> x1 15) (> x2 16))))

; (def exp5
;   '(and
;     (or (> x1 1) (< x2 2))
;     (or (> x1 5) (< x2 6))))

; (def exp-abs
;   '(and
;     (> x1 2) (> x2 2) (< x1 8) (< x2 8)
;     (or (> (+ x2 (* -1 x1)) 4) (> (+ x2 x1) 14))
;     (or (> x2 7.5) (< x2 6.5))))
;     ; (or (< (+ x2 (* -1 x1)) 0) (< (+ x2 x1) 10))))
;     ; (or (< (+ x2 (* -1 x1)) 10) (< (+ x2 (* -1 x1)) 0))))

; (def exp-abs
;   '(and
;     (> x1 2) (> x2 2) (< x1 8) (< x2 8)
;     (or (> (+ x2 (* -1 x1)) 4) (> (+ x2 (* 3 x1)) 23))
;     (or (> (+ x2 (* -0.1666 x1)) 6.666) (< x2 5))))
;     ; (or (< (+ x2 (* -1 x1)) 0) (< (+ x2 x1) 10))))
;     ; (or (< (+ x2 (* -1 x1)) 10) (< (+ x2 (* -1 x1)) 0))))

; (def exp-testy-3d
;   '(and
;      (or
;        (> (+ x2 (* -1 x1)) 4)
;        (> (+ x2 (* 3 x1)) 23)
;        (> (+ x1 (* -1 x3)) 0)
;        (> (+ x3 (* 1 x2)) 23))
;      (or
;        (> (+ x1 (* 1 x2) (* 1 x3)) 5)
;        (> (+ x2 (* 0.5 x3)) 6)
;        (> (+ x1 (* -1 x2) (* -1 x3)) 5)
;        (> (+ x3 (* 1 x2)) 0))))

; (def exp-rand-3d
;   `(~'and
;      (~'or
;        (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;              (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2)))
;        (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;              (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2))))
;      (~'or
;        (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;              (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2)))
;        (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;              (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2))))))

; (def exp-rand-and-3d
;   `(~'and
;      (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;            (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2)))
;      (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;            (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2)))
;      (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;            (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2)))
;      (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;            (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2)))
;      (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;            (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2)))))

; (def exp-rand-and-3d
;   `(~'or
;       (~'and
;          (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;                (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2)))
;          (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;                (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2)))
;          (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;                (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2)))
;          (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;                (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2)))
;          (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;                (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2))))

;         (~'and
;            (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;                  (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2)))
;            (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;                  (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2)))
;            (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;                  (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2)))
;            (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;                  (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2)))
;            (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;                  (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2))))

;         (~'and
;            (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;                  (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2)))
;            (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;                  (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2)))
;            (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;                  (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2)))
;            (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;                  (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2)))
;            (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;                  (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2))))

;         (~'and
;            (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;                  (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2)))
;            (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;                  (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2)))
;            (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;                  (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2)))
;            (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;                  (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2)))
;            (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;                  (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2))))

;             (~'and
;                (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;                      (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2)))
;                (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;                      (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2)))
;                (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;                      (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2)))
;                (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;                      (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2)))
;                (~'> (~'+ (~'* ~(dec (rand 2)) ~'x1) (~'* ~(dec (rand 2)) ~'x2)
;                      (~'* ~(dec (rand 2)) ~'x3) (~'* ~(dec (rand 2)) ~'x4)) ~(dec (rand 2))))


;         ))
;     ; The problem with this is that we'll end up with a ccombinatorial explosion.

; ; (if (if (> x1 1)
; ;         true
; ;         (if (< x2 2)
; ;              true
; ;              false))
; ;     (if (if (> x1 5)
; ;             true
; ;             (if (< x2 6)
; ;                 true
; ;                 false))
; ;         true
; ;         false)
; ;     false)