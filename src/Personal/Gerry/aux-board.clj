(in-ns 'Personal.Gerry.GoGame)
(import '(javax.swing JLabel JTextField  JTextArea JList JScrollPane ListSelectionModel))
(import '(java.util Vector))
(import '(java.awt GridLayout))
(import '(javax.swing.border TitledBorder EtchedBorder LineBorder))


(def aux-board (JPanel.))
(def aux-board1 (JPanel.))
(def aux-board2 (JPanel.))
(def aux-board3 (JPanel.))

(def white-player(JLabel. ((get-players-name @data) :w) (ImageIcon. "w24.png") JLabel/LEFT))
(def black-player (JLabel. ((get-players-name @data) :b) (ImageIcon. "b24.png") JLabel/RIGHT))
(def vs-player (JLabel. (get-result @data) (ImageIcon. "8.gif") JLabel/CENTER))
;(.setBorder white-player (BorderFactory/createLineBorder Color/lightGray 2))
;(.setBorder black-player (BorderFactory/createLineBorder Color/black 2))

(.setHorizontalTextPosition #^JLabel vs-player JLabel/CENTER)

(.addMouseListener #^JLabel white-player (proxy [MouseAdapter] []
  (mousePressed [#^MouseEvent e]
    (when (.isControlDown e)
      (let [content (JOptionPane/showInputDialog nil "The name of white:" "Information of this game" JOptionPane/PLAIN_MESSAGE)]
	(set-white-player-name content)
	;(.setText white-player ((get-players-name @data):w))
	(.validate #^JLabel white-player))))))

(.addMouseListener #^JLabel black-player (proxy [MouseAdapter] []
  (mousePressed [#^MouseEvent e]
    (when (.isControlDown e)
      (let [content (JOptionPane/showInputDialog nil "The name of black:" "Information of this game" JOptionPane/PLAIN_MESSAGE)]
				     
	(set-black-player-name content)
	;(.setText black-player ((get-players-name @data) :b))
	)))))
(.addMouseListener #^JLabel vs-player (proxy [MouseAdapter] []
  (mousePressed [#^MouseEvent e]
    (when (.isControlDown e)
      (let [content (JOptionPane/showInputDialog nil "The result of the game:" "Information of this game" JOptionPane/PLAIN_MESSAGE)]
				     
	(set-result content)
	;(.setText vs-player (get-result @data))
	)))))
(def data-watcher (agent 0))
(defn data-watcher-action [v r]
 (let [r (get-result @data)]
   (.setText #^JLabel black-player ((get-players-name @data) :b))
   (.setText #^JLabel white-player ((get-players-name @data) :w))
   (.setText #^JLabel vs-player r)
   (when r (.setIcon #^JLabel vs-player nil)
	(when (not= (.indexOf #^String (get-result @data) "white") -1) 
	  (.setIcon #^JLabel white-player (ImageIcon. "smile.gif")))
	(when (not= (.indexOf #^String (get-result @data) "black") -1) 
	  (.setIcon #^JLabel black-player (ImageIcon. "smile.gif"))))
   (when-not r (.setIcon #^JLabel vs-player (ImageIcon. "8.gif"))
	    (.setIcon #^JLabel white-player (ImageIcon. "w24.png"))
	    (.setIcon #^JLabel black-player (ImageIcon. "b24.png")))
   (inc v)))

;(defn set-comment []
 ; (when-let [ comment (@game-comments @id)]
 ;   (if (nil? comm) (.setText #^JTextArea msg-area "")
 ;     (.setText #^JTextArea msg-area comm))))

(add-watcher data :sendoff data-watcher data-watcher-action)

(def msg-area  (JTextArea. "" 8 15))

(.addMouseListener #^JTextArea msg-area (proxy [MouseAdapter] []
  (mousePressed [#^MouseEvent e]
    (when (.isControlDown e)
      (.setText #^JTextArea msg-area "") 
      (.setEditable #^JTextArea msg-area true)
      (.setBackground #^JTextArea msg-area Color/gray)))))

(.addMouseListener #^JPanel board (proxy [MouseAdapter] []
  (mousePressed [#^MouseEvent e]
    (when (.isControlDown e)
      (def backimg-name (random-img-name))
      (.repaint board)))))


 
(.addMouseListener #^JTextArea msg-area (proxy [MouseAdapter] []
  (mousePressed [#^MouseEvent e]
    (when (.isShiftDown e) 
      (let [text (.getText #^JTextArea msg-area)
	    len (count text)]
	(.setBackground #^JTextArea msg-area Color/white)
	(swap! game-comments assoc @id text))))))

(def dialog-field  (JTextField."Hello World" 30))
(.setMargin #^JTextField dialog-field (Insets. 2 2 2 2))
(.setBorder #^JTextField dialog-field (BorderFactory/createLineBorder Color/blue 1 ))

(action-listen #^JTextField dialog-field (let [content (.getText #^JTextField dialog-field)]
			   (.setText #^JTextField dialog-field "")
			   (.append #^JTextArea msg-area (str  content "\n"))
			   (.setCaretPosition #^JTextArea msg-area (.. #^JTextArea msg-area getDocument getLength))))



(def lists-data (Vector.))
;(doto #^Vector lists-data
;  (.add "Gerry")
;  (.add "Rose")
;  (.add "John")
;  (.add "Rich"))

(def lists (JList. #^Vector lists-data))


(doto #^JList lists
  (.setVisibleRowCount 7)
  (.setBorder (TitledBorder.(BorderFactory/createLineBorder Color/blue 1 ) "Spectators"))
  (.setSelectionMode ListSelectionModel/SINGLE_INTERVAL_SELECTION))
  ;(.setLayoutOrientation JList/HORIZONTAL_WRAP))

(doto #^JPanel aux-board2
  (.setBorder (TitledBorder. (BorderFactory/createLineBorder Color/blue 1 ) "Match"))
  (.setLayout (GridLayout. 3 1 ))
  (.add #^JLabel white-player )
  (.add #^JLabel vs-player)
  (.add #^JLabel black-player))

(doto #^JPanel aux-board1
  (.setLayout (BorderLayout. 1 1))
  (.add #^JScrollPane (JScrollPane. lists) BorderLayout/NORTH)
  (.add #^JPanel aux-board2 BorderLayout/SOUTH))

(doto #^JTextArea msg-area
  (.setWrapStyleWord  true)
  (.setBorder (TitledBorder. (BorderFactory/createLineBorder Color/blue 1 ) "message board"))
  (.setEditable false)
  (.setFont (Font. "Times-Roman" Font/PLAIN 12)))

(doto #^JPanel aux-board3
  (.setLayout (BorderLayout.))
  (.add (new JScrollPane msg-area) BorderLayout/CENTER)
  (.add #^JTextField dialog-field BorderLayout/SOUTH))

(doto #^JPanel aux-board
  (.setLayout (BorderLayout. 1 1))
  (.add #^JPanel aux-board1 BorderLayout/NORTH)
  (.add #^JPanel aux-board3 BorderLayout/CENTER))
  ;(.setPreferredSize (Dimension. 200 800))
  ;(.pack)
  ;(.setSize 200 800)
  ;(.setVisible true))

(defn aux-test [ ]
  (let [#^JFrame jf (JFrame.)]
    (doto jf
      (.add #^JPanel aux-board BorderLayout/CENTER)
      (.setPreferredSize (Dimension. 200 (* 0.75 (:h screen-size))))
      (.pack)
      (.setVisible true))))
