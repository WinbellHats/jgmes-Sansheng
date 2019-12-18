package com.demo.service;

import java.util.List;

import com.je.core.entity.extjs.JSONTreeNode;
import com.je.core.util.bean.DynaBean;
import com.je.dd.vo.DicInfoVo;
import com.je.develop.vo.ExcelParamVo;
import com.je.develop.vo.ExcelReturnVo;
import com.je.wf.processVo.WfAssgineSubmitInfo;
import com.je.wf.processVo.WfEventSubmitInfo;

public interface DemoService {
	/**
	 * 流程事件
	 * 
	 * @param eventInfo
	 */
	public void doWfProcessEvent(WfEventSubmitInfo eventInfo);

	/**
	 * 工作流 任务动作 执行事件
	 * 
	 * @param eventInfo
	 */
	public void doWfTaskEvent(WfEventSubmitInfo eventInfo);
	
	public List<DynaBean> doWfAssigneEvent1() ;

	/**
	 * 工作流自定义人员获取事件
	 * 
	 * @param eventInfo
	 */
	public List<DynaBean> doWfAssigneEvent(WfAssgineSubmitInfo eventInfo);
	/**
	 * 自定义字典
	 * @param dicInfoVo
	 * @return
	 */
	public JSONTreeNode getProjectTree(DicInfoVo dicInfoVo);
	/**
	 * 导入数据字典数据
	 * @param paramVo
	 * @return
	 */
	public ExcelReturnVo impDicData(ExcelParamVo paramVo);
}
