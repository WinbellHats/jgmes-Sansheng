//本地配置
var LocalConfig = {
    SrvPath: "",//"http://192.168.1.181:8080",
    SysDateTime:"",
    ClientOS: function () {// 获取当前操作系统
        var os;
        //alert("userAgent:" + navigator.userAgent);
        //console.log("userAgent:" + navigator.userAgent);
        if (navigator.userAgent.indexOf('Android') > -1 || navigator.userAgent.indexOf('Linux') > -1) {
            os = 'Android';
        } else if (navigator.userAgent.indexOf('iPhone') > -1) {
            os = 'iOS';
        } else if (navigator.userAgent.indexOf('Windows Phone') > -1) {
            os = 'WP';
        } else if (navigator.userAgent.indexOf("Windows NT") > -1 && navigator.userAgent.indexOf("Chrome/69.0") > -1)
            os = "JGWinChrome";
        else if (navigator.userAgent.indexOf("Windows NT") > -1) {
            os = "Windows NT";
        } else {
            os = 'Others';
        }
        return os;
    },
    ClientOSVersion: function () {  //当前操作系统版本号
        var OSVision = '1.0';
        var u = navigator.userAgent;
        var isAndroid = u.indexOf('Android') > -1 || u.indexOf('Linux') > -1; //Android
        var isIOS = !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/); //ios终端
        if (isAndroid) {
            OSVision = navigator.userAgent.split(';')[1].match(/\d+\.\d+/g)[0];
        }
        if (isIOS) {
            OSVision = navigator.userAgent.split(';')[1].match(/(\d+)_(\d+)_?(\d+)?/)[0];
        }
        return OSVision;
    },
    ClientOSMac: function () {  //客户端MAC
        var mac;
        //alert(this.ClientOS());
        switch (this.ClientOS()) {
            case "Android":
                var currMac = JGAndroid.GetNetMac();
                if (currMac) {
                    mac = currMac;
                } else alert("未能获取Android系统Mac地址");
                break;
            case "JGWinChrome":
                var currMac = JGWin.GetMacAddress();
                if (currMac) {
                    mac = currMac;
                } else alert("未能(Windows)本地系统Mac地址");
                break;
            default:
                mac = "Nothing Mac Address";
        }
        console.log("mac地址:" + mac);
        return mac;
    },
    GetClientOSIP: function () {//获取客户端IP地址
        var IP = "";
        switch (this.ClientOS()) {
            case "Android":
                IP = JGAndroid.GetHostIP();
                break;
            case "JGWinChrome":
                IP = JGWin.GetHostIP();
                break;
            default:
                IP = "127.0.0.1";
        }
        return IP;
    },
    GetServerClientConnectStatus: function () {//服务端连接
        var connectStatus = false;
        switch (this.ClientOS()) {
            case "Android":
                connectStatus = JGAndroid.GetServerConnectStatus();
                break;
                case "JGWinChrome":
                connectStatus = JGWin.GetServerConnectStatus();
                break;
            default:
                connectStatus = true;
        }
        return connectStatus;
    },
    GetServerSysTime: function () {  //获取服务器时间
        var srvSysTime = new Date();
        switch (this.ClientOS()) {
            case "Android":
                srvSysTime = new Date(JGAndroid.GetServerSysTime());
                break;
                case "JGWinChrome":
                srvSysTime = new Date(JGWin.GetServerSysTime());
                break;
            default:
                srvSysTime = new Date();
        }
        return srvSysTime;
    },
    HideSoftInputFromWindow:function(){
        switch (this.ClientOS()) {
            case "Android":
                JGAndroid.hideSoftInputFromWindow();
                break;
            default:
                srvSysTime = new Date();
        }
    }
}