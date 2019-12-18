package com.jgmes.action;

import java.text.ParseException;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.je.core.action.DynaAction;
import com.je.core.util.JEUUID;
import com.je.core.util.bean.DynaBean;
import com.jgmes.util.JgmesCommon;
import com.jgmes.util.JgmesResult;

import net.sf.json.JSONObject;
/**
 * 
 * @author liuc
 * @version 2019-02-22 21:22:45
 * @see /jgmes/jgmesLogAction!load.action
 */
@Component("jgmesLogAction")
@Scope("prototype")
public class JgmesLogAction extends DynaAction  {
	
	private static final long serialVersionUID = 1L;
	
	public void load(){
		toWrite("hello Action");
	}
	
	/*
	 * 保存日志 请求地址：/jgmes/ jgmesLogAction!doJsonSave.action 请求方法：POST
	 */

	public void doJsonSave() {
		String jsonStr = request.getParameter("jsonStr");
		// String userCode = request.getParameter("userCode");// 非必填，为空则获取当前登陆用户
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
		JgmesResult<JSONObject> jgmesResult = new JgmesResult<JSONObject>();
		try {
			DynaBean logDynaBean = jgmesCommon.getDynaBeanByJsonStr("JGMES_SYS_LOG", jsonStr);
			logDynaBean.set("JGMES_SYS_LOG_ID", JEUUID.uuid());
			serviceTemplate.insert(logDynaBean);
			jgmesResult.IsSuccess = true;
		} catch (ParseException e) {
			jgmesResult.IsSuccess = false;
		}

		toWrite(jsonBuilder.toJson(jgmesResult));

	}
	
}