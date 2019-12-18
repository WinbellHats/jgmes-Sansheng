package com.jgmes.service;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.je.core.facade.extjs.JsonBuilder;
//import org.apache.log4j.Logger;
import com.je.core.security.SecurityUserHolder;
import com.je.core.service.PCDynaServiceTemplate;
import com.je.core.service.PCServiceTemplate;
import com.je.core.util.bean.DynaBean;
import com.je.rbac.service.UserManager;
import com.jgmes.util.JgmesCommon;

import jxl.common.Logger;
import net.sf.json.JSONObject;

import com.je.rbac.model.Department;
import com.je.rbac.model.EndUser;
/**
 * ͨ��������
 * @author fm
 * @version 2018-12-21 15:53:29
 */
@Component("jgmesComService")
public class JgmesComServiceImpl implements JgmesComService  {

	private static Logger logger = Logger.getLogger(JgmesComServiceImpl.class);
	JsonBuilder jsonBuilder=JsonBuilder.getInstance();
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


	@Override
	public void doJsonSave(DynaBean mainDynaBean,List<DynaBean> detailDynaBean) {
		// TODO Auto-generated method stub
//		HttpServletRequest request = null; 
//		JgmesCommon jgmesCommon = new JgmesCommon(request,serviceTemplate);
//
//		String key ="";
//		String value ="";
//		String jsonString = request.getParameter("jsonStr");
//		String tabCode ="JGMES_PB_BGSJ";// ((String[])requestParams.get("tabCode"))[0];
//		String userCode="";
//		String userName="";
//		String bGSJ_GZSJ="";//过站时间
//        System.out.println("jsonString:"+jsonString);
//	
//		DynaBean bgsj = new DynaBean();
//        bgsj.setStr(beanUtils.KEY_TABLE_CODE, tabCode);
//        
//        //获取当前登陆用户
//         
// 		userCode =jgmesCommon.jgmesUser.getCurrentUserCode();
//		userName = jgmesCommon.jgmesUser.getCurrentUserName();
//        System.out.println("userCode:"+userCode);
//        System.out.println("userName:"+userName);
//        
//		//将传入的参数转到表中
////		bgsj= (DynaBean) jsonBuilder.fromJson(jsonString, bgsj.getClass());
//		JSONObject  jb1=JSONObject.fromObject(jsonString);
//		Iterator it = jb1.keys();  
//        List<String> keyListstr = new ArrayList<String>();  
//        while(it.hasNext()){  
//            key = (String) it.next();
//            value = jb1.getString(key);
//            bgsj.setStr(key,value);
//        }  
//		
//        DynaBean gw = getCurrentGW();
//        if (gw!=null) {
//			bgsj.setStr("BGSJ_GWBH", gw.getStr("GW_GWBH"));
//			bgsj.setStr("BGSJ_GWMC", gw.getStr("GW_GWMC"));
//			bgsj.setStr("BGSJ_GWID", gw.getStr("JGMES_BASE_GW_ID"));			
//		}
//        
//        DynaBean gx = getCurrentGX(bgsj.getStr("BGSJ_CPBH"));
//        if (gx!=null) {
//			bgsj.setStr("BGSJ_GXID", gx.getStr("GXGL_ID"));
//			bgsj.setStr("BGSJ_GXBH", gx.getStr("GXGL_NUM"));
//			bgsj.setStr("BGSJ_GXMC", gx.getStr("GXGL_NAME"));
//		}
//        
////		//获取当前用户的工位信息
////		DynaBean dlgl = serviceTemplate.selectOne("JGMES_PB_DLGL", " and DLGL_YHBM='"+userCode+"'");
////		if(dlgl!=null)
////		{
////			bgsj.setStr("BGSJ_GWBH", dlgl.getStr("DLGL_GWBH"));
////			bgsj.setStr("BGSJ_GWMC", dlgl.getStr("DLGL_GWMC"));
////			bgsj.setStr("BGSJ_GWID", dlgl.getStr("DLGL_GWID"));
////
////	        
////			//根据工位信息、产品信息获取工序信息
////			DynaBean cPGWGX = serviceTemplate.selectOne("JGMES_BASE_CPGWGX", " and CPGWGX_CPBH='"+bgsj.getStr("BGSJ_CPBH")+"' and JGMES_BASE_GW_ID='"+dlgl.getStr("DLGL_GWID")+"'");
////			if(cPGWGX!=null)
////			{
////				bgsj.setStr("BGSJ_GXID", cPGWGX.getStr("CPGWGX_GXID"));
////		//		bgsj.setStr("BGSJ_GXBH", cPGWGX.getStr("CPGWGX_GXID"));
////				bgsj.setStr("BGSJ_GXMC", cPGWGX.getStr("CPGWGX_GXNAME"));
////			}
////
////		}
// 		
//		//过站时间
//		bGSJ_GZSJ=bgsj.getStr("BGSJ_GZSJ");
//		if (bGSJ_GZSJ==null || bGSJ_GZSJ.isEmpty()) {
//			bgsj.set("BGSJ_GZSJ", jgmesCommon.getCurrentTime());
//		}			
//		
//        jgmesCommon.setDynaBeanInfo(bgsj);
////		serviceTemplate.buildModelCreateInfo(bgsj);
//		serviceTemplate.insert(bgsj);
//		
//		toWrite(jsonBuilder.returnSuccessJson("\"添加成功\""));
	}

	}