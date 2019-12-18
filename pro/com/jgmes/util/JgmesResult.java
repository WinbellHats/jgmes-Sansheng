package com.jgmes.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.je.core.util.bean.DynaBean;
import com.zeroturnaround.javarebel.jE;

import net.sf.json.JSONObject;

/*
 * 结果返回类
 */
public class JgmesResult<T> {
	
	public JgmesResult()
	{
		this.IsSuccess=true;
	}
	public boolean IsSuccess;
	public T Data;
	private String Message;
	public String getMessage() {
		return Message;
	}
	public void setMessage(String message) {
		this.Message = message;
		this.IsSuccess=false;
	}
	public Integer ErrorCode; 
	
	public Long TotalCount;
	
	//设置定义好的错误
	public void setErrorDic(JgmesEnumsErr eJgmesEnumsErr) {
		if (eJgmesEnumsErr != null) {
			setMessage(eJgmesEnumsErr.getValue());
			this.ErrorCode = eJgmesEnumsErr.getKey();
		}
	}

	//设置定义好的错误
	public void setErrorDicbyProperties(String errorCode) {
		if (errorCode != null&&!"".equals(errorCode)) {
			setMessage(PropertyUtil.getProperty(errorCode));
			this.ErrorCode = Integer.valueOf(errorCode);
		}
	}
	
//	public transient Long TotalCount;//表示不需要序列化
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		com.je.core.facade.extjs.JsonBuilder jsonBuilder =null;
		String reString ="{";
		reString+="\"IsSuccess\":"+IsSuccess;
		reString+=",\"Message\":"+Message;
		reString+=",\"ErrorCode\":"+ErrorCode;
		reString+=",\"TotalCount\":"+TotalCount;
		String jsString="";
		
		if (Data instanceof JSONObject) {
			reString+=",\"Data\":"+Data;
		}
		else if (Data instanceof net.sf.json.JSONArray) {
			jsString =""+ Data;
//			jsString=jsString.replaceAll("{\"values\":", "");
//			jsString=jsString.replaceAll("}}", "}");
			reString+=",\"Data\":"+jsString;
		}
		else if (Data instanceof List<?>) {
			reString+=",\"Data\":"+Data;
		}

		else {
			reString+=",\"Data\":"+Data.toString();
		}
		
		return reString;
	}
	
	
	public List<HashMap> getValues(List<DynaBean> records){
		List<HashMap> dataList = new ArrayList<HashMap>(records.size());
		for(DynaBean bean : records) {
			HashMap values = bean.getValues();
			dataList.add(values);
		}
		return dataList;
		
	}
	
	
}
