package com.jgmes.util;

public enum JgmesEnumsDic {
	//维修结果，需与数据字典保持一致
	WxjgFinish("完成维修", "WXJG01"),WxjgTeCai("特采" ,"WXJG02"),WxjgUseless("报废" ,"WXJG03")
	//维修状态
	,WxZtToDo("待维修", "WXZT01"),WxZtDoing("维修中" ,"WXZT02"),WxZtPause("暂停维修" ,"WXZT03"),WxZtFinish("维修完成" ,"WXZT04"),WxZtShift("转站" ,"WXZT05")
	//判定结果
	,PdJgPass("合格", "PDJG01"),PdJgUseless("不合格" ,"PDJG02"),PdJgTeDCai("特采" ,"PDJG03")
	//任务状态
	,SCToDo("待生产", "RWZT01"),ScDoing("生产中" ,"RWZT02"),ScFinished("完成生产" ,"RWZT03"),ScPause("暂停" ,"RWZT04")
	//排产状态
	,PCToDo("待排产", "PCZT01"),PCToDoing("排产中" ,"PCZT02"),PCFinished("暂停排产" ,"PCZT03"),PCPause("完成排产" ,"PCZT04")
	//条码类型
	,TMCP("产品条码", "TMLX01"),TMNX("内箱条码" ,"TMLX02"),TMZX("中箱条码" ,"TMLX03"),TMWX("外箱条码" ,"TMLX04"),TMZB("栈板条码" ,"TMLX05"),TMLCK("流程卡条码" ,"TMLX06"),TMGZB("工装板条码" ,"TMLX07"),TMHGB("货柜条码" ,"TMLX09")
	//,TMXZPCM("批次码" ,"TMXZ01"),TMXZWYM("唯一码" ,"TMXZ02")   //这个条码性质和数据字典中的不一样
	,TMXZPCM("批次码" ,"TMXZX01"),TMXZWYM("唯一码" ,"TMXZX02")
	//条码生成方式
	,CreateByTm("按条码规则生成", "BARCODEMODEL01"),CreateByIn("按传入值生成" ,"BARCODEMODEL02")
	//产线段
	,CXDQUD("全段","CXD01"),CXDQD("前段","CXD02"),CXDHD("后段","CXD03");

	private String value;
	private String key;

	private JgmesEnumsDic(String value, String key) {
	this.value = value;
	this.key = key;
	}

	public String getValue() {
	return value;
	}

	public void setValue(String value) {
	this.value = value;
	}

	public String getKey() {
	return key;
	}

	public void setKey(String key) {
	this.key = key;
	}
}
