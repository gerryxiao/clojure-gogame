(import '(javax.swing JFrame JLabel))
(import '(java.awt Color Font Graphics Canvas))
(import '(java.awt.event MouseEvent MouseAdapter))
(def main-window (new JFrame  "Clojure Go Game Test"))
(def board (proxy [Canvas] []
	     (paint [g]
		    (doseq [x (range 20 780 40)]
		      (.drawLine g 20 x 740 x)
		      (.drawLine g x 20 x 740))
		    (doseq [stone @whole-lists]
		      (if (= (:liberty stone) nil)
			(.drawOval (:x stone) (:y stone) 5 4)
			(if (odd? (:id stone) (.setColor g Color/BLACK)
				  (.setColor g Color/WHITE)))
			(.fillOval (:x stone) (:y stone 5 4)))))))
(def Id (atom 0))		      

(.addMouseListener board (proxy [MouseAdapter] []
				 (mousePressed [e]
				      (println {:x (.getX e) :y (.getY e)})
				      (go @(swap! id inc) (.getX e) (.getY e))
				      (.repaint board)
				      
				      (println "repaint..."))))

(defn play-go []
  (doto main-window
    (.setSize 900 900)
    (.add board)
    (.pack)
    (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
    (.setVisible true)))
		      
