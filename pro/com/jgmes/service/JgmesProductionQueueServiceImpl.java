package com.jgmes.service;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.je.core.action.DynaAction;
//import org.apache.log4j.Logger;
import com.je.core.security.SecurityUserHolder;
import com.je.core.service.PCDynaServiceTemplate;
import com.je.core.service.PCServiceTemplate;
import com.je.core.util.SpringContextHolder;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.DynaBean;
import com.je.rbac.service.UserManager;
import com.je.table.exception.PCExcuteException;
import com.jgmes.action.JgmesBaseAction;
import com.jgmes.util.JgmesCommon;
import com.je.rbac.model.Department;
import com.je.rbac.model.EndUser;
/**
 * ����������ز���ʵ����
 * @author cj
 * @version 2019-05-08 20:49:20
 */
@Component("jgmesProductionQueueService")
public class JgmesProductionQueueServiceImpl implements JgmesProductionQueueService  {
	private static final Logger logger = LoggerFactory.getLogger(JgmesProductionQueueServiceImpl.class);
	/**???Bean(DynaBean)??????*/
	private PCDynaServiceTemplate serviceTemplate;
	/**???Bean?????????,???????SQL*/
	private PCServiceTemplate pcServiceTemplate;
	/**????????*/
	private UserManager userManager;
	
	public void load(){
		System.out.println("hello serviceimpl");
	}
	
	/**
	 * ����ɾ����������
	 * @param tasks ��������ids
	 */
	@Override
	public void deleteAll(String tasks) {
		if (tasks==null||tasks.isEmpty()||"".equals(tasks)) {
			throw new PCExcuteException("��ȷ���Ƿ�ѡ�����ݡ�");
		}
		List<DynaBean> producDynaBeans = serviceTemplate.selectList("JGMES_PLAN_SCRW",
				" and JGMES_PLAN_SCRW_ID in (" + StringUtil.buildArrayToString(tasks.split(",")) + ")");
		if (producDynaBeans==null||producDynaBeans.size()<1) {
			throw new PCExcuteException("���ݲ����ڣ���ˢ�º����ԡ�");
		}
		for (DynaBean producDynaBean : producDynaBeans) {
			//�ж��Ƿ����б�������
			String producID=producDynaBean.getStr("JGMES_PLAN_SCRW_ID");//��������
			String taskNo = producDynaBean.getStr("SCRW_RWDH");//���񵥺�
			if (!"RWZT01".equals(producDynaBean.getStr("SCRW_RWZT_CODE"))) {
				throw new PCExcuteException("���񵥺�"+taskNo+"������\"������\"״̬���޷�ɾ����");
			}
			if (producID==null||producID.isEmpty()||" ".equals(producID)) {
				throw new PCExcuteException("�������񵥺ţ�"+taskNo+",��ѯ�쳣��������������ϵ����Ա");
			} else {
				List<DynaBean> submittedDynaBean=serviceTemplate.selectList("JGMES_PB_BGSJ", ""
															+ "and BGSJ_SCRWID='"+producID+"'");
				if (submittedDynaBean.size()>0) {
					logger.debug(taskNo+"���б�������,�޷�ɾ����");
					throw new PCExcuteException("���񵥺�"+taskNo+",���б������ݡ�");
				}
				//��ѯ�Ƿ���������
				List<DynaBean> barcodeDynaBean=serviceTemplate.selectList("JGMES_BASE_GDCPTM", "and JGMES_PLAN_SCRW_ID='"+producID+"'");
				if (barcodeDynaBean.size()>0) {
					logger.debug(taskNo+"������������,�޷�ɾ����");
					throw new PCExcuteException("���񵥺�"+taskNo+",�����������ݡ�");
				}
			}
			//��ʼִ��ɾ��
			try {
				String workOrderID=producDynaBean.getStr("JGMES_PLAN_GDLB_ID");
				if (workOrderID==null||"".equals(workOrderID)) {
					logger.debug("���ֶ����������,��ֱ��ɾ����");
				}else {
					//ͬ�������б��Ų�����Ų�����
					synWorkOrder(producDynaBean);
				}
				serviceTemplate.delete(producDynaBean);
			} catch (PCExcuteException e) {
				throw new PCExcuteException(e.getMessage());
			}catch (Exception e) {
				logger.error(e.getMessage());
				throw new PCExcuteException(e.getMessage());
			}
		}
	}
	
	/**
	 * ���ݴ����ɾ�����������񵥣���ͬ�������б�,�Ų����Ų������������
	 * @param producDynaBean �������񵥶���
	 */
	private void synWorkOrder(DynaBean producDynaBean) {
		String taskNo = producDynaBean.getStr("SCRW_RWDH");//���񵥺�
		String workOrderID=producDynaBean.getStr("JGMES_PLAN_GDLB_ID");//����ID
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String production =producDynaBean.getStr("SCRW_CXBM");//���߱��
		//��ѯ����Ӧ�Ĺ���
		DynaBean workOrderDynaBean=serviceTemplate.selectOne("JGMES_PLAN_GDLB", " "
										+ "and JGMES_PLAN_GDLB_ID='"+workOrderID+"'");
		if (workOrderDynaBean==null) {
			throw new PCExcuteException("�������񵥺ţ�"+taskNo+"��ѯ������Ϣʧ�ܡ�");
		}
		//��ȡ�����Ų��������Ų����ڣ��������
		int finishNum=producDynaBean.getInt("SCRW_WCSL",0);
		if (finishNum>0) {
			throw new PCExcuteException("���񵥺ţ�"+taskNo+"�Ѿ���ʼ����");
		}
		int schedulingPTNum = producDynaBean.getInt("SCRW_PCSL",0);//�Ų�����
		String finishData = producDynaBean.getStr("SCRW_PCRQ");//�Ų�����
		//��ʼͬ��
		//�ж��Ų������Ƿ�������Ų�����
		int ypcsl=workOrderDynaBean.getInt("GDLB_YPCSL")-schedulingPTNum;
		int wpcsl=workOrderDynaBean.getInt("GDLB_WPCSL",0)+schedulingPTNum;
		if (ypcsl<0) {
			throw new PCExcuteException("���񵥺ţ�"+taskNo+"�Ų������쳣"+schedulingPTNum);
		}
		if (schedulingPTNum!=0) {
			//�޸��Ų�״̬���Ų�
			workOrderDynaBean.setStr("GDLB_PCZT_CODE","PCZT02");
			workOrderDynaBean.setStr("GDLB_PCZT_NAME","�Ų���");
			workOrderDynaBean.setStr("GDLB_PCZT_ID","2");
		}
		workOrderDynaBean.set("GDLB_YPCSL",ypcsl);
		workOrderDynaBean.set("GDLB_WPCSL",wpcsl);
		//ͬ������
		serviceTemplate.update(workOrderDynaBean);
		//�ж��Ǵ������Ų���ʽ���ɵ�����,�Զ��Ų��򲻲���
		List<DynaBean> pcDynaBeans=serviceTemplate.selectList("JGMES_PLAN_PCLB",""
									+ "and JGMES_PLAN_GDLB_ID='"+workOrderID+"'");
		if (pcDynaBeans.size()>0) {
			for (DynaBean dynaBean : pcDynaBeans) {
				dynaBean.set("PCLB_YPCSL", ypcsl);
				dynaBean.set("PCLB_WPCSL", wpcsl);
				String pcrq="PCLB_RQ";
				try {
					Date date=sdf.parse(finishData);
					sdf=new SimpleDateFormat("dd");
					pcrq+=sdf.format(date);
					System.out.println("��ȡ���ڣ�"+pcrq);
				} catch (ParseException e) {
					logger.error("���񵥺ţ�"+taskNo+"�Ų������쳣��");
					e.printStackTrace();
				}
				//�ж��Ų�����,����
				if (production.equals(dynaBean.getStr("PCLB_CXBM"))&&estimateMonth(finishData, dynaBean.getStr("PCLB_JHKGSJ"))) {
					dynaBean.set(pcrq, 0);
				}
				try {
					//�޸��Ų���
					serviceTemplate.update(dynaBean);
					//ͬ���Ų�����
					String sql="update JGMES_PLAN_MAIN a,(select ifnull(sum(PCLB_GDSL),ifnull(sum(PCLB_DDSL),0)) as DDSL,ifnull(sum(PCLB_YPCSL),0) as YPCSL from JGMES_PLAN_PCLB where JGMES_PLAN_MAIN_ID='"+dynaBean.getStr("JGMES_PLAN_MAIN_ID")+"') b "
				      		+"set a.MAIN_DDL=b.DDSL,a.MAIN_PCL=b.YPCSL "
				            +"where a.JGMES_PLAN_MAIN_ID='"+dynaBean.getStr("JGMES_PLAN_MAIN_ID")+"'";
					pcServiceTemplate.executeSql(sql);
				} catch (PCExcuteException e) {
					throw new PCExcuteException("ϵͳ�쳣������ϵ�������Ա");
				}
			}
		}
		
	}
	
	/**
	 * �ж����������Ƿ�Ϊͬһ��
	 * @return
	 */
	private boolean estimateMonth(String dateOne,String dateTwo) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
		boolean len =false;
		try {
			Date date1=sdf.parse(dateOne);
			Date date2=sdf.parse(dateTwo);
			if (date1.getTime()==date2.getTime()) {
				len=true;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return len;
	}

	
	
	
	
	/**
	 * ?????????
	 * @return
	 */
	public EndUser getCurrentUser() {
		// TODO Auto-generated method stub
		return SecurityUserHolder.getCurrentUser();
	}
	/**
	 * ????????????????
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