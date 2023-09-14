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

(cljs.test/run-tests)
