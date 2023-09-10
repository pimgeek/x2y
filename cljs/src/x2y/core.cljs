(ns x2y.core
  (:require
   [clojure.string :as str] ;; 用于字符串处理
   ))

;;-- 打印 X2Y 工具基本信息 --
(js/console.log "== x2y 文本转换/处理工具生成器 ==")

;;-- 内部函数定义 --

;; 移除前导和末尾换行符
(defn trim-newlines [text]
  (let
      [text1 (str/trim-newline text)
       text2 (str/replace text1 #"(?iu)^[\r\n]+" "")]
    text2))

(defn wc [text]
  (let
      [lines (str/split-lines text)]
    (count lines)))

;;-- 对外输出的函数定义 --

;; 把输入笔记原样复制一份
(defn ^:export jsCopy [notes]
  notes)

;; 把输入笔记中的所有英文字符统一改为大写
(defn ^:export jsLower2Upper [notes]
  (str/upper-case notes))

;;
(defn ^:export jsCodemapStats [codemap]
  (let
      [line-count (wc codemap)]
    (str "总行数：" (str line-count))))
