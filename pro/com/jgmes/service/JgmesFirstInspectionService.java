package com.jgmes.service;

import com.jgmes.util.JgmesResult;
import com.jgmes.util.TreeDto;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

/**
 * �׼������service
 * @author liuc
 * @version 2019-04-17 20:57:26
 */
public interface JgmesFirstInspectionService {
	/**
	 * 加载
	 */
	void load();

	/**
	 *获取有效产品检验标准
	 * @param cpbm
	 * @param jylx
	 * @return
	 */
	JgmesResult<List<HashMap>> getEffectiveProductStandard(String cpbm, String jylx);

	/**
	 * 获取有效产品检验标准子表集合
	 * @param id  主键ID
	 * @param sjzbId 首件检验主表ID
	 * @param xjzbId 巡检主表ID
	 * @param rootId  检验项目根目录ID
	 * @return
	 */
	JgmesResult<List<TreeDto>> getEffectiveProductStandardChild(String id,String sjzbId,String xjzbId,String rootId);

	/**
	 * 保存首件检验单的主子表
	 * @param jsonStr
	 * @param jsonStrDetail
	 * @param requestT
	 * @return
	 */
    JgmesResult<String> doSaveFirstInspection(String jsonStr,String jsonStrDetail,String jsonStrRwGl,HttpServletRequest requestT,String userCode);

	/**
	 * 获取检验项目根目录
	 * @param id 检验方案主表ID
	 * @return
	 */
	JgmesResult<List<HashMap>> getInspectionItemRootClassify(String id);


	/**
	 *获取首件检验主表
	 * @param antistop 关键词查询
	 * @return
	 */
	JgmesResult<List<HashMap>> getFirstInspection(String antistop);

	/**
	 *根据首件检验主键ID，来查询相对应的首件检验子表
	 * @param id 首件检验主键ID
	 * @return
	 */
	JgmesResult<List<HashMap>> getFirstInspectionChild(String id);

	/**
	 *获取巡检主表
	 * @param antistop 关键词查询
	 * @return
	 */
	JgmesResult<List<HashMap>> getRoutingInspection(String antistop);

	/**
	 * 获取巡检首检任务单关联表
	 * @param id
	 * @return
	 */
	JgmesResult<List<HashMap>> getRWDXXGLB(String id);
}