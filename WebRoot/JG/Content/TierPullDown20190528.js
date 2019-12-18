/*递归实现获取无级树数据并生成DOM结构*/
var str = "";
var forTree = function (o) {
    for (var i = 0; i < o.length; i++) {
        var rootNode=o[i];
        var root=rootNode.syParent;
        var urlstr = "";
        try {
            if(root != "ROOT"){
            if (rootNode.childTreeDto.length > 0) {

                    urlstr = "<div><div><span>" + rootNode["flmc"] + "</span>/";
                    // urlstr = "<span>" + rootNode["flmc"] + "</span>/";

            } else {
                urlstr = "<span>" + rootNode["flmc"] + "</span><i><img src='/JG/Content/images/xiaojiantou.png' alt='' style='width:12px;margin-left: 12px;'></i></div>";
                // urlstr = "<span>" + rootNode["flmc"] + "</span>";
                if (rootNode.data && rootNode.data.length > 0) {
                    for (var j = 0; j < rootNode.data.length; j++) {
                        var itemData=rootNode.data[j].productInsStandardChildList;
                        for (var k = 0; k < itemData.length; k++) {
                            var item = itemData[k];
                            var InspectionCode=item.values.CPJYBZZB_NO_CODE;     //获取是否需要填检验值码
                            var JudgeCode=item.values.CPJYBZZB_JYPDFL_CODE;     //获取检验判断分类码

                            if(InspectionCode == 0){
                                urlstr += "<div class='radio_div'>"
                                    + "<div><span>" + item.values.JYXMDA_XMMC + "</span><input type='radio' name='+k+' value='合格'>合格<input type='radio' name='+k+' value='不合格' style='margin-left: 10px;'>不合格</div></div>";
                            }else if(InspectionCode == 1){

                                if(JudgeCode == 1){    //等于的情况
                                    urlstr += "<div class='radio_div'>"
                                        + "<div class='p_checkout'>"
                                        + "<p><span>"+ item.values.JYXMDA_XMMC +"</span><input type='radio' name='+k+' value='合格'>合格<input type='radio' name='+k+' value='不合格' style='margin-left: 10px;'>不合格</p>"
                                        + "<p><span>"+ item.values.CPJYBZZB_JYPDFL_NAME +"</span></p>"
                                        +"<p><span>参考值</span><input readonly='readonly' type='text' value='"+item.values.CPJYBZZB_CKZ+"'></p>"
                                        +"<p><span>检验值</span><input type='text' placeholder='输入检验值'></p>"
                                        + "</div></div>";
                                }else if(JudgeCode == 2){       //在范围内的情况
                                    urlstr += "<div class='radio_div'>"
                                        + "<div class='p_checkout2'>"
                                        + "<p><span>"+ item.values.JYXMDA_XMMC +"</span><input type='radio' name='+k+' value='合格'>合格<input type='radio' name='+k+' value='不合格' style='margin-left: 10px;'>不合格</p>"
                                        + "<p><span>"+ item.values.CPJYBZZB_JYPDFL_NAME +"</span></p>"
                                        +"<p><span>参考值</span><input readonly='readonly' type='text' value='"+item.values.CPJYBZZB_ZXZ+"' style='text-align: center;'><input readonly='readonly' type='text' value='"+item.values.CPJYBZZB_ZDZ+"' style='text-align: center;'></p>"
                                        +"<p><span>检验值</span><input type='text' placeholder='输入检验值'></p>"
                                        + "</div></div>";
                                }else if(JudgeCode == 3){       //在范围外的情况
                                    urlstr += "<div class='radio_div'>"
                                        + "<div class='p_checkout2'>"
                                        + "<p><span>"+ item.values.JYXMDA_XMMC +"</span><input type='radio' name='+k+' value='合格'>合格<input type='radio' name='+k+' value='不合格' style='margin-left: 10px;'>不合格</p>"
                                        + "<p><span>"+ item.values.CPJYBZZB_JYPDFL_NAME +"</span></p>"
                                        +"<p><span>参考值</span><input readonly='readonly' type='text' value='"+item.values.CPJYBZZB_ZXZ+"' style='text-align: center;'><input readonly='readonly' type='text' value='"+item.values.CPJYBZZB_ZDZ+"' style='text-align: center;'></p>"
                                        +"<p><span>检验值</span><input type='text' placeholder='输入检验值'></p>"
                                        + "</div></div>";
                                }
                            }
                        }
                    }
                }
            }
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