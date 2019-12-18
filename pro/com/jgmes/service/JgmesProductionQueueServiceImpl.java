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
 * 生产任务相关操作实现类
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
	 * 批量删除生产任务
	 * @param tasks 生产任务ids
	 */
	@Override
	public void deleteAll(String tasks) {
		if (tasks==null||tasks.isEmpty()||"".equals(tasks)) {
			throw new PCExcuteException("请确认是否选中数据。");
		}
		List<DynaBean> producDynaBeans = serviceTemplate.selectList("JGMES_PLAN_SCRW",
				" and JGMES_PLAN_SCRW_ID in (" + StringUtil.buildArrayToString(tasks.split(",")) + ")");
		if (producDynaBeans==null||producDynaBeans.size()<1) {
			throw new PCExcuteException("数据不存在，请刷新后重试。");
		}
		for (DynaBean producDynaBean : producDynaBeans) {
			//判断是否已有报工数据
			String producID=producDynaBean.getStr("JGMES_PLAN_SCRW_ID");//任务单主键
			String taskNo = producDynaBean.getStr("SCRW_RWDH");//任务单号
			if (!"RWZT01".equals(producDynaBean.getStr("SCRW_RWZT_CODE"))) {
				throw new PCExcuteException("任务单号"+taskNo+"不处于\"待生产\"状态，无法删除。");
			}
			if (producID==null||producID.isEmpty()||" ".equals(producID)) {
				throw new PCExcuteException("根据任务单号："+taskNo+",查询异常（主键）。请联系管理员");
			} else {
				List<DynaBean> submittedDynaBean=serviceTemplate.selectList("JGMES_PB_BGSJ", ""
															+ "and BGSJ_SCRWID='"+producID+"'");
				if (submittedDynaBean.size()>0) {
					logger.debug(taskNo+"已有报工数据,无法删除。");
					throw new PCExcuteException("任务单号"+taskNo+",已有报工数据。");
				}
				//查询是否生产条码
				List<DynaBean> barcodeDynaBean=serviceTemplate.selectList("JGMES_BASE_GDCPTM", "and JGMES_PLAN_SCRW_ID='"+producID+"'");
				if (barcodeDynaBean.size()>0) {
					logger.debug(taskNo+"已有条码数据,无法删除。");
					throw new PCExcuteException("任务单号"+taskNo+",已有条码数据。");
				}
			}
			//开始执行删除
			try {
				String workOrderID=producDynaBean.getStr("JGMES_PLAN_GDLB_ID");
				if (workOrderID==null||"".equals(workOrderID)) {
					logger.debug("是手动导入的任务,可直接删除。");
				}else {
					//同步工单列表，排产表和排产主表
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
	 * 根据传入待删除的生产任务单，来同步工单列表,排产表，排产主表相关数据
	 * @param producDynaBean 生产任务单对象
	 */
	private void synWorkOrder(DynaBean producDynaBean) {
		String taskNo = producDynaBean.getStr("SCRW_RWDH");//任务单号
		String workOrderID=producDynaBean.getStr("JGMES_PLAN_GDLB_ID");//工单ID
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String production =producDynaBean.getStr("SCRW_CXBM");//产线编号
		//查询出对应的工单
		DynaBean workOrderDynaBean=serviceTemplate.selectOne("JGMES_PLAN_GDLB", " "
										+ "and JGMES_PLAN_GDLB_ID='"+workOrderID+"'");
		if (workOrderDynaBean==null) {
			throw new PCExcuteException("根据任务单号："+taskNo+"查询工单信息失败。");
		}
		//获取任务单排产数量，排产日期，完成数量
		int finishNum=producDynaBean.getInt("SCRW_WCSL",0);
		if (finishNum>0) {
			throw new PCExcuteException("任务单号："+taskNo+"已经开始生产");
		}
		int schedulingPTNum = producDynaBean.getInt("SCRW_PCSL",0);//排产数量
		String finishData = producDynaBean.getStr("SCRW_PCRQ");//排产日期
		//开始同步
		//判断排产数量是否大于已排产数量
		int ypcsl=workOrderDynaBean.getInt("GDLB_YPCSL")-schedulingPTNum;
		int wpcsl=workOrderDynaBean.getInt("GDLB_WPCSL",0)+schedulingPTNum;
		if (ypcsl<0) {
			throw new PCExcuteException("任务单号："+taskNo+"排产数量异常"+schedulingPTNum);
		}
		if (schedulingPTNum!=0) {
			//修改排产状态待排产
			workOrderDynaBean.setStr("GDLB_PCZT_CODE","PCZT02");
			workOrderDynaBean.setStr("GDLB_PCZT_NAME","排产中");
			workOrderDynaBean.setStr("GDLB_PCZT_ID","2");
		}
		workOrderDynaBean.set("GDLB_YPCSL",ypcsl);
		workOrderDynaBean.set("GDLB_WPCSL",wpcsl);
		//同步工单
		serviceTemplate.update(workOrderDynaBean);
		//判断是从那种排产方式生成的任务单,自动排产则不操作
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
					System.out.println("截取日期："+pcrq);
				} catch (ParseException e) {
					logger.error("任务单号："+taskNo+"排产日期异常。");
					e.printStackTrace();
				}
				//判断排产日期,产线
				if (production.equals(dynaBean.getStr("PCLB_CXBM"))&&estimateMonth(finishData, dynaBean.getStr("PCLB_JHKGSJ"))) {
					dynaBean.set(pcrq, 0);
				}
				try {
					//修改排产表
					serviceTemplate.update(dynaBean);
					//同步排产主表
					String sql="update JGMES_PLAN_MAIN a,(select ifnull(sum(PCLB_GDSL),ifnull(sum(PCLB_DDSL),0)) as DDSL,ifnull(sum(PCLB_YPCSL),0) as YPCSL from JGMES_PLAN_PCLB where JGMES_PLAN_MAIN_ID='"+dynaBean.getStr("JGMES_PLAN_MAIN_ID")+"') b "
				      		+"set a.MAIN_DDL=b.DDSL,a.MAIN_PCL=b.YPCSL "
				            +"where a.JGMES_PLAN_MAIN_ID='"+dynaBean.getStr("JGMES_PLAN_MAIN_ID")+"'";
					pcServiceTemplate.executeSql(sql);
				} catch (PCExcuteException e) {
					throw new PCExcuteException("系统异常，请联系网络管理员");
				}
			}
		}
		
	}
	
	/**
	 * 判断两个日期是否为同一月
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