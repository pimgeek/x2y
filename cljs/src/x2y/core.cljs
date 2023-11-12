(ns x2y.core
  (:require
   [clojure.string :as str] ;; 用于字符串处理
   [markdown.core :as md]))

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
(defn count-lines-of-text [text]
  (let
      [lines (str/split-lines text)]
    (count lines)))

;; 判断给定字符是否为单个非 ASCII 字符（单个汉字）
(defn single-non-ascii? [s]
  (let
      [matched (re-seq #"(?u)^[^\u0000-\u007f]$" s)]
    (if matched true false)))

;; 判断给定字符是否为多个非 ASCII 字符（词组或短语）
(defn multiple-non-ascii? [s]
  (let
      [matched (re-seq #"(?u)^[^\u0000-\u007f]{2,}$" s)]
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

;; 对给定的单行文本做缩进改变处理，其中 d1, w1, d2, w2 分别代表
;; 原来的缩进字符 / 缩进宽度，新的缩进字符 / 缩进宽度
(defn reindent-line [line d1 w1 d2 w2]
  (let
      [ipattern1 (re-pattern (str "^" d1 "+"))
       imatch1 (re-find ipattern1 line)
       indent1 (if imatch1 imatch1 "")
       indent1-width (if indent1 (count indent1) 0)
       indent2 (nchars d2 (* w2 (/ indent1-width w1)))
       reindented (str/replace line indent1 indent2)]
    reindented))

;; 对给定的多行文本做缩进改变处理，其中 d1, w1, d2, w2 分别代表
;; 原来的缩进字符 / 缩进宽度，新的缩进字符 / 缩进宽度
(defn reindent-text [text d1 w1 d2 w2]
  (let
      [lines (str/split-lines text)
       reindented-lines (map #(reindent-line % d1 w1 d2 w2) lines)
       reindented-text (str/join "\n" reindented-lines)]
    reindented-text))

;; 对给定的单行文本做列表样式改变处理，其中 d1, s1, s2 分别代表
;; 原来的缩进字符 / 列表样式，新的列表样式（缩进字符保持不变）
(defn change-list-style [line d1 s1 s2]
  (let
      [ipattern1 (re-pattern (str "^(" d1 "*)" s1))
       imatch1 (re-find ipattern1 line)
       indent1 (if imatch1 (first imatch1) "")
       indent2 (if indent1 (str (second imatch1) s2) "")
       changed-line (str/replace line indent1 indent2)]
    changed-line))

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
      [lines (str/split-lines codemap)
       filtered-lines (filter #(re-matches #"^[^#\t]+\t[^\t]+(\t[^\t]+)*$" %) lines)
       line-count (count filtered-lines)
       filtered-text (str/join "\n" filtered-lines)
       first-col (get-nth-column-vec filtered-text 1)
       first-col-uniq (distinct first-col)
       second-col (get-nth-column-vec filtered-text 2)
       sna-elems (filter single-non-ascii? first-col-uniq)
       sna-count (count sna-elems)
       mna-elems (filter multiple-non-ascii? first-col-uniq)
       mna-count (count mna-elems)
       dups-in-second-col (count-element-dups second-col)]
    (str "总行数：" (str line-count) "\n"
         "单字数：" (str sna-count) "\n"
         "词组数：" (str mna-count) "\n"
         "冲突数：" (str dups-in-second-col) "\n")))
;;
;;
(defn ^:export jsLetter2Stroke [codemap]
  (let
      [t01 (str/replace codemap #"[Aa]" "㇋")
       t02 (str/replace t01 #"[Bb]" "㇄")
       t03 (str/replace t02 #"[Cc]" "𠃍")
       t04 (str/replace t03 #"[Dd]" "㇀")
       t05 (str/replace t04 #"[Ee]" "㇇")
       t06 (str/replace t05 #"[Ff]" "丨")
       t07 (str/replace t06 #"[Gg]" "亅")
       t08 (str/replace t07 #"[Hh]" "𠄌")
       t09 (str/replace t08 #"[Ii]" "㇀")
       t10 (str/replace t09 #"[Jj]" "一")
       t11 (str/replace t10 #"[Kk]" "丶")
       t12 (str/replace t11 #"[Ll]" "㇏")
       t13 (str/replace t12 #"[Mm]" "𡿨")
       t14 (str/replace t13 #"[Nn]" "𠃋")
       t15 (str/replace t14 #"[Oo]" "⺄")
       t16 (str/replace t15 #"[Pp]" "㇊")
       t17 (str/replace t16 #"[Qq]" "㇎")
       t18 (str/replace t17 #"[Rr]" "𠃌")
       t19 (str/replace t18 #"[Ss]" "丿")
       t20 (str/replace t19 #"[Tt]" "㇁")
       t21 (str/replace t20 #"[Uu]" "乚")
       t22 (str/replace t21 #"[Vv]" "㇅")
       t23 (str/replace t22 #"[Ww]" "𠄎")
       t24 (str/replace t23 #"[Xx]" "ㄣ")
       t25 (str/replace t24 #"[Yy]" "㇂")
       t26 (str/replace t25 #"[Zz]" "㇉")]
    t26))

;;
;;
(defn ^:export jsStroke2Letter [codemap]
  (let
      [t01 (str/replace codemap #"㇋" "A")
       t02 (str/replace t01 #"㇄" "B")
       t03 (str/replace t02 #"𠃍" "C")
       t05 (str/replace t03 #"㇇" "E")
       t06 (str/replace t05 #"丨" "F")
       t07 (str/replace t06 #"亅" "G")
       t08 (str/replace t07 #"𠄌" "H")
       t10 (str/replace t08 #"一" "J")
       t11 (str/replace t10 #"丶" "K")
       t12 (str/replace t11 #"㇏" "L")
       t13 (str/replace t12 #"𡿨" "M")
       t14 (str/replace t13 #"𠃋" "N")
       t15 (str/replace t14 #"⺄" "O")
       t16 (str/replace t15 #"㇊" "P")
       t17 (str/replace t16 #"㇎" "Q")
       t18 (str/replace t17 #"𠃌" "R")
       t19 (str/replace t18 #"丿" "S")
       t20 (str/replace t19 #"㇁" "T")
       t21 (str/replace t20 #"乚" "U")
       t22 (str/replace t21 #"㇅" "V")
       t23 (str/replace t22 #"𠄎" "W")
       t24 (str/replace t23 #"ㄣ" "X")
       t25 (str/replace t24 #"㇂" "Y")
       t26 (str/replace t25 #"㇉" "Z")]
    t26))

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

;; 把输入的 Roam 格式大纲笔记转换为 Markdown 标题大纲格式
(defn ^:export jsRoam2Markdown [roam-outline]
  (let
      [lines (str/split-lines roam-outline)
       reindented-lines (map #(reindent-line % " " 4 "#" 1) lines)
       md-lines (map #(change-list-style % "#" "- " "# ") reindented-lines)
       md-outline (str/join "\n" md-lines)]
    md-outline))
