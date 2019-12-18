package com.demo.action;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.je.core.action.DynaAction;
import com.je.core.util.JEUUID;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.BeanUtils;
import com.je.core.util.bean.DynaBean;
import com.je.datasource.runner.DynaSqlRunner;
/**
 * 第三方产品信息
 * @author Administrator
 * @see/demork/demoRkdAction!load.action*
 */
@Component("dsfCpInfoAction")
@Scope("prototype")
public class DsfCpInfoAction extends DynaAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3612220595716340098L;

	public void load(){
		//构建排序条件
		try {
			String order=dynaBeanTemplate.buildOrderSql(sort, orderSql,useOrderSql);
			dynaBean.set(BeanUtils.KEY_ORDER, order);
			ArrayList<DynaBean> list =new ArrayList<DynaBean>();
			if(StringUtil.isNotEmpty(expandSql)){
				whereSql+=expandSql;
			}
			DynaSqlRunner sqlRunner=DynaSqlRunner.getInstance("cpinfo");
			long count=0;
			if(limit==-1) {
				List<Map<String,Object>> lists=sqlRunner.queryMapList("SELECT CPINFO_CPMC,CPINFO_CPDM,CPINFO_GGXH,CPINFO_JG,CPINFO_DQKC FROM JE_DEMO_CPINFO WHERE 1=1 "+whereSql);
				if(lists!=null && lists.size()>0) {
					for(Map<String,Object> vals:lists) {
						DynaBean bean=new DynaBean("JE_DEMO_CPINFO",true);
						bean.set("CPINFO_CPMC", vals.get("CPINFO_CPMC")+"");
						bean.set("CPINFO_CPDM", vals.get("CPINFO_CPDM")+"");
						bean.set("CPINFO_GGXH", vals.get("CPINFO_GGXH")+"");
						bean.set("CPINFO_JG", vals.get("CPINFO_JG")+"");
						bean.set("CPINFO_DQKC", vals.get("CPINFO_DQKC")+"");
						bean.set("JE_DEMO_CPINFO_ID", JEUUID.uuid());
						list.add(bean);
					}
				}
				count=list.size();
			}else {
				String dbType="mysql";
				List<Map<String,Object>> counts=sqlRunner.queryMapList("SELECT COUNT(*) COUNTNUM FROM JE_DEMO_CPINFO WHERE 1=1 "+whereSql);
				if(counts!=null && counts.size()>0) {
					count=Long.parseLong(counts.get(0).get("COUNTNUM")+"");
				}
				List<Map<String,Object>> lists=null;
				if("mysql".equals(dbType)) {
					lists=sqlRunner.queryMapList("SELECT CPINFO_CPMC,CPINFO_CPDM,CPINFO_GGXH,CPINFO_JG,CPINFO_DQKC FROM JE_DEMO_CPINFO WHERE 1=1 "+whereSql+" "+orderSql+" limit "+start+","+limit);
				}
				if(lists!=null && lists.size()>0) {
					for(Map<String,Object> vals:lists) {
						DynaBean bean=new DynaBean("JE_DEMO_CPINFO",true);
						bean.set("CPINFO_CPMC", vals.get("CPINFO_CPMC")+"");
						bean.set("CPINFO_CPDM", vals.get("CPINFO_CPDM")+"");
						bean.set("CPINFO_GGXH", vals.get("CPINFO_GGXH")+"");
						bean.set("CPINFO_JG", vals.get("CPINFO_JG")+"");
						bean.set("CPINFO_DQKC", vals.get("CPINFO_DQKC")+"");
						bean.set("JE_DEMO_CPINFO_ID", JEUUID.uuid());
						list.add(bean);
					}
				}
			}
			strData = jsonBuilder.buildListPageJson(count, list, true);
			toWrite(strData);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void doUpdate() {
		//获取前台修改的参数
		String cpMc=dynaBean.getStr("CPINFO_CPMC");
		String cpDm=dynaBean.getStr("CPINFO_CPDM");
		String ggXh=dynaBean.getStr("CPINFO_GGXH");
		String jg=dynaBean.getStr("CPINFO_JG");
		String dqkc=dynaBean.getStr("CPINFO_DQKC");
		DynaSqlRunner sqlRunner=DynaSqlRunner.getInstance("cpinfo");		
		try {
			sqlRunner.execute(" UPDATE JE_DEMO_CPINFO SET CPINFO_CPMC='"+cpMc+"',CPINFO_CPDM='"+cpDm+"',CPINFO_GGXH='"+ggXh+"',CPINFO_JG='"+jg+"',CPINFO_DQKC='"+dqkc+"' WHERE CPINFO_CPDM='"+cpDm+"'");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		strData = jsonBuilder.toJson(dynaBean);
		toWrite(jsonBuilder.returnSuccessJson(strData));
	}
}
