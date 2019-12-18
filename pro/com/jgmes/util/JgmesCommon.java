package com.jgmes.util;

import com.je.core.action.DynaAction;
import com.je.core.service.PCDynaServiceTemplate;
import com.je.core.util.FileOperate;
import com.je.core.util.JEUUID;
import com.je.core.util.SpringContextHolder;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.DynaBean;
import net.sf.json.JSONObject;
import org.apache.struts2.dispatcher.multipart.MultiPartRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JgmesCommon  extends DynaAction {

	public JgmesUser jgmesUser = null;

	public JgmesCommon(HttpServletRequest requestT, PCDynaServiceTemplate serviceTemplateT) {
		request = requestT;
		serviceTemplate = serviceTemplateT;
		jgmesUser = new JgmesUser(requestT, serviceTemplateT);
	}

	public JgmesCommon(HttpServletRequest requestT, PCDynaServiceTemplate serviceTemplateT, String userCode) {
		request = requestT;
		serviceTemplate = serviceTemplateT;
		jgmesUser = new JgmesUser(requestT, serviceTemplateT, userCode);
	}


	/*
	 * 写入系统字段的值，如创建用户、创建时间等
	 */
	public void setDynaBeanInfo(DynaBean dynaBean) {
		DynaBean userDynaBean = jgmesUser.getCurrentUser();
		if(userDynaBean!=null) {
			dynaBean.set("SY_CREATEUSERID", userDynaBean.getStr("USERID"));
			dynaBean.set("SY_CREATEUSER", userDynaBean.getStr("USERCODE"));
			dynaBean.set("SY_CREATEUSERNAME", userDynaBean.getStr("USERNAME"));
			dynaBean.set("SY_CREATETIME", getCurrentTime());
			dynaBean.set("SY_CREATEORGID", userDynaBean.getStr("DEPTID"));
			dynaBean.set("SY_CREATEORG", userDynaBean.getStr("DEPTNAME"));
			dynaBean.set("SY_JTGSMC", userDynaBean.getStr("JTGSMC"));
			dynaBean.set("SY_JTGSID", userDynaBean.getStr("JTGSID"));
		}
//		dynaBean.set("SY_CREATEUSERID", jgmesUser.getCurrentUserID());
//		dynaBean.set("SY_CREATEUSER", jgmesUser.getCurrentUserCode());
//		dynaBean.set("SY_CREATEUSERNAME", jgmesUser.getCurrentUserName());
//		dynaBean.set("SY_CREATETIME", getCurrentTime());
//		dynaBean.set("SY_CREATEORGID", jgmesUser.getCurrentDeptID());
//		dynaBean.set("SY_CREATEORG", jgmesUser.getCurrentDeptName());
//		dynaBean.set("SY_JTGSMC", jgmesUser.getCurrentJTGSMC());
//		dynaBean.set("SY_JTGSID", jgmesUser.getCurrentJTGSID());		
	}

	/*
	 * 通过表中数据字典项的Code的值去设置数据字典项
	 * dicParentCode 数据字典编码
	 * codeStr 要更新表中数据字典的code
	 * idStr 要更新表中数据字典的ID
	 * nameStrStr 要更新表中数据字典的name
	 */
	public void setDynaBeanDicByCode(DynaBean dynaBean, String dicParentCode, String codeStr, String idStr, String nameStrStr) {
		String dicItemValue = "";
		if (dynaBean != null) {
			dicItemValue = dynaBean.getStr(codeStr);
		}

		setDynaBeanDic(dynaBean, dicParentCode, dicItemValue, idStr, codeStr, nameStrStr);
	}

	/*
	 * 设置数据字典项
	 * dicParentCode 数据字典编码
	 * dicItemValue 数据字典项目的值
	 * idStr 要更新表中数据字典的ID
	 * codeStr 要更新表中数据字典的code
	 * nameStrStr 要更新表中数据字典的name
	 */
	public void setDynaBeanDic(DynaBean dynaBean, String dicParentCode, String dicItemValue, String idStr, String codeStr, String nameStrStr) {
		DynaBean dic = getDic(dicParentCode, dicItemValue);//获取数据字典

		if (dic != null && dynaBean != null) {
			if (idStr != null && !idStr.isEmpty()) {
				dynaBean.setStr(idStr, dic.getStr("JE_CORE_DICTIONARYITEM_ID"));
			}
			if (codeStr != null && !codeStr.isEmpty()) {
				dynaBean.setStr(codeStr, dic.getStr("DICTIONARYITEM_ITEMCODE"));
			}
			if (nameStrStr != null && !nameStrStr.isEmpty()) {
				dynaBean.setStr(nameStrStr, dic.getStr("DICTIONARYITEM_ITEMNAME"));
			}
		} else {
			System.out.println("数据字典[" + dicParentCode + "]，值[dicItemValue]未获取到数据字典项！");
		}
	}

	/*
	 * 设置排产状态
	 */
	public void setDynaBeanDicPcZt(DynaBean dynaBean, String dicItemValue, String idStr, String codeStr, String nameStrStr) {
		setDynaBeanDic(dynaBean, "JGMES_DIC_PCZT", dicItemValue, idStr, codeStr, nameStrStr);
	}


	/*
	 * 设置生产任务单状态
	 */
	public void setDynaBeanDicScrwZt(DynaBean dynaBean, String dicItemValue, String idStr, String codeStr, String nameStrStr) {
		setDynaBeanDic(dynaBean, "JGMES_DIC_RWZT", dicItemValue, idStr, codeStr, nameStrStr);
	}

	/*
	 * 获取当前时间
	 */
	public String getCurrentTime() {
		Date date = new Date();//获得系统时间.
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String nowTime = sdf.format(date);
		return nowTime;
	}

	/*
	 * 获取当前日期
	 */
	public String getCurrentDate() {
		Date date = new Date();//获得系统时间.
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String nowTime = sdf.format(date);
		return nowTime;
	}

	/*
	 * 获取当前日期
	 */
	public String getCurrentMonth() {
		Date date = new Date();//获得系统时间.
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
		String nowTime = sdf.format(date);
		return nowTime;
	}

	/*
	 * 根据数据字典的ID或CODE获取数据字典项的集合
	 */
	public List<DynaBean> getDicList(String parentCode, String parentId) {
		List<DynaBean> dicList = new ArrayList<DynaBean>();
		if (parentId != null && parentId.length() > 0) {//有ID时直接根据ID获取列表
			dicList = serviceTemplate.selectList("je_core_dictionaryitem", " AND sy_parent='" + parentId + "'");
		} else {
			DynaBean dicItem = serviceTemplate.selectOne("je_core_dictionary", " AND DICTIONARY_DDCODE='" + parentCode + "'");
			dicList = serviceTemplate.selectList("je_core_dictionaryitem", " AND sy_parent='" + dicItem.getStr("DICTIONARY_ITEMROOT_ID") + "'");
		}
		return dicList;
	}

	/*
	 * 根据数据字典的编码及字典项的值获取字典项，ID：je_core_dictionaryitem_id，Code:dictionaryitem_itemcode,Name=dictionaryitem_itemname
	 */
	public DynaBean getDic(String parentCode, String itemValue) {
		DynaBean dicItem = serviceTemplate.selectOne("je_core_dictionary", " AND DICTIONARY_DDCODE='" + parentCode + "'");
		DynaBean dic = new DynaBean();
		if (dicItem != null) {
			dic = serviceTemplate.selectOne("je_core_dictionaryitem", " AND sy_parent='" + dicItem.getStr("DICTIONARY_ITEMROOT_ID") + "' and dictionaryitem_itemcode='" + itemValue + "'");
		}
		return dic;
	}

	/*
	 * 根据数据字典的编码及字典项的值获取字典项，ID：je_core_dictionaryitem_id，Code:dictionaryitem_itemcode,Name=dictionaryitem_itemname
	 */
	public DynaBean getDicByDicName(String parentCode, String itemName) {
		DynaBean dicItem = serviceTemplate.selectOne("je_core_dictionary", " AND DICTIONARY_DDCODE='" + parentCode + "'");
		DynaBean dic = new DynaBean();
		if (dicItem != null) {
			dic = serviceTemplate.selectOne("je_core_dictionaryitem", " AND sy_parent='" + dicItem.getStr("DICTIONARY_ITEMROOT_ID") + "' and dictionaryitem_itemname='" + itemName + "'");
		}
		return dic;
	}

	/*
	 * 根据数据字典的编码及字典项的名称获取字典项，ID：je_core_dictionaryitem_id，Code:dictionaryitem_itemcode,Name=dictionaryitem_itemname
	 */
	public DynaBean getDicByName(String parentCode, String itemName) {
		DynaBean dicItem = serviceTemplate.selectOne("je_core_dictionary", " AND DICTIONARY_DDCODE='" + parentCode + "'");
		DynaBean dic = new DynaBean();
		if (dicItem != null) {
			dic = serviceTemplate.selectOne("je_core_dictionaryitem", " AND sy_parent='" + dicItem.getStr("DICTIONARY_ITEMROOT_ID") + "' and dictionaryitem_itemname='" + itemName + "'");
		}
		return dic;
	}

	/*
	 * 验证
	 * */
	public boolean doCheck(String mac, String userCode) {
		boolean res = false;
		DynaBean dicItem = getDic("JGMES_YES_NO", "1");
		if (mac != null && !mac.isEmpty() && userCode != null && !userCode.isEmpty()) {
			String sqlWhere = " and DLGL_MACDZ='" + mac + "' and DLGL_NO_ID='" + dicItem.getStr("JE_CORE_DICTIONARYITEM_ID") + "' and DLGL_YHBM='" + userCode + "'";
			List<DynaBean> dlglList = serviceTemplate.selectList("JGMES_PB_DLGL", sqlWhere);
			if (dlglList != null && dlglList.size() == 1) {
				res = true;
			}
		}

		return res;
	}

	/*
	 * 验证登陆是否合法
	 */
	public JgmesResult<String> doCheckRes(String mac, String userCode) {
		JgmesResult<String> jgmesResult = new JgmesResult<String>();

		if (!isCheckMAC()) {//不校验MAC的直接返回
			return jgmesResult;
		}

		if (!doCheck(mac, userCode)) {
			jgmesResult.ErrorCode = JgmesEnumsErr.LoginErr.getKey();
			jgmesResult.setMessage(JgmesEnumsErr.LoginErr.getValue());
		}
		return jgmesResult;
	}

	/*
	 * 从一个DynaBean里面的字段设置到另一个DynaBean里面的字段，前提字段相同（即字段后缀都一样）
	 */
	public void setSameStr(DynaBean toDynaBean, DynaBean fromDynaBean, String fieldSuffix) {
		String toTabCode = toDynaBean.getStr(beanUtils.KEY_TABLE_CODE);
		String fromTabCode = fromDynaBean.getStr(beanUtils.KEY_TABLE_CODE);
		String toPrefix = toTabCode.substring(toTabCode.lastIndexOf("_") + 1);
		String fromPrefix = fromTabCode.substring(fromTabCode.lastIndexOf("_") + 1);

//		System.out.println(fromPrefix+"_"+fieldSuffix);
		Object sValue = fromDynaBean.get(fromPrefix + "_" + fieldSuffix);
		if (sValue != null) {
			toDynaBean.set(toPrefix + "_" + fieldSuffix, sValue);
		}
	}

	/*
	 * 从一个DynaBean里面的字段集合设置到另一个DynaBean里面，前提字段相同（即字段后缀都一样）
	 */
	public void setSameList(DynaBean toDynaBean, DynaBean fromDynaBean, String[] fieldSuffixs) {
		if (fieldSuffixs != null && fieldSuffixs.length > 0) {
			for (int i = 0; i < fieldSuffixs.length; i++) {
				String fieldSuffix = fieldSuffixs[i];
				setSameStr(toDynaBean, fromDynaBean, fieldSuffix);
			}
		}
	}

	public String getSqlWhere(String fieldName, String vStr) {
		String reString = "";
		if (vStr != null && !vStr.isEmpty()) {
			reString = " and " + fieldName + "='" + vStr + "'";
		}
		return reString;
	}

	/*
	 * 根据JSON转成对应的对象
	 */
	public DynaBean getDynaBeanByJsonStr(String tabCode, String jsonStr) throws ParseException {
		String key = "";
		String value = "";
		DynaBean dynaBean = new DynaBean();
		dynaBean.setStr(beanUtils.KEY_TABLE_CODE, tabCode);

		JSONObject jb1 = JSONObject.fromObject(jsonStr);
		if (jb1 != null) {
			Iterator it = jb1.keys();
			List<String> keyListstr = new ArrayList<String>();
			while (it.hasNext()) {
				key = (String) it.next();
				value = jb1.getString(key);
				dynaBean.setStr(key, value);
			}
		}

		setDynaBeanInfo(dynaBean);
		return dynaBean;
	}

	/*
	 * 根据JSON转成对应的对象
	 */
	public DynaBean getDynaBeanByJsonStrNotSys(String tabCode, String jsonStr) throws ParseException {
		String key = "";
		String value = "";
		DynaBean dynaBean = new DynaBean();
		dynaBean.setStr(beanUtils.KEY_TABLE_CODE, tabCode);

		JSONObject jb1 = JSONObject.fromObject(jsonStr);
		if (jb1 != null) {
			Iterator it = jb1.keys();
			List<String> keyListstr = new ArrayList<String>();
			while (it.hasNext()) {
				key = (String) it.next();
				value = jb1.getString(key);
				dynaBean.setStr(key, value);
			}
		}
		return dynaBean;
	}


	/*
	 *  根据JSON转成对应的对象
	 */
	public List<DynaBean> getListDynaBeanByJsonStr(String tabCode, String jsonStr) throws ParseException {
		String key = "";

		List<DynaBean> listDynaBean = new ArrayList<DynaBean>();

		if (jsonStr != null && !jsonStr.isEmpty()) {
			net.sf.json.JSONArray ja = net.sf.json.JSONArray.fromObject(jsonStr);
			if (ja.size() > 0) {
				for (int j = 0; j < ja.size(); j++) {
					DynaBean dynaBean = new DynaBean();
					if (!"".equals(tabCode) && tabCode != null) {
						dynaBean.setStr(beanUtils.KEY_TABLE_CODE, tabCode);
					}

					JSONObject jb1 = JSONObject.fromObject(ja.getJSONObject(j).get("values")); // 遍历 jsonarray 数组，把每一个对象转成 json 对象
					if (jb1 == null || jb1.size() == 0) {
						jb1 = JSONObject.fromObject(ja.getJSONObject(j));
					}
					if (jb1 != null && jb1.size() > 0) {
						Iterator it2 = jb1.keys();
						while (it2.hasNext()) {
							key = (String) it2.next();
							Object value = jb1.get(key);
							dynaBean.set(key, value);
						}
						listDynaBean.add(dynaBean);
					}
				}
			}
		}

		return listDynaBean;
	}

	/*
	 * 根据JSON更新对象
	 */
	public DynaBean updateDynaBeanByJsonStr(DynaBean dynaBean, String jsonStr) throws ParseException {
		String key = "";
		String value = "";

		JSONObject jb1 = JSONObject.fromObject(jsonStr);
		if (jb1 != null) {
			Iterator it = jb1.keys();
			List<String> keyListstr = new ArrayList<String>();
			while (it.hasNext()) {
				key = (String) it.next();
				value = jb1.getString(key);
				if (key != null && !key.isEmpty() && value != null && !value.isEmpty()) {
					dynaBean.setStr(key, value);
				}
			}
		}

		return dynaBean;
	}


	public List<String> getUpdateSqlList(List<DynaBean> dynaBeans) {
		List<String> list = new ArrayList<String>();


		return list;
	}

	/*
	 * 获取更新的SQL语句，注意主键ID必须时表名后面加“_ID”，调试时可以使用
	 */
	public String getUpdateSql(DynaBean dynaBean) {
		String res = "";
		String keyCode = "";
		StringBuilder sb = new StringBuilder();
		String tabCode = dynaBean.getStr(beanUtils.KEY_TABLE_CODE);
//		String	keyId =HibernateConfigurationUtil.getPKColumnName(dynaBean.getClass());
//		String	keyId = tabCode+"_ID";
		String keyId = beanUtils.getPKeyFieldNames(tabCode);

		sb.append("update " + tabCode + " set ");
		if (dynaBean != null) {
			keyCode = dynaBean.getPkValue();
			HashMap<String, Object> hashMap = dynaBean.getValues();
			for (String key : hashMap.keySet()) {
				if (key.equalsIgnoreCase(keyCode)) {
					continue;
				}
				sb.append(key + "='" + hashMap.get(key) + "' ");
				sb.append(",");
			}
			res = sb.substring(0, sb.length() - 1);
			res += (" where " + keyId + "='" + dynaBean.getStr(keyId) + "'");
		}
		System.out.println("更新的语句：" + res);

		return res;
	}


	//获取首道工序
	public String getFirstGxId(String barCode) {
		String reString = "";
		DynaBean cpDynaBean = getCpByBarCode(barCode);
		DynaBean gylxgxDynaBean = new DynaBean();
		if (cpDynaBean != null) {

			List<DynaBean> list = serviceTemplate.selectList("JGMES_GYGL_GYLXGX", " and GYLX_ID='" + cpDynaBean.getStr("PRODUCTDATA_CPGYLXID") + "' order by SY_ORDERINDEX asc", 0, 1);
			if (list != null && list.size() == 1) {
				gylxgxDynaBean = list.get(0);
				if (gylxgxDynaBean != null) {
					reString = gylxgxDynaBean.getStr("GYLXGX_ID");//.getPkValue();
				}
			}
//				gylxgxDynaBean = serviceTemplate.selectOne("JGMES_GYGL_GYLXGX", " and GYLX_ID='"+cpDynaBean.getStr("PRODUCTDATA_CPGYLXID")+"' and SY_ORDERINDEX=1");
		}
		return reString;
	}

	//获取末道工序
	public String getLastGxId(String barCode) {
		String reString = "";
		DynaBean cpDynaBean = getCpByBarCode(barCode);
		List<DynaBean> list = new ArrayList<DynaBean>();
		DynaBean gylxgxDynaBean = new DynaBean();
		if (cpDynaBean != null) {
			list = serviceTemplate.selectList("JGMES_GYGL_GYLXGX", " and GYLX_ID='" + cpDynaBean.getStr("PRODUCTDATA_CPGYLXID") + "' order by SY_ORDERINDEX desc", 0, 1);
			if (list != null && list.size() == 1) {
				gylxgxDynaBean = list.get(0);
			}
			if (gylxgxDynaBean != null) {
				reString = gylxgxDynaBean.getStr("GYLXGX_ID");//.getPkValue();
			}
		}
		return reString;
	}

	//获取工艺路线下道工序
	public String getNextGxId(String barCode, String currentGxId) {
		String reString = "";
		DynaBean cpDynaBean = getCpByBarCode(barCode);
		DynaBean gylxgxDynaBean = new DynaBean();
		List<DynaBean> list = new ArrayList<DynaBean>();
		StringBuilder sql = new StringBuilder();
		if (cpDynaBean != null) {
			sql.append("select t1.* from JGMES_GYGL_GYLXGX t1");
			sql.append(" inner join JGMES_GYGL_GYLXGX t2 on t1.GYLX_ID=t2.GYLX_ID and t1.SY_ORDERINDEX=t2.SY_ORDERINDEX+1");
			sql.append(" where t1.GYLX_ID='" + cpDynaBean.getStr("PRODUCTDATA_CPGYLXID") + "'");
			sql.append(" and  t2.GYLXGX_ID='" + currentGxId + "'");
			list = serviceTemplate.selectListBySql(sql.toString());
			if (list != null && list.size() > 0) {
				gylxgxDynaBean = list.get(0);
				if (gylxgxDynaBean != null) {
					reString = gylxgxDynaBean.getStr("GYLXGX_ID");//gylxgxDynaBean.getPkValue();
				}
			}
		}
		return reString;
	}


	/*
	 * 是否为首道工序
	 */
	public boolean IsFirstGx(String barCode, String gxId, String gxCode) {
		boolean b = false;

		if (gxId == null || gxId.isEmpty()) {
			if (gxCode != null && !gxCode.isEmpty()) {
				DynaBean gxDynaBean = serviceTemplate.selectOne("JGMES_GYGL_GYLXGX", " and GYLXGX_GXNUM='" + gxCode + "'");
				if (gxDynaBean != null) {
					gxId = gxDynaBean.getStr("GYLXGX_ID");
				}
			}
		}
		if (gxId != null && !gxId.isEmpty()) {
			b = gxId.equalsIgnoreCase(getFirstGxId(barCode));
		}

		return b;
	}

	/*
	 * 是否为末道工序
	 */
	public boolean IsLastGx(String barCode, String gxId, String gxCode) {
		boolean b = false;

		if (gxId == null || gxId.isEmpty()) {
			if (gxCode != null && !gxCode.isEmpty()) {
				DynaBean gxDynaBean = serviceTemplate.selectOne("JGMES_GYGL_GYLXGX", " and GYLXGX_GXNUM='" + gxCode + "'");
				if (gxDynaBean != null) {
					gxId = gxDynaBean.getStr("GYLXGX_ID");
				}
			}
		}
		if (gxId != null && !gxId.isEmpty()) {
			b = gxId.equalsIgnoreCase(getLastGxId(barCode));
		}

		return b;
	}

	/*
	 * 根据条码获取产品编号－－根据报工获取
	 */
	public String getCpCodeByBarCodeOfBgsj(String barCode) {
		String reString = "";

		try {
			DynaBean bgsjDynaBean = new DynaBean();
			bgsjDynaBean.set(beanUtils.KEY_TABLE_CODE, "JGMES_PB_BGSJ");
			bgsjDynaBean = serviceTemplate.selectOne("JGMES_PB_BGSJ", " and BGSJ_TMH='" + barCode + "' and BGSJ_GXSXH=1 and BGSJ_STATUS_CODE != '2'");
			if (bgsjDynaBean != null) {
				reString = bgsjDynaBean.getStr("BGSJ_CPBH");
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("根据条码获取产品失败！");
		}

		return reString;
	}

	/*
	 * 根据条码获取任务编号－－根据报工获取
	 */
	public String getScrwIdByBarCodeOfBgsj(String barCode) {
		String reString = "";

		try {
			DynaBean bgsjDynaBean = new DynaBean();
			bgsjDynaBean.set(beanUtils.KEY_TABLE_CODE, "JGMES_PB_BGSJ");
			if (barCode != null && !barCode.isEmpty()) {
				bgsjDynaBean = serviceTemplate.selectOne("JGMES_PB_BGSJ", " and BGSJ_TMH='" + barCode + "' and BGSJ_GXSXH=1 and BGSJ_STATUS_CODE != '2'");
			}
			if (bgsjDynaBean != null) {
				reString = bgsjDynaBean.getStr("BGSJ_SCRWID");
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("根据条码获取生产任务失败！");
		}

		return reString;
	}

	/*
	 * 根据条码获取产品编号
	 */
	public String getCpCodeByBarCode(String barCode) {
		DynaBean cptmDynaBean = serviceTemplate.selectOne("JGMES_BASE_GDCPTM", " and GDCPTM_TMH='" + barCode + "'");
		String cpCode = "";
		if (cptmDynaBean != null) {
			cpCode = cptmDynaBean.getStr("GDCPTM_CPBH");
		} else {
			System.out.println("该条码没有对应的产品！");
		}

		return cpCode;
	}


	/*
	 * 根据条码获取产品
	 */
	public DynaBean getCpByBarCode(String barCode) {
		DynaBean cpDynaBean = new DynaBean();
		String cpCode = getCpCodeByBarCode(barCode);
		cpDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " and PRODUCTDATA_BH='" + cpCode + "'");

		return cpDynaBean;
	}

	/*
	 * 校检数据合理性，先直接从报工数据表中读取数据检测
	 */
	public JgmesResult<String> 	doCheckSj(String barCode, String inGxId) {
		DynaBean bgsj = new DynaBean();
		if (serviceTemplate == null) {
			serviceTemplate = SpringContextHolder.getBean("PCDynaServiceTemplate");
		}
		List<DynaBean> tmList = serviceTemplate.selectList("JGMES_PB_BGSJ", " and BGSJ_TMH='" + barCode + "' and BGSJ_STATUS_CODE != '2' order by BGSJ_GZSJ desc", 0, 1);
		int sjlx = 0;
		String pdjg = "";
		String wxjg = "";
		String currentGxId = "";//报工数据表中最后一道工序
		String nextGxId = "";//报工数据表中最后一道工序的下道工序
		int currentGxIndex = 0;
		int inGxIndex = 0;
		int nextGxIndex = 0;//报工数据表中最后一道工序的下道工序在工艺路线中的位置
		JgmesEnumsErr jgmesEnumsErr = null;
		JgmesResult<String> result = new JgmesResult<String>();
		JgmesResult<JgmesBarCodeBase> tmLxByBarCode = getTmLxByBarCode(barCode, "", "");
		logger.error("123:" + (isCheckGxIndex() || JgmesEnumsDic.TMWX.getKey().equals(tmLxByBarCode.Data.TmLx.getKey())));
		//不校验工序防呆的直接返回
		if (!isCheckGxIndex() || JgmesEnumsDic.TMWX.getKey().equals(tmLxByBarCode.Data.TmLx.getKey())) {
			return result;
		}

		if (inGxId == null || inGxId.isEmpty()) {
			inGxId = "";
		}
		if (tmList != null && !tmList.isEmpty()) {
			bgsj = tmList.get(0);//获取已报工的最新一条记录
			sjlx = bgsj.getInt("BGSJ_SJLX");
			pdjg = bgsj.getStr("BGSJ_PDJG_CODE");
			wxjg = bgsj.getStr("BGSJ_WXJG_CODE");
			currentGxId = bgsj.getStr("BGSJ_GXID");
			logger.error("数据：" + bgsj + "," + sjlx + "," + pdjg + "," + wxjg + "," + currentGxId);
			DynaBean gylxgx = serviceTemplate.selectOneByPk("JGMES_GYGL_GYLXGX", inGxId);
			Boolean ok = false;
			if (gylxgx != null) {
				Integer ngCount = gylxgx.getInt("GYLXGX_NGCS");
				Integer againScanningNot = gylxgx.getInt("GYLXGX_SFCFSM_CODE");
				String okSql = " SELECT * FROM JGMES_PB_BGSJ WHERE 1=1  and BGSJ_TMH = '" + barCode + "' and BGSJ_GXID = '" + inGxId + "' and BGSJ_STATUS_CODE != '2' order by BGSJ_GZSJ desc limit " + ngCount;
				List<DynaBean> okBean = serviceTemplate.selectListBySql(okSql);
				Integer ngNum = 0;
				if (againScanningNot == 1) {
					for (DynaBean bean : okBean) {
						if (bean.getStr("BGSJ_PDJG_CODE").equals("PDJG02"))
							ngNum++;
					}
				}
				if (ngNum != ngCount && againScanningNot == 1) {
					ok = true;
				}
			}


			if (sjlx == 0) {//工位数据
				if (pdjg.equalsIgnoreCase(JgmesEnumsDic.PdJgPass.getKey()) || pdjg.equalsIgnoreCase(JgmesEnumsDic.PdJgTeDCai.getKey()) || ok) {
					//判断currentGxId这个是否是最后一道工序，如果为最后一道工序，则提示重复+该条码已经全部完成。
					if (IsLastGx(barCode, currentGxId, null)) {

						//获取是否可以重复扫描
						int GYLXGX_SFCFSM_CODE = 0;
						if (StringUtil.isNotEmpty(inGxId)) {
							DynaBean gxid = serviceTemplate.selectOneByPk("JGMES_GYGL_GYLXGX", inGxId);
							if (gxid != null) {
								GYLXGX_SFCFSM_CODE = gxid.getInt("GYLXGX_SFCFSM_CODE");
							}

						}
						logger.error("下道工序号：" + nextGxIndex);
						logger.error("当前工序号：" + inGxIndex);
						if (GYLXGX_SFCFSM_CODE == 0) {//不重复扫描
							result.setMessage(JgmesEnumsErr.CheckErrRepeat.getValue());
							result.ErrorCode = JgmesEnumsErr.CheckErrRepeat.getKey();
							return result;
						} else {
							return result;
						}
					} else {
						//判定合格或特采，下道 工序为工艺路线下道 工序
						nextGxId = getNextGxId(barCode, currentGxId);
						logger.error("nextGxId:" + nextGxId);
					}
				} else {
					//下道工序为维修站，维修站工序ID为空
					nextGxId = "";//currentGxId;//

					if (!inGxId.equals("")) {
						if (inGxId.equals(currentGxId)) {
							jgmesEnumsErr = JgmesEnumsErr.CheckErrRepeatInWxz;
						} else {
							currentGxIndex = getIndexOfGylx(currentGxId);
							inGxIndex = getIndexOfGylx(inGxId);
							if (inGxIndex > currentGxIndex) {
								jgmesEnumsErr = JgmesEnumsErr.CheckErrRepeatInOther;
//								jgmesEnumsErr = JgmesEnumsErr.CheckErrBefor;
							} else {
								jgmesEnumsErr = JgmesEnumsErr.CheckErrAfter;
							}
						}
					}

				}
			} else if (sjlx == 1) {//维修工站数据
				if (wxjg.equalsIgnoreCase(JgmesEnumsDic.WxjgFinish.getKey()) || wxjg.equalsIgnoreCase(JgmesEnumsDic.WxjgTeCai.getKey())) {
					nextGxId = bgsj.getStr("BGSJ_FHGXID");
				} else {
					nextGxId = "";//维修报废的即没有下工序
					jgmesEnumsErr = JgmesEnumsErr.CheckErrUseless;
				}
			}
		} else {//未找到数据即应为首道工序
			nextGxId = getFirstGxId(barCode);
		}
		logger.error("nextGxId:" + nextGxId + ",inGxId:" + inGxId);
		if (nextGxId == null || !inGxId.equalsIgnoreCase(nextGxId)) {//不匹配
			inGxIndex = getIndexOfGylx(inGxId);
			nextGxIndex = getIndexOfGylx(nextGxId);
			logger.error("inGxIndex:" + inGxIndex + ",nextGxIndex:" + nextGxIndex);
			String gxName = getGxNameByGygxId(nextGxId);
			//获取是否可以重复扫描
			int GYLXGX_SFCFSM_CODE = 0;
			if (StringUtil.isNotEmpty(inGxId)) {
				DynaBean gxid = serviceTemplate.selectOneByPk("JGMES_GYGL_GYLXGX", inGxId);
				if (gxid != null) {
					GYLXGX_SFCFSM_CODE = gxid.getInt("GYLXGX_SFCFSM_CODE");
				}

			}

			//获取工序类型
			String gxLx = getGxLxByGygxId(nextGxId);

			if (nextGxId.equals("") && jgmesEnumsErr != null) {//报废或进入维修站的
				result.setMessage(jgmesEnumsErr.getValue());
				result.ErrorCode = jgmesEnumsErr.getKey();
			} else if (nextGxIndex == inGxIndex + 1) {//重复扫描
				if (GYLXGX_SFCFSM_CODE == 0) {//不重复扫描
					System.out.println("下道工序号：" + nextGxIndex);
					System.out.println("当前工序号：" + inGxIndex);
					result.setMessage(JgmesEnumsErr.CheckErrRepeat.getValue());
					result.ErrorCode = JgmesEnumsErr.CheckErrRepeat.getKey();
				} else {
					return result;
				}
			} else if (nextGxIndex > inGxIndex) {
				result.setMessage(JgmesEnumsErr.CheckErrAfter.getValue() + "已做到第[" + nextGxIndex + "]工序[" + gxName + "]");
				result.ErrorCode = JgmesEnumsErr.CheckErrAfter.getKey();
			} else {
				result.setMessage(JgmesEnumsErr.CheckErrBefor.getValue() + "第[" + nextGxIndex + "]工序[" + gxName + "]未扫描");
				result.ErrorCode = JgmesEnumsErr.CheckErrBefor.getKey();
			}
		} else {//匹配时校验上工序是否为NG，如果为NG则提示上工序未完成
			System.out.println(bgsj);
			String passNot = bgsj.getStr("BGSJ_PDJG_CODE");
			if (StringUtil.isNotEmpty(passNot) && StringUtil.isNotEmpty(inGxId)) {
				if (passNot.equals("PDJG02")) {
					result.setMessage(JgmesEnumsErr.CheckErrNoFrontCode.getValue());
					result.ErrorCode = JgmesEnumsErr.CheckErrNoFrontCode.getKey();
				}
			}
		}
		return result;
	}

	/*
	 * 根据工艺路线工序ID获取本工序在工艺路线中顺序
	 */
	public int getIndexOfGylx(String gylxgxID) {
		int res = 0;
		DynaBean dynaBean = serviceTemplate.selectOne("JGMES_GYGL_GYLXGX", " and GYLXGX_ID='" + gylxgxID + "'");
		if (dynaBean != null) {
			res = dynaBean.getInt("SY_ORDERINDEX");
		}
		return res;
	}

	/*
	 * 根据工艺路线工序ID获取本工序的名称
	 */
	public String getGxNameByGygxId(String gygxId) {
		String res = "";
		DynaBean dynaBean = serviceTemplate.selectOne("JGMES_GYGL_GYLXGX", " and GYLXGX_ID='" + gygxId + "'");
		if (dynaBean != null) {
			res = dynaBean.getStr("GYLXGX_GXNAME");
		}
		return res;
	}

	/*
	 * 根据工艺路线工序ID获取本工序的名称
	 */
	public String getGxLxByGygxId(String gygxId) {
		String res = "";
		DynaBean dynaBean = serviceTemplate.selectOne("JGMES_GYGL_GYLXGX", " and GYLXGX_ID='" + gygxId + "'");
		if (dynaBean != null) {
			DynaBean gxDynaBean = serviceTemplate.selectOne("JGMES_GYGL_GXGL", " and GXGL_GXNUM = '" + dynaBean.getStr("GYLXGX_GXNUM") + "'");
			if (gxDynaBean != null) {
				res = gxDynaBean.getStr("GXGL_GXLX_NAME");
			}

		}
		return res;
	}

	/*
	 * 获取上传文件的相对路径，系统存储的是文件名+相对路径的形式，如：BarFalls.png*\/JE/data/upload/201901/DFbsIaOEzBnYpnB8fcg.png
	 */
	public String getPath(String filePath) {
		String reString = filePath;

		if (filePath.indexOf("*") != -1) {
			reString = reString.substring(reString.indexOf("*") + 1);
		}

		return reString;
	}

	/*
	 * 校验物料，如果是唯一码则做唯一性检验
	 */
	public JgmesResult<String> doCheckWlm(String wlBarCode, String wlCode) {
		JgmesResult<String> res = new JgmesResult<String>();
		String tmxz = "";//条码性质

		if (wlCode != null && !wlCode.isEmpty()) {
			DynaBean dynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " and PRODUCTDATA_BH='" + wlCode + "'");
			if (dynaBean != null) {
				tmxz = dynaBean.getStr("PRODUCTDATA_TMXZ_CODE");
				if (tmxz != null && tmxz.equalsIgnoreCase(JgmesEnumsDic.TMXZWYM.getKey())) {//唯一码
					res = doCheckWlmUniq(wlBarCode);
				}
			}
		}

		return res;
	}

	/*
	 * 检验物料唯一性
	 */
	public JgmesResult<String> doCheckWlmUniq(String wlBarCode) {
		JgmesResult<String> res = new JgmesResult<String>();

		List<DynaBean> list = serviceTemplate.selectList("JGMES_PB_BGSJZB", " and BGSJZB_TMH='" + wlBarCode + "'");
		if (list != null && list.size() > 0) {
			res.Data = "";
			res.setMessage(JgmesEnumsErr.CheckErrWlmExist.getValue());
			res.ErrorCode = JgmesEnumsErr.CheckErrWlmExist.getKey();
		}

		return res;
	}

	/*
	 * 报工保存前子表数据物料校验
	 */
	public JgmesResult<String> doCheckBgWl(String jsonArrStr) {
		JgmesResult<String> res = new JgmesResult<String>();

		String key = "";
		String value = "";

		String tm = "";
		String wlCode = "";

		if (jsonArrStr != null && !jsonArrStr.isEmpty()) {
			net.sf.json.JSONArray ja1 = net.sf.json.JSONArray.fromObject(jsonArrStr);
			if (ja1.size() > 0) {
				List<String> tmList = new ArrayList<>();
				for (int i = 0; i < ja1.size(); i++) {
					DynaBean bgsjzb = new DynaBean();
					bgsjzb.setStr(beanUtils.KEY_TABLE_CODE, "JGMES_PB_BGSJZB");

					tm = "";
					wlCode = "";
					JSONObject jb1 = ja1.getJSONObject(i); // 遍历 jsonarray 数组，把每一个对象转成 json 对象
					Iterator it1 = jb1.keys();
					while (it1.hasNext()) {
						key = (String) it1.next();
						if (key.equalsIgnoreCase("BGSJZB_TMH")) {
							tm = jb1.getString(key);
						}
						if (key.equalsIgnoreCase("BGSJZB_WLBH")) {
							wlCode = jb1.getString(key);
						}
					}
					if (tm != null && !tm.isEmpty()) {
						res = doCheckWlm(tm, wlCode);
					}
					if (wlCode != null && !wlCode.isEmpty()) {
						DynaBean dynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " and PRODUCTDATA_BH='" + wlCode + "'");
						if (dynaBean != null) {
							String tmxz = dynaBean.getStr("PRODUCTDATA_TMXZ_CODE");
							if (tmxz != null && tmxz.equalsIgnoreCase(JgmesEnumsDic.TMXZWYM.getKey())) {//唯一码
								if (tmList.contains(tm)) {
									res.setMessage(JgmesEnumsErr.CheckErrWlmExist.getValue());
									res.ErrorCode = JgmesEnumsErr.CheckErrWlmExist.getKey();
								} else {
									tmList.add(tm);
								}
							}
						}
					}
					if (!res.IsSuccess) {
						break;
					}
				}
			}
		}
		return res;
	}

	/*
	 * 是否检验MAC地址
	 * */
	public boolean isCheckMAC() {
		DynaBean selectOne = serviceTemplate.selectOne("JGMES_XTGL_XTCS", " AND XTCS_CSBH='XTCS0006' ");
		if (selectOne.get("XTCS_CSZ").equals("1")) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * 是否进行工序防呆校验
	 * */
	public boolean isCheckGxIndex() {
		DynaBean selectOne = serviceTemplate.selectOne("JGMES_XTGL_XTCS", " AND XTCS_CSBH='XTCS0007' ");
		if (selectOne.get("XTCS_CSZ").equals("1")) {
			return true;
		} else {
			return false;
		}
	}


	/*
	 * 根据条码来识别条码的类型、编号、名称
	 */
	public JgmesResult<JgmesBarCodeBase> getTmLxByBarCode(String barCode, String cpCode, String gygxId) {
		JgmesResult<JgmesBarCodeBase> res = new JgmesResult<JgmesBarCodeBase>();
		JgmesBarCodeBase jgmesBarCodeBase = new JgmesBarCodeBase();
		String tmlx = "";

		res.setErrorDic(JgmesEnumsErr.CheckErrNoThisCode);//未知号码
		//res.IsSuccess = false;

		//1、先从产品条码表里检索，如有相关记录即能确定是什么条码
		if (!res.IsSuccess) {
			DynaBean tmScDynaBean = serviceTemplate.selectOne("JGMES_BASE_GDCPTM", " and GDCPTM_TMH='" + barCode + "'");
			if (tmScDynaBean != null) {
				jgmesBarCodeBase.BarCode = barCode;
				jgmesBarCodeBase.IsMaterail = false;
				jgmesBarCodeBase.ObjCode = tmScDynaBean.getStr("GDCPTM_CPBH");
				jgmesBarCodeBase.ObjName = tmScDynaBean.getStr("GDCPTM_NAME");

				tmlx = tmScDynaBean.getStr("GDCPTM_TMLX_CODE");
				jgmesBarCodeBase.TmLx = getJgmesEnumsDic(tmlx);
				jgmesBarCodeBase.TmXz = getJgmesEnumsDic(tmScDynaBean.getStr("GDCPTM_TMXZ_CODE"));
				jgmesBarCodeBase.tmScDynaBean = tmScDynaBean.getValues();
				if (jgmesBarCodeBase.TmLx != null) {
					//外箱码：TMLX04,栈板码：TMLX05，货柜码：TMLX09
					List<DynaBean> ttyygzDynaBeanList = serviceTemplate.selectList("JGMES_BASE_CPTMYYGG",
							" and CPTMYYGG_CPBH = '" + tmScDynaBean.getStr("GDCPTM_CPBH") + "' and CPTMYYGG_TMLX_CODE in ('TMLX04','TMLX05','TMLX09') and CPTMYYGG_STATUS_CODE = '1'");
					if (ttyygzDynaBeanList != null && ttyygzDynaBeanList.size() > 0) {
						jgmesBarCodeBase.tmyygzDynaBean = res.getValues(ttyygzDynaBeanList);
					}
				}

				res.Data = jgmesBarCodeBase;
				res.IsSuccess = true;
			}
		}
		//2、从物料条码表里检索,如有相关记录即能确定是什么条码
		if (!res.IsSuccess) {
			DynaBean wltmScDynaBean = serviceTemplate.selectOne("JGMES_BASE_WLTM", " and WLTM_TMH='" + barCode + "'");
			if (wltmScDynaBean != null) {
				jgmesBarCodeBase.BarCode = barCode;
				jgmesBarCodeBase.IsMaterail = true;
				jgmesBarCodeBase.ObjCode = wltmScDynaBean.getStr("WLTM_WLBM");
				jgmesBarCodeBase.ObjName = wltmScDynaBean.getStr("WLTM_WLMC");
				jgmesBarCodeBase.TmXz = getJgmesEnumsDic(wltmScDynaBean.getStr("WLTM_TMXZ_CODE"));
				res.Data = jgmesBarCodeBase;
				res.IsSuccess = true;
			}
		}
		//3、从物料条码应用规则里根据正则进行验证，先
		if (!res.IsSuccess && cpCode != null && !cpCode.isEmpty()) {
			DynaBean cpDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " and PRODUCTDATA_BH='" + cpCode + "'");
			//获取工艺路线ID
			String gylxId = (String) cpDynaBean.get("PRODUCTDATA_CPGYLXID");
			//如果存在工艺路线ID，按工序关键物料查询
			if (gygxId != null && !gygxId.isEmpty()) {
				//根据工艺路线ID及工艺工序ID获取关联的关键物料信息
				String tmYzGz = "";
				DynaBean selectOne = serviceTemplate.selectOne("JGMES_GYGL_GYLXGX", " AND gylx_id='" + gylxId + "'  and GYLXGX_ID='" + gygxId + "'");
				List<DynaBean> selectListBySql =
						serviceTemplate.selectListBySql("select J.GXWL_WLBH,J.GXWL_WLMC,J1.PRODUCTDATA_TMYZGZ,J1.PRODUCTDATA_TMXZ_CODE  " +
								"from JGMES_GXGL_GXWL  J " +
								"LEFT JOIN JGMES_BASE_PRODUCTDATA J1 on j.GXWL_WLBH=J1.PRODUCTDATA_BH " +
								"where J.GYLXGX_ID='" + selectOne.get("GYLXGX_ID") + "'");
				//根据关键物料里面的正则来验证
				if (selectListBySql != null) {
					for (int i = 0; i < selectListBySql.size(); i++) {
						String regex = (String) selectListBySql.get(i).get("PRODUCTDATA_TMYZGZ");
						if (regex != null) {
							if (match(regex, barCode)) {
								jgmesBarCodeBase.BarCode = barCode;
								jgmesBarCodeBase.IsMaterail = true;
								jgmesBarCodeBase.ObjCode = (String) selectListBySql.get(i).get("GXWL_WLBH");
								jgmesBarCodeBase.ObjName = (String) selectListBySql.get(i).get("GXWL_WLMC");
								jgmesBarCodeBase.TmXz = getJgmesEnumsDic((String) selectListBySql.get(i).get("PRODUCTDATA_TMXZ_CODE"));
								res.Data = jgmesBarCodeBase;
								res.IsSuccess = true;
							}
						}
					}
				}

			}
			if (!res.IsSuccess) {
				List<DynaBean> selectList = serviceTemplate.selectList("JGMES_BASE_PRODUCTDATA", "  and PRODUCTDATA_WLTYPE_CODE='WL'  ");
				for (int i = 0; i < selectList.size(); i++) {
					String regex = (String) selectList.get(i).get("PRODUCTDATA_TMYZGZ");
					if (regex != null) {
						if (match(regex, barCode)) {
							jgmesBarCodeBase.BarCode = barCode;
							jgmesBarCodeBase.IsMaterail = true;
							jgmesBarCodeBase.ObjCode = (String) selectList.get(i).get("GXWL_WLBH");
							jgmesBarCodeBase.ObjName = (String) selectList.get(i).get("GXWL_WLMC");
							jgmesBarCodeBase.TmXz = getJgmesEnumsDic((String) selectList.get(i).get("PRODUCTDATA_TMXZ_CODE"));
							res.Data = jgmesBarCodeBase;
							res.IsSuccess = true;
						}
					}
				}
			}

		}

		//4、从产品条码应用规则里根据正则进行验证（一般用不上）
		if (!res.IsSuccess && cpCode != null && !cpCode.isEmpty()) {
			DynaBean cpDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " and PRODUCTDATA_BH='" + cpCode + "'");
			if (gygxId != null && !gygxId.isEmpty()) {
				String tmYzGz = "";
				if (cpDynaBean != null) {
					tmYzGz = cpDynaBean.getStr("PRODUCTDATA_TMYZGZ");
					//根据正则验证是否为产品条码

					//如果不是取产品的应用规里的条码验证规则一一验证
					//??
				}
			}
		}
//			if(res.Data==null){
//				res.setErrorDic(JgmesEnumsErr.CheckErrNoThisCode);//未知号码
//			}
		return res;
	}


	public JgmesEnumsDic getJgmesEnumsDic(String key) {
		JgmesEnumsDic jgRes = null;
		if (key != null && !key.isEmpty()) {
			for (JgmesEnumsDic jgmesEnumsDic : JgmesEnumsDic.values()) {
				if (key.equalsIgnoreCase(jgmesEnumsDic.getKey())) {
					jgRes = jgmesEnumsDic;
					break;
				}
			}
		}
		return jgRes;
	}


	/*
	 *  正则验证方法
	 *  lxc 2019-02-08
	 *  param1: 正则表达式   param2:值
	 */
	public boolean match(String regex, String str) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		return matcher.matches();
	}

	/*
	 *
	 */

	/*
	 * 获取当前产线   liuchao
	 */
	public DynaBean getCurrentCX(String userCode) {
		DynaBean cxDynaBean = new DynaBean();
		DynaBean gw = getCurrentGW(userCode);
		String cxId = "";
		if (gw != null) {
			cxDynaBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ", " and JGMES_BASE_CXSJ_ID='" + gw.getStr("JGMES_BASE_CXSJ_ID") + "'");
		} else {
			System.out.println("获取工位失败！");
		}
		return cxDynaBean;
	}


	/*
	 * 获取当前工位
	 */
	public DynaBean getCurrentGW(String userCode) {
		DynaBean gw = new DynaBean();
		if (userCode == null || userCode.isEmpty()) {
			userCode = jgmesUser.getCurrentUserCode();
		}
		String gwid = "";
		DynaBean dicNo = getDic("JGMES_YES_NO", "1");
		DynaBean dlgl = serviceTemplate.selectOne("JGMES_PB_DLGL", " and DLGL_YHBM='" + userCode + "' and DLGL_NO_ID='" + dicNo.getPkValue() + "'");
		if (dlgl != null) {
			gwid = dlgl.getStr("DLGL_GWID");
			if (gwid != null && !gwid.isEmpty()) {
				gw = serviceTemplate.selectOne("JGMES_BASE_GW", " and JGMES_BASE_GW_ID='" + dlgl.getStr("DLGL_GWID") + "'");
			} else {
				gw = serviceTemplate.selectOne("JGMES_BASE_GW", " and GW_GWBH='" + dlgl.getStr("DLGL_GWBH") + "'");
			}
		} else {
			System.out.println("[" + userCode + "]对应的登陆管理未找到！");
		}

		return gw;
	}


	/*
	 * 获取当前产品－
	 */
	public DynaBean getScrwByCxCp(String userCode) {
		DynaBean scrwDynaBean = new DynaBean();
		DynaBean cxDynaBean = new DynaBean();
		if (userCode == null || userCode.isEmpty()) {
			userCode = jgmesUser.getCurrentUserCode();
		}
		cxDynaBean = getCurrentCX(userCode);

		if (cxDynaBean != null) {
			scrwDynaBean = serviceTemplate.selectOneByPk("JGMES_PLAN_SCRW", cxDynaBean.getStr("JGMES_PLAN_SCRW_ID"));
			if (scrwDynaBean != null) {
				String cpCode = scrwDynaBean.getStr("SCRW_CPBH");
				String khcph = "";
				if (cpCode != null && !cpCode.isEmpty()) {
					DynaBean cpDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " and PRODUCTDATA_BH='" + cpCode + "'");
					khcph = cpDynaBean.getStr("PRODUCTDATA_KHCPH");
					if (khcph != null && !khcph.isEmpty()) {
						scrwDynaBean.set("SCRW_KHCPM", khcph);
					}
				}
			}
		}

		return scrwDynaBean;
	}


	/*
	 * 获取当前工序，说明GXGL_GYLX_GXID字段为工艺路线工序ID，后台专用；GXGL_ID这个是本身的工序ID，其它地方不要使用
	 */
	public DynaBean getCurrentGX(String cpCode, String userCode) {
		DynaBean gx = new DynaBean();
		DynaBean gw = getCurrentGW(userCode);
		String gxid = "";
		if (gw != null) {
			//根据工位信息、产品信息获取工序信息
			DynaBean cPGWGX = serviceTemplate.selectOne("JGMES_BASE_CPGWGX", " and CPGWGX_CPBH='" + cpCode + "' and JGMES_BASE_GW_ID='" + gw.getStr("JGMES_BASE_GW_ID") + "'");
			if (cPGWGX != null) {
				gxid = cPGWGX.getStr("CPGWGX_GXID");
				System.out.println("当前工序：" + gxid);
				if (gxid != null && !gxid.isEmpty()) {
					gx = serviceTemplate.selectOne("JGMES_GYGL_GXGL", " and GXGL_ID='" + gxid + "'");
					if (gx != null) {
						gx.setStr("GXGL_SOP", "");
					}
					//获取工艺路线下面工序的SOP
					String sql = "select t1.* from JGMES_GYGL_GYLXGX t1 left join JGMES_BASE_PRODUCTDATA t2 on t2.PRODUCTDATA_CPGYLXID=t1.GYLX_ID where t2.PRODUCTDATA_BH='" + cpCode + "' and GYLXGX_GXID='" + gxid + "'";
					List<DynaBean> gygxList = serviceTemplate.selectListBySql(sql);
					if (gygxList != null && gygxList.size() > 0) {
						DynaBean gygx = gygxList.get(0);//serviceTemplate.selectOne("JGMES_GYGL_GYLXGX", " and GYLXGX_GXID='"+gxid+"'");
						if (gygx != null && gx != null) {
							gx.setStr("GXGL_SOP", gygx.getStr("GYLXGX_SOP"));
							gx.setStr("GXGL_GYLX_GXID", gygx.getStr("GYLXGX_ID"));
							gx.set("GXGL_GXSXH", gygx.get("SY_ORDERINDEX"));
						}
					}
				} else {
					System.out.println("从产品工位工序对应表[" + cPGWGX + "]中未获取到工序ID数据！");
				}
			} else {
				System.out.println("产品[" + cpCode + "]、工位[" + gw.getStr("GW_GWBH") + "]无对应的产品工位工序对应表数据");
			}
		}
		return gx;
	}


	//文件上传方法，支持多文件上传
	//uplodPath想要上传的地址
	public String fileUplod(String uplodPath) {

		List<String> newfilePathList = new ArrayList<String>();//上传的文件地址
		MultiPartRequestWrapper req = (MultiPartRequestWrapper) request;
		String path = req.getRealPath("") + uplodPath;// 取系统当前路径
		File[] files = req.getFiles("files");
		List<HashMap> filePathList = new ArrayList<HashMap>(files.length);
		String msgString = "";
		String[] fileTypes = new String[]{""}; // 定义允许上传的文件扩展名
		long maxSize = 1024000; // 允许最大上传文件大小

		for (int i = 0; i < files.length; i++) {
			// 得到上传文件的扩展名
			String fileName = req.getFileNames("files")[i];

			String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1)
					.toLowerCase();

			File file = files[i];

			// 检查扩展名
//			   if (!Arrays.asList(fileTypes).contains(fileExt)) {
//			    msgString = "上传文件扩展名是不允许的扩展名。";
//			   }
			// 检查文件大小
			if (file.length() > maxSize) {
				msgString = "上传文件大小超过限制。";
			}
			// 检查目录写入权限
			File uploadDir = new File(path);
			if (!uploadDir.exists()) { //如果不存在 则创建
				uploadDir.mkdirs();
			}
			if (!uploadDir.canWrite()) {
				msgString = "上传目录没有写入权限。";
			}
			String newfilePath = JEUUID.uuid() + "." + fileExt;
			HashMap filePathMap = new HashMap();
			filePathMap.put("name", fileName);
			filePathMap.put("path", uplodPath + newfilePath);
			filePathMap.put("cls", "JE_FTYPE_BMP16");
			filePathMap.put("id", JEUUID.uuid());
			filePathMap.put("bigCls", "JE_FTYPE_BMP48");
			filePathList.add(filePathMap);
			FileOperate.copyFile(file, new File(path + newfilePath), false);
			newfilePathList.add(newfilePath);

		}
		if (files.length > 1) {
			return jsonBuilder.toJson(filePathList);
		} else if (files.length == 1) {
			return jsonBuilder.toJson(filePathList.get(0));
		} else {
			return "";
		}

	}

	/*
	 * 修改生产任务状态，工单状态
	 * scrwId:任务ID
	 * scrwZt:生产任务状态
	 * sl:完成数量
	 */
	public void doSaveScrwZt(String userCode, String scrwId, String scrwZt, int sl) {
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
		JgmesResult<String> ret = new JgmesResult<String>();
		try {
			System.out.println("生产任务单ID：" + scrwId);
			DynaBean scrwDynaBean = serviceTemplate.selectOneByPk("JGMES_PLAN_SCRW", scrwId);
			if (scrwDynaBean != null) {
				if (JgmesEnumsDic.ScDoing.getKey().equals(scrwZt)) {
					if (!JgmesEnumsDic.ScPause.getKey().equals(scrwDynaBean.getStr("SCRW_RWZT_CODE"))) {
						if (scrwDynaBean.getStr("SCRW_SJKGSJ") == null || "".equals(scrwDynaBean.getStr("SCRW_SJKGSJ"))) {
							scrwDynaBean.set("SCRW_SJKGSJ", jgmesCommon.getCurrentTime());
						}

						DynaBean gdDynaBean = serviceTemplate.selectOne("JGMES_PLAN_GDLB", " and JGMES_PLAN_GDLB_ID = '" + scrwDynaBean.getStr("JGMES_PLAN_GDLB_ID") + "'");///
						if (gdDynaBean != null) {
							if (gdDynaBean.getStr("GDLB_SJKGSJ") == null || "".equals(gdDynaBean.getStr("GDLB_SJKGSJ"))) {
								gdDynaBean.set("GDLB_SJKGSJ", jgmesCommon.getCurrentDate());
								serviceTemplate.update(gdDynaBean);
							}
						}
							
							/*
							List<DynaBean> cxList = serviceTemplate.selectList("JGMES_BASE_CXSJ", " and JGMES_PLAN_SCRW_ID='"+scrwId+"'");
							DynaBean cxDynaBean =new DynaBean();
							if (cxList != null && cxList.size()>0) {
								//
								cxDynaBean = cxList.get(0);
								cxDynaBean.set("JGMES_PLAN_SCRW_ID", scrwDynaBean.getStr("JGMES_PLAN_SCRW_ID"));
								serviceTemplate.update(cxDynaBean);
							}*/
					}
				}
				String newscrwZt = scrwZt;
				if (JgmesEnumsDic.ScFinished.getKey().equals(scrwZt)) {

					if (sl + scrwDynaBean.getInt("SCRW_WCSL") == scrwDynaBean.getInt("SCRW_PCSL")) {
						scrwDynaBean.set("SCRW_SJWGSJ", jgmesCommon.getCurrentTime());
					} else {
						newscrwZt = JgmesEnumsDic.ScDoing.getKey();
					}

					if (sl > 0) {
						scrwDynaBean.set("SCRW_WCSL", scrwDynaBean.getInt("SCRW_WCSL") + sl);

						//修改工单列表中的完成数量
						DynaBean gdDynaBean = serviceTemplate.selectOne("JGMES_PLAN_GDLB", " and JGMES_PLAN_GDLB_ID = '" + scrwDynaBean.getStr("JGMES_PLAN_GDLB_ID") + "'");///
						if (gdDynaBean != null) {
							gdDynaBean.set("GDLB_WCSL", gdDynaBean.getInt("GDLB_WCSL") + sl);
							serviceTemplate.update(gdDynaBean);
						}
					}

				}
				jgmesCommon.setDynaBeanDicScrwZt(scrwDynaBean, newscrwZt, "SCRW_RWZT_ID", "SCRW_RWZT_CODE",
						"SCRW_RWZT_NAME");
				serviceTemplate.update(scrwDynaBean);

				//判断工单下面的是否都完成
				if (JgmesEnumsDic.ScFinished.getKey().equals(scrwZt)) {
					List<DynaBean> scrwDynaBeanList = serviceTemplate.selectList("JGMES_PLAN_SCRW", " and SCRW_GDHM = '" + scrwDynaBean.getStr("SCRW_GDHM") + "'");
					boolean isComplete = true;
					int wcsl = 0;
					if (scrwDynaBeanList != null && scrwDynaBeanList.size() > 0) {
						for (DynaBean scrwDynaBean1 : scrwDynaBeanList) {
							if ("RWZT01".equals(scrwDynaBean1.getStr("SCRW_RWZT_CODE")) ||
									"RWZT02".equals(scrwDynaBean1.getStr("SCRW_RWZT_CODE")) ||
									"RWZT04".equals(scrwDynaBean1.getStr("SCRW_RWZT_CODE"))) {
								isComplete = false;
							}
							wcsl += scrwDynaBean1.getInt("SCRW_WCSL");
						}
						DynaBean gdDynaBean = serviceTemplate.selectOne("JGMES_PLAN_GDLB", " and JGMES_PLAN_GDLB_ID = '" + scrwDynaBean.getStr("JGMES_PLAN_GDLB_ID") + "'");///
						//DynaBean padDynaBean = serviceTemplate.selectOne("JGMES_PLAN_PCLB", " and PCLB_DDHM = '"+gdDynaBean.getStr("GDLB_DDHM")+"' and JGMES_PLAN_GDLB_ID = '"+gdDynaBean.getStr("JGMES_PLAN_GDLB_ID")+"'");
						DynaBean dic = jgmesCommon.getDic("JGMES_DIC_GDZT", "2");
						if (isComplete && gdDynaBean != null && wcsl >= gdDynaBean.getInt("GDLB_GDSL")) {
							if (dic != null && gdDynaBean != null) {
								gdDynaBean.set("GDLB_GDZT_NAME", dic.getStr("DICTIONARYITEM_ITEMNAME"));
								gdDynaBean.set("GDLB_GDZT_CODE", dic.getStr("DICTIONARYITEM_ITEMCODE"));
								gdDynaBean.set("GDLB_SJWGSJ", jgmesCommon.getCurrentDate());
								serviceTemplate.update(gdDynaBean);
							}
							//修改任务单中的工单状态
							for (DynaBean scrwDynaBean1 : scrwDynaBeanList) {
								if (dic != null) {
									scrwDynaBean1.set("SCRW_GDZT_NAME", dic.getStr("DICTIONARYITEM_ITEMNAME"));
									scrwDynaBean1.set("SCRW_GDZT_CODE", dic.getStr("DICTIONARYITEM_ITEMCODE"));
									serviceTemplate.update(scrwDynaBean1);
								}
							}
						}
					}
				}
					/*
					if (scrwZt.equalsIgnoreCase(JgmesEnumsDic.ScFinished.getKey()) || scrwZt.equalsIgnoreCase(JgmesEnumsDic.ScPause.getKey())) {//完成时删除产线指定的任务
						List<DynaBean> cxList = serviceTemplate.selectList("JGMES_BASE_CXSJ", " and JGMES_PLAN_SCRW_ID='"+scrwId+"'");
						DynaBean cxDynaBean =new DynaBean();
						if (cxList != null && cxList.size()>0) {
							//
							cxDynaBean = cxList.get(0);
							cxDynaBean.set("JGMES_PLAN_SCRW_ID", "");
							serviceTemplate.update(cxDynaBean);
						}
					}*/


			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ret.setMessage(e.toString());
			toWrite("保存状态出错：" + jsonBuilder.toJson(ret));
		}
	}

	public String checkSJ(String cxbm, String rwdh) {
		//判断是否进行首件检验
		String message = "";
		DynaBean zlglcsDynaBean = serviceTemplate.selectOne("JGMES_ZLGL_ZLGLCS", " and ZLGLCS_ZLKZFL1_CODE = 'SJJYKZ' and ZLGLCS_CXBM ='" + cxbm + "'");
		if (zlglcsDynaBean != null) {
			//按照任务单
			if (zlglcsDynaBean.getStr("ZLGLCS_ZLKZFL2_CODE") != null && "SJJYKZ02".equals(zlglcsDynaBean.getStr("ZLGLCS_ZLKZFL2_CODE"))) {
				List<DynaBean> sjjyDynaBean = serviceTemplate.selectList("JGMES_ZLGL_SJJY", "  and SJJY_CXBM = '" + cxbm + "' and SJJY_RWDH = '" + rwdh + "'");
				if (sjjyDynaBean == null || sjjyDynaBean.size() == 0) {
					message = "该生产任务单未进行首件检验，请先进行首件检验！";
				}

				//按天
			} else if (zlglcsDynaBean.getStr("ZLGLCS_ZLKZFL2_CODE") != null && "SJJYKZ03".equals(zlglcsDynaBean.getStr("ZLGLCS_ZLKZFL2_CODE"))) {
				List<DynaBean> sjjyDynaBean = serviceTemplate.selectList("JGMES_ZLGL_SJJY", "  and SJJY_CXBM = '" + cxbm + "' and str_to_date(SJJY_DJRQ,'%Y-%c-%d') = str_to_date(curdate(),'%Y-%c-%d')");
				if (sjjyDynaBean == null || sjjyDynaBean.size() == 0) {
					message = "今天未进行首件检验，请先进行首件检验！";
				}
			}
		}
		return message;
	}

	/**
	 * 获取异常详细信息，知道出了什么错，错在哪个类的第几行 .
	 *
	 * @param ex
	 * @return
	 */
	public String getExceptionDetail(Exception ex) {
		String ret = null;
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			PrintStream pout = new PrintStream(out);
			ex.printStackTrace(pout);
			ret = new String(out.toByteArray());
			pout.close();
			out.close();
		} catch (Exception e) {
		}
		return ret;
	}

	/**
	 * 获取异常详细信息，知道出了什么错，错在哪个类的第几行 .
	 *
	 * @param ex
	 * @return
	 */
	public static String getExceptionDetail2(Exception ex) {
		String ret = null;
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			PrintStream pout = new PrintStream(out);
			ex.printStackTrace(pout);
			ret = new String(out.toByteArray());
			pout.close();
			out.close();
		} catch (Exception e) {
		}
		return ret;
	}

	/**
	 * 把字符串转换成MD5格式
	 * @param psw
	 * @return
	 */
	public  String StringToMd5(String psw) {
		{
			try {
				MessageDigest md5 = MessageDigest.getInstance("MD5");
				md5.update(psw.getBytes("UTF-8"));
				byte[] encryption = md5.digest();

				StringBuffer strBuf = new StringBuffer();
				for (int i = 0; i < encryption.length; i++) {
					if (Integer.toHexString(0xff & encryption[i]).length() == 1) {
						strBuf.append("0").append(Integer.toHexString(0xff & encryption[i]));
					} else {
						strBuf.append(Integer.toHexString(0xff & encryption[i]));
					}
				}
				return strBuf.toString();
			} catch (NoSuchAlgorithmException e) {
				return "";
			} catch (UnsupportedEncodingException e) {
				return "";
			}
		}
	}

}
