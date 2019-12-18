package com.jgmes.action;

import com.je.core.action.DynaAction;
import com.je.core.util.JEUUID;
import com.je.core.util.bean.DynaBean;
import com.jgmes.util.JgmesBarCodeBase;
import com.jgmes.util.JgmesCommon;
import com.jgmes.util.JgmesEnumsErr;
import com.jgmes.util.JgmesResult;
import net.sf.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 
 * @author liuc
 * @version 2019-02-20 10:30:56
 * @see /jgmes/jgmesBarCodeAction!load.action
 */
@Component("jgmesBarCodeAction")
@Scope("prototype")
public class JgmesBarCodeAction extends DynaAction  {


	public void load(){
		toWrite("hello Action");
	}
	
	
	public void getInfoByBarCode() {
		// 用户编号
		String userCode = request.getParameter("userCode");
		String barCode = request.getParameter("barCode");
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
		String cpCode = jgmesCommon.getCpCodeByBarCode(barCode);
		JgmesResult<JgmesBarCodeBase> barCodeJgmesResult = jgmesCommon.getTmLxByBarCode(barCode, cpCode, null);
		JgmesBarCodeBase jgmesBarCodeBase= barCodeJgmesResult.Data;
		DynaBean wltmScDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA"," and PRODUCTDATA_BH='"+cpCode+"'");
		if(wltmScDynaBean!=null&&jgmesBarCodeBase!=null) {
			jgmesBarCodeBase.detailData = wltmScDynaBean;
		}

		barCodeJgmesResult.Data = jgmesBarCodeBase;
		toWrite(jsonBuilder.toJson(barCodeJgmesResult));
	}
	
	/**
	 * 
	 * 根据物料批次码来获取数量
	 */
	public void getNumberBySNcode() {
		// MAC地址
		String mac = request.getParameter("mac");
		// 用户编码
		String userCode = request.getParameter("userCode");
		// SN码
		String snCode = request.getParameter("snCode");
		String jsonStr = "";

		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
		JgmesResult<Integer> ret = new JgmesResult<Integer>();

		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {
			try {
				DynaBean wlDynaBean = serviceTemplate.selectOne("JGMES_BASE_WLTM", " and WLTM_TMH = '"+snCode+"'");
				if(wlDynaBean!=null) {
					ret.Data = wlDynaBean.getInt("WLTM_SL") - wlDynaBean.getInt("WLTM_YSYSL");
				}else {
					ret.setMessage("未找到数据，请确认输入的是否为物料批次码！");
				}

			} catch (Exception e) {
				// TODO: handle exception
				ret.IsSuccess = false;
				ret.setMessage("出错！详细信息：" + e.toString());
			}
			jsonStr = jsonBuilder.toJson(ret);
			toWrite(jsonStr);
		}
	}
	
	
	/**
	 * 
	 * 根据条码号来判断是物料条码，还是产品条码
	 */
	public void checkBarCodeLx() {
		// MAC地址
		String mac = request.getParameter("mac");
		// 用户编码
		String userCode = request.getParameter("userCode");
		// SN码
		String snCode = request.getParameter("snCode");
		String jsonStr = "";

		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
		JgmesResult<HashMap> ret=new JgmesResult<HashMap>();
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {
			try {
				Map<String,String> map=new HashMap<String,String>();
				//从产品条码中获取
				DynaBean tmScDynaBean = serviceTemplate.selectOne("JGMES_BASE_GDCPTM"," and GDCPTM_TMH='"+snCode+"'");
				
				//从物料条码中获取
				DynaBean wltmScDynaBean = serviceTemplate.selectOne("JGMES_BASE_WLTM"," and WLTM_TMH='"+snCode+"'");
				if(tmScDynaBean!=null) {
					map.put("IsMaterail", "false");
					map.put("BM", tmScDynaBean.getStr("GDCPTM_CPBH"));
					ret.Data = (HashMap) map;
				}else if(wltmScDynaBean!=null) {
					int sl = wltmScDynaBean.getInt("WLTM_SL");
					int yssl = wltmScDynaBean.getInt("WLTM_YSYSL");
					if(sl-yssl<=0){
						ret.setMessage("该物料已经使用了！");
					}else{
						map.put("IsMaterail", "true");
						map.put("BM", wltmScDynaBean.getStr("WLTM_WLBM"));
						ret.Data = (HashMap) map;
					}
				}else {
					//未知号码
					ret.setErrorDic(JgmesEnumsErr.CheckErrNoThisCode);
				}
			} catch (Exception e) {
				// TODO: handle exception
				ret.IsSuccess = false;
				ret.setMessage("出错！详细信息：" + e.toString());
			}
			jsonStr = jsonBuilder.toJson(ret);
			toWrite(jsonStr);
		}
	}

	/**
	 * 对栈板绑定主子表进行保存
	 */
	public synchronized void doSaveZBTM(){
		// MAC地址
		String mac = request.getParameter("mac");
		// 用户编码
		String userCode = request.getParameter("userCode");
		//栈板绑定主表
		String jsonString = request.getParameter("jsonStr");
		//栈板绑定子表
		String jsonStrDetail = request.getParameter("jsonStrDetail");


		String key = "";
		String value = "";
		StringBuffer message = new StringBuffer("");

		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
		JgmesResult<HashMap> ret=new JgmesResult<HashMap>();
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {
			DynaBean zbDynaBean = new DynaBean();
			if(jsonString!=null&&!"".equals(jsonString)){
				try {
					zbDynaBean = jgmesCommon.getDynaBeanByJsonStr("GMES_PB_ZBBD",jsonString);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				if(zbDynaBean!=null){
					//获取订单信息
					DynaBean gdDynaBean = serviceTemplate.selectOne("JGMES_PLAN_GDLB"," and GDLB_DDHM = '"+zbDynaBean.getStr("ZBBD_DDHM")+"'");
					if(gdDynaBean!=null){
						zbDynaBean.set("ZBBD_DDHM",gdDynaBean.getStr("GDLB_DDHM"));
						zbDynaBean.set("ZBBD_CPBM",gdDynaBean.getStr("GDLB_CPBH"));
						zbDynaBean.set("ZBBD_CPMC",gdDynaBean.getStr("GDLB_NAME"));
						zbDynaBean.set("ZBBD_CPGG",gdDynaBean.getStr("GDLB_CPGG"));
						zbDynaBean.set("ZBBD_DDSL",gdDynaBean.getStr("GDLB_DDSL"));
						//流程卡号
						zbDynaBean.set("ZBBD_LCKH",gdDynaBean.getStr("GDLB_LCKH"));
						if(zbDynaBean.getStr("GMES_PB_ZBBD_ID")!=null&&!"".equals(zbDynaBean.getStr("GMES_PB_ZBBD_ID"))){
							List<DynaBean> zbDynaBeanList = null;
							if(zbDynaBean.getStr("ZBBD_ZBTMH")!=null&&!"".equals(zbDynaBean.getStr("ZBBD_ZBTMH"))){
                                zbDynaBeanList = serviceTemplate.selectList("GMES_PB_ZBBD"," and ZBBD_ZBTMH = '"+zbDynaBean.getStr("ZBBD_ZBTMH")+"' and GMES_PB_ZBBD_ID !='"+zbDynaBean.getStr("GMES_PB_ZBBD_ID")+"'");
								if(zbDynaBeanList!=null&&zbDynaBeanList.size()>0){
									ret.setMessage("栈板条码号："+zbDynaBean.getStr("ZBBD_ZBTMH")+" 已经有绑定的数据了！");
									toWrite(jsonBuilder.toJson(ret));
									return;
								}
							}
							//校验是否是同一订单
							if(zbDynaBean.getStr("ZBBD_ZBTMH")!=null&&!"".equals(zbDynaBean.getStr("ZBBD_ZBTMH"))){
								DynaBean tmScDynaBean = serviceTemplate.selectOne("JGMES_BASE_GDCPTM"," and GDCPTM_TMH='"+zbDynaBean.getStr("ZBBD_ZBTMH")+"'");
								if(tmScDynaBean!=null&&tmScDynaBean.getStr("GDCPTM_DDHM")!=null&&tmScDynaBean.getStr("GDCPTM_DDHM").equals(zbDynaBean.getStr("ZBBD_DDHM"))){
								}else{
                                    ret.setMessage("栈板条码号不是本订单的，请检查！");
                                    toWrite(jsonBuilder.toJson(ret));
                                    return;
                                }
							}
							zbDynaBean.set("ZBBD_SJ",jgmesCommon.getCurrentTime());
							zbDynaBean = serviceTemplate.update(zbDynaBean);
						}else{
							List<DynaBean> zbDynaBeanList = null;
							if(zbDynaBean.getStr("ZBBD_ZBTMH")!=null&&!"".equals(zbDynaBean.getStr("ZBBD_ZBTMH"))){
                                zbDynaBeanList = serviceTemplate.selectList("GMES_PB_ZBBD"," and ZBBD_ZBTMH = '"+zbDynaBean.getStr("ZBBD_ZBTMH")+"'");
								if(zbDynaBeanList!=null&&zbDynaBeanList.size()>0){
									ret.setMessage("栈板条码号："+zbDynaBean.getStr("ZBBD_ZBTMH")+" 已经有绑定的数据了！");
									toWrite(jsonBuilder.toJson(ret));
									return;
								}
							}
							if(zbDynaBean.getStr("ZBBD_ZBTMH")!=null&&!"".equals(zbDynaBean.getStr("ZBBD_ZBTMH"))){
								//校验是否是同一订单
								DynaBean tmScDynaBean = serviceTemplate.selectOne("JGMES_BASE_GDCPTM"," and GDCPTM_TMH='"+zbDynaBean.getStr("ZBBD_ZBTMH")+"'");
								if(tmScDynaBean!=null&&tmScDynaBean.getStr("GDCPTM_DDHM")!=null&&tmScDynaBean.getStr("GDCPTM_DDHM").equals(zbDynaBean.getStr("ZBBD_DDHM"))){
								}else{
									ret.setMessage("栈板条码号不是本订单的，请检查！");
									toWrite(jsonBuilder.toJson(ret));
									return;
								}
							}
							zbDynaBean.set("ZBBD_SJ",jgmesCommon.getCurrentTime());
							zbDynaBean.set("GMES_PB_ZBBD_ID", JEUUID.uuid());
							zbDynaBean = serviceTemplate.insert(zbDynaBean);
						}

					}else{
						ret.setMessage("未获取到订单信息！");
						toWrite(jsonBuilder.toJson(ret));
						return;
					}
					if(zbDynaBean!=null){
						DynaBean newzbDynaBean  =zbDynaBean.clone();
						ret.Data = newzbDynaBean.getValues();
                    }
				}
			}
			List<DynaBean> zbzbDynaBeanList = new ArrayList<DynaBean>();
			if (jsonStrDetail != null && !jsonStrDetail.isEmpty()) {
				net.sf.json.JSONArray ja1 = net.sf.json.JSONArray.fromObject(jsonStrDetail);
				if (ja1.size() > 0) {
					for (int i = 0; i < ja1.size(); i++) {
						//	DynaBean bgsjzb = new DynaBean("aa");
						DynaBean zbzbDynaBean = new DynaBean("GMES_PB_ZBBDZB", true);
						// 遍历 jsonarray 数组，把每一个对象转成 json 对象
						JSONObject jb1 = ja1.getJSONObject(i);
						Iterator it1 = jb1.keys();
						while (it1.hasNext()) {
							key = (String) it1.next();
							value = jb1.getString(key);
							zbzbDynaBean.setStr(key, value);
						}

						DynaBean zbDynaBean1 = serviceTemplate.selectOne("GMES_PB_ZBBD"," and GMES_PB_ZBBD_ID = '"+zbzbDynaBean.getStr("ZBBDZB_ZBBDZBID")+"'");
						if(zbDynaBean1!=null){
							zbDynaBean = zbDynaBean1;
						}
						if(zbDynaBean!=null){
							//获取子表的条码号，来判断绑定的条码是否是本任务单的
							DynaBean tmScDynaBean = serviceTemplate.selectOne("JGMES_BASE_GDCPTM"," and GDCPTM_TMH='"+zbzbDynaBean.getStr("ZBBDZB_WXTMH")+"'");
							if(tmScDynaBean!=null){
								if(tmScDynaBean.getStr("GDCPTM_DDHM")!=null&&tmScDynaBean.getStr("GDCPTM_DDHM").equals(zbDynaBean.getStr("ZBBD_DDHM"))){

									//把栈板主表的主键ID存入进去
									DynaBean zbzbDynaBean1 = serviceTemplate.selectOne("GMES_PB_ZBBDZB",
											" and ZBBDZB_WXTMH = '"+zbzbDynaBean.getStr("ZBBDZB_WXTMH")+"' and ZBBDZB_ZBBDZBID != '"+zbDynaBean.getStr("GMES_PB_ZBBD_ID")+"'");
									if(zbzbDynaBean1==null){
										if(zbzbDynaBean.getStr("GMES_PB_ZBBDZB_ID")!=null&&!"".equals(zbzbDynaBean.getStr("GMES_PB_ZBBDZB_ID"))){
											zbzbDynaBean.set("ZBBDZB_ZBBDZBID", zbDynaBean.get("GMES_PB_ZBBD_ID"));
											serviceTemplate.update(zbzbDynaBean);
										}else{
											//判断条码号是否已经被绑定
											DynaBean zbzbDynaBean2 = serviceTemplate.selectOne("GMES_PB_ZBBDZB",
													" and ZBBDZB_WXTMH = '"+zbzbDynaBean.getStr("ZBBDZB_WXTMH")+"' and ZBBDZB_ZBBDZBID = '"+zbDynaBean.getStr("GMES_PB_ZBBD_ID")+"'");
											if(zbzbDynaBean2==null){
												zbzbDynaBean.set("GMES_PB_ZBBDZB_ID",JEUUID.uuid());
												zbzbDynaBean.set("ZBBDZB_ZBBDZBID", zbDynaBean.get("GMES_PB_ZBBD_ID"));
												jgmesCommon.setDynaBeanInfo(zbzbDynaBean);
												serviceTemplate.insert(zbzbDynaBean);
											}
										}
									}else{
										message.append(zbzbDynaBean.getStr("ZBBDZB_WXTMH")+"：此条码号已经被绑定！\n");
									}
								}else{
									message.append(zbzbDynaBean.getStr("ZBBDZB_WXTMH")+"：此条码号不是本订单的！请检查！\n");
								}
							}else{
								message.append(zbzbDynaBean.getStr("ZBBDZB_WXTMH")+"：未知条码号！\n");
							}
						}
					}
					//判断绑定是否完成
					if(zbDynaBean!=null){
						//查询子表数据数量
						List<DynaBean> zbDynaBeanList = serviceTemplate.selectList("GMES_PB_ZBBDZB"," and ZBBDZB_ZBBDZBID = '"+zbDynaBean.getStr("GMES_PB_ZBBD_ID")+"'");
						if(zbDynaBeanList!=null){
							if(zbDynaBeanList.size()==zbDynaBean.getInt("ZBBD_NXSL")&&"0".equals(zbDynaBean.getStr("ZBBD_NO_CODE"))&&zbDynaBean.getStr("ZBBD_ZBTMH")!=null&&!"".equals(zbDynaBean.getStr("ZBBD_ZBTMH"))){
								DynaBean dic = jgmesCommon.getDic("JGMES_YES_NO","1");
								if(dic!=null) {
									zbDynaBean.set("ZBBD_NO_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));
									zbDynaBean.set("ZBBD_NO_CODE", dic.get("DICTIONARYITEM_ITEMCODE"));
								}
							}
							zbDynaBean.set("ZBBD_YZSL",zbDynaBeanList.size());
							zbDynaBean = serviceTemplate.update(zbDynaBean);
							zbDynaBean.set("detail", ret.getValues(zbDynaBeanList));
							ret.Data = zbDynaBean.getValues();
						}

					}
				}
			}
			if(message!=null&&!"".equals(message.toString())){
				ret.setMessage(message.toString());
				ret.IsSuccess  = true;
			}
			toWrite(jsonBuilder.toJson(ret));
			return;
		}
		return;
	}

	/**
	 * 对栈板绑定进行删除
	 */
	public void doDeleteZBBD(){
		// MAC地址
		String mac = request.getParameter("mac");
		// 用户编码
		String userCode = request.getParameter("userCode");
		//栈板绑定主表ID
		String zbId = request.getParameter("zbId");
		//栈板绑定子表ID
		String zbzbId = request.getParameter("zbzbId");

		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);

		JgmesResult<HashMap> ret=new JgmesResult<HashMap>();
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {
			if((zbId==null||zbId.isEmpty())&&(zbzbId==null||zbzbId.isEmpty())){
				ret.setMessage("栈板绑定主表主键ID和栈板绑定子表主键ID不能同时为空！");
				toWrite(jsonBuilder.toJson(ret));
				return;
			}
			if(zbId!=null&&!"".equals(zbId)){
				String ids[] = zbId.split(",");
				if(ids!=null&&ids.length>0){
					for(String id:ids){
						//先删除子表，再删除主表
						serviceTemplate.deleteByWehreSql("GMES_PB_ZBBDZB"," and ZBBDZB_ZBBDZBID = '"+id+"'");
						//再删除主表
						serviceTemplate.deleteByWehreSql("GMES_PB_ZBBD"," and GMES_PB_ZBBD_ID = '"+id+"'");
					}
				}
			}
			if(zbzbId!=null&&!"".equals(zbzbId)){
				zbzbId = "'"+zbzbId.replaceAll(",","','")+"'";
				List<DynaBean> dynaBeanLlist = serviceTemplate.selectList("GMES_PB_ZBBDZB"," and GMES_PB_ZBBDZB_ID in ("+zbzbId+")");
				serviceTemplate.deleteByWehreSql("GMES_PB_ZBBDZB"," and GMES_PB_ZBBDZB_ID in ("+zbzbId+")");
				//修改主表中已经装箱数量
				if(dynaBeanLlist!=null&&dynaBeanLlist.size()>0){
					DynaBean dynaBean = serviceTemplate.selectOne("GMES_PB_ZBBD"," and GMES_PB_ZBBD_ID = '"+dynaBeanLlist.get(0).getStr("ZBBDZB_ZBBDZBID")+"'");
					if(dynaBean!=null){
						List<DynaBean> zbList = serviceTemplate.selectList("GMES_PB_ZBBDZB"," and ZBBDZB_ZBBDZBID = '"+dynaBean.getStr("GMES_PB_ZBBD_ID")+"'");
						DynaBean dic = jgmesCommon.getDic("JGMES_YES_NO","0");
						if(zbList!=null){
							dynaBean.set("ZBBD_YZSL",zbList.size());
							if(dynaBean.getInt("ZBBD_NXSL")>=dynaBean.getInt("ZBBD_YZSL")){
								dynaBean.set("ZBBD_NO_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));
								dynaBean.set("ZBBD_NO_CODE", dic.get("DICTIONARYITEM_ITEMCODE"));
							}
						}else{
							dynaBean.set("ZBBD_YZSL",0);
							if(dic!=null) {
								dynaBean.set("ZBBD_NO_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));
								dynaBean.set("ZBBD_NO_CODE", dic.get("DICTIONARYITEM_ITEMCODE"));
							}
						}

						serviceTemplate.update(dynaBean);
					}
				}

			}
			toWrite(jsonBuilder.toJson(ret));
		}
	}

	/**
	 * 对货柜绑定进行删除
	 */
	public void doDeleteHGBD(){
		// MAC地址
		String mac = request.getParameter("mac");
		// 用户编码
		String userCode = request.getParameter("userCode");
		//栈板绑定主表ID
		String zbId = request.getParameter("zbId");
		//栈板绑定子表ID
		String zbzbId = request.getParameter("zbzbId");

		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);

		JgmesResult<HashMap> ret=new JgmesResult<HashMap>();
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {
			if((zbId==null||zbId.isEmpty())&&(zbzbId==null||zbzbId.isEmpty())){
				ret.setMessage("货柜绑定主表主键ID和货柜绑定子表主键ID不能同时为空！");
				toWrite(jsonBuilder.toJson(ret));
				return;
			}
			if(zbId!=null&&!"".equals(zbId)){
				String ids[] = zbId.split(",");
				if(ids!=null&&ids.length>0){
					for(String id:ids){
						//先删除子表，再删除主表
						serviceTemplate.deleteByWehreSql("JGMES_PB_HGBDZB"," and HGBDZB_HGBDZJID = '"+id+"'");
						//再删除主表
						serviceTemplate.deleteByWehreSql("JGMES_PB_HGBD", " and JGMES_PB_HGBD_ID = '" +id+"'");
					}
				}
			}
			if(zbzbId!=null&&!"".equals(zbzbId)){
				zbzbId = "'"+zbzbId.replaceAll(",","','")+"'";
				List<DynaBean> dynaBeanLlist = serviceTemplate.selectList("JGMES_PB_HGBDZB"," and JGMES_PB_HGBDZB_ID in ("+zbzbId+")");
				serviceTemplate.deleteByWehreSql("JGMES_PB_HGBDZB"," and JGMES_PB_HGBDZB_ID in ("+zbzbId+")");

				//修改主表中已经装箱数量

				if(dynaBeanLlist!=null&&dynaBeanLlist.size()>0){
					DynaBean dynaBean = serviceTemplate.selectOne("JGMES_PB_HGBD"," and JGMES_PB_HGBD_ID = '"+dynaBeanLlist.get(0).getStr("HGBDZB_HGBDZJID")+"'");
					if(dynaBean!=null){
						List<DynaBean> zbList = serviceTemplate.selectList("JGMES_PB_HGBDZB"," and HGBDZB_HGBDZJID = '"+dynaBean.getStr("JGMES_PB_HGBD_ID")+"'");
						DynaBean dic = jgmesCommon.getDic("JGMES_YES_NO","0");
						if(zbList!=null){
							dynaBean.set("HGBD_YZSL",zbList.size());
							if(dynaBean.getInt("HGBD_NXSL")>=dynaBean.getInt("HGBD_YZSL")){
								dynaBean.set("HGBD_NO_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));
								dynaBean.set("HGBD_NO_CODE", dic.get("DICTIONARYITEM_ITEMCODE"));
							}
						}else{
							dynaBean.set("HGBD_YZSL",0);
							if(dic!=null) {
								dynaBean.set("HGBD_NO_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));
								dynaBean.set("HGBD_NO_CODE", dic.get("DICTIONARYITEM_ITEMCODE"));
							}
						}

						serviceTemplate.update(dynaBean);
					}
				}
			}
			toWrite(jsonBuilder.toJson(ret));
		}
	}



    /**
     * 对货柜绑定主子表进行
     */
    public synchronized void doSaveHGTM(){
        // MAC地址
        String mac = request.getParameter("mac");
        // 用户编码
        String userCode = request.getParameter("userCode");
        //栈板绑定主表
        String jsonString = request.getParameter("jsonStr");
        //栈板绑定子表
        String jsonStrDetail = request.getParameter("jsonStrDetail");


        String key = "";
        String value = "";
        StringBuffer message = new StringBuffer("");

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<HashMap> ret=new JgmesResult<HashMap>();
        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            DynaBean hgDynaBean = new DynaBean();
            if(jsonString!=null&&!"".equals(jsonString)){
                try {
                    hgDynaBean = jgmesCommon.getDynaBeanByJsonStr("JGMES_PB_HGBD",jsonString);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if(hgDynaBean!=null){
                    //获取订单信息
                    DynaBean gdDynaBean = serviceTemplate.selectOne("JGMES_PLAN_GDLB"," and GDLB_DDHM = '"+hgDynaBean.getStr("HGBD_DDHM")+"'");
                    if(gdDynaBean!=null){
                        hgDynaBean.set("HGBD_DDHM", gdDynaBean.getStr("GDLB_DDHM"));
                        hgDynaBean.set("HGBD_CPBH", gdDynaBean.getStr("GDLB_CPBH"));
                        hgDynaBean.set("HGBD_CPMC", gdDynaBean.getStr("GDLB_NAME"));
                        hgDynaBean.set("HGBD_CPGG", gdDynaBean.getStr("GDLB_CPGG"));
                        hgDynaBean.set("HGBD_DDSL", gdDynaBean.getStr("GDLB_DDSL"));
                    }
                    if(hgDynaBean.getStr("JGMES_PB_HGBD_ID")!=null&&!"".equals(hgDynaBean.getStr("JGMES_PB_HGBD_ID"))){
                        List<DynaBean> zbDynaBeanList = null;
                        if(hgDynaBean.getStr("HGBD_HGTMH")!=null&&!"".equals(hgDynaBean.getStr("HGBD_HGTMH"))){
                            zbDynaBeanList = serviceTemplate.selectList("JGMES_PB_HGBD",
                                    " and HGBD_HGTMH = '"+hgDynaBean.getStr("HGBD_HGTMH")+"' and JGMES_PB_HGBD_ID != '"+hgDynaBean.getStr("JGMES_PB_HGBD_ID")+"'");
                            if(zbDynaBeanList!=null&&zbDynaBeanList.size()>0){
                                ret.setMessage("货柜条码号："+hgDynaBean.getStr("HGBD_HGTMH")+" 已经有绑定的数据了！");
                                toWrite(jsonBuilder.toJson(ret));
                                return;
                            }
                        }
                        if(hgDynaBean.getStr("HGBD_HGTMH")!=null&&!"".equals(hgDynaBean.getStr("HGBD_HGTMH"))){
							//校验一下货柜编码是否纯在
							DynaBean hgapDynaBean = serviceTemplate.selectOne("JGMES_PB_HGAPZB"," and HGAPZB_HGBH = '"+hgDynaBean.getStr("HGBD_HGTMH")+"'");
							if(hgapDynaBean==null){
								ret.setMessage("货柜条码号不存在，请检查！");
								toWrite(jsonBuilder.toJson(ret));
								return;
							}
                        }
                        hgDynaBean.set("HGBD_SJ", jgmesCommon.getCurrentTime());
                        hgDynaBean = serviceTemplate.update(hgDynaBean);
                    }else{
                        List<DynaBean> zbDynaBeanList = serviceTemplate.selectList("JGMES_PB_HGBD",
                                    " and HGBD_HGTMH = '"+hgDynaBean.getStr("HGBD_HGTMH")+"'");
                        if(zbDynaBeanList!=null&&zbDynaBeanList.size()>0){
                            ret.setMessage("货柜条码号："+hgDynaBean.getStr("HGBD_HGTMH")+" 已经有绑定的数据了！");
                            toWrite(jsonBuilder.toJson(ret));
                            return;
                        }
						if(hgDynaBean.getStr("HGBD_HGTMH")!=null&&!"".equals(hgDynaBean.getStr("HGBD_HGTMH"))){
							//校验一下货柜编码是否纯在
							DynaBean hgapDynaBean = serviceTemplate.selectOne("JGMES_PB_HGAPZB"," and HGAPZB_HGBH = '"+hgDynaBean.getStr("HGBD_HGTMH")+"'");
							if(hgapDynaBean==null){
								ret.setMessage("货柜条码号不存在，请检查！");
								toWrite(jsonBuilder.toJson(ret));
								return;
							}
						}
                        hgDynaBean.set("JGMES_PB_HGBD_ID", JEUUID.uuid());
                        hgDynaBean.set("HGBD_SJ", jgmesCommon.getCurrentTime());
                        hgDynaBean = serviceTemplate.insert(hgDynaBean);
                    }
                    if(hgDynaBean!=null){
                        DynaBean newzbDynaBean  =hgDynaBean.clone();
                        ret.Data = newzbDynaBean.getValues();
                    }
                }
            }
            if (jsonStrDetail != null && !jsonStrDetail.isEmpty()) {
                net.sf.json.JSONArray ja1 = net.sf.json.JSONArray.fromObject(jsonStrDetail);
                if (ja1.size() > 0) {
                    for (int i = 0; i < ja1.size(); i++) {
                        //	DynaBean bgsjzb = new DynaBean("aa");
                        DynaBean hgzbDynaBean = new DynaBean("JGMES_PB_HGBDZB", true);
                        // 遍历 jsonarray 数组，把每一个对象转成 json 对象
                        JSONObject jb1 = ja1.getJSONObject(i);
                        Iterator it1 = jb1.keys();
                        while (it1.hasNext()) {
                            key = (String) it1.next();
                            value = jb1.getString(key);
                            hgzbDynaBean.setStr(key, value);
                        }

                        DynaBean hgDynaBean1 = serviceTemplate.selectOne("JGMES_PB_HGBD"," and JGMES_PB_HGBD_ID = '"+hgzbDynaBean.getStr("HGBDZB_HGBDZJID")+"'");
                        if(hgDynaBean1!=null){
                            hgDynaBean = hgDynaBean1;
                        }
                        if(hgDynaBean!=null){
                            List<DynaBean> hgapDynaBeanList = serviceTemplate.selectList("JGMES_PB_HGAPZBXQ",
                                    " and HGAPZBXQ_HGBH = '"+hgDynaBean.getStr("HGBD_HGTMH")+"' and HGAPZBXQ_ZBTMH = '"+hgzbDynaBean.getStr("HGBDZB_ZBTMH")+"'");
                            if(hgapDynaBeanList!=null&&hgapDynaBeanList.size()>0){
                                //把栈板主表的主键ID存入进去
                                DynaBean hgzbDynaBean1 = serviceTemplate.selectOne("JGMES_PB_HGBDZB",
                                        " and HGBDZB_ZBTMH = '"+hgzbDynaBean.getStr("HGBDZB_ZBTMH")+"' and HGBDZB_HGBDZJID != '"+hgDynaBean.getStr("JGMES_PB_HGBD_ID")+"'");
                                if(hgzbDynaBean1==null){
                                    if(hgzbDynaBean.getStr("JGMES_PB_HGBDZB_ID")!=null&&!"".equals(hgzbDynaBean.getStr("JGMES_PB_HGBDZB_ID"))){
                                        hgzbDynaBean.set("HGBDZB_HGBDZJID", hgDynaBean.get("JGMES_PB_HGBD_ID"));
                                        serviceTemplate.update(hgzbDynaBean);
                                    }else{
                                        //去除重复的
                                        DynaBean hgzbDynaBean2 = serviceTemplate.selectOne("JGMES_PB_HGBDZB",
                                                " and HGBDZB_ZBTMH = '"+hgzbDynaBean.getStr("HGBDZB_ZBTMH")+"' and HGBDZB_HGBDZJID = '"+hgDynaBean.getStr("JGMES_PB_HGBD_ID")+"'");
                                        if(hgzbDynaBean2==null){
                                            hgzbDynaBean.set("JGMES_PB_HGBDZB_ID",JEUUID.uuid());
                                            hgzbDynaBean.set("HGBDZB_HGBDZJID", hgDynaBean.get("JGMES_PB_HGBD_ID"));
                                            jgmesCommon.setDynaBeanInfo(hgzbDynaBean);
                                            serviceTemplate.insert(hgzbDynaBean);
                                        }
                                    }
                                }else{
                                    message.append(hgzbDynaBean.getStr("ZBBDZB_WXTMH")+"：此条码号已经被绑定！\n");
                                }
                            }else{
                                message.append(hgzbDynaBean.getStr("HGBDZB_ZBTMH")+"：在货柜安排中未找到此栈板号！请检查！\n");
                            }

                        }
                    }
                    //判断绑定是否完成
                    if(hgDynaBean!=null){
                        //查询子表数据数量
                        List<DynaBean> zbDynaBeanList = serviceTemplate.selectList("JGMES_PB_HGBDZB"," and HGBDZB_HGBDZJID = '"+hgDynaBean.getStr("JGMES_PB_HGBD_ID")+"'");
                        if(zbDynaBeanList!=null){
                            if(zbDynaBeanList.size()==hgDynaBean.getInt("HGBD_NZSL")&&"0".equals(hgDynaBean.getStr("HGBD_NO_CODE"))&&hgDynaBean.getStr("HGBD_HGTMH")!=null&&!"".equals(hgDynaBean.getStr("HGBD_HGTMH"))){
                                DynaBean dic = jgmesCommon.getDic("JGMES_YES_NO","1");
                                if(dic!=null) {
                                    hgDynaBean.set("HGBD_NO_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));
                                    hgDynaBean.set("HGBD_NO_CODE", dic.get("DICTIONARYITEM_ITEMCODE"));
                                }
                            }
                            hgDynaBean.set("HGBD_YZSL",zbDynaBeanList.size());
                            hgDynaBean = serviceTemplate.update(hgDynaBean);
                            hgDynaBean.set("detail", ret.getValues(zbDynaBeanList));
                            ret.Data = hgDynaBean.getValues();
                        }

                    }
                }
            }
            if(message!=null&&!"".equals(message.toString())){
                ret.setMessage(message.toString());
                ret.IsSuccess  = true;
            }
            toWrite(jsonBuilder.toJson(ret));
        }
    }





	/*
	/**
	 * 对货柜绑定主子表进行
	 */
	/*
	public void doSaveHGTM(){
		// MAC地址
		String mac = request.getParameter("mac");
		// 用户编码
		String userCode = request.getParameter("userCode");
		//栈板绑定主表
		String jsonString = request.getParameter("jsonStr");
		//栈板绑定子表
		String jsonStrDetail = request.getParameter("jsonStrDetail");


		String key = "";
		String value = "";
		StringBuffer message = new StringBuffer("");

		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
		JgmesResult<HashMap> ret=new JgmesResult<HashMap>();
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {
			DynaBean hgDynaBean = new DynaBean();
			if(jsonString!=null&&!"".equals(jsonString)){
				try {
					hgDynaBean = jgmesCommon.getDynaBeanByJsonStr("JGMES_PB_HGBD",jsonString);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				if(hgDynaBean!=null){
					//获取订单信息
					DynaBean gdDynaBean = serviceTemplate.selectOne("JGMES_PLAN_GDLB"," and GDLB_DDHM = '"+hgDynaBean.getStr("HGBD_DDHM")+"'");
					if(gdDynaBean!=null) {
						hgDynaBean.set("HGBD_DDHM", gdDynaBean.getStr("GDLB_DDHM"));
						hgDynaBean.set("HGBD_CPBH", gdDynaBean.getStr("GDLB_CPBH"));
						hgDynaBean.set("HGBD_CPMC", gdDynaBean.getStr("GDLB_NAME"));
						hgDynaBean.set("HGBD_CPGG", gdDynaBean.getStr("GDLB_CPGG"));
						hgDynaBean.set("HGBD_DDSL", gdDynaBean.getStr("GDLB_DDSL"));
						if(hgDynaBean.getStr("JGMES_PB_HGBD_ID")!=null&&!"".equals(hgDynaBean.getStr("JGMES_PB_HGBD_ID"))){
							DynaBean zbDynaBean1 = null;
							if(hgDynaBean.getStr("HGBD_HGTMH")!=null&&!"".equals(hgDynaBean.getStr("HGBD_HGTMH"))){
								zbDynaBean1 = serviceTemplate.selectOne("JGMES_PB_HGBD",
										" and HGBD_HGTMH = '"+hgDynaBean.getStr("HGBD_HGTMH")+"' and JGMES_PB_HGBD_ID != '"+hgDynaBean.getStr("JGMES_PB_HGBD_ID")+"'");
								if(zbDynaBean1!=null){
									ret.setMessage("货柜条码号："+hgDynaBean.getStr("HGBD_HGTMH")+" 已经有绑定的数据了！");
									toWrite(jsonBuilder.toJson(ret));
									return;
								}
							}
							if(hgDynaBean.getStr("HGBD_HGTMH")!=null&&!"".equals(hgDynaBean.getStr("HGBD_HGTMH"))){
								//校验是否是同一订单
								DynaBean tmScDynaBean = serviceTemplate.selectOne("JGMES_BASE_GDCPTM"," and GDCPTM_TMH='"+hgDynaBean.getStr("HGBD_HGTMH")+"'");
								if(tmScDynaBean!=null&&tmScDynaBean.getStr("GDCPTM_DDHM")!=null&&tmScDynaBean.getStr("GDCPTM_DDHM").equals(hgDynaBean.getStr("HGBD_DDHM"))){
								}else{
									ret.setMessage("货柜条码号不是本订单的，请检查！");
									toWrite(jsonBuilder.toJson(ret));
									return;
								}
							}
							//校验一下货柜编码是否纯在
							DynaBean hgapDynaBean = serviceTemplate.selectOne("JGMES_PB_HGAPZB"," and HGAPZB_HGBH = '"+hgDynaBean.getStr("HGBD_HGTMH")+"'");
							if(hgapDynaBean==null){
								ret.setMessage("货柜条码号不纯在，请检查！");
								toWrite(jsonBuilder.toJson(ret));
								return;
							}
							hgDynaBean.set("HGBD_SJ", jgmesCommon.getCurrentTime());
							hgDynaBean = serviceTemplate.update(hgDynaBean);
						}else{
							DynaBean zbDynaBean1 = null;
							if(hgDynaBean.getStr("HGBD_HGTMH")!=null&&!"".equals(hgDynaBean.getStr("HGBD_HGTMH"))){
								zbDynaBean1 = serviceTemplate.selectOne("JGMES_PB_HGBD",
										" and HGBD_HGTMH = '"+hgDynaBean.getStr("HGBD_HGTMH")+"' and JGMES_PB_HGBD_ID != '"+hgDynaBean.getStr("JGMES_PB_HGBD_ID")+"'");
								if(zbDynaBean1!=null){
									ret.setMessage("货柜条码号："+hgDynaBean.getStr("HGBD_HGTMH")+" 已经有绑定的数据了！");
									toWrite(jsonBuilder.toJson(ret));
									return;
								}
							}
							if(hgDynaBean.getStr("HGBD_HGTMH")!=null&&!"".equals(hgDynaBean.getStr("HGBD_HGTMH"))){
								//校验是否是同一订单
								DynaBean tmScDynaBean = serviceTemplate.selectOne("JGMES_BASE_GDCPTM"," and GDCPTM_TMH='"+hgDynaBean.getStr("HGBD_HGTMH")+"'");
								if(tmScDynaBean!=null&&tmScDynaBean.getStr("GDCPTM_DDHM")!=null&&tmScDynaBean.getStr("GDCPTM_DDHM").equals(hgDynaBean.getStr("HGBD_DDHM"))){
								}else{
									ret.setMessage("货柜条码号不是本订单的，请检查！");
									toWrite(jsonBuilder.toJson(ret));
									return;
								}
							}
							//校验一下货柜编码是否纯在
							DynaBean hgapDynaBean = serviceTemplate.selectOne("JGMES_PB_HGAPZB"," and HGAPZB_HGBH = '"+hgDynaBean.getStr("HGBD_HGTMH")+"'");
							if(hgapDynaBean==null){
								ret.setMessage("货柜条码号不纯在，请检查！");
								toWrite(jsonBuilder.toJson(ret));
								return;
							}
							hgDynaBean.set("JGMES_PB_HGBD_ID", JEUUID.uuid());
							hgDynaBean.set("HGBD_SJ", jgmesCommon.getCurrentTime());
							hgDynaBean = serviceTemplate.insert(hgDynaBean);
						}
					}else{
						ret.setMessage("未获取到订单信息！");
						toWrite(jsonBuilder.toJson(ret));
						return;
					}
				}
			}
			if (jsonStrDetail != null && !jsonStrDetail.isEmpty()) {
				net.sf.json.JSONArray ja1 = net.sf.json.JSONArray.fromObject(jsonStrDetail);
				if (ja1.size() > 0) {
					for (int i = 0; i < ja1.size(); i++) {
						//	DynaBean bgsjzb = new DynaBean("aa");
						DynaBean hgzbDynaBean = new DynaBean("JGMES_PB_HGBDZB", true);
						// 遍历 jsonarray 数组，把每一个对象转成 json 对象
						JSONObject jb1 = ja1.getJSONObject(i);
						Iterator it1 = jb1.keys();
						while (it1.hasNext()) {
							key = (String) it1.next();
							value = jb1.getString(key);
							hgzbDynaBean.setStr(key, value);
						}

						DynaBean hgDynaBean1 = serviceTemplate.selectOne("JGMES_PB_HGBD"," and JGMES_PB_HGBD_ID = '"+hgzbDynaBean.getStr("HGBDZB_HGBDZJID")+"'");
						if(hgDynaBean1!=null){
							hgDynaBean = hgDynaBean1;
						}
						if(hgDynaBean!=null){

							//获取子表的条码号，来判断绑定的条码是否是本任务单的
							DynaBean tmScDynaBean = serviceTemplate.selectOne("JGMES_BASE_GDCPTM"," and GDCPTM_TMH='"+hgzbDynaBean.getStr("HGBDZB_ZBTMH")+"'");
							if(tmScDynaBean!=null){
								if(tmScDynaBean.getStr("GDCPTM_DDHM")!=null&&tmScDynaBean.getStr("GDCPTM_DDHM").equals(hgDynaBean.getStr("HGBD_DDHM"))){
									//把栈板主表的主键ID存入进去
									DynaBean hgzbDynaBean1 = serviceTemplate.selectOne("JGMES_PB_HGBDZB",
											" and HGBDZB_ZBTMH = '"+hgzbDynaBean.getStr("HGBDZB_ZBTMH")+"' and HGBDZB_HGBDZJID != '"+hgDynaBean.getStr("JGMES_PB_HGBD_ID")+"'");
									if(hgzbDynaBean1==null){
										if(hgzbDynaBean.getStr("JGMES_PB_HGBDZB_ID")!=null&&!"".equals(hgzbDynaBean.getStr("JGMES_PB_HGBDZB_ID"))){
											hgzbDynaBean.set("HGBDZB_HGBDZJID", hgDynaBean.get("JGMES_PB_HGBD_ID"));
											serviceTemplate.update(hgzbDynaBean);
										}else{
											//去除重复的
											DynaBean hgzbDynaBean2 = serviceTemplate.selectOne("JGMES_PB_HGBDZB",
													" and HGBDZB_ZBTMH = '"+hgzbDynaBean.getStr("HGBDZB_ZBTMH")+"' and HGBDZB_HGBDZJID = '"+hgDynaBean.getStr("JGMES_PB_HGBD_ID")+"'");
											if(hgzbDynaBean2==null){
												hgzbDynaBean.set("JGMES_PB_HGBDZB_ID",JEUUID.uuid());
												hgzbDynaBean.set("HGBDZB_HGBDZJID", hgDynaBean.get("JGMES_PB_HGBD_ID"));
												jgmesCommon.setDynaBeanInfo(hgzbDynaBean);
												serviceTemplate.insert(hgzbDynaBean);
											}
										}
									}else{
										message.append(hgzbDynaBean.getStr("ZBBDZB_WXTMH")+"：此条码号已经被绑定！\n");
									}
								}else{
									message.append(hgzbDynaBean.getStr("HGBDZB_ZBTMH")+"：此条码号不是本订单的！请检查！\n");
								}
							}else{
								message.append(hgzbDynaBean.getStr("HGBDZB_ZBTMH")+"：未知条码号！\n");
							}
						}
					}
					//判断绑定是否完成
					if(hgDynaBean!=null){
						//查询子表数据数量
						List<DynaBean> zbDynaBeanList = serviceTemplate.selectList("JGMES_PB_HGBDZB"," and HGBDZB_HGBDZJID = '"+hgDynaBean.getStr("JGMES_PB_HGBD_ID")+"'");
						if(zbDynaBeanList!=null){
							if(zbDynaBeanList.size()==hgDynaBean.getInt("HGBD_NZSL")&&"0".equals(hgDynaBean.getStr("HGBD_NO_CODE"))&&hgDynaBean.getStr("HGBD_HGTMH")!=null&&!"".equals(hgDynaBean.getStr("HGBD_HGTMH"))){
								DynaBean dic = jgmesCommon.getDic("JGMES_YES_NO","1");
								if(dic!=null) {
									hgDynaBean.set("HGBD_NO_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));
									hgDynaBean.set("HGBD_NO_CODE", dic.get("DICTIONARYITEM_ITEMCODE"));
								}
							}
							hgDynaBean.set("HGBD_YZSL",zbDynaBeanList.size());
							hgDynaBean = serviceTemplate.update(hgDynaBean);
							hgDynaBean.set("detail", ret.getValues(zbDynaBeanList));
							ret.Data = hgDynaBean.getValues();
						}

					}
				}
			}
			if(message!=null&&!"".equals(message.toString())){
				ret.setMessage(message.toString());
				ret.IsSuccess  = true;
			}
			toWrite(jsonBuilder.toJson(ret));
		}
	}*/

	/**
	 * 获取栈板绑定
	 */
	public void getZBTMSj(){
		// MAC地址
		String mac = request.getParameter("mac");
		// 用户编码
		String userCode = request.getParameter("userCode");
		//是否完成
		String isComplete = request.getParameter("isComplete");
		//栈板条码号
		String barCode = request.getParameter("barCode");
		//订单号码
		String ddhm = request.getParameter("ddhm");
		// 每页码
		String pageSize = request.getParameter("PageSize");
		// 当前页
		String currPage = request.getParameter("CurrPage");


		String key = "";
		String value = "";
		StringBuffer message = new StringBuffer("");

		JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();

		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {
			StringBuffer whereSql = new StringBuffer();
			if(isComplete!=null&&!"".equals(isComplete)){
				whereSql.append(" and ZBBD_NO_CODE = '"+isComplete+"'");
			}
			if(ddhm!=null&&!"".equals(ddhm)){
				whereSql.append(" and ZBBD_DDHM = '"+ddhm+"'");
			}
			if(barCode!=null&&!"".equals(barCode)){
				whereSql.append(" and ZBBD_ZBTMH = '"+barCode+"'");
			}
			List<DynaBean> zbDynaBeanList  = serviceTemplate.selectList("GMES_PB_ZBBD",whereSql.toString());
			ret.TotalCount = Long.valueOf(zbDynaBeanList.size());
			if(pageSize!=null&&!"".equals(pageSize)&&currPage!=null&&!"".equals(currPage)){
				int kss = Integer.parseInt(pageSize)*(Integer.parseInt(currPage)-1);
				int size = Integer.parseInt(pageSize);
				whereSql.append("ORDER BY ZBBD_SJ desc,ZBBD_ZBTMH desc LIMIT "+kss+","+size+"");
			}
			zbDynaBeanList  = serviceTemplate.selectList("GMES_PB_ZBBD",whereSql.toString());
			if(zbDynaBeanList!=null&&zbDynaBeanList.size()>0){
				DynaBean zbDynaBean = new DynaBean();
				for (int i = 0; i < zbDynaBeanList.size(); i++) {
					zbDynaBean = zbDynaBeanList.get(i);
					List<DynaBean> zbzbList = serviceTemplate.selectList("GMES_PB_ZBBDZB",
							" and ZBBDZB_ZBBDZBID='" + zbDynaBean.getStr("GMES_PB_ZBBD_ID") + "' order by SY_ORDERINDEX ");
					if (zbzbList != null && zbzbList.size() > 0) {
						zbDynaBean.set("detail", ret.getValues(zbzbList));
					}
				}
				ret.Data = ret.getValues(zbDynaBeanList);
			}
			toWrite(jsonBuilder.toJson(ret));
		}
	}

	/**
	 * 获取货柜绑定
	 */
	public void getHGTMSj(){
		// MAC地址
		String mac = request.getParameter("mac");
		// 用户编码
		String userCode = request.getParameter("userCode");
		//是否完成
		String isComplete = request.getParameter("isComplete");
		//货柜条码号
		String barCode = request.getParameter("barCode");
		//订单号码
		String ddhm = request.getParameter("ddhm");
		//日期
		String rq = request.getParameter("rq");
		// 每页码
		String pageSize = request.getParameter("PageSize");
		// 当前页
		String currPage = request.getParameter("CurrPage");


		String key = "";
		String value = "";
		StringBuffer message = new StringBuffer("");

        SimpleDateFormat sdf =   new SimpleDateFormat( "yyyy-MM-dd" );

		JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();

		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {
			StringBuffer whereSql = new StringBuffer();
			if(isComplete!=null&&!"".equals(isComplete)){
				whereSql.append(" and HGBD_NO_CODE = '"+isComplete+"'");
			}
			if(ddhm!=null&&!"".equals(ddhm)){
				whereSql.append(" and HGBD_DDHM = '"+ddhm+"'");
			}
			if(barCode!=null&&!"".equals(barCode)){
				whereSql.append(" and HGBD_HGTMH = '"+barCode+"'");
			}
			if(rq!=null&&!"".equals(rq)){
				whereSql.append(" and str_to_date(HGBD_SJ,'%Y-%c-%d')=str_to_date('"+rq+"','%Y-%c-%d')");
			}

			List<DynaBean> hgDynaBeanList  = serviceTemplate.selectList("JGMES_PB_HGBD",whereSql.toString());
			ret.TotalCount = Long.valueOf(hgDynaBeanList.size());
			if(pageSize!=null&&!"".equals(pageSize)&&currPage!=null&&!"".equals(currPage)){
				int kss = Integer.parseInt(pageSize)*(Integer.parseInt(currPage)-1);
				int size = Integer.parseInt(pageSize);
				whereSql.append("ORDER BY HGBD_SJ desc,HGBD_HGTMH desc LIMIT "+kss+","+size+"");
			}
			hgDynaBeanList  = serviceTemplate.selectList("JGMES_PB_HGBD",whereSql.toString());
			if(hgDynaBeanList!=null&&hgDynaBeanList.size()>0){
				DynaBean zbDynaBean = new DynaBean();
				for (int i = 0; i < hgDynaBeanList.size(); i++) {
					zbDynaBean = hgDynaBeanList.get(i);
                    try {
                        if(zbDynaBean.getStr("HGBD_SJ")!=null&&!"".equals(zbDynaBean.getStr("HGBD_SJ"))){
                            zbDynaBean.set("HGBD_SJ",sdf.format(sdf.parse(zbDynaBean.getStr("HGBD_SJ"))));
                        }
                    } catch (Exception e) {
                        logger.error(JgmesCommon.getExceptionDetail2(e));
                        e.printStackTrace();
                    }
                    List<DynaBean> hgzbList = serviceTemplate.selectList("JGMES_PB_HGBDZB",
							" and HGBDZB_HGBDZJID='" + zbDynaBean.getStr("JGMES_PB_HGBD_ID") + "' order by SY_ORDERINDEX ");
					if (hgzbList != null && hgzbList.size() > 0) {
						zbDynaBean.set("detail", ret.getValues(hgzbList));
					}
				}
				ret.Data = ret.getValues(hgDynaBeanList);
			}
			toWrite(jsonBuilder.toJson(ret));
		}
	}


	/**
	 * 获取待装柜列表
	 */
	public void getPendingHg(){
		// MAC地址
		String mac = request.getParameter("mac");
		// 用户编码
		String userCode = request.getParameter("userCode");
		//日期
		String rq = request.getParameter("rq");
		//发向地
		String fxd = request.getParameter("fxd");
		//货柜编号
		String hgbh = request.getParameter("hgbh");
        // 每页码
        String pageSize = request.getParameter("PageSize");
        // 当前页
        String currPage = request.getParameter("CurrPage");


		String key = "";
		String value = "";
		StringBuffer sql = new StringBuffer();

		JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();

		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {
			if(rq!=null&&!"".equals(rq)){
				sql.append(" and str_to_date(HGAPZB_RQ,'%Y-%c-%d') = str_to_date('"+rq+"','%Y-%c-%d')");
			}
			if(fxd!=null&&!"".equals(fxd)){
				sql.append(" and HGAPZB_FXD = '"+fxd+"'");
			}
			if(hgbh!=null&&!"".equals(hgbh)){
				sql.append(" and HGAPZB_HGBH = '"+hgbh+"'");
			}
            List<DynaBean> hgapzbDynaBeanList  = serviceTemplate.selectList("JGMES_PB_HGAPZB",sql.toString());
            ret.TotalCount = Long.valueOf(hgapzbDynaBeanList.size());
            sql.append("ORDER BY HGAPZB_RQ desc,HGAPZB_HGBH desc");
            if(pageSize!=null&&!"".equals(pageSize)&&currPage!=null&&!"".equals(currPage)){
                int kss = Integer.parseInt(pageSize)*(Integer.parseInt(currPage)-1);
                int size = Integer.parseInt(pageSize);
                sql.append(" LIMIT "+kss+","+size+"");
            }
			hgapzbDynaBeanList = serviceTemplate.selectList("JGMES_PB_HGAPZB",sql.toString());
			if(hgapzbDynaBeanList!=null&&hgapzbDynaBeanList.size()>0){
				ret.Data = ret.getValues(hgapzbDynaBeanList);
			}
			toWrite(jsonBuilder.toJson(ret));
		}
	}


	/**
	 * 获取货柜安排详情
	 */
	public void getPendingHgDetail(){
		// MAC地址
		String mac = request.getParameter("mac");
		// 用户编码
		String userCode = request.getParameter("userCode");
		//条码号
		String barCode = request.getParameter("barCode");

		JgmesResult<HashMap> ret = new JgmesResult<>();

		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {
			if(barCode==null||barCode.isEmpty()){
				ret.setMessage("未获取到条码号！");
			}
			//先去查一下是否是货柜
			DynaBean hgapzbDynaBean = serviceTemplate.selectOne("JGMES_PB_HGAPZB"," and HGAPZB_HGBH = '"+barCode+"'");
			if(hgapzbDynaBean!=null){
				//获取货柜详情
				List<DynaBean> hgapzbxqDynaBeanList = serviceTemplate.selectList("JGMES_PB_HGAPZBXQ"," and HGAPZBXQ_HGBH = '"+hgapzbDynaBean.getStr("HGAPZB_HGBH")+"'");
				if(hgapzbxqDynaBeanList!=null&&hgapzbxqDynaBeanList.size()>0){
					hgapzbDynaBean.set("detail",ret.getValues(hgapzbxqDynaBeanList));
				}
				ret.Data = hgapzbDynaBean.getValues();
			}else{
				DynaBean hgapzbxqDynaBean = serviceTemplate.selectOne("JGMES_PB_HGAPZBXQ"," and HGAPZBXQ_ZBTMH = '"+barCode+"'");
				if(hgapzbxqDynaBean!=null){
					hgapzbDynaBean = serviceTemplate.selectOne("JGMES_PB_HGAPZB"," and HGAPZB_HGBH = '"+hgapzbxqDynaBean.getStr("HGAPZBXQ_HGBH")+"'");
					if(hgapzbDynaBean!=null){
						//获取货柜详情
						List<DynaBean> hgapzbxqDynaBeanList = serviceTemplate.selectList("JGMES_PB_HGAPZBXQ"," and HGAPZBXQ_HGBH = '"+hgapzbDynaBean.getStr("HGAPZB_HGBH")+"'");
						if(hgapzbxqDynaBeanList!=null&&hgapzbxqDynaBeanList.size()>0){
							hgapzbDynaBean.set("detail",ret.getValues(hgapzbxqDynaBeanList));
						}
						ret.Data = hgapzbDynaBean.getValues();
					}
				}else{
					ret.setMessage("未在货柜安排中获取到此条码号！");
				}
			}
			toWrite(jsonBuilder.toJson(ret));
		}
	}
	
	
	/**
     * 校验用户合法性，不合法直接提示。
     */
    private JgmesResult<String> doCheck(String userCode,String mac) {
    	JgmesResult<String> result = new JgmesCommon(request, serviceTemplate).doCheckRes(mac, userCode);
    	if (!result.IsSuccess) {
			toWrite(jsonBuilder.toJson(result));
		}
    	return result;
	}



	
}