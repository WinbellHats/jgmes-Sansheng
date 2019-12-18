package com.jgmes.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.python.antlr.PythonParser.return_stmt_return;

import com.je.core.action.DynaAction;
import com.je.core.service.PCDynaServiceTemplate;
import com.je.core.util.JEUUID;
import com.je.core.util.bean.BeanUtils;
import com.je.core.util.bean.DynaBean;
/*** 这个类是条码的方法的抽取类
 * @author Luo_wr
 * Date 2019/01/14
 */
public class JgmesBarCode  extends DynaAction{
	
	private static final long serialVersionUID = 1L;
	public JgmesUser jgmesUser = null;
	public JgmesBarCode(HttpServletRequest requestT,PCDynaServiceTemplate serviceTemplateT) {
		request = requestT;
		serviceTemplate = serviceTemplateT;
		jgmesUser =new JgmesUser(requestT,serviceTemplateT);
	}
	//public List<DynaBean> 
	/*
	 * 功能：获取流水号的抽取
	 * author: Luo_wr
	 * date 2019/01/15
	 */
	public Map<String, Object> getWaterNumber(Map map){
		List<DynaBean> lsbean=(List<DynaBean>) map.get("lsbean");
		String lsh= (String) map.get("lsh");
		System.out.println(lsbean.size() );
		if (lsbean.size() > 0) {
			lsh=lsbean.get(0).getStr("DQJL_DQLSH");
			map.put("lsh",lsh );
		} else {
			lsh = "0";
			map.put("lsh", lsh);
			DynaBean newBean = new DynaBean("JGMES_BARCODE_DQJL", true);
			String newid = JEUUID.uuid();
			newBean.set("DQJL_DQLSH", lsh);
			newBean.set("DQJL_N", (int)map.get("year"));
			newBean.set("DQJL_Y",	(int)map.get("month") );
			//newBean.set("DQJL_CPBH",	(String)map.get("wlcpbh") );
			//newBean.set("DQJL_YYGZBM",	(String)map.get("yygzbh") );
			newBean.set("DQJL_KHCPBH", (String)map.get("wlkhcpbh"));
			newBean.set("DQJL_GDTMDSCID", newid);
			serviceTemplate.insert(newBean);
		}
		return map;
	}
	
	/*
	 * 功能：获取产品编号的抽取
	 * author: Luo_wr
	 * date 2019/01/15
	 */
	public Map<String, Object> getProductNumber(Map map){
		DynaBean cxbean=new DynaBean();
		List<DynaBean> zbean=(List<DynaBean>) map.get("zbean");
		DynaBean  cpbean=(DynaBean) map.get("cpbean");
		System.out.println(zbean.get(0).getStr("PCLB_CXMC")+"0" + zbean.get(0).getStr("PCLB_CXBM"));
		String lsh= (String) map.get("lsh");
		
		cxbean = serviceTemplate.selectOne("JGMES_BASE_CXSJ", " and CXSJ_CXMC='"
				+ zbean.get(0).getStr("PCLB_CXMC") + "' and CXSJ_CXBM='" + zbean.get(0).getStr("PCLB_CXBM") + "'");
		map.put("cxbean", cxbean);
		String cpbh = "";
		String khcpbm = cpbean.getStr("PRODUCTDATA_KHCPH");
		if (khcpbm != null) {
			cpbh=khcpbm;
			map.put("khcpbm", cpbh);
		} else {
			khcpbm=zbean.get(0).getStr("PCLB_CPBH");
			map.put("khcpbm",khcpbm );
		}
		System.out.println(map.get("khcpbm"));
		return map;
	}
	
	/*
	 * 功能：生成or查询条码记录
	 * author: Luo_wr
	 * date 2019/01/15
	 */
	public Map<String, Object> addOrFindBarCode(Map map){
		List<DynaBean> zbean=(List<DynaBean>) map.get("zbean");
		List<DynaBean> tmlxbean=(List<DynaBean>) map.get("tmlxbean");
		DynaBean  cpbean=(DynaBean) map.get("cpbean");
		List<DynaBean> tmjlBean = serviceTemplate.selectList("JGMES_BARCODE_TMSCJL",
				" and JGMES_BARCODE_TMSCJL_ID='" + zbean.get(0).get("JGMES_BARCODE_TMSCJL_ID")
						+ "' and TMSCJL_TMLX_CODE='" + zbean.get(0).getStr("CPTMYYGG_TMLX_CODE") + "'");
		String tmscjlId = "";
		if (tmjlBean.size() > 0) {
			tmscjlId = tmjlBean.get(0).getStr("JGMES_BARCODE_TMSCJL_ID");
			map.put("tmscjlId", tmscjlId);
		} else {
			String tmjlId = JEUUID.uuid();
			map.put("tmscjlId", tmscjlId);
			tmscjlId = tmjlId;
			DynaBean tmjlbean = new DynaBean();
			tmjlbean.setStr(BeanUtils.KEY_TABLE_CODE, "JGMES_BARCODE_TMSCJL");
			serviceTemplate.buildModelCreateInfo(tmjlbean);
			tmjlbean.set("JGMES_BARCODE_TMSCJL_ID", tmjlId);
			tmjlbean.set("TMSCJL_CPBH", zbean.get(0).get("PCLB_CPBH"));
			tmjlbean.set("TMSCJL_CPMC", cpbean.get("PRODUCTDATA_NAME"));
			tmjlbean.set("TMSCJL_YSCSL", 0);
			tmjlbean.set("TMSCJL_GDHM", zbean.get(0).get("PCLB_GDHM"));
			tmjlbean.set("TMSCJL_PCSL", zbean.get(0).get("PCLB_PCSL"));
			tmjlbean.set("TMSCJL_PCDBH", zbean.get(0).get("PCLB_PCDBH"));
			tmjlbean.set("TMSCJL_PCDID", zbean.get(0).get("JGMES_PLAN_SCRW_ID"));
			tmjlbean.set("TMSCJL_PCRQ", zbean.get(0).get("PCLB_PCRQ"));
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String scrq = formatter.format(new Date());

			tmjlbean.set("TMSCJL_SCRQ", scrq);
			tmjlbean.set("TMSCJL_CXMC", zbean.get(0).get("PCLB_CXMC"));
			tmjlbean.set("TMSCJL_CXBH", zbean.get(0).get("PCLB_CXBM"));

			tmjlbean.set("TMSCJL_CXID", ((DynaBean)map.get("cxbean")).get("JGMES_BASE_CXSJ_ID"));
			tmjlbean.set("TMSCJL_MTMSL", tmlxbean.get(0).get("CPTMYYGG_MTMSL"));
			tmjlbean.set("TMSCJL_TMLX_ID", tmlxbean.get(0).get("CPTMYYGG_TMLX_ID"));
			tmjlbean.set("TMSCJL_TMLX_CODE", tmlxbean.get(0).get("CPTMYYGG_TMLX_CODE"));
			tmjlbean.set("TMSCJL_TMLX_NAME", tmlxbean.get(0).get("CPTMYYGG_TMLX_NAME"));
			tmjlbean.set("TMSCJL_TMBMMS", tmlxbean.get(0).get("CPTMYYGG_TMBMMS"));
			tmjlbean.set("TMSCJL_TMGZBH", tmlxbean.get(0).get("CPTMYYGG_TMGZBH"));
			tmjlbean.set("TMSCJL_YYGZBH", tmlxbean.get(0).get("CPTMYYGG_YYGZBH"));
			tmjlbean.set("TMSCJL_YYGGMC", tmlxbean.get(0).get("CPTMYYGG_YYGGMC"));
			tmjlbean.set("TMSCJL_NZWTMLX_NAME", tmlxbean.get(0).get("CPTMYYGG_NZWTMLX_NAME"));
			tmjlbean.set("TMSCJL_NZWTMLX_CODE", tmlxbean.get(0).get("CPTMYYGG_NZWTMLX_CODE"));
			tmjlbean.set("TMSCJL_NZWTMLX_ID", tmlxbean.get(0).get("CPTMYYGG_NZWTMLX_ID"));
			tmjlbean.set("TMSCJL_BQMB", tmlxbean.get(0).get("CPTMYYGG_BQMB"));
			tmjlbean.set("TMSCJL_TMYYGGID", tmlxbean.get(0).get("JGMES_BASE_CPTMYYGG_ID"));
			serviceTemplate.insert(tmjlbean);
		}
		return map;
	}
	
	
	
	public Map<String, Object> getWaterNumbers(Map map){
		List<DynaBean> lsbean=(List<DynaBean>) map.get("lsbean");
		String lsh= (String) map.get("lsh");
		if (lsbean.size() > 0) {
			lsh= lsbean.get(0).getStr("DQJL_DQLSH");
		} else {
			lsh = "0";
			DynaBean newBean = new DynaBean("JGMES_BARCODE_DQJL", true);
			newBean.set("DQJL_DQLSH", lsh);
			newBean.set("DQJL_N", (int)map.get("year"));
			newBean.set("DQJL_Y",	(int)map.get("month") );
			newBean.set("DQJL_GDTMDSCID", (String)map.get("id"));
			serviceTemplate.insert(newBean);
		}
		map.put("lsh", lsh);
		return map;
	}
	/*
	public int test(Map map,Object[] param1) {
		for (int i = 0; i < num; i++) {
			DynaBean bean = new DynaBean("JGMES_BASE_GDCPTM", true);
			newLsh = Integer.parseInt(lsh) + i + 1;
			String Lsh = "0000000" + newLsh;
			String tmLsh = Lsh.substring(Lsh.length() - 7, Lsh.length());
			tmh = gys + cpbh + fyear + fmonth + tmLsh;
			bean.set("GDCPTM_TMH", tmh);
			bean.set("NEWID", id);
			bean.set("GDCPTM_PCDBH", zbean.get(0).get("PCLB_PCDBH"));
			bean.set("GDCPTM_PCDMC", zbean.get(0).get("PCLB_PCDMC"));
			bean.set("GDCPTM_CPBH", zbean.get(0).get("PCLB_CPBH"));
			bean.set("GDCPTM_NAME", cpbean.get("PRODUCTDATA_NAME"));
			bean.set("GDCPTM_GDHM", zbean.get(0).get("PCLB_GDHM"));
			bean.set("GDCPTM_DDHM", zbean.get(0).get("PCLB_DDHM"));
			bean.set("JGMES_PLAN_SCRW_ID", zbean.get(0).get("JGMES_PLAN_SCRW_ID"));
			bean.set("GDCPTM_PCRQ", zbean.get(0).get("PCLB_PCRQ"));
			bean.set("GDCPTM_SL", (int) (((pcsl - yscsl) / mtmsl) > (i + 1) ? mtmsl : ((pcsl - yscsl) % mtmsl)));
			bean.set("GDCPTM_TMLX_CODE", zbean.get(0).get("CPTMYYGG_TMLX_CODE"));
			bean.set("GDCPTM_TMLX_NAME", tmlxbean.get(0).get("CPTMYYGG_TMLX_NAME"));
			bean.set("GDCPTM_TMLX_ID", tmlxbean.get(0).get("CPTMYYGG_TMLX_ID"));
			bean.set("JGMES_BARCODE_TMSCJL_ID", tmscjlId);
			bean.set("GDCPTM_SFYDY", 0);
			serviceTemplate.insert(bean);
		}
	}
	*/
	public static void main(String[] args) {
	}
}
