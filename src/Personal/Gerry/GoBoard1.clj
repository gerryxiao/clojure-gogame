 (in-ns 'Personal.Gerry.GoGame)
 (import '(java.awt Container Image MediaTracker Toolkit)
	'(java.net URL) '(javax.swing JMenuBar JMenu JMenuItem JCheckBoxMenuItem 
				      JToolBar JToolBar$Separator JButton ImageIcon)
	'(java.awt.event KeyEvent ActionListener))

(def Id (atom 0))

(def main-window (proxy [JFrame ActionListener] [ "Clojure 围棋游戏 作者：gerryxiao@gmail.com"]
		   (actionPerformed [e]
				    (when (and (not (empty? @snapshots)) (.equals (.getActionCommand e) "previous"))
				      (get-snapshot (dec @Id))
				      (swap! Id dec)
				      (.repaint this))
				     				     		      
				    (when (and (.equals (.getActionCommand e) "next") (not= (count @snapshots) @Id))
				      (get-snapshot (inc @Id))
				      (swap! Id inc )
				      (.repaint this))
				      
				    (when (.equals (.getActionCommand e) "last")
				      (get-snapshot (count @snapshots))
				      (compare-and-set! Id @Id (count @snapshots))
				      (.repaint this))
				      
				    (when (.equals (.getActionCommand e) "first")
				      (get-snapshot 1)
				      (compare-and-set! Id @Id 1)
				      (.repaint this)))))
				      
				      
				      
				      
(def paint-id? false)



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
(defn get-x [stone u]
  (- (:x (get-stone-cord stone u)) (/ u 2.0)))
(defn get-y [stone u]
  (- (:y (get-stone-cord stone u)) (/ u 2.0)))

(defn loadImage [url]
  (let [image (.getImage (.getDefaultTookit Toolkit) url)
	mediaTracker (MediaTracker. (Container.))]
    (.addImage mediaTracker image 0)
    (.waitForID mediaTracker 0)
     image))


(def board (proxy [JPanel] []
	     (paintComponent  [g]
		    (proxy-super paintComponent g)
		    (let [g2d #^Graphics2D g
			  w (.getWidth this)
			  u (/ w 20.0)
			  extent (range u (* u 20) u)
			  coords (for [x extent y extent] {:x x :y y})
			  last-stone (last @whole-lists)]
		      (.setRenderingHint g2d RenderingHints/KEY_ANTIALIASING RenderingHints/VALUE_ANTIALIAS_ON)
		      (.draw3DRect g2d 0 0 w w true)
		      (.setColor g2d (Color. 212 167 102))
		      (.fill3DRect g2d 0 0 w w true)
		      (.setColor g2d Color/BLACK)
		      (.setStroke g2d (BasicStroke. (float 1)))
		      (doseq [x extent]
			(.draw g2d (new Line2D$Float u x (* 19 u) x))
			(.draw g2d (new Line2D$Float x u x (* 19 u))))
		      (.draw g2d (Ellipse2D$Float. (* u 3.9) (* u 3.9) (* u 0.2) (* u 0.2)))
		      (.draw g2d (Ellipse2D$Float. (* u 3.9) (* u 15.9) (* u 0.2) (* u 0.2)))
		      (.draw g2d (Ellipse2D$Float. (* u 15.9) (* u 3.9) (* u 0.2) (* u 0.2)))   ;;draw 5 points:xing and tianyuan
		      (.draw g2d (Ellipse2D$Float. (* u 15.9) (* u 15.9) (* u 0.2) (* u 0.2)))
		      (.draw g2d (Ellipse2D$Float. (* u 9.9) (* u 9.9) (* u 0.2) (* u 0.2)))
		      
		      (doseq [stone @whole-lists]
			(when (= (:liberty stone) nil)
			  (.setColor g2d Color/black)
			  (.draw g2d (Ellipse2D$Float. (get-x stone u) (get-y stone u) u u))
			  (if (odd? (:id stone)) (.setColor g2d Color/black)
			      (.setColor g2d Color/white))
			  (.fill g2d (Ellipse2D$Float. (get-x stone u) (get-y stone u) u u))
			  (when paint-id?
			    (.setColor g2d Color/green)
			    (.setFont g2d (Font. "Serif" Font/PLAIN 12))
			    (.drawString g2d (.toString (:id stone)) (float (:x (get-stone-cord stone u)))
					 (float (:y (get-stone-cord stone u)))))))
			
		      (when (and (not (nil? last-stone)) (not paint-id?))
			(if (odd? (:id last-stone)) (.setColor g2d Color/white) (.setColor g2d Color/black))
			(.draw g2d (Ellipse2D$Float. (+ (get-x last-stone  u)(/ u 4.0))
						     (+ (get-y last-stone  u)(/ u 4.0)) (/ u 2.0) (/ u 2.0))))))
	     (getPreferredSize []
			      (Dimension. 500 500))))

		       
		      

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
(def menu-bar (JMenuBar.))
(def file-menu (JMenu. "File"))
(def option-menu (JMenu. "Option"))
(def open-menuitem (JMenuItem. "Save" KeyEvent/VK_T))

(defn menu-init []
  (.add file-menu open-menuitem)
  (.add menu-bar file-menu)
  (.add menu-bar option-menu))



(defn navigate-button [imagename actioncommand tooltiptext alttext]
  (let [ img-loc (str "images/" imagename ".png")
	button (JButton.)]
    (.setActionCommand button actioncommand)
    (.addActionListener button main-window) ;;wait to add 
    (.setIcon button (ImageIcon. img-loc))
    button))

(defn addButtons [jt]
  (let [button1 (navigate-button "previous" "previous" "previous step" "previous")
	button2 (navigate-button "next" "next" "next step " "next")
	button3 (navigate-button "first" "first" "go to first" "first")
	button4 (navigate-button "last" "last" "go to last" "last")]
    (.add jt button1)
    (.add jt button2)
    (.add jt button3)
    (.add jt button4)))
 
(def toolbar (JToolBar. "oops"))


(defn play-go []
  (.setBorder board (BevelBorder. BevelBorder/RAISED))
  (menu-init)
  (addButtons toolbar)
  (doto main-window
    (.setJMenuBar menu-bar)
    (.add board BorderLayout/CENTER)
    (.add toolbar BorderLayout/SOUTH)
    ;(.add menu-bar)
    (.setSize 800 900)
    ;(.pack) ;it seems swing component dont need pack
    (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
    (.setIconImage (.createImage (Toolkit/getDefaultToolkit) "images/clojure"))
    (.setVisible true)))
		      
