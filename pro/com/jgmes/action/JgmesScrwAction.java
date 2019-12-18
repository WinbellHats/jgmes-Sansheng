package com.jgmes.action;

import javax.annotation.Resource;

import org.apache.struts2.dispatcher.multipart.MultiPartRequestWrapper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.je.core.action.DynaAction;
import com.je.core.util.JEUUID;
import com.je.core.util.bean.DynaBean;
import com.jgmes.util.JgmesCommon;
import com.jgmes.util.JgmesEnumsDic;
import com.jgmes.util.JgmesResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/**
 * ����������صĽӿ�
 * @author liuc
 * @version 2019-03-28 15:52:04
 * @see /jgmes/jgmesScrwAction!load.action
 */
@Component("jgmesScrwAction")
@Scope("prototype")
public class JgmesScrwAction extends DynaAction  {
	
	private static final long serialVersionUID = 1L;
	
	public void load(){
		String form = request.getParameter("files");
	}
	/**
	 * 获取生产任务单 SN 码的生产情况
	 * 
	 * 
	 */
	public void getScrwScqk() {
		String mac = request.getParameter("mac");// MAC地址
		String userCode = request.getParameter("userCode");// 用户编码  必填
		String prodLineCode = request.getParameter("prodLineCode");// 产线编码
		String sCRWCode = request.getParameter("SCRWCode");// 生产任务单号
		String barCode = request.getParameter("barCode");// 生产任务单号
		String sCStatus = request.getParameter("SCStatus");// 生产状态
		String pageSize = request.getParameter("PageSize");// 每页码
		String currPage = request.getParameter("CurrPage");// 当前页
		
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
		JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
		String jsonStr = "";
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {
			try {
				//产线编码
				if ((prodLineCode == null || prodLineCode.isEmpty())) {
					ret.setMessage("产线编码不能都为空！");
				}
				//生产任务单号
				if (sCRWCode == null || sCRWCode.isEmpty()) {
					ret.setMessage("生产任务单号不能都为空！");
				}
				//每页码
				if (pageSize == null || pageSize.isEmpty()) {
					pageSize = "10";
				}
				//当前页
				if (currPage == null || currPage.isEmpty()) {
					currPage = "1";
				}
				
				if ((prodLineCode != null && !prodLineCode.isEmpty())&&
						(sCRWCode != null && !sCRWCode.isEmpty())){
					DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and SCRW_RWDH = '"+sCRWCode+"' and SCRW_CXBM = '"+prodLineCode+"'");
					if(scrwDynaBean!=null) {
						if(sCStatus!=null&&!"".equals(sCStatus)) {
							sCStatus = "('" + sCStatus.replaceAll(",", "','") + "')";
							sCStatus = "and (case WHEN b.SCJP_RWZT_CODE is null then 'RWZT01' else b.SCJP_RWZT_CODE end) in "+sCStatus+"\r\n";
						}else {
							sCStatus = "";
						}
						if(barCode!=null&&!"".equals(barCode)) {
							barCode = " and a.GDCPTM_TMH = '"+barCode+"'";
						}else {
							barCode = "";
						}
						int kss = Integer.parseInt(pageSize)*(Integer.parseInt(currPage)-1);
						int size = Integer.parseInt(pageSize);
						String sql = "select a.GDCPTM_TMH,b.SCJP_GXBH,b.SCJP_GXMC,b.SCJP_SCY,b.JGMES_PB_SCJP_ID,(case WHEN b.SCJP_RWZT_CODE is null then 'RWZT01' else b.SCJP_RWZT_CODE end) SCJP_RWZT_CODE from JGMES_BASE_GDCPTM a\r\n" + 
								"LEFT JOIN JGMES_PB_SCJP b on a.GDCPTM_TMH = b.SCJP_TM\r\n" + 
								"where a.JGMES_PLAN_SCRW_ID = '"+scrwDynaBean.getStr("JGMES_PLAN_SCRW_ID")+"'\r\n" + sCStatus +barCode+
								" and GDCPTM_TMLX_CODE = 'TMLX01' " +
								"ORDER BY a.GDCPTM_TMH LIMIT "+kss+","+size+"";
						List<DynaBean> scqkDynaBeanList = serviceTemplate.selectListBySql(sql);
						
						//获取总条数
						String getTotalCountSql = "select count(*) as totalCount from JGMES_BASE_GDCPTM a\r\n" + 
								"LEFT JOIN JGMES_PB_SCJP b on a.GDCPTM_TMH = b.SCJP_TM\r\n" + 
								"where a.JGMES_PLAN_SCRW_ID = '"+scrwDynaBean.getStr("JGMES_PLAN_SCRW_ID")+"'\r\n" + sCStatus +barCode+
								" and GDCPTM_TMLX_CODE = 'TMLX01' ";
						List<DynaBean> totalCount = serviceTemplate.selectListBySql(getTotalCountSql);
						insertScjbCZJL(mac,"获取生产任务单 SN 码的生产情况","CZLX04",jgmesCommon.jgmesUser.getCurrentUserName());
						ret.Data = ret.getValues(scqkDynaBeanList);
						if(totalCount!=null) {
							ret.TotalCount = (long) totalCount.get(0).getInt("totalCount");
						}
					}
				}
			} catch (Exception e) {
				logger.error(e.toString());
				e.printStackTrace();
				ret.setMessage("系统发生异常，请联系管理员！");
			}
		}else {
			ret.setMessage("用户验证合法性失败！");
		}
		jsonStr = jsonBuilder.toJson(ret);
		toWrite(jsonStr);
		
	}
	
	
	
	/**
	 * 获取产品条码报工状态表信息
	 * 
	 * 
	 */
	public void getCPTMBGZTB() {
		String mac = request.getParameter("mac");// MAC地址
		String userCode = request.getParameter("userCode");// 用户编码  必填
		String prodLineCode = request.getParameter("prodLineCode");// 产线编码
		String sCRWCode = request.getParameter("SCRWCode");// 生产任务单号
		String barCode = request.getParameter("barCode");// 生产任务单号
		String sCStatus = request.getParameter("SCStatus");// 生产状态
		String pageSize = request.getParameter("PageSize");// 每页码
		String currPage = request.getParameter("CurrPage");// 当前页
		
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
		JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
		String jsonStr = "";
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {
			try {
				//产线编码
				if ((prodLineCode == null || prodLineCode.isEmpty())) {
					ret.setMessage("产线编码不能都为空！");
				}
				//生产任务单号
				if (sCRWCode == null || sCRWCode.isEmpty()) {
					ret.setMessage("生产任务单号不能都为空！");
				}
				//每页码
				if (pageSize == null || pageSize.isEmpty()) {
					pageSize = "10";
				}
				//当前页
				if (currPage == null || currPage.isEmpty()) {
					currPage = "1";
				}
				
				if ((prodLineCode != null && !prodLineCode.isEmpty())&&
						(sCRWCode != null && !sCRWCode.isEmpty())){
					DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and SCRW_RWDH = '"+sCRWCode+"' and SCRW_CXBM = '"+prodLineCode+"'");
					if(scrwDynaBean!=null) {
						if(sCStatus!=null&&!"".equals(sCStatus)) {
							sCStatus = "('" + sCStatus.replaceAll(",", "','") + "')";
							sCStatus = "and (case WHEN b.CPTMBGZTB_RWZT_CODE is null then 'RWZT01' else b.CPTMBGZTB_RWZT_CODE end) in "+sCStatus+"\r\n";
						}else {
							sCStatus = "";
						}
						if(barCode!=null&&!"".equals(barCode)) {
							barCode = " and a.GDCPTM_TMH = '"+barCode+"'";
						}else {
							barCode = "";
						}
						int kss = Integer.parseInt(pageSize)*(Integer.parseInt(currPage)-1);
						int size = Integer.parseInt(pageSize);
						String sql = "select a.GDCPTM_TMH,b.CPTMBGZTB_DQGXBH,b.CPTMBGZTB_DQGXMC,b.CPTMBGZTB_SCY,(case WHEN b.CPTMBGZTB_RWZT_CODE is null then 'RWZT01' else b.CPTMBGZTB_RWZT_CODE end) SCJP_RWZT_CODE from JGMES_BASE_GDCPTM a\r\n" + 
								"LEFT JOIN JGMES_SCGCGL_CPTMBGZTB b on a.GDCPTM_TMH = b.CPTMBGZTB_TM\r\n" + 
								"left join je_core_dictionaryitem c on (case WHEN b.CPTMBGZTB_RWZT_CODE is null then 'RWZT01' else b.CPTMBGZTB_RWZT_CODE end) = c.dictionaryitem_itemcode\r\n" + 
								"LEFT JOIN je_core_dictionary d on d.DICTIONARY_ITEMROOT_ID = c.sy_parent" +
								" where a.JGMES_PLAN_SCRW_ID = '"+scrwDynaBean.getStr("JGMES_PLAN_SCRW_ID")+"'\r\n" + sCStatus +barCode+
								" and d.DICTIONARY_DDCODE = 'JGMES_DIC_RWZT' "+
								" and GDCPTM_TMLX_CODE = 'TMLX01' " +
								"ORDER BY c.SY_ORDERINDEX,a.GDCPTM_TMH LIMIT "+kss+","+size+"";
						List<DynaBean> scqkDynaBeanList = serviceTemplate.selectListBySql(sql);
						
						//获取总条数
						String getTotalCountSql = "select count(*) as totalCount from JGMES_BASE_GDCPTM a\r\n" + 
								"LEFT JOIN JGMES_SCGCGL_CPTMBGZTB b on a.GDCPTM_TMH = b.CPTMBGZTB_TM\r\n" + 
								"where a.JGMES_PLAN_SCRW_ID = '"+scrwDynaBean.getStr("JGMES_PLAN_SCRW_ID")+"'\r\n" + sCStatus +barCode+
								" and GDCPTM_TMLX_CODE = 'TMLX01' ";
						List<DynaBean> totalCount = serviceTemplate.selectListBySql(getTotalCountSql);
						insertScjbCZJL(mac,"获取产品条码报工状态表信息","CZLX04",jgmesCommon.jgmesUser.getCurrentUserName());
						ret.Data = ret.getValues(scqkDynaBeanList);
						if(totalCount!=null) {
							ret.TotalCount = (long) totalCount.get(0).getInt("totalCount");
						}
					}
				}
			} catch (Exception e) {
				logger.error(e.toString());
				e.printStackTrace();
				ret.setMessage("系统发生异常，请联系管理员！");
			}
		}else {
			ret.setMessage("用户验证合法性失败！");
		}
		jsonStr = jsonBuilder.toJson(ret);
		toWrite(jsonStr);
		
	}
	
	
	/**
	 * 获取生产节拍列表
	 * 
	 * 
	 */
	public void getScjpforQrr() {
		String mac = request.getParameter("mac");// MAC地址   
		String userCode = request.getParameter("userCode");// 用户编码  必填
		String prodLineCode = request.getParameter("prodLineCode");// 产线编码
		String sCRWCode = request.getParameter("SCRWCode");// 生产任务单号
		String barCode = request.getParameter("barCode");// 生产任务单号
		String sCStatus = request.getParameter("SCStatus");// 生产状态
		String pageSize = request.getParameter("PageSize");// 每页码
		String currPage = request.getParameter("CurrPage");// 当前页
		
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
		JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
		String jsonStr = "";
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {
			try {
				//产线编码
				if ((prodLineCode == null || prodLineCode.isEmpty())) {
					ret.setMessage("产线编码不能都为空！");
				}
				//生产任务单号
				if (sCRWCode == null || sCRWCode.isEmpty()) {
					ret.setMessage("生产任务单号不能都为空！");
				}
				//每页码
				if (pageSize == null || pageSize.isEmpty()) {
					pageSize = "10";
				}
				//当前页
				if (currPage == null || currPage.isEmpty()) {
					currPage = "1";
				}
				
				if ((prodLineCode != null && !prodLineCode.isEmpty())&&
						(sCRWCode != null && !sCRWCode.isEmpty())){
					if(sCStatus!=null&&!"".equals(sCStatus)) {
						sCStatus = "('" + sCStatus.replaceAll(",", "','") + "')";
						sCStatus = " and SCJP_RWZT_CODE in" + sCStatus;
					}else {
						sCStatus = "";
					}
					if(barCode!=null&&!"".equals(barCode)) {
						barCode = " and SCJP_TM = '"+barCode+"'";
					}else {
						barCode = "";
					}
					
					int kss = Integer.parseInt(pageSize)*(Integer.parseInt(currPage)-1);
					int size = Integer.parseInt(pageSize);
					
					String sql = "select * from JGMES_PB_SCJP a\r\n" + 
							"left join je_core_dictionaryitem b on a.SCJP_RWZT_CODE = b.dictionaryitem_itemcode\r\n" + 
							"LEFT JOIN je_core_dictionary c on c.DICTIONARY_ITEMROOT_ID = b.sy_parent\r\n" + 
							"where SCJP_RWDH = '"+sCRWCode+"' \r\n" + 
							"and SCJP_CXBH = '"+prodLineCode+"' \r\n" + 
							"and (SCJP_RWZT_CODE ='RWZT05' or SCJP_RWZT_CODE ='RWZT03')"+sCStatus+barCode+
							" order by b.SY_ORDERINDEX,a.SCJP_TM limit "+kss+","+size+"";
					
					List<DynaBean> scqkDynaBeanList = serviceTemplate.selectListBySql(sql);
					insertScjbCZJL(mac,"获取生产节拍列表","CZLX04",jgmesCommon.jgmesUser.getCurrentUserName());
					if(scqkDynaBeanList!=null&&scqkDynaBeanList.size()>0) {
						ret.Data = ret.getValues(scqkDynaBeanList);
						
						List<DynaBean> totalCount = serviceTemplate.selectList("JGMES_PB_SCJP", " and SCJP_RWDH = '"+sCRWCode+"' and SCJP_CXBH = '"+prodLineCode+"' and (SCJP_RWZT_CODE ='RWZT05' or SCJP_RWZT_CODE ='RWZT03') "+sCStatus+barCode);
						if(totalCount!=null&&totalCount.size()>0) {
							ret.TotalCount = (long) totalCount.size();
						}else {
							ret.TotalCount = (long) scqkDynaBeanList.size();
						}
					}
				}
			} catch (Exception e) {
				logger.error(e.toString());
				e.printStackTrace();
				ret.setMessage("系统发生异常，请联系管理员！");
			}
		}
		jsonStr = jsonBuilder.toJson(ret);
		toWrite(jsonStr);
		
	}
	
	/**
	 * 获取当前生产节拍明细表
	 * 
	 * 
	 */
	public void getCrrScrwScqk() {
		String mac = request.getParameter("mac");// MAC地址   
		String userCode = request.getParameter("userCode");// 用户编码  必填
		String prodLineCode = request.getParameter("prodLineCode");// 产线编码
		String scjpId = request.getParameter("JGMES_PB_SCJP_ID");// 节拍主键 ID
		
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
		JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
		String jsonStr = "";
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {
			try {
				//产线编码
				if ((prodLineCode == null || prodLineCode.isEmpty())) {
					ret.setMessage("产线编码不能都为空！");
				}
				//节拍主键 ID
				if (scjpId == null || scjpId.isEmpty()) {
					ret.setMessage("节拍主键 ID不能都为空！");
				}
				
				if ((prodLineCode != null && !prodLineCode.isEmpty())&&
						(scjpId != null && !scjpId.isEmpty())){
					DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PB_SCJP", " and JGMES_PB_SCJP_ID = '"+scjpId+"'");
					insertScjbCZJL(mac,"获取当前生产节拍明细表","CZLX04",jgmesCommon.jgmesUser.getCurrentUserName());
					if(scrwDynaBean!=null) {
						ret.Data = scrwDynaBean.getValues();
						ret.TotalCount = (long) 1;
					}
				}
			} catch (Exception e) {
				logger.error(e.toString());
				e.printStackTrace();
				ret.setMessage("系统发生异常，请联系管理员！");
			}
		}
		jsonStr = jsonBuilder.toJson(ret);
		toWrite(jsonStr);
		
	}
	
	/**
	 * 产品 SN 码开始生产
	 * 
	 * 
	 */
	public void doSaveScjbStart() {
		String mac = request.getParameter("mac");// MAC地址   
		String userCode = request.getParameter("userCode");// 用户编码  必填
		String prodLineCode = request.getParameter("prodLineCode");// 产线编码
		String barCode = request.getParameter("BarCode");// 产品 SN 码 
		String scrwId = request.getParameter("SCRWID");// 任务单 ID 
		String scrwNo = request.getParameter("SCRWNO");// 任务单号 
		String gxbh = request.getParameter("gxbh");// 工序编码
		String gxmc = request.getParameter("gxmc");// 工序名称 
		String cpbm = request.getParameter("cpbm");// 产品编码  
		String scy = request.getParameter("scy");// 生产员
		
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
		JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
		String jsonStr = "";
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {
			try {
				//产线编码
				if ((prodLineCode == null || prodLineCode.isEmpty())) {
					ret.setMessage("产线编码不能都为空！");
				}
				//产品 SN 码 
				if (barCode == null || barCode.isEmpty()) {
					ret.setMessage("产品 SN 码不能都为空！");
				}
				//任务单 ID
				if (scrwId == null || scrwId.isEmpty()) {
					ret.setMessage("任务单 ID不能都为空！");
				}
				//任务单号
				if (scrwNo == null || scrwNo.isEmpty()) {
					ret.setMessage("任务单号不能都为空！");
				}
				//工序编码
				if (gxbh == null || gxbh.isEmpty()) {
					ret.setMessage("工序编码不能都为空！");
				}
				//工序名称
				if (gxmc == null || gxmc.isEmpty()) {
					ret.setMessage("工序名称不能都为空！");
				}
				//产品编码
				if (cpbm == null || cpbm.isEmpty()) {
					ret.setMessage("产品编码不能都为空！");
				}
				//生产员
				if (scy == null || scy.isEmpty()) {
					ret.setMessage("生产员不能都为空！");
				}
				
				if ((prodLineCode != null && !prodLineCode.isEmpty())&&
					(barCode != null && !barCode.isEmpty())&&
					(scrwId != null && !scrwId.isEmpty())&&
					(scrwNo != null && !scrwNo.isEmpty())&&
					(gxbh != null && !gxbh.isEmpty())&&
					(gxmc != null && !gxmc.isEmpty())&&
					(cpbm != null && !cpbm.isEmpty())&&
					(scy != null && !scy.isEmpty())){


					//判断扫描的条码是否是该任务单中的条码
					DynaBean tmScDynaBean = serviceTemplate.selectOne("JGMES_BASE_GDCPTM"," and GDCPTM_TMH='"+barCode+"' and JGMES_PLAN_SCRW_ID = '"+scrwId+"'");
					if(tmScDynaBean == null){
						ret.setMessage("请确认扫描的条码是否是本任务单的条码！");
						toWrite(jsonBuilder.toJson(ret));
						return;
					}

					/*//检查有没有进行首件检验
					String message=jgmesCommon.checkSJ(prodLineCode,scrwNo);
					if(message!=null&&!"".equals(message)){
						ret.setMessage(message);
						toWrite(jsonBuilder.toJson(ret));
						return;
					}*/


					DynaBean scjbDynaBeans = serviceTemplate.selectOne("JGMES_PB_SCJP", " and SCJP_TM = '"+barCode+"' and SCJP_SCRWID = '"+scrwId+"' and SCJP_GXBH = '"+gxbh+"'");
					List<DynaBean> scjpList = serviceTemplate.selectList("JGMES_PB_SCJP", " and SCJP_TM = '"+barCode+"' and SCJP_SCRWID = '"+scrwId+"' and SCJP_GXBH != '"+gxbh+"' and SCJP_RWZT_CODE !='RWZT01' and SCJP_RWZT_CODE !='RWZT03'");
					insertScjbCZJL(mac,"产品 SN 码开始生产查询","CZLX04",jgmesCommon.jgmesUser.getCurrentUserName());
					if(scjbDynaBeans == null&&(scjpList == null || scjpList.size() == 0)) {
						DynaBean scjbDynaBean = new DynaBean();
						scjbDynaBean.set("SCJP_CXBH", prodLineCode);
						scjbDynaBean.set("SCJP_TM", barCode);
						scjbDynaBean.set("SCJP_SCRWID", scrwId);
						scjbDynaBean.set("SCJP_RWDH", scrwNo);
						scjbDynaBean.set("SCJP_GXBH", gxbh);
						scjbDynaBean.set("SCJP_GXMC", gxmc);
						scjbDynaBean.set("SCJP_CPBH", cpbm);
						scjbDynaBean.set("SCJP_SCY", scy);
						scjbDynaBean.set("SCJP_RWZT_CODE", "RWZT02");
						scjbDynaBean = setScjbInfo(scjbDynaBean);
						
						//先插入主表
						DynaBean cptmbgDynaBean = serviceTemplate.selectOne("JGMES_SCGCGL_CPTMBGZTB", " and CPTMBGZTB_TM = '"+barCode+"' and CPTMBGZTB_SCRWID = '"+scrwId+"'");
						if(cptmbgDynaBean!=null) {
							DynaBean gxDynaBean = serviceTemplate.selectOne("JGMES_GYGL_GXGL", " and GXGL_GXNUM = '"+gxbh+"'");
							if(gxDynaBean!=null) {
								cptmbgDynaBean.set("CPTMBGZTB_DQGXID", gxDynaBean.getStr("GXGL_ID"));//工序ID
								cptmbgDynaBean.set("CPTMBGZTB_GXLX", gxDynaBean.getStr("GXGL_GXLX_NAME"));//工序类型
							}
							cptmbgDynaBean.set("CPTMBGZTB_DQGXBH", gxbh);//工序编号
							cptmbgDynaBean.set("CPTMBGZTB_DQGXMC", gxmc);//工序名称
							cptmbgDynaBean.set("CPTMBGZTB_SCY", scy);
							serviceTemplate.update(cptmbgDynaBean);//修改产品条码报工状态表
						}else {
							cptmbgDynaBean = setCptmbgztb(scjbDynaBean);
							serviceTemplate.insert(cptmbgDynaBean);//插入产品条码报工状态表
						}
						
						
						
						//再插入字表
						serviceTemplate.insert(scjbDynaBean);
						insertScjbCZJL(mac,"产品 SN 码开始生产插入","CZLX01",jgmesCommon.jgmesUser.getCurrentUserName());
						
						//回写生产任务单中的相关数据
						jgmesCommon.doSaveScrwZt(userCode,scrwId,JgmesEnumsDic.ScDoing.getKey(),0);
						
					}else {
						ret.setMessage("已经有生产中的任务!");
					}
				}
			} catch (Exception e) {
				logger.error(e.toString());
				e.printStackTrace();
				ret.setMessage("系统发生异常，请联系管理员！");
			}
		}
		jsonStr = jsonBuilder.toJson(ret);
		toWrite(jsonStr);
		
	}
	
	
	/**
	 * 产品 SN 码完工生产
	 * 
	 * 
	 */
	public void doSaveScjbEnd() {
		String mac = request.getParameter("mac");// MAC地址   
		String userCode = request.getParameter("userCode");// 用户编码  必填
		String prodLineCode = request.getParameter("prodLineCode");// 产线编码
		String gxbm = request.getParameter("gxbm");// 工序编码
		String barCode = request.getParameter("BarCode");// 产品 SN 码 
		String scrwId = request.getParameter("SCRWID");// 任务单 ID
		String scy = request.getParameter("scy");// 生产员
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
		JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
		String jsonStr = "";
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {
			try {
				//产线编码
				if ((prodLineCode == null || prodLineCode.isEmpty())) {
					ret.setMessage("产线编码不能都为空！");
				}
				//产品 SN 码 
				if (barCode == null || barCode.isEmpty()) {
					ret.setMessage("产品 SN 码不能都为空！");
				}
				//任务单 ID
				if (scrwId == null || scrwId.isEmpty()) {
					ret.setMessage("任务单 ID不能都为空！");
				}
				//生产员
				if (scy == null || scy.isEmpty()) {
					ret.setMessage("生产员不能都为空！");
				}
				//工序编码
				if (gxbm == null || gxbm.isEmpty()) {
					ret.setMessage("工序编码不能都为空！");
				}
				if ((prodLineCode != null && !prodLineCode.isEmpty())&&
					(barCode != null && !barCode.isEmpty())&&
					(scrwId != null && !scrwId.isEmpty())&&
					(gxbm != null && !gxbm.isEmpty())&&
					(scy != null && !scy.isEmpty())){


					//判断扫描的条码是否是该任务单中的条码
					DynaBean tmScDynaBean = serviceTemplate.selectOne("JGMES_BASE_GDCPTM"," and GDCPTM_TMH='"+barCode+"' and JGMES_PLAN_SCRW_ID = '"+scrwId+"'");
					if(tmScDynaBean == null){
						ret.setMessage("请确认扫描的条码是否是本任务单的条码！");
						toWrite(jsonBuilder.toJson(ret));
					}


					DynaBean scjbDynaBean = serviceTemplate.selectOne("JGMES_PB_SCJP", " and SCJP_TM = '"+barCode+"' and SCJP_SCRWID = '"+scrwId+"' and SCJP_GXBH = '"+gxbm+"'");
					insertScjbCZJL(mac,"产品 SN码完工生产的时候查询","CZLX04",jgmesCommon.jgmesUser.getCurrentUserName());
					if(scjbDynaBean!=null) {
						if(!scy.equals(scjbDynaBean.getStr("SCJP_SCY"))) { //开工生产员与完工生产员保持一致；
							ret.setMessage("开工生产员与完工生产员请保持一致！");
						}else if(!"RWZT02".equals(scjbDynaBean.getStr("SCJP_RWZT_CODE"))) {
							ret.setMessage("当前产品 SN码的生产状态必须处于生产中！");
						}else if("GXLX016".equals(scjbDynaBean.getStr("SCJP_GXLX"))&&
								(scjbDynaBean.getStr("SCJP_QRR")==null||"".equals(scjbDynaBean.getStr("SCJP_QRR")))){//如果是关键工序，需要主管确认之后在完成
							//回写生产任务状态为待确认状态
							DynaBean dic1 = jgmesCommon.getDic("JGMES_DIC_RWZT","RWZT05");
							if(dic1!=null) {
								scjbDynaBean.set("SCJP_RWZT_NAME", dic1.get("DICTIONARYITEM_ITEMNAME"));//生产状态_NAME
								scjbDynaBean.set("SCJP_RWZT_CODE", dic1.get("DICTIONARYITEM_ITEMCODE"));//生产状态
							}
							scjbDynaBean.set("SCJP_JSSJ", jgmesCommon.getCurrentTime());//完成时间
							serviceTemplate.update(scjbDynaBean);
							insertScjbCZJL(mac,"产品 SN 码完工生产的时候修改","CZLX02",jgmesCommon.jgmesUser.getCurrentUserName());
						}else{
							//回写生产任务状态为完工状态
							DynaBean dic1 = jgmesCommon.getDic("JGMES_DIC_RWZT","RWZT03");
							if(dic1!=null) {
								scjbDynaBean.set("SCJP_RWZT_NAME", dic1.get("DICTIONARYITEM_ITEMNAME"));//生产状态_NAME
								scjbDynaBean.set("SCJP_RWZT_CODE", dic1.get("DICTIONARYITEM_ITEMCODE"));//生产状态
							}
							scjbDynaBean.set("SCJP_JSSJ", jgmesCommon.getCurrentTime());//完成时间
							serviceTemplate.update(scjbDynaBean);
							insertScjbCZJL(mac,"产品 SN码完工生产的时候修改","CZLX02",jgmesCommon.jgmesUser.getCurrentUserName());
							//产生报工数据
							DynaBean bgDynaBean = setBgsl(scjbDynaBean);
							serviceTemplate.insert(bgDynaBean);
							
							//回写产品条码报工状态表中的状态
							if(isComplete(scjbDynaBean.getStr("SCJP_CPBH"),barCode)) {
								DynaBean cptmbgztbDynaBean = serviceTemplate.selectOne("JGMES_SCGCGL_CPTMBGZTB", " and CPTMBGZTB_TM = '"+barCode+"' and CPTMBGZTB_SCRWID = '"+scrwId+"'");
								//回写状态为完工状态
								if(dic1!=null) {
									cptmbgztbDynaBean.set("CPTMBGZTB_RWZT_NAME", dic1.get("DICTIONARYITEM_ITEMNAME"));//生产状态_NAME
									cptmbgztbDynaBean.set("CPTMBGZTB_RWZT_CODE", dic1.get("DICTIONARYITEM_ITEMCODE"));//生产状态
								}
								cptmbgztbDynaBean.set("CPTMBGZTB_JSSJ", jgmesCommon.getCurrentTime());
								serviceTemplate.update(cptmbgztbDynaBean);
								
								
								//回写各数据表中的状态
								jgmesCommon.doSaveScrwZt(userCode,scrwId,JgmesEnumsDic.ScFinished.getKey(),1);
							}
							
//							//回写各数据表中的状态
//							if(isLastGx(scjbDynaBean.getStr("SCJP_CPBH"),scjbDynaBean.getStr("SCJP_GXBH"))) {
//								jgmesCommon.doSaveScrwZt(userCode,scrwId,JgmesEnumsDic.ScFinished.getKey(),1);
//							}
						}
						
					}
				}
			} catch (Exception e) {
				logger.error(e.toString());
				e.printStackTrace();
				ret.setMessage("系统发生异常，请联系管理员！");
			}
		}
		jsonStr = jsonBuilder.toJson(ret);
		toWrite(jsonStr);
		
	}
	
	
	/**
	 * 文件上传接口
	 * 
	 * 
	 */
	public void doFileUplod() {
		
		MultiPartRequestWrapper req = (MultiPartRequestWrapper) request;
		String userCode = req.getParameter("userCode");// 用户编码  必填
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate,userCode);
		String mac = req.getParameter("mac");// MAC地址   
		JgmesResult<String> ret = new JgmesResult<String>();
		String jsonStr = "";
		// 校检合法性，是否多地登陆
		//if (doCheck(userCode, mac).IsSuccess) {
			//把附件上传到服务器，并返回地址
			String realPath = "/JE/data/upload/"+jgmesCommon.getCurrentMonth()+"/";
			String filePath = jgmesCommon.fileUplod(realPath);
			ret.Data = filePath;
		//}
		jsonStr = jsonBuilder.toJson(ret);
		toWrite(jsonStr);
		
	}
	
	

	/**
	 * 确认产品 SN 码
	 * 
	 * 
	 */
	public void doConfirmSN() {
		String mac = request.getParameter("mac");// MAC地址   
		String userCode = request.getParameter("userCode");// 用户编码  必填
		String scjpId = request.getParameter("JGMES_PB_SCJP_ID");// 节拍表主键ID
		String scrwId = request.getParameter("SCRWID");// 任务单 ID
		//String sCStatus = request.getParameter("SCStatus");// 生产状态 
		String qrr = request.getParameter("qrr");// 确认人
		String remark = request.getParameter("Remark");// 确认说明
		String file = request.getParameter("file");// 文件地址
		
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
		JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
		String jsonStr = "";
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {
			try {
				//节拍表主键ID
				if ((scjpId == null || scjpId.isEmpty())) {
					ret.setMessage("节拍表主键ID不能都为空！");
				}
				//任务单ID
				if ((scrwId == null || scrwId.isEmpty())) {
					ret.setMessage("任务单ID不能都为空！");
				}
//				//生产状态 
//				if (sCStatus == null || sCStatus.isEmpty()) {
//					ret.setMessage("生产状态不能都为空！");
//				}
				//确认人
				if (qrr == null || qrr.isEmpty()) {
					ret.setMessage("确认人不能都为空！");
				}
				if ((scjpId != null && !scjpId.isEmpty())&&
					(scrwId != null && !scrwId.isEmpty())&&
					(qrr != null && !qrr.isEmpty())){
					DynaBean scjpDynaBean = serviceTemplate.selectOne("JGMES_PB_SCJP", " and JGMES_PB_SCJP_ID = '"+scjpId+"'");
					//回写生产任务状态为完工状态
					DynaBean dic1 = jgmesCommon.getDic("JGMES_DIC_RWZT","RWZT03");
					if(dic1!=null) {
						scjpDynaBean.set("SCJP_RWZT_NAME", dic1.get("DICTIONARYITEM_ITEMNAME"));//生产状态_NAME
						scjpDynaBean.set("SCJP_RWZT_CODE", dic1.get("DICTIONARYITEM_ITEMCODE"));//生产状态
					}
					//scjpDynaBean.set("SCJP_JSSJ", jgmesCommon.getCurrentTime()); //结束时间
					scjpDynaBean.set("SCJP_QRSJ", jgmesCommon.getCurrentTime());//确认时间
					scjpDynaBean.set("SCJP_QRR", qrr); //确认人
					scjpDynaBean.set("SCJP_QRSM", remark);//确认说明
					scjpDynaBean.set("SCJP_FJ", file);//附件文件地址
					serviceTemplate.update(scjpDynaBean);
					insertScjbCZJL(mac,"确认产品 SN码的时候修改","CZLX02",jgmesCommon.jgmesUser.getCurrentUserName());
					
					//产生报工数据
					DynaBean bgDynaBean = setBgsl(scjpDynaBean);
					serviceTemplate.insert(bgDynaBean);
					
					//回写产品条码报工状态表中的状态
					if(isComplete(scjpDynaBean.getStr("SCJP_CPBH"),scjpDynaBean.getStr("SCJP_TM"))) {
						DynaBean cptmbgztbDynaBean = serviceTemplate.selectOne("JGMES_SCGCGL_CPTMBGZTB", " and CPTMBGZTB_TM = '"+scjpDynaBean.getStr("SCJP_TM")+"' and CPTMBGZTB_SCRWID = '"+scrwId+"'");
						//回写状态为完工状态
						if(dic1!=null) {
							cptmbgztbDynaBean.set("CPTMBGZTB_RWZT_NAME", dic1.get("DICTIONARYITEM_ITEMNAME"));//生产状态_NAME
							cptmbgztbDynaBean.set("CPTMBGZTB_RWZT_CODE", dic1.get("DICTIONARYITEM_ITEMCODE"));//生产状态
						}
						cptmbgztbDynaBean.set("CPTMBGZTB_JSSJ", jgmesCommon.getCurrentTime());
						serviceTemplate.update(cptmbgztbDynaBean);
						
						//回写各任务表中的状态
						jgmesCommon.doSaveScrwZt(userCode,scrwId,JgmesEnumsDic.ScFinished.getKey(),1);
					}
					
//					//回写各任务表中的状态
//					if(isLastGx(scjpDynaBean.getStr("SCJP_CPBH"),scjpDynaBean.getStr("SCJP_GXBH"))) {
//						jgmesCommon.doSaveScrwZt(userCode,scrwId,JgmesEnumsDic.ScFinished.getKey(),1);
//					}
				}
			} catch (Exception e) {
				logger.error(e.toString());
				e.printStackTrace();
				ret.setMessage("系统发生异常，请联系管理员！");
			}
		}
		jsonStr = jsonBuilder.toJson(ret);
		toWrite(jsonStr);
		
	}
	
	
	/**
	 * 根据工序来获取节拍表信息
	 * 
	 * 
	 */
	public void getScjpByGX() {
		String mac = request.getParameter("mac");// MAC地址   
		String userCode = request.getParameter("userCode");// 用户编码  必填
		String scrwId = request.getParameter("SCRWID");// 任务单 ID
		String gxbm = request.getParameter("gxbm");// 工序编码
		String barCode = request.getParameter("barCode");// 条码号
		
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
		JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
		String jsonStr = "";
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {
			try {
				//任务单ID
				if ((scrwId == null || scrwId.isEmpty())) {
					ret.setMessage("任务单ID不能都为空！");
				}
				//生产状态 
				if (gxbm == null || gxbm.isEmpty()) {
					ret.setMessage("生产状态不能都为空！");
				}
				//条码号
				if (barCode == null || barCode.isEmpty()) {
					ret.setMessage("条码号不能都为空！");
				}
				if ((scrwId != null && !scrwId.isEmpty())&&
					(barCode != null && !barCode.isEmpty())&&
					(gxbm != null && !gxbm.isEmpty())){
					DynaBean scjpDynaBean = serviceTemplate.selectOne("JGMES_PB_SCJP", " and SCJP_SCRWID = '"+scrwId+"' and SCJP_GXBH = '"+gxbm+"' and SCJP_TM = '"+barCode+"'");
					
					insertScjbCZJL(mac,"根据工序来获取节拍表信息","CZLX04",jgmesCommon.jgmesUser.getCurrentUserName());
					if(scjpDynaBean!=null) {
						ret.Data = scjpDynaBean.getValues();
					}
					
				}
			} catch (Exception e) {
				logger.error(e.toString());
				e.printStackTrace();
				ret.setMessage("系统发生异常，请联系管理员！");
			}
		}
		jsonStr = jsonBuilder.toJson(ret);
		toWrite(jsonStr);
		
	}
	
	public boolean isLastGx(String cpbm,String gxbm) {
		boolean isLastGx = false;
		DynaBean cpDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " and PRODUCTDATA_BH = '"+cpbm+"'");
		if(cpDynaBean!=null) {
			List<DynaBean> gylxgxDynaBean = serviceTemplate.selectList("JGMES_GYGL_GYLXGX", " and GYLX_ID = '"+cpDynaBean.getStr("PRODUCTDATA_CPGYLXID")+"' order by SY_ORDERINDEX desc ");
			if(gylxgxDynaBean!=null&&gylxgxDynaBean.size()>0) {
				if(gxbm!=null&&!"".equals(gxbm)&&gxbm.equals(gylxgxDynaBean.get(0).getStr("GYLXGX_GXNUM"))) {
					isLastGx = true;
				}
			}
		}
		return isLastGx;
	}
	
	public boolean isComplete(String cpbm,String barCode) {
		boolean isComplete = true;
		DynaBean cpDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " and PRODUCTDATA_BH = '"+cpbm+"'");
		if(cpDynaBean!=null) {
			List<DynaBean> gylxgxDynaBean = serviceTemplate.selectList("JGMES_GYGL_GYLXGX", " and GYLX_ID = '"+cpDynaBean.getStr("PRODUCTDATA_CPGYLXID")+"' order by SY_ORDERINDEX desc ");
			if(gylxgxDynaBean!=null&&gylxgxDynaBean.size()>0) {
				for(DynaBean gxDynaBean:gylxgxDynaBean) {
					DynaBean scjpDynaBean = serviceTemplate.selectOne("JGMES_PB_SCJP", " and SCJP_TM = '"+barCode+"' and SCJP_GXBH = '"+gxDynaBean.getStr("GYLXGX_GXNUM")+"'");
					if(scjpDynaBean!=null&&"RWZT03".equals(scjpDynaBean.getStr("SCJP_RWZT_CODE"))) {
					}else {
						isComplete = false;
					}
				}
			}
		}
		return isComplete;
	}
	
	//给生产节拍操作记录表赋值
	public void insertScjbCZJL(String mac,String excuContent,String czlx,String czr) {
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
		DynaBean scjbjlDynaBean = new DynaBean();
		scjbjlDynaBean.setStr(beanUtils.KEY_TABLE_CODE, "JGMES_SCGCGL_SCJPCZJL");
		scjbjlDynaBean.set("JGMES_PB_SCJP_ID", JEUUID.uuid());
		scjbjlDynaBean.set("SCJPCZJL_MACDZ", mac);//MAC地址
		scjbjlDynaBean.set("SCJPCZJL_CZNR", excuContent);//操作内容
		DynaBean dic1 = jgmesCommon.getDic("JGMES_DIC_CZLX",czlx);
		if(dic1!=null) {
			scjbjlDynaBean.set("SCJPCZJL_CZLX_NAME", dic1.get("DICTIONARYITEM_ITEMNAME"));//操作类型名称
			scjbjlDynaBean.set("SCJPCZJL_CZLX_CODE", dic1.get("DICTIONARYITEM_ITEMCODE"));//操作类型编码
		}
		scjbjlDynaBean.set("SCJPCZJL_CZR", czr);//操作人
		scjbjlDynaBean.set("SCJPCZJL_CZRQ", jgmesCommon.getCurrentDate());//操作日期
		scjbjlDynaBean.set("SCJPCZJL_CZSJ", jgmesCommon.getCurrentTime());//操作时间
		serviceTemplate.insert(scjbjlDynaBean);
	}
	
	public DynaBean setScjbInfo(DynaBean dynaBean) {
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
		DynaBean scjbDynaBean = new DynaBean();
		scjbDynaBean.setStr(beanUtils.KEY_TABLE_CODE, "JGMES_PB_SCJP");
		scjbDynaBean.set("JGMES_PB_SCJP_ID", JEUUID.uuid());
		scjbDynaBean.set("SCJP_TM", dynaBean.getStr("SCJP_TM"));
		DynaBean cpDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " and PRODUCTDATA_BH = '"+dynaBean.getStr("SCJP_CPBH")+"'");
		scjbDynaBean.set("SCJP_CPBH", dynaBean.getStr("SCJP_CPBH"));
		if(cpDynaBean!=null) {
			scjbDynaBean.set("SCJP_CPMC", cpDynaBean.getStr("PRODUCTDATA_NAME"));
		}
		DynaBean cxDynaBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ", " and CXSJ_CXBM = '"+dynaBean.getStr("SCJP_CXBH")+"'");
		scjbDynaBean.set("SCJP_CXBH", dynaBean.getStr("SCJP_CXBH"));//产线编号
		if(cxDynaBean!=null) {
			scjbDynaBean.set("SCJP_CXID", cxDynaBean.getStr("JGMES_BASE_CXSJ_ID"));//产线ID
			scjbDynaBean.set("SCJP_CXMC", cxDynaBean.getStr("CXSJ_CXMC"));//产线名称
		}
		
		DynaBean gxDynaBean = serviceTemplate.selectOne("JGMES_GYGL_GXGL", " and GXGL_GXNUM = '"+dynaBean.getStr("SCJP_GXBH")+"'");
		if(gxDynaBean!=null) {
			scjbDynaBean.set("SCJP_GXID", gxDynaBean.getStr("GXGL_ID"));//工序ID
			scjbDynaBean.set("SCJP_GXLX", gxDynaBean.getStr("GXGL_GXLX_NAME"));//工序类型
		}
		scjbDynaBean.set("SCJP_GXBH", dynaBean.getStr("SCJP_GXBH"));//工序编号
		scjbDynaBean.set("SCJP_GXMC", dynaBean.getStr("SCJP_GXMC"));//工序名称
//		scjbDynaBean.set("SCJP_GWMC", value);//工位名称
//		scjbDynaBean.set("SCJP_GWBH", value);//工位编号
//		scjbDynaBean.set("SCJP_GWID", value);//工位ID
		if("RWZT02".equals(dynaBean.getStr("SCJP_RWZT_CODE"))) {
			scjbDynaBean.set("SCJP_KSSJ", jgmesCommon.getCurrentTime());//开始时间
		}
		if("RWZT03".equals(dynaBean.getStr("SCJP_RWZT_CODE"))) {
			scjbDynaBean.set("SCJP_JSSJ", jgmesCommon.getCurrentTime());//结束时间
		}
		
		//scjbDynaBean.set("SCJP_SC", value);//时长
		DynaBean dic1 = jgmesCommon.getDic("JGMES_DIC_RWZT","RWZT02");
		if(dic1!=null) {
			scjbDynaBean.set("SCJP_RWZT_NAME", dic1.get("DICTIONARYITEM_ITEMNAME"));//生产状态_NAME
			scjbDynaBean.set("SCJP_RWZT_CODE", dic1.get("DICTIONARYITEM_ITEMCODE"));//生产状态
		}
		scjbDynaBean.set("SCJP_SCRWID", dynaBean.getStr("SCJP_SCRWID"));//生产任务ID
		scjbDynaBean.set("SCJP_RWDH", dynaBean.getStr("SCJP_RWDH"));//任务单号
		scjbDynaBean.set("SCJP_SCY", dynaBean.getStr("SCJP_SCY"));//生产员
//		scjbDynaBean.set("SCJP_QRR", value);//确认人
//		scjbDynaBean.set("SCJP_QRSM", value);//确认说明
//		scjbDynaBean.set("SCJP_FJ", value);//附件
		
		return scjbDynaBean;
		
	}
	
	
	public DynaBean setCptmbgztb(DynaBean dynaBean) {
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
		DynaBean scjbDynaBean = new DynaBean();
		scjbDynaBean.setStr(beanUtils.KEY_TABLE_CODE, "JGMES_SCGCGL_CPTMBGZTB");
		scjbDynaBean.set("JGMES_SCGCGL_CPTMBGZTB_ID", JEUUID.uuid());
		scjbDynaBean.set("CPTMBGZTB_TM", dynaBean.getStr("SCJP_TM"));
		DynaBean cpDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " and PRODUCTDATA_BH = '"+dynaBean.getStr("SCJP_CPBH")+"'");
		scjbDynaBean.set("CPTMBGZTB_CPBM", dynaBean.getStr("SCJP_CPBH"));
		if(cpDynaBean!=null) {
			scjbDynaBean.set("CPTMBGZTB_CPMC", cpDynaBean.getStr("PRODUCTDATA_NAME"));
		}
		DynaBean cxDynaBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ", " and CXSJ_CXBM = '"+dynaBean.getStr("SCJP_CXBH")+"'");
		scjbDynaBean.set("CPTMBGZTB_CXBH", dynaBean.getStr("SCJP_CXBH"));//产线编号
		if(cxDynaBean!=null) {
			scjbDynaBean.set("CPTMBGZTB_CXID", cxDynaBean.getStr("JGMES_BASE_CXSJ_ID"));//产线ID
			scjbDynaBean.set("CPTMBGZTB_CXMC", cxDynaBean.getStr("CXSJ_CXMC"));//产线名称
		}
		
		DynaBean gxDynaBean = serviceTemplate.selectOne("JGMES_GYGL_GXGL", " and GXGL_GXNUM = '"+dynaBean.getStr("SCJP_GXBH")+"'");
		if(gxDynaBean!=null) {
			scjbDynaBean.set("CPTMBGZTB_DQGXID", gxDynaBean.getStr("GXGL_ID"));//工序ID
			scjbDynaBean.set("CPTMBGZTB_GXLX", gxDynaBean.getStr("GXGL_GXLX_NAME"));//工序类型
		}
		scjbDynaBean.set("CPTMBGZTB_DQGXBH", dynaBean.getStr("SCJP_GXBH"));//工序编号
		scjbDynaBean.set("CPTMBGZTB_DQGXMC", dynaBean.getStr("SCJP_GXMC"));//工序名称
//		scjbDynaBean.set("SCJP_GWMC", value);//工位名称
//		scjbDynaBean.set("SCJP_GWBH", value);//工位编号
//		scjbDynaBean.set("SCJP_GWID", value);//工位ID
		if("RWZT02".equals(dynaBean.getStr("SCJP_RWZT_CODE"))) {
			scjbDynaBean.set("CPTMBGZTB_KSSJ", jgmesCommon.getCurrentTime());//开始时间
		}
		if("RWZT03".equals(dynaBean.getStr("SCJP_RWZT_CODE"))) {
			scjbDynaBean.set("CPTMBGZTB_JSSJ", jgmesCommon.getCurrentTime());//结束时间
		}
		
		//scjbDynaBean.set("SCJP_SC", value);//时长
		DynaBean dic1 = jgmesCommon.getDic("JGMES_DIC_RWZT","RWZT02");
		if(dic1!=null) {
			scjbDynaBean.set("CPTMBGZTB_RWZT_NAME", dic1.get("DICTIONARYITEM_ITEMNAME"));//生产状态_NAME
			scjbDynaBean.set("CPTMBGZTB_RWZT_CODE", dic1.get("DICTIONARYITEM_ITEMCODE"));//生产状态
		}
		scjbDynaBean.set("CPTMBGZTB_SCRWID", dynaBean.getStr("SCJP_SCRWID"));//生产任务ID
		scjbDynaBean.set("CPTMBGZTB_RWDH", dynaBean.getStr("SCJP_RWDH"));//任务单号
		scjbDynaBean.set("CPTMBGZTB_SCY", dynaBean.getStr("SCJP_SCY"));//生产员
//		scjbDynaBean.set("SCJP_QRR", value);//确认人
//		scjbDynaBean.set("SCJP_QRSM", value);//确认说明
//		scjbDynaBean.set("SCJP_FJ", value);//附件
		
		return scjbDynaBean;
		
	}
	
	/**
	 * 设置报工数据
	 * @param scjbDynaBean
	 * @return
	 */
	public DynaBean setBgsl(DynaBean scjbDynaBean) {
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
		DynaBean bgsj = new DynaBean();
		bgsj.setStr(beanUtils.KEY_TABLE_CODE, "JGMES_PB_BGSJ");
		bgsj.set("JGMES_PB_BGSJ_ID", JEUUID.uuid());
		bgsj.set("BGSJ_GZSJ", jgmesCommon.getCurrentTime());
		if(scjbDynaBean==null) {
			return  null;
		}
		bgsj.set("BGSJ_CXMC", scjbDynaBean.getStr("SCJP_CXMC"));  //产线名称
		bgsj.set("BGSJ_CXBM", scjbDynaBean.getStr("SCJP_CXBH"));  //产线编码
		bgsj.set("BGSJ_GXBH", scjbDynaBean.getStr("SCJP_GXBH"));  //工序编号
		bgsj.set("BGSJ_GXMC", scjbDynaBean.getStr("SCJP_GXMC"));  //工序名称
		bgsj.set("BGSJ_CPBH", scjbDynaBean.getStr("SCJP_CPBH"));  //产品编号
		bgsj.set("BGSJ_CPMC", scjbDynaBean.getStr("SCJP_CPMC"));  //产品名称
		//bgsj.set("BGSJ_CPGG", scrwztDynaBean.getStr("SCRWZT_CPGG"));  //产品规格
		bgsj.set("BGSJ_TMH", scjbDynaBean.getStr("SCJP_TM"));  //条码号
		bgsj.set("BGSJ_TMLX", scjbDynaBean.getStr("SCJP_GXLX"));  //条码类型
//		bgsj.set("BGSJ_GWBH", value);  //工位编号
//		bgsj.set("BGSJ_GWMC", value);  //工位名称
		bgsj.set("BGSJ_SL", 1);  //数量
		//bgsj.set("BGSJ_BZ", value);  //备注
		
		//设置状态（默认开启）
		DynaBean dic = jgmesCommon.getDic("JGMES_STATUS","1");
		if(dic!=null) {
			bgsj.set("BGSJ_STATUS_NAME", dic.getStr("DICTIONARYITEM_ITEMNAME"));
			bgsj.set("BGSJ_STATUS_CODE", dic.getStr("DICTIONARYITEM_ITEMCODE"));
			bgsj.set("BGSJ_STATUS_ID", dic.getStr("JE_CORE_DICTIONARYITEM_ID"));
		}
		//生产任务单
		bgsj.set("BGSJ_SCRW", scjbDynaBean.getStr("SCJP_RWDH"));
		//生产任务单
		bgsj.set("BGSJ_SCRWID", scjbDynaBean.getStr("SCJP_SCRWID"));
		
		return bgsj;
	}



	/**
	 * 校验条码的类型是什么
	 */
	public void doCheckBarCode(){
		// MAC地址
		String mac = request.getParameter("mac");
		// 用户编码  必填
		String userCode = request.getParameter("userCode");
		// 条码
		String barCode = request.getParameter("barCode");

		JgmesResult<ResultBoolean> ret = new JgmesResult<ResultBoolean>();

		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {
			if(barCode!=null&&!"".equals(barCode)){
				ResultBoolean resultBoolean = checkBarCode(barCode);
				if(resultBoolean!=null){
					ret.Data = checkBarCode(barCode);
				}else{
					ret.setMessage("无效SN码！");
				}

			}
		}
		toWrite(jsonBuilder.toJson(ret));
	}

	public ResultBoolean checkBarCode(String barCode){
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
		ResultBoolean returnDynaBean = null;
		//先去产品码库里面找，是否未产品码
		DynaBean tmScDynaBean = serviceTemplate.selectOne("JGMES_BASE_GDCPTM"," and GDCPTM_TMH='"+barCode+"'");
		if (tmScDynaBean != null) {
			returnDynaBean = new ResultBoolean();
			returnDynaBean.setTmLx("TMLX01");
			returnDynaBean.setBarCode(barCode);
		}else{
			//获取条码标识信息  JGMES_SYS_TMBZ
			List<DynaBean> tmbzList = serviceTemplate.selectList("JGMES_SYS_TMBZ","");
			if(tmbzList!=null&&tmbzList.size()>0){
				for(DynaBean tmbz:tmbzList){
					String regex = "";
					if(tmbz.getStr("TMBZ_FGF")!=null&&!"".equals(tmbz.getStr("TMBZ_FGF"))){
						regex = "^"+tmbz.getStr("TMBZ_BSNR")+"\\"+tmbz.getStr("TMBZ_FGF")+"\\d{0,}\\S*$";
					}else{
						regex = "^"+tmbz.getStr("TMBZ_BSNR")+"\\d{0,}\\S*$";
					}
					if(jgmesCommon.match(regex,barCode)){
						if("KGM".equals(tmbz.getStr("TMBZ_TMFL_CODE"))){
							if("KGM".equals(barCode)){
								returnDynaBean = new ResultBoolean();
								returnDynaBean.setTmLx(tmbz.getStr("TMBZ_TMFL_CODE"));
								returnDynaBean.setBarCode(barCode);
							}
						}else if("WGM".equals(tmbz.getStr("TMBZ_TMFL_CODE"))){
							if("WGM".equals(barCode)){
								returnDynaBean = new ResultBoolean();
								returnDynaBean.setTmLx(tmbz.getStr("TMBZ_TMFL_CODE"));
								returnDynaBean.setBarCode(barCode);
							}
						}else if("GXM".equals(tmbz.getStr("TMBZ_TMFL_CODE"))){

							String gxBarCode = barCode.replace(tmbz.getStr("TMBZ_BSNR"),"").replace(tmbz.getStr("TMBZ_FGF"),"");
							DynaBean gxDynaBean = serviceTemplate.selectOne("JGMES_GYGL_GXGL"," and GXGL_GXNUM = '"+gxBarCode+"'");
							if(gxDynaBean!=null){
								returnDynaBean = new ResultBoolean();
								returnDynaBean.setTmLx(tmbz.getStr("TMBZ_TMFL_CODE"));
								returnDynaBean.setBarCode(barCode);
								//判断此条码是工序码，需要返回工序信息

								returnDynaBean.setGxDynaBean(gxDynaBean.getValues());
							}
						}
					}

				}
			}
		}

		return returnDynaBean;
	}


	/**
	 * 返回 布尔类型对象
	 *
	 * @author John
	 *
	 */
	public class ResultBoolean {

		private String tmLx;

		private String barCode;

		private HashMap gxDynaBean;

		public String getTmLx() {
			return tmLx;
		}

		public void setTmLx(String tmLx) {
			this.tmLx = tmLx;
		}

		public String getBarCode() {
			return barCode;
		}

		public void setBarCode(String barCode) {
			this.barCode = barCode;
		}

		public HashMap getGxDynaBean() {
			return gxDynaBean;
		}

		public void setGxDynaBean(HashMap gxDynaBean) {
			this.gxDynaBean = gxDynaBean;
		}
	}
	
	/**
     * 校验用户合法性，不合法直接提示。
     */
    private JgmesResult<String> doCheck(String userCode,String mac) {
    	JgmesResult<String> result = new JgmesCommon(request, serviceTemplate).doCheckRes(mac, userCode);
    	if (!result.IsSuccess) {
			toWrite(jsonBuilder.toJson(result));
		}
    	return result;
	}
	
}