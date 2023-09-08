// 用 process 工具包获取命令行参数
const ps = require('process');
// 用 fs 工具包读写配置文件
const fs = require('fs')

// 获取项目名称，比如：ime_codemap_converter (输入法码表转换工具)
const prj_name = ps.argv[2]
const tool_name = ps.argv[3]
// 读取项目专属配置文件，比如 ime_codemap_converter.json
const prj_conf = fs.readFileSync('./model/' + prj_name + '.json', 'utf8')
// 把配置文件的内容读取为 JSON 内存对象
const prj_conf_obj = JSON.parse(prj_conf)
// 定义另一个 JSON 内存对象，用于存储相关工具定义
let tool_conf_obj = {}
// 从整个项目的配置 prj_conf_obj 中提取 tool 相关的部分，存入 tool_conf_obj
tool_conf_obj["tool_spec"] = prj_conf_obj.tools[tool_name]
tool_conf_obj["tool_spec"]["prj_name"] = prj_name
tool_conf_obj["tool_spec"]["tool_name"] = tool_name
const tool_conf = JSON.stringify(tool_conf_obj)
console.log(tool_conf)