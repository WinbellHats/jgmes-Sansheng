package com.jgmes.service;

import com.je.core.util.bean.DynaBean;
import com.jgmes.util.JgmesResult;

import java.util.HashMap;

/**
 * 电子秤业务的service层
 * @author liuc
 * @version 2019-05-22 10:32:19
 */
public interface JgmesElectronicScaleService {

	public void load();

	/**
	 * 根据生产任务单号获取生产任务
	 * @param rwNo 任务单号
	 */
	public DynaBean getScrwByRwNo(String rwNo);

	/**
	 * 删除报工数据，并回写相关信息
	 * @param bgSjId 报工数据主表
	 * @param bgSjZbId 报工数据子表
	 * @return
	 */
	public JgmesResult<HashMap> deleteBgSj(String bgSjId, String bgSjZbId);

}