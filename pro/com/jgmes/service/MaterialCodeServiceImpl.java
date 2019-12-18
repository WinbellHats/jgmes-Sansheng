package com.jgmes.service;

import com.je.core.security.SecurityUserHolder;
import com.je.core.service.PCDynaServiceTemplate;
import com.je.core.service.PCServiceTemplate;
import com.je.core.util.bean.DynaBean;
import com.je.rbac.model.Department;
import com.je.rbac.model.EndUser;
import com.je.rbac.service.UserManager;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author admin
 * @version 2019-04-10 18:12:21
 */
@Component("materialCodeService")
public class MaterialCodeServiceImpl implements MaterialCodeService  {

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
		List<DynaBean> DataList = (List<DynaBean>) order.get("importDataList");
		List<DynaBean> updateDataList = (List<DynaBean>) order.get("updateDataList");
		String WLTM_TMXZ_CODE=order.getStr("WLTM_TMXZ_CODE").trim();//条码性质编号,批次码填TMXZX01，唯一码填TMXZX02;
		String VENDOR_CODE = order.getStr("VENDOR_CODE").trim();//供应商编码
		String WLTM_WLBM = order.getStr("WLTM_WLBM").trim();//物料编码
		Integer WLTM_SL=0;
		try {
			WLTM_SL =order.getInt("WLTM_SL");
			if (WLTM_TMXZ_CODE.equals("TMXZX02")){
				if (WLTM_SL!=1){
					errorMessage.append("唯一码的数量只能为1 ！"+"</br>");
					haveError=true;
				}
			}
			if (WLTM_TMXZ_CODE.equals("TMXZX01")){
				if (WLTM_SL<2){
					errorMessage.append("批次码的数量必须大于1 ！"+"</br>");
					haveError=true;
				}
			}
		} catch (Exception e) {
			errorMessage.append("数量栏输入错误，请输入数值。"+"</br>");
			haveError=true;
//			e.printStackTrace();
		}
		String WLTM_TMH = order.getStr("WLTM_TMH").trim();
		System.out.println(WLTM_TMXZ_CODE+"----"+VENDOR_CODE+"----"+WLTM_WLBM+"----"+WLTM_SL+"----"+WLTM_TMH);
		try {
			//物料是否存在检测
//		List<DynaBean> wlBean = serviceTemplate.selectList("JGMES_BASE_PRODUCTDATA", "and PRODUCTDATA_BH='" + WLTM_WLBM + "'");
			DynaBean wlBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", "and PRODUCTDATA_BH='" + WLTM_WLBM + "' and PRODUCTDATA_WLTYPE_CODE='WL'");
			if (wlBean== null){
				errorMessage.append("该物料不存在。"+"</br>");
				haveError=true;
			}else{
				order.setStr("WLTM_WLMC",wlBean.getStr("PRODUCTDATA_NAME"));//设置物料名称
			}
			//检查供应商是否存在
			DynaBean gysBean = serviceTemplate.selectOne("JGMES_BASE_VENDOR", "and VENDOR_CODE='" + VENDOR_CODE + "'");
			if (gysBean== null){
				errorMessage.append("该供应商不存在。"+"</br>");
				haveError=true;
			}else {
				String gysPK = gysBean.getStr("JGMES_BASE_VENDOR_ID");
				DynaBean gyszbBean = serviceTemplate.selectOne("JGMES_BASE_VENDORITEM", "and VENDORITEM_WLBM='" + WLTM_WLBM + "' and JGMES_BASE_VENDOR_ID='"+gysPK+"'");
				if (gyszbBean == null) {
					errorMessage.append("供应商下没有绑定该物料。"+"</br>");
					haveError = true;
				}else{
					order.setStr("VENDOR_NAME",gysBean.getStr("VENDOR_NAME"));//设置供应商名称
					order.setStr("VENDOR_SNAME",gysBean.getStr("VENDOR_SNAME"));//供应商简称
					order.setStr("VENDOR_SCODE",gysBean.getStr("VENDOR_SCODE"));//供应商简码
				}
			}
			//判定条码性质编号是否符合格式
			if(!(WLTM_TMXZ_CODE.equals("TMXZX01")||WLTM_TMXZ_CODE.equals("TMXZX02"))){
				errorMessage.append("条码性质编码输入有误，必须为TMXZX01或者TMXZX02。"+"</br>");
				haveError=true;
			}
			if(!haveError){
				order.setStr("WLTM_TMLY_CODE","DRM");//条码来源_CODE
				order.setStr("WLTM_TMLY_NAME","外部导入码");//条码来源_NAME
				//检查数据库，批次码更新数量，唯一码不插入,WLTM_TMH
				DynaBean tm = serviceTemplate.selectOne("JGMES_BASE_WLTM", "and WLTM_TMH='" + WLTM_TMH + "'");
				if (tm == null){
					//条码号不存在不做操作，进行插入
				}else {
					//批次码
					if(WLTM_TMXZ_CODE.equals("TMXZX01")){
//						if (WLTM_TMH.equals(tm.getStr("WLTM_TMH"))){
							String gysbm = tm.getStr("VENDOR_CODE");
							if (!(tm.getStr("WLTM_WLBM").equals(WLTM_WLBM))){
								errorMessage.append("该批次码已被物料："+tm.getStr("WLTM_WLMC")+"绑定,不允许插入"+"</br>");
								haveError = true;
							}
							if (!(tm.getStr("VENDOR_CODE").equals(VENDOR_CODE))){
								errorMessage.append("该批次码已被供应商："+tm.getStr("VENDOR_NAME")+"绑定,不允许插入"+"</br>");
								haveError = true;
							}
//						}
						Boolean open = false;
						if (updateDataList.size()>0){
							for (DynaBean updateBean : updateDataList) {
								if (updateBean.getStr("WLTM_TMH").equals(WLTM_TMH)){//条码号相同
									DataList=updateDataList;
									open = false;
								}else{
									open = true;
								}
							}
							if (open){
								int wltm_sl = tm.getInt("WLTM_SL");
								int sl = WLTM_SL+wltm_sl;
								order.setInt("WLTM_SL",sl);
								order.setStr("WLTM_TMXZ_NAME","批次码");//设置条码性质名称
							}
						}else{
							int wltm_sl = tm.getInt("WLTM_SL");
							int sl = WLTM_SL+wltm_sl;
							order.setInt("WLTM_SL",sl);
							order.setStr("WLTM_TMXZ_NAME","批次码");//设置条码性质名称
						}

					}
					//唯一码
					if(WLTM_TMXZ_CODE.equals("TMXZX02")){
						errorMessage.append("唯一码："+WLTM_TMH+"已存在！不进行插入"+"</br>");
						haveError = true;
						order.setStr("WLTM_TMXZ_NAME","唯一码");//设置条码性质名称
					}
				}
				//校验导入的数据是否含有重复的数据
				if (DataList.size() != 0) {
					Iterator<DynaBean> iterator = DataList.iterator();
					while(iterator.hasNext()){
						DynaBean dynaBean = iterator.next();
						String xh1 = dynaBean.getStr("rownumberer_1");
						if (dynaBean.getStr("WLTM_TMH").equals(WLTM_TMH)){//条码号相同
							if (dynaBean.getStr("WLTM_TMXZ_CODE").equals("TMXZX02")){//唯一码的情况下
								errorMessage.append("数据和序号："+xh1+"的条码号数据重复（该条码为唯一码，不允许插入)"+"</br>");
								haveError=true;
							}
							if (dynaBean.getStr("WLTM_TMXZ_CODE").equals("TMXZX01")){//批次码的情况下
								if (!(dynaBean.getStr("VENDOR_CODE").equals(VENDOR_CODE))){//供应商不同
									errorMessage.append("该行数据和序号："+xh1+"的条码号所绑定的供应商不一致（批次码需要供应商和物料编码一致才能进行叠加数量)"+"</br>");
								}
								if (!(dynaBean.getStr("WLTM_WLBM").equals(WLTM_WLBM))){//物料不同
									errorMessage.append("该行数据和序号："+xh1+"的条码号所绑定的物料不一致（批次码需要供应商和物料编码一致才能进行叠加数量)"+"</br>");
								}
							}
							int wltm_sl = order.getInt("WLTM_SL");//插入的数量
							int import_sl = dynaBean.getInt("WLTM_SL");
							int all_sl = wltm_sl+import_sl;
							order.set("WLTM_SL",all_sl);
							iterator.remove();
						}
					}
				}
			}
			DynaBean v_jgmes_base_vendoritem = serviceTemplate.selectOne("V_JGMES_BASE_VENDORITEM", "and VENDOR_CODE='" + VENDOR_CODE + "' and VENDORITEM_WLBM='" + WLTM_WLBM + "' and CPTMYYGG_TMXZ_CODE='" + WLTM_TMXZ_CODE + "'");
			if (v_jgmes_base_vendoritem==null){
				errorMessage.append("获取数据失败，请检查物料规则是否存在"+"</br>");
			}else{
				order.setStr("NEWID",v_jgmes_base_vendoritem.getStr("NEWID"));//设置主键
			}
		} catch (Exception e) {
			errorMessage.append(e.getMessage());
			haveError=true;
			e.printStackTrace();
		}
		if (haveError) {
			order.set("error", "序号:" + order.getStr("rownumberer_1") + ":" + errorMessage.toString());
		}
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