(ns x2y.core-test
  (:require
   [cljs.test :refer-macros [deftest is testing]]
   [x2y.core :as x2y]
   [clojure.string :as str]
   ))

;; 从本地文件系统中打开文本文件并读取其内容
(defn fread [fname]
  (:require "fs") ;; (需要使用 Node REPL)
  (let
      [file-buff (fs.readFileSync fname {:encoding "utf8" :flag "r"})]
    (.toString file-buff)))

(deftest test-fread
  (testing "测试 fread 函数"
    (is (= (count (str/split-lines (fread "../LICENSE"))) 201))))

(let
    [roam-outline "- TopicX\n    - Question_i\n        - Answer_ia\n            - ia1\n                - ia11\n                - ia12\n            - ia2\n                - ia21\n        - Answer_ib\n            - ib1\n                - ib1\n    - Question_j\n        - Answer_ja\n            - ja1\n            - ja2\n- TopicY\n    - Question_k\n"]
  (x2y/jsRoam2Anki roam-outline)
)

(cljs.test/run-tests)
