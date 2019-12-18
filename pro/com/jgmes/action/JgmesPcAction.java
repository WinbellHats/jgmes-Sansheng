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
	
	//�����Ų��°汾
	public void SubmitNewVerson() {
		String BBH = request.getParameter("bbh");
		int newBBH = 1 + Integer.parseInt(BBH);
		try {
			JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
			DynaBean dic = jgmesCommon.getDic("JGMES_DIC_PCZT","PCZT04");//��ȡ�Ų�״̬�ֵ��С�����Ų�����״̬
			//��������汾��
			String strSql="update JGMES_PLAN_MAIN set MAIN_BBH=ifnull(MAIN_BBH,0)+1 where JGMES_PLAN_MAIN_ID='"+pkValue+"'";
			long num=pcServiceTemplate.executeSql(strSql);
			int mDays=0;//�Ų���������
			if (num>0) {
				//�����Ų���汾��
				strSql="update JGMES_PLAN_PCLB set PCLB_BBH=(select MAIN_BBH from JGMES_PLAN_MAIN where JGMES_PLAN_MAIN_ID='"+pkValue+"') where JGMES_PLAN_MAIN_ID='"+pkValue+"'";
				pcServiceTemplate.executeSql(strSql);
				
				//���������ʷ
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
				
				//��ȡ�ѷ�������������
				strSql="select * from JGMES_PLAN_SCRW where JGMES_PLAN_MAIN_ID='"+pkValue+"'";
				List<DynaBean> scrws=serviceTemplate.selectListBySql(strSql);
				
				//������������
				strSql="select * from JGMES_PLAN_PCLB where JGMES_PLAN_MAIN_ID='"+pkValue+"'";
				List<DynaBean> lists=serviceTemplate.selectListBySql(strSql);
				String PCLB_GDHM="",PCLB_JHKGSJ="",PCLB_JHWGSJ="",PCLB_BBH="",PCLB_CPBH="";
				String iDay="",sDay="",iColName="";
				String iPcsl="",iDate="";
				int ypcsl=0;
				DateUtil dateUtil=new DateUtil();
				String jhDate=lists.get(0).getStr("PCLB_JHKGSJ");
				//���������ж�
				for (DynaBean dynaBean : lists) {
					jhDate=dynaBean.getStr("PCLB_JHKGSJ");
					if (jhDate!=null||!jhDate.isEmpty()) {
						break;
					}
				}
			    if(jhDate.isEmpty()) {
					int ypc = lists.get(0).getInt("PCLB_YPCSL");
					String gdh = lists.get(0).getStr("PCLB_GDHM");//������
					String cx = lists.get(0).getStr("PCLB_CXBM");//����
					if (ypc==0){//ֻ��Կ���
						//ɾ���ù����ţ��ڸò������Ų�������
						List<DynaBean> jgmes_plan_scrw = serviceTemplate.selectList("JGMES_PLAN_SCRW", "and SCRW_GDHM='" + gdh + "' and SCRW_CXBM='" + cx + "'");
						serviceTemplate.deleteByWehreSql("JGMES_PLAN_SCRW","and SCRW_GDHM='"+gdh+"' and SCRW_CXBM='"+cx+"'");
						toWrite(jsonBuilder.returnSuccessJson("\"����ɹ�\""));
						return;
					}
			    	toWrite(jsonBuilder.returnFailureJson("\"�Ų�����û�б仯��\""));
			    	return;
				}
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date date = sdf.parse(jhDate);
				//System.out.println(date);
				// ��ȡ��
				SimpleDateFormat y = new SimpleDateFormat("yyyy");
				int year=Integer.parseInt(y.format(date));
				// ��ȡ��
				SimpleDateFormat m = new SimpleDateFormat("MM");
				int month=Integer.parseInt(m.format(date));
				mDays=dateUtil.getMonthLastDay(year, month);//��ȡ�Ų����µ�����
				String SCRW_PCRQ="",SCRW_GDHM="";
				String yDay="";
				String transType="C";//N--���� U--���� C--������
				for (DynaBean pclb : lists) {
					PCLB_GDHM=pclb.getStr("PCLB_GDHM");//��������
					PCLB_JHKGSJ=pclb.getStr("PCLB_JHKGSJ");//�ƻ�����ʱ��
					PCLB_JHWGSJ=pclb.getStr("PCLB_JHWGSJ");//�ƻ��깤ʱ��
					PCLB_BBH=pclb.getStr("PCLB_BBH");//�汾��
					PCLB_CPBH=pclb.getStr("PCLB_CPBH");//��Ʒ���
					String PCLB_CXBM =pclb.getStr("PCLB_CXBM");
					ypcsl=getGDSL(PCLB_GDHM,PCLB_CPBH);//��ȡ�ù���ͬһ��Ʒ�����Ų���������
					
					if(PCLB_JHKGSJ.length()<8||"".equals(PCLB_JHKGSJ)) {
						//û���Ų�ʱ���ж��Ƿ��Ѿ������������ж�����Ϊ��������+���ߣ�����ɾ��
						List<DynaBean> alreadySCRWDynaBean=serviceTemplate.selectList("JGMES_PLAN_SCRW","and SCRW_GDHM='"+PCLB_GDHM+"' and SCRW_CXBM='"+PCLB_CXBM+"'");
						if(alreadySCRWDynaBean!=null&&alreadySCRWDynaBean.size()>0) {
							for (DynaBean dynaBean : alreadySCRWDynaBean) {
								serviceTemplate.delete(dynaBean);
							}
						}
						continue;
					}
					for (int i=1;i<=mDays;i++) {//���ݵ�������ѭ��
						iDay="0"+i;
						sDay=iDay.substring(iDay.length()-2, iDay.length());
						iDate=PCLB_JHKGSJ.substring(0, 8)+sDay;
						iColName="PCLB_RQ"+sDay;
						iPcsl=pclb.getStr(iColName);
						//�������ֶ���cj
						if (iPcsl==null) {
							iPcsl="0";
						}
						//�ж��Ƿ��Ѿ�����������(��֧����������粻ͬ��Ʒ)
						DynaBean alreadyDynaBean=serviceTemplate.selectOne("JGMES_PLAN_SCRW","and SCRW_GDHM='"+PCLB_GDHM+"' and SCRW_PCRQ='"+iDate+"' and SCRW_CXBM='"+PCLB_CXBM+"'");
						
						if (!iPcsl.equals("0")) {
							transType="N";
							//�����ѷ�������������,����Ѵ�������£������������SCRW_PCSL
							for (DynaBean scrw : scrws) {
								SCRW_GDHM=scrw.getStr("SCRW_GDHM");
								SCRW_PCRQ=scrw.getStr("SCRW_PCRQ");
								//�����ж����� cj
								if (SCRW_PCRQ==null) {
									toWrite(jsonBuilder.returnFailureJson("\"�������Ų������쳣����ϵ�������Ա��\""));
									return;
								}
								date = sdf.parse(SCRW_PCRQ);
								SimpleDateFormat d = new SimpleDateFormat("dd");
								yDay=d.format(date);
								//��Ҫ�����жϲ���0408cj
								String SCRW_CXBM=scrw.getStr("SCRW_CXBM");
								System.out.println(SCRW_CXBM+":"+PCLB_CXBM+":"+yDay+":"+sDay);
								if (SCRW_GDHM.equals(PCLB_GDHM) && yDay.endsWith(sDay)&& PCLB_CXBM.equals(SCRW_CXBM)) {
									transType="U";
								}
							}
							if (transType.equals("U")) {
								//�������񵥣����ϲ��ߵ��ж�0408cj��
								strSql="update JGMES_PLAN_SCRW set SCRW_JHKGSJ='"+PCLB_JHKGSJ+"',SCRW_JHWGSJ='"+PCLB_JHWGSJ+"',SCRW_PCSL="+iPcsl+",SCRW_BBH='"+PCLB_BBH+"' "
										+ "where SCRW_GDHM='"+PCLB_GDHM+"' and SCRW_PCRQ='"+iDate+"' and SCRW_CXBM='"+PCLB_CXBM+"'";
								pcServiceTemplate.executeSql(strSql);
							} else if (transType.equals("N")) {
								DynaBean bean = new DynaBean("JGMES_PLAN_SCRW", true);
								serviceTemplate.buildModelCreateInfo(bean);
								String RWDH=serviceTemplate.buildCode("SCRW_RWDH", "JGMES_PLAN_SCRW", bean);
								bean.set("SCRW_RWDH", RWDH);
								bean.set("SCRW_JHKGSJ", pclb.get("PCLB_JHKGSJ")+"");//�ƻ�����ʱ��
								bean.set("SCRW_PCDBH", pclb.get("PCLB_PCDBH")+"");//�Ų������
								bean.set("SCRW_PCDMC", pclb.get("PCLB_PCDMC")+"");//�Ų�������
								bean.set("SCRW_CPBH", pclb.get("PCLB_CPBH")+"");//��Ʒ���
								bean.set("SCRW_NAME", pclb.get("PCLB_NAME")+"");//��Ʒ����
								bean.set("SCRW_GDZT_CODE", pclb.get("PCLB_GDZT_CODE")+"");//����״̬
								bean.set("SCRW_BZ", pclb.get("PCLB_BZ")+"");//��ע
								bean.set("SCRW_WCL", pclb.get("PCLB_WCL")+"");//�����
								bean.set("SCRW_SJKGSJ", pclb.get("PCLB_SJKGSJ")+"");//ʵ�ʿ���ʱ��
								bean.set("SCRW_CPGG", pclb.get("PCLB_CPGG")+"");//��Ʒ���
								bean.set("SCRW_DDHM", pclb.get("PCLB_DDHM")+"");//��������
								bean.set("SCRW_GDHM", pclb.get("PCLB_GDHM")+"");//��������
								bean.set("SCRW_DEPTNAME", pclb.get("PCLB_DEPTNAME")+"");//��������
								bean.set("SCRW_LCKH", pclb.get("PCLB_LCKH")+"");//���̿���
								bean.set("SCRW_GDZT_NAME", pclb.get("PCLB_GDZT_NAME")+"");//����״̬_NAME
								bean.set("SCRW_WCSL", 0+"");
								bean.set("SCRW_JHWGSJ", pclb.get("PCLB_JHWGSJ")+"");//�ƻ��깤ʱ��
								bean.set("SCRW_DEPTCODE", pclb.get("PCLB_DEPTCODE")+"");//���ű���
								bean.set("SCRW_PCRQ", iDate);//�Ų�����
								bean.set("SCRW_SJWGSJ", pclb.get("PCLB_SJWGSJ")+"");//ʵ���깤ʱ��
								bean.set("SCRW_PGSL", pclb.get("PCLB_PGSL")+"");//�ɹ�����
								bean.set("SCRW_CXBM", pclb.get("PCLB_CXBM")+"");//���߱���
								bean.set("SCRW_CXMC", pclb.get("PCLB_CXMC")+"");//��������
								bean.set("JGMES_PLAN_GDLB_ID", pclb.get("JGMES_PLAN_GDLB_ID")+"");//�����б�_���ID
								bean.set("SCRW_GDSL", pclb.get("PCLB_GDSL")+"");//��������
								bean.set("SCRW_DDSL", pclb.get("PCLB_DDSL")+"");//��������
								DynaBean dic1 = jgmesCommon.getDic("JGMES_DIC_PCZT","PCZT02");
								bean.set("SCRW_PCZT_NAME", dic1.getStr("DICTIONARYITEM_ITEMNAME"));//�Ų�״̬_NAME
								bean.set("SCRW_PCZT_CODE", dic1.getStr("DICTIONARYITEM_ITEMCODE"));//�Ų�״̬
								bean.set("SCRW_PCZT_ID", dic1.getStr("JE_CORE_DICTIONARYITEM_ID"));//�Ų�״̬_ID
								bean.set("JGMES_PLAN_MAIN_ID", pclb.get("JGMES_PLAN_MAIN_ID")+"");//�Ų�������_���ID
								bean.set("SCRW_YPCSL", pclb.get("PCLB_YPCSL")+"");//���Ų�����
								bean.set("SCRW_WPCSL", pclb.get("PCLB_WPCSL")+"");//δ�Ų�����
								bean.set("SCRW_QGRQ", pclb.get("PCLB_QGRQ")+"");//�빺����
								bean.set("SCRW_OTDRQ", pclb.get("PCLB_OTDRQ")+"");//OTD����
								bean.set("SCRW_XPCSL", pclb.get("PCLB_XPCSL")+"");//���Ų�����
								bean.set("SCRW_KHBM", pclb.get("PCLB_KHBM")+"");//�ͻ�����
								bean.set("SCRW_KHMC", pclb.get("PCLB_KHMC")+"");//�ͻ�����
								bean.set("SCRW_CPXH", pclb.get("PCLB_CPXH")+"");//��Ʒ�ͺ�
								bean.set("SCRW_DTSL", pclb.get("PCLB_DTSL")+"");//��������
								bean.set("SCRW_BBH", pclb.get("PCLB_BBH")+"");//�汾��
								bean.set("SCRW_PCSL", iPcsl);//�Ų�����
								bean.set("SCRW_RWZT_NAME", "������");// ����״̬_NAME
								bean.set("SCRW_RWZT_CODE", "RWZT01");// ����״̬
								bean.set("SCRW_SCRWDATASOURCE_CODE","PC");//������Դ
								//����
								DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " order by SY_ORDERINDEX desc limit 0,1");
								int orderindex =0;
								if (scrwDynaBean==null) {//�״η�������ʱ���Ϊ1
									orderindex=1;
								}else {
									orderindex = scrwDynaBean.getInt("SY_ORDERINDEX")+1;
								}
								bean.set("SY_ORDERINDEX", orderindex);
								serviceTemplate.insert(bean);
							}

							//����ͬһ�������ŵ����Ų����������Ų�����=��������-�����Ų�����+��ǰ�Ų������Ų�����
							List<DynaBean> dynaBeans = serviceTemplate.selectList("JGMES_PLAN_PCLB", " and PCLB_GDHM='" + pclb.get("PCLB_GDHM") + "'");//��ȡͬһ�������ŵĻ���
							//��ȡ���Ų�����������
							int allYpcsl = 0;//���Ų�����
							int gdsl = pclb.getInt("PCLB_GDSL");//��������
							for (DynaBean dynaBean : dynaBeans) {
								allYpcsl+=dynaBean.getInt("PCLB_YPCSL");
							}
							int xpcsl = gdsl-allYpcsl;
							if (xpcsl>0){
								for (DynaBean dynaBean : dynaBeans) {
									dynaBean.setInt("PCLB_XPCSL",xpcsl+dynaBean.getInt("PCLB_YPCSL"));
									dynaBean.setInt("PCLB_WPCSL",xpcsl);
									serviceTemplate.update(dynaBean);
									//���¹��������Ų�����δ�Ų���
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

					//���¹����б�;cj
//					System.out.println("��ʼ���¹������Ų���");
					int gdsl=pclb.getInt("PCLB_GDSL");//��������
					DynaBean gdlb=serviceTemplate.selectOne(""
							+ "JGMES_PLAN_GDLB"," and JGMES_PLAN_GDLB_ID='"+pclb.getStr("JGMES_PLAN_GDLB_ID")+"'");
//					gdlb.set("GDLB_YPCSL", ypcsl);
////					gdlb.set("GDLB_WPCSL", pclb.getStr("PCLB_WPCSL"));
////					gdlb.set("GDLB_XPCSL", pclb.getStr("PCLB_XPCSL"));
					
					//�ж��Ƿ��Ѿ��Ų���ϸ����Ų������������Ǿ͸�״̬cj
					if (ypcsl==gdsl) {
						gdlb.set("GDLB_PCZT_CODE", dic.getStr("DICTIONARYITEM_ITEMCODE"));//���
						gdlb.set("GDLB_PCZT_NAME", dic.getStr("DICTIONARYITEM_ITEMNAME"));//����
						gdlb.set("GDLB_PCZT_ID", dic.getStr("JE_CORE_DICTIONARYITEM_ID"));//id
						List<DynaBean> bean=serviceTemplate.selectList("JGMES_PLAN_SCRW", "and SCRW_GDHM='"+PCLB_GDHM+"' and SCRW_CPBH='"+PCLB_CPBH+"'");
						for (DynaBean dynaBean : bean) {
							dynaBean.set("SCRW_PCZT_NAME", dic.getStr("DICTIONARYITEM_ITEMNAME"));//�Ų�״̬_NAME
							dynaBean.set("SCRW_PCZT_CODE", dic.getStr("DICTIONARYITEM_ITEMCODE"));//�Ų�״̬
							dynaBean.set("SCRW_PCZT_ID", dic.getStr("JE_CORE_DICTIONARYITEM_ID"));//�Ų�״̬_ID
							serviceTemplate.update(dynaBean);
						}
					}
					serviceTemplate.update(gdlb);
				}
				String sql1 = " update JGMES_PLAN_SCRW set SCRW_PCZT_CODE='PCZT03',SCRW_PCZT_NAME='��ͣ����',SCRW_RWZT_NAME='��ͣ',SCRW_RWZT_CODE='RWZT04' where SCRW_BBH="
						+ newBBH + " and JGMES_PLAN_MAIN_ID='" + pkValue
						+ "' and SCRW_PCZT_CODE='PCZT02' AND SCRW_RWZT_CODE='RWZT02'";
				String sql2 = "delete from JGMES_PLAN_SCRW where SCRW_BBH !=" + newBBH + " and JGMES_PLAN_MAIN_ID='"
						+ pkValue + "' and SCRW_RWZT_CODE='RWZT01' AND SCRW_PCZT_CODE='PCZT01'";
				pcServiceTemplate.executeSql(sql1);
				pcServiceTemplate.executeSql(sql2);
			}
			//�Ų���״̬����
			List<DynaBean> pcds=serviceTemplate.selectList("JGMES_PLAN_PCLB", "and JGMES_PLAN_MAIN_ID='"+pkValue+"'");
			if (pcds!=null) {
				for (DynaBean dynaBean : pcds) {
					String gdhm=dynaBean.getStr("PCLB_GDHM");
					String cpbm=dynaBean.getStr("PCLB_CPBH");
					int ypcsl = getGDSL(gdhm,cpbm);
					int gdsl=dynaBean.getInt("PCLB_GDSL");
					if (ypcsl==gdsl) {
						dynaBean.set("PCLB_PCZT_CODE", dic.getStr("DICTIONARYITEM_ITEMCODE"));//���
						dynaBean.set("PCLB_PCZT_NAME", dic.getStr("DICTIONARYITEM_ITEMNAME"));//����
						dynaBean.set("PCLB_PCZT_ID", dic.getStr("JE_CORE_DICTIONARYITEM_ID"));//id
					}
					serviceTemplate.update(dynaBean);
				}
			}
			toWrite(jsonBuilder.returnSuccessJson("\"����ɹ�\""));
		} catch (Exception e) {
			e.printStackTrace();
			toWrite(jsonBuilder.returnFailureJson("\"��̨���ִ���" + e.getMessage() + "\""));
		}		
	}
	
	/*
	 *���ݹ����ţ���Ʒ����  �������Ų�����(�Ų���)
	 * @return
	 */
	private int getGDSL(String gdhm,String cpbm) {
		String sql = "SELECT SUM(PCLB_YPCSL) as ypcsl from JGMES_PLAN_PCLB WHERE PCLB_GDHM='"+gdhm+"' and PCLB_CPBH='"+cpbm+"'";
		List<DynaBean> pcDynaBean = serviceTemplate.selectListBySql(sql);
		System.out.println("�������Ų�����="+pcDynaBean.get(0).getInt("ypcsl"));
		if (pcDynaBean==null||pcDynaBean.size()!=1) {
			throw new PCExcuteException("��ȡ�Ų���Ϣʧ�ܡ�");
		}
		if (pcDynaBean.get(0).getInt("ypcsl")<0) {
			throw new PCExcuteException("��ȡ���Ų�����ʧ�ܡ�");
		}
		return pcDynaBean.get(0).getInt("ypcsl");
	}
	
	/*
	   *     �Զ��Ų��ӿ�    
	 * 
	 */
	public void doAutoSort() {
		String userCode = request.getParameter("userCode");
		String cxId = request.getParameter("cxId");
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
		JgmesResult<Integer> ret = new JgmesResult<Integer>();
		List<DynaBean> cxDynaBeanList = null;  //��������List
		
		if(userCode==null || userCode.isEmpty()) {
			userCode = jgmesCommon.jgmesUser.getCurrentUserCode();
		}
		try {
			if(cxId==null || cxId.isEmpty()) {
				cxDynaBeanList = serviceTemplate.selectList("JGMES_BASE_CXSJ", " and CXSJ_STATUS_CODE = '1'");
				if(cxDynaBeanList==null||cxDynaBeanList.size()==0) {
					ret.setMessage("δ��ȡ�����ߣ�");
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
	 * ���ݲ���ID�����Զ��Ų�
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
				
				int pcsl = pcPlanDynaBean.getInt("DETAIL_XPCSL"); //��ȡ��ǰ������Ҫ�Ų�����
				String DETAIL_YQWCRQ =pcPlanDynaBean.getStr("DETAIL_YQWCRQ");
				if(DETAIL_YQWCRQ==null||DETAIL_YQWCRQ.isEmpty()) {
					System.out.println("-Ҫ���깤���ڴ���-");
					throw new PCExcuteException("Ҫ���깤���ڲ�����");
				}
				Date wcrwq = sdf.parse(DETAIL_YQWCRQ);
				//���ݲ�Ʒ��������ȡ�ò��߸ò�Ʒ�Ĳ���
				DynaBean cxCnDynaBean = serviceTemplate.selectOne("JGMES_BASE_CPCXCNINFO", " and CPCXCNINFO_BH = '"+pcPlanDynaBean.get("DETAIL_CPBH")+"' and CPCXCNINFO_CXid='"+cxId+"'");
				if(cxCnDynaBean!= null) {
					
					//���ݲ�Ʒ���߲��ܼ�����������߸�������Ʒ��Ҫ�Ĺ�ʱ���ӹ�ʱ������
					if("CNJSFS01".equals(cxCnDynaBean.get("CPCXCNINFO_CNJSFS_CODE"))) { //����Сʱ���ܼ���
						int xscn = cxCnDynaBean.getInt("CPCXCNINFO_XSCN");  //��ȡСʱ����
						int gs = pcsl%xscn == 0 ? (pcsl/xscn) : (pcsl/xscn)+1; //��ȡ��ʱ��Сʱ��
						end.setTime(wcrwq);
						end.add(Calendar.HOUR_OF_DAY, -gs);
					}else if("CNJSFS02".equals(cxCnDynaBean.get("CPCXCNINFO_CNJSFS_CODE"))) { //�����ղ��ܼ���
						int rcn = cxCnDynaBean.getInt("CPCXCNINFO_RCN");  //��ȡ�ղ���
						int gs = pcsl%rcn == 0 ? (pcsl/rcn) : (pcsl/rcn)+1;   //��ȡ��ʱ���죩
						end.setTime(wcrwq);
						end.add(Calendar.DAY_OF_MONTH, -gs);
					}
				}else {
					throw new PCExcuteException("�����øò�Ʒ���ߵĲ���");
				}
				
				Date zckgq = end.getTime();//��ȡ������ٿ���ʱ��
				pcPlanDynaBean.set("DETAIL_ZCKGSJ", sdf.format(zckgq));//��������ٿ���ʱ����뵽�Ų��ƻ�����
				serviceTemplate.update(pcPlanDynaBean);
			}
			
			DynaBean xtDynaBean = serviceTemplate.selectOne("JGMES_XTGL_XTCS"," and XTCS_CSBH='XTCS0009'");//ȡ�Զ��Ų����������׼��㷽ʽ
			
			if("1".equals(xtDynaBean.get("XTCS_CSZ"))) {   //1����������ʱ�����
				//pcPlanDynaBeanList = serviceTemplate.selectList("JGMES_PLAN_DETAIL", " and DETAIL_CXID = '"+cxId+"' and DETAIL_PCZT_NAME = '�Ų���'  and JGMES_PLAN_SCRW is null");
				String sql  = "select *,(case " + 
						"when DETAIL_NO_CODE = '1' then 1 " + 
						"when DETAIL_NO_CODE = '0' then 2 " + 
						"end) as sortDETAIL_NO_CODE from JGMES_PLAN_DETAIL " + 
						"where DETAIL_CXID ='"+cxId+"' " + 
						"and DETAIL_PCZT_CODE = 'PCZT01' " + 
						"and SY_ORDERINDEX=0 " + 
						"order by sortDETAIL_NO_CODE,DETAIL_ZCKGSJ ";
				orderBySQL(sql);
 			
			}else if("0".equals(xtDynaBean.get("XTCS_CSZ"))||xtDynaBean ==null) {//0��հ������Ƿ����׼���
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
			
			
			//���յ�ǰʱ�䡢�Ⱥ�˳�򼰼ӹ�ʱ��������������ļƻ�����ʱ�䡢�ƻ��깤ʱ�䣻
			Date  currentTime = sdf.parse(jgmesCommon.getCurrentTime());  //��ȡϵͳ��ǰ��ʱ��
			Date  currentTime1 = sdf.parse(jgmesCommon.getCurrentTime());  //��ȡϵͳ��ǰ��ʱ�䡣����Ļ�ȡ��ϵͳ��ǰʱ�䲻�����ˣ������Ѿ����ı��ˣ�����������������
			
			List<DynaBean> planDynaBeanList=serviceTemplate.selectList("JGMES_PLAN_DETAIL", " and DETAIL_CXID = '"+cxId+"' and DETAIL_PCZT_CODE = 'PCZT01'  order by SY_ORDERINDEX");
			
			if(planDynaBeanList ==null&&planDynaBeanList.size()==0) {
				throw new PCExcuteException("�����Ų�����δ�ҵ���");
			}
			DynaBean dic = jgmesCommon.getDic("JGMES_DIC_PCZT","PCZT02");
			for(DynaBean planDynaBean:planDynaBeanList) {
				
				DynaBean gdDynaBean = serviceTemplate.selectOne("JGMES_PLAN_GDLB", " and JGMES_PLAN_GDLB_ID='"+planDynaBean.getStr("JGMES_PLAN_GDLB_ID")+"'");
				
				if("PCZT03".equals(planDynaBean.getStr("DETAIL_PCZT_CODE"))) {
					//����ʣ��������Ĺ�ʱ
					int wcsl =planDynaBean.getInt("DETAIL_WCSL");  //��ȡ�ù������������
					int pcsl = planDynaBean.getInt("DETAIL_XPCSL"); //��ȡ��ǰ��Ҫ�Ų�����
					
					//���ݲ�Ʒ���߱�������ȡ�ò��߸ò�Ʒ�Ĳ���
					DynaBean cxCnDynaBean = serviceTemplate.selectOne("JGMES_BASE_CPCXCNINFO", " and CPCXCNINFO_BH = '"+planDynaBean.get("DETAIL_CPBH")+"' and CPCXCNINFO_CXid='"+cxId+"' ");
					if(cxCnDynaBean!= null) {
						if("CNJSFS01".equals(cxCnDynaBean.get("CPCXCNINFO_CNJSFS_CODE"))) { //����Сʱ���ܼ���
							int xscn = cxCnDynaBean.getInt("CPCXCNINFO_XSCN");  //��ȡСʱ����
							int spcsl = pcsl - wcsl;  //��ȡʣ��ʣ�����
							int sgs = spcsl%xscn == 0 ? (spcsl/xscn) : (spcsl/xscn)+1; //��ȡʣ�๤ʱ��Сʱ��
							int xhgs = wcsl%xscn == 0 ? (wcsl/xscn) : (wcsl/xscn)+1; //��ȡ�Ѿ����Ĺ�ʱ��Сʱ��
							//�ƻ��깤ʱ��
							end.setTime(currentTime);
							end.add(Calendar.HOUR_OF_DAY, sgs);
							//���㿪��ʱ��
							start.setTime(currentTime1);
							start.add(Calendar.HOUR_OF_DAY, -xhgs);
						}else if("CNJSFS02".equals(cxCnDynaBean.get("CPCXCNINFO_CNJSFS_CODE"))) { //�����ղ��ܼ���
							int rcn = cxCnDynaBean.getInt("CPCXCNINFO_RCN");  //��ȡ�ղ���
							int spcsl = pcsl - wcsl;  //��ȡʣ��ʣ�����
							int sgs = spcsl%rcn == 0 ? (spcsl/rcn) : (spcsl/rcn)+1; //��ȡʣ�๤ʱ���죩
							int xhgs = wcsl%rcn == 0 ? (wcsl/rcn) : (wcsl/rcn)+1; //��ȡ�Ѿ������������죩
							//�ƻ��깤ʱ��
							end.setTime(currentTime);
							end.add(Calendar.DAY_OF_MONTH, sgs);
							//���㿪��ʱ��
							start.setTime(currentTime1);
							start.add(Calendar.DAY_OF_MONTH, -xhgs);
						}else {
							throw new PCExcuteException("�����øò�Ʒ���ߵĲ���");
						}
						planDynaBean.set("DETAIL_JHKGSJ", sdf.format(start.getTime()));//���üƻ�����ʱ��
						planDynaBean.set("DETAIL_JHWGSJ", sdf.format(end.getTime()));//���üƻ��깤ʱ��
						
						//�޸��Ų�״̬ �������Ų���
						gdDynaBean.set("GDLB_PCZT_CODE", dic.get("DICTIONARYITEM_ITEMCODE"));
						gdDynaBean.set("GDLB_PCZT_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));
						gdDynaBean.set("GDLB_PCZT_ID", dic.get("JE_CORE_DICTIONARYITEM_ID"));
						serviceTemplate.update(gdDynaBean);
						planDynaBean.set("DETAIL_PCZT_CODE", dic.get("DICTIONARYITEM_ITEMCODE"));
						planDynaBean.set("DETAIL_PCZT_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));
						planDynaBean.set("DETAIL_PCZT_ID", dic.get("JE_CORE_DICTIONARYITEM_ID"));
						planDynaBean.set("DETAIL_PCRQ", jgmesCommon.getCurrentDate());
						serviceTemplate.update(planDynaBean);//cj
						
						end.setTime(end.getTime());//��һ�������ļƻ��깤ʱ������һ�������ļƻ�����ʱ��
						currentTime = end.getTime();//���üƻ�����ʱ��
						
					}
				}else {
					planDynaBean.set("DETAIL_JHKGSJ", sdf.format(currentTime)); //���üƻ�����ʱ��
					
					int pcsl = planDynaBean.getInt("DETAIL_XPCSL"); //��ȡ��ǰ������Ҫ�Ų�����
					//���ݲ�Ʒ��������ȡ�ò��߸ò�Ʒ�Ĳ���
					DynaBean cxCnDynaBean = serviceTemplate.selectOne("JGMES_BASE_CPCXCNINFO", " and CPCXCNINFO_BH = '"+planDynaBean.get("DETAIL_CPBH")+"' and CPCXCNINFO_CXid='"+cxId+"'");
					if(cxCnDynaBean!= null) {
						//���㹤���ļƻ��깤ʱ��
						if("CNJSFS01".equals(cxCnDynaBean.get("CPCXCNINFO_CNJSFS_CODE"))) { //����Сʱ���ܼ���
							int xscn = cxCnDynaBean.getInt("CPCXCNINFO_XSCN");  //��ȡСʱ����
							int gs = pcsl%xscn == 0 ? (pcsl/xscn) : (pcsl/xscn)+1; //��ȡ��ʱ��Сʱ��
							end.setTime(currentTime);
							end.add(Calendar.HOUR_OF_DAY, gs);
						}else if("CNJSFS02".equals(cxCnDynaBean.get("CPCXCNINFO_CNJSFS_CODE"))) { //�����ղ��ܼ���
							int rcn = cxCnDynaBean.getInt("CPCXCNINFO_RCN");  //��ȡ�ղ���
							int gs = pcsl%rcn == 0 ? (pcsl/rcn) : (pcsl/rcn)+1;   //��ȡ��ʱ���죩
							end.setTime(currentTime);
							end.add(Calendar.DAY_OF_MONTH, gs);
						}else {
							throw new PCExcuteException("�����øò�Ʒ���ߵĲ���");
						}
						planDynaBean.set("DETAIL_JHWGSJ", sdf.format(end.getTime()));//���üƻ��깤ʱ��
						//�޸��Ų�״̬ �������Ų���
						gdDynaBean.set("GDLB_PCZT_CODE", dic.get("DICTIONARYITEM_ITEMCODE"));
						gdDynaBean.set("GDLB_PCZT_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));
						gdDynaBean.set("GDLB_PCZT_ID", dic.get("JE_CORE_DICTIONARYITEM_ID"));
						serviceTemplate.update(gdDynaBean);
						planDynaBean.set("DETAIL_PCZT_CODE", dic.get("DICTIONARYITEM_ITEMCODE"));
						planDynaBean.set("DETAIL_PCZT_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));
						planDynaBean.set("DETAIL_PCZT_ID", dic.get("JE_CORE_DICTIONARYITEM_ID"));
						planDynaBean.set("DETAIL_PCRQ", jgmesCommon.getCurrentDate());
						serviceTemplate.update(planDynaBean);//cj
						
						end.setTime(end.getTime());//��һ�������ļƻ��깤ʱ������һ�������ļƻ�����ʱ��
						currentTime = end.getTime();//���üƻ�����ʱ��
					}
				}
				
			}
			
		}else {
			throw new PCExcuteException("������Ϣ��ȡʧ�ܣ���ѡ���Ų������ݡ�");
		}
		return result;
	}
	/*
	 * 
	 *   ���ݴ�������sql����
	 */
	public void orderBySQL(String Sql) {
		
			List<DynaBean> planDynaBeanList1=serviceTemplate.selectListBySql(Sql);//��ѯ��ȥ���������ţ�������״̬Ϊ�����еļ�¼
			List<DynaBean> planDynaBeanList2=serviceTemplate.selectList("JGMES_PLAN_DETAIL", " and SY_ORDERINDEX is not null");//�����Ĺ���˳���Ҫ���ֲ���
			List<DynaBean> planDynaBeanList3=serviceTemplate.selectList("JGMES_PLAN_DETAIL", " and DETAIL_PCZT_CODE = 'PCZT04'");//����������������״̬���Ų��ƻ���¼����ü�¼Ҫ���ڵ�һλ��
			
			List<Integer> orderIndexList = new ArrayList<Integer>();
			for(DynaBean planDynaBean:planDynaBeanList2) {//��ȡ�Ѿ������Ĺ�����˳���
				orderIndexList.add(planDynaBean.getInt("SY_ORDERINDEX"));
			}
			
			for (int i = 1; i < planDynaBeanList3.size(); i++) {//�����еĹ�������ŷ�����ǰ��
				if(!orderIndexList.contains(planDynaBeanList3.get(i).getInt("SY_ORDERINDEX"))) {
					DynaBean planDynaBean = planDynaBeanList3.get(i);
					planDynaBean.setInt("SY_ORDERINDEX", i);
					serviceTemplate.update(planDynaBean);
					orderIndexList.add(i);
				}
			}
			
			for(int i = 1;i<planDynaBeanList1.size();i++) {//��������Ĺ�����
				if(!orderIndexList.contains(planDynaBeanList1.get(i).getInt("SY_ORDERINDEX"))) {
					DynaBean planDynaBean = planDynaBeanList1.get(i);
					planDynaBean.setInt("SY_ORDERINDEX", i);
					serviceTemplate.update(planDynaBean);
				}
				
			}
		
		
	}
	
	
	/*
	 * �Զ��Ų��嵥����ִ��
	 * @param cxid ��������id
	 * @param gdids ��������id����Ϊ������
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
			//�жϹ����б������:������������;����ʱ��;
			for (DynaBean gddynaBean : sps) {
				if (gddynaBean.getStr("PCLB_JHKGSJ")!=null||gddynaBean.getInt("PCLB_YPCSL")>0) {
					toWrite(jsonBuilder.returnFailureJson("\"�������Ѿ����Ų���¼��\""));
					return ;
				}
				 if (gddynaBean.getStr("GDLB_DDJHRQ")==null||gddynaBean.getStr("GDLB_WLQTSJ")==null) {
					System.out.println("�������ڻ�����ʱ��δ����");
					toWrite(jsonBuilder.returnFailureJson("\"�������ڻ�����ʱ��δ����\""));
					return ;
				}
				 DynaBean cnDynaBean = serviceTemplate.selectOne("JGMES_BASE_CPCXCNINFO"," and CPCXCNINFO_BH='"+gddynaBean.getStr("GDLB_CPBH")+"' and CPCXCNINFO_CXid='"+cxid+"'");
				 if (cnDynaBean==null) {
					 System.out.println("������Ϣ�Ҳ���");
						toWrite(jsonBuilder.returnFailureJson("\"��������Ϣ�Ҳ���\""));
						return ;
				}
				 if (cnDynaBean.getStr("CPCXCNINFO_XSCN")==null&&cnDynaBean.getStr("CPCXCNINFO_RCN")==null) {
					 System.out.println("������Ϣδ����");
					toWrite(jsonBuilder.returnFailureJson("\"��������Ϣδ����\""));
					return ;
				}
			}
			System.out.println("��ʼ�嵥��"+cxid+":"+gdzjs);
			addSubmitNewVerson(cxid ,gdzjs);
			
			sortRW(gdzjs);
			System.out.println("��ɲ嵥��˳��ǰ�á�");
			toWrite(jsonBuilder.returnSuccessJson("\"��ɲ嵥��\""));
		} catch (Exception e) {
			toWrite(jsonBuilder.returnFailureJson("\"ϵͳ����"+e.getMessage()+"\""));
			e.printStackTrace();
		}
	}
	
	/*
	 * ���ݲ���id�͹���id��������(�Զ��嵥����)
	 * @param cxid
	 * @param gdzjs
	 */
	private void addSubmitNewVerson(String cxid , String gdzjs) {
		//���ݲ���id�͹���id��������
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
		DynaBean dic = jgmesCommon.getDic("JGMES_DIC_PCZT","PCZT04");//��ȡ�Ų�״̬�ֵ��С�����Ų�����״̬
		DynaBean pclb= serviceTemplate.selectOne("JGMES_PLAN_GDLB"," and JGMES_PLAN_GDLB_ID='"+gdzjs+"'");//��������
		String pcsl=pclb.getStr("GDLB_GDSL");
		if(pcsl==null||pcsl.isEmpty()) {
			pcsl=pclb.getStr("GDLB_DDSL");
		}
		DynaBean cxidDynaBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ","and JGMES_BASE_CXSJ_ID='"+cxid+"'");
		if (cxidDynaBean==null||cxidDynaBean.getStr("CXSJ_CXBM")==null||cxidDynaBean.getStr("CXSJ_CXMC")==null) {
			throw new PCExcuteException("������Ϣ��ȡʧ�ܣ���ȷ�ϡ�");
		}
		DynaBean bean = new DynaBean("JGMES_PLAN_SCRW", true);//��������
		serviceTemplate.buildModelCreateInfo(bean);
		String RWDH=serviceTemplate.buildCode("SCRW_RWDH", "JGMES_PLAN_SCRW", bean);
		System.out.println(RWDH);
		bean.set("SCRW_RWDH", RWDH);

		bean.set("SCRW_JHKGSJ", sdf.format(new Date()));//�ƻ�����ʱ��
		bean.set("SCRW_PCDBH", pclb.getStr("GDLB_PCDBH"));//�Ų������
		bean.set("SCRW_PCDMC", pclb.getStr("GDLB_PCDMC"));//�Ų�������
		bean.set("SCRW_CPBH", pclb.getStr("GDLB_CPBH"));//��Ʒ���
		bean.set("SCRW_NAME", pclb.getStr("GDLB_NAME"));//��Ʒ����
		bean.set("SCRW_GDZT_CODE", pclb.getStr("GDLB_GDZT_CODE"));//����״̬
		bean.set("SCRW_BZ", pclb.getStr("GDLB_BZ"));//��ע
		bean.set("SCRW_WCL", pclb.getStr("GDLB_WCL"));//�����
		bean.set("SCRW_SJKGSJ", pclb.getStr("GDLB_SJKGSJ"));//ʵ�ʿ���ʱ��
		bean.set("SCRW_CPGG", pclb.getStr("GDLB_CPGG"));//��Ʒ���
		bean.set("SCRW_DDHM", pclb.getStr("GDLB_DDHM"));//��������
		bean.set("SCRW_GDHM", pclb.getStr("GDLB_GDHM"));//��������
		bean.set("SCRW_DEPTNAME", pclb.getStr("GDLB_DEPTNAME"));//��������
		
		bean.set("SCRW_LCKH", pclb.getStr("GDLB_LCKH"));//���̿���
		bean.set("SCRW_GDZT_NAME", pclb.getStr("GDLB_GDZT_NAME"));//����״̬_NAME
		bean.set("SCRW_WCSL", pclb.getStr("GDLB_WCSL"));//�������
		bean.set("SCRW_JHWGSJ", sdf.format(new Date()));//�ƻ��깤ʱ��
		bean.set("SCRW_DEPTCODE", pclb.getStr("GDLB_DEPTCODE"));//���ű���
		bean.set("SCRW_PCRQ", sdf.format(new Date()));//�Ų�����
		bean.set("SCRW_SJWGSJ", pclb.getStr("GDLB_SJWGSJ"));//ʵ���깤ʱ��
		bean.set("SCRW_PGSL", pclb.getStr("GDLB_PGSL"));//�ɹ�����
		bean.set("SCRW_CXBM", cxidDynaBean.getStr("CXSJ_CXBM"));//���߱���
		bean.set("SCRW_CXMC", cxidDynaBean.getStr("CXSJ_CXMC"));//��������
		bean.set("JGMES_PLAN_GDLB_ID", pclb.getStr("JGMES_PLAN_GDLB_ID"));//�����б�_���ID
		bean.set("SCRW_GDSL", pcsl);//��������
		bean.set("SCRW_DDSL", pcsl);//��������
		bean.set("SCRW_PCZT_NAME", dic.getStr("DICTIONARYITEM_ITEMNAME"));//�Ų�״̬_NAME
		bean.set("SCRW_PCZT_CODE", dic.getStr("DICTIONARYITEM_ITEMCODE"));//�Ų�״̬
		bean.set("SCRW_PCZT_ID", dic.getStr("JE_CORE_DICTIONARYITEM_ID"));//�Ų�״̬_ID
		bean.set("JGMES_PLAN_MAIN_ID", pclb.getStr("JGMES_PLAN_MAIN_ID"));//�Ų�������_���ID
		bean.set("SCRW_YPCSL", pcsl);//���Ų�����
		bean.set("SCRW_WPCSL", 0);//δ�Ų�����
		bean.set("SCRW_QGRQ", pclb.getStr("GDLB_QGRQ"));//�빺����
		bean.set("SCRW_OTDRQ", pclb.getStr("GDLB_OTDRQ"));//OTD����
		bean.set("SCRW_XPCSL", pcsl);//���Ų�����
		bean.set("SCRW_KHBM", pclb.getStr("GDLB_KHBM"));//�ͻ�����
		bean.set("SCRW_KHMC", pclb.getStr("GDLB_KHMC"));//�ͻ�����
		bean.set("SCRW_CPXH", pclb.getStr("GDLB_CPXH"));//��Ʒ�ͺ�
		bean.set("SCRW_DTSL", pcsl);//��������
		bean.set("SCRW_BBH", 1);//�汾��
		bean.set("SCRW_PCSL", pcsl);//�Ų�����
		bean.set("SCRW_RWZT_NAME", "������");// ����״̬_NAME
		bean.set("SCRW_RWZT_CODE", "RWZT01");// ����״̬ 
		bean.set("SCRW_SCRWDATASOURCE_CODE","PC");//������Դ
		serviceTemplate.insert(bean);
		//��д�����б�
		pclb.set("GDLB_YPCSL", pcsl);
		pclb.set("GDLB_WPCSL", 0);
		pclb.set("GDLB_JHKGSJ", sdf.format(new Date()));
		pclb.set("GDLB_JHWGSJ", sdf.format(new Date()));
		pclb.set("GDLB_PCZT_NAME", dic.getStr("DICTIONARYITEM_ITEMNAME"));
		pclb.set("GDLB_PCZT_CODE", dic.getStr("DICTIONARYITEM_ITEMCODE"));//�Ų�״̬
		pclb.set("GDLB_PCZT_ID", dic.getStr("JE_CORE_DICTIONARYITEM_ID"));//�Ų�״̬_ID
		serviceTemplate.update(pclb);
		
		
	}
	/*
	 * ���ݹ����Ž�����������������
	 *
	 */
	private void sortRW (String gdzjs) {
		//��ȡָ�����ߵ���������"������"��,�ų�����ɵĶ���
		DynaBean mbrw= serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and SCRW_RWZT_CODE!='RWZT03' ORDER BY SY_ORDERINDEX limit 0,1"); 
		if (mbrw==null) {
			throw new PCExcuteException("��ѯ�����б�ʧ��");
		}
		int index = mbrw.getInt("SY_ORDERINDEX");//��ȡ��С���
		int num = 0;//�������������
		//���²嵥�����
		String[] gdzj=gdzjs.split(",");
		for (String string : gdzj) {
			List<DynaBean> sps = serviceTemplate.selectList("JGMES_PLAN_SCRW",
					" AND JGMES_PLAN_GDLB_ID= '" + string + "' ORDER BY SY_ORDERINDEX ");
			for (DynaBean swdynaBean : sps) {
				swdynaBean.set("SY_ORDERINDEX", index++);
				serviceTemplate.update(swdynaBean);
				num++;
				System.out.println("�嵥���="+index+":"+num);
			}
		}
		//������ͨ�������
		List<DynaBean> ptRWS=serviceTemplate.selectList("JGMES_PLAN_SCRW"," AND SCRW_RWZT_CODE!='RWZT03' "
				+ "AND JGMES_PLAN_GDLB_ID not in (" + StringUtil.buildArrayToString(gdzjs.split(",")) + ") ORDER BY SY_ORDERINDEX");
		for (DynaBean rwdynaBean : ptRWS) {
			rwdynaBean.set("SY_ORDERINDEX",rwdynaBean.getInt("SY_ORDERINDEX")+num);
			serviceTemplate.update(rwdynaBean);
		}
	}
	
}