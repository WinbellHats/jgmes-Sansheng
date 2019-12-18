package com.jgmes.service;

import com.je.core.util.bean.DynaBean;

/**
 * �ֻ�ģ�ߵ���Service��
 * @author admin
 * @version 2019-06-18 09:27:54
 */
public interface MouldCountService {
	
	public void load();
	public DynaBean mouldImport(DynaBean order);
}