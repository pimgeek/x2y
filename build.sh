# 获取项目名称，比如：make ime_codemap_converter ...
PROJECT=$1
# 获取目标工具名，比如：make ime_codemap_converter get_stats
# 如果想生成全部，可写成：make ime_codemap_converter all
# 如果想快速重新构建，可以写成 make ime_codemap_converter q
TARGET=$2
# 删除旧的自动生成的 Makefile
rm -f Makefile
# 利用项目的 JSON 配置文件生成专属 Makefile
pug --silent -P -O ./model/${PROJECT}.json -o ./tmp -E txt ./template/Makefile.pug
# 把新生成的 Makefile 复制到当前目录
mv ./tmp/Makefile.txt ./Makefile
# 开始构建项目网站
make --quiet ${TARGET}
