//登录用户及权限信息
var LocalUserInfo = {
    UserCodeKey: "UserCode",
    UserNameKey: "UserName",
    DepIDKey: "DepID",
    DepNameKey: "DepName",
    ProductionLineCodeKey: "ProductionLineCode",
    ProductionLineNameKey: "ProductionLineName",
    StationIDKey: "StationID",
    StationNameKey: "StationName",  //工位名称
    MacKey: "LocalMac",
    AvatarKey: "Avatar",
    Init: function (UserCode, UserName, DepID, DepName, ProductionLineCode, ProductionLineName, StationID, StationName, Mac, Avatar) {
        localStorage.setItem(this.UserCodeKey, UserCode);
        localStorage.setItem(this.UserNameKey, UserName);
        //alert(UserName);
        localStorage.setItem(this.DepIDKey, DepID);
        localStorage.setItem(this.DepNameKey, DepName);
        localStorage.setItem(this.ProductionLineCodeKey, ProductionLineCode);
        localStorage.setItem(this.ProductionLineNameKey, ProductionLineName);
        localStorage.setItem(this.StationIDKey, StationID);
        localStorage.setItem(this.StationNameKey, StationName);
        localStorage.setItem(this.MacKey, Mac);
        if (Avatar)
            localStorage.setItem(this.AvatarKey, LocalConfig.SrvPath + Avatar);
    },
    GetUserInfo: function () {
        return {
            UserCode: localStorage.getItem(this.UserCodeKey),
            UserName: localStorage.getItem(this.UserNameKey),
            DepID: localStorage.getItem(this.DepIDKey),
            DepName: localStorage.getItem(this.DepNameKey),
            ProductionLineCode: localStorage.getItem(this.ProductionLineCodeKey),
            ProductionLineName: localStorage.getItem(this.ProductionLineNameKey),
            StationID: localStorage.getItem(this.StationIDKey),
            StationName: localStorage.getItem(this.StationNameKey),
            Mac: localStorage.getItem(this.MacKey),
            Avatar: localStorage.getItem(this.AvatarKey),
        };
    },
    CheckLogin: function () {
        if (!localStorage.getItem(this.UserCodeKey)) {
            var returnUrl = "";
            if (document.location.href)
                returnUrl = "?returnUrl=" + document.location.href;
            window.location.href = "/JG/Auth/MobileLogin.html" + returnUrl;
        }
        else
            return true;
    },
    ClearUser: function () {
        localStorage.removeItem(this.UserCodeKey);
        localStorage.removeItem(this.UserNameKey);
        localStorage.removeItem(this.DepIDKey);
        localStorage.removeItem(this.DepNameKey);
        localStorage.removeItem(this.ProductionLineCodeKey);
        localStorage.removeItem(this.ProductionLineNameKey);
        localStorage.removeItem(this.StationIDKey);
        localStorage.removeItem(this.StationNameKey);
        localStorage.removeItem(this.MacKey);
        localStorage.removeItem(this.AvatarKey);
    }
};



