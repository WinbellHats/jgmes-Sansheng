package com.jgmes.service;

import com.je.core.util.bean.DynaBean;

/**
 * �����ռ��˶�Ӧ��
 * @author admin
 * @version 2019-06-20 15:54:34
 */
public interface emailRecipientsImportService {
	
	public void load();

	public DynaBean emailRecipientsImport(DynaBean order) ;
}