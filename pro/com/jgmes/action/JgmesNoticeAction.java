package com.jgmes.action;

import com.je.core.action.DynaAction;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.DynaBean;
import com.jgmes.util.JgmesCommon;
import com.jgmes.util.JgmesResult;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
/**
 * 
 * @author lxc
 * @version 2019-01-31
 * @see /jgmes/jgmesNoticeAction!getNoticeList.action
 */
@Component("jgmesNoticeAction")
@Scope("prototype")
public class JgmesNoticeAction extends DynaAction {
	
	/*
	 * 	获取通知列表
	 */
	public void getNoticeList() {
		//String ParrentID="e8b4eb90-1a21-4fdf-bfca-6c53409a80a8";
		String NOTICE_NOTICETYPE_CODE = request.getParameter("NOTICE_NOTICETYPE_CODE");
		String dateStart = request.getParameter("dateStart");
		String dateEnd = request.getParameter("dateEnd");
		String NOTICE_QY = request.getParameter("NOTICE_QY");
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
		JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
		String dateFilter=" ";
		String jsonStr = "";
		if(dateStart!=null&&!dateEnd.isEmpty()) {
			dateFilter=dateFilter+" and SY_CREATETIME>'"+dateStart+"'";
		}
		if(dateEnd!=null&&!dateEnd.isEmpty()) {
			dateFilter=dateFilter+" and SY_CREATETIME<'"+dateEnd+"'";
		}
		if (NOTICE_QY == null || NOTICE_QY.isEmpty()) {
			NOTICE_QY="0";
		}
		if(NOTICE_NOTICETYPE_CODE!=null&&!NOTICE_NOTICETYPE_CODE.isEmpty()) {
			dateFilter=dateFilter+" and NOTICE_NOTICETYPE_CODE='"+NOTICE_NOTICETYPE_CODE+"'";
		}
		dateFilter=dateFilter+" and NOTICE_QY='"+NOTICE_QY+"'";
		
		try {
			long selectCount = serviceTemplate.selectCount("JE_SYS_NOTICE", dateFilter);
			List<DynaBean> selectList = serviceTemplate.selectList("JE_SYS_NOTICE", dateFilter);
			ret.TotalCount=selectCount;
			ret.Data=ret.getValues(selectList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			ret.IsSuccess = false;
			ret.setMessage(e.toString());
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		jsonStr = jsonBuilder.toJson(ret);
		System.out.println("数据字典列表：");
		System.out.println(jsonStr);
		toWrite(jsonStr);
	}

	/*
	 * 	获取机体异常通知，author:ljs
	 */
	public void getMachineAbnormalList() {
		String dateStart = request.getParameter("dateStart");
//		String yclx = request.getParameter("yclx");
		String jtCode = request.getParameter("jtCode");//机台编号
		String jtName = request.getParameter("jtName");//机台名称
		String readNot = request.getParameter("ReadNot");
//		String dateEnd = request.getParameter("dateEnd");
		String AbnormalType = request.getParameter("AbnormalType");
		String userCode = request.getParameter("userCode");
		String yczt = request.getParameter("yczt");
		String date = request.getParameter("date");
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
		JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
		String dateFilter=" ";
		String jsonStr = "";
//		if(dateStart!=null&&!dateEnd.isEmpty()) {
//			dateFilter+=" and SY_CREATETIME>'"+dateStart+"'";
//		}
//		if(dateEnd!=null&&!dateEnd.isEmpty()) {
//			dateFilter+=" and SY_CREATETIME<'"+dateEnd+"'";
//		}
		if (StringUtil.isNotEmpty(jtCode)){
			dateFilter+="and JTYCJL_JTH like '%"+jtCode+"%'";
		}
		if (StringUtil.isNotEmpty(jtName)){
			dateFilter+="and JTYCJL_JTMC like '%"+jtName+"%'";
		}
		if (StringUtil.isNotEmpty(readNot)){
			if (readNot.indexOf(",")!=-1) {
				dateFilter += " and JTYCTZ_YDZT_CODE in ('"+readNot.replaceAll(",", "','")+"')";
			}
			else {
				dateFilter += jgmesCommon.getSqlWhere("JTYCTZ_YDZT_CODE", readNot);
			}
		}

		if (StringUtil.isNotEmpty(AbnormalType)){
			if (AbnormalType.indexOf(",")!=-1) {
				dateFilter += " and JTYCJL_JTYCLX_CODE in ('"+AbnormalType.replaceAll(",", "','")+"')";
			}
			else {
				dateFilter += jgmesCommon.getSqlWhere("JTYCJL_JTYCLX_CODE", AbnormalType);
			}
		}
		//处理状态
		if (StringUtil.isNotEmpty(yczt)){
			if (yczt.indexOf(",")!=-1) {
				dateFilter += " and JTYCJL_JTYCCLZT_CODE in ('"+yczt.replaceAll(",", "','")+"')";
			}
			else {
				dateFilter += jgmesCommon.getSqlWhere("JTYCJL_JTYCCLZT_CODE", yczt);
			}
		}

		//通知日期
		if (StringUtil.isNotEmpty(date)){
			dateFilter += "and DATE_FORMAT(JTYCJL_YCSJ,'%Y-%m-%d') = DATE_FORMAT('"+date+"','%Y-%m-%d')";
		}

		//获取发生异常多少分钟发送异常的系统参数
		DynaBean ycsjXtcsBean = serviceTemplate.selectOne("JGMES_XTGL_XTCS", " and XTCS_CXFL2_CODE='YCFSXXSJ'");//获取异常发送的最大时间，系统参数
		Integer time= 0;
		if (ycsjXtcsBean != null && ycsjXtcsBean.getStr("XTCS_NO_CODE").equals("1")) {//不等于空且启用状态
			time = ycsjXtcsBean.getInt("XTCS_CSZ");//获取参数值
		}
		dateFilter +="and TIMESTAMPDIFF(MINUTE,JTYCJL_YCSJ,now())>="+time;

		try {
			List<DynaBean> selectList=serviceTemplate.selectList("v_jgmes_lhyctzjl",dateFilter);
			long selectCount = serviceTemplate.selectCount("v_jgmes_lhyctzjl", dateFilter);
//			long selectCount = serviceTemplate.selectCount("JGMES_ADMK_JTYCTZ", dateFilter);
//			List<DynaBean> selectList = serviceTemplate.selectList("JGMES_ADMK_JTYCTZ", dateFilter);
//			List<DynaBean> userList = serviceTemplate.selectList("JE_CORE_ENDUSER", "and 1=1");
//			for (DynaBean bean : selectList) {
//				String tzrCode = bean.getStr("JTYCTZ_TZR");//通知人code
//				String userName = castUserName(tzrCode);
//				bean.set("JTYCTZ_TZR",userName);
//			}
			ret.TotalCount=selectCount;
			ret.Data=ret.getValues(selectList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			ret.IsSuccess = false;
			ret.setMessage(e.toString());
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		jsonStr = jsonBuilder.toJson(ret);
		System.out.println("数据字典列表：");
		System.out.println(jsonStr);
		toWrite(jsonStr);
	}

	/**
	 * 把usercode转换成username
	 * @param userStr
	 * @return
	 */
	private String castUserName(String userStr){
		String u="";
		if (userStr.indexOf(",")!=-1){
			String[] split = userStr.split(",");
			for (int i=0;i<split.length;i++){
				DynaBean user = serviceTemplate.selectOne("JE_CORE_ENDUSER", "and USERCODE='" + split[i]+"'");
				if (user!=null){
					split[i]=user.getStr("USERNAME");
					u+=split[i]+",";
				}
			}
			u=u.substring(0,u.length()-1);
		}else{
			DynaBean user = serviceTemplate.selectOne("JE_CORE_ENDUSER", "and USERCODE='" + userStr+"'");
			u+=user.getStr("USERNAME");
		}
		return u;
	}





	/*
	 *	根据通知ID获取通知
	 */
	public void getNoticeById() {
		String noticeId=request.getParameter("noticeId");
		String jsonStr="";
		//JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
		JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
		if(noticeId==null&&noticeId.isEmpty()) {
			ret.IsSuccess=false;
			ret.setMessage("参数不能为空");
		}else {//JE_SYS_NOTICE
			DynaBean selectOne = serviceTemplate.selectOne("JE_SYS_NOTICE", " and JE_SYS_NOTICE_ID='"+noticeId+"'");
//			jsonStr = jsonBuilder.toJson(selectOne.getValues());
			ret.Data=selectOne.getValues();
		}
		jsonStr = jsonBuilder.toJson(ret);
		System.out.println(jsonStr);
		toWrite(jsonStr);
	}
}
