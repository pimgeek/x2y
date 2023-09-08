#!/bin/sh

# TARGET_LOCATION 是目标存储位置，形如 domain.com:/path/to/x2y
# ./dist/ 目录下的所有文件将被同步到这个位置
#
# SSH_PORT 是通过 SSH 访问 domain.com 时选用的端口地址
# 即使 domain.com 使用了常规的 SSH 端口 22，也必须要输入
#
# 远程服务器上需要提前安装 rsync 工具，不然以下命令将无法正常运行

TARGET_LOCATION=$1
SSH_PORT=$2

rsync -rvz -e "ssh -p ${SSH_PORT}" ./dist/ ${TARGET_LOCATION}
