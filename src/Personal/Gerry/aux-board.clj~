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

(.setHorizontalTextPosition vs-player JLabel/CENTER)

(.addMouseListener white-player (proxy [MouseAdapter] []
  (mousePressed [#^MouseEvent e]
    (when (.isControlDown e)
      (let [content (JOptionPane/showInputDialog nil "The name of white:" "Information of this game" JOptionPane/PLAIN_MESSAGE)]
	(set-white-player-name content)
	;(.setText white-player ((get-players-name @data):w))
	(.validate white-player))))))

(.addMouseListener black-player (proxy [MouseAdapter] []
  (mousePressed [#^MouseEvent e]
    (when (.isControlDown e)
      (let [content (JOptionPane/showInputDialog nil "The name of black:" "Information of this game" JOptionPane/PLAIN_MESSAGE)]
				     
	(set-black-player-name content)
	;(.setText black-player ((get-players-name @data) :b))
	)))))
(.addMouseListener vs-player (proxy [MouseAdapter] []
  (mousePressed [#^MouseEvent e]
    (when (.isControlDown e)
      (let [content (JOptionPane/showInputDialog nil "The result of the game:" "Information of this game" JOptionPane/PLAIN_MESSAGE)]
				     
	(set-result content)
	;(.setText vs-player (get-result @data))
	)))))
(def data-watcher (agent 0))
(defn data-watcher-action [v r]
 (let [r (get-result @data)]
  (.setText black-player ((get-players-name @data) :b))
  (.setText white-player ((get-players-name @data) :w))
  (.setText vs-player r)
  (when r (.setIcon vs-player nil)
	(when (not= (.indexOf (get-result @data) "white") -1) 
	  (.setIcon white-player (ImageIcon. "smile.gif")))
	(when (not= (.indexOf (get-result @data) "black") -1) 
	  (.setIcon black-player (ImageIcon. "smile.gif"))))
  (when-not r (.setIcon vs-player (ImageIcon. "8.gif"))
	    (.setIcon white-player (ImageIcon. "w24.png"))
	    (.setIcon black-player (ImageIcon. "b24.png")))
  (inc v)))

(add-watcher data :sendoff data-watcher data-watcher-action)

(def msg-area  (JTextArea. "this is a msg area" 8 15))

(def dialog-field  (JTextField."Hello World" 30))
(action-listen dialog-field (let [content (.getText dialog-field)]
			   (.setText dialog-field "")
			   (.append msg-area (str  content "\n"))
			   (.setCaretPosition msg-area (.. msg-area getDocument getLength))))



(def lists-data (Vector.))
(doto lists-data
  (.add "Gerry")
  (.add "Rose")
  (.add "John")
  (.add "Rich"))

(def lists (JList. lists-data))


(doto lists
  (.setVisibleRowCount 7)
  (.setBorder (TitledBorder. "Spectators"))
  (.setSelectionMode ListSelectionModel/SINGLE_INTERVAL_SELECTION))
  ;(.setLayoutOrientation JList/HORIZONTAL_WRAP))

(doto aux-board2
  (.setBorder (TitledBorder. (BorderFactory/createRaisedBevelBorder) "Match"))
  (.setLayout (GridLayout. 3 1 ))
  (.add white-player )
  (.add vs-player)
  (.add black-player))

(doto aux-board1
  (.setLayout (BorderLayout. 1 1))
  (.add (JScrollPane. lists) BorderLayout/NORTH)
  (.add aux-board2 BorderLayout/SOUTH))

(doto msg-area
  (.setWrapStyleWord  true)
  (.setBorder (TitledBorder. (BorderFactory/createLoweredBevelBorder) "message board"))
  (.setEditable false)
  (.setFont (Font. "Times-Roman" Font/PLAIN 12)))

(doto aux-board3
  (.setLayout (BorderLayout.))
  (.add (new JScrollPane msg-area) BorderLayout/CENTER)
  (.add dialog-field BorderLayout/SOUTH))

(doto aux-board
  (.setLayout (BorderLayout. 1 1))
  (.add aux-board1 BorderLayout/NORTH)
  (.add aux-board3 BorderLayout/CENTER))
  ;(.setPreferredSize (Dimension. 200 800))
  ;(.pack)
  ;(.setSize 200 800)
  ;(.setVisible true))

(defn aux-test [ ]
  (let [jf (JFrame.)]
    (doto jf
      (.add aux-board BorderLayout/CENTER)
      (.setPreferredSize (Dimension. 200 (* 0.75 (:h screen-size))))
      (.pack)
      (.setVisible true))))
