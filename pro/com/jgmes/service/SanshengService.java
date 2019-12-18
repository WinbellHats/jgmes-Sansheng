package com.jgmes.service;

import com.je.core.util.bean.DynaBean;
import com.jgmes.util.JgmesResult;

import java.util.HashMap;

/**
 * ???????Service
 * @author admin
 * @version 2019-11-13 20:53:43
 */
public interface SanshengService {
	
	 void load();

	 DynaBean importProductData(DynaBean order);

	JgmesResult<HashMap> SumitScheduling(String sumitList,String cxCode);

	JgmesResult<HashMap> delScrw(String scrwId);

	JgmesResult<HashMap> startScrw(String taskcode);

	JgmesResult<HashMap> startScrwByBarCode(String barCode,String ProductionTaskCode);

	JgmesResult<HashMap> bindingBarCode(String barCode,String ProductionTaskCode);

    void synchronizationOrder();
}