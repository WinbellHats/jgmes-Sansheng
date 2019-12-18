package com.jgmes.service;

import com.je.core.util.bean.DynaBean;

/**
 * �ֻݰ���ģ�鵼��
 * @author ljs
 * @version 2019-06-28 10:10:52
 */
public interface lhImportService {
	
	public void load();
	/**
	 * 机台预警人员表导入
	 * @param order
	 * @return
	 */
	public DynaBean earlyWarningImport(DynaBean order);
}