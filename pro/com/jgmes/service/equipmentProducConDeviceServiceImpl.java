package com.jgmes.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * �豸�����Խӷ�����
 * @author liuc
 * @version 2019-04-23 11:20:05
 */
public class equipmentProducConDeviceServiceImpl implements equipmentProducConDeviceService  {

	private static final Logger logger = LoggerFactory.getLogger(equipmentProducConDeviceServiceImpl.class);
	
	public void load(){
		System.out.println("hello serviceimpl");
	}

	@Override
	public String getDeviceCount(String id, String count) {
		return null;
	}
}