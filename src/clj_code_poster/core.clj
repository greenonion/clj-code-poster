(ns clj-code-poster.core
  (:require [clojure.string :as string]
            [clojure.tools.cli :as cli]
            [mikera.image.core :as image]
            [mikera.image.colours :as colours]
            [taoensso.timbre :as timbre]
            [dali.io :as io])
  (:gen-class))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

;; Image functions

(defn- components-hex
  "Get hex colour values from image pixels."
  [^long rgb]
  (colours/with-components [[r g b] rgb]
    (->> (map #(format "%02x" %) [r g b])
         (apply str "#"))))

(defn- img->pixel-rows
  "Convert a BufferedImage to a 2D pixel array of rows."
  [^java.awt.image.BufferedImage image]
  (let [width (.getWidth image)]
    (partition width (image/get-pixels image))))

;; Code functions

(def ^:private font-ratio
  "Ratio of the fonts we used, needed for various transformations.
  TODO: This should be a map of font-name -> ratio."
  0.6)

(defn- join-code
  "Cleanup source code by removing spaces, newlines etc."
  [code]
  (-> (string/trim code)
      (string/replace #"\s*\n+\s*" " ")
      (string/replace #"\s" " ")))

;; Data modeling

(defrecord TextField [text x y fill])

(defn TextField->svg [{:keys [text x y fill]}]
  [:text {:x (* font-ratio x) :y y :fill (components-hex fill)} text])

;; Main algorithm

(defn- add-character-to-row
  "Adds a character `c` to a `row` (vector) of TextFields. Each TextField has
  a colour fill, a chunk of text and `x` and `y` coordinates. Each character
  has the `colour` of its matching pixel (based on location). The possible cases
  are:
  1. The `row` is empty: we create a new TextField containing the character and
     filled with the corresponding `colour`, placed on the pixel's `x` and `y`
     coordinates..
  2. The row is not empty and the last TextField is of the same fill as the
     character's `colour`: we simply append the character to the TextField.
  3. The row is not empty but the last TextField is of different fill than the
     character's `colour`: we create a new TextField, same as (1)."
  [c row colour x y]
  (if-let [{:keys [fill] :as tf} (peek row)]
    (if (= fill colour)
      (conj (pop row) (update tf :text #(str % c)))
      (conj row (->TextField c x y colour)))
    (conj row (->TextField c x y colour))))

(defn- produce-row
  "Accepts a vector of pixels, a vector of text and a row index. It creates
  a vector of [pixel, character, index] tuples and reduces them to a row of
  TextFields."
  [pixel-row code-row y]
  (reduce
   (fn [coll [p c x]]
     (add-character-to-row c coll p x y))
   [] (map vector pixel-row code-row (range))))

(defn- create-text-fields
  "Accepts a BufferedImage and a chunk of text. It splits the image into rows of
  pixels, creates a lazy sequence of text chunks of length same as the image
  width, and combines them to create a sequence of TextFields."
  [^java.awt.image.BufferedImage image code]
  (let [width (.getWidth image)
        pixel-rows (img->pixel-rows image)
        code-rows (partition width (cycle code))]
    (mapcat produce-row pixel-rows code-rows (range))))

(defn- create-document [image code]
  [:dali/page
   {:viewbox "0 0 300 300"
    :width 2000 :height 2000
    "xml:space" "preserve"
    :style "font-family: 'Source Code Pro'; font-size: 1; font-weight: 900;"}
   (map TextField->svg (create-text-fields image code))])

(defn produce-code-poster [image-file code-file svg-file]
  ;; TODO: Apply needed image scaling
  (let [image (image/load-image image-file)
        code (-> (slurp code-file) join-code)
        document (create-document image code)]
    (io/render-svg document svg-file)))

(def cli-options
  [["-i" "--image IMAGE" "Image file"]
   ["-c" "--code CODE" "Code file"]
   ["-o" "--out OUT" "SVG output file"]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["Create beautiful code posters."
        ""
        "Usage: clj-code-poster [options] action"
        ""
        "Options:"
        options-summary
        ""
        "Please refer to the github page for more information."]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn -main
  "Create beautiful code posters!"
  [& args]
  (let [{:keys [options arguments errors summary]}
        (cli/parse-opts args cli-options)
        {:keys [image code out]} options]
    (cond
      (:help options) (exit 0 (usage summary))
      (not (and image code out)) (exit 1 (usage summary))
      errors (exit 1 (error-msg errors)))
    (timbre/info "Creating poster, please be patient...")
    (produce-code-poster image code out)))
