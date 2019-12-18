package com.jgmes.action;

import com.je.core.action.DynaAction;
import com.je.core.util.bean.DynaBean;
import com.je.table.exception.PCExcuteException;
import com.jgmes.service.JgmesWorksheetService;
import com.jgmes.util.JgmesCommon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.ParseException;

//import org.apache.log4j.Logger;
/**
 * 工单列表控制器层 JGMES_PLAN_GDLB
 * @author cj
 * @version 2019-05-18 17:18:15
 * @see /jgmes/jgmesWorksheetAction!load.action
 */
@Component("jgmesWorksheetAction")
@Scope("prototype")
public class JgmesWorksheetAction extends DynaAction  {
	
	private static final long serialVersionUID = 1L;
	
	@Autowired
	private JgmesWorksheetService jgmesWorksheetService;
	
	@Override
	public void load(){
		toWrite("hello Action");
	}
	
	/**
	 * 工单数据更新
	 */
	public void updateSyn() {
		System.out.println("工单数据同步");
		DynaBean worksheetDynaBean=new DynaBean("JGMES_PLAN_GDLB", true);
		String updateDate=request.getParameter("updateData");
		System.out.println("数据："+updateDate);
		JgmesCommon jgmesCommon=new JgmesCommon(request, serviceTemplate);
		try {
			worksheetDynaBean=jgmesCommon.updateDynaBeanByJsonStr(worksheetDynaBean, updateDate);
			jgmesWorksheetService.updateDate(worksheetDynaBean);
		} catch (ParseException e) {
			toWrite(jsonBuilder.returnFailureJson("\"后台出现错误" + e.getMessage() + "\""));
			e.printStackTrace();
		}catch (PCExcuteException e) {
			toWrite(jsonBuilder.returnFailureJson("\"后台出现错误" + e.getMessage() + "\""));
			e.printStackTrace();
		}
		
	}
	
}