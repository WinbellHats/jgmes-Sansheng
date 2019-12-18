package com.jgmes.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.je.core.util.bean.DynaBean;

/**
 * 生产任务操作相关
 * @author cj
 * @version 2019-05-08 20:49:20
 */
public interface JgmesProductionQueueService {
	
	public void load();
	
	/**
	 * 批量删除生产任务
	 * @param tasks 生产任务ids
	 */
	public void deleteAll(String tasks);
	
	
}