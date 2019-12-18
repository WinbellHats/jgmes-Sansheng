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
import com.jgmes.util.JgmesResult;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * 打卡功能
 * @author liuc
 * @version 2019-05-10 10:42:34
 */
@Component("jgmesClockInService")
public class JgmesClockInServiceImpl implements JgmesClockInService  {

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
	public JgmesResult<HashMap> doSaveOrUpdateSkkqb(String jobNum, String skStatus, String cxCode, String scrwNo, String zgCxCode) {
		JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
		HashMap hashMap = new HashMap();
		JgmesCommon jgmesCommon = new JgmesCommon(null, serviceTemplate);
		if(jobNum==null||jobNum.isEmpty()){
			ret.setMessage("未获取到工号信息！");
		}
		if(skStatus==null||skStatus.isEmpty()){
			ret.setMessage("未获取到刷卡状态！");
		}
		if(cxCode == null || cxCode.isEmpty()){
			ret.setMessage("未获取到产线编码！");
		}
//		if(scrwNo == null || scrwNo.isEmpty()){
//			ret.setMessage("未获取到生产任务单号！");
//		}
		if(jobNum!=null&&!jobNum.isEmpty()&&skStatus!=null&&!skStatus.isEmpty()&&cxCode!=null&&!cxCode.isEmpty()) {
			//如果是上岗

			//判断是否调离岗位
			List<DynaBean> dgDynaBeanList = serviceTemplate.selectList("JGMES_SCGCGL_SKKQB",
					" and SKKQB_IDKH = '" + jobNum + "' and SKKQB_CXBM = '" + cxCode + "' and SKKQB_SKZT_CODE = 'SKZT03' and SKKQB_NO_CODE = '1' and str_to_date(skkqb_sksj,'%Y-%c-%d')=str_to_date(CURDATE(),'%Y-%c-%d') order by skkqb_sksj desc ");
			if(dgDynaBeanList!=null&&dgDynaBeanList.size()>0){
				DynaBean cxDynaBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ"," and CXSJ_CXBM = '"+dgDynaBeanList.get(0).getStr("SKKQB_ZGXX")+"'");
				String cxmc = "";
				if(cxDynaBean!=null){
					cxmc = cxDynaBean.getStr("CXSJ_CXMC");
				}else{
					cxmc = dgDynaBeanList.get(0).getStr("SKKQB_ZGXX");
				}
				ret.setMessage("你已经调离该产线！转岗信息："+cxmc);
				return ret;
			}

			String sql = "select distinct SKKQB_CXBM,SKKQB_CXMC from JGMES_SCGCGL_SKKQB where SKKQB_IDKH = '" + jobNum + "' and SKKQB_CXBM != '" + cxCode + "' and SKKQB_NO_CODE = '1' and str_to_date(skkqb_sksj,'%Y-%c-%d')=str_to_date(CURDATE(),'%Y-%c-%d') order by skkqb_sksj desc ";
			//判断一下是否在别的产线已经刷卡
			boolean isTrue = true;
			String cxmc = "";
			List<DynaBean> dynaBeanList1 = serviceTemplate.selectListBySql(sql);
			if(dynaBeanList1!=null&&dynaBeanList1.size()>0){
				for(DynaBean dynaBean1:dynaBeanList1){
					List<DynaBean> dynaBeanList2 = serviceTemplate.selectList("JGMES_SCGCGL_SKKQB",
							" and SKKQB_IDKH = '" + jobNum + "' and SKKQB_CXBM = '" + dynaBean1.getStr("SKKQB_CXBM") + "' and SKKQB_SKZT_CODE = 'SKZT03' and SKKQB_NO_CODE = '1' and str_to_date(skkqb_sksj,'%Y-%c-%d')=str_to_date(CURDATE(),'%Y-%c-%d') order by skkqb_sksj desc ");
					if(dynaBeanList2!=null&&dynaBeanList2.size()>0){
					}else{
						cxmc = dynaBean1.getStr("SKKQB_CXMC");
						isTrue = false;

					}
				}
			}
			if(!isTrue){
				ret.setMessage("你在"+cxmc+"已经刷过卡了！");
				return ret;
			}

			if ("SKZT01".equals(skStatus)) {
				//获取离岗数据
				List<DynaBean> lgDynaBeanList = serviceTemplate.selectList("JGMES_SCGCGL_SKKQB",
						" and SKKQB_IDKH = '" + jobNum + "' and SKKQB_CXBM = '" + cxCode + "' and SKKQB_SKZT_CODE = 'SKZT02' and SKKQB_NO_CODE = '1' and str_to_date(skkqb_sksj,'%Y-%c-%d')=str_to_date(CURDATE(),'%Y-%c-%d') order by skkqb_sksj desc ");
				//如果离岗数据不为空
				if(lgDynaBeanList!=null&&lgDynaBeanList.size()>0){
					
					//获取下班时间
					List<DynaBean> sgDynaBeanList = serviceTemplate.selectList("JGMES_SCGCGL_SKKQB",
							" and SKKQB_IDKH = '" + jobNum + "' and SKKQB_CXBM = '" + cxCode + "' and SKKQB_SKZT_CODE = 'SKZT05' and SKKQB_NO_CODE = '1' and str_to_date(skkqb_sksj,'%Y-%c-%d')=str_to_date(CURDATE(),'%Y-%c-%d') order by skkqb_sksj desc ");
					
					List<DynaBean> skkqbDynaBeanList = null;
					
					//获取最后一条有效数据
					skkqbDynaBeanList = serviceTemplate.selectList("JGMES_SCGCGL_SKKQB",
							" and SKKQB_IDKH = '" + jobNum + "' and SKKQB_CXBM = '" + cxCode + "' and SKKQB_NO_CODE = '1' and str_to_date(skkqb_sksj,'%Y-%c-%d')=str_to_date(CURDATE(),'%Y-%c-%d') order by skkqb_sksj desc");
					if(skkqbDynaBeanList!=null&&skkqbDynaBeanList.size()>0) {
						if("SKZT05".equals(skkqbDynaBeanList.get(0).getStr("SKKQB_SKZT_CODE"))) {
							skkqbDynaBeanList = null;
						}else{
							skkqbDynaBeanList = serviceTemplate.selectList("JGMES_SCGCGL_SKKQB",
									" and SKKQB_IDKH = '" + jobNum + "' and SKKQB_CXBM = '" + cxCode + "' and SKKQB_SKZT_CODE = 'SKZT01' and SKKQB_NO_CODE = '1' and str_to_date(skkqb_sksj,'%Y-%c-%d')=str_to_date(CURDATE(),'%Y-%c-%d') and str_to_date(skkqb_sksj,'%Y-%c-%d %H:%i:%s')>=str_to_date('" + lgDynaBeanList.get(0).getStr("SKKQB_SKSJ")+ "','%Y-%c-%d %H:%i:%s') order by skkqb_sksj desc ");

						}
					}
					
					if(skkqbDynaBeanList!=null&&skkqbDynaBeanList.size()>0){
						DynaBean skkqbDynaBean = skkqbDynaBeanList.get(0);
						if(skkqbDynaBean!=null&&skkqbDynaBean.getStr("SKKQB_CXBM")!=null&&!skkqbDynaBean.getStr("SKKQB_CXBM").equals(cxCode)){
							ret.setMessage("不在同一个产线！");
							return ret;
						}
						//是否有效
						DynaBean dic2 = jgmesCommon.getDic("JGMES_YES_NO", "0");
						if (dic2 != null) {
							skkqbDynaBean.set("SKKQB_NO_CODE", dic2.get("DICTIONARYITEM_ITEMCODE"));
							skkqbDynaBean.set("SKKQB_NO_NAME", dic2.get("DICTIONARYITEM_ITEMNAME"));
						}
						serviceTemplate.update(skkqbDynaBean);
						hashMap.put("Duplication",0);
					}
				}else{//如果离岗数据为空
					
					//获取下班时间
					List<DynaBean> sgDynaBeanList = serviceTemplate.selectList("JGMES_SCGCGL_SKKQB",
							" and SKKQB_IDKH = '" + jobNum + "' and SKKQB_CXBM = '" + cxCode + "' and SKKQB_SKZT_CODE = 'SKZT05' and SKKQB_NO_CODE = '1' and str_to_date(skkqb_sksj,'%Y-%c-%d')=str_to_date(CURDATE(),'%Y-%c-%d') order by skkqb_sksj desc ");
					
					
					List<DynaBean> skkqbDynaBeanList = null;

					//获取最迟有效上岗时间
					if(sgDynaBeanList!=null&&sgDynaBeanList.size()>0){
						skkqbDynaBeanList = serviceTemplate.selectList("JGMES_SCGCGL_SKKQB",
								" and SKKQB_IDKH = '" + jobNum + "' and SKKQB_CXBM = '" + cxCode + "' and SKKQB_SKZT_CODE = 'SKZT01' and SKKQB_NO_CODE = '1' and str_to_date(skkqb_sksj,'%Y-%c-%d')=str_to_date(CURDATE(),'%Y-%c-%d') and str_to_date(skkqb_sksj,'%Y-%c-%d %H:%i:%s')>=str_to_date('" + sgDynaBeanList.get(0).getStr("SKKQB_SKSJ")+ "','%Y-%c-%d %H:%i:%s') order by skkqb_sksj");
					}else{
						skkqbDynaBeanList = serviceTemplate.selectList("JGMES_SCGCGL_SKKQB",
								" and SKKQB_IDKH = '" + jobNum + "' and SKKQB_CXBM = '" + cxCode + "' and SKKQB_SKZT_CODE = 'SKZT01' and SKKQB_NO_CODE = '1' and str_to_date(skkqb_sksj,'%Y-%c-%d')=str_to_date(CURDATE(),'%Y-%c-%d') order by skkqb_sksj");
					}
					
					if(skkqbDynaBeanList!=null&&skkqbDynaBeanList.size()>0){
						DynaBean skkqbDynaBean = skkqbDynaBeanList.get(0);
						if(skkqbDynaBean!=null&&skkqbDynaBean.getStr("SKKQB_CXBM")!=null&&!skkqbDynaBean.getStr("SKKQB_CXBM").equals(cxCode)){
							ret.setMessage("不在同一个产线！");
							return ret;
						}
						//是否有效
						DynaBean dic2 = jgmesCommon.getDic("JGMES_YES_NO", "0");
						if (dic2 != null) {
							skkqbDynaBean.set("SKKQB_NO_CODE", dic2.get("DICTIONARYITEM_ITEMCODE"));
							skkqbDynaBean.set("SKKQB_NO_NAME", dic2.get("DICTIONARYITEM_ITEMNAME"));
						}
						serviceTemplate.update(skkqbDynaBean);
						hashMap.put("Duplication",0);
					}
				}
				ret = setDynaBeanInfo(jobNum,scrwNo,skStatus,cxCode,"1","",hashMap);
				//离岗
			} else if ("SKZT02".equals(skStatus)) {

				//获取上岗时间
				List<DynaBean> sgDynaBeanList = serviceTemplate.selectList("JGMES_SCGCGL_SKKQB",
						" and SKKQB_IDKH = '" + jobNum + "' and SKKQB_CXBM = '" + cxCode + "' and SKKQB_SKZT_CODE = 'SKZT01' and SKKQB_NO_CODE = '1' and str_to_date(skkqb_sksj,'%Y-%c-%d')=str_to_date(CURDATE(),'%Y-%c-%d') order by skkqb_sksj desc ");

				List<DynaBean> skkqbDynaBeanList = null;
				
				//是否已经下班
				skkqbDynaBeanList = serviceTemplate.selectList("JGMES_SCGCGL_SKKQB",
						" and SKKQB_IDKH = '" + jobNum + "' and SKKQB_CXBM = '" + cxCode + "' and SKKQB_NO_CODE = '1' and str_to_date(skkqb_sksj,'%Y-%c-%d')=str_to_date(CURDATE(),'%Y-%c-%d') order by skkqb_sksj desc");
				if(skkqbDynaBeanList!=null&&skkqbDynaBeanList.size()>0) {
					if("SKZT05".equals(skkqbDynaBeanList.get(0).getStr("SKKQB_SKZT_CODE"))) {
						ret.setMessage("已经刷卡下班了！");
						return ret;
					}
				}
			
				//获取最迟有效上岗时间获取最迟有效上岗时间
				if(sgDynaBeanList!=null&&sgDynaBeanList.size()>0){
					skkqbDynaBeanList = serviceTemplate.selectList("JGMES_SCGCGL_SKKQB",
							" and SKKQB_IDKH = '" + jobNum + "' and SKKQB_CXBM = '" + cxCode + "' and SKKQB_SKZT_CODE = 'SKZT02' and SKKQB_NO_CODE = '1' and str_to_date(skkqb_sksj,'%Y-%c-%d')=str_to_date(CURDATE(),'%Y-%c-%d') and str_to_date(skkqb_sksj,'%Y-%c-%d %H:%i:%s')>=str_to_date('" + sgDynaBeanList.get(0).getStr("SKKQB_SKSJ")+ "','%Y-%c-%d %H:%i:%s') order by skkqb_sksj");
				}else{
					skkqbDynaBeanList = serviceTemplate.selectList("JGMES_SCGCGL_SKKQB",
							" and SKKQB_IDKH = '" + jobNum + "' and SKKQB_CXBM = '" + cxCode + "' and SKKQB_SKZT_CODE = 'SKZT02' and SKKQB_NO_CODE = '1' and str_to_date(skkqb_sksj,'%Y-%c-%d')=str_to_date(CURDATE(),'%Y-%c-%d') order by skkqb_sksj");
				}

				if (skkqbDynaBeanList != null&&skkqbDynaBeanList.size()>0) {
					DynaBean skkqbDynaBean = skkqbDynaBeanList.get(0);
					if(skkqbDynaBean!=null&&skkqbDynaBean.getStr("SKKQB_CXBM")!=null&&!skkqbDynaBean.getStr("SKKQB_CXBM").equals(cxCode)){
						ret.setMessage("不在同一个产线！");
						return ret;
					}
					skkqbDynaBean.set("JGMES_SCGCGL_SKKQB_ID", JEUUID.uuid());
					//刷卡时间
					skkqbDynaBean.set("SKKQB_SKSJ", jgmesCommon.getCurrentTime());
					//刷卡状态
					DynaBean dic = jgmesCommon.getDic("JGMES_DIC_SKZT", "SKZT02");
					if (dic != null) {
						skkqbDynaBean.set("SKKQB_SKZT_CODE", dic.get("DICTIONARYITEM_ITEMCODE"));
						skkqbDynaBean.set("SKKQB_SKZT_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));
					}
					//是否有效
					DynaBean dic2 = jgmesCommon.getDic("JGMES_YES_NO", "0");
					if (dic != null) {
						skkqbDynaBean.set("SKKQB_NO_CODE", dic2.get("DICTIONARYITEM_ITEMCODE"));
						skkqbDynaBean.set("SKKQB_NO_NAME", dic2.get("DICTIONARYITEM_ITEMNAME"));
					}
					skkqbDynaBean = serviceTemplate.insert(skkqbDynaBean);
					hashMap.put("Duplication",0);
					hashMap.put("SKKQB_YHMC",skkqbDynaBean.getStr("SKKQB_YHMC"));
					hashMap.put("SKKQB_SKSJ",skkqbDynaBean.getStr("SKKQB_SKSJ"));
					ret.Data = hashMap;
				}else{
					ret = setDynaBeanInfo(jobNum,scrwNo,skStatus,cxCode,"1","",new HashMap());
				}
				//调出
			} else if ("SKZT03".equals(skStatus)) {
				if(zgCxCode==null||zgCxCode.isEmpty()){
					ret.setMessage("未获取到掉入得产线！");
					return ret;
				}
				if(zgCxCode!=null&&zgCxCode.equals(cxCode)){
					ret.setMessage("已经在本产线了，请检查！");
					return ret;
				}
				//处理两个产线互相调，出现的问题
				//判断是否调离岗位
				List<DynaBean> dgList = serviceTemplate.selectList("JGMES_SCGCGL_SKKQB",
						" and SKKQB_IDKH = '" + jobNum + "' and SKKQB_CXBM = '" + zgCxCode + "' and SKKQB_SKZT_CODE in ('SKZT03','SKZT04') and SKKQB_NO_CODE = '1' and str_to_date(skkqb_sksj,'%Y-%c-%d')=str_to_date(CURDATE(),'%Y-%c-%d') order by skkqb_sksj desc ");
				for(DynaBean dg:dgList){
					//是否有效
					DynaBean dic1 = jgmesCommon.getDic("JGMES_YES_NO", "0");
					if (dic1 != null) {
						dg.set("SKKQB_NO_CODE", dic1.get("DICTIONARYITEM_ITEMCODE"));
						dg.set("SKKQB_NO_NAME", dic1.get("DICTIONARYITEM_ITEMNAME"));
						serviceTemplate.update(dg);
					}
				}

				List<DynaBean> skkqbDynaBeanList = serviceTemplate.selectList("JGMES_SCGCGL_SKKQB",
						" and SKKQB_IDKH = '" + jobNum + "' and SKKQB_CXBM = '" + cxCode + "' and SKKQB_SKZT_CODE = 'SKZT03' and SKKQB_NO_CODE = '1' and str_to_date(skkqb_sksj,'%Y-%c-%d')=str_to_date(CURDATE(),'%Y-%c-%d')");
				if (skkqbDynaBeanList != null&&skkqbDynaBeanList.size()>0) {
					DynaBean skkqbDynaBean = skkqbDynaBeanList.get(0);
					if(skkqbDynaBean!=null&&skkqbDynaBean.getStr("SKKQB_CXBM")!=null&&!skkqbDynaBean.getStr("SKKQB_CXBM").equals(cxCode)){
						ret.setMessage("不在同一个产线！");
						return ret;
					}

					//是否有效
					DynaBean dic1 = jgmesCommon.getDic("JGMES_YES_NO", "0");
					if (dic1 != null) {
						skkqbDynaBean.set("SKKQB_NO_CODE", dic1.get("DICTIONARYITEM_ITEMCODE"));
						skkqbDynaBean.set("SKKQB_NO_NAME", dic1.get("DICTIONARYITEM_ITEMNAME"));
					}

					skkqbDynaBean = serviceTemplate.update(skkqbDynaBean);

					skkqbDynaBean.set("JGMES_SCGCGL_SKKQB_ID", JEUUID.uuid());
					//刷卡时间
					skkqbDynaBean.set("SKKQB_SKSJ", jgmesCommon.getCurrentTime());
					//刷卡状态
					DynaBean dic = jgmesCommon.getDic("JGMES_DIC_SKZT", "SKZT03");
					if (dic != null) {
						skkqbDynaBean.set("SKKQB_SKZT_CODE", dic.get("DICTIONARYITEM_ITEMCODE"));
						skkqbDynaBean.set("SKKQB_SKZT_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));
					}
					//是否有效
					DynaBean dic2 = jgmesCommon.getDic("JGMES_YES_NO", "1");
					if (dic2 != null) {
						skkqbDynaBean.set("SKKQB_NO_CODE", dic2.get("DICTIONARYITEM_ITEMCODE"));
						skkqbDynaBean.set("SKKQB_NO_NAME", dic2.get("DICTIONARYITEM_ITEMNAME"));
					}
					skkqbDynaBean = serviceTemplate.insert(skkqbDynaBean);
					hashMap.put("SKKQB_YHMC",skkqbDynaBean.getStr("SKKQB_YHMC"));
					hashMap.put("SKKQB_SKSJ",skkqbDynaBean.getStr("SKKQB_SKSJ"));
					ret.Data = hashMap;
				} else {
					ret = setDynaBeanInfo(jobNum,scrwNo,skStatus,cxCode,"1",zgCxCode,new HashMap());
					List<DynaBean> scrwDynaBeanList = serviceTemplate.selectList("JGMES_PLAN_SCRW"," and SCRW_CXBM = '"+zgCxCode+"' and SCRW_RWZT_CODE = 'RWZT02' order by SY_ORDERINDEX desc,SCRW_SJKGSJ desc");
					scrwNo = "";
					if(scrwDynaBeanList!=null&&scrwDynaBeanList.size()>0){
						DynaBean scrwDynaBean = scrwDynaBeanList.get(0);
						if(scrwDynaBean!=null){
							scrwNo = scrwDynaBean.getStr("SCRW_RWDH");
						}
					}
					ret = setDynaBeanInfo(jobNum,scrwNo,"SKZT01",zgCxCode,"1","",new HashMap());
				}
				//调出装配部
			}else if ("SKZT04".equals(skStatus)) {
				List<DynaBean> skkqbDynaBeanList = serviceTemplate.selectList("JGMES_SCGCGL_SKKQB",
						" and SKKQB_IDKH = '" + jobNum + "' and SKKQB_CXBM = '" + cxCode + "' and SKKQB_SKZT_CODE = 'SKZT04' and SKKQB_NO_CODE = '1' and str_to_date(skkqb_sksj,'%Y-%c-%d')=str_to_date(CURDATE(),'%Y-%c-%d')");
				if (skkqbDynaBeanList != null&&skkqbDynaBeanList.size()>0) {
					DynaBean skkqbDynaBean = skkqbDynaBeanList.get(0);
					if(skkqbDynaBean!=null&&skkqbDynaBean.getStr("SKKQB_CXBM")!=null&&!skkqbDynaBean.getStr("SKKQB_CXBM").equals(cxCode)){
						ret.setMessage("不在同一个产线！");
						return ret;
					}

					//是否有效
					DynaBean dic1 = jgmesCommon.getDic("JGMES_YES_NO", "0");
					if (dic1 != null) {
						skkqbDynaBean.set("SKKQB_NO_CODE", dic1.get("DICTIONARYITEM_ITEMCODE"));
						skkqbDynaBean.set("SKKQB_NO_NAME", dic1.get("DICTIONARYITEM_ITEMNAME"));
					}

					skkqbDynaBean = serviceTemplate.update(skkqbDynaBean);

					skkqbDynaBean.set("JGMES_SCGCGL_SKKQB_ID", JEUUID.uuid());
					//刷卡时间
					skkqbDynaBean.set("SKKQB_SKSJ", jgmesCommon.getCurrentTime());

					//转岗信息
					skkqbDynaBean.set("SKKQB_ZGXX","调出装配部");
					//刷卡状态
					DynaBean dic = jgmesCommon.getDic("JGMES_DIC_SKZT", "SKZT04");
					if (dic != null) {
						skkqbDynaBean.set("SKKQB_SKZT_CODE", dic.get("DICTIONARYITEM_ITEMCODE"));
						skkqbDynaBean.set("SKKQB_SKZT_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));
					}
					//是否有效
					DynaBean dic2 = jgmesCommon.getDic("JGMES_YES_NO", "1");
					if (dic2 != null) {
						skkqbDynaBean.set("SKKQB_NO_CODE", dic2.get("DICTIONARYITEM_ITEMCODE"));
						skkqbDynaBean.set("SKKQB_NO_NAME", dic2.get("DICTIONARYITEM_ITEMNAME"));
					}
					skkqbDynaBean = serviceTemplate.insert(skkqbDynaBean);
					hashMap.put("SKKQB_YHMC",skkqbDynaBean.getStr("SKKQB_YHMC"));
					hashMap.put("SKKQB_SKSJ",skkqbDynaBean.getStr("SKKQB_SKSJ"));
					ret.Data = hashMap;
				} else {
					ret = setDynaBeanInfo(jobNum,scrwNo,skStatus,cxCode,"1","调出装配部",new HashMap());
				}
				//下班
			} else if ("SKZT05".equals(skStatus)) {
				
//				//
//				List<DynaBean> skkqbDynaBeanList = serviceTemplate.selectList("JGMES_SCGCGL_SKKQB",
//						" and SKKQB_IDKH = '" + jobNum + "' and SKKQB_CXBM = '" + cxCode + "' and SKKQB_SKZT_CODE = 'SKZT05' and SKKQB_NO_CODE = '1' and str_to_date(skkqb_sksj,'%Y-%c-%d')=str_to_date(CURDATE(),'%Y-%c-%d')");
				
				//获取上岗时间
				List<DynaBean> sgDynaBeanList = serviceTemplate.selectList("JGMES_SCGCGL_SKKQB",
						" and SKKQB_IDKH = '" + jobNum + "' and SKKQB_CXBM = '" + cxCode + "' and SKKQB_SKZT_CODE = 'SKZT01' and SKKQB_NO_CODE = '1' and str_to_date(skkqb_sksj,'%Y-%c-%d')=str_to_date(CURDATE(),'%Y-%c-%d') order by skkqb_sksj desc ");
				
				
				List<DynaBean> skkqbDynaBeanList = null;

				//获取最迟有效上岗时间
				if(sgDynaBeanList!=null&&sgDynaBeanList.size()>0){
					skkqbDynaBeanList = serviceTemplate.selectList("JGMES_SCGCGL_SKKQB",
							" and SKKQB_IDKH = '" + jobNum + "' and SKKQB_CXBM = '" + cxCode + "' and SKKQB_SKZT_CODE = 'SKZT05' and SKKQB_NO_CODE = '1' and str_to_date(skkqb_sksj,'%Y-%c-%d')=str_to_date(CURDATE(),'%Y-%c-%d') and str_to_date(skkqb_sksj,'%Y-%c-%d %H:%i:%s')>=str_to_date('" + sgDynaBeanList.get(0).getStr("SKKQB_SKSJ")+ "','%Y-%c-%d %H:%i:%s') order by skkqb_sksj");
				}else{
					skkqbDynaBeanList = serviceTemplate.selectList("JGMES_SCGCGL_SKKQB",
							" and SKKQB_IDKH = '" + jobNum + "' and SKKQB_CXBM = '" + cxCode + "' and SKKQB_SKZT_CODE = 'SKZT05' and SKKQB_NO_CODE = '1' and str_to_date(skkqb_sksj,'%Y-%c-%d')=str_to_date(CURDATE(),'%Y-%c-%d') order by skkqb_sksj");
				}
				
				
				
				if (skkqbDynaBeanList != null&&skkqbDynaBeanList.size()>0) {
					DynaBean skkqbDynaBean = skkqbDynaBeanList.get(0);

					if(skkqbDynaBean!=null&&skkqbDynaBean.getStr("SKKQB_CXBM")!=null&&!skkqbDynaBean.getStr("SKKQB_CXBM").equals(cxCode)){
						ret.setMessage("不在同一个产线！");
						return ret;
					}

//					//是否有效
//					DynaBean dic1 = jgmesCommon.getDic("JGMES_YES_NO", "0");
//					if (dic1 != null) {
//						skkqbDynaBean.set("SKKQB_NO_CODE", dic1.get("DICTIONARYITEM_ITEMCODE"));
//						skkqbDynaBean.set("SKKQB_NO_NAME", dic1.get("DICTIONARYITEM_ITEMNAME"));
//					}
//					skkqbDynaBean = serviceTemplate.update(skkqbDynaBean);


					skkqbDynaBean.set("JGMES_SCGCGL_SKKQB_ID", JEUUID.uuid());
					//刷卡时间
					skkqbDynaBean.set("SKKQB_SKSJ", jgmesCommon.getCurrentTime());
					//刷卡状态
					DynaBean dic = jgmesCommon.getDic("JGMES_DIC_SKZT", "SKZT05");
					if (dic != null) {
						skkqbDynaBean.set("SKKQB_SKZT_CODE", dic.get("DICTIONARYITEM_ITEMCODE"));
						skkqbDynaBean.set("SKKQB_SKZT_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));
					}
					//是否有效
					DynaBean dic2 = jgmesCommon.getDic("JGMES_YES_NO", "0");
					if (dic2 != null) {
						skkqbDynaBean.set("SKKQB_NO_CODE", dic2.get("DICTIONARYITEM_ITEMCODE"));
						skkqbDynaBean.set("SKKQB_NO_NAME", dic2.get("DICTIONARYITEM_ITEMNAME"));
					}
					skkqbDynaBean = serviceTemplate.insert(skkqbDynaBean);
					hashMap.put("Duplication",0);
					hashMap.put("SKKQB_YHMC",skkqbDynaBean.getStr("SKKQB_YHMC"));
					hashMap.put("SKKQB_SKSJ",skkqbDynaBean.getStr("SKKQB_SKSJ"));
					ret.Data = hashMap;
				} else {
					ret = setDynaBeanInfo(jobNum,scrwNo,skStatus,cxCode,"1","",new HashMap());
				}
			}

		}
		return ret;
	}

	/**
	 *
	 * @param jobNum 工位号
	 * @param scrwNo 任务单号
	 * @param skStatus 刷卡状态
	 * @param cxCode 产线编码
	 * @param isvalid 是否有效
	 * @param zg 转岗信息
	 * @return
	 */
	public JgmesResult<HashMap> setDynaBeanInfo(String jobNum,String scrwNo,String skStatus,String cxCode,String isvalid,String zg,HashMap hashMap){
		JgmesCommon jgmesCommon = new JgmesCommon(null, serviceTemplate);
		JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
		//根据工号查出该工号的用户信息
		DynaBean userDynaBean = serviceTemplate.selectOne("je_core_enduser", " and JOBNUM = '" + jobNum + "'");
		//if (userDynaBean != null) {
			DynaBean cxDynaBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ", " and CXSJ_CXBM = '" + cxCode + "'");
			if(cxDynaBean!=null){
				DynaBean skkqbDynaBean = new DynaBean();
				skkqbDynaBean.set("JGMES_SCGCGL_SKKQB_ID", JEUUID.uuid());
				skkqbDynaBean.set(BeanUtils.KEY_TABLE_CODE,"JGMES_SCGCGL_SKKQB");
				//产线编码
				skkqbDynaBean.set("SKKQB_CXBM",cxDynaBean.getStr("CXSJ_CXBM"));
				//产线名称
				skkqbDynaBean.set("SKKQB_CXMC",cxDynaBean.getStr("CXSJ_CXMC"));
				//ID卡号
				skkqbDynaBean.set("SKKQB_IDKH",jobNum);
				//生产任务单号
				skkqbDynaBean.set("SKKQB_XCXSCDDH",scrwNo);
				//刷卡时间
				skkqbDynaBean.set("SKKQB_SKSJ",jgmesCommon.getCurrentTime());
				//转岗信息
				skkqbDynaBean.set("SKKQB_ZGXX",zg);
				//刷卡状态
				DynaBean dic = jgmesCommon.getDic("JGMES_DIC_SKZT",skStatus);
				if(dic!=null){
					skkqbDynaBean.set("SKKQB_SKZT_CODE",dic.get("DICTIONARYITEM_ITEMCODE"));
					skkqbDynaBean.set("SKKQB_SKZT_NAME",dic.get("DICTIONARYITEM_ITEMNAME"));
				}
				//是否有效
				DynaBean dic2 = jgmesCommon.getDic("JGMES_YES_NO",isvalid);
				if(dic!=null){
					skkqbDynaBean.set("SKKQB_NO_CODE",dic2.get("DICTIONARYITEM_ITEMCODE"));
					skkqbDynaBean.set("SKKQB_NO_NAME",dic2.get("DICTIONARYITEM_ITEMNAME"));
				}
				if(userDynaBean!=null){
					//用户编码
					skkqbDynaBean.set("SKKQB_YHBM",userDynaBean.getStr("USERCODE"));
					//用户名称
					skkqbDynaBean.set("SKKQB_YHMC",userDynaBean.getStr("USERNAME"));
				}else{
					//用户编码
					skkqbDynaBean.set("SKKQB_YHBM","");
					//用户名称
					skkqbDynaBean.set("SKKQB_YHMC","未知用户");
				}
				skkqbDynaBean = serviceTemplate.insert(skkqbDynaBean);
				if(hashMap.get("Duplication")!=null&&!"".equals(hashMap.get("Duplication").toString())){
				}else{
					hashMap.put("Duplication",isvalid);
				}
				hashMap.put("SKKQB_YHMC",skkqbDynaBean.getStr("SKKQB_YHMC"));
				hashMap.put("SKKQB_SKSJ",skkqbDynaBean.getStr("SKKQB_SKSJ"));
				ret.Data = hashMap;
			}else{
				ret.setMessage("未根据产线编码获取到产线信息！");
			}
//		} else {
//			ret.setMessage("未根据工号获取到用户信息！");
//		}
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