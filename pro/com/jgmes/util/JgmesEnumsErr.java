package com.jgmes.util;

public enum JgmesEnumsErr {
	LoginErr("�û���½ʧЧ���½���Ϸ���", 10001),
	CheckErrRepeat("�ظ�ɨ��" ,10101),
	CheckErr("�Ǳ������Ʒ�����飡" ,10102),
	CheckErrBefor("ǰ����©ɨ�����飡" ,10103),
	CheckErrAfter("��������ɨ�����飡" ,10104),
	CheckErrUseless("��Ʒ�ѱ��ϣ����飡" ,10105),
	CheckErrRepeatInWxz("�ظ�ɨ�裬�ѽ���ά��վ�����飡" ,10106),
	CheckErrWlmExist("����������ʹ�ã����飡" ,10107),
	CheckErrNoThisCode("δ֪������ţ����飡" ,10108),
	CheckErrNoFrontCode("ǰ����δ��ɣ������һ�α���û�м��񣩻��ѽ��뷵��վ�����飡",10109),
	
	CheckErrNoMatch("��Ʒ�����벻ƥ�䣬���飡" ,10102),

	CheckErrScrwNoMatch("�������������벻ƥ�䣬���飡" ,10110),
	
	CheckErrSCRWNoMatch("��������������ƥ�䣬���飡" ,10109),

	CheckErrRepeatInOther("�������ѽ��뷵��վ�����飡",10110),//������վɨ�赽�ڷ���վ�е�����
	CheckErrChange("����" ,10199);



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
