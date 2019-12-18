package com.jgmes.action;

import com.gexin.fastjson.JSON;
import com.je.core.action.DynaAction;
import com.je.core.dao.PCDaoTemplate;
import com.je.core.util.JdbcUtil;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.DynaBean;
import com.je.table.exception.PCExcuteException;
import com.jgmes.util.JgmesCommon;
import com.jgmes.util.JgmesResult;
import net.sf.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

//import org.apache.log4j.Logger;
/**
 * 
 * @author cj
 * @version 2019-05-21 22:19:58
 * @see /jgmes/jgmesFindRO!load.action
 */
@Component("jgmesFindRO")
@Scope("prototype")
public class JgmesFindRO extends DynaAction  {
	private static final long serialVersionUID = 1L;
	
	public void load(){
		toWrite("hello Action");
	}
	//基础数据    --获取产线产品
	public void getCpCx() {
		//获取产线
		String sql1="select CXSJ_CXBM,CXSJ_CXMC from JGMES_BASE_CXSJ  ORDER BY CXSJ_CXMC";
		//获取产品
		String sql2="select PRODUCTDATA_BH,PRODUCTDATA_NAME from JGMES_BASE_PRODUCTDATA\r\n" + 
				"where PRODUCTDATA_WLTYPE_CODE='CP' ";
		List<DynaBean> selectListBySql = serviceTemplate.selectListBySql(sql1);
		JgmesResult<Object> ret = new JgmesResult<Object>();
		List<HashMap> cx = ret.getValues(selectListBySql);
		List<DynaBean> selectListBySql2 = serviceTemplate.selectListBySql(sql2);
		JgmesResult<Object> ret2 = new JgmesResult<Object>();
		List<HashMap> cp = ret2.getValues(selectListBySql2);
		JSONObject object = new JSONObject();
		object.put("cp",cp);
		object.put("cx",cx);
		toWrite(jsonBuilder.toJson(object));
	}

	public void getCp() {
		String cpname = request.getParameter("cpname");// 用户编号
		String pages = request.getParameter("page");// 用户编号
		String limit = request.getParameter("limit");
		//获取产品
		String sql2="select PRODUCTDATA_BH,PRODUCTDATA_NAME from JGMES_BASE_PRODUCTDATA\r\n" +
				"where PRODUCTDATA_WLTYPE_CODE='CP' ";
		String sqlcount=" and PRODUCTDATA_WLTYPE_CODE='CP' ";

		if(cpname!=null&&!"".equals(cpname)){
			sqlcount+="  and (PRODUCTDATA_BH like '%"+cpname+"%'  or PRODUCTDATA_NAME like '%"+cpname+"%') ";
			sql2+="  and (PRODUCTDATA_BH like '%"+cpname+"%'  or PRODUCTDATA_NAME like '%"+cpname+"%' )";
		}
		try {
			int page = Integer.parseInt(pages)-1;
			sql2+=" limit "+ page + ","+limit;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		long count = serviceTemplate.selectCount("JGMES_BASE_PRODUCTDATA", sqlcount);
		List<DynaBean> selectListBySql2 = serviceTemplate.selectListBySql(sql2);
		JgmesResult<Object> ret2 = new JgmesResult<Object>();
		List<HashMap> cp = ret2.getValues(selectListBySql2);
		JSONObject object = new JSONObject();
		object.put("code","0");
		object.put("msg","0");
		object.put("data",cp);
		object.put("count",count);
		toWrite(jsonBuilder.toJson(object));
	}
	//每日产量统计报表
	public void getEverydayOutput() {
		String page = request.getParameter("page");// 用户编号
		String limit = request.getParameter("limit");
		String cp = request.getParameter("cp");
		String cx = request.getParameter("cx");
		String bgdate = request.getParameter("bgdate");
		String enddate = request.getParameter("enddate");
		String rwdh = request.getParameter("rwdh");
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String p_where="";
		if(bgdate!=null&&!"".equals(bgdate)) {
			//bgdate=simpleDateFormat.format(new Date());
			p_where+=" And DATE_FORMAT(日期,''%y-%m-%d'') >= DATE_FORMAT(''"+bgdate+"'',''%y-%m-%d'')  ";
		}
		if(enddate!=null&&!"".equals(enddate)){
			p_where+=" And DATE_FORMAT(日期,''%y-%m-%d'') <= DATE_FORMAT(''"+enddate+"'',''%y-%m-%d'')  ";
		}
		if(cp!=null&&!cp.equals("")) {
			p_where+=" And 产品编号=''"+cp+"''  ";
		}
		if(cx!=null&&!cx.equals("")) {
			p_where+=" And 产线编码=''"+cx+"''  ";
		}
		if (StringUtil.isNotEmpty(rwdh)){
			p_where+=" And 生产任务单=''"+rwdh+"''  ";
		}
		String Sql1="call  p_jgmes_everydayoutput('"+p_where+"',"+page+","+limit+")";
		List<DynaBean> selectListBySql = serviceTemplate.selectListBySql(Sql1);
		System.out.println(selectListBySql);
		int num=0;
		if(selectListBySql.size()>0) {
			num = selectListBySql.get(0).getInt("sl");
			//num=(String) selectListBySql.get(0).get("num");
		}
		JgmesResult<Object> ret = new JgmesResult<Object>();
		List<HashMap> values = ret.getValues(selectListBySql);
		JSONObject object = new JSONObject();
		object.put("code","0");
		object.put("msg","0");
		object.put("count",num);
		object.put("data",values);
		toWrite(jsonBuilder.toJson(object));
	}
	//每日产量统计报表
	public void getPieceWage() {
		String page = request.getParameter("page");// 用户编号
		String limit = request.getParameter("limit");
		String cp = request.getParameter("cp");
		String cx = request.getParameter("cx");
		String bgdate = request.getParameter("bgdate");
		String enddate = request.getParameter("enddate");
		String ddh = request.getParameter("ddh");
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String p_where="";
		if(bgdate!=null&&!"".equals(bgdate)) {
			//bgdate=simpleDateFormat.format(new Date());
			p_where+=" And DATE_FORMAT(过站时间,''%y-%m-%d'') >= DATE_FORMAT(''"+bgdate+"'',''%y-%m-%d'')  ";
		}
		if(enddate!=null&&!"".equals(enddate)){
			p_where+=" And DATE_FORMAT(过站时间,''%y-%m-%d'') <= DATE_FORMAT(''"+enddate+"'',''%y-%m-%d'')  ";
		}
		if(cp!=null&&!cp.equals("")) {
			p_where+=" And 产品名称=''"+cp+"''  ";
		}
//		if(cx!=null&&!cx.equals("")) {
//			p_where+=" And 产线编码=''"+cx+"''  ";
//		}
		if(cx!=null&&!cx.equals("")) {
			p_where+=" And 产线名称 like ''%"+cx+"%''  ";
		}
		if (ddh!=null&&!ddh.equals("")){
			p_where+=" And 订单号  like ''%"+ddh+"%''  ";
		}
		String Sql1="call  p_jgmes_poecewage('"+p_where+"',"+page+","+limit+")";
		String Sqlcoun="call  p_jgmes_poecewagecount('"+p_where+"',"+page+","+limit+")";
		List<DynaBean> selectListBySql = serviceTemplate.selectListBySql(Sql1);
		List<DynaBean> selectcount = serviceTemplate.selectListBySql(Sqlcoun);
		System.out.println(selectcount.get(0).get("count"));
		int num=0;
		if(selectListBySql.size()>0) {
			num = selectListBySql.get(0).getInt("sl");
			//num=(String) selectListBySql.get(0).get("num");
		}
		JgmesResult<Object> ret = new JgmesResult<Object>();
		List<HashMap> values = ret.getValues(selectListBySql);
		JSONObject object = new JSONObject();
		object.put("code","0");
		object.put("msg","0");
		object.put("count",selectcount.get(0).get("count"));
		object.put("data",values);
		toWrite(jsonBuilder.toJson(object));
	}
	
	//巨大追朔报表
	public  void JdRestspectStatm() {
		String palletbebar=request.getParameter("palletbebar");
		String palletendbar=request.getParameter("palletendbar");
		JSONObject object = new JSONObject();
		object.put("code","0");
		object.put("count",100000);
		if(palletbebar==null&&"".equals(palletbebar)){
			object.put("msg","栈板开始条码不能为空！");
			object.put("data","");
			toWrite(jsonBuilder.toJson(object));
		}
		if(palletendbar==null&&"".equals(palletendbar)){
			object.put("msg","栈板结束条码不能为空！");
			object.put("data","");
			toWrite(jsonBuilder.toJson(object));
		}
		String Sql1="call  jgmes_jdrestspect('"+palletbebar+"','"+palletendbar+"')";
		List<DynaBean> selectListBySql = serviceTemplate.selectListBySql(Sql1);
		JgmesResult<Object> ret = new JgmesResult<Object>();
		List<HashMap> values = ret.getValues(selectListBySql);
		object.put("data",values);
		toWrite(jsonBuilder.toJson(object));
	}

	//巨大追朔报表
	public  void lhAblStatm() {
		String cx=request.getParameter("cx");
		String bgdate=request.getParameter("bgdate");
		JSONObject object = new JSONObject();
		object.put("code","0");
		object.put("count",0);
		if(cx!=null&&!"".equals(cx)&&bgdate!=null&&!"".equals(bgdate)){
			String Sql1="call  jgmes_lhjtyc('"+cx+"','"+bgdate+"')";
			List<DynaBean> selectListBySql = serviceTemplate.selectListBySql(Sql1);
			JgmesResult<Object> ret = new JgmesResult<Object>();
			List<HashMap> values = ret.getValues(selectListBySql);
			object.put("data",values);
		}else{
			object.put("data","");
		}
		toWrite(jsonBuilder.toJson(object));
	}
	//巨大条码明细报表
	public  void jDBarCode() {
		String depthODM=request.getParameter("depthODM");
		JSONObject object = new JSONObject();
		object.put("code","0");
		object.put("count",0);
		if(depthODM!=null&&!"".equals(depthODM)){
			String Sql1="call  jgmes_snmx('"+depthODM+"')";
			List<DynaBean> selectListBySql = serviceTemplate.selectListBySql(Sql1);
			JgmesResult<Object> ret = new JgmesResult<Object>();
			List<HashMap> values = ret.getValues(selectListBySql);
			object.put("data",values);
		}else{
			object.put("data","");
		}
		toWrite(jsonBuilder.toJson(object));
	}

	//巨大订单明细报表
	public  void jDOdmDetail() {
		String depthODM=request.getParameter("depthODM");
		JSONObject object = new JSONObject();
		object.put("code","0");
		object.put("count",0);
		if(depthODM!=null&&!"".equals(depthODM)){
			String Sql1="call  jgmes_ODMZXSL('"+depthODM+"')";
			List<DynaBean> selectListBySql = serviceTemplate.selectListBySql(Sql1);
			JgmesResult<Object> ret = new JgmesResult<Object>();
			List<HashMap> values = ret.getValues(selectListBySql);
			object.put("data",values);
		}else{
			object.put("data","");
		}
		toWrite(jsonBuilder.toJson(object));
	}

	//乐惠APP任务报表
	public  void SrwFinish() {
		String date=request.getParameter("date");
		JSONObject object = new JSONObject();
		object.put("code","0");
		object.put("count",0);
		if(date!=null&&!"".equals(date)){
			String Sql1="call  jgmes_rwzt('"+date+"')";
			List<DynaBean> selectListBySql = serviceTemplate.selectListBySql(Sql1);
			JgmesResult<Object> ret = new JgmesResult<Object>();
			List<HashMap> values = ret.getValues(selectListBySql);
			object.put("data",values);
		}else{
			object.put("data","");
		}
		toWrite(jsonBuilder.toJson(object));
	}

	//乐惠APP机台报表
	public  void Machineunusual() {
		String date="1";
		JSONObject object = new JSONObject();
		object.put("code","0");
		object.put("count",0);
		if(date!=null&&!"".equals(date)){
			String Sql1="call  jgmes_jtzt()";
			List<DynaBean> selectListBySql = serviceTemplate.selectListBySql(Sql1);
			JgmesResult<Object> ret = new JgmesResult<Object>();
			List<HashMap> values = ret.getValues(selectListBySql);
			object.put("data",values);
		}else{
			object.put("data","");
		}
		toWrite(jsonBuilder.toJson(object));
	}




	//批处理
    public void gylx(){
		JSONObject object = new JSONObject();
        String ArrayList=request.getParameter("ArrayList");
		com.gexin.fastjson.JSONObject jsonObject = JSON.parseObject(ArrayList);
		List hbbj = (List)jsonObject.get("HBBJ");
		int add = add(hbbj);
		System.out.println(ArrayList);
        object.put("hello","成功");
        toWrite(jsonBuilder.toJson(object));
    }
    public int add(List datalist) throws PCExcuteException {
        Connection conn = this.daoTemplate.getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        PreparedStatement st = null;
        int count = 0;
        try {
            String sql = "insert into jgmes_tabll(iid,gxname,sjc) values(?,?,?)";
            st= conn.prepareStatement(sql);

            String v="";

            int n=1;
            for(int i=1;i<datalist.size();i++){  //i=1000  2000

				List o = (List)datalist.get(i);
				if(!v.equals((String)o.get(0))){
					v=(String)o.get(0);
					n=1;
				}else{
					n++;
				}
				st.setString(1,  (String)o.get(0));
				st.setString(2, (String)o.get(1));
                st.setLong(3, n);
                st.addBatch();
                if(i%100000==0){
                    st.executeBatch();
                    st.clearBatch();
                }
            }
            st.executeBatch();
        } catch (Exception var14) {
            logger.error("", var14);
        } finally {
            JdbcUtil.close(rs, stmt, conn);
        }

        return count;
    }


    private PCDaoTemplate daoTemplate;
	@Resource(name = "PCDAOTemplateORCL")
	public void setDaoTemplate(PCDaoTemplate daoTemplate) {
		this.daoTemplate = daoTemplate;
	}
}