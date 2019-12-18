package com.jgmes.service;

import com.jgmes.service.GZJDDeviceServiceImpl.*;

/**
 * 
 * @author xxp
 * @version 2019-02-28 16:15:04
 */
public interface GZJDDeviceService {
	
	public ResultBoolean SaveCLIOTestData(String fileName,String file);
	
	public ResultString GetBluetoothAddressBySN(String barCode);
	
	public ResultBoolean SaveBluetoothTestData(String barCode,String BTTestData,String TestResults);
	
}

