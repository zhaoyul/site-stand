(ns sit-stand.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.pprint :as pprint])
  (:gen-class))

(def cli-options
  [["-i" "--input input.jpg" "image file"
    :default "input.jpg"
    :validate [#(.exists (clojure.java.io/as-file %)) "文件不存在"]]
   ["-h" "--help"]])

(def url "https://api-cn.faceplusplus.com/humanbodypp/beta/detect")

(defn multi-part [file-name]
  {:multipart
   [{:name "api_key" :content "NftZDbTYUkRnnkxKuWlQMnn_ks5G3ITn"}
    {:name "api_secret" :content "cnMO24ie4I41m45ns8Y0d5_JabrWHfXs"}
    {:name "image_file" :content (clojure.java.io/file file-name)}]})

(defn rect->posture [body-dic]
  (let [rectangle (get body-dic "humanbody_rectangle")
        height (get rectangle "height")
        width (get rectangle "width")]
    (cond
      (>= 1/3 (/ width height)) :stand
      (<  1   (/ width height)) :lay
      :else                     :sit
      )))

(defn respond-body [respond]
  (let [status (:status respond)]
    (if (= 200 status)
      (:body respond)
      (println "something went wrong with the server"))))

(defn respond-content->body-array [respond-body]
  (let [respond-dic (json/read-str respond-body)
        body-array (get respond-dic "humanbodies") ]
    (if (zero? (count body-array))
      (println "no people detected")
      body-array)))


(defn make-request [file-name]
  (let [respond (client/post url (multi-part file-name))
        status (:status respond)]
    (pprint respond)
    (->> respond
         (respond-body)
         (respond-content->body-array)
         (map rect->posture ))))

(defn -main [& args]
  (let [options (parse-opts args cli-options)
        input (get-in options [:options :input])]
    (make-request input)))
