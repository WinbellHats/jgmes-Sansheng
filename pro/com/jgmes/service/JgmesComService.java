package com.jgmes.service;

import java.util.List;

import com.je.core.util.bean.DynaBean;

/**
 * ͨ��������
 * @author fm
 * @version 2018-12-21 15:53:29
 */
public interface JgmesComService {
	
	public void load();
	
	public void doJsonSave(DynaBean mainDynaBean,List<DynaBean> detailDynaBean);
}