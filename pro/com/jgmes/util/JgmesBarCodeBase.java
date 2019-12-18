package com.jgmes.util;

import com.je.core.util.bean.DynaBean;

import java.util.HashMap;
import java.util.List;

public class JgmesBarCodeBase {

	public JgmesBarCodeBase() {
		
	}
	
	public JgmesBarCodeBase(String barCode,JgmesEnumsDic tmXz,JgmesEnumsDic tmlx,String objCode,String objName) {
		BarCode = barCode;
		TmXz =tmXz;
		TmLx =tmlx;
		ObjCode = objCode;
		ObjName = objName;
	}
	
	/**
	 * 条码号
	 */
	public String BarCode;
	
	/**
	 * 是否为物料
	 */
	public boolean IsMaterail;
	
	/**
	 * 条码性质
	 */
	public JgmesEnumsDic TmXz;
	
	/**
	 * 条码类型
	 */
	public JgmesEnumsDic TmLx;
	
	/**
	 * 条码对应的编号
	 */
	public String ObjCode;
	
	/**
	 * 条码对应的名称
	 */
	public String ObjName;

	/**
	 * tmScDynaBean  条码库详细信息
	 *
	 */

	public HashMap tmScDynaBean;

	/**
	 * 获取条码应用规则（处理栈板条码和货柜条码的问题）
	 *
	 */

	public List<HashMap> tmyygzDynaBean;
	
	/**
	 * detailData  详细数据
	 * 
	 */
	
	public DynaBean detailData;
	
	
}
