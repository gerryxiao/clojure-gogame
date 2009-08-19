  ;Go game in clojure
 
(ns Personal.Gerry.GoGame 
  (:use clojure.contrib.seq-utils)
  (:require [clojure.zip :as zip])
  (:gen-class))
(def *board-size* 19)
 
(def whole-lists (ref [])) ; use as saving all played stones stones ,includes captured stones
(defstruct stone :id :loc :liberty)  ;loc is {:x,:y} liberty default to nil, if 0,then it's captured.


(def black-id-groups []) ;black groups grouped by id,dead groups will be removed.

(def white-id-groups [])

(def w-captured-groups [])

(def b-captured-groups []) ; dead-groups move to here as backup data

(defn getloc-from-id [id]
     (let [s (nth @whole-lists (dec id))]
       (:loc s)))


(defn getid-from-loc [loc] ;;there is one problem , one location maybe have been played two and more stones
      (for [s @whole-lists :when (= (:loc s) loc)] (:id s)))

(defn get-neighbors [stone]
     (let [x (:x (:loc stone)) y (:y (:loc stone))]
       {:left {:x (dec x) :y y} :right {:x (inc x) :y y}
	:down {:x x :y (dec y)} :up { :x x :y (inc y)}}))

(defn stone-in-lists? [loc]
  (some #(= (:loc %) loc) @whole-lists))

(defn get-neighbors-id [stone]
  (let [neigs (get-neighbors stone)]
    [(getid-from-loc (:left n)) (getid-from-loc (:right n)) ; map to hashmap?
     (getid-from-loc (:down n)) (getid-from-loc (:up n))]))
						



(defn liberty-of-stone [stone]
  (let [n (get-neighbors stone)]
    (+ (if (stone-in-lists? {:x (:left n) :y (:left n)}) 0 1 )
       (if (stone-in-lists? {:x (:right n) :y (:right n)}) 0 1 )
       (if (stone-in-lists? {:x (:up n) :y (:up n)}} 0 1 )
       (if (stone-in-lists? {:x (:down n):y (:down n)}) 0 1 ))))

(defn liberty-of-group [group]                    ;;only dead group call it,result is 0
  (let [liberty-list (map liberty-of-stone group)]
    (reduce + liberty-list)))
	 
;(defn append-to-blocks [x y] ;lookup x in blocks,if found then add new move to inner block
;   (loop [loc dz]            ;dz is blocks
;     (if (zip/end? loc)
;       (zip/root loc)
;       (recur
;	(zip/next
;	 (if (and (vector? (zip/node loc)) (includes? (zip/node loc) x))
;	   (let [new-coll (conj (zip/node loc) y)]
;	   (zip/replace loc new-coll))
;	   loc))))))

(defn stone-to-idgroup [id-of-neighbors id]  ;put playing stone to groups
  (let [ groups (if (odd? id) black-id-groups white-id-groups)]
   (into []
	 (for [s groups] 
	   (if (includes? s id-of-neighbors)(conj s id) s)))))

(defn merge-inner-idgroups [id groups] ;id  is new playing stone id,maybe cause to merge groups 
  (let [with-out-id  (filter #(not (includes? % id)) groups)
        with-id  (filter #(includes? % id) groups)]
       (concat  (vec with-out-x) (vec (set (flatten with-x))))))


(defn play [id x y]    ;; not finish
  (let [new-stone (struct stone id x y )]
    (def whole-lists (conj whole-lists new-stone))
    (if (odd? id) (add-stone-to black-id-lists)
	(add-stone-to white-id-lists))))

(defn -main []
  (println "Gogame Test")
  (let [one (struct stone 1 3 4 4)]
    (println (qi-of-stone one))))
