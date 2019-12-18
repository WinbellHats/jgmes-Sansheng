package com.jgmes.service;

import com.je.core.security.SecurityUserHolder;
import com.je.core.service.PCDynaServiceTemplate;
import com.je.core.service.PCServiceTemplate;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.DynaBean;
import com.je.rbac.model.Department;
import com.je.rbac.model.EndUser;
import com.je.rbac.service.UserManager;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * �ֻݰ���ģ�鵼��
 * @author ljs
 * @version 2019-06-28 10:10:52
 */
@Component("lhImportService")
public class lhImportServiceImpl implements lhImportService  {

	/**��̬Bean(DynaBean)�ķ����*/
	private PCDynaServiceTemplate serviceTemplate;
	/**ʵ��Bean���������,��Ҫ����SQL*/
	private PCServiceTemplate pcServiceTemplate;
	/**�û������*/
	private UserManager userManager;
	
	public void load(){
		System.out.println("hello serviceimpl");
	}

	@Override
	public DynaBean earlyWarningImport(DynaBean order) {
		StringBuilder errorMessage = new StringBuilder(2000);//错误信息记录
		Boolean haveError = false;
		String jth = order.getStr("JTYCYJB_JTH");//终端号
		String yjsj = order.getStr("JTYCYJB_YJJGSJ");//预警时间
		String tzr = order.getStr("JTYCYJB_YCTZR");//通知人
		String jtyclx = order.getStr("JTYCYJB_JTYCLX_CODE");//机台异常类型
		String cxbm = order.getStr("JTYCYJB_SCXBM");//产线编码
		String jtmc = order.getStr("JTYCYJB_JTMC");//机台名称，产线名称
		//终端号校验
		if (StringUtil.isNotEmpty(jth)) {
			//校验终端号是否在机台档案存在
			List<DynaBean> jtdaBeanList = serviceTemplate.selectList("JGMES_ADMK_JTDA", "and JTDA_JTH='" + jth + "'");
			if (jtdaBeanList.size() == 0) {
				errorMessage.append("该终端号在机台档案中不存在！" + "</br>");
				haveError = true;
			}
		} else {
			errorMessage.append("终端号不能为空！" + "</br>");
			haveError = true;
		}
		//预警时间校验
		if (StringUtil.isNotEmpty(yjsj)) {
			//校验预警时间是否是数值类型
			try {
				Integer i = Integer.parseInt(yjsj);
			} catch (Exception e) {
//				e.printStackTrace();
				errorMessage.append("预警时间必须为纯数字！" + "</br>");
				haveError = true;
			}
		} else {
			errorMessage.append("预警时间不能为空！" + "</br>");
			haveError = true;
		}
		//通知人校验
		if (StringUtil.isNotEmpty(tzr)) {
			//校验通知人是否存在于人员列表中
			List<DynaBean> je_core_enduser = serviceTemplate.selectList("JE_CORE_ENDUSER", "and USERCODE='" + tzr + "'");
			if (je_core_enduser.size() == 0) {
				errorMessage.append("该通知人在人员档案中不存在！" + "</br>");
				haveError = true;
			}
		} else {
			errorMessage.append("通知人不能为空！" + "</br>");
			haveError = true;
		}
		//机台异常类型校验
		if (StringUtil.isNotEmpty(jtyclx)) {
			//校验机台异常类型
			String sql = "select * from je_core_dictionaryitem where dictionaryitem_dictionary_ID = (select je_core_dictionary_ID from je_core_dictionary where dictionary_DDCODE = 'JGMES_DIC_JTYCLX')";
			String JTYCYJB_JTYCLX_NAME = "";
			List<DynaBean> tmlxDicList = serviceTemplate.selectListBySql(sql);
			for (DynaBean mlxDic : tmlxDicList) {
				String DICTIONARYITEM_ITEMNAME = mlxDic.getStr("DICTIONARYITEM_ITEMNAME");
				if (mlxDic.getStr("DICTIONARYITEM_ITEMCODE").equals(jtyclx)) {
					JTYCYJB_JTYCLX_NAME = DICTIONARYITEM_ITEMNAME;
				}
			}
			if (StringUtil.isEmpty(JTYCYJB_JTYCLX_NAME)) {
				errorMessage.append("导入的异常类型不存在！" + "</br>");
				haveError = true;
			} else {
//				order.set("JTYCYJB_JTYCLX_NAME", order.getStr("JTYCYJB_JTYCLX_CODE"));
//				order.set("JTYCYJB_JTYCLX_CODE", JTYCYJB_JTYCLX_CODE);
			}
		} else {
			errorMessage.append("机台异常类型不能为空！" + "</br>");
			haveError = true;
		}
		//校验产线编码和产线名称
		if (StringUtil.isEmpty(cxbm) || StringUtil.isEmpty(jtmc)) {
			errorMessage.append("产线名称或产线名称不能为空！" + "</br>");
			haveError = true;
		} else {
			//校验产线编码和产线名称是否和终端机台号匹配
			List<DynaBean> jgmes_admk_jtda = serviceTemplate.selectList("JGMES_ADMK_JTDA", "and JTDA_JTH='" + jth + "' and JTDA_SCXBM='" + cxbm + "' and JTDA_JTMC='" + jtmc + "'");
			if (jgmes_admk_jtda.size() == 0) {
				errorMessage.append("导入的产线编码或产线名称与终端号不匹配，请检查机台档案！" + "</br>");
				haveError = true;
			}
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