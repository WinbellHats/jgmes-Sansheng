package com.demo.service;

import com.je.core.facade.extjs.JsonBuilder;
import com.je.core.service.PCDynaServiceTemplate;
import com.je.core.service.PCServiceTemplate;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.BeanUtils;
import com.je.core.util.bean.DynaBean;
import com.je.phone.vo.DsInfoVo;
import com.je.task.vo.TimedTaskParamsVo;
import com.je.wf.processVo.WfAssgineSubmitInfo;
import com.je.wf.processVo.WfEventSubmitInfo;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("lessionService")
public class LessionServiceImpl implements LessionService {
	private static final Logger logger = LoggerFactory.getLogger(LessionServiceImpl.class);
	private PCDynaServiceTemplate serviceTemplate;
	private PCServiceTemplate pcServiceTemplate;

	@Override
	public String loadWfNum(DsInfoVo dsInfoVo) {
		long allCount = serviceTemplate.selectCount("JE_CORE_PROCESSLOG", "");
		long wfCount = serviceTemplate.selectCount("JE_CORE_VPROCESSINSTANCE", "");
		JSONObject returnObj = new JSONObject();
		returnObj.put("allCount", allCount);
		returnObj.put("wfCount", wfCount);
		return returnObj.toString();
	}

	@Override
	public void doTask() {
		// TODO Auto-generated method stub
		System.out.println("无参定时任务执行");
	}

	@Override
	public void doTask(TimedTaskParamsVo paramsVo) {
		// TODO Auto-generated method stub
		Map<String, String> params = paramsVo.getParams();
		String arg0 = params.get("arg0");
		System.out.println("有参定时任务执行");
	}

	@Override
	public void doWfProcessEvent(WfEventSubmitInfo eventInfo) {
		// TODO Auto-generated method stub
		// 获取当前业务表数据
		DynaBean dynaBean = eventInfo.getDynaBean();
		System.out.println("流程事件已执行..");
	}

	@Override
	public void doWfTaskEvent(WfEventSubmitInfo eventInfo) {
		// TODO Auto-generated method stub
		// 获取当前业务表数据

		DynaBean dynaBean = eventInfo.getDynaBean();
		System.out.println("任务动作事件已执行..");

	}

	@Override
	public List<DynaBean> doWfAssigneEvent(WfAssgineSubmitInfo eventInfo) {
		// TODO Auto-generated method stub
		// 指定的用户
		List<DynaBean> users = serviceTemplate.selectList("JE_CORE_ENDUSER", " AND USERCODE='admin'");
		// 如果不查询则指定 用户三个属性即可 USERNAME USERCODE USERID
		List<DynaBean> us = new ArrayList<DynaBean>();
		DynaBean u = new DynaBean("JE_CORE_ENDUSER", true);
		u.set("USERID", "");
		u.set("USERCODE", "");
		u.set("USERNAME", "");
		us.add(u);// 返回us也可以实现
		System.out.println("动态获取人员时间已执行..");
		return users;
	}

	@Resource(name = "PCDynaServiceTemplate")
	public void setServiceTemplate(PCDynaServiceTemplate serviceTemplate) {
		this.serviceTemplate = serviceTemplate;
	}

	@Resource(name = "PCServiceTemplateImpl")
	public void setPcServiceTemplate(PCServiceTemplate pcServiceTemplate) {
		this.pcServiceTemplate = pcServiceTemplate;
	}

	@Override
	public void doDynaBean() {
		// TODO Auto-generated method stub
		DynaBean dynaBean = new DynaBean("表名", true);
		// 获取表名
		String tableCode = dynaBean.getStr(BeanUtils.KEY_TABLE_CODE);
		// 获取主键名
		String pkCode = dynaBean.getStr(BeanUtils.KEY_PK_CODE);
		// 根据表名获取主键名
		BeanUtils beanUtils = BeanUtils.getInstance();
		pkCode = beanUtils.getPKeyFieldNames(tableCode);
		// 获取表结构对象
		DynaBean tableInfo = beanUtils.getResourceTable(tableCode);
		// 获取列集合
		List<DynaBean> columns = (List<DynaBean>) tableInfo.get(BeanUtils.KEY_TABLE_COLUMNS);
		// 设定字段值
		dynaBean.set("字段名", "字段值");
		// 获取指定类型的字段值
		int intVal = dynaBean.getInt("字段", 0);
		String stringVal = dynaBean.getStr("字段", "");
		double doubleVal = dynaBean.getDouble("字段", 0);
		// 获取表数据集合(该方法会删除指定表名等信息)
		HashMap values = dynaBean.getValues();
		// 获取重新构建表信息
		dynaBean.set(BeanUtils.KEY_TABLE_CODE, "表名");
		// 构建单dynaBean对象的json字符串
		JsonBuilder jsonBuilder = JsonBuilder.getInstance();
		jsonBuilder.toJson(dynaBean);
		// 构建集合dynaBean对象的json字符串
		List<DynaBean> beans = new ArrayList<DynaBean>();
		jsonBuilder.buildListPageJson(new Long(beans.size()), beans, true);// true为{totalCount:总数量,rows:[]} false直返数据[]
	}

	@Override
	public void doSelectDynaBean() {
		// TODO Auto-generated method stub
		String tableCode = "";
		// 查询条数
		serviceTemplate.selectCount(tableCode, " and 1=1 ");
		// 按照主键查数据
		serviceTemplate.selectOneByPk(tableCode, "主键值");
		serviceTemplate.selectOneByPk(tableCode, "主键值", "字段1,字段2");
		// 查询单条数据 如果出现多条返回null
		serviceTemplate.selectOne(tableCode, " AND 1=1");
		serviceTemplate.selectOne(tableCode, " AND 1=1", "字段1,字段2");
		// 查询集合数据
		serviceTemplate.selectList(tableCode, " AND 1=1");
		serviceTemplate.selectList(tableCode, " AND 1=1", "字段1,字段2");
		// 分页查集合数据
		serviceTemplate.selectList(tableCode, " AND 1=1", 1, 30);
		serviceTemplate.selectList(tableCode, " AND 1=1", "字段1,字段2", 1, 30);

	}

	@Override
	public void doSaveDynaBean() {
		// TODO Auto-generated method stub
		String tableCode = "";
		DynaBean dynaBean = new DynaBean(tableCode, true);
		dynaBean.set("字段A", "字段值");
		dynaBean.set("字段B", "字段值");
		// 设定主键值 如果未设定系统自动uuid，如果设定则按照设定的主键值
		dynaBean.set("主键字段", "主键值");
		// 构建创建信息
		serviceTemplate.buildModelCreateInfo(dynaBean);
		dynaBean = serviceTemplate.insert(dynaBean);
	}

	@Override
	public void doUpdateDynaBean() {
		// TODO Auto-generated method stub
		String tableCode = "";
		DynaBean dynaBean = serviceTemplate.selectOne(tableCode, " AND 1=1");
		dynaBean.set("字段1", "字段值");
		dynaBean.set("字段2", "字段值");
		// 删除字段key，实现局部更新
		dynaBean.remove("字段名");
		serviceTemplate.buildModelModifyInfo(dynaBean);
		serviceTemplate.update(dynaBean);
	}

	@Override
	public void doRemoveDynaBean() {
		// TODO Auto-generated method stub
		// 删除记录
		String tableCode = "";
		serviceTemplate.deleteByIds("主键值,主键值2", tableCode, "主键名");
		// 按照条件删除
		serviceTemplate.deleteByWehreSql(tableCode, " AND 1=1");
		// 使用纯SQL删除
		pcServiceTemplate.executeSql(" DELETE FROM 表名 where 1=1");
	}

	@Override
	public DynaBean implOrder(DynaBean order) {
		// TODO Auto-generated method stub
		JSONObject params = (JSONObject) order.get("params");
		// DataImplAction
		// DataImplManagerImpl
		String bm = order.getStr("CPBZ_CPMC");
		List<DynaBean> list = serviceTemplate.selectList("JGMES_ZLGL_CPBZ", " and CPBZ_CPMC='" + bm + "'");
		if (list.size() > 0) {
			return order;
		} else {
			order = serviceTemplate.insert(order);
			return order;
		}
	}
    
	/**
	 * 物料资料导入
	 */
	@Override
	public DynaBean implwlOrder(DynaBean order) {
		//获取系统参数来判断是否进行产品名称唯一校验
		DynaBean isVerifyingNames=serviceTemplate.selectOne("JGMES_XTGL_XTCS"," and XTCS_CXFL2_CODE='IsVerifyingNames'");
		boolean isVerName=true;
		if(isVerifyingNames!=null&&"0".equals(isVerifyingNames.getStr("XTCS_CSZ"))) {
			logger.debug("不校验产品名称");
			isVerName=false;
		}
		StringBuilder errorMessage=new StringBuilder(1000);//错误信息记录
		boolean toerror = false;
		if (order==null){
			errorMessage.append("没有要导入的资料！");
			toerror = true;
		}
		try {
			//物料属性校验
			String wlsx = order.getStr("PRODUCTDATA_WLTYPE_CODE");
			String wlsxMc = "";
			//swicth
			if (StringUtil.isNotEmpty(wlsx)){
				switch (wlsx.trim()){
					case "CP":
						wlsxMc = "成品";
						break;
					case "WL":
						wlsxMc = "材料";
						break;
					case "BCP":
						wlsxMc = "半成品";
						break;
					case "成品":
						wlsx="CP";
						wlsxMc = "成品";
						break;
					case "材料":
						wlsx="WL";
						wlsxMc = "材料";
						break;
					case "半成品":
						wlsx="BCP";
						wlsxMc = "半成品";
						break;
					default:
						logger.debug("物料属性填写错误，可选填写为成品，材料。");
						errorMessage.append("物料属性填写错误，可选填写为成品，材料。;");
						toerror = true;
				}
				order.setStr("PRODUCTDATA_WLTYPE_CODE",wlsx);
				order.setStr("PRODUCTDATA_WLTYPE_NAME",wlsxMc);
			}

			//判断产品编号是否重复
//			String bm = order.getStr("PRODUCTDATA_BH"," ").trim();//产品编号
			String bm = order.getStr("PRODUCTDATA_BH"," ").trim();//乐惠需要把规格当成编号使用
			String mc = order.getStr("PRODUCTDATA_NAME");
			if("".equals(mc)||mc==null||mc.isEmpty()) {
				logger.debug("产品名称为空。");
				errorMessage.append("产品名称为空;");
				toerror = true;
			}else if (bm==null||bm.isEmpty()||" ".equals(bm)) {
				logger.debug("产品编号为空。");
				errorMessage.append("产品编号为空;");
				toerror = true;
			}else {
				//判断已导入的数据中是否含有相同的产品编号和产品名称
				if(order.get("importDataList")!=null){
					List<DynaBean> importDataList = (List<DynaBean>) order.get("importDataList");
					if (importDataList.size() != 0) {
						for (DynaBean dynaBean : importDataList) {
							if (dynaBean.getStr("PRODUCTDATA_BH").equals(bm)){
								String xh1 = dynaBean.getStr("rownumberer_1");
								if (xh1==null) {
									xh1=" ;"+bm;
								}
								errorMessage.append("与序号："+xh1+"的产品编号重复;");
								toerror=true;
							}else if(isVerName&&dynaBean.getStr("PRODUCTDATA_NAME").toLowerCase().equals(mc.toLowerCase())){
								String xh1 = dynaBean.getStr("rownumberer_1");
								if (xh1==null) {
									xh1=" ;"+bm;
								}
								errorMessage.append("序号："+xh1+"的产品名称重复;");
								toerror=true;
							}
						}
					}
				}

				List<DynaBean> list = serviceTemplate.selectList("JGMES_BASE_PRODUCTDATA", " and PRODUCTDATA_BH='" + bm + "'");
				if (list.size() > 0) {
					logger.debug("产品编号重复。→"+bm);
					errorMessage.append("产品编号"+bm+"已存在资料表中;");
					toerror = true;
				}else if(isVerName){
					List<DynaBean> mcList = serviceTemplate.selectList("JGMES_BASE_PRODUCTDATA", " and PRODUCTDATA_NAME='" + mc + "'");
					if (mcList.size()>0) {
						logger.debug("产品名称重复。→"+mc);
						errorMessage.append("产品名称"+mc+"已存在资料表中;");
						toerror = true;
					}
				}
			}
			//绑定工艺路线
			String gylxbh=order.getStr("PRODUCTDATA_CPGYLX").trim();
			if (gylxbh!=null&&!gylxbh.isEmpty()&&!"".equals(gylxbh)) {
				DynaBean gyDynaBean = serviceTemplate.selectOne("JGMES_GYGL_GYLX"," and GYLX_GYLXNUM='"+gylxbh+"' and GYLX_STATUS='1'");
				if (gyDynaBean==null) {
					logger.debug("工艺路线信息获取不到，或工艺路线没有启用的。→"+gylxbh);
					errorMessage.append("工艺路线编号不存在，或没有启用的工艺路线;");
					toerror = true;
				}else {
					order.set("PRODUCTDATA_CPGYLXID", gyDynaBean.getStr("GYLX_ID"));
					order.set("PRODUCTDATA_CPGYLX", gyDynaBean.getStr("GYLX_GYLXNAME"));
				}
			}
		} catch (Exception e) {
			//截取错误内容，错误类不要
			String[] mess = e.getMessage().split(":");
			if (mess.length>1) {
				errorMessage.append(mess[0]+":"+mess[mess.length-1]);
			}else {
				errorMessage.append(mess[0]);
			}
			toerror = true;
			e.printStackTrace();
		}
		if (toerror) {
			String serial=order.getStr("rownumberer_1");
			if (serial==null) {
				serial=" ";
			}
			order.set("error", "序号:"+serial+":"+errorMessage.toString()+"</br>");
		}
		
		return order;
		
	}

    //此工单导入停用，已移到com/demo/service/WorkorderServiceImpl
	@Override
	public DynaBean implgdOrder(DynaBean order) {
		String bm = order.getStr("GDLB_DDHM").trim();
		System.out.println("1");
		String cpbm = order.getStr("GDLB_CPBH").trim();
		List<DynaBean> cplist = serviceTemplate.selectList("JGMES_BASE_PRODUCTDATA",
				" and PRODUCTDATA_BH='" + cpbm + "'");
		List<DynaBean> list = serviceTemplate.selectList("JGMES_PLAN_GDLB", " and GDLB_DDHM='" + bm + "'");
		System.out.println("2");
		order.set("GDLB_GDZT_NAME", "未完");
		order.set("GDLB_GDZT_CODE", 1);
		
		//System.out.println("gdhm:"+order.getStr("GDLB_GDHM"));
		
		//order.set("GDLB_GDHM", bm);
		// order.set("GDLB_WPCSL", order.getDouble("GDLB_XPCSL") 
		// order.getDouble("GDLB_YPCSL"));
		if (list.size() > 0) {
			return order;
		} else {
			System.out.println("3");

			if (cplist.size() > 0) {
				System.out.println("4");
				order.set("GDLB_GYLXID", cplist.get(0).get("PRODUCTDATA_CPGYLXID"));
			}
			System.out.println("5");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			try {
				String date = sdf.format(sdf.parse(order.getStr("GDLB_OTDRQ")));
				//System.out.println(date);
				order.set("GDLB_OTDRQ", date);
				order.set("GDLB_DDJHRQ", date);
				date = sdf.format(sdf.parse(order.getStr("GDLB_QGRQ")));
				//System.out.println(date);
				order.set("GDLB_QGRQ", date);
				date = sdf.format(sdf.parse(order.getStr("GDLB_RQ")));
				//System.out.println(date);
				order.set("GDLB_RQ", date);
				
				order = serviceTemplate.insert(order);
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return order;
		}
	}
	
	
	
}
