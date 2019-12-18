package com.jgmes.service;

import com.je.core.util.bean.DynaBean;

/**
 * 工单列表业务层 JGMES_PLAN_GDLB
 * @author cj
 * @version 2019-05-18 17:19:45
 */
public interface JgmesWorksheetService {
	
	public void load();
	
	/**
	 * 数据更新，并同步数据
	 * @param worksheetDynaBean
	 */
	public void updateDate(DynaBean worksheetDynaBean);
}