function alertMsg(title,msg) {
    //遮罩层
    var cirnterm = document.createElement("DIV");
    cirnterm.style.position = "absolute";
    cirnterm.style.width = "100%";
    cirnterm.style.height = "100%";
    cirnterm.style.zIndex = "50";
    cirnterm.style.top = "0";
    cirnterm.style.left = "0";
    cirnterm.style.backgroundColor = "rgba(0,0,0,0.1)";
    // cirnterm.style.opacity = "0.3";
    //显示信息层
    var alertBox = document.createElement("DIV");
    alertBox.style.position = "absolute";
    alertBox.style.top = "50%";
    alertBox.style.left = "50%";
    alertBox.style.zIndex = "300";
    alertBox.style.marginTop = "-140px";
    alertBox.style.marginLeft = "-120px";
    //内容块
    var alerHtml = "<ul style=\"list-style: none;border:1px solid #999999;margin:0px;padding:0px;width: 350px;\">";
    alerHtml += "<li style=\"text-align: center;line-height: 40px;font-size: 16px;background-color: #F2DEDF;color:#D9544F;\">"+title+"</li>";
    alerHtml += "<li style=\"text-align: center;line-height: 60px;font-size: 18px;border: 1px solid gainsboro;background-color: #fff\">"+msg+"</li>";
    alerHtml += "<li style=\"text-align: center;line-height: 50px;font-size: 15px;background-color: #cccccc\"><input value=\"确&nbsp;&nbsp;定\" style=\"cursor:pointer;height: 40px;width:100px;font-size: 18px;text-align: center;\"type=\"button\"onclick=\"okDo()\"></li>";
    alerHtml += "</ul>";
    alertBox.innerHTML = alerHtml;
    document.body.appendChild(alertBox);
    document.body.appendChild(cirnterm);
    this.okDo = function () {
        cirnterm.style.display = "none";
        alertBox.style.display = "none";
    }
}