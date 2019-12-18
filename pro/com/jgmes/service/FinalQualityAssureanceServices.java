package com.jgmes.service;

import com.jgmes.util.JgmesResult;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

/**
 * FQA services
 * @author admin
 * @version 2019-06-04 11:44:52
 */
public interface FinalQualityAssureanceServices {
	
	public void load();
	public JgmesResult<List<HashMap>> saveFQAData(String barCode,String OuterBarCode,String powerLineCode,String BoxCode);
	public JgmesResult<List<HashMap>> getBarcodeData( String barCode, String OuterBarCode, String powerLineCode, String BoxCode,String ssh);
	public JgmesResult<List<HashMap>> getSNBarcodeData(String barCode,String ssh);
	public JgmesResult<List<HashMap>> save(String jsonStr, String jsonStrDetail, HttpServletRequest request);
}