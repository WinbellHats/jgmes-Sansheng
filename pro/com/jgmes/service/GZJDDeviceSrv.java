package com.jgmes.service;

import com.jgmes.service.GZJDDeviceSrvImpl.*;
/**
 * CLIO ��������
 * @author admin
 * @version 2019-05-18 09:28:41
 */
public interface GZJDDeviceSrv {
	
	public void load();
	public ResultBoolean SaveClioTestData(String barCode,String TestResults);

}