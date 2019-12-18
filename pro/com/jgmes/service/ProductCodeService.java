package com.jgmes.service;

import com.je.core.util.bean.DynaBean;

/**
 * 
 * @author ljs
 * @version 2019-04-29 11:43:53
 */
public interface ProductCodeService {
	
	public void load();

	public DynaBean implOrder(DynaBean order) ;
}