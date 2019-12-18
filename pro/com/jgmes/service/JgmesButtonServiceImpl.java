package com.jgmes.service;

import com.je.core.security.SecurityUserHolder;
import com.je.core.service.PCDynaServiceTemplate;
import com.je.core.service.PCServiceTemplate;
import com.je.core.util.JEUUID;
import com.je.core.util.bean.BeanUtils;
import com.je.core.util.bean.DynaBean;
import com.je.rbac.model.Department;
import com.je.rbac.model.EndUser;
import com.je.rbac.service.UserManager;
import com.jgmes.util.JgmesCommon;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 专门处理按钮的service
 * @author liuc
 * @version 2019-05-05 10:47:02
 */
@Component("jgmesButtonService")
public class JgmesButtonServiceImpl implements JgmesButtonService  {

	/**动态Bean(DynaBean)的服务层*/
	private PCDynaServiceTemplate serviceTemplate;
	/**实体Bean操作服务层,主要操作SQL*/
	private PCServiceTemplate pcServiceTemplate;
	/**用户服务层*/
	private UserManager userManager;

	@Override
	public void load(){
		System.out.println("hello serviceimpl");
	}

	@Override
	public String doSaveCPGZTemplate(String[] ids) {
		if (ids != null && ids.length > 0) {
			JgmesCommon jgmesCommon = new JgmesCommon(null, serviceTemplate);
			for (int i = 0; i < ids.length; i++) {
				DynaBean cptmyyggDynaBean = serviceTemplate.selectOne("JGMES_BASE_CPTMYYGG", " and JGMES_BASE_CPTMYYGG_ID = '" + ids[i] + "'");
				if(cptmyyggDynaBean!=null){

					//查询一下，这条数据是否已经设置为模板
					List<DynaBean> cptmyyggDynaBeanList = serviceTemplate.selectList("JGMES_BASE_CPTMYYGG"," and CPTMYYGG_YYGGMC = '"+cptmyyggDynaBean.getStr("CPTMYYGG_YYGGMC")+"' and CPTMYYGG_NO_CODE = '1' ");
					if(cptmyyggDynaBeanList!=null&&cptmyyggDynaBeanList.size()>0){
						return "\""+cptmyyggDynaBean.getStr("CPTMYYGG_YYGGMC")+":该产品条码应用规则已经有模板了\"";
					}else{
						cptmyyggDynaBean.set("JGMES_BASE_CPTMYYGG_ID", JEUUID.uuid());
						cptmyyggDynaBean.set("CPTMYYGG_YYGGMC", cptmyyggDynaBean.getStr("CPTMYYGG_YYGGMC"));
						cptmyyggDynaBean.set("CPTMYYGG_YYGZBH", serviceTemplate.buildCode("CPTMYYGG_YYGZBH", "JGMES_BASE_CPTMYYGG", cptmyyggDynaBean));
						cptmyyggDynaBean.set("CPTMYYGG_STATUS_CODE",2);//存为模板时设置为不启用
						cptmyyggDynaBean.set("CPTMYYGG_STATUS_NAME","不启用");//存为模板时设置为不启用
						//模板条码应用规则中产品数据应该是空的
						cptmyyggDynaBean.remove("CPTMYYGG_CPBH");
						cptmyyggDynaBean.remove("CPTMYYGG_CPNAME");
						cptmyyggDynaBean.remove("JGMES_BASE_PRODUCTDATA_ID");
						//设置模板
						DynaBean dic = jgmesCommon.getDic("JGMES_YES_NO","1");
						if(dic!=null){
							cptmyyggDynaBean.set("CPTMYYGG_NO_CODE",dic.get("DICTIONARYITEM_ITEMCODE"));
							cptmyyggDynaBean.set("CPTMYYGG_NO_NAME",dic.get("DICTIONARYITEM_ITEMNAME"));
						}
						serviceTemplate.insert(cptmyyggDynaBean);
					}
				}

			}
		}
		return "\"设置模板成功！\"";
	}

	@Override
	public String doSaveCpTmYYGG(String[] ids, String cpbm) {
		if(ids!=null&&ids.length>0&&cpbm!=null&&!"".equals(cpbm)){
			JgmesCommon jgmesCommon = new JgmesCommon(null, serviceTemplate);

			//获取产品数据
			DynaBean cpDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA"," and PRODUCTDATA_BH = '"+cpbm+"'");
			if(cpDynaBean!=null){
				StringBuffer yyggmc = new StringBuffer();
				for (int i = 0; i < ids.length; i++) {
					DynaBean cptmyyggDynaBean = serviceTemplate.selectOne("JGMES_BASE_CPTMYYGG", " and JGMES_BASE_CPTMYYGG_ID = '" + ids[i] + "'");
					List<DynaBean> cptmyyggDynaBeanList = serviceTemplate.selectList("JGMES_BASE_CPTMYYGG", " and CPTMYYGG_CPBH = '"+cpbm+"' and CPTMYYGG_YYGGMC = '"+cptmyyggDynaBean.getStr("CPTMYYGG_YYGGMC")+"'");
					if(cptmyyggDynaBeanList!=null&&cptmyyggDynaBeanList.size()>0){
						yyggmc.append(cptmyyggDynaBean.getStr("CPTMYYGG_YYGGMC")+"、");
					}else{
						if(cptmyyggDynaBean!=null){
							cptmyyggDynaBean.set("JGMES_BASE_CPTMYYGG_ID", JEUUID.uuid());
							cptmyyggDynaBean.set("CPTMYYGG_YYGZBH", serviceTemplate.buildCode("CPTMYYGG_YYGZBH", "JGMES_BASE_CPTMYYGG", cptmyyggDynaBean));

							//模板条码应用规则中产品数据应该是空的
							cptmyyggDynaBean.set("CPTMYYGG_CPBH",cpDynaBean.getStr("PRODUCTDATA_BH"));
							cptmyyggDynaBean.set("CPTMYYGG_CPNAME",cpDynaBean.getStr("PRODUCTDATA_NAME"));
							cptmyyggDynaBean.set("JGMES_BASE_PRODUCTDATA_ID",cpDynaBean.getStr("JGMES_BASE_PRODUCTDATA_ID"));

							//设置模板
							DynaBean dic = jgmesCommon.getDic("JGMES_YES_NO","0");
							if(dic!=null){
								cptmyyggDynaBean.set("CPTMYYGG_NO_CODE",dic.get("DICTIONARYITEM_ITEMCODE"));
								cptmyyggDynaBean.set("CPTMYYGG_NO_NAME",dic.get("DICTIONARYITEM_ITEMNAME"));
							}

							serviceTemplate.insert(cptmyyggDynaBean);
						}
					}
				}
				if(yyggmc.length()>0){
					return "\""+yyggmc.toString()+":该产品条码应用规则已经有了\"";
				}
			}else{
				return "\"请检查产品数据是否保存！\"";
			}
		}else{
			return "\"请选择一条数据！\"";
		}
		return "\"产品条码应用规则导入成功！\"";
	}

	@Override
	public String doSavehgxq(HttpServletRequest request,String id,String[] zbidList) {
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
		SimpleDateFormat sdf =   new SimpleDateFormat( "yyyy-MM-dd" );
		DynaBean hgapzbDynaBean = null;
		String ddhm = null;
		if(id!=null&&!"".equals(id)){
			hgapzbDynaBean = serviceTemplate.selectOne("JGMES_PB_HGAPZB"," and JGMES_PB_HGAPZB_ID = '"+id+"'");
		}else{
			List<DynaBean> list = serviceTemplate.selectList("JGMES_PB_HGAPZB"," order by SY_CREATETIME desc",0,1);
			if(list!=null&&list.size()>0){
				hgapzbDynaBean = list.get(0);
			}
		}
		String message = "";
		if(hgapzbDynaBean!=null){
			ddhm = hgapzbDynaBean.getStr("HGAPZB_DDHM");
			int sl = hgapzbDynaBean.getInt("HGAPZB_ZZSL");
			if(sl==0) {
				return "\"货柜的装栈数量为0\"";
			}
			List<DynaBean> hgapzbxqDynaBeanList = serviceTemplate.selectList("JGMES_PB_HGAPZBXQ", " and HGAPZBXQ_HGAPZBZJID = '"+hgapzbDynaBean.getStr("JGMES_PB_HGAPZB_ID")+"'");
			if(hgapzbxqDynaBeanList!=null&&hgapzbxqDynaBeanList.size()>0) {
				sl = sl-hgapzbxqDynaBeanList.size();
			}

			//获取栈板条码
			List<DynaBean> zbtmList = null;
			if(zbidList!=null&&zbidList.length>0){
				for(int i=0;i<zbidList.length;i++) {
					if(i>=sl) {
						message =  "\"栈板数量不能超过装栈数量！\"";
						break;
					}
					DynaBean zbtm = serviceTemplate.selectOne("JGMES_BASE_GDCPTM"," and JGMES_BASE_GDCPTM_ID = '"+zbidList[i]+"'");
					DynaBean hgapzbxqDynaBean = new DynaBean();
					hgapzbxqDynaBean.set("JGMES_PB_HGAPZBXQ_ID",JEUUID.uuid());
					hgapzbxqDynaBean.set(BeanUtils.KEY_TABLE_CODE,"JGMES_PB_HGAPZBXQ");
					//日期
					hgapzbxqDynaBean.set("HGAPZBXQ_RQ",hgapzbDynaBean.getStr("HGAPZB_RQ"));
					//发向地
					hgapzbxqDynaBean.set("HGAPZBXQ_FXD",hgapzbDynaBean.getStr("HGAPZB_FXD"));
					//货柜编号
					hgapzbxqDynaBean.set("HGAPZBXQ_HGBH",hgapzbDynaBean.getStr("HGAPZB_HGBH"));
					//货柜安排子表主键ID
					hgapzbxqDynaBean.set("HGAPZBXQ_HGAPZBZJID",hgapzbDynaBean.getStr("JGMES_PB_HGAPZB_ID"));
					//栈板编号
					hgapzbxqDynaBean.set("HGAPZBXQ_ZBTMH",zbtm.getStr("GDCPTM_TMH"));
					//订单号
					hgapzbxqDynaBean.set("HGAPZBXQ_DDHM",zbtm.getStr("GDCPTM_DDHM"));
					//内部订单号
					hgapzbxqDynaBean.set("HGAPZBXQ_LCKH", zbtm.getStr("GDCPTM_LCKH"));

					jgmesCommon.setDynaBeanInfo(hgapzbxqDynaBean);

					serviceTemplate.insert(hgapzbxqDynaBean);
				}
			}else{

				//经过任务单过滤的
				String sql = "SELECT * FROM JGMES_BASE_GDCPTM " +
						"WHERE GDCPTM_TMLX_CODE = 'TMLX05' " +
						"AND GDCPTM_DDHM = '"+ddhm+"'  " +
						"AND ( " +
						"SELECT " +
						"count(*) " +
						"FROM " +
						"JGMES_PB_HGAPZBXQ " +
						"WHERE " +
						"JGMES_BASE_GDCPTM.GDCPTM_TMH = JGMES_PB_HGAPZBXQ.HGAPZBXQ_ZBTMH " +
						") = 0 " +
						"and " +
						"(" +
						"select (case JGMES_XTGL_XTCS.XTCS_CSZ " +
						"when  1  then (select ZBBD_NO_CODE from GMES_PB_ZBBD where JGMES_BASE_GDCPTM.GDCPTM_TMH = GMES_PB_ZBBD.ZBBD_ZBTMH limit 1) " +
						"when  2  then 1 " +
						"else  1 " +
						"end) from JGMES_XTGL_XTCS where XTCS_CXFL1_CODE = 'CXFL06' and XTCS_CXFL2_CODE = 'HGAPZBLX' " +
						")=1   order by GDCPTM_TMH";


				zbtmList = serviceTemplate.selectListBySql(sql);

				//不经过任务单过滤的
				String sql2 = "SELECT * FROM JGMES_BASE_GDCPTM " +
						"WHERE GDCPTM_TMLX_CODE = 'TMLX05' " +
						"AND ( " +
						"SELECT " +
						"count(*) " +
						"FROM " +
						"JGMES_PB_HGAPZBXQ " +
						"WHERE " +
						"JGMES_BASE_GDCPTM.GDCPTM_TMH = JGMES_PB_HGAPZBXQ.HGAPZBXQ_ZBTMH " +
						") = 0 " +
						"and " +
						"(" +
						"select (case JGMES_XTGL_XTCS.XTCS_CSZ " +
						"when  1  then (select ZBBD_NO_CODE from GMES_PB_ZBBD where JGMES_BASE_GDCPTM.GDCPTM_TMH = GMES_PB_ZBBD.ZBBD_ZBTMH limit 1) " +
						"when  2  then 1 " +
						"else  1 " +
						"end) from JGMES_XTGL_XTCS where XTCS_CXFL1_CODE = 'CXFL06' and XTCS_CXFL2_CODE = 'HGAPZBLX' " +
						")=1   order by GDCPTM_TMH";


				List<DynaBean> zbtmList1 = serviceTemplate.selectListBySql(sql2);
				int j = 0;
				if(zbtmList!=null&&zbtmList.size()>0){
					for(int i=0;i<sl;i++){
						if(i>=sl) {
							message =  "\"栈板数量不能超过装栈数量！\"";
							break;
						}
						DynaBean zbtm = null;
						if(i<zbtmList.size()){
							zbtm = zbtmList.get(i);
						}else{
							if(zbtmList1!=null&&zbtmList1.size()>0){
								zbtm  = zbtmList1.get(j);
								j++;
							}else{
								break;
							}

						}
						DynaBean hgapzbxqDynaBean = new DynaBean();
						hgapzbxqDynaBean.set("JGMES_PB_HGAPZBXQ_ID",JEUUID.uuid());
						hgapzbxqDynaBean.set(BeanUtils.KEY_TABLE_CODE,"JGMES_PB_HGAPZBXQ");
						//日期
						hgapzbxqDynaBean.set("HGAPZBXQ_RQ",hgapzbDynaBean.getStr("HGAPZB_RQ"));
						//发向地
						hgapzbxqDynaBean.set("HGAPZBXQ_FXD",hgapzbDynaBean.getStr("HGAPZB_FXD"));
						//货柜编号
						hgapzbxqDynaBean.set("HGAPZBXQ_HGBH",hgapzbDynaBean.getStr("HGAPZB_HGBH"));
						//货柜安排子表主键ID
						hgapzbxqDynaBean.set("HGAPZBXQ_HGAPZBZJID",hgapzbDynaBean.getStr("JGMES_PB_HGAPZB_ID"));
						//栈板编号
						hgapzbxqDynaBean.set("HGAPZBXQ_ZBTMH",zbtm.getStr("GDCPTM_TMH"));
						//订单号
						hgapzbxqDynaBean.set("HGAPZBXQ_DDHM",zbtm.getStr("GDCPTM_DDHM"));
						//内部订单号
						hgapzbxqDynaBean.set("HGAPZBXQ_LCKH", zbtm.getStr("GDCPTM_LCKH"));

						jgmesCommon.setDynaBeanInfo(hgapzbxqDynaBean);

						serviceTemplate.insert(hgapzbxqDynaBean);
					}
				}
			}



			
			//回写货柜安排子表的已安排数量
			hgapzbxqDynaBeanList = serviceTemplate.selectList("JGMES_PB_HGAPZBXQ", " and HGAPZBXQ_HGAPZBZJID = '"+hgapzbDynaBean.getStr("JGMES_PB_HGAPZB_ID")+"'");
			if(hgapzbxqDynaBeanList!=null&&hgapzbxqDynaBeanList.size()>0) {
				hgapzbDynaBean.set("HGAPZB_YAPSL", hgapzbxqDynaBeanList.size());
			}else {
				hgapzbDynaBean.set("HGAPZB_YAPSL", 0);
			}
			serviceTemplate.update(hgapzbDynaBean);
		}
		if(message!=null&&!"".equals(message)) {
			return message;
		}else {
			return "\"栈板添加成功！\"";
		}
	}

	@Override
	public String doDeletehgxq(String[] id) {
		if(id!=null&&id.length>0){
			String ids = String.join("','",id);
			serviceTemplate.deleteByWehreSql("JGMES_PB_HGAPZBXQ"," and HGAPZBXQ_HGAPZBZJID = ('"+ids+"')");
		}
		return "\"删除成功！\"";
	}


	/**
	 * 获取登录用户
	 * @return
	 */
	public EndUser getCurrentUser() {
		// TODO Auto-generated method stub
		return SecurityUserHolder.getCurrentUser();
	}
	/**
	 * 获取登录用户所在部门
	 * @return
	 */
	public Department getCurrentDept() {
		// TODO Auto-generated method stub
		return SecurityUserHolder.getCurrentUserDept();
	}
	@Resource(name="PCDynaServiceTemplate")
	public void setServiceTemplate(PCDynaServiceTemplate serviceTemplate) {
		this.serviceTemplate = serviceTemplate;
	}
	@Resource(name="PCServiceTemplateImpl")
	public void setPcServiceTemplate(PCServiceTemplate pcServiceTemplate) {
		this.pcServiceTemplate = pcServiceTemplate;
	}
	@Resource(name="userManager")
	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}
}