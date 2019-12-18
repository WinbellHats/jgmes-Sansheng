package com.jgmes.service;

import com.je.core.util.bean.DynaBean;

/**
 * 
 * @author admin
 * @version 2019-04-10 18:12:21
 */
public interface MaterialCodeService {
	
	public void load();

	public DynaBean implOrder(DynaBean order) ;
}