(import '(javax.swing JFrame JLabel))
(import '(java.awt Color Font Graphics Canvas))
(import '(java.awt.event MouseEvent MouseAdapter))
(def main-window (new JFrame  "Clojure Go Game Test"))
(defn abjust-cord [x]
  (/ (- x 20) 40))

(defn get-stone-cord [stone]
  (let [x (:x (:loc stone)) y (:y (:loc stone))]
    {:x (+ (* x 40) 20) :y (+ (* y 40) 20)}))

(def board (proxy [Canvas] []
	     (paint [g]
		    (doseq [x (range 20 780 40)]
		      (.drawLine g 20 x 740 x)
		      (.drawLine g x 20 x 740))
		    (doseq [stone @whole-lists]
		      (when (= (:liberty stone) nil)
			(.drawOval g (- (:x (get-stone-cord stone)) 15) (- (:y (get-stone-cord stone)) 15) 30 30)
			(if (odd? (:id stone)) (.setColor g Color/BLACK)
				  (.setColor g Color/WHITE))
			(.fillOval g (- (:x (get-stone-cord stone)) 15) (- (:y (get-stone-cord stone)) 15) 30 30))))))
(def Id (atom 0))		      

(.addMouseListener board (proxy [MouseAdapter] []
				 (mousePressed [e]
				      (println {:x (.getX e) :y (.getY e)})
				      (let [x (.getX e) y (.getY e) x1 (* (Math/round (/ x 20.0)) 20) y1 (* (Math/round (/ y 20.0)) 20)]
					(swap! Id inc)
					(go @Id (abjust-cord x1) (abjust-cord y1))
					(.repaint board)
				      
					(println "repaint...")))))

(defn play-go []
  (doto main-window
    (.setSize 900 900)
    (.add board)
    (.pack)
    (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
    (.setVisible true)))
		      
