package com.jgmes.action;


import com.je.core.action.DynaAction;
import com.je.core.util.bean.DynaBean;
import com.jgmes.util.JgmesCommon;
import com.jgmes.util.JgmesResult;
import com.jgmes.util.TreeDto;
import com.jgmes.util.TreeToolUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author liuc
 * @version 2019-02-20 15:30:27
 * @see /jgmes/jgmesQmsAction!load.action
 */
@Component("jgmesQmsAction")
@Scope("prototype")
public class JgmesQmsAction extends DynaAction  {
	
	@Override
	public void load(){
		toWrite("hello Action");
	}
	
	/*
	    *    获取不良类型列表
	 * 
	 */
	
	public void getBlList() {
		// 用户编号
		String userCode = request.getParameter("userCode");
		//工艺路线工序ID
		String gxlxgxId = request.getParameter("gxId");
		
		JgmesResult<List<DynaBean>> ret = new JgmesResult<List<DynaBean>>();
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
		
		List<DynaBean> blDynaBean = null; 
		
		if(userCode==null || userCode.isEmpty()) {
			userCode = jgmesCommon.jgmesUser.getCurrentUserCode();
		}
		//获取系统参数（不良项是否与工序挂勾）
		DynaBean wltmScDynaBean = serviceTemplate.selectOne("JGMES_XTGL_XTCS"," and XTCS_CXFL2_TEXT='不良项是否与工序挂勾'");
		//1表示挂勾，0表示不挂勾
		if("1".equals(wltmScDynaBean.getStr("XTCS_CSZ"))) {
			//获取工序检测不良项
			List<DynaBean> gxblDynaBeanList = serviceTemplate.selectList("JGMES_GYGL_GXBLX", " and GYLXGX_ID = '"+gxlxgxId+"'");
			ret = getBlDynaBean(ret,gxblDynaBeanList);
			if(ret.Data==null||ret.Data.size()==0){
				//获取工艺路线工序
				DynaBean gylxgxDynaBean=serviceTemplate.selectOne("JGMES_GYGL_GYLXGX"," and GYLXGX_ID = '"+gxlxgxId+"'");
				if(gylxgxDynaBean!=null){
					//获取工序检测不良项
					List<DynaBean> gxblDynaBeanList2 = serviceTemplate.selectList("JGMES_GYGL_GXBLX", " and GXGL_ID = '"+gylxgxDynaBean.getStr("GYLXGX_GXID")+"'");
					ret = getBlDynaBean(ret,gxblDynaBeanList2);
				}
			}
		}else if("0".equals(wltmScDynaBean.getStr("XTCS_CSZ"))||wltmScDynaBean==null) {
			blDynaBean = serviceTemplate.selectList("JGMES_BASE_BLLX", "");
			ret.IsSuccess = true;
			ret.Data = blDynaBean;
		}
		
		toWrite(jsonBuilder.toJson(ret));
	}

	/**
	 * 根据moID获取工单物料
	 */
	public void getGdWlByMoID() {
		// MAC地址
		String mac = request.getParameter("mac");
		// 用户编码  必填
		String userCode = request.getParameter("userCode");
		//MOID
		String moId = request.getParameter("moId");

		JgmesResult<List<HashMap>> ret = new JgmesResult<>();
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);

		List<DynaBean> blDynaBean = null;

		if (doCheck(userCode, mac).IsSuccess) {
			//获取工单物料表
			List<DynaBean> gdwlDynaBeanList = serviceTemplate.selectList("JGMES_PLAN_GDWLB", " and GDWLB_MoID = '"+moId+"'");
			if(gdwlDynaBeanList!=null&&gdwlDynaBeanList.size()>0){
				ret.Data = ret.getValues(gdwlDynaBeanList);
			}
			toWrite(jsonBuilder.toJson(ret));

		}
	}


	/**
	 * 获取物料不良项中的不良项目
	 */
	public void getWlBlList() {
		// MAC地址
		String mac = request.getParameter("mac");
		// 用户编码  必填
		String userCode = request.getParameter("userCode");
		//工单物料表主键ID
		String gdwlID = request.getParameter("gdwlID");

		JgmesResult<List<HashMap>> ret = new JgmesResult<>();
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);

		if (doCheck(userCode, mac).IsSuccess) {
			//获取工单物料表
			DynaBean gdwlDynaBean = serviceTemplate.selectOne("JGMES_PLAN_GDWLB", " and JGMES_PLAN_GDWLB_ID = '"+gdwlID+"'");
			if(gdwlDynaBean!=null){
				//获取物料不良项
				List<DynaBean> wlblDynaBeanList = serviceTemplate.selectList("JGMES_PLAN_WLBLX"," and WLBLX_GDWLBZJID = '"+gdwlDynaBean.getStr("JGMES_PLAN_GDWLB_ID")+"'");
				if(wlblDynaBeanList!=null&&wlblDynaBeanList.size()>0){
					StringBuffer sBuffer = new StringBuffer("(");
					for(int i = 0;i<wlblDynaBeanList.size();i++) {
						sBuffer.append("'"+wlblDynaBeanList.get(i).getStr("WLBLX_BLXBH")+"'");
						if(i < wlblDynaBeanList.size()-1) {
							sBuffer.append(",");
						}
					}
					sBuffer.append(")");
					List<DynaBean> blDynaBean = serviceTemplate.selectList("JGMES_BASE_BLLX", " and BLLX_BLLXBM in "+sBuffer);
					if(blDynaBean!=null&&blDynaBean.size()>0) {
						ret.IsSuccess = true;
						ret.Data = ret.getValues(blDynaBean);
					}
				}

			}
			toWrite(jsonBuilder.toJson(ret));

		}
	}


	/**
	 *
	 */
	public void getBlListByParam() {
		// MAC地址
		String mac = request.getParameter("mac");
		// 用户编码  必填
		String userCode = request.getParameter("userCode");
		// 不良编码
		String blbm = request.getParameter("blbm");
		
		//获取工序编码
		String gxbm = request.getParameter("gxbm");

		// 不良名称
		String blmc = request.getParameter("blmc");
		// 每页码
		String pageSize = request.getParameter("PageSize");
		// 当前页
		String currPage = request.getParameter("CurrPage");

		JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
		String jsonStr = "";
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {
			//每页码
			if (pageSize == null || pageSize.isEmpty()) {
				pageSize = "10";
			}
			//当前页
			if (currPage == null || currPage.isEmpty()) {
				currPage = "1";
			}

			int kss = Integer.parseInt(pageSize)*(Integer.parseInt(currPage)-1);
			int size = Integer.parseInt(pageSize);

			StringBuffer whereSql = new StringBuffer();
			whereSql.append(" and JGMES_BASE_BLLX_ID !='ROOT'");

			if(blbm!=null&&!"".equals(blbm)){
				whereSql.append(" and BLLX_BLLXBM like '%"+blbm+"%'");
			}
			if(blmc!=null&&!"".equals(blmc)){
				whereSql.append(" and BLLX_BLLXMC like '%"+blmc+"%'");
			}
			if(gxbm!=null&&!"".equals(gxbm)) {
				whereSql.append(" and (BLLX_GXBM = '"+gxbm+"' or BLLX_GXBM = '' or BLLX_GXBM is null)");
			}

			List<DynaBean> blxDynaBeanList = serviceTemplate.selectList("JGMES_BASE_BLLX",whereSql.toString());
			if(blxDynaBeanList!=null&&blxDynaBeanList.size()>0){
				ret.TotalCount = Long.valueOf(blxDynaBeanList.size());
			}

			whereSql.append(" ORDER BY BLLX_BLLXBM+0 LIMIT "+kss+","+size+"");
			blxDynaBeanList = serviceTemplate.selectList("JGMES_BASE_BLLX",whereSql.toString());
			if(blxDynaBeanList!=null&&blxDynaBeanList.size()>0){
				ret.Data = ret.getValues(blxDynaBeanList);
			}
		}
		jsonStr = jsonBuilder.toJson(ret);
		toWrite(jsonStr);
	}


	/**
	 * @author liuc
	 * 获取所有有效的不良项，按照项目分类的树形结构
	 * @see /jgmes/jgmesQmsAction!getBlListAll.action
	 */
	public void getBlListAll(){
		// MAC地址
		String mac = request.getParameter("mac");
		// 用户编码  必填
		String userCode = request.getParameter("userCode");
		// 不良编码
		String blbm = request.getParameter("blbm");

		// 不良名称
		String blmc = request.getParameter("blmc");

		JgmesResult<List<TreeDto>> ret = new JgmesResult<List<TreeDto>>();
		String jsonStr = "";
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {
			StringBuffer whereSql = new StringBuffer();
			if(blbm!=null&&!"".equals(blbm)){
				whereSql.append(" and BLLX_BLLXBM like '%"+blbm+"%'");
			}
			if(blmc!=null&&!"".equals(blmc)){
				whereSql.append(" and BLLX_BLLXMC like '%"+blmc+"%'");
			}
			List<DynaBean> blxDynaBeanList = serviceTemplate.selectList("V_JGMES_BASE_BLLX",whereSql.toString());
			if(blxDynaBeanList!=null&&blxDynaBeanList.size()>0){
				//根节点
				List<TreeDto> rootList = new ArrayList<TreeDto>();
				//子节点
				List<TreeDto> bodyList = new ArrayList<TreeDto>();

				for (DynaBean blxDynaBean:blxDynaBeanList){
					DynaBean xmflDynaBean = serviceTemplate.selectOne("JGMES_BASE_BLFL"," and BLFL_BLXMBM = '"+blxDynaBean.get("BLLX_BLFLBM")+"'");
					if(xmflDynaBean!=null){
						Map<String, List<TreeDto>> map = getXmFlTree(xmflDynaBean.getStr("JGMES_BASE_BLFL_ID"),rootList,bodyList);
						rootList = map.get("root");
						bodyList = map.get("body");
					}
				}

				rootList = getBlByJyxm(rootList,blxDynaBeanList);
				bodyList = getBlByJyxm(bodyList,blxDynaBeanList);

				TreeToolUtils utils =  new TreeToolUtils(rootList,bodyList);
				List<TreeDto> result =  utils.getTree();
				ret.Data = result;
			}
		}
		jsonStr = jsonBuilder.toJson(ret);
		toWrite(jsonStr);
	}
	
	
	/**
	 * 
	   *     根据工序ID获取对应的不良类型列表
	 */
	public JgmesResult<List<DynaBean>> getBlDynaBean(JgmesResult<List<DynaBean>> ret,List<DynaBean> gxblDynaBeanList){
		List<DynaBean> blDynaBean = null;
		if(gxblDynaBeanList!=null&&gxblDynaBeanList.size()>0) {
			StringBuffer sBuffer = new StringBuffer("(");
			for(int i = 0;i<gxblDynaBeanList.size();i++) {
				sBuffer.append("'"+gxblDynaBeanList.get(i).getStr("JGMES_BASE_BLLX_ID")+"'");
				if(i < gxblDynaBeanList.size()-1) {
					sBuffer.append(",");
				}
			}
			sBuffer.append(")");
			blDynaBean = serviceTemplate.selectList("JGMES_BASE_BLLX", " and JGMES_BASE_BLLX_ID in "+sBuffer);
			if(blDynaBean!=null&&blDynaBean.size()>0) {
				ret.IsSuccess = true;
				ret.Data = blDynaBean;
			}
		}
		return ret;
	}

	/**
	 * 校验用户合法性，不合法直接提示。
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

	public Map<String, List<TreeDto>> getXmFlTree(String id,List<TreeDto> rootList,List<TreeDto> bodyList){
		Map<String, List<TreeDto>> map = new HashMap<String, List<TreeDto>>();
		DynaBean xmflDynaBean = serviceTemplate.selectOne("JGMES_BASE_BLFL"," and JGMES_BASE_BLFL_ID = '"+id+"'");
		if(xmflDynaBean!=null){
			TreeDto<DynaBean> treeDto = new TreeDto<DynaBean>();
			treeDto.setId(xmflDynaBean.getStr("JGMES_BASE_BLFL_ID"));
			treeDto.setFlbm(xmflDynaBean.getStr("BLFL_BLXMBM"));
			treeDto.setFlmc(xmflDynaBean.getStr("BLFL_BLXMMC"));
			treeDto.setSyParent(xmflDynaBean.getStr("SY_PARENT"));
			if(!"ROOT".equals(xmflDynaBean.get("SY_PARENT"))){
				boolean isNotDuplicate = true;
				for(TreeDto tree:bodyList){
					if(tree.equals(treeDto)){
						isNotDuplicate = false;
					}
				}
				if(isNotDuplicate){
					bodyList.add(treeDto);
				}
				map = getXmFlTree(xmflDynaBean.getStr("SY_PARENT"),rootList,bodyList);
			}else{
				boolean isNotDuplicate = true;
				for(TreeDto tree:rootList){
					if(tree.equals(treeDto)){
						isNotDuplicate = false;
					}
				}
				if(isNotDuplicate){
					rootList.add(treeDto);
				}
				map.put("root",rootList);
				map.put("body",bodyList);
			}
		}
		return map;
	}


	/**
	 * 根据检测项目来获取产品检验树root
	 * @return
	 */
	public ArrayList<TreeDto> getBlByJyxm(List<TreeDto> treeList,List<DynaBean> DynaBeanList){
		ArrayList<TreeDto> list = new ArrayList<>();
		if(treeList == null || treeList.size() == 0){
			return list;
		}
		for(TreeDto tree:treeList){
			//根据不良项目编码来获取不良类型
			List<DynaBean> bllxList  = new ArrayList<DynaBean>();
			for(DynaBean dynaBean:DynaBeanList){
				if(tree.getFlbm()!=null&&tree.getFlbm().equals(dynaBean.get("BLLX_BLFLBM"))){
					bllxList.add(dynaBean);
				}
			}
			tree.setData(bllxList);
			list.add(tree);
		}
		return list;
	}
}