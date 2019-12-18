package com.jgmes.action;


import com.je.core.action.DynaAction;
import com.je.core.util.JEUUID;
import com.je.core.util.bean.DynaBean;
import com.jgmes.util.JgmesCommon;
import com.jgmes.util.JgmesEnumsDic;
import com.jgmes.util.JgmesResult;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
/**
 * 
 * @author liuc
 * @version 2019-02-20 10:29:27
 * @see /jgmes/jgmesBgBatchAction!load.action
 */
@Component("jgmesBgBatchAction")
@Scope("prototype")
public class JgmesBgBatchAction extends DynaAction  {
	
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(JgmesBgBatchAction.class);
	
	public void load(){
		toWrite("hello Action");
	}
	
	//批量报工接口
	public void doJsonSave(){
//		String mac = request.getParameter("mac");// MAC地址
		String userCode = request.getParameter("userCode");// 用户编号
		String jsonString = request.getParameter("jsonStr");
		String jsonStrDetail = request.getParameter("jsonStrDetail");
		String gdhm = request.getParameter("wordcode");
		String tmlx = request.getParameter("tmlx");
		String jsonStr = "";

		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate,userCode);
		 JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
		try {
			if(tmlx == null || tmlx.isEmpty()) {
				tmlx ="TMLX01";
			}
			ret = doJsonSaveBgSjAll(jsonString, jsonStrDetail,jgmesCommon,userCode,gdhm,tmlx);
		} catch (ParseException e) {
			logger.error(jgmesCommon.getExceptionDetail(e));
			ret.IsSuccess = false;
			//ret.setMessage(jgmesCommon.getExceptionDetail(e));
			e.printStackTrace();
		} catch (SQLException e) {
			logger.error(jgmesCommon.getExceptionDetail(e));
			ret.IsSuccess = false;
			//ret.setMessage(jgmesCommon.getExceptionDetail(e));
			e.printStackTrace();
		}
		jsonStr = jsonBuilder.toJson(ret);

		toWrite(jsonStr);
	}
	
	
	/*
	 * 
	 * 获取已经报工的数量
	 */
	public void getAlreadyReportedNum() {
		String scrwId = request.getParameter("scrwId");// 用户编号
		String gxbh = request.getParameter("gxbh");
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
		 JgmesResult<Integer> ret = new JgmesResult<Integer>();
		 try {
			 ret.Data = getAlreadyReportedNum(scrwId,gxbh);
		 }catch(Exception e) {
			 ret.setMessage("系统发生异常，请联系管理员！");
			 e.printStackTrace();
		 }
		 toWrite(jsonBuilder.toJson(ret));
	}
	
	//获取已经报工的数量
	public int getAlreadyReportedNum(String scrwId,String gxbh) {
		if(scrwId!=null&&!"".equals(scrwId)&&gxbh!=null&&!"".equals(gxbh)) {
			 List<DynaBean> bgDynaBeanList = serviceTemplate.selectList("JGMES_PB_BGSJ", " and BGSJ_SCRWID = '"+scrwId+"' and BGSJ_GXBH = '"+gxbh+"' and BGSJ_BGLX = '0'");
			 if(bgDynaBeanList!=null&&bgDynaBeanList.size()>0) {
				 int blsl = 0;
				 int sl = 0;
				 for(DynaBean bgDynaBean:bgDynaBeanList) {
					 blsl = blsl + bgDynaBean.getInt("BGSJ_BLSL");
					 sl = sl + bgDynaBean.getInt("BGSJ_SL");
				 }
				 return blsl+sl;
			 }else {
				 return 0;
			 }
		 }
		 return 0;
	}
	
	
	/**
	     * 方法内容：当前工序报工数量不超过上工序数量 
	 * @param scrwId  任务单ID
	 * @param sgxbh   上到工序编码
	 * @param dbgsl   当前工序报工数量
	 * @return
	 */
	public boolean checkBgsl(String scrwId,String gxbh,String sgxbh,int dbgsl,String xtcsCode) {
		if(scrwId!=null&&!"".equals(scrwId)) {
			if("IsCheckDGSBCSS".equals(xtcsCode)&&sgxbh!=null&&!"".equals(sgxbh)) {//当前工序报工数量不超过上工序数量
				List<DynaBean> bgDynaBeanList = serviceTemplate.selectList("JGMES_PB_BGSJ", " and BGSJ_SCRWID = '"+scrwId+"' and BGSJ_GXBH = '"+sgxbh+"' and BGSJ_BGLX = '0'");
				 if(bgDynaBeanList!=null&&bgDynaBeanList.size()>0) {
					 int ybgsl = 0;
					 int blsl = 0;
					 int sl = 0;
					 for(DynaBean bgDynaBean:bgDynaBeanList) {
						 blsl = blsl + bgDynaBean.getInt("BGSJ_BLSL");
						 sl = sl + bgDynaBean.getInt("BGSJ_SL");
					 }
					 if(dbgsl+getAlreadyReportedNum(scrwId,gxbh)>blsl+sl) {
						 return true;
					 }
				 }
			}else if("ISCheckDSBCRS".equals(xtcsCode)) {//当前工序报工数量不能超过任务数量
				DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and JGMES_PLAN_SCRW_ID = '"+scrwId+"'");
				 if(scrwDynaBean!=null) {
					 int rwsl = scrwDynaBean.getInt("SCRW_PCSL");
					 if(dbgsl+getAlreadyReportedNum(scrwId,gxbh)>rwsl) {
						 return true;
					 }
				 }
			}
		 }
		return false;
	}
	
	/**
	 * 根据条码类型来获取实际的数量
	 * @return
	 */
	public int getCPsl(String cpbh,int sl,String tmlx) {
		try {
			if(tmlx!=null&&!"".equals(tmlx)) {
				if(JgmesEnumsDic.TMCP.getKey().equals(tmlx)) {
					return sl;
				}else {
					List<DynaBean> tmygzDynaBeanList = serviceTemplate.selectList("JGMES_BASE_CPTMYYGG", " and CPTMYYGG_CPBH = '"+cpbh+"'");
					if(tmygzDynaBeanList!=null&&tmygzDynaBeanList.size()>0) {
						for(DynaBean tmygzDynaBean:tmygzDynaBeanList) {
							if(tmlx.equals(tmygzDynaBean.getStr("CPTMYYGG_TMLX_CODE"))) {
								if(tmygzDynaBean.getInt("CPTMYYGG_MTMSL")!=0) {
									sl = sl*tmygzDynaBean.getInt("CPTMYYGG_MTMSL");
								}
								if(tmygzDynaBean.getStr("CPTMYYGG_NZWTMLX_CODE")!=null&&!"".equals(tmygzDynaBean.getStr("CPTMYYGG_NZWTMLX_CODE"))) {
									sl = getCPsl(cpbh,sl,tmygzDynaBean.getStr("CPTMYYGG_NZWTMLX_CODE"));
									break;
								}
							}
						}
					}
				}
			}else {
				return sl;
			}
		}catch(Exception e) {
			return sl;
		}
		return sl;
	}
	

	/*
	 * 报工主子表保存方法
	 */
	public JgmesResult<HashMap> doJsonSaveBgSjAll(String jsonStr, String jsonArrStr,JgmesCommon jgmesCommon,String userCode,String gdhm,String tmlx)
			throws ParseException, SQLException {
		JgmesResult<HashMap> jgmesResult = new JgmesResult<HashMap>();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		JgmesResult<String> jgRes = new JgmesResult<String>();// 其它方法返回结果

		boolean res = true;
		String key = "";
		String value = "";
		String tabCode = "JGMES_PB_BGSJ";
		String tabCodeDetail = "JGMES_PB_BGSJZB";
		String userName = "";
		System.out.println("jsonArrStr:" + jsonArrStr);
		List<DynaBean> list = new ArrayList<DynaBean>();
		String keyCode = JEUUID.uuid();
		String cpBh = "";// 产品编号
		String cpMc = "";// 产品名称
		String cpGg = "";// 产品规格
		String barCode = "";
		String currentGxID = "";// 当前工序ID
		String scrwId = "";// 生产任务ID
		String scrw = "";// 生产任务
		boolean isInsert = false;
		int trsl = 0;// 投入数量
		String cxbm = "";// 产线编码
		String cxmc = "";// 产线名称
		String ddhm = "";// 订单号码
		String lckh = "";// 流程卡号
		int gdsl = 0;// 工单数量
		int ddsl = 0;// 工单数量
		String cxId = "";// 产线ID

		try {
			// 获取当前登陆用户
			
			if(userCode==null || userCode.isEmpty()) {
				userCode = jgmesCommon.jgmesUser.getCurrentUserCode();
			}
			userName = jgmesCommon.jgmesUser.getCurrentUserName();

			// 将前台数据转到对象中
			DynaBean bgsj = jgmesCommon.getDynaBeanByJsonStr(tabCode, jsonStr);
			bgsj.setStr(beanUtils.KEY_TABLE_CODE, tabCode);
			bgsj.setStr(beanUtils.KEY_PK_CODE, keyCode);
			jgmesResult.IsSuccess = false; //设置IsSuccess初始值
			if (bgsj != null) {
				if (bgsj.get("BGSJ_CPBH") == null || bgsj.get("BGSJ_CPBH").toString().isEmpty()) {
					DynaBean cpDynaBean = jgmesCommon.getScrwByCxCp(userCode);
					if(cpDynaBean!=null) {
						bgsj.set("BGSJ_CPBH", cpDynaBean.get("SCRW_CPBH")); // 产品编码
						bgsj.set("BGSJ_CPMC", cpDynaBean.get("SCRW_NAME")); // 产品名称
						bgsj.set("BGSJ_CPGG", cpDynaBean.get("SCRW_CPGG")); // 产品规格
						if (bgsj.get("BGSJ_GXBH") == null || bgsj.get("BGSJ_GXBH").toString().isEmpty()) {
							DynaBean gwDynaBean = jgmesCommon.getCurrentGX(cpDynaBean.getStr("SCRW_CPBH"), userCode);
							if(gwDynaBean!=null) {
								bgsj.set("BGSJ_GXBH", gwDynaBean.get("GXGL_GXNUM")); // 工序编码
								bgsj.set("BGSJ_GXMC", gwDynaBean.get("GXGL_GXNAME")); // 工序编码
								bgsj.set("BGSJ_GXSXH", gwDynaBean.get("GXGL_GXSXH")); // 工序顺序号
								bgsj.set("BGSJ_GXID", gwDynaBean.get("GXGL_ID")); // 工序ID
							}
						}
					}
				}
				if ((bgsj.get("BGSJ_CXBM") == null || bgsj.get("BGSJ_CXBM").toString().isEmpty())
					) {
					DynaBean cxDynaBean = jgmesCommon.getCurrentCX(userCode);
					if(cxDynaBean!=null) {
						bgsj.set("BGSJ_CXBM", cxDynaBean.get("CXSJ_CXBM")); // 产线编码
						bgsj.set("BGSJ_CXMC", cxDynaBean.get("CXSJ_CXMC")); // 产线名称
					}
				}
				if((bgsj.get("BGSJ_CXBM") != null && !bgsj.get("BGSJ_CXBM").toString().isEmpty())) {
					DynaBean cxDynaBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ", " and CXSJ_CXBM = '"+bgsj.get("BGSJ_CXBM")+"'");
					if(cxDynaBean!=null) {
						bgsj.set("BGSJ_CXBM", cxDynaBean.get("CXSJ_CXBM")); // 产线编码
						bgsj.set("BGSJ_CXMC", cxDynaBean.get("CXSJ_CXMC")); // 产线名称
					}
				}
				if ((bgsj.get("BGSJ_GWBH") == null || bgsj.get("BGSJ_GWBH").toString().isEmpty())	
					) {
					DynaBean gwDynaBean = jgmesCommon.getCurrentGW(userCode);
					if(gwDynaBean!=null) {
						bgsj.set("BGSJ_GWBH", gwDynaBean.get("GW_GWBH")); // 工位编码
						bgsj.set("BGSJ_GWMC", gwDynaBean.get("GW_GWMC")); // 工位名称
						bgsj.set("BGSJ_GWID", gwDynaBean.get("JGMES_BASE_GW_ID")); //工位id
					}
				}
				if(bgsj.get("BGSJ_GWBH") != null && !bgsj.get("BGSJ_GWBH").toString().isEmpty()) {
					DynaBean gwDynaBean = serviceTemplate.selectOne("JGMES_BASE_GW", " and GW_GWBH = '"+bgsj.get("BGSJ_GWBH")+"'");
					if(gwDynaBean!=null) {
						bgsj.set("BGSJ_GWBH", gwDynaBean.get("GW_GWBH")); // 工位编码
						bgsj.set("BGSJ_GWMC", gwDynaBean.get("GW_GWMC")); // 工位名称
						bgsj.set("BGSJ_GWID", gwDynaBean.get("JGMES_BASE_GW_ID")); //工位id
					}
				}
				
				if(bgsj!=null) {
					String bgsl = bgsj.getStr("BGSJ_SL");
					String bgblsl = bgsj.getStr("BGSJ_BLSL");
					
					if ("1".equals(bgsj.getStr("BGSJ_BGLX"))) {
						if (bgsl == null || bgsl.isEmpty()) {
							bgsj.set("BGSJ_SL", 0);
						}
						if(bgblsl == null || bgblsl.isEmpty()) {
							bgsj.set("BGSJ_BLSL", 1);
						}
					} else {
						if (bgsl == null || bgsl.isEmpty()) {
							bgsj.set("BGSJ_SL", 1);
						}
						if (bgblsl == null || bgblsl.isEmpty()) {
							bgsj.set("BGSJ_BLSL", 0);
						}
					}
				}
				
				//把生产任务单号取出来
				if(bgsj.getStr("BGSJ_SCRWID")!=null&&!"".equals(bgsj.getStr("BGSJ_SCRWID"))) {
					DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and JGMES_PLAN_SCRW_ID = '"+bgsj.getStr("BGSJ_SCRWID")+"'");
					if(scrwDynaBean!=null) {
						bgsj.set("BGSJ_SCRW", scrwDynaBean.getStr("SCRW_RWDH"));
					}
				}
				
				//设置状态（默认开启）
				DynaBean dic = jgmesCommon.getDic("JGMES_STATUS","1");
				if(dic!=null) {
					bgsj.set("BGSJ_STATUS_NAME", dic.getStr("DICTIONARYITEM_ITEMNAME"));
					bgsj.set("BGSJ_STATUS_CODE", dic.getStr("DICTIONARYITEM_ITEMCODE"));
					bgsj.set("BGSJ_STATUS_ID", dic.getStr("JE_CORE_DICTIONARYITEM_ID"));
				}

				//设置报工类型
				DynaBean bglxdic = jgmesCommon.getDic("JGMEST_DIC_BGLX",bgsj.getStr("BGSJ_BGLX"));
				if(bglxdic!=null) {
					bgsj.set("BGSJ_BGLX_NAME", bglxdic.getStr("DICTIONARYITEM_ITEMNAME"));
					bgsj.set("BGSJ_BGLX_CODE", bglxdic.getStr("DICTIONARYITEM_ITEMCODE"));
				}

				//判断一下产线人数和今日工时输入是否正确
				String regex = "^(([0-9]{1}\\d{0,1})|(0{1}))(\\.\\d{1,2})?$";
				if(bgsj.get("BGSJ_JRGS")!=null&&!"".equals(bgsj.get("BGSJ_JRGS"))){
					if(!jgmesCommon.match(regex,bgsj.get("BGSJ_JRGS").toString())){
						jgmesResult.setMessage("今日工时如数格式不对，无法报工！");
						return jgmesResult;
					}
				}
				
				
				/*
				//给报工数量和报工不良数量赋值
				bgsj.set("BGSJ_BGSL", bgsj.getStr("BGSJ_SL"));   //报工数量
				bgsj.set("BGSJ_BGBLSL", bgsj.getStr("BGSJ_BLSL"));  //报工不良数量
				
				//根据条码类型来算产品数量
				bgsj.set("BGSJ_SL", getCPsl(bgsj.getStr("BGSJ_CPBH"),bgsj.getInt("BGSJ_SL"),tmlx));   //数量
				bgsj.set("BGSJ_BLSL", getCPsl(bgsj.getStr("BGSJ_CPBH"),bgsj.getInt("BGSJ_BLSL"),tmlx));  //不良数量
				*/
				
				// 过站时间
				bgsj.set("BGSJ_GZSJ", jgmesCommon.getCurrentTime());

				/*
				//检查有没有进行首件检验
				String message=jgmesCommon.checkSJ(bgsj.getStr("BGSJ_CXBM"),bgsj.getStr("BGSJ_SCRW"));
				if(message!=null&&!"".equals(message)){
					jgmesResult.setMessage(message);
					return jgmesResult;
				}
				*/
				
				//判断是否为第一道工序
				if(bgsj!=null&&bgsj.getStr("BGSJ_BGLX")!=null&&!"1".equals(bgsj.getStr("BGSJ_BGLX"))) {
					//判断当前任务单是什么状态  
					DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and JGMES_PLAN_SCRW_ID = '"+bgsj.getStr("BGSJ_SCRWID")+"'");
					if(scrwDynaBean!=null) {
						if("RWZT03".equals(scrwDynaBean.getStr("SCRW_RWZT_CODE"))) {
							jgmesResult.setMessage("该生产任务已经是完工状态，现在无法报工！");
							return jgmesResult;
						}
					}
					//先获取工艺路线id
					DynaBean cpsj = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA"," and PRODUCTDATA_BH='" + bgsj.getStr("BGSJ_CPBH") + "'");
					DynaBean gylxgxDynaBean = null;
					if(cpsj!=null) {
						String cpgylxId = cpsj.getStr("PRODUCTDATA_CPGYLXID");
						gylxgxDynaBean = serviceTemplate.selectOne("JGMES_GYGL_GYLXGX", " and GYLXGX_GXNUM = '"+bgsj.getStr("BGSJ_GXBH")+"' and GYLXGX_GXNAME = '"+bgsj.getStr("BGSJ_GXMC")+"' and GYLX_ID = '"+cpgylxId+"'");
					}
					if(gylxgxDynaBean!=null) {
						int blsl =bgsj.getInt("BGSJ_BLSL");
						int sl = bgsj.getInt("BGSJ_SL");
						
						List<DynaBean> gylxgxDynaBeanList1 = serviceTemplate.selectList("JGMES_GYGL_GYLXGX", " and GYLX_ID = '"+gylxgxDynaBean.getStr("GYLX_ID")+"' order by SY_ORDERINDEX");
						if(gylxgxDynaBeanList1!=null&&gylxgxDynaBeanList1.size()>0
						  &&gylxgxDynaBeanList1.get(0).getStr("SY_ORDERINDEX")!=null
						  &&!gylxgxDynaBeanList1.get(0).getStr("SY_ORDERINDEX").equals(gylxgxDynaBean.getStr("SY_ORDERINDEX"))
						  &&gylxgxDynaBeanList1.size()!=1)
						{
							List<DynaBean> cpcxinfoDynaBean = serviceTemplate.selectList("JGMES_PB_CPCXINFO", " and CPCXINFO_SCRWID = '"+bgsj.getStr("BGSJ_SCRWID")+"'");
							DynaBean isCheckSGXDynaBean=serviceTemplate.selectOne("JGMES_XTGL_XTCS"," and  XTCS_CXFL1_CODE = 'CXFL06' and XTCS_CXFL2_CODE = 'IsCheckSGX'");//是否必须报首工序
							if(cpcxinfoDynaBean == null&&cpcxinfoDynaBean.size()==0) {
								if(isCheckSGXDynaBean!=null&&"1".equals(isCheckSGXDynaBean.getStr("XTCS_CSZ"))) {
									jgmesResult.setMessage("首道工序未报工，请先报工首道工序！");
									return jgmesResult;
								}
							}
							String gxbh = gylxgxDynaBean.getStr("GYLXGX_GXNUM");
							String sgxbh = "";
							for (int i = gylxgxDynaBeanList1.size()-1; i > 0; i--) {
								if(gylxgxDynaBeanList1.get(i).getStr("GYLXGX_GXNUM").equals(gxbh)) {
									gxbh = gylxgxDynaBeanList1.get(i-1).getStr("GYLXGX_GXNUM");
									List<DynaBean> bgDynaBeanList = serviceTemplate.selectList("JGMES_PB_BGSJ", " and BGSJ_SCRWID = '"+bgsj.getStr("BGSJ_SCRWID")+"' and BGSJ_GXBH = '"+gxbh+"' and BGSJ_BGLX = '0'");
									if(bgDynaBeanList==null || bgDynaBeanList.size()==0) {
										 DynaBean xtcsDynaBean=serviceTemplate.selectOne("JGMES_XTGL_XTCS"," and  XTCS_CXFL1_CODE = 'CXFL06' and XTCS_CXFL2_CODE = 'IsCheckGxIndex'");//是否工序防呆
										 if(xtcsDynaBean!=null&&"1".equals(xtcsDynaBean.getStr("XTCS_CSZ"))) {
											 jgmesResult.setMessage("上道工序未报工，请先报上道工序！");
											 return jgmesResult;
										 }
									}else {
										sgxbh = gylxgxDynaBeanList1.get(i-1).getStr("GYLXGX_GXNUM");
										break;
									}
								}
							}
							DynaBean xtcsDynaBean=serviceTemplate.selectOne("JGMES_XTGL_XTCS"," and  XTCS_CXFL1_CODE = 'CXFL06' and XTCS_CXFL2_CODE = 'IsCheckDGSBCSS'");//当前工序报工数量不超过上工序数量
							if(xtcsDynaBean!=null&&"1".equals(xtcsDynaBean.getStr("XTCS_CSZ"))) {
								if(checkBgsl(bgsj.getStr("BGSJ_SCRWID"),gylxgxDynaBean.getStr("GYLXGX_GXNUM"),sgxbh,blsl+sl,"IsCheckDGSBCSS")) {
									jgmesResult.setMessage("当前工序报工数量不超过上工序数量!");
									return jgmesResult;
								}
							}
						}
						DynaBean xtcsDynaBean1=serviceTemplate.selectOne("JGMES_XTGL_XTCS"," and XTCS_CXFL1_CODE = 'CXFL06' and XTCS_CXFL2_CODE = 'ISCheckDSBCRS'");//当前工序报工数量不能超过任务数量
						if("1".equals(xtcsDynaBean1.getStr("XTCS_CSZ"))) {
							if(checkBgsl(bgsj.getStr("BGSJ_SCRWID"),gylxgxDynaBean.getStr("GYLXGX_GXNUM"),null,blsl+sl,"ISCheckDSBCRS")) {
								jgmesResult.setMessage("当前工序报工数量不能超过任务数量!");
								return jgmesResult;
							}
						}
					}else {
						jgmesResult.setMessage("获取工艺路线ID失败或者工艺路线里面有重复的工序！");
						return jgmesResult;
					}
				}
				
				DynaBean bgsj_jg = serviceTemplate.insert(bgsj);
				//查询任务单数量
				DynaBean scrwDynaBean2 = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and JGMES_PLAN_SCRW_ID = '"+bgsj_jg.getStr("BGSJ_SCRWID")+"'");
				
				jgmesResult.IsSuccess = true;
				// 将传入的参数转到表中
				if (jsonArrStr != null && !jsonArrStr.isEmpty()) {
					net.sf.json.JSONArray ja1 = net.sf.json.JSONArray.fromObject(jsonArrStr);
					if (ja1.size() > 0) {
						List<String> jcxMc = new ArrayList<String>();
						for (int i = 0; i < ja1.size(); i++) {
							DynaBean bgsjzb = new DynaBean();
							bgsjzb.setStr(beanUtils.KEY_TABLE_CODE, tabCodeDetail);

							JSONObject jb1 = ja1.getJSONObject(i); // 遍历 jsonarray 数组，把每一个对象转成 json 对象
							Iterator it1 = jb1.keys();
							while (it1.hasNext()) {
								key = (String) it1.next();
								value = jb1.getString(key);
								bgsjzb.setStr(key, value);
							}
							bgsjzb.set("JGMES_PB_BGSJ_ID", bgsj_jg.get("JGMES_PB_BGSJ_ID"));
//							bgsjzb.set("BGSJZB_WLBH", bgsj_jg.getStr("BGSJ_CPBH"));
//							bgsjzb.set("BGSJZB_WLMC", bgsj_jg.getStr("BGSJ_CPMC"));
							jgmesCommon.setDynaBeanInfo(bgsjzb);
							if(scrwDynaBean2!=null&&scrwDynaBean2.getStr("SCRW_PCSL")!=null&&bgsjzb.getInt("BGSJZB_SL")>scrwDynaBean2.getInt("SCRW_PCSL")) {//
								jcxMc.add(bgsjzb.getStr("BGSJZB_BLMC"));
							}else {
								list.add(bgsjzb);
							}
						}
						if(jcxMc!=null&&jcxMc.size()>0) {
							jgmesResult.setMessage("不良项报工数量不能超过任务数量! 检测项名字:"+jcxMc.toString());
							return jgmesResult;
						}
						serviceTemplate.insert(list);
					}
				}
				
				
				if (JgmesEnumsDic.PdJgUseless.getKey().equals(bgsj_jg.getStr("BGSJ_PDJG_CODE")))// 判定结果不合格的生成返修单
				{
					DynaBean fxdDynaBean = new DynaBean();
					fxdDynaBean.setStr(beanUtils.KEY_TABLE_CODE, "JGMES_PB_FXD");
					fxdDynaBean.setStr("FXD_CPBH", bgsj_jg.getStr("BGSJ_CPBH"));
					fxdDynaBean.setStr("FXD_NAME", bgsj_jg.getStr("BGSJ_CPMC"));
					fxdDynaBean.setStr("FXD_CPGG", bgsj_jg.getStr("BGSJ_CPGG"));
					fxdDynaBean.setStr("FXD_SCRWDID", bgsj_jg.getStr("BGSJ_SCRWID"));
					fxdDynaBean.setStr("FXD_LYGWID", bgsj_jg.getStr("BGSJ_GWID"));
					fxdDynaBean.setStr("FXD_LYGWBH", bgsj_jg.getStr("BGSJ_GWBH"));
					fxdDynaBean.setStr("FXD_LYGWMC", bgsj_jg.getStr("BGSJ_GWMC"));
					fxdDynaBean.setStr("FXD_LYYHBM", userCode);
					fxdDynaBean.setStr("FXD_LYYHMC", userName);
					fxdDynaBean.setStr("FXD_CPTMH", bgsj_jg.getStr("BGSJ_TMH"));
					fxdDynaBean.setStr("FXD_LYMACDZ", bgsj_jg.getStr("BGSJ_MACDZ"));

					jgmesCommon.setDynaBeanDic(fxdDynaBean, "JGMES_DIC_WXZT", JgmesEnumsDic.WxZtToDo.getKey(),
							"FXD_WXZT_ID", "FXD_WXZT_CODE", "FXD_WXZT_NAME");

					jgmesCommon.setDynaBeanInfo(fxdDynaBean);
					DynaBean fxdDynaBean_jg = serviceTemplate.insert(fxdDynaBean);// 插入返修单主表
					String zbJgStr = "";
					if (list != null && !list.isEmpty()) {
						List<DynaBean> fxList = new ArrayList<DynaBean>();
						for (int i = 0; i < list.size(); i++) {
							DynaBean bgsjzbDynaBean = list.get(i);
							if (bgsjzbDynaBean != null) {
								DynaBean fxdzbDynaBean = new DynaBean();
								fxdzbDynaBean.setStr(beanUtils.KEY_TABLE_CODE, "JGMES_PB_FXDZB");
								fxdzbDynaBean.setStr("JGMES_PB_FXD_ID", fxdDynaBean.getPkValue());
								fxdzbDynaBean.setStr("FXDZB_BLXBH", bgsjzbDynaBean.getStr("BGSJZB_BLBH"));
								fxdzbDynaBean.setStr("FXDZB_BLXMC", bgsjzbDynaBean.getStr("BGSJZB_BLMC"));
								fxList.add(fxdzbDynaBean);
							}
						}
						if (fxList != null && fxList.size() > 0) {
							serviceTemplate.insert(fxList);
						}
					}

				}
				
				//如果是第一道工序、最后一道工序需要回写产品产线完成情况表
				
				if(bgsj_jg!=null&&bgsj_jg.getStr("BGSJ_BGLX")!=null&&!"1".equals(bgsj_jg.getStr("BGSJ_BGLX"))) {
					//先获取工艺路线id
					DynaBean cpsj = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA"," and PRODUCTDATA_BH='" + bgsj_jg.getStr("BGSJ_CPBH") + "'");
					DynaBean gylxgxDynaBean = null;
					if(cpsj!=null) {
						String cpgylxId = cpsj.getStr("PRODUCTDATA_CPGYLXID");
						gylxgxDynaBean = serviceTemplate.selectOne("JGMES_GYGL_GYLXGX", " and GYLXGX_GXNUM = '"+bgsj_jg.getStr("BGSJ_GXBH")+"' and GYLXGX_GXNAME = '"+bgsj_jg.getStr("BGSJ_GXMC")+"' and GYLX_ID = '"+cpgylxId+"'");
					}
					if(gylxgxDynaBean!=null) {
						List<DynaBean> gylxgxDynaBeanList1 = serviceTemplate.selectList("JGMES_GYGL_GYLXGX", " and GYLX_ID = '"+gylxgxDynaBean.getStr("GYLX_ID")+"' order by SY_ORDERINDEX");
						if(gylxgxDynaBeanList1!=null&&gylxgxDynaBeanList1.size()>0
						  &&gylxgxDynaBeanList1.get(0).getStr("SY_ORDERINDEX")!=null
						  &&gylxgxDynaBeanList1.get(0).getStr("SY_ORDERINDEX").equals(gylxgxDynaBean.getStr("SY_ORDERINDEX"))) 
						{//第一道工序要产生一条产品产线完成情况表的记录
							DynaBean cpCxInfoDynaBean =   serviceTemplate.selectOne("JGMES_PB_CPCXINFO", " and CPCXINFO_SCRWID = '"+bgsj_jg.getStr("BGSJ_SCRWID")+"' and str_to_date(CPCXINFO_RQ,'%Y-%c-%d')=str_to_date(now(),'%Y-%c-%d')");
							if(cpCxInfoDynaBean == null) {
								cpCxInfoDynaBean = setCpcxInfo(bgsj_jg,gdhm,jgmesCommon);
								if(cpCxInfoDynaBean!=null) {
									serviceTemplate.insert(cpCxInfoDynaBean);
								}
							}else {
								if(bgsj_jg.getStr("BGSJ_SL")!=null&&!"".equals(bgsj_jg.getStr("BGSJ_SL"))&&bgsj_jg.getStr("BGSJ_BLSL")!=null&&!"".equals(bgsj_jg.getStr("BGSJ_BLSL"))) {
									int sl = Integer.parseInt(bgsj_jg.getStr("BGSJ_SL"));
									int blsl = Integer.parseInt(bgsj_jg.getStr("BGSJ_BLSL"));
									cpCxInfoDynaBean.set("CPCXINFO_TRSL", sl+blsl+cpCxInfoDynaBean.getInt("CPCXINFO_TRSL"));//投入数量
									serviceTemplate.update(cpCxInfoDynaBean);
								}
							}
						}
						List<DynaBean> gylxgxDynaBeanList = serviceTemplate.selectList("JGMES_GYGL_GYLXGX", " and GYLX_ID = '"+gylxgxDynaBean.getStr("GYLX_ID")+"' order by SY_ORDERINDEX desc");
						if(gylxgxDynaBeanList!=null&&gylxgxDynaBeanList.size()>0
								  &&gylxgxDynaBeanList.get(0).getStr("SY_ORDERINDEX")!=null
								  &&gylxgxDynaBeanList.get(0).getStr("SY_ORDERINDEX").equals(gylxgxDynaBean.getStr("SY_ORDERINDEX"))){
							
							if(gylxgxDynaBeanList!=null&&gylxgxDynaBeanList.size()>0) {
								if(gylxgxDynaBeanList.get(0).getStr("SY_ORDERINDEX")!=null&&!"".equals(gylxgxDynaBeanList.get(0).getStr("SY_ORDERINDEX"))&&
								   gylxgxDynaBeanList.get(0).getStr("SY_ORDERINDEX").equals(gylxgxDynaBean.getStr("SY_ORDERINDEX"))
								  ) {//这个是最后一道工序
									
									
									//如果首工序不报工的话回写问题
									DynaBean isCheckSGXDynaBean=serviceTemplate.selectOne("JGMES_XTGL_XTCS"," and  XTCS_CXFL1_CODE = 'CXFL06' and XTCS_CXFL2_CODE = 'IsCheckSGX'");//是否必须报首工序
									if(isCheckSGXDynaBean!=null&&"0".equals(isCheckSGXDynaBean.getStr("XTCS_CSZ"))) {
										
										//获取工单信息
										DynaBean gdDynaBean = serviceTemplate.selectOne("JGMES_PLAN_GDLB", " and GDLB_GDHM = '"+gdhm+"'");
										//工单的实际开工时间就是首工序的过站的时间
										if(gdDynaBean!=null&&(gdDynaBean.getStr("GDLB_SJKGSJ")==null||"".equals(gdDynaBean.getStr("GDLB_SJKGSJ")))) {
											gdDynaBean.set("GDLB_SJKGSJ", jgmesCommon.getCurrentDate());
											serviceTemplate.update(gdDynaBean);
										}
										//生产任务单的实际开工时间
										DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and JGMES_PLAN_SCRW_ID = '"+bgsj_jg.getStr("BGSJ_SCRWID")+"'");
										if(scrwDynaBean!=null&&(scrwDynaBean.getStr("SCRW_SJKGSJ")==null||"".equals(scrwDynaBean.getStr("SCRW_SJKGSJ")))) {
											scrwDynaBean.set("SCRW_SJKGSJ", jgmesCommon.getCurrentTime());
											DynaBean dic1 = jgmesCommon.getDic("JGMES_DIC_RWZT","RWZT02");
											if(dic1!=null) {
												scrwDynaBean.set("SCRW_RWZT_CODE", dic1.get("DICTIONARYITEM_ITEMCODE"));
												scrwDynaBean.set("SCRW_RWZT_NAME", dic1.get("DICTIONARYITEM_ITEMNAME"));
												scrwDynaBean.set("SCRW_RWZT_ID", dic1.get("JE_CORE_DICTIONARYITEM_ID"));
											}
											serviceTemplate.update(scrwDynaBean);
										}
									}
									
									
									//回写产品产线完成情况表
									DynaBean cpCxInfoDynaBean = serviceTemplate.selectOne("JGMES_PB_CPCXINFO", " and CPCXINFO_SCRWID = '"+bgsj_jg.getStr("BGSJ_SCRWID")+"' and str_to_date(CPCXINFO_RQ,'%Y-%c-%d')=str_to_date(now(),'%Y-%c-%d')");

									if(cpCxInfoDynaBean!=null) {
										int czsl = 0;
										int czsl2 = 0;
										if(cpCxInfoDynaBean.getStr("CPCXINFO_CZSL")!=null&&!"".equals(cpCxInfoDynaBean.getStr("CPCXINFO_CZSL"))) {
											czsl = Integer.parseInt(cpCxInfoDynaBean.getStr("CPCXINFO_CZSL"));
										}
										if(bgsj_jg.getStr("BGSJ_SL")!=null&&!"".equals(bgsj_jg.getStr("BGSJ_SL"))) {
											czsl2 = Integer.parseInt(bgsj_jg.getStr("BGSJ_SL"));
										}
										cpCxInfoDynaBean.set("CPCXINFO_CZSL", czsl+czsl2);
										cpCxInfoDynaBean.set("CPCXINFO_WCSJ", jgmesCommon.getCurrentTime());
										serviceTemplate.update(cpCxInfoDynaBean);
									}else{
										DynaBean bgDynaBean = bgsj_jg.clone();
										bgDynaBean.set("BGSJ_SL",0);
										bgDynaBean.set("BGSJ_BLSL",0);
										cpCxInfoDynaBean = setCpcxInfo(bgDynaBean,gdhm,jgmesCommon);
										if(cpCxInfoDynaBean!=null) {
											cpCxInfoDynaBean = serviceTemplate.insert(cpCxInfoDynaBean);
											cpCxInfoDynaBean.set("CPCXINFO_CZSL",cpCxInfoDynaBean.getInt("CPCXINFO_CZSL")+bgsj_jg.getInt("BGSJ_SL"));
											cpCxInfoDynaBean.set("CPCXINFO_WCSJ", jgmesCommon.getCurrentTime());
											serviceTemplate.update(cpCxInfoDynaBean);
										}
									}
									
									DynaBean xtcsDynaBean=serviceTemplate.selectOne("JGMES_XTGL_XTCS"," and XTCS_CXFL1_CODE = 'CXFL06' and XTCS_CXFL2_CODE = 'IsCheckBSW'");//不合格数量是否算完成数量
									boolean isCheck = false;
									if("1".equals(xtcsDynaBean.getStr("XTCS_CSZ"))) {
										isCheck = true;
									}
									//回写到生产任务单
									DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and JGMES_PLAN_SCRW_ID = '"+bgsj_jg.getStr("BGSJ_SCRWID")+"'");
									
									if(scrwDynaBean!=null) {
										int lpsl = 0;
										int blsl = 0;
												
										if(scrwDynaBean.getStr("SCRW_LPSL")!=null&&!"".equals(scrwDynaBean.getStr("SCRW_LPSL"))) {
											lpsl = Integer.parseInt(scrwDynaBean.getStr("SCRW_LPSL"));
										}
										if(scrwDynaBean.getStr("SCRW_BLSL")!=null&&!"".equals(scrwDynaBean.getStr("SCRW_BLSL"))) {
											blsl = Integer.parseInt(scrwDynaBean.getStr("SCRW_BLSL"));
										}
										scrwDynaBean.set("SCRW_LPSL", lpsl+Integer.parseInt(bgsj_jg.getStr("BGSJ_SL"))); //良品数量
										scrwDynaBean.set("SCRW_BLSL", blsl+Integer.parseInt(bgsj_jg.getStr("BGSJ_BLSL"))); //不良数量
										if(isCheck) {
											scrwDynaBean.set("SCRW_WCSL", scrwDynaBean.getInt("SCRW_WCSL")+bgsj_jg.getInt("BGSJ_SL")+bgsj_jg.getInt("BGSJ_BLSL")); //完成数量
										}else {
											scrwDynaBean.set("SCRW_WCSL", lpsl+Integer.parseInt(bgsj_jg.getStr("BGSJ_SL"))); //完成数量
										}
										if(scrwDynaBean.getStr("SCRW_LPSL")!=null&&!"".equals(scrwDynaBean.getStr("SCRW_LPSL"))&&
												scrwDynaBean.getStr("SCRW_PCSL")!=null&&!"".equals(scrwDynaBean.getStr("SCRW_PCSL"))) {
											lpsl = Integer.parseInt(scrwDynaBean.getStr("SCRW_LPSL"));
											blsl = Integer.parseInt(scrwDynaBean.getStr("SCRW_BLSL"));
											int pcsl = Integer.parseInt(scrwDynaBean.getStr("SCRW_PCSL"));
											int bgsl = 0;
											if(isCheck) {//不合格数量是否算完成数量
												bgsl = lpsl+blsl;
											}else {
												bgsl = lpsl;
											}
											if(bgsl>=pcsl) {
												DynaBean dic1 = jgmesCommon.getDic("JGMES_DIC_RWZT","RWZT03");
												if(dic1!=null) {
													scrwDynaBean.set("SCRW_RWZT_CODE", dic1.get("DICTIONARYITEM_ITEMCODE"));
													scrwDynaBean.set("SCRW_RWZT_NAME", dic1.get("DICTIONARYITEM_ITEMNAME"));
													scrwDynaBean.set("SCRW_RWZT_ID", dic1.get("JE_CORE_DICTIONARYITEM_ID"));
												}
												scrwDynaBean.set("SCRW_SJWGSJ", jgmesCommon.getCurrentTime());
											}
											
										}
										serviceTemplate.update(scrwDynaBean);
									}
									
									//回写到工单列表中
									DynaBean gdDynaBean = serviceTemplate.selectOne("JGMES_PLAN_GDLB", " and GDLB_GDHM = '"+gdhm+"'");
									
									
									
									if(gdDynaBean!=null) {
										//获取排产单
										DynaBean padDynaBean = serviceTemplate.selectOne("JGMES_PLAN_PCLB", " and PCLB_DDHM = '"+gdDynaBean.getStr("GDLB_DDHM")+"' and JGMES_PLAN_GDLB_ID = '"+gdDynaBean.getStr("JGMES_PLAN_GDLB_ID")+"'");
										
										boolean isComplete = true;
										int wcsl = 0;
										List<DynaBean> scrwDynaBeanList = serviceTemplate.selectList("JGMES_PLAN_SCRW", " and SCRW_GDHM = '"+gdhm+"'");
										if(scrwDynaBeanList!=null&&scrwDynaBeanList.size()>0) {
											for(DynaBean scrwDynaBean1:scrwDynaBeanList) {
												if("RWZT01".equals(scrwDynaBean1.getStr("SCRW_RWZT_CODE"))||
													"RWZT02".equals(scrwDynaBean1.getStr("SCRW_RWZT_CODE"))||
													"RWZT04".equals(scrwDynaBean1.getStr("SCRW_RWZT_CODE"))) {
													isComplete = false;
												}
												if(scrwDynaBean1.getStr("SCRW_LPSL")!=null&&!"".equals(scrwDynaBean1.getStr("SCRW_LPSL"))) {//Float.parseFloat(a1)
													wcsl = wcsl + Integer.parseInt(scrwDynaBean1.getStr("SCRW_WCSL"));
												}
												
											}
										}
										gdDynaBean.set("GDLB_WCSL", wcsl);
										if(gdDynaBean.getStr("GDLB_DDSL")!=null&&!"".equals(gdDynaBean.getStr("GDLB_DDSL"))) {
											gdDynaBean.set("GDLB_WCL", (float)wcsl/Float.parseFloat(gdDynaBean.getStr("GDLB_DDSL")));
										}
										DynaBean dic1 = jgmesCommon.getDic("JGMES_DIC_GDZT","2");
										if(isComplete&&gdDynaBean!=null&&wcsl>=gdDynaBean.getInt("GDLB_GDSL")) {
											//还原到以前版本   isComplete&&"PCZT04".equals(padDynaBean.getStr("PCLB_PCZT_CODE"))
											gdDynaBean.set("GDLB_GDZT_CODE", dic1.get("DICTIONARYITEM_ITEMCODE"));
											gdDynaBean.set("GDLB_GDZT_NAME", dic1.get("DICTIONARYITEM_ITEMNAME"));
											gdDynaBean.set("GDLB_SJWGSJ", jgmesCommon.getCurrentDate());
											
											//修改生产任务中的工单状态
											List<DynaBean> scrwDynaBean1List = serviceTemplate.selectList("JGMES_PLAN_SCRW", " and SCRW_GDHM = '"+gdhm+"'");
											if(scrwDynaBean1List!=null&&scrwDynaBean1List.size()>0) {
												for(DynaBean scrwDynaBean1:scrwDynaBean1List) {
													scrwDynaBean1.set("SCRW_GDZT_CODE", dic1.get("DICTIONARYITEM_ITEMCODE"));
													scrwDynaBean1.set("SCRW_GDZT_NAME", dic1.get("DICTIONARYITEM_ITEMNAME"));
													serviceTemplate.update(scrwDynaBean1);
												}
											}
										}
										serviceTemplate.update(gdDynaBean);
										
										
										//修改排产单中的已经完成数量
										if(padDynaBean!=null) {
											int pcwcsl = padDynaBean.getInt("PCLB_WCSL");
											if(isCheck){
												padDynaBean.set("PCLB_WCSL", pcwcsl+bgsj_jg.getInt("BGSJ_SL")+bgsj_jg.getInt("BGSJ_BLSL"));
											}else {
												padDynaBean.set("PCLB_WCSL", pcwcsl+bgsj_jg.getInt("BGSJ_SL"));
											}
											if(isComplete) {
												padDynaBean.set("PCLB_GDZT_CODE", dic1.get("DICTIONARYITEM_ITEMCODE"));
												padDynaBean.set("PCLB_GDZT_NAME", dic1.get("DICTIONARYITEM_ITEMNAME"));
											}
											
											serviceTemplate.update(padDynaBean);
										}
									}
								}
							}
						}else {
							//如果首工序不报工的话回写问题
							DynaBean isCheckSGXDynaBean=serviceTemplate.selectOne("JGMES_XTGL_XTCS"," and  XTCS_CXFL1_CODE = 'CXFL06' and XTCS_CXFL2_CODE = 'IsCheckSGX'");//是否必须报首工序
							if(isCheckSGXDynaBean!=null&&"0".equals(isCheckSGXDynaBean.getStr("XTCS_CSZ"))) {
								
								/*
								//回写产品产线完成情况表
								DynaBean cpCxInfoDynaBean = serviceTemplate.selectOne("JGMES_PB_CPCXINFO", " and CPCXINFO_SCRWID = '"+bgsj_jg.getStr("BGSJ_SCRWID")+"'");
								if(cpCxInfoDynaBean == null) {
									cpCxInfoDynaBean = setCpcxInfo(bgsj_jg,gdhm,jgmesCommon);
									if(cpCxInfoDynaBean!=null) {
										serviceTemplate.insert(cpCxInfoDynaBean);
									}
								}else {
									DynaBean cpCxInfoDynaBean = serviceTemplate.selectOne("JGMES_PB_CPCXINFO", " and CPCXINFO_SCRWID = '"+bgsj_jg.getStr("BGSJ_SCRWID")+"'");
								}*/
								
								
								//获取工单信息
								DynaBean gdDynaBean = serviceTemplate.selectOne("JGMES_PLAN_GDLB", " and GDLB_GDHM = '"+gdhm+"'");
								//工单的实际开工时间就是首工序的过站的时间
								if(gdDynaBean!=null&&(gdDynaBean.getStr("GDLB_SJKGSJ")==null||"".equals(gdDynaBean.getStr("GDLB_SJKGSJ")))) {
									gdDynaBean.set("GDLB_SJKGSJ", jgmesCommon.getCurrentDate());
									serviceTemplate.update(gdDynaBean);
								}
								//生产任务单的实际开工时间
								DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and JGMES_PLAN_SCRW_ID = '"+bgsj_jg.getStr("BGSJ_SCRWID")+"'");
								if(scrwDynaBean!=null&&(scrwDynaBean.getStr("SCRW_SJKGSJ")==null||"".equals(scrwDynaBean.getStr("SCRW_SJKGSJ")))) {
									scrwDynaBean.set("SCRW_SJKGSJ", jgmesCommon.getCurrentTime());
									DynaBean dic1 = jgmesCommon.getDic("JGMES_DIC_RWZT","RWZT02");
									if(dic1!=null) {
										scrwDynaBean.set("SCRW_RWZT_CODE", dic1.get("DICTIONARYITEM_ITEMCODE"));
										scrwDynaBean.set("SCRW_RWZT_NAME", dic1.get("DICTIONARYITEM_ITEMNAME"));
										scrwDynaBean.set("SCRW_RWZT_ID", dic1.get("JE_CORE_DICTIONARYITEM_ID"));
									}
									serviceTemplate.update(scrwDynaBean);
								}
							}
						}
						
					}
					
				}
				jgmesResult.Data = bgsj_jg.getValues();
			}
		} catch (Exception e) {
			logger.error(jgmesCommon.getExceptionDetail(e));
			res = false;
			e.printStackTrace();
			jgmesResult.setMessage("系统发生异常，请联系管理员！");
		}

		return jgmesResult;
	}

	public DynaBean setCpcxInfo(DynaBean bgsj_jg,String gdhm,JgmesCommon jgmesCommon) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		DynaBean cpCxInfoDynaBean = new DynaBean();
		if(bgsj_jg!=null) {
			//获取工单信息
			DynaBean gdDynaBean = serviceTemplate.selectOne("JGMES_PLAN_GDLB", " and GDLB_GDHM = '"+gdhm+"'");
			//工单的实际开工时间就是首工序的过站的时间
			if(gdDynaBean!=null&&(gdDynaBean.getStr("GDLB_SJKGSJ")==null||"".equals(gdDynaBean.getStr("GDLB_SJKGSJ")))) {
				gdDynaBean.set("GDLB_SJKGSJ", jgmesCommon.getCurrentDate());
				serviceTemplate.update(gdDynaBean);
			}
			//生产任务单的实际开工时间
			DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and JGMES_PLAN_SCRW_ID = '"+bgsj_jg.getStr("BGSJ_SCRWID")+"'");
			if(scrwDynaBean!=null&&(scrwDynaBean.getStr("SCRW_SJKGSJ")==null||"".equals(scrwDynaBean.getStr("SCRW_SJKGSJ")))) {
				scrwDynaBean.set("SCRW_SJKGSJ", jgmesCommon.getCurrentTime());
				DynaBean dic1 = jgmesCommon.getDic("JGMES_DIC_RWZT","RWZT02");
				if(dic1!=null) {
					scrwDynaBean.set("SCRW_RWZT_CODE", dic1.get("DICTIONARYITEM_ITEMCODE"));
					scrwDynaBean.set("SCRW_RWZT_NAME", dic1.get("DICTIONARYITEM_ITEMNAME"));
					scrwDynaBean.set("SCRW_RWZT_ID", dic1.get("JE_CORE_DICTIONARYITEM_ID"));
				}
				serviceTemplate.update(scrwDynaBean);
			}
			cpCxInfoDynaBean.setStr(beanUtils.KEY_TABLE_CODE, "JGMES_PB_CPCXINFO");
			cpCxInfoDynaBean.set("JGMES_PB_CPCXINFO_ID", JEUUID.uuid());
			cpCxInfoDynaBean.set("CPCXINFO_CPBH", bgsj_jg.getStr("BGSJ_CPBH"));//产品编码
			cpCxInfoDynaBean.set("CPCXINFO_CPMC", bgsj_jg.getStr("BGSJ_CPMC"));//产品名称
			cpCxInfoDynaBean.set("CPCXINFO_CPGG", bgsj_jg.getStr("BGSJ_CPGG"));//产品规格
			if(bgsj_jg.getStr("BGSJ_SL")!=null&&!"".equals(bgsj_jg.getStr("BGSJ_SL"))&&bgsj_jg.getStr("BGSJ_BLSL")!=null&&!"".equals(bgsj_jg.getStr("BGSJ_BLSL"))) {
				int sl = Integer.parseInt(bgsj_jg.getStr("BGSJ_SL"));
				int blsl = Integer.parseInt(bgsj_jg.getStr("BGSJ_BLSL"));
				cpCxInfoDynaBean.set("CPCXINFO_TRSL", sl+blsl);//投入数量
			}
			cpCxInfoDynaBean.set("CPCXINFO_KSSJ", bgsj_jg.getStr("BGSJ_GZSJ"));//开始时间
			cpCxInfoDynaBean.set("CPCXINFO_CXBM", bgsj_jg.getStr("BGSJ_CXBM"));//产线编码
			cpCxInfoDynaBean.set("CPCXINFO_CXMC", bgsj_jg.getStr("BGSJ_CXMC"));//产线名称
			if(bgsj_jg.getStr("BGSJ_CXBM")!=null && !"".equals(bgsj_jg.getStr("BGSJ_CXBM"))) {
				DynaBean cxDynaBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ", " and CXSJ_CXBM = '"+bgsj_jg.getStr("BGSJ_CXBM")+"'");
				cpCxInfoDynaBean.set("JGMES_BASE_CXSJ_ID", cxDynaBean.getStr("JGMES_BASE_CXSJ_ID"));//产线数据_外键ID
			}
			//cpCxInfoDynaBean.set("CPCXINFO_CZSL", bgsj_jg.getStr("BGSJ_SL"));//出站数量 暂时用合格数量，后面再看
			//cpCxInfoDynaBean.set("CPCXINFO_WCSJ", bgsj_jg.getStr("BGSJ_CPBH"));//完成时间 
			if(gdDynaBean!=null) {
				cpCxInfoDynaBean.set("CPCXINFO_GDHM", gdDynaBean.getStr("GDLB_GDHM"));//工单号码
				cpCxInfoDynaBean.set("CPCXINFO_DDHM", gdDynaBean.getStr("GDLB_DDHM"));//订单号码    
				cpCxInfoDynaBean.set("CPCXINFO_LCKH", gdDynaBean.getStr("GDLB_LCKH"));//流程卡号
				cpCxInfoDynaBean.set("CPCXINFO_GDSL", gdDynaBean.getStr("GDLB_GDSL"));//工单数量   
				cpCxInfoDynaBean.set("CPCXINFO_XPCSL", gdDynaBean.getStr("GDLB_XPCSL"));//需排产数量  
				cpCxInfoDynaBean.set("CPCXINFO_DDSL", gdDynaBean.getStr("GDLB_DDSL"));//订单数量
			}
			cpCxInfoDynaBean.set("CPCXINFO_SCRW", bgsj_jg.getStr("BGSJ_SCRW"));//任务单号
			
			
			
			cpCxInfoDynaBean.set("CPCXINFO_SCRWID", bgsj_jg.getStr("BGSJ_SCRWID"));//任务单ID
			//cpCxInfoDynaBean.set("CPCXINFO_FXSL", bgsj_jg.getStr("BGSJ_CPBH"));//返修数量   1
			cpCxInfoDynaBean.set("CPCXINFO_BHGSL", bgsj_jg.getStr("BGSJ_BLSL"));//不合格数量 
			//cpCxInfoDynaBean.set("CPCXINFO_FXCS", bgsj_jg.getStr("BGSJ_CPBH"));//返修次数   1
			cpCxInfoDynaBean.set("CPCXINFO_RQ", sdf.format(new Date()));//日期
			
		}
		return cpCxInfoDynaBean;
	}
	
}