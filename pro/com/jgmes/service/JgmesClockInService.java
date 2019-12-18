package com.jgmes.service;

import com.jgmes.util.JgmesResult;

import java.util.HashMap;

/**
 * 打卡功能
 * @author liuc
 * @version 2019-05-10 10:42:34
 */
public interface JgmesClockInService {

	public void load();

	/**
	 *存储或修改刷卡考勤记录表
	 * @param jobNum
	 * @param skStatus
	 * @param cxCode
	 * @param scrwNo
	 * @param zgCxCode 转岗产线
	 */
	public JgmesResult<HashMap> doSaveOrUpdateSkkqb(String jobNum, String skStatus, String cxCode, String scrwNo, String zgCxCode);
}