//共用类库
LocalUserInfo.CheckLogin();
(function ($) {
    $.getUrlParam = function (name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]); return null;
    }
})(jQuery);

//数据字典
function GetDictionary(Code) {
    var data;
    $.ajax({
        cache: true,
        type: "Post",
        url: LocalConfig.SrvPath + "/jgmes/commonAction!getDictionary.action",
        data: {
            "userCode": LocalUserInfo.GetUserInfo().UserCode,
            "mac": LocalUserInfo.GetUserInfo().Mac,
            parentCode: Code
        },
        datatype: "json",
        async: false,
        success: function (ret) {
            data = JSON.parse(ret);
            //console.log("数据字典返回:");
            //console.log(data);
        }, error: function (XMLHttpRequest, textStatus, errorThrown) {
            alert("数据字典请求失败");
        }
    });
    return data;
}

function GetDictionaryByID(id) {
    var data;
    $.ajax({
        cache: true,
        type: "Post",
        url: LocalConfig.SrvPath + "/jgmes/commonAction!getDictionary.action",
        data: { "userCode": LocalUserInfo.GetUserInfo().UserCode, "mac": LocalUserInfo.GetUserInfo().Mac, parentId: id },
        datatype: "json",
        async: false,
        success: function (ret) {
            data = JSON.parse(ret);
            //console.log("数据字典返回:");
            //console.log(data);
        }, error: function (XMLHttpRequest, textStatus, errorThrown) {
            alert("数据字典请求失败");
        }
    });
    return data;
}

//获取当前用户工序
function GetCurrentProcess(InvCode) {
    var data;
    $.ajax({
        cache: true,
        type: 'post',
        url: LocalConfig.SrvPath + "/jgmes/commonAction!getCurrentGx.action",
        data: { "userCode": LocalUserInfo.GetUserInfo().UserCode, "mac": LocalUserInfo.GetUserInfo().Mac, "cpCode": InvCode },
        datatype: "json",
        async: false,
        success: function (ret) {
            var ret1 = JSON.parse(ret);
            data = ReturnData(ret1);
        }, error: function (XMLHttpRequest, textStatus, errorThrown) {
            console.log("获取当前生产用户工序失败");
            alert("请求发生异常");
        }
    });
    return data;
}


//获取当前工位扫码累计数量
function GetCountScan(scrwId,CurrProcessCode,InvCode,ProductionLineCode) {
    var amount;
    var url = LocalConfig.SrvPath + "/jgmes/commonAction!getGwQty.action";
    $.ajax({
        //ajax缓存,当datatype为script和jsonp时，默认为false
        cache: true,
        type: "post",
        url: url,
        data: {
            "userCode": LocalUserInfo.GetUserInfo().UserCode,
            "mac": LocalUserInfo.GetUserInfo().Mac,
            "scrwId": scrwId,
            "cpCode":InvCode,
        },
        dataType: "json",
        async: false,    //同步请求
        success: function (result) {
            console.log(result);
            amount = ReturnData(result);
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            console.log("数据请求失败！");
        }
    });
    return amount;
}

//公共返回数据
function ReturnData(retData) {
    if (!retData.IsSuccess) {
        if (retData.ErrorCode == 10001) { //登录错误编码
            LocalUserInfo.ClearUser();
            alert("登录状态已经失效,请重新登录");
            window.location.href = window.location.href;
        } else {
            $.toptip(retData.message, "error");
        }
    }
    return retData;
}

//检测产品和条形码
function CheckProductBarCode(InvCode, BarCode, scrwId) {
    var data;
    $.ajax({
        type: "post",
        async: false,
        cache: true,
        url: LocalConfig.SrvPath + "/jgmes/commonAction!doCheckChangeCp.action",
        data: { userCode: LocalUserInfo.GetUserInfo().UserCode, mac: LocalUserInfo.GetUserInfo().Mac, cpCode: InvCode, barCode: BarCode, scrwId: scrwId },
        dataType: "json",
        success: function (ret) {
            data = ReturnData(ret);
        }, error: function (xhr, status, errorThrown) {
            console.log(xhr);
            alert("检测产品和条形码,请求失败");
        }
    });
    return data;
}



//获取产品打印参数信息
function GetProductPrintParam(InvCode) {
    var data;
    $.ajax({
        type: "post",
        cache: true,
        async: false,
        url: LocalConfig.SrvPath + "/jgmes/commonAction!getGxPrintInfo.action",
        data: { "userCode": LocalUserInfo.GetUserInfo().UserCode, "mac": LocalUserInfo.GetUserInfo().Mac, cpCode: InvCode },
        dataType: "json",
        success: function (ret) {
            data = ReturnData(ret);
        }, error: function (xhr, status, err) {
            console.log("获取产品打印参数信息失败," + xhr);
        }
    });
    return data;
}

//发起安灯系统处理
//安灯系统类型:设备异常(ADLX01),品质异常(ADLX02),缺料异常(ADLX03),呼叫主管(ADLX04)
function SaveAnLamp(AnLampTypeCode, Abnormal) {
    $.showLoading();
    //异常状态:待处理(YCZT01)
    var jsonStr = JSON.stringify({ ADYCCL_ADLX_CODE: AnLampTypeCode, ADYCCL_YCYY: Abnormal, ADYCCL_YCZT_CODE: "YCZT01" });
    $.ajax({
        type: "post",
        url: LocalConfig.SrvPath + "/jgmes/commonAction!doJsonSaveYcCl.action",
        data: { "userCode": LocalUserInfo.GetUserInfo().UserCode, "mac": LocalUserInfo.GetUserInfo().Mac, jsonStr: jsonStr },
        dataType: "json",
        success: function (ret) {
            var data = ReturnData(ret);
            if (data.IsSuccess) {
                $.toptip("发起成功", "success");
                $(".menu_list").removeClass("menu_list_show");
                $(".menu_list>ul").fadeOut();
                setTimeout(function () {
                    $(".menu").fadeOut();
                }, 100)
            }
            else $.toptip("发起失败!异常原因:" + data.message, "error");
        }, error: function (xhr, status, err) {
            console.log("安灯发起失败," + xhr);
            $.alert("请求失败");
        }, complete: function () {
            $.hideLoading();
        }
    });
}
//获取产品首工位投入数量 InvCode:产品编码，TaskID:任务编号
function GetInputQuantity(InvCode, TaskID, callback) {
    $.ajax({
        type: "post",
        url: LocalConfig.SrvPath + "/jgmes/commonAction!getFristGwQty.action",
        data: { "userCode": LocalUserInfo.GetUserInfo().UserCode, "mac": LocalUserInfo.GetUserInfo().Mac, "cpCode": InvCode, "scrwId": TaskID },
        dataType: "json",
        success: function (ret) {
            callback(null, ReturnData(ret));
        }, error: function (xhr, status, err) {
            console.error("获取产品首工位收入数量发生异常，" + err);
            callback(err);
        }
    });
}

//条形码校验
function BarCodeCheck(barCode) {
    var data;
    $.ajax({
        type: "post",
        cache: true,
        async: false,
        url: LocalConfig.SrvPath + "/jgmes/jgmesBarCodeAction!checkBarCodeLx.action",
        data: { "userCode": LocalUserInfo.GetUserInfo().UserCode, "mac": LocalUserInfo.GetUserInfo().Mac, snCode: barCode },
        dataType: "json",
        success: function (ret) {
            data = ReturnData(ret);
        }, error: function (xhr, status, err) {
            console.error("获取条形码物料校验失败," + err);
        }
    });
    return data;
}

//校验是产品码 barCode：条码,invSNRule:校验规则
function IsCheckProductBarCode(barCode, invSNRule) {
    var checkData = BarCodeCheck(barCode);
    if (checkData && checkData.IsSuccess && checkData.Data.IsMaterail == false) {
        return true;
    } else if (Utils.IsTest(invSNRule, currSelf.barCode))
        return true;
    return false;
}

//校验是物料码 barCode:条码
function IsCheckMaterailBarCode(barCode) {
    var data;
    var checkData = BarCodeCheck(barCode);
    if (checkData && checkData.IsSuccess && checkData.Data.IsMaterail) {
        data = checkData.Data.BM;
    } else
        return data;
}

//校验是物料码 barCode:条码，invSNRule：校验规则,fixedCode:固定码
function IsCheckMaterailBarCode(barCode, invSNRule, fixedCode) {
    if (invSNRule && Utils.IsTest(invSNRule, barCode))
        return true;
    else if (barCode === fixedCode)
        return true;
    return false;
}

//操作记录显示条数限制
function MaxOperatingRecord() {
    var data;
    $.ajax({
        type: "post",
        cache: true,
        async: false,
        url: LocalConfig.SrvPath + "/jgmes/commonAction!getOperatingRow.action",
        data: { "userCode": LocalUserInfo.GetUserInfo().UserCode, "mac": LocalUserInfo.GetUserInfo().Mac },
        dataType: "json",
        success: function (ret) {
            data = ReturnData(ret);
        }, error: function (xhr, status, err) {
            console.error("获取失败," + err);
        }
    });
    return data;
}