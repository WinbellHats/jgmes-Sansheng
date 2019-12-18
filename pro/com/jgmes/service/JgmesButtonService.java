package com.jgmes.service;

import javax.servlet.http.HttpServletRequest;

/**
 * 专门处理按钮的service
 * @author liuc
 * @version 2019-05-05 10:47:02
 */
public interface JgmesButtonService {
	
	public void load();

	/**
	 * 存储条码应用规则模板
	 * @param ids
	 * @return
	 */
	public String doSaveCPGZTemplate(String[] ids);


	/**
	 * 插入产品条码应用规则
	 * @param ids
	 * @param cpbm
	 * @return
	 */
	public String doSaveCpTmYYGG(String[] ids,String cpbm);


	/**
	 *货柜安排
	 * @param request
	 * @param id
	 * @param zbid
	 * @return
	 */
	public String doSavehgxq(HttpServletRequest request,String id,String[] zbid);


	/**
	 * 刪除栈板详情
	 * @param id
	 * @return
	 */
	public String doDeletehgxq(String[] id);
}