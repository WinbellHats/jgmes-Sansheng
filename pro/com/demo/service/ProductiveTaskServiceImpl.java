package com.demo.service;

import com.je.core.security.SecurityUserHolder;
import com.je.core.service.PCDynaServiceTemplate;
import com.je.core.service.PCServiceTemplate;
import com.je.core.util.JEUUID;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.BeanUtils;
import com.je.core.util.bean.DynaBean;
import com.je.rbac.model.Department;
import com.je.rbac.model.EndUser;
import com.je.rbac.service.UserManager;
import com.je.table.exception.PCExcuteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

//import org.apache.log4j.Logger;
/**
 * 
 * @author cj
 * @version 2019-04-02 10:58:28
 */
@Component("productiveTaskService")
public class ProductiveTaskServiceImpl implements ProductiveTaskService  {
	
	private static final Logger logger = LoggerFactory.getLogger(ProductiveTaskServiceImpl.class);

	/**��̬Bean(DynaBean)�ķ����*/
	private PCDynaServiceTemplate serviceTemplate;
	/**ʵ��Bean���������,��Ҫ����SQL*/
	private PCServiceTemplate pcServiceTemplate;
	/**�û������*/
	private UserManager userManager;
	
	public void load(){
		System.out.println("hello serviceimpl");
	}
	
	
	/**
	 * ��ȡ��¼�û�
	 * @return
	 */
	public EndUser getCurrentUser() {
		// TODO Auto-generated method stub
		return SecurityUserHolder.getCurrentUser();
	}
	/**
	 * ��ȡ��¼�û����ڲ���
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
	
	
	/**
	 * 生产任务导入
	 *  @param order 生产任务数据
	 * 
	 */

	@Override
	public DynaBean implSCRWOrder(DynaBean order) {
		logger.debug("导入生产任务前：");
		StringBuilder errorMessage=new StringBuilder(2000);//错误信息记录
		boolean toerror = false;
		if(order==null) {
			logger.debug("这一行返回空值。");
			errorMessage.append("数据不存在;");
			toerror = true;
		}else {
		//  去掉关键字段内容两边的空白部分 订单号码，工单号码，产线编号，生产任务单号,客户编号
			try {
				order.set("SCRW_DDHM", order.getStr("SCRW_DDHM").trim());//订单号码
				order.set("SCRW_GDHM", order.getStr("SCRW_GDHM").trim());//工单号码
				order.set("SCRW_CXBM", order.getStr("SCRW_CXBM").trim());//产线编号
				order.set("SCRW_RWDH", order.getStr("SCRW_RWDH").trim());//任务单号
				order.set("SCRW_KHBM", order.getStr("SCRW_KHBM").trim());//客户编号
				order.set("SCRW_ERPGX", order.getStr("SCRW_ERPGX").trim());//产品编号
			} catch (Exception e) {
				errorMessage.append("重要数据填写错误请检查："+e.getMessage()+"");
				toerror = true;
			}
			//判断产品信息是否完整，根据 产品编号SCRW_CPBH
			String cpbh = order.getStr("SCRW_CPBH");
			logger.debug("当前产品编号："+cpbh);
			try {
				DynaBean cpDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA"," and PRODUCTDATA_BH='"+cpbh+"'");
				//判断编号
				if (cpbh==null||cpbh.isEmpty()||cpDynaBean==null) {
					errorMessage.append("产品信息"+cpbh+"不存在;");
					toerror = true;
				}else {
					order.set("SCRW_CPBH", cpDynaBean.getStr("PRODUCTDATA_BH"));//写入基础资料中的产品编号
					//查询产品名称    并校验，产品名称为null或与产品表不匹配时
					if (cpDynaBean.getStr("PRODUCTDATA_NAME")==null||cpDynaBean.getStr("PRODUCTDATA_NAME").isEmpty()) {
						errorMessage.append("产品编号"+cpbh+"在产品表中名称为空;");
						toerror = true;
					}else if(order.getStr("SCRW_NAME")==null||!order.getStr("SCRW_NAME").equals(cpDynaBean.getStr("PRODUCTDATA_NAME"))) {
						order.set("SCRW_NAME", cpDynaBean.getStr("PRODUCTDATA_NAME"));
					}
					
					//查询产品规格
					if(order.getStr("SCRW_CPGG")==null||!order.getStr("SCRW_CPGG").equals(cpDynaBean.getStr("PRODUCTDATA_GG"))) {
						order.set("SCRW_CPGG", cpDynaBean.getStr("PRODUCTDATA_GG"));
					}
					
					//查询产品型号
					if(order.getStr("SCRW_CPXH")==null||!order.getStr("SCRW_CPXH").equals(cpDynaBean.getStr("PRODUCTDATA_XH"))) {
						order.set("SCRW_CPXH", cpDynaBean.getStr("PRODUCTDATA_XH"));
					}
					
					//判断工艺路线ID
					String gylxID=cpDynaBean.getStr("PRODUCTDATA_CPGYLXID");//工艺路线ID
					DynaBean gylxDynaBean = serviceTemplate.selectOne("JGMES_GYGL_GYLX"," and GYLX_ID='"+gylxID+"'");
					if (gylxID==null||gylxDynaBean==null) {
						logger.debug("工艺路线获取失败。"+gylxID);
						errorMessage.append("产品工艺路线信息不存在;");
						toerror = true;
					}
				}	
			} catch (Exception e) {
				errorMessage.append("未知异常，请确认产品编号是否正确。");
				toerror = true;
			}
			
			
			//判断产线信息完整性
			String cxbm = order.getStr("SCRW_CXBM");
			try {
				DynaBean cxDynaBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ"," and CXSJ_STATUS_CODE='1' and CXSJ_CXBM='"+cxbm+"'");
				if (cxbm==null||cxbm.isEmpty()||cxDynaBean==null) {
					errorMessage.append("产线信息:"+cxbm+"不存在或未启用;");
					toerror = true;
				}else {
					order.set("SCRW_CXBM", cxDynaBean.getStr("CXSJ_CXBM"));//写入基础资料中的产线编号
					//查询写入产线名称
					if(order.getStr("SCRW_CXMC")==null||!order.getStr("SCRW_CXMC").equals(cxDynaBean.getStr("CXSJ_CXMC"))) {
						order.set("SCRW_CXMC", cxDynaBean.getStr("CXSJ_CXMC"));
					}
				}
			} catch (Exception e) {
				errorMessage.append("未知异常，请确认产线编号是否正确。");
				toerror = true;
			}
			
			//判断订单号码是否填写
			if (order.getStr("SCRW_DDHM")==null||order.getStr("SCRW_DDHM").isEmpty()) {
				errorMessage.append("订单号码:"+order.getStr("SCRW_DDHM")+"不存在;");
				toerror = true;
			}
			//判断工单号码是否填写
			if (order.getStr("SCRW_GDHM")==null||order.getStr("SCRW_GDHM").isEmpty()) {
				errorMessage.append("工单号码:"+order.getStr("SCRW_GDHM")+"不存在;");
				toerror = true;
			}
			//判断客户编号是否填写 有则校验
			String khbm = order.getStr("SCRW_KHBM");
			if (khbm!=null&&!khbm.isEmpty()) {
				try {
					DynaBean khDynaBean = serviceTemplate.selectOne("JGMES_BASE_CUST"," and CUST_CODE='"+khbm+"'");
					if (khDynaBean!=null) {
						order.set("SCRW_KHBM", khDynaBean.getStr("CUST_CODE"));//写入基础资料中的客户编号
						//写入客户名称
						if(order.getStr("SCRW_KHMC")==null||!order.getStr("SCRW_KHMC").equals(khDynaBean.getStr("CUST_NAME"))) {
							order.set("SCRW_KHMC", khDynaBean.getStr("CUST_NAME"));
						}
					}	
				} catch (Exception e) {
					errorMessage.append("未知异常，请确认客户编号是否正确。");
					toerror = true;
				}
			}
			
			//模具信息判断 有则校验
			String mjbh = order.getStr("SCRW_MJBH");
			if (mjbh!=null&&!mjbh.isEmpty()) {
				try {
					DynaBean mjDynaBean = serviceTemplate.selectOne("JGMES_BASE_MJ"," and MJ_BH='"+mjbh+"' and MJ_STATUS_CODE='1'");
					if (mjDynaBean!=null) {
						order.set("SCRW_MJBH", mjDynaBean.getStr("MJ_BH"));//写入基础资料中的模具编号
					}
				} catch (Exception e) {
					errorMessage.append("未知异常，请确认模具编号是否正确。");
					toerror = true;
				}
			}
			
			//判断任务单号是否已存在
			List<DynaBean> rwdhDynaBean=null;
			String rwdh = order.getStr("SCRW_RWDH");
			if (rwdh==null||rwdh.isEmpty()) {
				errorMessage.append("任务单号:"+rwdh+"获取不到;");
				toerror = true;
			}else {
				try {
					//判断已导入的数据中是否含有相同的任务单号
					if (order.get("importDataList")!=null){
						List<DynaBean> importDataList = (List<DynaBean>) order.get("importDataList");
						if (importDataList.size() != 0) {
							for (DynaBean dynaBean : importDataList) {
								if (dynaBean.getStr("SCRW_RWDH").equals(rwdh)){
									String xh1 = dynaBean.getStr("rownumberer_1");
									if (xh1==null) {
										xh1="未知;"+rwdh;
									}
									errorMessage.append("与序号："+xh1+":任务单号重复;");
									toerror=true;
								}
							}
						}
					}
					rwdhDynaBean= serviceTemplate.selectList("JGMES_PLAN_SCRW"," and SCRW_RWDH='"+rwdh+"'");
					if (rwdhDynaBean.size()>=1) {
						errorMessage.append("任务单号:"+rwdh+"已存在;");
						toerror = true;
					}
				} catch (Exception e) {
					errorMessage.append("未知异常，请确认任务单号是否正确。");
					toerror = true;
				}
			}
			
			//验证数量字段的合法性
			try {
				int gdsl =order.getInt("SCRW_GDSL");//工单数量
				if (gdsl<=0) {
					errorMessage.append("工单数量:"+gdsl+"异常;");
					toerror = true;
				}
				int pcsl = order.getInt("SCRW_PCSL");//本次加入的排产数量
				if (pcsl<=0) {
					errorMessage.append("排产数量:"+pcsl+"异常;");
					toerror = true;
				}
				if (pcsl>gdsl) {
					errorMessage.append("排产数量大于工单数量！");
					toerror=true;
				}
			} catch (Exception e) {
				errorMessage.append("数量:"+e.getMessage()+"异常;");
				toerror = true;
			}
			
			try {
				
				//添加生产任务默认数据
				updateSCRW(order);
				//同一条产线同一天顺序号不能重复。
				String pcxh = order.getStr("SY_ORDERINDEX");
				if(pcxh==null||pcxh.isEmpty()||Integer.parseInt(pcxh)<0) {
					errorMessage.append("排产序号:"+pcxh+"为无效数据;");
					toerror = true;
				}else {
					String pcrq = order.getStr("SCRW_PCRQ");
					//判断已导入的数据中是否含有相同的排产序号,（同一天，同一产线）
					if (order.get("importDataList")!=null){
						List<DynaBean> importDataList = (List<DynaBean>) order.get("importDataList");
						if (importDataList.size() != 0) {
							for (DynaBean dynaBean : importDataList) {
								if (dynaBean.getStr("SY_ORDERINDEX").equals(pcxh)&&dynaBean.getStr("SCRW_CXBM").equals(cxbm)){
									SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
									String sourceRP =dynaBean.getStr("SCRW_PCRQ");
									System.out.println("对比日期："+pcrq+":"+sourceRP);
									logger.debug("对比日期："+pcrq+":"+sourceRP);
									if (sourceRP.equals(pcrq)) {
										String xh1 = dynaBean.getStr("rownumberer_1");
										if (xh1==null) {
											xh1=" ;"+pcxh;
										}
										errorMessage.append("与序号："+xh1+"：排产排序重复(同一天，同一产线);");
										toerror=true;
									}
								}
							}
						}
					}
					//判断数据库数据是否有存在此序号
					rwdhDynaBean = serviceTemplate.selectList("JGMES_PLAN_SCRW"," and SY_ORDERINDEX='"+pcxh+"'"
									+ " and SCRW_CXBM='"+cxbm+"' and SCRW_PCRQ='"+pcrq+"'");
					if (rwdhDynaBean.size()>=1) {
						errorMessage.append("中排产序号:"+pcxh+"已存在(同一天，同一产线);");
						toerror = true;
					}
				}
				//order=serviceTemplate.insert(order);
				//return order;
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
		}
		
		//加入错误信息
		if (toerror) {
			String serial=order.getStr("rownumberer_1");
			if (serial==null) {
				serial=" ";
			}
			order.set("error", "序号:"+serial+":"+errorMessage.toString()+"</br>");
		}
		
		return order;
		
	}
	
	/**
	 * 添加生产任务默认数据
	 * @param order
	 */
	private void updateSCRW(DynaBean order) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		//Date date = new Date();
		//order.set("SCRW_PCRQ",sdf.format(date));
		order.set("SCRW_GDZT_NAME", "未完");//工单状态
		order.set("SCRW_GDZT_CODE", 1);
		order.set("SCRW_RWZT_NAME", "待生产");//任务状态
		order.set("SCRW_RWZT_CODE", "RWZT01");
		//添加序号
		int orderindex=order.getInt("SY_ORDERINDEX");
		order.set("SY_ORDERINDEX", orderindex);
		//去掉日期的时分秒
		String jhkgsj = order.getStr("SCRW_JHKGSJ");
		String pcrq = order.getStr("SCRW_PCRQ");
		String jhwgsj = order.getStr("SCRW_JHWGSJ");
		String otdrq = order.getStr("SCRW_OTDRQ");
		String qgrq = order.getStr("SCRW_QGRQ","");
		try {
			System.out.println(jhkgsj+":"+pcrq+":"+jhwgsj+":"+otdrq);
			
			if (pcrq==null||pcrq.isEmpty()) {
				throw new PCExcuteException("排产日期不存在。");
			}else {
				order.set("SCRW_PCRQ", sdf.format(sdf.parse(pcrq)));
			}
			
			if (jhkgsj==null||jhkgsj.isEmpty()) {
				throw new PCExcuteException("计划开工日期不存在");
			}else {
				order.set("SCRW_PCRQ", sdf.format(sdf.parse(pcrq)));
			}
			
			if (jhwgsj==null||jhwgsj.isEmpty()) {
				throw new PCExcuteException("计划完工日期不存在");
			}else {
				order.set("SCRW_JHWGSJ", sdf.format(sdf.parse(jhwgsj)));
			}
			
			if (otdrq==null||otdrq.isEmpty()) {
				throw new PCExcuteException("OTD（交货）日期不存在");
			}else {
				order.set("SCRW_OTDRQ", sdf.format(sdf.parse(otdrq)));
			}
			
			if (!qgrq.isEmpty()&&!"".equals(qgrq)) {
				order.set("SCRW_QGRQ", sdf.format(sdf.parse(qgrq)));
				if (sdf.parse(qgrq).getTime()>sdf.parse(jhkgsj).getTime()) {
					throw new PCExcuteException("请购日期大于计划开工日期,请检查日期。");
				}
			}
			
			serviceTemplate.buildModelCreateInfo(order);//插入系统参数
			order.set("SCRW_SCRWDATASOURCE_CODE","EXCEL");//数据来源
			
			if(sdf.parse(pcrq).getTime()<sdf.parse(jhkgsj).getTime()) {
				throw new PCExcuteException("排产日期小于计划开工日期,请检查日期。");
			}
			
			if (sdf.parse(jhkgsj).getTime()>sdf.parse(jhwgsj).getTime()) {
				throw new PCExcuteException("计划开工日期大于计划完工日期,请检查日期。");
			}
			
			if (sdf.parse(jhwgsj).getTime()>sdf.parse(otdrq).getTime()) {
				throw new PCExcuteException("计划完工日期大于(otd)交货日期,请检查日期。");
			}
			
		} catch (ParseException e) {
			throw new PCExcuteException("日期:"+e.getMessage()+"错误;");
		}
		
	}
	
	

	/**
	 * 乐惠生产任务导入
	 */
	@Override
	public DynaBean implSCRWOrderLH(DynaBean order) {

		logger.debug("导入（乐惠）生产任务前：");
		StringBuilder errorMessage=new StringBuilder(2000);//错误信息记录
		boolean toerror = false;
		//物料编号需做校验，物料资料里面不存在的不能导入，名称以物料资料里面的为准。
		String cpbh = order.getStr("SCRW_ERPGX");
		logger.debug("当前产品编号："+cpbh);
		try {
			DynaBean cpDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA"," and PRODUCTDATA_BH='"+cpbh+"'");
			//判断编号
			if (cpbh==null||cpbh.isEmpty()||cpDynaBean==null) {
				errorMessage.append("产品信息（ERP工序）"+cpbh+"不存在;");
				toerror = true;
			}else {
				order.set("SCRW_ERPGX", cpDynaBean.getStr("PRODUCTDATA_BH"));//写入基础资料中的产品编号
				//查询产品名称    并校验，产品名称为null或与产品表不匹配时
				if (cpDynaBean.getStr("PRODUCTDATA_NAME")==null||cpDynaBean.getStr("PRODUCTDATA_NAME").isEmpty()) {
					errorMessage.append("产品编号"+cpbh+"在产品表中名称为空;");
					toerror = true;
				}else if(order.getStr("SCRW_NAME")==null||!order.getStr("SCRW_NAME").equals(cpDynaBean.getStr("PRODUCTDATA_NAME"))) {
					order.set("SCRW_NAME", cpDynaBean.getStr("PRODUCTDATA_NAME"));
				}
//				if(order.getStr("SCRW_CPGG")==null||!order.getStr("SCRW_CPGG").equals(cpDynaBean.getStr("PRODUCTDATA_GG"))) {
//					order.set("SCRW_CPGG", cpDynaBean.getStr("PRODUCTDATA_GG"));
//				}
				if(order.getStr("SCRW_CPGG")==null||!order.getStr("SCRW_CPGG").equals(cpDynaBean.getStr("PRODUCTDATA_GG"))) {
					errorMessage.append("该产品绑定的规格与导入的规格不一致！");
					toerror = true;
				}
			}
			//查询产品规格，产线编码（机台号），工令单号，班别，类别，属性，班组,排产日期（导入日期)都一致时，不进行导入
			//工令单号代替原本的任务单号，任务单号设置成唯一，暂时与id一致
			//cpbh
			String scrwdh=order.getStr("SCRW_GLDH");//工令单号
			String cxbm=order.getStr("SCRW_SB");//设备编号、产线编码
			String bb = order.getStr("SCRW_BB");//班别
			String lb = order.getStr("SCRW_LB");//类别
			String sx = order.getStr("SCRW_SX");//属性
			String bz = order.getStr("SCRW_BZNAME");//班组
			String SCRW_ERPGX = order.getStr("SCRW_ERPGX");//ERP工序
			String pcrq = order.getStr("SCRW_PCRQ");//排产日期
			List<DynaBean> oldDataList = serviceTemplate.selectList("JGMES_PLAN_SCRW", "and SCRW_ERPGX ='" + cpbh + "' and SCRW_GLDH='" + scrwdh + "' and SCRW_CXBM='" + cxbm + "' and SCRW_BB ='"+bb+"' " +
					"and SCRW_LB='"+lb+"' and SCRW_SX='"+sx+"' and SCRW_BZNAME='"+bz+"' and SCRW_ERPGX='"+SCRW_ERPGX+"' and date_format(SCRW_PCRQ,'%Y-%m-%d') = date_format('"+pcrq+"','%Y-%m-%d')");
			if (oldDataList.size()>0){
				errorMessage.append("该数据在生产任务表中已存在！");
				toerror = true;
			}
			//模具校验
			String mjbh = order.getStr("SCRW_MJBH");
			if (StringUtil.isNotEmpty(mjbh)){
				DynaBean mj_bh = serviceTemplate.selectOne("JGMES_BASE_MJ", "and MJ_BH='"+mjbh+"'");
				if (mj_bh==null){
					errorMessage.append("该模具不存在！");
					toerror = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			errorMessage.append("未知异常，请确认产品编号是否正确。");
			toerror = true;
		}
		//其它字段不做校验（但要按标准格式），每导入一条即增加一条记录，系统自动生成
		//排产单号。
		//生产任务单号生成
//		String scrwdh=order.getStr("SCRW_RWDH");
//		if(scrwdh==null||"".equals(scrwdh)) {
//			scrwdh=serviceTemplate.buildCode("SCRW_RWDH", "JGMES_PLAN_SCRW", order);
//			order.set("SCRW_RWDH", scrwdh);
//		}
		//生产任务单号填写为uuid，暂与生产任务主键id一致
		String uuid = JEUUID.uuid();
		order.setStr("SCRW_RWDH",uuid);
		order.set(BeanUtils.KEY_PK_CODE,uuid);

		//添加序号
		DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " order by SY_ORDERINDEX desc limit 0,1");
		int orderindex =0;
		if (scrwDynaBean==null) {//首次发布任务时序号为1
			orderindex=1;
		}else {
			orderindex = scrwDynaBean.getInt("SY_ORDERINDEX")+1;
		}
		
		order.set("SY_ORDERINDEX", orderindex);
		//日期校验
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String pcrq = order.getStr("SCRW_PCRQ");
		if (pcrq==null||pcrq.isEmpty()) {
			errorMessage.append("日期不存在。");
			toerror = true;
		}else {
			try {
				order.set("SCRW_PCRQ", sdf.format(sdf.parse(pcrq)));
			} catch (ParseException e) {
				errorMessage.append("日期异常，请检查");
				toerror = true;
				e.printStackTrace();
			}
		}
		try {
			//排产数量=SCRW_ZBJHCL(正班计划数)+SCRW_JBJHCL(加班计划数)
			order.set("SCRW_PCSL", order.getInt("SCRW_ZBJHCL")+order.getInt("SCRW_JBJHCL"));
		} catch (Exception e) {
			errorMessage.append("正班计划数或加班计划数异常，请检测");
			toerror = true;
		}
		//产线编号=机台号（设备）
		order.set("SCRW_CXBM", order.getStr("SCRW_SB"));
		order.set("SCRW_GDZT_NAME", "未完");//工单状态
		order.set("SCRW_GDZT_CODE", 1);
		order.set("SCRW_RWZT_NAME", "待生产");//任务状态
		order.set("SCRW_RWZT_CODE", "RWZT01");
		String sb = order.getStr("SCRW_SB");
		order.set("SCRW_SB",sb.substring(0,sb.indexOf("#")+1));
		serviceTemplate.buildModelCreateInfo(order);//插入系统参数
		order.set("SCRW_SCRWDATASOURCE_CODE","EXCEL");//数据来源
		if (toerror) {
			String serial=order.getStr("rownumberer_1");
			if (serial==null) {
				serial=" ";
			}
			order.set("error", "序号:"+serial+":"+errorMessage.toString()+"</br>");
		}
		
		return order;
	}
	
	


	
}