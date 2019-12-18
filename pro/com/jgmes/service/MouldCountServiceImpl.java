package com.jgmes.service;

import com.je.core.security.SecurityUserHolder;
import com.je.core.service.PCDynaServiceTemplate;
import com.je.core.service.PCServiceTemplate;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.DynaBean;
import com.je.rbac.model.Department;
import com.je.rbac.model.EndUser;
import com.je.rbac.service.UserManager;
import com.jgmes.util.JgmesCommon;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * �ֻ�ģ�ߵ���Service��
 * @author admin
 * @version 2019-06-18 09:27:54
 */
@Component("mouldCountService")
public class MouldCountServiceImpl implements MouldCountService  {

	/**��̬Bean(DynaBean)�ķ����*/
	private PCDynaServiceTemplate serviceTemplate;
	/**ʵ��Bean���������,��Ҫ����SQL*/
	private PCServiceTemplate pcServiceTemplate;
	/**�û������*/
	private UserManager userManager;


	@Override
	public void load() {

	}

	@Override
	public DynaBean mouldImport(DynaBean order) {
		StringBuilder errorMessage=new StringBuilder(2000);//错误信息记录
		HttpServletRequest request = null;
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, "");
		Boolean haveError = false;
		/* 模具编号 */
		String mjbh = order.getStr("MJ_BH");
		String mjlx = order.getStr("MJ_MJLX_CODE");//模具类型
//		String mjtm = order.getStr("MJ_TM");//模具条码
		String status = order.getStr("MJ_STATUS_CODE");//模具启用状态
		//模具编号验证
		if (StringUtil.isNotEmpty(mjbh)){
			List<DynaBean> mjBeanList = serviceTemplate.selectList("JGMES_BASE_MJ", "and MJ_BH='" + mjbh + "'");
			if (mjBeanList.size()>0){
				errorMessage.append("该模具编号已存在！"+"</br>");
				haveError=true;
			}
		}
		try {
			Integer bypl = order.getInt("MJ_BYCS");//保养频率（冲次）
			Integer yjcc = order.getInt("MJ_MJBYSL");//预警冲次（正负值）
		}catch (Exception e){
			e.printStackTrace();
			errorMessage.append("保养频率或者预警冲次必须为纯数字！"+"</br>");
			haveError=true;
		}
//		if (StringUtil.isNotEmpty(mjmc)){
//			List<DynaBean> mjBeanList = serviceTemplate.selectList("JGMES_BASE_MJ", "and MJ_MC='" + mjmc + "'");
//			if (mjBeanList.size()>0){
//				errorMessage.append("该模具名称已存在！"+"</br>");
//				haveError=true;
//			}
//		}
//		if (StringUtil.isNotEmpty(mjtm)){
//			List<DynaBean> mjBeanList = serviceTemplate.selectList("JGMES_BASE_MJ", "and MJ_TM='" + mjtm + "'");
//			if (mjBeanList.size()>0){
//				errorMessage.append("该模具条码已存在！"+"</br>");
//				haveError=true;
//			}
//		}
		//模具类型校验
		if (StringUtil.isNotEmpty(mjlx)){
			DynaBean dic1 = jgmesCommon.getDic("JGMEST_DIC_MJLX",mjlx);
			if(dic1!=null) {
				order.set("MJ_MJLX_NAME", dic1.get("DICTIONARYITEM_ITEMNAME"));//生产状态_NAME
				order.set("MJ_MJLX_CODE", dic1.get("DICTIONARYITEM_ITEMCODE"));//生产状态
			}else{
				errorMessage.append("模具类型导入的数据不对！"+"</br>");
				haveError=true;
			}
		}
		if (StringUtil.isEmpty(status)){
			order.setStr("MJ_STATUS_CODE","1");
			order.setStr("MJ_STATUS_NAME","启用");
		}
		if (haveError) {
			order.set("error", "序号:" + order.getStr("rownumberer_1") + "的错误信息为：" + errorMessage.toString());
		}
		return order;
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