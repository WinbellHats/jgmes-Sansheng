package com.jgmes.action;

import com.je.core.action.DynaAction;
import com.je.core.util.bean.DynaBean;
import com.jgmes.util.JgmesCommon;
import com.jgmes.util.JgmesResult;
import net.sf.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.HashMap;

/**
 * 
 * @author fm
 * @version 2018-12-17 21:20:50
 * @see /jgmes/jgmesLoginAction!doSaveLogin.action
 */
@Component("jgmesLoginAction")
@Scope("prototype")
public class JgmesLoginAction  extends DynaAction{
		
	public void doSaveLogin() throws ParseException {
		JgmesResult<JSONObject> jgmesResult = new JgmesResult<JSONObject>();
		String mac = request.getParameter("mac");
		String ip = request.getParameter("ip");
		String userCode = request.getParameter("userCode");//非必填，为空则获取当前登陆用户
		String userName = request.getParameter("userName");//非必填，为空则获取当前登陆用户
		JgmesCommon jgmesCommon = new JgmesCommon(request,serviceTemplate,userCode);
		
		try {
			if (userCode==null || userCode.isEmpty()) {
	//			EndUser user = loginUser;//SecurityUserHolder.getCurrentUser();
				userCode =jgmesCommon.jgmesUser.getCurrentUserCode();// getCookieByName(request,"loginUserCode");//user.getUserCode();
				userName = jgmesCommon.jgmesUser.getCurrentUserName();
			}
			if (userName==null || userName.isEmpty()) {
	//			EndUser user = loginUser;//SecurityUserHolder.getUserByCode(userCode);
				userName =jgmesCommon.jgmesUser.getCurrentUserName();// user.getUsername();
			}
			if (mac==null || mac.isEmpty()) {
				jgmesResult.setMessage("MAC地址不能为空！");
				toWrite(jsonBuilder.toJson(jgmesResult));
			}
			DynaBean gwj = serviceTemplate.selectOne("JGMES_EQ_STATIONMACHINE", " and STATIONMACHINE_MACDZ='"+mac+"' and STATIONMACHINE_USESTATUS_CODE=0");
			if (gwj == null) {
				jgmesResult.setMessage("工位机中无此MAC地址！");
				toWrite(jsonBuilder.toJson(jgmesResult));
			}
			DynaBean dlgl = new DynaBean("JGMES_PB_DLGL", true);
			dlgl.set("DLGL_GWJBM", gwj.getStr("STATIONMACHINE_GWJBM"));
			dlgl.set("DLGL_GWJMC", gwj.getStr("STATIONMACHINE_GWJMC"));
			dlgl.set("DLGL_MACDZ", gwj.getStr("STATIONMACHINE_MACDZ"));
			dlgl.set("DLGL_IPDZ", ip);
			dlgl.set("DLGL_GWBH", gwj.getStr("STATIONMACHINE_GWBH"));
			dlgl.set("DLGL_GWMC", gwj.getStr("STATIONMACHINE_GWMC"));
			dlgl.set("DLGL_YHBM", userCode);
			dlgl.set("DLGL_YHMC", userName);
			dlgl.set("DLGL_DLSJ", jgmesCommon.getCurrentTime());
	//		dlgl.set("DLGL_NO_CODE", gwj.getStr("BOM_MM_MC"));
	//		dlgl.set("DLGL_NO_NAME", gwj.getStr("BOM_MM_MC"));
	//		dlgl.set("DLGL_NO_ID", gwj.getStr("BOM_MM_MC"));
			
			DynaBean dic = jgmesCommon.getDic("JGMES_YES_NO", "1");
			dlgl.set("DLGL_NO_ID", dic.get("JE_CORE_DICTIONARYITEM_ID"));
			dlgl.set("DLGL_NO_CODE", dic.get("DICTIONARYITEM_ITEMCODE"));
			dlgl.set("DLGL_NO_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));
			
			jgmesCommon.setDynaBeanInfo(dlgl);
			
	//		serviceTemplate.buildModelCreateInfo(dlgl);
			DynaBean dlgl_jg = serviceTemplate.insert(dlgl);
			
			DynaBean dicNo = jgmesCommon.getDic("JGMES_YES_NO", "0");
			StringBuilder sBuilder = new StringBuilder();
			sBuilder.append("update JGMES_PB_DLGL ");
			sBuilder.append("set DLGL_NO_ID='"+dicNo.get("JE_CORE_DICTIONARYITEM_ID")+"'");
			sBuilder.append(", DLGL_NO_CODE='"+dicNo.get("DICTIONARYITEM_ITEMCODE")+"'");
			sBuilder.append(", DLGL_NO_NAME='"+dicNo.get("DICTIONARYITEM_ITEMNAME")+"'");
			sBuilder.append(" where (DLGL_MACDZ='"+dlgl_jg.getStr("DLGL_MACDZ")+"'");
			sBuilder.append(" or DLGL_YHBM='"+dlgl_jg.get("DLGL_YHBM")+"')");
			sBuilder.append(" and DLGL_NO_ID='"+dic.get("JE_CORE_DICTIONARYITEM_ID")+"'");
			sBuilder.append(" and JGMES_PB_DLGL_ID<>'"+dlgl_jg.getStr("JGMES_PB_DLGL_ID")+"'");
			System.out.println("登陆状态登陆语句"+sBuilder.toString());
			pcServiceTemplate.executeSql(sBuilder.toString());
			
			jgmesResult.IsSuccess =true;
			
			CommonAction commonAction = new CommonAction();
			jgmesResult.Data=commonAction.getBaseInfo(userCode,request,serviceTemplate);
			
		} catch (Exception e) {
			// TODO: handle exception
			jgmesResult.setMessage(e.getMessage());
		}
		toWrite(jsonBuilder.toJson(jgmesResult));
	}
	
	/**
	 * 不校验mac地址
	 * 
	 * @throws ParseException
	 */
	public void doSaveLoginForPDA() throws ParseException {
		JgmesResult<JSONObject> jgmesResult = new JgmesResult<JSONObject>();
		String mac = request.getParameter("mac");
		String ip = request.getParameter("ip");
		String userCode = request.getParameter("userCode");//非必填，为空则获取当前登陆用户
		String userName = request.getParameter("userName");//非必填，为空则获取当前登陆用户
		JgmesCommon jgmesCommon = new JgmesCommon(request,serviceTemplate,userCode);
		
		try {
			if (userCode==null || userCode.isEmpty()) {
	//			EndUser user = loginUser;//SecurityUserHolder.getCurrentUser();
				userCode =jgmesCommon.jgmesUser.getCurrentUserCode();// getCookieByName(request,"loginUserCode");//user.getUserCode();
				userName = jgmesCommon.jgmesUser.getCurrentUserName();
			}
			if (userName==null || userName.isEmpty()) {
	//			EndUser user = loginUser;//SecurityUserHolder.getUserByCode(userCode);
				userName =jgmesCommon.jgmesUser.getCurrentUserName();// user.getUsername();
			}
			if (mac==null || mac.isEmpty()) {
				jgmesResult.setMessage("MAC地址不能为空！");
				toWrite(jsonBuilder.toJson(jgmesResult));
			}
//			DynaBean gwj = serviceTemplate.selectOne("JGMES_EQ_STATIONMACHINE", " and STATIONMACHINE_MACDZ='"+mac+"'");
//			if (gwj == null) {
//				jgmesResult.setMessage("工位机中无此MAC地址！");
//				toWrite(jsonBuilder.toJson(jgmesResult));
//			}
			DynaBean dlgl = new DynaBean("JGMES_PB_DLGL", true);
//			dlgl.set("DLGL_GWJBM", gwj.getStr("STATIONMACHINE_GWJBM"));
//			dlgl.set("DLGL_GWJMC", gwj.getStr("STATIONMACHINE_GWJMC"));
			dlgl.set("DLGL_MACDZ", mac);
			dlgl.set("DLGL_IPDZ", ip);
//			dlgl.set("DLGL_GWBH", gwj.getStr("STATIONMACHINE_GWBH"));
//			dlgl.set("DLGL_GWMC", gwj.getStr("STATIONMACHINE_GWMC"));
			dlgl.set("DLGL_YHBM", userCode);
			dlgl.set("DLGL_YHMC", userName);
			dlgl.set("DLGL_DLSJ", jgmesCommon.getCurrentTime());
	//		dlgl.set("DLGL_NO_CODE", gwj.getStr("BOM_MM_MC"));
	//		dlgl.set("DLGL_NO_NAME", gwj.getStr("BOM_MM_MC"));
	//		dlgl.set("DLGL_NO_ID", gwj.getStr("BOM_MM_MC"));
			
			DynaBean dic = jgmesCommon.getDic("JGMES_YES_NO", "1");
			dlgl.set("DLGL_NO_ID", dic.get("JE_CORE_DICTIONARYITEM_ID"));
			dlgl.set("DLGL_NO_CODE", dic.get("DICTIONARYITEM_ITEMCODE"));
			dlgl.set("DLGL_NO_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));
			
			jgmesCommon.setDynaBeanInfo(dlgl);
			
	//		serviceTemplate.buildModelCreateInfo(dlgl);
			DynaBean dlgl_jg = serviceTemplate.insert(dlgl);
			
			DynaBean dicNo = jgmesCommon.getDic("JGMES_YES_NO", "0");
			StringBuilder sBuilder = new StringBuilder();
			sBuilder.append("update JGMES_PB_DLGL ");
			sBuilder.append("set DLGL_NO_ID='"+dicNo.get("JE_CORE_DICTIONARYITEM_ID")+"'");
			sBuilder.append(", DLGL_NO_CODE='"+dicNo.get("DICTIONARYITEM_ITEMCODE")+"'");
			sBuilder.append(", DLGL_NO_NAME='"+dicNo.get("DICTIONARYITEM_ITEMNAME")+"'");
			sBuilder.append(" where (DLGL_MACDZ='"+dlgl_jg.getStr("DLGL_MACDZ")+"'");
			sBuilder.append(" or DLGL_YHBM='"+dlgl_jg.get("DLGL_YHBM")+"')");
			sBuilder.append(" and DLGL_NO_ID='"+dic.get("JE_CORE_DICTIONARYITEM_ID")+"'");
			sBuilder.append(" and JGMES_PB_DLGL_ID<>'"+dlgl_jg.getStr("JGMES_PB_DLGL_ID")+"'");
			System.out.println("登陆状态登陆语句"+sBuilder.toString());
			pcServiceTemplate.executeSql(sBuilder.toString());
			
			jgmesResult.IsSuccess =true;
			
			CommonAction commonAction = new CommonAction();
			jgmesResult.Data=commonAction.getBaseInfo(userCode,request,serviceTemplate);
			
		} catch (Exception e) {
			// TODO: handle exception
			jgmesResult.setMessage(e.getMessage());
		}
		toWrite(jsonBuilder.toJson(jgmesResult));
	}

	public void getXtcsByCode(){
		// MAC地址
		String mac = request.getParameter("mac");
		// 用户编码  必填
		String userCode = request.getParameter("userCode");
		//参数分类数据字典中的值
		String cxflCode = request.getParameter("cxflCode");

		JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
		String jsonStr = "";
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {
			DynaBean isCheckSGXDynaBean=serviceTemplate.selectOne("JGMES_XTGL_XTCS"," and  XTCS_CXFL1_CODE = 'CXFL06' and XTCS_CXFL2_CODE = '"+cxflCode+"'");
			if(isCheckSGXDynaBean!=null){
				ret.Data = isCheckSGXDynaBean.getValues();
			}
		}
		jsonStr = jsonBuilder.toJson(ret);
		toWrite(jsonStr);
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
	
	
}
