package com.jgmes.action;

import javax.annotation.Resource;

//import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.je.core.action.DynaAction;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.DynaBean;
import com.jgmes.service.JgmesProductionQueueService;

import java.io.Serializable;
import java.util.List;
/**
 * 生产任务单相关操作控制层
 * @author cj
 * @version 2019-05-08 20:42:57
 * @see /jgmes/jgmesProductionQueueAction!load.action
 */
@Component("jgmesProductionQueueAction")
@Scope("prototype")
public class JgmesProductionQueueAction extends DynaAction  {
	
	private static final long serialVersionUID = 1L;
	
	public void load(){
		toWrite("hello Action");
	}
	@Resource(name="jgmesProductionQueueService")
	private JgmesProductionQueueService jgProductionQueueService;
	
	/**
	 * 删除生产任务数据同步
	 */
	public void deleteSCRWAll() {
		System.out.println("开始删除");
		String tasks = request.getParameter("tasks");
		
		if (tasks==null||tasks.isEmpty()||"".equals(tasks)) {
			toWrite(jsonBuilder.returnFailureJson("\"请确认是否选中数据。\""));
			return;
		}
		try {
			jgProductionQueueService.deleteAll(tasks);
			toWrite(jsonBuilder.returnSuccessJson("\"删除成功。\""));
		} catch (Exception e) {
			String[] mess = e.getMessage().split(":");
			String msg=null;
			if (mess.length>1) {
				msg=mess[0]+":"+mess[mess.length-1];
			}else {
				msg=mess[0];
			}
			toWrite(jsonBuilder.returnFailureJson("\"错误："+msg+"\""));
		}
		
		
		
	}
}