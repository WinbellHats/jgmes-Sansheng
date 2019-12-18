package com.jgmes.action;

import com.je.core.action.DynaAction;
import com.je.core.dao.PCDaoTemplate;
import com.je.core.service.PCDynaServiceTemplate;
import com.je.core.util.JEUUID;
import com.je.core.util.JdbcUtil;
import com.je.core.util.SpringContextHolder;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.DynaBean;
import com.je.table.exception.PCExcuteException;
import com.jgmes.util.*;
import net.sf.json.JSONObject;
import org.codehaus.jettison.json.JSONArray;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author fm
 * @version 2018-12-17 21:20:50
 * @see /jgmes/commonAction!load.action
 */
@Component("commonAction")
@Scope("prototype")
public class CommonAction extends DynaAction {

    @Override
    public void doSave() {
        // TODO Auto-generated method stub

        toWrite(jsonBuilder.returnSuccessJson("\"添加成功\""));
        super.doSave();
    }


    /*do
     * 通用单表保存方法
     * */
    public void doJsonSave() throws ParseException {
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);

        String key = "";
        String value = "";
        String jsonString = ((String[]) requestParams.get("jsonStr"))[0];
        String tabCode = ((String[]) requestParams.get("tabCode"))[0];

        DynaBean bgsj = new DynaBean();
        bgsj.setStr(beanUtils.KEY_TABLE_CODE, tabCode);


        JSONObject jb1 = JSONObject.fromObject(jsonString);
        Iterator it = jb1.keys();
        List<String> keyListstr = new ArrayList<String>();
        while (it.hasNext()) {
            key = (String) it.next();
            value = jb1.getString(key);
            bgsj.setStr(key, value);
        }

        jgmesCommon.setDynaBeanInfo(bgsj);
//		serviceTemplate.buildModelCreateInfo(bgsj);
        serviceTemplate.insert(bgsj);

        toWrite(jsonBuilder.returnSuccessJson("\"添加成功\""));

    }

    /*
     * 报工主表 保存方法
     * */
    private boolean doJsonSaveBgSj(String jsonStr, String userCode) throws ParseException {
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);

        boolean res = true;
        String key = "";
        String value = "";
        String tabCode = "JGMES_PB_BGSJ";// ((String[])requestParams.get("tabCode"))[0];
        String userName = "";
        String bGSJ_GZSJ = "";//过站时间
        System.out.println("jsonString:" + jsonStr);

        DynaBean bgsj = new DynaBean();
        bgsj.setStr(beanUtils.KEY_TABLE_CODE, tabCode);

        try {
            //获取当前登陆用户
            if (userCode != null && !userCode.isEmpty()) {
                jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
            }

            userCode = jgmesCommon.jgmesUser.getCurrentUserCode();
            userName = jgmesCommon.jgmesUser.getCurrentUserName();
            System.out.println("userCode:" + userCode);
            System.out.println("userName:" + userName);
            //将传入的参数转到表中
            //		bgsj= (DynaBean) jsonBuilder.fromJson(jsonString, bgsj.getClass());
            JSONObject jb1 = JSONObject.fromObject(jsonStr);
            Iterator it = jb1.keys();
            List<String> keyListstr = new ArrayList<String>();
            while (it.hasNext()) {
                key = (String) it.next();
                value = jb1.getString(key);
                bgsj.setStr(key, value);
            }

            DynaBean gw = getCurrentGW(userCode);
            if (gw != null) {
                bgsj.setStr("BGSJ_GWBH", gw.getStr("GW_GWBH"));
                bgsj.setStr("BGSJ_GWMC", gw.getStr("GW_GWMC"));
                bgsj.setStr("BGSJ_GWID", gw.getStr("JGMES_BASE_GW_ID"));
            }

            DynaBean gx = getCurrentGX(bgsj.getStr("BGSJ_CPBH"), userCode);
            if (gx != null) {
                bgsj.setStr("BGSJ_GXID", gx.getStr("GXGL_ID"));
                bgsj.setStr("BGSJ_GXBH", gx.getStr("GXGL_NUM"));
                bgsj.setStr("BGSJ_GXMC", gx.getStr("GXGL_NAME"));
            }


            //过站时间
            bGSJ_GZSJ = bgsj.getStr("BGSJ_GZSJ");
            if (bGSJ_GZSJ == null || bGSJ_GZSJ.isEmpty()) {
                bgsj.set("BGSJ_GZSJ", jgmesCommon.getCurrentTime());
            }

            jgmesCommon.setDynaBeanInfo(bgsj);
            //		serviceTemplate.buildModelCreateInfo(bgsj);
            serviceTemplate.insert(bgsj);
        } catch (Exception e) {
            logger.error(e.toString());
            // TODO: handle exception
            res = false;
        }

        return res;
    }

    /*
     * 报工子表保存方法
     * */
    private boolean doJsonSaveBgSjZb(String jsonArrStr, String userCode) throws ParseException {
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);

        boolean res = true;
        String key = "";
        String value = "";
        String tabCode = "JGMES_PB_BGSJZB";// ((String[])requestParams.get("tabCode"))[0];
        String userName = "";
        System.out.println("jsonArrStr:" + jsonArrStr);
        List<DynaBean> list = new ArrayList<DynaBean>();

        try {
            //获取当前登陆用户
            if (userCode != null && !userCode.isEmpty()) {
                jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
            }

            userCode = jgmesCommon.jgmesUser.getCurrentUserCode();
            userName = jgmesCommon.jgmesUser.getCurrentUserName();
            System.out.println("userCode:" + userCode);
            System.out.println("userName:" + userName);

            //将传入的参数转到表中
            //		bgsj= (DynaBean) jsonBuilder.fromJson(jsonString, bgsj.getClass());
            net.sf.json.JSONArray ja1 = net.sf.json.JSONArray.fromObject(jsonArrStr);
            if (ja1.size() > 0) {
                for (int i = 0; i < ja1.size(); i++) {
                    DynaBean bgsj = new DynaBean();
                    bgsj.setStr(beanUtils.KEY_TABLE_CODE, tabCode);

                    JSONObject jb1 = ja1.getJSONObject(i);  // 遍历 jsonarray 数组，把每一个对象转成 json 对象
                    Iterator it = jb1.keys();
                    while (it.hasNext()) {
                        key = (String) it.next();
                        value = jb1.getString(key);
                        bgsj.setStr(key, value);
                    }
                    jgmesCommon.setDynaBeanInfo(bgsj);
                    list.add(bgsj);
                }
            }

            serviceTemplate.insert(list);
        } catch (Exception e) {
            logger.error(JgmesCommon.getExceptionDetail2(e));
            // TODO: handle exception
            res = false;
        }

        return res;
    }

    /*
     * 根据报工数据回写产品生产明细情况表:JGMES_PB_SCDETAIL
     * lxc
     * 参数
     * bgsjDynaBean   :报工数据
     * detailTmList:关联的报工数据子表的条码的接口
     */
    private JgmesResult<String> doSaveScDetail(DynaBean bgsjDynaBean, List<DynaBean> list) {
        //条码号
        String BGSJ_TMH = bgsjDynaBean.getStr("BGSJ_TMH");
        //getTmLxByBarCode
        //过站时间
        String BGSJ_GZSJ = (String) bgsjDynaBean.get("BGSJ_GZSJ");
        //产线编码
        String BGSJ_CXBM = (String) bgsjDynaBean.get("BGSJ_CXBM");
        //产品编码
        String BGSJ_CPBH = (String) bgsjDynaBean.get("BGSJ_CPBH");
        //产线名称
        String BGSJ_CXMC = (String) bgsjDynaBean.get("BGSJ_CXMC");
        //当前工序ID
        String BGSJ_GXID = (String) bgsjDynaBean.get("BGSJ_GXID");
        //当前工序编号
        String BGSJ_GXBH = (String) bgsjDynaBean.get("BGSJ_GXBH");
        //工序顺序号
        int BGSJ_GXSXH = (int) bgsjDynaBean.get("BGSJ_GXSXH");
        //工艺路线_工序_外键ID
        String GYLXGX_ID = (String) bgsjDynaBean.get("GYLXGX_ID");
        //
        String BGSJZB_GXMC = (String) bgsjDynaBean.get("BGSJZB_GXMC");
        //获取条码类型
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        JgmesResult<JgmesBarCodeBase> tmLxByBarCode = jgmesCommon.getTmLxByBarCode(BGSJ_TMH, BGSJ_CPBH, null);
        //条码类型
        JgmesEnumsDic tmLx = tmLxByBarCode.Data.TmLx;

        //当前时间
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        String scrq = formatter.format(new java.util.Date());
        DynaBean JGMES_PB_SCDETAIL = null;


        JgmesEnumsDic ss = JgmesEnumsDic.TMCP;

        //产品条码or 流程卡条码
        if (JgmesEnumsDic.TMCP.equals(tmLx) || JgmesEnumsDic.TMLCK.equals(tmLx)) {
            String tmType = "SCDETAIL_CPTM";
            if (JgmesEnumsDic.TMLCK.equals(tmLx)) {
                tmType = "SCDETAIL_LCKTM";
            }
            JGMES_PB_SCDETAIL = serviceTemplate.selectOne("JGMES_PB_SCDETAIL", "  and (SCDETAIL_CPTM='" + BGSJ_TMH + "' or  SCDETAIL_LCKTM='" + BGSJ_TMH + "')");
            if (BGSJ_CXBM != null && !BGSJ_CXBM.isEmpty()) {
                //获取产品信息   JGMES_BASE_PRODUCTDATA 产品信息表
                DynaBean productData = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " AND  PRODUCTDATA_BH='" + BGSJ_CPBH + "'");
                //工艺路线编号
                String PRODUCTDATA_CPGYLX = (String) productData.get("PRODUCTDATA_CPGYLX");
                if (PRODUCTDATA_CPGYLX != null && !PRODUCTDATA_CPGYLX.isEmpty()) {
                    //JGMES_GYGL_GYLX  工艺路线表
                    DynaBean GYLX_GYLXNAME = serviceTemplate.selectOne("JGMES_GYGL_GYLX", " AND  GYLX_GYLXNAME='" + PRODUCTDATA_CPGYLX + "'");
                    //工艺路线主键id
                    String GYLX_ID = (String) GYLX_GYLXNAME.get("GYLX_ID");
                    //获取工艺路线的所有工序
                    if (GYLX_ID != null && !GYLX_ID.isEmpty()) {//
                        List<DynaBean> jgmesGYLXGX = serviceTemplate.selectList("JGMES_GYGL_GYLXGX", " AND  GYLX_ID='" + GYLX_ID + "'   ORDER BY SY_ORDERINDEX asc");
                        //当前工序所在序列
                        int listsize = 0;
                        //当前工序
                        for (int i = 0; i < jgmesGYLXGX.size(); i++) {
                            if (jgmesGYLXGX.get(i).get("SY_ORDERINDEX").equals(BGSJ_GXSXH)) {
                                listsize = i + 1;
                                break;
                            }
                        }
                        //第一道工序
                        if (listsize == 1 && jgmesGYLXGX.size() > 0) {
                            if (JGMES_PB_SCDETAIL == null) {
                                JGMES_PB_SCDETAIL = new DynaBean("JGMES_PB_SCDETAIL", true);
                                evalDynaBean(JGMES_PB_SCDETAIL, bgsjDynaBean, jgmesGYLXGX, tmType, listsize);
                                try {
                                    jgmesCommon.setDynaBeanInfo(JGMES_PB_SCDETAIL);
                                } catch (Exception e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                //SCDETAIL_DQTMLX  当前条码类型
                                serviceTemplate.insert(JGMES_PB_SCDETAIL);
                            }
                        } else if (listsize == jgmesGYLXGX.size()) {//最后一道工序
                            if (JGMES_PB_SCDETAIL == null) {
                                JGMES_PB_SCDETAIL = new DynaBean("JGMES_PB_SCDETAIL", true);
                            }
                            evalDynaBean(JGMES_PB_SCDETAIL, bgsjDynaBean, jgmesGYLXGX, tmType, listsize);
                            serviceTemplate.update(JGMES_PB_SCDETAIL);
                        } else {//中间工序
                            if (JGMES_PB_SCDETAIL == null) {
                                JGMES_PB_SCDETAIL = new DynaBean("JGMES_PB_SCDETAIL", true);
                            }
                            evalDynaBean(JGMES_PB_SCDETAIL, bgsjDynaBean, jgmesGYLXGX, tmType, listsize);
                            serviceTemplate.update(JGMES_PB_SCDETAIL);
                        }
                    }
                }
            }
        }
        if (JgmesEnumsDic.TMZX.equals(tmLx) || JgmesEnumsDic.TMWX.equals(tmLx) || JgmesEnumsDic.TMZB.equals(tmLx)) {
            String tmType = "SCDETAIL_ZXTM";
            if (JgmesEnumsDic.TMZX.equals(tmLx)) {
                tmType = "SCDETAIL_ZBTM";
            }
            if (JgmesEnumsDic.TMZX.equals(tmLx)) {
                tmType = "SCDETAIL_WXTM";
            }
            //报工数据主键---JGMES_PB_BGSJ_ID
            List<DynaBean> bgsjzbList = serviceTemplate.selectList("JGMES_PB_BGSJZB", " AND  JGMES_PB_BGSJ_ID='" + bgsjDynaBean.getStr("JGMES_PB_BGSJ_ID") + "'");
            for (int i = 0; i < bgsjzbList.size(); i++) {
                DynaBean PRODUCTDATA = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " and PRODUCTDATA_BH='" + bgsjzbList.get(i).getStr("BGSJZB_WLBH") + "'");//PRODUCTDATA_CPGYLXID
                List<DynaBean> jgmesGYLXGX = serviceTemplate.selectList("JGMES_GYGL_GYLXGX", " AND  GYLX_ID='" + PRODUCTDATA.getStr("PRODUCTDATA_CPGYLXID") + "' ORDER BY SY_ORDERINDEX asc");
                //当前工序所在序列
                int listsize = 0;
                //当前工序
                for (int n = 0; n < jgmesGYLXGX.size(); n++) {
                    if (jgmesGYLXGX.get(n).get("SY_ORDERINDEX").equals(BGSJ_GXSXH)) {
                        listsize = n + 1;
                        break;
                    }
                }

                if (jgmesGYLXGX != null && jgmesGYLXGX.size() > 0) {
                    JGMES_PB_SCDETAIL = serviceTemplate.selectOne("JGMES_PB_SCDETAIL", "  and (SCDETAIL_CPTM='" + bgsjzbList.get(i).getStr("BGSJZB_TMH") + "' or  SCDETAIL_LCKTM='" + bgsjzbList.get(i).getStr("BGSJZB_TMH") + "')");
                    if (JGMES_PB_SCDETAIL == null) {
                        JGMES_PB_SCDETAIL = new DynaBean("JGMES_PB_SCDETAIL", true);

                        evalDynaBean(JGMES_PB_SCDETAIL, bgsjDynaBean, jgmesGYLXGX, tmType, listsize);
                        try {
                            jgmesCommon.setDynaBeanInfo(JGMES_PB_SCDETAIL);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        //SCDETAIL_DQTMLX  当前条码类型
                        serviceTemplate.insert(JGMES_PB_SCDETAIL);
                    } else {
                        //int listsize = JGMES_PB_SCDETAIL.getInt("SCDETAIL_DQGXSZXL");
                        if (JGMES_PB_SCDETAIL == null) {
                            JGMES_PB_SCDETAIL = new DynaBean("JGMES_PB_SCDETAIL", true);
                        }
                        evalDynaBean(JGMES_PB_SCDETAIL, bgsjDynaBean, jgmesGYLXGX, tmType, listsize);
                        serviceTemplate.update(JGMES_PB_SCDETAIL);
                    }
                }
            }
        }
        if (JgmesEnumsDic.TMGZB.equals(tmLx)) {
            JGMES_PB_SCDETAIL = serviceTemplate.selectOne("JGMES_PB_SCDETAIL", "  and  SCDETAIL_GZBTM='" + BGSJ_TMH + "'  and SCDETAIL_JSBJ='0'");
            if (BGSJ_CXBM != null && !BGSJ_CXBM.isEmpty()) {
                //获取产品信息   JGMES_BASE_PRODUCTDATA 产品信息表
                DynaBean productData = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " AND  PRODUCTDATA_BH='" + BGSJ_CPBH + "'");
                //工艺路线编号
                String PRODUCTDATA_CPGYLX = (String) productData.get("PRODUCTDATA_CPGYLX");
                if (PRODUCTDATA_CPGYLX != null && !PRODUCTDATA_CPGYLX.isEmpty()) {
                    //JGMES_GYGL_GYLX  工艺路线表
                    DynaBean GYLX_GYLXNUM = serviceTemplate.selectOne("JGMES_GYGL_GYLX", " AND  GYLX_GYLXNAME='" + PRODUCTDATA_CPGYLX + "'");
                    //工艺路线主键id
                    String GYLX_ID = (String) GYLX_GYLXNUM.get("GYLX_ID");
                    //获取工艺路线的所有工序
                    if (GYLX_ID != null && !GYLX_ID.isEmpty()) {//
                        List<DynaBean> jgmesGYLXGX = serviceTemplate.selectList("JGMES_GYGL_GYLXGX", " AND  GYLX_ID='" + GYLX_ID + "'   ORDER BY SY_ORDERINDEX asc");
                        if (jgmesGYLXGX != null && jgmesGYLXGX.size() > 0) {
                            //当前工序所在序列
                            int listsize = 0;
                            //当前工序
                            for (int i = 0; i < jgmesGYLXGX.size(); i++) {
                                if (jgmesGYLXGX.get(i).get("SY_ORDERINDEX").equals(BGSJ_GXSXH)) {
                                    listsize = i + 1;
                                    break;
                                }
                            }
                            //第一道工序
                            if (listsize == 1 && jgmesGYLXGX.size() > 0) {
                                if (JGMES_PB_SCDETAIL == null) {
                                    JGMES_PB_SCDETAIL = new DynaBean("JGMES_PB_SCDETAIL", true);
                                    evalDynaBean(JGMES_PB_SCDETAIL, bgsjDynaBean, jgmesGYLXGX, "SCDETAIL_GZBTM", listsize);
                                    try {
                                        jgmesCommon.setDynaBeanInfo(JGMES_PB_SCDETAIL);
                                    } catch (Exception e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                    //SCDETAIL_DQTMLX  当前条码类型
                                    serviceTemplate.insert(JGMES_PB_SCDETAIL);
                                }
                            } else if (listsize == jgmesGYLXGX.size()) {//最后一道工序
                                if (JGMES_PB_SCDETAIL == null) {
                                    JGMES_PB_SCDETAIL = new DynaBean("JGMES_PB_SCDETAIL", true);
                                }
                                evalDynaBean(JGMES_PB_SCDETAIL, bgsjDynaBean, jgmesGYLXGX, "SCDETAIL_GZBTM", listsize);
                                serviceTemplate.update(JGMES_PB_SCDETAIL);
                            } else {//中间工序
                                if (JGMES_PB_SCDETAIL == null) {
                                    JGMES_PB_SCDETAIL = new DynaBean("JGMES_PB_SCDETAIL", true);
                                }
                                evalDynaBean(JGMES_PB_SCDETAIL, bgsjDynaBean, jgmesGYLXGX, "SCDETAIL_GZBTM", listsize);
                                serviceTemplate.update(JGMES_PB_SCDETAIL);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /*
     * 报工主子表保存方法－一起保存
     * */
    //****
    public JgmesResult<HashMap> doJsonSaveBgSjAll(String jsonStr, String jsonArrStr, String userCode, String mac, JgmesEnumsBgsjLx bgsjLxEnums, boolean BatchCode) throws ParseException {
        JgmesResult<HashMap> jgmesResult = new JgmesResult<HashMap>();
        JgmesResult<String> jgRes = new JgmesResult<String>();//其它方法返回结果
        if (serviceTemplate == null) {
            serviceTemplate = SpringContextHolder.getBean("PCDynaServiceTemplate");
        }
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);

		boolean res = true;
        String key = "";
        String value = "";
        String tabCode = "JGMES_PB_BGSJ";
        String tabCodeDetail = "JGMES_PB_BGSJZB";
        String userName = "";
        List<DynaBean> list = new ArrayList<DynaBean>();
        String keyCode = JEUUID.uuid();
        String cpBh = "";// 产品编号
        String cpMc = "";// 产品名称
        String cpGg = "";// 产品规格
        String barCode = "";
        String currentGxID = "";// 当前工序ID
        String scrwId = "";// 生产任务ID
        String scrw = "";// 生产任务
        boolean isInsert = false;
        boolean isBg = false;//是否已经报工
        int trsl = 0;// 投入数量
        int czsl = 0;// 出站数量
        String cxbm = "";// 产线编码
        String cxmc = "";// 产线名称
        String gdhm = "";// 工单号码
        String ddhm = "";// 订单号码
        String lckh = "";// 流程卡号
        int gdsl = 0;// 工单数量
        int ddsl = 0;// 工单数量
        int wcsl = 0;// 完成数量
        String cxId = "";// 产线ID

        try {
            // 获取当前登陆用户
            if (bgsjLxEnums.equals(JgmesEnumsBgsjLx.LySj)) {//蓝牙用户没有用户
                if (serviceTemplate == null) {
                    serviceTemplate = SpringContextHolder.getBean("PCDynaServiceTemplate");
                }
            } else {
                if (userCode != null && !userCode.isEmpty()) {
                    jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
                }
                userCode = jgmesCommon.jgmesUser.getCurrentUserCode();
                userName = jgmesCommon.jgmesUser.getCurrentUserName();
            }

            // 将前台数据转到对象中
            DynaBean bgsj = null;
            if (bgsjLxEnums.equals(JgmesEnumsBgsjLx.LySj)) {
                bgsj = jgmesCommon.getDynaBeanByJsonStrNotSys(tabCode, jsonStr);
            } else {
                bgsj = jgmesCommon.getDynaBeanByJsonStr(tabCode, jsonStr);
            }
            bgsj.setStr(beanUtils.KEY_TABLE_CODE, tabCode);
            bgsj.setStr(beanUtils.KEY_PK_CODE, keyCode);
            barCode = bgsj.getStr("BGSJ_TMH");
            cpBh = bgsj.getStr("BGSJ_CPBH");
            DynaBean scrwDynaBean = null;
            if (bgsjLxEnums.equals(JgmesEnumsBgsjLx.LySj)) {
                //蓝牙数据不需要重新获取
                scrw = bgsj.getStr("BGSJ_SCRW");//任务单号
                scrwId = bgsj.getStr("BGSJ_SCRWID");
                bgsj.setStr("SY_CREATETIME", bgsj.getStr("BGSJ_GZSJ"));
                userCode = "admin";
                jgmesResult = doCheckChangeCp(userCode, barCode, cpBh, currentGxID, bgsj.getStr("BGSJ_SCRWID"));
            } else {

                jgmesResult = doCheckChangeCp(userCode, barCode, cpBh, currentGxID, bgsj.getStr("BGSJ_SCRWID"));// 需判断条码与产品是否匹配，不匹配返回条码对应的产品信息
                if (jgmesResult.IsSuccess) {
                    JgmesResult<String> jgmesResult2 = jgmesCommon.doCheckBgWl(jsonArrStr);//校验绑定的关键物料
                    if (!jgmesResult2.IsSuccess) {
                        jgmesResult.setMessage(jgmesResult2.getMessage());
                    }
                }

                if (!jgmesResult.IsSuccess) {// 如果换产则返回对应的资料
                    //toWrite(jsonBuilder.toJson(jgmesResult));
                    return jgmesResult;
                } else {
                    // 生产任务
                    scrwId = bgsj.getStr("BGSJ_SCRWID");
                    if (scrwId == null || scrwId.isEmpty()) {
                        jgmesResult.setMessage("缺少任务单！");
                        //toWrite(jsonBuilder.toJson(jgmesResult));
                        return jgmesResult;
                    } else {
                        scrwDynaBean = serviceTemplate.selectOneByPk("JGMES_PLAN_SCRW", scrwId);
                        scrw = scrwDynaBean.getStr("SCRW_RWDH");
//								gdhm = scrwDynaBean.getStr("SCRW_GDHM");
//								ddhm = scrwDynaBean.getStr("SCRW_DDHM");
//								lckh = scrwDynaBean.getStr("SCRW_LCKH");
//								gdsl = scrwDynaBean.getInt("SCRW_GDSL");
//								ddsl = scrwDynaBean.getInt("SCRW_DDSL");
                        bgsj.setStr("BGSJ_SCRW", scrw);
                        bgsj.setStr("BGSJ_MACDZ", mac);// MAC地址

                        if (bgsjLxEnums.equals(JgmesEnumsBgsjLx.GwSj)) {// 工位数据保存
                            bgsj.set("BGSJ_SJLX", 0);// 数据类型
                        } else {
                            bgsj.set("BGSJ_SJLX", 1);
                        }

                        // 工位信息
                        DynaBean gw = getCurrentGW(userCode);
                        if (gw != null) {
                            bgsj.setStr("BGSJ_GWBH", gw.getStr("GW_GWBH"));
                            bgsj.setStr("BGSJ_GWMC", gw.getStr("GW_GWMC"));
                            bgsj.setStr("BGSJ_GWID", gw.getStr("JGMES_BASE_GW_ID"));
                        }

                        // 产线信息
                        DynaBean cxDynaBean = getCurrentCX(userCode);
                        if (cxDynaBean != null) {
                            cxId = cxDynaBean.getStr("JGMES_BASE_CXSJ_ID");
                            cxbm = cxDynaBean.getStr("CXSJ_CXBM");
                            cxmc = cxDynaBean.getStr("CXSJ_CXMC");
                            bgsj.setStr("BGSJ_CXBM", cxbm);
                            bgsj.setStr("BGSJ_CXMC", cxmc);
                        }
                        if (cpBh != null && !cpBh.isEmpty()) {
                            // 工序信息
                            DynaBean gx = getCurrentGX(cpBh, userCode);
                            if (gx != null) {
                                bgsj.setStr("BGSJ_GXID", gx.getStr("GXGL_GYLX_GXID"));
                                bgsj.setStr("BGSJ_GXBH", gx.getStr("GXGL_GXNUM"));
                                bgsj.setStr("BGSJ_GXMC", gx.getStr("GXGL_GXNAME"));
                                bgsj.set("BGSJ_GXSXH", gx.get("GXGL_GXSXH"));// 工序顺序号

                            }
                            // 产品信息
                            DynaBean cpDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA",
                                    " AND PRODUCTDATA_BH='" + cpBh + "'");
                            if (cpDynaBean != null) {
                                cpMc = cpDynaBean.getStr("PRODUCTDATA_NAME");
                                cpGg = cpDynaBean.getStr("PRODUCTDATA_GG");
                                bgsj.setStr("BGSJ_CPMC", cpMc);// 产品名称
                                bgsj.setStr("BGSJ_CPGG", cpGg);// 产品规格
                            }
                        }

                        // 过站时间
                        bgsj.set("BGSJ_GZSJ", jgmesCommon.getCurrentTime());
                    }
                }

                // 返修单ID，根据条码及当前维修状态获取返修站ID
                String tmString = bgsj.getStr("BGSJ_TMH");
                DynaBean fxDynaBean = new DynaBean();
                fxDynaBean.setStr(beanUtils.KEY_TABLE_CODE, "JGMES_PB_FXD");
                if (tmString != null && !tmString.isEmpty()) {
                    fxDynaBean = serviceTemplate.selectOne("JGMES_PB_FXD", " and FXD_CPTMH='" + tmString
                            + "' and FXD_WXZT_CODE='" + JgmesEnumsDic.WxZtToDo.getKey() + "'");
                    if (fxDynaBean != null) {
                        bgsj.set("BGSJ_FXDID", fxDynaBean.getPkValue());
                    }
                }

//					//返回工序ID更新
//					String fhgxCode = bgsj.getStr("BGSJ_FHGXBH");
//					if (fhgxCode != null && !fhgxCode.isEmpty()) {
//						DynaBean gxBean = serviceTemplate.selectOne(tmString, fhgxCode)
//					}
                jgmesCommon.setDynaBeanInfo(bgsj);
            }
            // 保存前进行工序防呆检测
            currentGxID = bgsj.getStr("BGSJ_GXID");
            // 保存前检验一下是否已经报工，如果已经报工就把之前的报工数据改成不启用
            List<DynaBean> ybgDynaBeanList = serviceTemplate.selectList("JGMES_PB_BGSJ", " and BGSJ_TMH = '" + barCode + "' and BGSJ_GXID = '" + currentGxID + "' and  (BGSJ_SCRW = '" + scrw + "' or BGSJ_SCRWID = '" + scrwId + "')");
            //如果能查出数据，就说明是重复报工需要做一些处理
            if (ybgDynaBeanList != null && ybgDynaBeanList.size() > 0) {
                //标记为已经报工，下面不会回写产品完工情况表的数据
                isBg = true;
                //设置状态（默认开启）
                DynaBean dic = jgmesCommon.getDic("JGMES_STATUS", "2");
                //把之前的报工数据的状态改成不启用
                for (DynaBean ybgDynaBean : ybgDynaBeanList) {
                    if (dic != null) {
                        ybgDynaBean.set("BGSJ_STATUS_NAME", dic.getStr("DICTIONARYITEM_ITEMNAME"));
                        ybgDynaBean.set("BGSJ_STATUS_CODE", dic.getStr("DICTIONARYITEM_ITEMCODE"));
                        ybgDynaBean.set("BGSJ_STATUS_ID", dic.getStr("JE_CORE_DICTIONARYITEM_ID"));
                        serviceTemplate.update(ybgDynaBean);
                    }
                }
            }

            jgRes = jgmesCommon.doCheckSj(barCode, currentGxID);
            if (!jgRes.IsSuccess) {
                jgmesResult.setMessage(jgRes.getMessage());
//						System.out.println(jgmesResult);
                if (bgsjLxEnums.equals(JgmesEnumsBgsjLx.LySj)) {
                    DynaBean dic = jgmesCommon.getDic("JGMES_STATUS", "2");
                    //把之前的报工数据的状态改成启用
                    List<DynaBean> aList = serviceTemplate.selectList("JGMES_PB_BGSJ", " and BGSJ_TMH = '" + barCode + "' and BGSJ_GXID = '" + currentGxID + "' and  (BGSJ_SCRW = '" + scrw + "' or BGSJ_SCRWID = '" + scrwId + "') order by BGSJ_GZSJ desc limit 1 ");
                    DynaBean adic = jgmesCommon.getDic("JGMES_STATUS", "1");
                    for (DynaBean bean : aList) {
                        bean.set("BGSJ_STATUS_NAME", adic.getStr("DICTIONARYITEM_ITEMNAME"));
                        bean.set("BGSJ_STATUS_CODE", adic.getStr("DICTIONARYITEM_ITEMCODE"));
                        bean.set("BGSJ_STATUS_ID", adic.getStr("JE_CORE_DICTIONARYITEM_ID"));
                        serviceTemplate.update(bean);
                    }
                    return jgmesResult;
                } else {
                    //toWrite(jsonBuilder.toJson(jgmesResult));
                    return jgmesResult;
                }
            } else {
                DynaBean bgsj_jg = serviceTemplate.insert(bgsj);
                //	        list.add(bgsj);

                // 将传入的参数转到表中
                // bgsj= (DynaBean) jsonBuilder.fromJson(jsonString, bgsj.getClass());
                if (jsonArrStr != null && !jsonArrStr.isEmpty()) {
                    net.sf.json.JSONArray ja1 = net.sf.json.JSONArray.fromObject(jsonArrStr);
                    if (ja1.size() > 0) {
                        for (int i = 0; i < ja1.size(); i++) {
                            DynaBean bgsjzb = new DynaBean("JGMES_PB_BGSJZB", true);
                            bgsjzb.setStr(beanUtils.KEY_TABLE_CODE, tabCodeDetail);
                            JSONObject jb1 = ja1.getJSONObject(i); // 遍历 jsonarray 数组，把每一个对象转成 json 对象
                            Iterator it1 = jb1.keys();
                            while (it1.hasNext()) {
                                key = (String) it1.next();
                                value = jb1.getString(key);
                                bgsjzb.setStr(key, value);
                            }
                            // bgsjzb.set("JGMES_PB_BGSJ_ID", bgsj.get("JGMES_PB_BGSJ_ID"));
                            bgsjzb.set("JGMES_PB_BGSJ_ID", bgsj_jg.get("JGMES_PB_BGSJ_ID"));
                            // bgsjzb.set("JGMES_PB_BGSJ_ID", keyCode);
                            if (!bgsjLxEnums.equals(JgmesEnumsBgsjLx.LySj)) {
                                jgmesCommon.setDynaBeanInfo(bgsjzb);
                            }
                            list.add(bgsjzb);
                        }
                    }
                }
                // SET FOREIGN_KEY_CHECKS=0;
                serviceTemplate.insert(list);
                //过滤掉重复扫码遇到的问题
                if (!isBg) {
                    //保存产品产线完成情况表
                    jgRes = doSaveCpCxInfo(bgsj_jg);
                }
                if (!jgRes.IsSuccess) {
                    jgmesResult.setMessage(jgRes.getMessage());
//							toWrite(jsonBuilder.toJson(jgmesResult));
                }
                if (bgsj_jg.getStr("BGSJ_GXSXH") != null && !bgsj_jg.getStr("BGSJ_GXSXH").isEmpty()) {
                    //	doSaveScDetail(bgsj_jg,list);
                }
                if (bgsjLxEnums.equals(JgmesEnumsBgsjLx.LySj)) {
                } else {
                    // 如果是末道工序，更新产品产线完成情况表中的出站数量、完成时间；更新生产任务完成数量、排产单完成数量、工单完成数量
                    if (jgmesCommon.IsLastGx(barCode, currentGxID, null) && scrwId != null && !scrwId.isEmpty() && !isBg) {
                        // 更新生产任务完成数量
                        if (scrwDynaBean != null) {
                            wcsl = scrwDynaBean.getInt("SCRW_WCSL");
                            scrwDynaBean.set("SCRW_WCSL", wcsl + 1);
                            if (scrwDynaBean.getInt("SCRW_WCSL")==scrwDynaBean.getInt("SCRW_PCSL")){
                                scrwDynaBean.set("SCRW_RWZT_CODE","RWZT03");
                                scrwDynaBean.set("SCRW_RWZT_NAME","完成生产");
                            }
                            serviceTemplate.update(scrwDynaBean);
                            // 更新工单完成数量
                            DynaBean gdlbDynaBean = serviceTemplate.selectOne("JGMES_PLAN_GDLB",
                                    " and JGMES_PLAN_GDLB_ID='" + scrwDynaBean.getStr("JGMES_PLAN_GDLB_ID") + "'");
                            if (gdlbDynaBean != null) {
                                wcsl = gdlbDynaBean.getInt("GDLB_WCSL");
                                gdlbDynaBean.set("GDLB_WCSL", wcsl + 1);
                                serviceTemplate.update(gdlbDynaBean);
                            }
                        }
                    }
                }


                if (bgsjLxEnums.equals(JgmesEnumsBgsjLx.GwSj) || bgsjLxEnums.equals(JgmesEnumsBgsjLx.LySj)) {// 工位数据保存，后成返修单
                    String jgString = bgsj_jg.getStr("BGSJ_PDJG_CODE");
//							List<DynaBean> okList = serviceTemplate.selectList("JGMES_PB_BGSJ"," and BGSJ_TMH = '"+barCode+"' and BGSJ_GXID = '"+currentGxID+"' and  (BGSJ_SCRW = '"+scrw+"' or BGSJ_SCRWID = '"+scrwId+"')",1,2);
                    DynaBean gylxgx = serviceTemplate.selectOneByPk("JGMES_GYGL_GYLXGX", currentGxID);
                    Integer ngNum = 0;
                    Integer ngCount = 0;
                    Integer againScanningNot = 0;
                    if (gylxgx != null) {
                        ngCount = gylxgx.getInt("GYLXGX_NGCS");
                        againScanningNot = gylxgx.getInt("GYLXGX_SFCFSM_CODE");
                        String okSql = " SELECT * FROM JGMES_PB_BGSJ WHERE 1=1  and BGSJ_TMH = '" + barCode + "' and BGSJ_GXID = '" + currentGxID + "' and  (BGSJ_SCRW = '" + scrw + "' or BGSJ_SCRWID = '" + scrwId + "') order by BGSJ_GZSJ desc limit " + ngCount;
                        List<DynaBean> okBean = serviceTemplate.selectListBySql(okSql);
                        if (againScanningNot == 1) {
                            for (DynaBean bean : okBean) {
                                if (bean.getStr("BGSJ_PDJG_CODE").equals("PDJG02"))
                                    ngNum++;
                            }
                        }
                    }
                    if (ngNum != ngCount && againScanningNot == 1) {
                        //数量不等不处理，ng数量相等即插入返修
                    } else {
                        if (jgString.equals(JgmesEnumsDic.PdJgUseless.getKey()))// 判定结果不合格的生成返修单
                        {
                            DynaBean fxdDynaBean = new DynaBean();
                            fxdDynaBean.setStr(beanUtils.KEY_TABLE_CODE, "JGMES_PB_FXD");
                            fxdDynaBean.setStr("FXD_CPBH", bgsj_jg.getStr("BGSJ_CPBH"));
                            fxdDynaBean.setStr("FXD_NAME", bgsj_jg.getStr("BGSJ_CPMC"));
                            fxdDynaBean.setStr("FXD_CPGG", bgsj_jg.getStr("BGSJ_CPGG"));
                            fxdDynaBean.setStr("FXD_SCRWDID", bgsj_jg.getStr("BGSJ_SCRWID"));
                            fxdDynaBean.setStr("FXD_LYGWID", bgsj_jg.getStr("BGSJ_GWID"));
                            fxdDynaBean.setStr("FXD_LYGWBH", bgsj_jg.getStr("BGSJ_GWBH"));
                            fxdDynaBean.setStr("FXD_LYGWMC", bgsj_jg.getStr("BGSJ_GWMC"));
                            fxdDynaBean.setStr("FXD_LYYHBM", userCode);
                            fxdDynaBean.setStr("FXD_LYYHMC", userName);
                            fxdDynaBean.setStr("FXD_CPTMH", bgsj_jg.getStr("BGSJ_TMH"));
                            fxdDynaBean.setStr("FXD_LYMACDZ", bgsj_jg.getStr("BGSJ_MACDZ"));
                            fxdDynaBean.setStr("FXD_RWDH", bgsj_jg.getStr("BGSJ_SCRW"));
                            jgmesCommon.setDynaBeanDic(fxdDynaBean, "JGMES_DIC_WXZT",
                                    JgmesEnumsDic.WxZtToDo.getKey(), "FXD_WXZT_ID", "FXD_WXZT_CODE",
                                    "FXD_WXZT_NAME");
                            if (bgsjLxEnums.equals(JgmesEnumsBgsjLx.LySj)) {

                            } else {
                                jgmesCommon.setDynaBeanInfo(fxdDynaBean);
                            }

                            DynaBean fxdDynaBean_jg = serviceTemplate.insert(fxdDynaBean);// 插入返修单主表
                            String zbJgStr = "";

                            if (list != null && !list.isEmpty()) {
                                List<DynaBean> fxList = new ArrayList<DynaBean>();
                                for (int i = 0; i < list.size(); i++) {
                                    DynaBean bgsjzbDynaBean = list.get(i);
                                    if (bgsjzbDynaBean != null) {
//								zbJgStr = bgsjzbDynaBean.getStr("BGSJZB_PDJG_CODE");
//								if (zbJgStr!=null && zbJgStr.equals(JgmesEnumsDic.PdJgUseless.getKey()))// 判定结果不合格的生成返修单子表
//								{
                                        DynaBean fxdzbDynaBean = new DynaBean();
                                        fxdzbDynaBean.setStr(beanUtils.KEY_TABLE_CODE, "JGMES_PB_FXDZB");
                                        fxdzbDynaBean.setStr("JGMES_PB_FXD_ID", fxdDynaBean.getPkValue());
                                        fxdzbDynaBean.setStr("FXDZB_BLXBH", bgsjzbDynaBean.getStr("BGSJZB_BLBH"));
                                        fxdzbDynaBean.setStr("FXDZB_BLXMC", bgsjzbDynaBean.getStr("BGSJZB_BLMC"));
                                        fxList.add(fxdzbDynaBean);
//								}
                                    }
                                }
                                if (fxList != null && fxList.size() > 0) {
                                    serviceTemplate.insert(fxList);
                                }
                            }

                        }
                    }

                }
                if (bgsjLxEnums.equals(JgmesEnumsBgsjLx.FxSj) && false) {// 返修数据保存，完善返修单数据
                    DynaBean fDynaBean = new DynaBean();
                    String fxdId = bgsj_jg.getStr("BGSJ_FXDID");
                    String wxzId = bgsj_jg.getStr("BGSJ_WXZID");// 维修站ID

                    if (fxdId != null && !fxdId.isEmpty()) {
                        fDynaBean = serviceTemplate.selectOneByPk("JGMES_PB_FXD", fxdId);
                        // 回写是哪个维修站处理的
                        if (wxzId != null && !wxzId.isEmpty()) {
                            DynaBean wxzDynaBean = serviceTemplate.selectOne("JGMES_PB_FXZ", " and JGMES_PB_FXZ_ID='" + wxzId + "'");
                            fDynaBean.setStr("JGMES_PB_FXZ_ID", wxzId);
                            if (wxzDynaBean != null) {
                                fDynaBean.setStr("FXD_FXZBH", wxzDynaBean.getStr("FXZ_FXZBH"));
                                fDynaBean.setStr("FXD_FXZMC", wxzDynaBean.getStr("FXZ_FXZMC"));
                            }
                        }
                        // 维修结果，通过报工维修结果字段回写
                        jgmesCommon.setDynaBeanDic(fDynaBean, "JGMES_MES_WXJG",
                                bgsj_jg.getStr("BGSJ_WXJG_CODE"), "FXD_WXJG_ID", "FXD_WXJG_CODE",
                                "FXD_WXJG_NAME");
                        // 维修状态，写死－－完成维修
                        jgmesCommon.setDynaBeanDic(fDynaBean, "JGMES_DIC_WXZT",
                                JgmesEnumsDic.WxZtFinish.getKey(), "FXD_WXZT_ID", "FXD_WXZT_CODE",
                                "FXD_WXZT_NAME");
                        serviceTemplate.update(fDynaBean);
                    }
                }
                //判断是否为关键物料批次码，如果是关键物料批次码就要回写条码表中的使用状态
                if (BatchCode) {
                    //获取条码
                    JgmesResult<JgmesBarCodeBase> barCodeJgmesResult = jgmesCommon.getTmLxByBarCode(barCode, null, null);
                    //判断一下是否为物料条码
                    if (barCodeJgmesResult.Data.IsMaterail) {
                        DynaBean wltmScDynaBean = serviceTemplate.selectOne("JGMES_BASE_WLTM", " and WLTM_TMH='" + barCode + "'");
                        if (wltmScDynaBean != null) {
                            wltmScDynaBean.set("WLTM_YSYSL", wltmScDynaBean.getInt("WLTM_YSYSL") + bgsj_jg.getInt("sl"));
                        }
                    }
                }

            }
            // 改成在保存之前校检 保存成功后校检是否更换产品，如已更换产品则返回新产品的相关信息
//			DynaBean currentPcd = getCurrentProductByCxCp(userCode);
//			if (currentPcd != null) {
//				if(!bgsj_jg.getStr("BGSJ_CPBH").equals(currentPcd.getStr("SCRW_CPBH"))) {//换产了
//					System.out.println("报工的产品："+bgsj_jg.getStr("BGSJ_CPBH")+"||产线现在做的产品:"+currentPcd.getStr("SCRW_CPBH"));
//					jgmesResult.Data = currentPcd.getValues();
//				}
//			}
        } catch (Exception e) {
            logger.error(JgmesCommon.getExceptionDetail2(e));
            // TODO: handle exception
            e.printStackTrace();
            res = false;
            jgmesResult.setMessage(e.toString());
        }

        return jgmesResult;
    }

    public void getOldCriticalComponent() {
        // MAC地址
        String mac = request.getParameter("mac");
        // 用户编码  必填
        String userCode = request.getParameter("userCode");
        //任务单号
        String rwNo = request.getParameter("rwNo");
        //产品条码号
        String cpCode = request.getParameter("cpCode");
        // 物料条码号
        String wlBarCode = request.getParameter("wlBarCode");

        JgmesResult<HashMap> ret = new JgmesResult<>();

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            if (wlBarCode == null || wlBarCode.isEmpty()) {
                ret.setMessage("未获取到物料条码号！");
            }
            if (cpCode == null || cpCode.isEmpty()) {
                ret.setMessage("未获取到产品编码！");
            }
            if (rwNo == null || rwNo.isEmpty()) {
                ret.setMessage("未获取到任务单号！");
            }
            if (ret.IsSuccess) {
                List<DynaBean> bgslzbDynaBeanList = serviceTemplate.selectList("JGMES_PB_BGSJZB", " and BGSJZB_TMH = '" + wlBarCode + "' and (BGSJZB_WLBH is not null or BGSJZB_WLBH !='')");
                if (bgslzbDynaBeanList != null && bgslzbDynaBeanList.size() > 0) {
                    if (bgslzbDynaBeanList.size() == 1) {
                        DynaBean bgslzbDynaBean = bgslzbDynaBeanList.get(0);
                        DynaBean bgslDynaBean = serviceTemplate.selectOne("JGMES_PB_BGSJ", " and JGMES_PB_BGSJ_ID = '" + bgslzbDynaBean.getStr("JGMES_PB_BGSJ_ID") + "' and BGSJ_SCRW = '" + rwNo + "'");
                        if (bgslDynaBean != null) {
                            if (cpCode.equals(bgslDynaBean.getStr("BGSJ_CPBH"))) {
                                DynaBean wlDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " and PRODUCTDATA_BH = '" + bgslzbDynaBean.getStr("BGSJZB_WLBH") + "'");
                                if (wlDynaBean != null) {
                                    bgslzbDynaBean.set("detail", wlDynaBean.getValues());
                                }
                                ret.Data = bgslzbDynaBean.getValues();
                            } else {
                                ret.setMessage("物料条码与产品条码不匹配！");
                            }
                        } else {
                            ret.setMessage("物料条码号已经在别的任务单中！");
                        }
                    } else {
                        /* 规避蓝牙数据蓝牙版影响，并且修改时同步把蓝牙数据的蓝牙版条码更新 */
                        if (bgslzbDynaBeanList.size()==2) {
                            DynaBean bgslzbDynaBean = new DynaBean();
                            /* 工序号4扫解码板二维码/BT板二维码与蓝牙测试=2条数据 */
                            /* 校验另一条是否是蓝牙数据,把不是蓝牙测试的数据提取出来 */
                            StringBuilder testData = new StringBuilder();
                            for (DynaBean bean : bgslzbDynaBeanList) {
                                String jcz = bean.getStr("BGSJZB_JCZ");
                                testData.append(jcz);
                                if (StringUtil.isEmpty(jcz)) {
                                    bgslzbDynaBean = bean;
                                }
                            }
                            /* 其中一个为蓝牙数据 */
                            if (StringUtil.isNotEmpty(testData.toString())) {
                                DynaBean bgslDynaBean = serviceTemplate.selectOne("JGMES_PB_BGSJ", " and JGMES_PB_BGSJ_ID = '" + bgslzbDynaBean.getStr("JGMES_PB_BGSJ_ID") + "' and BGSJ_SCRW = '" + rwNo + "'");
                                if (bgslDynaBean != null) {
                                    if (cpCode.equals(bgslDynaBean.getStr("BGSJ_CPBH"))) {
                                        DynaBean wlDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " and PRODUCTDATA_BH = '" + bgslzbDynaBean.getStr("BGSJZB_WLBH") + "'");
                                        if (wlDynaBean != null) {
                                            bgslzbDynaBean.set("detail", wlDynaBean.getValues());
                                        }
                                        ret.Data = bgslzbDynaBean.getValues();
                                    } else {
                                        ret.setMessage("物料条码与产品条码不匹配！");
                                    }
                                } else {
                                    ret.setMessage("物料条码号已经在别的任务单中！");
                                }
                            }
                        }else{
                            ret.setMessage("请确认扫描的是物料唯一码，有多处使用该条码！");
                        }
                    }
                }
            }
        }

        toWrite(jsonBuilder.toJson(ret));
    }


    /**
     * 保存物料更换信息
     */
    public void doSaveCriticalComponent() {
        // MAC地址
        String mac = request.getParameter("mac");
        // 用户编码  必填
        String userCode = request.getParameter("userCode");
        //产品编码
        String cpCode = request.getParameter("cpCode");
        //产品条码号
        String barCode = request.getParameter("barCode");
        // 新物料条码号
        String newWlBarCode = request.getParameter("newWlBarCode");
        // 旧物料条码号
        String oldWlBarCode = request.getParameter("oldWlBarCode");

        //报工数据子表数据
        String bgsjzbId = request.getParameter("bgsjzbId");

        JgmesResult<List<HashMap>> ret = new JgmesResult<>();

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            if (newWlBarCode == null || newWlBarCode.isEmpty()) {
                ret.setMessage("未获取到新物料条码号！");
            }
            if (oldWlBarCode == null || oldWlBarCode.isEmpty()) {
                ret.setMessage("未获取到旧物料条码号！");
            }
            if (cpCode == null || cpCode.isEmpty()) {
                ret.setMessage("未获取到产品编码！");
            }
            if (bgsjzbId == null || bgsjzbId.isEmpty()) {
                ret.setMessage("未获取到报工数据子表主键ID！");
            }
            if (ret.IsSuccess) {
                //获取报工数据子表
                DynaBean bgsjzbDynaBean = serviceTemplate.selectOne("JGMES_PB_BGSJZB", " and JGMES_PB_BGSJZB_ID = '" + bgsjzbId + "'");
                if (bgsjzbDynaBean != null) {
                    if (oldWlBarCode.equals(bgsjzbDynaBean.getStr("BGSJZB_WLBH"))) {
                        ret.setMessage("旧物料条码输入错误！");
                        toWrite(jsonBuilder.toJson(ret));
                        return;
                    }
                    //获取报工数据主表
                    DynaBean bgsjDynaBean = serviceTemplate.selectOne("JGMES_PB_BGSJ", " and JGMES_PB_BGSJ_ID = '" + bgsjzbDynaBean.getStr("JGMES_PB_BGSJ_ID") + "'");
                    if (bgsjDynaBean != null && cpCode.equals(bgsjDynaBean.getStr("BGSJ_CPBH"))) {
                        //记录物料更换记录表
                        DynaBean wlghDynaBean = new DynaBean();
                        wlghDynaBean.set("JGMES_SCGCGL_WLGHJLB_ID", JEUUID.uuid());
                        wlghDynaBean.set(beanUtils.KEY_TABLE_CODE, "JGMES_SCGCGL_WLGHJLB");
                        //产品
                        wlghDynaBean.set("WLGHJLB_CPMC", bgsjDynaBean.getStr("BGSJ_CPMC"));
                        wlghDynaBean.set("WLGHJLB_CPBM", bgsjDynaBean.getStr("BGSJ_CPBH"));
                        //物料
                        wlghDynaBean.set("WLGHJLB_WLBM", bgsjzbDynaBean.getStr("BGSJZB_WLBH"));
                        wlghDynaBean.set("WLGHJLB_WLMC", bgsjzbDynaBean.getStr("BGSJZB_WLMC"));
                        //工序
                        wlghDynaBean.set("WLGHJLB_GXBM", bgsjDynaBean.getStr("BGSJ_GXBH"));
                        wlghDynaBean.set("WLGHJLB_GXMC", bgsjDynaBean.getStr("BGSJ_GXMC"));

                        //产品条码号
                        wlghDynaBean.set("WLGHJLB_CPTMH", barCode);
                        //旧物料条码号
                        wlghDynaBean.set("WLGHJLB_JWLTMH", oldWlBarCode);
                        //新物料条码号
                        wlghDynaBean.set("WLGHJLB_XWLTMH", newWlBarCode);
                        //任务单号
                        wlghDynaBean.set("WLGHJLB_RWDH", bgsjDynaBean.getStr("BGSJ_SCRW"));
                        //获取订单号
                        DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and SCRW_RWDH = '" + bgsjDynaBean.getStr("BGSJ_SCRW") + "'");
                        //工单号码
                        wlghDynaBean.set("WLGHJLB_GDHM", bgsjDynaBean.getStr("BGSJ_GDHM"));
                        //订单号码
                        if (scrwDynaBean != null) {
                            wlghDynaBean.set("WLGHJLB_DDHM", scrwDynaBean.getStr("SCRW_DDHM"));
                            //内部订单号
                            wlghDynaBean.set("WLGHJLB_LCKH", scrwDynaBean.getStr("SCRW_LCKH"));
                            if (wlghDynaBean.getStr("WLGHJLB_GDHM") == null || "".equals(wlghDynaBean.getStr("WLGHJLB_GDHM"))) {
                                wlghDynaBean.set("WLGHJLB_GDHM", scrwDynaBean.getStr("SCRW_GDHM"));
                            }
                        }
                        //更换时间
                        wlghDynaBean.set("WLGHJLB_GHSJ", jgmesCommon.getCurrentTime());
                        //报工数据主键ID
                        wlghDynaBean.set("WLGHJLB_BGSJZJID", bgsjDynaBean.getStr("JGMES_PB_BGSJ_ID"));

                        //插入物料更换记录
                        serviceTemplate.insert(wlghDynaBean);
                        bgsjzbDynaBean.set("BGSJZB_TMH", newWlBarCode);
                        serviceTemplate.update(bgsjzbDynaBean);
                    }

                    //更換其他的物料（例如藍牙版的物料）
                    List<DynaBean> bgslzbDynaBeanList = serviceTemplate.selectList("JGMES_PB_BGSJZB", " and BGSJZB_TMH = '" + oldWlBarCode + "' and JGMES_PB_BGSJ_ID != '" + bgsjzbDynaBean.getStr("JGMES_PB_BGSJ_ID") + "'");
                    if (bgslzbDynaBeanList != null && bgslzbDynaBeanList.size() > 0) {
                        for (DynaBean bgslzb : bgslzbDynaBeanList) {
                            //获取报工数据主表
                            bgsjDynaBean = serviceTemplate.selectOne("JGMES_PB_BGSJ", " and JGMES_PB_BGSJ_ID = '" + bgslzb.getStr("JGMES_PB_BGSJ_ID") + "'");
                            if (bgsjDynaBean != null && cpCode.equals(bgsjDynaBean.getStr("BGSJ_CPBH"))) {
                                //记录物料更换记录表
                                DynaBean wlghDynaBean = new DynaBean();
                                wlghDynaBean.set("JGMES_SCGCGL_WLGHJLB_ID", JEUUID.uuid());
                                wlghDynaBean.set(beanUtils.KEY_TABLE_CODE, "JGMES_SCGCGL_WLGHJLB");
                                //产品
                                wlghDynaBean.set("WLGHJLB_CPMC", bgsjDynaBean.getStr("BGSJ_CPMC"));
                                wlghDynaBean.set("WLGHJLB_CPBM", bgsjDynaBean.getStr("BGSJ_CPBH"));
                                //物料
                                wlghDynaBean.set("WLGHJLB_WLBM", bgslzb.getStr("BGSJZB_WLBH"));
                                wlghDynaBean.set("WLGHJLB_WLMC", bgslzb.getStr("BGSJZB_WLMC"));
                                //工序
                                wlghDynaBean.set("WLGHJLB_GXBM", bgsjDynaBean.getStr("BGSJ_GXBH"));
                                wlghDynaBean.set("WLGHJLB_GXMC", bgsjDynaBean.getStr("BGSJ_GXMC"));

                                //产品条码号
                                wlghDynaBean.set("WLGHJLB_CPTMH", barCode);
                                //旧物料条码号
                                wlghDynaBean.set("WLGHJLB_JWLTMH", oldWlBarCode);
                                //新物料条码号
                                wlghDynaBean.set("WLGHJLB_XWLTMH", newWlBarCode);
                                //任务单号
                                wlghDynaBean.set("WLGHJLB_RWDH", bgsjDynaBean.getStr("BGSJ_SCRW"));
                                //获取订单号
                                DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and SCRW_RWDH = '" + bgsjDynaBean.getStr("BGSJ_SCRW") + "'");
                                //工单号码
                                wlghDynaBean.set("WLGHJLB_GDHM", bgsjDynaBean.getStr("BGSJ_GDHM"));
                                //订单号码
                                if (scrwDynaBean != null) {
                                    wlghDynaBean.set("WLGHJLB_DDHM", scrwDynaBean.getStr("SCRW_DDHM"));
                                    //内部订单号
                                    wlghDynaBean.set("WLGHJLB_LCKH", scrwDynaBean.getStr("SCRW_LCKH"));
                                    if (wlghDynaBean.getStr("WLGHJLB_GDHM") == null || "".equals(wlghDynaBean.getStr("WLGHJLB_GDHM"))) {
                                        wlghDynaBean.set("WLGHJLB_GDHM", scrwDynaBean.getStr("SCRW_GDHM"));
                                    }
                                }
                                //更换时间
                                wlghDynaBean.set("WLGHJLB_GHSJ", jgmesCommon.getCurrentTime());
                                //报工数据主键ID
                                wlghDynaBean.set("WLGHJLB_BGSJZJID", bgsjDynaBean.getStr("JGMES_PB_BGSJ_ID"));

                                //插入物料更换记录
                                serviceTemplate.insert(wlghDynaBean);
                                bgslzb.set("BGSJZB_TMH", newWlBarCode);
                                serviceTemplate.update(bgslzb);
                            }
                        }
                    }
                } else {
                    ret.setMessage("未获取到报工数据！");
                }
            }
        }
        toWrite(jsonBuilder.toJson(ret));
    }

    /*
     * 保存产品产线完成情况
     */
    private JgmesResult<String> doSaveCpCxInfo(DynaBean bgsjDynaBean) {
        JgmesResult<String> result = new JgmesResult<String>();
        boolean isInsert = false;
        if (bgsjDynaBean == null) {
            return result;
        }
        String userCode = bgsjDynaBean.getStr("SY_CREATEUSER");
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);

        String barCode = bgsjDynaBean.getStr("BGSJ_TMH");
        String currentGxID = bgsjDynaBean.getStr("BGSJ_GXID");
        String scrwId = bgsjDynaBean.getStr("BGSJ_SCRWID");
        String cpBh = bgsjDynaBean.getStr("BGSJ_CPBH");
        String cpMc = bgsjDynaBean.getStr("BGSJ_CPMC");
        String cpGg = bgsjDynaBean.getStr("BGSJ_CPGG");
        String cxbm = bgsjDynaBean.getStr("BGSJ_CXBM");
        String cxmc = bgsjDynaBean.getStr("BGSJ_CXMC");
        String cxId = "";// 产线ID
        String gdhm = "";// 工单号码
        String ddhm = "";// 订单号码
        String lckh = "";// 流程卡号
        String scrw = bgsjDynaBean.getStr("BGSJ_SCRW");// 任务单号
        int gdsl = 0;// 工单数量
        int ddsl = 0;// 订单数量
        int trsl = 0;
        int czsl = 0;
        DynaBean cpcxInfoDynaBean = null;
        String currentDate = jgmesCommon.getCurrentDate();

        try {
            if (scrwId != null && !scrwId.isEmpty()) {
                DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW",
                        " and JGMES_PLAN_SCRW_ID='" + scrwId + "'");
                scrw = scrwDynaBean.getStr("SCRW_RWDH");
                gdhm = scrwDynaBean.getStr("SCRW_GDHM");
                ddhm = scrwDynaBean.getStr("SCRW_DDHM");
                lckh = scrwDynaBean.getStr("SCRW_LCKH");
                gdsl = scrwDynaBean.getInt("SCRW_GDSL");
                ddsl = scrwDynaBean.getInt("SCRW_DDSL");
            }
            if (cxbm != null && !cxbm.isEmpty()) {
                DynaBean cxDynaBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ", " and CXSJ_CXBM='" + cxbm + "'");
                cxId = cxDynaBean.getStr("JGMES_BASE_CXSJ_ID");
            }

            // 如果是首道工序，更新产品产线完成情况表中的投入数量、开始时间，另写入相关基础信息
            if (scrwId != null && !scrwId.isEmpty() && jgmesCommon.IsFirstGx(barCode, currentGxID, null)) {
                isInsert = false;
                cpcxInfoDynaBean = serviceTemplate.selectOne("JGMES_PB_CPCXINFO",
                        " and CPCXINFO_SCRWID='" + scrwId + "' and CPCXINFO_RQ='" + currentDate + "'");
                if (cpcxInfoDynaBean == null) {
                    cpcxInfoDynaBean = new DynaBean();
                    cpcxInfoDynaBean.set(beanUtils.KEY_TABLE_CODE, "JGMES_PB_CPCXINFO");
                    isInsert = true;
                    cpcxInfoDynaBean.set("CPCXINFO_KSSJ", jgmesCommon.getCurrentTime());// 开始时间

                    cpcxInfoDynaBean.set("CPCXINFO_CPBH", cpBh);// 产品
                    cpcxInfoDynaBean.set("CPCXINFO_CPMC", cpMc);
                    cpcxInfoDynaBean.set("CPCXINFO_CPGG", cpGg);
                    cpcxInfoDynaBean.set("CPCXINFO_CXBM", cxbm);// 产线
                    cpcxInfoDynaBean.set("CPCXINFO_CXMC", cxmc);
                    cpcxInfoDynaBean.set("JGMES_BASE_CXSJ_ID", cxId);
                    cpcxInfoDynaBean.set("CPCXINFO_GDHM", gdhm);// 工单号码
                    cpcxInfoDynaBean.set("CPCXINFO_DDHM", ddhm);// 订单号码
                    cpcxInfoDynaBean.set("CPCXINFO_LCKH", lckh);// 流程卡号
                    cpcxInfoDynaBean.set("CPCXINFO_SCRWID", scrwId);// 任务单ID
                    cpcxInfoDynaBean.set("CPCXINFO_RWDH", scrw);// 任务单号
                    cpcxInfoDynaBean.set("CPCXINFO_GDSL", gdsl);// 工单数量
                    cpcxInfoDynaBean.set("CPCXINFO_GDSL", ddsl);// 订单数量
                    cpcxInfoDynaBean.set("CPCXINFO_RQ", currentDate);//日期

                    jgmesCommon.setDynaBeanInfo(cpcxInfoDynaBean);

                } else {
                    trsl = cpcxInfoDynaBean.getInt("CPCXINFO_TRSL");
                }
                cpcxInfoDynaBean.set("CPCXINFO_TRSL", trsl + 1);// 投入数量

                if (isInsert) {
                    serviceTemplate.insert(cpcxInfoDynaBean);
                } else {
                    serviceTemplate.update(cpcxInfoDynaBean);
                }
            }
            // 如果是末道工序，更新产品产线完成情况表中的出站数量、完成时间；更新生产任务完成数量、排产单完成数量、工单完成数量

            if (scrwId != null && !scrwId.isEmpty() && jgmesCommon.IsLastGx(barCode, currentGxID, null)) {
                isInsert = false;
                cpcxInfoDynaBean = serviceTemplate.selectOne("JGMES_PB_CPCXINFO",
                        " and CPCXINFO_SCRWID='" + scrwId + "' and CPCXINFO_RQ='" + currentDate + "'");
                if (cpcxInfoDynaBean == null) {// 正常情况应该不存在在末道工序尚未有数据的记录
                    cpcxInfoDynaBean = new DynaBean();
                    cpcxInfoDynaBean.set(beanUtils.KEY_TABLE_CODE, "JGMES_PB_CPCXINFO");
                    isInsert = true;

                    cpcxInfoDynaBean.set("CPCXINFO_CPBH", cpBh);// 产品
                    cpcxInfoDynaBean.set("CPCXINFO_CPMC", cpMc);
                    cpcxInfoDynaBean.set("CPCXINFO_CPGG", cpGg);
                    cpcxInfoDynaBean.set("CPCXINFO_CXBM", cxbm);// 产线
                    cpcxInfoDynaBean.set("CPCXINFO_CXMC", cxmc);
                    cpcxInfoDynaBean.set("JGMES_BASE_CXSJ_ID", cxId);
                    cpcxInfoDynaBean.set("CPCXINFO_GDHM", gdhm);// 工单号码
                    cpcxInfoDynaBean.set("CPCXINFO_DDHM", ddhm);// 订单号码
                    cpcxInfoDynaBean.set("CPCXINFO_LCKH", lckh);// 流程卡号
                    cpcxInfoDynaBean.set("CPCXINFO_SCRWID", scrwId);// 任务单ID
                    cpcxInfoDynaBean.set("CPCXINFO_RWDH", scrw);// 任务单号
                    cpcxInfoDynaBean.set("CPCXINFO_GDSL", gdsl);// 工单数量
                    cpcxInfoDynaBean.set("CPCXINFO_GDSL", ddsl);// 工单数量
                    cpcxInfoDynaBean.set("CPCXINFO_RQ", currentDate);//日期

                    jgmesCommon.setDynaBeanInfo(cpcxInfoDynaBean);

                } else {
                    czsl = cpcxInfoDynaBean.getInt("CPCXINFO_CZSL");
                }
                cpcxInfoDynaBean.set("CPCXINFO_WCSJ", jgmesCommon.getCurrentTime());// 完成时间
                cpcxInfoDynaBean.set("CPCXINFO_CZSL", czsl + 1);// 出站数量

                if (isInsert) {
                    serviceTemplate.insert(cpcxInfoDynaBean);
                } else {
                    serviceTemplate.update(cpcxInfoDynaBean);
                }
            }
        } catch (Exception e) {
            logger.error(JgmesCommon.getExceptionDetail2(e));
            // TODO: handle exception
            e.printStackTrace();
            result.setMessage("保存产品产线完成情况表出错！详细信息如下：" + e.toString());
        }
        return result;
    }


    /*
     * 报工表主从表保存方法--单独保存、测试用
     */
    private boolean doJsonSaveBgSjAllA(String jsonStr, String jsonArrStr, String userCode) throws ParseException {
        boolean res = false;
        res = doJsonSaveBgSj(jsonStr, userCode);

        if (res) {
            res = doJsonSaveBgSjZb(jsonArrStr, userCode);
        }

        return res;
    }

    /*
     * 子表保存
     * */
    public void doJsonSaveBGSJZB() throws ParseException {
        String jsonArrStr = request.getParameter("jsonStrDetail");
        String userCode = request.getParameter("userCode");
        ;
        System.out.println("jsonArrStr:" + jsonArrStr);

        if (doJsonSaveBgSjZb(jsonArrStr, userCode)) {
            toWrite(jsonBuilder.returnSuccessJson("\"添加成功\""));
        } else {
            toWrite(jsonBuilder.returnFailureJson("\"添加失败\""));
        }
    }

    /*
     * 主表保存
     * */
    public void doJsonSaveBGSJ() throws ParseException {
        String jsonString = request.getParameter("jsonStr");
        String userCode = request.getParameter("userCode");
        ;
        System.out.println("jsonString:" + jsonString);

        if (doJsonSaveBgSj(jsonString, userCode)) {
            toWrite(jsonBuilder.returnSuccessJson("\"添加成功\""));
        } else {
            toWrite(jsonBuilder.returnFailureJson("\"添加失败\""));
        }
    }

    /*
     * 通用单表保存方法,默认将工位信息、工序信息保存
     * */
    public void doJsonSaveBGSJAll() throws ParseException {
        String mac = request.getParameter("mac");// MAC地址
        String userCode = request.getParameter("userCode");// 用户编号
        String jsonString = request.getParameter("jsonStr");
        String jsonStrDetail = request.getParameter("jsonStrDetail");
        String jsonStr = "";

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();

        System.out.println("jsonString:" + jsonString);
        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                ret = doJsonSaveBgSjAll(jsonString, jsonStrDetail, userCode, mac, JgmesEnumsBgsjLx.GwSj, false);
            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                // TODO: handle exception
                ret.IsSuccess = false;
                ret.setMessage("出错!" + e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);

            toWrite(jsonStr);
        }
    }


    /*
     * 关键部件批次码过站
     * */
    public void doJsonSaveBGSJAllForBatchCode() throws ParseException {
        String mac = request.getParameter("mac");// MAC地址
        String userCode = request.getParameter("userCode");// 用户编号
        String jsonString = request.getParameter("jsonStr");
        String jsonStrDetail = request.getParameter("jsonStrDetail");
        String jsonStr = "";

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();

        System.out.println("jsonString:" + jsonString);
        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                ret = doJsonSaveBgSjAll(jsonString, jsonStrDetail, userCode, mac, JgmesEnumsBgsjLx.GwSj, true);
            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                // TODO: handle exception
                ret.IsSuccess = false;
                ret.setMessage("出错!" + e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);

            toWrite(jsonStr);
        }
    }

    /*
     * 维修结果保存
     * */
    public void doJsonSaveWxAll() throws ParseException {
        String mac = request.getParameter("mac");// MAC地址
        String userCode = request.getParameter("userCode");// 用户编号
        String jsonString = request.getParameter("jsonStr");
        String jsonStrDetail = request.getParameter("jsonStrDetail");
        String jsonStr = "";

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<String> ret = new JgmesResult<String>();

        System.out.println("jsonString:" + jsonString);
        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            try {

                ret = doJsonSaveWxAll(jsonString, jsonStrDetail, userCode, mac);
//				ret = doJsonSaveBgSjAll(jsonString, jsonStrDetail, userCode, mac, JgmesEnumsBgsjLx.FxSj);

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                // TODO: handle exception
                ret.IsSuccess = false;
                ret.setMessage("出错!" + e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);

            toWrite(jsonStr);
        }
    }


    private JgmesResult<String> doJsonSaveWxAll(String jsonStr, String jsonStrDetail, String userCode, String mac) {
        JgmesResult<String> jgmesResult = new JgmesResult<String>();
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);

        DynaBean fxdDynaBean_jg = new DynaBean();
        String tabCode = "JGMES_PB_FXD";
        String tabCodeDetail = "JGMES_PB_FXDETAIL";
        String pkValue = "";
        String cpCode = "";//产品编号
        String barCode = "";//条码
        String fhgygxId = "";//返回工艺工序ID
        String fhcxId = "";//返回产线ID
        String wxzId = "";//返修站ID
        String fxdId = "";//返修站ID

        List<DynaBean> detailList = new ArrayList<DynaBean>();

        String jsonStrBg = "";
        String jsonArrBg = "";
        //???

        try {
            DynaBean fxdDynaBean = new DynaBean();
            fxdDynaBean = jgmesCommon.updateDynaBeanByJsonStr(fxdDynaBean, jsonStr);
            if (fxdDynaBean == null) {
                jgmesResult.setMessage("传入数据为空！");
            }
            if (jgmesResult.IsSuccess) {
                barCode = fxdDynaBean.getStr("FXD_CPTMH");
                fxdId = fxdDynaBean.getStr("JGMES_PB_FXD_ID");
                if (barCode == null || barCode.isEmpty()) {
                    jgmesResult.setMessage("条码为空！");
                }
            }
            if (jgmesResult.IsSuccess) {
                if (fxdId != null && !fxdId.isEmpty()) {
                    fxdDynaBean = serviceTemplate.selectOne("JGMES_PB_FXD",
                            " and JGMES_PB_FXD_ID = '" + fxdId + "' and FXD_WXZT_CODE='" + JgmesEnumsDic.WxZtToDo.getKey() + "'");
                } else {
                    List<DynaBean> fxdDynaBeanList = serviceTemplate.selectList("JGMES_PB_FXD",
                            " and FXD_CPTMH='" + barCode + "' and FXD_WXZT_CODE='" + JgmesEnumsDic.WxZtToDo.getKey() + "'");
                    if (fxdDynaBeanList != null && fxdDynaBeanList.size() > 0) {
                        fxdDynaBean = fxdDynaBeanList.get(0);
                    }
                }
                if (fxdDynaBean == null) {
                    jgmesResult.setMessage("返修单不存在！");
                }
            }
            if (jgmesResult.IsSuccess) {
                pkValue = fxdDynaBean.getStr("JGMES_PB_FXD_ID");
                fxdDynaBean = jgmesCommon.updateDynaBeanByJsonStr(fxdDynaBean, jsonStr);// 用传入的数据做更新
                if (pkValue == null || pkValue.isEmpty()) {// 不存在
                    jgmesResult.setMessage("返修单ID不存在！");
                }
            }
            //返修站信息
            if (jgmesResult.IsSuccess) {
                wxzId = fxdDynaBean.getStr("JGMES_PB_FXZ_ID");
                if (wxzId == null || wxzId.isEmpty()) {
                    jgmesResult.setMessage("返修站ID未传入！");
                } else {
                    DynaBean wxzDynaBean = serviceTemplate.selectOneByPk("JGMES_PB_FXZ", wxzId);
                    if (wxzDynaBean != null) {
                        fxdDynaBean.setStr("FXD_FXZBH", wxzDynaBean.getStr("FXZ_FXZBH"));
                        fxdDynaBean.setStr("FXD_FXZMC", wxzDynaBean.getStr("FXZ_FXZMC"));
                    } else {
                        jgmesResult.setMessage("根据返修站ID[" + wxzId + "]获取返修站失败，请检查是否重复！");
                    }
                }
            }

            DynaBean cpDynaBean = null;
            if (jgmesResult.IsSuccess) {
                cpDynaBean = jgmesCommon.getCpByBarCode(barCode);
                if (cpDynaBean != null) {
                    cpCode = cpDynaBean.getStr("PRODUCTDATA_BH");
                    fxdDynaBean.setStr("FXD_CPBH", cpCode);
                    fxdDynaBean.setStr("FXD_NAME", cpDynaBean.getStr("PRODUCTDATA_NAME"));
                    fxdDynaBean.setStr("FXD_CPGG", cpDynaBean.getStr("PRODUCTDATA_GG"));
                } else {
                    System.out.println("根据条码[" + barCode + "]获取产品失败，检查该条码是否存在或是否有重复的！");
                    jgmesResult.setMessage("根据条码[" + barCode + "]获取产品失败，检查该条码是否存在或是否有重复的！");
                }
            }
            String fhgxCode = "";
            String gylxId = "";
            if (jgmesResult.IsSuccess) {
                //返回工序信息
                fhgxCode = fxdDynaBean.getStr("FXD_FHGXBH");
                gylxId = cpDynaBean.getStr("PRODUCTDATA_CPGYLXID");
            }
            if (fxdDynaBean != null && !"WXJG03".equals(fxdDynaBean.getStr("FXD_WXJG_CODE"))) {
                if (jgmesResult.IsSuccess) {
                    DynaBean gxDynaBean = serviceTemplate.selectOne("JGMES_GYGL_GYLXGX", " and GYLXGX_GXNUM='" + fhgxCode + "' and GYLX_ID='" + gylxId + "'");
                    if (gxDynaBean != null) {
                        fhgygxId = gxDynaBean.getStr("GYLXGX_ID");
                        fxdDynaBean.setStr("FXD_FHGXMC", gxDynaBean.getStr("GYLXGX_GXNAME"));
                        fxdDynaBean.setStr("FXD_FHGXID", fhgygxId);
                    } else {
                        jgmesResult.setMessage("根据返回工序编号[" + fhgxCode + "]获取工艺工序失败，请检查！");
                    }
                }
            }
            if (jgmesResult.IsSuccess) {
                //返回工位信息--要通过返回工序获取
                //DynaBean cpGwGxDenaBean = serviceTemplate.selectOne("JGMES_BASE_CPGWGX"," and CPGWGX_GYGXID='"+fhgygxId+"' and CPGWGX_CPBH='"+cpCode+"'");
                DynaBean cpGwGxDenaBean = serviceTemplate.selectOne("JGMES_BASE_CPGWGX",
                        " and CPGWGX_GYGXID='" + fhgygxId + "' and CPGWGX_CPBH='" + cpCode + "' and CPGWGX_CXBM = '" + fxdDynaBean.getStr("FXD_LYCXBH") + "'");
                if (cpGwGxDenaBean != null) {
                    String gwId = cpGwGxDenaBean.getStr("JGMES_BASE_GW_ID");
                    DynaBean gwDenaBean = serviceTemplate.selectOne("JGMES_BASE_GW", " and JGMES_BASE_GW_ID='" + gwId + "'");
                    if (gwDenaBean != null) {
                        fxdDynaBean.setStr("FXD_FHGWBH", gwDenaBean.getStr("GW_GWBH"));
                        fxdDynaBean.setStr("FXD_FHGWMC", gwDenaBean.getStr("GW_GWMC"));
                        fxdDynaBean.setStr("FXD_FHGWID", gwDenaBean.getStr("JGMES_BASE_GW_ID"));
                        fhcxId = gwDenaBean.getStr("JGMES_BASE_CXSJ_ID");
                    } else {
                        System.out.println("根据返回工序编号[" + fhgxCode + "]获取工位数据失败，请检查！");
                        jgmesResult.setMessage("根据返回工序编号[" + fhgxCode + "]获取工位数据失败，请检查！");
                    }
                }
            }
            if (fxdDynaBean != null && !"WXJG03".equals(fxdDynaBean.getStr("FXD_WXJG_CODE"))) {
                if (jgmesResult.IsSuccess) {
                    //返回产线信息
                    DynaBean cxDynaBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ", " and JGMES_BASE_CXSJ_ID='" + fhcxId + "'");
                    if (cxDynaBean != null) {
                        fxdDynaBean.setStr("FXD_FHCXBH", cxDynaBean.getStr("CXSJ_CXBM"));
                        fxdDynaBean.setStr("FXD_FHCXMC", cxDynaBean.getStr("CXSJ_CXMC"));
                        fxdDynaBean.setStr("FXD_FHCXID", cxDynaBean.getStr("JGMES_BASE_CXSJ_ID"));
                    } else {
                        jgmesResult.setMessage("根据返回产线ID[" + fhcxId + "]获取产线数据失败，请检查！");
                    }
                }
            }


            //主表更新到数据库中
            if (jgmesResult.IsSuccess) {
                // 维修结果，通过报工维修结果字段回写
                jgmesCommon.setDynaBeanDicByCode(fxdDynaBean, "JGMES_MES_WXJG", "FXD_WXJG_CODE", "FXD_WXJG_ID",
                        "FXD_WXJG_NAME");

                if (fxdDynaBean.getStr("FXD_WXJG_CODE").equals("WXJG01") || fxdDynaBean.getStr("FXD_WXJG_CODE").equals("WXJG03")) {
                    fxdDynaBean.set("FXD_WXZT_CODE", "WXZT04");
                }

                // 维修状态
                jgmesCommon.setDynaBeanDicByCode(fxdDynaBean, "JGMES_DIC_WXZT", "FXD_WXZT_CODE", "FXD_WXZT_ID",
                        "FXD_WXZT_NAME");

                jgmesCommon.setDynaBeanInfo(fxdDynaBean);
                fxdDynaBean_jg = serviceTemplate.update(fxdDynaBean);// 更新
            }

            //返修单明细表
            if (jgmesResult.IsSuccess) {
                String key = "";
                String value = "";
                if (jsonStrDetail != null && !jsonStrDetail.isEmpty()) {
                    net.sf.json.JSONArray ja1 = net.sf.json.JSONArray.fromObject(jsonStrDetail);
                    if (ja1.size() > 0) {
                        for (int i = 0; i < ja1.size(); i++) {
                            DynaBean fxdDetail = new DynaBean();
                            fxdDetail.setStr(beanUtils.KEY_TABLE_CODE, tabCodeDetail);

                            JSONObject jb1 = ja1.getJSONObject(i); // 遍历 jsonarray 数组，把每一个对象转成 json 对象
                            Iterator it1 = jb1.keys();
                            while (it1.hasNext()) {
                                key = (String) it1.next();
                                value = jb1.getString(key);
                                fxdDetail.setStr(key, value);
                            }
                            // bgsjzb.set("JGMES_PB_BGSJ_ID", bgsj.get("JGMES_PB_BGSJ_ID"));
                            fxdDetail.set("JGMES_PB_FXD_ID", fxdDynaBean_jg.get("JGMES_PB_FXD_ID"));
                            // bgsjzb.set("JGMES_PB_BGSJ_ID", keyCode);
                            jgmesCommon.setDynaBeanInfo(fxdDetail);
                            detailList.add(fxdDetail);
                        }
                        serviceTemplate.insert(detailList);
                    }
                }
            }
            //报工主表
            DynaBean bgsjDynaBean = new DynaBean();
            if (jgmesResult.IsSuccess) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("BGSJ_TMH", barCode);
                jsonObject.put("BGSJ_WXZID", wxzId);
                jsonObject.put("BGSJ_WXJG_CODE", fxdDynaBean_jg.getStr("FXD_WXJG_CODE"));
                jsonObject.put("BGSJ_WXJG_ID", fxdDynaBean_jg.getStr("FXD_WXJG_ID"));
                jsonObject.put("BGSJ_WXJG_NAME", fxdDynaBean_jg.getStr("FXD_WXJG_NAME"));

                jsonObject.put("BGSJ_CPBH", fxdDynaBean_jg.getStr("FXD_CPBH"));
                jsonObject.put("BGSJ_CPMC", fxdDynaBean_jg.getStr("FXD_CPBH"));
                jsonObject.put("BGSJ_FHGXID", fxdDynaBean_jg.getStr("FXD_FHGXID"));

                jsonObject.put("BGSJ_SCRWID", fxdDynaBean_jg.getStr("FXD_SCRWDID"));
                jsonStrBg = jsonObject.toString();

            }
            //报工子表
            if (jgmesResult.IsSuccess) {
                //暂不处理
            }

            //存储报工数据
            if (jgmesResult.IsSuccess) {
                JgmesResult<HashMap> jgRes = doJsonSaveBgSjAll(jsonStrBg, jsonArrBg, userCode, mac, JgmesEnumsBgsjLx.FxSj, false);
                if (!jgRes.IsSuccess) {
                    jgmesResult.setMessage(jgRes.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error(JgmesCommon.getExceptionDetail2(e));
            // TODO: handle exception
            jgmesResult.setMessage("方法[doJsonSaveWxAll]调用失败！详细原因如下：" + e.toString());
        }

        return jgmesResult;
    }

    /*
     * 通用单表保存方法,默认将工位信息、工序信息保存，主从独立保存，测试使用
     * */
    public void doJsonSaveBGSJAllA() throws ParseException {
        String jsonString = request.getParameter("jsonStr");
        String jsonStrDetail = request.getParameter("jsonStrDetail");
        String userCode = request.getParameter("userCode");//用户编号
        System.out.println("jsonString:" + jsonString);

        if (doJsonSaveBgSjAllA(jsonString, jsonStrDetail, userCode)) {
            toWrite(jsonBuilder.returnSuccessJson("\"添加成功\""));
        } else {
            toWrite(jsonBuilder.returnFailureJson("\"添加失败\""));
        }
    }

    /*
     * 设置产线的当前生产任务（排产单）
     */
    public void doSaveCurrentCxScrw() {
        String mac = getParamStr("mac");// MAC地址
        String userCode = getParamStr("userCode");// 产线编码
        String cxCode = request.getParameter("cxCode");
        String cxId = request.getParameter("cxId");
        String scrwId = request.getParameter("scrwId");
        String jsonStr = "";

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<String> ret = new JgmesResult<String>();

        if ((cxCode == null || cxCode.isEmpty()) && (cxId == null || cxId.isEmpty())) {
            ret.IsSuccess = false;
            ret.setMessage("产线编码、产线ID不能都为空！");
        }
        if (scrwId == null || scrwId.isEmpty()) {
            ret.IsSuccess = false;
            ret.setMessage("生产任务ID不能为空！");
        }
        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {

            try {

                DynaBean cxDynaBean = new DynaBean();
                if (cxId != null && !cxId.isEmpty()) {
                    cxDynaBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ", " and JGMES_BASE_CXSJ_ID='" + cxId + "'");
                }
                if ((cxId == null || cxId.isEmpty()) && cxCode != null && !cxCode.isEmpty()) {
                    cxDynaBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ", " and CXSJ_CXBM='" + cxCode + "'");
                }
                if (cxDynaBean != null) {
                    cxCode = cxDynaBean.getStr("CXSJ_CXBM");
					/*
					cxDynaBean.setStr("JGMES_PLAN_SCRW_ID", scrwId);
					serviceTemplate.update(cxDynaBean);
					*/

					/*
					//检查有没有进行首件检验
					DynaBean scrwDynaBean1 = serviceTemplate.selectOneByPk("JGMES_PLAN_SCRW", scrwId);
					if(scrwDynaBean1!=null){
						String message=jgmesCommon.checkSJ(cxCode,scrwDynaBean1.getStr("SCRW_RWDH"));
						if(message!=null&&!"".equals(message)){
							ret.setMessage(message);
							jsonStr = jsonBuilder.toJson(ret);
							toWrite(jsonStr);
						}
					}*/

                    //将该产线上其它的生产任务单暂停掉
                    List<DynaBean> list = serviceTemplate.selectList("JGMES_PLAN_SCRW", " and SCRW_CXBM='" + cxCode + "' and JGMES_PLAN_SCRW_ID<>'" + scrwId + "' and SCRW_RWZT_CODE='" + JgmesEnumsDic.ScDoing.getKey() + "'");
                    if (list != null && list.size() > 0) {
                        for (int i = 0; i < list.size(); i++) {
                            DynaBean scrwDynaBean = list.get(0);
                            doSaveScrwZt(userCode, scrwDynaBean.getStr("JGMES_PLAN_SCRW_ID"), JgmesEnumsDic.ScPause.getKey(), "");
                        }
                    }

                    // 存储排产状态
                    doSaveScrwZt(userCode, scrwId, JgmesEnumsDic.ScDoing.getKey(), "");
                    ret.IsSuccess = true;
                } else {
                    ret.IsSuccess = false;
                    ret.setMessage("获取产线数据错误！");
                }

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                // TODO: handle exception
                ret.IsSuccess = false;
                ret.setMessage("错误！" + e.toString());
            }

            jsonStr = jsonBuilder.toJson(ret);
            toWrite(jsonStr);
        }
    }

    /*
     * 当前任务状态更改
     */
    public void doSaveScrwZt() {
        String mac = getParamStr("mac");// MAC地址
        String userCode = getParamStr("userCode");// 产线编码
        String scrwId = request.getParameter("scrwId");
        String scrwZt = request.getParameter("scrwZt");
        String ztyyZ = request.getParameter("ztyyZ");
        String jsonStr = "";

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<String> ret = new JgmesResult<String>();
        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                // 存储排产状态
                doSaveScrwZt(userCode, scrwId, scrwZt, ztyyZ);

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                // TODO Auto-generated catch block
//			e.printStackTrace();
                ret.setMessage(e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);
            toWrite(jsonStr);
        }

    }

    /*
     * 保存排产单状态
     */
    public void doSaveScrwZt(String userCode, String scrwId, String scrwZt, String ztyyZ) {
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<String> ret = new JgmesResult<String>();
        try {
            System.out.println("生产任务单ID：" + scrwId);
            DynaBean scrwDynaBean = serviceTemplate.selectOneByPk("JGMES_PLAN_SCRW", scrwId);
            if (scrwDynaBean != null) {
                jgmesCommon.setDynaBeanDicScrwZt(scrwDynaBean, scrwZt, "SCRW_RWZT_ID", "SCRW_RWZT_CODE",
                        "SCRW_RWZT_NAME");
                if (JgmesEnumsDic.ScDoing.getKey().equals(scrwZt)) {
                    DynaBean gdDynaBean = serviceTemplate.selectOne("JGMES_PLAN_GDLB", " and JGMES_PLAN_GDLB_ID = '" + scrwDynaBean.getStr("JGMES_PLAN_GDLB_ID") + "'");///
                    if (gdDynaBean != null) {
                        if (gdDynaBean.getStr("GDLB_SJKGSJ") == null || "".equals(gdDynaBean.getStr("GDLB_SJKGSJ"))) {
                            gdDynaBean.set("GDLB_SJKGSJ", jgmesCommon.getCurrentDate());
                            serviceTemplate.update(gdDynaBean);
                        }
                    }
                    scrwDynaBean.set("SCRW_SJKGSJ", jgmesCommon.getCurrentTime());
                }
                if (JgmesEnumsDic.ScFinished.getKey().equals(scrwZt)) {
                    scrwDynaBean.set("SCRW_SJWGSJ", jgmesCommon.getCurrentTime());
                }
                serviceTemplate.update(scrwDynaBean);

                if (scrwZt.equalsIgnoreCase(JgmesEnumsDic.ScFinished.getKey()) || scrwZt.equalsIgnoreCase(JgmesEnumsDic.ScPause.getKey())) {//完成时删除产线指定的任务
                    List<DynaBean> cxList = serviceTemplate.selectList("JGMES_BASE_CXSJ", " and JGMES_PLAN_SCRW_ID='" + scrwId + "'");
                    DynaBean cxDynaBean = new DynaBean();
                    if (cxList != null && cxList.size() > 0) {
                        //
                        cxDynaBean = cxList.get(0);
                        cxDynaBean.set("JGMES_PLAN_SCRW_ID", "");
                        serviceTemplate.update(cxDynaBean);
//						for (int i = 0; i < cxList.size(); i++) {
//							cxDynaBean = cxList.get(0);
//							cxDynaBean.set("JGMES_PLAN_SCRW_ID", "");
//						}
////						pcServiceTemplate.//??
//						pcServiceTemplate.listUpdate(updateSqls);
//						serviceTemplate.listUpdate(sqls)
                    }
                } else {

                }
                //工单状态
//				DynaBean gdlbDynaBean = serviceTemplate.selectOneByPk("JGMES_PLAN_GDLB", scrwDynaBean.getStr("JGMES_PLAN_GDLB_ID"));
//				if (gdlbDynaBean != null) {
//					jgmesCommon.setDynaBeanDicScrwZt(gdlbDynaBean, pcdZt, "GDLB_PCZT_ID", "GDLB_PCZT_CODE",
//						"GDLB_PCZT_NAME");
//					serviceTemplate.update(gdlbDynaBean);
//				}

                //排产日志
//				DynaBean pcrzDynaBean= getPcRz(userCode,scrwDynaBean, scrwZt,ztyyZ);
//				pcrzDynaBean.setStr(beanUtils.KEY_TABLE_CODE, "JGMES_PLAN_PCRZ");
//
//				serviceTemplate.insert(pcrzDynaBean);
            }
        } catch (Exception e) {
            logger.error(JgmesCommon.getExceptionDetail2(e));
            // TODO Auto-generated catch block
//			e.printStackTrace();
            ret.setMessage(e.toString());
            toWrite("保存状态出错：" + jsonBuilder.toJson(ret));
        }
    }

    /*
     * 获取基础信息部门、产线、工位、用户等
     */
    public JSONObject getBaseInfo(String userCode, HttpServletRequest requestA, PCDynaServiceTemplate serviceTemplateA) {
        request = requestA;
        serviceTemplate = serviceTemplateA;
        JSONObject jsonObject = new JSONObject();

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        jsonObject.put("userCode", userCode);
        jsonObject.put("userName", jgmesCommon.jgmesUser.getCurrentUserName());

        jsonObject.put("deptId", jgmesCommon.jgmesUser.getCurrentDeptID());
        jsonObject.put("deptCode", jgmesCommon.jgmesUser.getCurrentDeptCode());
        jsonObject.put("deptName", jgmesCommon.jgmesUser.getCurrentDeptName());


        DynaBean gwDynaBean = getCurrentGW(userCode);
        if (gwDynaBean != null) {
            jsonObject.put("gwId", gwDynaBean.getStr("JGMES_BASE_GW_ID"));//.getPkValue());
            jsonObject.put("gwCode", gwDynaBean.getStr("GW_GWBH"));
            jsonObject.put("gwName", gwDynaBean.getStr("GW_GWMC"));
        }

        DynaBean cxDynaBean = getCurrentCX(userCode);
        if (cxDynaBean != null) {
            jsonObject.put("cxId", cxDynaBean.getStr("JGMES_BASE_CXSJ_ID"));//.getPkValue());
            jsonObject.put("cxCode", cxDynaBean.getStr("CXSJ_CXBM"));
            jsonObject.put("cxName", cxDynaBean.getStr("CXSJ_CXMC"));
        }

        DynaBean userDynabean = serviceTemplate.selectOne("je_core_enduser", " and USERCODE='" + userCode + "'");
        if (userDynabean != null) {
            jsonObject.put("photo", jgmesCommon.getPath(userDynabean.getStr("PHOTO")));
        }

        return jsonObject;
    }

    /*
     * 排产日志保存－－暂不用
     */
    private DynaBean getPcRz(String userCode, DynaBean pcDynaBean, String pcztValue, String reansonStr) throws ParseException {
        boolean jgmesResult = true;
        DynaBean pcrzDynaBean = new DynaBean();
        pcrzDynaBean.setStr(beanUtils.KEY_TABLE_CODE, "JGMES_PLAN_PCRZ");
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        if (pcDynaBean != null) {//???
//			jgmesCommon.setSameList(pcrzDynaBean, pcDynaBean,
//					new String[] { "PCDBH", "PCDMC", "CPBH", "NAME", "CPGG", "DDHM", "GDHM", "DEPTNAME", "RWDH", "LCKH",
//							"DEPTCODE", "PCRQ", "PGSL", "CXBM", "CXMC", "GDSL", "DDSL" });
            pcrzDynaBean.set("PCRZ_RQ", jgmesCommon.getCurrentTime());

            // 设置排产状态
            if (pcztValue != null && !pcztValue.isEmpty()) {

                jgmesCommon.setDynaBeanDicPcZt(pcrzDynaBean, pcztValue, "PCRZ_PCZT_ID", "PCRZ_PCZT_CODE",
                        "PCRZ_PCZT_NAME");
            }
            // 设置暂停原因
            if (reansonStr != null && !reansonStr.isEmpty()) {
                jgmesCommon.setDynaBeanDic(pcrzDynaBean, "JGMES_DIC_PCZTYY", reansonStr, "PCRZ_PCZTYY_ID",
                        "PCRZ_PCZTYY_CODE", "PCRZ_PCZTYY_NAME");
            }

            jgmesCommon.setDynaBeanInfo(pcrzDynaBean);
        } else {
            jgmesResult = false;
        }

        System.out.println("排产日志数据：" + jsonBuilder.toJson(pcrzDynaBean));
        return pcrzDynaBean;
    }


    /*
     * 根据产线获取排产列表
     * */
    public void getScrw() {
        String mac = getParamStr("mac");// MAC地址
        String userCode = getParamStr("userCode");// 用户编码
        String cxCode = getParamStr("cxCode");// 产线编码
        String cxId = getParamStr("cxId");// 产线ID
        String curDate = getParamStr("curDate");// 日期
        String curDateSection = getParamStr("curDateSection");// 日期区间
        String zt = getParamStr("zt");// 生产任务状态
        String noLike = getParamStr("noLike");// 单号关键字模糊(包含工单号、任务单号)
        String cpLike = getParamStr("cpLike");// 产品关键字模糊查询(产品编码、产品名称和产品规格)
        String pageSize = getParamStr("PageSize");// 每页码
        String currPage = getParamStr("CurrPage");// 当前页
        String jsonStr = "";

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();

        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                JgmesResult<List<DynaBean>> rets = getScrw(userCode, cxId, cxCode, curDate, zt, noLike, cpLike, pageSize, currPage, curDateSection);
                List<DynaBean> dicList = rets.Data;

                ret.Data = ret.getValues(dicList);
                ret.TotalCount = rets.TotalCount;

                // jsonStr = jsonBuilder.buildListPageJson(new Long(dicList.size()), dicList,
                // true);
                System.out.println("产线[" + cxCode + "]任务列表：");
                System.out.println(jsonStr);

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                // TODO: handle exception
                ret.IsSuccess = false;
                ret.setMessage("出错！详细信息：" + e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);
            toWrite(jsonStr);
        }
    }


    /*
     * 根据产线获取排产列表，并且过滤掉没有检测项的的任务
     * */
    public void getScrwByBLX() {
        String mac = getParamStr("mac");// MAC地址
        String userCode = getParamStr("userCode");// 用户编码
        String cxCode = getParamStr("cxCode");// 产线编码
        String cxId = getParamStr("cxId");// 产线ID
        String curDate = getParamStr("curDate");// 日期
        String curDateSection = getParamStr("curDateSection");// 日期区间
        String zt = getParamStr("zt");// 生产任务状态
        String noLike = getParamStr("noLike");// 单号关键字模糊(包含工单号、任务单号)
        String cpLike = getParamStr("cpLike");// 产品关键字模糊查询(产品编码、产品名称和产品规格)
        String pageSize = getParamStr("PageSize");// 每页码
        String currPage = getParamStr("CurrPage");// 当前页
        String jsonStr = "";

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();

        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                JgmesResult<List<DynaBean>> rets = getScrw(userCode, cxId, cxCode, curDate, zt, noLike, cpLike, pageSize, currPage, curDateSection);
                List<DynaBean> dicListforRe = new ArrayList<DynaBean>();
                List<DynaBean> dicList = rets.Data;
                for (DynaBean scrwDynaBean : dicList) {
                    ret = getGXListByCpBh(scrwDynaBean.getStr("SCRW_CPBH"));
                    if (ret != null && ret.Data != null && ret.Data.size() > 0) {
                        dicListforRe.add(scrwDynaBean);
                    }
                }
                if (dicListforRe != null && dicListforRe.size() > 0) {
                    ret.Data = ret.getValues(dicListforRe);
                    ret.TotalCount = (long) dicListforRe.size();
                }

                // jsonStr = jsonBuilder.buildListPageJson(new Long(dicList.size()), dicList,
                // true);
                System.out.println("产线[" + cxCode + "]任务列表：");
                System.out.println(jsonStr);

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                // TODO: handle exception
                ret.IsSuccess = false;
                ret.setMessage("出错！详细信息：" + e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);
            toWrite(jsonStr);
        }
    }


    /*
     * 获取生产任务，并且过滤掉工序的生产任务
     * */
    public void getScrwByGX() {
        String mac = getParamStr("mac");// MAC地址
        String userCode = getParamStr("userCode");// 用户编码
        String cxCode = getParamStr("cxCode");// 产线编码
        String cxId = getParamStr("cxId");// 产线ID
        String curDate = getParamStr("curDate");// 日期
        String curDateSection = getParamStr("curDateSection");// 日期区间
        String zt = getParamStr("zt");// 生产任务状态
        String noLike = getParamStr("noLike");// 单号关键字模糊(包含工单号、任务单号)
        String cpLike = getParamStr("cpLike");// 产品关键字模糊查询(产品编码、产品名称和产品规格)
        String pageSize = getParamStr("PageSize");// 每页码
        String currPage = getParamStr("CurrPage");// 当前页
        String jsonStr = "";

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();

        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                JgmesResult<List<DynaBean>> rets = getScrw(userCode, cxId, cxCode, curDate, zt, noLike, cpLike, pageSize, currPage, curDateSection);
                List<DynaBean> dicListforRe = new ArrayList<DynaBean>();

                List<DynaBean> dicList = rets.Data;
                for (DynaBean scrwDynaBean : dicList) {
                    List<DynaBean> gxList = getGXList(scrwDynaBean.getStr("SCRW_CPBH"));
                    if (gxList != null && gxList.size() > 0) {
                        dicListforRe.add(scrwDynaBean);
                    }
                }
                if (dicListforRe != null && dicListforRe.size() > 0) {
                    ret.Data = ret.getValues(dicListforRe);
                    ret.TotalCount = (long) dicListforRe.size();
                }

                // jsonStr = jsonBuilder.buildListPageJson(new Long(dicList.size()), dicList,
                // true);
                System.out.println("产线[" + cxCode + "]任务列表：");
                System.out.println(jsonStr);

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                // TODO: handle exception
                ret.IsSuccess = false;
                ret.setMessage("出错！详细信息：" + e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);
            toWrite(jsonStr);
        }
    }

    /**
     * 根据任务单号获取生产任务
     * rwNo 任务单号
     * mac mac地址
     * userCode 用户编码
     *
     * @see /jgmes/commonAction!getScrwByRwNo.action
     */
    public void getScrwByRwNo() {
        // MAC地址
        String mac = request.getParameter("mac");
        // 用户编码  必填
        String userCode = request.getParameter("userCode");
        // 任务单号
        String rwNo = request.getParameter("rwNo");

        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
        String jsonStr = "";
        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            if (rwNo != null && !"".equals(rwNo)) {
                DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and SCRW_RWDH = '" + rwNo + "'");
                if (scrwDynaBean != null) {
                    ret.Data = scrwDynaBean.getValues();
                }
            } else {
                ret.setMessage("未获取到生产任务单号！");
            }
        }
        jsonStr = jsonBuilder.toJson(ret);
        toWrite(jsonStr);
    }


    /*
     * 获取产线排产列表，包含暂停的
     */
    public JgmesResult<List<DynaBean>> getScrw(String userCode, String cxId, String cxCode, String curDate, String zt, String noLike, String cpLike, String pageSize, String currPage, String curDateSection) {
        List<DynaBean> dicList = new ArrayList<DynaBean>();
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);

        JgmesResult<List<DynaBean>> ret = new JgmesResult<List<DynaBean>>();

        String sql = "";
        String[] ztStrs = null;

        if (cxId != null && cxId.length() > 0) {
            DynaBean cxDynaBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ", " and JGMES_BASE_CXSJ_ID='" + cxId + "'");
            if (cxDynaBean != null) {
                cxCode = cxDynaBean.getStr("CXSJ_CXBM");
            }
        }
        if (cxCode == null || cxCode.isEmpty()) {
            DynaBean cxDynaBean = getCurrentCX(userCode);
            if (cxDynaBean != null) {
                cxCode = cxDynaBean.getStr("CXSJ_CXBM");
            }
        }
        if (cxCode != null && !"".equals(cxCode)) {
            sql += jgmesCommon.getSqlWhere("SCRW_CXBM", cxCode);
        }
        //sql += jgmesCommon.getSqlWhere("SCRW_PCRQ", curDate);
        if (curDate != null && !curDate.isEmpty()) {
            sql += " and SCRW_PCRQ like '%" + curDate + "%'";
        }

        if (curDateSection != null && !curDateSection.isEmpty()) {
            String[] dateList = curDateSection.split(" - ");
            if (dateList != null && dateList.length > 0) {
                sql += " and str_to_date(SCRW_PCRQ,'%Y-%c-%d')>=str_to_date('" + dateList[0].trim() + "','%Y-%c-%d') ";
                sql += " and str_to_date(SCRW_PCRQ,'%Y-%c-%d')<=str_to_date('" + dateList[1].trim() + "','%Y-%c-%d') ";
            }
        }

//		if (zt != null && !zt.isEmpty()) {
//			if (zt.indexOf(",")!=-1) {
//				//sql += jgmesCommon.getSqlWhere("SCRW_PCZT_CODE", "'"+zt.replaceAll(",", "','")+"'");
//				sql += " and SCRW_RWZT_CODE in ('"+zt.replaceAll(",", "','")+"')";
//			}
//			else {
//				//sql += jgmesCommon.getSqlWhere("SCRW_PCZT_CODE", zt);
//				sql += jgmesCommon.getSqlWhere("SCRW_RWZT_CODE", zt);
//			}
//		}
        //获取系统参数——物料不良报工完成时间
        DynaBean xtcsBean = serviceTemplate.selectOne("JGMES_XTGL_XTCS", " and XTCS_CXFL2_CODE='WLBLBGTIME'");
        String time = "";
        if (xtcsBean != null && xtcsBean.getStr("XTCS_NO_CODE").equals("1")) {//不等于空且启用状态
            time = xtcsBean.getStr("XTCS_CSZ");//获取参数值
        }
        if (zt != null && !zt.isEmpty()) {
            if (zt.indexOf(",") != -1) {
                //sql += jgmesCommon.getSqlWhere("SCRW_PCZT_CODE", "'"+zt.replaceAll(",", "','")+"'");
//                sql += " and SCRW_RWZT_CODE in ('" + zt.replaceAll(",", "','") + "')";
                if (zt.indexOf("RWZT03") != -1 && StringUtil.isNotEmpty(time)) {
                    zt = zt.replaceAll("RWZT03", "");
                    if (zt.indexOf(",,") != -1) {
                        zt = zt.replaceAll(",,", ",");
                    }
                    if (zt.indexOf(",") == 0) {
                        zt = zt.substring(1, zt.length());
                    }
                    if (zt.indexOf(",") == zt.length() - 1) {
                        zt = zt.substring(0, zt.length() - 1);
                    }
                    sql += " and SCRW_RWZT_CODE in ('" + zt.replaceAll(",", "','") + "') or SCRW_RWZT_CODE='RWZT03' and TO_DAYS(NOW())- TO_DAYS(SCRW_SJWGSJ)<='" + time + "'";
                } else {
                    sql += " and SCRW_RWZT_CODE in ('" + zt.replaceAll(",", "','") + "')";
                }
            } else {
                //sql += jgmesCommon.getSqlWhere("SCRW_PCZT_CODE", zt);
                sql += "and SCRW_RWZT_CODE='" + zt + "' ";
                if (zt.equals("RWZT03")) {//若为完成生产状态
                    if (StringUtil.isNotEmpty(time)) {//且系统参数启用并且有值，则加入时间校验
                        sql += "and TO_DAYS(NOW())- TO_DAYS(SCRW_SJWGSJ)<='" + time + "' ";
                    }
                }
            }
        }

        if (noLike != null && !"".equals(noLike)) {
            sql += " and (SCRW_GDHM like '%" + noLike + "%' or SCRW_RWDH like '%" + noLike + "%' or SCRW_DDHM like '%" + noLike + "%') ";
        }

        if (cpLike != null && !"".equals(cpLike)) {
            sql += " and (SCRW_CPBH like '%" + cpLike + "%' or SCRW_NAME like '%" + cpLike + "%' or SCRW_CPGG like '%" + cpLike + "%') ";
        }

        if (cxCode != null && !cxCode.isEmpty()) {
            System.out.println("获取生成任务列表：" + sql);
            sql += " and SCRW_PCRQ !=' ' order by b.SY_ORDERINDEX,a.SCRW_PCRQ desc";

            sql = "select a.* from JGMES_PLAN_SCRW a\r\n" +
                    "left join je_core_dictionaryitem b on a.SCRW_RWZT_CODE = b.dictionaryitem_itemcode\r\n" +
                    "LEFT JOIN je_core_dictionary c on c.DICTIONARY_ITEMROOT_ID = b.sy_parent\r\n" +
                    "where c.DICTIONARY_DDCODE = 'JGMES_DIC_RWZT' " + sql;
            //dicList = serviceTemplate.selectList("JGMES_PLAN_SCRW",sql);
            dicList = serviceTemplate.selectListBySql(sql);

            ret.TotalCount = Long.valueOf(dicList.size());

			/*
			//每页码
			if (pageSize == null || pageSize.isEmpty()) {
				pageSize = "10";
			}
			//当前页
			if (currPage == null || currPage.isEmpty()) {
				currPage = "1";
			}*/

            if (pageSize != null && !"".equals(pageSize) && currPage != null && !"".equals(currPage)) {

                int kss = Integer.parseInt(pageSize) * (Integer.parseInt(currPage) - 1);
                int size = Integer.parseInt(pageSize);

                sql += " LIMIT " + kss + "," + size + "";

            }


            dicList = serviceTemplate.selectListBySql(sql);

            ret.Data = dicList;
        }
        return ret;
    }


    public void getMachineState() {
        String userCode = request.getParameter("userCode");
        String pageSize = request.getParameter("PageSize");
        String currPage = request.getParameter("CurrPage");
        String state = request.getParameter("state");//状态
        String jtmc = request.getParameter("JtName");//机台名称
        List<DynaBean> dicList = new ArrayList<DynaBean>();
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        String sql = "select * from JGMES_ADMK_JTDA where 1=1 ";

        if (StringUtil.isNotEmpty(state)) {
            sql += "and  JTDA_JTZT_CODE='" + state + "'";
        }
        if (StringUtil.isNotEmpty(jtmc)) {
            sql += " and JTDA_JTMC like '%" + jtmc + "%'";
        }
//		String userSQL = "select * from JGMES_ADMK_YCLXFP where YCLXFP_YCCLRBM like '%"+userCode+"%'";
//		List<DynaBean> dynaBeans = serviceTemplate.selectListBySql(userSQL);//根据当前登录用户获取能处理的异常类型
//		String yclx = "";
//		if (dynaBeans!=null){
//			for (DynaBean bean : dynaBeans) {
//				yclx+="'"+bean.getStr("YCLXFP_JTYCLX_CODE")+"',";
//			}
//		}else{
//			return ;
//		}
//		yclx=yclx.substring(0,yclx.length()-1);//去除最后一个逗号
//		if (StringUtil.isNotEmpty(jth)){
//			sql+="and JTYCJL_JTH='"+jth+"'";
//		}
//		if (StringUtil.isNotEmpty(yclx)){
//			sql += "and JTYCJL_JTYCLX_CODE in ("+yclx+")";
//		}
        dicList = serviceTemplate.selectListBySql(sql);

        ret.TotalCount = Long.valueOf(dicList.size());

        if (pageSize != null && !"".equals(pageSize) && currPage != null && !"".equals(currPage)) {

            int kss = Integer.parseInt(pageSize) * (Integer.parseInt(currPage) - 1);
            int size = Integer.parseInt(pageSize);
            sql += " LIMIT " + kss + "," + size + "";
        }
//		sql +=" order by JTDA_JTH asc";
        System.out.println(sql);
        dicList = serviceTemplate.selectListBySql(sql);
        for (DynaBean bean : dicList) {
            if (bean.getStr("JTDA_JTSYZT_CODE").equals("2")) {//不启用为灰色
                bean.set("color", "gray");
            } else {
                if (bean.getStr("JTDA_JTZT_CODE").equals("1")) {
                    bean.set("color", "green");
                }
                if (bean.getStr("JTDA_JTZT_CODE").equals("2")) {
                    bean.set("color", "red");
                }
            }
        }
        ret.Data = ret.getValues(dicList);
        String jsonStr = jsonBuilder.toJson(ret);
        toWrite(jsonStr);

    }

    /*
     * 获取产线排产列表，包含暂停的
     */
    public void getA() {
        String userCode = request.getParameter("userCode");
        String pageSize = request.getParameter("pageSize");
        String currPage = request.getParameter("currPage");
        String jth = request.getParameter("jth");
        List<DynaBean> dicList = new ArrayList<DynaBean>();
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<List<HashMap>> ret = getAbnormalDynaBean(userCode, jth, pageSize, currPage);
        String jsonStr = jsonBuilder.toJson(ret);
        toWrite(jsonStr);
    }

    /**
     * 查询异常记录表，事务独立
     *
     * @param userCode
     * @param jth
     * @param pageSize
     * @param currPage
     * @return
     */
    private JgmesResult<List<HashMap>> getAbnormalDynaBean(String userCode, String jth, String pageSize, String currPage) {
        List<DynaBean> dicList = new ArrayList<DynaBean>();
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        String sql = "select * from JGMES_ADMK_JTYCJL where JTYCJL_JTYCCLZT_CODE!='CLZT03' and JTYCJL_JTYCCLZT_CODE!='CLZT04' and JTYCJL_JTYCCLZT_CODE!='CLZT06'";
        String user = userCode;//获取当前登录用户
        String userSQL = "select * from JGMES_ADMK_YCLXFP where YCLXFP_YCCLRBM like '%" + userCode + "%'";
        List<DynaBean> dynaBeans = serviceTemplate.selectListBySql(userSQL);//根据当前登录用户获取能处理的异常类型
        String yclx = "";
        if (dynaBeans.size() > 0) {
            for (DynaBean bean : dynaBeans) {
                yclx += "'" + bean.getStr("YCLXFP_JTYCLX_CODE") + "',";
            }
        } else {
            return ret;
        }
        yclx = yclx.substring(0, yclx.length() - 1);//去除最后一个逗号
        if (StringUtil.isNotEmpty(jth)) {
            sql += "and JTYCJL_JTH='" + jth + "'";
        }
        if (StringUtil.isNotEmpty(yclx)) {
            sql += "and JTYCJL_JTYCLX_CODE in (" + yclx + ")";
        }
        dicList = serviceTemplate.selectListBySql(sql);

        ret.TotalCount = Long.valueOf(dicList.size());

        if (pageSize != null && !"".equals(pageSize) && currPage != null && !"".equals(currPage)) {

            int kss = Integer.parseInt(pageSize) * (Integer.parseInt(currPage) - 1);
            int size = Integer.parseInt(pageSize);
            sql += " LIMIT " + kss + "," + size + "";
        }
        sql += " order by JTYCJL_JTYCCLZT_CODE asc";
        dicList = serviceTemplate.selectListBySql(sql);
        ret.Data = ret.getValues(dicList);
        return ret;
    }

    /**
     * 更改状态
     */
    public void changeAbnormalState() {
        String userCode = request.getParameter("userCode");
        String mac = request.getParameter("mac");
        String pageSize = request.getParameter("pageSize");
        String currPage = request.getParameter("currPage");
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        String state = request.getParameter("state");//处理状态
        String id = request.getParameter("id");//主键id
        String jth = request.getParameter("jth");//机台号
        String yclx = request.getParameter("yclx");//异常类型
        String sc = request.getParameter("sc");
        String pz = request.getParameter("pz");
        String dicSql = "select * from je_core_dictionaryitem where dictionaryitem_dictionary_ID = (select je_core_dictionary_ID from je_core_dictionary where dictionary_DDCODE = 'JGMES_DIC_JTYCCLZT')";
        List<DynaBean> clztDicList = serviceTemplate.selectListBySql(dicSql);
        String stateName = "";
        if (clztDicList != null) {
            for (DynaBean mlxDic : clztDicList) {
                String DICTIONARYITEM_ITEMNAME = mlxDic.getStr("DICTIONARYITEM_ITEMNAME");
                if (mlxDic.getStr("DICTIONARYITEM_ITEMCODE").equals(state)) {
                    stateName = DICTIONARYITEM_ITEMNAME;
                }
            }
        }
        if (StringUtil.isNotEmpty(state) && StringUtil.isNotEmpty(yclx)) {
            if (state.equals("CLZT03") || state.equals("CLZT04")) {
//				String sql = "update JGMES_ADMK_JTYCJL set JTYCJL_JTYCCLZT_CODE = '"+state+"',JTYCJL_JTYCCLZT_NAME = '"+stateName+"',JTYCJL_CLR='"+userCode+"',JTYCJL_CLSJ=now()  where JGMES_ADMK_JTYCJL_ID = '"+id+"'";
//				pcServiceTemplate.executeSql(sql);
                String tzSql = "update JGMES_ADMK_JTYCTZ set JTYCTZ_YDR='" + userCode + "',JTYCTZ_YDSJ=now(),JTYCTZ_YDZT_CODE='3',JTYCTZ_YDZT_NAME='结束' where JGMES_ADMK_JTYCJL_ID='" + id + "'";
                pcServiceTemplate.executeSql(tzSql);
            }
            if (state.equals("CLZT07")) {
                String tzSql = "update JGMES_ADMK_JTYCTZ set JTYCTZ_YDR='" + userCode + "',JTYCTZ_YDSJ=now(),JTYCTZ_YDZT_CODE='2',JTYCTZ_YDZT_NAME='已读' where JGMES_ADMK_JTYCJL_ID='" + id + "'";
                pcServiceTemplate.executeSql(tzSql);
            }
        }
        String sql = "update JGMES_ADMK_JTYCJL set JTYCJL_JTYCCLZT_CODE = '" + state + "',JTYCJL_JTYCCLZT_NAME = '" + stateName + "',JTYCJL_CLR='" + userCode + "',JTYCJL_CLSJ=now() ";
        if (StringUtil.isNotEmpty(sc)) {
            sql += " ,JTYCJL_SJSC='" + sc + "'";
        }
        if (StringUtil.isNotEmpty(pz)){
            sql += " ,JTYCJL_CLPZ='" + pz + "'";
        }
//        if (state.equals("CLZT04") && false) {
//            sql += " ,JTYCJL_CLPZ='" + sc + "' ";
//        }
        sql += " where JGMES_ADMK_JTYCJL_ID = '" + id + "'";
        pcServiceTemplate.executeSql(sql);
        JgmesResult<List<HashMap>> ret = getAbnormalDynaBean(userCode, jth, pageSize, currPage);
        String jsonStr = jsonBuilder.toJson(ret);
        toWrite(jsonStr);
    }


    /*
     * 获取数据字典集合
     * */
    public void getDictionary() throws ParseException {
//		Map ss = request.getParameterMap();
//		JSONObject dd = JSONObject.fromObject(requestParams);
        String mac = getParamStr("mac");// MAC地址
        String userCode = getParamStr("userCode");// 用户
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        String parentCode = getParamStr("parentCode");// 父级CODE@
        String parentId = getParamStr("parentId");// 父级ID
        Long count = (long) 0;
        String jsonStr = "";

        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            try {

                System.out.println(parentId);

                List<DynaBean> dicList = jgmesCommon.getDicList(parentCode, parentId);
                ret.Data = ret.getValues(dicList);
                ret.TotalCount = (long) dicList.size();

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                // TODO: handle exception
                ret.IsSuccess = false;
                ret.setMessage(e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);
            System.out.println("数据字典列表：");
            System.out.println(jsonStr);
            toWrite(jsonStr);
        }
    }

    /*
     * 根据产品编号获取该产品对应的工序列表
     * */
    public void getGXList() {
        String mac = getParamStr("mac");// MAC地址
        String userCode = getParamStr("userCode");// 用户
        String cpCode = request.getParameter("cpCode");// 产品编号
        String isBackWorkStation = request.getParameter("isBackWorkStation");// 是否为返回工站
        String jsonStr = "";
        String cpgylxId = "";// 产品工序路线ID
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
            try {
                List<DynaBean> gylxList = getGXList(cpCode);
                if (gylxList != null && gylxList.size() > 0) {
                    if (isBackWorkStation != null && !"".equals(isBackWorkStation)) {
                        List<DynaBean> gylxListforReturn = new ArrayList<>();
                        for (DynaBean dynaBean : gylxList) {
                            if (dynaBean.getStr("GYLXGX_NOWORK_CODE") != null && "1".equals(dynaBean.getStr("GYLXGX_NOWORK_CODE"))) {
                                gylxListforReturn.add(dynaBean);
                            }
                        }
                        ret.Data = ret.getValues(gylxListforReturn);
                        ret.TotalCount = (long) gylxListforReturn.size();
                    } else {
                        ret.Data = ret.getValues(gylxList);
                        ret.TotalCount = (long) gylxList.size();
                    }

                }
            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                // TODO: handle exception
                ret.setMessage(e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);

            toWrite(jsonStr);
        }
    }

    public List<DynaBean> getGXList(String cpCode) {
        String cpgylxId = "";// 产品工序路线ID
        List<DynaBean> gylxList = new ArrayList<DynaBean>();
        DynaBean cpsj = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA",
                " and PRODUCTDATA_BH='" + cpCode + "'");
        if (cpsj != null) {
            cpgylxId = cpsj.getStr("PRODUCTDATA_CPGYLXID");
            if (cpgylxId != null && !cpgylxId.isEmpty()) {
                gylxList = serviceTemplate.selectList("JGMES_GYGL_GYLXGX",
                        " and GYLX_ID='" + cpgylxId + "' order by SY_ORDERINDEX");//"GYLXGX_GXID,GYLXGX_ID,GYLXGX_GXNUM,GYLXGX_GXNAME,GYLXGX_SX"

//		jsonStr = jsonBuilder.buildListPageJson(new Long(gylxList.size()), gylxList, false);
            } else {
                System.out.println("cpgylxId为空");
            }
        } else {
            System.out.println("编号为[" + cpCode + "]cpsj为空");
        }
        return gylxList;
    }


    /*
     * 根据产品编号获取该产品对应的有不良项的工序列表
     * */
    public void getGXListByCpBh() {
        String mac = getParamStr("mac");// MAC地址
        String userCode = getParamStr("userCode");// 用户
        String cpCode = request.getParameter("cpCode");// 产品编号
        String jsonStr = "";
        String cpgylxId = "";// 产品工序路线ID
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
            try {
                ret = getGXListByCpBh(cpCode);
            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                // TODO: handle exception
                ret.setMessage(e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);

            toWrite(jsonStr);
        }
    }

    public JgmesResult<List<HashMap>> getGXListByCpBh(String cpCode) {
        List<DynaBean> gylxListforRe = new ArrayList<DynaBean>();
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        String cpgylxId = "";// 产品工序路线ID
        DynaBean cpsj = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA",
                " and PRODUCTDATA_BH='" + cpCode + "'");
        if (cpsj != null) {
            cpgylxId = cpsj.getStr("PRODUCTDATA_CPGYLXID");
            if (cpgylxId != null && !cpgylxId.isEmpty()) {
                JgmesQmsAction jgmesQmsAction = new JgmesQmsAction();
                JgmesResult<List<DynaBean>> ret1 = new JgmesResult<List<DynaBean>>();
                List<DynaBean> gylxList = serviceTemplate.selectList("JGMES_GYGL_GYLXGX",
                        " and GYLX_ID='" + cpgylxId + "' order by SY_ORDERINDEX");//"GYLXGX_GXID,GYLXGX_ID,GYLXGX_GXNUM,GYLXGX_GXNAME,GYLXGX_SX"
                for (DynaBean gylxDynaBean : gylxList) {
                    List<DynaBean> re = getBlDynaBean(gylxDynaBean.getStr("GYLXGX_ID"));
                    if (re != null && re.size() > 0) {
                        gylxListforRe.add(gylxDynaBean);
                    }
                }
                if (gylxListforRe != null && gylxListforRe.size() > 0) {
                    ret.Data = ret.getValues(gylxListforRe);
                } else {
                    logger.debug("请检查工序是否绑定不良项！：" + cpCode);
                    //ret.setErrorDicbyProperties("101");
                }
//		jsonStr = jsonBuilder.buildListPageJson(new Long(gylxList.size()), gylxList, false);
            } else {
                logger.debug("未从产品中获取到工艺路线：" + cpCode);
                //ret.setErrorDicbyProperties("100");
            }
        } else {
            logger.debug("未获取到产品数据：" + cpCode);
            //ret.setErrorDicbyProperties("1000");
        }

        return ret;
    }

    /*
     *
     *     根据工序ID获取对应的不良类型列表
     */
    public List<DynaBean> getBlDynaBean(String gxlxgxId) {
        List<DynaBean> blDynaBean = null;
        //获取工序检测不良项
        List<DynaBean> gxblDynaBeanList = serviceTemplate.selectList("JGMES_GYGL_GXBLX", " and GYLXGX_ID = '" + gxlxgxId + "'");
        if (gxblDynaBeanList != null && gxblDynaBeanList.size() > 0) {
            StringBuffer sBuffer = new StringBuffer("(");
            for (int i = 0; i < gxblDynaBeanList.size(); i++) {
                sBuffer.append("'" + gxblDynaBeanList.get(i).getStr("JGMES_BASE_BLLX_ID") + "'");
                if (i < gxblDynaBeanList.size() - 1) {
                    sBuffer.append(",");
                }
            }
            sBuffer.append(")");
            blDynaBean = serviceTemplate.selectList("JGMES_BASE_BLLX", " and JGMES_BASE_BLLX_ID in " + sBuffer);
        }
        return blDynaBean;
    }

    /*
     * 根据产品编号、工序获取工序不良检测项列表
     */

    public void getGXBLXMList() {
        String mac = getParamStr("mac");// MAC地址
        String userCode = getParamStr("userCode");// 产线编码
        String cpCode = request.getParameter("cpCode");// 产品编号
        String gxId = request.getParameter("gxId");// 工序ID，非必填，可通过当前用户去关联获取。
        String gxCode = request.getParameter("gxCode");// 工序编号，非必填，可通过当前用户去关联获取。
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        String jsonStr = "";
        System.out.println(mac + "----" + userCode + "---" + gxId + "----" + gxCode + "---" + cpCode);
        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();

            try {

                List<DynaBean> gxblxmList = getGXBLXMList(cpCode, gxId, gxCode, userCode);
                if (gxblxmList != null && gxblxmList.size() > 0) {
                    // jsonStr = jsonBuilder.buildListPageJson(new Long(gxxmList.size()), gxxmList,
                    // false);
                    ret.Data = ret.getValues(gxblxmList);
                    ret.TotalCount = (long) gxblxmList.size();
                } else {
                    System.out.println("产品编号：[" + cpCode + "]，工序ID：[" + gxId + "]，工序代码：[" + gxCode + "]，用户：[" + userCode
                            + "]的工项目列表gxblxmList为空");
                }
            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                // TODO: handle exception
                ret.setMessage(e.toString());
            }

            jsonStr = jsonBuilder.toJson(ret);

            toWrite(jsonStr);
        }
    }

    /*
     * 根据产品编号、工序获取工序物料项列表
     */

    public void getGXWLXMList() {
        String mac = getParamStr("mac");// MAC地址
        String userCode = getParamStr("userCode");// 产线编码
        String cpCode = request.getParameter("cpCode");// 产品编号
        String gxId = request.getParameter("gxId");// 工序ID，非必填，可通过当前用户去关联获取。
        String gxCode = request.getParameter("gxCode");// 工序编号，非必填，可通过当前用户去关联获取。
        String jsonStr = "";
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        System.out.println(mac + "----" + userCode + "---" + gxId + "----" + gxCode + "---" + cpCode);
        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();

            try {

                List<HashMap> gxwlxmList = getGXWLXMList(cpCode, gxId, gxCode, userCode);
                if (gxwlxmList != null && gxwlxmList.size() > 0) {
                    // jsonStr = jsonBuilder.buildListPageJson(new Long(gxxmList.size()), gxxmList,
                    // false);
                    ret.Data = gxwlxmList;
                    ret.TotalCount = (long) gxwlxmList.size();

                } else {
                    System.out.println("产品编号：[" + cpCode + "]，工序ID：[" + gxId + "]，工序代码：[" + gxCode + "]，用户：[" + userCode
                            + "]的工项目列表gxwlxmList为空");
                }
            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                // TODO: handle exception
                ret.setMessage(e.toString());
            }

            jsonStr = jsonBuilder.toJson(ret);

            toWrite(jsonStr);
        }
    }


    /*
     * 根据产品编号、工序获取对应的工序项目列表
     * */
    public void getGXXMList() {
        String mac = getParamStr("mac");// MAC地址
        String userCode = getParamStr("userCode");// 产线编码
        String cpCode = request.getParameter("cpCode");// 产品编号
        String gxId = request.getParameter("gxId");// 工序ID，非必填，可通过当前用户去关联获取。
        String gxCode = request.getParameter("gxCode");// 工序编号，非必填，可通过当前用户去关联获取。
        String jsonStr = "";
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();

            try {

                List<DynaBean> gxxmList = getGXXMList(cpCode, gxId, gxCode, userCode);
                if (gxxmList != null && gxxmList.size() > 0) {
//			jsonStr = jsonBuilder.buildListPageJson(new Long(gxxmList.size()), gxxmList, false);
                    ret.Data = ret.getValues(gxxmList);
                    ret.TotalCount = (long) gxxmList.size();

                } else {
                    System.out.println("产品编号：[" + cpCode + "]，工序ID：[" + gxId + "]，工序代码：[" + gxCode + "]，用户：[" + userCode
                            + "]的工项目列表gxxmList为空");
                }
            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                // TODO: handle exception
                ret.setMessage(e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);

            toWrite(jsonStr);
        }
    }

    /*
     * 获取当前工序项目列表
     * */
    public void getCurrentGXXMList() {
        String mac = getParamStr("mac");//MAC地址
        String userCode = getParamStr("userCode");//用户
        String jsonStr = "";

        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {

            List<DynaBean> gxxmList = getGXXMList(null, null, null, userCode);
            if (gxxmList != null && gxxmList.size() > 0) {
                jsonStr = jsonBuilder.buildListPageJson(new Long(gxxmList.size()), gxxmList, false);
            }

            toWrite(jsonStr);
        }
    }

    // 获取当前工序不良检测数据
    private List<DynaBean> getGXBLXMList(String cpCode, String gxId, String gxCode, String userCode) {
        List<DynaBean> gxblxmList = new ArrayList<DynaBean>();
        if (cpCode == null || cpCode.isEmpty()) {
            DynaBean cpDynaBean = getCurrentProductDB(userCode);
            if (cpDynaBean != null) {
                cpCode = cpDynaBean.getStr("SCRW_CPBH");
            }
        }

        if (gxCode != null && !gxCode.isEmpty()) {
            DynaBean gxDynaBean = serviceTemplate.selectOne("JGMES_GYGL_GYLXGX", " and GYLXGX_GXNUM='" + gxCode + "'");
            if (gxDynaBean != null) {
                gxId = gxDynaBean.getStr("GYLXGX_ID");//.getPkValue();
            }
        }
        if (gxId == null || gxId.isEmpty()) {
            DynaBean gx = getCurrentGX(cpCode, userCode);
            gxId = gx.getStr("GXGL_GYLX_GXID");
        }

        if (gxId != null && !gxId.isEmpty()) {
            System.out.println("getCurrentGXXMList——工序ID：" + gxId);
            gxblxmList = serviceTemplate.selectList("JGMES_GYGL_GXBLX", " and GYLXGX_ID='" + gxId + "'");
        }
        return gxblxmList;
    }

    // 获取当前工序物料列表
    private List<HashMap> getGXWLXMList(String cpCode, String gxId, String gxCode, String userCode) {
        List<HashMap> res = new ArrayList<HashMap>();
        List<DynaBean> gxwlxmList = new ArrayList<DynaBean>();
        if (cpCode == null || cpCode.isEmpty()) {
            DynaBean cpDynaBean = getCurrentProductDB(userCode);
            if (cpDynaBean != null) {
                cpCode = cpDynaBean.getStr("SCRW_CPBH");
            }
        }

        if (gxCode != null && !gxCode.isEmpty()) {
            DynaBean gxDynaBean = serviceTemplate.selectOne("JGMES_GYGL_GYLXGX", " and GYLXGX_GXNUM='" + gxCode + "'");
            if (gxDynaBean != null) {
                gxId = gxDynaBean.getPkValue();
            }
        }
        if (gxId == null || gxId.isEmpty()) {
            DynaBean gx = getCurrentGX(cpCode, userCode);
            gxId = gx.getStr("GXGL_GYLX_GXID");
        }

        if (gxId != null && !gxId.isEmpty()) {
            gxwlxmList = serviceTemplate.selectList("JGMES_GXGL_GXWL", " and GYLXGX_ID='" + gxId + "'");
            res = updateWlTm(gxwlxmList);
        }
        return res;
    }

    /*
     * 更新物料条码
     */
    private List<HashMap> updateWlTm(List<DynaBean> origList) {
        List<HashMap> gxwlxmList = new ArrayList<HashMap>();
        if (origList != null && origList.size() > 0) {
            for (int i = 0; i < origList.size(); i++) {
                DynaBean gxwl = origList.get(i);
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                if (gxwl != null) {
                    DynaBean cpDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " and PRODUCTDATA_BH='" + gxwl.getStr("GXWL_WLBH") + "'");
                    if (cpDynaBean != null) {
                        gxwl.setStr("GXWL_TM", cpDynaBean.getStr("PRODUCTDATA_TMH"));
                        hashMap = gxwl.getValues();
                        hashMap.put("Detail", cpDynaBean.getValues());
                    } else {
                        hashMap = gxwl.getValues();
                    }
                    gxwlxmList.add(hashMap);
                }
            }
        }
        return gxwlxmList;
    }

    private List<DynaBean> getGXXMList(String cpCode, String gxId, String gxCode, String userCode) {
        List<DynaBean> gxxmList = new ArrayList<DynaBean>();
        if (cpCode == null || cpCode.isEmpty()) {
            DynaBean cpDynaBean = getCurrentProductDB(userCode);
            if (cpDynaBean != null) {
                cpCode = cpDynaBean.getStr("SCRW_CPBH");
            }
        }

        if (gxCode != null && !gxCode.isEmpty()) {
            DynaBean gxDynaBean = serviceTemplate.selectOne("JGMES_GYGL_GYLXGX", " and GYLXGX_GXNUM='" + gxCode + "'");
            if (gxDynaBean != null) {
                gxId = gxDynaBean.getPkValue();
            }
        }
        if (gxId == null || gxId.isEmpty()) {
            DynaBean gx = getCurrentGX(cpCode, userCode);
            gxId = gx.getStr("GXGL_GYLX_GXID");
        }

        if (gxId != null && !gxId.isEmpty()) {
            gxxmList = serviceTemplate.selectList("JGMES_GXGL_GXXM", " and GYLXGX_ID='" + gxId + "'");
        }
        return gxxmList;
    }


    /*
     * 获取当前排产列表
     */
    public void getCurrentPCLB() {
        String mac = getParamStr("mac");//MAC地址
        String userCode = request.getParameter("userCode");
        String cxCode = request.getParameter("cxCode");
        List<DynaBean> dicList = new ArrayList<DynaBean>();
        String jsonStr = "";
        JgmesResult<List<HashMap>> jgmesResult = new JgmesResult<List<HashMap>>();
        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {

            try {


                if (cxCode == null || cxCode.isEmpty()) {
                    DynaBean cxDynaBean = getCurrentCX(userCode);
                    if (cxDynaBean != null) {
                        cxCode = cxDynaBean.getStr("CXSJ_CXBM");
                    }
                }
                if (cxCode != null && !cxCode.isEmpty()) {
                    dicList = serviceTemplate.selectList("JGMES_PLAN_PCLB", " AND PCLB_CXBM='" + cxCode + "'");
                    if (dicList != null && dicList.size() > 0) {
                        jgmesResult.Data = jgmesResult.getValues(dicList);
                        jgmesResult.TotalCount = (long) dicList.size();
                    }
                }

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                // TODO: handle exception
                jgmesResult.setMessage("获取当前排产列表错误！" + e.toString());
            }
            jsonStr = jsonBuilder.toJson(jgmesResult);

            toWrite(jsonStr);
        }
    }


    /*
     * 获取当前生产任务列表
     */
    public void getCurrentScrw() {
        String mac = getParamStr("mac");//MAC地址
        String userCode = request.getParameter("userCode");
        String cxCode = request.getParameter("cxCode");
        List<DynaBean> dicList = new ArrayList<DynaBean>();
        String jsonStr = "";
        JgmesResult<List<HashMap>> jgmesResult = new JgmesResult<List<HashMap>>();

        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                if (cxCode == null || cxCode.isEmpty()) {
                    DynaBean cxDynaBean = getCurrentCX(userCode);
                    if (cxDynaBean != null) {
                        cxCode = cxDynaBean.getStr("CXSJ_CXBM");
                    }
                }
                if (cxCode != null && !cxCode.isEmpty()) {
                    dicList = serviceTemplate.selectList("JGMES_PLAN_SCRW", " AND SCRW_CXBM='" + cxCode + "'");
                    if (dicList != null && dicList.size() > 0) {
                        jgmesResult.Data = jgmesResult.getValues(dicList);
                        jgmesResult.TotalCount = (long) dicList.size();
                    }
                }

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                // TODO: handle exception
                jgmesResult.setMessage("获取当前生产任务列表错误！" + e.toString());
            }
            jsonStr = jsonBuilder.toJson(jgmesResult);

            toWrite(jsonStr);
        }
    }


    /*
     * 获取当前产品－－排产的第一条记录
     */
    private DynaBean getCurrentProductDB(String userCode) {
        DynaBean pcDynaBean = new DynaBean();

        DynaBean cxDynaBean = getCurrentCX(userCode);
        if (cxDynaBean != null) {
            List<DynaBean> dicList = serviceTemplate.selectList("JGMES_PLAN_SCRW", " AND SCRW_CXBM='" + cxDynaBean.getStr("CXSJ_CXBM") + "'", 0, 1);
            if (dicList != null && dicList.size() > 0) {
                pcDynaBean = (DynaBean) dicList.get(0);
            }
        }

        return pcDynaBean;
    }

    /*
     * 获取当前产品－－根据产线指定的生产任务单来获取
     */
    private DynaBean getCurrentProductByCxCp(String userCode) {
        DynaBean scrwDynaBean = getScrwByCxCp(userCode, null, null);
        return scrwDynaBean;
    }


    /*
     * 获取当前产品－－根据产线指定的生产任务单来获取
     */
    public JgmesResult<HashMap> getCurrentScrwProduct(String userCode, String cxId, String cxCode, String barCode) {

        return getScrwProduct(userCode, cxId, cxCode, barCode);

    }


    /*
     * 获取当前产品－－根据产线指定的生产任务单来获取
     */

    /**
     * 获取当前产品－－根据产线指定的生产任务单来获取(如果有条码的话，根据条码来获取)
     *
     * @param userCode
     * @param cxId
     * @param cxCode
     * @param barCode
     * @return
     */
    private JgmesResult<HashMap> getScrwProduct(String userCode, String cxId, String cxCode, String barCode) {
        HashMap<String, Object> res = new HashMap<String, Object>();

        DynaBean scrwDynaBean = new DynaBean();
        DynaBean cxDynaBean = new DynaBean();
        JgmesResult<HashMap> jgmesResult = new JgmesResult<HashMap>();
        if (barCode != null && !"".equals(barCode)) {
            DynaBean tmScDynaBean = serviceTemplate.selectOne("JGMES_BASE_GDCPTM", " and GDCPTM_TMH='" + barCode + "'");
            if (tmScDynaBean != null) {
                //获取生产任务
                scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and JGMES_PLAN_SCRW_ID = '" + tmScDynaBean.getStr("JGMES_PLAN_SCRW_ID") + "'");
            }
        } else {
            List<DynaBean> scrwDynaBeanList = serviceTemplate.selectList("JGMES_PLAN_SCRW", " and SCRW_CXBM = '" + cxCode + "' and SCRW_RWZT_CODE = 'RWZT02' order by SY_ORDERINDEX desc,SCRW_SJKGSJ desc");
            if (scrwDynaBeanList != null && scrwDynaBeanList.size() > 0) {
                scrwDynaBean = scrwDynaBeanList.get(0);
            }
        }
        if (scrwDynaBean == null) {
            System.out.println("当前产线未指定生产任务！");
        } else {
            res = scrwDynaBean.getValues();
            String cpCode = scrwDynaBean.getStr("SCRW_CPBH");
            String khcph = "";
            if (cpCode != null && !cpCode.isEmpty()) {
                DynaBean cpDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " and PRODUCTDATA_BH='" + cpCode + "'");
                khcph = cpDynaBean.getStr("PRODUCTDATA_KHCPH");
                if (khcph != null && !khcph.isEmpty()) {
                    scrwDynaBean.set("SCRW_KHCPM", khcph);
                    res = scrwDynaBean.getValues();
                }
                res.put("Detail", cpDynaBean.getValues());
            }

            jgmesResult.Data = res;
        }
        return jgmesResult;
    }


    /*
     * 获取当前产品－－根据产线指定的生产任务单来获取,
     */
    private DynaBean getScrwByCxCp(String userCode, String cxId, String cxCode) {
        DynaBean scrwDynaBean = new DynaBean();
        DynaBean cxDynaBean = new DynaBean();
        JgmesResult<String> jgmesResult = new JgmesResult<String>();

        if (cxId != null && !cxId.isEmpty()) {//有ID则通过ID获取产线
            cxDynaBean = serviceTemplate.selectOneByPk("JGMES_BASE_CXSJ", cxId);
        } else {
            if (cxCode != null && !cxCode.isEmpty()) {//无ID有CODE则通过CODE获取产线
                cxDynaBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ", " and CXSJ_CXBM='" + cxCode + "'");
            } else {//都没有则获取当前的产线
                cxDynaBean = getCurrentCX(userCode);
            }
        }

        if (cxDynaBean != null) {
            //scrwDynaBean =serviceTemplate.selectOneByPk("JGMES_PLAN_SCRW", cxDynaBean.getStr("JGMES_PLAN_SCRW_ID"));

            scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and SCRW_CXBM = '" + cxDynaBean.getStr("CXSJ_CXBM") + "' and SCRW_RWZT_CODE = 'RWZT02'");
            if (scrwDynaBean == null) {
                jgmesResult.setMessage("当前产线没有生产中的生产任务！");
                System.out.println("当前产线没有生产中的生产任务！");
                toWrite(jsonBuilder.toJson(jgmesResult));
            } else {//将客户产品号从产品数据中获取过来
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
        } else {
            jgmesResult.setMessage("产线数据为空！");
            System.out.println("getScrwByCxCp产线数据为空！");
            toWrite(jsonBuilder.toJson(jgmesResult));
        }

        return scrwDynaBean;
    }

    /*
     * 通过条码获取生产任务，首任务则只能按排产的获取，产线数据可不传
     */
    private DynaBean getScrwByBarcode(String barCode, String userCode, String cxId, String cxCode) {
        DynaBean scrwDynaBean = new DynaBean();
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);

        String scrwId = jgmesCommon.getScrwIdByBarCodeOfBgsj(barCode);
        if (scrwId != null && !scrwId.isEmpty()) {
            scrwDynaBean = serviceTemplate.selectOneByPk("JGMES_PLAN_SCRW", scrwId);
        } else {
            scrwDynaBean = getScrwByCxCp(userCode, cxId, cxCode);
        }
        return scrwDynaBean;
    }

    /*
     * 获取当前产品
     */
    public void getCurrentProduct() {
        String mac = getParamStr("mac");//MAC地址
        String userCode = getParamStr("userCode");//产线编码

        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {

            JgmesResult<String> ret = new JgmesResult<String>();
            String jsonStr = "";

            try {
                DynaBean pcDynaBean = getCurrentProductDB(userCode);
                if (pcDynaBean != null) {
                    jsonStr = jsonBuilder.toJson(pcDynaBean);
                    ret.Data = jsonStr;
                }
            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                ret.setMessage(e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);
            toWrite(jsonStr);
        }
    }

    public void getCurrentProductByScrwId() {
        String mac = getParamStr("mac");// MAC地址
        String userCode = getParamStr("userCode");// 用户编码
        String scrwId = getParamStr("scrwId");// 排产单ID
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
        String jsonStr = "";
        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                DynaBean scrwDynaBean = serviceTemplate.selectOneByPk("JGMES_PLAN_SCRW", scrwId);
                if (scrwDynaBean != null) {
                    ret.Data = scrwDynaBean.getValues();
                }

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                ret.setMessage(e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);
            toWrite(jsonStr);
        }
    }

    /*
     * 根据产线指定的产品来获取当前工位的产品信息
     */
    public void getCurrentProductByCxCp() {
        String mac = getParamStr("mac");// MAC地址
        String userCode = getParamStr("userCode");// 用户编码
        String cxId = getParamStr("cxId");// 产线ID
        String cxCode = getParamStr("cxCode");// 产线编码
        String barCode = getParamStr("barCode");// 产品条码号
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
        String jsonStr = "";

        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                ret = getCurrentScrwProduct(userCode, cxId, cxCode, barCode);

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                ret.setMessage(e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);
            toWrite(jsonStr);
        }
    }


    /*
     * 获取当前产线
     */
    private DynaBean getCurrentCX(String userCode) {
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
     * 获取当前产线
     */
    public void getCurrentCx() {
        String mac = getParamStr("mac");//MAC地址
        String userCode = getParamStr("userCode");//产线编码

        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {

            DynaBean cxDynaBean = getCurrentCX(userCode);
            String jsonStr = jsonBuilder.toJson(cxDynaBean);
            toWrite(jsonStr);
        }
    }

    /*
     * 获取当前用户
     */
    private String getCurrentUserCode(String userCode) {
        if (userCode == null || userCode.isEmpty()) {
            JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
            userCode = jgmesCommon.jgmesUser.getCurrentUserCode();
        }
        return userCode;
    }

    /*
     * 获取当前工位
     */
    private DynaBean getCurrentGW(String userCode) {
        DynaBean gw = new DynaBean();
        userCode = getCurrentUserCode(userCode);
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        String gwid = "";
        DynaBean dicNo = jgmesCommon.getDic("JGMES_YES_NO", "1");
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
     * 获取当前工位--对外
     */
    public void getCurrentGw() {
        String mac = getParamStr("mac");//MAC地址
        String userCode = getParamStr("userCode");//产线编码
        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {

            DynaBean gwDynaBean = getCurrentGW(userCode);
            String jsonStr = jsonBuilder.toJson(gwDynaBean);
            toWrite(jsonStr);
        }
    }

    /*
     * 获取当前工序，说明GXGL_GYLX_GXID字段为工艺路线工序ID，后台专用；GXGL_ID这个是本身的工序ID，其它地方不要使用
     */
    public DynaBean getCurrentGX(String cpCode, String userCode) {
        DynaBean gx = new DynaBean();
        DynaBean gw = getCurrentGW(userCode);
        DynaBean cPGWGX = null;
        String gxid = "";
        if (gw != null) {
            //获取产品工艺路线 add 2019-05-09
            DynaBean cpDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " and PRODUCTDATA_BH = '" + cpCode + "'");
            if (cpDynaBean == null) {
                logger.error("获取当前工序接口，未根据产品编码：" + cpCode + "获取到产品信息");
                return gx;
            }

            //根据工位信息、产品信息获取工序信息
//			List<DynaBean> list = serviceTemplate.selectList("JGMES_BASE_CPGWGX", " and CPGWGX_CPBH='"+cpCode+"' and JGMES_BASE_GW_ID='"+gw.getStr("JGMES_BASE_GW_ID")+"'");
            List<DynaBean> list = serviceTemplate.selectList("JGMES_BASE_CPGWGX", " and CPGWGX_CPBH='" + cpCode + "' and JGMES_BASE_GW_ID='" + gw.getStr("JGMES_BASE_GW_ID") + "' and CPGWGX_CPGYLXID = '" + cpDynaBean.getStr("PRODUCTDATA_CPGYLXID") + "'");
            if (list != null && list.size() > 0) {
                cPGWGX = list.get(0);
                if (list.size() > 1) {
                    System.out.println("产品[" + cpCode + "]、工位[" + gw.getStr("GW_GWBH") + "]有多条记录！");
                }
            }
//			DynaBean cPGWGX = serviceTemplate.selectOne("JGMES_BASE_CPGWGX", " and CPGWGX_CPBH='"+cpCode+"' and JGMES_BASE_GW_ID='"+gw.getStr("JGMES_BASE_GW_ID")+"'");
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

    /*
     * 获取当前工序－－对外
     */
    public void getCurrentGx() {
        String mac = getParamStr("mac");//MAC地址
        String userCode = getParamStr("userCode");// 用户
        String cpCode = request.getParameter("cpCode");// 产品编号

        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {

            JgmesResult<HashMap> ret = new JgmesResult<HashMap>();

            try {
                if (cpCode == null || cpCode.isEmpty()) {
                    DynaBean pcDynaBean = getCurrentProductByCxCp(userCode);
                    if (pcDynaBean != null) {
                        cpCode = pcDynaBean.getStr("SCRW_CPBH");
                    }
                }
                DynaBean gxBean = getCurrentGX(cpCode, userCode);
                // String jsonStr = jsonBuilder.toJson(gxBean);
                ret.Data = gxBean.getValues();
            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                ret.setMessage(e.toString());
            }
            String jsonStr = jsonBuilder.toJson(ret);

            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "x-requested-with");
            response.addHeader("Access-Control-Max-Age", "1800");//30 min

            toWrite(jsonStr);
        }
    }

    /*
     * 获取所有的维修站的集合
     */
    public void getWXZList() {
        String mac = getParamStr("mac");// MAC地址
        String userCode = getParamStr("userCode");// 用户编码
        String cxCode = getParamStr("cxCode");// 产线编号
        String cxId = getParamStr("cxId");// 产线ID
        String fxzLx = getParamStr("fxzLx");// 返修站类型值
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        String jsonStr = "";
        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {

            try {
                List<DynaBean> wxzList = getWxzList(cxId, cxCode, fxzLx);
                if (wxzList != null && wxzList.size() > 0) {
//					jsonStr = jsonBuilder.toJson(pcDynaBean);
                    ret.Data = ret.getValues(wxzList);
                    ret.TotalCount = (long) wxzList.size();
                }

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                ret.setMessage(e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);
            toWrite(jsonStr);
        }
    }


    /*
     * 获取所有的维修站的集合
     */
    private List<DynaBean> getWxzList(String cxId, String cxCode, String fxzLx) {
        List<DynaBean> wxzList = new ArrayList<DynaBean>();
        String sql = "";
        if (cxId != null && !cxId.isEmpty()) {
            sql += " and FXZ_CXID='" + cxId + "'";
        } else {
            if (cxCode != null && !cxCode.isEmpty()) {
                sql += " and FXZ_CXBH='" + cxCode + "'";
            }
        }
        if (fxzLx != null && !fxzLx.isEmpty()) {
            sql += " and FXZ_FXZLX_CODE='" + fxzLx + "'";
        }
        wxzList = serviceTemplate.selectList("JGMES_PB_FXZ", sql);

        return wxzList;
    }

    /*
     * 获取获取返修站项目集合
     */
    public void getWXZXMList() {
        String mac = getParamStr("mac");// MAC地址
        String userCode = getParamStr("userCode");// 用户编码
        String fxzId = getParamStr("fxzId");// 返修站ID
        String fxzCode = getParamStr("fxzCode");// 返修站ID
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        String jsonStr = "";

        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                List<DynaBean> wxzList = getWXZXmList(fxzId, fxzCode);
                if (wxzList != null && wxzList.size() > 0) {
//  					jsonStr = jsonBuilder.toJson(pcDynaBean);
                    ret.Data = ret.getValues(wxzList);
                    ret.TotalCount = (long) wxzList.size();
                }

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                ret.setMessage(e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);
            toWrite(jsonStr);
        }
    }


    /*
     * 获取获取返修站项目集合
     */
    private List<DynaBean> getWXZXmList(String fxzId, String fxzCode) {
        List<DynaBean> wxzList = new ArrayList<DynaBean>();
        DynaBean wxzDynaBean = getWXZ(fxzId, fxzCode);
        if (wxzDynaBean != null) {
            wxzList = serviceTemplate.selectList("JGMES_PB_FXZZB", " and JGMES_PB_FXZ_ID='" + wxzDynaBean.getPkValue() + "'");
        }

        return wxzList;
    }


    /*
     * 根据维修站ID或编码获取维修站
     */
    public void getWXZ() {
        String mac = getParamStr("mac");// MAC地址
        String userCode = getParamStr("userCode");// 用户编码
        String wxzCode = getParamStr("wxzCode");// 用户编码
        String wxzId = getParamStr("wxzId");// 用户编码
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
        String jsonStr = "";
        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {

            try {
                DynaBean wxzDynaBean = getWXZ(wxzId, wxzCode);
                if (wxzDynaBean != null) {
//					jsonStr = jsonBuilder.toJson(pcDynaBean);
                    ret.Data = wxzDynaBean.getValues();
                }

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                ret.setMessage(e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);
            toWrite(jsonStr);
        }
    }


    /*
     * 根据维修站ID或编码获取维修站
     */
    private DynaBean getWXZ(String wxzId, String wxzCode) {
        DynaBean wxzDynaBean = new DynaBean();
        if (wxzId == null || wxzId.isEmpty()) {
            if (wxzCode == null || wxzCode.isEmpty()) {
                return null;
            } else {
                wxzDynaBean = serviceTemplate.selectOne("JGMES_PB_FXZ", " and FXZ_FXZBH='" + wxzCode + "'");
            }
        } else {
            wxzDynaBean = serviceTemplate.selectOneByPk("JGMES_PB_FXZ", wxzId);
        }
        return wxzDynaBean;
    }

    /*
     *根据条码获取相关产品信息接口，返回产品信息、维修项目明细
     */
    public void getWXZCpInfo() {
        String mac = getParamStr("mac");// MAC地址
        String userCode = getParamStr("userCode");// 用户编码
        String barCode = getParamStr("barCode");// 条码
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
        String jsonStr = "";
        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {

            try {
                HashMap wxzDynaBean = getWXZCpInfo(barCode);
                if (wxzDynaBean != null) {
//					jsonStr = jsonBuilder.toJson(pcDynaBean);
                    ret.Data = wxzDynaBean;
                }

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                ret.setMessage(e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);
            System.out.println("返修的数据：" + jsonStr);

            toWrite(jsonStr);
        }
    }


    /*
     * 根据维修站ID或编码获取返修单详细信息
     */
    private HashMap<String, Object> getWXZCpInfo(String barCode) {
        HashMap<String, Object> res = new HashMap<String, Object>();
        String pcdId = "";
        JgmesResult<List<HashMap>> result = new JgmesResult<List<HashMap>>();
        List<DynaBean> fxdDynaBeanList = serviceTemplate.selectList("JGMES_PB_FXD", " and FXD_CPTMH='" + barCode + "' and FXD_WXZT_CODE='" + JgmesEnumsDic.WxZtToDo.getKey() + "'");
        if (fxdDynaBeanList != null && fxdDynaBeanList.size() > 0) {

            res.put("cpInfo", fxdDynaBeanList.get(0).getValues());//改成直接返回返修单主表
            List<DynaBean> fxdzbList = serviceTemplate.selectList("JGMES_PB_FXDZB", " and JGMES_PB_FXD_ID='" + fxdDynaBeanList.get(0).getStr("JGMES_PB_FXD_ID") + "' order by SY_ORDERINDEX");
            if (fxdzbList != null && fxdzbList.size() > 0) {
                res.put("fxXmList", result.getValues(fxdzbList));
                System.out.println("返修单子表数据条数：" + fxdzbList.size());
                System.out.println("返修单子表数据：" + jsonBuilder.toJson(result.getValues(fxdzbList)));
            }
        }

        return res;
    }


    /*
     * 获取异常处理列表
     */
    public void getYcClList() {
        String mac = getParamStr("mac");// MAC地址
        String userCode = getParamStr("userCode");// 用户编码
        String cpCode = getParamStr("cpCode");// 产品代码
        String ycztCode = getParamStr("ycztCode");// 异常处理状态
        String gxCode = getParamStr("gxCode");// 工序代码
        String gwCode = getParamStr("gwCode");// 工位代码
        String cxCode = getParamStr("cxCode");// 产线代码
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        String jsonStr = "";

        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                List<DynaBean> adycList = getYcClList(userCode, cpCode, ycztCode, gxCode, gwCode, cxCode);
                if (adycList != null && adycList.size() > 0) {
//  					jsonStr = jsonBuilder.toJson(pcDynaBean);
                    ret.Data = ret.getValues(adycList);
                    ret.TotalCount = (long) adycList.size();
                }

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                ret.setMessage(e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);
            toWrite(jsonStr);
        }
    }


    /*
     * 获取异常处理列表
     */
    private List<DynaBean> getYcClList(String userCode, String cpCode, String ycztCode, String gxCode, String gwCode, String cxCode) {
        List<DynaBean> adycList = new ArrayList<DynaBean>();
        String sql = "";
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);

        if ((cpCode != null && !cpCode.isEmpty()) || (gxCode != null && !gxCode.isEmpty()) || (gwCode != null && !gwCode.isEmpty()) || (cxCode != null && !cxCode.isEmpty())) {
            ///??
        } else {//按当前产品当前工序资料去获取
            DynaBean cpDynaBean = getCurrentProductByCxCp(userCode);
            if (cpDynaBean != null) {
                cpCode = cpDynaBean.getStr("SCRW_CPBH");
            }
        }

        sql += jgmesCommon.getSqlWhere("ADYCCL_YCZT_CODE", ycztCode);
        sql += jgmesCommon.getSqlWhere("ADYCCL_CPBH", cpCode);
        sql += jgmesCommon.getSqlWhere("ADYCCL_GXBH", gxCode);
        sql += jgmesCommon.getSqlWhere("ADYCCL_GWBH", gwCode);
        sql += jgmesCommon.getSqlWhere("ADYCCL_CXBH", cxCode);

        adycList = serviceTemplate.selectList("JGMES_PB_ADYCCL", sql);

        return adycList;
    }

    /*
     * 根据异常处理ID或编码获取异常处理 的数据
     */
    public void getYcClById() {
        String mac = getParamStr("mac");// MAC地址
        String userCode = getParamStr("userCode");// 用户编码
        String ycclCode = getParamStr("ycclCode");//
        String ycclId = getParamStr("ycclId");//
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
        String jsonStr = "";
        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {

            try {
                DynaBean ycclDynaBean = getYcClById(ycclId, ycclCode);
                if (ycclDynaBean != null) {
//  					jsonStr = jsonBuilder.toJson(pcDynaBean);
                    ret.Data = ycclDynaBean.getValues();
                }

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                ret.setMessage(e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);
            toWrite(jsonStr);
        }
    }


    /*
     * 根据异常处理ID或编码获取异常处理 的数据
     */
    private DynaBean getYcClById(String ycclId, String ycclCode) {
        DynaBean ycclDynaBean = new DynaBean();
        if (ycclId == null || ycclId.isEmpty()) {
            if (ycclCode == null || ycclCode.isEmpty()) {
                return null;
            } else {
//  	    		wxzDynaBean = serviceTemplate.selectOne("JGMES_PB_ADYCCL", " and FXZ_FXZBH='"+ycclCode+"'");
            }
        } else {
            ycclDynaBean = serviceTemplate.selectOneByPk("JGMES_PB_ADYCCL", ycclId);
        }
        return ycclDynaBean;
    }


    /*
     * 保存 异常处理
     */
    public void doJsonSaveYcCl() {

        String mac = request.getParameter("mac");// MAC地址
        String userCode = request.getParameter("userCode");// 用户编号
        String jsonStr = request.getParameter("jsonStr");

        String tabCode = "JGMES_PB_ADYCCL";
        String pkValue = "";

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();

        System.out.println("jsonString:" + jsonStr);
        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                DynaBean ycclDynaBean = jgmesCommon.getDynaBeanByJsonStr(tabCode, jsonStr);
                // 其它字段在这里赋值、数据字典、产线、工序、工位、产品等相应信息需要添加当前
                // 安灯系统类型
                jgmesCommon.setDynaBeanDicByCode(ycclDynaBean, "JGMES_DIC_ADLX", "ADYCCL_ADLX_CODE", "ADYCCL_ADLX_ID",
                        "ADYCCL_ADLX_NAME");
                // 异常状态
                jgmesCommon.setDynaBeanDicByCode(ycclDynaBean, "JGMES_DIC_YCZT", "ADYCCL_YCZT_CODE", "ADYCCL_YCZT_ID",
                        "ADYCCL_YCZT_NAME");
                // 异常原因－－树形第二级
                jgmesCommon.setDynaBeanDicByCode(ycclDynaBean, "JGMES_DIC_ADLX", "ADYCCL_ADLXMX_CODE",
                        "ADYCCL_ADLXMX_ID", "ADYCCL_ADLXMX_NAME");

                // 处理产品
                String cpCode = ycclDynaBean.getStr("ADYCCL_CPBH");
                if (cpCode == null || cpCode.isEmpty()) {
                    DynaBean pcDynaBean = getCurrentProductByCxCp(userCode);
                    if (pcDynaBean != null) {
                        cpCode = pcDynaBean.getStr("SCRW_CPBH");
                    }
                    if (cpCode != null && !cpCode.isEmpty()) {
                        DynaBean cpDenaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA",
                                " and PRODUCTDATA_BH='" + cpCode + "'");
                        if (cpDenaBean != null) {
                            ycclDynaBean.set("ADYCCL_CPMC", cpDenaBean.get("PRODUCTDATA_NAME"));
                        }
                    }
                }

                // 处理产线
                String cxCode = ycclDynaBean.getStr("ADYCCL_CXBH");
                DynaBean cxDenaBean = null;
                if (cxCode == null || cxCode.isEmpty()) {
                    cxDenaBean = getCurrentCX(userCode);
                } else {
                    cxDenaBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ", " and CXSJ_CXBM='" + cxCode + "'");
                }
                if (cxDenaBean != null) {
                    ycclDynaBean.set("ADYCCL_CXID", cxDenaBean.get("JGMES_BASE_CXSJ_ID"));
                    ycclDynaBean.set("ADYCCL_CXBH", cxDenaBean.get("CXSJ_CXBM"));
                    ycclDynaBean.set("ADYCCL_CXMC", cxDenaBean.get("CXSJ_CXMC"));
                }
                // 处理工序
                String gxCode = ycclDynaBean.getStr("ADYCCL_GXBH");
                DynaBean gxDenaBean = null;
                if (gxCode == null || gxCode.isEmpty()) {
                    gxDenaBean = getCurrentGX(cpCode, userCode);
                    if (gxDenaBean != null) {
                        ycclDynaBean.set("ADYCCL_GXMC", gxDenaBean.get("GXGL_GXNAME"));
                        ycclDynaBean.set("ADYCCL_GXBH", gxDenaBean.get("GXGL_GXNUM"));
                        ycclDynaBean.set("ADYCCL_GXID", gxDenaBean.get("GXGL_GYLX_GXID"));
                    }
                } else {
                    DynaBean gygxDenaBean = serviceTemplate.selectOne("JGMES_GYGL_GYLXGX",
                            " and GYLXGX_GXNUM='" + gxCode + "'");
                    gxDenaBean = serviceTemplate.selectOne("JGMES_GYGL_GXGL", " and GXGL_GXNUM='" + gxCode + "'");
                    if (gxDenaBean != null) {
                        ycclDynaBean.set("ADYCCL_GXMC", gxDenaBean.get("GXGL_GXNAME"));
                        ycclDynaBean.set("ADYCCL_GXBH", gxDenaBean.get("GXGL_GXNUM"));
                    }
                    if (gygxDenaBean != null) {
                        ycclDynaBean.set("ADYCCL_GXID", gxDenaBean.get("GYLXGX_ID"));
                    }
                }
                // 处理工位
                String gwCode = ycclDynaBean.getStr("ADYCCL_GWBH");
                DynaBean gwDenaBean = null;
                if (gwCode == null || gwCode.isEmpty()) {
                    gwDenaBean = getCurrentGW(userCode);
                } else {
                    gwDenaBean = serviceTemplate.selectOne("JGMES_BASE_GW", " and GW_GWBH='" + gwCode + "'");
                }
                if (gwDenaBean != null) {
                    ycclDynaBean.set("ADYCCL_GWID", gwDenaBean.get("JGMES_BASE_GW_ID"));
                    ycclDynaBean.set("ADYCCL_GWBH", gwDenaBean.get("GW_GWBH"));
                    ycclDynaBean.set("ADYCCL_GWMC", gwDenaBean.get("GW_GWMC"));
                }


                pkValue = ycclDynaBean.getPkValue();
                if (pkValue != null && !pkValue.isEmpty()) {
                    //处理时间
                    String clsj = "";
                    clsj = ycclDynaBean.getStr("ADYCCL_CLSJ");
                    if (clsj == null || clsj.isEmpty()) {
                        ycclDynaBean.set("ADYCCL_CLSJ", jgmesCommon.getCurrentTime());
                    }
                    serviceTemplate.update(ycclDynaBean);
                } else {
                    //提报时间
                    String tbsj = "";
                    tbsj = ycclDynaBean.getStr("ADYCCL_TBSJ");
                    if (tbsj == null || tbsj.isEmpty()) {
                        ycclDynaBean.set("ADYCCL_TBSJ", jgmesCommon.getCurrentTime());
                    }
                    serviceTemplate.insert(ycclDynaBean);
                }

//  			ret =doJsonSaveBgSjAll(jsonString, jsonStrDetail, userCode,JgmesEnumsBgsjLx.GwSj);

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                // TODO: handle exception
                ret.IsSuccess = false;
                ret.setMessage("出错!" + e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);

            toWrite(jsonStr);
        }

    }


    /*
     * 获取产品追溯列表
     */
    public void getScList() {
        String mac = getParamStr("mac");// MAC地址
        String userCode = getParamStr("userCode");// 用户编码
        String barCode = getParamStr("barCode");// 条码

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        String jsonStr = "";
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();

        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                HashMap data = getScList(userCode, barCode);
                if (data != null) {
//    					jsonStr = jsonBuilder.toJson(pcDynaBean);
                    ret.Data = data;
                }

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                ret.setMessage(e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);
            toWrite(jsonStr);
        }
    }

    /*
     * 获取产品追溯列表，子表组装到主表中一起返回
     */
    private HashMap getScList(String userCode, String barCode) {
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        HashMap res = new HashMap<String, Object>();
        List<DynaBean> bgsjList = new ArrayList<DynaBean>();
        List<DynaBean> bgsjzbList = new ArrayList<DynaBean>();
        String sql = "";
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<HashMap> jgmesResult = new JgmesResult<HashMap>();
        String bgId = "";
        String cpCode = "";
        DynaBean cpDynaBean = null;

        if ((barCode != null && !barCode.isEmpty())) {
            bgsjList = serviceTemplate.selectList("JGMES_PB_BGSJ", " and BGSJ_TMH='" + barCode + "' and BGSJ_STATUS_CODE != '2' order by BGSJ_GZSJ");

            if (bgsjList != null && bgsjList.size() > 0) {
                for (int i = 0; i < bgsjList.size(); i++) {
                    DynaBean dynaBean = bgsjList.get(i);
                    if (dynaBean != null) {
                        bgId = dynaBean.getPkValue();
                        if (bgId != null && !bgId.isEmpty()) {
                            bgsjzbList = serviceTemplate.selectList("JGMES_PB_BGSJZB", " and JGMES_PB_BGSJ_ID='" + dynaBean.getPkValue() + "' order by SY_CREATETIME");
                            if (bgsjzbList != null && bgsjzbList.size() > 0) {
                                dynaBean.set("detail", jgmesResult.getValues(bgsjzbList));
                            }
                        }
                        cpCode = dynaBean.getStr("BGSJ_CPBH");
                    }
                }
                res.put("mainData", ret.getValues(bgsjList));
            }
            cpDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " and PRODUCTDATA_BH='" + cpCode + "'");
            if (cpDynaBean != null) {
                res.put("cpData", cpDynaBean.getValues());
            }
        }
        return res;
    }


    /*
     * 获取生产进度列表
     */
    public void getScJdList() {
        String mac = getParamStr("mac");// MAC地址
        String userCode = getParamStr("userCode");// 用户编码
        String cpCode = getParamStr("cpCode");// 产品代码
        String rwztCode = getParamStr("rwztCode");// 生产任务状态
        String gxCode = getParamStr("gxCode");// 工序代码
        String gwCode = getParamStr("gwCode");// 工位代码
        String cxCode = getParamStr("cxCode");// 产线代码
        String scrwNo = request.getParameter("scrwNo");// 生产任务单
        String cpLike = request.getParameter("cpLike");// 产品模糊查询字段(产品编码、产品名称、产品规格)
        String pageSize = request.getParameter("PageSize");// 每页码
        String currPage = request.getParameter("CurrPage");// 当前页
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        String jsonStr = "";

        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                ret = getScJdList(userCode, cpCode, rwztCode, cxCode, scrwNo, cpLike, pageSize, currPage);
            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                ret.setMessage(e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);
            toWrite(jsonStr);
        }
    }


    /*
     * 获取生产进度列表
     */
    private JgmesResult<List<HashMap>> getScJdList(String userCode, String cpCode, String rwztCode, String cxCode, String scrwNo, String cpLike, String pageSize, String currPage) {
        List<DynaBean> scjdList = new ArrayList<DynaBean>();

        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        String sql = "";
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);

        if ((cpCode != null && !cpCode.isEmpty()) || (cxCode != null && !cxCode.isEmpty())) {
            //??
        } else {//按当前产品当前工序资料去获取
            DynaBean cpDynaBean = getCurrentProductByCxCp(userCode);
            if (cpDynaBean != null) {
                cpCode = cpDynaBean.getStr("SCRW_CPBH");
            }
        }
        if (rwztCode != null && !rwztCode.isEmpty()) {
            if (rwztCode.indexOf(",") != -1) {
                //sql += jgmesCommon.getSqlWhere("SCRW_PCZT_CODE", "'"+zt.replaceAll(",", "','")+"'");
                sql += " and SCRW_RWZT_CODE in ('" + rwztCode.replaceAll(",", "','") + "')";
            } else {
                //sql += jgmesCommon.getSqlWhere("SCRW_PCZT_CODE", zt);
                sql += jgmesCommon.getSqlWhere("SCRW_RWZT_CODE", rwztCode);
            }
        }
        //sql +=jgmesCommon.getSqlWhere("SCRW_RWZT_CODE",rwztCode);
        sql += jgmesCommon.getSqlWhere("SCRW_CPBH", cpCode);
        sql += jgmesCommon.getSqlWhere("SCRW_CXBM", cxCode);

        if (scrwNo != null && !"".equals(scrwNo)) {
            //sql +=jgmesCommon.getSqlWhere("SCRW_RWDH",scrwNo);//任务单号
            //sql +=" and SCRW_RWDH like '%"+scrwNo+"%'";//任务单号

            sql += " and (SCRW_RWDH like '%" + scrwNo + "%' or SCRW_GDHM like '%" + scrwNo + "%' or SCRW_DDHM like '%" + scrwNo + "%') ";
        }

        if (cpLike != null && !"".equals(cpLike)) {
            sql += " and (SCRW_CPBH like '%" + cpLike + "%' or SCRW_NAME like '%" + cpLike + "%' or SCRW_CPGG like '%" + cpLike + "%') ";
        }

        sql += " ORDER BY SCRW_JHKGSJ DESC,SCRW_RWDH DESC,SCRW_NAME DESC,SCRW_RWZT_CODE DESC";

        scjdList = serviceTemplate.selectList("JGMES_PLAN_SCRW", sql);
        if (scjdList != null && scjdList.size() > 0) {
            ret.TotalCount = (long) scjdList.size();
        }

        int curr = 0;
        int size = 10;
        try {
            if (pageSize != null && !"".equals(pageSize)) {
                size = Integer.parseInt(pageSize);
            }
            if (currPage != null && !"".equals(currPage)) {
                curr = (Integer.parseInt(currPage) - 1) * size;
            }
        } catch (Exception e) {
            logger.error(JgmesCommon.getExceptionDetail2(e));
            System.out.println("当前页和，每页码的装换成int型失败，请输入正确的当前页和每页码！");
        }

        sql += " LIMIT " + curr + "," + size;
        scjdList = serviceTemplate.selectList("JGMES_PLAN_SCRW", sql);
        if (scjdList != null && scjdList.size() > 0) {
            ret.Data = ret.getValues(scjdList);
        }

        return ret;
    }

    /*
     * 获取产线、工位列表
     */
    public void getCxGwList() {
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        String jsonStr = "";

        try {

            List<DynaBean> cxList = getCXGWList();
            if (cxList != null && cxList.size() > 0) {
//        					jsonStr = jsonBuilder.toJson(pcDynaBean);
                ret.Data = ret.getValues(cxList);
                ret.TotalCount = (long) cxList.size();
            }
        } catch (Exception e) {
            logger.error(JgmesCommon.getExceptionDetail2(e));
            ret.setMessage(e.toString());
        }
        jsonStr = jsonBuilder.toJson(ret);
        toWrite(jsonStr);
    }

    /**
     *
     */
    public void getGwByCxCode() {
        // MAC地址
        String mac = request.getParameter("mac");
        // 用户编码  必填
        String userCode = request.getParameter("userCode");
        // 任务单号
        String cxCode = request.getParameter("cxCode");

        JgmesResult<List<HashMap>> ret = new JgmesResult<>();
        String jsonStr = "";
        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            if (cxCode != null && !"".equals(cxCode)) {
                DynaBean cxDynaBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ", " and CXSJ_CXBM = '" + cxCode + "'");
                if (cxDynaBean != null) {
                    List<DynaBean> gwList = serviceTemplate.selectList("JGMES_BASE_GW",
                            " and JGMES_BASE_CXSJ_ID='" + cxDynaBean.getStr("JGMES_BASE_CXSJ_ID") + "' order by SY_ORDERINDEX ");
                    if (gwList != null && gwList.size() > 0) {
                        ret.Data = ret.getValues(gwList);
                    }

                }
            } else {
                ret.setMessage("未获取到产线编码！");
            }
        }
        jsonStr = jsonBuilder.toJson(ret);
        toWrite(jsonStr);
    }

    /*
     * 获取产线、工位列表
     */
    private List<DynaBean> getCXGWList() {
        List<DynaBean> cxList = new ArrayList<DynaBean>();
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        DynaBean cxDynaBean = new DynaBean();

        List<DynaBean> List = serviceTemplate.selectList("JGMES_BASE_CXSJ", " order by CXSJ_CXMC");
        if (List != null && List.size() > 0) {
            for (int i = 0; i < List.size(); i++) {
                cxDynaBean = List.get(i);
                List<DynaBean> gwList = serviceTemplate.selectList("JGMES_BASE_GW",
                        " and JGMES_BASE_CXSJ_ID='" + cxDynaBean.getStr("JGMES_BASE_CXSJ_ID") + "' order by SY_ORDERINDEX ");//cxDynaBean.getPkValue()
                if (gwList != null && gwList.size() > 0) {
                    cxDynaBean.set("detail", ret.getValues(gwList));
                }
            }

        }
        return List;
    }


    /*
     * 获取产线、工位列表并且过滤掉没有生产任务的产线
     */
    public void getCxGwListFilterOutNoScrw() {
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        String jsonStr = "";

        try {

            List<DynaBean> cxList = getCxGwListFilterOutNoScrws();
            if (cxList != null && cxList.size() > 0) {
//					jsonStr = jsonBuilder.toJson(pcDynaBean);
                ret.Data = ret.getValues(cxList);
                ret.TotalCount = (long) cxList.size();
            }
        } catch (Exception e) {
            logger.error(JgmesCommon.getExceptionDetail2(e));
            ret.setMessage(e.toString());
        }
        jsonStr = jsonBuilder.toJson(ret);
        toWrite(jsonStr);
    }

    /*
     * 获取产线、工位列表
     */
    private List<DynaBean> getCxGwListFilterOutNoScrws() {
        List<DynaBean> cxList = new ArrayList<DynaBean>();
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        DynaBean cxDynaBean = new DynaBean();

        //修改于20190408  刘超
        //List<DynaBean> List = serviceTemplate.selectList("JGMES_BASE_CXSJ", " order by CXSJ_CXMC");
        //过滤掉没有生产任务的产线
        List<DynaBean> List = serviceTemplate.selectListBySql("select * from JGMES_BASE_CXSJ a where (select count(SCRW_CXBM) from JGMES_PLAN_SCRW b where b.SCRW_CXBM = a.CXSJ_CXBM and SCRW_RWZT_CODE !='RWZT03') > 0");
        if (List != null && List.size() > 0) {
            for (int i = 0; i < List.size(); i++) {
                cxDynaBean = List.get(i);
                List<DynaBean> gwList = serviceTemplate.selectList("JGMES_BASE_GW",
                        " and JGMES_BASE_CXSJ_ID='" + cxDynaBean.getStr("JGMES_BASE_CXSJ_ID") + "' order by SY_ORDERINDEX ");//cxDynaBean.getPkValue()
                if (gwList != null && gwList.size() > 0) {
                    cxDynaBean.set("detail", ret.getValues(gwList));
                }
            }

        }
        return List;
    }


    /*
     * 工位电脑绑定
     */
    public void doSaveFirst() {

        String cxCode = request.getParameter("cxCode");// 产线编号
        String gwCode = request.getParameter("gwCode");// 工位编码
        String mac = request.getParameter("mac");// MAC地址

        String tabCode = "JGMES_PB_ADYCCL";
        String pkValue = "";
        String jsonStr = "";
        DynaBean gwjDynaBean = new DynaBean();
        boolean isInsert = true;
        String updateSql = "";

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();

        try {
            if (mac == null || mac.isEmpty()) {
                ret.setMessage("MAC地址不能为空！");
                toWrite(jsonBuilder.toJson(ret));
            } else {
                if (gwCode == null || gwCode.isEmpty()) {
                    ret.setMessage("工位不能为空！");
                } else {
                    List<DynaBean> list = serviceTemplate.selectList("JGMES_EQ_STATIONMACHINE",
                            " and STATIONMACHINE_MACDZ='" + mac + "'");
                    if (list != null && list.size() > 1) {
                        ret.setMessage("MAC地址重复，请在后台处理！");
                        toWrite(jsonBuilder.toJson(ret));
                    } else {
                        if (list != null && list.size() == 1) {
                            gwjDynaBean = list.get(0);
                            isInsert = false;
                        }

                        // 确保工位的唯一性
                        List<DynaBean> gwjList = serviceTemplate.selectList("JGMES_EQ_STATIONMACHINE",
                                " and STATIONMACHINE_GWBH='" + gwCode + "' and STATIONMACHINE_MACDZ<>'" + mac + "'");
                        if (gwjList != null && gwjList.size() > 0) {
                            ret.setMessage("该工位已被工位电脑[" + gwjList.get(0).getStr("STATIONMACHINE_MACDZ") + "]绑定！");
                            toWrite(jsonBuilder.toJson(ret));
                        } else {
                            gwjDynaBean.setStr("STATIONMACHINE_MACDZ", mac);

                            // 设置工位信息
                            DynaBean gwDynaBean = serviceTemplate.selectOne("JGMES_BASE_GW",
                                    " and GW_GWBH='" + gwCode + "'");
                            if (gwDynaBean != null) {
                                // gwjDynaBean.set("JGMES_BASE_CXSJ_ID", gwDynaBean.getPkValue());
                                gwjDynaBean.set("STATIONMACHINE_GWBH", gwDynaBean.getStr("GW_GWBH"));
                                gwjDynaBean.set("STATIONMACHINE_GWMC", gwDynaBean.getStr("GW_GWMC"));
                                if (cxCode == null || cxCode.isEmpty()) {
                                    cxCode = gwDynaBean.getStr("GW_CXBH");
                                }
                                // 设置产线信息
                                DynaBean cxDynaBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ",
                                        " and CXSJ_CXBM='" + cxCode + "'");
                                if (cxDynaBean != null) {
                                    gwjDynaBean.set("JGMES_BASE_CXSJ_ID", cxDynaBean.getPkValue());
                                    gwjDynaBean.set("STATIONMACHINE_CXBM", cxDynaBean.getStr("CXSJ_CXBM"));
                                    gwjDynaBean.set("STATIONMACHINE_CXMC", cxDynaBean.getStr("CXSJ_CXMC"));
                                }
                            }

                            if (isInsert) {// 插入
                                // 工位机编号
                                // serviceTemplate.buildCode("STATIONMACHINE_GWJBM", "JGMES_EQ_STATIONMACHINE",
                                // gwjDynaBean);
                                gwjDynaBean.set("STATIONMACHINE_GWJBM", serviceTemplate.buildCode("STATIONMACHINE_GWJBM",
                                        "JGMES_EQ_STATIONMACHINE", gwjDynaBean));
                                gwjDynaBean.setStr("STATIONMACHINE_GWJMC", "系统生成");

                                jgmesCommon.setDynaBeanDic(gwjDynaBean, "JGMES_USESTATUS", "0", null,
                                        "STATIONMACHINE_USESTATUS_CODE", "STATIONMACHINE_USESTATUS_NAME");
                                jgmesCommon.setDynaBeanDic(gwjDynaBean, "JGMES_YES_NO", "0", null, "STATIONMACHINE_SFYDY",
                                        null);

                                gwjDynaBean.set(beanUtils.KEY_TABLE_CODE, "JGMES_EQ_STATIONMACHINE");

                                jgmesCommon.setDynaBeanInfo(gwjDynaBean);

                                serviceTemplate.insert(gwjDynaBean);
                            } else {// 更新
                                // pcServiceTemplate.executeSql("");
                                updateSql = jgmesCommon.getUpdateSql(gwjDynaBean);
                                pcServiceTemplate.executeSql(updateSql);
                                // serviceTemplate.update(gwjDynaBean);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(JgmesCommon.getExceptionDetail2(e));
            // TODO: handle exception
            ret.IsSuccess = false;
            ret.setMessage("出错!" + e.toString());
        }
        jsonStr = jsonBuilder.toJson(ret);

        toWrite(jsonStr);

    }

    /*
     * 获取工序打印的基础信息
     */
    public void getGxPrintInfo() {
        String userCode = request.getParameter("userCode");// 用户编号
        String mac = request.getParameter("mac");// MAC地址
        String cpCode = request.getParameter("cpCode");// 产品编码

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
        String jsonStr = "";

        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            try {

                DynaBean yyggDynaBean = getGxPrintInfo(userCode, mac, cpCode);
                if (yyggDynaBean != null) {
                    ret.Data = yyggDynaBean.getValues();
                }
            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                ret.setMessage(e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);
            toWrite(jsonStr);
        }
    }

    /*
     * 获取工序打印的基础信息
     */
    private DynaBean getGxPrintInfo(String userCode, String mac, String cpCode) {
        DynaBean yyggDynaBean = new DynaBean();
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        DynaBean gwjDynaBean = new DynaBean();// 工位机
        String yyggId = "";// 应用规则ID
        String tmLxId = "";// 条码类型ID

        if (cpCode == null || cpCode.isEmpty()) {
            DynaBean scrwDynaBean = getCurrentProductByCxCp(userCode);
            cpCode = scrwDynaBean.getStr("SCRW_CPBH");
        }

        gwjDynaBean = getCurrentGwj(userCode);
        if (gwjDynaBean != null) {
            yyggId = gwjDynaBean.getStr("JGMES_BASE_CPTMYYGG_ID");
            tmLxId = gwjDynaBean.getStr("STATIONMACHINE_TMLX_ID");
            if (yyggId != null && !yyggId.isEmpty()) {
                yyggDynaBean = serviceTemplate.selectOneByPk("JGMES_BASE_CPTMYYGG", yyggId);
            } else {
                if (tmLxId != null && !tmLxId.isEmpty()) {
                    List<DynaBean> list = serviceTemplate.selectList("JGMES_BASE_CPTMYYGG", " and CPTMYYGG_CPBH='" + cpCode + "' and CPTMYYGG_TMLX_ID='" + tmLxId + "'");
                    if (list != null && list.size() > 0) {
                        yyggDynaBean = list.get(0);
                    }
                }
            }
        }

        return yyggDynaBean;
    }

    /*
     * 获取工位机
     */
    private DynaBean getCurrentGwj(String userCode) {
        if (userCode == null || userCode.isEmpty()) {
            userCode = getCurrentUserCode(null);
        }
        DynaBean gwDynaBean = getCurrentGW(userCode);
        DynaBean gwjDynaBean = new DynaBean();
        if (gwDynaBean != null) {
            gwjDynaBean = serviceTemplate.selectOne("JGMES_EQ_STATIONMACHINE",
                    " and STATIONMACHINE_GWBH='" + gwDynaBean.getStr("GW_GWBH") + "'");
        }
        return gwjDynaBean;
    }


    /*
     * 获取工序打印的基础信息
     */
    public void getPrintInfo() {
        String userCode = request.getParameter("userCode");// 用户编号
        String mac = request.getParameter("mac");// MAC地址
        String printQty = request.getParameter("printQty");// 打印数理
        String barCodes = request.getParameter("barCodes");// 条码集合
        String cpCode = request.getParameter("cpCode");// 产品编码

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
        String jsonStr = "";

        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            try {

                ret = getPrintInfo(userCode, mac, printQty, barCodes, cpCode);

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                ret.setMessage(e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);
            toWrite(jsonStr);
        }
    }

    /*
     * 获取工序打印的基础信息
     * printQty－－打印份数，barCodes－－传入的值，可以是条码集合，也可以是需要打印的条码，cpCode－－产品编号
     */
    private JgmesResult<HashMap> getPrintInfo(String userCode, String mac, String printQty, String barCodes, String cpCode) {
        JgmesResult<HashMap> res = new JgmesResult<HashMap>();
        HashMap data = new HashMap();
        DynaBean yyggDynaBean = new DynaBean();
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        DynaBean gwjDynaBean = getCurrentGwj(userCode);// 工位机
        String printTFilePath = "";// 打印模板文件地址，相对路径。
        String bqcs = "";// 条码标签参数模板
        String printParams = "";// 最终的打印命令
        List<DynaBean> gdcptmList = new ArrayList<DynaBean>();
        DynaBean gdcptmDynaBean = new DynaBean();
        DynaBean gdcptmDynaBean_jg = new DynaBean();
        String scrwID = "";// 生产任务ID
        String tm = "";// 条码
        String inBarcode = "";// 传入的首个条码
        String strUrlPre = request.getScheme() + "://" + request.getServerName();
        JSONObject json = new JSONObject();
        JSONArray jsonArr = new JSONArray();
        String sqlWhere = "";
        String tmscfs = "";
        boolean isCreateByIn = false;// 是否按传入值生成

        if (request.getServerPort() != 80) {
            strUrlPre += ":" + request.getServerPort();
        }

        if (printQty == null || printQty.isEmpty()) {//打印份数，为空默认打印1份
            printQty = "1";
        }
        if (gwjDynaBean != null) {
            data.put("Address", gwjDynaBean.getStr("STATIONMACHINE_DYJJKDZ"));//从工位电脑配置中获取打印地址
        }
        if (barCodes != null && !barCodes.isEmpty()) {
            if (barCodes.indexOf(",") != -1) {
                inBarcode = barCodes.substring(0, barCodes.indexOf(",") - 1);
            } else {
                inBarcode = barCodes;
            }
        }
        System.out.println("######################################################:开始条码打印");
        yyggDynaBean = getGxPrintInfo(userCode, mac, cpCode);
        if (yyggDynaBean != null) {
            tmscfs = yyggDynaBean.getStr("CPTMYYGG_BARCODEMODEL_CODE");
            isCreateByIn = tmscfs != null && tmscfs.equalsIgnoreCase(JgmesEnumsDic.CreateByIn.getKey());
            printTFilePath = yyggDynaBean.getStr("CPTMYYGG_BQMB");
            bqcs = yyggDynaBean.getStr("CPTMYYGG_BQCS");
            System.out.println(bqcs + "-----bqcs");

            String zxQty = yyggDynaBean.getStr("CPTMYYGG_MTMSL"); //获取每条码数量

            if (bqcs != null && !bqcs.isEmpty()) {// 标签参数不为空

                DynaBean scrwDynaBean = getScrwByBarcode(inBarcode, userCode, null, null);
                System.out.println("######################################################:获取生产任务");
                if (!isCreateByIn) {// 根据条码生成方式判断，如果是按条码规则生成则需更新打印标记
                    if (scrwDynaBean != null) {
                        System.out.println("######################################################:获取生产任务成功！");
                        scrwID = scrwDynaBean.getStr("JGMES_PLAN_SCRW_ID");
                        System.out.println("######################################################:生产任务ID:" + scrwID);
                        sqlWhere = " and GDCPTM_SFYDY=0 and GDCPTM_CPBH='" + cpCode + "' and JGMES_PLAN_SCRW_ID='"
                                + scrwID + "' and GDCPTM_TMLX_CODE='" + yyggDynaBean.getStr("CPTMYYGG_TMLX_CODE")
                                + "' order by GDCPTM_TMH";
//						if (isCreateByIn) {
//							sqlWhere = " and GDCPTM_CPBH='" + cpCode + "' and JGMES_PLAN_SCRW_ID='" + scrwID
//									+ "' order by GDCPTM_TMH";
//						}
                        System.out.println("######################################################:生产任务sql:" + sqlWhere);
                        // 获取最小的那条条码记录
                        gdcptmList = serviceTemplate.selectList("JGMES_BASE_GDCPTM", sqlWhere, 0, 1);
                        if (gdcptmList != null && gdcptmList.size() > 0) {
                            // 将该条条码更改成已打印
                            gdcptmDynaBean = gdcptmList.get(0);
                            gdcptmDynaBean.set("GDCPTM_SFYDY", 1);
                            gdcptmDynaBean_jg = serviceTemplate.update(gdcptmDynaBean);

                            tm = gdcptmDynaBean_jg.getStr("GDCPTM_TMH");
                        } else {
                            res.setMessage("没有需要打印的条码，请检查是否还有已生成未打印的条码！");
                            System.out.println("没有需要打印的条码！" + sqlWhere);
                        }
                    } else {
                        res.setMessage("未获取到当前工位的生产任务！");
                        System.out.println("生产任务单号为空！");
                    }

                } else {//按传入值生成
                    tm = inBarcode;
                }

                System.out.println("当前要打印的条码是：" + tm);
                data.put("BarCode", tm);
                printParams = bqcs;
                System.out.println("######################################################:开始条码打印：" + printParams);
                // 获取对应规则的大表数据
//						String str = "{'FilePath':'@filePath@','ColQty':'@colQty@','Copies':'@copies@','cpName':'@JGMES_BASE_PRODUCTDATA.PRODUCTDATA_NAME@','cpCode':'@JGMES_BASE_PRODUCTDATA.PRODUCTDATA_CPFL_NAME@','BatchTmList':'@BatchTmList@','Tm1':'@tm@','TmList':[@tmList@]}";
                String cpbh = yyggDynaBean.getStr("CPTMYYGG_CPBH");// 获取从表的产品编号
                DynaBean productData = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA",
                        " AND  PRODUCTDATA_BH='" + cpbh + "'");// 根据从表的产品编号是主表的外键，得出主表的DynaBean
                List<String> list = new ArrayList<String>();
                String regex = "@[^\\.@]*\\.[^@\\.]*@";// 获取这种格式的字符串：@JGMES_BASE_PRODUCTDATA.PRODUCTDATA_NAME@
                Pattern pattern = Pattern.compile(regex);// 根据正则表达式截取字符串
                Matcher m = pattern.matcher(printParams);
                while (m.find()) {
                    int i = 1;
                    list.add(m.group(i - 1));
                    i++;
                }
                if (!list.isEmpty() || list.size() != 0) {
                    for (String match : list) {//循环所有的 字段
//					        	System.out.println(string);//例子：@JGMES_BASE_PRODUCTDATA.PRODUCTDATA_NAME@
                        String s = match.substring(1, match.length() - 1);// JGMES_BASE_PRODUCTDATA.PRODUCTDATA_NAME
                        s = s.replaceAll(" ", "");//去除字符串中的空格
                        String[] split = s.split("\\.");// split[0]=JGMES_BASE_PRODUCTDATA，split[1]=PRODUCTDATA_NAME
                        String tabName = split[0];
                        String colName = split[1];
                        if (tabName.equalsIgnoreCase("JGMES_BASE_PRODUCTDATA"))// 物料表时
                        {
                            String st = productData.getStr(colName);// 获取主表的对应列名数据
                            if (st != null && !"".equals(st)) {
                                printParams = printParams.replaceAll(match, st);// 把@JGMES_BASE_PRODUCTDATA.PRODUCTDATA_NAME@替换成读取的数据
                            }
                        } else if (tabName.equalsIgnoreCase("JGMES_PLAN_SCRW")) {// 当前生产任务表时
                            if (scrwDynaBean != null) {
                                printParams = printParams.replaceAll(match, scrwDynaBean.getStr(colName));    //替换数据
                            }
                        } else if (tabName.equalsIgnoreCase("JGMES_PLAN_GDLB")) {// 当前工单表时
                            if (scrwDynaBean != null) {
                                String gdhm = scrwDynaBean.getStr("SCRW_GDHM");
                                List<DynaBean> gdhmDynaBeanList = serviceTemplate.selectList("JGMES_PLAN_GDLB", " and GDLB_GDHM='" + gdhm + "'");
                                if (gdhmDynaBeanList != null && gdhmDynaBeanList.size() > 0) {
                                    DynaBean gdDynaBean = gdhmDynaBeanList.get(0);
                                    printParams = printParams.replaceAll(match, gdDynaBean.getStr(colName));//替换数据
                                }
                            }
                        }
                    }
                }
                //替换装箱数量
                if (zxQty != null && !zxQty.isEmpty() && printParams.indexOf("@zxQty@") != -1) {
                    printParams = printParams.replaceAll("@zxQty@", zxQty);// 装箱数量
                }
                //替换装箱数量  @barCodes@
                if (barCodes != null && !barCodes.isEmpty() && printParams.indexOf("@barCodes@") != -1) {
                    printParams = printParams.replaceAll("@barCodes@", barCodes);// 条码list
                }
                if (printTFilePath != null && !printTFilePath.isEmpty()) {//模板文件 地址
                    if (printTFilePath.indexOf("*") != -1) {
                        printTFilePath = printTFilePath.substring(printTFilePath.indexOf("*") + 1);
                    }
                    printTFilePath = strUrlPre + printTFilePath;// 模板文件的绝对路径
                    printParams = printParams.replaceAll("@filePath@", printTFilePath);// 模板文件相对路径
                }
                if (printQty != null && !printQty.isEmpty()) {
                    printParams = printParams.replaceAll("@copies@", printQty);// 打印份数
                }
                if (tm != null && !tm.isEmpty()) {
                    printParams = printParams.replaceAll("@tm@", tm);// 从数据库中动态获取的条码
                }
                if (barCodes != null && !barCodes.isEmpty() && !isCreateByIn) {// 要加判断，按条码生成规则时才替换@tmList@

                    barCodes = "\"" + barCodes.replaceAll(",", "\",\"") + "\"";
                    printParams = printParams.replaceAll("@tmList@", barCodes);// 从数据库中动态获取的条码

                }
                // 将没数据的替换成默认值
                if (printParams.indexOf("@colQty@") != -1) {
                    printParams = printParams.replaceAll("@colQty@", "0");
                }
                if (printParams.indexOf("@copies@") != -1) {
                    printParams = printParams.replaceAll("@copies@", "1");
                }
                System.out.println("######################################################:开始条码打印2：" + printParams);
                printParams = printParams.replaceAll("@[^@]*@", "");

                data.put("PrintParams", printParams);

            } else {
                res.setMessage("条码应用规则里的模板参数为空！");
                System.out.println("模板参数[CPTMYYGG_BQCS]为空！");
            }
        } else {
            res.setMessage("未获取到对应的条码应用规则！");
            System.out.println("未获取到对应的条码应用规则！");
        }

        res.Data = data;
        return res;
    }

    /*
     * 校验用户合法性，不合法直接提示。
     */
    private JgmesResult<String> doCheck(String userCode, String mac) {
        JgmesResult<String> result = new JgmesCommon(request, serviceTemplate).doCheckRes(mac, userCode);
        if (!result.IsSuccess) {
            toWrite(jsonBuilder.toJson(result));
        }
        return result;
    }

    /*
     * 校验物料，如果是唯一码则做唯一性检验
     */
    public void doCheckWlm() {
        String mac = getParamStr("mac");//MAC地址
        String userCode = getParamStr("userCode");//用户
        String barCode = request.getParameter("barCode");//物料条码
        String wlCode = request.getParameter("wlCode");//物料码
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);

        JgmesResult<String> res = jgmesCommon.doCheckWlm(barCode, wlCode);

        toWrite(jsonBuilder.toJson(res));
    }

    /*
     * 校验物料唯一性
     */
    public void doCheckWlmUniq() {
        String mac = getParamStr("mac");//MAC地址
        String userCode = getParamStr("userCode");//用户
        String barCode = request.getParameter("barCode");//物料条码
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);

        JgmesResult<String> res = jgmesCommon.doCheckWlmUniq(barCode);

        toWrite(jsonBuilder.toJson(res));
    }

    /*
     * 校检是否换产，如换产返回新产品、新工序及工序项目、工序物料项目等
     */
    public void doCheckChangeCp() {
        String mac = getParamStr("mac");//MAC地址
        String userCode = getParamStr("userCode");//用户
        String cpCode = request.getParameter("cpCode");//产品编号
        String barCode = request.getParameter("barCode");//主条码
        String gygxId = request.getParameter("gygxId");//工艺工序ID
        String scrwId = request.getParameter("scrwId");//生产任务ID

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        JgmesResult<String> jgmesResult = new JgmesResult<String>();
        String currentGxId = "";
        // 校检合法性，是否多地登陆
        jgmesResult = doCheck(userCode, mac);

        try {
            if (jgmesResult.IsSuccess) {
                JgmesResult<HashMap> ret = doCheckChangeCp(userCode, barCode, cpCode, gygxId, scrwId);
                if (ret.IsSuccess) {
                    DynaBean gxDynaBean = getCurrentGX(cpCode, userCode);
                    if (gxDynaBean != null) {
                        currentGxId = gxDynaBean.getStr("GXGL_GYLX_GXID");
                        jgmesResult = jgmesCommon.doCheckSj(barCode, currentGxId);
                    } else {
                        jgmesResult = jgmesCommon.doCheckSj(barCode, "");
                        //					jgmesResult.setMessage("获取当前工序出错！");
                    }
                    toWrite(jsonBuilder.toJson(jgmesResult));
                } else {
                    toWrite(jsonBuilder.toJson(ret));
                }

            } else {
                toWrite(jsonBuilder.toJson(jgmesResult));
            }
        } catch (Exception e) {
            logger.error(JgmesCommon.getExceptionDetail2(e));
            // TODO: handle exception
            jgmesResult.setMessage("校验是否换产出现未知错误，请联系管理员！" + e.toString());
            toWrite(jsonBuilder.toJson(jgmesResult));
        }
    }

    /*
     * 校验是否换产
     */
    private JgmesResult<HashMap> doCheckChangeCp(String userCode, String barCode, String cpCode, String gygxId, String scrwId) {
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        String cpNewCode = "";//jgmesCommon.getCpCodeByBarCode(barCode);
        JgmesBarCodeBase jgmesBarCodeBase = null;

        if (gygxId == null || gygxId.isEmpty()) {
            DynaBean gxDynaBean = getCurrentGX(cpCode, userCode);
            gygxId = gxDynaBean.getStr("GXGL_GYLX_GXID");
        }

        JgmesResult<JgmesBarCodeBase> jgmesResult = jgmesCommon.getTmLxByBarCode(barCode, cpCode, gygxId);
        if (jgmesResult.IsSuccess) {
            jgmesBarCodeBase = jgmesResult.Data;
            if (jgmesBarCodeBase.TmLx != null && jgmesBarCodeBase.TmLx.getKey().equals(JgmesEnumsDic.TMCP.getKey())) {

                //获取当前任务单号
                String newScrwId = "";
                if (jgmesBarCodeBase.tmScDynaBean != null && jgmesBarCodeBase.tmScDynaBean.get("JGMES_PLAN_SCRW_ID") != null) {
                    newScrwId = jgmesBarCodeBase.tmScDynaBean.get("JGMES_PLAN_SCRW_ID").toString();
                }
                logger.error("newScrwId:" + newScrwId + ",scrwId:" + scrwId);

                cpNewCode = jgmesCommon.getCpCodeByBarCode(barCode);
                if (cpCode != null && !cpCode.equalsIgnoreCase(cpNewCode)) {//
                    System.out.println("传入的产品编号[" + cpCode + "]，与通过条码[" + barCode + "]获取 的产品编号[" + cpNewCode + "]不相同！");
                    ret.setErrorDic(JgmesEnumsErr.CheckErrChange);
//	   	    		ret.ErrorCode = JgmesEnumsErr.CheckErrNoMatch.getKey();
//	   	    		ret.setMessage("条码与产品不匹配！");
                    //根据条码获取相应生产任务ID，再获取对应排产单信息，再根据产品、当前工位获取当前工序、工序项目、工序物料、不良项目等
                    ret.Data = getCurrentGxAllInfoByBarCode(userCode, null, cpNewCode);
                } else if (newScrwId != null && !"".equals(newScrwId) && scrwId != null && !"".equals(scrwId) && !newScrwId.equals(scrwId)) {
                    System.out.println("传入的生产任务[" + scrwId + "]，与通过条码[" + barCode + "]获取 的生产任务[" + newScrwId + "]不相同！");
                    ret.setErrorDic(JgmesEnumsErr.CheckErrChange);
//	   	    		ret.ErrorCode = JgmesEnumsErr.CheckErrNoMatch.getKey();
//	   	    		ret.setMessage("条码与产品不匹配！");
                    //根据条码获取相应生产任务ID，再获取对应排产单信息，再根据产品、当前工位获取当前工序、工序项目、工序物料、不良项目等
                    ret.Data = getCurrentGxAllInfoByBarCode(userCode, null, cpNewCode);
                }
            } else {
                //其它类型的校验
            }
        } else {//获取不到条码的类型
            ret.setMessage(jgmesResult.getMessage());
        }
        return ret;
    }

    /**
     * 根据条码获取相应生产任务ID，再获取对应排产单信息，再根据产品、当前工位获取当前工序、工序项目、工序物料、不良项目等
     *
     * @param userCode 用户编码
     * @param barCode  条码
     * @param cpCode   产品编码
     * @return
     */
    private HashMap<String, Object> getCurrentGxAllInfoByBarCode(String userCode, String barCode, String cpCode) {
        HashMap<String, Object> res = new HashMap<String, Object>();
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        if (barCode != null && !barCode.isEmpty()) {
            cpCode = jgmesCommon.getCpCodeByBarCode(barCode);
        }

        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();

        //根据条码获取相应生产任务ID，再获取对应排产单信息，再根据产品、当前工位获取当前工序、工序项目、工序物料、不良项目等
        DynaBean gwDynaBean = getCurrentGW(userCode);
        if (gwDynaBean != null) {
            DynaBean scrwDynaBean = getScrwByBarcode(barCode, userCode, gwDynaBean.getStr("JGMES_BASE_CXSJ_ID"), null);//先根据条码获取对应报工数据首道工序的生产任务，如为首道工序而根据当前产线指定的生产任务
            if (scrwDynaBean != null) {
                res.put("ScrwInfo", scrwDynaBean.getValues());
            }
        }

        DynaBean gxDynaBean = getCurrentGX(cpCode, userCode);
        if (gxDynaBean != null) {
            res.put("GxInfo", gxDynaBean.getValues());
        }
        String gxId = gxDynaBean.getPkValue();
        List<DynaBean> gxxmList = getGXXMList(cpCode, gxId, "", userCode);
        if (gxxmList != null && gxxmList.size() > 0) {
            res.put("GxXmInfo", ret.getValues(gxxmList));
        }
        List<DynaBean> gxblxmList = getGXBLXMList(cpCode, gxId, "", userCode);
        if (gxblxmList != null && gxblxmList.size() > 0) {
            res.put("GxBlXmInfo", ret.getValues(gxblxmList));
        }
        List<HashMap> gxwlxmList = getGXWLXMList(cpCode, gxId, "", userCode);
        if (gxwlxmList != null && gxwlxmList.size() > 0) {
            res.put("GxWlInfo", gxwlxmList);
        }

        return res;
    }

    /*
     * 获取下一道工位id,工位名称,工位编号
     * 参数：参品编号，工艺路线id,工序id
     * 修改，查这个表字需要工序ID都行了
     */
    private DynaBean nextGWbean(String cpbh, String cpgylxid, String gxid) {
        //	DynaBean selectOne = serviceTemplate.selectOne("JGMES_BASE_CPGWGX"," AND CPGWGX_CPBH='"+cpbh+"' AND CPGWGX_CPGYLXID='"+cpgylxid+"' AND CPGWGX_GXID='"+gxid+"'");
        DynaBean selectOne = serviceTemplate.selectOne("JGMES_BASE_CPGWGX", " AND CPGWGX_CPBH='" + cpbh + "' AND CPGWGX_GXID='" + gxid + "'");
        return selectOne;
    }

    /*
     * 插入产品生产明细情况表
     * lxc
     * 参数1：产品生产明细情况bean
     * 参数2：报工数据bean
     * 参数3：工艺流程bean
     * 参数4：tmType：条码类型字段
     * 参数5：listsize ：第几道工序
     */
    private void evalDynaBean(DynaBean JGMES_PB_SCDETAIL, DynaBean bgsjDynaBean, List<DynaBean> jgmesGYLXGX, String tmType, int listsize) {
        //当前时间
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String scrq = formatter.format(new java.util.Date());
        if (listsize == 1) {
            //通用
            JGMES_PB_SCDETAIL.set(tmType, bgsjDynaBean.getStr("BGSJ_TMH"));//条码类型
            JGMES_PB_SCDETAIL.set("SCDETAIL_TM", bgsjDynaBean.getStr("BGSJ_TMH"));
            JGMES_PB_SCDETAIL.set("SCDETAIL_CPBH", bgsjDynaBean.getStr("BGSJ_CPBH"));
            JGMES_PB_SCDETAIL.set("SCDETAIL_CPMC", bgsjDynaBean.getStr("BGSJ_CPMC"));
            JGMES_PB_SCDETAIL.set("GYLX_ID", bgsjDynaBean.getStr("GYLX_ID"));//工艺路线
            //JGMES_PB_SCDETAIL.set("JGMES_BASE_CXSJ_ID", (String) bgsjDynaBean.get("BGSJZB_GXMC"));//JGMES_BASE_CXSJ_ID产线数据_外键ID
            JGMES_PB_SCDETAIL.set("SCDETAIL_JSBJ", 0);//结束标志
            JGMES_PB_SCDETAIL.set("SCDETAIL_DYDGXTM", bgsjDynaBean.getStr("BGSJ_TMH"));//第一道工序
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGXID", bgsjDynaBean.getStr("BGSJ_GXID"));//当前工序ID
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGXBH", bgsjDynaBean.getStr("BGSJ_GXBH"));//当前工序编号
            DynaBean nextGWbean = null;
            if (jgmesGYLXGX != null && jgmesGYLXGX.size() > 1) {
                JGMES_PB_SCDETAIL.set("SCDETAIL_DQGYLX_GX_WJID", jgmesGYLXGX.get(listsize - 1).getStr("GYLXGX_GYLXGXID"));//当前工艺路线_工序_外键ID
                JGMES_PB_SCDETAIL.set("SCDETAIL_GXLX_ID", jgmesGYLXGX.get(listsize - 1).getStr("GYLXGX_GXLX_ID"));//工序类型_ID
                nextGWbean = nextGWbean(bgsjDynaBean.getStr("BGSJ_CPBH"), bgsjDynaBean.getStr("GYLX_ID"), jgmesGYLXGX.get(listsize).getStr("GYLXGX_GXID"));
            }

            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGXMC", bgsjDynaBean.getStr("BGSJ_GXMC"));//当前工序名称

            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGWBH", bgsjDynaBean.getStr("BGSJ_GWBH"));//当前工位编号
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGWMC", bgsjDynaBean.getStr("BGSJ_GWMC"));//当前工位名称
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGWID", bgsjDynaBean.getStr("BGSJ_GWID"));//当前工位ID

            //下
            if (nextGWbean != null) {
                JGMES_PB_SCDETAIL.set("SCDETAIL_XDGWID", nextGWbean.getStr("JGMES_BASE_GW_ID"));
                JGMES_PB_SCDETAIL.set("SCDETAIL_XDGWBH", nextGWbean.getStr("CPGWGX_GWBH"));
                JGMES_PB_SCDETAIL.set("SCDETAIL_XDGWMC", nextGWbean.getStr("CPGWGX_GWMC"));
            }
            if (jgmesGYLXGX != null && jgmesGYLXGX.size() > 1) {
                JGMES_PB_SCDETAIL.set("SCDETAIL_XDGXID", jgmesGYLXGX.get(listsize).getStr("GYLXGX_GXID"));
                JGMES_PB_SCDETAIL.set("SCDETAIL_XDGXBH", jgmesGYLXGX.get(listsize).getStr("GYLXGX_GXNUM"));
                JGMES_PB_SCDETAIL.set("SCDETAIL_XDGXMC", jgmesGYLXGX.get(listsize).getStr("GYLXGX_GXNAME"));
            }
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGXSZXL", listsize);
        }
        if (listsize == jgmesGYLXGX.size()) {
            JGMES_PB_SCDETAIL.set(tmType, bgsjDynaBean.getStr("BGSJ_TMH"));//条码类型
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGXID", bgsjDynaBean.getStr("BGSJ_GXID"));//当前工序ID
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGXBH", bgsjDynaBean.getStr("BGSJ_GXBH"));//当前工序编号
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGYLX_GX_WJID", jgmesGYLXGX.get(listsize - 1).getStr("GYLXGX_GYLXGXID"));//当前工艺路线_工序_外键ID
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGXMC", bgsjDynaBean.getStr("BGSJ_GXMC"));//当前工序名称
            JGMES_PB_SCDETAIL.set("SCDETAIL_GXLX_ID", jgmesGYLXGX.get(listsize - 1).getStr("GYLXGX_GXLX_ID"));//工序类型_ID
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGWBH", bgsjDynaBean.getStr("BGSJ_GWBH"));//当前工位编号
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGWMC", bgsjDynaBean.getStr("BGSJ_GWMC"));//当前工位名称
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGWID", bgsjDynaBean.getStr("BGSJ_GWID"));//当前工位ID
            JGMES_PB_SCDETAIL.set("SCDETAIL_JSSJ", scrq);
            //下----最后一道工序传空
            JGMES_PB_SCDETAIL.set("SCDETAIL_XDGXID", "");// jgmesGYLXGX.get(listsize+1).getStr("GYLXGX_GXID")
            JGMES_PB_SCDETAIL.set("SCDETAIL_XDGXBH", "");//jgmesGYLXGX.get(listsize+1).getStr("GYLXGX_GXNUM")
            JGMES_PB_SCDETAIL.set("SCDETAIL_XDGXMC", "");//jgmesGYLXGX.get(listsize+1).getStr("GYLXGX_GXNAME")
            JGMES_PB_SCDETAIL.set("SCDETAIL_XDGWMC", "");
            JGMES_PB_SCDETAIL.set("SCDETAIL_XDGWBH", "");
            JGMES_PB_SCDETAIL.set("SCDETAIL_XDGWID", "");
            JGMES_PB_SCDETAIL.set("SCDETAIL_JSBJ", 1);//结束标志
            JGMES_PB_SCDETAIL.set("SCDETAIL_JSSJ", scrq);
            String kssj = JGMES_PB_SCDETAIL.getStr("SCDETAIL_KSSJ");
            int hh = 0;
            if (kssj != null && !kssj.isEmpty()) {
                long startT = fromDateStringToLong(kssj); //开始时间
                long endT = fromDateStringToLong(scrq);  //结束时间
                long ss = (endT - startT) / (1000); //共计秒数
                int MM = (int) ss / 60;   //共计分钟数
                hh = (int) ss / 3600;  //共计小时数
                //int dd=(int)hh/24;   //共计天数
            }
            JGMES_PB_SCDETAIL.set("SCDETAIL_ZSC", hh);//总时长
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGXSZXL", listsize);
        }
        if (listsize < jgmesGYLXGX.size() && listsize != 1) {

            String BGSJ_TMH = bgsjDynaBean.getStr("BGSJ_TMH");

            JGMES_PB_SCDETAIL.set(tmType, BGSJ_TMH);//条码类型
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGXID", bgsjDynaBean.getStr("BGSJ_GXID"));//当前工序ID
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGXBH", bgsjDynaBean.getStr("BGSJ_GXBH"));//当前工序编号
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGYLX_GX_WJID", jgmesGYLXGX.get(listsize - 1).getStr("GYLXGX_GYLXGXID"));//当前工艺路线_工序_外键ID
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGXMC", bgsjDynaBean.getStr("BGSJ_GXMC"));//当前工序名称
            JGMES_PB_SCDETAIL.set("SCDETAIL_GXLX_ID", jgmesGYLXGX.get(listsize - 1).getStr("GYLXGX_GXLX_ID"));//工序类型_ID
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGWBH", bgsjDynaBean.getStr("BGSJ_GWBH"));//当前工位编号
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGWMC", bgsjDynaBean.getStr("BGSJ_GWMC"));//当前工位名称
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGWID", bgsjDynaBean.getStr("BGSJ_GWID"));//当前工位ID
            JGMES_PB_SCDETAIL.set("SCDETAIL_JSSJ", scrq);
            //下
            JGMES_PB_SCDETAIL.set("SCDETAIL_XDGXID", jgmesGYLXGX.get(listsize).getStr("GYLXGX_GXID"));
            JGMES_PB_SCDETAIL.set("SCDETAIL_XDGXBH", jgmesGYLXGX.get(listsize).getStr("GYLXGX_GXNUM"));
            JGMES_PB_SCDETAIL.set("SCDETAIL_XDGXMC", jgmesGYLXGX.get(listsize).getStr("GYLXGX_GXNAME"));
            DynaBean nextGWbean = nextGWbean(bgsjDynaBean.getStr("BGSJ_CPBH"), bgsjDynaBean.getStr("GYLX_ID"), jgmesGYLXGX.get(listsize).getStr("GYLXGX_GXID"));
            //下
            JGMES_PB_SCDETAIL.set("SCDETAIL_XDGWID", nextGWbean.get("JGMES_BASE_GW_ID"));
            JGMES_PB_SCDETAIL.set("SCDETAIL_XDGWBH", nextGWbean.get("CPGWGX_GWBH"));
            JGMES_PB_SCDETAIL.set("SCDETAIL_XDGWMC", nextGWbean.get("CPGWGX_GWMC"));

            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGXSZXL", listsize);
            JGMES_PB_SCDETAIL.set("SCDETAIL_JSSJ", scrq);
        }
    }


    public long fromDateStringToLong(String inVal) { // 此方法计算时间毫秒
        java.util.Date date = null; // 定义时间类型????? ?
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            if (inVal != null && !inVal.isEmpty()) {
                date = inputFormat.parse(inVal); // 将字符型转换成日期型
            }
        } catch (Exception e) {
            logger.error(JgmesCommon.getExceptionDetail2(e));
            e.printStackTrace();
        }
        if (date != null) {
            return date.getTime(); // 返回毫秒数
        } else {
            return 0;
        }

    }


    /*
     * 获取服务器当前时间
     */
    public void getServerTime() {
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        toWrite(jgmesCommon.getCurrentTime());
    }

    private String getParamStr(String paramCode) {
//		Object obj=requestParams.get(paramCode);
//		String res="";
//		if (obj!=null) {
//			res = ((String[])obj)[0];
//		}
//		return res;


        return request.getParameter(paramCode);
    }


    /*
     *
     * 获取工位计数数据    根据任务单来计算
     */
    public void getGwQtyByRWDH() {
        String userCode = request.getParameter("userCode");//非必填，为空则获取当前登陆用户
        String mac = request.getParameter("mac");          //当前MAC地址，必填
        String gwCode = request.getParameter("gwCode");    //工位编码
        String gxCode = request.getParameter("gxCode");    //工序编码
        String cpCode = request.getParameter("cpCode");    //产品编码
        String cxCode = request.getParameter("cxCode");    //产线编码
        String scrwId = request.getParameter("scrwId");    //生产任务ID

        JgmesResult<Integer> ret = new JgmesResult<Integer>();


        ret.Data = 0;
        //校验是否获取到userCode 和mac
        if ((userCode == null || userCode.isEmpty()) && (mac == null || mac.isEmpty())) {
            ret.IsSuccess = false;
            ret.setMessage("用户登陆名、当前MAC地址、不能都为空！");
        }
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);

        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                //如果没有工位编码 ,获取当前工位编码
                if (gwCode == null || gwCode.isEmpty()) {
                    gwCode = getCurrentGW(userCode).getStr("GW_GWBH");
                }

                //如果没有产品编码 ,获取当前产品编码
                if (cpCode == null || cpCode.isEmpty()) {
                    cpCode = getCurrentProductByCxCp(userCode).getStr("SCRW_CPBH");
                }

                //如果没有工序编码 ,获取当前工序编码
                if (gxCode == null || gxCode.isEmpty()) {
                    gxCode = getCurrentGX(cpCode, userCode).getStr("GXGL_GXNUM");
                }

                //如果没有产线编码 ,获取当前产线编码
                if (cxCode == null || cxCode.isEmpty()) {
                    cxCode = getCurrentCX(userCode).getStr("CXSJ_CXBM");
                }

                ////如果没有生产任务ID ,获取当前生产任务ID
                if (scrwId == null || scrwId.isEmpty()) {
                    List<DynaBean> scrwDynaBeanList = serviceTemplate.selectList("JGMES_PLAN_SCRW", " and SCRW_CXBM = '" + cxCode + "' and SCRW_RWZT_CODE = 'RWZT02'");
                    if (scrwDynaBeanList != null && scrwDynaBeanList.size() > 0) {
                        scrwId = scrwDynaBeanList.get(0).getStr("JGMES_PLAN_SCRW_ID");
                    }
                }

                if ((gwCode != null && !gwCode.isEmpty()) &&
                        (gxCode != null && !gxCode.isEmpty()) &&
                        (cpCode != null && !cpCode.isEmpty()) &&
                        (cxCode != null && !cxCode.isEmpty())) {

                    //	List<DynaBean> GwQtyBean = serviceTemplate.selectList("JGMES_PB_BGSJ", " and BGSJ_GWBH='" + gwCode + "' and BGSJ_GXBH = '"+gxCode+"' and BGSJ_CPBH = '"+cpCode+"' and BGSJ_CXBM = '"+cxCode+"' and BGSJ_SCRWID = '"+scrwId+"' and BGSJ_STATUS_CODE != '2'");
                    String sql = "select sum(BGSJ_SL) from JGMES_PB_BGSJ where BGSJ_GWBH='" + gwCode + "' and BGSJ_GXBH = '" + gxCode + "' and BGSJ_CPBH = '" + cpCode + "' and BGSJ_CXBM = '" + cxCode + "' and BGSJ_SCRWID = '" + scrwId + "' and str_to_date(BGSJ_GZSJ,'%Y-%c-%d')=str_to_date(now(),'%Y-%c-%d') and BGSJ_STATUS_CODE != '2'";

//					List<String> tmCodeList = new ArrayList<String>();
//					for(int i = 0;i<GwQtyBean.size();i++) {
//						String tmcode = GwQtyBean.get(i).getStr("BGSJ_TMH");
//							if(!tmCodeList.contains(tmcode)) {
//								tmCodeList.add(tmcode);
//							}
//					}
                    ret.Data = selectListCountBySql(sql);
                    ret.IsSuccess = true;
                }

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                // TODO: handle exception
                ret.IsSuccess = false;
                ret.setMessage("错误！" + e.toString());
            }
        }
        toWrite(jsonBuilder.toJson(ret));
    }


    /*
     *
     * 获取工位计数数据按照订单累加显示
     */
    public void getGwQty() {
        String userCode = request.getParameter("userCode");//非必填，为空则获取当前登陆用户
        String mac = request.getParameter("mac");          //当前MAC地址，必填
        String gwCode = request.getParameter("gwCode");    //工位编码
        String gxCode = request.getParameter("gxCode");    //工序编码
        String cpCode = request.getParameter("cpCode");    //产品编码
        String cxCode = request.getParameter("cxCode");    //产线编码
        String scrwId = request.getParameter("scrwId");    //生产任务ID
        String ddhm = "";//订单号码

        JgmesResult<Integer> ret = new JgmesResult<Integer>();


        ret.Data = 0;
        //校验是否获取到userCode 和mac
        if ((userCode == null || userCode.isEmpty()) && (mac == null || mac.isEmpty())) {
            ret.IsSuccess = false;
            ret.setMessage("用户登陆名、当前MAC地址、不能都为空！");
        }
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);

        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                //如果没有工位编码 ,获取当前工位编码
                if (gwCode == null || gwCode.isEmpty()) {
                    gwCode = getCurrentGW(userCode).getStr("GW_GWBH");
                }

                //如果没有产品编码 ,获取当前产品编码
                if (cpCode == null || cpCode.isEmpty()) {
                    cpCode = getCurrentProductByCxCp(userCode).getStr("SCRW_CPBH");
                }

                //如果没有工序编码 ,获取当前工序编码
                if (gxCode == null || gxCode.isEmpty()) {
                    gxCode = getCurrentGX(cpCode, userCode).getStr("GXGL_GXNUM");
                }

                //如果没有产线编码 ,获取当前产线编码
                if (cxCode == null || cxCode.isEmpty()) {
                    cxCode = getCurrentCX(userCode).getStr("CXSJ_CXBM");
                }

                ////如果没有生产任务ID ,获取当前生产任务ID
                if (scrwId == null || scrwId.isEmpty()) {
                    List<DynaBean> scrwDynaBeanList = serviceTemplate.selectList("JGMES_PLAN_SCRW", " and SCRW_CXBM = '" + cxCode + "' and SCRW_RWZT_CODE = 'RWZT02'");
                    if (scrwDynaBeanList != null && scrwDynaBeanList.size() > 0) {
                        scrwId = scrwDynaBeanList.get(0).getStr("JGMES_PLAN_SCRW_ID");
                        ddhm = scrwDynaBeanList.get(0).getStr("SCRW_DDHM");
                    }
                } else {
                    DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and JGMES_PLAN_SCRW_ID = '" + scrwId + "'");
                    if (scrwDynaBean != null) {
                        ddhm = scrwDynaBean.getStr("SCRW_DDHM");
                    }
                }

                if ((gwCode != null && !gwCode.isEmpty()) &&
                        (gxCode != null && !gxCode.isEmpty()) &&
                        (cpCode != null && !cpCode.isEmpty()) &&
                        (cxCode != null && !cxCode.isEmpty())) {

                    //	List<DynaBean> GwQtyBean = serviceTemplate.selectList("JGMES_PB_BGSJ", " and BGSJ_GWBH='" + gwCode + "' and BGSJ_GXBH = '"+gxCode+"' and BGSJ_CPBH = '"+cpCode+"' and BGSJ_CXBM = '"+cxCode+"' and BGSJ_SCRWID = '"+scrwId+"' and BGSJ_STATUS_CODE != '2'");
                    String sql = "select sum(JGMES_PB_BGSJ.BGSJ_SL) " +
                            "from JGMES_PB_BGSJ " +
                            "LEFT JOIN JGMES_PLAN_SCRW on JGMES_PB_BGSJ.BGSJ_SCRW = JGMES_PLAN_SCRW.SCRW_RWDH " +
                            "WHERE " +
                            //		"JGMES_PB_BGSJ.BGSJ_GWBH = '" + gwCode + "' " +
                            "JGMES_PB_BGSJ.BGSJ_GXBH = '" + gxCode + "' " +
                            "AND JGMES_PB_BGSJ.BGSJ_CPBH = '" + cpCode + "' " +
                            //		"AND JGMES_PB_BGSJ.BGSJ_CXBM = '"+cxCode+"' " +
                            "AND JGMES_PLAN_SCRW.SCRW_DDHM = '" + ddhm + "' " +
                            "AND BGSJ_STATUS_CODE != '2'";

//					List<String> tmCodeList = new ArrayList<String>();
//					for(int i = 0;i<GwQtyBean.size();i++) {
//						String tmcode = GwQtyBean.get(i).getStr("BGSJ_TMH");
//							if(!tmCodeList.contains(tmcode)) {
//								tmCodeList.add(tmcode);
//							}
//					}
                    ret.Data = selectListCountBySql(sql);
                    ret.IsSuccess = true;
                }

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                // TODO: handle exception
                ret.IsSuccess = false;
                ret.setMessage("错误！" + e.toString());
            }
        }
        toWrite(jsonBuilder.toJson(ret));
    }


    /*
     *
     * 获取首工位的投入数量
     */
    public void getFristGwQty() {
        String userCode = request.getParameter("userCode");//非必填，为空则获取当前登陆用户
        String mac = request.getParameter("mac"); //当前MAC地址，必填
        String cpCode = request.getParameter("cpCode");    //产品编码
        String scrwId = request.getParameter("scrwId");    //生产任务ID

        JgmesResult<Integer> ret = new JgmesResult<Integer>();


        ret.Data = 0;
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);

        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                //如果没有产品编码 ,获取当前产品编码
                if (cpCode == null || cpCode.isEmpty()) {
                    cpCode = getCurrentProductByCxCp(userCode).getStr("SCRW_CPBH");
                }

                ////如果没有生产任务ID ,获取当前生产任务ID
                if (scrwId == null || scrwId.isEmpty()) {
                    scrwId = getCurrentCX(userCode).getStr("JGMES_PLAN_SCRW_ID");
                }

                if ((cpCode != null && !cpCode.isEmpty()) &&
                        (scrwId != null && !scrwId.isEmpty())) {

                    DynaBean cpcxDynaBean = serviceTemplate.selectOne("JGMES_PB_CPCXINFO", " and CPCXINFO_SCRWID = '" + scrwId + "' and CPCXINFO_CPBH = '" + cpCode + "' and str_to_date(CPCXINFO_RQ,'%Y-%c-%d')=str_to_date(now(),'%Y-%c-%d')");
                    if (cpcxDynaBean != null) {
                        ret.Data = cpcxDynaBean.getInt("CPCXINFO_TRSL");
                    }

//					//获取产品信息
//					DynaBean cpDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA"," and PRODUCTDATA_BH = '"+cpCode+"'");
//					if(cpDynaBean!=null){
//						//获取当前产线CODe
//						String cxCode = getCurrentCX(userCode).getStr("CXSJ_CXBM");
//						//获取该产品的首工位
//						String gwCode = "";
//						List<DynaBean> cpGwGxBean = serviceTemplate.selectList("JGMES_BASE_CPGWGX", " and CPGWGX_CPBH = '"+cpCode+"' and CPGWGX_CXBM = '"+cxCode+"' order by SY_ORDERINDEX");
//						if(cpGwGxBean!=null&&cpGwGxBean.size()>0) {
//							gwCode = cpGwGxBean.get(0).getStr("CPGWGX_GWBH");
//						}
//						List<DynaBean> GwQtyBean = serviceTemplate.selectList("JGMES_PB_BGSJ", " and BGSJ_GWBH='" + gwCode + "' and BGSJ_CPBH = '"+cpCode+"' and BGSJ_SCRWID = '"+scrwId+"' and BGSJ_STATUS_CODE != '2' ");
//						//						int sl =  0;
////						for(int i = 0;i<GwQtyBean.size();i++) {
////							sl +=  GwQtyBean.get(i).getInt("BGSJ_SL");
////						}
////						ret.Data = GwQtyBean.size();
////						ret.IsSuccess = true;
//					}
                } else {
                    ret.IsSuccess = false;
                    ret.setMessage("信息获取失败！");
                }

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                // TODO: handle exception
                ret.IsSuccess = false;
                ret.setMessage("错误！" + e.toString());
            }
        }
        toWrite(jsonBuilder.toJson(ret));
    }

    public void getOperatingRow() {
        String userCode = request.getParameter("userCode");//非必填，为空则获取当前登陆用户
        String mac = request.getParameter("mac"); //当前MAC地址，必填

        JgmesResult<Integer> ret = new JgmesResult<Integer>();


        ret.Data = 0;
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);

        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                //参数值是就是操作记录的显示条数
                DynaBean xtcsDynaBean = serviceTemplate.selectOne("JGMES_XTGL_XTCS", " and  XTCS_CXFL1_CODE = 'CXFL06' and XTCS_CXFL2_CODE = 'CZJLXSTS'");
                if (xtcsDynaBean != null) {
                    ret.Data = xtcsDynaBean.getInt("XTCS_CSZ");
                } else {
                    //默认是显示1000条
                    ret.Data = 1000;
                }

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                // TODO: handle exception
                ret.IsSuccess = false;
                ret.setMessage("错误！" + e.toString());
            }
        }
        toWrite(jsonBuilder.toJson(ret));
    }


    public int selectListCountBySql(String Sql) throws PCExcuteException {
        Connection conn = this.daoTemplate.getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        int count = 0;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(Sql);
            rs.next();
            count = rs.getInt(1);
        } catch (Exception var14) {
            logger.error("", var14);
        } finally {
            JdbcUtil.close(rs, stmt, conn);
        }

        return count;
    }

    private PCDaoTemplate daoTemplate;

    @Resource(
            name = "PCDAOTemplateORCL"
    )
    public void setDaoTemplate(PCDaoTemplate daoTemplate) {
        this.daoTemplate = daoTemplate;
    }
}
