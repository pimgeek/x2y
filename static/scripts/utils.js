// 实现自定义格式显示日期
Date.prototype.format = function(fmt){
  var o = {
    "M+" : this.getMonth()+1,                 // 月份
    "d+" : this.getDate(),                    // 日
    "h+" : this.getHours(),                   // 小时
    "m+" : this.getMinutes(),                 // 分
    "s+" : this.getSeconds(),                 // 秒
    "q+" : Math.floor((this.getMonth()+3)/3), // 季度
    "S"  : this.getMilliseconds()             // 毫秒
  };
  if(/(y+)/.test(fmt)){
    fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length));
  }
  for(var k in o){
    if(new RegExp("("+ k +")").test(fmt)){
      fmt = fmt.replace(
        RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));
    }
  }
  return fmt;
}
// 获取当前日期
function getTimestamp(){
  let ts = new Date().format('yyyy年MM月dd日-hh时mm分');
  return ts
}
// 尝试把给定的内容写入一个 DOM 对象（TEXTAREA）
function setNativeValue(element, value) {
  const { set: valueSetter } = Object.getOwnPropertyDescriptor(element, 'value') || {}
  const prototype = Object.getPrototypeOf(element)
  const { set: prototypeValueSetter } = Object.getOwnPropertyDescriptor(prototype, 'value') || {}
  if (prototypeValueSetter && valueSetter !== prototypeValueSetter) {
    prototypeValueSetter.call(element, value)
  } else if (valueSetter) {
    valueSetter.call(element, value)
  } else {
    throw new Error('指定的元素没有 setter.')
  }
}
// 利用 setNativeValue 函数把指定内容写入 TextArea 编辑框
function fillTextArea(textarea_id, contents) {
  let textarea = document.getElementById(textarea_id)
  setNativeValue(textarea, contents)
  textarea.dispatchEvent(new Event('input', { bubbles: true }))
}
// 读取本地文件内容并做处理
function readLocalFile(event) {
  // 获取用户手动上传的文件名
  let file = event.target.files[0]
  let textarea_id = event.target.textarea_id
  // 如果文件不存在，暂停执行
  if (!file) {
    alert("无法打开文件...") 
  } else {
    let reader = new FileReader()
    reader.onload = function(evt) {
      let content = evt.target.result
      fillTextArea(textarea_id, content)
    }
    reader.readAsText(file)
  }
}
function genAbstract(tx_id){
  let tx = document.getElementById(tx_id)
  let markdown_titles = tx.value
  // let leveled_obj = text2LeveledObj(markdown_titles)
  let dt = new Date().format("yyyy年MM月dd日-")
  // let topic = getXPathInOutline(leveled_obj, leveled_obj[3])
  return dt // + topic
}
// 复制到剪贴板
function doCopy(text) {
  var input = document.createElement('textarea');
  input.innerHTML = text;
  document.body.appendChild(input);
  input.select();
  var result = document.execCommand('copy');
  document.body.removeChild(input);
  return result;
}
// 点击下载按钮后，利用此方法创建文件并唤起下载动作
function downloadAsFile(tx_id, filename) {
  let tx = document.getElementById(tx_id)
  let text = tx.value
  let pom = document.createElement('a')
  pom.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(text))
  pom.setAttribute('download', filename)
  if (document.createEvent) {
    let event = document.createEvent('MouseEvents')
    event.initEvent('click', true, true)
    pom.dispatchEvent(event)
  }
  else { pom.click() }
}
// 提示文件正在下载，避免用户重复点击下载按钮
function lockoutSubmit(btn_id) {
  let btn = document.getElementById(btn_id)
  let oldLabel = btn.textContent
  btn.setAttribute('disabled', true)
  btn.textContent = '...文件下载中...'
  setTimeout(function() {
    btn.textContent = oldLabel
    btn.removeAttribute('disabled')
  }, 3000)
}
