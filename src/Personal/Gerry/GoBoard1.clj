(def main-window (new JFrame  "Clojure Go Game Test"))

(defn nearby [a b]
  (and (> a (* 0.95 b)) (< a (* 1.05 b))))
  
(defn set-xy [x y coords]  ; get x y from real coords 
  (for  [p coords :when (and (nearby x (:x p)) (nearby y (:y p)))]  
      {:x (:x p) :y (:y p)}))

(defn trans-coord [x y u] ;;from real coords to 
  {:x (/ (- x u) u) :y (/ (- y u) u)})

(defn trans-coord1 [ x y u] ;;from virtual to real coords
  {:x (* (inc x) u) :y (* (inc y) u)})

(defn get-stone-cord [stone u]
  (trans-coord1 (:x (:loc stone)) (:y (:loc stone)) u))

(def board (proxy [JPanel] []
	     (paintComponent  [g]
		    (proxy-super paintComponent g)
		    (let [g2d #^Graphics2D g
			  w (.getWidth this)
			  u (/ w 20.0)
			  extent (range u (* u 20) u)
			  coords (for [x extent y extent] {:x x :y y})]
		      (.draw3DRect g2d 1 1 w w true)
		      (.setColor g2d Color/yellow)
		      (.fill3DRect g2d 1 1 w w true)
		      (.setColor g2d Color/BLACK)
		      (.setStroke g2d (BasicStroke. (float 1)))
		      (doseq [x extent]
			(.draw g2d (new Line2D$Float u x (* 19 u) x))
			(.draw g2d (new Line2D$Float x u x (* 19 u))))
		      (doseq [stone @whole-lists]
			(when (= (:liberty stone) nil)
			  (.draw g2d (Ellipse2D$Float. (- (:x (get-stone-cord stone u)) (/ u 2.0)) (- (:y (get-stone-cord stone u)) (/ u 2.0)) u u))
			  (if (odd? (:id stone)) (.setColor g2d Color/BLACK)
			      (.setColor g2d Color/WHITE))
			  (.fill g2d (Ellipse2D$Float. (- (:x (get-stone-cord stone u)) (/ u 2.0)) (- (:y (get-stone-cord stone u)) (/ u 2.0)) u u))))))
	     (getPreferredSize []
			      (Dimension. 600 600))))

		       
(def Id (atom 0))		      

(.addMouseListener board (proxy [MouseAdapter] []
				 (mousePressed [e]
				      (println {:x (.getX e) :y (.getY e)})
				      (let [x (.getX e) y (.getY e) 
					    w (.getWidth board)
					    u (/ w  20)
					    extent (range u (* u 20) u)
					    coords (for [a extent b extent] {:x a :y b})
					    xy  (set-xy x y coords)
					    XY (if (empty? xy) nil (trans-coord (:x (first xy)) (:y (first xy)) u))]
					    
					(when (not (nil? XY))
					  (let [x (Math/round (float (:x XY))) y (Math/round (float (:y XY)))]
					    (if (stone-in-lists? {:x x :y y}) (println "oops you canot play there")
						(do 
						  (swap! Id inc)
					  
						  (go @Id x y)
						  (.repaint board)
				      
						  (println "repaint...")))))))))

(defn play-go []
  (doto main-window
    (.add board)
    (.setSize 800 800)
    ;;(.pack) it seems swing component dont need pack
    (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
    (.setVisible true)))
		      
