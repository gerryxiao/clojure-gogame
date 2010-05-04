;;go game gui file

(in-ns 'Personal.Gerry.GoGame)
(import '(java.awt Container Image MediaTracker Toolkit)
	 '(java.net URL) '(javax.swing JMenuBar JMenu JMenuItem JCheckBoxMenuItem 
				       JToolBar JToolBar$Separator JButton ImageIcon JFileChooser)
	 '(java.awt.event KeyEvent ActionListener)
	 '(javax.swing JCheckBoxMenuItem JTextArea)
	 '(java.awt.image BufferedImage)
	 '(javax.imageio ImageIO))
(def screen-size 
     (let [#^Dimension screensize (.. Toolkit getDefaultToolkit getScreenSize)]
       {:w (.getWidth screensize) :h (.getHeight screensize)}))

(def max-length (if (> (:h screen-size) (:w screen-size )) (:w screen-size) (:h screen-size))) ;;availble max width and height for board
(def bs (* 0.8 max-length))
(def fs-w (* 1.15 bs))
(def fs-h (+ bs 30))
(def au-w (* 0.2 max-length))
(def au-h bs)

(def pframe-size (Dimension. fs-w fs-h))  ;preferred size
(def mframe-size (Dimension. (* 1.15 (* 1.0 max-length)) (* 1.0 max-length))) ;;max size
(def pboard-size (Dimension. bs bs))
(def mboard-size (Dimension. (* 0.9 max-length) (* 0.9 max-length)))
(def paux-size (Dimension. au-w au-h))
(def maux-size (Dimension. (* 0.2 (:h screen-size)) (.getHeight #^Dimension mboard-size)))

(declare msg-area)

(def main-window (proxy [JFrame ActionListener] [ "Clojure 围棋游戏 作者：gerryxiao@gmail.com"]
   (actionPerformed [#^ActionEvent e]
     (when (and (not (empty? @whole-lists)) (.equals (.getActionCommand e) "previous"))
       (condp = @id
	 1 (do (reset-envs)
	       (.repaint #^JFrame this))
	 (do (get-snapshot (dec @id))
	   (when-let [comm (get-from-comments (dec @id))] (.setText #^JTextArea msg-area comm))
	   (.repaint #^JFrame this))))
				     				     		      
     (when (and (.equals (.getActionCommand e) "next") (> (count @snapshots) @id))
       (get-snapshot (inc @id))
       (when-let [comm (get-from-comments (inc @id))] (.setText #^JTextArea msg-area comm))
       (.repaint #^JFrame this))
	  		      
     (when (.equals (.getActionCommand e) "last")
       (get-snapshot (count @snapshots))
       (when-let [comm (get-from-comments (count @snapshots))] (.setText #^JTextArea msg-area comm))
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
(defn nearby1 [a b u]
  (let [distance (Math/abs (float (- a b)))]
    (< distance (* u 0.3))))

  
(defn set-xy [x y coords u]  ; get x y from real coords 
  (for  [p coords :when (and (nearby1 x (:x p) u) (nearby1 y (:y p) u))]  
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
(defn random-img-name []
  (let [images ["wood.png" "wood2.jpg" "wood3.jpg"  "Vwood.gif" "wood057.gif" "board.png" "kaya.jpg"]
	number (count images)]
    (nth images (rand-int number))))
(def backimg-name (atom (random-img-name)))

(def draw-coords? true)

(def #^JPanel board  (proxy [JPanel] []
  (paintComponent [#^Graphics2D g]
     (proxy-super paintComponent #^Graphics2D g)
     (let [g2d #^Graphics2D g
	   w (.getWidth #^JPanel this)
	   u (/ w 20.0)
	   extent (range u (* u 20) u)
	   coords (for [x extent y extent] {:x x :y y})
	   last-stone (last @whole-lists)
	   ;backimg (ImageIO/read (File. (str  "/images" file-separator @backimg-name)))
	   backimg (ImageIO/read (get-resource (str "images/" @backimg-name)))
	   ;bimg (ImageIO/read (File. (str  "/stones" file-separator "blk.png")))
	   bimg (ImageIO/read (get-resource "stones/blk.png"))
	   ;wimg (ImageIO/read (File. (str "/stones" file-separator "hyuga2.png")))
	   wimg (ImageIO/read (get-resource "stones/hyuga2.png"))]
       (doto g2d
	 (.setRenderingHint  RenderingHints/KEY_ANTIALIASING RenderingHints/VALUE_ANTIALIAS_ON)
	 ;(.draw3DRect  0 0 w w true)
	 ;(.setColor  (Color. 212 167 102))
	 ;(.fill3DRect  0 0 w w true)
	 (.drawImage backimg 0 0 w w nil)
	 (.setColor  Color/BLACK)
	 (.setStroke  (BasicStroke. (float 1)))
	 (.setFont  (Font. "Sans" Font/BOLD 10)))
       (doall
	(map #(.draw g2d (new Line2D$Float u % (* 19 u) %)) extent))
       (doall
	(map #(.draw g2d (new Line2D$Float % u % (* 19 u))) extent))
       (when draw-coords? 
	 (doall
	  (map #(.drawString g2d (str (char (+ 96 (Math/round (float (/ % u)))))) (float (* 0.48 u)) (float %)) extent))
	 (doall 
	   (map #(.drawString #^Graphics2D g2d (str (Math/round (float (/ %  u)))) (float (- % 2.5))(float (* 0.5 u))) extent)))
       (.draw g2d (Ellipse2D$Float. (* u 3.9) (* u 3.9) (* u 0.2) (* u 0.2)))
       (.draw g2d (Ellipse2D$Float. (* u 3.9) (* u 15.9) (* u 0.2) (* u 0.2)))
       (.draw g2d (Ellipse2D$Float. (* u 15.9) (* u 3.9) (* u 0.2) (* u 0.2))) ;;draw 5 points:xing and tianyuan
       (.draw g2d (Ellipse2D$Float. (* u 15.9) (* u 15.9) (* u 0.2) (* u 0.2)))
       (.draw g2d (Ellipse2D$Float. (* u 9.9) (* u 9.9) (* u 0.2) (* u 0.2)))

       (.draw g2d (Ellipse2D$Float. (* u 9.9) (* u 3.9) (* u 0.2) (* u 0.2)))
       (.draw g2d (Ellipse2D$Float. (* u 15.9) (* u 9.9) (* u 0.2) (* u 0.2)))
       (.draw g2d (Ellipse2D$Float. (* u 3.9) (* u 9.9) (* u 0.2) (* u 0.2)))
       (.draw g2d (Ellipse2D$Float. (* u 9.9) (* u 15.9) (* u 0.2) (* u 0.2)))

       (when-let [lists (filter #(= (:liberty %) nil) @whole-lists)]
	 (let [draw-img-fn (fn [stone] (if (odd? (:id stone))
					 (.drawImage g2d bimg (get-x stone u) (get-y stone u) u u this)
					 (.drawImage g2d wimg (get-x stone u) (get-y stone u) u u this)))
	       draw-id-fn  (fn [stone] 
			     (.setColor g2d (if (odd? (:id stone)) Color/white Color/black))
			     (.setFont g2d (Font. "Sans" Font/BOLD (Math/round (/ u 3.0))))
			     (let [x (:x (get-stone-cord stone u)) x1 (float (- x (* u 0.1))) 
				    x2 (float (- x (* u 0.2))) x3 (float (- x (* u 0.35)))
				    xx (cond 
					 (< (:id stone) 10) x1
					 (< (:id stone) 100) x2
					 (< (:id stone) 1000) x3)]
			       (.drawString g2d (str (:id stone)) xx
				 (float (-  (:y (get-stone-cord stone u)) (* u 0.03))))))]
	   (dorun (pmap draw-img-fn lists))
	   (when paint-id? (dorun (map draw-id-fn lists)))))
       			
       (when (and (not (nil? last-stone)) (not paint-id?))
	 (if (odd? (:id last-stone)) (.setColor g2d Color/white) (.setColor g2d Color/black))
	 (.draw g2d (Ellipse2D$Float. (+ (get-x last-stone  u)(/ u 4.2))
		      (+ (get-y last-stone  u)(/ u 4.2)) (/ u 2.0) (/ u 2.0))))))
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
		xy  (set-xy x y coords u)
		XY (if (empty? xy) nil (trans-coord (:x (first xy)) (:y (first xy)) u))]
					    
	    (when (not (nil? XY))
	      (let [x (Math/round (float (:x XY))) y (Math/round (float (:y XY)))]
		(if (stone-in-lists? {:x x :y y}) (println "sorry you canot play there")
		    (when-not (forbidden-point? (inc @id) {:x x :y y}) ;; check probidden point
		      (do 
			(swap! id inc)
			(when play-audio? (.start (Thread. #(play-sound1 "stone.wav"))))
					  
			(go @id x y)
			(let [comm (.getText #^JTextArea  msg-area)
			      len (count comm)]
			  (if (> len 1) (add-to-comments @id comm)))
			(if (even? @id)
			  (.setIcon #^JButton w-b-button (ImageIcon. "images/gogui-black-24x24.png"))
			  (.setIcon #^JButton w-b-button (ImageIcon. "images/gogui-white-24x24.png")))))))))))))
						  ;(.repaint #^JPanel board)
						  

(def lists-watcher (agent 0))
(defn lists-watcher-action [v r]  ;; v is the state of agent,and r is the ref 
  (.repaint #^JPanel board)
  (inc v))
(add-watch whole-lists :key (fn [k r old new] (send lists-watcher lists-watcher-action  new)))

(def capture-watcher (agent 0))
(defn capture-watcher-action [v r]
     (if play-audio? (play-sound1 "stone.wav")
       (inc v)))
(add-watch  w-captured-groups :key1 (fn [k r old new ] (send capture-watcher capture-watcher-action new)))
(add-watch  b-captured-groups :key2 (fn [k r old new ] (send capture-watcher capture-watcher-action new)))

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

(defmacro mice-listen [ target & action]
  `(. ~target addMouseListener (proxy [MouseAdapter] []
				 (mousePressed [e#]
					       ~@action ))))
(defmacro action-listen [ target & action]
  `(. ~target addActionListener (proxy [ActionListener] [] 
				  (actionPerformed [e#]
						   ~@action))))
(mice-listen #^JMenuItem new-menuitem (reset-envs) (reset-snaps) (reset-data) 
  (.setText #^JTextArea msg-area "") (swap! game-comments empty) (.repaint #^JFrame main-window))

(mice-listen #^JMenuItem save-menuitem (let [fc (JFileChooser.)
				 return (.showSaveDialog fc nil)]
			     (if (= return JFileChooser/APPROVE_OPTION)
			       (let [file (.getSelectedFile fc)
				     the-data (assoc @data :qipu ( simply-whole-lists @whole-lists))
				     the-data1 (assoc the-data :comments @game-comments)]
				 (println (.getPath file))
				 (save-to-file the-data1 (str (.toURL file)))))))

(mice-listen #^JMenuItem open-menuitem (let [fc (JFileChooser.)
				 return (.showOpenDialog fc nil)]
			     (if (= return JFileChooser/APPROVE_OPTION)
			       (let [file (.getSelectedFile fc)
				     data1 (load-data-from-file (.getPath file))
				     qipu (:qipu data1) comm (:comments data1)]
				 (reset! data data1)
				 (reset! game-comments comm)
				 (println (.getName file))
					;(compare-and-set! snapshots @snapshots data)
					;(get-snapshot (count data))
				 (load-data1 qipu)
				 ;(println "whole-lists is" @whole-lists)
				 (.repaint #^JFrame main-window)))))

(mice-listen #^JMenuItem exit-menuitem (System/exit 0))

(mice-listen #^JMenuItem paint-menuitem (alter-var-root (var paint-id?) not) (.repaint #^JPanel board))
	     
(mice-listen #^JMenuItem audio-menuitem (alter-var-root (var play-audio?) not) (.repaint #^JPanel board))

(mice-listen #^JMenuItem undo-menuitem (alter-var-root (var undo?) not))

(mice-listen #^JMenuItem coord-menuitem (alter-var-root (var draw-coords?) not)(.repaint #^JPanel board))

						 

(defn menu-init []
  (.add #^JMenu file-menu #^JMenuItem new-menuitem)
  (.addSeparator #^JMenu file-menu)
  (.add #^JMenu file-menu #^JMenuItem open-menuitem)
  (.addSeparator #^JMenu file-menu)
  (.add #^JMenu file-menu #^JMenuItem save-menuitem)
  (.addSeparator #^JMenu file-menu)
  (.add #^JMenu file-menu #^JMenuItem exit-menuitem)
  (.add #^JMenu option-menu #^JMenuItem paint-menuitem)
  (.addSeparator #^JMenu option-menu)
  (.add #^JMenu option-menu #^JMenuItem audio-menuitem)
  (.addSeparator #^JMenu option-menu)
  (.add #^JMenu option-menu #^JMenuItem undo-menuitem)
  (.add #^JMenu option-menu #^JMenuItem coord-menuitem)
  (.add #^JMenu help-menu #^JMenuItem about-menuitem)
  (.addSeparator #^JMenu help-menu)
  (.add #^JMenu help-menu #^JMenuItem manual-menuitem)
  (.add #^JMenuBar menu-bar #^JMenu file-menu)
  (.add #^JMenuBar menu-bar #^JMenu option-menu)
  (.add #^JMenuBar menu-bar #^JMenu help-menu)
  (.setBorder #^JMenuBar menu-bar (BorderFactory/createBevelBorder BevelBorder/LOWERED)))


(defn navigate-button [imagename actioncommand tooltiptext alttext]
  (let [ img-loc (str "images" file-separator imagename ".png")
	button (JButton.)]
    (.setActionCommand button actioncommand)
    (.addActionListener button main-window) ;;wait to add 
    (.setIcon button (ImageIcon. (get-res img-loc)))
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
    (.add jt #^JButton w-b-button)))
(def w-b-button (JButton. (ImageIcon. "images/clojure2.png")))
 
(def toolbar (JToolBar. "oops"))

(defn setup-mode [mode]
  (condp = mode
    "play" (.setVisible #^JToolBar toolbar false)
    "review" (.setVisible #^JToolBar toolbar true)
    "view" (.setVisible #^JToolBar toolbar true)
    "game-over" (.setVisible #^JToolBar toolbar true)))

(declare aux-board)


(defn play-go []
  (.setEnabled #^JToolBar toolbar false)
  ;(.setBorder #^JPanel board (BevelBorder. BevelBorder/RAISED))
  (menu-init)
  (addButtons #^JToolBar toolbar)
  (setup-mode "review")
  (.setBorderPainted #^JToolBar toolbar true)
  (.setPreferredSize #^JPanel aux-board paux-size)
  ;(.setPreferredSize board (Dimension. 800 800))
  ;(.setPreferredSize toolbar (Dimension. 600 50))
  (doto #^JFrame main-window
	  (.setJMenuBar menu-bar)
    
	  (.add #^JPanel  board BorderLayout/CENTER)
	  (.add #^JToolBar toolbar BorderLayout/NORTH)
	  (.add #^JPanel aux-board BorderLayout/EAST)
    	  (.pack) 
	  (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
	  (.setIconImage (.createImage (Toolkit/getDefaultToolkit) "images/clojure-icon.gif"))
	  (.setVisible true)))

		      
