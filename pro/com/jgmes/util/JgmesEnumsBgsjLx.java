package com.jgmes.util;

public enum JgmesEnumsBgsjLx {
	GwSj("工位数据", 1),FxSj("返修数据" ,2),LySj("蓝牙数据",3);
	private String value;
	private int key;

	private JgmesEnumsBgsjLx(String value, int key) {
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
