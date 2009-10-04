(in-ns 'Personal.Gerry.GoGame)
(import '(javax.swing JLabel JTextField  JTextArea JList JScrollPane ListSelectionModel))
(import '(java.util Vector))
(import '(java.awt GridLayout))
(import '(javax.swing.border TitledBorder))


(def aux-board (JPanel.))
(def aux-board1 (JPanel.))
(def aux-board2 (JPanel.))
(def aux-board3 (JPanel.))

(def white-player(JLabel. "name1" (ImageIcon. "pic1") JLabel/RIGHT))
(def black-player (JLabel. "name2" (ImageIcon. "pic2") JLabel/RIGHT))
(def vs-player (JLabel. "VS" JLabel/CENTER))

(.setHorizontalTextPosition vs-player JLabel/CENTER)

(def dialog-field  (JTextField."Hello World" 12))

(def msg-area  (JTextArea. "this is a msg area" 5 20))

(def lists-data (Vector.))
(doto lists-data
  (.add "Gerry")
  (.add "Rose")
  (.add "John")
  (.add "Rich"))

(def lists (JList. lists-data))


(doto lists
  (.setVisibleRowCount 7)
  (.setBorder (TitledBorder. "viewers"))
  (.setSelectionMode ListSelectionModel/SINGLE_INTERVAL_SELECTION))
  ;(.setLayoutOrientation JList/HORIZONTAL_WRAP))

(def jsp-for-lists (JScrollPane. lists))
;;(.setPreferredSize jsp-for-lists (Dimension. 80 60))

(doto aux-board2
  (.setBorder (TitledBorder. "players"))
  (.setLayout (GridLayout. 1 3))
  (.add white-player )
  (.add vs-player)
  (.add black-player))

(doto aux-board1
  (.setLayout (BorderLayout.))
  (.add lists BorderLayout/NORTH)
  (.add aux-board2 BorderLayout/SOUTH))

(doto msg-area
  (.setWrapStyleWord  true)
  (.setEditable false)
  (.setFont (Font. "Times-Roman" Font/PLAIN 12)))

(doto aux-board3
  (.setLayout (BorderLayout.))
  (.add (new JScrollPane msg-area) BorderLayout/CENTER)
  (.add dialog-field BorderLayout/SOUTH))

(doto aux-board
  (.setLayout (BorderLayout.))
  (.add aux-board1 BorderLayout/NORTH)
  (.add aux-board3 BorderLayout/SOUTH))
  ;(.pack)
  ;(.setSize 200 800)
  ;(.setVisible true))

(defn aux-test [ ]
  (let [jf (JFrame.)]
    (doto jf
      (.add aux-board BorderLayout/CENTER)
      ;(.setSize 200 800)
      (.pack)
      (.setVisible true))))