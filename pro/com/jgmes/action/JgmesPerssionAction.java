package com.jgmes.action;

import com.je.core.action.DynaAction;
import com.je.core.util.JEUUID;
import com.je.core.util.bean.DynaBean;
import com.jgmes.util.JgmesResult;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author fm
 * @version 2019-02-02 11:38:02
 * @see /jgmes/jgmesPerssionAction!load.action
 */
@Component("jgmesPerssionAction")
@Scope("prototype")
public class JgmesPerssionAction extends DynaAction {

	/*
	 * 根据用户编码获取用户ID
	 */
	public JgmesResult<String> getUserIdByUserCode(String userCode) {
		JgmesResult<String> res = new JgmesResult<String>();

		DynaBean dynaBean = serviceTemplate.selectOne("je_core_enduser", " and USERCODE='" + userCode + "'");
		if (dynaBean != null) {
			res.Data = dynaBean.getStr("USERID");
		} else {
			res.setMessage("用户不存在！");
		}

		return res;
	}

	/*
	 * 根据用户ID获取对应的角色列表
	 */
	public JgmesResult<List<DynaBean>> getRoleLByUserId(String userId) {
		JgmesResult<List<DynaBean>> res = new JgmesResult<List<DynaBean>>();

		List<DynaBean> list = serviceTemplate.selectList("JE_CORE_ROLE_USER", " and USERID='" + userId + "'");
		if (list != null && list.size() > 0) {
			res.Data = list;
		} else {
			res.setMessage("角色不存在！");
		}
		return res;
	}

	/*
	 * 根据角色ID获取对应的权限列表
	 */
	public JgmesResult<List<DynaBean>> getPerListByRoleId(String roleId) {
		JgmesResult<List<DynaBean>> res = new JgmesResult<List<DynaBean>>();

		List<DynaBean> list = serviceTemplate.selectList("je_core_role_perm", " and ROLEID='" + roleId + "'");
		if (list != null && list.size() > 0) {
			res.Data = list;
		} else {
			res.setMessage("该角色没有权限！");
		}
		return res;
	}

	/*
	 * 根据权限ID获取对应的功能菜单列表
	 */
	public JgmesResult<List<DynaBean>> getMenuIdListByPerId(String perId) {
		JgmesResult<List<DynaBean>> res = new JgmesResult<List<DynaBean>>();

		List<DynaBean> list = serviceTemplate.selectList("JE_CORE_PERMISSION", " and PERID='" + perId + "'");
		if (list != null && list.size() > 0) {
			res.Data = list;
		} else {
			res.setMessage("该权限没有对应的功能菜单！");
		}
		return res;
	}

	/*
	 * 根据功能菜单ID获取对应的功能菜单
	 */
	public JgmesResult<DynaBean> getMenuListByMenuId(String menuId) {
		JgmesResult<DynaBean> res = new JgmesResult<DynaBean>();

		DynaBean dynaBean = serviceTemplate.selectOne("je_core_menu", " and JE_CORE_MENU_ID='" + menuId + "'");
		if (dynaBean != null) {
			res.Data = dynaBean;
		} else {
			res.setMessage("获取功能菜单出错！");
		}
		return res;
	}

	/*
	 * 根据用户编码获取对应的功能菜单
	 */
	public JgmesResult<List<HashMap>> getMenuListByUserCode(String userCode) {
		JgmesResult<List<HashMap>> res = new JgmesResult<List<HashMap>>();
		StringBuilder sBuilder = new StringBuilder();
		String sql = "";

		sBuilder.append("select t5.* from \n");
		sBuilder.append("je_core_enduser t1 \n");
		sBuilder.append("INNER JOIN JE_CORE_ROLE_USER t2 on t1.USERID=t2.USERID \n");
		sBuilder.append("inner join je_core_role_perm t3 on t2.ROLEID = t3.ROLEID \n");
		sBuilder.append("inner join JE_CORE_PERMISSION t4 on t3.PERID = t4.PERID \n");
		sBuilder.append("inner join je_core_menu t5 on t4.PERMCODE = t5.JE_CORE_MENU_ID \n");
		sBuilder.append("where t1.USERCODE='" + userCode + "' \n");

		sql = sBuilder.toString();
		List<DynaBean> list = serviceTemplate.selectListBySql(sql);
		if (list != null && list.size() > 0) {
			res.Data = res.getValues(list);
		} else {
			res.setMessage("没有对应的功能菜单！");
		}
		return res;
	}

	/*
	 * 根据用户编码获取平板端对应的功能菜单
	 */
	public JgmesResult<List<HashMap>> getPbMenuListByUserCode(String userCode) {
		JgmesResult<List<HashMap>> res = new JgmesResult<List<HashMap>>();
		StringBuilder sBuilder = new StringBuilder();
		String sql = "";

		try {
			sBuilder.append("select DISTINCT t7.* from \n");
			sBuilder.append("je_core_enduser t1 \n");
			sBuilder.append("INNER JOIN JE_CORE_ROLE_USER t2 on t1.USERID=t2.USERID \n");
			sBuilder.append("inner join je_core_role_perm t3 on t2.ROLEID = t3.ROLEID \n");
			sBuilder.append("inner join JE_CORE_PERMISSION t4 on t3.PERID = t4.PERID \n");
			sBuilder.append("inner join je_core_menu t5 on t4.PERMCODE = t5.JE_CORE_MENU_ID \n");
			sBuilder.append("inner join je_core_menu t6 on t5.SY_PARENT = t6.JE_CORE_MENU_ID \n");
			sBuilder.append("inner join JGMES_PB_MENUSET t7 on t5.MENU_HELP = t7.MENUSET_CDBH \n");
			sBuilder.append("where t1.USERCODE='" + userCode + "' and t6.MENU_NODEINFO='jgmes_pb' \n");
			sBuilder.append(" and t7.MENUSET_NO_CODE='1' \n");
			sBuilder.append(" and t3.ENABLED = '1' \n");
			sBuilder.append("order by t7.MENUSET_SXH");

			sql = sBuilder.toString();
			List<DynaBean> list = serviceTemplate.selectListBySql(sql);
			if (list != null && list.size() > 0) {
				res.Data = res.getValues(list);
			} else {
				res.setMessage("没有对应的功能菜单！");
			}
		} catch (Exception e) {
			// TODO: handle exception
			res.setMessage("获取功能菜单出错！" + e.toString());
		}
		return res;
	}
	
	/*
	 * 根据用户编码获取PDA端对应的功能菜单
	 */
	public JgmesResult<List<HashMap>> getPdaMenuListByUserCode(String userCode) {
		JgmesResult<List<HashMap>> res = new JgmesResult<List<HashMap>>();
		StringBuilder sBuilder = new StringBuilder();
		String sql = "";

		try {
			sBuilder.append("select DISTINCT t7.* from \n");
			sBuilder.append("je_core_enduser t1 \n");
			sBuilder.append("INNER JOIN JE_CORE_ROLE_USER t2 on t1.USERID=t2.USERID \n");
			sBuilder.append("inner join je_core_role_perm t3 on t2.ROLEID = t3.ROLEID \n");
			sBuilder.append("inner join JE_CORE_PERMISSION t4 on t3.PERID = t4.PERID \n");
			sBuilder.append("inner join je_core_menu t5 on t4.PERMCODE = t5.JE_CORE_MENU_ID \n");
			sBuilder.append("inner join je_core_menu t6 on t5.SY_PARENT = t6.JE_CORE_MENU_ID \n");
			sBuilder.append("inner join JGMES_PB_MENUSET t7 on t5.MENU_HELP = t7.MENUSET_CDBH \n");
			sBuilder.append("where t1.USERCODE='" + userCode + "' and t6.MENU_NODEINFO='jgmes_pda' \n");
			sBuilder.append(" and t7.MENUSET_NO_CODE='1' \n");
			sBuilder.append(" and t3.ENABLED = '1' \n");
			sBuilder.append("order by t7.MENUSET_SXH");

			sql = sBuilder.toString();
			List<DynaBean> list = serviceTemplate.selectListBySql(sql);
			if (list != null && list.size() > 0) {
				res.Data = res.getValues(list);
			} else {
				res.setMessage("没有对应的功能菜单！");
			}
		} catch (Exception e) {
			// TODO: handle exception
			res.setMessage("获取功能菜单出错！" + e.toString());
		}
		return res;
	}

	public void getPbMenuListByUserCode() {
		String mac = request.getParameter("mac");// MAC地址
		String userCode = request.getParameter("userCode");// 用户编号

		JgmesResult<List<HashMap>> res = getPbMenuListByUserCode(userCode);
		toWrite(jsonBuilder.toJson(res));
	}
	
	public void getPdaMenuListByUserCode() {
		String mac = request.getParameter("mac");// MAC地址
		String userCode = request.getParameter("userCode");// 用户编号

		JgmesResult<List<HashMap>> res = getPdaMenuListByUserCode(userCode);
		toWrite(jsonBuilder.toJson(res));
	}

	/*
	 * 平板端菜端配置自动生成接口
	 * 
	 */
	public void updateOrAddMenu() {
		String menuSetId = request.getParameter("menuSetId");// 菜单配置ID
		String menuNodeInfo = request.getParameter("menuNodeInfo");// 菜单配置ID
		DynaBean cdDynaBean = new DynaBean();
		JgmesResult<Integer> ret = new JgmesResult<Integer>();
		if ((menuSetId != null && !menuSetId.isEmpty())) {
			cdDynaBean = serviceTemplate.selectOne("JGMES_PB_MENUSET", "and JGMES_PB_MENUSET_ID = '" + menuSetId + "'");

			// 获取平板端的JE_CORE_MENU_ID
			DynaBean pbMenu = serviceTemplate.selectOne("je_core_menu", "and MENU_NODEINFO = '"+menuNodeInfo+"'");
			if (cdDynaBean != null && (cdDynaBean.getStr("MENUSET_CDHXID") != null
					&& !cdDynaBean.getStr("MENUSET_CDHXID").isEmpty())) {
				DynaBean jcMenu = serviceTemplate.selectOne("je_core_menu",
						"and JE_CORE_MENU_ID = '" + cdDynaBean.getStr("MENUSET_CDHXID") + "'");
				if (jcMenu != null) {
					jcMenu.set("MENU_MENUNAME", cdDynaBean.get("MENUSET_CDMC"));
					String cdbzlj = cdDynaBean.get("MENUSET_CDBZLJ").toString();
					String p = "\"([^\"]*)\"";
					Pattern pa = Pattern.compile(p);
					Matcher matcher1 = pa.matcher(cdbzlj);
					if (!matcher1.find()) {
						cdbzlj = "\"" + cdbzlj + "\"";
					}
					jcMenu.set("MENU_NODEINFO", cdbzlj);
					jcMenu.set("MENU_HELP", cdDynaBean.get("MENUSET_CDBH"));
					serviceTemplate.update(jcMenu);
					ret.IsSuccess = true;
				} else {
					jcMenu = toJcMenu(cdDynaBean, pbMenu.getStr("JE_CORE_MENU_ID"));
					jcMenu = serviceTemplate.insert(jcMenu);
					cdDynaBean.set("MENUSET_CDHXID", jcMenu.get("JE_CORE_MENU_ID"));
					serviceTemplate.update(cdDynaBean);
					ret.IsSuccess = true;
				}

			} else {
				DynaBean jcMenu = toJcMenu(cdDynaBean, pbMenu.getStr("JE_CORE_MENU_ID"));
				jcMenu = serviceTemplate.insert(jcMenu);
				cdDynaBean.set("MENUSET_CDHXID", jcMenu.get("JE_CORE_MENU_ID"));
				serviceTemplate.update(cdDynaBean);
				ret.IsSuccess = true;
			}
		} else {
			ret.IsSuccess = false;
			ret.setMessage("未接收到菜单配置ID");
		}
		toWrite(jsonBuilder.toJson(ret));

	}

	/*
	 * 
	 * 菜单配置表和je_core_menu对应
	 */
	public DynaBean toJcMenu(DynaBean cdDynaBean, String syParent) {
		DynaBean jcMenu = new DynaBean();
		jcMenu.setStr(beanUtils.KEY_TABLE_CODE, "je_core_menu");
		jcMenu.set("JE_CORE_MENU_ID", JEUUID.uuid());
		jcMenu.set("SY_AUDFLAG", cdDynaBean.get("SY_AUDFLAG"));
		jcMenu.set("SY_CREATEORG", cdDynaBean.get("SY_CREATEORG"));
		jcMenu.set("SY_CREATEORGNAME", cdDynaBean.get("SY_CREATEORGNAME"));
		jcMenu.set("SY_CREATETIME", cdDynaBean.get("SY_CREATETIME"));
		jcMenu.set("SY_CREATEUSER", cdDynaBean.get("SY_CREATEUSER"));
		jcMenu.set("SY_CREATEUSERNAME", cdDynaBean.get("SY_CREATEUSERNAME"));
		jcMenu.set("SY_STATUS", cdDynaBean.get("SY_STATUS"));
		jcMenu.set("SY_ORDERINDEX", cdDynaBean.get("SY_ORDERINDEX"));
		jcMenu.set("SY_PIID", cdDynaBean.get("SY_PIID"));
		jcMenu.set("SY_PDID", cdDynaBean.get("SY_PDID"));
		jcMenu.set("SY_PARENT", syParent);
		jcMenu.set("SY_NODETYPE", cdDynaBean.get("SY_NODETYPE"));
		jcMenu.set("SY_LAYER", cdDynaBean.get("SY_LAYER"));
		jcMenu.set("SY_PATH", cdDynaBean.get("SY_PATH"));
		jcMenu.set("SY_PARENTPATH", cdDynaBean.get("SY_PARENTPATH"));
		jcMenu.set("MENU_MENUNAME", cdDynaBean.get("MENUSET_CDMC")); // 菜单名称对应
		String cdbzlj = cdDynaBean.get("MENUSET_CDBZLJ").toString();
		String p = "\"([^\"]*)\"";
		Pattern pa = Pattern.compile(p);
		Matcher matcher1 = pa.matcher(cdbzlj);
		if (!matcher1.find()) {
			cdbzlj = "\"" + cdbzlj + "\"";
		}
		jcMenu.set("MENU_NODEINFO", cdbzlj); // 菜单连接
		jcMenu.set("MENU_HELP", cdDynaBean.get("MENUSET_CDBH")); // 菜单编码
		// jcMenu.set("MENU_HELP", cdDynaBean.get("MENUSET_CDBZLJ")); //菜单帮助连接
		jcMenu.set("MENU_FONTICONCOLOR", cdDynaBean.get("MENUSET_ZTYS")); // 菜单字体样式
		jcMenu.set("MENU_ICON", cdDynaBean.get("MENUSET_CDTB")); // 菜单图标
		jcMenu.set("MENU_NODEINFOTYPE", "IFRAME"); // 菜单图标 MENU_NODEINFOTYPE
		return jcMenu;

	}

}
