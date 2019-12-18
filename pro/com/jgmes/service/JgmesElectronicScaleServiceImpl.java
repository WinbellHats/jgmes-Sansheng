package com.jgmes.service;

import com.je.core.security.SecurityUserHolder;
import com.je.core.service.PCDynaServiceTemplate;
import com.je.core.service.PCServiceTemplate;
import com.je.core.util.bean.DynaBean;
import com.je.rbac.model.Department;
import com.je.rbac.model.EndUser;
import com.je.rbac.service.UserManager;
import com.jgmes.action.CommonAction;
import com.jgmes.util.JgmesResult;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * 电子秤业务的service层
 * @author liuc
 * @version 2019-05-22 10:32:19
 */
@Component("jgmesElectronicScaleService")
public class JgmesElectronicScaleServiceImpl implements JgmesElectronicScaleService  {

	/**动态Bean(DynaBean)的服务层*/
	private PCDynaServiceTemplate serviceTemplate;
	/**实体Bean操作服务层,主要操作SQL*/
	private PCServiceTemplate pcServiceTemplate;
	/**用户服务层*/
	private UserManager userManager;

	public void load(){
		System.out.println("hello serviceimpl");
	}

	@Override
	public DynaBean getScrwByRwNo(String rwNo) {
		DynaBean scrwDynaBean = null;
		if(rwNo!=null&&!"".equals(rwNo)){
			scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW"," and SCRW_RWDH = '"+rwNo+"'");
		}
		return scrwDynaBean;
	}

	@Override
	public JgmesResult<HashMap> deleteBgSj(String bgSjId, String bgSjZbId) {
		JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
		if(bgSjId!=null&&!"".equals(bgSjId)){
			DynaBean bgDynaBean = serviceTemplate.selectOne("JGMES_PB_BGSJ"," and JGMES_PB_BGSJ_ID = '"+bgSjId+"'");
			//任务单中的完成数量
			if(bgDynaBean!=null){
				CommonAction commonAction = new CommonAction();
				commonAction.setServiceTemplate(serviceTemplate);
				List<DynaBean> gylxgxList = commonAction.getGXList(bgDynaBean.getStr("BGSJ_CPBH"));
				if(gylxgxList!=null&&gylxgxList.size()>0){
					if(bgDynaBean.getStr("BGSJ_GXBH")!=null){
						//判断是不是最后一道工序，如果是最后一道工序要回写一些数据
						if(bgDynaBean.getStr("BGSJ_GXBH").equals(gylxgxList.get(gylxgxList.size()-1).getStr("GYLXGX_GXNUM"))){
							//回写任务单中的完成数量
							DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW",
									" and (SCRW_RWDH = '"+bgDynaBean.getStr("BGSJ_SCRW")+"' or JGMES_PLAN_SCRW_ID = '"+bgDynaBean.getStr("BGSJ_SCRWID")+"')");
							if(scrwDynaBean!=null){
								scrwDynaBean.set("SCRW_WCSL",scrwDynaBean.getInt("SCRW_WCSL")-bgDynaBean.getInt("BGSJ_SL"));
								serviceTemplate.update(scrwDynaBean);

								//回写工单列表中的完成数量

								DynaBean gdDynaBean = serviceTemplate.selectOne("JGMES_PLAN_GDLB"," and GDLB_GDHM = '"+scrwDynaBean.getStr("SCRW_GDHM")+"'");
								if(gdDynaBean!=null){
									gdDynaBean.set("GDLB_WCSL",gdDynaBean.getInt("GDLB_WCSL")-bgDynaBean.getInt("BGSJ_SL"));
									serviceTemplate.update(gdDynaBean);
								}

								//回写产品产线完成情况表

								DynaBean cpCxInfoDynaBean = serviceTemplate.selectOne("JGMES_PB_CPCXINFO",
										" and (CPCXINFO_SCRW = '"+bgDynaBean.getStr("BGSJ_SCRW")+"' or CPCXINFO_SCRWID = '"+bgDynaBean.getStr("BGSJ_SCRWID")+"') and str_to_date(CPCXINFO_RQ,'%Y-%c-%d')=str_to_date(now(),'%Y-%c-%d')");
								if(cpCxInfoDynaBean!=null){
									cpCxInfoDynaBean.set("CPCXINFO_CZSL",cpCxInfoDynaBean.getInt("CPCXINFO_CZSL")-bgDynaBean.getInt("BGSJ_SL"));
									serviceTemplate.update(cpCxInfoDynaBean);
								}
							}
						}else if(bgDynaBean.getStr("BGSJ_GXBH").equals(gylxgxList.get(0).getStr("GYLXGX_GXNUM"))){
							//删除首工序，需要回写的数据
							DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW",
									" and (SCRW_RWDH = '"+bgDynaBean.getStr("BGSJ_SCRW")+"' or JGMES_PLAN_SCRW_ID = '"+bgDynaBean.getStr("BGSJ_SCRWID")+"')");
							if(scrwDynaBean!=null){
								//回写产品产线完成情况表
								DynaBean cpCxInfoDynaBean = serviceTemplate.selectOne("JGMES_PB_CPCXINFO",
										" and (CPCXINFO_SCRW = '"+bgDynaBean.getStr("BGSJ_SCRW")+"' or CPCXINFO_SCRWID = '"+bgDynaBean.getStr("BGSJ_SCRWID")+"') and str_to_date(CPCXINFO_RQ,'%Y-%c-%d')=str_to_date(now(),'%Y-%c-%d')");
								if(cpCxInfoDynaBean!=null){
									cpCxInfoDynaBean.set("CPCXINFO_TRSL",cpCxInfoDynaBean.getInt("CPCXINFO_TRSL")-bgDynaBean.getInt("BGSJ_SL"));
									serviceTemplate.update(cpCxInfoDynaBean);
								}
							}
						}
					}
				}
				serviceTemplate.delete(bgDynaBean);
			}
		}
		return ret;
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