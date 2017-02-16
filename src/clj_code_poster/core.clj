(ns clj-code-poster.core
  (:require clojure.string
            [mikera.image.core :as image]
            [mikera.image.colours :as colours]
            [taoensso.timbre :as timbre]
            [dali.io :as io])
  (:gen-class))

(def image-file "clj.png")

(def code-file "resources/core.clj")

(def img
  (image/load-image-resource image-file))
(def width (.getWidth img))
(def height (.getHeight img))

(def ratio 0.6)

(defn components-hex
  "Get hex colour values from image pixels"
  ([^long rgb]
   (apply str "#"
          (map #(format "%02x" %)
               [(colours/extract-red rgb)
                (colours/extract-green rgb)
                (colours/extract-blue rgb)]))))

(def img-hex
  (->>
   (map components-hex (image/get-pixels img))
   (partition width)))

(defn join-code [code]
  (-> (clojure.string/trim code)
      (clojure.string/replace #"\s*\n+\s*" " ")
      (clojure.string/replace #"\s" " ")))

(def code
  (apply str (repeat 100 (-> (slurp code-file)
                            join-code
                            ;; not sure if char-array is what we need here, moving on.
                            ;;char-array
                            ;;      cycle
                            ))))

(defn word [x y]
  (get code (+ (* width y) x)))

(defrecord TextField [text x y fill])

(defn TextField->svg [{:keys [text x y fill]}]
  [:text {:x x :y y :fill fill} text])

(defn produce-row [row-pixels y]
  (reduce
   (fn [coll [p idx]]
     (let [c (word idx y)]
       (if-let [{:keys [fill] :as tf} (peek coll)]
         (if (= fill p)
           (conj (pop coll) (update tf :text #(str % c)))
           (conj coll (->TextField c (* ratio idx) y p)))
         (conj coll (->TextField c (* ratio idx) y p)))))
   [] row-pixels))

(def document
  [:dali/page {:viewbox "0 0 300 300"
               :width 2000 :height 2000
               "xml:space" "preserve"
               :style "font-family: 'Source Code Pro'; font-size: 1; font-weight: 900;"}
   (->> (mapcat
         (fn [[x y]] (produce-row (map vector x (range)) y))
         (map vector img-hex (range)))
        (map TextField->svg))])

;; (clojure.pprint/pprint
;;  (mapcat
;;   (fn [[x y]] (produce-row (map vector x (range)) y))
;;   (map vector (take 2 img-hex) (range))))

;; (clojure.pprint/pprint
;;  (->> (produce-row (map vector (first img-hex) (range)) 35)
;;       (map TextField->svg)
;;       vec))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
