package com.jgmes.action;

import com.je.core.action.DynaAction;
import com.jgmes.service.JgmesClockInService;
import com.jgmes.util.JgmesCommon;
import com.jgmes.util.JgmesResult;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * 刷卡功能
 * @author liuc
 * @version 2019-05-10 10:39:40
	 * @see /jgmes/jgmesClockInAction!load.action
 */
@Component("jgmesClockInAction")
@Scope("prototype")
public class JgmesClockInAction extends DynaAction  {

	private static final long serialVersionUID = 1L;

	private JgmesClockInService jgmesClockInService;

	public JgmesClockInService getJgmesClockInService() {
		return jgmesClockInService;
	}

	@Resource(name = "jgmesClockInService")
	public void setJgmesClockInService(JgmesClockInService jgmesClockInService) {
		this.jgmesClockInService = jgmesClockInService;
	}

	@Override
	public void load(){
		toWrite("hello Action");
	}

	public void doSaveSkkqb(){
		// MAC地址
		String mac = request.getParameter("mac");
		// 用户编码  必填
		String userCode = request.getParameter("userCode");
		// 工号
		String jobNum = request.getParameter("jobNum");
		// 刷卡状态
		String skStatus = request.getParameter("skStatus");
		// 产线编码
		String cxCode = request.getParameter("cxCode");
		// 生产任务单号
		String scrwNo = request.getParameter("scrwNo");
		// 转岗产线
		String zgCxCode = request.getParameter("zgCxCode");

		JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {
			ret = jgmesClockInService.doSaveOrUpdateSkkqb(jobNum, skStatus, cxCode, scrwNo,zgCxCode);
		}
		toWrite(jsonBuilder.toJson(ret));
	}


	/**
	 * 校验用户合法性，不合法直接提示。
	 * @param userCode
	 * @param mac
	 * @return
	 */
	private JgmesResult<String> doCheck(String userCode,String mac) {
		JgmesResult<String> result = new JgmesCommon(request, serviceTemplate).doCheckRes(mac, userCode);
		if (!result.IsSuccess) {
			toWrite(jsonBuilder.toJson(result));
		}
		return result;
	}

}