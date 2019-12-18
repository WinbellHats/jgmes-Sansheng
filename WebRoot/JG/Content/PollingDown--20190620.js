/*递归实现获取无级树数据并生成DOM结构*/
var str = "";
var divId = "CJ"+guid();
var forTree = function (o) {
    for (var i = 0; i < o.length; i++) {
        if (divId == "" || divId == null || divId == undefined) {
            divId = "CJ"+guid();
        }
        var rootNode=o[i];
        //var root=rootNode.syParent;
        var urlstr = "";
        try {
            if (rootNode.childTreeDto.length > 0) {
                urlstr = "<div  style='border: 1px solid #E5E5E5;padding-left: 5px;padding-right: 5px;padding-bottom: 10px;'><div style='margin-top: 10px;'><span style='color:#74C0B8;'>" + rootNode["flmc"] + "</span>/";
            } else {
                urlstr = "<span style='color:#74C0B8;'>" + rootNode["flmc"] + "</span><span><i><img id='"+ rootNode["id"]+"1"+"' src='/JG/Content/images/on.png' name='"+ divId +divId+"' onclick='show(this)' alt='' style='width:22px;margin-left: 22px;float: right;'></i><i><img id='"+ rootNode["id"]+"0"+"' src='/JG/Content/images/off.png' name='"+ divId +divId+"' onclick='hid(this)' alt='' style='width:22px;margin-left: 22px;float: right;display: none'></i></span></div>";
                // urlstr = "<span>" + rootNode["flmc"] + "</span>";
                if (rootNode.data && rootNode.data.length > 0) {
                    for (var j = 0; j < rootNode.data.length; j++) {
                        var itemData=rootNode.data[j].productInsStandardChildList;
                        for (var k = 0; k < itemData.length; k++) {
                            var item = itemData[k];
                            console.log("item");
                            console.log(item);
                            if(item.values.XJZBZB_NO_CODE==0){
                                var s= "<p style='text-align: center'><input type='radio' name='"+ item.values.JGMES_ZLGL_XJZBZB_ID +"' value='1' disabled=true/>合格<input type='radio' name='"+ item.values.JGMES_ZLGL_XJZBZB_ID +"' value='0' style='margin-left: 10px;' checked disabled=true/>不合格</p>"
                            }else{
                                var s= "<p style='text-align: center'><input type='radio' name='"+ item.values.JGMES_ZLGL_XJZBZB_ID +"' value='1' checked disabled=true/>合格<input type='radio' name='"+ item.values.JGMES_ZLGL_XJZBZB_ID +"' value='0' style='margin-left: 10px;' disabled=true>不合格</p>"
                            }

                            urlstr += "<div   class='radio_div' style='border: 1px solid #E5E5E5;'>"
                                + "<div class='"+ divId +divId+"' style='display: none'>"
                                + "<span style='padding-top: 10px'>" + item.values.XJZBZB_JYXMMC + "</span>：<span style='padding-top: 10px'>" + item.values.XJZBZB_JYYQ + "</span><br/>"
                                + s
                                + "</div></div>";
                        }
                    }
                }
                divId="";
            }

            str += urlstr;
            if (o[i].childTreeDto.length > 0) {
                forTree(o[i].childTreeDto);
            }
            str += "</div>";
        } catch (e) {}
    }
    return str;
}

function hid(data) {
    var id = "."+data.name;
    var imgId = "#"+data.id;
    var imgIdOther = imgId.substring(0,imgId.length-1)+"1";
    $(imgId).hide();
    $(imgIdOther).show();
    $(id).hide();



}
function show(data) {
    var id = "."+data.name;
    var imgId = "#"+data.id;
    var imgIdOther = imgId.substring(0,imgId.length-1)+"0";
    $(imgId).hide();
    $(imgIdOther).show();
    $(id).show();
}

function guid() {
    function S4() {
        return (((1+Math.random())*0x10000)|0).toString(16).substring(1);
    }
    return (S4()+S4()+S4()+S4()+S4()+S4()+S4()+S4());
}