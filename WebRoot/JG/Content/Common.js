/**
 * 工具类
 */
var Utils = {
    /**
     * 是否匹配
     * @param {正则表达式} regRule 
     * @param {值} value 
     */
    IsTest: function (regRule, value) {
        var re = new RegExp(regRule);    //定义正则模式
        if (re.test(value))
            return true;
        else return false;
    },
    SetCookie: function (key, value, exdays) {
        var str = key + "=" + value + ";";
        if (exdays && exdays > 0) {
            var d = new Date();
            d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
            str + "expires=" + d.toGMTString();
        }
        document.cookie = str;
    },
    GetCookie: function (key) {
        var name = key + "=";
        var ca = document.cookie.split(';');
        for (var i = 0; i < ca.length; i++) {
            var c = ca[i].trim();
            if (c.indexOf(name) == 0) return c.substring(name.length, c.length);
        }
        return "";
    },
    /**
     * SN码输入框自动回归焦点
     * @param {周期毫秒,默认10秒} milliseconds 
     */
    AutoBarCodeFocus: function (milliseconds) {
        var milliseconds = milliseconds ? milliseconds : 10000;
        setInterval(function () {
            $("#BarCode").focus();
            LocalConfig.HideSoftInputFromWindow();
        }, milliseconds)
    },
    ToptipWaitTime: {
        Success: 10000, //  成功10秒
        Warning: 20000, //警告20秒
        Error: 40000, //异常40秒
        //Utils.ToptipWaitTime.Success
        //Utils.ToptipWaitTime.Warning
        //Utils.ToptipWaitTime.Error
    },
    TabletsGlobalParam: { //平板全局参数
        IsOpenInputQtyTask: true, //是否开启投入数量
        InputQtyTaskFrequency:300000  //投入数量执行频率(秒)
    }
};
//日期格式化
Date.prototype.Format = function (fmt) {
    var o = {
        "M+": this.getMonth() + 1, //月份 
        "d+": this.getDate(), //日 
        "h+": this.getHours(), //小时 
        "m+": this.getMinutes(), //分 
        "s+": this.getSeconds(), //秒 
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度 
        "S": this.getMilliseconds() //毫秒 
    };
    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
        if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
};


