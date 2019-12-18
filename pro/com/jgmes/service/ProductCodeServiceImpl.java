package com.jgmes.service;

import com.je.core.security.SecurityUserHolder;
import com.je.core.service.PCDynaServiceTemplate;
import com.je.core.service.PCServiceTemplate;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.DynaBean;
import com.je.rbac.model.Department;
import com.je.rbac.model.EndUser;
import com.je.rbac.service.UserManager;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 
 * @author ljs
 * @version 2019-04-29 11:43:53
 */
@Component("productCodeService")
public class ProductCodeServiceImpl implements ProductCodeService  {

	/**��̬Bean(DynaBean)�ķ����*/
	private PCDynaServiceTemplate serviceTemplate;
	/**ʵ��Bean���������,��Ҫ����SQL*/
	private PCServiceTemplate pcServiceTemplate;
	/**�û������*/
	private UserManager userManager;
	
	public void load(){
		System.out.println("hello serviceimpl");
	}
	@Override
	public DynaBean implOrder(DynaBean order) {
		StringBuilder errorMessage=new StringBuilder(2000);//错误信息记录
		Boolean haveError = false;
		String GDCPTM_TMH=order.getStr("GDCPTM_TMH").trim();//条码号
		String GDCPTM_CPBH=order.getStr("GDCPTM_CPBH").trim();//产品编号
		String JGMES_PLAN_SCRW_ID=order.getStr("JGMES_PLAN_SCRW_ID").trim();//生产任务单号（获取单号，替换成id）
		String GDCPTM_TMLX_CODE = order.getStr("GDCPTM_TMLX_CODE").trim();//条码类型

		String sql = "select * from je_core_dictionaryitem where dictionaryitem_dictionary_ID = (select je_core_dictionary_ID from je_core_dictionary where dictionary_DDCODE = 'JGMES_DIC_TMLX')";
		String GDCPTM_TMLX_NAME="";
		List<DynaBean> tmlxDicList = serviceTemplate.selectListBySql(sql);
		for (DynaBean mlxDic : tmlxDicList) {
			String DICTIONARYITEM_ITEMNAME = mlxDic.getStr("DICTIONARYITEM_ITEMNAME");
			if (mlxDic.getStr("DICTIONARYITEM_ITEMCODE").equals(GDCPTM_TMLX_CODE)) {
				GDCPTM_TMLX_NAME = DICTIONARYITEM_ITEMNAME;
			}
		}
		if(StringUtil.isEmpty(GDCPTM_TMLX_NAME)){
			errorMessage.append("导入的条码类型不存在！请检查导入数据"+"</br>");
			haveError=true;
		}
		try {
			//判断导入的数据中是否含有相同的数据，即条码号和条码类型一致的数据
			if (order.get("importDataList")!=null){
				List<DynaBean> importDataList = (List<DynaBean>) order.get("importDataList");
				if (importDataList.size() != 0) {
					for (DynaBean dynaBean : importDataList) {
						if (dynaBean.getStr("GDCPTM_TMH").equals(GDCPTM_TMH)){//条码号相同
							if (dynaBean.getStr("GDCPTM_TMLX_CODE").equals(GDCPTM_TMLX_CODE)){//条码类型相同
								String xh1 = dynaBean.getStr("rownumberer_1");
								errorMessage.append("数据和序号："+xh1+"的条码号数据重复（即条码号和条码类型一致)"+"</br>");
								haveError=true;
							}
						}
					}
				}
			}
			/* 判断产品是否在物料资料存在 */
			DynaBean cpBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", "and PRODUCTDATA_BH='" +GDCPTM_CPBH+ "' and PRODUCTDATA_WLTYPE_CODE='CP'");
			if (cpBean== null){
				errorMessage.append("该产品在物料中不存在，请检查！"+"</br>");
				haveError=true;
			}else{
				order.setStr("GDCPTM_TMLX_CODE",GDCPTM_TMLX_CODE);//设置条码类型
				order.setStr("GDCPTM_TMLX_NAME",GDCPTM_TMLX_NAME);//设置条码类型名字
				order.setStr("GDCPTM_TMXZ_CODE","TMXZX02");//设置条码类型
				order.setStr("GDCPTM_TMXZ_NAME","唯一码");//设置条码性质名称
				order.setStr("GDCPTM_NAME",cpBean.getStr("PRODUCTDATA_NAME"));//设置产品名称
			}
			//判定条码号是否为空
			if(GDCPTM_TMH.equals("")){
				errorMessage.append("条码号为空");
				haveError=true;
			}else{
				DynaBean cptmBean = serviceTemplate.selectOne("JGMES_BASE_GDCPTM", "and GDCPTM_TMH='" +GDCPTM_TMH+ "' and GDCPTM_TMLX_CODE='"+GDCPTM_TMLX_CODE+"'");
				if (cptmBean!=null){
					errorMessage.append("条码号已存在，不允许插入");
					haveError=true;
				}
			}
			//生产任务单号在生产任务是否存在，并替换成生产任务id
			DynaBean gdBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", "and SCRW_RWDH='" + JGMES_PLAN_SCRW_ID+"'");
			if (gdBean==null){
				errorMessage.append("该生产任务单号不存在，请检查！"+"</br>");
				haveError=true;
			}else{
				String SCRW_CPBH = gdBean.getStr("SCRW_CPBH");//产品编号
				if (!SCRW_CPBH.equals(GDCPTM_CPBH)){
					errorMessage.append("该生产任务单号绑定的产品和导入的产品不一致，请检查！"+"</br>");
					haveError=true;
				}else{
					String gdid = gdBean.getStr("JGMES_PLAN_SCRW_ID");
					order.setStr("JGMES_PLAN_SCRW_ID",gdid);//替换生产任务外键id
					order.setStr("GDCPTM_SCRWDH",gdBean.getStr("SCRW_RWDH"));//生产任务单号
					DynaBean cpCodeBean = serviceTemplate.selectOne("jgmes_barcode_todolist", "and JGMES_PLAN_SCRW_ID='" + gdid+"' and CPTMYYGG_TMLX_CODE = '"+GDCPTM_TMLX_CODE+"'");
					if(cpCodeBean==null){
						errorMessage.append("该产品没有设置"+GDCPTM_TMLX_NAME+"的条码规则"+"</br>");
						haveError=true;
					}else{
						Integer PCLB_PCSL = cpCodeBean.getInt("PCLB_PCSL");//排产数量
						Integer TMSCJL_YSCSL = cpCodeBean.getInt("TMSCJL_YSCSL");//已生成数量
						Integer CPTMYYGG_MTMSL = cpCodeBean.getInt("CPTMYYGG_MTMSL");//每条码数量
						Integer CreatSL = TMSCJL_YSCSL*CPTMYYGG_MTMSL;
						if(CreatSL>=PCLB_PCSL){
							errorMessage.append("已超出可以生成的条数，不进行导入"+"</br>");
							haveError=true;
						}
						String newId = cpCodeBean.getStr("NEWID");
						String JGMES_BARCODE_TMSCJL_ID = cpCodeBean.getStr("JGMES_BARCODE_TMSCJL_ID");
						String PCLB_GDHM = cpCodeBean.getStr("PCLB_GDHM");
						String PCLB_DDHM = cpCodeBean.getStr("PCLB_DDHM");
						order.setStr("NEWID",newId);//设置成品条码生成外键
						order.setStr("GDCPTM_SFYDY","0");//设置是否已打印
						order.setStr("JGMES_BARCODE_TMSCJL_ID",JGMES_BARCODE_TMSCJL_ID);//设置条码生成记录_外键ID
						order.setStr("GDCPTM_GDHM",PCLB_GDHM);//工单号码
						order.setStr("GDCPTM_DDHM",PCLB_DDHM);//订单号码
					}

				}
			}
		}catch (Exception e) {
			errorMessage.append(e.getMessage()+"</br>");
			haveError=true;
			e.printStackTrace();
		}
		order.setStr("GDCPTM_SL","1");
		order.setStr("GDCPTM_TMLY_NAME","外部码导入");
		order.setStr("GDCPTM_TMLY_CODE","DRM");
		if (haveError) {
			order.set("error", "序号:" + order.getStr("rownumberer_1") + "的错误信息为：" + errorMessage.toString());
		}
		order.set("LX","CP");//设置类型，以标识启动回调方法
		return order;
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
}