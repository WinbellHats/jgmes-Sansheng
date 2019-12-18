package com.demo.service;
import com.je.core.security.SecurityUserHolder;
import com.je.core.service.PCDynaServiceTemplate;
import com.je.core.service.PCServiceTemplate;
import com.je.core.util.bean.DynaBean;
import com.je.rbac.model.Department;
import com.je.rbac.model.EndUser;
import com.je.rbac.service.UserManager;
import com.je.table.exception.PCExcuteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

//import org.apache.log4j.Logger;
/**
 * 工单导入
 * @author cj
 * @version 2019-04-02 20:23:07
 */
@Component("workorderService")
public class WorkorderServiceImpl implements WorkorderService  {
	private static final Logger logger = LoggerFactory.getLogger(WorkorderServiceImpl.class);

	/**��̬Bean(DynaBean)�ķ����*/
	private PCDynaServiceTemplate serviceTemplate;
	/**ʵ��Bean���������,��Ҫ����SQL*/
	private PCServiceTemplate pcServiceTemplate;
	/**�û������*/
	private UserManager userManager;
	
	public void load(){
		logger.debug("");
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


	/**
	 * 工单导入
	 */
	@Override
	public DynaBean implGDOrder(DynaBean order) {
		System.out.println("开始工单导入：");
		StringBuilder errorMessage=new StringBuilder(1000);//错误信息记录
		boolean toerror = false;
		//判断对象是否为空
		if(order==null) {
			order=new DynaBean();
			System.out.println("这一行返回空值。");
			errorMessage.append("数据不存在;");
			toerror = true;
		}else {
			
			//关键字段去空白操作
			try {
				//order.set("GDLB_DDHM", order.getStr("GDLB_DDHM").trim());//订单号码
				order.set("GDLB_GDHM", order.getStr("GDLB_GDHM").trim());//工单号码
				order.set("GDLB_CPBH", order.getStr("GDLB_CPBH").trim());//产品编号
			} catch (Exception e) {
				errorMessage.append("请确认必填数据（订单，工单，产品编号），是否为空:"+e.getMessage());
				toerror = true;
			}
			
			//判断产品编号,排除材料
			String cpbm = order.getStr("GDLB_CPBH");
			DynaBean cpDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA"," and PRODUCTDATA_BH='"+cpbm+"' and  PRODUCTDATA_WLTYPE_CODE!='WL'");
			if(cpbm==null||cpDynaBean==null) {
				System.out.println("产品信息获取失败。");
				errorMessage.append("产品信息"+cpbm+"不存在;");
				toerror = true;
			}else {
				order.set("GDLB_CPBH", cpDynaBean.getStr("PRODUCTDATA_BH"));//写入基础资料中的产品编号
				//查询产品名称    并校验，产品名称为null或与产品表不匹配时
				if (cpDynaBean.getStr("PRODUCTDATA_NAME")==null||cpDynaBean.getStr("PRODUCTDATA_NAME").isEmpty()) {
					errorMessage.append("产品编号"+cpbm+"在产品表中名称为空;");
					toerror = true;
				}else if(order.getStr("GDLB_NAME")==null||!order.getStr("GDLB_NAME").equals(cpDynaBean.getStr("PRODUCTDATA_NAME"))) {
					order.set("GDLB_NAME", cpDynaBean.getStr("PRODUCTDATA_NAME"));
				}
				
				//查询产品规格
				if(order.getStr("GDLB_CPGG")==null||!order.getStr("GDLB_CPGG").equals(cpDynaBean.getStr("PRODUCTDATA_GG"))) {
					order.set("GDLB_CPGG", cpDynaBean.getStr("PRODUCTDATA_GG"));
				}
				
				//查询产品型号
				if(order.getStr("GDLB_CPXH")==null||!order.getStr("GDLB_CPXH").equals(cpDynaBean.getStr("PRODUCTDATA_XH"))) {
					order.set("GDLB_CPXH", cpDynaBean.getStr("PRODUCTDATA_XH"));
				}
				//绑定工艺路线ID
				String gylxID=cpDynaBean.getStr("PRODUCTDATA_CPGYLXID");//工艺路线ID
				DynaBean gylxDynaBean = serviceTemplate.selectOne("JGMES_GYGL_GYLX"," and GYLX_ID='"+gylxID+"'");
				if (gylxID==null||gylxDynaBean==null) {
					System.out.println("工艺路线获取失败。");
					errorMessage.append("工艺路线信息"+gylxID+"不存在;");
					toerror = true;
				}else {
					System.out.println("插入工艺路线ID");
					order.set("GDLB_GYLXID", gylxID);
				}
			}
			//验证工单号码唯一性
			String gdhm = order.getStr("GDLB_GDHM");
			if (gdhm == null||gdhm.isEmpty()) {
				System.out.println("获取工单号码失败。");
				errorMessage.append("工单号码"+gdhm+"获取失败;");
				toerror = true;
			}else {
				//判断已导入的数据中是否含有相同的工单号码
				if (order.get("importDataList")!=null){
					List<DynaBean> importDataList = (List<DynaBean>) order.get("importDataList");
					if (importDataList.size() != 0) {
						for (DynaBean dynaBean : importDataList) {
							if (dynaBean.getStr("GDLB_GDHM").equals(gdhm)){
								String xh1 = dynaBean.getStr("rownumberer_1");
								if (xh1==null) {
									xh1=" ;"+gdhm;
								}
								errorMessage.append("与序号："+xh1+":工单号码重复;");
								toerror=true;
							}
						}
					}
				}
				List<DynaBean> gdDynaBean = serviceTemplate.selectList("JGMES_PLAN_GDLB"," and GDLB_GDHM='"+gdhm+"'");
				if (gdDynaBean.size()>0) {
					System.out.println("工单号码唯一性异常。");
					errorMessage.append("工单号码"+gdhm+"已在工单表存在;");
					toerror = true;
				}
			}
			
			try {
				updateGDLB(order);//执行默认数据插入
			} catch (Exception e) {
				//截取错误内容，错误类不要
				String[] mess = e.getMessage().split(":");
				if (mess.length>1) {
					errorMessage.append(mess[0]+":"+mess[mess.length-1]);
				}else {
					errorMessage.append(mess[0]);
				}
				toerror = true;
				e.printStackTrace();
			}
		}
		if (toerror) {
			String serial=order.getStr("rownumberer_1");
			if (serial==null) {
				serial=" ";
			}
			order.set("error", "序号:"+serial+":"+errorMessage.toString()+"</br>");
		} 
		
		return order;
	}
	
	/**
	 * 写入工单默认参数
	 * @param order
	 */
	private void updateGDLB(DynaBean order) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			String delivery = order.getStr("GDLB_OTDRQ");
			if (delivery==null||delivery.isEmpty()) {
				delivery = order.getStr("GDLB_DDJHRQ");
				if (delivery==null||delivery.isEmpty()) {
					throw new PCExcuteException("交货日期异常。");
				}
			}
			
			//判断订单号码是否为空
			//if (order.getStr("GDLB_DDHM")==null||order.getStr("GDLB_DDHM").isEmpty()) {
			//	throw new PCExcuteException("订单号为空");
			//}
			Date otdDate = sdf.parse(delivery);
			String date = sdf.format(otdDate);//处理后的日期“2019-04-03”
			System.out.println(date);
			order.set("GDLB_OTDRQ", date);//otd日期
			order.set("GDLB_DDJHRQ", date);//订单交货日期
			
			String requisition = order.getStr("GDLB_QGRQ");
			if (requisition==null||"".equals(requisition)) {
				throw new PCExcuteException("请购日期异常。");
			}
			Date qgDate = sdf.parse(requisition);
			date = sdf.format(qgDate);
			System.out.println(date);
			order.set("GDLB_QGRQ", date);//请购日期
			
			String dateRQ = order.getStr("GDLB_RQ"); //日期没有则填写当前日期
			System.out.println("日期：："+dateRQ);
			if (dateRQ!=null&&!"".equals(dateRQ)) {
				date = sdf.format(sdf.parse(dateRQ));
				System.out.println(date);
				order.set("GDLB_RQ", date);
			}else {
				order.set("GDLB_RQ","");
			}
			//对比各个日期的合法性，请购日期小于等于交货日期，交货日期 大于等于 当前日期
			if (otdDate.getTime()<qgDate.getTime()||otdDate.getTime()<new Date().getTime()) {
				throw new PCExcuteException("OTD（交货）日期不合法,请检查相关日期");
			}
			//order.set("GDLB_JHKGSJ","");
			//order.set("GDLB_JHWGSJ","");
			
			int gdsl=order.getInt("GDLB_DDSL",0);//工单数量
			if (gdsl<=0) {
				gdsl=order.getInt("GDLB_GDSL");
				if (gdsl<=0) {
					throw new PCExcuteException("工单或订单数量异常。");
				}
			}
			order.set("GDLB_WPCSL",gdsl);//初始为订单数量
			order.set("GDLB_XPCSL",gdsl);//初始为订单数量
			order.set("GDLB_DDSL", gdsl);//订单数量
			order.set("GDLB_GDSL", gdsl);//工单数量
			
			order.set("GDLB_GDZT_NAME", "未完");//工单状态
			order.set("GDLB_GDZT_CODE", 1);
			order.set("GDLB_PCZT_NAME", " ");//排产状态
			//order.set("GDLB_PCZT_CODE", "PCZT01");
			
			String whether = order.getStr("GDLB_NO_CODE");//物料齐套情况，默认为是
			String qtTime = order.getStr("GDLB_WLQTSJ");//物料齐套时间，默认为当前时间
			if (whether==null||whether.isEmpty()) {
				whether="1";
			}
			if (qtTime==null||qtTime.isEmpty()) {
				qtTime=sdf.format(new Date());
			}
			order.set("GDLB_NO_CODE", whether);//是否齐套  1  是，0  否
			order.set("GDLB_WLQTSJ", qtTime);
			serviceTemplate.buildModelCreateInfo(order);//插入系统参数
		} catch (Exception e) {
			throw new PCExcuteException("错误;"+e.getMessage());
		}
	}
	
	
}