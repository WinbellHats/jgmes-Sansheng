package com.jgmes.service;

/**
 * �豸�����Խӷ�����
 * @author liuc
 * @version 2019-04-23 11:20:05
 */
public interface equipmentProducConDeviceService {
	
	public void load();

	/**
	 * 获取设备计数数量
	 * @param id
	 * @param count
	 * @return
	 */
	public String getDeviceCount(String id,String count);
}