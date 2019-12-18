package com.jgmes.service;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;
//import org.apache.log4j.Logger;
import com.je.core.security.SecurityUserHolder;
import com.je.core.service.PCDynaServiceTemplate;
import com.je.core.service.PCServiceTemplate;
import com.je.core.util.SpringContextHolder;
import com.je.core.util.bean.DynaBean;
import com.je.rbac.service.UserManager;
import com.je.rbac.model.Department;
import com.je.rbac.model.EndUser;
/**
 * 工单列表业务层实现类 JGMES_PLAN_GDLB
 * @author cj
 * @version 2019-05-18 17:19:45
 */
@Component("jgmesWorksheetService")
public class JgmesWorksheetServiceImpl implements JgmesWorksheetService  {

	/**��̬Bean(DynaBean)�ķ����*/
	private PCDynaServiceTemplate serviceTemplate;
	/**ʵ��Bean���������,��Ҫ����SQL*/
	private PCServiceTemplate pcServiceTemplate;
	/**�û������*/
	private UserManager userManager;
	
	public void load(){
		System.out.println("hello serviceimpl");
	}
	

	/**
	 * 数据更新，并同步数据
	 * @param worksheetDynaBean
	 */
	@Override
	public void updateDate(DynaBean worksheetDynaBean) {
		System.out.println("数据同步");
		//需同步的表：
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * ��ȡ��¼�û�
	 * @return
	 */
	public EndUser getCurrentUser() {
		// TODO Auto-generated method stub
		return SecurityUserHolder.getCurrentUser();
	}
	/**
	 * ��ȡ��¼�û����ڲ���
	 * @return
	 */
	public Department getCurrentDept() {
		// TODO Auto-generated method stub
		return SecurityUserHolder.getCurrentUserDept();
	}
	@Resource(name="PCDynaServiceTemplate")
	public void setServiceTemplate(PCDynaServiceTemplate serviceTemplate) {
		this.serviceTemplate = serviceTemplate;
	}
	@Resource(name="PCServiceTemplateImpl")
	public void setPcServiceTemplate(PCServiceTemplate pcServiceTemplate) {
		this.pcServiceTemplate = pcServiceTemplate;
	}
	@Resource(name="userManager")
	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}




}