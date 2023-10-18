(ns x2y.core
  (:require
   [clojure.string :as str] ;; 用于字符串处理
   [markdown.core :as md]
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
       sum-dups (reduce + sub-dups)]
    sum-dups))

;; 生成指定宽度的字符串
(defn nchars [char n]
  (reduce str (repeat n char)))

;; 给定一段大纲笔记(纯文本格式)，对它进行分块处理
;; 确保每块都是单根结构的大纲列表
(defn get-root-blocks [outline]
  (str/split outline #"(?m)(?=^[*-]) "))

;; 给定一个单根结构的大纲块，获取它的所有子级节点(纯文本格式)
;; 并对这些节点统一减少一级缩进，这样就能进一步分块
(defn get-sub-outline [outline-block ichar isize]
  (let
      [lines (str/split-lines outline-block)
       sub-lines (rest lines)
       indent-pattern (re-pattern (str "^" (nchars ichar isize)))
       outdent #(str/replace % indent-pattern "")
       outdented (map outdent sub-lines)]
    (str/join "\n" outdented)))

;; 给定一个单根结构的大纲块，获取它根节点的文字内容
;; 作为当前大纲的标题，返回给调用者
(defn get-block-title [outline-block]
  (let
      [lines (str/split-lines outline-block)
       line1 (first lines)]
    (str/replace line1 #"[*-] " "")))

;; 给定一段大纲笔记，按照 Markdown 语法解析它们
;; 并生成对应的 HTML 代码，返回给调用者
(defn format-answer [answer-outline]
  (md/md->html answer-outline))

;; 给定一个单根结构的大纲块 (问答结构)，把它的第一行
;; 当作问题，其余部分当作回答，转换为 CSV 格式
(defn format-question [question-block ichar isize]
  (let
      [question-name (get-block-title question-block)
       answer-outline (get-sub-outline question-block ichar isize)
       answer-html (format-answer answer-outline)]
    (str question-name "," answer-html)))

;; 给定一个单根结构的大纲块 (主题结构)，把它的第一行
;; 当作专题名，其余部分当作问答块，转换为 CSV 格式
(defn format-topic [topic-block ichar isize]
  (let
      [topic-name (get-block-title topic-block)
       question-outline (get-sub-outline topic-block ichar isize)
       question-blocks (get-root-blocks question-outline)
       formatted-questions (map #(format-question % ichar isize) question-blocks)
       topic-added (map #(str topic-name "," %) formatted-questions)]
    (str/join "\n" topic-added)))

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

;; 把输入的 Roam 格式大纲笔记转换为用于导入 Anki 的 CSV 格式
(defn ^:export jsRoam2Anki [roam-outline]
  (let
      [topic-blocks (get-root-blocks roam-outline)
       formatted-topics (map #(format-topic % " " 4) topic-blocks)]
    (str/join "\n" formatted-topics)))

;; 把输入的 WorkFlowy 格式大纲笔记转换为用于导入 Anki 的 CSV 格式
(defn ^:export jsWorkFlowy2Anki [wf-outline]
  (let
      [topic-blocks (get-root-blocks wf-outline)
       formatted-topics (map #(format-topic % " " 2) topic-blocks)]
    (str/join "\n" formatted-topics)))
