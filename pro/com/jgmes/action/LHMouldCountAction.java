package com.jgmes.action;

import com.je.core.action.DynaAction;
import com.jgmes.service.DeviceProducConService;
import com.jgmes.service.DeviceProducConServiceImpl;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
/**
 * 
 * @author admin
 * @version 2019-06-12 15:34:12
 * @see /jgmes/lHMouldCountAction!load.action
 */
@Component("lHMouldCountAction")
@Scope("prototype")
public class LHMouldCountAction extends DynaAction  {
	
	private static final long serialVersionUID = 1L;
	
	public void load(){
		toWrite("hello Action");
	}
	DeviceProducConService dpsc = new DeviceProducConServiceImpl();

	/**
	 * 	保养，后台操作方法
	 */
	public void upKeep(){
		String id = request.getParameter("id");//模具资料id集合
		DeviceProducConServiceImpl.ResultBoolean resultBoolean = dpsc.upKeep(id,request);
		toWrite(jsonBuilder.toJson(resultBoolean));
	}

}