package com.jgmes.action;

import com.je.core.action.DynaAction;
import com.jgmes.service.JgmesButtonService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 专门处理按钮的action
 * @author liuc
 * @version 2019-05-05 10:45:36
 * @see /jgmes/jgmesButtonAction!load.action
 */
@Component("jgmesButtonAction")
@Scope("prototype")
public class JgmesButtonAction extends DynaAction  {
	
	private static final long serialVersionUID = 1L;

	private JgmesButtonService jgmesButtonService;

	public JgmesButtonService getJgmesButtonService() {
		return jgmesButtonService;
	}

	@Resource(name="jgmesButtonService")
	public void setJgmesButtonService(JgmesButtonService jgmesButtonService) {
		this.jgmesButtonService = jgmesButtonService;
	}

	@Override
	public void load(){
		toWrite("hello Action");
	}

	/**
	 * 存储条码应用规则模板
	 */
	public void doSaveCPGZTemplate() {
		String[] ids = request.getParameterValues("ids");
		logger.debug("存储条码应用规则模板获取到的ids:" + ids.toString());

		if(ids!=null&&ids.length>0){
			toWrite(jsonBuilder.returnSuccessJson(jgmesButtonService.doSaveCPGZTemplate(ids)));
		}else{
			toWrite(jsonBuilder.returnSuccessJson("\"请选择一条数据！\""));
		}
	}


	/**
	 * 插入产品条码应用规则
	 */
	public void doSaveCpTmYYGG(){
		//条码应用规则的主键ID
		String[] ids = request.getParameterValues("ids");
		//产品编码
		String cpbm = request.getParameter("cpbm");

		toWrite(jsonBuilder.returnSuccessJson(jgmesButtonService.doSaveCpTmYYGG(ids,cpbm)));

	}

	/**
	 * 货柜安排批量插入货柜安排详情
	 */
	public void doSavehgxq(){
		//货柜安排子表主键ID
		String id = request.getParameter("id");
		//栈板的主键ID
		String[] zbid = request.getParameterValues("zbid");

		toWrite(jsonBuilder.returnSuccessJson(jgmesButtonService.doSavehgxq(request,id,zbid)));
	}

	/**
	 * 删错子表信息
	 */
	public void doDeletehgxq(){
		//子表详情主键
		String[] id = request.getParameterValues("id");

		toWrite(jsonBuilder.returnSuccessJson(jgmesButtonService.doDeletehgxq(id)));
	}
	
}