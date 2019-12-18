package com.demo.action;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.je.core.action.DynaAction;
import com.je.core.util.bean.DynaBean;
import com.je.phone.vo.DsInfoVo;

/**
 *图标报表action数据源
 * @author Administrator
 * @see/demork/demoRkdAction!load.action*
 */
@Component("tbbbDbAction")
@Scope("prototype")
public class TbbbDbAction extends DynaAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3318618481537466015L;
	public String loadXmInfo(DsInfoVo dsInfoVo){
		List<DynaBean> xmInfos=serviceTemplate.selectList("JE_DEMO_XMINFO"," ORDER BY XMINFO_FZR ASC");
		JSONArray arrays=new JSONArray();
		for(DynaBean xm:xmInfos){
			JSONObject obj=new JSONObject();
			obj.put("项目状态_NAME", xm.get("XMINFO_XMZT_NAME"));
			obj.put("项目状态", xm.get("XMINFO_XMZT_CODE"));
			obj.put("内部_NAME", xm.get("XMINFO_NB_NAME"));
			obj.put("内部", xm.get("XMINFO_NB_CODE"));
			obj.put("项目名称", xm.get("XMINFO_XMMC"));
			obj.put("项目编码", xm.get("XMINFO_XMBM"));
			obj.put("负责人", xm.get("XMINFO_FZR"));
			obj.put("立项人", xm.get("XMINFO_LXR"));
			obj.put("客户名称", xm.get("XMINFO_KHMC"));
			obj.put("费用金额", xm.get("XMINFO_FYJE"));
			obj.put("已用金额", xm.get("XMINFO_YYJE"));
			obj.put("预计收入", xm.get("XMINFO_YJSR"));
			obj.put("项目阶段_NAME", xm.get("XMINFO_XMJD_NAME"));
			obj.put("项目阶段", xm.get("XMINFO_XMJD_CODE"));
			obj.put("项目类型_NAME", xm.get("XMINFO_XMLX_NAME"));
			obj.put("项目类型", xm.get("XMINFO_XMLX_CODE"));
			arrays.add(obj);
		}
		return arrays.toString();

	}
}
