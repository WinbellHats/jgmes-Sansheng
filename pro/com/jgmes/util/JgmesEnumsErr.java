package com.jgmes.util;

public enum JgmesEnumsErr {
	LoginErr("用户登陆失效或登陆不合法！", 10001),
	CheckErrRepeat("重复扫描" ,10101),
	CheckErr("非本工序产品，请检查！" ,10102),
	CheckErrBefor("前工序漏扫，请检查！" ,10103),
	CheckErrAfter("本工序已扫，请检查！" ,10104),
	CheckErrUseless("产品已报废，请检查！" ,10105),
	CheckErrRepeatInWxz("重复扫描，已进入维修站，请检查！" ,10106),
	CheckErrWlmExist("该物料码已使用，请检查！" ,10107),
	CheckErrNoThisCode("未知的条码号，请检查！" ,10108),
	CheckErrNoFrontCode("前工序未完成（即最后一次报工没有及格）或已进入返修站，请检查！",10109),
	
	CheckErrNoMatch("产品与条码不匹配，请检查！" ,10102),

	CheckErrScrwNoMatch("生产任务与条码不匹配，请检查！" ,10110),
	
	CheckErrSCRWNoMatch("条码与生产任务不匹配，请检查！" ,10109),

	CheckErrRepeatInOther("该条码已进入返修站，请检查！",10110),//其他工站扫描到在返修站中的条码
	CheckErrChange("换产" ,10199);



	private String value;
	private int key;

	private JgmesEnumsErr(String value, int key) {
	this.value = value;
	this.key = key;
	}

	public String getValue() {
	return value;
	}

	public void setValue(String value) {
	this.value = value;
	}

	public int getKey() {
	return key;
	}

	public void setKey(int key) {
	this.key = key;
	}
}
