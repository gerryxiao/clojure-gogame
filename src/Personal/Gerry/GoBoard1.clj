 ;;go game gui file

(in-ns 'Personal.Gerry.GoGame)
(import '(java.awt Container Image MediaTracker Toolkit)
	 '(java.net URL) '(javax.swing JMenuBar JMenu JMenuItem JCheckBoxMenuItem 
				       JToolBar JToolBar$Separator JButton ImageIcon JFileChooser)
	 '(java.awt.event KeyEvent ActionListener)
	 '(javax.swing JCheckBoxMenuItem)
	 '(java.awt.image BufferedImage)
	 '(javax.imageio ImageIO))
(def screen-size 
     (let [screensize (.. Toolkit getDefaultToolkit getScreenSize)]
       {:w (.getWidth screensize) :h (.getHeight screensize)}))

(def max-length (if (> (:h screen-size) (:w screen-size )) (:w screen-size) (:h screen-size))) ;;availble max width and height for board
(def bs (* 0.8 max-length))
(def fs-w (* 1.18 bs))
(def fs-h (+ bs 30))
(def au-w (* 0.2 max-length))
(def au-h bs)

(def pframe-size (Dimension. fs-w fs-h))  ;preferred size
(def mframe-size (Dimension. (* 1.15 (* 1.0 max-length)) (* 1.0 max-length))) ;;max size
(def pboard-size (Dimension. bs bs))
(def mboard-size (Dimension. (* 0.9 max-length) (* 0.9 max-length)))
(def paux-size (Dimension. au-w au-h))
(def maux-size (Dimension. (* 0.2 (:h screen-size)) (.getHeight mboard-size)))

(declare set-comment)	 
(def main-window (proxy [JFrame ActionListener] [ "Clojure 围棋游戏 作者：gerryxiao@gmail.com"]
   (actionPerformed [#^ActionEvent e]
     (when (and (not (empty? @whole-lists)) (.equals (.getActionCommand e) "previous"))
       (condp = @id
	 1 (do (reset-envs)
	       (.repaint #^JFrame this))
	 (do (get-snapshot (dec @id)) 
	     (println "id is " @id)
	     (set-comment)
	     (println "count is" (count @snapshots))
	     (.repaint #^JFrame this))))
				     				     		      
     (when (and (.equals (.getActionCommand e) "next") (> (count @snapshots) @id))
       (get-snapshot (inc @id))
       (set-comment)
       (.repaint #^JFrame this))
				      
     (when (.equals (.getActionCommand e) "last")
       (get-snapshot (count @snapshots))
       (.repaint #^JFrame this))
				      
     (when (.equals (.getActionCommand e) "first")
       (reset-envs)
       (.repaint #^JFrame this))

     (when (and (not (empty? @whole-lists)) (.equals (.getActionCommand e) "undo"))
					;(get-snapshot (dec @id))  ;;problem
					;(swap! snapshots pop)
       (condp = @id
	 1 (do (reset-envs) (swap! snapshots empty)
	       (.repaint #^JFrame this))
	 (do (get-snapshot (dec @id)) 
	     (swap! snapshots pop)
	     (.repaint #^JFrame this)))))
		      
   (getPreferredSize []
		     pframe-size)
   (getMaximumSize   []
		     mframe-size)
   ))
				      
				      


(defn nearby [a b]
  (and (> a (* 0.95 b)) (< a (* 1.05 b))))
  
(defn set-xy [x y coords]  ; get x y from real coords 
  (for  [p coords :when (and (nearby x (:x p)) (nearby y (:y p)))]  
      {:x (:x p) :y (:y p)}))

(defn trans-coord [x y u] ;;from real coords to 
  {:x (/ (- x u) u) :y (/ (- y u) u)})

(defn trans-coord1 [ x y u] ;;from virtual to real coords
  {:x (*  (inc x) u) :y (* (inc  y)  u)})

(defn get-stone-cord [stone u]
  (trans-coord1 (:x (:loc stone)) (:y (:loc stone)) u))
(defn get-x [stone u]
  (- (:x (get-stone-cord stone u)) (/ u 2.0)))
(defn get-y [stone u]
  (- (:y (get-stone-cord stone u)) (/ u 2.0)))

(defn loadImage [url]
  (let [#^Image image  (.getImage  (Toolkit/getDefaultToolkit) url)
	mediaTracker (MediaTracker. (Container.))]
    (.addImage mediaTracker image 0)
    (.waitForID mediaTracker 0)
     image))

(def draw-coords? true)

(def board (proxy [JPanel] []
  (paintComponent [g]
     (proxy-super paintComponent #^Graphics2D g)
     (let [g2d #^Graphics2D g
	   w (.getWidth #^JPanel this)
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
       (.setFont g2d (Font. "Times" Font/BOLD 8))
       (doall
	(map #(.draw g2d (new Line2D$Float u % (* 19 u) %)) extent))
       (doall
	(map #(.draw g2d (new Line2D$Float % u % (* 19 u))) extent))
       (when draw-coords? 
	 (doall
	  (map #(.drawString g2d (str (char (+ 96 (Math/round (float (/ % u)))))) (float (* 0.5 u)) (float %)) extent))
	 (doall 
	  (map #(.drawString #^Graphics2D g2d (str (Math/round (float (/ %  u)))) (float %)(float (* 0.5 u))) extent)))
       (.draw g2d (Ellipse2D$Float. (* u 3.9) (* u 3.9) (* u 0.2) (* u 0.2)))
       (.draw g2d (Ellipse2D$Float. (* u 3.9) (* u 15.9) (* u 0.2) (* u 0.2)))
       (.draw g2d (Ellipse2D$Float. (* u 15.9) (* u 3.9) (* u 0.2) (* u 0.2))) ;;draw 5 points:xing and tianyuan
       (.draw g2d (Ellipse2D$Float. (* u 15.9) (* u 15.9) (* u 0.2) (* u 0.2)))
       (.draw g2d (Ellipse2D$Float. (* u 9.9) (* u 9.9) (* u 0.2) (* u 0.2)))
		      
       (doseq [stone @whole-lists]
	 (when (and (not-empty stone) (= (:liberty stone) nil))
			  
			  ; (let [bimg (loadImage (str "images" file-separator "black.gif"))  ;;change old loadImage to new ImageIO
			  ;wimg (loadImage (str "images" file-separator "white.gif"))]
	   (let [bimg (ImageIO/read (File. (str "images" file-separator "black.gif")))
		 wimg (ImageIO/read (File. (str "images" file-separator "white.gif")))]	
			   
	     (.setRenderingHint g2d RenderingHints/KEY_ANTIALIASING RenderingHints/VALUE_ANTIALIAS_ON)
			  
			  
			  
	     (if (odd? (:id stone))
					; (.drawImage g2d bimg at this)
					; (.drawImage g2d wimg at this)))
			     
	       (.drawImage g2d bimg (get-x stone u) (get-y stone u) u u this)
	       (.drawImage g2d wimg (get-x stone u) (get-y stone u) u u this)))
					;(.fill g2d (Ellipse2D$Float. (get-x stone u) (get-y stone u) u u))
	   (when paint-id?
	     (.setColor g2d Color/red)
	     (.setFont g2d (Font. "Times" Font/PLAIN 6))
	     (.drawString g2d (str (:id stone)) (float (-  (:x (get-stone-cord stone u)) (* u 0.1)))
			  (float (-  (:y (get-stone-cord stone u)) (* u 0.03)))))))
			
       (when (and (not (nil? last-stone)) (not paint-id?))
	 (if (odd? (:id last-stone)) (.setColor g2d Color/white) (.setColor g2d Color/black))
	 (.draw g2d (Ellipse2D$Float. (+ (get-x last-stone  u)(/ u 4.0))
						   (+ (get-y last-stone  u)(/ u 4.0)) (/ u 2.0) (/ u 2.0))))))
  (getPreferredSize []
		    pboard-size)
  (getMaximumSize  []
		   mboard-size)))

		       
(declare w-b-button setup-mode msg-area)		      

(.addMouseListener #^JPanel board 
    (proxy [MouseAdapter] []
      (mousePressed [#^MouseEvent e]
	(when (= (count @snapshots) (count @whole-lists))
	  (println {:x (.getX e) :y (.getY e)}) ;debug
	  (let [x (.getX e) y (.getY e) 
		w (.getWidth #^JPanel board)
		u (/ w  20.0)
		extent (range u (* u 20.0) u)
		coords (for [a extent b extent] {:x a :y b})
		xy  (set-xy x y coords)
		XY (if (empty? xy) nil (trans-coord (:x (first xy)) (:y (first xy)) u))]
					    
	    (when (not (nil? XY))
	      (let [x (Math/round (float (:x XY))) y (Math/round (float (:y XY)))]
		(if (stone-in-lists? {:x x :y y}) (println "sorry you canot play there")
		    (when-not (forbidden-point? (inc @id) {:x x :y y}) ;; check probidden point
		      (do 
			(swap! id inc)
			(when play-audio? (.start (Thread. #(play-sound1 "stone.wav"))))
					  
			(go @id x y)
			(if (even? @id)
			  (.setIcon w-b-button (ImageIcon. "images/gogui-black-24x24.png"))
			  (.setIcon w-b-button (ImageIcon. "images/gogui-white-24x24.png")))))))))))))
						  ;(.repaint #^JPanel board)
						  

(def lists-watcher (agent 0))
(defn lists-watcher-action [v r]  ;; v is the state of agent,and r is the ref 
  (.repaint board)
  (println "value of agent is" v)
  (inc v))
(add-watcher whole-lists :send-off lists-watcher lists-watcher-action)

(def capture-watcher (agent 0))
(defn capture-watcher-action [v r]
     (if play-audio? (play-sound1 "stone.wav")
	 (inc v)))
(add-watcher w-captured-groups :send-off capture-watcher capture-watcher-action)
(add-watcher b-captured-groups :send-off capture-watcher capture-watcher-action)

(def menu-bar (JMenuBar.))
(def file-menu (JMenu. "File"))
(def option-menu (JMenu. "Option"))
(def help-menu (JMenu. "Help"))
(def new-menuitem (JMenuItem. "New" KeyEvent/VK_N))
(def open-menuitem (JMenuItem. "Open" KeyEvent/VK_O))
(def save-menuitem (JMenuItem. "Save" KeyEvent/VK_S))
(def exit-menuitem (JMenuItem. "Exit" KeyEvent/VK_Q))
(def about-menuitem (JMenuItem. "About"))
(def manual-menuitem (JMenuItem. "Manual"))

(def paint-menuitem (JCheckBoxMenuItem. "Enable Id" false))
(def audio-menuitem (JCheckBoxMenuItem. "Enable Audio" false))
(def undo-menuitem (JCheckBoxMenuItem. "Enable undo" false))
(def coord-menuitem (JCheckBoxMenuItem. "Enable coords" false))

(defmacro mice-listen [target & action]
  `(. ~target addMouseListener (proxy [MouseAdapter] []
				 (mousePressed [e#]
					       ~@action ))))
(defmacro action-listen [target & action]
  `(. ~target addActionListener (proxy [ActionListener] [] 
				  (actionPerformed [e#]
						   ~@action))))
(mice-listen new-menuitem (reset-envs) (reset-snaps) (reset-data) (.repaint main-window))

(mice-listen save-menuitem (let [fc (JFileChooser.)
				 return (.showSaveDialog fc nil)]
			     (if (= return JFileChooser/APPROVE_OPTION)
			       (let [file (.getSelectedFile fc)
				     the-data (assoc @data :qipu ( simply-whole-lists @whole-lists))]
				 (println (.getPath file))
				 (save-to-file the-data (str (.toURL file)))))))

(mice-listen open-menuitem (let [fc (JFileChooser.)
				 return (.showOpenDialog fc nil)]
			     (if (= return JFileChooser/APPROVE_OPTION)
			       (let [file (.getSelectedFile fc)
				     data1 (load-data-from-file (.getPath file))
				     qipu (:qipu data1)]
				 (reset! data data1)
				 (println (.getName file))
					;(compare-and-set! snapshots @snapshots data)
					;(get-snapshot (count data))
				 (load-data1 qipu)
				 ;(println "whole-lists is" @whole-lists)
				 (.repaint #^JFrame main-window)))))

(mice-listen exit-menuitem (System/exit 0))

(mice-listen paint-menuitem (alter-var-root (var paint-id?) not) (.repaint board))
	     
(mice-listen audio-menuitem (alter-var-root (var play-audio?) not) (.repaint board))

(mice-listen undo-menuitem (alter-var-root (var undo?) not))

(mice-listen coord-menuitem (alter-var-root (var draw-coords?) not)(.repaint board))

						 

(defn menu-init []
  (.add file-menu new-menuitem)
  (.addSeparator file-menu)
  (.add #^JMenu file-menu #^JMenuItem open-menuitem)
  (.addSeparator file-menu)
  (.add #^JMenu file-menu #^JMenuItem save-menuitem)
  (.addSeparator file-menu)
  (.add file-menu exit-menuitem)
  (.add option-menu paint-menuitem)
  (.addSeparator option-menu)
  (.add option-menu audio-menuitem)
  (.addSeparator option-menu)
  (.add option-menu undo-menuitem)
  (.add option-menu coord-menuitem)
  (.add help-menu about-menuitem)
  (.addSeparator help-menu)
  (.add help-menu manual-menuitem)
  (.add #^JMenuBar menu-bar #^JMenu file-menu)
  (.add #^JMenuBar menu-bar #^JMenu option-menu)
  (.add menu-bar help-menu)
  (.setBorder menu-bar (BorderFactory/createBevelBorder BevelBorder/LOWERED)))


(defn navigate-button [imagename actioncommand tooltiptext alttext]
  (let [ img-loc (str "images" file-separator imagename ".png")
	button (JButton.)]
    (.setActionCommand button actioncommand)
    (.addActionListener button main-window) ;;wait to add 
    (.setIcon button (ImageIcon. img-loc))
    button))
(declare w-b-button)
(defn addButtons [#^JToolBar jt]
  (let [#^JButton button1 (navigate-button "previous" "previous" "previous step" "previous")
	#^JButton button2 (navigate-button "next" "next" "next step " "next")
	#^JButton button3 (navigate-button "first" "first" "go to first" "first")
	#^JButton button4 (navigate-button "last" "last" "go to last" "last")
	#^JButton button5 (navigate-button "pass" "undo" "undo to previous" "undo")]
    (.add  jt button1)
    (.addSeparator jt)
    (.add jt button2)
    (.addSeparator jt)
    (.add jt button3)
    (.addSeparator jt)
    (.add jt button4)
    (.addSeparator jt)
    (.add jt button5)
    (.addSeparator jt)
    (.add jt w-b-button)))
(def w-b-button (JButton. (ImageIcon. "images/clojure2.png")))
 
(def toolbar (JToolBar. "oops"))

(defn setup-mode [mode]
  (condp = mode
    "play" (.setVisible toolbar false)
    "review" (.setVisible toolbar true)
    "view" (.setVisible toolbar true)
    "game-over" (.setVisible toolbar true)))

(declare aux-board)


(defn play-go []
  (.setEnabled toolbar false)
  (.setBorder #^JPanel board (BevelBorder. BevelBorder/RAISED))
  (menu-init)
  (addButtons toolbar)
  (setup-mode "review")
  (.setBorderPainted #^JToolBar toolbar true)
  (.setPreferredSize aux-board paux-size)
  ;(.setPreferredSize board (Dimension. 800 800))
  ;(.setPreferredSize toolbar (Dimension. 600 50))
  (doto #^JFrame main-window
	  (.setJMenuBar menu-bar)
    
	  (.add #^JPanel  board BorderLayout/CENTER)
	  (.add #^JToolBar toolbar BorderLayout/NORTH)
	  (.add aux-board BorderLayout/EAST)
    	  (.pack) 
	  (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
	  (.setIconImage (.createImage (Toolkit/getDefaultToolkit) "images/clojure-icon.gif"))
	  (.setVisible true)))

		      
