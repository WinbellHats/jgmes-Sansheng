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
	 * �⻧��Ϣ�ϴ�
	 * 
	 */
	public void getUserIps() {
		//System.out.println("��ʼ");
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
			
			DynaBean userOne=serviceTemplate.selectOne("JGMES_XTGL_XTCS"," and XTCS_CXFL2_TEXT='�û�ID' and XTCS_CXFL1_TEXT='ȫ�ֲ���'");
			if (userOne==null) {
				return;
			}
			userId=userOne.getStr("XTCS_CSZ").trim();
			if (userId==null&&userId.isEmpty()) {
				return;
			}
			DynaBean user = new DynaBean();
			if (userId.indexOf("|")!=-1) {
				//���userId
				String[] users=userId.split("\\|");
				userId=users[0];
				String ip =request.getParameter("ip");
				
				if(ip==null||ip.isEmpty()) {
					if(users[1].matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
						ip=users[1];
					}
				}
				System.out.println("ip:"+ip+"userid"+userId);
			    logger.debug("�û�ID="+userId+"IP"+ip);
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
			
			
			List<DynaBean> userList = serviceTemplate.selectList("JGMES_XTGL_XTCS", "and XTCS_CXFL1_TEXT='ȫ�ֲ���' and XTCS_CXFL2_TEXT !='�û�ID'");
			if (userList==null) {
				return;
			}
			for (DynaBean dynaBean : userList) {
				if (dynaBean.getStr("XTCS_CSZ")!=null&&"�⻧ID".equals(dynaBean.getStr("XTCS_CXFL2_TEXT"))) {
					//��ʽΪ ���⻧ID|��ַ|��Կ
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
				System.out.println("������ȱʧ");
				return;
			}
			JSONObject userToken=null;
			Client client = new Client(new URL(String.format("%s/services/AuthInterfaceService?wsdl",
					saasWebHost)));
			Object[] res = client.invoke("authInterface", new Object[] {zhID,sec});
			String re = (String) res[0];
			userToken=JSONObject.fromObject(re);
			logger.debug("����="+zhID+":"+sec+":"+saasWebHost);
			logger.debug("��Ȩ="+userToken+":"+userToken.getString("Token"));
			System.out.println("��Ȩ="+userToken+":"+userToken.getString("Token"));
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
	 * ��ȡ�����б�
	 */
	public void getCxList() {
		String deptCode = request.getParameter("DEPTCODE");//��ȡ����code
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
	 * ���ݲ���id��ȡ��λ�б�
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
	   *   ���ݹ�λid��ȡ��Ʒ��λ�����б�   liuc
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
				ret.setMessage("�����ȡʧ�ܣ�");
			}
		} catch (Exception e) {
			ret.setMessage(e.toString());
		}
		toWrite(jsonBuilder.toJson(ret));
	}
	
	

	/**
	 * �Զ��Ų�ǰ���������ݹ�������������id�����Ų����ݵ����Ų��ƻ���JGMES_PLAN_DETAIL����
	 */
	public void addZDMulity() {
		try {
			
			JgmesCommon jc = new JgmesCommon(request, serviceTemplate, null);
			String spIds = request.getParameter("gdzjs");// ��������
			String cxId = request.getParameter("cxId");
			StringBuilder msg=new StringBuilder(1000);
			//System.out.println("gdzjs:" + spIds);
			//System.out.println("cxId:" + cxId);
			DynaBean cxDynaBeans = serviceTemplate.selectOne("JGMES_BASE_CXSJ", " and JGMES_BASE_CXSJ_ID='" + cxId + "'");
			if (cxDynaBeans == null) {
				toWrite(jsonBuilder.returnFailureJson("\"ϵͳ������ȷ�Ϻ����ԡ�\""));
				return;
			}
			String cxbm = cxDynaBeans.getStr("CXSJ_CXBM");
			String cxmc = cxDynaBeans.getStr("CXSJ_CXMC");
			
			List<DynaBean> sps = serviceTemplate.selectList("JGMES_PLAN_GDLB",
					" AND JGMES_PLAN_GDLB_ID in (" + StringUtil.buildArrayToString(spIds.split(",")) + ")");
			
			for (DynaBean sp : sps) {
				
				// �ж��Ƿ��Ѿ��������Ų��ƻ�����
				DynaBean gdDynaBeans = serviceTemplate.selectOne("JGMES_PLAN_DETAIL", " and JGMES_PLAN_GDLB_ID='" + sp.getStr("JGMES_PLAN_GDLB_ID") + "'");
				if (gdDynaBeans != null) {
					msg.append(sp.getStr("GDLB_GDHM")+"�Ѿ����Ų�����");
					continue;
				}
				String code=sp.getStr("GDLB_PCZT_CODE");
				System.out.println("code:"+code);
				if (!code.isEmpty()) {
					msg.append(sp.getStr("GDLB_GDHM")+"�Ѿ����Ų�����");
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
				detail.set("DETAIL_YQWCRQ", sp.getStr("GDLB_DDJHRQ"));// Ҫ���깤����
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
				detail.set("DETAIL_CXID", cxId);// ����ID
				detail.set("JGMES_PLAN_GDLB_ID", sp.getStr("JGMES_PLAN_GDLB_ID"));// �����б�_���ID
				// DETAIL.set("JGMES_PLAN_MAIN_ID", pkValue);//�Ų��������ID
				// �޸��Ų�״̬����������
				DynaBean dic = jc.getDic("JGMES_DIC_PCZT","PCZT01");
				detail.set("DETAIL_PCZT_CODE", dic.getStr("DICTIONARYITEM_ITEMCODE"));//���
				detail.set("DETAIL_PCZT_NAME", dic.getStr("DICTIONARYITEM_ITEMNAME"));//����
				detail.set("DETAIL_PCZT_ID", dic.getStr("JE_CORE_DICTIONARYITEM_ID"));//id
				
				detail.set("DETAIL_QGRQ", sp.getStr("GDLB_QGRQ"));// �빺����
				detail.set("DETAIL_OTDRQ", sp.getStr("GDLB_OTDRQ"));// OTD����
				detail.set("DETAIL_KHBM", sp.getStr("GDLB_KHBM"));// �ͻ�����
				detail.set("DETAIL_KHMC", sp.getStr("GDLB_KHMC"));// �ͻ�����
				detail.set("DETAIL_CPXH", sp.getStr("GDLB_CPXH"));// ��Ʒ�ͺ�

				detail.set("DETAIL_DDSL", sp.getStr("GDLB_DDSL"));// ��������

				String GDLB_WPCSL = sp.getStr("GDLB_WPCSL");// ����δ������
				detail.set("DETAIL_YPCSL", sp.getStr("GDLB_WPCSL"));// ���Ų�����

				int GDLB_XPCSL = sp.getInt("GDLB_XPCSL");// �������Ų�����
				int GDLB_WCSL = sp.getInt("GDLB_WCSL");// �����������
				if (GDLB_WCSL > 0) {
					// ��ʾ�Ѿ���ʼ����
					detail.set("DETAIL_XPCSL", GDLB_XPCSL - GDLB_WCSL);// ���Ų�����
					detail.set("DETAIL_WPCSL", GDLB_XPCSL - GDLB_WCSL);// δ�Ų�����
				} else {
					detail.set("DETAIL_XPCSL", GDLB_XPCSL);// ���Ų�����
					detail.set("DETAIL_WPCSL", GDLB_WPCSL);// δ�Ų�����
				}
				
				//��ӵ�ǰ������Ϣ
				String productNum=sp.getStr("GDLB_CPBH",null);
				if (productNum==null) {
					throw new PCExcuteException("��Ʒ��Ż�ȡʧ�ܡ�");
				}
				int capacity=getCapacity(productNum, cxId);//��ȡ�ղ���
				System.out.println("����:"+capacity);
				detail.set("DETAIL_DQCN",capacity);
				//��Ӽӹ�ʱ����Ϣ,��
				int unfinished=detail.getInt("DETAIL_WPCSL");
				float f1=(float)unfinished/capacity;
				//������λС�������ҵ���λ����0ʱ��ǰ��һλ
				int num = (int) (f1 * 1000);
				float unfinishDay =0;
				 if (num % 10 > 0) {
					 unfinishDay = (num -num % 10 + 10 * 1.0f) / 1000.0f;
				 }else {
					 unfinishDay =num * 1.0f / 1000.0f;
				 }
				System.out.println("��ʱ��:"+unfinishDay);
				detail.set("DETAIL_JGSC", unfinishDay);
				
				// �޸Ĺ����б��Ӧ���ݡ�
				sp.set("CXSJ_CXBM", cxbm);
				sp.set("CXSJ_CXMC", cxmc);
				sp.set("GDLB_PCZT_CODE", dic.getStr("DICTIONARYITEM_ITEMCODE"));//���
				sp.set("GDLB_PCZT_NAME", dic.getStr("DICTIONARYITEM_ITEMNAME"));//����
				serviceTemplate.update(sp);
				/*
				 * Date dt = new Date(); String strDate=DateUtil.formatDate(dt, "yyyy-MM-dd");
				 * DETAIL.set("DETAIL_PCRQ", strDate);
				 */

				serviceTemplate.buildModelCreateInfo(detail);
				serviceTemplate.insert(detail);
			
			}
			toWrite(jsonBuilder.returnSuccessJson("\"ִ�гɹ�:</br>"+msg.toString()+"\""));
		
		} catch (Exception e) {
			toWrite(jsonBuilder.returnFailureJson("\"ִ��ʧ��:</br>"+e.getMessage()+"\""));
		}
		
	}
	
	/**
	 * �����б���������������ݵĲ��ܺͼӹ�ʱ������ֵ������ֵ�Ĳ��ḳֵ
	 */
	public void updateCapacityAndUnfinishDay() {
		StringBuilder msg=new StringBuilder(2000);
		boolean toerror=false;
		String tableName = request.getParameter("tableName");
		List<DynaBean> tableDynaBeans=serviceTemplate.selectList(tableName,"");
		if (tableDynaBeans==null||tableDynaBeans.size()<1) {
			toWrite(jsonBuilder.returnFailureJson("\"ϵͳ�쳣�������ԡ�\""));
			return;
		}
		String target =tableName.split("_")[tableName.split("_").length-1];//����ǰ׺
		for (DynaBean dynaBean : tableDynaBeans) {
			String dqcn=dynaBean.getStr(target+"_DQCN");
			String jgsc=dynaBean.getStr(target+"_JGSC");
			if (dqcn==null||"".equals(dqcn)||jgsc==null||"".equals(jgsc)) {
				try {
					setCapacityAndUnfinishDay(tableName, dynaBean.getPkValue());
					msg.append("�������ţ�"+dynaBean.getStr(target+"_GDHM")+":");
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
			toWrite(jsonBuilder.returnFailureJson("\"ִ�����:</br>���У�"+msg+"\""));
		}else {
			toWrite(jsonBuilder.returnSuccessJson("\"ִ�гɹ�:</br>"+msg.toString()+"\""));
		}
	}
	
	/**
	 * ���ݱ�������������������ܺͼӹ�ʱ���������ݸ�ֵ
	 * @param tableName ����
	 * @param primaryKeys �����ַ���
	 */
	private void setCapacityAndUnfinishDay(String tableName,String primaryKeys) {
		
		if (tableName==null||"".equals(tableName)) {
			throw new PCExcuteException("��ȡ����������ơ�");
		}
		if(primaryKeys==null||"".equals(primaryKeys)) {
			throw new PCExcuteException("��ȡ������������");
		}
		List<DynaBean> tableDynaBeans=serviceTemplate.selectList(tableName, "and "+tableName+"_ID "
				+ "in("+StringUtil.buildArrayToString(primaryKeys.split(",")) +")");
		if(tableDynaBeans==null) {
			throw new PCExcuteException("��ȡ�������쳣");
		}
		for (DynaBean tableDynaBean : tableDynaBeans) {
			String target =tableName.split("_")[tableName.split("_").length-1];//����ǰ׺
			//ƴ�Ӳ�Ʒ�������
			String postfix="_CPBH";//��Ʒ������ƺ�׺
			logger.debug("��Ʒ���"+target+postfix);
			String productNum=tableDynaBean.getStr(target+postfix);//��Ʒ���
			//ƴ�Ӳ���id����
			String productionID=null; 
			if("PCLB".equals(target)) {
				//��ȡ����ID
				DynaBean pclbDynaBean=serviceTemplate.selectOne("JGMES_BASE_CXSJ","and CXSJ_CXBM='"+tableDynaBean.getStr("PCLB_CXBM")+"'");
				if (pclbDynaBean==null) {
					throw new PCExcuteException("�Ų������ݻ�ȡʧ�ܡ�");
				}
				productionID=pclbDynaBean.getStr("JGMES_BASE_CXSJ_ID");
			}else if("DETAIL".equals(target)) {
				productionID=tableDynaBean.getStr("DETAIL_CXID");
			}else {
				throw new PCExcuteException("������¼�޷�������ܣ��ӹ�ʱ��");
			}
			//��ӵ�ǰ������Ϣ
			if (productNum==null) {
				throw new PCExcuteException("��Ʒ��Ż�ȡʧ�ܡ�");
			}
			int capacity=getCapacity(productNum, productionID);//��ȡ�ղ���
			System.out.println("����:"+capacity);
			tableDynaBean.set(target+"_DQCN",capacity);
			//��Ӽӹ�ʱ����Ϣ,��
			int unfinished=tableDynaBean.getInt(target+"_WPCSL");
			double unfinishDay =Math.scalb((double)unfinished/capacity+0.004, 2);
			System.out.println("��ʱ��:"+unfinishDay);
			tableDynaBean.set(target+"_JGSC", unfinishDay);
		}
	}
	
	/**
	 * ���ݲ�Ʒ��źͲ���ID��ѯ��Ʒ���߲��ܱ�������ղ��ܲ�����
	 * ����1����Ʒ��� ����2������ID
	 * @return ��ǰ����  Ĭ��ֵ����0
	 */
	private int getCapacity(String productNum,String productionID) {
		int capacity=0;//����
		DynaBean capDynaBean = serviceTemplate.selectOne("JGMES_BASE_CPCXCNINFO", "and CPCXCNINFO_BH='"+productNum+"' and CPCXCNINFO_CXid='"+productionID+"'");
		if(capDynaBean==null) {
			throw new PCExcuteException("δ�ҵ���Ʒ���߲�����Ϣ��");
		}
		//��ȡ�����Ϣ
		DynaBean cxDynaBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ", "and JGMES_BASE_CXSJ_ID='"+productionID+"'");//��ѯ��������
		if (cxDynaBean==null) {
			throw new PCExcuteException("������Ϣ��ȡʧ�ܡ�����ϵ����Ա");
		}
		String flightNum=cxDynaBean.getStr("DEPTCODE");//������
		String ways=capDynaBean.getStr("CPCXCNINFO_CNJSFS_CODE");
		if ("CNJSFS01".equals(ways)) {//��Сʱ���ܼ���
			int shift = getShiftTime(flightNum);//���ݰ����Ų�ѯ�����ʱ��
			int hoursWork=capDynaBean.getInt("CPCXCNINFO_XSCN",0);//Сʱ����
			if (hoursWork<1) {
				throw new PCExcuteException("��ƷСʱ���ܻ�ȡʧ�ܡ���ȷ�������Ƿ����");
			}
			//������ղ���
			capacity=shift*hoursWork;
		} else if("CNJSFS02".equals(ways)){//���ղ��ܼ���
			//������ղ�����ֱ�ӷ���
			capacity=capDynaBean.getInt("CPCXCNINFO_RCN",0);
		}else {
			throw new PCExcuteException("δ¼��������ݡ�");
		}
		
		return capacity;
	}
	
	/**
	 * �ݹ��ȡ��Ч�İ������
	 * @param flightNum ������
	 * @return  ���õİ�����
	 */
	private String getFlightNum(String flightNum) {
		if (flightNum==null||flightNum.isEmpty()) {
			throw new PCExcuteException("�����а����Ż�ȡʧ�ܡ�");
		}
		//����ʵ��ID��ѯ�����Ϣ
		DynaBean bcDynaBean = serviceTemplate.selectOne("JGMES_BASE_BC","and BC_STID='"+flightNum+"' "
				+ "and BC_STATUS_CODE='1' and BC_STARTDATE<NOW() and BC_ENDDATE>NOW()");
		if (bcDynaBean==null||bcDynaBean.getStr("BC_STID")==null||bcDynaBean.getStr("BC_STID").isEmpty()) {//�鲻��ʱ
			//���ݰ�α������֯�������Ҹ����İ�α��
			DynaBean zzDynaBean = serviceTemplate.selectOne("JE_CORE_DEPARTMENT","and DEPTCODE='"+flightNum+"'");
			//��֤��֯������Ƿ���ڼ�¼
			if (zzDynaBean==null) {
				logger.debug("��֯�����δ�ҵ������Ϣ");
				throw new PCExcuteException("��֯�����δ�ҵ������Ϣ������ϵ����Ա��");
			}
			String deptID =zzDynaBean.getStr("PARENT");//���ڵ�ID
			if (deptID==null||deptID.isEmpty()) {
				logger.debug("���ڵ�ID:"+deptID+"δ�ҵ���");
				return null;
			}
			DynaBean parentDynaBean=serviceTemplate.selectOne("JE_CORE_DEPARTMENT","and DEPTID='"+deptID+"'");
			flightNum=parentDynaBean.getStr("DEPTCODE");//���ڵ������
			if (flightNum==null||flightNum.isEmpty()) {
				logger.debug("���ڵ�ID:"+deptID+"û�а�α��");
				throw new PCExcuteException("��֯�����δ�ҵ������Ϣ������ϵ����Ա��");
			}
			flightNum=getFlightNum(flightNum);
		}
		return flightNum;
	}
	
	/**
	 * ���ݰ�������ѯ���ʱ�䣬��ѯ����ʱ��ѯ���ڵ��Ӧ�����Ϣ
	 * ʹ�õ��ݹ�
	 * @param flightNum
	 * @return �����ʱ��
	 */
	private int getShiftTime(String flightNum) {
	    int shiftTime=0;
	    //��ȡ��Ч�İ�����
	    flightNum = getFlightNum(flightNum);
	    if (flightNum==null) {
			shiftTime=8;
		} else {
			 //��ȡ�����Ϣ
		    DynaBean bcDynaBean = serviceTemplate.selectOne("JGMES_BASE_BC"," and BC_STID='"+flightNum+"' "
		    		+ "and BC_STATUS_CODE='1' and BC_STARTDATE<NOW() and BC_ENDDATE>NOW()");
		    try {
		    	shiftTime=bcDynaBean.getInt("BC_DQBCZSJ");
			} catch (Exception e) {
				throw new PCExcuteException("�����ʱ���ʽ���󡣰�����Ϊ��"+flightNum);
			}
		    //�����ʱ��Ϊ0ʱ����ͨ����ʼ�ͽ���ʱ���ȡ����ʱ��
		    if (shiftTime<1||shiftTime>24) {
				throw new PCExcuteException("��α��а����ʱ���������ʵ��");
			}
		}
	   
	    
	    return shiftTime;
	}
	
	/**
	 * ���ݴ���Ŀ�ʼ�ͽ���ʱ�����������Сʱ��
	 * @param startTime ��ʼʱ��
	 * @param endTime ����ʱ��
	 * @return ���ʱ�䣬 Ĭ��Ϊ8Сʱ
	 */
	private int getTime(String startTime,String endTime) {
		int time=0;
		SimpleDateFormat myFormatter = new SimpleDateFormat("HH:mm:ss");
		long day = 0;
		try {
	    Date date = myFormatter.parse(startTime);
		Date mydate = myFormatter.parse(endTime);
	
        day = (date.getTime() - mydate.getTime()) /(1000*60*60);
	
        //���ﾫȷ�����룬���ǿ����������ʱ��ʱ�侫ȷ����
		} catch (Exception e) {
			throw new PCExcuteException("���ʱ���쳣��");
		}
		time=(int)day;
		
		return time;
	}

	/**
	 * �Զ��Ų�����������
	 */
	public void zdSubmitNewVerson() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
		try {
			// ������������
			String spIds = request.getParameter("spIds");
			List<DynaBean> sps = serviceTemplate.selectList("JGMES_PLAN_DETAIL",
					" AND JGMES_PLAN_DETAIL_ID in (" + StringUtil.buildArrayToString(spIds.split(",")) + ")");
			DynaBean dic = jgmesCommon.getDic("JGMES_DIC_PCZT","PCZT04");
			for (DynaBean detail : sps) {
				
				// ��ȷ���Ų��������Ų���״̬
				String pcCode = detail.getStr("DETAIL_PCZT_CODE");
				if("PCZT04".equals(pcCode)) {
					System.out.println("���Ų����");
					continue;
				}
				DynaBean isSkipAPS=serviceTemplate.selectOne("JGMES_XTGL_XTCS"," and XTCS_CXFL2_CODE='IsSkipAPS'");
				if(isSkipAPS!=null&&"0".equals(isSkipAPS.getStr("XTCS_CSZ"))) {
					System.out.println("�����Զ��Ų�");
				}else {
					if (!"PCZT02".equals(pcCode)) {
						toWrite(jsonBuilder.returnFailureJson("\"��ȷ�����й����Ѿ��Ų����" + "\""));
						return;
					}
				}
				
				// �жϵ�����������
				if (detail.getStr("DETAIL_JHKGSJ") == null && detail.getStr("DETAIL_JHWGSJ") == null) {
					toWrite(jsonBuilder.returnFailureJson("\"�ƻ��������ڻ�ƻ��깤���ڲ���ȷ����ȷ��" + "\""));
					return;
				}
				Date date=null;
				Date date2=null;
				try {
					date = format.parse(detail.getStr("DETAIL_JHKGSJ"));
					date2 = format.parse(detail.getStr("DETAIL_JHWGSJ"));
				} catch (ParseException e) {
					toWrite(jsonBuilder.returnFailureJson("�ƻ��������ڻ�ƻ��깤���ڴ���"));
					return;
				}
				int rwDays = differentDays(date, date2);
				
				// ��ȡ��Ҫ�Ų�����
				if(detail.getStr("DETAIL_XPCSL")==null&&detail.getStr("DETAIL_XPCSL").isEmpty()) {
					toWrite(jsonBuilder.returnFailureJson("\"����"+detail.getStr("GDLB_GDHM")+"���Ų�����Ϊ0\""));
					return;
				}
				int xpcsl = Integer.parseInt(detail.getStr("DETAIL_XPCSL"));
				// �ֽ�����
				if (rwDays <1) {
					rwDays = 1;
				}
				int fwpcsl = xpcsl;//ʣ���Ų�����
				int syday = rwDays;//ʣ������
				if (fwpcsl==0) {
					toWrite(jsonBuilder.returnFailureJson("���Ų�����Ϊ0"));
					return;
				}
				System.out.println("ʣ���Ų�������"+fwpcsl+"ʣ������"+syday);
				logger.debug("ʣ���Ų�������"+fwpcsl+"ʣ������"+syday);
				//���ݿ���ʱ������������
				for (int i = 0; i < rwDays; i++) {
					DynaBean bean = new DynaBean("JGMES_PLAN_SCRW", true);// �������񵥶���				
					jgmesCommon.setDynaBeanInfo(bean);
					//serviceTemplate.buildModelCreateInfo(bean);
					String  RWDH = serviceTemplate.buildCode("SCRW_RWDH", "JGMES_PLAN_SCRW", bean);
					//System.out.println("���񵥺�=" + RWDH);
					bean.set("SCRW_RWDH", RWDH);
					bean.set("SCRW_PCRQ",format.format(date));//�Ų�����
					System.out.println("�Ų����ڣ�"+format.format(date));
					int dtsl = mathGZL(syday--, fwpcsl);//��ȡ�����Ų�����
					System.out.println("ʣ���Ų�������"+fwpcsl+"ʣ������"+syday);
					logger.debug("ʣ���Ų�������"+fwpcsl+"ʣ������"+syday);
					System.out.println("����������"+dtsl);
					bean.set("SCRW_PCZT_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));// �Ų�״̬_NAME
					bean.set("SCRW_PCZT_CODE", dic.get("DICTIONARYITEM_ITEMCODE") );// �Ų�״̬
					bean.set("SCRW_PCZT_ID",dic.get("JE_CORE_DICTIONARYITEM_ID"));// �Ų�״̬_ID
					bean= newVerson(bean, detail, dtsl);//д���������
					serviceTemplate.insert(bean);
					fwpcsl = fwpcsl - dtsl;
					date=getPCRQ(date);
				}
				//�����Ų����Ų�״̬Ϊ����Ų�
				detail.set("DETAIL_PCZT_CODE", dic.get("DICTIONARYITEM_ITEMCODE"));
				detail.set("DETAIL_PCZT_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));
				detail.set("DETAIL_PCZT_ID", dic.get("JE_CORE_DICTIONARYITEM_ID"));
				serviceTemplate.update(detail);

				// ��д�����������Ϣ
				DynaBean gdDynaBean = serviceTemplate.selectOne("JGMES_PLAN_GDLB",
						" AND JGMES_PLAN_GDLB_ID ='" + detail.getStr("JGMES_PLAN_GDLB_ID") + "'");
				int ypcsl=Integer.parseInt(detail.getStr("DETAIL_YPCSL")==null?"0":detail.getStr("DETAIL_YPCSL"));
				ypcsl=ypcsl+xpcsl;
				int wpcslNew=Integer.parseInt(gdDynaBean.getStr("GDLB_DDSL"));
				wpcslNew=wpcslNew-ypcsl;
				gdDynaBean.set("GDLB_YPCSL", ypcsl);// ���Ų�����
				gdDynaBean.set("GDLB_WPCSL", wpcslNew);//δ������
				gdDynaBean.set("GDLB_PCZT_CODE", dic.get("DICTIONARYITEM_ITEMCODE"));
				gdDynaBean.set("GDLB_PCZT_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));
				gdDynaBean.set("GDLB_PCZT_ID", dic.get("JE_CORE_DICTIONARYITEM_ID"));
				//gdDynaBean.set("GDLB_RWDH", RWDH);// ���񵥺�
				serviceTemplate.update(gdDynaBean);
				//ɾ��������������Ų���¼
//				serviceTemplate.deleteByIds(detail.getStr("JGMES_PLAN_DETAIL_ID"), "JGMES_PLAN_DETAIL", "JGMES_PLAN_DETAIL_ID");
//				System.out.println("�Ų���¼ɾ��");
			
			}
			
			toWrite(jsonBuilder.returnSuccessJson("\"�����ɹ�\""));
		} catch (Exception e) {
			e.printStackTrace();
			toWrite(jsonBuilder.returnFailureJson("\"��̨���ִ���,����ϵ�������Ա��" + e.getMessage()==null?"":e.getMessage() + "\""));
		}
	 }
	
/**
 * ���������������һ��󷵻�
 * @param date 
 * @return
 */
	private Date getPCRQ(Date date) {
		Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, +1);//+1�����ʱ���һ��
        date = calendar.getTime();
        return date;
	}


	/**
	 * ����ÿ�����������
	 * 
	 * @param rwDays ������
	 * @return �������������
	 */
	private int mathGZL(int rwDays, Integer xpcsl) {
		//System.out.println("���㵱����������" + rwDays + "+" + xpcsl);
		Integer mrsl = xpcsl / rwDays;
		Integer rem = 0;
		if ((rem = xpcsl % rwDays) != 0) {
			mrsl = mrsl + rem;
		}
		return mrsl;
	}

	/**
	 * �ж������ڵ����������date1Ϊ��ʼʱ�䣬date2Ϊ����ʱ��
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
		if (year1 != year2) {// ͬһ��
			int timeDistance = 0;
			for (int i = year1; i < year2; i++) {
				if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0) // ����
				{
					timeDistance += 366;
				} else // ��������
				{
					timeDistance += 365;
				}
			}
			return timeDistance + (day2 - day1);
		} else // ��ͬ��
		{
			System.out.println("�ж�day2 - day1 : " + (day2 - day1));
			return day2 - day1;
		}
	}

	/**
	 * ���ɷ����������� ֵ��DynaBean ������������scrwDB���Ų��ƻ������detailDB�� String
	 * ������������dtsl(SCRW_DTSL)
	 * 
	 */
	private DynaBean newVerson(DynaBean scrwDB, DynaBean detailDB, int dtsl) {
		
		//System.out.println("��������" + scrwDB + "+" + detailDB + "+" + dtsl);
		String BBH = detailDB.getStr("DETAIL_BBH") ;
		if (BBH != null && !BBH.isEmpty()) {
			BBH = 1 + Integer.parseInt(BBH) + "";
		}
		scrwDB.set("SCRW_JHKGSJ", detailDB.getStr("DETAIL_JHKGSJ") );// �ƻ�����ʱ��
		scrwDB.set("SCRW_PCDBH", detailDB.getStr("DETAIL_PCDBH") );// �Ų������
		scrwDB.set("SCRW_PCDMC", detailDB.getStr("DETAIL_PCDMC"));// �Ų�������
		scrwDB.set("SCRW_CPBH", detailDB.getStr("DETAIL_CPBH"));// ��Ʒ���
		scrwDB.set("SCRW_NAME", detailDB.getStr("DETAIL_NAME"));// ��Ʒ����
		scrwDB.set("SCRW_GDZT_CODE", detailDB.getStr("DETAIL_GDZT_CODE"));// ����״̬
		scrwDB.set("SCRW_BZ", detailDB.getStr("DETAIL_BZ"));// ��ע
		
		scrwDB.set("SCRW_WCL", detailDB.getStr("DETAIL_WCL"));// �����
		scrwDB.set("SCRW_SJKGSJ", detailDB.getStr("DETAIL_SJKGSJ") );// ʵ�ʿ���ʱ��
		scrwDB.set("SCRW_CPGG", detailDB.getStr("DETAIL_CPGG") );// ��Ʒ���
		scrwDB.set("SCRW_DDHM", detailDB.getStr("DETAIL_DDHM"));// ��������
		scrwDB.set("SCRW_GDHM", detailDB.getStr("DETAIL_GDHM") );// ��������
		scrwDB.set("SCRW_DEPTNAME", detailDB.getStr("DETAIL_DEPTNAME") );// ��������

		scrwDB.set("SCRW_LCKH", detailDB.getStr("DETAIL_LCKH") );// ���̿���
		scrwDB.set("SCRW_GDZT_NAME", detailDB.getStr("DETAIL_GDZT_NAME"));// ����״̬_NAME
		scrwDB.set("SCRW_WCSL", detailDB.getStr("DETAIL_WCSL") );// �������
		scrwDB.set("SCRW_JHWGSJ", detailDB.getStr("DETAIL_JHWGSJ") );// �ƻ��깤ʱ��
		scrwDB.set("SCRW_DEPTCODE", detailDB.getStr("DETAIL_DEPTCODE"));// ���ű���
		//scrwDB.set("SCRW_PCRQ", detailDB.getStr("DETAIL_PCRQ") );// �Ų�����
		scrwDB.set("SCRW_SJWGSJ", detailDB.getStr("DETAIL_SJWGSJ") );// ʵ���깤ʱ��
		scrwDB.set("SCRW_PGSL", detailDB.getStr("DETAIL_PGSL") );// �ɹ�����
		scrwDB.set("SCRW_CXBM", detailDB.getStr("DETAIL_CXBM") );// ���߱���
		scrwDB.set("SCRW_CXMC", detailDB.getStr("DETAIL_CXMC") );// ��������
		scrwDB.set("JGMES_PLAN_GDLB_ID", detailDB.getStr("JGMES_PLAN_GDLB_ID") );// �����б�_���ID
		scrwDB.set("SCRW_GDSL", detailDB.getStr("DETAIL_GDSL") );// ��������
		scrwDB.set("SCRW_DDSL", detailDB.getStr("DETAIL_DDSL"));// ��������
		
		// bean.set("JGMES_PLAN_MAIN_ID", DETAIL.get("JGMES_PLAN_MAIN_ID") + "");//
		// �Ų�������_���ID
		scrwDB.set("SCRW_YPCSL", detailDB.getStr("DETAIL_YPCSL"));// ���Ų�����
		scrwDB.set("SCRW_WPCSL", detailDB.getStr("DETAIL_WPCSL") );// δ�Ų�����
		scrwDB.set("SCRW_QGRQ", detailDB.getStr("DETAIL_QGRQ"));// �빺����
		scrwDB.set("SCRW_OTDRQ", detailDB.getStr("DETAIL_OTDRQ") );// OTD����
		scrwDB.set("SCRW_XPCSL", detailDB.getStr("DETAIL_XPCSL") );// ���Ų�����
		scrwDB.set("SCRW_KHBM", detailDB.getStr("DETAIL_KHBM") );// �ͻ�����
		scrwDB.set("SCRW_KHMC", detailDB.getStr("DETAIL_KHMC") );// �ͻ�����
		scrwDB.set("SCRW_CPXH", detailDB.getStr("DETAIL_CPXH") );// ��Ʒ�ͺ�
		scrwDB.set("SCRW_DTSL", dtsl);// ��������
		scrwDB.set("SCRW_BBH", BBH);// �汾��
		scrwDB.set("SCRW_PCSL", dtsl);// �Ų�����
		scrwDB.set("SCRW_RWZT_NAME", "������");// ����״̬_NAME
		scrwDB.set("SCRW_RWZT_CODE", "RWZT01");
		scrwDB.set("SCRW_SCRWDATASOURCE_CODE","PC");//������Դ
		//����Ÿ��ݲ��߷���
		DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", 
				" and SCRW_CXBM='"+scrwDB.getStr("SCRW_CXBM")+"' order by SY_ORDERINDEX desc limit 0,1");
		int orderindex =0;
		if (scrwDynaBean==null) {//�״η�������ʱ���Ϊ1
			orderindex=1;
		}else {
			orderindex = scrwDynaBean.getInt("SY_ORDERINDEX")+1;
		}
		scrwDB.set("SY_ORDERINDEX", orderindex);// �����ֶ�
		 
		//serviceTemplate.insert(scrwDB);
		//System.out.println("�Զ���������+1");
		return scrwDB;

	}
	
	
	/*
	 * ���ݲ�Ʒ��Ż�ȡ�ò�Ʒ��Ӧ�Ĺ����б�
	 * */
	public void getGXSum() {
		String cpbm = request.getParameter("cpbm");//��Ʒ����
		String scrwNo = request.getParameter("scrwNo");// �������񵥺�
		JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
			try {
				if(cpbm!=null&&!"".equals(cpbm)&&scrwNo!=null&&!"".equals(scrwNo)) {
					//��ȡ��Ʒ��Ϣ�еĹ���·��
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
								returnData.set("SY_ORDERINDEX", gylxgxDynaBean.getStr("SY_ORDERINDEX")); //���չ���˳���
								returnData.set("GYLXGX_GXNUM", gylxgxDynaBean.getStr("GYLXGX_GXNUM")); //������
								returnData.set("GYLXGX_GXNAME", gylxgxDynaBean.getStr("GYLXGX_GXNAME")); //��������
								//returnData.set("SY_ORDERINDEX", gylxgxDynaBean.getStr("SY_ORDERINDEX")); //����ģʽ
								if(scrwDynaBean!=null) {
									returnData.set("SCRW_PGSL", scrwDynaBean.getStr("SCRW_PCSL")); //���ɹ�����  ����ʱȡ�����Ų�����
								}
								returnData.set("Already_Number", sumDynaBean.get(0).getInt("BGSJ_SL_SUM")+sumDynaBean.get(0).getInt("BGSJ_BLSL_SUM")); //�ѱ�������
								/* ��ȡ�ù����£��Ƿ����������ı��� */
								List<DynaBean> dynaBeans = serviceTemplate.selectList("JGMES_PB_BGSJ", "and BGSJ_SCRW = '" + scrwNo + "' and BGSJ_GXBH = '" + gylxgxDynaBean.getStr("GYLXGX_GXNUM") + "' GROUP BY BGSJ_TMH");
								/* �����ͣ��Ƿ񱨹���1Ϊ�ǣ�0Ϊ�� */
								returnData.set("Already_Number_LCX", dynaBeans.size());
								/* ��ȡ����վ���� */
								String lyGx = gylxgxDynaBean.getStr("GYLXGX_ID");//��Դ����·�߹���ID
								if (StringUtil.isNotEmpty(lyGx)){
									/* ���ݹ���ID��ȡ��λ */
									List<DynaBean> jgmes_base_cpgwgx = serviceTemplate.selectList("JGMES_BASE_CPGWGX", "and CPGWGX_CPBH='" + cpbm + "' and CPGWGX_GYGXID='" + lyGx + "'","CPGWGX_GWBH");
									if (jgmes_base_cpgwgx.size()>0) {
										StringBuilder gwListStr = new StringBuilder();
										for (DynaBean jgmesBaseCpgwgx : jgmes_base_cpgwgx) {
											gwListStr.append("'"+jgmesBaseCpgwgx.getStr("CPGWGX_GWBH")+"'"+",");
										}
										if (StringUtil.isNotEmpty(gwListStr.toString()))
										{
											String substring = gwListStr.substring(0, gwListStr.length() - 1);
											/* ��ȡ���޵� */
											List<DynaBean> fxdBean = serviceTemplate.selectList("JGMES_PB_FXD", "and FXD_LYGWBH in(" + substring + ") and FXD_RWDH='" + scrwNo + "'");
											returnData.set("Fx_Number", fxdBean.size());
										}

									}

								}

								//returnData.set("SY_ORDERINDEX", gylxgxDynaBean.getStr("SY_ORDERINDEX")); //��ҵ˵��
								returnData.set("GYLXGX_ID", gylxgxDynaBean.getStr("GYLXGX_ID")); //���չ����������
								returnList.add(returnData);
							}
							if(returnList!=null&&returnList.size()>0) {
								ret.Data = ret.getValues(returnList);
								ret.TotalCount = (long) returnList.size();
							}else {
								ret.setMessage("δ��ȡ�����ݣ�");
							}
						}else {
							ret.setMessage("δ����·�߹���");
						}
					}else {
						ret.setMessage("δ���ݲ�Ʒ�����ȡ����Ʒ��Ϣ���߸Ĳ�Ʒ�Ĺ���·��Ϊ�գ�");
					}
				}else {
					ret.setMessage("δ��ȡ����Ʒ��������������񵥺ţ�");
				}
			} catch (Exception e) {
				// TODO: handle exception
				ret.setMessage("ϵͳ�쳣������ϵ����Ա��");
				e.printStackTrace();
			}

			toWrite(jsonBuilder.toJson(ret));
	}

	/**
	 * @author liuc
	 * ��ȡϵͳ����
	 * @see /jgmes/jgmesBaseAction!getXTCS.action
	 */
	public void getXTCS(){
		// MAC��ַ
		String mac = request.getParameter("mac");
		// �û�����  ����
		String userCode = request.getParameter("userCode");
		// ��������
		String csfl = request.getParameter("csfl");

		JgmesResult<Integer> ret = new JgmesResult<Integer>();
		String jsonStr = "";
		// У��Ϸ��ԣ��Ƿ��ص�½
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
	 * У���û��Ϸ��ԣ����Ϸ�ֱ����ʾ��
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