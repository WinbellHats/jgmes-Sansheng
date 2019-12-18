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
import com.jgmes.action.JgmesFirstInspectionAction;
import com.jgmes.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 首件检验的service
 * @author liuc
 * @version 2019-04-17 20:57:26
 */
@Component("jgmesFirstInspectionService")
public class JgmesFirstInspectionServiceImpl implements JgmesFirstInspectionService  {

	private static final Logger logger = LoggerFactory.getLogger(JgmesFirstInspectionAction.class);

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
	public JgmesResult<List<HashMap>> getEffectiveProductStandard(String cpbm, String jylx) {
		logger.debug("产品编码："+cpbm+",检验类型："+jylx);
		JgmesCommon jgmesCommon = new JgmesCommon(null, serviceTemplate);
		JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
//		if(cpbm==null||cpbm.isEmpty()){
//			ret.setMessage("产品编码不能为空！");
//			logger.debug("产品编码不能为空！");
//		}
		if(jylx==null||jylx.isEmpty()){
			ret.setMessage("检验类型不能为空！");
			logger.debug("检验类型不能为空！");
		}
		//校验必填项是否必填
		boolean existed = (jylx!=null&&!jylx.isEmpty());
		logger.debug("existed:"+existed);
		if(existed){
		    String cpSql = "";
		    if(cpbm!=null&&!"".equals(cpbm)){
                cpSql = " and CPJYBZ_CPBM = '"+cpbm+"'";
            }
			List<DynaBean>  cpjyDynaBean= serviceTemplate.selectList("V_JGMES_ZLGL_CPJYBZ",cpSql+" and CPJYBZ_JYLX_CODE = '"+jylx+"' and CPJYBZ_USESTATUS_CODE = '0' order by CPJYBZ_NO_CODE,CPJYBZ_BZMC");
			logger.debug("cpjyDynaBean的数量:"+cpjyDynaBean.size());
			if(cpjyDynaBean!=null&&cpjyDynaBean.size()>0){
				ret.Data = ret.getValues(cpjyDynaBean);
			}
		}
		return ret;
	}

	@Override
	public JgmesResult<List<TreeDto>> getEffectiveProductStandardChild(String id,String sjzbId,String xjzbId,String rootId) {
		logger.debug("产品检验主键ID:"+id);
		JgmesCommon jgmesCommon = new JgmesCommon(null, serviceTemplate);
		JgmesResult<List<TreeDto>> ret = new JgmesResult<List<TreeDto>>();
		if((id==null||id.isEmpty())&&(sjzbId==null||sjzbId.isEmpty())&&(xjzbId==null||xjzbId.isEmpty())){
			ret.setMessage("产品检验标准主键和首件检验，巡检不能同时为空不能为空！");
		}

        //根节点
        List<TreeDto> rootList = new ArrayList<TreeDto>();
        //子节点
        List<TreeDto> bodyList = new ArrayList<TreeDto>();
        List<InsItemDynaBean> insItemDynaBeanList = new ArrayList<>();
        List<DynaBean>  dynaBeanList= new ArrayList<>();
        if(id!=null&&!"".equals(id)){
        	String sql1 = "select V_JGMES_ZLGL_CPJYBZZB.*,JGMES_ZLGL_JYXMDA.JYXMDA_FLBM\r\n" + 
        			"from V_JGMES_ZLGL_CPJYBZZB \r\n" + 
        			"LEFT JOIN JGMES_ZLGL_JYXMDA on JGMES_ZLGL_JYXMDA.JYXMDA_XMBM = V_JGMES_ZLGL_CPJYBZZB.CPJYBZZB_JYXMBM\r\n" + 
        			"where V_JGMES_ZLGL_CPJYBZZB.JGMES_ZLGL_CPJYBZ_ID = '"+id+"'"+
        			" ORDER BY V_JGMES_ZLGL_CPJYBZZB.SY_ORDERINDEX ";
            dynaBeanList= serviceTemplate.selectListBySql(sql1);
            //获取分类编码
            String sql =  "select V_JGMES_ZLGL_CPJYBZZB.JGMES_ZLGL_CPJYBZ_ID,JGMES_ZLGL_JCXMFL.JGMES_ZLGL_JCXMFL_ID\r\n" + 
            		"from V_JGMES_ZLGL_CPJYBZZB \r\n" + 
            		"LEFT JOIN JGMES_ZLGL_JYXMDA on JGMES_ZLGL_JYXMDA.JYXMDA_XMBM = V_JGMES_ZLGL_CPJYBZZB.CPJYBZZB_JYXMBM\r\n" + 
            		"LEFT JOIN JGMES_ZLGL_JCXMFL on JGMES_ZLGL_JYXMDA.JYXMDA_FLBM = JGMES_ZLGL_JCXMFL.JCXMFL_FLBM\r\n" + 
            		"where V_JGMES_ZLGL_CPJYBZZB.JGMES_ZLGL_CPJYBZ_ID = '"+id+"'"+
            		"GROUP BY JGMES_ZLGL_JYXMDA.JYXMDA_FLBM\r\n";
            List<DynaBean> flDynaBeanList = serviceTemplate.selectListBySql(sql);
            if(flDynaBeanList!=null&&flDynaBeanList.size()>0) {
				for(DynaBean flDynaBean:flDynaBeanList) {
					String jcxmflId = flDynaBean.getStr("JGMES_ZLGL_JCXMFL_ID");
					if(jcxmflId!=null&&!"".equals(jcxmflId)) {
						Map<String, List<TreeDto>> map = getXmFlTree(jcxmflId,rootId,rootList,bodyList);
						rootList = map.get("root");
						bodyList = map.get("body");
					}
				}
			  rootList = getCpTreeByJyxm(rootList,dynaBeanList);
              bodyList = getCpTreeByJyxm(bodyList,dynaBeanList);

              TreeToolUtils utils =  new TreeToolUtils(rootList,bodyList);
              List<TreeDto> result =  utils.getTree();

              ret.Data = result;
            }
        }else if(sjzbId!=null&&!"".equals(sjzbId)){
        	String sql = "select V_JGMES_ZLGL_SJJYZB.*,JGMES_ZLGL_JYXMDA.JYXMDA_FLBM\r\n" + 
        			"from V_JGMES_ZLGL_SJJYZB \r\n" + 
        			"LEFT JOIN JGMES_ZLGL_JYXMDA on JGMES_ZLGL_JYXMDA.JYXMDA_XMBM = V_JGMES_ZLGL_SJJYZB.SJJYZB_JYXMBM\r\n" + 
        			"where V_JGMES_ZLGL_SJJYZB.JGMES_ZLGL_SJJY_ID = '"+sjzbId+"'"+
        			" ORDER BY V_JGMES_ZLGL_SJJYZB.SY_ORDERINDEX ";
            dynaBeanList= serviceTemplate.selectListBySql(sql);
            
            
          //获取分类编码
            String sql1 =  "select JGMES_ZLGL_SJJYZB.JGMES_ZLGL_SJJYZB_ID,JGMES_ZLGL_JCXMFL.JGMES_ZLGL_JCXMFL_ID\r\n" + 
            		"from JGMES_ZLGL_SJJYZB \r\n" + 
            		"LEFT JOIN JGMES_ZLGL_JYXMDA on JGMES_ZLGL_JYXMDA.JYXMDA_XMBM = JGMES_ZLGL_SJJYZB.SJJYZB_JYXMBM\r\n" + 
            		"LEFT JOIN JGMES_ZLGL_JCXMFL on JGMES_ZLGL_JYXMDA.JYXMDA_FLBM = JGMES_ZLGL_JCXMFL.JCXMFL_FLBM\r\n" + 
            		"where JGMES_ZLGL_SJJYZB.JGMES_ZLGL_SJJY_ID = '"+sjzbId+"'"+
            		"GROUP BY JGMES_ZLGL_JYXMDA.JYXMDA_FLBM\r\n";
            List<DynaBean> flDynaBeanList = serviceTemplate.selectListBySql(sql1);
            if(flDynaBeanList!=null&&flDynaBeanList.size()>0) {
				for(DynaBean flDynaBean:flDynaBeanList) {
				      Map<String, List<TreeDto>> map = getXmFlTree(flDynaBean.getStr("JGMES_ZLGL_JCXMFL_ID"),rootId,rootList,bodyList);
				      rootList = map.get("root");
				      bodyList = map.get("body");
				}
			  rootList = getCpTreeByJyxm(rootList,dynaBeanList);
              bodyList = getCpTreeByJyxm(bodyList,dynaBeanList);

              TreeToolUtils utils =  new TreeToolUtils(rootList,bodyList);
              List<TreeDto> result =  utils.getTree();

              ret.Data = result;
            }
        }else if(xjzbId!=null&&!"".equals(xjzbId)){
        	String sql = "select JGMES_ZLGL_XJZBZB.*,JGMES_ZLGL_JYXMDA.JYXMDA_FLBM\r\n" + 
        			"from JGMES_ZLGL_XJZBZB \r\n" + 
        			"LEFT JOIN JGMES_ZLGL_JYXMDA on JGMES_ZLGL_JYXMDA.JYXMDA_XMBM = JGMES_ZLGL_XJZBZB.XJZBZB_JYXMBM\r\n" + 
        			"where JGMES_ZLGL_XJZBZB.XJZBZB_XJZBID = '"+xjzbId+"'"+
        			" ORDER BY JGMES_ZLGL_XJZBZB.SY_ORDERINDEX ";
            dynaBeanList= serviceTemplate.selectListBySql(sql);
            
            
          //获取分类编码
            String sql1 =  "select JGMES_ZLGL_XJZBZB.JGMES_ZLGL_XJZBZB_ID,JGMES_ZLGL_JCXMFL.JGMES_ZLGL_JCXMFL_ID\r\n" + 
            		"from JGMES_ZLGL_XJZBZB \r\n" + 
            		"LEFT JOIN JGMES_ZLGL_JYXMDA on JGMES_ZLGL_JYXMDA.JYXMDA_XMBM = JGMES_ZLGL_XJZBZB.XJZBZB_JYXMBM\r\n" +
            		"LEFT JOIN JGMES_ZLGL_JCXMFL on JGMES_ZLGL_JYXMDA.JYXMDA_FLBM = JGMES_ZLGL_JCXMFL.JCXMFL_FLBM\r\n" + 
            		"where JGMES_ZLGL_XJZBZB.XJZBZB_XJZBID = '"+xjzbId+"'"+
            		"GROUP BY JGMES_ZLGL_JYXMDA.JYXMDA_FLBM\r\n";
            List<DynaBean> flDynaBeanList = serviceTemplate.selectListBySql(sql1);
            if(flDynaBeanList!=null&&flDynaBeanList.size()>0) {
				for(DynaBean flDynaBean:flDynaBeanList) {
				      Map<String, List<TreeDto>> map = getXmFlTree(flDynaBean.getStr("JGMES_ZLGL_JCXMFL_ID"),rootId,rootList,bodyList);
				      rootList = map.get("root");
				      bodyList = map.get("body");
				}
			  rootList = getCpTreeByJyxm(rootList,dynaBeanList);
              bodyList = getCpTreeByJyxm(bodyList,dynaBeanList);

              TreeToolUtils utils =  new TreeToolUtils(rootList,bodyList);
              List<TreeDto> result =  utils.getTree();

              ret.Data = result;
            }
        }
		return ret;
	}

    @Override
    public JgmesResult<String> doSaveFirstInspection(String jsonStr, String jsonStrDetail,String jsonStrRwGl,HttpServletRequest requestT,String userCode) {
        logger.debug("首检主表："+jsonStr+",首检子表："+jsonStrDetail);
        JgmesCommon jgmesCommon = new JgmesCommon(requestT, serviceTemplate,userCode);
        JgmesResult<String> ret = new JgmesResult<String>();
        if((jsonStr==null||jsonStr.isEmpty())&&(jsonStrDetail==null||jsonStrDetail.isEmpty())){
            ret.setMessage("首检主表和首件子表不能同时为空！");
            logger.debug("首检主表和首件子表不能同时为空！");
        }
        //校验必填项是否必填
        boolean existed = (jsonStr!=null&&!jsonStr.isEmpty())&&(jsonStrDetail!=null&&!jsonStrDetail.isEmpty());
        logger.debug("existed:"+existed);
        if(existed){
            // 将前台数据转到对象中
            try {
                //转换为首件检验主表
                DynaBean sjb = jgmesCommon.getDynaBeanByJsonStr("JGMES_ZLGL_SJJY", jsonStr);
                if(sjb!=null){
                    DynaBean dic = jgmesCommon.getDic("JGMES_YES_NO",sjb.getStr("SJJY_NO_CODE"));
                    if(sjb.getStr("JGMES_ZLGL_SJJY_ID")!=null&&!"".equals(sjb.getStr("JGMES_ZLGL_SJJY_ID"))){
                        if(dic!=null) {
                            //是否合格
                            sjb.set("SJJY_NO_NAME",dic.get("DICTIONARYITEM_ITEMNAME"));
                            sjb.set("SJJY_NO_CODE",dic.get("DICTIONARYITEM_ITEMCODE"));
                        }
                        sjb.set("SJJY_DJRQ",jgmesCommon.getCurrentDate());
                        sjb.set("SJJY_JYSJ",jgmesCommon.getCurrentTime());
                        serviceTemplate.update(sjb);
                    }else{
                        if(dic!=null) {
                            //是否合格
                            sjb.set("SJJY_NO_NAME",dic.get("DICTIONARYITEM_ITEMNAME"));
                            sjb.set("SJJY_NO_CODE",dic.get("DICTIONARYITEM_ITEMCODE"));
                        }
                        sjb.set("JGMES_ZLGL_SJJY_ID", JEUUID.uuid());
                        sjb.set("SJJY_DJRQ",jgmesCommon.getCurrentDate());
                        sjb.set("SJJY_JYSJ",jgmesCommon.getCurrentTime());
                        sjb.set("SJJY_DJH",serviceTemplate.buildCode("SJJY_DJH", "V_JGMES_ZLGL_SJJY", sjb));
                        sjb = serviceTemplate.insert(sjb);
                    }
                }

                List<DynaBean> sjzbList = jgmesCommon.getListDynaBeanByJsonStr("JGMES_ZLGL_SJJYZB",jsonStrDetail);
                List<DynaBean> sjzbListforInsert = new ArrayList<DynaBean>();
                if(sjzbList!=null&&sjzbList.size()>0){
                    for (DynaBean sjzb:sjzbList){

                        DynaBean sjb1 = serviceTemplate.selectOne("JGMES_ZLGL_SJJY"," and JGMES_ZLGL_SJJY_ID = '"+sjzb.getStr("JGMES_ZLGL_SJJY_ID")+"'");
                        if(sjb1!=null){
                            sjb = sjb1;
                        }
                        DynaBean sjzbforInsert = new DynaBean();
                        //获取检验方案子表主键ID 并根据ID获取检验项目
                        DynaBean jyzbDynaBean = serviceTemplate.selectOne("V_JGMES_ZLGL_CPJYBZZB"," and JGMES_ZLGL_CPJYBZZB_ID = '"+sjzb.getStr("JGMES_ZLGL_CPJYBZZB_ID")+"'");
                        if(jyzbDynaBean!=null){
                            sjzbforInsert.set(BeanUtils.KEY_TABLE_CODE,"JGMES_ZLGL_SJJYZB");
                            //检验项目编码
                            sjzbforInsert.set("SJJYZB_JYXMBM",jyzbDynaBean.getStr("CPJYBZZB_JYXMBM"));
                            //检验要求
                            sjzbforInsert.set("SJJYZB_JYYQ",jyzbDynaBean.getStr("CPJYBZZB_JYYQ"));
                            //是否检验
                            sjzbforInsert.set("SJJYZB_NO_NAME",jyzbDynaBean.getStr("CPJYBZZB_NO_NAME"));
                            sjzbforInsert.set("SJJYZB_NO_CODE",jyzbDynaBean.getStr("CPJYBZZB_NO_CODE"));
                            //是否备注
                            sjzbforInsert.set("SJJYZB_BZ_NAME",jyzbDynaBean.getStr("CPJYBZZB_BZ_NAME"));
                            sjzbforInsert.set("SJJYZB_BZ_CODE",jyzbDynaBean.getStr("CPJYBZZB_BZ_CODE"));
                            //是否附件
                            sjzbforInsert.set("SJJYZB_FJ_NAME",jyzbDynaBean.getStr("CPJYBZZB_FJ_NAME"));
                            sjzbforInsert.set("SJJYZB_FJ_CODE",jyzbDynaBean.getStr("CPJYBZZB_FJ_CODE"));
                            //是否必检
                            sjzbforInsert.set("SJJYZB_BJ_NAME",jyzbDynaBean.getStr("CPJYBZZB_BJ_NAME"));
                            sjzbforInsert.set("SJJYZB_BJ_CODE",jyzbDynaBean.getStr("CPJYBZZB_BJ_CODE"));
                            //参考值
                            sjzbforInsert.set("SJJYZB_CKZ",jyzbDynaBean.getStr("CPJYBZZB_CKZ"));
                            DynaBean dic = null;
                            dic = jgmesCommon.getDic("JGMES_YES_NO",sjzb.getStr("SJJYZB_HG_CODE"));
//                            if(isQualified(sjzb.getInt("SJJYZB_JYZ"),jyzbDynaBean.getInt("CPJYBZZB_ZDZ"),jyzbDynaBean.getInt("CPJYBZZB_ZXZ"),jyzbDynaBean.getInt("CPJYBZZB_CKZ"),jyzbDynaBean.getInt("CPJYBZZB_JYPDFL_CODE"))){
//                                dic = jgmesCommon.getDic("JGMES_YES_NO","1");
//                            }else{
//                                dic = jgmesCommon.getDic("JGMES_YES_NO","0");
//                            }
                            if(dic!=null) {
                                //是否合格
                                sjzbforInsert.set("SJJYZB_HG_NAME",dic.get("DICTIONARYITEM_ITEMNAME"));
                                sjzbforInsert.set("SJJYZB_HG_CODE",dic.get("DICTIONARYITEM_ITEMCODE"));
                            }

                            //检验值
                            sjzbforInsert.set("SJJYZB_JYZ",sjzb.getStr("SJJYZB_JYZ"));
                            //备注
                            sjzbforInsert.set("SJJYZB_BZ",sjzb.getStr("SJJYZB_BZ"));
                            //最大值
                            sjzbforInsert.set("SJJYZB_ZDZ",jyzbDynaBean.getStr("CPJYBZZB_ZDZ"));
                            //最小值
                            sjzbforInsert.set("SJJYZB_ZXZ",jyzbDynaBean.getStr("CPJYBZZB_ZXZ"));
                            //检验判断分类
                            sjzbforInsert.set("SJJYZB_JYPDFL_NAME",jyzbDynaBean.getStr("CPJYBZZB_JYPDFL_NAME"));
                            sjzbforInsert.set("SJJYZB_JYPDFL_CODE",jyzbDynaBean.getStr("CPJYBZZB_JYPDFL_CODE"));
                            //首件检验主表外键ID
                            sjzbforInsert.set("JGMES_ZLGL_SJJY_ID",sjb.getStr("JGMES_ZLGL_SJJY_ID"));
                            //检验方案子表ID
                            sjzbforInsert.set("SJJYZB_JYFAZBZJID",sjzb.getStr("SJJYZB_JYFAZBZJID"));
                            if(sjzb.getStr("JGMES_ZLGL_SJJYZB_ID")!=null&&!"".equals(sjzb.getStr("JGMES_ZLGL_SJJYZB_ID"))){
                            	sjzbforInsert.set("JGMES_ZLGL_SJJYZB_ID",sjzb.getStr("JGMES_ZLGL_SJJYZB_ID"));
                                serviceTemplate.update(sjzbforInsert);
                            }else{
                                sjzbforInsert.set("JGMES_ZLGL_SJJYZB_ID",JEUUID.uuid());
                                serviceTemplate.insert(sjzbforInsert);
                            }
                        }
                    }
                }

                if(jsonStrRwGl!=null&&!"".equals(jsonStrRwGl)&&sjb!=null){
                    List<DynaBean> rwglDynaBeanList = jgmesCommon.getListDynaBeanByJsonStr("JGMES_ZLGL_RWDXXGLB", jsonStrRwGl);
                    List<DynaBean> xibzbDynaBeanListForInsert = new ArrayList<>();
                    for(DynaBean rwglDynaBean:rwglDynaBeanList){
                        if(rwglDynaBean.getStr("JGMES_ZLGL_RWDXXGLB_ID")!=null&&!"".equals(rwglDynaBean.getStr("JGMES_ZLGL_RWDXXGLB_ID"))){
                            rwglDynaBean.set("RWDXXGLB_JYDGLID",sjb.getStr("JGMES_ZLGL_SJJY_ID"));
                            serviceTemplate.update(rwglDynaBean);
                        }else{
                            rwglDynaBean.set("JGMES_ZLGL_RWDXXGLB_ID",JEUUID.uuid());
                            rwglDynaBean.set("RWDXXGLB_JYDGLID",sjb.getStr("JGMES_ZLGL_SJJY_ID"));
                            serviceTemplate.insert(rwglDynaBean);
                        }
                    }
                }
            } catch (ParseException e) {
                logger.error("系统发生异常："+e.toString());
                ret.setMessage("系统发生异常，请联系管理员！");
                e.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * 判断检验值是否合格
     * @param jyz 检验值
     * @param zdz 最大值
     * @param zxz 最小值
     * @param code 检验标准
     * @return
     */
    public boolean isQualified(int jyz,int zdz,int zxz,int ckz,int code){
        switch(code) {
            case 1:
                if(jyz==ckz){
                    return true;
                }else{
                    return false;
                }

            case 2:
                if(jyz>=zxz&&jyz<=zdz){
                    return true;
                }else{
                    return false;
                }
            case 3:
                if(jyz<=zxz||jyz>=zdz){
                    return true;
                }else{
                    return false;
                }
            default:
                return false;
        }
    }

    @Override
    public JgmesResult<List<HashMap>> getInspectionItemRootClassify(String id) {
        logger.debug("产品检验主键ID:"+id);
        JgmesCommon jgmesCommon = new JgmesCommon(null, serviceTemplate);
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        if(id==null||id.isEmpty()){
            ret.setMessage("产品检验标准主键不能为空！");
        }

        //根节点
        List<TreeDto> rootList = new ArrayList<TreeDto>();
        //子节点
        List<TreeDto> bodyList = new ArrayList<TreeDto>();

        if(id!=null&&!id.isEmpty()){
        	//获取分类编码
            String sql =  "select V_JGMES_ZLGL_CPJYBZZB.JGMES_ZLGL_CPJYBZ_ID,JGMES_ZLGL_JCXMFL.JGMES_ZLGL_JCXMFL_ID\r\n" + 
            		"from V_JGMES_ZLGL_CPJYBZZB \r\n" + 
            		"LEFT JOIN JGMES_ZLGL_JYXMDA on JGMES_ZLGL_JYXMDA.JYXMDA_XMBM = V_JGMES_ZLGL_CPJYBZZB.CPJYBZZB_JYXMBM\r\n" + 
            		"LEFT JOIN JGMES_ZLGL_JCXMFL on JGMES_ZLGL_JYXMDA.JYXMDA_FLBM = JGMES_ZLGL_JCXMFL.JCXMFL_FLBM\r\n" + 
            		"where V_JGMES_ZLGL_CPJYBZZB.JGMES_ZLGL_CPJYBZ_ID = '"+id+"'"+
            		"GROUP BY JGMES_ZLGL_JYXMDA.JYXMDA_FLBM\r\n";
            List<DynaBean> flDynaBeanList = serviceTemplate.selectListBySql(sql);
            if(flDynaBeanList!=null&&flDynaBeanList.size()>0) {
				for(DynaBean flDynaBean:flDynaBeanList) {
					String jcxmflId = flDynaBean.getStr("JGMES_ZLGL_JCXMFL_ID");
					if(jcxmflId!=null&&!"".equals(jcxmflId)) {
						Map<String, List<TreeDto>> map = getXmFlTree(jcxmflId,null,rootList,bodyList);
					      rootList = map.get("root");
					      bodyList = map.get("body");
					}
				}
				rootList.sort(new Comparator<TreeDto>() {
		        	public int compare(TreeDto o1,TreeDto o2){
		                return o1.getSyTreeOrderIndex().compareTo(o2.getSyTreeOrderIndex());
		            }
		        });
				List<DynaBean> rootDynaBeanList = new ArrayList<DynaBean>();
				if(rootList!=null&&rootList.size()>0) {
					for(TreeDto root:rootList){
		                  DynaBean rootDynaBean = new DynaBean();
		                  rootDynaBean.set("JGMES_ZLGL_JCXMFL_ID",root.getId());
		                  rootDynaBean.set("JCXMFL_FLBM",root.getFlbm());
		                  rootDynaBean.set("JCXMFL_FLMC",root.getFlmc());
		                  rootDynaBean.set("SY_PARENT",root.getSyParent());
		                  rootDynaBeanList.add(rootDynaBean);
		              }
				}
              ret.Data = ret.getValues(rootDynaBeanList);
            }
        }
        return ret;
    }

    @Override
    public JgmesResult<List<HashMap>> getFirstInspection(String antistop) {
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
	    StringBuffer whereSql = new StringBuffer();
	    if(antistop!=null&&!antistop.isEmpty()){
            whereSql.append(" and (SJJY_RWDH like '%"+antistop+"%' or SJJY_DJH like '%"+antistop+"%' or SJJY_DDH like '%"+antistop+"%' or SJJY_DJRQ like '%"+antistop+"%' or PRODUCTDATA_NAME like '%"+antistop+"%' or PRODUCTDATA_BH like '%"+antistop+"%' or SJJY_SJR like '%"+antistop+"%' or SJJY_JYR like '%"+antistop+"%')");
        }
        whereSql.append(" order by SJJY_JYSJ desc ");
        List<DynaBean> firstInspectionList = serviceTemplate.selectList("V_JGMES_ZLGL_SJJY",whereSql.toString());
	    if(firstInspectionList!=null&&firstInspectionList.size()>0){
            for (DynaBean dynaBean : firstInspectionList) {
                String JGMES_ZLGL_SJJY_ID=dynaBean.getStr("JGMES_ZLGL_SJJY_ID");
                List<DynaBean> rwglList = serviceTemplate.selectList("JGMES_ZLGL_RWDXXGLB"," and RWDXXGLB_JYDGLID = '"+JGMES_ZLGL_SJJY_ID+"'");
                dynaBean.set("rwglList",rwglList);
            }
            ret.Data = ret.getValues(firstInspectionList);
        }
        return ret;
    }

    @Override
    public JgmesResult<List<HashMap>> getFirstInspectionChild(String id) {
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        List<DynaBean> firstInspectionChildList = serviceTemplate.selectList("V_JGMES_ZLGL_SJJYZB"," and JGMES_ZLGL_SJJY_ID = '"+id+"'");
        if(firstInspectionChildList!=null&&firstInspectionChildList.size()>0){
            ret.Data = ret.getValues(firstInspectionChildList);
        }
        return ret;
    }

    @Override
    public JgmesResult<List<HashMap>> getRoutingInspection(String antistop) {
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        StringBuffer whereSql = new StringBuffer();
        if(antistop!=null&&!antistop.isEmpty()){
            whereSql.append(" and (XJZB_RWDH like '%"+antistop+"%' or XJZB_DDH like '%"+antistop+"%' or XJZB_KHMC like '%"+antistop+"%' or XJZB_XJSJ like '%"+antistop+"%' or XJZB_DJH like '%"+antistop+"%' or XJZB_CPMC like '%"+antistop+"%' or XJZB_CPBM like '%"+antistop+"%' or XJZB_XJR like '%"+antistop+"%')");
        }
        whereSql.append(" order by XJZB_XJSJ desc ");
        List<DynaBean> firstInspectionList = serviceTemplate.selectList("JGMES_ZLGL_XJZB",whereSql.toString());
        if(firstInspectionList!=null&&firstInspectionList.size()>0){
            for (DynaBean dynaBean : firstInspectionList) {
                String JGMES_ZLGL_XJZB_ID=dynaBean.getStr("JGMES_ZLGL_XJZB_ID");
                List<DynaBean> rwglList = serviceTemplate.selectList("JGMES_ZLGL_RWDXXGLB"," and RWDXXGLB_JYDGLID = '"+JGMES_ZLGL_XJZB_ID+"'");
                dynaBean.set("rwglList",rwglList);
            }
                ret.Data = ret.getValues(firstInspectionList);
            }
        return ret;
    }

    @Override
    public JgmesResult<List<HashMap>> getRWDXXGLB(String id) {
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        List<DynaBean> rwglList = serviceTemplate.selectList("JGMES_ZLGL_RWDXXGLB"," and RWDXXGLB_JYDGLID = '"+id+"'");
        if(rwglList!=null&&rwglList.size()>0){
            ret.Data = ret.getValues(rwglList);
        }
        return ret;
    }

    /**
     * 根据检测项目来获取产品检验树root
     * @return
     */
    public ArrayList<TreeDto> getCpTreeByJyxm(List<TreeDto> treeList,List<DynaBean> jybzzbDynaBeanList){
        ArrayList<TreeDto> list = new ArrayList<>();
        if(treeList == null || treeList.size() == 0){
            return list;
        }
        for(TreeDto tree:treeList){
            //根据检测项目来获取产品检验树
            List<DynaBean> insItemList  = new ArrayList<DynaBean>();
            for(DynaBean DynaBean:jybzzbDynaBeanList){
                if(tree.getFlbm()!=null&&tree.getFlbm().equals(DynaBean.get("JYXMDA_FLBM"))){
                    insItemList.add(DynaBean);
                }
            }
            tree.setData(insItemList);
            list.add(tree);
        }
        list.sort(new Comparator<TreeDto>() {
        	public int compare(TreeDto o1,TreeDto o2){
                return o1.getSyTreeOrderIndex().compareTo(o2.getSyTreeOrderIndex());
            }
        });
        return list;
    }

	public List<InsItemDynaBean> getCpjyTree(List<DynaBean> jcxmDynaBeanList){
        List<InsItemDynaBean> insItemDynaBeanList = new ArrayList<>();
        if(jcxmDynaBeanList!=null&&jcxmDynaBeanList.size()>0){
            for(DynaBean jcxmDynaBean:jcxmDynaBeanList){
            	InsItemDynaBean insItemDynaBean = new InsItemDynaBean();
            	insItemDynaBean.setInsItem(jcxmDynaBean);
            	//检验子表
            	List<DynaBean> cpjyzbDynaBeanList = serviceTemplate.selectList("V_JGMES_ZLGL_CPJYBZZB"," and CPJYBZZB_JYXMBM = '"+jcxmDynaBean.getStr("JYXMDA_XMBM")+"'");
            	insItemDynaBean.setProductInsStandardChildList(cpjyzbDynaBeanList);
                insItemDynaBeanList.add(insItemDynaBean);
            }
        }
        return insItemDynaBeanList;
//        List<DynaBean> jcxmDynaBeanList = new ArrayList<>();
//        List<InsItemDynaBean> insItemDynaBeanList = new ArrayList<>();
//        if(cpjyzbDynaBeanList!=null&&cpjyzbDynaBeanList.size()>0){
//            for(DynaBean cpjyzbDynaBean:cpjyzbDynaBeanList){
//                //获取检测项目
//                DynaBean jcxmDynaBean = serviceTemplate.selectOne("JGMES_ZLGL_JYXMDA"," and JYXMDA_XMBM = '"+cpjyzbDynaBean.getStr("CPJYBZZB_JYXMBM")+"'");
//                boolean isNotDuplicate = true;
//                if(jcxmDynaBean!=null&&jcxmDynaBeanList.size()>0){
//                    for(DynaBean jcxmDynaBean1:jcxmDynaBeanList){
//                        if(jcxmDynaBean1.get("JYXMDA_XMBM").equals(jcxmDynaBean.get("JYXMDA_XMBM"))){
//                            isNotDuplicate = false;
//                        }
//                    }
//                }
//                if(isNotDuplicate){
//                    jcxmDynaBeanList.add(jcxmDynaBean);
//                }
//            }
//            if(jcxmDynaBeanList.size()>0){
//                for(DynaBean jcxmDynaBean:jcxmDynaBeanList){
//                    InsItemDynaBean insItemDynaBean = new InsItemDynaBean();
//                    insItemDynaBean.setInsItem(jcxmDynaBean);
//                    List<DynaBean> productInsStandardChildList = new ArrayList<DynaBean>();
//                    for(DynaBean cpjyzbDynaBean:cpjyzbDynaBeanList){
//                        if(cpjyzbDynaBean.getStr("JYXMDA_XMBM")!=null&&cpjyzbDynaBean.getStr("JYXMDA_XMBM").equals(jcxmDynaBean.getStr("JYXMDA_XMBM"))){
//                            productInsStandardChildList.add(cpjyzbDynaBean);
//                        }
//                    }
//                    insItemDynaBean.setProductInsStandardChildList(productInsStandardChildList);
//                    insItemDynaBeanList.add(insItemDynaBean);
//                }
//            }
//        }
//        return insItemDynaBeanList;
    }

    public List<InsItemDynaBean> getCpjyTreeBySj(List<DynaBean> cpjyzbDynaBeanList){
        List<DynaBean> jcxmDynaBeanList = new ArrayList<>();
        List<InsItemDynaBean> insItemDynaBeanList = new ArrayList<>();
        if(cpjyzbDynaBeanList!=null&&cpjyzbDynaBeanList.size()>0){
            for(DynaBean cpjyzbDynaBean:cpjyzbDynaBeanList){
                //获取检测项目
                DynaBean jcxmDynaBean = serviceTemplate.selectOne("JGMES_ZLGL_JYXMDA"," and JYXMDA_XMBM = '"+cpjyzbDynaBean.getStr("SJJYZB_JYXMBM")+"'");
                boolean isNotDuplicate = true;
                if(jcxmDynaBean!=null&&jcxmDynaBeanList.size()>0){
                    for(DynaBean jcxmDynaBean1:jcxmDynaBeanList){
                        if(jcxmDynaBean1.get("JYXMDA_XMBM").equals(jcxmDynaBean.get("JYXMDA_XMBM"))){
                            isNotDuplicate = false;
                        }
                    }
                }
                if(isNotDuplicate){
                    jcxmDynaBeanList.add(jcxmDynaBean);
                }
            }
            if(jcxmDynaBeanList.size()>0){
                for(DynaBean jcxmDynaBean:jcxmDynaBeanList){
                    InsItemDynaBean insItemDynaBean = new InsItemDynaBean();
                    insItemDynaBean.setInsItem(jcxmDynaBean);
                    List<DynaBean> productInsStandardChildList = new ArrayList<DynaBean>();
                    for(DynaBean cpjyzbDynaBean:cpjyzbDynaBeanList){
                        if(cpjyzbDynaBean.getStr("SJJYZB_JYXMBM")!=null&&cpjyzbDynaBean.getStr("SJJYZB_JYXMBM").equals(jcxmDynaBean.getStr("JYXMDA_XMBM"))){
                            productInsStandardChildList.add(cpjyzbDynaBean);
                        }
                    }
                    insItemDynaBean.setProductInsStandardChildList(productInsStandardChildList);
                    insItemDynaBeanList.add(insItemDynaBean);
                }
            }
        }
        return insItemDynaBeanList;
    }

    public List<InsItemDynaBean> getCpjyTreeByXj(List<DynaBean> cpjyzbDynaBeanList){
        List<DynaBean> jcxmDynaBeanList = new ArrayList<>();
        List<InsItemDynaBean> insItemDynaBeanList = new ArrayList<>();
        if(cpjyzbDynaBeanList!=null&&cpjyzbDynaBeanList.size()>0){
            for(DynaBean cpjyzbDynaBean:cpjyzbDynaBeanList){
                //获取检测项目
                DynaBean jcxmDynaBean = serviceTemplate.selectOne("JGMES_ZLGL_JYXMDA"," and JYXMDA_XMBM = '"+cpjyzbDynaBean.getStr("XJZBZB_JYXMBM")+"'");
                boolean isNotDuplicate = true;
                if(jcxmDynaBean!=null&&jcxmDynaBeanList.size()>0){
                    for(DynaBean jcxmDynaBean1:jcxmDynaBeanList){
                        if(jcxmDynaBean1.get("JYXMDA_XMBM").equals(jcxmDynaBean.get("JYXMDA_XMBM"))){
                            isNotDuplicate = false;
                        }
                    }
                }
                if(isNotDuplicate){
                    jcxmDynaBeanList.add(jcxmDynaBean);
                }
            }
            if(jcxmDynaBeanList.size()>0){
                for(DynaBean jcxmDynaBean:jcxmDynaBeanList){
                    InsItemDynaBean insItemDynaBean = new InsItemDynaBean();
                    insItemDynaBean.setInsItem(jcxmDynaBean);
                    List<DynaBean> productInsStandardChildList = new ArrayList<DynaBean>();
                    for(DynaBean cpjyzbDynaBean:cpjyzbDynaBeanList){
                        if(cpjyzbDynaBean.getStr("XJZBZB_JYXMBM")!=null&&cpjyzbDynaBean.getStr("XJZBZB_JYXMBM").equals(jcxmDynaBean.getStr("JYXMDA_XMBM"))){
                            productInsStandardChildList.add(cpjyzbDynaBean);
                        }
                    }
                    insItemDynaBean.setProductInsStandardChildList(productInsStandardChildList);
                    insItemDynaBeanList.add(insItemDynaBean);
                }
            }
        }
        return insItemDynaBeanList;
    }

	public Map<String, List<TreeDto>> getXmFlTree(String id,String rootID,List<TreeDto> rootList,List<TreeDto> bodyList){
        Map<String, List<TreeDto>> map = new HashMap<String, List<TreeDto>>();
        DynaBean xmflDynaBean = serviceTemplate.selectOne("JGMES_ZLGL_JCXMFL"," and JGMES_ZLGL_JCXMFL_ID = '"+id+"'");
        if(xmflDynaBean!=null){
            TreeDto<List<InsItemDynaBean>> treeDto = new TreeDto<List<InsItemDynaBean>>();
            treeDto.setId(xmflDynaBean.getStr("JGMES_ZLGL_JCXMFL_ID"));
            treeDto.setFlbm(xmflDynaBean.getStr("JCXMFL_FLBM"));
            treeDto.setFlmc(xmflDynaBean.getStr("JCXMFL_FLMC"));
            treeDto.setSyParent(xmflDynaBean.getStr("SY_PARENT"));
            treeDto.setSyTreeOrderIndex(xmflDynaBean.getStr("SY_TREEORDERINDEX"));
            if(!"ROOT".equals(xmflDynaBean.get("SY_PARENT"))){
                boolean isNotDuplicate = true;
                if(bodyList!=null&&bodyList.size()>0) {
                	for(TreeDto tree:bodyList){
                        if(tree.equals(treeDto)){
                            isNotDuplicate = false;
                        }
                    }
                }
                if(isNotDuplicate){
                	if(bodyList==null) {
                		bodyList = new ArrayList<TreeDto>();
                	}
                    bodyList.add(treeDto);
                }
                map = getXmFlTree(xmflDynaBean.getStr("SY_PARENT"),rootID,rootList,bodyList);
            }else{
                boolean isNotDuplicate = true;
                if(rootList!=null&&rootList.size()>0) {
	                for(TreeDto tree:rootList){
	                    if(tree.equals(treeDto)){
	                        isNotDuplicate = false;
	                    }
	                }
                }
                if(rootList==null) {
                	rootList = new ArrayList<TreeDto>();
                }
                if(rootID!=null&&!rootID.isEmpty()){
                    if(isNotDuplicate&&rootID.equals(xmflDynaBean.getStr("JGMES_ZLGL_JCXMFL_ID"))){
                        rootList.add(treeDto);
                    }
                }else{
                    if(isNotDuplicate){
                        rootList.add(treeDto);
                    }
                }
                map.put("root",rootList);
                map.put("body",bodyList);
            }
        }
        return map;
    }


	/**
<<<<<<< .mine
	 * 获取登录用户
||||||| .r469
	 * ��ȡ��¼�û�
=======
	 * ��ȡ��¼�û�
>>>>>>> .r478
	 * @return
	 */
	public EndUser getCurrentUser() {
		// TODO Auto-generated method stub
		return SecurityUserHolder.getCurrentUser();
	}
	/**
<<<<<<< .mine
	 * 获取登录用户所在部门
||||||| .r469
	 * ��ȡ��¼�û����ڲ���
=======
	 * ��ȡ��¼�û����ڲ���
>>>>>>> .r478
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