;Define Go game core algorithm in this file
;Go game in clojure
;围棋游戏
 
(ns Personal.Gerry.GoGame 
  (:use [clojure.contrib seq-utils duck-streams])
  (:require [clojure.zip :as zip])
  (:import (java.awt Color Graphics Font Graphics2D BasicStroke Image Canvas Dimension RenderingHints BorderLayout)
	   (javax.swing.border BevelBorder)
	   (javax.swing JFrame JPanel BorderFactory) (java.awt.event MouseAdapter MouseEvent ActionEvent)
	   (javax.swing JOptionPane)
	   (java.awt.geom Line2D$Float Ellipse2D$Float AffineTransform )
	   (java.util Properties)(java.io File FileInputStream)
           (sun.audio AudioPlayer AudioStream)
	   (javax.sound.sampled AudioFormat AudioInputStream SourceDataLine DataLine$Info AudioSystem Clip ))
  (:gen-class))

(def *board-size* 19)
(def *debug* true)
(def id (atom 0)) ; stone id number
(def paint-id? false)
(def play-audio? false)
(def undo? false)
(def modes ["play" "view" "review" "game-over"])

(def whole-lists (ref [])) ; use as saving all played stones stones ,includes captured stones 按序储存所有已下棋子包括死子

(defstruct stone :id :loc :liberty)  ;loc is {:x,:y} liberty default to nil, if 0,then it's captured.死子气为0

(def data (atom {:players {:w nil :b nil} :qipu nil :result nil :comments []  :handicap nil})) ;; data will be saved in disks

(def go-config (atom {:sound nil :coord nil :mode nil :paint-id nil :undo nil}))  ;;config will be saved to disk

(def file-separator File/separator)

(def snapshots (atom [])) ; environments  snapshots

(def black-id-groups (atom [])) ;black groups grouped by id,dead groups will be removed. [ [1 2 3]]

(def white-id-groups (atom []))

(def w-captured-groups (atom []))

(def b-captured-groups (atom [])) ; dead-groups move to here as backup data

(defn simply-whole-lists [lists]   ;;simply whole-lists in order to save smaller data
  (for [s lists] (vals (get-in s [:loc]))))

(defn reflect-warn [ ]   ;debug mode setup
  (if *debug* (set! *warn-on-reflection* true) 
      (set! *warn-on-reflection* false)))

(reflect-warn)

(defn reset-data []
  (swap! data assoc :players {:w nil :b nil} :qipu nil :result nil :comments [] :handicap nil))

(defn get-players-name [the-data]
  (let [players (the-data :players)]
    (if players players {:w "nobody" :b "nobody"})))

(defn get-comments [the-data]
  (:comment the-data))

(defn get-qipu [the-data]
  (:qipu the-data))

(defn get-result [the-data]
  (:result the-data))

(defn set-players-name [name]
  (swap! data assoc :players name))

(defn set-white-player-name [name]
  (swap! data assoc-in [:players :w] name))
(defn set-black-player-name [name]
  (swap! data assoc-in [:players :b] name))

(defn set-result [theResult]
  (swap! data assoc :result theResult))

(defn add-to-comments [n comm]
  (let [content (get-comments data)]
    (swap! data assoc :comment (conj content {:id n :comment comm}))))

(defn getloc-from-id [id]
  (get-in @whole-lists [(dec id) :loc]))


(defn getid-from-loc [loc]
  (if (or (nil? loc) (empty? @whole-lists)) nil
      (first (for [s @whole-lists :when (and (= (:loc s) loc) (= (:liberty s) nil))] (:id s)))))

(defn get-neighbors [stone]
     (let [x (:x (:loc stone)) y (:y (:loc stone))]
       {:left (if (= x 0) nil {:x (dec x) :y y}) :right (if (= x 18) nil {:x (inc x) :y y})
	:down (if (= y 0) nil {:x x :y (dec y)}) :up (if (= y 18) nil { :x x :y (inc y)})}))

(defn stone-in-lists? [loc]
  (if (nil? loc) true
      (some #(and (= (:loc %) loc) (= nil (:liberty %))) @whole-lists)))  ;;not dead

(defn get-neighbors-id [stone]
  (let [n (get-neighbors stone)
	ids [(getid-from-loc (:left n)) (getid-from-loc (:right n)) (getid-from-loc (:down n)) (getid-from-loc (:up n))]]
    (into [] (filter #(not (nil? %)) ids))))
						



(defn liberty-of-stone [id]
  (let [stone (nth @whole-lists (dec id))
	n (get-neighbors stone)]
    (+ (if (stone-in-lists?  (:left n) ) 0 1 )
       (if (stone-in-lists?  (:right n) ) 0 1 )  ;;problem is here,solved
       (if (stone-in-lists?  (:up n) ) 0 1 )
       (if (stone-in-lists?  (:down n)) 0 1 ))))

(defn liberty-of-group [group]                    ;;only dead group call it,result is 0
  (let [liberty-list (map liberty-of-stone group)]
    (reduce + liberty-list)))

(defn liberty-of-group-real [group] ;qi of group
  (let [grp (for [id group] (nth @whole-lists (dec id)))  
	ns (for [s grp] (filter (complement nil?)(vals (get-neighbors s))))
	part1 (filter stone-in-lists? (flatten ns))
	part2 (filter (complement stone-in-lists?) (flatten ns))]
    ;(println "grp" grp)
    ;(println "part1 " part1)
    ;(println "ns " (flatten ns))
    (- (liberty-of-group group) (- (count part2) (count (set part2))))))
	
       
	 

(defn stone-to-idgroup [groups id-of-neighbors id]  ;put playing stone to groups
       (into []
	 (for [s groups] 
	   (if (includes? s id-of-neighbors)(conj s id) s))))

(defn stone-to-group [stone] ;; same color group
  (let [idss (get-neighbors-id stone)
	ids (if (odd? (:id stone)) (filter #(odd? %) idss) (filter #(even? %) idss))]
			   
    (if  (or (nil? ids) (empty? ids))  (swap! (if (odd? (:id stone)) black-id-groups white-id-groups) conj  [(:id stone)])

     (doseq [id ids]
      (if (odd? (:id stone)) (swap! black-id-groups stone-to-idgroup id (:id stone))
	  (swap! white-id-groups stone-to-idgroup id (:id stone)))))))

(defn merge-inner-idgroups [groups id] ;id  is new playing stone id,maybe cause to merge groups 
  (let [with-out-id  (filter #(not (includes? % id)) groups)
        with-id  (filter #(includes? % id) groups)]
   (if (empty? with-out-id) [(vec (set (flatten with-id)))]
       (conj (into [] with-out-id) (vec (set (flatten with-id)))))))

(defn merge-inner-groups [groups id]
  (swap! groups merge-inner-idgroups id))

;; helper functions for serialization

(defn save-to-file [data filename]
  (spit filename
	(with-out-str (pr data))))
(defn load-data-from-file [filename]
  (with-in-str (slurp filename)
	       (read)))

;; dead stones cal

(defn scan-groups-liberty [groups]
  (vec (filter #(not= (liberty-of-group %) 0) groups)))

(defn get-dead-ids [groups]
  (vec (filter #(= (liberty-of-group %) 0) groups)))

(defn remove-dead-stones [groups]
  (swap! groups scan-groups-liberty))

(defn move-dead-stones [groups back]  ;move dead stones to back vector
  (let [ids (flatten (get-dead-ids groups))]
    (when (not (empty? ids))
      (swap! back into ids))))

(defn mark-deadstones [groups]
  (let [deadstones (get-dead-ids groups)]
    (if (not (empty? deadstones))
       (doseq [id (flatten deadstones)]
	 (let [loc (getloc-from-id id)]
	  (dosync ( alter whole-lists assoc (dec id) (struct stone id loc 0))))))))

(defn test-content [msg]   ;;debug 
  (if *debug*
    (do (println msg)
      ;(println "whole-list is: " @whole-lists)
	(println "black is : " @black-id-groups)
	(println "white is : " @white-id-groups)
	(println "dead blacks: " @w-captured-groups)
	(println "dead whites: " @b-captured-groups))))

(defstruct snapshot :id :lists :bgroups :wgroups :bdead-grpus :wdead-groups)

(defn save-snapshot [id lists bg wg bdg wdg]
  (let [snap (struct snapshot id lists bg wg bdg wdg)]
    (swap! snapshots conj snap)))

(defn reset-snaps []
  (swap! snapshots empty))

(defn reset-envs []   ;clear global envs for reset
  (swap! black-id-groups empty)
  (swap! white-id-groups empty)
  (swap! b-captured-groups empty)
  (swap! w-captured-groups empty)
  (compare-and-set! id @id 0)
  (dosync 
   (alter whole-lists empty)))


(defn get-snapshot [n]
  (let [snap (get @snapshots (dec n) )]
    (reset! id  (:id snap)) 
    (reset! black-id-groups  (:bgroups snap))
    (reset! white-id-groups  (:wgroups snap))
    (reset! b-captured-groups (:bdead-groups snap))
    (reset! w-captured-groups (:wdead-groups snap))
    (dosync 
     (ref-set whole-lists (:lists snap)))))

(declare play-sound)	
	      
(declare dead-point?)

(defn play-one-stone [number loc]
  (let [s (struct stone number loc)
	groups (if (odd? number) black-id-groups white-id-groups)]
    (stone-to-group s)
    (merge-inner-groups groups number)
    (dosync
     (alter whole-lists conj s))
    ;(test-content "before mark and remove")
    (let [groups (if (odd? number) white-id-groups black-id-groups)
	  back (if (odd? number) b-captured-groups w-captured-groups)]
      (move-dead-stones @groups back)
      (mark-deadstones @groups)
      (remove-dead-stones groups)
      (save-snapshot number @whole-lists @black-id-groups @white-id-groups @b-captured-groups @w-captured-groups)
      )))

(defn forbidden-point? [aid loc]  ;;check forbidden point which can't be played
    (binding [whole-lists (ref @whole-lists)
	      black-id-groups (atom @black-id-groups)
	      white-id-groups (atom @white-id-groups)
	      b-captured-groups (atom @b-captured-groups)
	      w-captured-groups (atom @w-captured-groups)
	      snapshots (atom [])]
      (play-one-stone aid loc)
      (let [grp (if (odd? aid) @black-id-groups @white-id-groups) ]
	(if (includes? (map liberty-of-group-real grp) 0) true false))))
					   
						  
	    
(defn load-data [simple-whole-lists]
  (binding [whole-lists (ref [])     ;setup environment to protect real environs
	    black-id-groups (atom [])
	    white-id-groups (atom [])
	    b-captured-groups (atom [])
	    w-captured-groups (atom [])
	    snapshots (atom [])
	    ]
    (dotimes [idx (count simple-whole-lists)] 
      (let [e (nth simple-whole-lists idx)]
	(play-one-stone (inc  idx) {:x (first e) :y (second e)})))
    {:snapshots @snapshots}))  

(defn load-data1 [simple-whole-lists]  ;change environmnet to loading data
  (let [e (load-data simple-whole-lists)
        sn (:snapshots e)]
    (reset! snapshots sn)
    ;(println "snaps is" @snapshots)
    ;(println)
    ;(println "sn is " sn)
    (get-snapshot (count @snapshots))
    ))
	    
	    
	

(defn go [n x y]
  (play-one-stone n {:x x :y y}))


(defn play-sound [filename]
  (let [soundfile (File. filename)
	audioinputstream (AudioSystem/getAudioInputStream soundfile)
	audioformat (.getFormat audioinputstream)
	datalineinfo (DataLine$Info.  SourceDataLine audioformat)
	sourcedataline #^SourceDataLine (AudioSystem/getLine datalineinfo)
	tempbuffer (make-array (. Byte TYPE) 1000)]
    (.open sourcedataline audioformat)
    (.start sourcedataline)
    (loop []
      (let [cnt (.read audioinputstream tempbuffer 0 (count tempbuffer))]
	(when (= cnt -1) nil)
	(when (> cnt 0) 
	  (.write sourcedataline tempbuffer 0 cnt)
	  (recur))))
    (.drain sourcedataline)
    (.close sourcedataline)))

(defn play-sound2 [filename]
  (with-open [audioinputstream (AudioSystem/getAudioInputStream (File. filename))
		clip (AudioSystem/getClip )]
    (doto clip 
      (.open audioinputstream)
      (.start)
      (.drain))))

(defn play-sound1 [filename]
  (let [in (FileInputStream. filename)
	as (AudioStream. in)]
    (.start AudioPlayer/player as)))

;;dialog-box

(defn show-warning-box [ parent msg title]
  (JOptionPane/showMessageDialog parent msg title JOptionPane/WARNING_MESSAGE))
(load "GoBoard1")
(load "aux-board")	    
(defn -main []
  (play-go))

(-main)