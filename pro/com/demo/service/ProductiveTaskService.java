package com.demo.service;

import com.je.core.util.bean.DynaBean;

/**
 * 生产任务数据导入
 * @author cj
 * @version 2019-04-02 10:58:28
 */
public interface ProductiveTaskService {
	
	public void load();
	
	/**
	 * 生产任务导入
	 * @param order 生产任务数据
	 * @return
	 */
	public DynaBean implSCRWOrder(DynaBean order);
	
	/**
	 * 乐惠生产任务导入
	 * @param order 生产任务数据
	 * @return
	 */
	public DynaBean implSCRWOrderLH(DynaBean order);
	
}