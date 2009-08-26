   (def main-window (new JFrame  "Clojure Go Game Test"))
(defn abjust-cord [x]
  (/ (- x 20) 40))

(defn get-stone-cord [stone]
  (let [x (:x (:loc stone)) y (:y (:loc stone))]
    {:x (+ (* x 40) 20) :y (+ (* y 40) 20)}))

(def board (proxy [Canvas] []
	     (paint [g]
		    (let [g2d #^Graphics2D g]
		      ;(.setStroke g2d (BasicStroke.(float 5)))
		      (.draw3DRect g2d 1 1 760 760 true)
		      (.setColor g2d Color/yellow)
		      (.fill3DRect g2d 1 1 760 760 true)
		      (.setColor g2d Color/BLACK)
		      (.setStroke g2d (BasicStroke. (float 1)))
		      (doseq [x (range 20 780 40)]
			(.draw g2d (new Line2D$Float 20 x 740 x))
			(.draw g2d (new Line2D$Float x 20 x 740)))
		      (doseq [stone @whole-lists]
			(when (= (:liberty stone) nil)
			  (.draw g2d (Ellipse2D$Float. (- (:x (get-stone-cord stone)) 20) (- (:y (get-stone-cord stone)) 20) 40 40))
			  (if (odd? (:id stone)) (.setColor g2d Color/BLACK)
			      (.setColor g2d Color/WHITE))
			  (.fill g2d (Ellipse2D$Float. (- (:x (get-stone-cord stone)) 20) (- (:y (get-stone-cord stone)) 20) 40 40))))))
	     (update [g]
		     (let [offscreenimage (.createImage this 800 800)
			   graphics #^Graphics2D (.getGraphics offscreenimage)]
		       (.setColor graphics (.getBackground this))
		       (.fillRect graphics 0 0 800 800)
		       (.setColor graphics (.getColor g))
		       (.paint this graphics)
		       (.drawImage g offscreenimage 0 0 Color/black  nil)))))

		       
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
    (.setSize 800 800)
    (.add board)
    (.pack)
    (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
    (.setVisible true)))
		      
