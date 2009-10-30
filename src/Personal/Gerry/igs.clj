(import '(java.io PrintWriter OutputStream InputStream BufferedOutputStream)
  '(java.net Socket))
(use 'clojure.contrib.duck-streams)

(def socket (Socket. "igs.joyjoy.net" 7777))
(def ins (reader (.getInputStream socket)))
(def ous (writer (.getOutputStream socket)))

