package com.demo.service;

import com.je.core.util.bean.DynaBean;

/**
 * 工单列表操作
 * @author cj
 * @version 2019-04-02 20:23:07
 */
public interface WorkorderService {
	
	public void load();
	
	/**
	 * 工单导入
	 * @param order
	 * @return
	 */
	public DynaBean implGDOrder(DynaBean order);
}