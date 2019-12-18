
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


    // 工艺路线复制功能
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
            tx.begin();//开启事务
            gylxbean.set("GYLX_GYLXNAME", "复制" + gylxbean.getStr("GYLX_GYLXNAME"));
            //迭代版本号
            int bbh = gylxbean.getInt("GYLX_BBH");
            System.out.println("版本号：" + bbh);
            if (bbh < 1) {
                bbh = 1;
            }
            gylxbean.set("GYLX_BBH", bbh + 1);
            //更改使用状态为启用1,不启用2
            gylxbean.set("GYLX_STATUS", 1);
            //是否有引用初始化 有1 ，否 0
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

            //修改原工艺路线使用状态为2不启用
            String sql = "update JGMES_GYGL_GYLX set GYLX_STATUS='2' where GYLX_ID='" + id + "'";
            pcServiceTemplate.executeSql(sql);
            //System.out.println("回滚测试。");

            tx.commit();//执行
        } catch (Exception e) {
            tx.rollback();//回滚
            logger.error("复制工艺路线失败" + e);
            toWrite(jsonBuilder.returnFailureJson("\"复制失败，请联系网络管理员。\""));
            return;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }


        toWrite(jsonBuilder.returnSuccessJson("\"复制成功\""));
    }

    // 对工艺路线添加明细或修改时，检查是否被引用生产
    public void doChack() {
        String gylxId = request.getParameter("id");
        String sql = " and PCLB_CPBH in (select PRODUCTDATA_BH from JGMES_BASE_PRODUCTDATA where PRODUCTDATA_CPGYLXID='"
                + gylxId + "')";
        long i = serviceTemplate.selectCount("JGMES_PLAN_PCLB", sql);
        System.out.println(i);
        toWrite(jsonBuilder.returnSuccessJson("\"" + i + "\""));
    }

    // 生成条码
    public void doCreateTM() {
        Map<String, Object> map = new HashMap<String, Object>();
        JgmesBarCode jgmesBarCode = new JgmesBarCode(request, serviceTemplate);
        // 基础数据
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
        String tmlx = zbean.get(0).getStr("CPTMYYGG_TMLX_CODE");// 条码类型
        map.put("zbean", zbean);
        // 根据不同的条码类型生成不同的条码
        if (tmlx.equalsIgnoreCase(JgmesEnumsDic.TMCP.getKey())) {
            List<DynaBean> tmlxbean = serviceTemplate.selectList("JGMES_BASE_CPTMYYGG",
                    " and CPTMYYGG_CPBH='" + zbean.get(0).get("PCLB_CPBH") + "' and CPTMYYGG_TMLX_CODE='" + tmlx
                            + "' and CPTMYYGG_YYGZBH='" + zbean.get(0).get("CPTMYYGG_YYGZBH") + "'");
            map.put("tmlxbean", tmlxbean);
            String lsh = "0";
            map.put("lsh", lsh);
            String tmh = "";
            // 根据NEWID查询当前的流水号
            List<DynaBean> lsbean = serviceTemplate.selectList("JGMES_BARCODE_DQJL",
                    " and DQJL_KHCPBH='" + wlkhcpbh + "'  and DQJL_N=" + year + " and DQJL_Y=" + month);
            map.put("lsbean", lsbean);
            DynaBean cpbean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA",
                    " and PRODUCTDATA_BH='" + zbean.get(0).get("PCLB_CPBH") + "'");
            long selectCount = serviceTemplate.selectCount("JGMES_BASE_PRODUCTDATA",
                    " and PRODUCTDATA_KHCPH='" + cpbean.get("PRODUCTDATA_KHCPH") + "' and PRODUCTDATA_WLTYPE_CODE='CP' ");
//			if (cpbean.get("PRODUCTDATA_KHCPH").equals("") || selectCount > 1
//					|| cpbean.get("PRODUCTDATA_KHCPH") == null) {
//				toWrite(jsonBuilder.returnSuccessJson("物料信息客户产品编号重复！"));
//				return;
//			}
            map.put("cpbean", cpbean);
            String gys = "VG";
            String fyear = getYear(year);
            String fmonth = getMonth(month);

            // 1获取流水号
            map = jgmesBarCode.getWaterNumber(map);
            // 2获取产品编号
            map = jgmesBarCode.getProductNumber(map);
            // 3生成or查询条码记录
            map = jgmesBarCode.addOrFindBarCode(map);
            // 条码表主键
            String tmscjlId = (String) map.get("tmscjlId");
            // 客户产品编号
            String cpbh = (String) map.get("khcpbm");
            int newLsh = 0;
            double pcsl = zbean.get(0).getInt("PCSL");
            double yscsl = zbean.get(0).getInt("TMSCJL_YSCSL");
            double mtmsl = zbean.get(0).getInt("CPTMYYGG_MTMSL");
            lsh = (String) map.get("lsh");
            // Object[] rule= {num,gys,cpbh};
            // 批量生成条码
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
            // 根据NEWID查询当前的流水号
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
            // 条码表主键
            String tmscjlId = (String) map.get("tmscjlId");
            int newLsh = 0;
            double pcsl = zbean.get(0).getInt("PCSL");
            double yscsl = zbean.get(0).getInt("TMSCJL_YSCSL");
            double mtmsl = zbean.get(0).getInt("CPTMYYGG_MTMSL");
            lsh = (String) map.get("lsh");
            // 批量生成条码
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


    //通用条码获取模板和打印机地址，模板一样，通用
    public void findUniversallyBarcodeBQCS() {
        String JGMES_SYS_CLTMB_ID = request.getParameter("JGMES_SYS_CLTMB_ID");
        System.out.println(JGMES_SYS_CLTMB_ID);
        DynaBean selectOne = serviceTemplate.selectOne("JGMES_SYS_CLTMB",
                " and JGMES_SYS_CLTMB_ID = '" + JGMES_SYS_CLTMB_ID + "' ");
        String CLTMB_DYJDZ = selectOne.getStr("CLTMB_DYJDZ");//打印机地址
        String ip = request.getServerName();
        //端口号
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

    //工序条码,打印机地址
    public void findGXBarcodeBQCS() {
        DynaBean selectOne = serviceTemplate.selectOne("JGMES_SYS_TMBZ",
                " and TMBZ_TMFL_CODE = 'GXM'");
        String ip = request.getServerName();
        //端口号
        int port = request.getServerPort();
        String jsonStr = "";
        String printerUrl = selectOne.getStr("TMBZ_DYJDZ");//打印机地址
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

    //获取工序码条码
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
                " and TMBZ_TMFL_NAME = '工序码' ");
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
                GXGL_GXTMH = selectOne.getStr("GXGL_GXTMH");//工序条码
                GXGL_GXNUM = selectOne.getStr("GXGL_GXNUM");//工序编号
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
        //端口号
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
        toWrite("成功");
    }

    /*
     * 通用排序
     * 参数1：表名 参数2：排序字段   参数3：过滤条件
     */
    public void todoUpdateList() {
        String tableName = request.getParameter("tableName");//表名
        String id = request.getParameter("pk");//外键
        String idColumn = request.getParameter("pkColumn");//外键列名
        List<String[]> list = new ArrayList<String[]>();
        List<DynaBean> tmlxbean = serviceTemplate.selectList(tableName,
                " and " + idColumn + " ='" + id + "'  order by SY_ORDERINDEX asc");
        if (StringUtil.isNotEmpty(tableName) && StringUtil.isNotEmpty(id) && StringUtil.isNotEmpty(idColumn) && tmlxbean.size() > 0) {
            //事务执行
            Session ss = serviceTemplate.getSessionFactory().openSession();
            Transaction tr = ss.beginTransaction();
            tr.begin();
            for (int i = 0; i < tmlxbean.size(); i++) {
                String idbean = tmlxbean.get(i).getPkValue();
                String pkName = (String) tmlxbean.get(i).get(BeanUtils.KEY_PK_CODE);
                ss.createSQLQuery("update " + tableName + " set SY_ORDERINDEX=? where " + pkName + "=?").setParameter(0, (i + 1)).setParameter(1, idbean).executeUpdate();
            }
            //事务回滚、关流
            try {
                tr.commit();
            } catch (Exception e) {
                tr.rollback();
                e.printStackTrace();
                toWrite(jsonBuilder.returnFailureJson("\"排序更新失败\""));
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

    //返回当前键值
/*	public void BarcodeKey() {
		String item=request.getParameter("item");//产品编号
		String cust=request.getParameter("cust");//客户编码
		String vend=request.getParameter("vend");//客户编码
		String bcrule = request.getParameter("bcrule");//条码生成规则编号

		//生成条码键值
		String barcodekey=getBarcodeKey(cust,vend,item,bcrule);
		if (barcodekey.indexOf("err")==0) {
			toWrite(jsonBuilder.returnFailureJson("错误：" + barcodekey.substring(4,barcodekey.length()-4) + ""));
		} else {
			toWrite(jsonBuilder.returnSuccessJson(barcodekey));
		}
	}*/

    //生成产品条码
    public void doCreatePGTM() {
        Map<String, Object> map = new HashMap<String, Object>();
        //是否强制生成，是→1.否→0
        String qzsc = request.getParameter("QZSC");
        if (StringUtil.isEmpty(qzsc)) {
            qzsc = "0";
        }
        // 前台传入参数
        int startnum = Integer.parseInt(request.getParameter("startnum"));//开始流水号
        int endnum = Integer.parseInt(request.getParameter("endnum"));//结束流水号
        String LOT = request.getParameter("LOT");//批号
        //获取条码待生成视图信息
        String sql = "select * from jgmes_barcode_todolist where NEWID='" + pkValue + "'";
        List<DynaBean> zbean = serviceTemplate.selectListBySql(sql);
        String tmlx = zbean.get(0).getStr("CPTMYYGG_TMLX_CODE");// 条码类型
        String PCLB_CPBH = zbean.get(0).getStr("PCLB_CPBH");//产品编号
        String PCLB_KHBM = zbean.get(0).getStr("PCLB_KHBM");//客户编码
        String CPTMYYGG_TMGZBH = zbean.get(0).getStr("CPTMYYGG_TMGZBH");//条码规则编号
        String CPTMYYGG_YYGZBH = zbean.get(0).getStr("CPTMYYGG_YYGZBH");//应用规则编号
        int PCLB_PCSL = zbean.get(0).getInt("PCLB_PCSL");//排产数量TMSCJL_YSCSL
        int TMSCJL_YSCSL = zbean.get(0).getInt("TMSCJL_YSCSL");//已生成数量
        String lckh = zbean.get(0).getStr("PCLB_LCKH");//流程卡号，内部订单号
        //获取产线数量
        DynaBean cxbean = serviceTemplate.selectOne("JGMES_BASE_CXSJ", " and  CXSJ_CXBM='" + zbean.get(0).getStr("PCLB_CXBM") + "'");
        //获取产品数据
        DynaBean cpbean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " and PRODUCTDATA_BH='" + PCLB_CPBH + "'");
        if (cpbean == null) {
            toWrite(jsonBuilder.returnFailureJson("\"产品获取失败！\""));
            return;
        }
        //获取条码类型
        List<DynaBean> tmlxbean = serviceTemplate.selectList("JGMES_BASE_CPTMYYGG", " and CPTMYYGG_CPBH='" + PCLB_CPBH + "' and CPTMYYGG_TMLX_CODE='" + tmlx + "' and CPTMYYGG_YYGZBH='" + CPTMYYGG_YYGZBH + "'");
        if (tmlxbean == null) {
            toWrite(jsonBuilder.returnFailureJson("\"条码类型获取失败！\""));
            return;
        }
        //生成or查询条码生成记录
        String tmscjlId = addOrFindBarCode(zbean.get(0), tmlxbean.get(0), cpbean, cxbean);

        //返回流水号规则
        DynaBean tmgz = serviceTemplate.selectOne("JGMES_BASE_TTGGZB", "and JGMES_BASE_TMGG_ID=(select JGMES_BASE_TMGG_ID from JGMES_BASE_TMGG where TMGG_BH='" + CPTMYYGG_TMGZBH + "') and SVTYPE_CODE='4'", "TTGG_STR_LENGTH,TTGG_NUM_START,TTGG_STEP_SIZE");
        if (tmgz == null) {
            toWrite(jsonBuilder.returnFailureJson("\"错误：条码规则中没有定义流水号或定义了多个流水号\""));
            return;
        }
        int TTGG_STR_LENGTH = tmgz.getInt("TTGG_STR_LENGTH");//长度
        int TTGG_NUM_START = tmgz.getInt("TTGG_NUM_START");//起始流水号
        int TTGG_STEP_SIZE = tmgz.getInt("TTGG_STEP_SIZE");//流水号步长
        if (TTGG_STR_LENGTH == 0 || TTGG_STEP_SIZE == 0) {
            toWrite(jsonBuilder.returnFailureJson("\"错误：条码规则中没有定义流水号长度或流水号步长\" "));
            return;
        }
        if (startnum < TTGG_NUM_START) {
            startnum = TTGG_NUM_START;
        }
        double ts = (double) (endnum - startnum + 1) / TTGG_STEP_SIZE;
        int nums = (int) Math.ceil(ts);//条数：向上取整
        //System.out.println(ts+"条数:"+nums);
        if (nums == 0) {
            toWrite(jsonBuilder.returnFailureJson("\"错误：生成的流水号已达上限，不能继续生成！\""));
            return;
        }
        //将生成条码数
        int num1 = TMSCJL_YSCSL + nums;//已生成条码数+本次生成条码数
        String barcodekey = "";//键值——若为QZSC（强制生成）则为强制生成。
        if (qzsc.equals("1")) {
            //强制生成不校验能生成数量
            barcodekey = "QZSC";
        } else {
            //按照订单计算应生成条码数
            int num2 = calBarcodeNum(PCLB_CPBH, PCLB_PCSL, CPTMYYGG_YYGZBH);
            //System.out.println("将生成条码数:"+num1+"  应生成条码数:"+num2);
            if (num1 > num2) {
                toWrite(jsonBuilder.returnFailureJson("\"错误：超过规定数量【" + num2 + "】！\""));
                return;
            }
        }
//        //生成条码键值****键值已无作用
//        if (StringUtil.isEmpty(barcodekey)) {
//            barcodekey = getBarcodeKey(PCLB_KHBM, "", PCLB_CPBH, CPTMYYGG_TMGZBH, LOT);
//            if (barcodekey.indexOf("err") == 0) {
//                toWrite(jsonBuilder.returnFailureJson("\"错误：" + barcodekey.substring(4, barcodekey.length() - 4) + "\""));
//                return;
//            }
//        }

        HashMap<String, Object> first = getBarcodeMain(PCLB_KHBM, "", PCLB_CPBH, CPTMYYGG_TMGZBH, startnum, LOT, 1);//第一个条码
//		HashMap<String,Object> end=getBarcodeMain(PCLB_KHBM,"",PCLB_CPBH,CPTMYYGG_TMGZBH,endnum,LOT,1);//最后一个条码
//        DynaBean f = (DynaBean) first.get("wlmaxKey");
        //判断是否有错误
        String firstBarcode = first.get("result").toString();
        if (firstBarcode.indexOf("错误") == 0) {
            toWrite(jsonBuilder.returnFailureJson("\"" + firstBarcode + "\""));
            return;
        }
        //获取前后缀
        String qz = (String) first.get("QZ");
        String hz = (String) first.get("HZ");
        boolean creat = true;
        try {
            String l = (String) first.get("lshgs");
            Integer lshgs = Integer.parseInt(l);
            if (isSussessCreateTM(qz, hz, startnum, endnum, 1, lshgs)) {
                String barcode = "";//条码号
                String strnum = "";
                int inum = 0;
                String createBarcodeQZ = "";//要生成的条码的前缀
                String createBarcodeHZ = "";//要生成的条码的后缀
                HashMap<String, Object> bc = null;//初始化
                String lshgs_frist = "";//流水号格式初始化
                DynaBean wlmaxKey = null;//物料最大流水号表初始化
                List<DynaBean> pgtmList = new ArrayList<DynaBean>();
                //获取最大流水号表对应数据
                Integer maxLsh = 0;
                //插入工单产品条码对应表：JGMES_BASE_GDCPTM，以下为通用数据
                DynaBean beanOne = new DynaBean("JGMES_BASE_GDCPTM", true);
                serviceTemplate.buildModelCreateInfo(beanOne);
                beanOne.set("NEWID", pkValue);//工单条码生成记录_外键ID
                beanOne.set("GDCPTM_PCDBH", zbean.get(0).get("PCLB_PCDBH"));//排产单编号
                beanOne.set("GDCPTM_PCDMC", zbean.get(0).get("PCLB_PCDMC"));//排产单名称
                beanOne.set("GDCPTM_CPBH", zbean.get(0).get("PCLB_CPBH"));//产品编号
                beanOne.set("GDCPTM_NAME", zbean.get(0).get("PCLB_CPNAME"));//产品名称
                beanOne.set("GDCPTM_GDHM", zbean.get(0).get("PCLB_GDHM"));//工单号码
                beanOne.set("GDCPTM_DDHM", zbean.get(0).get("PCLB_DDHM"));//订单号码
                beanOne.set("JGMES_PLAN_SCRW_ID", zbean.get(0).get("JGMES_PLAN_SCRW_ID"));//生产任务_外键ID
                beanOne.set("GDCPTM_SCRWDH", zbean.get(0).get("PCLB_RWDH"));//生产任务单号
                beanOne.set("GDCPTM_PCRQ", zbean.get(0).get("PCLB_PCRQ"));//排产日期
                beanOne.set("GDCPTM_SL", 1);//数量
                beanOne.set("GDCPTM_TMLX_CODE", zbean.get(0).get("CPTMYYGG_TMLX_CODE"));//条码类型
                beanOne.set("GDCPTM_TMLX_NAME", zbean.get(0).get("CPTMYYGG_TMLX_NAME"));//条码类型_NAME
                beanOne.set("GDCPTM_TMLX_ID", zbean.get(0).get("CPTMYYGG_TMLX_ID"));//条码类型_ID
                beanOne.set("JGMES_BARCODE_TMSCJL_ID", tmscjlId);//条码生成记录_外键ID
                beanOne.set("GDCPTM_TMXZ_NAME", zbean.get(0).get("CPTMYYGG_TMXZ_NAME"));//条码性质_NAME
                beanOne.set("GDCPTM_TMXZ_CODE", zbean.get(0).get("CPTMYYGG_TMXZ_CODE"));//条码性质
                beanOne.set("GDCPTM_TMXZ_ID", zbean.get(0).get("CPTMYYGG_TMXZ_ID"));//条码性质_ID
                beanOne.set("GDCPTM_TMLY_CODE", "XTM");//条码来源_CODE
                beanOne.set("GDCPTM_TMLY_NAME", "系统生成码");//条码来源_NAME
                beanOne.set("GDCPTM_SFYDY", 0);//是否已打印
                beanOne.set("GDCPTM_LCKH", lckh);//流程卡号，内部订单号
                for (int i = 0; i < nums; i++) {
                    DynaBean bean = beanOne.clone();
                    //以下为条码生成处理
                    inum = startnum + i * TTGG_STEP_SIZE;
                    //生成条码
                    if (StringUtil.isEmpty(createBarcodeQZ) && StringUtil.isEmpty(createBarcodeHZ)) {//都为空，即第一条生成
                        bc = getBarcodeMain(PCLB_KHBM, "", PCLB_CPBH, CPTMYYGG_TMGZBH, inum, LOT, 1);
                        wlmaxKey = (DynaBean) bc.get("wlmaxKey");
                        if (wlmaxKey == null) {
                            toWrite(jsonBuilder.returnFailureJson("\"错误：获取最大流水号失败\""));
                            return;
                        }
                        barcode = bc.get("result").toString();
                        if (barcode.indexOf("错误") == 0) {
                            toWrite(jsonBuilder.returnFailureJson("\"" + barcode + "\""));
                            return;
                        }
                        createBarcodeQZ = (String) bc.get("QZ");//前缀
                        createBarcodeHZ = (String) bc.get("HZ");//后缀
                        lshgs_frist = (String) bc.get("lshgs");//流水号格式
                        wlmaxKey.setInt("WLMAX_LSHCD", lshgs_frist.length());//流水号长度
//                        //查询要生成的条码是否在成品条码中存在
//                        List<DynaBean> jgmes_base_gdcptm = serviceTemplate.selectList("JGMES_BASE_GDCPTM", "and GDCPTM_TMQZ='" + qz + "' and GDCPTM_TMHZ='" + hz + "' and GDCPTM_TMLSHCD='" + lshgs_frist.length() + "' and GDCPTM_LSH between " + startnum + " and " + endnum);
//                        if (jgmes_base_gdcptm.size() > 0) {
//
//                            return;
//                        }
                        DynaBean jgmes_barcode_wlmax = serviceTemplate.selectOne("JGMES_BARCODE_WLMAX", "and WLMAX_QZ='" + createBarcodeQZ + "'and WLMAX_HZ='" + createBarcodeHZ + "' and WLMAX_LSHCD=" + lshgs_frist.length() + " and WLMAX_FLBH=1");
                        //若旧条码已存在，则回写物料最大流水号表
                        List<DynaBean> cptm = serviceTemplate.selectList("JGMES_BASE_GDCPTM", "and GDCPTM_TMH = '" + barcode + "'");
                        //获取当前物料条码生成记录表中前缀，后缀，流水号长度一致的条码的最大流水号
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
                        //回写
                        if (cptm.size() > 0) {
                            if (jgmes_barcode_wlmax == null) {
                                wlmaxKey.setStr("WLMAX_ZDLSH", thisMaxLsh + "");//最大流水号;
                                serviceTemplate.insert(wlmaxKey);
                            } else {
                                jgmes_barcode_wlmax.setStr("WLMAX_ZDLSH", thisMaxLsh + "");//最大流水号
                                jgmes_barcode_wlmax.setStr("WLMAX_LSHCD", wlmaxKey.getStr("WLMAX_LSHCD"));//最大流水号
                                jgmes_barcode_wlmax.setStr("WLMAX_QZ", createBarcodeQZ);
                                jgmes_barcode_wlmax.setStr("WLMAX_HZ", createBarcodeHZ);
                                jgmes_barcode_wlmax.setInt("WLMAX_LSHCD", lshgs_frist.length());
                                serviceTemplate.update(jgmes_barcode_wlmax);
                            }
                            toWrite(jsonBuilder.returnFailureJson("\"条码：" + barcode + "已存在！已修正最大流水号，请重试！\""));
                            return;
                        }
                    }
                    //处理流水号
                    String codeFormat = "%0" + String.valueOf(lshgs_frist.length() + "d");//格式
                    String lshAll = String.format(codeFormat, inum);//流水号格式
                    bean.set("GDCPTM_LSH", inum);//流水号
                    barcode = createBarcodeQZ + lshAll + createBarcodeHZ;
                    bean.set("GDCPTM_TMH", barcode);//条码号
                    String pk = JEUUID.uuid();
                    bean.set("JGMES_BASE_GDCPTM_ID", pk);//主键id
                    bean.set(BeanUtils.KEY_PK_CODE, pk);
                    wlmaxKey.setStr("WLMAX_ZDLSH", inum + "");//最大流水号
                    bean.setStr("GDCPTM_TMQZ", createBarcodeQZ);//条码前缀
                    bean.setStr("GDCPTM_TMHZ", createBarcodeHZ);//条码后缀
                    bean.setInt("GDCPTM_TMLSHCD", lshgs_frist.length());//条码流水号长度
                    if (StringUtil.isEmpty(barcodekey)){
                        barcodekey=createBarcodeQZ+createBarcodeHZ;
                    }
                    bean.setStr("GDCPTM_JZ",barcodekey);//键值
                    pgtmList.add(bean);
                }

                serviceTemplate.insert(pgtmList);
                DynaBean jgmes_barcode_wlmax = serviceTemplate.selectOne("JGMES_BARCODE_WLMAX", "and WLMAX_QZ='" + createBarcodeQZ + "'and WLMAX_HZ='" + createBarcodeHZ + "' and WLMAX_LSHCD=" + lshgs_frist.length() + " and WLMAX_FLBH = 1");
                if (jgmes_barcode_wlmax == null) {//更新或插入物料最大流水号记录表
                    wlmaxKey.setStr("JGMES_BARCODE_WLMAX_ID", JEUUID.uuid());//物料条码最大流水号表主键
                    wlmaxKey.setInt("WLMAX_FLBH", 1);
                    serviceTemplate.insert(wlmaxKey);
                } else {
                    jgmes_barcode_wlmax.setStr("WLMAX_ZDLSH", wlmaxKey.getStr("WLMAX_ZDLSH"));
                    serviceTemplate.update(jgmes_barcode_wlmax);
                }
                //更新已生成条码数量
                pcServiceTemplate.executeSql(
                        "update JGMES_BARCODE_TMSCJL set TMSCJL_YSCSL=(select count(*) from JGMES_BASE_GDCPTM where NEWID='" + pkValue + "') "
                                + "where JGMES_BARCODE_TMSCJL_ID='" + tmscjlId + "' "
                                + "and TMSCJL_TMLX_CODE='" + tmlx + "'");

                toWrite(jsonBuilder.returnSuccessJson("\"条码生成成功！\""));
            } else {
                toWrite(jsonBuilder.returnFailureJson("\"错误：开始流水号" + startnum + "和结束流水号之间" + endnum + "之间已经生成了条码，不允许重复生成！\""));
            }
            //返回成功标识
            toWrite(jsonBuilder.returnSuccessJson("\"" + strData + "\""));
        } catch (Exception e) {
            e.printStackTrace();
            toWrite(jsonBuilder.returnSuccessJson("\"异常未知错误！\""));
        }
    }

    // 生成物料条码
    public void doCreateWLTM() {
        //Map<String, Object> map = new HashMap<String, Object>();

        // 前台传入参数
        int startnum = Integer.parseInt(request.getParameter("startnum"));//开始流水号
        int endnum = Integer.parseInt(request.getParameter("endnum"));//结束流水号
        String LOT = request.getParameter("LOT");//批号
        int sl = Integer.parseInt(request.getParameter("sl"));//生成条数
        String tmxz = request.getParameter("tmxz");
        //String bcrule = request.getParameter("bcrule");//条码生成规则编号
        //查询物料条码生成资料
        String strSql = "select * from V_JGMES_BASE_VENDORITEM where NEWID='" + pkValue + "'";
        List<DynaBean> items = serviceTemplate.selectListBySql(strSql);
        if (items == null || items.size() == 0) {
            toWrite(jsonBuilder.returnFailureJson("\"错误：未能查找到物料条码生成资料\""));
            return;
        }
        DynaBean venditem = items.get(0);
        String VENDOR_CODE = venditem.getStr("VENDOR_CODE");//供应商编码
        String VENDOR_NAME = venditem.getStr("VENDOR_NAME");//供应商名称
        String VENDOR_SNAME = venditem.getStr("VENDOR_SNAME");//供应商简称
        String VENDOR_SCODE = venditem.getStr("VENDOR_SCODE");//供应商简码
        String VENDORITEM_WLBM = venditem.getStr("VENDORITEM_WLBM");//物料编码
        String VENDORITEM_WLMC = venditem.getStr("VENDORITEM_WLMC");//物料名称
        String CPTMYYGG_TMGZBH = venditem.getStr("CPTMYYGG_TMGZBH");//条码规则编号
        String CPTMYYGG_TMXZ_ID = venditem.getStr("CPTMYYGG_TMXZ_ID");//条码性质_ID
        String CPTMYYGG_TMXZ_CODE = venditem.getStr("CPTMYYGG_TMXZ_CODE");//条码性质
        String CPTMYYGG_TMXZ_NAME = venditem.getStr("CPTMYYGG_TMXZ_NAME");//条码性质_NAME
        //返回流水号规则
        DynaBean bean = serviceTemplate.selectOne("JGMES_BASE_TTGGZB", "and JGMES_BASE_TMGG_ID=(select JGMES_BASE_TMGG_ID from JGMES_BASE_TMGG where TMGG_BH='" + CPTMYYGG_TMGZBH + "') and SVTYPE_CODE='4'", "TTGG_STR_LENGTH,TTGG_NUM_START,TTGG_STEP_SIZE");
        if (bean == null) {
            toWrite(jsonBuilder.returnFailureJson("\"错误：条码规则中没有定义流水号或定义了多个流水号\""));
            return;
        }
        int TTGG_STR_LENGTH = bean.getInt("TTGG_STR_LENGTH");//长度
        int TTGG_NUM_START = bean.getInt("TTGG_NUM_START");//起始流水号
        int TTGG_STEP_SIZE = bean.getInt("TTGG_STEP_SIZE");//流水号步长
        if (TTGG_STR_LENGTH == 0 || TTGG_STEP_SIZE == 0) {
            toWrite(jsonBuilder.returnFailureJson("\"错误：条码规则中没有定义流水号长度或流水号步长\""));
            return;
        }
        if (startnum < TTGG_NUM_START) {
            startnum = TTGG_NUM_START;
        }
        double ts = (double) (endnum - startnum + 1) / TTGG_STEP_SIZE;
        int nums = (int) Math.ceil(ts);//条数：向上取整
        //System.out.println(ts+"条数:"+nums);
        if (tmxz.equals("TMXZX01")) {//批次码只生成一条
            nums = 1;
        }
        if (nums == 0) {
            toWrite(jsonBuilder.returnFailureJson("\"错误：生成的流水号已达上限，不能继续生成！\""));
            return;
        }
        System.out.println(endnum + "----" + startnum + "----" + nums);
//        //生成条码键值
//        String barcodekey = getBarcodeKey("", VENDOR_CODE, VENDORITEM_WLBM, CPTMYYGG_TMGZBH, LOT);
//        if (barcodekey.indexOf("err") == 0) {
//            toWrite(jsonBuilder.returnFailureJson("\"错误：" + barcodekey.substring(4, barcodekey.length() - 4) + "\""));
//            return;
//        }
        HashMap<String, Object> first = getBarcodeMain("", VENDOR_CODE, VENDORITEM_WLBM, CPTMYYGG_TMGZBH, startnum, LOT, 0);//第一个条码
//        DynaBean f = (DynaBean) first.get("wlmaxKey");
        //判断是否有错误
        String firstBarcode = first.get("result").toString();
        if (firstBarcode.indexOf("错误") == 0) {
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
                String createBarcodeQZ = "";//要生成的条码的前缀
                String createBarcodeHZ = "";//要生成的条码的后缀
                HashMap<String, Object> bc = null;//初始化
                String lshgs_frist = "";//流水号格式初始化
                DynaBean wlmaxKey = null;//物料最大流水号表初始化
                List<DynaBean> wltmList = new ArrayList<DynaBean>();
                //获取最大流水号表对应数据
                Integer maxLsh = 0;
                //插入物料条码记录表：JGMES_BASE_WLTM
                DynaBean vibarOne = new DynaBean("JGMES_BASE_WLTM", true);
                serviceTemplate.buildModelCreateInfo(vibarOne);
                vibarOne.set("NEWID", pkValue);
                vibarOne.set("VENDOR_CODE", VENDOR_CODE);//供应商编码
                vibarOne.set("VENDOR_NAME", VENDOR_NAME);//供应商名称
                vibarOne.set("VENDOR_SNAME", VENDOR_SNAME);//供应商简称
                vibarOne.set("VENDOR_SCODE", VENDOR_SCODE);//供应商简码
                vibarOne.set("WLTM_WLBM", VENDORITEM_WLBM);//物料编码
                vibarOne.set("WLTM_WLMC", VENDORITEM_WLMC);//物料名称
                vibarOne.set("CPTMYYGG_TMGZBH", CPTMYYGG_TMGZBH);//条码规则编号
                vibarOne.set("WLTM_TMXZ_ID", CPTMYYGG_TMXZ_ID);//条码性质_ID
                vibarOne.set("WLTM_TMXZ_CODE", CPTMYYGG_TMXZ_CODE);//条码性质
                vibarOne.set("WLTM_TMXZ_NAME", CPTMYYGG_TMXZ_NAME);//条码性质_NAME
                vibarOne.set("WLTM_TMLY_CODE", "XTM");//条码来源_CODE
                vibarOne.set("WLTM_TMLY_NAME", "系统生成码");//条码来源_NAME
                vibarOne.set("WLTM_SFYDY", 0);//是否已打印

                for (int i = 0; i < nums; i++) {
                    DynaBean vibar = vibarOne.clone();
                    inum = startnum + i * TTGG_STEP_SIZE;
                    //strnum=getCurNo(TTGG_NUM_START+i*TTGG_STEP_SIZE,TTGG_STR_LENGTH,"0");
                    //barcode=barcodemain+strnum;
                    //生成条码
                    if (StringUtil.isEmpty(createBarcodeQZ) && StringUtil.isEmpty(createBarcodeHZ)) {//都为空，即第一条生成
                        bc = getBarcodeMain("", VENDOR_CODE, VENDORITEM_WLBM, CPTMYYGG_TMGZBH, inum, LOT, 0);
                        wlmaxKey = (DynaBean) bc.get("wlmaxKey");
                        if (wlmaxKey == null) {
                            toWrite(jsonBuilder.returnFailureJson("\"错误：获取最大流水号失败\""));
                            return;
                        }
                        barcode = bc.get("result").toString();
                        if (barcode.indexOf("错误") == 0) {
                            toWrite(jsonBuilder.returnFailureJson("\"错误：" + barcode + "\""));
                            return;
                        }
                        createBarcodeQZ = (String) bc.get("QZ");//前缀
                        createBarcodeHZ = (String) bc.get("HZ");//后缀
                        lshgs_frist = (String) bc.get("lshgs");//流水号格式
                        wlmaxKey.setInt("WLMAX_LSHCD", lshgs_frist.length());//流水号长度
                        DynaBean jgmes_barcode_wlmax = serviceTemplate.selectOne("JGMES_BARCODE_WLMAX", "and WLMAX_QZ='" + createBarcodeQZ + "'and WLMAX_HZ='" + createBarcodeHZ + "' and WLMAX_LSHCD=" + lshgs_frist.length() + " and WLMAX_FLBH=0");
                        List<DynaBean> wltm = serviceTemplate.selectList("JGMES_BASE_WLTM", "and WLTM_TMH = '" + barcode + "'");
                        if (wltm.size() > 0) {
                            if (jgmes_barcode_wlmax == null) {
                                serviceTemplate.insert(wlmaxKey);
                            } else {
                                jgmes_barcode_wlmax.setStr("WLMAX_ZDLSH", inum + "");//最大流水号
                                jgmes_barcode_wlmax.setStr("WLMAX_LSHCD", wlmaxKey.getStr("WLMAX_LSHCD"));//最大流水号
                                jgmes_barcode_wlmax.setStr("WLMAX_QZ", createBarcodeQZ);
                                jgmes_barcode_wlmax.setStr("WLMAX_HZ", createBarcodeHZ);
                                jgmes_barcode_wlmax.setInt("WLMAX_LSHCD", lshgs_frist.length());
                                serviceTemplate.update(jgmes_barcode_wlmax);
                            }
                            toWrite(jsonBuilder.returnFailureJson("\"条码：" + barcode + "已存在！\""));
                            return;
                        }
                    }
                    //处理条码号
                    String codeFormat = "%0" + String.valueOf(lshgs_frist.length() + "d");//格式
                    String lshAll = String.format(codeFormat, inum);//流水号格式
                    vibar.set("WLTM_LSH", inum);//流水号
                    barcode = createBarcodeQZ + lshAll + createBarcodeHZ;
                    vibar.set("WLTM_TMH", barcode);//条码号
                    String pk = JEUUID.uuid();
                    vibar.set("JGMES_BASE_WLTM_ID", pk);//主键id
                    vibar.set(BeanUtils.KEY_PK_CODE, pk);
                    wlmaxKey.setStr("WLMAX_ZDLSH", inum + "");//最大流水号
                    vibar.setStr("WLTM_TMQZ", createBarcodeQZ);//条码前缀
                    vibar.setStr("WLTM_TMHZ", createBarcodeHZ);//条码后缀
                    vibar.setInt("WLTM_TMLSHCD", lshgs_frist.length());//条码流水号长度
                    vibar.set("WLTM_JZ", createBarcodeQZ+createBarcodeHZ);//键值
                    if (tmxz.equals("TMXZX01")) {
                        vibar.set("WLTM_SL", sl);//数量
                    } else {
                        vibar.set("WLTM_SL", 1);//数量
                    }
                    wltmList.add(vibar);
                }
                serviceTemplate.insert(wltmList);
                DynaBean jgmes_barcode_wlmax = serviceTemplate.selectOne("JGMES_BARCODE_WLMAX", "and WLMAX_QZ='" + createBarcodeQZ + "'and WLMAX_HZ='" + createBarcodeHZ + "' and WLMAX_LSHCD=" + lshgs_frist.length() + " and WLMAX_FLBH=0");
                if (jgmes_barcode_wlmax == null) {//更新或插入物料最大流水号记录表
                    wlmaxKey.setStr("JGMES_BARCODE_WLMAX_ID", JEUUID.uuid());//物料条码最大流水号表主键
                    wlmaxKey.setInt("WLMAX_FLBH", 0);
                    serviceTemplate.insert(wlmaxKey);
                } else {
                    jgmes_barcode_wlmax.setStr("WLMAX_ZDLSH", wlmaxKey.getStr("WLMAX_ZDLSH"));
                    serviceTemplate.update(jgmes_barcode_wlmax);
                }
                toWrite(jsonBuilder.returnSuccessJson("\"物料条码生成成功！\""));
            } else {
                toWrite(jsonBuilder.returnFailureJson("\"错误：流水号" + startnum + "已经生成了条码，不允许重复生成！\""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            toWrite(jsonBuilder.returnSuccessJson("\"异常未知错误！\""));
        }
    }

    /**
     * 删除成品条码
     */
    public void delTM() {
        String cpid = request.getParameter("cpid").replace("[", "").replace("]", "").replace("\"", "");//产品id
        String TMSCJL_YSCSL = request.getParameter("TMSCJL_YSCSL");//已生成条数
        String tmid = request.getParameter("tmid").replace("[", "").replace("]", "").replace("\"", "");
        //条码id
        String cpidList[] = cpid.split(",");
        List<String> cList = Arrays.asList(cpidList);
        String tmidList[] = tmid.split(",");
        List<String> tList = Arrays.asList(tmidList);
        DynaBean tm = new DynaBean();//条码主表
        tm.setStr(beanUtils.KEY_TABLE_CODE, "JGMES_BARCODE_TMSCJL");
        int num = 0;
        try {
            num = Integer.parseInt(TMSCJL_YSCSL);
            num = num - cList.size();
        } catch (Exception e) {
            toWrite(jsonBuilder.returnFailureJson("\"错误：已生成数量转换失败\""));
            return;
        }
        DynaBean jgmes_base_gdcptm = serviceTemplate.selectOne("JGMES_BASE_GDCPTM", "and JGMES_BASE_GDCPTM_ID='" + cList.get(0) + "'");
        String qz = jgmes_base_gdcptm.getStr("GDCPTM_TMQZ");//前缀
        String hz = jgmes_base_gdcptm.getStr("GDCPTM_TMHZ");//后缀
        String lshcd = jgmes_base_gdcptm.getStr("GDCPTM_TMLSHCD");//流水号长度
        Integer lsh = jgmes_base_gdcptm.getInt("GDCPTM_TMLSHCD");//流水号
        Integer maxLsh = 0;
        List<String> notLsh = new ArrayList<String>();
        //事务执行
        Session ss = serviceTemplate.getSessionFactory().openSession();
        Transaction tr = ss.beginTransaction();
        tr.begin();
        for (int i = 0; i < cList.size(); i++) {
            //删除条码
            ss.createSQLQuery("delete from JGMES_BASE_GDCPTM where JGMES_BASE_GDCPTM_ID=?").setParameter(0, cList.get(i)).executeUpdate();
            //更新最大流水号记录表，处理旧数据
            if (StringUtil.isEmpty(qz) && StringUtil.isEmpty(hz) && StringUtil.isEmpty(lshcd)) {
                ss.createSQLQuery("delete from JGMES_BARCODE_WLMAX where JGMES_BARCODE_WLMAX_ID=?").setParameter(0, cList.get(i)).executeUpdate();
            }
            //获取当前删除的条码流水号
            DynaBean jgmes_base_gdcptm1 = serviceTemplate.selectOne("JGMES_BASE_GDCPTM", "and JGMES_BASE_GDCPTM_ID='" + cList.get(i) + "'");
            if (jgmes_base_gdcptm1 != null) {
                notLsh.add(jgmes_base_gdcptm1.getStr("GDCPTM_LSH"));
            }
        }
        //更新物料最大流水号记录表
        if (StringUtil.isNotEmpty(lshcd)) {
            //获取除了当前最大流水号之外的最大流水号
//            string s = string.Join(",", list.ToArray());
            String str = String.join(",", notLsh);
            String sql = "select max(GDCPTM_LSH) GDCPTM_LSH from JGMES_BASE_GDCPTM where GDCPTM_TMQZ = '" + qz + "' and GDCPTM_TMHZ = '" + hz + "' and GDCPTM_TMLSHCD='" + lshcd + "'  and GDCPTM_LSH not in (" + str + ")";
            System.out.println(sql);
            List<DynaBean> dynaBeans = serviceTemplate.selectListBySql(sql);
            if (dynaBeans.size() > 0) {
                maxLsh = dynaBeans.get(0).getInt("GDCPTM_LSH");//获取最大流水号
            }
            ss.createSQLQuery("update JGMES_BARCODE_WLMAX set WLMAX_ZDLSH = ? where WLMAX_QZ=? and WLMAX_HZ=? and WLMAX_LSHCD=? and WLMAX_FLBH=1").setParameter(0, maxLsh).setParameter(1, qz).setParameter(2, hz)
                    .setParameter(3, lshcd).executeUpdate();
        } else {
            //处理旧数据

        }
        //更新已生成数量
        int query = ss.createSQLQuery("update JGMES_BARCODE_TMSCJL set TMSCJL_YSCSL=? where JGMES_BARCODE_TMSCJL_ID=?").setParameter(0, num + "").setParameter(1, tList.get(0)).executeUpdate();
        System.out.println(query);
        //事务回滚、关流
        try {
            tr.commit();
        } catch (Exception e) {
            tr.rollback();
            e.printStackTrace();
        } finally {
            ss.close();
        }
        toWrite(jsonBuilder.returnSuccessJson("\"删除成功！\""));
    }

    /**
     * 删除物料条码
     */
    public void delWLBarcode() {
        String wlId = request.getParameter("wlId").replace("[", "").replace("]", "").replace("\"", "");//要删除的物料条码id
        String[] split = wlId.split(",");
        Integer maxLsh = 0;
        //事务执行
        Session ss = serviceTemplate.getSessionFactory().openSession();
        Transaction tr = ss.beginTransaction();
        tr.begin();
        for (String id : split) {
            ss.createSQLQuery("delete from JGMES_BASE_WLTM where JGMES_BASE_WLTM_ID=?").setParameter(0, id).executeUpdate();//删除物料条码
            //获取当前物料条码对应的信息
            DynaBean jgmes_base_wltm = serviceTemplate.selectOne("JGMES_BASE_WLTM", "and JGMES_BASE_WLTM_ID='" + id + "'");
            if (jgmes_base_wltm != null) {
                String qz = jgmes_base_wltm.getStr("WLTM_TMQZ");//前缀
                String hz = jgmes_base_wltm.getStr("WLTM_TMHZ");//后缀
                String lshcd = jgmes_base_wltm.getStr("WLTM_TMLSHCD");//条码流水号长度
                String lsh = jgmes_base_wltm.getStr("WLTM_LSH");//流水号
                //获取除了当前条码流水号之外的最大流水号
                String sql = "select max(WLTM_LSH) WLTM_LSH from JGMES_BASE_WLTM where WLTM_TMQZ='" + qz + "' and WLTM_TMHZ ='" + hz + "' and WLTM_TMLSHCD=" + lshcd + " and WLTM_LSH not in (" + lsh + ")";
                List<DynaBean> dynaBeans = serviceTemplate.selectListBySql(sql);
                if (dynaBeans.size() > 0) {
                    maxLsh = dynaBeans.get(0).getInt("WLTM_LSH");//获取最大流水号
                    ss.createSQLQuery("update JGMES_BARCODE_WLMAX set WLMAX_ZDLSH = ? where WLMAX_QZ=? and WLMAX_HZ=? and WLMAX_LSHCD=? and WLMAX_FLBH=0").setParameter(0, maxLsh).setParameter(1, qz).setParameter(2, hz)
                            .setParameter(3, lshcd).executeUpdate();
                }
            }
        }
        //事务回滚、关流
        try {
            tr.commit();
        } catch (Exception e) {
            tr.rollback();
            e.printStackTrace();
        } finally {
            ss.close();
        }
        toWrite(jsonBuilder.returnSuccessJson("\"删除成功！\""));


    }


    /**
     * 删除外部码
     */
    public void delOutTM() {
        String cpid = request.getParameter("cpid").replace("[", "").replace("]", "").replace("\"", "");//条码主键id
        String scrwid = request.getParameter("scrwid").replace("[", "").replace("]", "").replace("\"", "");
        ;//生产任务id
        String tmid = request.getParameter("tmid").replace("[", "").replace("]", "").replace("\"", "");
        ;//条码id
        String cpidList[] = cpid.split(",");
        List<String> cList = Arrays.asList(cpidList);
        String scrwidList[] = scrwid.split(",");
        List<String> sList = Arrays.asList(scrwid);
        String tmidList[] = tmid.split(",");
        List<String> tList = Arrays.asList(tmidList);
        List<Integer> yscslList = new ArrayList();
        DynaBean tm = new DynaBean();//条码主表
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
        //事务执行
        Session ss = serviceTemplate.getSessionFactory().openSession();
        Transaction tr = ss.beginTransaction();
        tr.begin();
        Iterator<String> keys = map.keySet().iterator();
        //获取同一个生产任务单号的条码数量
//        List<Integer> num = new ArrayList();
        while (keys.hasNext()) {
            String key = keys.next();
            Integer n = map.get(key).intValue();
            int i = 0;
            int yscsl = yscslList.get(i);
            int lastNum = yscsl - n;//最后值
//            num.add(map.get(key).intValue());
            int query = ss.createSQLQuery("update JGMES_BARCODE_TMSCJL set TMSCJL_YSCSL=? where JGMES_BARCODE_TMSCJL_ID=?").setParameter(0, lastNum + "").setParameter(1, key).executeUpdate();
        }

        for (int i = 0; i < cList.size(); i++) {
            System.out.println(cList.get(i));
            ss.createSQLQuery("delete from JGMES_BASE_GDCPTM where JGMES_BASE_GDCPTM_ID=?").setParameter(0, cList.get(i)).executeUpdate();
        }
        //事务回滚、关流
        try {
            tr.commit();
        } catch (Exception e) {
            tr.rollback();
            e.printStackTrace();
        } finally {
            ss.close();
        }
        toWrite(jsonBuilder.returnSuccessJson("\"删除成功！\""));
    }

    /**
     * 获取能最大生成的条码的数量
     */
    public void getAllBarcodeNum() {
        String PCLB_CPBH = request.getParameter("cpbh");//产品编号
        String PCLB_PCSLStr = request.getParameter("pcsl");//排产数量
        String CPTMYYGG_YYGZBH = request.getParameter("yygzbh");//应用规则编号
        String yscslStr = request.getParameter("yscsl");//已生成数量
        Integer num = 0;
        try {
            Integer PCLB_PCSL = Integer.parseInt(PCLB_PCSLStr);//转换数值类型
            Integer yscsl = Integer.parseInt(yscslStr);
            int canCreateNum = calBarcodeNum(PCLB_CPBH, PCLB_PCSL, CPTMYYGG_YYGZBH);//可以生成的数量
            if (canCreateNum - yscsl > 0) {
                num = canCreateNum - yscsl;
                toWrite(jsonBuilder.returnSuccessJson("\"" + num + "\""));
            } else {
                toWrite(jsonBuilder.returnFailureJson("\"已超出能生成的最大数量,若要继续生成请使用强制生成按钮！\""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            toWrite(jsonBuilder.returnFailureJson("\"系统错误！\""));
            return;
        }
    }

    /**
     * 检查条数是否可以生成
     * 0 代表已经生成完毕，不能再继续生成
     * 大于0表示还可以生成多少条码
     * -1代表可以正常生成条码
     */
    public void checkCreateNum() {
        String PCLB_CPBH = request.getParameter("cpbh");//产品编号
        String PCLB_PCSLStr = request.getParameter("pcsl");//排产数量
        String CPTMYYGG_YYGZBH = request.getParameter("yygzbh");//应用规则编号
        String createNumStr = request.getParameter("createNum");//需要生成的数量
        String yscslStr = request.getParameter("yscsl");//已生成数量
        Integer num = 0;
        try {
            Integer PCLB_PCSL = Integer.parseInt(PCLB_PCSLStr);//转换数值类型
            Integer createNum = Integer.parseInt(createNumStr);
            Integer yscsl = Integer.parseInt(yscslStr);
            int canCreateNum = calBarcodeNum(PCLB_CPBH, PCLB_PCSL, CPTMYYGG_YYGZBH);//可以生成的数量
            if (canCreateNum == yscsl) {
                num = 0;
            } else if (createNum > canCreateNum) {
                num = canCreateNum - yscsl;
            } else {
                num = -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            toWrite(jsonBuilder.returnFailureJson("\"系统错误！\""));
            return;
        }
        toWrite(jsonBuilder.returnSuccessJson("\"" + num + "\""));
    }

    /**
     * 判断流水号是否有效检测类
     */
    public void netIsSussessCreateTM() {
        String qz = request.getParameter("QZ");//前缀
        String hz = request.getParameter("HZ");//后缀
        String s = request.getParameter("startnum");//开始流水号
        String e = request.getParameter("endnum");//结束流水号
        String f = request.getParameter("FL");//分类
        String l = request.getParameter("lshgs");//流水号长度
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
            toWrite(jsonBuilder.returnFailureJson("\"异常错误！\""));
            exception.printStackTrace();
        }
        boolean sussessCreateTM = isSussessCreateTM(qz, hz, startnum, endnum, fl, lshcd);
        if (sussessCreateTM) {
            toWrite(jsonBuilder.returnSuccessJson("\"" + startnum + "\""));
        } else {
            toWrite(jsonBuilder.returnFailureJson("\"要生成的条码中含有已生成条码，不能生成！\""));
        }
    }

    /**
     * 检查流水号是否有效
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
     * 获取最大流水号
     */
    public void getKeyValues() {
        // 前台传入参数,ljs
        String LOT = request.getParameter("LOT");//批号
        String cust = request.getParameter("cust");//客户编号
        String VENDOR = request.getParameter("VENDOR");//供应商编号
        String WLBH = request.getParameter("WLBH");//物料（产品）编码
        String BCRULE = request.getParameter("BCRULE");//条码生成规则代码
        String fls = request.getParameter("FL");//分类
        String lshcd = request.getParameter("lshgs");//流水号长度
        int fl = 0;
        try {
            fl = Integer.parseInt(fls);
        } catch (Exception e) {
            e.printStackTrace();
            toWrite(jsonBuilder.returnFailureJson("获取物料分类失败！"));
            return;
        }
        HashMap<String, Object> code = getBarcodeMain(cust, VENDOR, WLBH, BCRULE, 1, LOT, fl);//第一个条码
        DynaBean f = (DynaBean) code.get("wlmaxKey");
        //判断是否有错误
        String barcode = code.get("result").toString();
        if (barcode.indexOf("错误") == 0) {
            toWrite(jsonBuilder.returnFailureJson("\"" + barcode + "\""));
            return;
        }
        if (barcode.length() > 100) {
            toWrite(jsonBuilder.returnFailureJson("\"错误：生成的条码长度过长，请适当减少批号长度或者条码规则表中的选项\""));
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

    //根据条码规则生成条码
    //cValue:查询条件值--客户编码
    //vValue:查询条件值--供应商编码
    //iValue:查询条件值--物料编码
    //bcrule:条码生成规则代码
    //num：流水号
    private HashMap<String, Object> getBarcodeMain(String cValue, String vValue, String iValue, String bcrule, int num, String LOT, int flbh) {
        String result = "";
        String errMsg = "";
        String strSql = "select * from JGMES_BASE_TTGGZB where JGMES_BASE_TMGG_ID=(select JGMES_BASE_TMGG_ID from JGMES_BASE_TMGG where TMGG_BH='" + bcrule + "') order by SY_ORDERINDEX";

        DynaBean wlmaxKey = new DynaBean();//条码最大流水号表
        wlmaxKey.setStr(beanUtils.KEY_TABLE_CODE, "JGMES_BARCODE_WLMAX");
        wlmaxKey.setInt("WLMAX_FLBH", flbh);//物料或者成品
        String qz = "";//前缀
        String hz = "";//后缀
        String qLsh = "";//前缀+流水号
        String lshgs = "";//流水号格式
        HashMap<String, Object> BarcodeMain = new HashMap();

        List<DynaBean> beans = serviceTemplate.selectListBySql(strSql);
        if (beans != null) {
            String SVTYPE_CODE = "";//取值方式：1--常量	2--全局变量	3--表单字段	4--流水号
            String TTGG_CVALUE = "";//常量对应值
            String GVAR_CODE = "";//全局变量：UserName--登录用户	UserCode--登录用户编码	UserID--登录用户ID	CurrentDate--当前日期
            String GVAR_NAME = "";
            String SVTABLE_CODE = "";//来源表单：JGMES_BASE_CUST--客户		JGMES_BASE_VENDOR--供应商		JGMES_BASE_PRODUCTDATA--物料
            String SVTABLE_NAME = "";
            String FFIELD_CODE = "";//条件字段
            String FVALUE_CODE = "";//取值字段
            String TTGG_SOURCE = "";//替换源
            String TTGG_TARGET = "";//替换目标
            String TTGG_FORMAT = "";//格式[日期用]
            int TTGG_STR_START = 0;//开始截取
            int TTGG_STR_LENGTH = 0;//长度
            //int TTGG_NUM_START=0;//起始流水号
            int TTGG_STEP_SIZE = 0;//流水号步长

            String curCode = "";

            //字典bean
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

                //用字典替换，返回字典bean
                if (TTGG_SOURCE.equals("DIC")) {
                    if (StringUtils.isEmpty(TTGG_TARGET)) {
                        errMsg += "错误：用字典替换时没有输入字典代码!";
                    } else {
                        strSql = "select DICTIONARYITEM_ITEMNAME,DICTIONARYITEM_ITEMCODE from JE_CORE_DICTIONARYITEM a "
                                + "left join JE_CORE_DICTIONARY b on a.DICTIONARYITEM_DICTIONARY_ID=b.JE_CORE_DICTIONARY_ID "
                                + "where 1=1 and SY_PARENT !='' and DICTIONARY_DDCODE='" + TTGG_TARGET + "'";
                        dics = serviceTemplate.selectListBySql(strSql);
                    }
                }

                if (SVTYPE_CODE.equals("1")) {
                    //常量
                    if (TTGG_CVALUE.equals("LOT")) {
                        curCode = LOT;
                    } else {
                        curCode = TTGG_CVALUE;
                    }
                } else if (SVTYPE_CODE.equals("2")) {
                    //全局变量
                    JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
                    if (GVAR_NAME.equals("登录用户")) {
//						curCode=SecurityUserHolder.getCurrentUser().getUsername();
                        curCode = jgmesCommon.jgmesUser.getCurrentUserName();
                    } else if (GVAR_NAME.equals("登录用户编码")) {
//						curCode=SecurityUserHolder.getCurrentUser().getUserCode();
                        curCode = jgmesCommon.jgmesUser.getCurrentUserCode();
                    } else if (GVAR_NAME.equals("登录用户ID")) {
//						curCode=SecurityUserHolder.getCurrentUser().getUserId();
                        jgmesCommon.jgmesUser.getCurrentUserID();
                    } else if (GVAR_NAME.equals("当前日期")) {
                        //处理日期格式
                        if (StringUtil.isNotEmpty(TTGG_FORMAT)) {
                            SimpleDateFormat sDateFormat = new SimpleDateFormat(TTGG_FORMAT);
                            curCode = sDateFormat.format(new java.util.Date());
                        }
                    }
                } else if (SVTYPE_CODE.equals("3")) {
                    //表单字段
                    String FFIELD_VALUE = "";
                    if (SVTABLE_NAME.equals("客户")) {
                        FFIELD_VALUE = cValue;
                    } else if (SVTABLE_NAME.equals("供应商")) {
                        FFIELD_VALUE = vValue;
                    } else if (SVTABLE_NAME.equals("物料")) {
                        FFIELD_VALUE = iValue;
                    }
                    if (FFIELD_VALUE.isEmpty() || FFIELD_VALUE.equals("")) {
                        if (SVTABLE_NAME.equals("客户")) {
                            errMsg += "错误：" + SVTABLE_NAME + "信息为空,请检查生产任务表中的客户信息。";
                            errMsg += "生成条码如果是物料条码，请检查条码规则表，删除客户表单字段！";
                        }
                        if (SVTABLE_NAME.equals("供应商")) {
                            errMsg += "错误：" + SVTABLE_NAME + "信息为空,请检查表单信息。";
                            errMsg += SVTABLE_NAME + "生成条码如果是成品条码，请检查条码规则表，删除供应商表单字段！";
                        }
                        BarcodeMain.put("result", errMsg);
                        return BarcodeMain;
                    }
                    DynaBean fbean = serviceTemplate.selectOne(SVTABLE_CODE, "and " + FFIELD_CODE + "='" + FFIELD_VALUE + "'", FVALUE_CODE);
                    if (fbean != null) {
                        curCode = fbean.getStr(FVALUE_CODE);
                    } else {
//						errMsg+="查询表单"+SVTABLE_CODE+"的字段值"+FVALUE_CODE+"时出错!";
                        errMsg += "错误：生成条码异常！";
                    }
                } else if (SVTYPE_CODE.equals("4")) {
                    //流水号
                    //int num=startNum+(iNum)*TTGG_STEP_SIZE;
                    curCode = getCurNo(num, TTGG_STR_LENGTH, "0");//流水号
                    //System.out.println(num+"/"+curCode);
//                    wlmaxKey.setStr("WLMAX_ZDLSH", num + "");//物料最大表最大流水号
                }

                //替换
                if (TTGG_SOURCE.equals("DIC")) {
                    //System.out.println("OK");
                    //用字典替换
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

                //截取
                curCode = getStr(curCode, TTGG_STR_START, TTGG_STR_LENGTH);
                if (curCode.indexOf("错误") == 0) {  //SVTABLE_NAME
                    errMsg += curCode;
                }
                if (SVTYPE_CODE.equals("4")) {
                    qz = result;
                    wlmaxKey.setStr("WLMAX_QZ", qz);
                    lshgs = curCode;
                    qLsh = qz + curCode;
                }
                //组合
                result += curCode;
            }
        }

        if (errMsg.isEmpty()) {
            hz = result.replace(qLsh, "");//获取后缀
            wlmaxKey.setStr("WLMAX_HZ", hz);//后缀
            BarcodeMain.put("wlmaxKey", wlmaxKey);//最大流水号
            BarcodeMain.put("result", result);//条码号
            BarcodeMain.put("QZ", qz);//前缀
            BarcodeMain.put("HZ", hz);//后缀
            BarcodeMain.put("lshgs", lshgs);//第一个条码的流水号+格式
//			serviceTemplate.insert(wlmaxKey);//插入记录表 ljs
            return BarcodeMain;
        } else {
            BarcodeMain.put("result", errMsg);
            return BarcodeMain;
        }
    }


//    //根据条码规则生成条码关键值(不包含流水号)
//    //cValue:查询条件值--客户编码
//    //vValue:查询条件值--供应商编码
//    //iValue:查询条件值--物料编码
//    //bcrule:条码生成规则代码
//    private String getBarcodeKey(String cValue, String vValue, String iValue, String bcrule, String LOT) {
//        String result = "";
//        String errMsg = "";
//        //获取条码应用规则
//        String strSql = "select * from JGMES_BASE_TTGGZB where JGMES_BASE_TMGG_ID=(select JGMES_BASE_TMGG_ID from JGMES_BASE_TMGG where TMGG_BH='" + bcrule + "') and SVTYPE_CODE<>'4' and TTGGZB_XHSZ=1 order by SY_ORDERINDEX";
//        List<DynaBean> beans = serviceTemplate.selectListBySql(strSql);
//        if (beans != null) {
//            String SVTYPE_CODE = "";//取值方式：1--常量	2--全局变量	3--表单字段	4--流水号
//            String TTGG_CVALUE = "";//常量对应值
//            String GVAR_CODE = "";//全局变量：UserName--登录用户	UserCode--登录用户编码	UserID--登录用户ID	CurrentDate--当前日期
//            String GVAR_NAME = "";
//            String SVTABLE_CODE = "";//来源表单：JGMES_BASE_CUST--客户		JGMES_BASE_VENDOR--供应商		JGMES_BASE_PRODUCTDATA--物料
//            String SVTABLE_NAME = "";
//            String FFIELD_CODE = "";//条件字段
//            String FVALUE_CODE = "";//取值字段
//            String TTGG_SOURCE = "";//替换源
//            String TTGG_TARGET = "";//替换目标
//            String TTGG_FORMAT = "";//格式[日期用]
//            int TTGG_STR_START = 0;//开始截取
//            int TTGG_STR_LENGTH = 0;//长度
//            //int TTGG_NUM_START=0;//起始流水号
//            //int TTGG_STEP_SIZE=0;//流水号步长
//            String curCode = "";
//
//            //字典bean
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
//                //用字典替换，返回字典bean
//                if (TTGG_SOURCE.equals("DIC")) {
//                    if (StringUtils.isEmpty(TTGG_TARGET)) {
//                        errMsg += "错误：用字典替换时没有输入字典代码!";
//                    } else {
//                        strSql = "select DICTIONARYITEM_ITEMNAME,DICTIONARYITEM_ITEMCODE from JE_CORE_DICTIONARYITEM a "
//                                + "left join JE_CORE_DICTIONARY b on a.DICTIONARYITEM_DICTIONARY_ID=b.JE_CORE_DICTIONARY_ID "
//                                + "where 1=1 and SY_PARENT !='' and DICTIONARY_DDCODE='" + TTGG_TARGET + "'";
//                        dics = serviceTemplate.selectListBySql(strSql);
//                    }
//                }
//
//                if (SVTYPE_CODE.equals("1")) {
//                    //常量
//                    if (TTGG_CVALUE.equals("LOT")) {
//                        curCode = LOT;
//                    } else {
//                        curCode = TTGG_CVALUE;
//                    }
//                } else if (SVTYPE_CODE.equals("2")) {
//                    //全局变量
//                    JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
//                    if (GVAR_NAME.equals("登录用户")) {
////						curCode=SecurityUserHolder.getCurrentUser().getUsername();
//                        curCode = jgmesCommon.jgmesUser.getCurrentUserName();
//                    } else if (GVAR_NAME.equals("登录用户编码")) {
////						curCode=SecurityUserHolder.getCurrentUser().getUserCode();
//                        curCode = jgmesCommon.jgmesUser.getCurrentUserCode();
//                    } else if (GVAR_NAME.equals("登录用户ID")) {
////						curCode=SecurityUserHolder.getCurrentUser().getUserId();
//                        jgmesCommon.jgmesUser.getCurrentUserID();
//                    } else if (GVAR_NAME.equals("当前日期")) {
//                        //处理日期格式
//                        if (StringUtil.isNotEmpty(TTGG_FORMAT)) {
//                            SimpleDateFormat sDateFormat = new SimpleDateFormat(TTGG_FORMAT);
//                            curCode = sDateFormat.format(new java.util.Date());
//                        }
//                    }
//                } else if (SVTYPE_CODE.equals("3")) {
//                    //表单字段
//                    String FFIELD_VALUE = "";
//                    if (SVTABLE_NAME.equals("客户")) {
//                        FFIELD_VALUE = cValue;
//                    } else if (SVTABLE_NAME.equals("供应商")) {
//                        FFIELD_VALUE = vValue;
//                    } else if (SVTABLE_NAME.equals("物料")) {
//                        FFIELD_VALUE = iValue;
//                    }
//                    if (FFIELD_VALUE.isEmpty() || FFIELD_VALUE.equals("")) {
//                        if (SVTABLE_NAME.equals("客户")) {
//                            errMsg += "错误：" + SVTABLE_NAME + "信息为空,请检查生产任务表中的客户信息。";
//                            errMsg += "生成条码如果是物料条码，请检查条码规则表，删除客户表单字段！";
//                        }
//                        if (SVTABLE_NAME.equals("供应商")) {
//                            errMsg += "错误：" + SVTABLE_NAME + "信息为空,请检查表单信息。";
//                            errMsg += SVTABLE_NAME + "生成条码如果是成品条码，请检查条码规则表，删除供应商表单字段！";
//                        }
//                        return errMsg;
//                    }
//                    DynaBean fbean = serviceTemplate.selectOne(SVTABLE_CODE, "and " + FFIELD_CODE + "='" + FFIELD_VALUE + "'", FVALUE_CODE);
//                    if (fbean != null) {
//                        curCode = fbean.getStr(FVALUE_CODE);
//                    } else {
////						errMsg+="查询表单"+SVTABLE_CODE+"的字段值"+FVALUE_CODE+"时出错!";
//                        errMsg += "该条码规则不能添加" + SVTABLE_NAME + "信息或者" + SVTABLE_NAME + "信息为空，请检查条码规则表中对应的规则";
//                        errMsg += "生成条码异常！";
//                    }
//                }
//                //替换
//                if (TTGG_SOURCE.equals("DIC")) {
//                    //用字典替换
//                    if (dics != null) {
//                        for (DynaBean dic : dics) {
//                            DICTIONARYITEM_ITEMNAME = dic.getStr("DICTIONARYITEM_ITEMNAME");//字典项名称
//                            DICTIONARYITEM_ITEMCODE = dic.getStr("DICTIONARYITEM_ITEMCODE");//字典项值
//                            if (DICTIONARYITEM_ITEMNAME.equals(curCode)) {
//                                curCode = curCode.replaceAll(DICTIONARYITEM_ITEMNAME, DICTIONARYITEM_ITEMCODE);
//                            }
//                        }
//                    }
//                } else {
//                    curCode = curCode.replaceAll(TTGG_SOURCE, TTGG_TARGET);
//                }
//
//                //截取
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

    //截取字符串
    private String getStr(String fSource, int sNum, int sLen) {
        String result = "";
        if (StringUtil.isNotEmpty(fSource)) {
            if (fSource.length() <= sNum) {
                result = "错误:开始截取的长度大于或者等于对应字段的长！请检查物料条码规则！";
                return result;
            }
            if (fSource.length() < sLen) {
                result = "错误:截取的长度大于对应字段的长！请检查物料条码规则！";
                return result;
            }
            int length = sNum + sLen;
            if (length > fSource.length()) {
                result = "错误:可以截取的长度超出字段的长度！请检查物料条码规则！";
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
     * curno表示需要被填充的字符
     * length表示要填充的长度
     * fillStr表示需要填充的字符
     * 根据要求来填充字符串
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

    //按照订单计算应生成条码数
    public void getBcNum() {
        String Item = request.getParameter("Item");
        int OrderQty = Integer.parseInt(request.getParameter("OrderQty"));
        String yyrule = request.getParameter("yyrule");

        int num = calBarcodeNum(Item, OrderQty, yyrule);
        toWrite(jsonBuilder.returnSuccessJson(String.valueOf(num)));
    }

    //计算订单应生成条码数量
    //Item:物料编码
    //OrderQty：订单数量
    //yyrule：应用规则编码
    public int calBarcodeNum(String Item, int OrderQty, String yyrule) {
        int num = 0;
        //查询物料条码应用规则
        String strSql = "select CPTMYYGG_YYGZBH,CPTMYYGG_TMLX_CODE,CPTMYYGG_NZWTMLX_CODE,CPTMYYGG_MTMSL from JGMES_BASE_CPTMYYGG where CPTMYYGG_CPBH='" + Item + "'";
        List<DynaBean> items = serviceTemplate.selectListBySql(strSql);
        if (items == null || items.size() == 0) {
            toWrite(jsonBuilder.returnFailureJson("错误：未能查找到物料" + Item + "的条码应用规则"));
            return num;
        }
        String NZWTMLX_CODE = "", YYGZBH = "";
        int MTMSL = 0, nMTMSL = 0;
        for (DynaBean item : items) {
            YYGZBH = item.getStr("CPTMYYGG_YYGZBH");//应用规则编号
            NZWTMLX_CODE = item.getStr("CPTMYYGG_NZWTMLX_CODE");//内装物条码类型
            if (yyrule.equals(YYGZBH)) {
                //System.out.println("1:"+TMLX_CODE+"/"+YYGZBH);
                MTMSL = item.getInt("CPTMYYGG_MTMSL");//本级每条码数量

                nMTMSL = getBarcodeNum(items, NZWTMLX_CODE);//循环调用计算下级应生成条码数量

                num = nMTMSL * MTMSL;
            }
        }
        //System.out.println("num:"+num);
        double ts = (double) OrderQty / num;
        int nums = (int) Math.ceil(ts);//条数：向上取整

        return nums;
    }

    //循环调用计算下级应生成条码数量
    public int getBarcodeNum(List<DynaBean> items, String pCODE) {
        int num = 1;

        String TMLX_CODE = "", NZWTMLX_CODE = "";
        int MTMSL = 0, nMTMSL = 0;
        for (DynaBean item : items) {
            TMLX_CODE = item.getStr("CPTMYYGG_TMLX_CODE");//条码类型
            NZWTMLX_CODE = item.getStr("CPTMYYGG_NZWTMLX_CODE");//内装物条码类型
            //System.out.println("2:"+NZWTMLX_CODE+"/"+TMLX_CODE);
            if (TMLX_CODE.equals(pCODE)) {
                MTMSL = item.getInt("CPTMYYGG_MTMSL");//本级每条码数量

                nMTMSL = getBarcodeNum(items, NZWTMLX_CODE);//下级每条码数量

                num = nMTMSL * MTMSL;
            }
        }

        return num;
    }

    /*
     * 功能：生成or查询条码记录
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
     * 产线复制功能
     */
    public void doCxCopy() {
        String[] ids = request.getParameterValues("ids");
        logger.debug("复制功能获取到的ids:" + ids.toString());
        if (ids != null && ids.length > 0) {
            for (int i = 0; i < ids.length; i++) {
                DynaBean cxDynaBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ", " and JGMES_BASE_CXSJ_ID = '" + ids[i] + "'");
                cxDynaBean.set("JGMES_BASE_CXSJ_ID", JEUUID.uuid());
                cxDynaBean.set("CXSJ_CXMC", cxDynaBean.getStr("CXSJ_CXMC") + "-复制");
                cxDynaBean.set("CXSJ_CXBM", serviceTemplate.buildCode("CXSJ_CXBM", "JGMES_BASE_CXSJ", cxDynaBean));
                cxDynaBean = serviceTemplate.insert(cxDynaBean);
                //获取产线下面的工位
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
        toWrite(jsonBuilder.returnSuccessJson("\"复制成功\""));
    }

    /**
     * 物料资料复制功能
     */
    public void doCpCopy() {
        String[] ids = request.getParameterValues("ids");
        logger.debug("复制功能获取到的ids:" + ids.toString());
        if (ids != null && ids.length > 0) {
            for (int i = 0; i < ids.length; i++) {
                DynaBean cpDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " and JGMES_BASE_PRODUCTDATA_ID = '" + ids[i] + "'");
                if (cpDynaBean == null) {
                    continue;
                }
                String cpbh = cpDynaBean.getStr("PRODUCTDATA_BH");//产品编码
                String cpgylx = cpDynaBean.getStr("PRODUCTDATA_CPGYLXID");//产品工艺路线
                cpDynaBean.set("JGMES_BASE_PRODUCTDATA_ID", JEUUID.uuid());
                cpDynaBean.set("PRODUCTDATA_NAME", cpDynaBean.getStr("PRODUCTDATA_NAME") + "-复制");
                cpDynaBean.set("PRODUCTDATA_BH", serviceTemplate.buildCode("PRODUCTDATA_BH", "JGMES_BASE_PRODUCTDATA", cpDynaBean));
                cpDynaBean = serviceTemplate.insert(cpDynaBean);
                //获取产品下面的产品文档
                List<DynaBean> wdDynaBeanList = serviceTemplate.selectList("JGMES_BASE_CPWD", " and JGMES_BASE_PRODUCTDATA_ID = '" + ids[i] + "'");
                if (wdDynaBeanList != null && wdDynaBeanList.size() > 0) {
                    for (DynaBean wdDynaBean : wdDynaBeanList) {
                        wdDynaBean.set("JGMES_BASE_CPWD_ID", JEUUID.uuid());
                        wdDynaBean.set("CPWD_BH", cpDynaBean.getStr("PRODUCTDATA_BH"));
                        wdDynaBean.set("JGMES_BASE_PRODUCTDATA_ID", cpDynaBean.getStr("JGMES_BASE_PRODUCTDATA_ID"));
                        serviceTemplate.insert(wdDynaBean);
                    }

                }

                //获取产品下面的产品文档
                List<DynaBean> tmyyggDynaBeanList = serviceTemplate.selectList("JGMES_BASE_CPTMYYGG", " and JGMES_BASE_PRODUCTDATA_ID = '" + ids[i] + "'");
                if (tmyyggDynaBeanList != null && tmyyggDynaBeanList.size() > 0) {
                    for (DynaBean tmyyggDynaBean : tmyyggDynaBeanList) {
                        tmyyggDynaBean.set("JGMES_BASE_CPTMYYGG_ID", JEUUID.uuid());
                        tmyyggDynaBean.set("CPTMYYGG_CPBH", cpDynaBean.getStr("PRODUCTDATA_BH"));
                        tmyyggDynaBean.set("JGMES_BASE_PRODUCTDATA_ID", cpDynaBean.getStr("JGMES_BASE_PRODUCTDATA_ID"));
                        serviceTemplate.insert(tmyyggDynaBean);
                    }

                }


                //获取产品工位工序对应JGMES_BASE_CPGWGX
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
        toWrite(jsonBuilder.returnSuccessJson("\"复制成功\""));
    }

    public void updateGxTmh() {
        String str = request.getParameter("str");//标识码+分隔符
        String sql = "select GXGL_GXNUM from JGMES_GYGL_GXGL";
        List<DynaBean> gxbmList = serviceTemplate.selectListBySql(sql);
        //事务执行
        Session ss = serviceTemplate.getSessionFactory().openSession();
        Transaction tr = ss.beginTransaction();
        tr.begin();
        for (DynaBean dynaBean : gxbmList) {
            String GXGL_GXNUM = dynaBean.getStr("GXGL_GXNUM");
            String s = str + GXGL_GXNUM;
            ss.createSQLQuery("update JGMES_GYGL_GXGL set GXGL_GXTMH=? where GXGL_GXNUM=?").setParameter(0, s).setParameter(1, GXGL_GXNUM).executeUpdate();
        }
        //事务回滚、关流
        try {
            tr.commit();
        } catch (Exception e) {
            tr.rollback();
            e.printStackTrace();
            toWrite(jsonBuilder.returnFailureJson("\"工序条码号更新失败\""));
        } finally {
            ss.close();
        }
        toWrite(jsonBuilder.returnSuccessJson("\"工序条码号更新成功！\""));
    }

    //内部条码获取模板和打印机地址，模板一样，通用
    public void findCreateBarcodeBQCS() {
        String CPTMYYGG_YYGZBH = request.getParameter("CPTMYYGG_YYGZBH");//产品规则编号
        DynaBean ggBean = serviceTemplate.selectOne("JGMES_BASE_CPTMYYGG", "and CPTMYYGG_YYGZBH = '" + CPTMYYGG_YYGZBH + "'");//读取规则以获取标签参数模板
        String jsonStr = "";
        if (ggBean == null) {
            toWrite("获取标签参数模板失败！");
        } else {
            String CLTMB_DYJDZ = "http://192.168.101.105:5868/Print/";//打印机地址
            //端口号
            String ip = request.getServerName();
            int port = request.getServerPort();
            String CPTMYYGG_BQCS = ggBean.getStr("CPTMYYGG_BQCS");//获取参数模板
            String CPTMYYGG_BQMB = ggBean.getStr("CPTMYYGG_BQMB");//获取标签模板文件
            int index = CPTMYYGG_BQMB.indexOf("*");
            String substring = CPTMYYGG_BQMB.substring(index + 1, CPTMYYGG_BQMB.length());
            jsonStr = CPTMYYGG_BQCS.replace("@filePath@", "http://" + ip + ":" + port + substring + "");
            jsonStr += "%,%" + CLTMB_DYJDZ;
        }
        System.out.println(jsonStr);
        toWrite(jsonStr);
    }

    //根据标识获取条码标识的模板，并进行数据替换
    public void findGeneralBarcodeBQCS() {
        String sign = request.getParameter("sign");//条码标识符
        String pk = request.getParameter("pk");//表单数据主键
        DynaBean selectOne = serviceTemplate.selectOne("JGMES_SYS_TMBZ",
                " and TMBZ_TMFL_CODE = '" + sign + "'");
        String printerUrl = "";
        String ip = request.getServerName();
        //端口号
        int port = request.getServerPort();
        String jsonStr = "";
        if (selectOne != null) {
            printerUrl = selectOne.getStr("TMBZ_DYJDZ");//打印机地址
            String TMBZ_BQMBWJ = selectOne.getStr("TMBZ_BQMBWJ");
            int index = TMBZ_BQMBWJ.indexOf("*");
            String TMBZ_BQCSMB = selectOne.getStr("TMBZ_BQCSMB");
            String substring = TMBZ_BQMBWJ.substring(index + 1, TMBZ_BQMBWJ.length());
            jsonStr = TMBZ_BQCSMB.replace("@filePath@", "http://" + ip + ":" + port + substring + "");//填充filePath字段
            List<String> list = new ArrayList<String>();
            String regex = "@[^\\.@]*\\.[^@\\.]*@";// 获取这种格式的字符串：@JGMES_BASE_PRODUCTDATA.PRODUCTDATA_NAME@
            Pattern pattern = Pattern.compile(regex);// 根据正则表达式截取字符串
            Matcher m = pattern.matcher(jsonStr);
            while (m.find()) {
                int i = 1;
                list.add(m.group(i - 1));
                i++;
            }
            if (!list.isEmpty() || list.size() != 0) {
                for (String match : list) {//循环所有的字段
//					System.out.println(string);//例子：@JGMES_BASE_PRODUCTDATA.PRODUCTDATA_NAME@
                    String s = match.substring(1, match.length() - 1);// JGMES_BASE_PRODUCTDATA.PRODUCTDATA_NAME
                    s = s.replaceAll(" ", "");//去除字符串中的空格
                    String[] split = s.split("\\.");// split[0]=JGMES_BASE_PRODUCTDATA，split[1]=PRODUCTDATA_NAME
                    String tabName = split[0];
                    String colName = split[1];
                    DynaBean dataBean = serviceTemplate.selectOneByPk(tabName, pk);
                    if (dataBean != null) {
                        String data = dataBean.getStr(colName);
                        if (StringUtil.isNotEmpty(data)) {
                            jsonStr = jsonStr.replaceAll(match, data);//替换数据
                        } else {
                            jsonStr = jsonStr.replaceAll(match, "");//数据为空时替换成空值
                        }
                    } else {
                        toWrite("获取数据失败！");
                        return;
                    }
                }
            }
            System.out.println(jsonStr);
        } else {
            toWrite("获取条码标识失败");
            return;
        }
        jsonStr += "%,%" + printerUrl;
        toWrite(jsonStr);
    }
}
