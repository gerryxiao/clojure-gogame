(in-ns 'Personal.Gerry.GoGame)
(import '(javax.swing JLabel JTextField  JTextArea JList JScrollPane ListSelectionModel))
(import '(java.util Vector))
(import '(java.awt GridLayout))
(import '(javax.swing.border TitledBorder EtchedBorder LineBorder))


(def aux-board (JPanel.))
(def aux-board1 (JPanel.))
(def aux-board2 (JPanel.))
(def aux-board3 (JPanel.))

(def white-player(JLabel. "gerry" (ImageIcon. "white-small.png") JLabel/CENTER))
(def black-player (JLabel. "rose" (ImageIcon. "black-small.png") JLabel/CENTER))
(def vs-player (JLabel. "<=>" JLabel/CENTER))
(.setBorder white-player (BorderFactory/createLineBorder Color/lightGray 2))
(.setBorder black-player (BorderFactory/createLineBorder Color/black 2))

(.setHorizontalTextPosition vs-player JLabel/CENTER)

(def dialog-field  (JTextField."Hello World" 30))

(def msg-area  (JTextArea. "this is a msg area" 8 30))

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
  (.setBorder (TitledBorder. "Match"))
  (.setLayout (GridLayout. 1 2 ))
  (.add white-player )
  ;(.add vs-player)
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
