package com.jgmes.saas;

import com.je.core.facade.extjs.JsonBuilder;
import com.je.core.util.bean.DynaBean;
import com.je.task.service.PcTimedTaskTemplate;
import com.je.task.vo.TimedTaskParamsVo;
import com.jgmes.util.JgmesCommon;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.codehaus.xfire.client.Client;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

//import org.apache.log4j.Logger;

/**
 * Saas版本同步任务处理类
 * 
 * @author xxp
 * @version 2019-03-02 11:51:24
 */
@Component("saasSynchronizeTask")
public class SaasSynchronizeTask extends PcTimedTaskTemplate {

	private final String TAG = "SaasSynchronizeTask";
	// private static Logger logger = Logger.getLogger(SaasSynchronizeTask.class);
	private String SaasWebHost = "";
	private String TenantID = "";
	private String TenantKey = "";
	private String Token = "";
	private String lastDate = "";
	private JsonBuilder jsonBuilder = JsonBuilder.getInstance();

	public void load(TimedTaskParamsVo vo) {

		System.out.println("hello TASK");
	}

	// region

	/**
	 * 获取Token
	 * 
	 * @return
	 * @throws Exception
	 */
	private JSONObject GetAuthToken() throws Exception {
		String strUrl = String.format("%s/services/AuthInterfaceService?wsdl", SaasWebHost);
		URL url = new URL(strUrl);
		Client client = new Client(url);
		Object[] res = client.invoke("authInterface", new Object[] { TenantID, TenantKey });
		String re = (String) res[0];
		return JSONObject.fromObject(re);
	}

	/**
	 * 获取租户有效数据接口
	 *
	 * @return
	 * @throws Exception
	 */
	private JSONObject GetDataInterface(String interfaceType) throws Exception {

		String strUrl = String.format("%s/services/AuthInterfaceService?wsdl", SaasWebHost);
		URL url = new URL(strUrl);
		Client client = new Client(url);
		Object[] res = client.invoke("getTenantSataExecution", new Object[] { Token, interfaceType });
		String re = (String) res[0];
		return JSONObject.fromObject(re);
	}

	// endregion

	// region 基础档案同步

	/**
	 * 基础档案同步
	 * 
	 * @author xxp
	 * @param vo
	 */
	public void BasicFileSyncTask(TimedTaskParamsVo vo) {
		
		JgmesCommon jgmesCommon = new JgmesCommon(null, serviceTemplate);
		Map<String, String> params = vo.getParams();
		if (params != null && params.size() > 0) {
			SaasWebHost = params.get("SaasWebHost");
			TenantID = params.get("TenantID");
			TenantKey = params.get("TenantKey");
			if (!SaasWebHost.equals("") && !TenantID.equals("") && !TenantKey.equals("")) {
				try {
					JSONObject object = GetAuthToken();
					if (object.getBoolean("IsSuccess")) {
						Token = object.getString("Token");

						JSONObject jsonObject = GetDataInterface("BasicData");
						if (jsonObject.getBoolean("IsSuccess")) {
							JSONArray jsonArray = jsonObject.getJSONArray("Data");
							List<DynaBean> getInterfaceList = jgmesCommon.getListDynaBeanByJsonStr("",
									jsonBuilder.toJson(jsonArray));
							if (getInterfaceList != null && getInterfaceList.size() > 0) {
								//延迟执行数据同步
								Random ran = new Random();
								long time = ran.nextInt(600)*1000;
								//long time = 10000;
								new Thread(){
						            public void run(){
						               try {
						            	  System.out.println("开始同步："+"时间"+new Date()+":"+time+":"+Thread.currentThread().getId());
						                  Thread.sleep(time);
						                  System.out.println("同步中："+"时间"+new Date()+":"+Thread.currentThread().getId());
											for (DynaBean dynaBean : getInterfaceList) {
												String interfaceID = dynaBean.getStr("INTERFACE_ID");
												String interfaceName = dynaBean.getStr("INTERFACE_NAME");
												//System.out.println("参数："+interfaceID+"："+interfaceName);
												BaseDataSynch(interfaceID, interfaceName);	
											}
										  System.out.println("结束同步："+"时间"+new Date()+":"+Thread.currentThread().getId());
						               } catch (InterruptedException e) { }
						            }
						         }.start();
							}
						}
					}
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				System.out.println(TAG + ":参数不全,请核对");
			}
		} else {
			System.out.println(TAG + ":参数不全,请核对");
		}
	}
	
	
	
	/**
	 * 基础数据同步
	 *
	 */
	private void BaseDataSynch(String dataType, String dataTypeName) {
		try {
			String strUrl = String.format("%s/services/BasicArchivesService?wsdl", SaasWebHost);
			URL url = new URL(strUrl);
			Client client = new Client(url);
			String data = "";
			switch (dataType) {
			case "DEPARTMENT": // 组织架构
				List<DynaBean> lstDepartData = serviceTemplate.selectList("JE_CORE_DEPARTMENT", "");
				data = jsonBuilder.toJson(lstDepartData);
				break;
			case "USER": // 人员信息
				List<DynaBean> lstUserData = serviceTemplate.selectList("JE_CORE_ENDUSER", "");
				data = jsonBuilder.toJson(lstUserData);
				break;
			case "PRODUCTLINE": // 生产线
				List<DynaBean> lstProductLineData = serviceTemplate.selectList("JGMES_BASE_CXSJ", "");
				data = jsonBuilder.toJson(lstProductLineData);
				break;
			default:
				break;
			}
			if (!data.equals("") && data.length() > 0) {
				Object[] res = client.invoke("basicArchives", new Object[] { Token, dataType, data });
				if (res != null && res.length > 0) {
					String re = (String) res[0];
					JSONObject.fromObject(re);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(String.format("%s:【%s】%s", TAG, dataTypeName, e.toString()));
			e.printStackTrace();
		}
	}

	// endregion

	/**
	 * 业务数据同步
	 * 
	 * @param vo
	 */
	public void BusinessDataSynchTask(TimedTaskParamsVo vo) {

		JgmesCommon jgmesCommon = new JgmesCommon(null, serviceTemplate);
		Map<String, String> params = vo.getParams();
		if (params != null && params.size() > 0) {
			SaasWebHost = params.get("SaasWebHost");
			TenantID = params.get("TenantID");
			TenantKey = params.get("TenantKey");
			lastDate = params.get("LastDate");
			if (!"".equals(SaasWebHost) && !"".equals(TenantID) && !"".equals(TenantKey)) {
				try {
					JSONObject object = GetAuthToken();
					if (object.getBoolean("IsSuccess")) {
						Token = object.getString("Token");

						JSONObject jsonObject = GetDataInterface("BusinessData");
						if (jsonObject.getBoolean("IsSuccess")) {
							JSONArray jsonArray = jsonObject.getJSONArray("Data");
							List<DynaBean> getInterfaceList = jgmesCommon.getListDynaBeanByJsonStr("",
									jsonBuilder.toJson(jsonArray));
							if (getInterfaceList != null && getInterfaceList.size() > 0) {
								//延迟执行业务数据同步
								Random ran = new Random();
								long time = ran.nextInt(600)*1000;
								new Thread(){
						            public void run(){
						               try { 
						            	  System.out.println("开始同步："+"时间"+new Date()+":"+time+":"+Thread.currentThread().getId());
						                  Thread.sleep(time);
						                  System.out.println("同步中："+"时间"+new Date()+":"+Thread.currentThread().getId());
										for (DynaBean dynaBean : getInterfaceList) {
											String interfaceID = dynaBean.getStr("INTERFACE_ID");
											String interfaceName = dynaBean.getStr("INTERFACE_NAME");
											// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
											String lastSynDate = "".equals(dynaBean.getStr("LASTEXECUTE_Time")) == false
													? dynaBean.getStr("LASTEXECUTE_Time")
													: lastDate;
											 BusinessDataSynch(interfaceID, interfaceName, lastSynDate);
											 System.out.println("结束同步："+"时间"+new Date()+":"+Thread.currentThread().getId());
										}
						               } catch (InterruptedException e) { }
						            }
						         }.start();
						         
							}
						}
					}
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				System.out.println(TAG + ":参数不全,请核对");
			}
		} else {
			System.out.println(TAG + ":参数不全,请核对");
		}
	}

	/**
	 * 业务数据同步
	 * 
	 * @param dataType
	 * @param dataTypeName
	 * @param lastDate
	 */
	private void BusinessDataSynch(String dataType, String dataTypeName, String lastDate) {

		try {
			String strUrl = String.format("%s/services/BusinessDataSynService?wsdl", SaasWebHost);
			URL url = new URL(strUrl);
			Client client = new Client(url);
			String data = "";
			switch (dataType) {
			case "PRODUCTANALYSIS": // 产品分析
				List<DynaBean> lstProductAnalysis = serviceTemplate.selectList("JGMES_YWB_CPFX",
						" and str_to_date(CPFX_GXSJ,'%Y-%c-%d %H:%i:%s')>=str_to_date('" + lastDate
								+ "','%Y-%c-%d %H:%i:%s')");
				data = jsonBuilder.toJson(lstProductAnalysis);

				break;
			case "PRODUCTIONLINEANALYSIS": // 产线分析
				List<DynaBean> lstProductionLineAnalysis = serviceTemplate.selectList("JGMES_YWB_CXFX",
						" and str_to_date(CXFX_GXSJ,'%Y-%c-%d %H:%i:%s')>=str_to_date('" + lastDate
								+ "','%Y-%c-%d %H:%i:%s')");
				data = jsonBuilder.toJson(lstProductionLineAnalysis);

				break;
			case "WORKORDERANALYSIS":// 工单分析
				List<DynaBean> lstWorkOrderAnalysis = serviceTemplate.selectList("JGMES_YWB_GDFX",
						" and str_to_date(GDFX_GXSJ,'%Y-%c-%d %H:%i:%s')>=str_to_date('" + lastDate
								+ "','%Y-%c-%d %H:%i:%s')");
				data = jsonBuilder.toJson(lstWorkOrderAnalysis);
				break;
			case "STAFFEFFICIENCYANALYSIS":// 人员效率分析
				List<DynaBean> lstStaffEfficiencyAnalysis = serviceTemplate.selectList("JGMES_YWB_RYXLFX",
						" and str_to_date(RYXLFX_GXSJ,'%Y-%c-%d %H:%i:%s')>=str_to_date('" + lastDate
								+ "','%Y-%c-%d %H:%i:%s')");
				data = jsonBuilder.toJson(lstStaffEfficiencyAnalysis);
				break;
			case "PRODUCTIONANALYSIS": // 生产分析
				List<DynaBean> lstProductionAnalysis = serviceTemplate.selectList("JGMES_YWB_SCFXSJ",
						" and str_to_date(SCFXSJ_GXSJ,'%Y-%c-%d %H:%i:%s')>=str_to_date('" + lastDate
								+ "','%Y-%c-%d %H:%i:%s')");
				data = jsonBuilder.toJson(lstProductionAnalysis);
				break;
			case "TRACEABILITYDATA":// 追溯数据
				List<DynaBean> lstTraceabilityData = serviceTemplate.selectList("JGMES_YWB_ZSSJ",
						" and str_to_date(ZSSJ_GXSJ,'%Y-%c-%d %H:%i:%s')>=str_to_date('" + lastDate
								+ "','%Y-%c-%d %H:%i:%s')");
				data = jsonBuilder.toJson(lstTraceabilityData);
				break;
			case "TRACEBACKDATASUBTABLE":// 追溯数据子表
				List<DynaBean> lstTracebackDataSubtable = serviceTemplate.selectList("JGMES_YWB_ZSSJZB",
						" and  str_to_date(ZSSJZB_GXSJ,'%Y-%c-%d %H:%i:%s')>=str_to_date('" + lastDate
								+ "','%Y-%c-%d %H:%i:%s')");
				data = jsonBuilder.toJson(lstTracebackDataSubtable);
				break;
			case "TRACEBACKDATASCHEDULE":// 追溯数据明细表
				List<DynaBean> lstTracebackDataSchedule = serviceTemplate.selectList("JGMES_YWB_ZSSJMXB",
						"  and str_to_date(ZSSJMXB_GXSJ,'%Y-%c-%d %H:%i:%s')>=str_to_date('" + lastDate
								+ "','%Y-%c-%d %H:%i:%s')");
				data = jsonBuilder.toJson(lstTracebackDataSchedule);
				break;
			case "BADCAUSEKANBAN":// 看板不良原因
				List<DynaBean> lstBadCauseKanban = serviceTemplate.selectList("JGMES_YWB_KBBLYY",
						" and str_to_date(KBBLYY_GXSJ,'%Y-%c-%d %H:%i:%s')>=str_to_date('" + lastDate
								+ "','%Y-%c-%d %H:%i:%s')");
				data = jsonBuilder.toJson(lstBadCauseKanban);
				break;
			case "PRODUCTIONLIVE": // 生产实况
				List<DynaBean> lstProductionLive = serviceTemplate.selectList("JGMES_YWB_SCSK",
						" and str_to_date(SCSK_GXSJ,'%Y-%c-%d %H:%i:%s')>=str_to_date('" + lastDate
								+ "','%Y-%c-%d %H:%i:%s')");
				data = jsonBuilder.toJson(lstProductionLive);
				break;
			default:
				break;
			}

			Object[] res = client.invoke("businessDataSyn", new Object[] { Token, dataType, data });
			if (res != null && res.length > 0) {
				String re = (String) res[0];
				JSONObject.fromObject(re);
			}

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(String.format("%s:【%s】%s", TAG, dataTypeName, e.toString()));
			e.printStackTrace();
		}
	}

	/**
	 * 看板数据同步
	 * 
	 * @param vo
	 */
	public void KanbanDataSynchTask(TimedTaskParamsVo vo) {
		JgmesCommon jgmesCommon = new JgmesCommon(null, serviceTemplate);
		Map<String, String> params = vo.getParams();
		if (params != null && params.size() > 0) {
			SaasWebHost = params.get("SaasWebHost");
			TenantID = params.get("TenantID");
			TenantKey = params.get("TenantKey");
			lastDate = params.get("LastDate");
			if (!"".equals(SaasWebHost) && !"".equals(TenantID) && !"".equals(TenantKey)) {
				try {
					JSONObject object = GetAuthToken();
					if (object.getBoolean("IsSuccess")) {
						Token = object.getString("Token");

						JSONObject jsonObject = GetDataInterface("KanBanData");
						if (jsonObject.getBoolean("IsSuccess")) {
							JSONArray jsonArray = jsonObject.getJSONArray("Data");
							List<DynaBean> getInterfaceList = jgmesCommon.getListDynaBeanByJsonStr("",
									jsonBuilder.toJson(jsonArray));
							if (getInterfaceList != null && getInterfaceList.size() > 0) {
								for (DynaBean dynaBean : getInterfaceList) {
									String interfaceID = dynaBean.getStr("INTERFACE_ID");
									String interfaceName = dynaBean.getStr("INTERFACE_NAME");
									// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
									String lastSynDate = "".equals(dynaBean.getStr("LASTEXECUTE_Time")) == false
											? dynaBean.getStr("LASTEXECUTE_Time")
											: lastDate;
									KanBanDataSynch(interfaceID, interfaceName, lastSynDate);
								}
							}
						}
					}
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				System.out.println(TAG + ":参数不全,请核对");
			}
		} else {
			System.out.println(TAG + ":参数不全,请核对");
		}

	}

	/**
	 * 看板数据同步
	 * 
	 * @param dataType
	 * @param dataTypeName
	 * @param lastDate
	 */
	private void KanBanDataSynch(String dataType, String dataTypeName, String lastDate) {

		try {
			String strUrl = String.format("%s/services/KanbanDataSynService?wsdl", SaasWebHost);
			URL url = new URL(strUrl);
			Client client = new Client(url);
			String data = "";
			switch (dataType) {
			case "JOBCOMPLETION": // 作业完成情况
				List<DynaBean> lstJobCompletion = serviceTemplate.selectList("JGMES_YWB_ZYWCQK",
						" and str_to_date(ZYWCQK_GXSJ,'%Y-%c-%d %H:%i:%s')>=str_to_date('" + lastDate
								+ "','%Y-%c-%d %H:%i:%s')");
				data = jsonBuilder.toJson(lstJobCompletion);
				
				break;
			case "DEPARTMENTCOMPLETION": //部门完成情况表
				List<DynaBean> lstDepartmentCompletion = serviceTemplate.selectList("JGMES_YWB_BMWCQKB",
						" and str_to_date(BMWCQKB_GXSJ,'%Y-%c-%d %H:%i:%s')>=str_to_date('" + lastDate
								+ "','%Y-%c-%d %H:%i:%s')");
				data = jsonBuilder.toJson(lstDepartmentCompletion);
				
				break;
			case "OVERDUENUMBER": //超期未完成作业数
				List<DynaBean> lstOverdueNumber = serviceTemplate.selectList("JGMES_YWB_CQWWCZYS",
						" and str_to_date(CQWWCZYS_GXSJ,'%Y-%c-%d %H:%i:%s')>=str_to_date('" + lastDate
								+ "','%Y-%c-%d %H:%i:%s')");
				data = jsonBuilder.toJson(lstOverdueNumber);
				
				break;
			case "MONTHLYPROGRESSSCHEDULE": //月度产量进度情况表
				List<DynaBean> lstMonthlyProgressSchedule = serviceTemplate.selectList("JGMES_YWB_YDCLJDQKB",
						" and str_to_date(YDCLJDQKB_GXSJ,'%Y-%c-%d %H:%i:%s')>=str_to_date('" + lastDate
								+ "','%Y-%c-%d %H:%i:%s')");
				data = jsonBuilder.toJson(lstMonthlyProgressSchedule);
				
				break;
			case "PRODUCTIONLINEPULLBOARD": //产线拉头看板
				List<DynaBean> lstProductionLinePullBoard = serviceTemplate.selectList("JGMES_YWB_XLTKB",
						" and str_to_date(XLTKB_GXSJ,'%Y-%c-%d %H:%i:%s')>=str_to_date('" + lastDate
								+ "','%Y-%c-%d %H:%i:%s')");
				data = jsonBuilder.toJson(lstProductionLinePullBoard);
				
				break;
			case "BADPROBLEMSEVERYDAY": //每天不良问题点
				List<DynaBean> lstBadProblemsEveryDay = serviceTemplate.selectList("JGMES_YWB_MTBLWTD",
						" and str_to_date(MTBLWTD_GXSJ,'%Y-%c-%d %H:%i:%s')>=str_to_date('" + lastDate
								+ "','%Y-%c-%d %H:%i:%s')");
				data = jsonBuilder.toJson(lstBadProblemsEveryDay);
				
				break;
			case "KANBANDEPARTMENT": //看板组织机构
				List<DynaBean> lstKanbanDepartment = serviceTemplate.selectList("JGMES_YWB_ZZJG",
						" and str_to_date(ZZJG_XLTKB_GXSJ,'%Y-%c-%d %H:%i:%s')>=str_to_date('" + lastDate
								+ "','%Y-%c-%d %H:%i:%s')");
				data = jsonBuilder.toJson(lstKanbanDepartment);
			break;
			case "CXLTKBTIME": //产线拉头看板_按时间段
				List<DynaBean> lstCXLTKBTIME = serviceTemplate.selectList("JGMES_YWB_CXLTKBTIME",
						" and str_to_date(CXLTKBTIME_GXSJ,'%Y-%c-%d %H:%i:%s')>=str_to_date('" + lastDate
								+ "','%Y-%c-%d %H:%i:%s')");
				data = jsonBuilder.toJson(lstCXLTKBTIME);
				break;
			default:
				break;
			}

			Object[] res = client.invoke("kanbanDataSyn", new Object[] { Token, dataType, data });
			if (res != null && res.length > 0) {
				String re = (String) res[0];
				JSONObject.fromObject(re);
			}

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(String.format("%s:【%s】%s", TAG, dataTypeName, e.toString()));
			e.printStackTrace();
		}
	}

}