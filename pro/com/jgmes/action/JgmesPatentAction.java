package com.jgmes.action;


import com.gexin.fastjson.JSON;
import com.gexin.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.je.core.facade.extjs.JsonBuilder;
import com.je.core.util.bean.DynaBean;
import com.jgmes.util.JgmesCommon;
import com.jgmes.util.JgmesResult;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.je.core.action.DynaAction;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Patent
 * @author lxc
 * @version 2019-07-23 15:39:37
 * @see /JGMES/jgmesPatentAction!load.action
 */
@Component("jgmesPatentAction")
@Scope("prototype")
public class JgmesPatentAction extends DynaAction  {
	
	private static final long serialVersionUID = 1L;

	public void load(){
		toWrite("hello Action");
	}


	//获取不良项
	public void  findPageBadness(){
		// 第几页
		String pages = request.getParameter("page");
		// 每页数量
		String pageSizes = request.getParameter("pageSize");
		// 工艺路线工序ID
		String gylxgxId = request.getParameter("GYLXGX_ID");
		String jsonStr = "";
		int page=Integer.parseInt(pages);
		int pageSize=Integer.parseInt(pageSizes);
		JgmesResult<List<DynaBean>> badnessData = getBadnessData(gylxgxId,page,pageSize);
		jsonStr = jsonBuilder.toJson(badnessData);
		toWrite(jsonStr);
	}
	public void badnessReport(){
		String jsonStr = request.getParameter("jsonStr");//报工主表
		String jsonArrStr = request.getParameter("jsonStrDetail");//报工子表
		String userCode = request.getParameter("userCode");//用户
		String mac = request.getParameter("mac");//mac
		String gdhm = request.getParameter("gdhm");//mac
		JSONObject jsonObject = JSON.parseObject(jsonStr);
		String barcode = jsonObject.getString("BGSJ_TMH");//条码号
		String scheduleID = jsonObject.getString("BGSJ_SCRW");//任务单号
		String cpBH = jsonObject.getString("BGSJ_CPBH");//任务单号


		JgmesResult<DynaBean> ret = new JgmesResult<DynaBean>();

		JgmesBgBatchAction jgmesBgBatchAction = new JgmesBgBatchAction();
		jgmesBgBatchAction.setServiceTemplate(serviceTemplate);

		Date date = new Date();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss");
		String format = simpleDateFormat.format(date);

		JSONObject object = new JSONObject();

		DynaBean dynaBean1 = new DynaBean();
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate,userCode);
		if(barcode!=null&&!"".equals(barcode)&&scheduleID!=null&&!"".equals(scheduleID)){
			//现在成品条码中找
			DynaBean jgmes_base_gdcptm = serviceTemplate.selectOne("JGMES_BASE_GDCPTM", " AND  GDCPTM_TMH='" + barcode + "'");
			if(jgmes_base_gdcptm!=null){
				if(scheduleID.equals(jgmes_base_gdcptm.get("GDCPTM_SCRWDH"))&&cpBH.equals(jgmes_base_gdcptm.get("GDCPTM_CPBH"))){
					if(jsonArrStr!=null&&!"[]".equals(jsonArrStr)) {
						//添加报工数据
						try {
							JgmesResult<HashMap> hashMapJgmesResult = jgmesBgBatchAction.doJsonSaveBgSjAll(jsonStr, jsonArrStr, jgmesCommon, userCode, gdhm, "");
							object.put("Status", true);
							object.put("mesage", format + "		条码：" + barcode + "		不良报工成功！");
							ret.setMessage(object.toString());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}else{
						object.put("Status",false);
						object.put("mesage",format+"		条码："+barcode+"		请选择不良项");
						ret.setMessage(object.toString());
					}
				}else{
					//换产
					dynaBean1.setStr("CPSCRWDH",jgmes_base_gdcptm.getStr("GDCPTM_SCRWDH"));
					dynaBean1.setStr("CPDDHM",jgmes_base_gdcptm.getStr("GDCPTM_DDHM"));
					dynaBean1.setStr("CPGDHM",jgmes_base_gdcptm.getStr("GDCPTM_GDHM"));
					dynaBean1.setStr("CPCPBH",jgmes_base_gdcptm.getStr("GDCPTM_CPBH"));
					dynaBean1.setStr("CPNAME",jgmes_base_gdcptm.getStr("GDCPTM_NAME"));
					dynaBean1.setStr("CPGG",jgmes_base_gdcptm.getStr("BGSJ_CPGG"));
					DynaBean SCRWDb = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and SCRW_RWDH='" + jgmes_base_gdcptm.getStr("GDCPTM_SCRWDH") + "'");
					if(SCRWDb!=null){
						dynaBean1.setStr("SCRWDH",SCRWDb.getStr("SCRW_RWDH"));
						dynaBean1.setStr("DDSL",SCRWDb.getStr("SCRW_DDSL"));
						dynaBean1.setStr("PCSL",SCRWDb.getStr("SCRW_PCSL"));
						dynaBean1.setStr("WCSL",SCRWDb.getStr("SCRW_WCSL"));
						//dynaBean1.setStr("WCSL",SCRWDb.getStr("SCRW_WCSL"));
					}
					ret.Data=dynaBean1;
					object.put("Status",false);
					object.put("mesage",format+"		条码："+barcode+"		产品和任务单不符,请再次扫码报工！！");
					ret.setMessage(object.toString());
				}
			}else{
				DynaBean jgmes_base_wltm = serviceTemplate.selectOne("JGMES_BASE_WLTM", " AND  WLTM_TMH='" + barcode + "'");
				if(jgmes_base_wltm!=null){
					DynaBean dynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " AND  PRODUCTDATA_BH='" + jgmes_base_wltm.getStr("WLTM_WLBM") + "'");
					//判断物料工艺路线是否真确
					if(dynaBean!=null){
						if(dynaBean.getStr("PRODUCTDATA_BH").equals(cpBH)){
							//if(scheduleID.equals(jgmes_base_gdcptm.get("GDCPTM_SCRWDH"))){
							if(jsonArrStr!=null&&!"[]".equals(jsonArrStr)) {
								//添加报工数据
								try {
									//添加报工数据
									JgmesResult<HashMap> hashMapJgmesResult = jgmesBgBatchAction.doJsonSaveBgSjAll(jsonStr, jsonArrStr, jgmesCommon, userCode, gdhm, "");
									object.put("Status",true);
									object.put("mesage",format+"		条码："+barcode+"		物料报工成功！");
									ret.setMessage(object.toString());
								} catch (Exception e) {
									e.printStackTrace();
								}
							}else{
								object.put("Status",false);
								object.put("mesage",format+"		条码："+barcode+"		请选择不良项");
								ret.setMessage(object.toString());
							}

						}else{
							//返回工艺路线
							ret.Data=dynaBean;
							//换产
							object.put("Status",false);
							object.put("mesage",format+"		条码："+barcode+"		加载物料条码工艺路线！");
							ret.setMessage(object.toString());
						}
					}
					//}
				}else{
					//ret.setMessage("{\"Status\":\"true\",\"mesage\":\""+format+"		此条码不能识别！\"}");
					//返回工艺路线
					object.put("Status",false);
					object.put("mesage",format+"		条码:	"+barcode+"		此条码不能识别！");
					ret.setMessage(object.toString());
				}
			}
		}else{
			ret.setMessage("条码号不能识别！");
		}
		toWrite(jsonBuilder.toJson(ret));
	}
	//根据工艺路线工序ID获取不良项
	public JgmesResult<List<DynaBean>> getBadnessData(String gylxgxId,int page,int pageSize ){
		JgmesResult<List<DynaBean>> ret = new JgmesResult<List<DynaBean>>();
		if(gylxgxId!=null&&!"".equals(gylxgxId)){
			long jgmes_gygl_gxblx1 = serviceTemplate.selectCount("JGMES_GYGL_GXBLX", " and GYLXGX_ID='" + gylxgxId + "'");
			ret.TotalCount= jgmes_gygl_gxblx1;
			if(jgmes_gygl_gxblx1>0) {
				String fy="";
				if(pageSize>0){
					pageSize=page*pageSize;
					page=(page-1)*pageSize;
					fy="limit "+page+","+pageSize;
				}else{
					fy="limit 0,"+jgmes_gygl_gxblx1;
				}
				ret.Data = serviceTemplate.selectList("JGMES_GYGL_GXBLX", " and GYLXGX_ID='" + gylxgxId + "'  " +fy);
			}
		}
		return ret;
	}
	//获取报工记录
	public void getSbuRecord(){
		String cpbh=request.getParameter("cpbh");
		String rwbh=request.getParameter("rwbh");
		String pages=request.getParameter("page");
		String pageSizes=request.getParameter("pageSize");
		int page = Integer.parseInt(pages);
		int pageSize = Integer.parseInt(pageSizes);
		JgmesResult<List<DynaBean>> ret = new JgmesResult<List<DynaBean>>();
		if(cpbh!=null&&!"".equals(cpbh)&&rwbh!=null&&!"".equals(rwbh)){
			long jgmes_gygl_gxblx1 = serviceTemplate.selectCount("JGMES_PB_BGSJ", " and BGSJ_CPBH='" + cpbh + "'  and BGSJ_SCRW='" + rwbh + "'");
			ret.TotalCount= jgmes_gygl_gxblx1;
			if(jgmes_gygl_gxblx1>0) {
				String fy="";
				if(pageSize>0){
					 page=(page-1)*pageSize;
					//pageSize=page*pageSize;
					fy=" limit "+page+","+pageSize;
				}else{
					fy=" limit 0,"+jgmes_gygl_gxblx1;
				}
				ret.Data = serviceTemplate.selectList("JGMES_PB_BGSJ", " and BGSJ_CPBH='" + cpbh + "'  and BGSJ_SCRW='" + rwbh + "'"+fy);
			}
		}
		toWrite(jsonBuilder.toJson(ret));
	}

	//
	public void dleSbuRecord(){
		String bgid=request.getParameter("bgid");
		JgmesResult<Integer> ret = new JgmesResult<>();
		if(bgid!=null&&!"".equals(bgid)){
			int jgmes_pb_bgsj = serviceTemplate.deleteByWehreSql("JGMES_PB_BGSJ", " And  JGMES_PB_BGSJ_ID='" + bgid + "'");
			if(jgmes_pb_bgsj>0){
				 serviceTemplate.deleteByWehreSql("JGMES_PB_BGSJZB", " And  JGMES_PB_BGSJ_ID='" + bgid + "'");
			}
			ret.Data=jgmes_pb_bgsj;
		}
		toWrite(jsonBuilder.toJson(ret));
	}

	
}