package com.jgmes.action;

import com.je.core.action.DynaAction;
import com.je.core.util.bean.DynaBean;
import com.jgmes.service.JgmesElectronicScaleService;
import com.jgmes.util.JgmesCommon;
import com.jgmes.util.JgmesResult;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * 处理电子秤的业务
 * @author liuc
 * @version 2019-05-22 10:30:25
 * @see /jgmes/jgmesElectronicScaleAction!load.action
 */
@Component("jgmesElectronicScaleAction")
@Scope("prototype")
public class JgmesElectronicScaleAction extends DynaAction  {

	private static final long serialVersionUID = 1L;

	private JgmesElectronicScaleService jgmesElectronicScaleService;

	public JgmesElectronicScaleService getJgmesElectronicScaleService() {
		return jgmesElectronicScaleService;
	}

	@Resource(name="jgmesElectronicScaleService")
	public void setJgmesElectronicScaleService(JgmesElectronicScaleService jgmesElectronicScaleService) {
		this.jgmesElectronicScaleService = jgmesElectronicScaleService;
	}

	public void load(){
		toWrite("hello Action");
	}

	/**
	 * 根据任务单号获取生产任务
	 * rwNo 任务单号
	 * mac mac地址
	 * userCode 用户编码
	 * @see /jgmes/jgmesElectronicScaleAction!getScrwByRwNo.action
	 */
	public void getScrwByRwNo(){
		// MAC地址
		String mac = request.getParameter("mac");
		// 用户编码  必填
		String userCode = request.getParameter("userCode");
		// 任务单号
		String rwNo = request.getParameter("rwNo");

		JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
		String jsonStr = "";
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {
			if(rwNo!=null&&!"".equals(rwNo)){
				DynaBean scrwDynaBean = jgmesElectronicScaleService.getScrwByRwNo(rwNo);
				if(scrwDynaBean!=null){
					ret.Data = scrwDynaBean.getValues();
				}
			}else{
				ret.setMessage("未获取到生产任务单号！");
			}
		}
		jsonStr = jsonBuilder.toJson(ret);
		toWrite(jsonStr);
	}

	/**
	 * 删除报工数据
	 * mac mac地址
	 * userCode 用户编码
	 * bgSjId 报工数据主表主键ID
	 * bgSjZbId 报工数据子表主键ID
	 * @see /jgmes/jgmesElectronicScaleAction!deleteBgSj.action
	 */
	public void deleteBgSj(){
		// MAC地址
		String mac = request.getParameter("mac");
		// 用户编码  必填
		String userCode = request.getParameter("userCode");
		// 报工数据主表主键ID
		String bgSjId = request.getParameter("bgSjId");
		// 报工数据子表主键ID
		String bgSjZbId = request.getParameter("bgSjZbId");

		JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
		String jsonStr = "";
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {
			ret = jgmesElectronicScaleService.deleteBgSj(bgSjId,bgSjZbId);
		}
		jsonStr = jsonBuilder.toJson(ret);
		toWrite(jsonStr);
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