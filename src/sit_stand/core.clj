(ns sit-stand.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clj-http.client :as client]
            [clojure.data.json :as json])
  (:gen-class))

(def cli-options
  [["-i" "--input input.jpg" "image file"
    :default "input.jpg"]
   ["-h" "--help"]])

(def url "https://api-cn.faceplusplus.com/humanbodypp/beta/detect")

(defn multi-part [file-name]
  {:multipart
   [{:name "api_key" :content "NftZDbTYUkRnnkxKuWlQMnn_ks5G3ITn"}
    {:name "api_secret" :content "cnMO24ie4I41m45ns8Y0d5_JabrWHfXs"}
    {:name "image_file" :content (clojure.java.io/file file-name)}]})

(defn sit? [rectangle]
  (let [height (get rectangle "height")
        width (get rectangle "width")]
    (if (< 1/3 (/ width height))
      true
      false)))

(defn human? [respond-body]
  (let [humanbodies (get respond-body "humanbodies")
        body-count (count humanbodies)]
    (if (= 0 body-count)
      false
      true)))

(defn sit-or-stand [human-body]
  (if (sit? (human-body "humanbody_rectangle"))
    "坐着"
    "站着")
  )

(defn make-request [file-name]
  (let [respond (client/post url (multi-part file-name))
        status (:status respond)]
    (pprint respond)
    (if (= 200 status)
      (let [respond-body (json/read-str (get respond :body))]
        (if (human? respond-body)
          (map sit-or-stand
               (respond-body "humanbodies" ))
          (println "没有检测到人脸，或者图片格式错误")))
      )))


(defn -main [& args]
  (parse-opts args cli-options))
