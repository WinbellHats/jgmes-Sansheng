/*递归实现获取无级树数据并生成DOM结构*/
var tree={
    str:"",
    forTree:function (o) {
        for (var i = 0; i < o.length; i++) {
            var rootNode=o[i];
            // if (rootNode.childTreeDto==0){
            //     rootNode =o;
            // }
            //var root=rootNode.syParent;
            var urlstr = "";
            try {
                if (rootNode.childTreeDto.length > 0) {
                    urlstr = "<div style='border: 1px solid #E5E5E5;padding-left: 5px;padding-right: 5px;padding-bottom: 10px;'><div style='margin-top: 10px;'><span style='color:#74C0B8;'>" + rootNode["flmc"] + "</span>/";
                } else {
                    urlstr = "<span style='color:#74C0B8;'>" + rootNode["flmc"] + "</span></div>";
                    // urlstr = "<span>" + rootNode["flmc"] + "</span>";
                    if (rootNode.data && rootNode.data.length > 0) {
                        for (var j = 0; j < rootNode.data.length; j++) {
                            var itemData=rootNode.data[j].productInsStandardChildList;
                            for (var k = 0; k < itemData.length; k++) {
                                var item = itemData[k];
                                urlstr += "<div class='radio_div' style='border: 1px solid #E5E5E5'>"
                                    + "<div>"
                                    + "<span style='padding-top: 10px'>" + item.values.JYXMDA_XMMC + "</span>：<span style='padding-top: 10px'>" + item.values.CPJYBZZB_JYYQ + "</span><br/>"
                                    + "<p style='text-align: center'><input type='radio' id='"+JSON.stringify(item.values)+"' name='"+ item.values.JYXMDA_XMMC +k+"' value='1' checked/>合格<input type='radio' id='"+JSON.stringify(item.values)+"' name='"+ item.values.JYXMDA_XMMC +k+"' value='0' style='margin-left: 10px;'>不合格</p>"
                                    + "</div>";
                            }
                            urlstr+="</div>";
                        }
                    }
                }
                this.str += urlstr;
                if (o[i].childTreeDto.length > 0) {
                    this.forTree(o[i].childTreeDto);
                }
                this.str += "</div>";
            } catch (e) {}
        }
        return this.str;
    }
}
//var str = "";
// var forTree = function (o,str) {
//     for (var i = 0; i < o.length; i++) {
//         var rootNode=o[i];
//         //var root=rootNode.syParent;
//         var urlstr = "";
//         try {
//             if (rootNode.childTreeDto.length > 0) {
//                 urlstr = "<div style='border: 1px solid #E5E5E5;padding-left: 5px;padding-right: 5px;padding-bottom: 10px;'><div style='margin-top: 10px;'><span style='color:#74C0B8;'>" + rootNode["flmc"] + "</span>/";
//                 // urlstr = "<span>" + rootNode["flmc"] + "</span>/";
//
//             } else {
//                 urlstr = "<span style='color:#74C0B8;'>" + rootNode["flmc"] + "</span><i><img src='/JG/Content/images/xiaojiantou.png' alt='' style='width:12px;margin-left: 12px;'></i></div>";
//                 // urlstr = "<span>" + rootNode["flmc"] + "</span>";
//                 if (rootNode.data && rootNode.data.length > 0) {
//                     for (var j = 0; j < rootNode.data.length; j++) {
//                         var itemData=rootNode.data[j].productInsStandardChildList;
//                         for (var k = 0; k < itemData.length; k++) {
//                             var item = itemData[k];
//                             urlstr += "<div class='radio_div' style='border: 1px solid #E5E5E5'>"
//                                 + "<div>"
//                                 + "<span style='padding-top: 10px'>" + item.values.JYXMDA_XMMC + "</span>：<span style='padding-top: 10px'>" + item.values.CPJYBZZB_JYYQ + "</span>"
//                                 + "<p style='text-align: center'><input type='radio' id='"+JSON.stringify(item.values)+"' name='"+ item.values.JYXMDA_XMMC +k+"' value='1' checked/>合格<input type='radio' id='"+JSON.stringify(item.values)+"' name='"+ item.values.JYXMDA_XMMC +k+"' value='0' style='margin-left: 10px;'>不合格</p>"
//                                 + "</div></div>";
//                         }
//                     }
//                 }
//             }
//             str += urlstr;
//             if (o[i].childTreeDto.length > 0) {
//                 forTree(o[i].childTreeDto,str);
//             }
//             str += "</div>";
//         } catch (e) {}
//     }
//     return str;
// }