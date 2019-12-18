package com.jgmes.util;

public enum JgmesEnumsDic {
	//ά�޽�������������ֵ䱣��һ��
	WxjgFinish("���ά��", "WXJG01"),WxjgTeCai("�ز�" ,"WXJG02"),WxjgUseless("����" ,"WXJG03")
	//ά��״̬
	,WxZtToDo("��ά��", "WXZT01"),WxZtDoing("ά����" ,"WXZT02"),WxZtPause("��ͣά��" ,"WXZT03"),WxZtFinish("ά�����" ,"WXZT04"),WxZtShift("תվ" ,"WXZT05")
	//�ж����
	,PdJgPass("�ϸ�", "PDJG01"),PdJgUseless("���ϸ�" ,"PDJG02"),PdJgTeDCai("�ز�" ,"PDJG03")
	//����״̬
	,SCToDo("������", "RWZT01"),ScDoing("������" ,"RWZT02"),ScFinished("�������" ,"RWZT03"),ScPause("��ͣ" ,"RWZT04")
	//�Ų�״̬
	,PCToDo("���Ų�", "PCZT01"),PCToDoing("�Ų���" ,"PCZT02"),PCFinished("��ͣ�Ų�" ,"PCZT03"),PCPause("����Ų�" ,"PCZT04")
	//��������
	,TMCP("��Ʒ����", "TMLX01"),TMNX("��������" ,"TMLX02"),TMZX("��������" ,"TMLX03"),TMWX("��������" ,"TMLX04"),TMZB("ջ������" ,"TMLX05"),TMLCK("���̿�����" ,"TMLX06"),TMGZB("��װ������" ,"TMLX07"),TMHGB("��������" ,"TMLX09")
	//,TMXZPCM("������" ,"TMXZ01"),TMXZWYM("Ψһ��" ,"TMXZ02")   //����������ʺ������ֵ��еĲ�һ��
	,TMXZPCM("������" ,"TMXZX01"),TMXZWYM("Ψһ��" ,"TMXZX02")
	//�������ɷ�ʽ
	,CreateByTm("�������������", "BARCODEMODEL01"),CreateByIn("������ֵ����" ,"BARCODEMODEL02")
	//���߶�
	,CXDQUD("ȫ��","CXD01"),CXDQD("ǰ��","CXD02"),CXDHD("���","CXD03");

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
