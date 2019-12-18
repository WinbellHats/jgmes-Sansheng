package com.demo.action;

import java.util.List;

import net.sf.json.JSONObject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.je.core.action.DynaAction;
import com.je.core.entity.QueryInfo;
import com.je.core.util.MathExtend;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.DynaBean;
import com.je.dd.vo.DictionaryItemVo;

/**
 * 按钮测试的antion
 * 
 * @author admin
 * @version 2017-05-23 12:31:39
 * @see /demork/demoRkdAction!load.action
 */
@Component("demoRkdAction")
@Scope("prototype")
public class DemoRkdAction extends DynaAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -317517086379795602L;

	public void doUpdate() {
		DynaBean oldBean = serviceTemplate.selectOneByPk("JE_DEMO_RKD",
				dynaBean.getStr("JE_DEMO_RKD_ID"));
		if (!oldBean.getStr("RKD_RKZT_CODE").equals(
				dynaBean.getStr("RKD_RKZT_CODE"))) {
			if ("ZF".equals(dynaBean.getStr("RKD_RKZT_CODE"))) {
				// crkdManager.doRkdZf(dynaBean);
				System.out.println("作废入库单处理...");
			} else if ("YTJ".equals(dynaBean.getStr("RKD_RKZT_CODE"))) {
				// crkdManager.doRkdSubmit(dynaBean);
				System.out.println("提交入库单处理...");
			}
		}
		super.doUpdate();
	}

	public void addRkdMx() {
		String spIds = request.getParameter("spIds");
		DynaBean rkd = serviceTemplate.selectOneByPk("JE_DEMO_RKD", pkValue);
		List<DynaBean> sps = serviceTemplate
				.selectList("ERP_CP_BOM", " AND ERP_CP_BOM_ID in ("
						+ StringUtil.buildArrayToString(spIds.split(",")) + ")");
		for (DynaBean sp : sps) {
			DynaBean rkdMx = new DynaBean("JE_DEMO_RKDMX", true);
			rkdMx.set("RKDMX_MM_MC", sp.getStr("BOM_MM_MC"));
			rkdMx.set("RKDMX_MM_BM", sp.getStr("BOM_MM_BM"));
			rkdMx.set("RKDMX_TYPE_NAME", sp.getStr("SP_TYPE_NAME"));
			rkdMx.set("ERP_RKDMX_CP_ID", sp.getStr("ERP_BOM_CP_ID"));
			rkdMx.set("RKDMX_SL", sp.getStr("BOM_SL"));
			rkdMx.set("RKDMX_MM_ID", sp.getStr("BOM_MM_ID"));
			rkdMx.set("RKDMX_DW_NAME", sp.getStr("BOM_DW_NAME"));
			rkdMx.set("RKDMX_DW_CODE", sp.getStr("BOM_DW_CODE"));
			rkdMx.set("RKDMX_GX_CODE", sp.getStr("BOM_GX_CODE"));
			rkdMx.set("ERP_CP_BOM_ID", sp.getStr("ERP_CP_BOM_ID"));
			rkdMx.set("RKDMX_XH", sp.getStr("BOM_XH"));
			rkdMx.set("ERP_CP_BOM_ID", sp.getStr("ERP_CP_BOM_ID"));
			rkdMx.set("RKDMX_DJ", 0);
			rkdMx.set("RKDMX_JE", 0);
			rkdMx.set("RKDMX_RKSL", 0);
			// 入库方式
			rkdMx.set("JE_DEMO_RKD_ID", rkd.getStr("JE_DEMO_RKD_ID"));
			serviceTemplate.buildModelCreateInfo(rkdMx);
			serviceTemplate.insert(rkdMx);
		}
		toWrite(jsonBuilder.returnSuccessJson("\"添加明细成功\""));
	}

	public void doCount() {
		List<DynaBean> rkdMxs = serviceTemplate.selectList("JE_DEMO_RKDMX",
				" AND JE_DEMO_RKD_ID='" + dynaBean.getStr("JE_DEMO_RKD_ID")
						+ "'");
		double je = 0.0;
		double jsje = 0.0;
		for (DynaBean rkdMx : rkdMxs) {
			double mxJe = rkdMx.getDouble("RKDMX_JE", 0.0);
			// double mxJsJe=rkdMx.getDouble("RKDMX_JSJE",0.0);
			je = MathExtend.add(je, mxJe);
			// jsje=MathExtend.add(jsje, mxJsJe);
		}
		JSONObject returnObj = new JSONObject();
		returnObj.put("RKD_ZJE", je);
		// returnObj.put("RKD_SSJE", jsje);
		toWrite(jsonBuilder.returnSuccessJson(returnObj.toString()));
	}
}
