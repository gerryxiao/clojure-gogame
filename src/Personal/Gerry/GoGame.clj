   ;Go game in clojure
;围棋游戏
 
(ns Personal.Gerry.GoGame 
  (:use [clojure.contrib seq-utils duck-streams])
  (:require [clojure.zip :as zip])
  (:gen-class))
(def *board-size* 19)
 
(def whole-lists (ref [])) ; use as saving all played stones stones ,includes captured stones 按序储存所有已下棋子包括死子
(defstruct stone :id :loc :liberty)  ;loc is {:x,:y} liberty default to nil, if 0,then it's captured.死子气为0


(def black-id-groups (atom [])) ;black groups grouped by id,dead groups will be removed. [ [1 2 3]]

(def white-id-groups (atom []))

;(def w-captured-groups [])

;(def b-captured-groups []) ; dead-groups move to here as backup data

(defn getloc-from-id [id]
     (let [s (nth @whole-lists (dec id))]
       (:loc s)))


(defn getid-from-loc [loc] ; one location maybe have been played two and more stones,dead stone don't count
  (if (nil? loc) nil
      (first (for [s @whole-lists :when (and (= (:loc s) loc) (= (:liberty s) nil))] (:id s)))))

(defn get-neighbors [stone]
     (let [x (:x (:loc stone)) y (:y (:loc stone))]
       {:left (if (= x 0) nil {:x (dec x) :y y}) :right (if (= x 18) nil {:x (inc x) :y y})
	:down (if (= y 0) nil {:x x :y (dec y)}) :up (if (= y 18) nil { :x x :y (inc y)})}))

(defn stone-in-lists? [loc]
  (some #(and (= (:loc %) loc) (= nil (:liberty %))) @whole-lists))  ;;not dead

(defn get-neighbors-id [stone]
  (let [n (get-neighbors stone)
	ids [(getid-from-loc (:left n)) (getid-from-loc (:right n)) (getid-from-loc (:down n)) (getid-from-loc (:up n))]]
    (into [] (filter #(not (nil? %)) ids))))
						



(defn liberty-of-stone [stone]
  (let [n (get-neighbors stone)]
    (+ (if (stone-in-lists?  (:left n) ) 0 1 )
       (if (stone-in-lists?  (:right n) ) 0 1 )
       (if (stone-in-lists?  (:up n) ) 0 1 )
       (if (stone-in-lists?  (:down n)) 0 1 ))))

(defn liberty-of-group [group]                    ;;only dead group call it,result is 0
  (let [liberty-list (map liberty-of-stone group)]
    (reduce + liberty-list)))
	 

(defn stone-to-idgroup [groups id-of-neighbors id]  ;put playing stone to groups
       (into []
	 (for [s groups] 
	   (if (includes? s id-of-neighbors)(conj s id) s))))

(defn stone-to-group [stone]
  (let [ids (get-neighbors-id stone)]
    (if  (or (nil? ids) (empty? ids))  (swap! (if (odd? (:id stone)) black-id-groups white-id-groups) conj  [(:id stone)])

     (doseq [id ids]
      (if (and (odd? (:id stone)) (odd? id)) (swap! black-id-groups stone-to-idgroup id (:id stone))
	  (if (and (even? (:id stone))(even? id)) (swap! white-id-groups stone-to-idgroup id (:id stone))))))))

(defn merge-inner-idgroups [groups id] ;id  is new playing stone id,maybe cause to merge groups 
  (let [with-out-id  (filter #(not (includes? % id)) groups)
        with-id  (filter #(includes? % id) groups)]
   (if (empty? with-out-id) [(vec (set (flatten with-id)))]
       (conj (into [] with-out-id) (vec (set (flatten with-id)))))))

(defn merge-inner-groups [groups id]
  (swap! groups merge-inner-idgroups id))

;; helper functions for serialization
;; struct map can't be serialize?
(defn save-to-file [data filename]
  (spit filename
	(with-out-str (pr data))))
(defn load-data-from-file [filename]
  (with-in-str (slurp filename)
	       (read)))

(defn play-one-stone [id loc]
  (let [s (struct stone id loc)
	groups (if (odd? id) black-id-groups white-id-groups)]
    (do
      (stone-to-group s)
      (merge-inner-groups groups id)
      (dosync
       (ref-set whole-lists (conj @whole-lists s))))))
	