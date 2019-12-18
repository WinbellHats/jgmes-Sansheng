package com.jgmes.action;

import com.je.core.action.DynaAction;
import com.je.core.util.JEUUID;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.DynaBean;
import com.jgmes.util.JgmesCommon;
import com.jgmes.util.JgmesResult;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
/**
 * Ѳ��ģ��
 * @author liuc
 * @version 2019-03-12 16:20:10
 * @see /jgmes/jgmesXunJianAction!load.action
 */
@Component("jgmesXunJianAction")
@Scope("prototype")
public class JgmesXunJianAction extends DynaAction  {
	
	private static final long serialVersionUID = 1L;
	@Override
	public void load(){
		toWrite("hello Action");
	}
	
	/*
	 * 
	 * 获取车间数据
	 * 
	 */
	
	public void getCjSu() {
		JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
		try {
			String sql = "SELECT STATUS,DEPTNAME,DEPTCODE,RANKCODE,CHARGEUSERNAME,PHONE,ORDERINDEX,ADDRESS,TREEORDERINDEX,CHARGEUSER,RANKNAME,PARENT,DEPTID,PATH,NODETYPE,JTGSID,JTGSMC,GSBMID \r\n" + 
					"FROM JE_CORE_DEPARTMENT WHERE STATUS='1' and  rankcoDE='CEJIN' and PARENT !=''    ORDER BY TREEORDERINDEX";
			List<DynaBean> cjList = serviceTemplate.selectListBySql(sql);
			if (cjList != null && cjList.size() > 0) {
				ret.Data = ret.getValues(cjList);
				ret.TotalCount = (long) cjList.size();
			}else {
				ret.setMessage("未获取到车间数据！");
			}
		} catch (Exception e) {
			logger.error(e.toString());
			ret.setMessage(e.toString());
		}
		toWrite(jsonBuilder.toJson(ret));
	}
	
	/**
	 * 获取公有的检测项目
	 * 
	 */
	public void getJCX() {
		JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
		try {
			List<DynaBean> cxList = serviceTemplate.selectList("JGMES_ZLGL_JCXM", " and JCXM_JCXMLX_CODE='JCXMLX01' ");
			if (cxList != null && cxList.size() > 0) {
				ret.Data = ret.getValues(cxList);
				ret.TotalCount = (long) cxList.size();
			}else {
				ret.setMessage("未获取到检测项目数据！");
			}
		} catch (Exception e) {
			logger.error(e.toString());
			ret.setMessage(e.toString());
		}
		toWrite(jsonBuilder.toJson(ret));
	
	}
	/**
	 * 巡检功能保存，巡检表：JGMES_PB_XJB ， 巡检表子表:JGMES_PB_XJBZB
	 * 
	 */
	public void doXunJianSave() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyMM");
		String jsonStr = request.getParameter("jsonStr");
		String jsonStrDetail = request.getParameter("jsonStrDetail");
		String bhgsl = request.getParameter("bhgsl");
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
		JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
		try {
			if(jsonStr!=null&&!"".equals(jsonStr)) {
				DynaBean xjbDynaBean = jgmesCommon.getDynaBeanByJsonStr("JGMES_PB_XJB", jsonStr);
				if(xjbDynaBean!=null) {
				//	if(xjbDynaBean.getStr("XJB_XJDBH")!=null&&!"".equals(xjbDynaBean.getStr("XJB_XJDBH"))) {
						DynaBean xjbDynaBean1 = serviceTemplate.selectOne("JGMES_PB_XJB", " and XJB_XJDBH = '"+xjbDynaBean.getStr("XJB_XJDBH")+"'");
						if(xjbDynaBean1!=null) {
							xjbDynaBean.set("JGMES_PB_XJB_ID", xjbDynaBean1.getStr("JGMES_PB_XJB_ID"));
							xjbDynaBean.set("XJB_RQ", jgmesCommon.getCurrentTime());
							xjbDynaBean = serviceTemplate.update(xjbDynaBean);
						}else {
							List<DynaBean> xjbDynaBeanList = serviceTemplate.selectList("JGMES_PB_XJB", " and XJB_XJDBH like '%XJ"+sdf.format(new Date())+"%' order by XJB_XJDBH desc");
							xjbDynaBean.set("JGMES_PB_XJB_ID", JEUUID.uuid());
							xjbDynaBean.set("XJB_RQ", jgmesCommon.getCurrentTime());
							if(xjbDynaBeanList!=null&&xjbDynaBeanList.size()>0) {
								String XJB_XJDBH = xjbDynaBeanList.get(0).getStr("XJB_XJDBH");
								if(XJB_XJDBH!=null&&!"".equals(XJB_XJDBH)) {
									int lsh = Integer.parseInt(XJB_XJDBH.substring(XJB_XJDBH.length()-5)) + 1;
									xjbDynaBean.set("XJB_XJDBH", "XJ"+sdf.format(new Date())+lsh);
								}
							}else {
								xjbDynaBean.set("XJB_XJDBH", "XJ"+sdf.format(new Date())+"00001");
							}
							xjbDynaBean = serviceTemplate.insert(xjbDynaBean);
						}
					}
				//}
				if(jsonStrDetail!=null&&!"".equals(jsonStrDetail)) {
					List<DynaBean> xibzbDynaBeanList = jgmesCommon.getListDynaBeanByJsonStr("JGMES_PB_XJBZB", jsonStrDetail);
					List<DynaBean> xibzbDynaBeanListForInsert = new ArrayList<DynaBean>();
					if(xibzbDynaBeanList!=null&&xibzbDynaBeanList.size()>0) {
						for(DynaBean xibzbDynaBean:xibzbDynaBeanList) {
							if(!"".equals(xibzbDynaBean.getStr(""))) {
								DynaBean xibzbDynaBean1 = serviceTemplate.selectOne("JGMES_PB_XJBZB", " and XJBZB_XJDBH = '"+xibzbDynaBean.getStr("XJBZB_XJDBH")+"' and XJBZB_JCXBH = '"+xibzbDynaBean.getStr("XJBZB_JCXBH")+"'");
								if(xibzbDynaBean1!=null) {
									xibzbDynaBean.set("JGMES_PB_XJBZB_ID", xibzbDynaBean1.getStr("JGMES_PB_XJBZB_ID"));
									xibzbDynaBean.set("JGMES_PB_XJB_ID", xjbDynaBean.getStr("JGMES_PB_XJB_ID"));
									xibzbDynaBean.set("XJBZB_XJDBH", xjbDynaBean.getStr("XJB_XJDBH"));
									if(xibzbDynaBean.getStr("XJBZB_BHGSL")==null||"".equals(xibzbDynaBean.getStr("XJBZB_BHGSL"))) {
										xibzbDynaBean.set("XJBZB_BHGSL", 1);
									}
									if(xibzbDynaBean.getStr("JGMES_ZLGL_JCXM_ID")==null||"".equals(xibzbDynaBean.getStr("JGMES_ZLGL_JCXM_ID"))) {
										DynaBean xjxmDynaBean = serviceTemplate.selectOne("JGMES_ZLGL_JCXM", " and JCXM_JCXBH  = '"+xibzbDynaBean.getStr("XJBZB_JCXBH")+"'");
										if(xjxmDynaBean!=null) {
											xibzbDynaBean.set("JGMES_ZLGL_JCXM_ID", xjxmDynaBean.getStr("JGMES_ZLGL_JCXM_ID"));
										}
									}
									serviceTemplate.update(xibzbDynaBean);
								}else {
									xibzbDynaBean.set("JGMES_PB_XJBZB_ID", JEUUID.uuid());
									xibzbDynaBean.set("JGMES_PB_XJB_ID", xjbDynaBean.getStr("JGMES_PB_XJB_ID"));
									xibzbDynaBean.set("XJBZB_XJDBH", xjbDynaBean.getStr("XJB_XJDBH"));
									if(xibzbDynaBean.getStr("XJBZB_BHGSL")==null||"".equals(xibzbDynaBean.getStr("XJBZB_BHGSL"))) {
										xibzbDynaBean.set("XJBZB_BHGSL", 1);
									}
									if(xibzbDynaBean.getStr("JGMES_ZLGL_JCXM_ID")==null||"".equals(xibzbDynaBean.getStr("JGMES_ZLGL_JCXM_ID"))) {
										DynaBean xjxmDynaBean = serviceTemplate.selectOne("JGMES_ZLGL_JCXM", " and JCXM_JCXBH  = '"+xibzbDynaBean.getStr("XJBZB_JCXBH")+"'");
										if(xjxmDynaBean!=null) {
											xibzbDynaBean.set("JGMES_ZLGL_JCXM_ID", xjxmDynaBean.getStr("JGMES_ZLGL_JCXM_ID"));
										}
									}
									xibzbDynaBeanListForInsert.add(xibzbDynaBean);
								}
							}
						}
						serviceTemplate.insert(xibzbDynaBeanListForInsert);
						ret.Data = ret.getValues(xibzbDynaBeanListForInsert);
						/*
						//插入巡检报表
						String gdhm = xjbDynaBean.getStr("GDLB_GDHM");//工单号码
						String ddhm = xjbDynaBean.getStr("GDLB_DDHM");//订单号码
						String lckh = xjbDynaBean.getStr("GDLB_LCKH");//流程卡号
						DynaBean xjbbDynaBean = new DynaBean(); //巡检报表 JGMES_PB_XJBB
						xjbbDynaBean.set("JGMES_PB_XJBB_ID", JEUUID.uuid());
						xjbbDynaBean.set("JGMES_PB_XJB_ID", xjbDynaBean.getStr("JGMES_PB_XJB_ID"));
						DynaBean gdDynaBean = new DynaBean();
						if(gdhm!=null&&!"".equals(gdhm)) {
							gdDynaBean  = serviceTemplate.selectOne("JGMES_PLAN_GDLB", " and GDLB_GDHM = '"+gdhm+"'");
							
						}else if(ddhm!=null&&!"".equals(ddhm)) {
							gdDynaBean  = serviceTemplate.selectOne("JGMES_PLAN_GDLB", " and GDLB_DDHM = '"+ddhm+"'");
							
						}else if(lckh!=null&&!"".equals(lckh)) {
							gdDynaBean = serviceTemplate.selectOne("JGMES_PLAN_GDLB", " and GDLB_LCKH = '"+lckh+"'");
						}
						
						if(gdDynaBean!=null) {
							xjbbDynaBean.set("XJBB_DDZS", gdDynaBean.getStr("GDLB_DDSL"));//订单总数
							xjbbDynaBean.set("XJBB_YWGSL", gdDynaBean.getStr("GDLB_WCSL"));//已经完工数量
							xjbbDynaBean.set("XJBB_YCJSL", xjbDynaBean.getStr("XJB_XJSL"));//已抽检数量
							xjbbDynaBean.set("XJBB_BHGSL", bhgsl);//不合格数量
							serviceTemplate.insert(xjbbDynaBean);
						}else {
							ret.setMessage("未获取到工单信息！");
						}
						*/
						
						
						
					}
				}
				//serviceTemplate.insert(dynaBean);
			}else {
				ret.setMessage("未获取到数据！");
			}
		}catch(Exception e) {
			logger.error(e.toString());
			ret.setMessage("系统发生异常，请联系管理员！");
		}
		toWrite(jsonBuilder.toJson(ret));
	}


	/**
	 * 巡检功能保存修改
	 *
	 */
	public void dopatrolSave() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyMM");
		String jsonStr = request.getParameter("jsonStr");
		String jsonStrDetail = request.getParameter("jsonStrDetail");
		String jsonStrRwGl = request.getParameter("jsonStrRwGl");
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
		JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
		try {
			logger.error("开始保存巡检信息，jsonStr："+jsonStr);
			logger.error("开始保存巡检信息，jsonStrDetail："+jsonStrDetail);
			DynaBean xjDynaBean = null;
			if(jsonStr!=null&&!"".equals(jsonStr)) {
				xjDynaBean = jgmesCommon.getDynaBeanByJsonStr("JGMES_ZLGL_XJZB", jsonStr);
				if(xjDynaBean!=null) {
					DynaBean xjDynaBean1 = serviceTemplate.selectOne("JGMES_ZLGL_XJZB", " and JGMES_ZLGL_XJZB_ID = '"+xjDynaBean.getStr("JGMES_ZLGL_XJZB_ID")+"'  and XJZB_DJH = '"+xjDynaBean.getStr("XJZB_DJH")+"'");
					if(xjDynaBean1!=null) {
						xjDynaBean.set("JGMES_ZLGL_XJZB_ID", xjDynaBean.getStr("JGMES_ZLGL_XJZB_ID"));
						xjDynaBean.set("XJZB_XJSJ", jgmesCommon.getCurrentTime());
						xjDynaBean = serviceTemplate.update(xjDynaBean);
					}else {
						xjDynaBean.set("JGMES_ZLGL_XJZB_ID", JEUUID.uuid());
						//获取订单号
						DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW"," and SCRW_RWDH = '"+xjDynaBean.getStr("XJZB_RWDH")+"'");
						if(scrwDynaBean!=null){
							xjDynaBean.set("XJZB_DDH",scrwDynaBean.getStr("SCRW_DDHM"));
						}
						xjDynaBean.set("XJZB_XJSJ", jgmesCommon.getCurrentTime());
						xjDynaBean.set("XJZB_DJH",serviceTemplate.buildCode("XJZB_DJH", "JGMES_ZLGL_XJZB", xjDynaBean));
						xjDynaBean = serviceTemplate.insert(xjDynaBean);
					}
				}
			}
			if(jsonStrDetail!=null&&!"".equals(jsonStrDetail)) {
				List<DynaBean> xjzbDynaBeanList = jgmesCommon.getListDynaBeanByJsonStr("JGMES_ZLGL_XJZBZB", jsonStrDetail);
				if(xjzbDynaBeanList!=null&&xjzbDynaBeanList.size()>0) {
					for(DynaBean xjzbDynaBean:xjzbDynaBeanList) {
						//获取主表
						if(xjzbDynaBean.getStr("XJZBZB_XJZBID")!=null&&!"".equals(xjzbDynaBean.getStr("XJZBZB_XJZBID"))){
							xjDynaBean = serviceTemplate.selectOne("JGMES_ZLGL_XJZB"," and JGMES_ZLGL_XJZB_ID = '"+xjzbDynaBean.getStr("XJZBZB_XJZBID")+"'");
						}
						if(xjDynaBean!=null){
							if(xjzbDynaBean.getStr("JGMES_ZLGL_XJZBZB_ID")!=null&&!"".equals(xjzbDynaBean.getStr("JGMES_ZLGL_XJZBZB_ID"))) {
								xjzbDynaBean.set("XJZBZB_XJZBID", xjDynaBean.getStr("JGMES_ZLGL_XJZB_ID"));
								serviceTemplate.update(xjzbDynaBean);
							}else {
								xjzbDynaBean.set("XJZBZB_XJZBID", xjDynaBean.getStr("JGMES_ZLGL_XJZB_ID"));
								xjzbDynaBean.set("JGMES_ZLGL_XJZBZB_ID", JEUUID.uuid());
								serviceTemplate.insert(xjzbDynaBean);
							}
						}else{
							logger.error("未获取到主表的信息！jsonStrDetail："+jsonStrDetail);
						}
					}
				}
			}
			if(jsonStrRwGl!=null&&!"".equals(jsonStrRwGl)&&xjDynaBean!=null){
				List<DynaBean> rwglDynaBeanList = jgmesCommon.getListDynaBeanByJsonStr("JGMES_ZLGL_RWDXXGLB", jsonStrRwGl);
				List<DynaBean> xibzbDynaBeanListForInsert = new ArrayList<>();
				for(DynaBean rwglDynaBean:rwglDynaBeanList){
					if(rwglDynaBean.getStr("JGMES_ZLGL_RWDXXGLB_ID")!=null&&!"".equals(rwglDynaBean.getStr("JGMES_ZLGL_RWDXXGLB_ID"))){
						rwglDynaBean.set("RWDXXGLB_JYDGLID",xjDynaBean.getStr("JGMES_ZLGL_XJZB_ID"));
						serviceTemplate.update(rwglDynaBean);
					}else{
						rwglDynaBean.set("JGMES_ZLGL_RWDXXGLB_ID",JEUUID.uuid());
						rwglDynaBean.set("RWDXXGLB_JYDGLID",xjDynaBean.getStr("JGMES_ZLGL_XJZB_ID"));
						serviceTemplate.insert(rwglDynaBean);
					}
				}
			}
		}catch(Exception e) {
			logger.error(jgmesCommon.getExceptionDetail(e));
			ret.setMessage("系统发生异常，请联系管理员！");
		}
		toWrite(jsonBuilder.toJson(ret));
	}

	public void updatePassNot(){
		JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
		String id = request.getParameter("idList");
		String value = request.getParameter("valueList");
		try {
			if (StringUtil.isNotEmpty(id)&&StringUtil.isNotEmpty(value)){
				id = id.replaceAll("\\[","").replaceAll("\\]","").replaceAll("\"","");
				value = value.replaceAll("\\[","").replaceAll("\\]","").replaceAll("\"","");
				String[] idList = id.split(",");
				String[] valueList = value.split(",");
				for (int k = 0;k<idList.length;k++){
					String pk = idList[k];
					String val = valueList[k];
					String valName = "是";
					if (val.equals("0"))
						valName = "否";
					String updateSql = "update JGMES_ZLGL_XJZBZB set XJZBZB_NO_CODE = '"+val+"',XJZBZB_NO_NAME='"+valName+"' where JGMES_ZLGL_XJZBZB_ID = '"+pk+"'";
					pcServiceTemplate.executeSql(updateSql);
				}
			}
		}catch ( Exception e){
			logger.error(jgmesCommon.getExceptionDetail(e));
			ret.setMessage("系统发生异常，请联系管理员！");
		}
		toWrite(jsonBuilder.toJson(ret));
	}
	
}