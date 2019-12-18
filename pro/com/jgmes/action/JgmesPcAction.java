package com.jgmes.action;

import com.je.core.action.DynaAction;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.DynaBean;
import com.je.table.exception.PCExcuteException;
import com.jgmes.service.JgmesProductionQueueService;
import com.jgmes.util.DateUtil;
import com.jgmes.util.JgmesCommon;
import com.jgmes.util.JgmesResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

//import org.apache.log4j.Logger;
/**
 * 
 * @author xxp
 * @version 2019-01-04 20:39:26
 * @see /JGMES/jgmesPcAction!load.action
 */
@Component("jgmesPcAction")
@Scope("prototype")
public class JgmesPcAction extends DynaAction  {
	
	private static final long serialVersionUID = 1L;
	//private static Logger logger = Logger.getLogger(JgmesPcAction.class);
	@Autowired
	private JgmesProductionQueueService jgmesProductionQueueService;
	
	public void load(){
		toWrite("hello Action");
	}
	
	//发布排产新版本
	public void SubmitNewVerson() {
		String BBH = request.getParameter("bbh");
		int newBBH = 1 + Integer.parseInt(BBH);
		try {
			JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
			DynaBean dic = jgmesCommon.getDic("JGMES_DIC_PCZT","PCZT04");//获取排产状态字典中“完成排产”的状态
			//更新主表版本号
			String strSql="update JGMES_PLAN_MAIN set MAIN_BBH=ifnull(MAIN_BBH,0)+1 where JGMES_PLAN_MAIN_ID='"+pkValue+"'";
			long num=pcServiceTemplate.executeSql(strSql);
			int mDays=0;//排产当月天数
			if (num>0) {
				//更新排产表版本号
				strSql="update JGMES_PLAN_PCLB set PCLB_BBH=(select MAIN_BBH from JGMES_PLAN_MAIN where JGMES_PLAN_MAIN_ID='"+pkValue+"') where JGMES_PLAN_MAIN_ID='"+pkValue+"'";
				pcServiceTemplate.executeSql(strSql);
				
				//保存更新历史
				strSql="insert into JGMES_PLAN_PCLSJL(PCLB_QGRQ,PCLB_PCZT_NAME,PCLB_CXMC,PCLB_SJWGSJ,JGMES_PLAN_GDLB_ID,JGMES_PLAN_MAIN_ID,"
					  +"PCLB_PCDBH,PCLB_KHBM,PCLB_JHKGSJ,SY_ORDERINDEX,PCLB_DTSL,PCLB_RQ30,PCLB_RQ31,SY_CREATEORGID,PCLB_KHMC,PCLB_GDZT_NAME,"
					  +"SY_STATUS,SY_CREATEUSERID,PCLB_DDSL,SY_AUDFLAG,SY_PDID,PCLB_RQ29,SY_CREATEORG,SY_CREATEORGNAME,PCLB_RQ25,PCLB_RQ26,"
					  +"SY_JTGSMC,PCLB_RQ27,PCLB_RQ28,PCLB_SJKGSJ,PCLB_RQ21,PCLB_RQ22,PCLB_RQ23,SY_JTGSID,PCLB_BZ,PCLB_RQ24,PCLB_RQ20,PCLB_DEPTCODE,"
					  +"PCLB_LCKH,PCLB_GDZT_CODE,PCLB_OTDRQ,PCLB_PCDMC,PCLB_CPGG,PCLB_NAME,PCLB_PGSL,SY_CREATEUSERNAME,PCLB_RQ18,PCLB_RQ19,PCLB_WPCSL,"
					  +"PCLB_PCZT_ID,PCLB_RQ14,PCLB_RQ15,PCLB_WCSL,PCLB_CPXH,PCLB_RQ16,PCLB_RQ17,PCLB_RQ10,PCLB_RQ11,PCLB_RQ12,PCLB_RQ13,PCLB_GDHM,"
					  +"PCLB_JHWGSJ,PCLB_PCRQ,PCLB_XPCSL,PCLB_PCZT_CODE,PCLB_DEPTNAME,SY_PIID,PCLB_GDSL,PCLB_BBH,PCLB_CPBH,PCLB_RQ07,PCLB_RQ08,"
					  +"SY_CREATEUSER,PCLB_RQ09,SY_CREATETIME,PCLB_CXBM,PCLB_RWDH,PCLB_RQ03,PCLB_WCL,PCLB_RQ04,PCLB_RQ05,PCLB_RQ06,PCLB_YPCSL,"
					  +"PCLB_RQ01,PCLB_RQ02,JGMES_PLAN_PCLB_ID,PCLB_DDHM) "
					  +"select PCLB_QGRQ,PCLB_PCZT_NAME,PCLB_CXMC,PCLB_SJWGSJ,JGMES_PLAN_GDLB_ID,JGMES_PLAN_MAIN_ID,"
					  +"PCLB_PCDBH,PCLB_KHBM,PCLB_JHKGSJ,SY_ORDERINDEX,PCLB_DTSL,PCLB_RQ30,PCLB_RQ31,SY_CREATEORGID,PCLB_KHMC,PCLB_GDZT_NAME,"
					  +"SY_STATUS,SY_CREATEUSERID,PCLB_DDSL,SY_AUDFLAG,SY_PDID,PCLB_RQ29,SY_CREATEORG,SY_CREATEORGNAME,PCLB_RQ25,PCLB_RQ26,"
					  +"SY_JTGSMC,PCLB_RQ27,PCLB_RQ28,PCLB_SJKGSJ,PCLB_RQ21,PCLB_RQ22,PCLB_RQ23,SY_JTGSID,PCLB_BZ,PCLB_RQ24,PCLB_RQ20,PCLB_DEPTCODE,"
					  +"PCLB_LCKH,PCLB_GDZT_CODE,PCLB_OTDRQ,PCLB_PCDMC,PCLB_CPGG,PCLB_NAME,PCLB_PGSL,SY_CREATEUSERNAME,PCLB_RQ18,PCLB_RQ19,PCLB_WPCSL,"
					  +"PCLB_PCZT_ID,PCLB_RQ14,PCLB_RQ15,PCLB_WCSL,PCLB_CPXH,PCLB_RQ16,PCLB_RQ17,PCLB_RQ10,PCLB_RQ11,PCLB_RQ12,PCLB_RQ13,PCLB_GDHM,"
					  +"PCLB_JHWGSJ,PCLB_PCRQ,PCLB_XPCSL,PCLB_PCZT_CODE,PCLB_DEPTNAME,SY_PIID,PCLB_GDSL,PCLB_BBH,PCLB_CPBH,PCLB_RQ07,PCLB_RQ08,"
					  +"SY_CREATEUSER,PCLB_RQ09,SY_CREATETIME,PCLB_CXBM,PCLB_RWDH,PCLB_RQ03,PCLB_WCL,PCLB_RQ04,PCLB_RQ05,PCLB_RQ06,PCLB_YPCSL,"
					  +"PCLB_RQ01,PCLB_RQ02,JGMES_PLAN_PCLB_ID,PCLB_DDHM "
					  +"from JGMES_PLAN_PCLB where JGMES_PLAN_MAIN_ID='"+pkValue+"'";
				pcServiceTemplate.executeSql(strSql);
				
				//获取已发布的生产任务
				strSql="select * from JGMES_PLAN_SCRW where JGMES_PLAN_MAIN_ID='"+pkValue+"'";
				List<DynaBean> scrws=serviceTemplate.selectListBySql(strSql);
				
				//发布生产任务
				strSql="select * from JGMES_PLAN_PCLB where JGMES_PLAN_MAIN_ID='"+pkValue+"'";
				List<DynaBean> lists=serviceTemplate.selectListBySql(strSql);
				String PCLB_GDHM="",PCLB_JHKGSJ="",PCLB_JHWGSJ="",PCLB_BBH="",PCLB_CPBH="";
				String iDay="",sDay="",iColName="";
				String iPcsl="",iDate="";
				int ypcsl=0;
				DateUtil dateUtil=new DateUtil();
				String jhDate=lists.get(0).getStr("PCLB_JHKGSJ");
				//增加日期判断
				for (DynaBean dynaBean : lists) {
					jhDate=dynaBean.getStr("PCLB_JHKGSJ");
					if (jhDate!=null||!jhDate.isEmpty()) {
						break;
					}
				}
			    if(jhDate.isEmpty()) {
					int ypc = lists.get(0).getInt("PCLB_YPCSL");
					String gdh = lists.get(0).getStr("PCLB_GDHM");//工单号
					String cx = lists.get(0).getStr("PCLB_CXBM");//产线
					if (ypc==0){//只针对空排
						//删除该工单号，在该产线下排产的任务单
						List<DynaBean> jgmes_plan_scrw = serviceTemplate.selectList("JGMES_PLAN_SCRW", "and SCRW_GDHM='" + gdh + "' and SCRW_CXBM='" + cx + "'");
						serviceTemplate.deleteByWehreSql("JGMES_PLAN_SCRW","and SCRW_GDHM='"+gdh+"' and SCRW_CXBM='"+cx+"'");
						toWrite(jsonBuilder.returnSuccessJson("\"处理成功\""));
						return;
					}
			    	toWrite(jsonBuilder.returnFailureJson("\"排产数据没有变化。\""));
			    	return;
				}
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date date = sdf.parse(jhDate);
				//System.out.println(date);
				// 获取年
				SimpleDateFormat y = new SimpleDateFormat("yyyy");
				int year=Integer.parseInt(y.format(date));
				// 获取月
				SimpleDateFormat m = new SimpleDateFormat("MM");
				int month=Integer.parseInt(m.format(date));
				mDays=dateUtil.getMonthLastDay(year, month);//获取排产当月的天数
				String SCRW_PCRQ="",SCRW_GDHM="";
				String yDay="";
				String transType="C";//N--插入 U--更新 C--不处理
				for (DynaBean pclb : lists) {
					PCLB_GDHM=pclb.getStr("PCLB_GDHM");//工单号码
					PCLB_JHKGSJ=pclb.getStr("PCLB_JHKGSJ");//计划开工时间
					PCLB_JHWGSJ=pclb.getStr("PCLB_JHWGSJ");//计划完工时间
					PCLB_BBH=pclb.getStr("PCLB_BBH");//版本号
					PCLB_CPBH=pclb.getStr("PCLB_CPBH");//产品编号
					String PCLB_CXBM =pclb.getStr("PCLB_CXBM");
					ypcsl=getGDSL(PCLB_GDHM,PCLB_CPBH);//获取该工单同一产品的已排产数量汇总
					
					if(PCLB_JHKGSJ.length()<8||"".equals(PCLB_JHKGSJ)) {
						//没有排产时，判断是否已经有生产任务（判断条件为工单号码+产线）有则删除
						List<DynaBean> alreadySCRWDynaBean=serviceTemplate.selectList("JGMES_PLAN_SCRW","and SCRW_GDHM='"+PCLB_GDHM+"' and SCRW_CXBM='"+PCLB_CXBM+"'");
						if(alreadySCRWDynaBean!=null&&alreadySCRWDynaBean.size()>0) {
							for (DynaBean dynaBean : alreadySCRWDynaBean) {
								serviceTemplate.delete(dynaBean);
							}
						}
						continue;
					}
					for (int i=1;i<=mDays;i++) {//根据当月天来循环
						iDay="0"+i;
						sDay=iDay.substring(iDay.length()-2, iDay.length());
						iDate=PCLB_JHKGSJ.substring(0, 8)+sDay;
						iColName="PCLB_RQ"+sDay;
						iPcsl=pclb.getStr(iColName);
						//超过了字段数cj
						if (iPcsl==null) {
							iPcsl="0";
						}
						//判断是否已经有生产任务(不支持上午和下午不同产品)
						DynaBean alreadyDynaBean=serviceTemplate.selectOne("JGMES_PLAN_SCRW","and SCRW_GDHM='"+PCLB_GDHM+"' and SCRW_PCRQ='"+iDate+"' and SCRW_CXBM='"+PCLB_CXBM+"'");
						
						if (!iPcsl.equals("0")) {
							transType="N";
							//遍历已发布的生产任务,如果已存在则更新，不存在则插入SCRW_PCSL
							for (DynaBean scrw : scrws) {
								SCRW_GDHM=scrw.getStr("SCRW_GDHM");
								SCRW_PCRQ=scrw.getStr("SCRW_PCRQ");
								//增加判断日期 cj
								if (SCRW_PCRQ==null) {
									toWrite(jsonBuilder.returnFailureJson("\"生产单排产日期异常请联系网络管理员。\""));
									return;
								}
								date = sdf.parse(SCRW_PCRQ);
								SimpleDateFormat d = new SimpleDateFormat("dd");
								yDay=d.format(date);
								//需要增加判断产线0408cj
								String SCRW_CXBM=scrw.getStr("SCRW_CXBM");
								System.out.println(SCRW_CXBM+":"+PCLB_CXBM+":"+yDay+":"+sDay);
								if (SCRW_GDHM.equals(PCLB_GDHM) && yDay.endsWith(sDay)&& PCLB_CXBM.equals(SCRW_CXBM)) {
									transType="U";
								}
							}
							if (transType.equals("U")) {
								//更新任务单（加上产线的判断0408cj）
								strSql="update JGMES_PLAN_SCRW set SCRW_JHKGSJ='"+PCLB_JHKGSJ+"',SCRW_JHWGSJ='"+PCLB_JHWGSJ+"',SCRW_PCSL="+iPcsl+",SCRW_BBH='"+PCLB_BBH+"' "
										+ "where SCRW_GDHM='"+PCLB_GDHM+"' and SCRW_PCRQ='"+iDate+"' and SCRW_CXBM='"+PCLB_CXBM+"'";
								pcServiceTemplate.executeSql(strSql);
							} else if (transType.equals("N")) {
								DynaBean bean = new DynaBean("JGMES_PLAN_SCRW", true);
								serviceTemplate.buildModelCreateInfo(bean);
								String RWDH=serviceTemplate.buildCode("SCRW_RWDH", "JGMES_PLAN_SCRW", bean);
								bean.set("SCRW_RWDH", RWDH);
								bean.set("SCRW_JHKGSJ", pclb.get("PCLB_JHKGSJ")+"");//计划开工时间
								bean.set("SCRW_PCDBH", pclb.get("PCLB_PCDBH")+"");//排产单编号
								bean.set("SCRW_PCDMC", pclb.get("PCLB_PCDMC")+"");//排产单名称
								bean.set("SCRW_CPBH", pclb.get("PCLB_CPBH")+"");//产品编号
								bean.set("SCRW_NAME", pclb.get("PCLB_NAME")+"");//产品名称
								bean.set("SCRW_GDZT_CODE", pclb.get("PCLB_GDZT_CODE")+"");//工单状态
								bean.set("SCRW_BZ", pclb.get("PCLB_BZ")+"");//备注
								bean.set("SCRW_WCL", pclb.get("PCLB_WCL")+"");//完成率
								bean.set("SCRW_SJKGSJ", pclb.get("PCLB_SJKGSJ")+"");//实际开工时间
								bean.set("SCRW_CPGG", pclb.get("PCLB_CPGG")+"");//产品规格
								bean.set("SCRW_DDHM", pclb.get("PCLB_DDHM")+"");//订单号码
								bean.set("SCRW_GDHM", pclb.get("PCLB_GDHM")+"");//工单号码
								bean.set("SCRW_DEPTNAME", pclb.get("PCLB_DEPTNAME")+"");//部门名称
								bean.set("SCRW_LCKH", pclb.get("PCLB_LCKH")+"");//流程卡号
								bean.set("SCRW_GDZT_NAME", pclb.get("PCLB_GDZT_NAME")+"");//工单状态_NAME
								bean.set("SCRW_WCSL", 0+"");
								bean.set("SCRW_JHWGSJ", pclb.get("PCLB_JHWGSJ")+"");//计划完工时间
								bean.set("SCRW_DEPTCODE", pclb.get("PCLB_DEPTCODE")+"");//部门编码
								bean.set("SCRW_PCRQ", iDate);//排产日期
								bean.set("SCRW_SJWGSJ", pclb.get("PCLB_SJWGSJ")+"");//实际完工时间
								bean.set("SCRW_PGSL", pclb.get("PCLB_PGSL")+"");//派工数量
								bean.set("SCRW_CXBM", pclb.get("PCLB_CXBM")+"");//产线编码
								bean.set("SCRW_CXMC", pclb.get("PCLB_CXMC")+"");//产线名称
								bean.set("JGMES_PLAN_GDLB_ID", pclb.get("JGMES_PLAN_GDLB_ID")+"");//工单列表_外键ID
								bean.set("SCRW_GDSL", pclb.get("PCLB_GDSL")+"");//工单数量
								bean.set("SCRW_DDSL", pclb.get("PCLB_DDSL")+"");//订单数量
								DynaBean dic1 = jgmesCommon.getDic("JGMES_DIC_PCZT","PCZT02");
								bean.set("SCRW_PCZT_NAME", dic1.getStr("DICTIONARYITEM_ITEMNAME"));//排产状态_NAME
								bean.set("SCRW_PCZT_CODE", dic1.getStr("DICTIONARYITEM_ITEMCODE"));//排产状态
								bean.set("SCRW_PCZT_ID", dic1.getStr("JE_CORE_DICTIONARYITEM_ID"));//排产状态_ID
								bean.set("JGMES_PLAN_MAIN_ID", pclb.get("JGMES_PLAN_MAIN_ID")+"");//排产单主表_外键ID
								bean.set("SCRW_YPCSL", pclb.get("PCLB_YPCSL")+"");//已排产数量
								bean.set("SCRW_WPCSL", pclb.get("PCLB_WPCSL")+"");//未排产数量
								bean.set("SCRW_QGRQ", pclb.get("PCLB_QGRQ")+"");//请购日期
								bean.set("SCRW_OTDRQ", pclb.get("PCLB_OTDRQ")+"");//OTD日期
								bean.set("SCRW_XPCSL", pclb.get("PCLB_XPCSL")+"");//需排产数量
								bean.set("SCRW_KHBM", pclb.get("PCLB_KHBM")+"");//客户编码
								bean.set("SCRW_KHMC", pclb.get("PCLB_KHMC")+"");//客户名称
								bean.set("SCRW_CPXH", pclb.get("PCLB_CPXH")+"");//产品型号
								bean.set("SCRW_DTSL", pclb.get("PCLB_DTSL")+"");//当天数量
								bean.set("SCRW_BBH", pclb.get("PCLB_BBH")+"");//版本号
								bean.set("SCRW_PCSL", iPcsl);//排产数量
								bean.set("SCRW_RWZT_NAME", "待生产");// 任务状态_NAME
								bean.set("SCRW_RWZT_CODE", "RWZT01");// 任务状态
								bean.set("SCRW_SCRWDATASOURCE_CODE","PC");//数据来源
								//排序
								DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " order by SY_ORDERINDEX desc limit 0,1");
								int orderindex =0;
								if (scrwDynaBean==null) {//首次发布任务时序号为1
									orderindex=1;
								}else {
									orderindex = scrwDynaBean.getInt("SY_ORDERINDEX")+1;
								}
								bean.set("SY_ORDERINDEX", orderindex);
								serviceTemplate.insert(bean);
							}

							//修正同一个工单号的需排产数量，需排产数量=工单数量-总已排产数量+当前排产单已排产数量
							List<DynaBean> dynaBeans = serviceTemplate.selectList("JGMES_PLAN_PCLB", " and PCLB_GDHM='" + pclb.get("PCLB_GDHM") + "'");//获取同一个工单号的汇总
							//获取已排产数量，汇总
							int allYpcsl = 0;//已排产数量
							int gdsl = pclb.getInt("PCLB_GDSL");//工单数量
							for (DynaBean dynaBean : dynaBeans) {
								allYpcsl+=dynaBean.getInt("PCLB_YPCSL");
							}
							int xpcsl = gdsl-allYpcsl;
							if (xpcsl>0){
								for (DynaBean dynaBean : dynaBeans) {
									dynaBean.setInt("PCLB_XPCSL",xpcsl+dynaBean.getInt("PCLB_YPCSL"));
									dynaBean.setInt("PCLB_WPCSL",xpcsl);
									serviceTemplate.update(dynaBean);
									//更新工单的需排产数和未排产数
									DynaBean gdBean = serviceTemplate.selectOne("JGMES_PLAN_GDLB", "and GDLB_GDHM='" + pclb.get("PCLB_GDHM") + "'");
									if (gdBean!=null){
										gdBean.setInt("GDLB_XPCSL",xpcsl);
										gdBean.setInt("GDLB_WPCSL",xpcsl);
										gdBean.setInt("GDLB_YPCSL",allYpcsl);
										serviceTemplate.update(gdBean);
									}
								}
							}
						}else if (alreadyDynaBean!=null) {
							serviceTemplate.delete(alreadyDynaBean);
						}
					}

					//更新工单列表;cj
//					System.out.println("开始更新工单和排产单");
					int gdsl=pclb.getInt("PCLB_GDSL");//工单数量
					DynaBean gdlb=serviceTemplate.selectOne(""
							+ "JGMES_PLAN_GDLB"," and JGMES_PLAN_GDLB_ID='"+pclb.getStr("JGMES_PLAN_GDLB_ID")+"'");
//					gdlb.set("GDLB_YPCSL", ypcsl);
////					gdlb.set("GDLB_WPCSL", pclb.getStr("PCLB_WPCSL"));
////					gdlb.set("GDLB_XPCSL", pclb.getStr("PCLB_XPCSL"));
					
					//判断是否已经排产完毕根据排产表订单数量，是就改状态cj
					if (ypcsl==gdsl) {
						gdlb.set("GDLB_PCZT_CODE", dic.getStr("DICTIONARYITEM_ITEMCODE"));//编号
						gdlb.set("GDLB_PCZT_NAME", dic.getStr("DICTIONARYITEM_ITEMNAME"));//名称
						gdlb.set("GDLB_PCZT_ID", dic.getStr("JE_CORE_DICTIONARYITEM_ID"));//id
						List<DynaBean> bean=serviceTemplate.selectList("JGMES_PLAN_SCRW", "and SCRW_GDHM='"+PCLB_GDHM+"' and SCRW_CPBH='"+PCLB_CPBH+"'");
						for (DynaBean dynaBean : bean) {
							dynaBean.set("SCRW_PCZT_NAME", dic.getStr("DICTIONARYITEM_ITEMNAME"));//排产状态_NAME
							dynaBean.set("SCRW_PCZT_CODE", dic.getStr("DICTIONARYITEM_ITEMCODE"));//排产状态
							dynaBean.set("SCRW_PCZT_ID", dic.getStr("JE_CORE_DICTIONARYITEM_ID"));//排产状态_ID
							serviceTemplate.update(dynaBean);
						}
					}
					serviceTemplate.update(gdlb);
				}
				String sql1 = " update JGMES_PLAN_SCRW set SCRW_PCZT_CODE='PCZT03',SCRW_PCZT_NAME='暂停生产',SCRW_RWZT_NAME='暂停',SCRW_RWZT_CODE='RWZT04' where SCRW_BBH="
						+ newBBH + " and JGMES_PLAN_MAIN_ID='" + pkValue
						+ "' and SCRW_PCZT_CODE='PCZT02' AND SCRW_RWZT_CODE='RWZT02'";
				String sql2 = "delete from JGMES_PLAN_SCRW where SCRW_BBH !=" + newBBH + " and JGMES_PLAN_MAIN_ID='"
						+ pkValue + "' and SCRW_RWZT_CODE='RWZT01' AND SCRW_PCZT_CODE='PCZT01'";
				pcServiceTemplate.executeSql(sql1);
				pcServiceTemplate.executeSql(sql2);
			}
			//排产表状态更新
			List<DynaBean> pcds=serviceTemplate.selectList("JGMES_PLAN_PCLB", "and JGMES_PLAN_MAIN_ID='"+pkValue+"'");
			if (pcds!=null) {
				for (DynaBean dynaBean : pcds) {
					String gdhm=dynaBean.getStr("PCLB_GDHM");
					String cpbm=dynaBean.getStr("PCLB_CPBH");
					int ypcsl = getGDSL(gdhm,cpbm);
					int gdsl=dynaBean.getInt("PCLB_GDSL");
					if (ypcsl==gdsl) {
						dynaBean.set("PCLB_PCZT_CODE", dic.getStr("DICTIONARYITEM_ITEMCODE"));//编号
						dynaBean.set("PCLB_PCZT_NAME", dic.getStr("DICTIONARYITEM_ITEMNAME"));//名称
						dynaBean.set("PCLB_PCZT_ID", dic.getStr("JE_CORE_DICTIONARYITEM_ID"));//id
					}
					serviceTemplate.update(dynaBean);
				}
			}
			toWrite(jsonBuilder.returnSuccessJson("\"处理成功\""));
		} catch (Exception e) {
			e.printStackTrace();
			toWrite(jsonBuilder.returnFailureJson("\"后台出现错误" + e.getMessage() + "\""));
		}		
	}
	
	/*
	 *根据工单号，产品编码  汇总已排产数量(排产表)
	 * @return
	 */
	private int getGDSL(String gdhm,String cpbm) {
		String sql = "SELECT SUM(PCLB_YPCSL) as ypcsl from JGMES_PLAN_PCLB WHERE PCLB_GDHM='"+gdhm+"' and PCLB_CPBH='"+cpbm+"'";
		List<DynaBean> pcDynaBean = serviceTemplate.selectListBySql(sql);
		System.out.println("汇总已排产数量="+pcDynaBean.get(0).getInt("ypcsl"));
		if (pcDynaBean==null||pcDynaBean.size()!=1) {
			throw new PCExcuteException("获取排产信息失败。");
		}
		if (pcDynaBean.get(0).getInt("ypcsl")<0) {
			throw new PCExcuteException("获取已排产数量失败。");
		}
		return pcDynaBean.get(0).getInt("ypcsl");
	}
	
	/*
	   *     自动排产接口    
	 * 
	 */
	public void doAutoSort() {
		String userCode = request.getParameter("userCode");
		String cxId = request.getParameter("cxId");
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
		JgmesResult<Integer> ret = new JgmesResult<Integer>();
		List<DynaBean> cxDynaBeanList = null;  //产线数据List
		
		if(userCode==null || userCode.isEmpty()) {
			userCode = jgmesCommon.jgmesUser.getCurrentUserCode();
		}
		try {
			if(cxId==null || cxId.isEmpty()) {
				cxDynaBeanList = serviceTemplate.selectList("JGMES_BASE_CXSJ", " and CXSJ_STATUS_CODE = '1'");
				if(cxDynaBeanList==null||cxDynaBeanList.size()==0) {
					ret.setMessage("未获取到产线！");
				}
				for(DynaBean cxDynaBean:cxDynaBeanList) {
					doAutoSortByCxID(cxDynaBean.getStr("CXSJ_STATUS_ID"));
				}
			}else {
				ret.IsSuccess = doAutoSortByCxID(cxId);
			}
		}catch (Exception e) {
			ret.setMessage(e.getMessage());
			e.printStackTrace();
		}finally {
			toWrite(jsonBuilder.toJson(ret));
		}
		
	}
	

	
	/*
	 * 
	 * 根据产线ID。来自动排产
	 */
	
	public boolean doAutoSortByCxID(String cxId) throws Exception {
		boolean result = true;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
		Calendar start=Calendar.getInstance();
		Calendar end=Calendar.getInstance();
		List<DynaBean> pcPlanDynaBeanList = null; 
		if(cxId!=null && !cxId.isEmpty()) {
			pcPlanDynaBeanList = serviceTemplate.selectList("JGMES_PLAN_DETAIL", " and DETAIL_CXID = '"+cxId+"' and DETAIL_PCZT_CODE = 'PCZT01'");
			for(DynaBean pcPlanDynaBean:pcPlanDynaBeanList) {
				
				int pcsl = pcPlanDynaBean.getInt("DETAIL_XPCSL"); //获取当前产线需要排产数量
				String DETAIL_YQWCRQ =pcPlanDynaBean.getStr("DETAIL_YQWCRQ");
				if(DETAIL_YQWCRQ==null||DETAIL_YQWCRQ.isEmpty()) {
					System.out.println("-要求完工日期错误-");
					throw new PCExcuteException("要求完工日期不存在");
				}
				Date wcrwq = sdf.parse(DETAIL_YQWCRQ);
				//根据产品编码来获取该产线该产品的产能
				DynaBean cxCnDynaBean = serviceTemplate.selectOne("JGMES_BASE_CPCXCNINFO", " and CPCXCNINFO_BH = '"+pcPlanDynaBean.get("DETAIL_CPBH")+"' and CPCXCNINFO_CXid='"+cxId+"'");
				if(cxCnDynaBean!= null) {
					
					//根据产品产线产能计算出该条产线各工单产品需要的工时（加工时长）。
					if("CNJSFS01".equals(cxCnDynaBean.get("CPCXCNINFO_CNJSFS_CODE"))) { //按照小时产能计算
						int xscn = cxCnDynaBean.getInt("CPCXCNINFO_XSCN");  //获取小时产能
						int gs = pcsl%xscn == 0 ? (pcsl/xscn) : (pcsl/xscn)+1; //获取工时（小时）
						end.setTime(wcrwq);
						end.add(Calendar.HOUR_OF_DAY, -gs);
					}else if("CNJSFS02".equals(cxCnDynaBean.get("CPCXCNINFO_CNJSFS_CODE"))) { //按照日产能计算
						int rcn = cxCnDynaBean.getInt("CPCXCNINFO_RCN");  //获取日产能
						int gs = pcsl%rcn == 0 ? (pcsl/rcn) : (pcsl/rcn)+1;   //获取工时（天）
						end.setTime(wcrwq);
						end.add(Calendar.DAY_OF_MONTH, -gs);
					}
				}else {
					throw new PCExcuteException("请设置该产品产线的产能");
				}
				
				Date zckgq = end.getTime();//获取理论最迟开工时间
				pcPlanDynaBean.set("DETAIL_ZCKGSJ", sdf.format(zckgq));//将理论最迟开工时间存入到排产计划表中
				serviceTemplate.update(pcPlanDynaBean);
			}
			
			DynaBean xtDynaBean = serviceTemplate.selectOne("JGMES_XTGL_XTCS"," and XTCS_CSBH='XTCS0009'");//取自动排产里物料齐套计算方式
			
			if("1".equals(xtDynaBean.get("XTCS_CSZ"))) {   //1则按物料齐套时间计算
				//pcPlanDynaBeanList = serviceTemplate.selectList("JGMES_PLAN_DETAIL", " and DETAIL_CXID = '"+cxId+"' and DETAIL_PCZT_NAME = '排产中'  and JGMES_PLAN_SCRW is null");
				String sql  = "select *,(case " + 
						"when DETAIL_NO_CODE = '1' then 1 " + 
						"when DETAIL_NO_CODE = '0' then 2 " + 
						"end) as sortDETAIL_NO_CODE from JGMES_PLAN_DETAIL " + 
						"where DETAIL_CXID ='"+cxId+"' " + 
						"and DETAIL_PCZT_CODE = 'PCZT01' " + 
						"and SY_ORDERINDEX=0 " + 
						"order by sortDETAIL_NO_CODE,DETAIL_ZCKGSJ ";
				orderBySQL(sql);
 			
			}else if("0".equals(xtDynaBean.get("XTCS_CSZ"))||xtDynaBean ==null) {//0或空按物料是否齐套计算
				String sql  =   "select *,(case " + 
								"when DETAIL_WLQTSJ>DETAIL_ZCKGSJ  " + 
								"then DETAIL_WLQTSJ  " + 
								"ELSE DETAIL_ZCKGSJ " + 
								"end) as sortA  " + 
								"from JGMES_PLAN_DETAIL  " + 
								"where DETAIL_CXID = '"+cxId+"' " + 
								"and DETAIL_PCZT_CODE = 'PCZT01' " + 
								"and SY_ORDERINDEX=0 " + 
								"order by sortA ";
				orderBySQL(sql);
			}
			
			
			//按照当前时间、先后顺序及加工时长计算出各工单的计划开工时间、计划完工时间；
			Date  currentTime = sdf.parse(jgmesCommon.getCurrentTime());  //获取系统当前的时间
			Date  currentTime1 = sdf.parse(jgmesCommon.getCurrentTime());  //获取系统当前的时间。上面的获取的系统当前时间不能用了，可能已经被改变了，看下面代码就明白了
			
			List<DynaBean> planDynaBeanList=serviceTemplate.selectList("JGMES_PLAN_DETAIL", " and DETAIL_CXID = '"+cxId+"' and DETAIL_PCZT_CODE = 'PCZT01'  order by SY_ORDERINDEX");
			
			if(planDynaBeanList ==null&&planDynaBeanList.size()==0) {
				throw new PCExcuteException("可用排产数据未找到！");
			}
			DynaBean dic = jgmesCommon.getDic("JGMES_DIC_PCZT","PCZT02");
			for(DynaBean planDynaBean:planDynaBeanList) {
				
				DynaBean gdDynaBean = serviceTemplate.selectOne("JGMES_PLAN_GDLB", " and JGMES_PLAN_GDLB_ID='"+planDynaBean.getStr("JGMES_PLAN_GDLB_ID")+"'");
				
				if("PCZT03".equals(planDynaBean.getStr("DETAIL_PCZT_CODE"))) {
					//计算剩余的数量的工时
					int wcsl =planDynaBean.getInt("DETAIL_WCSL");  //获取该工单的完成数量
					int pcsl = planDynaBean.getInt("DETAIL_XPCSL"); //获取当前需要排产数量
					
					//根据产品产线编码来获取该产线该产品的产能
					DynaBean cxCnDynaBean = serviceTemplate.selectOne("JGMES_BASE_CPCXCNINFO", " and CPCXCNINFO_BH = '"+planDynaBean.get("DETAIL_CPBH")+"' and CPCXCNINFO_CXid='"+cxId+"' ");
					if(cxCnDynaBean!= null) {
						if("CNJSFS01".equals(cxCnDynaBean.get("CPCXCNINFO_CNJSFS_CODE"))) { //按照小时产能计算
							int xscn = cxCnDynaBean.getInt("CPCXCNINFO_XSCN");  //获取小时产能
							int spcsl = pcsl - wcsl;  //获取剩余剩余产量
							int sgs = spcsl%xscn == 0 ? (spcsl/xscn) : (spcsl/xscn)+1; //获取剩余工时（小时）
							int xhgs = wcsl%xscn == 0 ? (wcsl/xscn) : (wcsl/xscn)+1; //获取已经消耗工时（小时）
							//计划完工时间
							end.setTime(currentTime);
							end.add(Calendar.HOUR_OF_DAY, sgs);
							//计算开工时间
							start.setTime(currentTime1);
							start.add(Calendar.HOUR_OF_DAY, -xhgs);
						}else if("CNJSFS02".equals(cxCnDynaBean.get("CPCXCNINFO_CNJSFS_CODE"))) { //按照日产能计算
							int rcn = cxCnDynaBean.getInt("CPCXCNINFO_RCN");  //获取日产能
							int spcsl = pcsl - wcsl;  //获取剩余剩余产量
							int sgs = spcsl%rcn == 0 ? (spcsl/rcn) : (spcsl/rcn)+1; //获取剩余工时（天）
							int xhgs = wcsl%rcn == 0 ? (wcsl/rcn) : (wcsl/rcn)+1; //获取已经消耗天数（天）
							//计划完工时间
							end.setTime(currentTime);
							end.add(Calendar.DAY_OF_MONTH, sgs);
							//计算开工时间
							start.setTime(currentTime1);
							start.add(Calendar.DAY_OF_MONTH, -xhgs);
						}else {
							throw new PCExcuteException("请设置该产品产线的产能");
						}
						planDynaBean.set("DETAIL_JHKGSJ", sdf.format(start.getTime()));//设置计划开工时间
						planDynaBean.set("DETAIL_JHWGSJ", sdf.format(end.getTime()));//设置计划完工时间
						
						//修改排产状态 工单，排产单
						gdDynaBean.set("GDLB_PCZT_CODE", dic.get("DICTIONARYITEM_ITEMCODE"));
						gdDynaBean.set("GDLB_PCZT_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));
						gdDynaBean.set("GDLB_PCZT_ID", dic.get("JE_CORE_DICTIONARYITEM_ID"));
						serviceTemplate.update(gdDynaBean);
						planDynaBean.set("DETAIL_PCZT_CODE", dic.get("DICTIONARYITEM_ITEMCODE"));
						planDynaBean.set("DETAIL_PCZT_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));
						planDynaBean.set("DETAIL_PCZT_ID", dic.get("JE_CORE_DICTIONARYITEM_ID"));
						planDynaBean.set("DETAIL_PCRQ", jgmesCommon.getCurrentDate());
						serviceTemplate.update(planDynaBean);//cj
						
						end.setTime(end.getTime());//上一个订单的计划完工时间是下一个订单的计划开工时间
						currentTime = end.getTime();//设置计划开工时间
						
					}
				}else {
					planDynaBean.set("DETAIL_JHKGSJ", sdf.format(currentTime)); //设置计划开工时间
					
					int pcsl = planDynaBean.getInt("DETAIL_XPCSL"); //获取当前产线需要排产数量
					//根据产品编码来获取该产线该产品的产能
					DynaBean cxCnDynaBean = serviceTemplate.selectOne("JGMES_BASE_CPCXCNINFO", " and CPCXCNINFO_BH = '"+planDynaBean.get("DETAIL_CPBH")+"' and CPCXCNINFO_CXid='"+cxId+"'");
					if(cxCnDynaBean!= null) {
						//计算工单的计划完工时间
						if("CNJSFS01".equals(cxCnDynaBean.get("CPCXCNINFO_CNJSFS_CODE"))) { //按照小时产能计算
							int xscn = cxCnDynaBean.getInt("CPCXCNINFO_XSCN");  //获取小时产能
							int gs = pcsl%xscn == 0 ? (pcsl/xscn) : (pcsl/xscn)+1; //获取工时（小时）
							end.setTime(currentTime);
							end.add(Calendar.HOUR_OF_DAY, gs);
						}else if("CNJSFS02".equals(cxCnDynaBean.get("CPCXCNINFO_CNJSFS_CODE"))) { //按照日产能计算
							int rcn = cxCnDynaBean.getInt("CPCXCNINFO_RCN");  //获取日产能
							int gs = pcsl%rcn == 0 ? (pcsl/rcn) : (pcsl/rcn)+1;   //获取工时（天）
							end.setTime(currentTime);
							end.add(Calendar.DAY_OF_MONTH, gs);
						}else {
							throw new PCExcuteException("请设置该产品产线的产能");
						}
						planDynaBean.set("DETAIL_JHWGSJ", sdf.format(end.getTime()));//设置计划完工时间
						//修改排产状态 工单，排产单
						gdDynaBean.set("GDLB_PCZT_CODE", dic.get("DICTIONARYITEM_ITEMCODE"));
						gdDynaBean.set("GDLB_PCZT_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));
						gdDynaBean.set("GDLB_PCZT_ID", dic.get("JE_CORE_DICTIONARYITEM_ID"));
						serviceTemplate.update(gdDynaBean);
						planDynaBean.set("DETAIL_PCZT_CODE", dic.get("DICTIONARYITEM_ITEMCODE"));
						planDynaBean.set("DETAIL_PCZT_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));
						planDynaBean.set("DETAIL_PCZT_ID", dic.get("JE_CORE_DICTIONARYITEM_ID"));
						planDynaBean.set("DETAIL_PCRQ", jgmesCommon.getCurrentDate());
						serviceTemplate.update(planDynaBean);//cj
						
						end.setTime(end.getTime());//上一个订单的计划完工时间是下一个订单的计划开工时间
						currentTime = end.getTime();//设置计划开工时间
					}
				}
				
			}
			
		}else {
			throw new PCExcuteException("产线信息获取失败，请选择排产的数据。");
		}
		return result;
	}
	/*
	 * 
	 *   根据传过来的sql排序
	 */
	public void orderBySQL(String Sql) {
		
			List<DynaBean> planDynaBeanList1=serviceTemplate.selectListBySql(Sql);//查询除去锁定工单号，和生产状态为生产中的记录
			List<DynaBean> planDynaBeanList2=serviceTemplate.selectList("JGMES_PLAN_DETAIL", " and SY_ORDERINDEX is not null");//锁定的工单顺序号要保持不变
			List<DynaBean> planDynaBeanList3=serviceTemplate.selectList("JGMES_PLAN_DETAIL", " and DETAIL_PCZT_CODE = 'PCZT04'");//如果如果有在生产中状态的排产计划记录，则该记录要排在第一位。
			
			List<Integer> orderIndexList = new ArrayList<Integer>();
			for(DynaBean planDynaBean:planDynaBeanList2) {//获取已经锁定的工单的顺序号
				orderIndexList.add(planDynaBean.getInt("SY_ORDERINDEX"));
			}
			
			for (int i = 1; i < planDynaBeanList3.size(); i++) {//生产中的工单排序号放在最前面
				if(!orderIndexList.contains(planDynaBeanList3.get(i).getInt("SY_ORDERINDEX"))) {
					DynaBean planDynaBean = planDynaBeanList3.get(i);
					planDynaBean.setInt("SY_ORDERINDEX", i);
					serviceTemplate.update(planDynaBean);
					orderIndexList.add(i);
				}
			}
			
			for(int i = 1;i<planDynaBeanList1.size();i++) {//其他情况的工单号
				if(!orderIndexList.contains(planDynaBeanList1.get(i).getInt("SY_ORDERINDEX"))) {
					DynaBean planDynaBean = planDynaBeanList1.get(i);
					planDynaBean.setInt("SY_ORDERINDEX", i);
					serviceTemplate.update(planDynaBean);
				}
				
			}
		
		
	}
	
	
	/*
	 * 自动排产插单建议执行
	 * @param cxid 产线主键id
	 * @param gdids 工单主键id（可为复数）
	 * 
	 */
	public void gdAllInsert() {
		String cxid = request.getParameter("cxId");
		String gdzjs = request.getParameter("gdzjs");
		JgmesResult<String> ret = new JgmesResult<String>();
		try {
			JgmesBaseAction jgmesBaseAction = new JgmesBaseAction();
			List<DynaBean> sps = serviceTemplate.selectList("JGMES_PLAN_GDLB",
					" AND JGMES_PLAN_GDLB_ID in (" + StringUtil.buildArrayToString(gdzjs.split(",")) + ")");
			//判断工单中必填参数:订单交货日期;齐套时间;
			for (DynaBean gddynaBean : sps) {
				if (gddynaBean.getStr("PCLB_JHKGSJ")!=null||gddynaBean.getInt("PCLB_YPCSL")>0) {
					toWrite(jsonBuilder.returnFailureJson("\"：工单已经有排产记录。\""));
					return ;
				}
				 if (gddynaBean.getStr("GDLB_DDJHRQ")==null||gddynaBean.getStr("GDLB_WLQTSJ")==null) {
					System.out.println("交货日期或齐套时间未设置");
					toWrite(jsonBuilder.returnFailureJson("\"交货日期或齐套时间未设置\""));
					return ;
				}
				 DynaBean cnDynaBean = serviceTemplate.selectOne("JGMES_BASE_CPCXCNINFO"," and CPCXCNINFO_BH='"+gddynaBean.getStr("GDLB_CPBH")+"' and CPCXCNINFO_CXid='"+cxid+"'");
				 if (cnDynaBean==null) {
					 System.out.println("产能信息找不到");
						toWrite(jsonBuilder.returnFailureJson("\"：产能信息找不到\""));
						return ;
				}
				 if (cnDynaBean.getStr("CPCXCNINFO_XSCN")==null&&cnDynaBean.getStr("CPCXCNINFO_RCN")==null) {
					 System.out.println("产能信息未设置");
					toWrite(jsonBuilder.returnFailureJson("\"：产能信息未设置\""));
					return ;
				}
			}
			System.out.println("开始插单："+cxid+":"+gdzjs);
			addSubmitNewVerson(cxid ,gdzjs);
			
			sortRW(gdzjs);
			System.out.println("完成插单的顺序前置。");
			toWrite(jsonBuilder.returnSuccessJson("\"完成插单。\""));
		} catch (Exception e) {
			toWrite(jsonBuilder.returnFailureJson("\"系统错误："+e.getMessage()+"\""));
			e.printStackTrace();
		}
	}
	
	/*
	 * 根据产线id和工单id发布任务(自动插单建议)
	 * @param cxid
	 * @param gdzjs
	 */
	private void addSubmitNewVerson(String cxid , String gdzjs) {
		//根据产线id和工单id发布任务
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
		DynaBean dic = jgmesCommon.getDic("JGMES_DIC_PCZT","PCZT04");//获取排产状态字典中“完成排产”的状态
		DynaBean pclb= serviceTemplate.selectOne("JGMES_PLAN_GDLB"," and JGMES_PLAN_GDLB_ID='"+gdzjs+"'");//工单数据
		String pcsl=pclb.getStr("GDLB_GDSL");
		if(pcsl==null||pcsl.isEmpty()) {
			pcsl=pclb.getStr("GDLB_DDSL");
		}
		DynaBean cxidDynaBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ","and JGMES_BASE_CXSJ_ID='"+cxid+"'");
		if (cxidDynaBean==null||cxidDynaBean.getStr("CXSJ_CXBM")==null||cxidDynaBean.getStr("CXSJ_CXMC")==null) {
			throw new PCExcuteException("产线信息获取失败，请确认。");
		}
		DynaBean bean = new DynaBean("JGMES_PLAN_SCRW", true);//生成任务单
		serviceTemplate.buildModelCreateInfo(bean);
		String RWDH=serviceTemplate.buildCode("SCRW_RWDH", "JGMES_PLAN_SCRW", bean);
		System.out.println(RWDH);
		bean.set("SCRW_RWDH", RWDH);

		bean.set("SCRW_JHKGSJ", sdf.format(new Date()));//计划开工时间
		bean.set("SCRW_PCDBH", pclb.getStr("GDLB_PCDBH"));//排产单编号
		bean.set("SCRW_PCDMC", pclb.getStr("GDLB_PCDMC"));//排产单名称
		bean.set("SCRW_CPBH", pclb.getStr("GDLB_CPBH"));//产品编号
		bean.set("SCRW_NAME", pclb.getStr("GDLB_NAME"));//产品名称
		bean.set("SCRW_GDZT_CODE", pclb.getStr("GDLB_GDZT_CODE"));//工单状态
		bean.set("SCRW_BZ", pclb.getStr("GDLB_BZ"));//备注
		bean.set("SCRW_WCL", pclb.getStr("GDLB_WCL"));//完成率
		bean.set("SCRW_SJKGSJ", pclb.getStr("GDLB_SJKGSJ"));//实际开工时间
		bean.set("SCRW_CPGG", pclb.getStr("GDLB_CPGG"));//产品规格
		bean.set("SCRW_DDHM", pclb.getStr("GDLB_DDHM"));//订单号码
		bean.set("SCRW_GDHM", pclb.getStr("GDLB_GDHM"));//工单号码
		bean.set("SCRW_DEPTNAME", pclb.getStr("GDLB_DEPTNAME"));//部门名称
		
		bean.set("SCRW_LCKH", pclb.getStr("GDLB_LCKH"));//流程卡号
		bean.set("SCRW_GDZT_NAME", pclb.getStr("GDLB_GDZT_NAME"));//工单状态_NAME
		bean.set("SCRW_WCSL", pclb.getStr("GDLB_WCSL"));//完成数量
		bean.set("SCRW_JHWGSJ", sdf.format(new Date()));//计划完工时间
		bean.set("SCRW_DEPTCODE", pclb.getStr("GDLB_DEPTCODE"));//部门编码
		bean.set("SCRW_PCRQ", sdf.format(new Date()));//排产日期
		bean.set("SCRW_SJWGSJ", pclb.getStr("GDLB_SJWGSJ"));//实际完工时间
		bean.set("SCRW_PGSL", pclb.getStr("GDLB_PGSL"));//派工数量
		bean.set("SCRW_CXBM", cxidDynaBean.getStr("CXSJ_CXBM"));//产线编码
		bean.set("SCRW_CXMC", cxidDynaBean.getStr("CXSJ_CXMC"));//产线名称
		bean.set("JGMES_PLAN_GDLB_ID", pclb.getStr("JGMES_PLAN_GDLB_ID"));//工单列表_外键ID
		bean.set("SCRW_GDSL", pcsl);//工单数量
		bean.set("SCRW_DDSL", pcsl);//订单数量
		bean.set("SCRW_PCZT_NAME", dic.getStr("DICTIONARYITEM_ITEMNAME"));//排产状态_NAME
		bean.set("SCRW_PCZT_CODE", dic.getStr("DICTIONARYITEM_ITEMCODE"));//排产状态
		bean.set("SCRW_PCZT_ID", dic.getStr("JE_CORE_DICTIONARYITEM_ID"));//排产状态_ID
		bean.set("JGMES_PLAN_MAIN_ID", pclb.getStr("JGMES_PLAN_MAIN_ID"));//排产单主表_外键ID
		bean.set("SCRW_YPCSL", pcsl);//已排产数量
		bean.set("SCRW_WPCSL", 0);//未排产数量
		bean.set("SCRW_QGRQ", pclb.getStr("GDLB_QGRQ"));//请购日期
		bean.set("SCRW_OTDRQ", pclb.getStr("GDLB_OTDRQ"));//OTD日期
		bean.set("SCRW_XPCSL", pcsl);//需排产数量
		bean.set("SCRW_KHBM", pclb.getStr("GDLB_KHBM"));//客户编码
		bean.set("SCRW_KHMC", pclb.getStr("GDLB_KHMC"));//客户名称
		bean.set("SCRW_CPXH", pclb.getStr("GDLB_CPXH"));//产品型号
		bean.set("SCRW_DTSL", pcsl);//当天数量
		bean.set("SCRW_BBH", 1);//版本号
		bean.set("SCRW_PCSL", pcsl);//排产数量
		bean.set("SCRW_RWZT_NAME", "待生产");// 任务状态_NAME
		bean.set("SCRW_RWZT_CODE", "RWZT01");// 任务状态 
		bean.set("SCRW_SCRWDATASOURCE_CODE","PC");//数据来源
		serviceTemplate.insert(bean);
		//回写工单列表
		pclb.set("GDLB_YPCSL", pcsl);
		pclb.set("GDLB_WPCSL", 0);
		pclb.set("GDLB_JHKGSJ", sdf.format(new Date()));
		pclb.set("GDLB_JHWGSJ", sdf.format(new Date()));
		pclb.set("GDLB_PCZT_NAME", dic.getStr("DICTIONARYITEM_ITEMNAME"));
		pclb.set("GDLB_PCZT_CODE", dic.getStr("DICTIONARYITEM_ITEMCODE"));//排产状态
		pclb.set("GDLB_PCZT_ID", dic.getStr("JE_CORE_DICTIONARYITEM_ID"));//排产状态_ID
		serviceTemplate.update(pclb);
		
		
	}
	/*
	 * 根据工单号将生产任务重新排序；
	 *
	 */
	private void sortRW (String gdzjs) {
		//获取指定产线的生产任务"生产中"的,排除已完成的订单
		DynaBean mbrw= serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and SCRW_RWZT_CODE!='RWZT03' ORDER BY SY_ORDERINDEX limit 0,1"); 
		if (mbrw==null) {
			throw new PCExcuteException("查询任务列表失败");
		}
		int index = mbrw.getInt("SY_ORDERINDEX");//获取最小序号
		int num = 0;//插入的任务单数量
		//更新插单的序号
		String[] gdzj=gdzjs.split(",");
		for (String string : gdzj) {
			List<DynaBean> sps = serviceTemplate.selectList("JGMES_PLAN_SCRW",
					" AND JGMES_PLAN_GDLB_ID= '" + string + "' ORDER BY SY_ORDERINDEX ");
			for (DynaBean swdynaBean : sps) {
				swdynaBean.set("SY_ORDERINDEX", index++);
				serviceTemplate.update(swdynaBean);
				num++;
				System.out.println("插单序号="+index+":"+num);
			}
		}
		//更新普通单的序号
		List<DynaBean> ptRWS=serviceTemplate.selectList("JGMES_PLAN_SCRW"," AND SCRW_RWZT_CODE!='RWZT03' "
				+ "AND JGMES_PLAN_GDLB_ID not in (" + StringUtil.buildArrayToString(gdzjs.split(",")) + ") ORDER BY SY_ORDERINDEX");
		for (DynaBean rwdynaBean : ptRWS) {
			rwdynaBean.set("SY_ORDERINDEX",rwdynaBean.getInt("SY_ORDERINDEX")+num);
			serviceTemplate.update(rwdynaBean);
		}
	}
	
}