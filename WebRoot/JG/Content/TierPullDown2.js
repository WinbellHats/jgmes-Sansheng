/*递归实现获取无级树数据并生成DOM结构*/
// var str = "";
// var forTree = function (o) {
//     for (var i = 0; i < o.length; i++) {
//         var rootNode=o[i];
//         var root=rootNode.syParent;
//         var urlstr = "";
//         try {
//             if(root != "ROOT"){
//                 if (rootNode.childTreeDto.length > 0) {
//                     urlstr = "<span style='color:#74C0B8;'>" + rootNode["flmc"] + "</span>/";
//                 } else {
//                     urlstr += "<span style='color:#74C0B8;'>" + rootNode["flmc"] + "</span><i><img src='/JG/Content/images/xiaojiantou.png' alt='' style='width:12px;margin-left: 12px;'></i>";
//
//                     if (rootNode.data && rootNode.data.length > 0) {
//                         for (var j = 0; j < rootNode.data.length; j++) {
//                             var itemData=rootNode.data[j].productInsStandardChildList;
//                             for (var k = 0; k < itemData.length; k++) {
//                                 var item = itemData[k];
//                                 var InspectionCode=item.values.SJJYZB_JYZ;     //获取检验值
//                                 var JudgeCode=item.values.SJJYZB_JYPDFL_CODE;     //获取检验判断分类码
//
//                                 if(InspectionCode == null || !InspectionCode){
//                                     urlstr += "<div class='radio_div'>"
//                                         + "<div><span>" + item.values.JYXMDA_XMMC + "</span><span>" + item.values.SJJYZB_HG_NAME + "</span></div></div>";
//                                 }else{
//
//                                     if(JudgeCode == 1){    //等于的情况
//                                         urlstr += "<div class='radio_div'>"
//                                             + "<p><span>"+ item.values.JYXMDA_XMMC +"</span><span>"+ item.values.SJJYZB_HG_NAME +"</span></p>"
//                                             + "<p><span>"+ item.values.SJJYZB_JYPDFL_NAME +"</span></p>"
//                                             +"<p><span>参考值</span><input readonly='readonly' type='text' value='"+item.values.SJJYZB_CKZ+"'></p>"
//                                             +"<p><span>检验值</span><input type='text' value='"+item.values.SJJYZB_JYZ+"'></p>"
//                                             + "</div>";
//                                     }else if(JudgeCode == 2){       //在范围内的情况
//                                         urlstr += "<div class='radio_div'>"
//                                             + "<p><span>"+ item.values.JYXMDA_XMMC +"</span><span>"+ item.values.SJJYZB_HG_NAME +"</span></p>"
//                                             + "<p><span>"+ item.values.SJJYZB_JYPDFL_NAME +"</span></p>"
//                                             +"<p><span>参考值</span><input readonly='readonly' type='text' value='"+item.values.SJJYZB_ZXZ+"' style='text-align: center;'><input readonly='readonly' type='text' value='"+item.values.SJJYZB_ZDZ+"' style='text-align: center;'></p>"
//                                             +"<p><span>检验值</span><input type='text' value='"+item.values.SJJYZB_JYZ+"'></p>"
//                                             + "</div>";
//                                     }else if(JudgeCode == 3){       //在范围外的情况
//                                         urlstr += "<div class='radio_div'>"
//                                             + "<p><span>"+ item.values.JYXMDA_XMMC +"</span><span>"+ item.values.SJJYZB_HG_NAME +"</span></p>"
//                                             + "<p><span>"+ item.values.SJJYZB_JYPDFL_NAME +"</span></p>"
//                                             +"<p><span>参考值</span><input readonly='readonly' type='text' value='"+item.values.SJJYZB_ZXZ+"' style='text-align: center;'><input readonly='readonly' type='text' value='"+item.values.SJJYZB_ZDZ+"' style='text-align: center;'></p>"
//                                             +"<p><span>检验值</span><input type='text' value='"+item.values.SJJYZB_JYZ+"'></p>"
//                                             + "</div>";
//                                     }
//                                 }
//                             }
//                         }
//                     }
//                 }
//             }
//             str += urlstr;
//             if (rootNode.childTreeDto.length > 0) {
//                 forTree(rootNode.childTreeDto);
//             }
//             str += "</div>";
//         } catch (e) {}
//     }
//     return str;
// }

var str = "";
var strJson="{";
var oid='';

var forTree = function (o,isTop) {
	if(oid==''){
		oid=o[0].id;
	}
    for (var i = 0; i < o.length; i++) {
		
		var jsonOb={};
        var rootNode=o[i];
        var root=rootNode.syParent;
        var urlstr ="";
        try {
//            if(root != "ROOT"){
                if(isTop){
                    urlstr = "<div class='outermost_div'>";
                }
                if (rootNode.childTreeDto!=null&&rootNode.childTreeDto.length>0) {
                    urlstr += "<span style='color:#74C0B8;'>" + rootNode["flmc"] + "</span>/";
                } else {
                    urlstr += "<span style='color:#74C0B8;'>" + rootNode["flmc"] + "</span><i><img src='/JG/Content/images/xiaojiantou.png' alt='' style='width:12px;margin-left: 12px;'></i>";

                    if (rootNode.data && rootNode.data.length > 0) {
  //                      for (var j = 0; j < rootNode.data.length; j++) {
            //                var itemData=rootNode.data[j].productInsStandardChildList;
                    	 var itemData=rootNode.data;
                            for (var k = 0; k < itemData.length; k++) {
                                //var item = itemData[k];
								if(itemData[k].values){
									var item = itemData[k];
								}else{ 
									var item = itemData[k].productInsStandardChildList[0];
								}
								
                                var InspectionCode=item.values.SJJYZB_NO_CODE;     //获取检验值
                                var JudgeCode=item.values.SJJYZB_JYPDFL_CODE;     //获取检验判断分类码
								var keyId= item.values.JGMES_ZLGL_SJJYZB_ID;
								var JYFAZBZJID=item.values.SJJYZB_JYFAZBZJID;
								jsonOb.keyId=keyId;
                                if(InspectionCode == 0){
									var hg=item.values.SJJYZB_HG_CODE==1?'合格':'不合格';
									strJson+='"'+item.values.SJJYZB_JYFAZBZJID+'":["'+keyId+'","'+hg+'"],';
                                    urlstr += "<div class='radio_div'>"
                                       // + "<div><span style='display: inline-block;width:50%;'>" + item.values.JYXMDA_XMMC + "</span><span style='display: inline-block;width:50%;text-align: right'>" + item.values.SJJYZB_HG_NAME + "</span></div>"
										//style='display: inline-block;width:50%;'
										+ "<p><span >" + item.values.JYXMDA_XMMC + "</span>"
                                       if(hg=='合格'){
										 urlstr +=  "<span><input type='radio' checked='checked'id='"+keyId+"'  title='"+JYFAZBZJID+"' onclick='changeSave(this)' name='"+keyId+"' value='合格'>合格<input type='radio'id='"+keyId+"'  title='"+JYFAZBZJID+"' onclick='changeSave(this)' name='"+keyId+"' value='不合格' style='margin-left: 10px;'>不合格</span></p>"
									   }else if(hg=='不合格'){
										  urlstr +=  "<span><input type='radio' id='"+keyId+"' title='"+JYFAZBZJID+"'  onclick='changeSave(this)' jyfazbzjid='"+JYFAZBZJID+"'  name='"+keyId+"' value='合格'>合格<input type='radio'id='"+keyId+"' title='"+JYFAZBZJID+"'  onclick='changeSave(this)' name='"+keyId+"' checked='checked' value='不合格' style='margin-left: 10px;'>不合格</span></p>" 
									   }else{
										  urlstr +=  "<span><input  type='radio' id='"+keyId+"'  title='"+JYFAZBZJID+"' onclick='changeSave(this)'  jyfazbzjid='"+JYFAZBZJID+"' name='"+keyId+"' value='合格'>合格<input type='radio'id='"+keyId+"'  title='"+JYFAZBZJID+"' onclick='changeSave(this)' name='"+keyId+"'   value='不合格' style='margin-left: 10px;'>不合格</span></p>" 
									   }
										urlstr += "</div>";
									
                                }else{
                                    if(JudgeCode == 1){    //等于的情况
										var hg=item.values.SJJYZB_HG_CODE==1?'合格':'不合格';
										strJson+='"'+item.values.SJJYZB_JYFAZBZJID+'":["'+keyId+'","'+hg+'","'+item.values.SJJYZB_JYZ+'"],';
                                        urlstr += "<div class='radio_div'>"
											+ "<p><span>"+ item.values.JYXMDA_XMMC +"</span><span>"
											if(hg=='合格'){
												 urlstr +=  "<input type='radio' checked='checked'id='"+keyId+"'  onclick='changeSave(this)' name='"+keyId+"' value='合格'>合格<input type='radio'id='"+keyId+"'  onclick='changeSave(this)' name='"+keyId+"' value='不合格' style='margin-left: 10px;'>不合格"
											}else  if(hg=='不合格'){
												  urlstr +=  "<input type='radio' id='"+keyId+"' onclick='changeSave(this)' name='"+keyId+"' value='合格'>合格<input type='radio'id='"+keyId+"'  onclick='changeSave(this)' name='"+keyId+"' checked='checked' value='不合格' style='margin-left: 10px;'>不合格" 
											}else {
												  urlstr +=  "<input type='radio' id='"+keyId+"' onclick='changeSave(this)' name='"+keyId+"' value='合格'>合格<input type='radio'id='"+keyId+"'  onclick='changeSave(this)' name='"+keyId+"'  value='不合格' style='margin-left: 10px;'>不合格" 
											}
											urlstr +="</span></p>"
                                            urlstr += "<p><span>"+ item.values.SJJYZB_JYPDFL_NAME +"</span></p>"
                                            urlstr +="<p><span>参考值</span><input readonly='readonly' type='text' value='"+item.values.SJJYZB_CKZ+"'></p>"
                                            urlstr +="<p><span>检验值</span><input type='text' id='"+keyId+"' name='"+keyId+"s'  onchange='changeSave(this)'   value='"+item.values.SJJYZB_JYZ+"'></p>"
                                            urlstr += "</div>";
                                    }else if(JudgeCode == 2){       //在范围内的情况
										var hg=item.values.SJJYZB_HG_CODE==1?'合格':'不合格';
										strJson+='"'+item.values.SJJYZB_JYFAZBZJID+'":["'+keyId+'","'+hg+'","'+item.values.SJJYZB_JYZ+'"],';
                                        urlstr += "<div class='radio_div'>"
                                            + "<p><span>"+ item.values.JYXMDA_XMMC +"</span><span>"+ item.values.SJJYZB_HG_NAME +"</span></p>"
											if(hg=='合格'){
												 urlstr +=  "<input type='radio' checked='checked'id='"+keyId+"'  onclick='changeSave(this)' name='"+keyId+"' value='合格'>合格<input type='radio'id='"+keyId+"'  onclick='changeSave(this)' name='"+keyId+"' value='不合格' style='margin-left: 10px;'>不合格"
											}else if(hg=='不合格'){
												  urlstr +=  "<input type='radio' id='"+keyId+"'  onclick='changeSave(this)'   name='"+keyId+"' value='合格'>合格<input type='radio' name='"+keyId+"'id='"+keyId+"'  onclick='changeSave(this)' checked='checked' value='不合格' style='margin-left: 10px;'>不合格" 
											}else{
												  urlstr +=  "<input type='radio' id='"+keyId+"'  onclick='changeSave(this)'   name='"+keyId+"' value='合格'>合格<input type='radio' name='"+keyId+"'id='"+keyId+"'  onclick='changeSave(this)'   value='不合格' style='margin-left: 10px;'>不合格" 
											}
                                            urlstr +="<p><span>参考值</span><input readonly='readonly' type='text' value='"+item.values.SJJYZB_ZXZ+"' style='text-align: center;'><input readonly='readonly' type='text' value='"+item.values.SJJYZB_ZDZ+"' style='text-align: center;'></p>"
                                            urlstr +="<p><span>检验值</span><input type='text'id='"+keyId+"' name='"+keyId+"s'  onchange='changeSave(this)' value='"+item.values.SJJYZB_JYZ+"'></p>"
                                            urlstr += "</div>";
                                    }else if(JudgeCode == 3){       //在范围外的情况
										var hg=item.values.SJJYZB_HG_CODE==1?'合格':'不合格';
										strJson+='"'+item.values.SJJYZB_JYFAZBZJID+'":["'+keyId+'","'+hg+'","'+item.values.SJJYZB_JYZ+'"],';
                                        urlstr += "<div class='radio_div'>"
                                            + "<p><span>"+ item.values.JYXMDA_XMMC +"</span><span>"+ item.values.SJJYZB_HG_NAME +"</span></p>"
											if(hg=='合格'){
												 urlstr +=  "<input type='radio' id='"+keyId+"'  onclick='changeSave(this)' checked='checked'  name='"+keyId+"' value='合格'>合格<input type='radio' id='"+keyId+"'  onclick='changeSave(this)' name='"+keyId+"' value='不合格' style='margin-left: 10px;'>不合格"
											}else  if(hg=='不合格'){
												 urlstr +=  "<input type='radio' id='"+keyId+"'  onclick='changeSave(this)'   name='"+keyId+"' value='合格'>合格<input type='radio' id='"+keyId+"'  onclick='changeSave(this)'  name='"+keyId+"' checked='checked' value='不合格' style='margin-left: 10px;'>不合格" 
											}else{
												  urlstr +=  "<input type='radio' id='"+keyId+"'  onclick='changeSave(this)'   name='"+keyId+"' value='合格'>合格<input type='radio' name='"+keyId+"'id='"+keyId+"'  onclick='changeSave(this)'   value='不合格' style='margin-left: 10px;'>不合格" 
											}
                                            urlstr +="<p><span>参考值</span><input readonly='readonly' type='text' value='"+item.values.SJJYZB_ZXZ+"' style='text-align: center;'><input readonly='readonly' type='text' value='"+item.values.SJJYZB_ZDZ+"' style='text-align: center;'></p>"
                                            urlstr +="<p><span>检验值</span><input type='text' id='"+keyId+"' name='"+keyId+"s'  onchange='changeSave(this)'  value='"+item.values.SJJYZB_JYZ+"'></p>"
                                            urlstr += "</div>";
                                    }
                                }
                            }
 //                       }
                    }
                }
 //           }
            str += urlstr;

            if (o[i].childTreeDto.length > 0) {
                if(root != "ROOT") {
                    forTree(o[i].childTreeDto,false);
                }else{
                    forTree(o[i].childTreeDto,true);
                }
            }
            str += "</div>";
        } catch (e) {}
    }
    return str;
}

//初始化数据 
var initData=function() {
	var reg=/,$/gi;
	strJson=strJson.replace(reg,"");
	strJson+="}";
	var StrDetail=localStorage.getItem('jsonStrDetail');
	if(StrDetail){
		StrDetail=$.parseJSON(StrDetail);
	 }
	 if(!StrDetail||StrDetail[oid]==""){
		var jsonzb=$.parseJSON(strJson);
		StrDetail[oid]=jsonzb;
		localStorage.setItem("jsonStrDetail",JSON.stringify(StrDetail));
	}
	console.log(strJson);
	oid='';
	strJson="{";
}






















