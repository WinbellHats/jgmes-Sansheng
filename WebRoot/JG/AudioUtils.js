var AudioUtils = {
    AudioObject: "",
    ScanCodeFail: "ScanCodeFail",
    ScanCodeSuccess: "ScanCodeSuccess",
    StartPrint: "StartPrint",
    PrintSuccess: "PrintSuccess",
    PrintFail: "PrintFail",
    ScanCodeSuccessPrint: "ScanCodeSuccessPrint",
    OutStationSuccess: "OutStationSuccess",
    OutStationFail: "OutStationFail",
    ComponentRepeat: "ComponentRepeat",
    NetworkInterruption: "NetworkInterruption",
    SubmitSuccess: "SubmitSuccess",
    SubmitFail: "SubmitFail",

    LeaveWord: "LeaveWord",
    UnknowBarcode: "UnknowBarcode",
    SweepCodeEntry: "SweepCodeEntry",
    SaveSuccess: "SaveSuccess",
    PalletBarcodeEmpty: "PalletBarcodeEmpty",
    OuterBarcodeEmpty: "OuterBarcodeEmpty",
    NotFind: "NotFind",
    SelectDeleted: "SelectDeleted",
    Cartons: "Cartons",
    BarcodeExists: "BarcodeExists",
    ErrorMessage: "ErrorMessage",
    NoVerificationRules: "NoVerificationRules",
    PalletBarcode: "PalletBarcode",
    PalletBarcodeExists: "PalletBarcodeExists",
    ContainerCodeEmpty: "ContainerCodeEmpty",
    AlarmSound: "AlarmSound",

    Play: function (AudioType) {
        if (!this.AudioObject) {
            this.AudioObject = "<audio id=\"bgAudio\" controls=\"controls\" hidden=\"true\" />";
            $("#MainPage").append(this.AudioObject);
        }
        var myAuto = document.getElementById("bgAudio");
        switch (AudioType) {
            case this.ScanCodeFail: //扫码失败
                myAuto.src = "/JG/Audio/ScanCodeFail_Male.mp3";
                myAuto.play();
                break;
            case this.ScanCodeSuccess: //扫码成功
                myAuto.src = "/JG/Audio/ScanCodeSuccess_Male.mp3";
                myAuto.play();
                break;
            case this.StartPrint: //开始打印
                myAuto.src = "/JG/Audio/StartPrint_Male.mp3";
                myAuto.play();
                break;
            case this.PrintSuccess: //打印成功
                myAuto.src = "/JG/Audio/PrintSuccess_Male.mp3";
                myAuto.play();
                break;
            case this.PrintFail: //打印失败
                myAuto.src = "/JG/Audio/PrintFail_Male.mp3";
                myAuto.play();
                break;
            case this.ScanCodeSuccessPrint: //扫码成功开始打印
                myAuto.src = "/JG/Audio/ScanCodeSuccessPrint_Male.mp3";
                myAuto.play();
                break;
            case this.OutStationSuccess://过站成功
                myAuto.src = "/JG/Audio/OutStationSuccess_Male.mp3";
                myAuto.play();
                break;
            case this.OutStationFail: //过站失败
                myAuto.src = "/JG/Audio/OutStationFail_Male.mp3";
                myAuto.play();
                break;
            case this.ComponentRepeat: //部件重复
                myAuto.src = "/JG/Audio/ComponentRepeat_Male.mp3";
                myAuto.play();
                break;
            case this.NetworkInterruption: //网络中断
                myAuto.src = "/JG/Audio/NetworkInterruption_Male.mp3";
                myAuto.play();
                break;
            case this.SubmitSuccess: //提交成功
                myAuto.src = "/JG/Audio/SubmitSuccess_Male.mp3";
                myAuto.play();
                break;
            case this.SubmitFail: //提交失败
                myAuto.src = "/JG/Audio/SubmitFail_Male.mp3";
                myAuto.play();
                break;
            case this.LeaveWord: //已离岗不用再刷卡
                myAuto.src = "/JG/Audio/SwipingCard_Male.mp3";
                myAuto.play();
                break;
            case this.UnknowBarcode: //未知条码，请检查
                myAuto.src = "/JG/Audio/UnknowBarcode_Male.mp3";
                myAuto.play();
                break;
            case this.SweepCodeEntry: //请扫码或录入
                myAuto.src = "/JG/Audio/SweepCodeEntry_Male.mp3";
                myAuto.play();
                break;
            case this.SaveSuccess: //保存成功
                myAuto.src = "/JG/Audio/SaveSuccess_Male.mp3";
                myAuto.play();
                break;
            case this.PalletBarcodeEmpty: //栈板条码为空，不能进行尾数保存
                //myAuto.src = "/JG/Audio/PalletBarcodeEmpty_Male.mp3";
            	myAuto.src = "/JG/Audio/AlarmSound.mp3";
                myAuto.play();
                break;
            case this.OuterBarcodeEmpty: //外箱条码为空，不能进行尾数保存
                //myAuto.src = "/JG/Audio/OuterBarcodeEmpty_Male.mp3";
            	myAuto.src = "/JG/Audio/AlarmSound.mp3";
                myAuto.play();
                break;
            case this.NotFind: //未找到相关信息
                //myAuto.src = "/JG/Audio/NotFind_Male.mp3";
            	myAuto.src = "/JG/Audio/AlarmSound.mp3";
                myAuto.play();
                break;
            case this.SelectDeleted: //请选择您要删除的数据
                //myAuto.src = "/JG/Audio/SelectDeleted_Male.mp3";
            	myAuto.src = "/JG/Audio/AlarmSound.mp3";
                myAuto.play();
                break;
            case this.Cartons: //外箱条码箱数已达到应装箱数
                //myAuto.src = "/JG/Audio/Cartons_Male.mp3";
            	myAuto.src = "/JG/Audio/AlarmSound.mp3";
                myAuto.play();
                break;
            case this.BarcodeExists: //此外箱条码已存在
                //myAuto.src = "/JG/Audio/BarcodeExists_Male.mp3";
            	myAuto.src = "/JG/Audio/AlarmSound.mp3";
                myAuto.play();
                break;
            case this.ErrorMessage: //有错误信息,请检查
                //myAuto.src = "/JG/Audio/ErrorMessage_Male.mp3";
            	myAuto.src = "/JG/Audio/AlarmSound.mp3";
                myAuto.play();
                break;
            case this.NoVerificationRules: //栈板条码无验证规则,请维护！
                //myAuto.src = "/JG/Audio/NoVerificationRules_Male.mp3";
            	myAuto.src = "/JG/Audio/AlarmSound.mp3";
                myAuto.play();
                break;
            case this.PalletBarcode: //栈板条码数量已达到应装数量
                //myAuto.src = "/JG/Audio/PalletBarcode_Male.mp3";
            	myAuto.src = "/JG/Audio/AlarmSound.mp3";
                myAuto.play();
                break;
            case this.PalletBarcodeExists: //此栈板条码已存在
                //myAuto.src = "/JG/Audio/PalletBarcodeExists_Male.mp3";
            	myAuto.src = "/JG/Audio/AlarmSound.mp3";
                myAuto.play();
                break;
            case this.ContainerCodeEmpty: //货柜编码为空，不能进行尾数保存
                //myAuto.src = "/JG/Audio/ContainerCodeEmpty_Male.mp3";
            	myAuto.src = "/JG/Audio/AlarmSound.mp3";
                myAuto.play();
                break;
            case this.AlarmSound: //警示提示音
                //myAuto.src = "/JG/Audio/ContainerCodeEmpty_Male.mp3";
            	myAuto.src = "/JG/Audio/AlarmSound.mp3";
                myAuto.play();
                break;

        }
    }
}