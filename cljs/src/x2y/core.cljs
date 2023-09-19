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

;; 统计文本行数
(defn count-lines [text]
  (let
      [lines (str/split-lines text)]
    (count lines)))

;; 判断给定字符是否为单个非 ASCII 字符
(defn single-non-ascii? [s]
  (let
      [matched (re-seq #"(?u)^[^\u0000-\u007f]$" s)]
    (if matched true false)))

;; 提取 TSV 文件中的第 n 列，返回纯文本
(defn get-nth-column-vec [tsv n]
  (let
      [lines (str/split-lines tsv)
       vectors (map (fn [s] (str/split s "\t")) lines)
       nth-of-lines (map (fn [l] (nth l (- n 1))) vectors)]
    nth-of-lines))

;; 对给定的字符串列表分类汇总，统计每个字符串重复出现的次数
;; 返回一个哈希表，便于事后查询
(defn count-element-dups [v]
  (let
      [sorted (sort v)
       grouped (partition-by identity sorted)
       dup-groups (filter #(> (count %) 1) grouped)  ; 从码数切换到重复数
       sub-dups (map count dup-groups)               ; 统计每组重码的个数
       sum-dups (reduce + sub-dups)
       ]
    sum-dups))

;;-- 对外输出的函数定义 --

;; 把输入笔记原样复制一份
(defn ^:export jsCopy [notes]
  notes)

;; 把输入笔记中的所有英文字符统一改为大写
(defn ^:export jsLower2Upper [notes]
  (str/upper-case notes))

;; 输出码表文件的统计分析信息
(defn ^:export jsCodemapStats [codemap]
  (let
      [line-count (count-lines codemap)
       first-col (get-nth-column-vec codemap 1)
       first-col-uniq (distinct first-col)
       second-col (get-nth-column-vec codemap 2)
       sna-elems (filter single-non-ascii? first-col-uniq)
       sna-count (count sna-elems)
       dups-in-second-col (count-element-dups second-col)]
    (str "总行数：" (str line-count) "\n"
         "单字数：" (str sna-count) "\n"
         "冲突数：" (str dups-in-second-col) "\n")))
