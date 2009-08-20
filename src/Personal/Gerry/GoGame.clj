 ;Go game in clojure
 ;围棋游戏
 
(ns Personal.Gerry.GoGame 
  (:use clojure.contrib.seq-utils)
  (:use clojure.contrib.duck-stream)
  (:require [clojure.zip :as zip])
  (:gen-class))
(def *board-size* 19)
 
(def whole-lists (ref [])) ; use as saving all played stones stones ,includes captured stones 按序储存所有已下棋子包括死子
(defstruct stone :id :loc :liberty)  ;loc is {:x,:y} liberty default to nil, if 0,then it's captured.死子气为0


(def black-id-groups (atom #{})) ;black groups grouped by id,dead groups will be removed. #{ #{1 2 3} #{ 19 28}}

(def white-id-groups (atom #{)))

;(def w-captured-groups [])

;(def b-captured-groups []) ; dead-groups move to here as backup data

(defn getloc-from-id [id]
     (let [s (nth @whole-lists (dec id))]
       (:loc s)))


(defn getid-from-loc [loc] ; one location maybe have been played two and more stones,dead stone don't count
  (if (nil? loc) nil
      (first (for [s @whole-lists :when (and (= (:loc s) loc) (= (:liberty s) nil))] (:id s)))))

(defn get-neighbors [stone]
     (let [x (:x (:loc stone)) y (:y (:loc stone))]
       {:left (if (= x 0) nil {:x (dec x) :y y}) :right (if (= x 18) nil {:x (inc x) :y y})
	:down (if (= y 0) nil {:x x :y (dec y)}) :up (if (= y 18) nil { :x x :y (inc y)})}))

(defn stone-in-lists? [loc]
  (some #(and (= (:loc %) loc) (= nil (:liberty %))) @whole-lists))  ;;not dead

(defn get-neighbors-id [stone]
  (let [n (get-neighbors stone)
	ids [(getid-from-loc (:left n)) (getid-from-loc (:right n)) (getid-from-loc (:down n)) (getid-from-loc (:up n))]]
    (filter #(not (nil? %)) ids)))
						



(defn liberty-of-stone [stone]
  (let [n (get-neighbors stone)]
    (+ (if (stone-in-lists? {:x (:left n) :y (:left n)}) 0 1 )
       (if (stone-in-lists? {:x (:right n) :y (:right n)}) 0 1 )
       (if (stone-in-lists? {:x (:up n) :y (:up n)}} 0 1 )
       (if (stone-in-lists? {:x (:down n):y (:down n)}) 0 1 ))))

(defn liberty-of-group [group]                    ;;only dead group call it,result is 0
  (let [liberty-list (map liberty-of-stone group)]
    (reduce + liberty-list)))
	 
;(defn append-to-blocks [x y] ;lookup x in blocks,if found then add new move to inner block
;   (loop [loc dz]            ;dz is blocks
;     (if (zip/end? loc)
;       (zip/root loc)
;       (recur
;	(zip/next
;	 (if (and (vector? (zip/node loc)) (includes? (zip/node loc) x))
;	   (let [new-coll (conj (zip/node loc) y)]
;	   (zip/replace loc new-coll))
;	   loc))))))

(defn stone-to-idgroup [id-of-neighbors id]  ;put playing stone to groups
  (let [ groups (if (odd? id) black-id-groups white-id-groups)]
   (into []
	 (for [s groups] 
	   (if (includes? s id-of-neighbors)(conj s id) s)))))

(defn merge-inner-idgroups [id groups] ;id  is new playing stone id,maybe cause to merge groups 
  (let [with-out-id  (filter #(not (includes? % id)) groups)
        with-id  (filter #(includes? % id) groups)]
       (concat  (vec with-out-x) (vec (set (flatten with-x))))))

;; helper functions for serialization
;; struct map can't be serialize?
(defn save-to-file [data filename]
  (spit filename
	(with-out-str (pr data))))
(defn load-data-from-file [filename]
  (with-in-str (slurp filename)
	       (read)))
;;


(defn play [id x y]    ;; not finish
  (let [new-stone (struct stone id x y )]
    (def whole-lists (conj whole-lists new-stone))
    (if (odd? id) (add-stone-to black-id-lists)
	(add-stone-to white-id-lists))))

(defn -main []
  (println "Gogame Test")
  (let [one (struct stone 1 3 4 4)]
    (println (qi-of-stone one))))


;围棋术语
(comment 
aji 味，余味 (*)

aji-keshi 解消余味 (*)

amashi (*)这词常用，但中文围棋术语中没有正式的翻译。原来专指江户

时期执白者的一种战略，先取实地然后再侵消黑势，治理孤棋。江户时期

的名家八世安井知得仙知，现代的坂田荣男、赵治勋都是“amashi”的

高手。从这一意义上来说，这一词的意思就是现在所谓的“先捞后洗”。

atari 打吃 (*)

-bango 番棋

byo-yomi 读秒 (*)

carpenter's square 小曲尺 (*)

chuban (middle game) 中盘 (*)

dame 单官，气 (*)

dan 段 (*)

empty triangle 空三角 (*)

eye 眼 (*)

furikawari (exchange, trade, swap) 转换、交换 (*)

fuseki 布局 (*)

geta 枷 (*)

gaisei (outside influence) 外势 (*)

gankei (eye shape) 眼形 (*)

go 碁(在日本专指围棋)，围棋 (*)

goban 围棋盘 (*)

godokoro 碁所(棋所)

Gosei 碁圣(小棋圣)

gote 后手 (*)

gote no sente 后中先

gukei (bad shape) 愚形 (*)

gyaku sente (reverse sente) 逆先手 (*)

gyaku yose (reverse yose) 逆官子 (*)

hamete (trick move) 嵌手 (*)

hanami-ko 看花劫(即无忧劫)

hana-zuke (nose attachment) 鼻顶 (*)

hane 扳 (*)

hane-kaeshi 反扳，连扳

hangan (half an eye) 半眼

han-ko 半劫

han-moku (half a point) 半目 (*)

hasami (pincer attack) 夹攻(如定式中的一间夹、二间夹等等) (*)

hasami-kaeshi 反夹(对应于上述夹攻之反夹)

haya-go 快棋 (*)

hazama 穿象眼

hazama tobi 象飞

hiraki (extension) 开拆(如拆二、拆三等等) (*)

hon-ko (real ko) 本劫

Honinbo 本因坊(*)

honte 本手 (*)

hoshi (star point) 星 (*)

igo 围棋(正式的名称，与上面的“碁”相对照) (*)

ikken 一间(常见的有以下四种)：

ikken-basami (one-space pincer) 一间夹 (*)

ikken-biraki (one-space extension) 一间拆 (*)

ikken-jimari (one-space enclosure) 单关守角 (*)

ikken-tobi (one-space jump) 一间跳(单关跳) (*)

insei 院生 (*)

ishi-no-shita 倒脱靴 (*)

ji (territory) 地 (*)

jigo (a drawn game) 和棋，持棋 (*)

joseki 定式 (*)

joban (opening game) 序盘 (*)

josen 定先

jozu 上手，又常指江户时期的七段 (*)

jubango 十番棋 (*)

Judan Sen 十段战

jun-Meijin 准名人

kabe (wall) 壁 (*)

kakari (approach move) 挂角 (*)

kake 飞压

kake-me (false eye) 假眼 (*)

keshi 消，侵消 (*)

ki 棋

kiai 气合

kishi 棋士 (*)

ki-in 棋院 (*)

kido 棋道 (*)

kifu 棋谱 (*)

kikashi (a forcing move requring an answer) 先手利 (*)

kiri (cut) 切断 (*)

kiri-chigai (crosscut) 扭断 (*)

Kisei 棋圣 (*)

ko 劫 (*)

kogeima 小飞，小桂马(台湾版的书常用) (*)

komi 贴目 (*)

komoku 小目 (*)

kosumi (diagonal move) 小尖 (*)

kosumi-tsuke 尖顶

kozai (ko threat) 劫材 (*)

kyu 级 (*)

kyusho (vital point) 急所 (*)

liberty 气 (*)

magari (turning move) 曲 (*)

magari-tsuke 拐头

mane-go (mimic go) 模仿棋 (*)

mannen-ko (thousand-year ko) 万年劫 (*)

me (eye) 眼(*)

me-gatachi (eye shape) 眼形

mekura-go (blindfold go) 盲棋

Meijin 名人 (*)

menjo 免状，棋力证书 (*)

miai 见合 (*)

mokuhazushi (5-3 point) 目外(*)

moyo 模样 (*)

myoshu (A brilliant move) 妙手 (*)

nadare (avalanche joseki) 雪崩 (*)

nidan-bane (two-step hane) 连扳 (*)

nigiri 猜先 (*)

Nihon Ki-in日本棋院

niken 二间(用于下列术语)

niken-basami (two-space pincer) 二间夹(*)

niken-biraki (two-space extension) 二间拆(*)

niken-jimari (two-space corner enclosure) 二间守角

niken-taka-basami (high two-space pincer) 二间高夹(*)

niken-taka-gakari (two-space high approach) 二间高挂(*)

niken-tobi (two-space jump) 二间跳(*)

niren-sei 二连星(*)

nobi (solid extension) 长(*)

nozoki (peep) 刺(*)

nurui (lukewarm, slack) 缓(*)

oba (large fuseki point or extension) 大场，常用 “large point”

ogeima (large-knight approach move) 大飞挂，大桂马(台湾用法)

ogeima-jimari (large-knight's corner enclosure) 大飞守角 (*)

oki-go (Handicap Go) 让子棋(*)

omoi (heavy) 重(*)

onadare (large avalanche joseki) 大雪崩(*)

osae (block) 挡 (*)

oshi (pushing move) 压 (*)

o-shiro go (castle game) 御城棋

Paduk (韩语)围棋

poka (blunder) (*) 恶手，错着(mistake)，漏着(oversight)，以上几个词义相近，

但有程度上的差别。

ponnuki 开花(*)

Ranka 烂柯(围棋的别名)

rengo (team go) 连棋

ryo-atari (double atari) 双打(*)

ryo-ko (double ko) 双劫(同时打两个劫)(*)

ryo-gote (double gote) 双方后手(*)

ryo-sente (double sente) 双方先手(*)

sabaki (making light, flexible shape in order to save a group) 腾挪(*)

sagari (a descent) 立(*)

sandan-bane (triple hane) 三连扳

san-ko (triple ko) 三劫 (*)

sangen 三间(用于下列术语)

sangen-basami (two-space pincer) 三间夹

sangen-biraki (two-space extension) 三间拆

sangen-tobi (two-space jump) 三间跳

san-san (the 3-3 point) 三三

sanren-sei 三连星(*)

saru-suberi (the monkey jump) 大飞伸腿(收官)

seki 双活(*)

sekito shibori (the stone-tower squeeze) 大头鬼(“石塔”)

seme (attacking) 攻击(*)

semeai (a capture race) 对杀(一般指杀气)(*)

sen-ai-sen 先相先，有时简写为B-W-B或B-B-W(江户时期的规则)

senban 先番

san-ni 先二

sente 先手(*)

shibori (squeeze) 滚打包收(*)

shicho (ladder) 征(*)

shicho-atari (a ladder-breaking move or a ladder-making move) 引征 (*)

shido-go (a teaching game) 指导棋 (*)

shitatsu (life and death) 死活 (*)

shi-ko (a quadrule ko) 四劫

shimari (a corner enclosure) 守角(见一间、二间守角等)

shimbun go (Newspaper go) 新闻棋战

Shin Fuseki (New Fuseki) 新布局(一般指吴清源与木谷实的新布局) (*)

shini-ishi (dead stone) 死棋

shinogi (saving an endangered group or stones) 治孤(*)

shitate (the weaker or lower-ranked player) 下手

shodan (1-dan) 初段(*)

shuban (the closing stages of the game) 终盘(收官阶段)，一般用“end game”(*)

shudan 手谈(围棋的别名)

Shusaku-ryu (the Shusaku opening) 秀策流

sogo 争棋(*)

son 损(不常用)

son-ko 损劫

soto dame (an outside liberty) 外气(*)

sugata (shape, formation) 形(*)

suji 筋

sumi (the corner) 角 (*)

suteru (to sacrifice, discard) 弃子(动词)(*)

sute-ishi (sacrifice stones) 弃子

tagai-sen 互先

taisha 大斜(*)

takamoku (the 5-4 point) 高目(*)

takefu (a bamboo joint) (连接时用的)双，竹节。(*)

te (a move) 手(*)

tejun (order of moves) 次序，手顺

tenuki 脱先(*)

tesuji 手筋(*)

tetchu (iron pillar) 铁柱守角(*)

tewari 手割(*)

tengen 天元(*)

tobi (a jump, usually toward the center of the board) 跳(*)

tsugi (a connection) 连，接，粘(*)

tsuke (an attachment; a contact play) 靠，碰(*)

tsuki-dashi (pushing in between two enemy stones) 冲

tsume (a cheking extension) 逼(*)

tsume-go (a life and death problem) 诘棋，死活棋(题目)(*)

uchikake (adjourning a game) 打挂

uchikaki (a throw-in move which reduces the opponent's liberties or eyes)

扑入，英文一般用“throw-in”

uchikomi (an invasion) 打入(*)，另一意思是把对手降级(江户时期)。

uchisugi (overplay) 过分之意，中文似乎没有对应的术语。

uki-ishi (floating stones. stones without a base) 浮棋(*)

usui (thin) 薄(*)

usui katachi (a thin or weak shape that can easily be attacked) 薄形

utte-gaeshi (a snapback) 倒扑(*)

warikomi (a wedge between two stones) 挖(*)

wariuchi (an invasion, a splitting move) 割打，分投(*)

watari 渡(*)

yomi (reading, analyzing a position) 读棋(*)

yose (the endgame) 收官(*)

yurumi shicho (a loose ladder) 宽气征(*)

zoku suji (bad style, a crude move) 俗筋(*)))


;;The end