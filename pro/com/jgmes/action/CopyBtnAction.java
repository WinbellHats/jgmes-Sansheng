
package com.jgmes.action;

import com.alibaba.druid.util.StringUtils;
import com.je.core.action.DynaAction;
import com.je.core.util.JEUUID;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.BeanUtils;
import com.je.core.util.bean.DynaBean;
import com.jgmes.util.JgmesBarCode;
import com.jgmes.util.JgmesCommon;
import com.jgmes.util.JgmesEnumsDic;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author fm
 * @version 2018-12-15 00:04:12
 * @see /jgmes/copyBtnAction!doCopy.action
 */
@Component("copyBtnAction")
@Scope("prototype")
public class CopyBtnAction extends DynaAction {
    private static final long serialVersionUID = 1L;


    // ����·�߸��ƹ���
    @Transactional
    public void doCopy() {
        String id = request.getParameter("id");
        Session session = pcServiceTemplate.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        DynaBean gylxbean = serviceTemplate.selectOneByPk("JGMES_GYGL_GYLX", id);
        String newGylxId = JEUUID.uuid();
        gylxbean.set("GYLX_ID", newGylxId);
        //gylxbean.set("GYLX_GYLXNUM", serviceTemplate.buildCode("GYLX_GYLXNUM", "JGMES_GYGL_GYLX", gylxbean));

        try {
            tx.begin();//��������
            gylxbean.set("GYLX_GYLXNAME", "����" + gylxbean.getStr("GYLX_GYLXNAME"));
            //�����汾��
            int bbh = gylxbean.getInt("GYLX_BBH");
            System.out.println("�汾�ţ�" + bbh);
            if (bbh < 1) {
                bbh = 1;
            }
            gylxbean.set("GYLX_BBH", bbh + 1);
            //����ʹ��״̬Ϊ����1,������2
            gylxbean.set("GYLX_STATUS", 1);
            //�Ƿ������ó�ʼ�� ��1 ���� 0
            gylxbean.set("GYLX_NO_CODE", 0);
            serviceTemplate.insert(gylxbean);
            List<DynaBean> gxbeans = serviceTemplate.selectList("JGMES_GYGL_GYLXGX", "  and GYLX_ID='" + id + "'");
            if (gxbeans.size() > 0) {
                for (DynaBean gxbean : gxbeans) {
                    String gxid = gxbean.getPkValue();
                    gxbean.set("GYLX_ID", newGylxId);
                    String newGxId = JEUUID.uuid();
                    gxbean.set("GYLXGX_ID", newGxId);
                    serviceTemplate.insert(gxbean);
                    List<DynaBean> gxxmbeans = serviceTemplate.selectList("JGMES_GXGL_GXXM",
                            "  and GYLXGX_ID='" + gxid + "'");
                    if (gxxmbeans.size() > 0) {
                        for (DynaBean gxxmbean : gxxmbeans) {
                            String gxxmid = gxxmbean.getPkValue();
                            gxxmbean.set("GYLXGX_ID", newGxId);
                            String newGxxmId = JEUUID.uuid();
                            gxxmbean.set("GXXM_ID", newGxxmId);
                            gxxmbean.set("GYLX_ID", newGylxId);
                            serviceTemplate.insert(gxxmbean);
                            List<DynaBean> yclzbeans = serviceTemplate.selectList("JGMES_GYGL_EXCEPTIONOPTION",
                                    "  and GXXM_ID='" + gxxmid + "'");
                            if (yclzbeans.size() > 0) {
                                for (DynaBean yclzbean : yclzbeans) {
                                    yclzbean.set("GXXM_ID", newGxxmId);
                                    String newYclzId = JEUUID.uuid();
                                    yclzbean.set("EXCEPTIONOPTION_ID", newYclzId);
                                    yclzbean.set("GYLX_ID", newGylxId);
                                    serviceTemplate.insert(yclzbean);
                                }
                            }

                        }
                    }
                    List<DynaBean> gxwlbeans = serviceTemplate.selectList("JGMES_GXGL_GXWL",
                            "  and GYLXGX_ID='" + gxid + "'");
                    if (gxwlbeans.size() > 0) {
                        for (DynaBean gxwlbean : gxwlbeans) {
                            gxwlbean.set("GYLXGX_ID", newGxId);
                            String newGxwlId = JEUUID.uuid();
                            gxwlbean.set("JGMES_GXGL_GXWL_ID", newGxwlId);
                            gxwlbean.set("GYLX_ID", newGylxId);
                            serviceTemplate.insert(gxwlbean);
                        }
                    }

                    List<DynaBean> gxblxbeans = serviceTemplate.selectList("JGMES_GYGL_GXBLX",
                            "  and GYLXGX_ID='" + gxid + "'");
                    if (gxblxbeans.size() > 0) {
                        for (DynaBean gxblxbean : gxblxbeans) {
                            gxblxbean.set("GYLXGX_ID", newGxId);
                            String newGxblxId = JEUUID.uuid();
                            gxblxbean.set("JGMES_GYGL_GXBLX_ID", newGxblxId);
                            gxblxbean.set("GYLX_ID", newGylxId);
                            serviceTemplate.insert(gxblxbean);
                        }
                    }
                }
            }

            //�޸�ԭ����·��ʹ��״̬Ϊ2������
            String sql = "update JGMES_GYGL_GYLX set GYLX_STATUS='2' where GYLX_ID='" + id + "'";
            pcServiceTemplate.executeSql(sql);
            //System.out.println("�ع����ԡ�");

            tx.commit();//ִ��
        } catch (Exception e) {
            tx.rollback();//�ع�
            logger.error("���ƹ���·��ʧ��" + e);
            toWrite(jsonBuilder.returnFailureJson("\"����ʧ�ܣ�����ϵ�������Ա��\""));
            return;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }


        toWrite(jsonBuilder.returnSuccessJson("\"���Ƴɹ�\""));
    }

    // �Թ���·�������ϸ���޸�ʱ������Ƿ���������
    public void doChack() {
        String gylxId = request.getParameter("id");
        String sql = " and PCLB_CPBH in (select PRODUCTDATA_BH from JGMES_BASE_PRODUCTDATA where PRODUCTDATA_CPGYLXID='"
                + gylxId + "')";
        long i = serviceTemplate.selectCount("JGMES_PLAN_PCLB", sql);
        System.out.println(i);
        toWrite(jsonBuilder.returnSuccessJson("\"" + i + "\""));
    }

    // ��������
    public void doCreateTM() {
        Map<String, Object> map = new HashMap<String, Object>();
        JgmesBarCode jgmesBarCode = new JgmesBarCode(request, serviceTemplate);
        // ��������
        String id = request.getParameter("id");
        String ts = request.getParameter("ts");
        String wlkhcpbh = request.getParameter("PRODUCTDATA_KHCPH");
        if ("".equals(wlkhcpbh) || wlkhcpbh == null) {
            wlkhcpbh = "cpbh0001";
        }
        map.put("id", id);
        map.put("ts", ts);
        map.put("wlkhcpbh", wlkhcpbh);
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        map.put("year", year);
        map.put("month", month);
        // Connection conn = pcServiceTemplate.getConnection();
        int num = Integer.parseInt(ts);
        String sql = "select * from jgmes_barcode_todolist where NEWID='" + id + "'";
        List<DynaBean> zbean = serviceTemplate.selectListBySql(sql);
        String tmlx = zbean.get(0).getStr("CPTMYYGG_TMLX_CODE");// ��������
        map.put("zbean", zbean);
        // ���ݲ�ͬ�������������ɲ�ͬ������
        if (tmlx.equalsIgnoreCase(JgmesEnumsDic.TMCP.getKey())) {
            List<DynaBean> tmlxbean = serviceTemplate.selectList("JGMES_BASE_CPTMYYGG",
                    " and CPTMYYGG_CPBH='" + zbean.get(0).get("PCLB_CPBH") + "' and CPTMYYGG_TMLX_CODE='" + tmlx
                            + "' and CPTMYYGG_YYGZBH='" + zbean.get(0).get("CPTMYYGG_YYGZBH") + "'");
            map.put("tmlxbean", tmlxbean);
            String lsh = "0";
            map.put("lsh", lsh);
            String tmh = "";
            // ����NEWID��ѯ��ǰ����ˮ��
            List<DynaBean> lsbean = serviceTemplate.selectList("JGMES_BARCODE_DQJL",
                    " and DQJL_KHCPBH='" + wlkhcpbh + "'  and DQJL_N=" + year + " and DQJL_Y=" + month);
            map.put("lsbean", lsbean);
            DynaBean cpbean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA",
                    " and PRODUCTDATA_BH='" + zbean.get(0).get("PCLB_CPBH") + "'");
            long selectCount = serviceTemplate.selectCount("JGMES_BASE_PRODUCTDATA",
                    " and PRODUCTDATA_KHCPH='" + cpbean.get("PRODUCTDATA_KHCPH") + "' and PRODUCTDATA_WLTYPE_CODE='CP' ");
//			if (cpbean.get("PRODUCTDATA_KHCPH").equals("") || selectCount > 1
//					|| cpbean.get("PRODUCTDATA_KHCPH") == null) {
//				toWrite(jsonBuilder.returnSuccessJson("������Ϣ�ͻ���Ʒ����ظ���"));
//				return;
//			}
            map.put("cpbean", cpbean);
            String gys = "VG";
            String fyear = getYear(year);
            String fmonth = getMonth(month);

            // 1��ȡ��ˮ��
            map = jgmesBarCode.getWaterNumber(map);
            // 2��ȡ��Ʒ���
            map = jgmesBarCode.getProductNumber(map);
            // 3����or��ѯ�����¼
            map = jgmesBarCode.addOrFindBarCode(map);
            // ���������
            String tmscjlId = (String) map.get("tmscjlId");
            // �ͻ���Ʒ���
            String cpbh = (String) map.get("khcpbm");
            int newLsh = 0;
            double pcsl = zbean.get(0).getInt("PCSL");
            double yscsl = zbean.get(0).getInt("TMSCJL_YSCSL");
            double mtmsl = zbean.get(0).getInt("CPTMYYGG_MTMSL");
            lsh = (String) map.get("lsh");
            // Object[] rule= {num,gys,cpbh};
            // ������������
            for (int i = 0; i < num; i++) {
                DynaBean bean = new DynaBean("JGMES_BASE_GDCPTM", true);
                newLsh = Integer.parseInt(lsh) + i + 1;
                String Lsh = "0000000" + newLsh;
                String tmLsh = Lsh.substring(Lsh.length() - 7, Lsh.length());
                tmh = gys + cpbh + "-" + fmonth + fyear + tmLsh;
                bean.set("GDCPTM_TMH", tmh);
                bean.set("NEWID", id);
                bean.set("GDCPTM_PCDBH", zbean.get(0).get("PCLB_PCDBH"));
                bean.set("GDCPTM_PCDMC", zbean.get(0).get("PCLB_PCDMC"));
                bean.set("GDCPTM_CPBH", zbean.get(0).get("PCLB_CPBH"));
                bean.set("GDCPTM_NAME", cpbean.get("PRODUCTDATA_NAME"));
                bean.set("GDCPTM_GDHM", zbean.get(0).get("PCLB_GDHM"));
                bean.set("GDCPTM_DDHM", zbean.get(0).get("PCLB_DDHM"));
                bean.set("JGMES_PLAN_SCRW_ID", zbean.get(0).get("JGMES_PLAN_SCRW_ID"));
                bean.set("GDCPTM_PCRQ", zbean.get(0).get("PCLB_PCRQ"));
                bean.set("GDCPTM_SL", (int) (((pcsl - yscsl) / mtmsl) > (i + 1) ? mtmsl : ((pcsl - yscsl) % mtmsl)));
                bean.set("GDCPTM_TMLX_CODE", zbean.get(0).get("CPTMYYGG_TMLX_CODE"));
                bean.set("GDCPTM_TMLX_NAME", tmlxbean.get(0).get("CPTMYYGG_TMLX_NAME"));
                bean.set("GDCPTM_TMLX_ID", tmlxbean.get(0).get("CPTMYYGG_TMLX_ID"));
                bean.set("JGMES_BARCODE_TMSCJL_ID", tmscjlId);
                bean.set("GDCPTM_SFYDY", 0);
                serviceTemplate.insert(bean);
            }
            long yscts = serviceTemplate.selectCount("JGMES_BASE_GDCPTM", " and NEWID='" + id + "'");
            pcServiceTemplate.executeSql(
                    "update JGMES_BARCODE_TMSCJL set TMSCJL_YSCSL=" + yscts + " where " + " JGMES_BARCODE_TMSCJL_ID='"
                            + tmscjlId + "' and TMSCJL_TMLX_CODE='" + zbean.get(0).getStr("CPTMYYGG_TMLX_CODE") + "'");
            pcServiceTemplate.executeSql(
                    "update JGMES_BARCODE_DQJL set DQJL_DQLSH=" + newLsh + " where DQJL_KHCPBH='" + wlkhcpbh + "'" +
                            " and DQJL_N=" + year + " and DQJL_Y=" + month + " ");
            toWrite(jsonBuilder.returnSuccessJson(strData));
        } else if (tmlx.equalsIgnoreCase(JgmesEnumsDic.TMZB.getKey())) {
            List<DynaBean> tmlxbean = serviceTemplate.selectList("JGMES_BASE_CPTMYYGG",
                    " and CPTMYYGG_CPBH='" + zbean.get(0).get("PCLB_CPBH") + "' and CPTMYYGG_TMLX_CODE='" + tmlx
                            + "' and CPTMYYGG_YYGZBH='" + zbean.get(0).get("CPTMYYGG_YYGZBH") + "'");
            String lsh = "0";
            map.put("lsh", lsh);
            String tmh = "";
//			Calendar cal = Calendar.getInstance();
//			int year = cal.get(Calendar.YEAR);
//			int month = cal.get(Calendar.MONTH) + 1;
            // ����NEWID��ѯ��ǰ����ˮ��
            List<DynaBean> lsbean = serviceTemplate.selectList("JGMES_BARCODE_DQJL",
                    " and DQJL_GDTMDSCID='" + id + "' and DQJL_N=" + year + " and DQJL_Y=" + month);
            map.put("lsbean", lsbean);
            DynaBean cpbean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA",
                    " and PRODUCTDATA_BH='" + zbean.get(0).get("PCLB_CPBH") + "'");
            map.put("cpbean", cpbean);

            String gys = "ZB";
            String khcph = cpbean.getStr("PRODUCTDATA_KHCPH");

            // 1
            map = jgmesBarCode.getWaterNumber(map);
            // 2
            map = jgmesBarCode.getProductNumber(map);
            // 3
            map = jgmesBarCode.addOrFindBarCode(map);
            // ���������
            String tmscjlId = (String) map.get("tmscjlId");
            int newLsh = 0;
            double pcsl = zbean.get(0).getInt("PCSL");
            double yscsl = zbean.get(0).getInt("TMSCJL_YSCSL");
            double mtmsl = zbean.get(0).getInt("CPTMYYGG_MTMSL");
            lsh = (String) map.get("lsh");
            // ������������
            for (int i = 0; i < num; i++) {
                DynaBean bean = new DynaBean("JGMES_BASE_GDCPTM", true);
                newLsh = Integer.parseInt(lsh) + i + 1;
                String Lsh = "000000" + newLsh;
                String tmLsh = Lsh.substring(Lsh.length() - 6, Lsh.length());
                // cpbh + fyear + fmonth
                tmh = gys + khcph + tmLsh;
                bean.set("GDCPTM_TMH", tmh);
                bean.set("NEWID", id);
                bean.set("GDCPTM_PCDBH", zbean.get(0).get("PCLB_PCDBH"));
                bean.set("GDCPTM_PCDMC", zbean.get(0).get("PCLB_PCDMC"));
                bean.set("GDCPTM_CPBH", zbean.get(0).get("PCLB_CPBH"));
                bean.set("GDCPTM_NAME", cpbean.get("PRODUCTDATA_NAME"));
                bean.set("GDCPTM_GDHM", zbean.get(0).get("PCLB_GDHM"));
                bean.set("GDCPTM_DDHM", zbean.get(0).get("PCLB_DDHM"));
                bean.set("JGMES_PLAN_SCRW_ID", zbean.get(0).get("JGMES_PLAN_SCRW_ID"));
                bean.set("GDCPTM_PCRQ", zbean.get(0).get("PCLB_PCRQ"));
                bean.set("GDCPTM_SL", (int) (((pcsl - yscsl) / mtmsl) > (i + 1) ? mtmsl : ((pcsl - yscsl) % mtmsl)));
                bean.set("GDCPTM_TMLX_CODE", zbean.get(0).get("CPTMYYGG_TMLX_CODE"));
                bean.set("GDCPTM_TMLX_NAME", tmlxbean.get(0).get("CPTMYYGG_TMLX_NAME"));
                bean.set("GDCPTM_TMLX_ID", tmlxbean.get(0).get("CPTMYYGG_TMLX_ID"));
                bean.set("JGMES_BARCODE_TMSCJL_ID", tmscjlId);
                bean.set("GDCPTM_SFYDY", 0);
                serviceTemplate.insert(bean);
            }
            long yscts = serviceTemplate.selectCount("JGMES_BASE_GDCPTM", " and NEWID='" + id + "'");
            pcServiceTemplate.executeSql(
                    "update JGMES_BARCODE_TMSCJL set TMSCJL_YSCSL=" + yscts + " where " + " JGMES_BARCODE_TMSCJL_ID='"
                            + tmscjlId + "' and TMSCJL_TMLX_CODE='" + zbean.get(0).getStr("CPTMYYGG_TMLX_CODE") + "'");

            pcServiceTemplate.executeSql(
                    "update JGMES_BARCODE_DQJL set DQJL_DQLSH=" + newLsh + " where DQJL_GDTMDSCID='" + id + "'");
            toWrite(jsonBuilder.returnSuccessJson(strData));

        }
    }


    //ͨ�������ȡģ��ʹ�ӡ����ַ��ģ��һ����ͨ��
    public void findUniversallyBarcodeBQCS() {
        String JGMES_SYS_CLTMB_ID = request.getParameter("JGMES_SYS_CLTMB_ID");
        System.out.println(JGMES_SYS_CLTMB_ID);
        DynaBean selectOne = serviceTemplate.selectOne("JGMES_SYS_CLTMB",
                " and JGMES_SYS_CLTMB_ID = '" + JGMES_SYS_CLTMB_ID + "' ");
        String CLTMB_DYJDZ = selectOne.getStr("CLTMB_DYJDZ");//��ӡ����ַ
        String ip = request.getServerName();
        //�˿ں�
        int port = request.getServerPort();
        String jsonStr = "";
        if (selectOne != null) {
            String TMBZ_BQMBWJ = selectOne.getStr("TMBZ_BQMBWJ");
            int index = TMBZ_BQMBWJ.indexOf("*");
            String CLTMB_BQCSMB = selectOne.getStr("CLTMB_BQCSMB");
            String substring = TMBZ_BQMBWJ.substring(index + 1, TMBZ_BQMBWJ.length());
            jsonStr = CLTMB_BQCSMB.replace("@filePath@", "http://" + ip + ":" + port + substring + "");
        }
        jsonStr += "%,%" + CLTMB_DYJDZ;
        System.out.println(jsonStr);
        toWrite(jsonStr);
    }

    //��������,��ӡ����ַ
    public void findGXBarcodeBQCS() {
        DynaBean selectOne = serviceTemplate.selectOne("JGMES_SYS_TMBZ",
                " and TMBZ_TMFL_CODE = 'GXM'");
        String ip = request.getServerName();
        //�˿ں�
        int port = request.getServerPort();
        String jsonStr = "";
        String printerUrl = selectOne.getStr("TMBZ_DYJDZ");//��ӡ����ַ
        if (selectOne != null) {
            String TMBZ_BQMBWJ = selectOne.getStr("TMBZ_BQMBWJ");
            int index = TMBZ_BQMBWJ.indexOf("*");
            String TMBZ_BQCSMB = selectOne.getStr("TMBZ_BQCSMB");
            String substring = TMBZ_BQMBWJ.substring(index + 1, TMBZ_BQMBWJ.length());
            jsonStr = TMBZ_BQCSMB.replace("@filePath@", "http://" + ip + ":" + port + substring + "");
        }
        jsonStr += "%,%" + printerUrl;

        toWrite(jsonStr);
    }

    //��ȡ����������
    public void getGxCode() {
        String GXGL_ID = request.getParameter("GXGL_ID");
        String[] split = GXGL_ID.split(",");
        List<String> d = new ArrayList<String>();
        for (int i = 0; i < split.length; i++) {
            d.add(split[i]);
        }
        String bs = "";
        String js = "";
        DynaBean tmbs = serviceTemplate.selectOne("JGMES_SYS_TMBZ",
                " and TMBZ_TMFL_NAME = '������' ");
        if (tmbs != null) {
            bs = tmbs.getStr("TMBZ_BSNR");
        }

        for (int i = 0; i < d.size(); i++) {
            String jsonStr = "";
            String GXGL_GXTMH = "";
            String GXGL_GXNUM = "";
            DynaBean selectOne = serviceTemplate.selectOne("JGMES_GYGL_GXGL",
                    " and GXGL_ID = '" + d.get(i) + "'");
            if (selectOne != null) {
                GXGL_GXTMH = selectOne.getStr("GXGL_GXTMH");//��������
                GXGL_GXNUM = selectOne.getStr("GXGL_GXNUM");//������
            }
            jsonStr += bs + GXGL_GXNUM + GXGL_GXTMH;
            js += jsonStr;
            if (i + 1 < d.size()) {
                js += ",";
            }
        }
        System.out.println(js);
        toWrite(js);

    }

    public void findBQCS() {
        String CPTMYYGG_YYGZBH = request.getParameter("CPTMYYGG_YYGZBH");
        String GDCPTM_CPBH = request.getParameter("GDCPTM_CPBH");
        DynaBean selectOne = serviceTemplate.selectOne("JGMES_BASE_CPTMYYGG",
                " and CPTMYYGG_YYGZBH = '" + CPTMYYGG_YYGZBH + "' and  CPTMYYGG_CPBH='" + GDCPTM_CPBH + "' ");
        String ip = request.getServerName();
        //�˿ں�
        int port = request.getServerPort();
        String jsonStr = "";
        if (selectOne != null) {
            String CPTMYYGG_BQMB = selectOne.getStr("CPTMYYGG_BQMB");
            int index = CPTMYYGG_BQMB.indexOf("*");
            String CPTMYYGG_BQCS = selectOne.getStr("CPTMYYGG_BQCS");
            String substring = CPTMYYGG_BQMB.substring(index + 1, CPTMYYGG_BQMB.length());
            jsonStr = CPTMYYGG_BQCS.replace("@filePath@", "http://" + ip + ":" + port + substring + "");
        }
        toWrite(jsonStr);
    }

    public void tmBatchList() {
        String BatchList = request.getParameter("tmBatchList");
        String[] Tmlist = BatchList.split(",");
        String[] sqls = new String[Tmlist.length];
        for (int i = 0; i < Tmlist.length; i++) {
            sqls[i] = "update JGMES_BASE_GDCPTM set GDCPTM_SFYDY=1 where GDCPTM_TMH='" + Tmlist[i] + "';";
        }
        Integer listUpdate = serviceTemplate.listUpdate(sqls);

        System.out.println(listUpdate);
        toWrite("�ɹ�");
    }

    /*
     * ͨ������
     * ����1������ ����2�������ֶ�   ����3����������
     */
    public void todoUpdateList() {
        String tableName = request.getParameter("tableName");//����
        String id = request.getParameter("pk");//���
        String idColumn = request.getParameter("pkColumn");//�������
        List<String[]> list = new ArrayList<String[]>();
        List<DynaBean> tmlxbean = serviceTemplate.selectList(tableName,
                " and " + idColumn + " ='" + id + "'  order by SY_ORDERINDEX asc");
        if (StringUtil.isNotEmpty(tableName) && StringUtil.isNotEmpty(id) && StringUtil.isNotEmpty(idColumn) && tmlxbean.size() > 0) {
            //����ִ��
            Session ss = serviceTemplate.getSessionFactory().openSession();
            Transaction tr = ss.beginTransaction();
            tr.begin();
            for (int i = 0; i < tmlxbean.size(); i++) {
                String idbean = tmlxbean.get(i).getPkValue();
                String pkName = (String) tmlxbean.get(i).get(BeanUtils.KEY_PK_CODE);
                ss.createSQLQuery("update " + tableName + " set SY_ORDERINDEX=? where " + pkName + "=?").setParameter(0, (i + 1)).setParameter(1, idbean).executeUpdate();
            }
            //����ع�������
            try {
                tr.commit();
            } catch (Exception e) {
                tr.rollback();
                e.printStackTrace();
                toWrite(jsonBuilder.returnFailureJson("\"�������ʧ��\""));
            } finally {
                ss.close();
            }
        }

    }

    private String getYear(int year) {
        String Fyear = null;
        String[] yearArr = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
                "S", "T", "U", "V", "W", "X", "Y", "Z"};
        int i = 2010;
        Fyear = yearArr[year - i];
        return Fyear;
    }

    private String getMonth(int month) {
        String Fyear = null;
        String[] yearArr = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
                "S", "T", "U", "V", "W", "X", "Y", "Z"};
        Fyear = yearArr[month - 1];
        return Fyear;
    }

    //���ص�ǰ��ֵ
/*	public void BarcodeKey() {
		String item=request.getParameter("item");//��Ʒ���
		String cust=request.getParameter("cust");//�ͻ�����
		String vend=request.getParameter("vend");//�ͻ�����
		String bcrule = request.getParameter("bcrule");//�������ɹ�����

		//���������ֵ
		String barcodekey=getBarcodeKey(cust,vend,item,bcrule);
		if (barcodekey.indexOf("err")==0) {
			toWrite(jsonBuilder.returnFailureJson("����" + barcodekey.substring(4,barcodekey.length()-4) + ""));
		} else {
			toWrite(jsonBuilder.returnSuccessJson(barcodekey));
		}
	}*/

    //���ɲ�Ʒ����
    public void doCreatePGTM() {
        Map<String, Object> map = new HashMap<String, Object>();
        //�Ƿ�ǿ�����ɣ��ǡ�1.���0
        String qzsc = request.getParameter("QZSC");
        if (StringUtil.isEmpty(qzsc)) {
            qzsc = "0";
        }
        // ǰ̨�������
        int startnum = Integer.parseInt(request.getParameter("startnum"));//��ʼ��ˮ��
        int endnum = Integer.parseInt(request.getParameter("endnum"));//������ˮ��
        String LOT = request.getParameter("LOT");//����
        //��ȡ�����������ͼ��Ϣ
        String sql = "select * from jgmes_barcode_todolist where NEWID='" + pkValue + "'";
        List<DynaBean> zbean = serviceTemplate.selectListBySql(sql);
        String tmlx = zbean.get(0).getStr("CPTMYYGG_TMLX_CODE");// ��������
        String PCLB_CPBH = zbean.get(0).getStr("PCLB_CPBH");//��Ʒ���
        String PCLB_KHBM = zbean.get(0).getStr("PCLB_KHBM");//�ͻ�����
        String CPTMYYGG_TMGZBH = zbean.get(0).getStr("CPTMYYGG_TMGZBH");//���������
        String CPTMYYGG_YYGZBH = zbean.get(0).getStr("CPTMYYGG_YYGZBH");//Ӧ�ù�����
        int PCLB_PCSL = zbean.get(0).getInt("PCLB_PCSL");//�Ų�����TMSCJL_YSCSL
        int TMSCJL_YSCSL = zbean.get(0).getInt("TMSCJL_YSCSL");//����������
        String lckh = zbean.get(0).getStr("PCLB_LCKH");//���̿��ţ��ڲ�������
        //��ȡ��������
        DynaBean cxbean = serviceTemplate.selectOne("JGMES_BASE_CXSJ", " and  CXSJ_CXBM='" + zbean.get(0).getStr("PCLB_CXBM") + "'");
        //��ȡ��Ʒ����
        DynaBean cpbean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " and PRODUCTDATA_BH='" + PCLB_CPBH + "'");
        if (cpbean == null) {
            toWrite(jsonBuilder.returnFailureJson("\"��Ʒ��ȡʧ�ܣ�\""));
            return;
        }
        //��ȡ��������
        List<DynaBean> tmlxbean = serviceTemplate.selectList("JGMES_BASE_CPTMYYGG", " and CPTMYYGG_CPBH='" + PCLB_CPBH + "' and CPTMYYGG_TMLX_CODE='" + tmlx + "' and CPTMYYGG_YYGZBH='" + CPTMYYGG_YYGZBH + "'");
        if (tmlxbean == null) {
            toWrite(jsonBuilder.returnFailureJson("\"�������ͻ�ȡʧ�ܣ�\""));
            return;
        }
        //����or��ѯ�������ɼ�¼
        String tmscjlId = addOrFindBarCode(zbean.get(0), tmlxbean.get(0), cpbean, cxbean);

        //������ˮ�Ź���
        DynaBean tmgz = serviceTemplate.selectOne("JGMES_BASE_TTGGZB", "and JGMES_BASE_TMGG_ID=(select JGMES_BASE_TMGG_ID from JGMES_BASE_TMGG where TMGG_BH='" + CPTMYYGG_TMGZBH + "') and SVTYPE_CODE='4'", "TTGG_STR_LENGTH,TTGG_NUM_START,TTGG_STEP_SIZE");
        if (tmgz == null) {
            toWrite(jsonBuilder.returnFailureJson("\"�������������û�ж�����ˮ�Ż����˶����ˮ��\""));
            return;
        }
        int TTGG_STR_LENGTH = tmgz.getInt("TTGG_STR_LENGTH");//����
        int TTGG_NUM_START = tmgz.getInt("TTGG_NUM_START");//��ʼ��ˮ��
        int TTGG_STEP_SIZE = tmgz.getInt("TTGG_STEP_SIZE");//��ˮ�Ų���
        if (TTGG_STR_LENGTH == 0 || TTGG_STEP_SIZE == 0) {
            toWrite(jsonBuilder.returnFailureJson("\"�������������û�ж�����ˮ�ų��Ȼ���ˮ�Ų���\" "));
            return;
        }
        if (startnum < TTGG_NUM_START) {
            startnum = TTGG_NUM_START;
        }
        double ts = (double) (endnum - startnum + 1) / TTGG_STEP_SIZE;
        int nums = (int) Math.ceil(ts);//����������ȡ��
        //System.out.println(ts+"����:"+nums);
        if (nums == 0) {
            toWrite(jsonBuilder.returnFailureJson("\"�������ɵ���ˮ���Ѵ����ޣ����ܼ������ɣ�\""));
            return;
        }
        //������������
        int num1 = TMSCJL_YSCSL + nums;//������������+��������������
        String barcodekey = "";//��ֵ������ΪQZSC��ǿ�����ɣ���Ϊǿ�����ɡ�
        if (qzsc.equals("1")) {
            //ǿ�����ɲ�У������������
            barcodekey = "QZSC";
        } else {
            //���ն�������Ӧ����������
            int num2 = calBarcodeNum(PCLB_CPBH, PCLB_PCSL, CPTMYYGG_YYGZBH);
            //System.out.println("������������:"+num1+"  Ӧ����������:"+num2);
            if (num1 > num2) {
                toWrite(jsonBuilder.returnFailureJson("\"���󣺳����涨������" + num2 + "����\""));
                return;
            }
        }
//        //���������ֵ****��ֵ��������
//        if (StringUtil.isEmpty(barcodekey)) {
//            barcodekey = getBarcodeKey(PCLB_KHBM, "", PCLB_CPBH, CPTMYYGG_TMGZBH, LOT);
//            if (barcodekey.indexOf("err") == 0) {
//                toWrite(jsonBuilder.returnFailureJson("\"����" + barcodekey.substring(4, barcodekey.length() - 4) + "\""));
//                return;
//            }
//        }

        HashMap<String, Object> first = getBarcodeMain(PCLB_KHBM, "", PCLB_CPBH, CPTMYYGG_TMGZBH, startnum, LOT, 1);//��һ������
//		HashMap<String,Object> end=getBarcodeMain(PCLB_KHBM,"",PCLB_CPBH,CPTMYYGG_TMGZBH,endnum,LOT,1);//���һ������
//        DynaBean f = (DynaBean) first.get("wlmaxKey");
        //�ж��Ƿ��д���
        String firstBarcode = first.get("result").toString();
        if (firstBarcode.indexOf("����") == 0) {
            toWrite(jsonBuilder.returnFailureJson("\"" + firstBarcode + "\""));
            return;
        }
        //��ȡǰ��׺
        String qz = (String) first.get("QZ");
        String hz = (String) first.get("HZ");
        boolean creat = true;
        try {
            String l = (String) first.get("lshgs");
            Integer lshgs = Integer.parseInt(l);
            if (isSussessCreateTM(qz, hz, startnum, endnum, 1, lshgs)) {
                String barcode = "";//�����
                String strnum = "";
                int inum = 0;
                String createBarcodeQZ = "";//Ҫ���ɵ������ǰ׺
                String createBarcodeHZ = "";//Ҫ���ɵ�����ĺ�׺
                HashMap<String, Object> bc = null;//��ʼ��
                String lshgs_frist = "";//��ˮ�Ÿ�ʽ��ʼ��
                DynaBean wlmaxKey = null;//���������ˮ�ű��ʼ��
                List<DynaBean> pgtmList = new ArrayList<DynaBean>();
                //��ȡ�����ˮ�ű��Ӧ����
                Integer maxLsh = 0;
                //���빤����Ʒ�����Ӧ��JGMES_BASE_GDCPTM������Ϊͨ������
                DynaBean beanOne = new DynaBean("JGMES_BASE_GDCPTM", true);
                serviceTemplate.buildModelCreateInfo(beanOne);
                beanOne.set("NEWID", pkValue);//�����������ɼ�¼_���ID
                beanOne.set("GDCPTM_PCDBH", zbean.get(0).get("PCLB_PCDBH"));//�Ų������
                beanOne.set("GDCPTM_PCDMC", zbean.get(0).get("PCLB_PCDMC"));//�Ų�������
                beanOne.set("GDCPTM_CPBH", zbean.get(0).get("PCLB_CPBH"));//��Ʒ���
                beanOne.set("GDCPTM_NAME", zbean.get(0).get("PCLB_CPNAME"));//��Ʒ����
                beanOne.set("GDCPTM_GDHM", zbean.get(0).get("PCLB_GDHM"));//��������
                beanOne.set("GDCPTM_DDHM", zbean.get(0).get("PCLB_DDHM"));//��������
                beanOne.set("JGMES_PLAN_SCRW_ID", zbean.get(0).get("JGMES_PLAN_SCRW_ID"));//��������_���ID
                beanOne.set("GDCPTM_SCRWDH", zbean.get(0).get("PCLB_RWDH"));//�������񵥺�
                beanOne.set("GDCPTM_PCRQ", zbean.get(0).get("PCLB_PCRQ"));//�Ų�����
                beanOne.set("GDCPTM_SL", 1);//����
                beanOne.set("GDCPTM_TMLX_CODE", zbean.get(0).get("CPTMYYGG_TMLX_CODE"));//��������
                beanOne.set("GDCPTM_TMLX_NAME", zbean.get(0).get("CPTMYYGG_TMLX_NAME"));//��������_NAME
                beanOne.set("GDCPTM_TMLX_ID", zbean.get(0).get("CPTMYYGG_TMLX_ID"));//��������_ID
                beanOne.set("JGMES_BARCODE_TMSCJL_ID", tmscjlId);//�������ɼ�¼_���ID
                beanOne.set("GDCPTM_TMXZ_NAME", zbean.get(0).get("CPTMYYGG_TMXZ_NAME"));//��������_NAME
                beanOne.set("GDCPTM_TMXZ_CODE", zbean.get(0).get("CPTMYYGG_TMXZ_CODE"));//��������
                beanOne.set("GDCPTM_TMXZ_ID", zbean.get(0).get("CPTMYYGG_TMXZ_ID"));//��������_ID
                beanOne.set("GDCPTM_TMLY_CODE", "XTM");//������Դ_CODE
                beanOne.set("GDCPTM_TMLY_NAME", "ϵͳ������");//������Դ_NAME
                beanOne.set("GDCPTM_SFYDY", 0);//�Ƿ��Ѵ�ӡ
                beanOne.set("GDCPTM_LCKH", lckh);//���̿��ţ��ڲ�������
                for (int i = 0; i < nums; i++) {
                    DynaBean bean = beanOne.clone();
                    //����Ϊ�������ɴ���
                    inum = startnum + i * TTGG_STEP_SIZE;
                    //��������
                    if (StringUtil.isEmpty(createBarcodeQZ) && StringUtil.isEmpty(createBarcodeHZ)) {//��Ϊ�գ�����һ������
                        bc = getBarcodeMain(PCLB_KHBM, "", PCLB_CPBH, CPTMYYGG_TMGZBH, inum, LOT, 1);
                        wlmaxKey = (DynaBean) bc.get("wlmaxKey");
                        if (wlmaxKey == null) {
                            toWrite(jsonBuilder.returnFailureJson("\"���󣺻�ȡ�����ˮ��ʧ��\""));
                            return;
                        }
                        barcode = bc.get("result").toString();
                        if (barcode.indexOf("����") == 0) {
                            toWrite(jsonBuilder.returnFailureJson("\"" + barcode + "\""));
                            return;
                        }
                        createBarcodeQZ = (String) bc.get("QZ");//ǰ׺
                        createBarcodeHZ = (String) bc.get("HZ");//��׺
                        lshgs_frist = (String) bc.get("lshgs");//��ˮ�Ÿ�ʽ
                        wlmaxKey.setInt("WLMAX_LSHCD", lshgs_frist.length());//��ˮ�ų���
//                        //��ѯҪ���ɵ������Ƿ��ڳ�Ʒ�����д���
//                        List<DynaBean> jgmes_base_gdcptm = serviceTemplate.selectList("JGMES_BASE_GDCPTM", "and GDCPTM_TMQZ='" + qz + "' and GDCPTM_TMHZ='" + hz + "' and GDCPTM_TMLSHCD='" + lshgs_frist.length() + "' and GDCPTM_LSH between " + startnum + " and " + endnum);
//                        if (jgmes_base_gdcptm.size() > 0) {
//
//                            return;
//                        }
                        DynaBean jgmes_barcode_wlmax = serviceTemplate.selectOne("JGMES_BARCODE_WLMAX", "and WLMAX_QZ='" + createBarcodeQZ + "'and WLMAX_HZ='" + createBarcodeHZ + "' and WLMAX_LSHCD=" + lshgs_frist.length() + " and WLMAX_FLBH=1");
                        //���������Ѵ��ڣ����д���������ˮ�ű�
                        List<DynaBean> cptm = serviceTemplate.selectList("JGMES_BASE_GDCPTM", "and GDCPTM_TMH = '" + barcode + "'");
                        //��ȡ��ǰ�����������ɼ�¼����ǰ׺����׺����ˮ�ų���һ�µ�����������ˮ��
                        String numStr = "";
                        for (int j = 0; j < lshgs_frist.length(); j++) {
                            numStr += "_";
                        }
                        String thisMaxLsh = "";
                        String seachLikeStr = createBarcodeQZ + numStr + createBarcodeHZ;
                        String checkOldBarcodesql = "select max(GDCPTM_LSH) GDCPTM_LSH from JGMES_BASE_GDCPTM where GDCPTM_TMH like '" + seachLikeStr + "'";
                        List<DynaBean> dynaBeans = serviceTemplate.selectListBySql(checkOldBarcodesql);
                        if (dynaBeans.size() > 0) {
                            thisMaxLsh = dynaBeans.get(0).getStr("GDCPTM_LSH");
                        } else {
                            thisMaxLsh = "0";
                        }
                        //��д
                        if (cptm.size() > 0) {
                            if (jgmes_barcode_wlmax == null) {
                                wlmaxKey.setStr("WLMAX_ZDLSH", thisMaxLsh + "");//�����ˮ��;
                                serviceTemplate.insert(wlmaxKey);
                            } else {
                                jgmes_barcode_wlmax.setStr("WLMAX_ZDLSH", thisMaxLsh + "");//�����ˮ��
                                jgmes_barcode_wlmax.setStr("WLMAX_LSHCD", wlmaxKey.getStr("WLMAX_LSHCD"));//�����ˮ��
                                jgmes_barcode_wlmax.setStr("WLMAX_QZ", createBarcodeQZ);
                                jgmes_barcode_wlmax.setStr("WLMAX_HZ", createBarcodeHZ);
                                jgmes_barcode_wlmax.setInt("WLMAX_LSHCD", lshgs_frist.length());
                                serviceTemplate.update(jgmes_barcode_wlmax);
                            }
                            toWrite(jsonBuilder.returnFailureJson("\"���룺" + barcode + "�Ѵ��ڣ������������ˮ�ţ������ԣ�\""));
                            return;
                        }
                    }
                    //������ˮ��
                    String codeFormat = "%0" + String.valueOf(lshgs_frist.length() + "d");//��ʽ
                    String lshAll = String.format(codeFormat, inum);//��ˮ�Ÿ�ʽ
                    bean.set("GDCPTM_LSH", inum);//��ˮ��
                    barcode = createBarcodeQZ + lshAll + createBarcodeHZ;
                    bean.set("GDCPTM_TMH", barcode);//�����
                    String pk = JEUUID.uuid();
                    bean.set("JGMES_BASE_GDCPTM_ID", pk);//����id
                    bean.set(BeanUtils.KEY_PK_CODE, pk);
                    wlmaxKey.setStr("WLMAX_ZDLSH", inum + "");//�����ˮ��
                    bean.setStr("GDCPTM_TMQZ", createBarcodeQZ);//����ǰ׺
                    bean.setStr("GDCPTM_TMHZ", createBarcodeHZ);//�����׺
                    bean.setInt("GDCPTM_TMLSHCD", lshgs_frist.length());//������ˮ�ų���
                    if (StringUtil.isEmpty(barcodekey)){
                        barcodekey=createBarcodeQZ+createBarcodeHZ;
                    }
                    bean.setStr("GDCPTM_JZ",barcodekey);//��ֵ
                    pgtmList.add(bean);
                }

                serviceTemplate.insert(pgtmList);
                DynaBean jgmes_barcode_wlmax = serviceTemplate.selectOne("JGMES_BARCODE_WLMAX", "and WLMAX_QZ='" + createBarcodeQZ + "'and WLMAX_HZ='" + createBarcodeHZ + "' and WLMAX_LSHCD=" + lshgs_frist.length() + " and WLMAX_FLBH = 1");
                if (jgmes_barcode_wlmax == null) {//���»�������������ˮ�ż�¼��
                    wlmaxKey.setStr("JGMES_BARCODE_WLMAX_ID", JEUUID.uuid());//�������������ˮ�ű�����
                    wlmaxKey.setInt("WLMAX_FLBH", 1);
                    serviceTemplate.insert(wlmaxKey);
                } else {
                    jgmes_barcode_wlmax.setStr("WLMAX_ZDLSH", wlmaxKey.getStr("WLMAX_ZDLSH"));
                    serviceTemplate.update(jgmes_barcode_wlmax);
                }
                //������������������
                pcServiceTemplate.executeSql(
                        "update JGMES_BARCODE_TMSCJL set TMSCJL_YSCSL=(select count(*) from JGMES_BASE_GDCPTM where NEWID='" + pkValue + "') "
                                + "where JGMES_BARCODE_TMSCJL_ID='" + tmscjlId + "' "
                                + "and TMSCJL_TMLX_CODE='" + tmlx + "'");

                toWrite(jsonBuilder.returnSuccessJson("\"�������ɳɹ���\""));
            } else {
                toWrite(jsonBuilder.returnFailureJson("\"���󣺿�ʼ��ˮ��" + startnum + "�ͽ�����ˮ��֮��" + endnum + "֮���Ѿ����������룬�������ظ����ɣ�\""));
            }
            //���سɹ���ʶ
            toWrite(jsonBuilder.returnSuccessJson("\"" + strData + "\""));
        } catch (Exception e) {
            e.printStackTrace();
            toWrite(jsonBuilder.returnSuccessJson("\"�쳣δ֪����\""));
        }
    }

    // ������������
    public void doCreateWLTM() {
        //Map<String, Object> map = new HashMap<String, Object>();

        // ǰ̨�������
        int startnum = Integer.parseInt(request.getParameter("startnum"));//��ʼ��ˮ��
        int endnum = Integer.parseInt(request.getParameter("endnum"));//������ˮ��
        String LOT = request.getParameter("LOT");//����
        int sl = Integer.parseInt(request.getParameter("sl"));//��������
        String tmxz = request.getParameter("tmxz");
        //String bcrule = request.getParameter("bcrule");//�������ɹ�����
        //��ѯ����������������
        String strSql = "select * from V_JGMES_BASE_VENDORITEM where NEWID='" + pkValue + "'";
        List<DynaBean> items = serviceTemplate.selectListBySql(strSql);
        if (items == null || items.size() == 0) {
            toWrite(jsonBuilder.returnFailureJson("\"����δ�ܲ��ҵ�����������������\""));
            return;
        }
        DynaBean venditem = items.get(0);
        String VENDOR_CODE = venditem.getStr("VENDOR_CODE");//��Ӧ�̱���
        String VENDOR_NAME = venditem.getStr("VENDOR_NAME");//��Ӧ������
        String VENDOR_SNAME = venditem.getStr("VENDOR_SNAME");//��Ӧ�̼��
        String VENDOR_SCODE = venditem.getStr("VENDOR_SCODE");//��Ӧ�̼���
        String VENDORITEM_WLBM = venditem.getStr("VENDORITEM_WLBM");//���ϱ���
        String VENDORITEM_WLMC = venditem.getStr("VENDORITEM_WLMC");//��������
        String CPTMYYGG_TMGZBH = venditem.getStr("CPTMYYGG_TMGZBH");//���������
        String CPTMYYGG_TMXZ_ID = venditem.getStr("CPTMYYGG_TMXZ_ID");//��������_ID
        String CPTMYYGG_TMXZ_CODE = venditem.getStr("CPTMYYGG_TMXZ_CODE");//��������
        String CPTMYYGG_TMXZ_NAME = venditem.getStr("CPTMYYGG_TMXZ_NAME");//��������_NAME
        //������ˮ�Ź���
        DynaBean bean = serviceTemplate.selectOne("JGMES_BASE_TTGGZB", "and JGMES_BASE_TMGG_ID=(select JGMES_BASE_TMGG_ID from JGMES_BASE_TMGG where TMGG_BH='" + CPTMYYGG_TMGZBH + "') and SVTYPE_CODE='4'", "TTGG_STR_LENGTH,TTGG_NUM_START,TTGG_STEP_SIZE");
        if (bean == null) {
            toWrite(jsonBuilder.returnFailureJson("\"�������������û�ж�����ˮ�Ż����˶����ˮ��\""));
            return;
        }
        int TTGG_STR_LENGTH = bean.getInt("TTGG_STR_LENGTH");//����
        int TTGG_NUM_START = bean.getInt("TTGG_NUM_START");//��ʼ��ˮ��
        int TTGG_STEP_SIZE = bean.getInt("TTGG_STEP_SIZE");//��ˮ�Ų���
        if (TTGG_STR_LENGTH == 0 || TTGG_STEP_SIZE == 0) {
            toWrite(jsonBuilder.returnFailureJson("\"�������������û�ж�����ˮ�ų��Ȼ���ˮ�Ų���\""));
            return;
        }
        if (startnum < TTGG_NUM_START) {
            startnum = TTGG_NUM_START;
        }
        double ts = (double) (endnum - startnum + 1) / TTGG_STEP_SIZE;
        int nums = (int) Math.ceil(ts);//����������ȡ��
        //System.out.println(ts+"����:"+nums);
        if (tmxz.equals("TMXZX01")) {//������ֻ����һ��
            nums = 1;
        }
        if (nums == 0) {
            toWrite(jsonBuilder.returnFailureJson("\"�������ɵ���ˮ���Ѵ����ޣ����ܼ������ɣ�\""));
            return;
        }
        System.out.println(endnum + "----" + startnum + "----" + nums);
//        //���������ֵ
//        String barcodekey = getBarcodeKey("", VENDOR_CODE, VENDORITEM_WLBM, CPTMYYGG_TMGZBH, LOT);
//        if (barcodekey.indexOf("err") == 0) {
//            toWrite(jsonBuilder.returnFailureJson("\"����" + barcodekey.substring(4, barcodekey.length() - 4) + "\""));
//            return;
//        }
        HashMap<String, Object> first = getBarcodeMain("", VENDOR_CODE, VENDORITEM_WLBM, CPTMYYGG_TMGZBH, startnum, LOT, 0);//��һ������
//        DynaBean f = (DynaBean) first.get("wlmaxKey");
        //�ж��Ƿ��д���
        String firstBarcode = first.get("result").toString();
        if (firstBarcode.indexOf("����") == 0) {
            toWrite(jsonBuilder.returnFailureJson("\"" + firstBarcode + "\""));
            return;
        }
        String qz = (String) first.get("QZ");
        String hz = (String) first.get("HZ");

        boolean creat = true;
        try {
            String l = request.getParameter("lshgs");
            Integer lshgs = Integer.parseInt(l);
            if (isSussessCreateTM(qz, hz, startnum, endnum, 0, lshgs)) {
                String barcode = "";
                String strnum = "";
                int inum = 0;
                String createBarcodeQZ = "";//Ҫ���ɵ������ǰ׺
                String createBarcodeHZ = "";//Ҫ���ɵ�����ĺ�׺
                HashMap<String, Object> bc = null;//��ʼ��
                String lshgs_frist = "";//��ˮ�Ÿ�ʽ��ʼ��
                DynaBean wlmaxKey = null;//���������ˮ�ű��ʼ��
                List<DynaBean> wltmList = new ArrayList<DynaBean>();
                //��ȡ�����ˮ�ű��Ӧ����
                Integer maxLsh = 0;
                //�������������¼��JGMES_BASE_WLTM
                DynaBean vibarOne = new DynaBean("JGMES_BASE_WLTM", true);
                serviceTemplate.buildModelCreateInfo(vibarOne);
                vibarOne.set("NEWID", pkValue);
                vibarOne.set("VENDOR_CODE", VENDOR_CODE);//��Ӧ�̱���
                vibarOne.set("VENDOR_NAME", VENDOR_NAME);//��Ӧ������
                vibarOne.set("VENDOR_SNAME", VENDOR_SNAME);//��Ӧ�̼��
                vibarOne.set("VENDOR_SCODE", VENDOR_SCODE);//��Ӧ�̼���
                vibarOne.set("WLTM_WLBM", VENDORITEM_WLBM);//���ϱ���
                vibarOne.set("WLTM_WLMC", VENDORITEM_WLMC);//��������
                vibarOne.set("CPTMYYGG_TMGZBH", CPTMYYGG_TMGZBH);//���������
                vibarOne.set("WLTM_TMXZ_ID", CPTMYYGG_TMXZ_ID);//��������_ID
                vibarOne.set("WLTM_TMXZ_CODE", CPTMYYGG_TMXZ_CODE);//��������
                vibarOne.set("WLTM_TMXZ_NAME", CPTMYYGG_TMXZ_NAME);//��������_NAME
                vibarOne.set("WLTM_TMLY_CODE", "XTM");//������Դ_CODE
                vibarOne.set("WLTM_TMLY_NAME", "ϵͳ������");//������Դ_NAME
                vibarOne.set("WLTM_SFYDY", 0);//�Ƿ��Ѵ�ӡ

                for (int i = 0; i < nums; i++) {
                    DynaBean vibar = vibarOne.clone();
                    inum = startnum + i * TTGG_STEP_SIZE;
                    //strnum=getCurNo(TTGG_NUM_START+i*TTGG_STEP_SIZE,TTGG_STR_LENGTH,"0");
                    //barcode=barcodemain+strnum;
                    //��������
                    if (StringUtil.isEmpty(createBarcodeQZ) && StringUtil.isEmpty(createBarcodeHZ)) {//��Ϊ�գ�����һ������
                        bc = getBarcodeMain("", VENDOR_CODE, VENDORITEM_WLBM, CPTMYYGG_TMGZBH, inum, LOT, 0);
                        wlmaxKey = (DynaBean) bc.get("wlmaxKey");
                        if (wlmaxKey == null) {
                            toWrite(jsonBuilder.returnFailureJson("\"���󣺻�ȡ�����ˮ��ʧ��\""));
                            return;
                        }
                        barcode = bc.get("result").toString();
                        if (barcode.indexOf("����") == 0) {
                            toWrite(jsonBuilder.returnFailureJson("\"����" + barcode + "\""));
                            return;
                        }
                        createBarcodeQZ = (String) bc.get("QZ");//ǰ׺
                        createBarcodeHZ = (String) bc.get("HZ");//��׺
                        lshgs_frist = (String) bc.get("lshgs");//��ˮ�Ÿ�ʽ
                        wlmaxKey.setInt("WLMAX_LSHCD", lshgs_frist.length());//��ˮ�ų���
                        DynaBean jgmes_barcode_wlmax = serviceTemplate.selectOne("JGMES_BARCODE_WLMAX", "and WLMAX_QZ='" + createBarcodeQZ + "'and WLMAX_HZ='" + createBarcodeHZ + "' and WLMAX_LSHCD=" + lshgs_frist.length() + " and WLMAX_FLBH=0");
                        List<DynaBean> wltm = serviceTemplate.selectList("JGMES_BASE_WLTM", "and WLTM_TMH = '" + barcode + "'");
                        if (wltm.size() > 0) {
                            if (jgmes_barcode_wlmax == null) {
                                serviceTemplate.insert(wlmaxKey);
                            } else {
                                jgmes_barcode_wlmax.setStr("WLMAX_ZDLSH", inum + "");//�����ˮ��
                                jgmes_barcode_wlmax.setStr("WLMAX_LSHCD", wlmaxKey.getStr("WLMAX_LSHCD"));//�����ˮ��
                                jgmes_barcode_wlmax.setStr("WLMAX_QZ", createBarcodeQZ);
                                jgmes_barcode_wlmax.setStr("WLMAX_HZ", createBarcodeHZ);
                                jgmes_barcode_wlmax.setInt("WLMAX_LSHCD", lshgs_frist.length());
                                serviceTemplate.update(jgmes_barcode_wlmax);
                            }
                            toWrite(jsonBuilder.returnFailureJson("\"���룺" + barcode + "�Ѵ��ڣ�\""));
                            return;
                        }
                    }
                    //���������
                    String codeFormat = "%0" + String.valueOf(lshgs_frist.length() + "d");//��ʽ
                    String lshAll = String.format(codeFormat, inum);//��ˮ�Ÿ�ʽ
                    vibar.set("WLTM_LSH", inum);//��ˮ��
                    barcode = createBarcodeQZ + lshAll + createBarcodeHZ;
                    vibar.set("WLTM_TMH", barcode);//�����
                    String pk = JEUUID.uuid();
                    vibar.set("JGMES_BASE_WLTM_ID", pk);//����id
                    vibar.set(BeanUtils.KEY_PK_CODE, pk);
                    wlmaxKey.setStr("WLMAX_ZDLSH", inum + "");//�����ˮ��
                    vibar.setStr("WLTM_TMQZ", createBarcodeQZ);//����ǰ׺
                    vibar.setStr("WLTM_TMHZ", createBarcodeHZ);//�����׺
                    vibar.setInt("WLTM_TMLSHCD", lshgs_frist.length());//������ˮ�ų���
                    vibar.set("WLTM_JZ", createBarcodeQZ+createBarcodeHZ);//��ֵ
                    if (tmxz.equals("TMXZX01")) {
                        vibar.set("WLTM_SL", sl);//����
                    } else {
                        vibar.set("WLTM_SL", 1);//����
                    }
                    wltmList.add(vibar);
                }
                serviceTemplate.insert(wltmList);
                DynaBean jgmes_barcode_wlmax = serviceTemplate.selectOne("JGMES_BARCODE_WLMAX", "and WLMAX_QZ='" + createBarcodeQZ + "'and WLMAX_HZ='" + createBarcodeHZ + "' and WLMAX_LSHCD=" + lshgs_frist.length() + " and WLMAX_FLBH=0");
                if (jgmes_barcode_wlmax == null) {//���»�������������ˮ�ż�¼��
                    wlmaxKey.setStr("JGMES_BARCODE_WLMAX_ID", JEUUID.uuid());//�������������ˮ�ű�����
                    wlmaxKey.setInt("WLMAX_FLBH", 0);
                    serviceTemplate.insert(wlmaxKey);
                } else {
                    jgmes_barcode_wlmax.setStr("WLMAX_ZDLSH", wlmaxKey.getStr("WLMAX_ZDLSH"));
                    serviceTemplate.update(jgmes_barcode_wlmax);
                }
                toWrite(jsonBuilder.returnSuccessJson("\"�����������ɳɹ���\""));
            } else {
                toWrite(jsonBuilder.returnFailureJson("\"������ˮ��" + startnum + "�Ѿ����������룬�������ظ����ɣ�\""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            toWrite(jsonBuilder.returnSuccessJson("\"�쳣δ֪����\""));
        }
    }

    /**
     * ɾ����Ʒ����
     */
    public void delTM() {
        String cpid = request.getParameter("cpid").replace("[", "").replace("]", "").replace("\"", "");//��Ʒid
        String TMSCJL_YSCSL = request.getParameter("TMSCJL_YSCSL");//����������
        String tmid = request.getParameter("tmid").replace("[", "").replace("]", "").replace("\"", "");
        //����id
        String cpidList[] = cpid.split(",");
        List<String> cList = Arrays.asList(cpidList);
        String tmidList[] = tmid.split(",");
        List<String> tList = Arrays.asList(tmidList);
        DynaBean tm = new DynaBean();//��������
        tm.setStr(beanUtils.KEY_TABLE_CODE, "JGMES_BARCODE_TMSCJL");
        int num = 0;
        try {
            num = Integer.parseInt(TMSCJL_YSCSL);
            num = num - cList.size();
        } catch (Exception e) {
            toWrite(jsonBuilder.returnFailureJson("\"��������������ת��ʧ��\""));
            return;
        }
        DynaBean jgmes_base_gdcptm = serviceTemplate.selectOne("JGMES_BASE_GDCPTM", "and JGMES_BASE_GDCPTM_ID='" + cList.get(0) + "'");
        String qz = jgmes_base_gdcptm.getStr("GDCPTM_TMQZ");//ǰ׺
        String hz = jgmes_base_gdcptm.getStr("GDCPTM_TMHZ");//��׺
        String lshcd = jgmes_base_gdcptm.getStr("GDCPTM_TMLSHCD");//��ˮ�ų���
        Integer lsh = jgmes_base_gdcptm.getInt("GDCPTM_TMLSHCD");//��ˮ��
        Integer maxLsh = 0;
        List<String> notLsh = new ArrayList<String>();
        //����ִ��
        Session ss = serviceTemplate.getSessionFactory().openSession();
        Transaction tr = ss.beginTransaction();
        tr.begin();
        for (int i = 0; i < cList.size(); i++) {
            //ɾ������
            ss.createSQLQuery("delete from JGMES_BASE_GDCPTM where JGMES_BASE_GDCPTM_ID=?").setParameter(0, cList.get(i)).executeUpdate();
            //���������ˮ�ż�¼�����������
            if (StringUtil.isEmpty(qz) && StringUtil.isEmpty(hz) && StringUtil.isEmpty(lshcd)) {
                ss.createSQLQuery("delete from JGMES_BARCODE_WLMAX where JGMES_BARCODE_WLMAX_ID=?").setParameter(0, cList.get(i)).executeUpdate();
            }
            //��ȡ��ǰɾ����������ˮ��
            DynaBean jgmes_base_gdcptm1 = serviceTemplate.selectOne("JGMES_BASE_GDCPTM", "and JGMES_BASE_GDCPTM_ID='" + cList.get(i) + "'");
            if (jgmes_base_gdcptm1 != null) {
                notLsh.add(jgmes_base_gdcptm1.getStr("GDCPTM_LSH"));
            }
        }
        //�������������ˮ�ż�¼��
        if (StringUtil.isNotEmpty(lshcd)) {
            //��ȡ���˵�ǰ�����ˮ��֮��������ˮ��
//            string s = string.Join(",", list.ToArray());
            String str = String.join(",", notLsh);
            String sql = "select max(GDCPTM_LSH) GDCPTM_LSH from JGMES_BASE_GDCPTM where GDCPTM_TMQZ = '" + qz + "' and GDCPTM_TMHZ = '" + hz + "' and GDCPTM_TMLSHCD='" + lshcd + "'  and GDCPTM_LSH not in (" + str + ")";
            System.out.println(sql);
            List<DynaBean> dynaBeans = serviceTemplate.selectListBySql(sql);
            if (dynaBeans.size() > 0) {
                maxLsh = dynaBeans.get(0).getInt("GDCPTM_LSH");//��ȡ�����ˮ��
            }
            ss.createSQLQuery("update JGMES_BARCODE_WLMAX set WLMAX_ZDLSH = ? where WLMAX_QZ=? and WLMAX_HZ=? and WLMAX_LSHCD=? and WLMAX_FLBH=1").setParameter(0, maxLsh).setParameter(1, qz).setParameter(2, hz)
                    .setParameter(3, lshcd).executeUpdate();
        } else {
            //���������

        }
        //��������������
        int query = ss.createSQLQuery("update JGMES_BARCODE_TMSCJL set TMSCJL_YSCSL=? where JGMES_BARCODE_TMSCJL_ID=?").setParameter(0, num + "").setParameter(1, tList.get(0)).executeUpdate();
        System.out.println(query);
        //����ع�������
        try {
            tr.commit();
        } catch (Exception e) {
            tr.rollback();
            e.printStackTrace();
        } finally {
            ss.close();
        }
        toWrite(jsonBuilder.returnSuccessJson("\"ɾ���ɹ���\""));
    }

    /**
     * ɾ����������
     */
    public void delWLBarcode() {
        String wlId = request.getParameter("wlId").replace("[", "").replace("]", "").replace("\"", "");//Ҫɾ������������id
        String[] split = wlId.split(",");
        Integer maxLsh = 0;
        //����ִ��
        Session ss = serviceTemplate.getSessionFactory().openSession();
        Transaction tr = ss.beginTransaction();
        tr.begin();
        for (String id : split) {
            ss.createSQLQuery("delete from JGMES_BASE_WLTM where JGMES_BASE_WLTM_ID=?").setParameter(0, id).executeUpdate();//ɾ����������
            //��ȡ��ǰ���������Ӧ����Ϣ
            DynaBean jgmes_base_wltm = serviceTemplate.selectOne("JGMES_BASE_WLTM", "and JGMES_BASE_WLTM_ID='" + id + "'");
            if (jgmes_base_wltm != null) {
                String qz = jgmes_base_wltm.getStr("WLTM_TMQZ");//ǰ׺
                String hz = jgmes_base_wltm.getStr("WLTM_TMHZ");//��׺
                String lshcd = jgmes_base_wltm.getStr("WLTM_TMLSHCD");//������ˮ�ų���
                String lsh = jgmes_base_wltm.getStr("WLTM_LSH");//��ˮ��
                //��ȡ���˵�ǰ������ˮ��֮��������ˮ��
                String sql = "select max(WLTM_LSH) WLTM_LSH from JGMES_BASE_WLTM where WLTM_TMQZ='" + qz + "' and WLTM_TMHZ ='" + hz + "' and WLTM_TMLSHCD=" + lshcd + " and WLTM_LSH not in (" + lsh + ")";
                List<DynaBean> dynaBeans = serviceTemplate.selectListBySql(sql);
                if (dynaBeans.size() > 0) {
                    maxLsh = dynaBeans.get(0).getInt("WLTM_LSH");//��ȡ�����ˮ��
                    ss.createSQLQuery("update JGMES_BARCODE_WLMAX set WLMAX_ZDLSH = ? where WLMAX_QZ=? and WLMAX_HZ=? and WLMAX_LSHCD=? and WLMAX_FLBH=0").setParameter(0, maxLsh).setParameter(1, qz).setParameter(2, hz)
                            .setParameter(3, lshcd).executeUpdate();
                }
            }
        }
        //����ع�������
        try {
            tr.commit();
        } catch (Exception e) {
            tr.rollback();
            e.printStackTrace();
        } finally {
            ss.close();
        }
        toWrite(jsonBuilder.returnSuccessJson("\"ɾ���ɹ���\""));


    }


    /**
     * ɾ���ⲿ��
     */
    public void delOutTM() {
        String cpid = request.getParameter("cpid").replace("[", "").replace("]", "").replace("\"", "");//��������id
        String scrwid = request.getParameter("scrwid").replace("[", "").replace("]", "").replace("\"", "");
        ;//��������id
        String tmid = request.getParameter("tmid").replace("[", "").replace("]", "").replace("\"", "");
        ;//����id
        String cpidList[] = cpid.split(",");
        List<String> cList = Arrays.asList(cpidList);
        String scrwidList[] = scrwid.split(",");
        List<String> sList = Arrays.asList(scrwid);
        String tmidList[] = tmid.split(",");
        List<String> tList = Arrays.asList(tmidList);
        List<Integer> yscslList = new ArrayList();
        DynaBean tm = new DynaBean();//��������
        tm.setStr(beanUtils.KEY_TABLE_CODE, "JGMES_BARCODE_TMSCJL");
        for (String s : scrwidList) {
            String sql = "select TMSCJL_YSCSL from jgmes_barcode_todolist where JGMES_PLAN_SCRW_ID ='" + s + "'";
            List<DynaBean> yscslBeans = serviceTemplate.selectListBySql(sql);
            yscslList.add(yscslBeans.get(0).getInt("TMSCJL_YSCSL"));
        }

        Map<String, Integer> map = new HashMap<String, Integer>();
        for (String item : tList) {
            if (map.containsKey(item)) {
                map.put(item, map.get(item).intValue() + 1);
            } else {
                map.put(item, new Integer(1));
            }
        }
        //����ִ��
        Session ss = serviceTemplate.getSessionFactory().openSession();
        Transaction tr = ss.beginTransaction();
        tr.begin();
        Iterator<String> keys = map.keySet().iterator();
        //��ȡͬһ���������񵥺ŵ���������
//        List<Integer> num = new ArrayList();
        while (keys.hasNext()) {
            String key = keys.next();
            Integer n = map.get(key).intValue();
            int i = 0;
            int yscsl = yscslList.get(i);
            int lastNum = yscsl - n;//���ֵ
//            num.add(map.get(key).intValue());
            int query = ss.createSQLQuery("update JGMES_BARCODE_TMSCJL set TMSCJL_YSCSL=? where JGMES_BARCODE_TMSCJL_ID=?").setParameter(0, lastNum + "").setParameter(1, key).executeUpdate();
        }

        for (int i = 0; i < cList.size(); i++) {
            System.out.println(cList.get(i));
            ss.createSQLQuery("delete from JGMES_BASE_GDCPTM where JGMES_BASE_GDCPTM_ID=?").setParameter(0, cList.get(i)).executeUpdate();
        }
        //����ع�������
        try {
            tr.commit();
        } catch (Exception e) {
            tr.rollback();
            e.printStackTrace();
        } finally {
            ss.close();
        }
        toWrite(jsonBuilder.returnSuccessJson("\"ɾ���ɹ���\""));
    }

    /**
     * ��ȡ��������ɵ����������
     */
    public void getAllBarcodeNum() {
        String PCLB_CPBH = request.getParameter("cpbh");//��Ʒ���
        String PCLB_PCSLStr = request.getParameter("pcsl");//�Ų�����
        String CPTMYYGG_YYGZBH = request.getParameter("yygzbh");//Ӧ�ù�����
        String yscslStr = request.getParameter("yscsl");//����������
        Integer num = 0;
        try {
            Integer PCLB_PCSL = Integer.parseInt(PCLB_PCSLStr);//ת����ֵ����
            Integer yscsl = Integer.parseInt(yscslStr);
            int canCreateNum = calBarcodeNum(PCLB_CPBH, PCLB_PCSL, CPTMYYGG_YYGZBH);//�������ɵ�����
            if (canCreateNum - yscsl > 0) {
                num = canCreateNum - yscsl;
                toWrite(jsonBuilder.returnSuccessJson("\"" + num + "\""));
            } else {
                toWrite(jsonBuilder.returnFailureJson("\"�ѳ��������ɵ��������,��Ҫ����������ʹ��ǿ�����ɰ�ť��\""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            toWrite(jsonBuilder.returnFailureJson("\"ϵͳ����\""));
            return;
        }
    }

    /**
     * ��������Ƿ��������
     * 0 �����Ѿ�������ϣ������ټ�������
     * ����0��ʾ���������ɶ�������
     * -1�������������������
     */
    public void checkCreateNum() {
        String PCLB_CPBH = request.getParameter("cpbh");//��Ʒ���
        String PCLB_PCSLStr = request.getParameter("pcsl");//�Ų�����
        String CPTMYYGG_YYGZBH = request.getParameter("yygzbh");//Ӧ�ù�����
        String createNumStr = request.getParameter("createNum");//��Ҫ���ɵ�����
        String yscslStr = request.getParameter("yscsl");//����������
        Integer num = 0;
        try {
            Integer PCLB_PCSL = Integer.parseInt(PCLB_PCSLStr);//ת����ֵ����
            Integer createNum = Integer.parseInt(createNumStr);
            Integer yscsl = Integer.parseInt(yscslStr);
            int canCreateNum = calBarcodeNum(PCLB_CPBH, PCLB_PCSL, CPTMYYGG_YYGZBH);//�������ɵ�����
            if (canCreateNum == yscsl) {
                num = 0;
            } else if (createNum > canCreateNum) {
                num = canCreateNum - yscsl;
            } else {
                num = -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            toWrite(jsonBuilder.returnFailureJson("\"ϵͳ����\""));
            return;
        }
        toWrite(jsonBuilder.returnSuccessJson("\"" + num + "\""));
    }

    /**
     * �ж���ˮ���Ƿ���Ч�����
     */
    public void netIsSussessCreateTM() {
        String qz = request.getParameter("QZ");//ǰ׺
        String hz = request.getParameter("HZ");//��׺
        String s = request.getParameter("startnum");//��ʼ��ˮ��
        String e = request.getParameter("endnum");//������ˮ��
        String f = request.getParameter("FL");//����
        String l = request.getParameter("lshgs");//��ˮ�ų���
        int startnum = 0;
        int endnum = 0;
        int fl = 0;
        int lshcd = 0;
        try {
            startnum = Integer.parseInt(s);
            endnum = Integer.parseInt(e);
            fl = Integer.parseInt(f);
            lshcd = Integer.parseInt(l);
        } catch (Exception exception) {
            toWrite(jsonBuilder.returnFailureJson("\"�쳣����\""));
            exception.printStackTrace();
        }
        boolean sussessCreateTM = isSussessCreateTM(qz, hz, startnum, endnum, fl, lshcd);
        if (sussessCreateTM) {
            toWrite(jsonBuilder.returnSuccessJson("\"" + startnum + "\""));
        } else {
            toWrite(jsonBuilder.returnFailureJson("\"Ҫ���ɵ������к������������룬�������ɣ�\""));
        }
    }

    /**
     * �����ˮ���Ƿ���Ч
     *
     * @param qz
     * @param hz
     * @param startnum
     * @param endnum
     */
    public boolean isSussessCreateTM(String qz, String hz, int startnum, int endnum, int FL, int lshcd) {
        sql = "select WLMAX_ZDLSH from JGMES_BARCODE_WLMAX where WLMAX_QZ = '" + qz + "' and WLMAX_HZ='" + hz + "' and WLMAX_FLBH=" + FL + " and WLMAX_LSHCD =" + lshcd + " and WLMAX_FLBH=" + FL;
        List<DynaBean> lshList = serviceTemplate.selectListBySql(sql);
        if (lshList.size() == 0) {
            return true;
        } else {
            for (DynaBean d : lshList) {
                for (int i = startnum; i <= endnum; i++) {
                    if (i == d.getInt("WLMAX_ZDLSH")) {
                        return false;
                    }
                }
            }

            return true;
        }
    }

    /**
     * ��ȡ�����ˮ��
     */
    public void getKeyValues() {
        // ǰ̨�������,ljs
        String LOT = request.getParameter("LOT");//����
        String cust = request.getParameter("cust");//�ͻ����
        String VENDOR = request.getParameter("VENDOR");//��Ӧ�̱��
        String WLBH = request.getParameter("WLBH");//���ϣ���Ʒ������
        String BCRULE = request.getParameter("BCRULE");//�������ɹ������
        String fls = request.getParameter("FL");//����
        String lshcd = request.getParameter("lshgs");//��ˮ�ų���
        int fl = 0;
        try {
            fl = Integer.parseInt(fls);
        } catch (Exception e) {
            e.printStackTrace();
            toWrite(jsonBuilder.returnFailureJson("��ȡ���Ϸ���ʧ�ܣ�"));
            return;
        }
        HashMap<String, Object> code = getBarcodeMain(cust, VENDOR, WLBH, BCRULE, 1, LOT, fl);//��һ������
        DynaBean f = (DynaBean) code.get("wlmaxKey");
        //�ж��Ƿ��д���
        String barcode = code.get("result").toString();
        if (barcode.indexOf("����") == 0) {
            toWrite(jsonBuilder.returnFailureJson("\"" + barcode + "\""));
            return;
        }
        if (barcode.length() > 100) {
            toWrite(jsonBuilder.returnFailureJson("\"�������ɵ����볤�ȹ��������ʵ��������ų��Ȼ������������е�ѡ��\""));
        }
        DynaBean c = (DynaBean) code.get("wlmaxKey");
        String qz = c.getStr("WLMAX_QZ");
        String hz = c.getStr("WLMAX_HZ");
        String maxLsh = "";
        String maxLshSql = "select max(WLMAX_ZDLSH+0) as WLMAX_ZDLSH from JGMES_BARCODE_WLMAX  where WLMAX_QZ='" + qz + "' and WLMAX_HZ='" + hz + "' and WLMAX_FLBH=" + fl + " and WLMAX_LSHCD=" + lshcd + " and WLMAX_FLBH=" + fl;
        List<DynaBean> maxLshList = serviceTemplate.selectListBySql(maxLshSql);
        if (StringUtil.isEmpty(maxLshList.get(0).getStr("WLMAX_ZDLSH"))) {
            maxLsh = "0";
        } else {
            maxLsh = maxLshList.get(0).getStr("WLMAX_ZDLSH");
        }
        c.setStr("maxLsh", maxLsh);
        toWrite(jsonBuilder.returnSuccessJson(DynaAction.jsonBuilder.toJson(c)));
//        toWrite(jsonBuilder.returnSuccessJson("\"" + maxLsh + "\""));
    }

    //�������������������
    //cValue:��ѯ����ֵ--�ͻ�����
    //vValue:��ѯ����ֵ--��Ӧ�̱���
    //iValue:��ѯ����ֵ--���ϱ���
    //bcrule:�������ɹ������
    //num����ˮ��
    private HashMap<String, Object> getBarcodeMain(String cValue, String vValue, String iValue, String bcrule, int num, String LOT, int flbh) {
        String result = "";
        String errMsg = "";
        String strSql = "select * from JGMES_BASE_TTGGZB where JGMES_BASE_TMGG_ID=(select JGMES_BASE_TMGG_ID from JGMES_BASE_TMGG where TMGG_BH='" + bcrule + "') order by SY_ORDERINDEX";

        DynaBean wlmaxKey = new DynaBean();//���������ˮ�ű�
        wlmaxKey.setStr(beanUtils.KEY_TABLE_CODE, "JGMES_BARCODE_WLMAX");
        wlmaxKey.setInt("WLMAX_FLBH", flbh);//���ϻ��߳�Ʒ
        String qz = "";//ǰ׺
        String hz = "";//��׺
        String qLsh = "";//ǰ׺+��ˮ��
        String lshgs = "";//��ˮ�Ÿ�ʽ
        HashMap<String, Object> BarcodeMain = new HashMap();

        List<DynaBean> beans = serviceTemplate.selectListBySql(strSql);
        if (beans != null) {
            String SVTYPE_CODE = "";//ȡֵ��ʽ��1--����	2--ȫ�ֱ���	3--���ֶ�	4--��ˮ��
            String TTGG_CVALUE = "";//������Ӧֵ
            String GVAR_CODE = "";//ȫ�ֱ�����UserName--��¼�û�	UserCode--��¼�û�����	UserID--��¼�û�ID	CurrentDate--��ǰ����
            String GVAR_NAME = "";
            String SVTABLE_CODE = "";//��Դ����JGMES_BASE_CUST--�ͻ�		JGMES_BASE_VENDOR--��Ӧ��		JGMES_BASE_PRODUCTDATA--����
            String SVTABLE_NAME = "";
            String FFIELD_CODE = "";//�����ֶ�
            String FVALUE_CODE = "";//ȡֵ�ֶ�
            String TTGG_SOURCE = "";//�滻Դ
            String TTGG_TARGET = "";//�滻Ŀ��
            String TTGG_FORMAT = "";//��ʽ[������]
            int TTGG_STR_START = 0;//��ʼ��ȡ
            int TTGG_STR_LENGTH = 0;//����
            //int TTGG_NUM_START=0;//��ʼ��ˮ��
            int TTGG_STEP_SIZE = 0;//��ˮ�Ų���

            String curCode = "";

            //�ֵ�bean
            List<DynaBean> dics = null;
            String DICTIONARYITEM_ITEMNAME = "", DICTIONARYITEM_ITEMCODE = "";

            for (DynaBean bean : beans) {
                SVTYPE_CODE = bean.getStr("SVTYPE_CODE");
                TTGG_CVALUE = bean.getStr("TTGG_CVALUE");
                GVAR_CODE = bean.getStr("GVAR_CODE");
                GVAR_NAME = bean.getStr("GVAR_NAME");
                SVTABLE_CODE = bean.getStr("SVTABLE_CODE");
                SVTABLE_NAME = bean.getStr("SVTABLE_NAME");
                FFIELD_CODE = bean.getStr("FFIELD_CODE");
                FVALUE_CODE = bean.getStr("FVALUE_CODE");
                TTGG_SOURCE = bean.getStr("TTGG_SOURCE");
                TTGG_TARGET = bean.getStr("TTGG_TARGET");
                TTGG_FORMAT = bean.getStr("TTGG_FORMAT");
                TTGG_STR_START = bean.getInt("TTGG_STR_START");
                TTGG_STR_LENGTH = bean.getInt("TTGG_STR_LENGTH");
                //TTGG_NUM_START=bean.getInt("TTGG_NUM_START");
                TTGG_STEP_SIZE = bean.getInt("TTGG_STEP_SIZE");

                //���ֵ��滻�������ֵ�bean
                if (TTGG_SOURCE.equals("DIC")) {
                    if (StringUtils.isEmpty(TTGG_TARGET)) {
                        errMsg += "�������ֵ��滻ʱû�������ֵ����!";
                    } else {
                        strSql = "select DICTIONARYITEM_ITEMNAME,DICTIONARYITEM_ITEMCODE from JE_CORE_DICTIONARYITEM a "
                                + "left join JE_CORE_DICTIONARY b on a.DICTIONARYITEM_DICTIONARY_ID=b.JE_CORE_DICTIONARY_ID "
                                + "where 1=1 and SY_PARENT !='' and DICTIONARY_DDCODE='" + TTGG_TARGET + "'";
                        dics = serviceTemplate.selectListBySql(strSql);
                    }
                }

                if (SVTYPE_CODE.equals("1")) {
                    //����
                    if (TTGG_CVALUE.equals("LOT")) {
                        curCode = LOT;
                    } else {
                        curCode = TTGG_CVALUE;
                    }
                } else if (SVTYPE_CODE.equals("2")) {
                    //ȫ�ֱ���
                    JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
                    if (GVAR_NAME.equals("��¼�û�")) {
//						curCode=SecurityUserHolder.getCurrentUser().getUsername();
                        curCode = jgmesCommon.jgmesUser.getCurrentUserName();
                    } else if (GVAR_NAME.equals("��¼�û�����")) {
//						curCode=SecurityUserHolder.getCurrentUser().getUserCode();
                        curCode = jgmesCommon.jgmesUser.getCurrentUserCode();
                    } else if (GVAR_NAME.equals("��¼�û�ID")) {
//						curCode=SecurityUserHolder.getCurrentUser().getUserId();
                        jgmesCommon.jgmesUser.getCurrentUserID();
                    } else if (GVAR_NAME.equals("��ǰ����")) {
                        //�������ڸ�ʽ
                        if (StringUtil.isNotEmpty(TTGG_FORMAT)) {
                            SimpleDateFormat sDateFormat = new SimpleDateFormat(TTGG_FORMAT);
                            curCode = sDateFormat.format(new java.util.Date());
                        }
                    }
                } else if (SVTYPE_CODE.equals("3")) {
                    //���ֶ�
                    String FFIELD_VALUE = "";
                    if (SVTABLE_NAME.equals("�ͻ�")) {
                        FFIELD_VALUE = cValue;
                    } else if (SVTABLE_NAME.equals("��Ӧ��")) {
                        FFIELD_VALUE = vValue;
                    } else if (SVTABLE_NAME.equals("����")) {
                        FFIELD_VALUE = iValue;
                    }
                    if (FFIELD_VALUE.isEmpty() || FFIELD_VALUE.equals("")) {
                        if (SVTABLE_NAME.equals("�ͻ�")) {
                            errMsg += "����" + SVTABLE_NAME + "��ϢΪ��,��������������еĿͻ���Ϣ��";
                            errMsg += "��������������������룬������������ɾ���ͻ����ֶΣ�";
                        }
                        if (SVTABLE_NAME.equals("��Ӧ��")) {
                            errMsg += "����" + SVTABLE_NAME + "��ϢΪ��,�������Ϣ��";
                            errMsg += SVTABLE_NAME + "������������ǳ�Ʒ���룬������������ɾ����Ӧ�̱��ֶΣ�";
                        }
                        BarcodeMain.put("result", errMsg);
                        return BarcodeMain;
                    }
                    DynaBean fbean = serviceTemplate.selectOne(SVTABLE_CODE, "and " + FFIELD_CODE + "='" + FFIELD_VALUE + "'", FVALUE_CODE);
                    if (fbean != null) {
                        curCode = fbean.getStr(FVALUE_CODE);
                    } else {
//						errMsg+="��ѯ��"+SVTABLE_CODE+"���ֶ�ֵ"+FVALUE_CODE+"ʱ����!";
                        errMsg += "�������������쳣��";
                    }
                } else if (SVTYPE_CODE.equals("4")) {
                    //��ˮ��
                    //int num=startNum+(iNum)*TTGG_STEP_SIZE;
                    curCode = getCurNo(num, TTGG_STR_LENGTH, "0");//��ˮ��
                    //System.out.println(num+"/"+curCode);
//                    wlmaxKey.setStr("WLMAX_ZDLSH", num + "");//�������������ˮ��
                }

                //�滻
                if (TTGG_SOURCE.equals("DIC")) {
                    //System.out.println("OK");
                    //���ֵ��滻
                    if (dics != null) {
                        //System.out.println("OK1");
                        for (DynaBean dic : dics) {
                            DICTIONARYITEM_ITEMNAME = dic.getStr("DICTIONARYITEM_ITEMNAME");
                            DICTIONARYITEM_ITEMCODE = dic.getStr("DICTIONARYITEM_ITEMCODE");
                            //System.out.println(DICTIONARYITEM_ITEMNAME+"/"+DICTIONARYITEM_ITEMCODE+"/"+curCode);
                            if (DICTIONARYITEM_ITEMNAME.equals(curCode)) {
                                curCode = curCode.replaceAll(DICTIONARYITEM_ITEMNAME, DICTIONARYITEM_ITEMCODE);
                            }
                        }
                    }
                } else {
                    curCode = curCode.replaceAll(TTGG_SOURCE, TTGG_TARGET);
                }

                //��ȡ
                curCode = getStr(curCode, TTGG_STR_START, TTGG_STR_LENGTH);
                if (curCode.indexOf("����") == 0) {  //SVTABLE_NAME
                    errMsg += curCode;
                }
                if (SVTYPE_CODE.equals("4")) {
                    qz = result;
                    wlmaxKey.setStr("WLMAX_QZ", qz);
                    lshgs = curCode;
                    qLsh = qz + curCode;
                }
                //���
                result += curCode;
            }
        }

        if (errMsg.isEmpty()) {
            hz = result.replace(qLsh, "");//��ȡ��׺
            wlmaxKey.setStr("WLMAX_HZ", hz);//��׺
            BarcodeMain.put("wlmaxKey", wlmaxKey);//�����ˮ��
            BarcodeMain.put("result", result);//�����
            BarcodeMain.put("QZ", qz);//ǰ׺
            BarcodeMain.put("HZ", hz);//��׺
            BarcodeMain.put("lshgs", lshgs);//��һ���������ˮ��+��ʽ
//			serviceTemplate.insert(wlmaxKey);//�����¼�� ljs
            return BarcodeMain;
        } else {
            BarcodeMain.put("result", errMsg);
            return BarcodeMain;
        }
    }


//    //�������������������ؼ�ֵ(��������ˮ��)
//    //cValue:��ѯ����ֵ--�ͻ�����
//    //vValue:��ѯ����ֵ--��Ӧ�̱���
//    //iValue:��ѯ����ֵ--���ϱ���
//    //bcrule:�������ɹ������
//    private String getBarcodeKey(String cValue, String vValue, String iValue, String bcrule, String LOT) {
//        String result = "";
//        String errMsg = "";
//        //��ȡ����Ӧ�ù���
//        String strSql = "select * from JGMES_BASE_TTGGZB where JGMES_BASE_TMGG_ID=(select JGMES_BASE_TMGG_ID from JGMES_BASE_TMGG where TMGG_BH='" + bcrule + "') and SVTYPE_CODE<>'4' and TTGGZB_XHSZ=1 order by SY_ORDERINDEX";
//        List<DynaBean> beans = serviceTemplate.selectListBySql(strSql);
//        if (beans != null) {
//            String SVTYPE_CODE = "";//ȡֵ��ʽ��1--����	2--ȫ�ֱ���	3--���ֶ�	4--��ˮ��
//            String TTGG_CVALUE = "";//������Ӧֵ
//            String GVAR_CODE = "";//ȫ�ֱ�����UserName--��¼�û�	UserCode--��¼�û�����	UserID--��¼�û�ID	CurrentDate--��ǰ����
//            String GVAR_NAME = "";
//            String SVTABLE_CODE = "";//��Դ����JGMES_BASE_CUST--�ͻ�		JGMES_BASE_VENDOR--��Ӧ��		JGMES_BASE_PRODUCTDATA--����
//            String SVTABLE_NAME = "";
//            String FFIELD_CODE = "";//�����ֶ�
//            String FVALUE_CODE = "";//ȡֵ�ֶ�
//            String TTGG_SOURCE = "";//�滻Դ
//            String TTGG_TARGET = "";//�滻Ŀ��
//            String TTGG_FORMAT = "";//��ʽ[������]
//            int TTGG_STR_START = 0;//��ʼ��ȡ
//            int TTGG_STR_LENGTH = 0;//����
//            //int TTGG_NUM_START=0;//��ʼ��ˮ��
//            //int TTGG_STEP_SIZE=0;//��ˮ�Ų���
//            String curCode = "";
//
//            //�ֵ�bean
//            List<DynaBean> dics = null;
//            String DICTIONARYITEM_ITEMNAME = "", DICTIONARYITEM_ITEMCODE = "";
//
//            for (DynaBean bean : beans) {
//                SVTYPE_CODE = bean.getStr("SVTYPE_CODE");
//                TTGG_CVALUE = bean.getStr("TTGG_CVALUE");
//                GVAR_CODE = bean.getStr("GVAR_CODE");
//                GVAR_NAME = bean.getStr("GVAR_NAME");
//                SVTABLE_CODE = bean.getStr("SVTABLE_CODE");
//                SVTABLE_NAME = bean.getStr("SVTABLE_NAME");
//                FFIELD_CODE = bean.getStr("FFIELD_CODE");
//                FVALUE_CODE = bean.getStr("FVALUE_CODE");
//                TTGG_SOURCE = bean.getStr("TTGG_SOURCE");
//                TTGG_TARGET = bean.getStr("TTGG_TARGET");
//                TTGG_FORMAT = bean.getStr("TTGG_FORMAT");
//                TTGG_STR_START = bean.getInt("TTGG_STR_START");
//                TTGG_STR_LENGTH = bean.getInt("TTGG_STR_LENGTH");
//                //TTGG_NUM_START=bean.getInt("TTGG_NUM_START");
//                //TTGG_STEP_SIZE=bean.getInt("TTGG_STEP_SIZE");
//
//                //���ֵ��滻�������ֵ�bean
//                if (TTGG_SOURCE.equals("DIC")) {
//                    if (StringUtils.isEmpty(TTGG_TARGET)) {
//                        errMsg += "�������ֵ��滻ʱû�������ֵ����!";
//                    } else {
//                        strSql = "select DICTIONARYITEM_ITEMNAME,DICTIONARYITEM_ITEMCODE from JE_CORE_DICTIONARYITEM a "
//                                + "left join JE_CORE_DICTIONARY b on a.DICTIONARYITEM_DICTIONARY_ID=b.JE_CORE_DICTIONARY_ID "
//                                + "where 1=1 and SY_PARENT !='' and DICTIONARY_DDCODE='" + TTGG_TARGET + "'";
//                        dics = serviceTemplate.selectListBySql(strSql);
//                    }
//                }
//
//                if (SVTYPE_CODE.equals("1")) {
//                    //����
//                    if (TTGG_CVALUE.equals("LOT")) {
//                        curCode = LOT;
//                    } else {
//                        curCode = TTGG_CVALUE;
//                    }
//                } else if (SVTYPE_CODE.equals("2")) {
//                    //ȫ�ֱ���
//                    JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
//                    if (GVAR_NAME.equals("��¼�û�")) {
////						curCode=SecurityUserHolder.getCurrentUser().getUsername();
//                        curCode = jgmesCommon.jgmesUser.getCurrentUserName();
//                    } else if (GVAR_NAME.equals("��¼�û�����")) {
////						curCode=SecurityUserHolder.getCurrentUser().getUserCode();
//                        curCode = jgmesCommon.jgmesUser.getCurrentUserCode();
//                    } else if (GVAR_NAME.equals("��¼�û�ID")) {
////						curCode=SecurityUserHolder.getCurrentUser().getUserId();
//                        jgmesCommon.jgmesUser.getCurrentUserID();
//                    } else if (GVAR_NAME.equals("��ǰ����")) {
//                        //�������ڸ�ʽ
//                        if (StringUtil.isNotEmpty(TTGG_FORMAT)) {
//                            SimpleDateFormat sDateFormat = new SimpleDateFormat(TTGG_FORMAT);
//                            curCode = sDateFormat.format(new java.util.Date());
//                        }
//                    }
//                } else if (SVTYPE_CODE.equals("3")) {
//                    //���ֶ�
//                    String FFIELD_VALUE = "";
//                    if (SVTABLE_NAME.equals("�ͻ�")) {
//                        FFIELD_VALUE = cValue;
//                    } else if (SVTABLE_NAME.equals("��Ӧ��")) {
//                        FFIELD_VALUE = vValue;
//                    } else if (SVTABLE_NAME.equals("����")) {
//                        FFIELD_VALUE = iValue;
//                    }
//                    if (FFIELD_VALUE.isEmpty() || FFIELD_VALUE.equals("")) {
//                        if (SVTABLE_NAME.equals("�ͻ�")) {
//                            errMsg += "����" + SVTABLE_NAME + "��ϢΪ��,��������������еĿͻ���Ϣ��";
//                            errMsg += "��������������������룬������������ɾ���ͻ����ֶΣ�";
//                        }
//                        if (SVTABLE_NAME.equals("��Ӧ��")) {
//                            errMsg += "����" + SVTABLE_NAME + "��ϢΪ��,�������Ϣ��";
//                            errMsg += SVTABLE_NAME + "������������ǳ�Ʒ���룬������������ɾ����Ӧ�̱��ֶΣ�";
//                        }
//                        return errMsg;
//                    }
//                    DynaBean fbean = serviceTemplate.selectOne(SVTABLE_CODE, "and " + FFIELD_CODE + "='" + FFIELD_VALUE + "'", FVALUE_CODE);
//                    if (fbean != null) {
//                        curCode = fbean.getStr(FVALUE_CODE);
//                    } else {
////						errMsg+="��ѯ��"+SVTABLE_CODE+"���ֶ�ֵ"+FVALUE_CODE+"ʱ����!";
//                        errMsg += "��������������" + SVTABLE_NAME + "��Ϣ����" + SVTABLE_NAME + "��ϢΪ�գ��������������ж�Ӧ�Ĺ���";
//                        errMsg += "���������쳣��";
//                    }
//                }
//                //�滻
//                if (TTGG_SOURCE.equals("DIC")) {
//                    //���ֵ��滻
//                    if (dics != null) {
//                        for (DynaBean dic : dics) {
//                            DICTIONARYITEM_ITEMNAME = dic.getStr("DICTIONARYITEM_ITEMNAME");//�ֵ�������
//                            DICTIONARYITEM_ITEMCODE = dic.getStr("DICTIONARYITEM_ITEMCODE");//�ֵ���ֵ
//                            if (DICTIONARYITEM_ITEMNAME.equals(curCode)) {
//                                curCode = curCode.replaceAll(DICTIONARYITEM_ITEMNAME, DICTIONARYITEM_ITEMCODE);
//                            }
//                        }
//                    }
//                } else {
//                    curCode = curCode.replaceAll(TTGG_SOURCE, TTGG_TARGET);
//                }
//
//                //��ȡ
//                curCode = getStr(curCode, TTGG_STR_START, TTGG_STR_LENGTH);
//                result += curCode;
//            }
//        }
//        if (errMsg.isEmpty()) {
//            return result;
//        } else {
//            return "err:" + errMsg;
//        }
//    }

    //��ȡ�ַ���
    private String getStr(String fSource, int sNum, int sLen) {
        String result = "";
        if (StringUtil.isNotEmpty(fSource)) {
            if (fSource.length() <= sNum) {
                result = "����:��ʼ��ȡ�ĳ��ȴ��ڻ��ߵ��ڶ�Ӧ�ֶεĳ������������������";
                return result;
            }
            if (fSource.length() < sLen) {
                result = "����:��ȡ�ĳ��ȴ��ڶ�Ӧ�ֶεĳ������������������";
                return result;
            }
            int length = sNum + sLen;
            if (length > fSource.length()) {
                result = "����:���Խ�ȡ�ĳ��ȳ����ֶεĳ��ȣ����������������";
                return result;
            }
        }
        if (sNum >= 0 && sLen > 0) {
            result = fSource.substring(sNum, sNum + sLen);
        } else {
            result = fSource;
        }

        return result;
    }


    /**
     * curno��ʾ��Ҫ�������ַ�
     * length��ʾҪ���ĳ���
     * fillStr��ʾ��Ҫ�����ַ�
     * ����Ҫ��������ַ���
     */
    public String getCurNo(int curno, int length, String fillStr) {
        int temp = curno;
        StringBuffer sb = new StringBuffer(length);
        int count = 0;
        while (curno / 10 != 0) {
            curno = curno / 10;
            count++;
        }
        int size = length - count - 1;
        for (int i = 0; i < size; i++) {
            sb.append(fillStr);
        }
        sb.append(temp);
        return sb.toString();
    }

    //���ն�������Ӧ����������
    public void getBcNum() {
        String Item = request.getParameter("Item");
        int OrderQty = Integer.parseInt(request.getParameter("OrderQty"));
        String yyrule = request.getParameter("yyrule");

        int num = calBarcodeNum(Item, OrderQty, yyrule);
        toWrite(jsonBuilder.returnSuccessJson(String.valueOf(num)));
    }

    //���㶩��Ӧ������������
    //Item:���ϱ���
    //OrderQty����������
    //yyrule��Ӧ�ù������
    public int calBarcodeNum(String Item, int OrderQty, String yyrule) {
        int num = 0;
        //��ѯ��������Ӧ�ù���
        String strSql = "select CPTMYYGG_YYGZBH,CPTMYYGG_TMLX_CODE,CPTMYYGG_NZWTMLX_CODE,CPTMYYGG_MTMSL from JGMES_BASE_CPTMYYGG where CPTMYYGG_CPBH='" + Item + "'";
        List<DynaBean> items = serviceTemplate.selectListBySql(strSql);
        if (items == null || items.size() == 0) {
            toWrite(jsonBuilder.returnFailureJson("����δ�ܲ��ҵ�����" + Item + "������Ӧ�ù���"));
            return num;
        }
        String NZWTMLX_CODE = "", YYGZBH = "";
        int MTMSL = 0, nMTMSL = 0;
        for (DynaBean item : items) {
            YYGZBH = item.getStr("CPTMYYGG_YYGZBH");//Ӧ�ù�����
            NZWTMLX_CODE = item.getStr("CPTMYYGG_NZWTMLX_CODE");//��װ����������
            if (yyrule.equals(YYGZBH)) {
                //System.out.println("1:"+TMLX_CODE+"/"+YYGZBH);
                MTMSL = item.getInt("CPTMYYGG_MTMSL");//����ÿ��������

                nMTMSL = getBarcodeNum(items, NZWTMLX_CODE);//ѭ�����ü����¼�Ӧ������������

                num = nMTMSL * MTMSL;
            }
        }
        //System.out.println("num:"+num);
        double ts = (double) OrderQty / num;
        int nums = (int) Math.ceil(ts);//����������ȡ��

        return nums;
    }

    //ѭ�����ü����¼�Ӧ������������
    public int getBarcodeNum(List<DynaBean> items, String pCODE) {
        int num = 1;

        String TMLX_CODE = "", NZWTMLX_CODE = "";
        int MTMSL = 0, nMTMSL = 0;
        for (DynaBean item : items) {
            TMLX_CODE = item.getStr("CPTMYYGG_TMLX_CODE");//��������
            NZWTMLX_CODE = item.getStr("CPTMYYGG_NZWTMLX_CODE");//��װ����������
            //System.out.println("2:"+NZWTMLX_CODE+"/"+TMLX_CODE);
            if (TMLX_CODE.equals(pCODE)) {
                MTMSL = item.getInt("CPTMYYGG_MTMSL");//����ÿ��������

                nMTMSL = getBarcodeNum(items, NZWTMLX_CODE);//�¼�ÿ��������

                num = nMTMSL * MTMSL;
            }
        }

        return num;
    }

    /*
     * ���ܣ�����or��ѯ�����¼
     * author: Luo_wr
     * date 2019/01/15
     */
    private String addOrFindBarCode(DynaBean zbean, DynaBean tmlxbean, DynaBean cpbean, DynaBean cxbean) {
        List<DynaBean> tmjlBean = serviceTemplate.selectList("JGMES_BARCODE_TMSCJL",
                " and JGMES_BARCODE_TMSCJL_ID='" + zbean.get("JGMES_BARCODE_TMSCJL_ID")
                        + "' and TMSCJL_TMLX_CODE='" + zbean.getStr("CPTMYYGG_TMLX_CODE") + "'");
        String tmscjlId = "";
        if (tmjlBean.size() > 0) {
            tmscjlId = tmjlBean.get(0).getStr("JGMES_BARCODE_TMSCJL_ID");
        } else {
            String tmjlId = JEUUID.uuid();
            tmscjlId = tmjlId;
            DynaBean tmjlbean = new DynaBean();
            tmjlbean.setStr(BeanUtils.KEY_TABLE_CODE, "JGMES_BARCODE_TMSCJL");
            serviceTemplate.buildModelCreateInfo(tmjlbean);
            tmjlbean.set("JGMES_BARCODE_TMSCJL_ID", tmjlId);
            tmjlbean.set("TMSCJL_CPBH", zbean.get("PCLB_CPBH"));
            tmjlbean.set("TMSCJL_CPMC", cpbean.get("PRODUCTDATA_NAME"));
            tmjlbean.set("TMSCJL_YSCSL", 0);
            tmjlbean.set("TMSCJL_GDHM", zbean.get("PCLB_GDHM"));
            tmjlbean.set("TMSCJL_PCSL", zbean.get("PCLB_PCSL"));
            tmjlbean.set("TMSCJL_PCDBH", zbean.get("PCLB_PCDBH"));
            tmjlbean.set("TMSCJL_PCDID", zbean.get("JGMES_PLAN_SCRW_ID"));
            tmjlbean.set("TMSCJL_PCRQ", zbean.get("PCLB_PCRQ"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            String scrq = formatter.format(new Date());

            tmjlbean.set("TMSCJL_SCRQ", scrq);

            tmjlbean.set("TMSCJL_CXMC", zbean.get("PCLB_CXMC"));
            tmjlbean.set("TMSCJL_CXBH", zbean.get("PCLB_CXBM"));
            if (cxbean != null) {
                tmjlbean.set("TMSCJL_CXID", cxbean.get("JGMES_BASE_CXSJ_ID"));
            }
            tmjlbean.set("TMSCJL_MTMSL", tmlxbean.get("CPTMYYGG_MTMSL"));
            tmjlbean.set("TMSCJL_TMLX_ID", tmlxbean.get("CPTMYYGG_TMLX_ID"));
            tmjlbean.set("TMSCJL_TMLX_CODE", tmlxbean.get("CPTMYYGG_TMLX_CODE"));
            tmjlbean.set("TMSCJL_TMLX_NAME", tmlxbean.get("CPTMYYGG_TMLX_NAME"));

            tmjlbean.set("TMSCJL_TMBMMS", tmlxbean.get("CPTMYYGG_TMBMMS"));
            tmjlbean.set("TMSCJL_TMGZBH", tmlxbean.get("CPTMYYGG_TMGZBH"));
            tmjlbean.set("TMSCJL_YYGZBH", tmlxbean.get("CPTMYYGG_YYGZBH"));
            tmjlbean.set("TMSCJL_YYGGMC", tmlxbean.get("CPTMYYGG_YYGGMC"));
            tmjlbean.set("TMSCJL_NZWTMLX_NAME", tmlxbean.get("CPTMYYGG_NZWTMLX_NAME"));
            tmjlbean.set("TMSCJL_NZWTMLX_CODE", tmlxbean.get("CPTMYYGG_NZWTMLX_CODE"));
            tmjlbean.set("TMSCJL_NZWTMLX_ID", tmlxbean.get("CPTMYYGG_NZWTMLX_ID"));
            tmjlbean.set("TMSCJL_BQMB", tmlxbean.get("CPTMYYGG_BQMB"));
            tmjlbean.set("TMSCJL_TMYYGGID", tmlxbean.get("JGMES_BASE_CPTMYYGG_ID"));
            serviceTemplate.insert(tmjlbean);
        }
        return tmscjlId;
    }

    /**
     * ���߸��ƹ���
     */
    public void doCxCopy() {
        String[] ids = request.getParameterValues("ids");
        logger.debug("���ƹ��ܻ�ȡ����ids:" + ids.toString());
        if (ids != null && ids.length > 0) {
            for (int i = 0; i < ids.length; i++) {
                DynaBean cxDynaBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ", " and JGMES_BASE_CXSJ_ID = '" + ids[i] + "'");
                cxDynaBean.set("JGMES_BASE_CXSJ_ID", JEUUID.uuid());
                cxDynaBean.set("CXSJ_CXMC", cxDynaBean.getStr("CXSJ_CXMC") + "-����");
                cxDynaBean.set("CXSJ_CXBM", serviceTemplate.buildCode("CXSJ_CXBM", "JGMES_BASE_CXSJ", cxDynaBean));
                cxDynaBean = serviceTemplate.insert(cxDynaBean);
                //��ȡ��������Ĺ�λ
                List<DynaBean> gwDynaBeanList = serviceTemplate.selectList("JGMES_BASE_GW", " and JGMES_BASE_CXSJ_ID = '" + ids[i] + "'");
                if (gwDynaBeanList != null && gwDynaBeanList.size() > 0) {
                    for (DynaBean gwDynaBean : gwDynaBeanList) {
                        gwDynaBean.set("JGMES_BASE_GW_ID", JEUUID.uuid());
                        gwDynaBean.set("GW_GWBH", serviceTemplate.buildCode("GW_GWBH", "JGMES_BASE_GW", gwDynaBean));
                        gwDynaBean.set("GW_CXBH", cxDynaBean.getStr("CXSJ_CXBM"));
                        gwDynaBean.set("GW_CXMC", cxDynaBean.getStr("CXSJ_CXMC"));
                        gwDynaBean.set("JGMES_BASE_CXSJ_ID", cxDynaBean.getStr("JGMES_BASE_CXSJ_ID"));
                        serviceTemplate.insert(gwDynaBean);
                    }

                }
            }
        }
        toWrite(jsonBuilder.returnSuccessJson("\"���Ƴɹ�\""));
    }

    /**
     * �������ϸ��ƹ���
     */
    public void doCpCopy() {
        String[] ids = request.getParameterValues("ids");
        logger.debug("���ƹ��ܻ�ȡ����ids:" + ids.toString());
        if (ids != null && ids.length > 0) {
            for (int i = 0; i < ids.length; i++) {
                DynaBean cpDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " and JGMES_BASE_PRODUCTDATA_ID = '" + ids[i] + "'");
                if (cpDynaBean == null) {
                    continue;
                }
                String cpbh = cpDynaBean.getStr("PRODUCTDATA_BH");//��Ʒ����
                String cpgylx = cpDynaBean.getStr("PRODUCTDATA_CPGYLXID");//��Ʒ����·��
                cpDynaBean.set("JGMES_BASE_PRODUCTDATA_ID", JEUUID.uuid());
                cpDynaBean.set("PRODUCTDATA_NAME", cpDynaBean.getStr("PRODUCTDATA_NAME") + "-����");
                cpDynaBean.set("PRODUCTDATA_BH", serviceTemplate.buildCode("PRODUCTDATA_BH", "JGMES_BASE_PRODUCTDATA", cpDynaBean));
                cpDynaBean = serviceTemplate.insert(cpDynaBean);
                //��ȡ��Ʒ����Ĳ�Ʒ�ĵ�
                List<DynaBean> wdDynaBeanList = serviceTemplate.selectList("JGMES_BASE_CPWD", " and JGMES_BASE_PRODUCTDATA_ID = '" + ids[i] + "'");
                if (wdDynaBeanList != null && wdDynaBeanList.size() > 0) {
                    for (DynaBean wdDynaBean : wdDynaBeanList) {
                        wdDynaBean.set("JGMES_BASE_CPWD_ID", JEUUID.uuid());
                        wdDynaBean.set("CPWD_BH", cpDynaBean.getStr("PRODUCTDATA_BH"));
                        wdDynaBean.set("JGMES_BASE_PRODUCTDATA_ID", cpDynaBean.getStr("JGMES_BASE_PRODUCTDATA_ID"));
                        serviceTemplate.insert(wdDynaBean);
                    }

                }

                //��ȡ��Ʒ����Ĳ�Ʒ�ĵ�
                List<DynaBean> tmyyggDynaBeanList = serviceTemplate.selectList("JGMES_BASE_CPTMYYGG", " and JGMES_BASE_PRODUCTDATA_ID = '" + ids[i] + "'");
                if (tmyyggDynaBeanList != null && tmyyggDynaBeanList.size() > 0) {
                    for (DynaBean tmyyggDynaBean : tmyyggDynaBeanList) {
                        tmyyggDynaBean.set("JGMES_BASE_CPTMYYGG_ID", JEUUID.uuid());
                        tmyyggDynaBean.set("CPTMYYGG_CPBH", cpDynaBean.getStr("PRODUCTDATA_BH"));
                        tmyyggDynaBean.set("JGMES_BASE_PRODUCTDATA_ID", cpDynaBean.getStr("JGMES_BASE_PRODUCTDATA_ID"));
                        serviceTemplate.insert(tmyyggDynaBean);
                    }

                }


                //��ȡ��Ʒ��λ�����ӦJGMES_BASE_CPGWGX
                List<DynaBean> cpgwgxDynaBeanList = serviceTemplate.selectList("JGMES_BASE_CPGWGX", " and CPGWGX_CPBH = '" + cpbh + "' and CPGWGX_CPGYLXID = '" + cpgylx + "'");
                if (cpgwgxDynaBeanList != null && cpgwgxDynaBeanList.size() > 0) {
                    for (DynaBean cpgwgxDynaBean : cpgwgxDynaBeanList) {
                        cpgwgxDynaBean.set("JGMES_BASE_CPGWGX_ID", JEUUID.uuid());
                        cpgwgxDynaBean.set("CPGWGX_CPBH", cpDynaBean.getStr("PRODUCTDATA_BH"));
                        cpgwgxDynaBean.set("CPGWGX_CPGYLXID", cpDynaBean.getStr("PRODUCTDATA_CPGYLXID"));
                        serviceTemplate.insert(cpgwgxDynaBean);
                    }

                }
            }
        }
        toWrite(jsonBuilder.returnSuccessJson("\"���Ƴɹ�\""));
    }

    public void updateGxTmh() {
        String str = request.getParameter("str");//��ʶ��+�ָ���
        String sql = "select GXGL_GXNUM from JGMES_GYGL_GXGL";
        List<DynaBean> gxbmList = serviceTemplate.selectListBySql(sql);
        //����ִ��
        Session ss = serviceTemplate.getSessionFactory().openSession();
        Transaction tr = ss.beginTransaction();
        tr.begin();
        for (DynaBean dynaBean : gxbmList) {
            String GXGL_GXNUM = dynaBean.getStr("GXGL_GXNUM");
            String s = str + GXGL_GXNUM;
            ss.createSQLQuery("update JGMES_GYGL_GXGL set GXGL_GXTMH=? where GXGL_GXNUM=?").setParameter(0, s).setParameter(1, GXGL_GXNUM).executeUpdate();
        }
        //����ع�������
        try {
            tr.commit();
        } catch (Exception e) {
            tr.rollback();
            e.printStackTrace();
            toWrite(jsonBuilder.returnFailureJson("\"��������Ÿ���ʧ��\""));
        } finally {
            ss.close();
        }
        toWrite(jsonBuilder.returnSuccessJson("\"��������Ÿ��³ɹ���\""));
    }

    //�ڲ������ȡģ��ʹ�ӡ����ַ��ģ��һ����ͨ��
    public void findCreateBarcodeBQCS() {
        String CPTMYYGG_YYGZBH = request.getParameter("CPTMYYGG_YYGZBH");//��Ʒ������
        DynaBean ggBean = serviceTemplate.selectOne("JGMES_BASE_CPTMYYGG", "and CPTMYYGG_YYGZBH = '" + CPTMYYGG_YYGZBH + "'");//��ȡ�����Ի�ȡ��ǩ����ģ��
        String jsonStr = "";
        if (ggBean == null) {
            toWrite("��ȡ��ǩ����ģ��ʧ�ܣ�");
        } else {
            String CLTMB_DYJDZ = "http://192.168.101.105:5868/Print/";//��ӡ����ַ
            //�˿ں�
            String ip = request.getServerName();
            int port = request.getServerPort();
            String CPTMYYGG_BQCS = ggBean.getStr("CPTMYYGG_BQCS");//��ȡ����ģ��
            String CPTMYYGG_BQMB = ggBean.getStr("CPTMYYGG_BQMB");//��ȡ��ǩģ���ļ�
            int index = CPTMYYGG_BQMB.indexOf("*");
            String substring = CPTMYYGG_BQMB.substring(index + 1, CPTMYYGG_BQMB.length());
            jsonStr = CPTMYYGG_BQCS.replace("@filePath@", "http://" + ip + ":" + port + substring + "");
            jsonStr += "%,%" + CLTMB_DYJDZ;
        }
        System.out.println(jsonStr);
        toWrite(jsonStr);
    }

    //���ݱ�ʶ��ȡ�����ʶ��ģ�壬�����������滻
    public void findGeneralBarcodeBQCS() {
        String sign = request.getParameter("sign");//�����ʶ��
        String pk = request.getParameter("pk");//����������
        DynaBean selectOne = serviceTemplate.selectOne("JGMES_SYS_TMBZ",
                " and TMBZ_TMFL_CODE = '" + sign + "'");
        String printerUrl = "";
        String ip = request.getServerName();
        //�˿ں�
        int port = request.getServerPort();
        String jsonStr = "";
        if (selectOne != null) {
            printerUrl = selectOne.getStr("TMBZ_DYJDZ");//��ӡ����ַ
            String TMBZ_BQMBWJ = selectOne.getStr("TMBZ_BQMBWJ");
            int index = TMBZ_BQMBWJ.indexOf("*");
            String TMBZ_BQCSMB = selectOne.getStr("TMBZ_BQCSMB");
            String substring = TMBZ_BQMBWJ.substring(index + 1, TMBZ_BQMBWJ.length());
            jsonStr = TMBZ_BQCSMB.replace("@filePath@", "http://" + ip + ":" + port + substring + "");//���filePath�ֶ�
            List<String> list = new ArrayList<String>();
            String regex = "@[^\\.@]*\\.[^@\\.]*@";// ��ȡ���ָ�ʽ���ַ�����@JGMES_BASE_PRODUCTDATA.PRODUCTDATA_NAME@
            Pattern pattern = Pattern.compile(regex);// ����������ʽ��ȡ�ַ���
            Matcher m = pattern.matcher(jsonStr);
            while (m.find()) {
                int i = 1;
                list.add(m.group(i - 1));
                i++;
            }
            if (!list.isEmpty() || list.size() != 0) {
                for (String match : list) {//ѭ�����е��ֶ�
//					System.out.println(string);//���ӣ�@JGMES_BASE_PRODUCTDATA.PRODUCTDATA_NAME@
                    String s = match.substring(1, match.length() - 1);// JGMES_BASE_PRODUCTDATA.PRODUCTDATA_NAME
                    s = s.replaceAll(" ", "");//ȥ���ַ����еĿո�
                    String[] split = s.split("\\.");// split[0]=JGMES_BASE_PRODUCTDATA��split[1]=PRODUCTDATA_NAME
                    String tabName = split[0];
                    String colName = split[1];
                    DynaBean dataBean = serviceTemplate.selectOneByPk(tabName, pk);
                    if (dataBean != null) {
                        String data = dataBean.getStr(colName);
                        if (StringUtil.isNotEmpty(data)) {
                            jsonStr = jsonStr.replaceAll(match, data);//�滻����
                        } else {
                            jsonStr = jsonStr.replaceAll(match, "");//����Ϊ��ʱ�滻�ɿ�ֵ
                        }
                    } else {
                        toWrite("��ȡ����ʧ�ܣ�");
                        return;
                    }
                }
            }
            System.out.println(jsonStr);
        } else {
            toWrite("��ȡ�����ʶʧ��");
            return;
        }
        jsonStr += "%,%" + printerUrl;
        toWrite(jsonStr);
    }
}
