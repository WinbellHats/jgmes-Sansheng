package com.demo.service;

import java.util.List;

import com.je.core.util.bean.DynaBean;
import com.je.phone.vo.DsInfoVo;
import com.je.task.vo.TimedTaskParamsVo;
import com.je.wf.processVo.WfAssgineSubmitInfo;
import com.je.wf.processVo.WfEventSubmitInfo;

public interface LessionService {
	/**
	 * 数据源
	 * 
	 * @param dsInfoVo
	 * @return
	 */
	public String loadWfNum(DsInfoVo dsInfoVo);

	/**
	 * 无参定时任务
	 */
	public void doTask();

	/**
	 * 有参数定时任务
	 * 
	 * @param paramsVo
	 */
	public void doTask(TimedTaskParamsVo paramsVo);

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

	/**
	 * 工作流自定义人员获取事件
	 * 
	 * @param eventInfo
	 */
	public List<DynaBean> doWfAssigneEvent(WfAssgineSubmitInfo eventInfo);

	/**
	 * 动态Bean介绍
	 */
	public void doDynaBean();

	/**
	 * 查询DynaBean
	 */
	public void doSelectDynaBean();

	/**
	 * 保存DynaBean
	 */
	public void doSaveDynaBean();

	/**
	 * 修改DynaBean
	 */
	public void doUpdateDynaBean();

	/**
	 * 删除数据
	 */
	public void doRemoveDynaBean();

	public DynaBean implOrder(DynaBean order);

	public DynaBean implwlOrder(DynaBean order);

	public DynaBean implgdOrder(DynaBean order);
}
