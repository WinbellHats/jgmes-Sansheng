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

        toWrite(jsonBuilder.returnSuccessJson("\"��ӳɹ�\""));
        super.doSave();
    }


    /*do
     * ͨ�õ����淽��
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

        toWrite(jsonBuilder.returnSuccessJson("\"��ӳɹ�\""));

    }

    /*
     * �������� ���淽��
     * */
    private boolean doJsonSaveBgSj(String jsonStr, String userCode) throws ParseException {
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);

        boolean res = true;
        String key = "";
        String value = "";
        String tabCode = "JGMES_PB_BGSJ";// ((String[])requestParams.get("tabCode"))[0];
        String userName = "";
        String bGSJ_GZSJ = "";//��վʱ��
        System.out.println("jsonString:" + jsonStr);

        DynaBean bgsj = new DynaBean();
        bgsj.setStr(beanUtils.KEY_TABLE_CODE, tabCode);

        try {
            //��ȡ��ǰ��½�û�
            if (userCode != null && !userCode.isEmpty()) {
                jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
            }

            userCode = jgmesCommon.jgmesUser.getCurrentUserCode();
            userName = jgmesCommon.jgmesUser.getCurrentUserName();
            System.out.println("userCode:" + userCode);
            System.out.println("userName:" + userName);
            //������Ĳ���ת������
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


            //��վʱ��
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
     * �����ӱ��淽��
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
            //��ȡ��ǰ��½�û�
            if (userCode != null && !userCode.isEmpty()) {
                jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
            }

            userCode = jgmesCommon.jgmesUser.getCurrentUserCode();
            userName = jgmesCommon.jgmesUser.getCurrentUserName();
            System.out.println("userCode:" + userCode);
            System.out.println("userName:" + userName);

            //������Ĳ���ת������
            //		bgsj= (DynaBean) jsonBuilder.fromJson(jsonString, bgsj.getClass());
            net.sf.json.JSONArray ja1 = net.sf.json.JSONArray.fromObject(jsonArrStr);
            if (ja1.size() > 0) {
                for (int i = 0; i < ja1.size(); i++) {
                    DynaBean bgsj = new DynaBean();
                    bgsj.setStr(beanUtils.KEY_TABLE_CODE, tabCode);

                    JSONObject jb1 = ja1.getJSONObject(i);  // ���� jsonarray ���飬��ÿһ������ת�� json ����
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
     * ���ݱ������ݻ�д��Ʒ������ϸ�����:JGMES_PB_SCDETAIL
     * lxc
     * ����
     * bgsjDynaBean   :��������
     * detailTmList:�����ı��������ӱ������Ľӿ�
     */
    private JgmesResult<String> doSaveScDetail(DynaBean bgsjDynaBean, List<DynaBean> list) {
        //�����
        String BGSJ_TMH = bgsjDynaBean.getStr("BGSJ_TMH");
        //getTmLxByBarCode
        //��վʱ��
        String BGSJ_GZSJ = (String) bgsjDynaBean.get("BGSJ_GZSJ");
        //���߱���
        String BGSJ_CXBM = (String) bgsjDynaBean.get("BGSJ_CXBM");
        //��Ʒ����
        String BGSJ_CPBH = (String) bgsjDynaBean.get("BGSJ_CPBH");
        //��������
        String BGSJ_CXMC = (String) bgsjDynaBean.get("BGSJ_CXMC");
        //��ǰ����ID
        String BGSJ_GXID = (String) bgsjDynaBean.get("BGSJ_GXID");
        //��ǰ������
        String BGSJ_GXBH = (String) bgsjDynaBean.get("BGSJ_GXBH");
        //����˳���
        int BGSJ_GXSXH = (int) bgsjDynaBean.get("BGSJ_GXSXH");
        //����·��_����_���ID
        String GYLXGX_ID = (String) bgsjDynaBean.get("GYLXGX_ID");
        //
        String BGSJZB_GXMC = (String) bgsjDynaBean.get("BGSJZB_GXMC");
        //��ȡ��������
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        JgmesResult<JgmesBarCodeBase> tmLxByBarCode = jgmesCommon.getTmLxByBarCode(BGSJ_TMH, BGSJ_CPBH, null);
        //��������
        JgmesEnumsDic tmLx = tmLxByBarCode.Data.TmLx;

        //��ǰʱ��
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        String scrq = formatter.format(new java.util.Date());
        DynaBean JGMES_PB_SCDETAIL = null;


        JgmesEnumsDic ss = JgmesEnumsDic.TMCP;

        //��Ʒ����or ���̿�����
        if (JgmesEnumsDic.TMCP.equals(tmLx) || JgmesEnumsDic.TMLCK.equals(tmLx)) {
            String tmType = "SCDETAIL_CPTM";
            if (JgmesEnumsDic.TMLCK.equals(tmLx)) {
                tmType = "SCDETAIL_LCKTM";
            }
            JGMES_PB_SCDETAIL = serviceTemplate.selectOne("JGMES_PB_SCDETAIL", "  and (SCDETAIL_CPTM='" + BGSJ_TMH + "' or  SCDETAIL_LCKTM='" + BGSJ_TMH + "')");
            if (BGSJ_CXBM != null && !BGSJ_CXBM.isEmpty()) {
                //��ȡ��Ʒ��Ϣ   JGMES_BASE_PRODUCTDATA ��Ʒ��Ϣ��
                DynaBean productData = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " AND  PRODUCTDATA_BH='" + BGSJ_CPBH + "'");
                //����·�߱��
                String PRODUCTDATA_CPGYLX = (String) productData.get("PRODUCTDATA_CPGYLX");
                if (PRODUCTDATA_CPGYLX != null && !PRODUCTDATA_CPGYLX.isEmpty()) {
                    //JGMES_GYGL_GYLX  ����·�߱�
                    DynaBean GYLX_GYLXNAME = serviceTemplate.selectOne("JGMES_GYGL_GYLX", " AND  GYLX_GYLXNAME='" + PRODUCTDATA_CPGYLX + "'");
                    //����·������id
                    String GYLX_ID = (String) GYLX_GYLXNAME.get("GYLX_ID");
                    //��ȡ����·�ߵ����й���
                    if (GYLX_ID != null && !GYLX_ID.isEmpty()) {//
                        List<DynaBean> jgmesGYLXGX = serviceTemplate.selectList("JGMES_GYGL_GYLXGX", " AND  GYLX_ID='" + GYLX_ID + "'   ORDER BY SY_ORDERINDEX asc");
                        //��ǰ������������
                        int listsize = 0;
                        //��ǰ����
                        for (int i = 0; i < jgmesGYLXGX.size(); i++) {
                            if (jgmesGYLXGX.get(i).get("SY_ORDERINDEX").equals(BGSJ_GXSXH)) {
                                listsize = i + 1;
                                break;
                            }
                        }
                        //��һ������
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
                                //SCDETAIL_DQTMLX  ��ǰ��������
                                serviceTemplate.insert(JGMES_PB_SCDETAIL);
                            }
                        } else if (listsize == jgmesGYLXGX.size()) {//���һ������
                            if (JGMES_PB_SCDETAIL == null) {
                                JGMES_PB_SCDETAIL = new DynaBean("JGMES_PB_SCDETAIL", true);
                            }
                            evalDynaBean(JGMES_PB_SCDETAIL, bgsjDynaBean, jgmesGYLXGX, tmType, listsize);
                            serviceTemplate.update(JGMES_PB_SCDETAIL);
                        } else {//�м乤��
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
            //������������---JGMES_PB_BGSJ_ID
            List<DynaBean> bgsjzbList = serviceTemplate.selectList("JGMES_PB_BGSJZB", " AND  JGMES_PB_BGSJ_ID='" + bgsjDynaBean.getStr("JGMES_PB_BGSJ_ID") + "'");
            for (int i = 0; i < bgsjzbList.size(); i++) {
                DynaBean PRODUCTDATA = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " and PRODUCTDATA_BH='" + bgsjzbList.get(i).getStr("BGSJZB_WLBH") + "'");//PRODUCTDATA_CPGYLXID
                List<DynaBean> jgmesGYLXGX = serviceTemplate.selectList("JGMES_GYGL_GYLXGX", " AND  GYLX_ID='" + PRODUCTDATA.getStr("PRODUCTDATA_CPGYLXID") + "' ORDER BY SY_ORDERINDEX asc");
                //��ǰ������������
                int listsize = 0;
                //��ǰ����
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
                        //SCDETAIL_DQTMLX  ��ǰ��������
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
                //��ȡ��Ʒ��Ϣ   JGMES_BASE_PRODUCTDATA ��Ʒ��Ϣ��
                DynaBean productData = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " AND  PRODUCTDATA_BH='" + BGSJ_CPBH + "'");
                //����·�߱��
                String PRODUCTDATA_CPGYLX = (String) productData.get("PRODUCTDATA_CPGYLX");
                if (PRODUCTDATA_CPGYLX != null && !PRODUCTDATA_CPGYLX.isEmpty()) {
                    //JGMES_GYGL_GYLX  ����·�߱�
                    DynaBean GYLX_GYLXNUM = serviceTemplate.selectOne("JGMES_GYGL_GYLX", " AND  GYLX_GYLXNAME='" + PRODUCTDATA_CPGYLX + "'");
                    //����·������id
                    String GYLX_ID = (String) GYLX_GYLXNUM.get("GYLX_ID");
                    //��ȡ����·�ߵ����й���
                    if (GYLX_ID != null && !GYLX_ID.isEmpty()) {//
                        List<DynaBean> jgmesGYLXGX = serviceTemplate.selectList("JGMES_GYGL_GYLXGX", " AND  GYLX_ID='" + GYLX_ID + "'   ORDER BY SY_ORDERINDEX asc");
                        if (jgmesGYLXGX != null && jgmesGYLXGX.size() > 0) {
                            //��ǰ������������
                            int listsize = 0;
                            //��ǰ����
                            for (int i = 0; i < jgmesGYLXGX.size(); i++) {
                                if (jgmesGYLXGX.get(i).get("SY_ORDERINDEX").equals(BGSJ_GXSXH)) {
                                    listsize = i + 1;
                                    break;
                                }
                            }
                            //��һ������
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
                                    //SCDETAIL_DQTMLX  ��ǰ��������
                                    serviceTemplate.insert(JGMES_PB_SCDETAIL);
                                }
                            } else if (listsize == jgmesGYLXGX.size()) {//���һ������
                                if (JGMES_PB_SCDETAIL == null) {
                                    JGMES_PB_SCDETAIL = new DynaBean("JGMES_PB_SCDETAIL", true);
                                }
                                evalDynaBean(JGMES_PB_SCDETAIL, bgsjDynaBean, jgmesGYLXGX, "SCDETAIL_GZBTM", listsize);
                                serviceTemplate.update(JGMES_PB_SCDETAIL);
                            } else {//�м乤��
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
     * �������ӱ��淽����һ�𱣴�
     * */
    //****
    public JgmesResult<HashMap> doJsonSaveBgSjAll(String jsonStr, String jsonArrStr, String userCode, String mac, JgmesEnumsBgsjLx bgsjLxEnums, boolean BatchCode) throws ParseException {
        JgmesResult<HashMap> jgmesResult = new JgmesResult<HashMap>();
        JgmesResult<String> jgRes = new JgmesResult<String>();//�����������ؽ��
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
        String cpBh = "";// ��Ʒ���
        String cpMc = "";// ��Ʒ����
        String cpGg = "";// ��Ʒ���
        String barCode = "";
        String currentGxID = "";// ��ǰ����ID
        String scrwId = "";// ��������ID
        String scrw = "";// ��������
        boolean isInsert = false;
        boolean isBg = false;//�Ƿ��Ѿ�����
        int trsl = 0;// Ͷ������
        int czsl = 0;// ��վ����
        String cxbm = "";// ���߱���
        String cxmc = "";// ��������
        String gdhm = "";// ��������
        String ddhm = "";// ��������
        String lckh = "";// ���̿���
        int gdsl = 0;// ��������
        int ddsl = 0;// ��������
        int wcsl = 0;// �������
        String cxId = "";// ����ID

        try {
            // ��ȡ��ǰ��½�û�
            if (bgsjLxEnums.equals(JgmesEnumsBgsjLx.LySj)) {//�����û�û���û�
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

            // ��ǰ̨����ת��������
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
                //�������ݲ���Ҫ���»�ȡ
                scrw = bgsj.getStr("BGSJ_SCRW");//���񵥺�
                scrwId = bgsj.getStr("BGSJ_SCRWID");
                bgsj.setStr("SY_CREATETIME", bgsj.getStr("BGSJ_GZSJ"));
                userCode = "admin";
                jgmesResult = doCheckChangeCp(userCode, barCode, cpBh, currentGxID, bgsj.getStr("BGSJ_SCRWID"));
            } else {

                jgmesResult = doCheckChangeCp(userCode, barCode, cpBh, currentGxID, bgsj.getStr("BGSJ_SCRWID"));// ���ж��������Ʒ�Ƿ�ƥ�䣬��ƥ�䷵�������Ӧ�Ĳ�Ʒ��Ϣ
                if (jgmesResult.IsSuccess) {
                    JgmesResult<String> jgmesResult2 = jgmesCommon.doCheckBgWl(jsonArrStr);//У��󶨵Ĺؼ�����
                    if (!jgmesResult2.IsSuccess) {
                        jgmesResult.setMessage(jgmesResult2.getMessage());
                    }
                }

                if (!jgmesResult.IsSuccess) {// ��������򷵻ض�Ӧ������
                    //toWrite(jsonBuilder.toJson(jgmesResult));
                    return jgmesResult;
                } else {
                    // ��������
                    scrwId = bgsj.getStr("BGSJ_SCRWID");
                    if (scrwId == null || scrwId.isEmpty()) {
                        jgmesResult.setMessage("ȱ�����񵥣�");
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
                        bgsj.setStr("BGSJ_MACDZ", mac);// MAC��ַ

                        if (bgsjLxEnums.equals(JgmesEnumsBgsjLx.GwSj)) {// ��λ���ݱ���
                            bgsj.set("BGSJ_SJLX", 0);// ��������
                        } else {
                            bgsj.set("BGSJ_SJLX", 1);
                        }

                        // ��λ��Ϣ
                        DynaBean gw = getCurrentGW(userCode);
                        if (gw != null) {
                            bgsj.setStr("BGSJ_GWBH", gw.getStr("GW_GWBH"));
                            bgsj.setStr("BGSJ_GWMC", gw.getStr("GW_GWMC"));
                            bgsj.setStr("BGSJ_GWID", gw.getStr("JGMES_BASE_GW_ID"));
                        }

                        // ������Ϣ
                        DynaBean cxDynaBean = getCurrentCX(userCode);
                        if (cxDynaBean != null) {
                            cxId = cxDynaBean.getStr("JGMES_BASE_CXSJ_ID");
                            cxbm = cxDynaBean.getStr("CXSJ_CXBM");
                            cxmc = cxDynaBean.getStr("CXSJ_CXMC");
                            bgsj.setStr("BGSJ_CXBM", cxbm);
                            bgsj.setStr("BGSJ_CXMC", cxmc);
                        }
                        if (cpBh != null && !cpBh.isEmpty()) {
                            // ������Ϣ
                            DynaBean gx = getCurrentGX(cpBh, userCode);
                            if (gx != null) {
                                bgsj.setStr("BGSJ_GXID", gx.getStr("GXGL_GYLX_GXID"));
                                bgsj.setStr("BGSJ_GXBH", gx.getStr("GXGL_GXNUM"));
                                bgsj.setStr("BGSJ_GXMC", gx.getStr("GXGL_GXNAME"));
                                bgsj.set("BGSJ_GXSXH", gx.get("GXGL_GXSXH"));// ����˳���

                            }
                            // ��Ʒ��Ϣ
                            DynaBean cpDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA",
                                    " AND PRODUCTDATA_BH='" + cpBh + "'");
                            if (cpDynaBean != null) {
                                cpMc = cpDynaBean.getStr("PRODUCTDATA_NAME");
                                cpGg = cpDynaBean.getStr("PRODUCTDATA_GG");
                                bgsj.setStr("BGSJ_CPMC", cpMc);// ��Ʒ����
                                bgsj.setStr("BGSJ_CPGG", cpGg);// ��Ʒ���
                            }
                        }

                        // ��վʱ��
                        bgsj.set("BGSJ_GZSJ", jgmesCommon.getCurrentTime());
                    }
                }

                // ���޵�ID���������뼰��ǰά��״̬��ȡ����վID
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

//					//���ع���ID����
//					String fhgxCode = bgsj.getStr("BGSJ_FHGXBH");
//					if (fhgxCode != null && !fhgxCode.isEmpty()) {
//						DynaBean gxBean = serviceTemplate.selectOne(tmString, fhgxCode)
//					}
                jgmesCommon.setDynaBeanInfo(bgsj);
            }
            // ����ǰ���й���������
            currentGxID = bgsj.getStr("BGSJ_GXID");
            // ����ǰ����һ���Ƿ��Ѿ�����������Ѿ������Ͱ�֮ǰ�ı������ݸĳɲ�����
            List<DynaBean> ybgDynaBeanList = serviceTemplate.selectList("JGMES_PB_BGSJ", " and BGSJ_TMH = '" + barCode + "' and BGSJ_GXID = '" + currentGxID + "' and  (BGSJ_SCRW = '" + scrw + "' or BGSJ_SCRWID = '" + scrwId + "')");
            //����ܲ�����ݣ���˵�����ظ�������Ҫ��һЩ����
            if (ybgDynaBeanList != null && ybgDynaBeanList.size() > 0) {
                //���Ϊ�Ѿ����������治���д��Ʒ�깤����������
                isBg = true;
                //����״̬��Ĭ�Ͽ�����
                DynaBean dic = jgmesCommon.getDic("JGMES_STATUS", "2");
                //��֮ǰ�ı������ݵ�״̬�ĳɲ�����
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
                    //��֮ǰ�ı������ݵ�״̬�ĳ�����
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

                // ������Ĳ���ת������
                // bgsj= (DynaBean) jsonBuilder.fromJson(jsonString, bgsj.getClass());
                if (jsonArrStr != null && !jsonArrStr.isEmpty()) {
                    net.sf.json.JSONArray ja1 = net.sf.json.JSONArray.fromObject(jsonArrStr);
                    if (ja1.size() > 0) {
                        for (int i = 0; i < ja1.size(); i++) {
                            DynaBean bgsjzb = new DynaBean("JGMES_PB_BGSJZB", true);
                            bgsjzb.setStr(beanUtils.KEY_TABLE_CODE, tabCodeDetail);
                            JSONObject jb1 = ja1.getJSONObject(i); // ���� jsonarray ���飬��ÿһ������ת�� json ����
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
                //���˵��ظ�ɨ������������
                if (!isBg) {
                    //�����Ʒ������������
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
                    // �����ĩ�����򣬸��²�Ʒ�������������еĳ�վ���������ʱ�䣻����������������������Ų�����������������������
                    if (jgmesCommon.IsLastGx(barCode, currentGxID, null) && scrwId != null && !scrwId.isEmpty() && !isBg) {
                        // �������������������
                        if (scrwDynaBean != null) {
                            wcsl = scrwDynaBean.getInt("SCRW_WCSL");
                            scrwDynaBean.set("SCRW_WCSL", wcsl + 1);
                            if (scrwDynaBean.getInt("SCRW_WCSL")==scrwDynaBean.getInt("SCRW_PCSL")){
                                scrwDynaBean.set("SCRW_RWZT_CODE","RWZT03");
                                scrwDynaBean.set("SCRW_RWZT_NAME","�������");
                            }
                            serviceTemplate.update(scrwDynaBean);
                            // ���¹����������
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


                if (bgsjLxEnums.equals(JgmesEnumsBgsjLx.GwSj) || bgsjLxEnums.equals(JgmesEnumsBgsjLx.LySj)) {// ��λ���ݱ��棬��ɷ��޵�
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
                        //�������Ȳ�����ng������ȼ����뷵��
                    } else {
                        if (jgString.equals(JgmesEnumsDic.PdJgUseless.getKey()))// �ж�������ϸ�����ɷ��޵�
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

                            DynaBean fxdDynaBean_jg = serviceTemplate.insert(fxdDynaBean);// ���뷵�޵�����
                            String zbJgStr = "";

                            if (list != null && !list.isEmpty()) {
                                List<DynaBean> fxList = new ArrayList<DynaBean>();
                                for (int i = 0; i < list.size(); i++) {
                                    DynaBean bgsjzbDynaBean = list.get(i);
                                    if (bgsjzbDynaBean != null) {
//								zbJgStr = bgsjzbDynaBean.getStr("BGSJZB_PDJG_CODE");
//								if (zbJgStr!=null && zbJgStr.equals(JgmesEnumsDic.PdJgUseless.getKey()))// �ж�������ϸ�����ɷ��޵��ӱ�
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
                if (bgsjLxEnums.equals(JgmesEnumsBgsjLx.FxSj) && false) {// �������ݱ��棬���Ʒ��޵�����
                    DynaBean fDynaBean = new DynaBean();
                    String fxdId = bgsj_jg.getStr("BGSJ_FXDID");
                    String wxzId = bgsj_jg.getStr("BGSJ_WXZID");// ά��վID

                    if (fxdId != null && !fxdId.isEmpty()) {
                        fDynaBean = serviceTemplate.selectOneByPk("JGMES_PB_FXD", fxdId);
                        // ��д���ĸ�ά��վ�����
                        if (wxzId != null && !wxzId.isEmpty()) {
                            DynaBean wxzDynaBean = serviceTemplate.selectOne("JGMES_PB_FXZ", " and JGMES_PB_FXZ_ID='" + wxzId + "'");
                            fDynaBean.setStr("JGMES_PB_FXZ_ID", wxzId);
                            if (wxzDynaBean != null) {
                                fDynaBean.setStr("FXD_FXZBH", wxzDynaBean.getStr("FXZ_FXZBH"));
                                fDynaBean.setStr("FXD_FXZMC", wxzDynaBean.getStr("FXZ_FXZMC"));
                            }
                        }
                        // ά�޽����ͨ������ά�޽���ֶλ�д
                        jgmesCommon.setDynaBeanDic(fDynaBean, "JGMES_MES_WXJG",
                                bgsj_jg.getStr("BGSJ_WXJG_CODE"), "FXD_WXJG_ID", "FXD_WXJG_CODE",
                                "FXD_WXJG_NAME");
                        // ά��״̬��д���������ά��
                        jgmesCommon.setDynaBeanDic(fDynaBean, "JGMES_DIC_WXZT",
                                JgmesEnumsDic.WxZtFinish.getKey(), "FXD_WXZT_ID", "FXD_WXZT_CODE",
                                "FXD_WXZT_NAME");
                        serviceTemplate.update(fDynaBean);
                    }
                }
                //�ж��Ƿ�Ϊ�ؼ����������룬����ǹؼ������������Ҫ��д������е�ʹ��״̬
                if (BatchCode) {
                    //��ȡ����
                    JgmesResult<JgmesBarCodeBase> barCodeJgmesResult = jgmesCommon.getTmLxByBarCode(barCode, null, null);
                    //�ж�һ���Ƿ�Ϊ��������
                    if (barCodeJgmesResult.Data.IsMaterail) {
                        DynaBean wltmScDynaBean = serviceTemplate.selectOne("JGMES_BASE_WLTM", " and WLTM_TMH='" + barCode + "'");
                        if (wltmScDynaBean != null) {
                            wltmScDynaBean.set("WLTM_YSYSL", wltmScDynaBean.getInt("WLTM_YSYSL") + bgsj_jg.getInt("sl"));
                        }
                    }
                }

            }
            // �ĳ��ڱ���֮ǰУ�� ����ɹ���У���Ƿ������Ʒ�����Ѹ�����Ʒ�򷵻��²�Ʒ�������Ϣ
//			DynaBean currentPcd = getCurrentProductByCxCp(userCode);
//			if (currentPcd != null) {
//				if(!bgsj_jg.getStr("BGSJ_CPBH").equals(currentPcd.getStr("SCRW_CPBH"))) {//������
//					System.out.println("�����Ĳ�Ʒ��"+bgsj_jg.getStr("BGSJ_CPBH")+"||�����������Ĳ�Ʒ:"+currentPcd.getStr("SCRW_CPBH"));
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
        // MAC��ַ
        String mac = request.getParameter("mac");
        // �û�����  ����
        String userCode = request.getParameter("userCode");
        //���񵥺�
        String rwNo = request.getParameter("rwNo");
        //��Ʒ�����
        String cpCode = request.getParameter("cpCode");
        // ���������
        String wlBarCode = request.getParameter("wlBarCode");

        JgmesResult<HashMap> ret = new JgmesResult<>();

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        // У��Ϸ��ԣ��Ƿ��ص�½
        if (doCheck(userCode, mac).IsSuccess) {
            if (wlBarCode == null || wlBarCode.isEmpty()) {
                ret.setMessage("δ��ȡ����������ţ�");
            }
            if (cpCode == null || cpCode.isEmpty()) {
                ret.setMessage("δ��ȡ����Ʒ���룡");
            }
            if (rwNo == null || rwNo.isEmpty()) {
                ret.setMessage("δ��ȡ�����񵥺ţ�");
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
                                ret.setMessage("�����������Ʒ���벻ƥ�䣡");
                            }
                        } else {
                            ret.setMessage("����������Ѿ��ڱ�������У�");
                        }
                    } else {
                        /* �����������������Ӱ�죬�����޸�ʱͬ�����������ݵ�������������� */
                        if (bgslzbDynaBeanList.size()==2) {
                            DynaBean bgslzbDynaBean = new DynaBean();
                            /* �����4ɨ������ά��/BT���ά������������=2������ */
                            /* У����һ���Ƿ�����������,�Ѳ����������Ե�������ȡ���� */
                            StringBuilder testData = new StringBuilder();
                            for (DynaBean bean : bgslzbDynaBeanList) {
                                String jcz = bean.getStr("BGSJZB_JCZ");
                                testData.append(jcz);
                                if (StringUtil.isEmpty(jcz)) {
                                    bgslzbDynaBean = bean;
                                }
                            }
                            /* ����һ��Ϊ�������� */
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
                                        ret.setMessage("�����������Ʒ���벻ƥ�䣡");
                                    }
                                } else {
                                    ret.setMessage("����������Ѿ��ڱ�������У�");
                                }
                            }
                        }else{
                            ret.setMessage("��ȷ��ɨ���������Ψһ�룬�жദʹ�ø����룡");
                        }
                    }
                }
            }
        }

        toWrite(jsonBuilder.toJson(ret));
    }


    /**
     * �������ϸ�����Ϣ
     */
    public void doSaveCriticalComponent() {
        // MAC��ַ
        String mac = request.getParameter("mac");
        // �û�����  ����
        String userCode = request.getParameter("userCode");
        //��Ʒ����
        String cpCode = request.getParameter("cpCode");
        //��Ʒ�����
        String barCode = request.getParameter("barCode");
        // �����������
        String newWlBarCode = request.getParameter("newWlBarCode");
        // �����������
        String oldWlBarCode = request.getParameter("oldWlBarCode");

        //���������ӱ�����
        String bgsjzbId = request.getParameter("bgsjzbId");

        JgmesResult<List<HashMap>> ret = new JgmesResult<>();

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        // У��Ϸ��ԣ��Ƿ��ص�½
        if (doCheck(userCode, mac).IsSuccess) {
            if (newWlBarCode == null || newWlBarCode.isEmpty()) {
                ret.setMessage("δ��ȡ������������ţ�");
            }
            if (oldWlBarCode == null || oldWlBarCode.isEmpty()) {
                ret.setMessage("δ��ȡ������������ţ�");
            }
            if (cpCode == null || cpCode.isEmpty()) {
                ret.setMessage("δ��ȡ����Ʒ���룡");
            }
            if (bgsjzbId == null || bgsjzbId.isEmpty()) {
                ret.setMessage("δ��ȡ�����������ӱ�����ID��");
            }
            if (ret.IsSuccess) {
                //��ȡ���������ӱ�
                DynaBean bgsjzbDynaBean = serviceTemplate.selectOne("JGMES_PB_BGSJZB", " and JGMES_PB_BGSJZB_ID = '" + bgsjzbId + "'");
                if (bgsjzbDynaBean != null) {
                    if (oldWlBarCode.equals(bgsjzbDynaBean.getStr("BGSJZB_WLBH"))) {
                        ret.setMessage("�����������������");
                        toWrite(jsonBuilder.toJson(ret));
                        return;
                    }
                    //��ȡ������������
                    DynaBean bgsjDynaBean = serviceTemplate.selectOne("JGMES_PB_BGSJ", " and JGMES_PB_BGSJ_ID = '" + bgsjzbDynaBean.getStr("JGMES_PB_BGSJ_ID") + "'");
                    if (bgsjDynaBean != null && cpCode.equals(bgsjDynaBean.getStr("BGSJ_CPBH"))) {
                        //��¼���ϸ�����¼��
                        DynaBean wlghDynaBean = new DynaBean();
                        wlghDynaBean.set("JGMES_SCGCGL_WLGHJLB_ID", JEUUID.uuid());
                        wlghDynaBean.set(beanUtils.KEY_TABLE_CODE, "JGMES_SCGCGL_WLGHJLB");
                        //��Ʒ
                        wlghDynaBean.set("WLGHJLB_CPMC", bgsjDynaBean.getStr("BGSJ_CPMC"));
                        wlghDynaBean.set("WLGHJLB_CPBM", bgsjDynaBean.getStr("BGSJ_CPBH"));
                        //����
                        wlghDynaBean.set("WLGHJLB_WLBM", bgsjzbDynaBean.getStr("BGSJZB_WLBH"));
                        wlghDynaBean.set("WLGHJLB_WLMC", bgsjzbDynaBean.getStr("BGSJZB_WLMC"));
                        //����
                        wlghDynaBean.set("WLGHJLB_GXBM", bgsjDynaBean.getStr("BGSJ_GXBH"));
                        wlghDynaBean.set("WLGHJLB_GXMC", bgsjDynaBean.getStr("BGSJ_GXMC"));

                        //��Ʒ�����
                        wlghDynaBean.set("WLGHJLB_CPTMH", barCode);
                        //�����������
                        wlghDynaBean.set("WLGHJLB_JWLTMH", oldWlBarCode);
                        //�����������
                        wlghDynaBean.set("WLGHJLB_XWLTMH", newWlBarCode);
                        //���񵥺�
                        wlghDynaBean.set("WLGHJLB_RWDH", bgsjDynaBean.getStr("BGSJ_SCRW"));
                        //��ȡ������
                        DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and SCRW_RWDH = '" + bgsjDynaBean.getStr("BGSJ_SCRW") + "'");
                        //��������
                        wlghDynaBean.set("WLGHJLB_GDHM", bgsjDynaBean.getStr("BGSJ_GDHM"));
                        //��������
                        if (scrwDynaBean != null) {
                            wlghDynaBean.set("WLGHJLB_DDHM", scrwDynaBean.getStr("SCRW_DDHM"));
                            //�ڲ�������
                            wlghDynaBean.set("WLGHJLB_LCKH", scrwDynaBean.getStr("SCRW_LCKH"));
                            if (wlghDynaBean.getStr("WLGHJLB_GDHM") == null || "".equals(wlghDynaBean.getStr("WLGHJLB_GDHM"))) {
                                wlghDynaBean.set("WLGHJLB_GDHM", scrwDynaBean.getStr("SCRW_GDHM"));
                            }
                        }
                        //����ʱ��
                        wlghDynaBean.set("WLGHJLB_GHSJ", jgmesCommon.getCurrentTime());
                        //������������ID
                        wlghDynaBean.set("WLGHJLB_BGSJZJID", bgsjDynaBean.getStr("JGMES_PB_BGSJ_ID"));

                        //�������ϸ�����¼
                        serviceTemplate.insert(wlghDynaBean);
                        bgsjzbDynaBean.set("BGSJZB_TMH", newWlBarCode);
                        serviceTemplate.update(bgsjzbDynaBean);
                    }

                    //���Q���������ϣ������{��������ϣ�
                    List<DynaBean> bgslzbDynaBeanList = serviceTemplate.selectList("JGMES_PB_BGSJZB", " and BGSJZB_TMH = '" + oldWlBarCode + "' and JGMES_PB_BGSJ_ID != '" + bgsjzbDynaBean.getStr("JGMES_PB_BGSJ_ID") + "'");
                    if (bgslzbDynaBeanList != null && bgslzbDynaBeanList.size() > 0) {
                        for (DynaBean bgslzb : bgslzbDynaBeanList) {
                            //��ȡ������������
                            bgsjDynaBean = serviceTemplate.selectOne("JGMES_PB_BGSJ", " and JGMES_PB_BGSJ_ID = '" + bgslzb.getStr("JGMES_PB_BGSJ_ID") + "'");
                            if (bgsjDynaBean != null && cpCode.equals(bgsjDynaBean.getStr("BGSJ_CPBH"))) {
                                //��¼���ϸ�����¼��
                                DynaBean wlghDynaBean = new DynaBean();
                                wlghDynaBean.set("JGMES_SCGCGL_WLGHJLB_ID", JEUUID.uuid());
                                wlghDynaBean.set(beanUtils.KEY_TABLE_CODE, "JGMES_SCGCGL_WLGHJLB");
                                //��Ʒ
                                wlghDynaBean.set("WLGHJLB_CPMC", bgsjDynaBean.getStr("BGSJ_CPMC"));
                                wlghDynaBean.set("WLGHJLB_CPBM", bgsjDynaBean.getStr("BGSJ_CPBH"));
                                //����
                                wlghDynaBean.set("WLGHJLB_WLBM", bgslzb.getStr("BGSJZB_WLBH"));
                                wlghDynaBean.set("WLGHJLB_WLMC", bgslzb.getStr("BGSJZB_WLMC"));
                                //����
                                wlghDynaBean.set("WLGHJLB_GXBM", bgsjDynaBean.getStr("BGSJ_GXBH"));
                                wlghDynaBean.set("WLGHJLB_GXMC", bgsjDynaBean.getStr("BGSJ_GXMC"));

                                //��Ʒ�����
                                wlghDynaBean.set("WLGHJLB_CPTMH", barCode);
                                //�����������
                                wlghDynaBean.set("WLGHJLB_JWLTMH", oldWlBarCode);
                                //�����������
                                wlghDynaBean.set("WLGHJLB_XWLTMH", newWlBarCode);
                                //���񵥺�
                                wlghDynaBean.set("WLGHJLB_RWDH", bgsjDynaBean.getStr("BGSJ_SCRW"));
                                //��ȡ������
                                DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and SCRW_RWDH = '" + bgsjDynaBean.getStr("BGSJ_SCRW") + "'");
                                //��������
                                wlghDynaBean.set("WLGHJLB_GDHM", bgsjDynaBean.getStr("BGSJ_GDHM"));
                                //��������
                                if (scrwDynaBean != null) {
                                    wlghDynaBean.set("WLGHJLB_DDHM", scrwDynaBean.getStr("SCRW_DDHM"));
                                    //�ڲ�������
                                    wlghDynaBean.set("WLGHJLB_LCKH", scrwDynaBean.getStr("SCRW_LCKH"));
                                    if (wlghDynaBean.getStr("WLGHJLB_GDHM") == null || "".equals(wlghDynaBean.getStr("WLGHJLB_GDHM"))) {
                                        wlghDynaBean.set("WLGHJLB_GDHM", scrwDynaBean.getStr("SCRW_GDHM"));
                                    }
                                }
                                //����ʱ��
                                wlghDynaBean.set("WLGHJLB_GHSJ", jgmesCommon.getCurrentTime());
                                //������������ID
                                wlghDynaBean.set("WLGHJLB_BGSJZJID", bgsjDynaBean.getStr("JGMES_PB_BGSJ_ID"));

                                //�������ϸ�����¼
                                serviceTemplate.insert(wlghDynaBean);
                                bgslzb.set("BGSJZB_TMH", newWlBarCode);
                                serviceTemplate.update(bgslzb);
                            }
                        }
                    }
                } else {
                    ret.setMessage("δ��ȡ���������ݣ�");
                }
            }
        }
        toWrite(jsonBuilder.toJson(ret));
    }

    /*
     * �����Ʒ����������
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
        String cxId = "";// ����ID
        String gdhm = "";// ��������
        String ddhm = "";// ��������
        String lckh = "";// ���̿���
        String scrw = bgsjDynaBean.getStr("BGSJ_SCRW");// ���񵥺�
        int gdsl = 0;// ��������
        int ddsl = 0;// ��������
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

            // ������׵����򣬸��²�Ʒ�������������е�Ͷ����������ʼʱ�䣬��д����ػ�����Ϣ
            if (scrwId != null && !scrwId.isEmpty() && jgmesCommon.IsFirstGx(barCode, currentGxID, null)) {
                isInsert = false;
                cpcxInfoDynaBean = serviceTemplate.selectOne("JGMES_PB_CPCXINFO",
                        " and CPCXINFO_SCRWID='" + scrwId + "' and CPCXINFO_RQ='" + currentDate + "'");
                if (cpcxInfoDynaBean == null) {
                    cpcxInfoDynaBean = new DynaBean();
                    cpcxInfoDynaBean.set(beanUtils.KEY_TABLE_CODE, "JGMES_PB_CPCXINFO");
                    isInsert = true;
                    cpcxInfoDynaBean.set("CPCXINFO_KSSJ", jgmesCommon.getCurrentTime());// ��ʼʱ��

                    cpcxInfoDynaBean.set("CPCXINFO_CPBH", cpBh);// ��Ʒ
                    cpcxInfoDynaBean.set("CPCXINFO_CPMC", cpMc);
                    cpcxInfoDynaBean.set("CPCXINFO_CPGG", cpGg);
                    cpcxInfoDynaBean.set("CPCXINFO_CXBM", cxbm);// ����
                    cpcxInfoDynaBean.set("CPCXINFO_CXMC", cxmc);
                    cpcxInfoDynaBean.set("JGMES_BASE_CXSJ_ID", cxId);
                    cpcxInfoDynaBean.set("CPCXINFO_GDHM", gdhm);// ��������
                    cpcxInfoDynaBean.set("CPCXINFO_DDHM", ddhm);// ��������
                    cpcxInfoDynaBean.set("CPCXINFO_LCKH", lckh);// ���̿���
                    cpcxInfoDynaBean.set("CPCXINFO_SCRWID", scrwId);// ����ID
                    cpcxInfoDynaBean.set("CPCXINFO_RWDH", scrw);// ���񵥺�
                    cpcxInfoDynaBean.set("CPCXINFO_GDSL", gdsl);// ��������
                    cpcxInfoDynaBean.set("CPCXINFO_GDSL", ddsl);// ��������
                    cpcxInfoDynaBean.set("CPCXINFO_RQ", currentDate);//����

                    jgmesCommon.setDynaBeanInfo(cpcxInfoDynaBean);

                } else {
                    trsl = cpcxInfoDynaBean.getInt("CPCXINFO_TRSL");
                }
                cpcxInfoDynaBean.set("CPCXINFO_TRSL", trsl + 1);// Ͷ������

                if (isInsert) {
                    serviceTemplate.insert(cpcxInfoDynaBean);
                } else {
                    serviceTemplate.update(cpcxInfoDynaBean);
                }
            }
            // �����ĩ�����򣬸��²�Ʒ�������������еĳ�վ���������ʱ�䣻����������������������Ų�����������������������

            if (scrwId != null && !scrwId.isEmpty() && jgmesCommon.IsLastGx(barCode, currentGxID, null)) {
                isInsert = false;
                cpcxInfoDynaBean = serviceTemplate.selectOne("JGMES_PB_CPCXINFO",
                        " and CPCXINFO_SCRWID='" + scrwId + "' and CPCXINFO_RQ='" + currentDate + "'");
                if (cpcxInfoDynaBean == null) {// �������Ӧ�ò�������ĩ��������δ�����ݵļ�¼
                    cpcxInfoDynaBean = new DynaBean();
                    cpcxInfoDynaBean.set(beanUtils.KEY_TABLE_CODE, "JGMES_PB_CPCXINFO");
                    isInsert = true;

                    cpcxInfoDynaBean.set("CPCXINFO_CPBH", cpBh);// ��Ʒ
                    cpcxInfoDynaBean.set("CPCXINFO_CPMC", cpMc);
                    cpcxInfoDynaBean.set("CPCXINFO_CPGG", cpGg);
                    cpcxInfoDynaBean.set("CPCXINFO_CXBM", cxbm);// ����
                    cpcxInfoDynaBean.set("CPCXINFO_CXMC", cxmc);
                    cpcxInfoDynaBean.set("JGMES_BASE_CXSJ_ID", cxId);
                    cpcxInfoDynaBean.set("CPCXINFO_GDHM", gdhm);// ��������
                    cpcxInfoDynaBean.set("CPCXINFO_DDHM", ddhm);// ��������
                    cpcxInfoDynaBean.set("CPCXINFO_LCKH", lckh);// ���̿���
                    cpcxInfoDynaBean.set("CPCXINFO_SCRWID", scrwId);// ����ID
                    cpcxInfoDynaBean.set("CPCXINFO_RWDH", scrw);// ���񵥺�
                    cpcxInfoDynaBean.set("CPCXINFO_GDSL", gdsl);// ��������
                    cpcxInfoDynaBean.set("CPCXINFO_GDSL", ddsl);// ��������
                    cpcxInfoDynaBean.set("CPCXINFO_RQ", currentDate);//����

                    jgmesCommon.setDynaBeanInfo(cpcxInfoDynaBean);

                } else {
                    czsl = cpcxInfoDynaBean.getInt("CPCXINFO_CZSL");
                }
                cpcxInfoDynaBean.set("CPCXINFO_WCSJ", jgmesCommon.getCurrentTime());// ���ʱ��
                cpcxInfoDynaBean.set("CPCXINFO_CZSL", czsl + 1);// ��վ����

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
            result.setMessage("�����Ʒ�����������������ϸ��Ϣ���£�" + e.toString());
        }
        return result;
    }


    /*
     * ���������ӱ��淽��--�������桢������
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
     * �ӱ���
     * */
    public void doJsonSaveBGSJZB() throws ParseException {
        String jsonArrStr = request.getParameter("jsonStrDetail");
        String userCode = request.getParameter("userCode");
        ;
        System.out.println("jsonArrStr:" + jsonArrStr);

        if (doJsonSaveBgSjZb(jsonArrStr, userCode)) {
            toWrite(jsonBuilder.returnSuccessJson("\"��ӳɹ�\""));
        } else {
            toWrite(jsonBuilder.returnFailureJson("\"���ʧ��\""));
        }
    }

    /*
     * ������
     * */
    public void doJsonSaveBGSJ() throws ParseException {
        String jsonString = request.getParameter("jsonStr");
        String userCode = request.getParameter("userCode");
        ;
        System.out.println("jsonString:" + jsonString);

        if (doJsonSaveBgSj(jsonString, userCode)) {
            toWrite(jsonBuilder.returnSuccessJson("\"��ӳɹ�\""));
        } else {
            toWrite(jsonBuilder.returnFailureJson("\"���ʧ��\""));
        }
    }

    /*
     * ͨ�õ����淽��,Ĭ�Ͻ���λ��Ϣ��������Ϣ����
     * */
    public void doJsonSaveBGSJAll() throws ParseException {
        String mac = request.getParameter("mac");// MAC��ַ
        String userCode = request.getParameter("userCode");// �û����
        String jsonString = request.getParameter("jsonStr");
        String jsonStrDetail = request.getParameter("jsonStrDetail");
        String jsonStr = "";

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();

        System.out.println("jsonString:" + jsonString);
        // У��Ϸ��ԣ��Ƿ��ص�½
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                ret = doJsonSaveBgSjAll(jsonString, jsonStrDetail, userCode, mac, JgmesEnumsBgsjLx.GwSj, false);
            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                // TODO: handle exception
                ret.IsSuccess = false;
                ret.setMessage("����!" + e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);

            toWrite(jsonStr);
        }
    }


    /*
     * �ؼ������������վ
     * */
    public void doJsonSaveBGSJAllForBatchCode() throws ParseException {
        String mac = request.getParameter("mac");// MAC��ַ
        String userCode = request.getParameter("userCode");// �û����
        String jsonString = request.getParameter("jsonStr");
        String jsonStrDetail = request.getParameter("jsonStrDetail");
        String jsonStr = "";

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();

        System.out.println("jsonString:" + jsonString);
        // У��Ϸ��ԣ��Ƿ��ص�½
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                ret = doJsonSaveBgSjAll(jsonString, jsonStrDetail, userCode, mac, JgmesEnumsBgsjLx.GwSj, true);
            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                // TODO: handle exception
                ret.IsSuccess = false;
                ret.setMessage("����!" + e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);

            toWrite(jsonStr);
        }
    }

    /*
     * ά�޽������
     * */
    public void doJsonSaveWxAll() throws ParseException {
        String mac = request.getParameter("mac");// MAC��ַ
        String userCode = request.getParameter("userCode");// �û����
        String jsonString = request.getParameter("jsonStr");
        String jsonStrDetail = request.getParameter("jsonStrDetail");
        String jsonStr = "";

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<String> ret = new JgmesResult<String>();

        System.out.println("jsonString:" + jsonString);
        // У��Ϸ��ԣ��Ƿ��ص�½
        if (doCheck(userCode, mac).IsSuccess) {
            try {

                ret = doJsonSaveWxAll(jsonString, jsonStrDetail, userCode, mac);
//				ret = doJsonSaveBgSjAll(jsonString, jsonStrDetail, userCode, mac, JgmesEnumsBgsjLx.FxSj);

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                // TODO: handle exception
                ret.IsSuccess = false;
                ret.setMessage("����!" + e.toString());
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
        String cpCode = "";//��Ʒ���
        String barCode = "";//����
        String fhgygxId = "";//���ع��չ���ID
        String fhcxId = "";//���ز���ID
        String wxzId = "";//����վID
        String fxdId = "";//����վID

        List<DynaBean> detailList = new ArrayList<DynaBean>();

        String jsonStrBg = "";
        String jsonArrBg = "";
        //???

        try {
            DynaBean fxdDynaBean = new DynaBean();
            fxdDynaBean = jgmesCommon.updateDynaBeanByJsonStr(fxdDynaBean, jsonStr);
            if (fxdDynaBean == null) {
                jgmesResult.setMessage("��������Ϊ�գ�");
            }
            if (jgmesResult.IsSuccess) {
                barCode = fxdDynaBean.getStr("FXD_CPTMH");
                fxdId = fxdDynaBean.getStr("JGMES_PB_FXD_ID");
                if (barCode == null || barCode.isEmpty()) {
                    jgmesResult.setMessage("����Ϊ�գ�");
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
                    jgmesResult.setMessage("���޵������ڣ�");
                }
            }
            if (jgmesResult.IsSuccess) {
                pkValue = fxdDynaBean.getStr("JGMES_PB_FXD_ID");
                fxdDynaBean = jgmesCommon.updateDynaBeanByJsonStr(fxdDynaBean, jsonStr);// �ô��������������
                if (pkValue == null || pkValue.isEmpty()) {// ������
                    jgmesResult.setMessage("���޵�ID�����ڣ�");
                }
            }
            //����վ��Ϣ
            if (jgmesResult.IsSuccess) {
                wxzId = fxdDynaBean.getStr("JGMES_PB_FXZ_ID");
                if (wxzId == null || wxzId.isEmpty()) {
                    jgmesResult.setMessage("����վIDδ���룡");
                } else {
                    DynaBean wxzDynaBean = serviceTemplate.selectOneByPk("JGMES_PB_FXZ", wxzId);
                    if (wxzDynaBean != null) {
                        fxdDynaBean.setStr("FXD_FXZBH", wxzDynaBean.getStr("FXZ_FXZBH"));
                        fxdDynaBean.setStr("FXD_FXZMC", wxzDynaBean.getStr("FXZ_FXZMC"));
                    } else {
                        jgmesResult.setMessage("���ݷ���վID[" + wxzId + "]��ȡ����վʧ�ܣ������Ƿ��ظ���");
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
                    System.out.println("��������[" + barCode + "]��ȡ��Ʒʧ�ܣ����������Ƿ���ڻ��Ƿ����ظ��ģ�");
                    jgmesResult.setMessage("��������[" + barCode + "]��ȡ��Ʒʧ�ܣ����������Ƿ���ڻ��Ƿ����ظ��ģ�");
                }
            }
            String fhgxCode = "";
            String gylxId = "";
            if (jgmesResult.IsSuccess) {
                //���ع�����Ϣ
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
                        jgmesResult.setMessage("���ݷ��ع�����[" + fhgxCode + "]��ȡ���չ���ʧ�ܣ����飡");
                    }
                }
            }
            if (jgmesResult.IsSuccess) {
                //���ع�λ��Ϣ--Ҫͨ�����ع����ȡ
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
                        System.out.println("���ݷ��ع�����[" + fhgxCode + "]��ȡ��λ����ʧ�ܣ����飡");
                        jgmesResult.setMessage("���ݷ��ع�����[" + fhgxCode + "]��ȡ��λ����ʧ�ܣ����飡");
                    }
                }
            }
            if (fxdDynaBean != null && !"WXJG03".equals(fxdDynaBean.getStr("FXD_WXJG_CODE"))) {
                if (jgmesResult.IsSuccess) {
                    //���ز�����Ϣ
                    DynaBean cxDynaBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ", " and JGMES_BASE_CXSJ_ID='" + fhcxId + "'");
                    if (cxDynaBean != null) {
                        fxdDynaBean.setStr("FXD_FHCXBH", cxDynaBean.getStr("CXSJ_CXBM"));
                        fxdDynaBean.setStr("FXD_FHCXMC", cxDynaBean.getStr("CXSJ_CXMC"));
                        fxdDynaBean.setStr("FXD_FHCXID", cxDynaBean.getStr("JGMES_BASE_CXSJ_ID"));
                    } else {
                        jgmesResult.setMessage("���ݷ��ز���ID[" + fhcxId + "]��ȡ��������ʧ�ܣ����飡");
                    }
                }
            }


            //������µ����ݿ���
            if (jgmesResult.IsSuccess) {
                // ά�޽����ͨ������ά�޽���ֶλ�д
                jgmesCommon.setDynaBeanDicByCode(fxdDynaBean, "JGMES_MES_WXJG", "FXD_WXJG_CODE", "FXD_WXJG_ID",
                        "FXD_WXJG_NAME");

                if (fxdDynaBean.getStr("FXD_WXJG_CODE").equals("WXJG01") || fxdDynaBean.getStr("FXD_WXJG_CODE").equals("WXJG03")) {
                    fxdDynaBean.set("FXD_WXZT_CODE", "WXZT04");
                }

                // ά��״̬
                jgmesCommon.setDynaBeanDicByCode(fxdDynaBean, "JGMES_DIC_WXZT", "FXD_WXZT_CODE", "FXD_WXZT_ID",
                        "FXD_WXZT_NAME");

                jgmesCommon.setDynaBeanInfo(fxdDynaBean);
                fxdDynaBean_jg = serviceTemplate.update(fxdDynaBean);// ����
            }

            //���޵���ϸ��
            if (jgmesResult.IsSuccess) {
                String key = "";
                String value = "";
                if (jsonStrDetail != null && !jsonStrDetail.isEmpty()) {
                    net.sf.json.JSONArray ja1 = net.sf.json.JSONArray.fromObject(jsonStrDetail);
                    if (ja1.size() > 0) {
                        for (int i = 0; i < ja1.size(); i++) {
                            DynaBean fxdDetail = new DynaBean();
                            fxdDetail.setStr(beanUtils.KEY_TABLE_CODE, tabCodeDetail);

                            JSONObject jb1 = ja1.getJSONObject(i); // ���� jsonarray ���飬��ÿһ������ת�� json ����
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
            //��������
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
            //�����ӱ�
            if (jgmesResult.IsSuccess) {
                //�ݲ�����
            }

            //�洢��������
            if (jgmesResult.IsSuccess) {
                JgmesResult<HashMap> jgRes = doJsonSaveBgSjAll(jsonStrBg, jsonArrBg, userCode, mac, JgmesEnumsBgsjLx.FxSj, false);
                if (!jgRes.IsSuccess) {
                    jgmesResult.setMessage(jgRes.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error(JgmesCommon.getExceptionDetail2(e));
            // TODO: handle exception
            jgmesResult.setMessage("����[doJsonSaveWxAll]����ʧ�ܣ���ϸԭ�����£�" + e.toString());
        }

        return jgmesResult;
    }

    /*
     * ͨ�õ����淽��,Ĭ�Ͻ���λ��Ϣ��������Ϣ���棬���Ӷ������棬����ʹ��
     * */
    public void doJsonSaveBGSJAllA() throws ParseException {
        String jsonString = request.getParameter("jsonStr");
        String jsonStrDetail = request.getParameter("jsonStrDetail");
        String userCode = request.getParameter("userCode");//�û����
        System.out.println("jsonString:" + jsonString);

        if (doJsonSaveBgSjAllA(jsonString, jsonStrDetail, userCode)) {
            toWrite(jsonBuilder.returnSuccessJson("\"��ӳɹ�\""));
        } else {
            toWrite(jsonBuilder.returnFailureJson("\"���ʧ��\""));
        }
    }

    /*
     * ���ò��ߵĵ�ǰ���������Ų�����
     */
    public void doSaveCurrentCxScrw() {
        String mac = getParamStr("mac");// MAC��ַ
        String userCode = getParamStr("userCode");// ���߱���
        String cxCode = request.getParameter("cxCode");
        String cxId = request.getParameter("cxId");
        String scrwId = request.getParameter("scrwId");
        String jsonStr = "";

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<String> ret = new JgmesResult<String>();

        if ((cxCode == null || cxCode.isEmpty()) && (cxId == null || cxId.isEmpty())) {
            ret.IsSuccess = false;
            ret.setMessage("���߱��롢����ID���ܶ�Ϊ�գ�");
        }
        if (scrwId == null || scrwId.isEmpty()) {
            ret.IsSuccess = false;
            ret.setMessage("��������ID����Ϊ�գ�");
        }
        // У��Ϸ��ԣ��Ƿ��ص�½
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
					//�����û�н����׼�����
					DynaBean scrwDynaBean1 = serviceTemplate.selectOneByPk("JGMES_PLAN_SCRW", scrwId);
					if(scrwDynaBean1!=null){
						String message=jgmesCommon.checkSJ(cxCode,scrwDynaBean1.getStr("SCRW_RWDH"));
						if(message!=null&&!"".equals(message)){
							ret.setMessage(message);
							jsonStr = jsonBuilder.toJson(ret);
							toWrite(jsonStr);
						}
					}*/

                    //���ò���������������������ͣ��
                    List<DynaBean> list = serviceTemplate.selectList("JGMES_PLAN_SCRW", " and SCRW_CXBM='" + cxCode + "' and JGMES_PLAN_SCRW_ID<>'" + scrwId + "' and SCRW_RWZT_CODE='" + JgmesEnumsDic.ScDoing.getKey() + "'");
                    if (list != null && list.size() > 0) {
                        for (int i = 0; i < list.size(); i++) {
                            DynaBean scrwDynaBean = list.get(0);
                            doSaveScrwZt(userCode, scrwDynaBean.getStr("JGMES_PLAN_SCRW_ID"), JgmesEnumsDic.ScPause.getKey(), "");
                        }
                    }

                    // �洢�Ų�״̬
                    doSaveScrwZt(userCode, scrwId, JgmesEnumsDic.ScDoing.getKey(), "");
                    ret.IsSuccess = true;
                } else {
                    ret.IsSuccess = false;
                    ret.setMessage("��ȡ�������ݴ���");
                }

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                // TODO: handle exception
                ret.IsSuccess = false;
                ret.setMessage("����" + e.toString());
            }

            jsonStr = jsonBuilder.toJson(ret);
            toWrite(jsonStr);
        }
    }

    /*
     * ��ǰ����״̬����
     */
    public void doSaveScrwZt() {
        String mac = getParamStr("mac");// MAC��ַ
        String userCode = getParamStr("userCode");// ���߱���
        String scrwId = request.getParameter("scrwId");
        String scrwZt = request.getParameter("scrwZt");
        String ztyyZ = request.getParameter("ztyyZ");
        String jsonStr = "";

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<String> ret = new JgmesResult<String>();
        // У��Ϸ��ԣ��Ƿ��ص�½
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                // �洢�Ų�״̬
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
     * �����Ų���״̬
     */
    public void doSaveScrwZt(String userCode, String scrwId, String scrwZt, String ztyyZ) {
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<String> ret = new JgmesResult<String>();
        try {
            System.out.println("��������ID��" + scrwId);
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

                if (scrwZt.equalsIgnoreCase(JgmesEnumsDic.ScFinished.getKey()) || scrwZt.equalsIgnoreCase(JgmesEnumsDic.ScPause.getKey())) {//���ʱɾ������ָ��������
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
                //����״̬
//				DynaBean gdlbDynaBean = serviceTemplate.selectOneByPk("JGMES_PLAN_GDLB", scrwDynaBean.getStr("JGMES_PLAN_GDLB_ID"));
//				if (gdlbDynaBean != null) {
//					jgmesCommon.setDynaBeanDicScrwZt(gdlbDynaBean, pcdZt, "GDLB_PCZT_ID", "GDLB_PCZT_CODE",
//						"GDLB_PCZT_NAME");
//					serviceTemplate.update(gdlbDynaBean);
//				}

                //�Ų���־
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
            toWrite("����״̬����" + jsonBuilder.toJson(ret));
        }
    }

    /*
     * ��ȡ������Ϣ���š����ߡ���λ���û���
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
     * �Ų���־���棭���ݲ���
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

            // �����Ų�״̬
            if (pcztValue != null && !pcztValue.isEmpty()) {

                jgmesCommon.setDynaBeanDicPcZt(pcrzDynaBean, pcztValue, "PCRZ_PCZT_ID", "PCRZ_PCZT_CODE",
                        "PCRZ_PCZT_NAME");
            }
            // ������ͣԭ��
            if (reansonStr != null && !reansonStr.isEmpty()) {
                jgmesCommon.setDynaBeanDic(pcrzDynaBean, "JGMES_DIC_PCZTYY", reansonStr, "PCRZ_PCZTYY_ID",
                        "PCRZ_PCZTYY_CODE", "PCRZ_PCZTYY_NAME");
            }

            jgmesCommon.setDynaBeanInfo(pcrzDynaBean);
        } else {
            jgmesResult = false;
        }

        System.out.println("�Ų���־���ݣ�" + jsonBuilder.toJson(pcrzDynaBean));
        return pcrzDynaBean;
    }


    /*
     * ���ݲ��߻�ȡ�Ų��б�
     * */
    public void getScrw() {
        String mac = getParamStr("mac");// MAC��ַ
        String userCode = getParamStr("userCode");// �û�����
        String cxCode = getParamStr("cxCode");// ���߱���
        String cxId = getParamStr("cxId");// ����ID
        String curDate = getParamStr("curDate");// ����
        String curDateSection = getParamStr("curDateSection");// ��������
        String zt = getParamStr("zt");// ��������״̬
        String noLike = getParamStr("noLike");// ���Źؼ���ģ��(���������š����񵥺�)
        String cpLike = getParamStr("cpLike");// ��Ʒ�ؼ���ģ����ѯ(��Ʒ���롢��Ʒ���ƺͲ�Ʒ���)
        String pageSize = getParamStr("PageSize");// ÿҳ��
        String currPage = getParamStr("CurrPage");// ��ǰҳ
        String jsonStr = "";

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();

        // У��Ϸ��ԣ��Ƿ��ص�½
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                JgmesResult<List<DynaBean>> rets = getScrw(userCode, cxId, cxCode, curDate, zt, noLike, cpLike, pageSize, currPage, curDateSection);
                List<DynaBean> dicList = rets.Data;

                ret.Data = ret.getValues(dicList);
                ret.TotalCount = rets.TotalCount;

                // jsonStr = jsonBuilder.buildListPageJson(new Long(dicList.size()), dicList,
                // true);
                System.out.println("����[" + cxCode + "]�����б�");
                System.out.println(jsonStr);

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                // TODO: handle exception
                ret.IsSuccess = false;
                ret.setMessage("������ϸ��Ϣ��" + e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);
            toWrite(jsonStr);
        }
    }


    /*
     * ���ݲ��߻�ȡ�Ų��б����ҹ��˵�û�м����ĵ�����
     * */
    public void getScrwByBLX() {
        String mac = getParamStr("mac");// MAC��ַ
        String userCode = getParamStr("userCode");// �û�����
        String cxCode = getParamStr("cxCode");// ���߱���
        String cxId = getParamStr("cxId");// ����ID
        String curDate = getParamStr("curDate");// ����
        String curDateSection = getParamStr("curDateSection");// ��������
        String zt = getParamStr("zt");// ��������״̬
        String noLike = getParamStr("noLike");// ���Źؼ���ģ��(���������š����񵥺�)
        String cpLike = getParamStr("cpLike");// ��Ʒ�ؼ���ģ����ѯ(��Ʒ���롢��Ʒ���ƺͲ�Ʒ���)
        String pageSize = getParamStr("PageSize");// ÿҳ��
        String currPage = getParamStr("CurrPage");// ��ǰҳ
        String jsonStr = "";

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();

        // У��Ϸ��ԣ��Ƿ��ص�½
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
                System.out.println("����[" + cxCode + "]�����б�");
                System.out.println(jsonStr);

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                // TODO: handle exception
                ret.IsSuccess = false;
                ret.setMessage("������ϸ��Ϣ��" + e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);
            toWrite(jsonStr);
        }
    }


    /*
     * ��ȡ�������񣬲��ҹ��˵��������������
     * */
    public void getScrwByGX() {
        String mac = getParamStr("mac");// MAC��ַ
        String userCode = getParamStr("userCode");// �û�����
        String cxCode = getParamStr("cxCode");// ���߱���
        String cxId = getParamStr("cxId");// ����ID
        String curDate = getParamStr("curDate");// ����
        String curDateSection = getParamStr("curDateSection");// ��������
        String zt = getParamStr("zt");// ��������״̬
        String noLike = getParamStr("noLike");// ���Źؼ���ģ��(���������š����񵥺�)
        String cpLike = getParamStr("cpLike");// ��Ʒ�ؼ���ģ����ѯ(��Ʒ���롢��Ʒ���ƺͲ�Ʒ���)
        String pageSize = getParamStr("PageSize");// ÿҳ��
        String currPage = getParamStr("CurrPage");// ��ǰҳ
        String jsonStr = "";

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();

        // У��Ϸ��ԣ��Ƿ��ص�½
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
                System.out.println("����[" + cxCode + "]�����б�");
                System.out.println(jsonStr);

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                // TODO: handle exception
                ret.IsSuccess = false;
                ret.setMessage("������ϸ��Ϣ��" + e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);
            toWrite(jsonStr);
        }
    }

    /**
     * �������񵥺Ż�ȡ��������
     * rwNo ���񵥺�
     * mac mac��ַ
     * userCode �û�����
     *
     * @see /jgmes/commonAction!getScrwByRwNo.action
     */
    public void getScrwByRwNo() {
        // MAC��ַ
        String mac = request.getParameter("mac");
        // �û�����  ����
        String userCode = request.getParameter("userCode");
        // ���񵥺�
        String rwNo = request.getParameter("rwNo");

        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
        String jsonStr = "";
        // У��Ϸ��ԣ��Ƿ��ص�½
        if (doCheck(userCode, mac).IsSuccess) {
            if (rwNo != null && !"".equals(rwNo)) {
                DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and SCRW_RWDH = '" + rwNo + "'");
                if (scrwDynaBean != null) {
                    ret.Data = scrwDynaBean.getValues();
                }
            } else {
                ret.setMessage("δ��ȡ���������񵥺ţ�");
            }
        }
        jsonStr = jsonBuilder.toJson(ret);
        toWrite(jsonStr);
    }


    /*
     * ��ȡ�����Ų��б�������ͣ��
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
        //��ȡϵͳ�����������ϲ����������ʱ��
        DynaBean xtcsBean = serviceTemplate.selectOne("JGMES_XTGL_XTCS", " and XTCS_CXFL2_CODE='WLBLBGTIME'");
        String time = "";
        if (xtcsBean != null && xtcsBean.getStr("XTCS_NO_CODE").equals("1")) {//�����ڿ�������״̬
            time = xtcsBean.getStr("XTCS_CSZ");//��ȡ����ֵ
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
                if (zt.equals("RWZT03")) {//��Ϊ�������״̬
                    if (StringUtil.isNotEmpty(time)) {//��ϵͳ�������ò�����ֵ�������ʱ��У��
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
            System.out.println("��ȡ���������б�" + sql);
            sql += " and SCRW_PCRQ !=' ' order by b.SY_ORDERINDEX,a.SCRW_PCRQ desc";

            sql = "select a.* from JGMES_PLAN_SCRW a\r\n" +
                    "left join je_core_dictionaryitem b on a.SCRW_RWZT_CODE = b.dictionaryitem_itemcode\r\n" +
                    "LEFT JOIN je_core_dictionary c on c.DICTIONARY_ITEMROOT_ID = b.sy_parent\r\n" +
                    "where c.DICTIONARY_DDCODE = 'JGMES_DIC_RWZT' " + sql;
            //dicList = serviceTemplate.selectList("JGMES_PLAN_SCRW",sql);
            dicList = serviceTemplate.selectListBySql(sql);

            ret.TotalCount = Long.valueOf(dicList.size());

			/*
			//ÿҳ��
			if (pageSize == null || pageSize.isEmpty()) {
				pageSize = "10";
			}
			//��ǰҳ
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
        String state = request.getParameter("state");//״̬
        String jtmc = request.getParameter("JtName");//��̨����
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
//		List<DynaBean> dynaBeans = serviceTemplate.selectListBySql(userSQL);//���ݵ�ǰ��¼�û���ȡ�ܴ�����쳣����
//		String yclx = "";
//		if (dynaBeans!=null){
//			for (DynaBean bean : dynaBeans) {
//				yclx+="'"+bean.getStr("YCLXFP_JTYCLX_CODE")+"',";
//			}
//		}else{
//			return ;
//		}
//		yclx=yclx.substring(0,yclx.length()-1);//ȥ�����һ������
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
            if (bean.getStr("JTDA_JTSYZT_CODE").equals("2")) {//������Ϊ��ɫ
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
     * ��ȡ�����Ų��б�������ͣ��
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
     * ��ѯ�쳣��¼���������
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
        String user = userCode;//��ȡ��ǰ��¼�û�
        String userSQL = "select * from JGMES_ADMK_YCLXFP where YCLXFP_YCCLRBM like '%" + userCode + "%'";
        List<DynaBean> dynaBeans = serviceTemplate.selectListBySql(userSQL);//���ݵ�ǰ��¼�û���ȡ�ܴ�����쳣����
        String yclx = "";
        if (dynaBeans.size() > 0) {
            for (DynaBean bean : dynaBeans) {
                yclx += "'" + bean.getStr("YCLXFP_JTYCLX_CODE") + "',";
            }
        } else {
            return ret;
        }
        yclx = yclx.substring(0, yclx.length() - 1);//ȥ�����һ������
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
     * ����״̬
     */
    public void changeAbnormalState() {
        String userCode = request.getParameter("userCode");
        String mac = request.getParameter("mac");
        String pageSize = request.getParameter("pageSize");
        String currPage = request.getParameter("currPage");
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        String state = request.getParameter("state");//����״̬
        String id = request.getParameter("id");//����id
        String jth = request.getParameter("jth");//��̨��
        String yclx = request.getParameter("yclx");//�쳣����
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
                String tzSql = "update JGMES_ADMK_JTYCTZ set JTYCTZ_YDR='" + userCode + "',JTYCTZ_YDSJ=now(),JTYCTZ_YDZT_CODE='3',JTYCTZ_YDZT_NAME='����' where JGMES_ADMK_JTYCJL_ID='" + id + "'";
                pcServiceTemplate.executeSql(tzSql);
            }
            if (state.equals("CLZT07")) {
                String tzSql = "update JGMES_ADMK_JTYCTZ set JTYCTZ_YDR='" + userCode + "',JTYCTZ_YDSJ=now(),JTYCTZ_YDZT_CODE='2',JTYCTZ_YDZT_NAME='�Ѷ�' where JGMES_ADMK_JTYCJL_ID='" + id + "'";
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
     * ��ȡ�����ֵ伯��
     * */
    public void getDictionary() throws ParseException {
//		Map ss = request.getParameterMap();
//		JSONObject dd = JSONObject.fromObject(requestParams);
        String mac = getParamStr("mac");// MAC��ַ
        String userCode = getParamStr("userCode");// �û�
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        String parentCode = getParamStr("parentCode");// ����CODE@
        String parentId = getParamStr("parentId");// ����ID
        Long count = (long) 0;
        String jsonStr = "";

        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        // У��Ϸ��ԣ��Ƿ��ص�½
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
            System.out.println("�����ֵ��б�");
            System.out.println(jsonStr);
            toWrite(jsonStr);
        }
    }

    /*
     * ���ݲ�Ʒ��Ż�ȡ�ò�Ʒ��Ӧ�Ĺ����б�
     * */
    public void getGXList() {
        String mac = getParamStr("mac");// MAC��ַ
        String userCode = getParamStr("userCode");// �û�
        String cpCode = request.getParameter("cpCode");// ��Ʒ���
        String isBackWorkStation = request.getParameter("isBackWorkStation");// �Ƿ�Ϊ���ع�վ
        String jsonStr = "";
        String cpgylxId = "";// ��Ʒ����·��ID
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        // У��Ϸ��ԣ��Ƿ��ص�½
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
        String cpgylxId = "";// ��Ʒ����·��ID
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
                System.out.println("cpgylxIdΪ��");
            }
        } else {
            System.out.println("���Ϊ[" + cpCode + "]cpsjΪ��");
        }
        return gylxList;
    }


    /*
     * ���ݲ�Ʒ��Ż�ȡ�ò�Ʒ��Ӧ���в�����Ĺ����б�
     * */
    public void getGXListByCpBh() {
        String mac = getParamStr("mac");// MAC��ַ
        String userCode = getParamStr("userCode");// �û�
        String cpCode = request.getParameter("cpCode");// ��Ʒ���
        String jsonStr = "";
        String cpgylxId = "";// ��Ʒ����·��ID
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        // У��Ϸ��ԣ��Ƿ��ص�½
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
        String cpgylxId = "";// ��Ʒ����·��ID
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
                    logger.debug("���鹤���Ƿ�󶨲������" + cpCode);
                    //ret.setErrorDicbyProperties("101");
                }
//		jsonStr = jsonBuilder.buildListPageJson(new Long(gylxList.size()), gylxList, false);
            } else {
                logger.debug("δ�Ӳ�Ʒ�л�ȡ������·�ߣ�" + cpCode);
                //ret.setErrorDicbyProperties("100");
            }
        } else {
            logger.debug("δ��ȡ����Ʒ���ݣ�" + cpCode);
            //ret.setErrorDicbyProperties("1000");
        }

        return ret;
    }

    /*
     *
     *     ���ݹ���ID��ȡ��Ӧ�Ĳ��������б�
     */
    public List<DynaBean> getBlDynaBean(String gxlxgxId) {
        List<DynaBean> blDynaBean = null;
        //��ȡ�����ⲻ����
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
     * ���ݲ�Ʒ��š������ȡ������������б�
     */

    public void getGXBLXMList() {
        String mac = getParamStr("mac");// MAC��ַ
        String userCode = getParamStr("userCode");// ���߱���
        String cpCode = request.getParameter("cpCode");// ��Ʒ���
        String gxId = request.getParameter("gxId");// ����ID���Ǳ����ͨ����ǰ�û�ȥ������ȡ��
        String gxCode = request.getParameter("gxCode");// �����ţ��Ǳ����ͨ����ǰ�û�ȥ������ȡ��
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        String jsonStr = "";
        System.out.println(mac + "----" + userCode + "---" + gxId + "----" + gxCode + "---" + cpCode);
        // У��Ϸ��ԣ��Ƿ��ص�½
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
                    System.out.println("��Ʒ��ţ�[" + cpCode + "]������ID��[" + gxId + "]��������룺[" + gxCode + "]���û���[" + userCode
                            + "]�Ĺ���Ŀ�б�gxblxmListΪ��");
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
     * ���ݲ�Ʒ��š������ȡ�����������б�
     */

    public void getGXWLXMList() {
        String mac = getParamStr("mac");// MAC��ַ
        String userCode = getParamStr("userCode");// ���߱���
        String cpCode = request.getParameter("cpCode");// ��Ʒ���
        String gxId = request.getParameter("gxId");// ����ID���Ǳ����ͨ����ǰ�û�ȥ������ȡ��
        String gxCode = request.getParameter("gxCode");// �����ţ��Ǳ����ͨ����ǰ�û�ȥ������ȡ��
        String jsonStr = "";
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        System.out.println(mac + "----" + userCode + "---" + gxId + "----" + gxCode + "---" + cpCode);
        // У��Ϸ��ԣ��Ƿ��ص�½
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
                    System.out.println("��Ʒ��ţ�[" + cpCode + "]������ID��[" + gxId + "]��������룺[" + gxCode + "]���û���[" + userCode
                            + "]�Ĺ���Ŀ�б�gxwlxmListΪ��");
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
     * ���ݲ�Ʒ��š������ȡ��Ӧ�Ĺ�����Ŀ�б�
     * */
    public void getGXXMList() {
        String mac = getParamStr("mac");// MAC��ַ
        String userCode = getParamStr("userCode");// ���߱���
        String cpCode = request.getParameter("cpCode");// ��Ʒ���
        String gxId = request.getParameter("gxId");// ����ID���Ǳ����ͨ����ǰ�û�ȥ������ȡ��
        String gxCode = request.getParameter("gxCode");// �����ţ��Ǳ����ͨ����ǰ�û�ȥ������ȡ��
        String jsonStr = "";
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        // У��Ϸ��ԣ��Ƿ��ص�½
        if (doCheck(userCode, mac).IsSuccess) {
            JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();

            try {

                List<DynaBean> gxxmList = getGXXMList(cpCode, gxId, gxCode, userCode);
                if (gxxmList != null && gxxmList.size() > 0) {
//			jsonStr = jsonBuilder.buildListPageJson(new Long(gxxmList.size()), gxxmList, false);
                    ret.Data = ret.getValues(gxxmList);
                    ret.TotalCount = (long) gxxmList.size();

                } else {
                    System.out.println("��Ʒ��ţ�[" + cpCode + "]������ID��[" + gxId + "]��������룺[" + gxCode + "]���û���[" + userCode
                            + "]�Ĺ���Ŀ�б�gxxmListΪ��");
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
     * ��ȡ��ǰ������Ŀ�б�
     * */
    public void getCurrentGXXMList() {
        String mac = getParamStr("mac");//MAC��ַ
        String userCode = getParamStr("userCode");//�û�
        String jsonStr = "";

        // У��Ϸ��ԣ��Ƿ��ص�½
        if (doCheck(userCode, mac).IsSuccess) {

            List<DynaBean> gxxmList = getGXXMList(null, null, null, userCode);
            if (gxxmList != null && gxxmList.size() > 0) {
                jsonStr = jsonBuilder.buildListPageJson(new Long(gxxmList.size()), gxxmList, false);
            }

            toWrite(jsonStr);
        }
    }

    // ��ȡ��ǰ�������������
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
            System.out.println("getCurrentGXXMList��������ID��" + gxId);
            gxblxmList = serviceTemplate.selectList("JGMES_GYGL_GXBLX", " and GYLXGX_ID='" + gxId + "'");
        }
        return gxblxmList;
    }

    // ��ȡ��ǰ���������б�
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
     * ������������
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
     * ��ȡ��ǰ�Ų��б�
     */
    public void getCurrentPCLB() {
        String mac = getParamStr("mac");//MAC��ַ
        String userCode = request.getParameter("userCode");
        String cxCode = request.getParameter("cxCode");
        List<DynaBean> dicList = new ArrayList<DynaBean>();
        String jsonStr = "";
        JgmesResult<List<HashMap>> jgmesResult = new JgmesResult<List<HashMap>>();
        // У��Ϸ��ԣ��Ƿ��ص�½
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
                jgmesResult.setMessage("��ȡ��ǰ�Ų��б����" + e.toString());
            }
            jsonStr = jsonBuilder.toJson(jgmesResult);

            toWrite(jsonStr);
        }
    }


    /*
     * ��ȡ��ǰ���������б�
     */
    public void getCurrentScrw() {
        String mac = getParamStr("mac");//MAC��ַ
        String userCode = request.getParameter("userCode");
        String cxCode = request.getParameter("cxCode");
        List<DynaBean> dicList = new ArrayList<DynaBean>();
        String jsonStr = "";
        JgmesResult<List<HashMap>> jgmesResult = new JgmesResult<List<HashMap>>();

        // У��Ϸ��ԣ��Ƿ��ص�½
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
                jgmesResult.setMessage("��ȡ��ǰ���������б����" + e.toString());
            }
            jsonStr = jsonBuilder.toJson(jgmesResult);

            toWrite(jsonStr);
        }
    }


    /*
     * ��ȡ��ǰ��Ʒ�����Ų��ĵ�һ����¼
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
     * ��ȡ��ǰ��Ʒ�������ݲ���ָ����������������ȡ
     */
    private DynaBean getCurrentProductByCxCp(String userCode) {
        DynaBean scrwDynaBean = getScrwByCxCp(userCode, null, null);
        return scrwDynaBean;
    }


    /*
     * ��ȡ��ǰ��Ʒ�������ݲ���ָ����������������ȡ
     */
    public JgmesResult<HashMap> getCurrentScrwProduct(String userCode, String cxId, String cxCode, String barCode) {

        return getScrwProduct(userCode, cxId, cxCode, barCode);

    }


    /*
     * ��ȡ��ǰ��Ʒ�������ݲ���ָ����������������ȡ
     */

    /**
     * ��ȡ��ǰ��Ʒ�������ݲ���ָ����������������ȡ(���������Ļ���������������ȡ)
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
                //��ȡ��������
                scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and JGMES_PLAN_SCRW_ID = '" + tmScDynaBean.getStr("JGMES_PLAN_SCRW_ID") + "'");
            }
        } else {
            List<DynaBean> scrwDynaBeanList = serviceTemplate.selectList("JGMES_PLAN_SCRW", " and SCRW_CXBM = '" + cxCode + "' and SCRW_RWZT_CODE = 'RWZT02' order by SY_ORDERINDEX desc,SCRW_SJKGSJ desc");
            if (scrwDynaBeanList != null && scrwDynaBeanList.size() > 0) {
                scrwDynaBean = scrwDynaBeanList.get(0);
            }
        }
        if (scrwDynaBean == null) {
            System.out.println("��ǰ����δָ����������");
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
     * ��ȡ��ǰ��Ʒ�������ݲ���ָ����������������ȡ,
     */
    private DynaBean getScrwByCxCp(String userCode, String cxId, String cxCode) {
        DynaBean scrwDynaBean = new DynaBean();
        DynaBean cxDynaBean = new DynaBean();
        JgmesResult<String> jgmesResult = new JgmesResult<String>();

        if (cxId != null && !cxId.isEmpty()) {//��ID��ͨ��ID��ȡ����
            cxDynaBean = serviceTemplate.selectOneByPk("JGMES_BASE_CXSJ", cxId);
        } else {
            if (cxCode != null && !cxCode.isEmpty()) {//��ID��CODE��ͨ��CODE��ȡ����
                cxDynaBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ", " and CXSJ_CXBM='" + cxCode + "'");
            } else {//��û�����ȡ��ǰ�Ĳ���
                cxDynaBean = getCurrentCX(userCode);
            }
        }

        if (cxDynaBean != null) {
            //scrwDynaBean =serviceTemplate.selectOneByPk("JGMES_PLAN_SCRW", cxDynaBean.getStr("JGMES_PLAN_SCRW_ID"));

            scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and SCRW_CXBM = '" + cxDynaBean.getStr("CXSJ_CXBM") + "' and SCRW_RWZT_CODE = 'RWZT02'");
            if (scrwDynaBean == null) {
                jgmesResult.setMessage("��ǰ����û�������е���������");
                System.out.println("��ǰ����û�������е���������");
                toWrite(jsonBuilder.toJson(jgmesResult));
            } else {//���ͻ���Ʒ�ŴӲ�Ʒ�����л�ȡ����
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
            jgmesResult.setMessage("��������Ϊ�գ�");
            System.out.println("getScrwByCxCp��������Ϊ�գ�");
            toWrite(jsonBuilder.toJson(jgmesResult));
        }

        return scrwDynaBean;
    }

    /*
     * ͨ�������ȡ����������������ֻ�ܰ��Ų��Ļ�ȡ���������ݿɲ���
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
     * ��ȡ��ǰ��Ʒ
     */
    public void getCurrentProduct() {
        String mac = getParamStr("mac");//MAC��ַ
        String userCode = getParamStr("userCode");//���߱���

        // У��Ϸ��ԣ��Ƿ��ص�½
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
        String mac = getParamStr("mac");// MAC��ַ
        String userCode = getParamStr("userCode");// �û�����
        String scrwId = getParamStr("scrwId");// �Ų���ID
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
        String jsonStr = "";
        // У��Ϸ��ԣ��Ƿ��ص�½
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
     * ���ݲ���ָ���Ĳ�Ʒ����ȡ��ǰ��λ�Ĳ�Ʒ��Ϣ
     */
    public void getCurrentProductByCxCp() {
        String mac = getParamStr("mac");// MAC��ַ
        String userCode = getParamStr("userCode");// �û�����
        String cxId = getParamStr("cxId");// ����ID
        String cxCode = getParamStr("cxCode");// ���߱���
        String barCode = getParamStr("barCode");// ��Ʒ�����
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
        String jsonStr = "";

        // У��Ϸ��ԣ��Ƿ��ص�½
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
     * ��ȡ��ǰ����
     */
    private DynaBean getCurrentCX(String userCode) {
        DynaBean cxDynaBean = new DynaBean();
        DynaBean gw = getCurrentGW(userCode);
        String cxId = "";
        if (gw != null) {
            cxDynaBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ", " and JGMES_BASE_CXSJ_ID='" + gw.getStr("JGMES_BASE_CXSJ_ID") + "'");
        } else {
            System.out.println("��ȡ��λʧ�ܣ�");
        }
        return cxDynaBean;
    }

    /*
     * ��ȡ��ǰ����
     */
    public void getCurrentCx() {
        String mac = getParamStr("mac");//MAC��ַ
        String userCode = getParamStr("userCode");//���߱���

        // У��Ϸ��ԣ��Ƿ��ص�½
        if (doCheck(userCode, mac).IsSuccess) {

            DynaBean cxDynaBean = getCurrentCX(userCode);
            String jsonStr = jsonBuilder.toJson(cxDynaBean);
            toWrite(jsonStr);
        }
    }

    /*
     * ��ȡ��ǰ�û�
     */
    private String getCurrentUserCode(String userCode) {
        if (userCode == null || userCode.isEmpty()) {
            JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
            userCode = jgmesCommon.jgmesUser.getCurrentUserCode();
        }
        return userCode;
    }

    /*
     * ��ȡ��ǰ��λ
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
            System.out.println("[" + userCode + "]��Ӧ�ĵ�½����δ�ҵ���");
        }

        return gw;
    }

    /*
     * ��ȡ��ǰ��λ--����
     */
    public void getCurrentGw() {
        String mac = getParamStr("mac");//MAC��ַ
        String userCode = getParamStr("userCode");//���߱���
        // У��Ϸ��ԣ��Ƿ��ص�½
        if (doCheck(userCode, mac).IsSuccess) {

            DynaBean gwDynaBean = getCurrentGW(userCode);
            String jsonStr = jsonBuilder.toJson(gwDynaBean);
            toWrite(jsonStr);
        }
    }

    /*
     * ��ȡ��ǰ����˵��GXGL_GYLX_GXID�ֶ�Ϊ����·�߹���ID����̨ר�ã�GXGL_ID����Ǳ���Ĺ���ID�������ط���Ҫʹ��
     */
    public DynaBean getCurrentGX(String cpCode, String userCode) {
        DynaBean gx = new DynaBean();
        DynaBean gw = getCurrentGW(userCode);
        DynaBean cPGWGX = null;
        String gxid = "";
        if (gw != null) {
            //��ȡ��Ʒ����·�� add 2019-05-09
            DynaBean cpDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " and PRODUCTDATA_BH = '" + cpCode + "'");
            if (cpDynaBean == null) {
                logger.error("��ȡ��ǰ����ӿڣ�δ���ݲ�Ʒ���룺" + cpCode + "��ȡ����Ʒ��Ϣ");
                return gx;
            }

            //���ݹ�λ��Ϣ����Ʒ��Ϣ��ȡ������Ϣ
//			List<DynaBean> list = serviceTemplate.selectList("JGMES_BASE_CPGWGX", " and CPGWGX_CPBH='"+cpCode+"' and JGMES_BASE_GW_ID='"+gw.getStr("JGMES_BASE_GW_ID")+"'");
            List<DynaBean> list = serviceTemplate.selectList("JGMES_BASE_CPGWGX", " and CPGWGX_CPBH='" + cpCode + "' and JGMES_BASE_GW_ID='" + gw.getStr("JGMES_BASE_GW_ID") + "' and CPGWGX_CPGYLXID = '" + cpDynaBean.getStr("PRODUCTDATA_CPGYLXID") + "'");
            if (list != null && list.size() > 0) {
                cPGWGX = list.get(0);
                if (list.size() > 1) {
                    System.out.println("��Ʒ[" + cpCode + "]����λ[" + gw.getStr("GW_GWBH") + "]�ж�����¼��");
                }
            }
//			DynaBean cPGWGX = serviceTemplate.selectOne("JGMES_BASE_CPGWGX", " and CPGWGX_CPBH='"+cpCode+"' and JGMES_BASE_GW_ID='"+gw.getStr("JGMES_BASE_GW_ID")+"'");
            if (cPGWGX != null) {
                gxid = cPGWGX.getStr("CPGWGX_GXID");
                System.out.println("��ǰ����" + gxid);
                if (gxid != null && !gxid.isEmpty()) {
                    gx = serviceTemplate.selectOne("JGMES_GYGL_GXGL", " and GXGL_ID='" + gxid + "'");
                    if (gx != null) {
                        gx.setStr("GXGL_SOP", "");
                    }
                    //��ȡ����·�����湤���SOP
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
                    System.out.println("�Ӳ�Ʒ��λ�����Ӧ��[" + cPGWGX + "]��δ��ȡ������ID���ݣ�");
                }
            } else {
                System.out.println("��Ʒ[" + cpCode + "]����λ[" + gw.getStr("GW_GWBH") + "]�޶�Ӧ�Ĳ�Ʒ��λ�����Ӧ������");
            }
        }
        return gx;
    }

    /*
     * ��ȡ��ǰ���򣭣�����
     */
    public void getCurrentGx() {
        String mac = getParamStr("mac");//MAC��ַ
        String userCode = getParamStr("userCode");// �û�
        String cpCode = request.getParameter("cpCode");// ��Ʒ���

        // У��Ϸ��ԣ��Ƿ��ص�½
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
     * ��ȡ���е�ά��վ�ļ���
     */
    public void getWXZList() {
        String mac = getParamStr("mac");// MAC��ַ
        String userCode = getParamStr("userCode");// �û�����
        String cxCode = getParamStr("cxCode");// ���߱��
        String cxId = getParamStr("cxId");// ����ID
        String fxzLx = getParamStr("fxzLx");// ����վ����ֵ
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        String jsonStr = "";
        // У��Ϸ��ԣ��Ƿ��ص�½
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
     * ��ȡ���е�ά��վ�ļ���
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
     * ��ȡ��ȡ����վ��Ŀ����
     */
    public void getWXZXMList() {
        String mac = getParamStr("mac");// MAC��ַ
        String userCode = getParamStr("userCode");// �û�����
        String fxzId = getParamStr("fxzId");// ����վID
        String fxzCode = getParamStr("fxzCode");// ����վID
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        String jsonStr = "";

        // У��Ϸ��ԣ��Ƿ��ص�½
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
     * ��ȡ��ȡ����վ��Ŀ����
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
     * ����ά��վID������ȡά��վ
     */
    public void getWXZ() {
        String mac = getParamStr("mac");// MAC��ַ
        String userCode = getParamStr("userCode");// �û�����
        String wxzCode = getParamStr("wxzCode");// �û�����
        String wxzId = getParamStr("wxzId");// �û�����
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
        String jsonStr = "";
        // У��Ϸ��ԣ��Ƿ��ص�½
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
     * ����ά��վID������ȡά��վ
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
     *���������ȡ��ز�Ʒ��Ϣ�ӿڣ����ز�Ʒ��Ϣ��ά����Ŀ��ϸ
     */
    public void getWXZCpInfo() {
        String mac = getParamStr("mac");// MAC��ַ
        String userCode = getParamStr("userCode");// �û�����
        String barCode = getParamStr("barCode");// ����
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
        String jsonStr = "";
        // У��Ϸ��ԣ��Ƿ��ص�½
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
            System.out.println("���޵����ݣ�" + jsonStr);

            toWrite(jsonStr);
        }
    }


    /*
     * ����ά��վID������ȡ���޵���ϸ��Ϣ
     */
    private HashMap<String, Object> getWXZCpInfo(String barCode) {
        HashMap<String, Object> res = new HashMap<String, Object>();
        String pcdId = "";
        JgmesResult<List<HashMap>> result = new JgmesResult<List<HashMap>>();
        List<DynaBean> fxdDynaBeanList = serviceTemplate.selectList("JGMES_PB_FXD", " and FXD_CPTMH='" + barCode + "' and FXD_WXZT_CODE='" + JgmesEnumsDic.WxZtToDo.getKey() + "'");
        if (fxdDynaBeanList != null && fxdDynaBeanList.size() > 0) {

            res.put("cpInfo", fxdDynaBeanList.get(0).getValues());//�ĳ�ֱ�ӷ��ط��޵�����
            List<DynaBean> fxdzbList = serviceTemplate.selectList("JGMES_PB_FXDZB", " and JGMES_PB_FXD_ID='" + fxdDynaBeanList.get(0).getStr("JGMES_PB_FXD_ID") + "' order by SY_ORDERINDEX");
            if (fxdzbList != null && fxdzbList.size() > 0) {
                res.put("fxXmList", result.getValues(fxdzbList));
                System.out.println("���޵��ӱ�����������" + fxdzbList.size());
                System.out.println("���޵��ӱ����ݣ�" + jsonBuilder.toJson(result.getValues(fxdzbList)));
            }
        }

        return res;
    }


    /*
     * ��ȡ�쳣�����б�
     */
    public void getYcClList() {
        String mac = getParamStr("mac");// MAC��ַ
        String userCode = getParamStr("userCode");// �û�����
        String cpCode = getParamStr("cpCode");// ��Ʒ����
        String ycztCode = getParamStr("ycztCode");// �쳣����״̬
        String gxCode = getParamStr("gxCode");// �������
        String gwCode = getParamStr("gwCode");// ��λ����
        String cxCode = getParamStr("cxCode");// ���ߴ���
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        String jsonStr = "";

        // У��Ϸ��ԣ��Ƿ��ص�½
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
     * ��ȡ�쳣�����б�
     */
    private List<DynaBean> getYcClList(String userCode, String cpCode, String ycztCode, String gxCode, String gwCode, String cxCode) {
        List<DynaBean> adycList = new ArrayList<DynaBean>();
        String sql = "";
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);

        if ((cpCode != null && !cpCode.isEmpty()) || (gxCode != null && !gxCode.isEmpty()) || (gwCode != null && !gwCode.isEmpty()) || (cxCode != null && !cxCode.isEmpty())) {
            ///??
        } else {//����ǰ��Ʒ��ǰ��������ȥ��ȡ
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
     * �����쳣����ID������ȡ�쳣���� ������
     */
    public void getYcClById() {
        String mac = getParamStr("mac");// MAC��ַ
        String userCode = getParamStr("userCode");// �û�����
        String ycclCode = getParamStr("ycclCode");//
        String ycclId = getParamStr("ycclId");//
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
        String jsonStr = "";
        // У��Ϸ��ԣ��Ƿ��ص�½
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
     * �����쳣����ID������ȡ�쳣���� ������
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
     * ���� �쳣����
     */
    public void doJsonSaveYcCl() {

        String mac = request.getParameter("mac");// MAC��ַ
        String userCode = request.getParameter("userCode");// �û����
        String jsonStr = request.getParameter("jsonStr");

        String tabCode = "JGMES_PB_ADYCCL";
        String pkValue = "";

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();

        System.out.println("jsonString:" + jsonStr);
        // У��Ϸ��ԣ��Ƿ��ص�½
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                DynaBean ycclDynaBean = jgmesCommon.getDynaBeanByJsonStr(tabCode, jsonStr);
                // �����ֶ������︳ֵ�������ֵ䡢���ߡ����򡢹�λ����Ʒ����Ӧ��Ϣ��Ҫ��ӵ�ǰ
                // ����ϵͳ����
                jgmesCommon.setDynaBeanDicByCode(ycclDynaBean, "JGMES_DIC_ADLX", "ADYCCL_ADLX_CODE", "ADYCCL_ADLX_ID",
                        "ADYCCL_ADLX_NAME");
                // �쳣״̬
                jgmesCommon.setDynaBeanDicByCode(ycclDynaBean, "JGMES_DIC_YCZT", "ADYCCL_YCZT_CODE", "ADYCCL_YCZT_ID",
                        "ADYCCL_YCZT_NAME");
                // �쳣ԭ�򣭣����εڶ���
                jgmesCommon.setDynaBeanDicByCode(ycclDynaBean, "JGMES_DIC_ADLX", "ADYCCL_ADLXMX_CODE",
                        "ADYCCL_ADLXMX_ID", "ADYCCL_ADLXMX_NAME");

                // �����Ʒ
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

                // �������
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
                // ������
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
                // ����λ
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
                    //����ʱ��
                    String clsj = "";
                    clsj = ycclDynaBean.getStr("ADYCCL_CLSJ");
                    if (clsj == null || clsj.isEmpty()) {
                        ycclDynaBean.set("ADYCCL_CLSJ", jgmesCommon.getCurrentTime());
                    }
                    serviceTemplate.update(ycclDynaBean);
                } else {
                    //�ᱨʱ��
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
                ret.setMessage("����!" + e.toString());
            }
            jsonStr = jsonBuilder.toJson(ret);

            toWrite(jsonStr);
        }

    }


    /*
     * ��ȡ��Ʒ׷���б�
     */
    public void getScList() {
        String mac = getParamStr("mac");// MAC��ַ
        String userCode = getParamStr("userCode");// �û�����
        String barCode = getParamStr("barCode");// ����

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        String jsonStr = "";
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();

        // У��Ϸ��ԣ��Ƿ��ص�½
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
     * ��ȡ��Ʒ׷���б��ӱ���װ��������һ�𷵻�
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
     * ��ȡ���������б�
     */
    public void getScJdList() {
        String mac = getParamStr("mac");// MAC��ַ
        String userCode = getParamStr("userCode");// �û�����
        String cpCode = getParamStr("cpCode");// ��Ʒ����
        String rwztCode = getParamStr("rwztCode");// ��������״̬
        String gxCode = getParamStr("gxCode");// �������
        String gwCode = getParamStr("gwCode");// ��λ����
        String cxCode = getParamStr("cxCode");// ���ߴ���
        String scrwNo = request.getParameter("scrwNo");// ��������
        String cpLike = request.getParameter("cpLike");// ��Ʒģ����ѯ�ֶ�(��Ʒ���롢��Ʒ���ơ���Ʒ���)
        String pageSize = request.getParameter("PageSize");// ÿҳ��
        String currPage = request.getParameter("CurrPage");// ��ǰҳ
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        String jsonStr = "";

        // У��Ϸ��ԣ��Ƿ��ص�½
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
     * ��ȡ���������б�
     */
    private JgmesResult<List<HashMap>> getScJdList(String userCode, String cpCode, String rwztCode, String cxCode, String scrwNo, String cpLike, String pageSize, String currPage) {
        List<DynaBean> scjdList = new ArrayList<DynaBean>();

        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        String sql = "";
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);

        if ((cpCode != null && !cpCode.isEmpty()) || (cxCode != null && !cxCode.isEmpty())) {
            //??
        } else {//����ǰ��Ʒ��ǰ��������ȥ��ȡ
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
            //sql +=jgmesCommon.getSqlWhere("SCRW_RWDH",scrwNo);//���񵥺�
            //sql +=" and SCRW_RWDH like '%"+scrwNo+"%'";//���񵥺�

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
            System.out.println("��ǰҳ�ͣ�ÿҳ���װ����int��ʧ�ܣ���������ȷ�ĵ�ǰҳ��ÿҳ�룡");
        }

        sql += " LIMIT " + curr + "," + size;
        scjdList = serviceTemplate.selectList("JGMES_PLAN_SCRW", sql);
        if (scjdList != null && scjdList.size() > 0) {
            ret.Data = ret.getValues(scjdList);
        }

        return ret;
    }

    /*
     * ��ȡ���ߡ���λ�б�
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
        // MAC��ַ
        String mac = request.getParameter("mac");
        // �û�����  ����
        String userCode = request.getParameter("userCode");
        // ���񵥺�
        String cxCode = request.getParameter("cxCode");

        JgmesResult<List<HashMap>> ret = new JgmesResult<>();
        String jsonStr = "";
        // У��Ϸ��ԣ��Ƿ��ص�½
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
                ret.setMessage("δ��ȡ�����߱��룡");
            }
        }
        jsonStr = jsonBuilder.toJson(ret);
        toWrite(jsonStr);
    }

    /*
     * ��ȡ���ߡ���λ�б�
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
     * ��ȡ���ߡ���λ�б��ҹ��˵�û����������Ĳ���
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
     * ��ȡ���ߡ���λ�б�
     */
    private List<DynaBean> getCxGwListFilterOutNoScrws() {
        List<DynaBean> cxList = new ArrayList<DynaBean>();
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        DynaBean cxDynaBean = new DynaBean();

        //�޸���20190408  ����
        //List<DynaBean> List = serviceTemplate.selectList("JGMES_BASE_CXSJ", " order by CXSJ_CXMC");
        //���˵�û����������Ĳ���
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
     * ��λ���԰�
     */
    public void doSaveFirst() {

        String cxCode = request.getParameter("cxCode");// ���߱��
        String gwCode = request.getParameter("gwCode");// ��λ����
        String mac = request.getParameter("mac");// MAC��ַ

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
                ret.setMessage("MAC��ַ����Ϊ�գ�");
                toWrite(jsonBuilder.toJson(ret));
            } else {
                if (gwCode == null || gwCode.isEmpty()) {
                    ret.setMessage("��λ����Ϊ�գ�");
                } else {
                    List<DynaBean> list = serviceTemplate.selectList("JGMES_EQ_STATIONMACHINE",
                            " and STATIONMACHINE_MACDZ='" + mac + "'");
                    if (list != null && list.size() > 1) {
                        ret.setMessage("MAC��ַ�ظ������ں�̨����");
                        toWrite(jsonBuilder.toJson(ret));
                    } else {
                        if (list != null && list.size() == 1) {
                            gwjDynaBean = list.get(0);
                            isInsert = false;
                        }

                        // ȷ����λ��Ψһ��
                        List<DynaBean> gwjList = serviceTemplate.selectList("JGMES_EQ_STATIONMACHINE",
                                " and STATIONMACHINE_GWBH='" + gwCode + "' and STATIONMACHINE_MACDZ<>'" + mac + "'");
                        if (gwjList != null && gwjList.size() > 0) {
                            ret.setMessage("�ù�λ�ѱ���λ����[" + gwjList.get(0).getStr("STATIONMACHINE_MACDZ") + "]�󶨣�");
                            toWrite(jsonBuilder.toJson(ret));
                        } else {
                            gwjDynaBean.setStr("STATIONMACHINE_MACDZ", mac);

                            // ���ù�λ��Ϣ
                            DynaBean gwDynaBean = serviceTemplate.selectOne("JGMES_BASE_GW",
                                    " and GW_GWBH='" + gwCode + "'");
                            if (gwDynaBean != null) {
                                // gwjDynaBean.set("JGMES_BASE_CXSJ_ID", gwDynaBean.getPkValue());
                                gwjDynaBean.set("STATIONMACHINE_GWBH", gwDynaBean.getStr("GW_GWBH"));
                                gwjDynaBean.set("STATIONMACHINE_GWMC", gwDynaBean.getStr("GW_GWMC"));
                                if (cxCode == null || cxCode.isEmpty()) {
                                    cxCode = gwDynaBean.getStr("GW_CXBH");
                                }
                                // ���ò�����Ϣ
                                DynaBean cxDynaBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ",
                                        " and CXSJ_CXBM='" + cxCode + "'");
                                if (cxDynaBean != null) {
                                    gwjDynaBean.set("JGMES_BASE_CXSJ_ID", cxDynaBean.getPkValue());
                                    gwjDynaBean.set("STATIONMACHINE_CXBM", cxDynaBean.getStr("CXSJ_CXBM"));
                                    gwjDynaBean.set("STATIONMACHINE_CXMC", cxDynaBean.getStr("CXSJ_CXMC"));
                                }
                            }

                            if (isInsert) {// ����
                                // ��λ�����
                                // serviceTemplate.buildCode("STATIONMACHINE_GWJBM", "JGMES_EQ_STATIONMACHINE",
                                // gwjDynaBean);
                                gwjDynaBean.set("STATIONMACHINE_GWJBM", serviceTemplate.buildCode("STATIONMACHINE_GWJBM",
                                        "JGMES_EQ_STATIONMACHINE", gwjDynaBean));
                                gwjDynaBean.setStr("STATIONMACHINE_GWJMC", "ϵͳ����");

                                jgmesCommon.setDynaBeanDic(gwjDynaBean, "JGMES_USESTATUS", "0", null,
                                        "STATIONMACHINE_USESTATUS_CODE", "STATIONMACHINE_USESTATUS_NAME");
                                jgmesCommon.setDynaBeanDic(gwjDynaBean, "JGMES_YES_NO", "0", null, "STATIONMACHINE_SFYDY",
                                        null);

                                gwjDynaBean.set(beanUtils.KEY_TABLE_CODE, "JGMES_EQ_STATIONMACHINE");

                                jgmesCommon.setDynaBeanInfo(gwjDynaBean);

                                serviceTemplate.insert(gwjDynaBean);
                            } else {// ����
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
            ret.setMessage("����!" + e.toString());
        }
        jsonStr = jsonBuilder.toJson(ret);

        toWrite(jsonStr);

    }

    /*
     * ��ȡ�����ӡ�Ļ�����Ϣ
     */
    public void getGxPrintInfo() {
        String userCode = request.getParameter("userCode");// �û����
        String mac = request.getParameter("mac");// MAC��ַ
        String cpCode = request.getParameter("cpCode");// ��Ʒ����

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
        String jsonStr = "";

        // У��Ϸ��ԣ��Ƿ��ص�½
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
     * ��ȡ�����ӡ�Ļ�����Ϣ
     */
    private DynaBean getGxPrintInfo(String userCode, String mac, String cpCode) {
        DynaBean yyggDynaBean = new DynaBean();
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        DynaBean gwjDynaBean = new DynaBean();// ��λ��
        String yyggId = "";// Ӧ�ù���ID
        String tmLxId = "";// ��������ID

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
     * ��ȡ��λ��
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
     * ��ȡ�����ӡ�Ļ�����Ϣ
     */
    public void getPrintInfo() {
        String userCode = request.getParameter("userCode");// �û����
        String mac = request.getParameter("mac");// MAC��ַ
        String printQty = request.getParameter("printQty");// ��ӡ����
        String barCodes = request.getParameter("barCodes");// ���뼯��
        String cpCode = request.getParameter("cpCode");// ��Ʒ����

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
        String jsonStr = "";

        // У��Ϸ��ԣ��Ƿ��ص�½
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
     * ��ȡ�����ӡ�Ļ�����Ϣ
     * printQty������ӡ������barCodes���������ֵ�����������뼯�ϣ�Ҳ��������Ҫ��ӡ�����룬cpCode������Ʒ���
     */
    private JgmesResult<HashMap> getPrintInfo(String userCode, String mac, String printQty, String barCodes, String cpCode) {
        JgmesResult<HashMap> res = new JgmesResult<HashMap>();
        HashMap data = new HashMap();
        DynaBean yyggDynaBean = new DynaBean();
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        DynaBean gwjDynaBean = getCurrentGwj(userCode);// ��λ��
        String printTFilePath = "";// ��ӡģ���ļ���ַ�����·����
        String bqcs = "";// �����ǩ����ģ��
        String printParams = "";// ���յĴ�ӡ����
        List<DynaBean> gdcptmList = new ArrayList<DynaBean>();
        DynaBean gdcptmDynaBean = new DynaBean();
        DynaBean gdcptmDynaBean_jg = new DynaBean();
        String scrwID = "";// ��������ID
        String tm = "";// ����
        String inBarcode = "";// ������׸�����
        String strUrlPre = request.getScheme() + "://" + request.getServerName();
        JSONObject json = new JSONObject();
        JSONArray jsonArr = new JSONArray();
        String sqlWhere = "";
        String tmscfs = "";
        boolean isCreateByIn = false;// �Ƿ񰴴���ֵ����

        if (request.getServerPort() != 80) {
            strUrlPre += ":" + request.getServerPort();
        }

        if (printQty == null || printQty.isEmpty()) {//��ӡ������Ϊ��Ĭ�ϴ�ӡ1��
            printQty = "1";
        }
        if (gwjDynaBean != null) {
            data.put("Address", gwjDynaBean.getStr("STATIONMACHINE_DYJJKDZ"));//�ӹ�λ���������л�ȡ��ӡ��ַ
        }
        if (barCodes != null && !barCodes.isEmpty()) {
            if (barCodes.indexOf(",") != -1) {
                inBarcode = barCodes.substring(0, barCodes.indexOf(",") - 1);
            } else {
                inBarcode = barCodes;
            }
        }
        System.out.println("######################################################:��ʼ�����ӡ");
        yyggDynaBean = getGxPrintInfo(userCode, mac, cpCode);
        if (yyggDynaBean != null) {
            tmscfs = yyggDynaBean.getStr("CPTMYYGG_BARCODEMODEL_CODE");
            isCreateByIn = tmscfs != null && tmscfs.equalsIgnoreCase(JgmesEnumsDic.CreateByIn.getKey());
            printTFilePath = yyggDynaBean.getStr("CPTMYYGG_BQMB");
            bqcs = yyggDynaBean.getStr("CPTMYYGG_BQCS");
            System.out.println(bqcs + "-----bqcs");

            String zxQty = yyggDynaBean.getStr("CPTMYYGG_MTMSL"); //��ȡÿ��������

            if (bqcs != null && !bqcs.isEmpty()) {// ��ǩ������Ϊ��

                DynaBean scrwDynaBean = getScrwByBarcode(inBarcode, userCode, null, null);
                System.out.println("######################################################:��ȡ��������");
                if (!isCreateByIn) {// �����������ɷ�ʽ�жϣ�����ǰ������������������´�ӡ���
                    if (scrwDynaBean != null) {
                        System.out.println("######################################################:��ȡ��������ɹ���");
                        scrwID = scrwDynaBean.getStr("JGMES_PLAN_SCRW_ID");
                        System.out.println("######################################################:��������ID:" + scrwID);
                        sqlWhere = " and GDCPTM_SFYDY=0 and GDCPTM_CPBH='" + cpCode + "' and JGMES_PLAN_SCRW_ID='"
                                + scrwID + "' and GDCPTM_TMLX_CODE='" + yyggDynaBean.getStr("CPTMYYGG_TMLX_CODE")
                                + "' order by GDCPTM_TMH";
//						if (isCreateByIn) {
//							sqlWhere = " and GDCPTM_CPBH='" + cpCode + "' and JGMES_PLAN_SCRW_ID='" + scrwID
//									+ "' order by GDCPTM_TMH";
//						}
                        System.out.println("######################################################:��������sql:" + sqlWhere);
                        // ��ȡ��С�����������¼
                        gdcptmList = serviceTemplate.selectList("JGMES_BASE_GDCPTM", sqlWhere, 0, 1);
                        if (gdcptmList != null && gdcptmList.size() > 0) {
                            // ������������ĳ��Ѵ�ӡ
                            gdcptmDynaBean = gdcptmList.get(0);
                            gdcptmDynaBean.set("GDCPTM_SFYDY", 1);
                            gdcptmDynaBean_jg = serviceTemplate.update(gdcptmDynaBean);

                            tm = gdcptmDynaBean_jg.getStr("GDCPTM_TMH");
                        } else {
                            res.setMessage("û����Ҫ��ӡ�����룬�����Ƿ���������δ��ӡ�����룡");
                            System.out.println("û����Ҫ��ӡ�����룡" + sqlWhere);
                        }
                    } else {
                        res.setMessage("δ��ȡ����ǰ��λ����������");
                        System.out.println("�������񵥺�Ϊ�գ�");
                    }

                } else {//������ֵ����
                    tm = inBarcode;
                }

                System.out.println("��ǰҪ��ӡ�������ǣ�" + tm);
                data.put("BarCode", tm);
                printParams = bqcs;
                System.out.println("######################################################:��ʼ�����ӡ��" + printParams);
                // ��ȡ��Ӧ����Ĵ������
//						String str = "{'FilePath':'@filePath@','ColQty':'@colQty@','Copies':'@copies@','cpName':'@JGMES_BASE_PRODUCTDATA.PRODUCTDATA_NAME@','cpCode':'@JGMES_BASE_PRODUCTDATA.PRODUCTDATA_CPFL_NAME@','BatchTmList':'@BatchTmList@','Tm1':'@tm@','TmList':[@tmList@]}";
                String cpbh = yyggDynaBean.getStr("CPTMYYGG_CPBH");// ��ȡ�ӱ�Ĳ�Ʒ���
                DynaBean productData = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA",
                        " AND  PRODUCTDATA_BH='" + cpbh + "'");// ���ݴӱ�Ĳ�Ʒ����������������ó������DynaBean
                List<String> list = new ArrayList<String>();
                String regex = "@[^\\.@]*\\.[^@\\.]*@";// ��ȡ���ָ�ʽ���ַ�����@JGMES_BASE_PRODUCTDATA.PRODUCTDATA_NAME@
                Pattern pattern = Pattern.compile(regex);// ����������ʽ��ȡ�ַ���
                Matcher m = pattern.matcher(printParams);
                while (m.find()) {
                    int i = 1;
                    list.add(m.group(i - 1));
                    i++;
                }
                if (!list.isEmpty() || list.size() != 0) {
                    for (String match : list) {//ѭ�����е� �ֶ�
//					        	System.out.println(string);//���ӣ�@JGMES_BASE_PRODUCTDATA.PRODUCTDATA_NAME@
                        String s = match.substring(1, match.length() - 1);// JGMES_BASE_PRODUCTDATA.PRODUCTDATA_NAME
                        s = s.replaceAll(" ", "");//ȥ���ַ����еĿո�
                        String[] split = s.split("\\.");// split[0]=JGMES_BASE_PRODUCTDATA��split[1]=PRODUCTDATA_NAME
                        String tabName = split[0];
                        String colName = split[1];
                        if (tabName.equalsIgnoreCase("JGMES_BASE_PRODUCTDATA"))// ���ϱ�ʱ
                        {
                            String st = productData.getStr(colName);// ��ȡ����Ķ�Ӧ��������
                            if (st != null && !"".equals(st)) {
                                printParams = printParams.replaceAll(match, st);// ��@JGMES_BASE_PRODUCTDATA.PRODUCTDATA_NAME@�滻�ɶ�ȡ������
                            }
                        } else if (tabName.equalsIgnoreCase("JGMES_PLAN_SCRW")) {// ��ǰ���������ʱ
                            if (scrwDynaBean != null) {
                                printParams = printParams.replaceAll(match, scrwDynaBean.getStr(colName));    //�滻����
                            }
                        } else if (tabName.equalsIgnoreCase("JGMES_PLAN_GDLB")) {// ��ǰ������ʱ
                            if (scrwDynaBean != null) {
                                String gdhm = scrwDynaBean.getStr("SCRW_GDHM");
                                List<DynaBean> gdhmDynaBeanList = serviceTemplate.selectList("JGMES_PLAN_GDLB", " and GDLB_GDHM='" + gdhm + "'");
                                if (gdhmDynaBeanList != null && gdhmDynaBeanList.size() > 0) {
                                    DynaBean gdDynaBean = gdhmDynaBeanList.get(0);
                                    printParams = printParams.replaceAll(match, gdDynaBean.getStr(colName));//�滻����
                                }
                            }
                        }
                    }
                }
                //�滻װ������
                if (zxQty != null && !zxQty.isEmpty() && printParams.indexOf("@zxQty@") != -1) {
                    printParams = printParams.replaceAll("@zxQty@", zxQty);// װ������
                }
                //�滻װ������  @barCodes@
                if (barCodes != null && !barCodes.isEmpty() && printParams.indexOf("@barCodes@") != -1) {
                    printParams = printParams.replaceAll("@barCodes@", barCodes);// ����list
                }
                if (printTFilePath != null && !printTFilePath.isEmpty()) {//ģ���ļ� ��ַ
                    if (printTFilePath.indexOf("*") != -1) {
                        printTFilePath = printTFilePath.substring(printTFilePath.indexOf("*") + 1);
                    }
                    printTFilePath = strUrlPre + printTFilePath;// ģ���ļ��ľ���·��
                    printParams = printParams.replaceAll("@filePath@", printTFilePath);// ģ���ļ����·��
                }
                if (printQty != null && !printQty.isEmpty()) {
                    printParams = printParams.replaceAll("@copies@", printQty);// ��ӡ����
                }
                if (tm != null && !tm.isEmpty()) {
                    printParams = printParams.replaceAll("@tm@", tm);// �����ݿ��ж�̬��ȡ������
                }
                if (barCodes != null && !barCodes.isEmpty() && !isCreateByIn) {// Ҫ���жϣ����������ɹ���ʱ���滻@tmList@

                    barCodes = "\"" + barCodes.replaceAll(",", "\",\"") + "\"";
                    printParams = printParams.replaceAll("@tmList@", barCodes);// �����ݿ��ж�̬��ȡ������

                }
                // ��û���ݵ��滻��Ĭ��ֵ
                if (printParams.indexOf("@colQty@") != -1) {
                    printParams = printParams.replaceAll("@colQty@", "0");
                }
                if (printParams.indexOf("@copies@") != -1) {
                    printParams = printParams.replaceAll("@copies@", "1");
                }
                System.out.println("######################################################:��ʼ�����ӡ2��" + printParams);
                printParams = printParams.replaceAll("@[^@]*@", "");

                data.put("PrintParams", printParams);

            } else {
                res.setMessage("����Ӧ�ù������ģ�����Ϊ�գ�");
                System.out.println("ģ�����[CPTMYYGG_BQCS]Ϊ�գ�");
            }
        } else {
            res.setMessage("δ��ȡ����Ӧ������Ӧ�ù���");
            System.out.println("δ��ȡ����Ӧ������Ӧ�ù���");
        }

        res.Data = data;
        return res;
    }

    /*
     * У���û��Ϸ��ԣ����Ϸ�ֱ����ʾ��
     */
    private JgmesResult<String> doCheck(String userCode, String mac) {
        JgmesResult<String> result = new JgmesCommon(request, serviceTemplate).doCheckRes(mac, userCode);
        if (!result.IsSuccess) {
            toWrite(jsonBuilder.toJson(result));
        }
        return result;
    }

    /*
     * У�����ϣ������Ψһ������Ψһ�Լ���
     */
    public void doCheckWlm() {
        String mac = getParamStr("mac");//MAC��ַ
        String userCode = getParamStr("userCode");//�û�
        String barCode = request.getParameter("barCode");//��������
        String wlCode = request.getParameter("wlCode");//������
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);

        JgmesResult<String> res = jgmesCommon.doCheckWlm(barCode, wlCode);

        toWrite(jsonBuilder.toJson(res));
    }

    /*
     * У������Ψһ��
     */
    public void doCheckWlmUniq() {
        String mac = getParamStr("mac");//MAC��ַ
        String userCode = getParamStr("userCode");//�û�
        String barCode = request.getParameter("barCode");//��������
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);

        JgmesResult<String> res = jgmesCommon.doCheckWlmUniq(barCode);

        toWrite(jsonBuilder.toJson(res));
    }

    /*
     * У���Ƿ񻻲����绻�������²�Ʒ���¹��򼰹�����Ŀ������������Ŀ��
     */
    public void doCheckChangeCp() {
        String mac = getParamStr("mac");//MAC��ַ
        String userCode = getParamStr("userCode");//�û�
        String cpCode = request.getParameter("cpCode");//��Ʒ���
        String barCode = request.getParameter("barCode");//������
        String gygxId = request.getParameter("gygxId");//���չ���ID
        String scrwId = request.getParameter("scrwId");//��������ID

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        JgmesResult<String> jgmesResult = new JgmesResult<String>();
        String currentGxId = "";
        // У��Ϸ��ԣ��Ƿ��ص�½
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
                        //					jgmesResult.setMessage("��ȡ��ǰ�������");
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
            jgmesResult.setMessage("У���Ƿ񻻲�����δ֪��������ϵ����Ա��" + e.toString());
            toWrite(jsonBuilder.toJson(jgmesResult));
        }
    }

    /*
     * У���Ƿ񻻲�
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

                //��ȡ��ǰ���񵥺�
                String newScrwId = "";
                if (jgmesBarCodeBase.tmScDynaBean != null && jgmesBarCodeBase.tmScDynaBean.get("JGMES_PLAN_SCRW_ID") != null) {
                    newScrwId = jgmesBarCodeBase.tmScDynaBean.get("JGMES_PLAN_SCRW_ID").toString();
                }
                logger.error("newScrwId:" + newScrwId + ",scrwId:" + scrwId);

                cpNewCode = jgmesCommon.getCpCodeByBarCode(barCode);
                if (cpCode != null && !cpCode.equalsIgnoreCase(cpNewCode)) {//
                    System.out.println("����Ĳ�Ʒ���[" + cpCode + "]����ͨ������[" + barCode + "]��ȡ �Ĳ�Ʒ���[" + cpNewCode + "]����ͬ��");
                    ret.setErrorDic(JgmesEnumsErr.CheckErrChange);
//	   	    		ret.ErrorCode = JgmesEnumsErr.CheckErrNoMatch.getKey();
//	   	    		ret.setMessage("�������Ʒ��ƥ�䣡");
                    //���������ȡ��Ӧ��������ID���ٻ�ȡ��Ӧ�Ų�����Ϣ���ٸ��ݲ�Ʒ����ǰ��λ��ȡ��ǰ���򡢹�����Ŀ���������ϡ�������Ŀ��
                    ret.Data = getCurrentGxAllInfoByBarCode(userCode, null, cpNewCode);
                } else if (newScrwId != null && !"".equals(newScrwId) && scrwId != null && !"".equals(scrwId) && !newScrwId.equals(scrwId)) {
                    System.out.println("�������������[" + scrwId + "]����ͨ������[" + barCode + "]��ȡ ����������[" + newScrwId + "]����ͬ��");
                    ret.setErrorDic(JgmesEnumsErr.CheckErrChange);
//	   	    		ret.ErrorCode = JgmesEnumsErr.CheckErrNoMatch.getKey();
//	   	    		ret.setMessage("�������Ʒ��ƥ�䣡");
                    //���������ȡ��Ӧ��������ID���ٻ�ȡ��Ӧ�Ų�����Ϣ���ٸ��ݲ�Ʒ����ǰ��λ��ȡ��ǰ���򡢹�����Ŀ���������ϡ�������Ŀ��
                    ret.Data = getCurrentGxAllInfoByBarCode(userCode, null, cpNewCode);
                }
            } else {
                //�������͵�У��
            }
        } else {//��ȡ�������������
            ret.setMessage(jgmesResult.getMessage());
        }
        return ret;
    }

    /**
     * ���������ȡ��Ӧ��������ID���ٻ�ȡ��Ӧ�Ų�����Ϣ���ٸ��ݲ�Ʒ����ǰ��λ��ȡ��ǰ���򡢹�����Ŀ���������ϡ�������Ŀ��
     *
     * @param userCode �û�����
     * @param barCode  ����
     * @param cpCode   ��Ʒ����
     * @return
     */
    private HashMap<String, Object> getCurrentGxAllInfoByBarCode(String userCode, String barCode, String cpCode) {
        HashMap<String, Object> res = new HashMap<String, Object>();
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        if (barCode != null && !barCode.isEmpty()) {
            cpCode = jgmesCommon.getCpCodeByBarCode(barCode);
        }

        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();

        //���������ȡ��Ӧ��������ID���ٻ�ȡ��Ӧ�Ų�����Ϣ���ٸ��ݲ�Ʒ����ǰ��λ��ȡ��ǰ���򡢹�����Ŀ���������ϡ�������Ŀ��
        DynaBean gwDynaBean = getCurrentGW(userCode);
        if (gwDynaBean != null) {
            DynaBean scrwDynaBean = getScrwByBarcode(barCode, userCode, gwDynaBean.getStr("JGMES_BASE_CXSJ_ID"), null);//�ȸ��������ȡ��Ӧ���������׵����������������Ϊ�׵���������ݵ�ǰ����ָ������������
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
     * ��ȡ��һ����λid,��λ����,��λ���
     * ��������Ʒ��ţ�����·��id,����id
     * �޸ģ������������Ҫ����ID������
     */
    private DynaBean nextGWbean(String cpbh, String cpgylxid, String gxid) {
        //	DynaBean selectOne = serviceTemplate.selectOne("JGMES_BASE_CPGWGX"," AND CPGWGX_CPBH='"+cpbh+"' AND CPGWGX_CPGYLXID='"+cpgylxid+"' AND CPGWGX_GXID='"+gxid+"'");
        DynaBean selectOne = serviceTemplate.selectOne("JGMES_BASE_CPGWGX", " AND CPGWGX_CPBH='" + cpbh + "' AND CPGWGX_GXID='" + gxid + "'");
        return selectOne;
    }

    /*
     * �����Ʒ������ϸ�����
     * lxc
     * ����1����Ʒ������ϸ���bean
     * ����2����������bean
     * ����3����������bean
     * ����4��tmType�����������ֶ�
     * ����5��listsize ���ڼ�������
     */
    private void evalDynaBean(DynaBean JGMES_PB_SCDETAIL, DynaBean bgsjDynaBean, List<DynaBean> jgmesGYLXGX, String tmType, int listsize) {
        //��ǰʱ��
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String scrq = formatter.format(new java.util.Date());
        if (listsize == 1) {
            //ͨ��
            JGMES_PB_SCDETAIL.set(tmType, bgsjDynaBean.getStr("BGSJ_TMH"));//��������
            JGMES_PB_SCDETAIL.set("SCDETAIL_TM", bgsjDynaBean.getStr("BGSJ_TMH"));
            JGMES_PB_SCDETAIL.set("SCDETAIL_CPBH", bgsjDynaBean.getStr("BGSJ_CPBH"));
            JGMES_PB_SCDETAIL.set("SCDETAIL_CPMC", bgsjDynaBean.getStr("BGSJ_CPMC"));
            JGMES_PB_SCDETAIL.set("GYLX_ID", bgsjDynaBean.getStr("GYLX_ID"));//����·��
            //JGMES_PB_SCDETAIL.set("JGMES_BASE_CXSJ_ID", (String) bgsjDynaBean.get("BGSJZB_GXMC"));//JGMES_BASE_CXSJ_ID��������_���ID
            JGMES_PB_SCDETAIL.set("SCDETAIL_JSBJ", 0);//������־
            JGMES_PB_SCDETAIL.set("SCDETAIL_DYDGXTM", bgsjDynaBean.getStr("BGSJ_TMH"));//��һ������
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGXID", bgsjDynaBean.getStr("BGSJ_GXID"));//��ǰ����ID
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGXBH", bgsjDynaBean.getStr("BGSJ_GXBH"));//��ǰ������
            DynaBean nextGWbean = null;
            if (jgmesGYLXGX != null && jgmesGYLXGX.size() > 1) {
                JGMES_PB_SCDETAIL.set("SCDETAIL_DQGYLX_GX_WJID", jgmesGYLXGX.get(listsize - 1).getStr("GYLXGX_GYLXGXID"));//��ǰ����·��_����_���ID
                JGMES_PB_SCDETAIL.set("SCDETAIL_GXLX_ID", jgmesGYLXGX.get(listsize - 1).getStr("GYLXGX_GXLX_ID"));//��������_ID
                nextGWbean = nextGWbean(bgsjDynaBean.getStr("BGSJ_CPBH"), bgsjDynaBean.getStr("GYLX_ID"), jgmesGYLXGX.get(listsize).getStr("GYLXGX_GXID"));
            }

            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGXMC", bgsjDynaBean.getStr("BGSJ_GXMC"));//��ǰ��������

            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGWBH", bgsjDynaBean.getStr("BGSJ_GWBH"));//��ǰ��λ���
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGWMC", bgsjDynaBean.getStr("BGSJ_GWMC"));//��ǰ��λ����
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGWID", bgsjDynaBean.getStr("BGSJ_GWID"));//��ǰ��λID

            //��
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
            JGMES_PB_SCDETAIL.set(tmType, bgsjDynaBean.getStr("BGSJ_TMH"));//��������
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGXID", bgsjDynaBean.getStr("BGSJ_GXID"));//��ǰ����ID
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGXBH", bgsjDynaBean.getStr("BGSJ_GXBH"));//��ǰ������
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGYLX_GX_WJID", jgmesGYLXGX.get(listsize - 1).getStr("GYLXGX_GYLXGXID"));//��ǰ����·��_����_���ID
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGXMC", bgsjDynaBean.getStr("BGSJ_GXMC"));//��ǰ��������
            JGMES_PB_SCDETAIL.set("SCDETAIL_GXLX_ID", jgmesGYLXGX.get(listsize - 1).getStr("GYLXGX_GXLX_ID"));//��������_ID
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGWBH", bgsjDynaBean.getStr("BGSJ_GWBH"));//��ǰ��λ���
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGWMC", bgsjDynaBean.getStr("BGSJ_GWMC"));//��ǰ��λ����
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGWID", bgsjDynaBean.getStr("BGSJ_GWID"));//��ǰ��λID
            JGMES_PB_SCDETAIL.set("SCDETAIL_JSSJ", scrq);
            //��----���һ�����򴫿�
            JGMES_PB_SCDETAIL.set("SCDETAIL_XDGXID", "");// jgmesGYLXGX.get(listsize+1).getStr("GYLXGX_GXID")
            JGMES_PB_SCDETAIL.set("SCDETAIL_XDGXBH", "");//jgmesGYLXGX.get(listsize+1).getStr("GYLXGX_GXNUM")
            JGMES_PB_SCDETAIL.set("SCDETAIL_XDGXMC", "");//jgmesGYLXGX.get(listsize+1).getStr("GYLXGX_GXNAME")
            JGMES_PB_SCDETAIL.set("SCDETAIL_XDGWMC", "");
            JGMES_PB_SCDETAIL.set("SCDETAIL_XDGWBH", "");
            JGMES_PB_SCDETAIL.set("SCDETAIL_XDGWID", "");
            JGMES_PB_SCDETAIL.set("SCDETAIL_JSBJ", 1);//������־
            JGMES_PB_SCDETAIL.set("SCDETAIL_JSSJ", scrq);
            String kssj = JGMES_PB_SCDETAIL.getStr("SCDETAIL_KSSJ");
            int hh = 0;
            if (kssj != null && !kssj.isEmpty()) {
                long startT = fromDateStringToLong(kssj); //��ʼʱ��
                long endT = fromDateStringToLong(scrq);  //����ʱ��
                long ss = (endT - startT) / (1000); //��������
                int MM = (int) ss / 60;   //���Ʒ�����
                hh = (int) ss / 3600;  //����Сʱ��
                //int dd=(int)hh/24;   //��������
            }
            JGMES_PB_SCDETAIL.set("SCDETAIL_ZSC", hh);//��ʱ��
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGXSZXL", listsize);
        }
        if (listsize < jgmesGYLXGX.size() && listsize != 1) {

            String BGSJ_TMH = bgsjDynaBean.getStr("BGSJ_TMH");

            JGMES_PB_SCDETAIL.set(tmType, BGSJ_TMH);//��������
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGXID", bgsjDynaBean.getStr("BGSJ_GXID"));//��ǰ����ID
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGXBH", bgsjDynaBean.getStr("BGSJ_GXBH"));//��ǰ������
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGYLX_GX_WJID", jgmesGYLXGX.get(listsize - 1).getStr("GYLXGX_GYLXGXID"));//��ǰ����·��_����_���ID
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGXMC", bgsjDynaBean.getStr("BGSJ_GXMC"));//��ǰ��������
            JGMES_PB_SCDETAIL.set("SCDETAIL_GXLX_ID", jgmesGYLXGX.get(listsize - 1).getStr("GYLXGX_GXLX_ID"));//��������_ID
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGWBH", bgsjDynaBean.getStr("BGSJ_GWBH"));//��ǰ��λ���
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGWMC", bgsjDynaBean.getStr("BGSJ_GWMC"));//��ǰ��λ����
            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGWID", bgsjDynaBean.getStr("BGSJ_GWID"));//��ǰ��λID
            JGMES_PB_SCDETAIL.set("SCDETAIL_JSSJ", scrq);
            //��
            JGMES_PB_SCDETAIL.set("SCDETAIL_XDGXID", jgmesGYLXGX.get(listsize).getStr("GYLXGX_GXID"));
            JGMES_PB_SCDETAIL.set("SCDETAIL_XDGXBH", jgmesGYLXGX.get(listsize).getStr("GYLXGX_GXNUM"));
            JGMES_PB_SCDETAIL.set("SCDETAIL_XDGXMC", jgmesGYLXGX.get(listsize).getStr("GYLXGX_GXNAME"));
            DynaBean nextGWbean = nextGWbean(bgsjDynaBean.getStr("BGSJ_CPBH"), bgsjDynaBean.getStr("GYLX_ID"), jgmesGYLXGX.get(listsize).getStr("GYLXGX_GXID"));
            //��
            JGMES_PB_SCDETAIL.set("SCDETAIL_XDGWID", nextGWbean.get("JGMES_BASE_GW_ID"));
            JGMES_PB_SCDETAIL.set("SCDETAIL_XDGWBH", nextGWbean.get("CPGWGX_GWBH"));
            JGMES_PB_SCDETAIL.set("SCDETAIL_XDGWMC", nextGWbean.get("CPGWGX_GWMC"));

            JGMES_PB_SCDETAIL.set("SCDETAIL_DQGXSZXL", listsize);
            JGMES_PB_SCDETAIL.set("SCDETAIL_JSSJ", scrq);
        }
    }


    public long fromDateStringToLong(String inVal) { // �˷�������ʱ�����
        java.util.Date date = null; // ����ʱ������????? ?
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            if (inVal != null && !inVal.isEmpty()) {
                date = inputFormat.parse(inVal); // ���ַ���ת����������
            }
        } catch (Exception e) {
            logger.error(JgmesCommon.getExceptionDetail2(e));
            e.printStackTrace();
        }
        if (date != null) {
            return date.getTime(); // ���غ�����
        } else {
            return 0;
        }

    }


    /*
     * ��ȡ��������ǰʱ��
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
     * ��ȡ��λ��������    ��������������
     */
    public void getGwQtyByRWDH() {
        String userCode = request.getParameter("userCode");//�Ǳ��Ϊ�����ȡ��ǰ��½�û�
        String mac = request.getParameter("mac");          //��ǰMAC��ַ������
        String gwCode = request.getParameter("gwCode");    //��λ����
        String gxCode = request.getParameter("gxCode");    //�������
        String cpCode = request.getParameter("cpCode");    //��Ʒ����
        String cxCode = request.getParameter("cxCode");    //���߱���
        String scrwId = request.getParameter("scrwId");    //��������ID

        JgmesResult<Integer> ret = new JgmesResult<Integer>();


        ret.Data = 0;
        //У���Ƿ��ȡ��userCode ��mac
        if ((userCode == null || userCode.isEmpty()) && (mac == null || mac.isEmpty())) {
            ret.IsSuccess = false;
            ret.setMessage("�û���½������ǰMAC��ַ�����ܶ�Ϊ�գ�");
        }
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);

        // У��Ϸ��ԣ��Ƿ��ص�½
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                //���û�й�λ���� ,��ȡ��ǰ��λ����
                if (gwCode == null || gwCode.isEmpty()) {
                    gwCode = getCurrentGW(userCode).getStr("GW_GWBH");
                }

                //���û�в�Ʒ���� ,��ȡ��ǰ��Ʒ����
                if (cpCode == null || cpCode.isEmpty()) {
                    cpCode = getCurrentProductByCxCp(userCode).getStr("SCRW_CPBH");
                }

                //���û�й������ ,��ȡ��ǰ�������
                if (gxCode == null || gxCode.isEmpty()) {
                    gxCode = getCurrentGX(cpCode, userCode).getStr("GXGL_GXNUM");
                }

                //���û�в��߱��� ,��ȡ��ǰ���߱���
                if (cxCode == null || cxCode.isEmpty()) {
                    cxCode = getCurrentCX(userCode).getStr("CXSJ_CXBM");
                }

                ////���û����������ID ,��ȡ��ǰ��������ID
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
                ret.setMessage("����" + e.toString());
            }
        }
        toWrite(jsonBuilder.toJson(ret));
    }


    /*
     *
     * ��ȡ��λ�������ݰ��ն����ۼ���ʾ
     */
    public void getGwQty() {
        String userCode = request.getParameter("userCode");//�Ǳ��Ϊ�����ȡ��ǰ��½�û�
        String mac = request.getParameter("mac");          //��ǰMAC��ַ������
        String gwCode = request.getParameter("gwCode");    //��λ����
        String gxCode = request.getParameter("gxCode");    //�������
        String cpCode = request.getParameter("cpCode");    //��Ʒ����
        String cxCode = request.getParameter("cxCode");    //���߱���
        String scrwId = request.getParameter("scrwId");    //��������ID
        String ddhm = "";//��������

        JgmesResult<Integer> ret = new JgmesResult<Integer>();


        ret.Data = 0;
        //У���Ƿ��ȡ��userCode ��mac
        if ((userCode == null || userCode.isEmpty()) && (mac == null || mac.isEmpty())) {
            ret.IsSuccess = false;
            ret.setMessage("�û���½������ǰMAC��ַ�����ܶ�Ϊ�գ�");
        }
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);

        // У��Ϸ��ԣ��Ƿ��ص�½
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                //���û�й�λ���� ,��ȡ��ǰ��λ����
                if (gwCode == null || gwCode.isEmpty()) {
                    gwCode = getCurrentGW(userCode).getStr("GW_GWBH");
                }

                //���û�в�Ʒ���� ,��ȡ��ǰ��Ʒ����
                if (cpCode == null || cpCode.isEmpty()) {
                    cpCode = getCurrentProductByCxCp(userCode).getStr("SCRW_CPBH");
                }

                //���û�й������ ,��ȡ��ǰ�������
                if (gxCode == null || gxCode.isEmpty()) {
                    gxCode = getCurrentGX(cpCode, userCode).getStr("GXGL_GXNUM");
                }

                //���û�в��߱��� ,��ȡ��ǰ���߱���
                if (cxCode == null || cxCode.isEmpty()) {
                    cxCode = getCurrentCX(userCode).getStr("CXSJ_CXBM");
                }

                ////���û����������ID ,��ȡ��ǰ��������ID
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
                ret.setMessage("����" + e.toString());
            }
        }
        toWrite(jsonBuilder.toJson(ret));
    }


    /*
     *
     * ��ȡ�׹�λ��Ͷ������
     */
    public void getFristGwQty() {
        String userCode = request.getParameter("userCode");//�Ǳ��Ϊ�����ȡ��ǰ��½�û�
        String mac = request.getParameter("mac"); //��ǰMAC��ַ������
        String cpCode = request.getParameter("cpCode");    //��Ʒ����
        String scrwId = request.getParameter("scrwId");    //��������ID

        JgmesResult<Integer> ret = new JgmesResult<Integer>();


        ret.Data = 0;
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);

        // У��Ϸ��ԣ��Ƿ��ص�½
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                //���û�в�Ʒ���� ,��ȡ��ǰ��Ʒ����
                if (cpCode == null || cpCode.isEmpty()) {
                    cpCode = getCurrentProductByCxCp(userCode).getStr("SCRW_CPBH");
                }

                ////���û����������ID ,��ȡ��ǰ��������ID
                if (scrwId == null || scrwId.isEmpty()) {
                    scrwId = getCurrentCX(userCode).getStr("JGMES_PLAN_SCRW_ID");
                }

                if ((cpCode != null && !cpCode.isEmpty()) &&
                        (scrwId != null && !scrwId.isEmpty())) {

                    DynaBean cpcxDynaBean = serviceTemplate.selectOne("JGMES_PB_CPCXINFO", " and CPCXINFO_SCRWID = '" + scrwId + "' and CPCXINFO_CPBH = '" + cpCode + "' and str_to_date(CPCXINFO_RQ,'%Y-%c-%d')=str_to_date(now(),'%Y-%c-%d')");
                    if (cpcxDynaBean != null) {
                        ret.Data = cpcxDynaBean.getInt("CPCXINFO_TRSL");
                    }

//					//��ȡ��Ʒ��Ϣ
//					DynaBean cpDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA"," and PRODUCTDATA_BH = '"+cpCode+"'");
//					if(cpDynaBean!=null){
//						//��ȡ��ǰ����CODe
//						String cxCode = getCurrentCX(userCode).getStr("CXSJ_CXBM");
//						//��ȡ�ò�Ʒ���׹�λ
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
                    ret.setMessage("��Ϣ��ȡʧ�ܣ�");
                }

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                // TODO: handle exception
                ret.IsSuccess = false;
                ret.setMessage("����" + e.toString());
            }
        }
        toWrite(jsonBuilder.toJson(ret));
    }

    public void getOperatingRow() {
        String userCode = request.getParameter("userCode");//�Ǳ��Ϊ�����ȡ��ǰ��½�û�
        String mac = request.getParameter("mac"); //��ǰMAC��ַ������

        JgmesResult<Integer> ret = new JgmesResult<Integer>();


        ret.Data = 0;
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);

        // У��Ϸ��ԣ��Ƿ��ص�½
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                //����ֵ�Ǿ��ǲ�����¼����ʾ����
                DynaBean xtcsDynaBean = serviceTemplate.selectOne("JGMES_XTGL_XTCS", " and  XTCS_CXFL1_CODE = 'CXFL06' and XTCS_CXFL2_CODE = 'CZJLXSTS'");
                if (xtcsDynaBean != null) {
                    ret.Data = xtcsDynaBean.getInt("XTCS_CSZ");
                } else {
                    //Ĭ������ʾ1000��
                    ret.Data = 1000;
                }

            } catch (Exception e) {
                logger.error(JgmesCommon.getExceptionDetail2(e));
                // TODO: handle exception
                ret.IsSuccess = false;
                ret.setMessage("����" + e.toString());
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
