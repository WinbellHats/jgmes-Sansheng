package com.jgmes.action;


import com.je.core.action.DynaAction;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.DynaBean;
import com.je.table.exception.PCExcuteException;
import com.jgmes.util.JgmesCommon;
import com.jgmes.util.JgmesResult;
import net.sf.json.JSONObject;
import org.codehaus.xfire.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 
 * @author cj
 * @version 2019-02-23 19:10:47
 * @see /jgmes/jgmesBaseAction!load.action
 */
@Component("jgmesBaseAction")
@Scope("prototype")
public class JgmesBaseAction extends DynaAction {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(JgmesBaseAction.class);
	
	/**
	 * 租户信息上传
	 * 
	 */
	public void getUserIps() {
		//System.out.println("开始");
		try {
			JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
			String userId = jgmesCommon.jgmesUser.getCurrentUserCode();
			System.out.println("userID="+userId);
//			if (jgmesCommon.jgmesUser.getCurrentUserCode().equals(userId)) {
//				userId=jgmesCommon.jgmesUser.getCurrentUserCode();
//			}
			DynaBean userDynaBean=serviceTemplate.selectOne("JE_SYS_LOGINLOG","and LOGINLOG_DLR_CODE='"+userId+"'order by LOGINLOG_DLSJ DESC limit 0,1");
			if (userDynaBean==null) {
				return;
			}
			String companyID=userDynaBean.getStr("SY_JTGSID");
			String companyName=userDynaBean.getStr("SY_JTGSMC");
			String userName=jgmesCommon.jgmesUser.getCurrentUserName();
			String loginDate=jgmesCommon.getCurrentDate();
			String loginTime=jgmesCommon.getCurrentTime();
			
			DynaBean userOne=serviceTemplate.selectOne("JGMES_XTGL_XTCS"," and XTCS_CXFL2_TEXT='用户ID' and XTCS_CXFL1_TEXT='全局参数'");
			if (userOne==null) {
				return;
			}
			userId=userOne.getStr("XTCS_CSZ").trim();
			if (userId==null&&userId.isEmpty()) {
				return;
			}
			DynaBean user = new DynaBean();
			if (userId.indexOf("|")!=-1) {
				//拆分userId
				String[] users=userId.split("\\|");
				userId=users[0];
				String ip =request.getParameter("ip");
				
				if(ip==null||ip.isEmpty()) {
					if(users[1].matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
						ip=users[1];
					}
				}
				System.out.println("ip:"+ip+"userid"+userId);
			    logger.debug("用户ID="+userId+"IP"+ip);
				user.set("LOGINLOG_DLIP",ip);
			}
			user.set("UserId", userId);
			user.set("UserName", userName);
			user.set("LoginDate", loginDate);
			user.set("LoginTime", loginTime);
			//System.out.println("user="+userId+userName+loginDate+loginTime+user);
			
			String saasWebHost = null;
			String zhID = null;
			String sec = null;
			
			
			List<DynaBean> userList = serviceTemplate.selectList("JGMES_XTGL_XTCS", "and XTCS_CXFL1_TEXT='全局参数' and XTCS_CXFL2_TEXT !='用户ID'");
			if (userList==null) {
				return;
			}
			for (DynaBean dynaBean : userList) {
				if (dynaBean.getStr("XTCS_CSZ")!=null&&"租户ID".equals(dynaBean.getStr("XTCS_CXFL2_TEXT"))) {
					//格式为 ：租户ID|地址|密钥
					String[] strs=dynaBean.getStr("XTCS_CSZ").split("\\|");
					if (strs.length==3) {
						zhID=strs[0].trim();
						saasWebHost=strs[1].trim();
						sec=strs[2].trim();
					}
				}
			}
			//System.out.println("token="+saasWebHost+zhID+sec);
			if (saasWebHost==null||saasWebHost.isEmpty()||zhID==null||zhID.isEmpty()||sec==null||sec.isEmpty()) {
				System.out.println("参数有缺失");
				return;
			}
			JSONObject userToken=null;
			Client client = new Client(new URL(String.format("%s/services/AuthInterfaceService?wsdl",
					saasWebHost)));
			Object[] res = client.invoke("authInterface", new Object[] {zhID,sec});
			String re = (String) res[0];
			userToken=JSONObject.fromObject(re);
			logger.debug("参数="+zhID+":"+sec+":"+saasWebHost);
			logger.debug("授权="+userToken+":"+userToken.getString("Token"));
			System.out.println("授权="+userToken+":"+userToken.getString("Token"));
			String strUrl = String.format("%s/services/BasicArchivesService?wsdl", saasWebHost);
			URL url = new URL(strUrl);
		    client = new Client(url);
			res = client.invoke("uploadLoginLogs", new Object[] {userToken.getString("Token").trim(),jsonBuilder.toJson(user)});
			System.out.println((String)res[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	/*
	 * 获取产线列表
	 */
	public void getCxList() {
		String deptCode = request.getParameter("DEPTCODE");//获取车间code
		JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
		String jsonStr = "";

		try {
			if(deptCode!=null&&!"".equals(deptCode)) {
				List<DynaBean> cxList = serviceTemplate.selectList("JGMES_BASE_CXSJ", "and CXSJ_STATUS_CODE='1' and CXSJ_SZCJID = '"+deptCode+"'");
				if (cxList != null && cxList.size() > 0) {
					ret.Data = ret.getValues(cxList);
					ret.TotalCount = (long) cxList.size();
				}
			}else {
				List<DynaBean> cxList = serviceTemplate.selectList("JGMES_BASE_CXSJ", "and CXSJ_STATUS_CODE='1' ");
				if (cxList != null && cxList.size() > 0) {
					ret.Data = ret.getValues(cxList);
					ret.TotalCount = (long) cxList.size();
				}
			}
		} catch (Exception e) {
			ret.setMessage(e.toString());
		}
		jsonStr = jsonBuilder.toJson(ret);
		toWrite(jsonStr);
	}

	/*
	 * 根据产线id获取工位列表
	 */
	public void getGwList() {
		JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
		String cxId = request.getParameter("CxId");
		System.out.println("getGwList=" + cxId);
		String jsonStr = "";
		try {
			if (cxId == null || "".equals(cxId)) {
				JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
				String userCode = jgmesCommon.jgmesUser.getCurrentUserCode();
				DynaBean cxDynaBean = jgmesCommon.getCurrentCX(userCode);
				cxId = cxDynaBean.getStr("JGMES_BASE_CXSJ_ID");
			}
			List<DynaBean> gwList = serviceTemplate.selectList("JGMES_BASE_GW",
					" and JGMES_BASE_CXSJ_ID='" + cxId + "' order by SY_ORDERINDEX ");
			if (gwList != null && gwList.size() > 0) {
				ret.Data = ret.getValues(gwList);
				ret.TotalCount = (long) gwList.size();
			}
		} catch (Exception e) {
			ret.setMessage(e.toString());
		}
		jsonStr = jsonBuilder.toJson(ret);
		toWrite(jsonStr);
	}
	
	
	/*
	   *   根据工位id获取产品工位工序列表   liuc
	 * 
	 */
	public void getGXListByGwId() {
		JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
		String gwId = request.getParameter("gwId");
		try {
			List<DynaBean> gxList = serviceTemplate.selectList("JGMES_BASE_CPGWGX",
					" and JGMES_BASE_GW_ID='" + gwId + "' order by SY_ORDERINDEX ");
			if (gxList != null && gxList.size() > 0) {
				ret.Data = ret.getValues(gxList);;
				ret.TotalCount = (long) gxList.size();
			}else {
				ret.setMessage("工序获取失败！");
			}
		} catch (Exception e) {
			ret.setMessage(e.toString());
		}
		toWrite(jsonBuilder.toJson(ret));
	}
	
	

	/**
	 * 自动排产前序工作，根据工单主键，产线id将待排产数据导入排产计划表（JGMES_PLAN_DETAIL）中
	 */
	public void addZDMulity() {
		try {
			
			JgmesCommon jc = new JgmesCommon(request, serviceTemplate, null);
			String spIds = request.getParameter("gdzjs");// 工单主键
			String cxId = request.getParameter("cxId");
			StringBuilder msg=new StringBuilder(1000);
			//System.out.println("gdzjs:" + spIds);
			//System.out.println("cxId:" + cxId);
			DynaBean cxDynaBeans = serviceTemplate.selectOne("JGMES_BASE_CXSJ", " and JGMES_BASE_CXSJ_ID='" + cxId + "'");
			if (cxDynaBeans == null) {
				toWrite(jsonBuilder.returnFailureJson("\"系统错误，请确认后重试。\""));
				return;
			}
			String cxbm = cxDynaBeans.getStr("CXSJ_CXBM");
			String cxmc = cxDynaBeans.getStr("CXSJ_CXMC");
			
			List<DynaBean> sps = serviceTemplate.selectList("JGMES_PLAN_GDLB",
					" AND JGMES_PLAN_GDLB_ID in (" + StringUtil.buildArrayToString(spIds.split(",")) + ")");
			
			for (DynaBean sp : sps) {
				
				// 判断是否已经导入了排产计划表中
				DynaBean gdDynaBeans = serviceTemplate.selectOne("JGMES_PLAN_DETAIL", " and JGMES_PLAN_GDLB_ID='" + sp.getStr("JGMES_PLAN_GDLB_ID") + "'");
				if (gdDynaBeans != null) {
					msg.append(sp.getStr("GDLB_GDHM")+"已经在排产表中");
					continue;
				}
				String code=sp.getStr("GDLB_PCZT_CODE");
				System.out.println("code:"+code);
				if (!code.isEmpty()) {
					msg.append(sp.getStr("GDLB_GDHM")+"已经在排产表中");
					continue;
				}
				DynaBean detail = new DynaBean("JGMES_PLAN_DETAIL", true);
				jc.setDynaBeanInfo(detail);
				detail.set("DETAIL_CPBH", sp.getStr("GDLB_CPBH"));
				detail.set("DETAIL_NAME", sp.getStr("GDLB_NAME"));
				detail.set("DETAIL_GDZT_CODE", sp.getStr("GDLB_GDZT_CODE"));
				detail.set("DETAIL_BZ", sp.getStr("GDLB_BZ"));
				detail.set("DETAIL_WCL", sp.getStr("GDLB_WCL"));
				detail.set("DETAIL_JHKGSJ", sp.getStr("GDLB_JHKGSJ"));
				detail.set("DETAIL_SJKGSJ", sp.getStr("GDLB_SJKGSJ"));
				detail.set("DETAIL_CPGG", sp.getStr("GDLB_CPGG"));
				detail.set("DETAIL_DDHM", sp.getStr("GDLB_DDHM"));
				detail.set("DETAIL_GDHM", sp.getStr("GDLB_GDHM"));
				detail.set("DETAIL_YQWCRQ", sp.getStr("GDLB_DDJHRQ"));// 要求完工日期
				detail.set("DETAIL_RWDH", sp.getStr("GDLB_RWDH"));
				detail.set("DETAIL_LCKH", sp.getStr("GDLB_LCKH"));
				detail.set("DETAIL_WCSL", sp.getStr("GDLB_WCSL"));
				detail.set("DETAIL_JHWGSJ", sp.getStr("GDLB_JHWGSJ"));
				detail.set("DETAIL_RQ", sp.getStr("GDLB_RQ"));
				detail.set("DETAIL_SJWGSJ", sp.getStr("GDLB_SJWGSJ"));
				detail.set("DETAIL_PGSL", sp.getStr("GDLB_PGSL"));
				detail.set("DETAIL_SJWGSJ", sp.getStr("GDLB_SJWGSJ"));
				detail.set("DETAIL_CXBM", cxbm);
				detail.set("DETAIL_CXMC", cxmc);
				detail.set("DETAIL_CXID", cxId);// 产线ID
				detail.set("JGMES_PLAN_GDLB_ID", sp.getStr("JGMES_PLAN_GDLB_ID"));// 工单列表_外键ID
				// DETAIL.set("JGMES_PLAN_MAIN_ID", pkValue);//排产主表外键ID
				// 修改排产状态的三个属性
				DynaBean dic = jc.getDic("JGMES_DIC_PCZT","PCZT01");
				detail.set("DETAIL_PCZT_CODE", dic.getStr("DICTIONARYITEM_ITEMCODE"));//编号
				detail.set("DETAIL_PCZT_NAME", dic.getStr("DICTIONARYITEM_ITEMNAME"));//名称
				detail.set("DETAIL_PCZT_ID", dic.getStr("JE_CORE_DICTIONARYITEM_ID"));//id
				
				detail.set("DETAIL_QGRQ", sp.getStr("GDLB_QGRQ"));// 请购日期
				detail.set("DETAIL_OTDRQ", sp.getStr("GDLB_OTDRQ"));// OTD日期
				detail.set("DETAIL_KHBM", sp.getStr("GDLB_KHBM"));// 客户编码
				detail.set("DETAIL_KHMC", sp.getStr("GDLB_KHMC"));// 客户名称
				detail.set("DETAIL_CPXH", sp.getStr("GDLB_CPXH"));// 产品型号

				detail.set("DETAIL_DDSL", sp.getStr("GDLB_DDSL"));// 订单数量

				String GDLB_WPCSL = sp.getStr("GDLB_WPCSL");// 订单未排数量
				detail.set("DETAIL_YPCSL", sp.getStr("GDLB_WPCSL"));// 已排产数量

				int GDLB_XPCSL = sp.getInt("GDLB_XPCSL");// 订单需排产数量
				int GDLB_WCSL = sp.getInt("GDLB_WCSL");// 订单完成数量
				if (GDLB_WCSL > 0) {
					// 表示已经开始生产
					detail.set("DETAIL_XPCSL", GDLB_XPCSL - GDLB_WCSL);// 需排产数量
					detail.set("DETAIL_WPCSL", GDLB_XPCSL - GDLB_WCSL);// 未排产数量
				} else {
					detail.set("DETAIL_XPCSL", GDLB_XPCSL);// 需排产数量
					detail.set("DETAIL_WPCSL", GDLB_WPCSL);// 未排产数量
				}
				
				//添加当前产能信息
				String productNum=sp.getStr("GDLB_CPBH",null);
				if (productNum==null) {
					throw new PCExcuteException("产品编号获取失败。");
				}
				int capacity=getCapacity(productNum, cxId);//获取日产能
				System.out.println("产能:"+capacity);
				detail.set("DETAIL_DQCN",capacity);
				//添加加工时长信息,天
				int unfinished=detail.getInt("DETAIL_WPCSL");
				float f1=(float)unfinished/capacity;
				//保留两位小数，并且第三位大于0时向前进一位
				int num = (int) (f1 * 1000);
				float unfinishDay =0;
				 if (num % 10 > 0) {
					 unfinishDay = (num -num % 10 + 10 * 1.0f) / 1000.0f;
				 }else {
					 unfinishDay =num * 1.0f / 1000.0f;
				 }
				System.out.println("天时长:"+unfinishDay);
				detail.set("DETAIL_JGSC", unfinishDay);
				
				// 修改工单列表对应数据。
				sp.set("CXSJ_CXBM", cxbm);
				sp.set("CXSJ_CXMC", cxmc);
				sp.set("GDLB_PCZT_CODE", dic.getStr("DICTIONARYITEM_ITEMCODE"));//编号
				sp.set("GDLB_PCZT_NAME", dic.getStr("DICTIONARYITEM_ITEMNAME"));//名称
				serviceTemplate.update(sp);
				/*
				 * Date dt = new Date(); String strDate=DateUtil.formatDate(dt, "yyyy-MM-dd");
				 * DETAIL.set("DETAIL_PCRQ", strDate);
				 */

				serviceTemplate.buildModelCreateInfo(detail);
				serviceTemplate.insert(detail);
			
			}
			toWrite(jsonBuilder.returnSuccessJson("\"执行成功:</br>"+msg.toString()+"\""));
		
		} catch (Exception e) {
			toWrite(jsonBuilder.returnFailureJson("\"执行失败:</br>"+e.getMessage()+"\""));
		}
		
	}
	
	/**
	 * 根据列表名计算出里面数据的产能和加工时长并赋值，已有值的不会赋值
	 */
	public void updateCapacityAndUnfinishDay() {
		StringBuilder msg=new StringBuilder(2000);
		boolean toerror=false;
		String tableName = request.getParameter("tableName");
		List<DynaBean> tableDynaBeans=serviceTemplate.selectList(tableName,"");
		if (tableDynaBeans==null||tableDynaBeans.size()<1) {
			toWrite(jsonBuilder.returnFailureJson("\"系统异常，请重试。\""));
			return;
		}
		String target =tableName.split("_")[tableName.split("_").length-1];//名称前缀
		for (DynaBean dynaBean : tableDynaBeans) {
			String dqcn=dynaBean.getStr(target+"_DQCN");
			String jgsc=dynaBean.getStr(target+"_JGSC");
			if (dqcn==null||"".equals(dqcn)||jgsc==null||"".equals(jgsc)) {
				try {
					setCapacityAndUnfinishDay(tableName, dynaBean.getPkValue());
					msg.append("工单单号："+dynaBean.getStr(target+"_GDHM")+":");
				} catch (Exception e) {
					String[] mess = e.getMessage().split(":");
					if (mess.length>1) {
						msg.append(mess[0]+":"+mess[mess.length-1]);
					}else {
						msg.append(mess[0]);
					}
					toerror = true;
					e.printStackTrace();
				}
			}
		}
		msg.append("</br>");
		if (toerror) {
			toWrite(jsonBuilder.returnFailureJson("\"执行完毕:</br>其中："+msg+"\""));
		}else {
			toWrite(jsonBuilder.returnSuccessJson("\"执行成功:</br>"+msg.toString()+"\""));
		}
	}
	
	/**
	 * 根据表名和主键来计算出产能和加工时长并给数据赋值
	 * @param tableName 表名
	 * @param primaryKeys 主键字符串
	 */
	private void setCapacityAndUnfinishDay(String tableName,String primaryKeys) {
		
		if (tableName==null||"".equals(tableName)) {
			throw new PCExcuteException("获取不到表格名称。");
		}
		if(primaryKeys==null||"".equals(primaryKeys)) {
			throw new PCExcuteException("获取不到表单主键。");
		}
		List<DynaBean> tableDynaBeans=serviceTemplate.selectList(tableName, "and "+tableName+"_ID "
				+ "in("+StringUtil.buildArrayToString(primaryKeys.split(",")) +")");
		if(tableDynaBeans==null) {
			throw new PCExcuteException("获取表数据异常");
		}
		for (DynaBean tableDynaBean : tableDynaBeans) {
			String target =tableName.split("_")[tableName.split("_").length-1];//名称前缀
			//拼接产品编号名称
			String postfix="_CPBH";//产品编号名称后缀
			logger.debug("产品编号"+target+postfix);
			String productNum=tableDynaBean.getStr(target+postfix);//产品编号
			//拼接产线id名称
			String productionID=null; 
			if("PCLB".equals(target)) {
				//获取产线ID
				DynaBean pclbDynaBean=serviceTemplate.selectOne("JGMES_BASE_CXSJ","and CXSJ_CXBM='"+tableDynaBean.getStr("PCLB_CXBM")+"'");
				if (pclbDynaBean==null) {
					throw new PCExcuteException("排产表数据获取失败。");
				}
				productionID=pclbDynaBean.getStr("JGMES_BASE_CXSJ_ID");
			}else if("DETAIL".equals(target)) {
				productionID=tableDynaBean.getStr("DETAIL_CXID");
			}else {
				throw new PCExcuteException("该条记录无法计算产能，加工时长");
			}
			//添加当前产能信息
			if (productNum==null) {
				throw new PCExcuteException("产品编号获取失败。");
			}
			int capacity=getCapacity(productNum, productionID);//获取日产能
			System.out.println("产能:"+capacity);
			tableDynaBean.set(target+"_DQCN",capacity);
			//添加加工时长信息,天
			int unfinished=tableDynaBean.getInt(target+"_WPCSL");
			double unfinishDay =Math.scalb((double)unfinished/capacity+0.004, 2);
			System.out.println("天时长:"+unfinishDay);
			tableDynaBean.set(target+"_JGSC", unfinishDay);
		}
	}
	
	/**
	 * 根据产品编号和产线ID查询产品产线产能表并计算出日产能并返回
	 * 参数1：产品编号 参数2：产线ID
	 * @return 当前产能  默认值返回0
	 */
	private int getCapacity(String productNum,String productionID) {
		int capacity=0;//产能
		DynaBean capDynaBean = serviceTemplate.selectOne("JGMES_BASE_CPCXCNINFO", "and CPCXCNINFO_BH='"+productNum+"' and CPCXCNINFO_CXid='"+productionID+"'");
		if(capDynaBean==null) {
			throw new PCExcuteException("未找到产品产线产能信息。");
		}
		//获取班次信息
		DynaBean cxDynaBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ", "and JGMES_BASE_CXSJ_ID='"+productionID+"'");//查询产线数据
		if (cxDynaBean==null) {
			throw new PCExcuteException("产线信息获取失败。请联系管理员");
		}
		String flightNum=cxDynaBean.getStr("DEPTCODE");//班组编号
		String ways=capDynaBean.getStr("CPCXCNINFO_CNJSFS_CODE");
		if ("CNJSFS01".equals(ways)) {//按小时产能计算
			int shift = getShiftTime(flightNum);//根据班组编号查询班次总时长
			int hoursWork=capDynaBean.getInt("CPCXCNINFO_XSCN",0);//小时产能
			if (hoursWork<1) {
				throw new PCExcuteException("产品小时产能获取失败。请确认数据是否存在");
			}
			//计算出日产能
			capacity=shift*hoursWork;
		} else if("CNJSFS02".equals(ways)){//按日产能计算
			//如果是日产能则直接返回
			capacity=capDynaBean.getInt("CPCXCNINFO_RCN",0);
		}else {
			throw new PCExcuteException("未录入产能数据。");
		}
		
		return capacity;
	}
	
	/**
	 * 递归获取有效的班组编码
	 * @param flightNum 班组编号
	 * @return  可用的班组编号
	 */
	private String getFlightNum(String flightNum) {
		if (flightNum==null||flightNum.isEmpty()) {
			throw new PCExcuteException("产线中班组编号获取失败。");
		}
		//根据实体ID查询班次信息
		DynaBean bcDynaBean = serviceTemplate.selectOne("JGMES_BASE_BC","and BC_STID='"+flightNum+"' "
				+ "and BC_STATUS_CODE='1' and BC_STARTDATE<NOW() and BC_ENDDATE>NOW()");
		if (bcDynaBean==null||bcDynaBean.getStr("BC_STID")==null||bcDynaBean.getStr("BC_STID").isEmpty()) {//查不到时
			//根据班次编号在组织管理中找父级的班次编号
			DynaBean zzDynaBean = serviceTemplate.selectOne("JE_CORE_DEPARTMENT","and DEPTCODE='"+flightNum+"'");
			//验证组织管理表是否存在记录
			if (zzDynaBean==null) {
				logger.debug("组织管理表未找到班次信息");
				throw new PCExcuteException("组织管理表未找到班次信息，请联系管理员。");
			}
			String deptID =zzDynaBean.getStr("PARENT");//父节点ID
			if (deptID==null||deptID.isEmpty()) {
				logger.debug("父节点ID:"+deptID+"未找到。");
				return null;
			}
			DynaBean parentDynaBean=serviceTemplate.selectOne("JE_CORE_DEPARTMENT","and DEPTID='"+deptID+"'");
			flightNum=parentDynaBean.getStr("DEPTCODE");//父节点班组编号
			if (flightNum==null||flightNum.isEmpty()) {
				logger.debug("父节点ID:"+deptID+"没有班次编号");
				throw new PCExcuteException("组织管理表未找到班次信息，请联系管理员。");
			}
			flightNum=getFlightNum(flightNum);
		}
		return flightNum;
	}
	
	/**
	 * 根据班组编码查询班次时间，查询不到时查询父节点对应班次信息
	 * 使用到递归
	 * @param flightNum
	 * @return 班次总时长
	 */
	private int getShiftTime(String flightNum) {
	    int shiftTime=0;
	    //获取有效的班组编号
	    flightNum = getFlightNum(flightNum);
	    if (flightNum==null) {
			shiftTime=8;
		} else {
			 //获取班次信息
		    DynaBean bcDynaBean = serviceTemplate.selectOne("JGMES_BASE_BC"," and BC_STID='"+flightNum+"' "
		    		+ "and BC_STATUS_CODE='1' and BC_STARTDATE<NOW() and BC_ENDDATE>NOW()");
		    try {
		    	shiftTime=bcDynaBean.getInt("BC_DQBCZSJ");
			} catch (Exception e) {
				throw new PCExcuteException("班次总时间格式错误。班组编号为："+flightNum);
			}
		    //当班次时间为0时可以通过开始和结束时间获取工作时长
		    if (shiftTime<1||shiftTime>24) {
				throw new PCExcuteException("班次表中班次总时长错误，请核实。");
			}
		}
	   
	    
	    return shiftTime;
	}
	
	/**
	 * 根据传入的开始和结束时间计算出间隔的小时数
	 * @param startTime 开始时间
	 * @param endTime 结束时间
	 * @return 间隔时间， 默认为8小时
	 */
	private int getTime(String startTime,String endTime) {
		int time=0;
		SimpleDateFormat myFormatter = new SimpleDateFormat("HH:mm:ss");
		long day = 0;
		try {
	    Date date = myFormatter.parse(startTime);
		Date mydate = myFormatter.parse(endTime);
	
        day = (date.getTime() - mydate.getTime()) /(1000*60*60);
	
        //这里精确到了秒，我们可以在做差的时候将时间精确到天
		} catch (Exception e) {
			throw new PCExcuteException("班次时间异常。");
		}
		time=(int)day;
		
		return time;
	}

	/**
	 * 自动排产发布任务功能
	 */
	public void zdSubmitNewVerson() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
		try {
			// 发布生产任务
			String spIds = request.getParameter("spIds");
			List<DynaBean> sps = serviceTemplate.selectList("JGMES_PLAN_DETAIL",
					" AND JGMES_PLAN_DETAIL_ID in (" + StringUtil.buildArrayToString(spIds.split(",")) + ")");
			DynaBean dic = jgmesCommon.getDic("JGMES_DIC_PCZT","PCZT04");
			for (DynaBean detail : sps) {
				
				// 先确认排产数据是排产中状态
				String pcCode = detail.getStr("DETAIL_PCZT_CODE");
				if("PCZT04".equals(pcCode)) {
					System.out.println("已排产完成");
					continue;
				}
				DynaBean isSkipAPS=serviceTemplate.selectOne("JGMES_XTGL_XTCS"," and XTCS_CXFL2_CODE='IsSkipAPS'");
				if(isSkipAPS!=null&&"0".equals(isSkipAPS.getStr("XTCS_CSZ"))) {
					System.out.println("跳过自动排产");
				}else {
					if (!"PCZT02".equals(pcCode)) {
						toWrite(jsonBuilder.returnFailureJson("\"请确认所有工单已经排产完毕" + "\""));
						return;
					}
				}
				
				// 判断当天任务数量
				if (detail.getStr("DETAIL_JHKGSJ") == null && detail.getStr("DETAIL_JHWGSJ") == null) {
					toWrite(jsonBuilder.returnFailureJson("\"计划开工日期或计划完工日期不正确。请确认" + "\""));
					return;
				}
				Date date=null;
				Date date2=null;
				try {
					date = format.parse(detail.getStr("DETAIL_JHKGSJ"));
					date2 = format.parse(detail.getStr("DETAIL_JHWGSJ"));
				} catch (ParseException e) {
					toWrite(jsonBuilder.returnFailureJson("计划开工日期或计划完工日期错误。"));
					return;
				}
				int rwDays = differentDays(date, date2);
				
				// 获取需要排产数量
				if(detail.getStr("DETAIL_XPCSL")==null&&detail.getStr("DETAIL_XPCSL").isEmpty()) {
					toWrite(jsonBuilder.returnFailureJson("\"错误："+detail.getStr("GDLB_GDHM")+"需排产数量为0\""));
					return;
				}
				int xpcsl = Integer.parseInt(detail.getStr("DETAIL_XPCSL"));
				// 分解任务
				if (rwDays <1) {
					rwDays = 1;
				}
				int fwpcsl = xpcsl;//剩余排产数量
				int syday = rwDays;//剩余天数
				if (fwpcsl==0) {
					toWrite(jsonBuilder.returnFailureJson("需排产数量为0"));
					return;
				}
				System.out.println("剩余排产数量："+fwpcsl+"剩余天数"+syday);
				logger.debug("剩余排产数量："+fwpcsl+"剩余天数"+syday);
				//根据开工时间递增输出日期
				for (int i = 0; i < rwDays; i++) {
					DynaBean bean = new DynaBean("JGMES_PLAN_SCRW", true);// 生成任务单对象				
					jgmesCommon.setDynaBeanInfo(bean);
					//serviceTemplate.buildModelCreateInfo(bean);
					String  RWDH = serviceTemplate.buildCode("SCRW_RWDH", "JGMES_PLAN_SCRW", bean);
					//System.out.println("任务单号=" + RWDH);
					bean.set("SCRW_RWDH", RWDH);
					bean.set("SCRW_PCRQ",format.format(date));//排产日期
					System.out.println("排产日期："+format.format(date));
					int dtsl = mathGZL(syday--, fwpcsl);//获取单天排产数量
					System.out.println("剩余排产数量："+fwpcsl+"剩余天数"+syday);
					logger.debug("剩余排产数量："+fwpcsl+"剩余天数"+syday);
					System.out.println("当天数量："+dtsl);
					bean.set("SCRW_PCZT_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));// 排产状态_NAME
					bean.set("SCRW_PCZT_CODE", dic.get("DICTIONARYITEM_ITEMCODE") );// 排产状态
					bean.set("SCRW_PCZT_ID",dic.get("JE_CORE_DICTIONARYITEM_ID"));// 排产状态_ID
					bean= newVerson(bean, detail, dtsl);//写入其余参数
					serviceTemplate.insert(bean);
					fwpcsl = fwpcsl - dtsl;
					date=getPCRQ(date);
				}
				//更改排产表排产状态为完成排产
				detail.set("DETAIL_PCZT_CODE", dic.get("DICTIONARYITEM_ITEMCODE"));
				detail.set("DETAIL_PCZT_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));
				detail.set("DETAIL_PCZT_ID", dic.get("JE_CORE_DICTIONARYITEM_ID"));
				serviceTemplate.update(detail);

				// 回写工单表相关信息
				DynaBean gdDynaBean = serviceTemplate.selectOne("JGMES_PLAN_GDLB",
						" AND JGMES_PLAN_GDLB_ID ='" + detail.getStr("JGMES_PLAN_GDLB_ID") + "'");
				int ypcsl=Integer.parseInt(detail.getStr("DETAIL_YPCSL")==null?"0":detail.getStr("DETAIL_YPCSL"));
				ypcsl=ypcsl+xpcsl;
				int wpcslNew=Integer.parseInt(gdDynaBean.getStr("GDLB_DDSL"));
				wpcslNew=wpcslNew-ypcsl;
				gdDynaBean.set("GDLB_YPCSL", ypcsl);// 已排产数量
				gdDynaBean.set("GDLB_WPCSL", wpcslNew);//未排数量
				gdDynaBean.set("GDLB_PCZT_CODE", dic.get("DICTIONARYITEM_ITEMCODE"));
				gdDynaBean.set("GDLB_PCZT_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));
				gdDynaBean.set("GDLB_PCZT_ID", dic.get("JE_CORE_DICTIONARYITEM_ID"));
				//gdDynaBean.set("GDLB_RWDH", RWDH);// 任务单号
				serviceTemplate.update(gdDynaBean);
				//删除已生成任务的排产记录
//				serviceTemplate.deleteByIds(detail.getStr("JGMES_PLAN_DETAIL_ID"), "JGMES_PLAN_DETAIL", "JGMES_PLAN_DETAIL_ID");
//				System.out.println("排产记录删除");
			
			}
			
			toWrite(jsonBuilder.returnSuccessJson("\"发布成功\""));
		} catch (Exception e) {
			e.printStackTrace();
			toWrite(jsonBuilder.returnFailureJson("\"后台出现错误,请联系网络管理员。" + e.getMessage()==null?"":e.getMessage() + "\""));
		}
	 }
	
/**
 * 将传入的日期增加一天后返回
 * @param date 
 * @return
 */
	private Date getPCRQ(Date date) {
		Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, +1);//+1传入的时间加一天
        date = calendar.getTime();
        return date;
	}


	/**
	 * 计算每天的任务数量
	 * 
	 * @param rwDays 任务天
	 * @return 当天的任务数量
	 */
	private int mathGZL(int rwDays, Integer xpcsl) {
		//System.out.println("计算当天任务数量" + rwDays + "+" + xpcsl);
		Integer mrsl = xpcsl / rwDays;
		Integer rem = 0;
		if ((rem = xpcsl % rwDays) != 0) {
			mrsl = mrsl + rem;
		}
		return mrsl;
	}

	/**
	 * 判断两日期的相差天数，date1为开始时间，date2为结束时间
	 */
	public int differentDays(Date date1, Date date2) {
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		int day1 = cal1.get(Calendar.DAY_OF_YEAR);
		int day2 = cal2.get(Calendar.DAY_OF_YEAR);
		int year1 = cal1.get(Calendar.YEAR);
		int year2 = cal2.get(Calendar.YEAR);
		if (year1 != year2) {// 同一年
			int timeDistance = 0;
			for (int i = year1; i < year2; i++) {
				if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0) // 闰年
				{
					timeDistance += 366;
				} else // 不是闰年
				{
					timeDistance += 365;
				}
			}
			return timeDistance + (day2 - day1);
		} else // 不同年
		{
			System.out.println("判断day2 - day1 : " + (day2 - day1));
			return day2 - day1;
		}
	}

	/**
	 * 生成发布任务数据 值：DynaBean 生产任务表对象scrwDB，排产计划表对象detailDB， String
	 * 当天任务数量dtsl(SCRW_DTSL)
	 * 
	 */
	private DynaBean newVerson(DynaBean scrwDB, DynaBean detailDB, int dtsl) {
		
		//System.out.println("生产任务：" + scrwDB + "+" + detailDB + "+" + dtsl);
		String BBH = detailDB.getStr("DETAIL_BBH") ;
		if (BBH != null && !BBH.isEmpty()) {
			BBH = 1 + Integer.parseInt(BBH) + "";
		}
		scrwDB.set("SCRW_JHKGSJ", detailDB.getStr("DETAIL_JHKGSJ") );// 计划开工时间
		scrwDB.set("SCRW_PCDBH", detailDB.getStr("DETAIL_PCDBH") );// 排产单编号
		scrwDB.set("SCRW_PCDMC", detailDB.getStr("DETAIL_PCDMC"));// 排产单名称
		scrwDB.set("SCRW_CPBH", detailDB.getStr("DETAIL_CPBH"));// 产品编号
		scrwDB.set("SCRW_NAME", detailDB.getStr("DETAIL_NAME"));// 产品名称
		scrwDB.set("SCRW_GDZT_CODE", detailDB.getStr("DETAIL_GDZT_CODE"));// 工单状态
		scrwDB.set("SCRW_BZ", detailDB.getStr("DETAIL_BZ"));// 备注
		
		scrwDB.set("SCRW_WCL", detailDB.getStr("DETAIL_WCL"));// 完成率
		scrwDB.set("SCRW_SJKGSJ", detailDB.getStr("DETAIL_SJKGSJ") );// 实际开工时间
		scrwDB.set("SCRW_CPGG", detailDB.getStr("DETAIL_CPGG") );// 产品规格
		scrwDB.set("SCRW_DDHM", detailDB.getStr("DETAIL_DDHM"));// 订单号码
		scrwDB.set("SCRW_GDHM", detailDB.getStr("DETAIL_GDHM") );// 工单号码
		scrwDB.set("SCRW_DEPTNAME", detailDB.getStr("DETAIL_DEPTNAME") );// 部门名称

		scrwDB.set("SCRW_LCKH", detailDB.getStr("DETAIL_LCKH") );// 流程卡号
		scrwDB.set("SCRW_GDZT_NAME", detailDB.getStr("DETAIL_GDZT_NAME"));// 工单状态_NAME
		scrwDB.set("SCRW_WCSL", detailDB.getStr("DETAIL_WCSL") );// 完成数量
		scrwDB.set("SCRW_JHWGSJ", detailDB.getStr("DETAIL_JHWGSJ") );// 计划完工时间
		scrwDB.set("SCRW_DEPTCODE", detailDB.getStr("DETAIL_DEPTCODE"));// 部门编码
		//scrwDB.set("SCRW_PCRQ", detailDB.getStr("DETAIL_PCRQ") );// 排产日期
		scrwDB.set("SCRW_SJWGSJ", detailDB.getStr("DETAIL_SJWGSJ") );// 实际完工时间
		scrwDB.set("SCRW_PGSL", detailDB.getStr("DETAIL_PGSL") );// 派工数量
		scrwDB.set("SCRW_CXBM", detailDB.getStr("DETAIL_CXBM") );// 产线编码
		scrwDB.set("SCRW_CXMC", detailDB.getStr("DETAIL_CXMC") );// 产线名称
		scrwDB.set("JGMES_PLAN_GDLB_ID", detailDB.getStr("JGMES_PLAN_GDLB_ID") );// 工单列表_外键ID
		scrwDB.set("SCRW_GDSL", detailDB.getStr("DETAIL_GDSL") );// 工单数量
		scrwDB.set("SCRW_DDSL", detailDB.getStr("DETAIL_DDSL"));// 订单数量
		
		// bean.set("JGMES_PLAN_MAIN_ID", DETAIL.get("JGMES_PLAN_MAIN_ID") + "");//
		// 排产单主表_外键ID
		scrwDB.set("SCRW_YPCSL", detailDB.getStr("DETAIL_YPCSL"));// 已排产数量
		scrwDB.set("SCRW_WPCSL", detailDB.getStr("DETAIL_WPCSL") );// 未排产数量
		scrwDB.set("SCRW_QGRQ", detailDB.getStr("DETAIL_QGRQ"));// 请购日期
		scrwDB.set("SCRW_OTDRQ", detailDB.getStr("DETAIL_OTDRQ") );// OTD日期
		scrwDB.set("SCRW_XPCSL", detailDB.getStr("DETAIL_XPCSL") );// 需排产数量
		scrwDB.set("SCRW_KHBM", detailDB.getStr("DETAIL_KHBM") );// 客户编码
		scrwDB.set("SCRW_KHMC", detailDB.getStr("DETAIL_KHMC") );// 客户名称
		scrwDB.set("SCRW_CPXH", detailDB.getStr("DETAIL_CPXH") );// 产品型号
		scrwDB.set("SCRW_DTSL", dtsl);// 当天数量
		scrwDB.set("SCRW_BBH", BBH);// 版本号
		scrwDB.set("SCRW_PCSL", dtsl);// 排产数量
		scrwDB.set("SCRW_RWZT_NAME", "待生产");// 任务状态_NAME
		scrwDB.set("SCRW_RWZT_CODE", "RWZT01");
		scrwDB.set("SCRW_SCRWDATASOURCE_CODE","PC");//数据来源
		//排序号根据产线分组
		DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", 
				" and SCRW_CXBM='"+scrwDB.getStr("SCRW_CXBM")+"' order by SY_ORDERINDEX desc limit 0,1");
		int orderindex =0;
		if (scrwDynaBean==null) {//首次发布任务时序号为1
			orderindex=1;
		}else {
			orderindex = scrwDynaBean.getInt("SY_ORDERINDEX")+1;
		}
		scrwDB.set("SY_ORDERINDEX", orderindex);// 排序字段
		 
		//serviceTemplate.insert(scrwDB);
		//System.out.println("自动发布任务+1");
		return scrwDB;

	}
	
	
	/*
	 * 根据产品编号获取该产品对应的工序列表
	 * */
	public void getGXSum() {
		String cpbm = request.getParameter("cpbm");//产品编码
		String scrwNo = request.getParameter("scrwNo");// 生产任务单号
		JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
			try {
				if(cpbm!=null&&!"".equals(cpbm)&&scrwNo!=null&&!"".equals(scrwNo)) {
					//获取产品信息中的工艺路线
					DynaBean cpDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA"," and PRODUCTDATA_BH = '"+cpbm+"'");
					if(cpDynaBean!=null&&cpDynaBean.getStr("PRODUCTDATA_CPGYLXID")!=null&&!"".equals(cpDynaBean.getStr("PRODUCTDATA_CPGYLXID"))) {
						List<DynaBean> gylxgxDynaBeanList = serviceTemplate.selectList("JGMES_GYGL_GYLXGX"," and GYLX_ID = '"+cpDynaBean.getStr("PRODUCTDATA_CPGYLXID")+"' order by SY_ORDERINDEX");
						List<DynaBean> returnList = new ArrayList<DynaBean>();
						if(gylxgxDynaBeanList!=null&&gylxgxDynaBeanList.size()>0) {
							for(DynaBean gylxgxDynaBean:gylxgxDynaBeanList) {
								String sql = "select SUM(cast(BGSJ_SL as UNSIGNED INTEGER)) as BGSJ_SL_SUM , SUM(cast(BGSJ_BLSL as UNSIGNED INTEGER)) as BGSJ_BLSL_SUM from JGMES_PB_BGSJ where BGSJ_SCRW = '"+scrwNo+"' and BGSJ_GXBH = '"+gylxgxDynaBean.getStr("GYLXGX_GXNUM")+"'";
								List<DynaBean> sumDynaBean = serviceTemplate.selectListBySql(sql);
								DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and SCRW_RWDH = '"+scrwNo+"'");
								DynaBean returnData = new DynaBean();
								returnData.set("SY_ORDERINDEX", gylxgxDynaBean.getStr("SY_ORDERINDEX")); //工艺工序顺序号
								returnData.set("GYLXGX_GXNUM", gylxgxDynaBean.getStr("GYLXGX_GXNUM")); //工序编号
								returnData.set("GYLXGX_GXNAME", gylxgxDynaBean.getStr("GYLXGX_GXNAME")); //工序名称
								//returnData.set("SY_ORDERINDEX", gylxgxDynaBean.getStr("SY_ORDERINDEX")); //质量模式
								if(scrwDynaBean!=null) {
									returnData.set("SCRW_PGSL", scrwDynaBean.getStr("SCRW_PCSL")); //已派工数量  我暂时取得是排产数量
								}
								returnData.set("Already_Number", sumDynaBean.get(0).getInt("BGSJ_SL_SUM")+sumDynaBean.get(0).getInt("BGSJ_BLSL_SUM")); //已报工数量
								/* 获取该工序下，是否有这个条码的报工 */
								List<DynaBean> dynaBeans = serviceTemplate.selectList("JGMES_PB_BGSJ", "and BGSJ_SCRW = '" + scrwNo + "' and BGSJ_GXBH = '" + gylxgxDynaBean.getStr("GYLXGX_GXNUM") + "' GROUP BY BGSJ_TMH");
								/* 流程型，是否报工，1为是，0为否 */
								returnData.set("Already_Number_LCX", dynaBeans.size());
								/* 获取返修站数据 */
								String lyGx = gylxgxDynaBean.getStr("GYLXGX_ID");//来源工艺路线工序ID
								if (StringUtil.isNotEmpty(lyGx)){
									/* 根据工序ID获取工位 */
									List<DynaBean> jgmes_base_cpgwgx = serviceTemplate.selectList("JGMES_BASE_CPGWGX", "and CPGWGX_CPBH='" + cpbm + "' and CPGWGX_GYGXID='" + lyGx + "'","CPGWGX_GWBH");
									if (jgmes_base_cpgwgx.size()>0) {
										StringBuilder gwListStr = new StringBuilder();
										for (DynaBean jgmesBaseCpgwgx : jgmes_base_cpgwgx) {
											gwListStr.append("'"+jgmesBaseCpgwgx.getStr("CPGWGX_GWBH")+"'"+",");
										}
										if (StringUtil.isNotEmpty(gwListStr.toString()))
										{
											String substring = gwListStr.substring(0, gwListStr.length() - 1);
											/* 获取返修单 */
											List<DynaBean> fxdBean = serviceTemplate.selectList("JGMES_PB_FXD", "and FXD_LYGWBH in(" + substring + ") and FXD_RWDH='" + scrwNo + "'");
											returnData.set("Fx_Number", fxdBean.size());
										}

									}

								}

								//returnData.set("SY_ORDERINDEX", gylxgxDynaBean.getStr("SY_ORDERINDEX")); //作业说明
								returnData.set("GYLXGX_ID", gylxgxDynaBean.getStr("GYLXGX_ID")); //工艺工序主键编号
								returnList.add(returnData);
							}
							if(returnList!=null&&returnList.size()>0) {
								ret.Data = ret.getValues(returnList);
								ret.TotalCount = (long) returnList.size();
							}else {
								ret.setMessage("未获取到数据！");
							}
						}else {
							ret.setMessage("未工艺路线工序！");
						}
					}else {
						ret.setMessage("未根据产品编码获取到产品信息或者改产品的工艺路线为空！");
					}
				}else {
					ret.setMessage("未获取到产品编码或者生产任务单号！");
				}
			} catch (Exception e) {
				// TODO: handle exception
				ret.setMessage("系统异常，请联系管理员！");
				e.printStackTrace();
			}

			toWrite(jsonBuilder.toJson(ret));
	}

	/**
	 * @author liuc
	 * 获取系统参数
	 * @see /jgmes/jgmesBaseAction!getXTCS.action
	 */
	public void getXTCS(){
		// MAC地址
		String mac = request.getParameter("mac");
		// 用户编码  必填
		String userCode = request.getParameter("userCode");
		// 参数分类
		String csfl = request.getParameter("csfl");

		JgmesResult<Integer> ret = new JgmesResult<Integer>();
		String jsonStr = "";
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {
			DynaBean dynaBean=serviceTemplate.selectOne("JGMES_XTGL_XTCS"," and  XTCS_CXFL1_CODE = 'CXFL06' and XTCS_CXFL2_CODE = '"+csfl+"'");
			if(dynaBean!=null){
				ret.Data = dynaBean.getInt("XTCS_CSZ");
			}
		}
		jsonStr = jsonBuilder.toJson(ret);
		toWrite(jsonStr);
	}

	/**
	 * 校验用户合法性，不合法直接提示。
	 * @param userCode
	 * @param mac
	 * @return
	 */
	private JgmesResult<String> doCheck(String userCode,String mac) {
		JgmesResult<String> result = new JgmesCommon(request, serviceTemplate).doCheckRes(mac, userCode);
		if (!result.IsSuccess) {
			toWrite(jsonBuilder.toJson(result));
		}
		return result;
	}



}