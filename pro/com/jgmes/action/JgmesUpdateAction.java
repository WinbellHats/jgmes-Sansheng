package com.jgmes.action;

import java.util.HashMap;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.je.core.action.DynaAction;
import com.je.core.util.bean.DynaBean;
import com.jgmes.util.JgmesCommon;
import com.jgmes.util.JgmesResult;
/**
 * 
 * @author lxc
 * @version 2019-01-31
 * @see /jgmes/jgmesUpdateAction!doCheckUpdate.action
 */
@Component("jgmesUpdateAction")
@Scope("prototype")
public class JgmesUpdateAction extends DynaAction {
//	public JgmesUser jgmesUser = null;
//	public JgmesUpdateAction(HttpServletRequest requestT,PCDynaServiceTemplate serviceTemplateT) {
//		request = requestT;
//		serviceTemplate = serviceTemplateT;
//		jgmesUser =new JgmesUser(requestT,serviceTemplateT);
//	}
	/*
	 * 	壳升级管理
	 */
	public void doCheckUpdate() {
		//String ParrentID="e8b4eb90-1a21-4fdf-bfca-6c53409a80a8";
		String mac = request.getParameter("mac");
		String ip = request.getParameter("ip");
		String currentVer = request.getParameter("currentVer");
		String updateType = request.getParameter("updateType");
		JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
		String jsonStr="";
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
		if(mac!=null&&!mac.isEmpty()&&ip!=null&&!ip.isEmpty()&&updateType!=null
				&&!updateType.isEmpty()&&currentVer!=null&&!currentVer.isEmpty()) {
			//查询最新版本号 updateType
			DynaBean newVersions  = serviceTemplate.selectOne("JGMES_SYS_PACKAGE", " and PACKAGE_PACKAGETYPE_CODE='"+updateType+"' and PACKAGE_NO_CODE='1'");
			//升级包版本
			String newVersionsNum=(String) newVersions.get("PACKAGE_SJBBBH");
			//升级包名称
			String package_SJBMC=(String) newVersions.get("PACKAGE_SJBMC");
			//最新版本升级包编号
			String package_SJBBH=(String) newVersions.get("PACKAGE_SJBBH");
			//主键ID
			String JGMES_SYS_PACKAGE_ID=(String) newVersions.get("JGMES_SYS_PACKAGE_ID");
			if(compareVersion(currentVer,newVersionsNum)>=0) {
				//ret.setMessage("已经是最新版本!");
				ret.IsSuccess=false;
			}else {
				ret.IsSuccess=true;
				//获取下载路经
				String PACKAGE_SJB=(String) newVersions.get("PACKAGE_SJB");
				String path = PACKAGE_SJB.substring(PACKAGE_SJB.indexOf("*")+1, PACKAGE_SJB.length());
				newVersions.set("PACKAGE_SJB", path);
				ret.Data=newVersions.getValues();
				DynaBean selectOne = serviceTemplate.selectOne("JGMES_SYS_UPDATERECORD", " and UPDATERECORD_SJBBH='"+package_SJBBH+"'  and UPDATERECORD_MACDZ='"+mac+"'" );
				if(selectOne!=null) {
					//ret.setMessage("请安装最新版本！");
				}else {
					DynaBean newBean = new DynaBean("JGMES_SYS_UPDATERECORD", true);
					//升级包编号
					newBean.set("UPDATERECORD_SJBBH", package_SJBBH);
					//升级包名称
					newBean.set("UPDATERECORD_SJBMC", package_SJBMC );
					//升级包类型
					DynaBean dic1 = jgmesCommon.getDic("JGMES_DIC_PACKAGETYPE", updateType);
					newBean.set("UPDATERECORD_PACKAGETYPE_ID",dic1.get("JE_CORE_DICTIONARYITEM_ID"));//数据字典
					newBean.set("UPDATERECORD_PACKAGETYPE_CODE", dic1.get("DICTIONARYITEM_ITEMCODE"));//数据字典
					newBean.set("UPDATERECORD_PACKAGETYPE_NAME", dic1.get("DICTIONARYITEM_ITEMNAME"));
					//升级包 外键ID
					newBean.set("JGMES_SYS_PACKAGE_ID", JGMES_SYS_PACKAGE_ID);
					//newBean.set("PACKAGE_SJBMC", package_SJBMC);
					//newBean.set("UPDATERECORD_PACKAGETYPE_NAME", updateTypes);
					//MAC地址
					newBean.set("UPDATERECORD_MACDZ", mac);
					//IP地址
					newBean.set("UPDATERECORD_IPDZ",ip);
					//版本
					newBean.set("UPDATERECORD_SJBBB",newVersionsNum);
					//是否是最新版本
					DynaBean dic = jgmesCommon.getDic("JGMES_YES_NO", "1");
					newBean.set("UPDATERECORD_NO_ID", dic.get("JE_CORE_DICTIONARYITEM_ID"));//数据字典
					newBean.set("UPDATERECORD_NO_CODE",dic.get("DICTIONARYITEM_ITEMCODE"));
					newBean.set("UPDATERECORD_NO_NAME",dic.get("DICTIONARYITEM_ITEMNAME"));
					try {
						jgmesCommon.setDynaBeanInfo(newBean);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						System.out.println("壳管理添加基本信息出错"+e.getMessage());
						e.printStackTrace();
					}
					serviceTemplate.insert(newBean);
				}
			}
		}else {
			//ret.IsSuccess=true;
			//ret.setMessage("请联系管理员!");
		}
		jsonStr = jsonBuilder.toJson(ret);
		System.out.println(jsonStr);
		toWrite(jsonStr);
	}
	
	
	public static int compareVersion(String newv1, String v2) {
        if (newv1.equals(v2)) {
            return 0;
        }
        String[] version1Array = newv1.split("[._]");
        String[] version2Array = v2.split("[._]");
        int index = 0;
        int minLen = Math.min(version1Array.length, version2Array.length);
        long diff = 0;
        
        while (index < minLen
                && (diff = Long.parseLong(version1Array[index])
                - Long.parseLong(version2Array[index])) == 0) {
            index++;
        }
        if (diff == 0) {
            for (int i = index; i < version1Array.length; i++) {
                if (Long.parseLong(version1Array[i]) > 0) {
                    return 1;
                }
            }
            
            for (int i = index; i < version2Array.length; i++) {
                if (Long.parseLong(version2Array[i]) > 0) {
                    return -1;
                }
            }
            return 0;
        } else {
            return diff > 0 ? 1 : -1;
        }
    }
	
}
