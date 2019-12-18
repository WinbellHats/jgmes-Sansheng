package com.jgmes.service;

import com.alibaba.druid.pool.DruidDataSource;
import com.je.core.facade.extjs.JsonBuilder;
import com.je.core.security.SecurityUserHolder;
import com.je.core.service.PCDynaServiceTemplate;
import com.je.core.service.PCServiceTemplate;
import com.je.core.util.SpringContextHolder;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.BeanUtils;
import com.je.core.util.bean.DynaBean;
import com.je.rbac.model.Department;
import com.je.rbac.model.EndUser;
import com.je.rbac.service.UserManager;
import com.jgmes.util.JgmesCommon;
import com.jgmes.util.JgmesResult;
import com.jgmes.util.RegularExpressionCheck;
import net.sf.json.JSONObject;
import org.apache.tools.ant.taskdefs.LoadProperties;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.je.core.action.DynaAction.beanUtils;

/**
 * ERP�ӿ�
 *
 * @author admin
 * @version 2019-07-29 16:01:38
 */
@Component("eRPService")
public class ERPServiceImpl implements ERPService {

    /**
     * ��̬Bean(DynaBean)�ķ����
     */
    private PCDynaServiceTemplate serviceTemplate;
    /**
     * ʵ��Bean���������,��Ҫ����SQL
     */
    private PCServiceTemplate pcServiceTemplate;
    /**
     * �û������
     */
    private UserManager userManager;
    RegularExpressionCheck pec = new RegularExpressionCheck();

    public void load() {
        System.out.println("hello serviceimpl");
    }

    JsonBuilder jsonBuilder = JsonBuilder.getInstance();

    /**
     * 通过物料编号查询数据库中对应的物料资料
     *
     * @param MaterialCode 物料编号
     * @param MaterialName 物料名称
     * @return
     */
    @Override
    public String GetMaterialData(String MaterialCode, String MaterialName) {

        if (serviceTemplate == null) {
            serviceTemplate = SpringContextHolder.getBean("PCDynaServiceTemplate");
        }
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        try {
            String whereSql = "";
            //物料编号，支持，号多查询
            if (StringUtil.isNotEmpty(MaterialCode)) {
                if (MaterialCode.indexOf(",") != -1) {
                    whereSql += " and PRODUCTDATA_BH in ('" + MaterialCode.replaceAll(",", "','") + "')";
                } else {
                    whereSql += " and PRODUCTDATA_BH like '%" + MaterialCode + "%'";
                }
            }
            //物料名称，支持，号多查询
            if (StringUtil.isNotEmpty(MaterialName)) {
                if (MaterialName.indexOf(",") != -1) {
                    whereSql += " and PRODUCTDATA_NAME in ('" + MaterialName.replaceAll(",", "','") + "')";
                } else {
                    whereSql += " and PRODUCTDATA_NAME like '%" + MaterialName + "%'";
                }
            }
            List<DynaBean> jgmes_base_productdata = serviceTemplate.selectList("JGMES_BASE_PRODUCTDATA", whereSql);
            ret.Data = ret.getValues(jgmes_base_productdata);
        } catch (Exception e) {
            e.printStackTrace();
            ret.setMessage("异常错误");
        }
        return jsonBuilder.toJson(ret);
    }

    /**
     * 传入多条数据的Json字符串值修改或新增物料资料
     *
     * @param jsonList
     * @return
     */
    public String UpdateOrAddMaterialData(String jsonList) {
        if (serviceTemplate == null) {
            serviceTemplate = SpringContextHolder.getBean("PCDynaServiceTemplate");
        }
        Boolean haveError = false;
        StringBuffer errorMessage = new StringBuffer(2000);//错误信息记录
        HttpServletRequest request = null;
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        if (StringUtil.isNotEmpty(jsonList)) {
            List<DynaBean> list = new Vector<DynaBean>();
            //转换成List数组
            try {
                String key = "";
                String value = "";
                net.sf.json.JSONArray ja1 = net.sf.json.JSONArray.fromObject(jsonList);
                JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, "");
                if (ja1.size() > 0) {
                    for (int i = 0; i < ja1.size(); i++) {
                        DynaBean bean = new DynaBean();
                        bean.setStr(beanUtils.KEY_TABLE_CODE, "JGMES_BASE_PRODUCTDATA");
                        JSONObject jb1 = ja1.getJSONObject(i);  // 遍历 jsonarray 数组，把每一个对象转成 json 对象
                        Iterator it = jb1.keys();
                        while (it.hasNext()) {
                            key = (String) it.next();
                            value = jb1.getString(key);
                            bean.setStr(key, value);
                        }
                        list.add(bean);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage.append("数据转换失败！");
                haveError = true;
            }
            if (!haveError) {
                //开启线程
                ExecutorService pool = Executors.newFixedThreadPool(10);
                CountDownLatch latch = new CountDownLatch(1);
                List<Integer> res = new Vector<>();
                try {
                    pool.submit(new Runnable() {
                        @Override
                        public void run() {
                            //开启JDBC批数据处理
                            Integer countErrorIndex = 1;
                            Integer trueImport = 0;
                            Integer trueUpdate = 0;
                            Integer falseImport = 0;
                            Connection conn = null;
                            PreparedStatement insertSt = null;
                            PreparedStatement updateSt = null;
                            ResultSet rs = null;
                            String updateSql = "update JGMES_BASE_PRODUCTDATA set PRODUCTDATA_NAME=?,PRODUCTDATA_STATUS_CODE=?,PRODUCTDATA_WLTYPE_CODE=?,PRODUCTDATA_WLTYPE_NAME=?,PRODUCTDATA_CPFL_NAME=?,PRODUCTDATA_JLDW_NAME=?," +
                                    "PRODUCTDATA_GG=?,PRODUCTDATA_MS=?,PRODUCTDATA_TMYZGZ=?,PRODUCTDATA_JYJB_CODE=?,PRODUCTDATA_JYJB_NAME=?,PRODUCTDATA_JYLX_CODE=?,PRODUCTDATA_JYLX_NAME=?,PRODUCTDATA_BZ=?,PRODUCTDATA_ZJM=?," +
                                    "PRODUCTDATA_TMH=?,PRODUCTDATA_KHCPH=?,PRODUCTDATA_CPGYLX=?,PRODUCTDATA_CPGYLXID=?,PRODUCTDATA_TMXZ_CODE=?,PRODUCTDATA_TMXZ_NAME=?,PRODUCTDATA_XH=? where PRODUCTDATA_BH=?";

                            String insertSql = "insert into JGMES_BASE_PRODUCTDATA (PRODUCTDATA_NAME,PRODUCTDATA_STATUS_CODE,PRODUCTDATA_WLTYPE_CODE,PRODUCTDATA_WLTYPE_NAME,PRODUCTDATA_CPFL_NAME,PRODUCTDATA_JLDW_NAME,PRODUCTDATA_GG," +
                                    "PRODUCTDATA_MS,PRODUCTDATA_TMYZGZ,PRODUCTDATA_JYJB_CODE,PRODUCTDATA_JYJB_NAME,PRODUCTDATA_JYLX_CODE,PRODUCTDATA_JYLX_NAME,PRODUCTDATA_BZ,PRODUCTDATA_ZJM,PRODUCTDATA_TMH,PRODUCTDATA_KHCPH," +
                                    "PRODUCTDATA_CPGYLX,PRODUCTDATA_CPGYLXID,PRODUCTDATA_TMXZ_CODE,PRODUCTDATA_TMXZ_NAME,PRODUCTDATA_XH,PRODUCTDATA_BH,JGMES_BASE_PRODUCTDATA_ID)" +
                                    "Values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                            try {
                                Properties prop = new Properties();
                                InputStream inputStream = LoadProperties.class.getClassLoader().getResourceAsStream("jdbc.properties");
                                prop.load(inputStream);
                                String driverClass = prop.getProperty("jdbc.Driver");
                                String url = prop.getProperty("jdbc.url");
                                String username = prop.getProperty("jdbc.username");
                                String password = prop.getProperty("jdbc.password");
                                conn = DriverManager.getConnection(url, username, password);
                                conn.setAutoCommit(false);
                                updateSt = conn.prepareStatement(updateSql);
                                insertSt = conn.prepareStatement(insertSql);
                                Integer i = 1;
                                for (DynaBean dynaBean : list) {
                                    //数据处理
                                    DynaBean clone = dynaBean.clone();
                                    DynaBean newData = dynaBean.clone();
                                    Hashtable hashMap = checkValuesLen(clone, "PRODUCTDATA_BH");
                                    Boolean IsUpdate = false;
                                    DynaBean oldDataBean = new DynaBean();
                                    if ((Boolean) hashMap.get("IsSussess")) {
                                        //数据库校验长度成功，特别字段校验
                                        Boolean haveErrors = false;
                                        //物料编号，不能为空
                                        String wlBh = newData.getStr("PRODUCTDATA_BH");
                                        if (StringUtil.isEmpty(wlBh)) {
                                            errorMessage.append(countErrorIndex++ + "：第" + i + "条数据导入失败，原因：物料编号为空！");
                                            haveErrors = true;
                                        } else {
                                            oldDataBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", "and PRODUCTDATA_BH='" + wlBh + "'");
                                            if (oldDataBean != null) {
                                                newData.set("JGMES_BASE_PRODUCTDATA_ID", oldDataBean.getStr("JGMES_BASE_PRODUCTDATA_ID"));
                                                IsUpdate = true;
                                            }
                                        }
                                        //物料名称，若不允许重复则进行校验
                                        String wlmc = newData.getStr("PRODUCTDATA_NAME");
                                        if (StringUtil.isEmpty(wlmc)){
                                            errorMessage.append(countErrorIndex++ + "：物料编号为" + wlBh + "的物料名称不能为空 !");
                                            haveErrors = true;
                                        }else{
                                            String IsMaterialNameStr = newData.getStr("IsMaterialName");
                                            Integer IsMaterialName = StringUtil.isEmpty(IsMaterialNameStr)||!IsMaterialNameStr.equals("0") ? 1 : 0;
                                            if (IsMaterialName == 0) {
                                                List<DynaBean> jgmes_base_productdata = serviceTemplate.selectList("JGMES_BASE_PRODUCTDATA", "and PRODUCTDATA_NAME='" + wlmc + "' and PRODUCTDATA_BH!='" + wlBh + "'");
                                                if (jgmes_base_productdata.size() > 0) {
                                                    errorMessage.append(countErrorIndex++ + "：物料编号为" + wlBh + "的物料名称" + wlmc + "已存在！导入失败!");
                                                    haveErrors = true;
                                                }
                                            }
                                        }
                                        // 物料属性,MaterialClassify
                                        String MaterialClassify = newData.getStr("PRODUCTDATA_WLTYPE_CODE");
                                        if (StringUtil.isNotEmpty(MaterialClassify)) {
                                            switch (MaterialClassify.toLowerCase().replaceAll(" ", "")) {
                                                case "cp":
                                                    newData.set("PRODUCTDATA_WLTYPE_CODE", "CP");
                                                    newData.set("PRODUCTDATA_WLTYPE_NAME", "成品");
                                                    break;
                                                case "bcp":
                                                    newData.set("PRODUCTDATA_WLTYPE_CODE", "BCP");
                                                    newData.set("PRODUCTDATA_WLTYPE_NAME", "半成品");
                                                    break;
                                                case "WL":
                                                    newData.set("PRODUCTDATA_WLTYPE_CODE", "WL");
                                                    newData.set("PRODUCTDATA_WLTYPE_NAME", "材料");
                                                    break;
                                                default:
                                                    errorMessage.append(countErrorIndex++ + "：物料编号为" + wlBh + "的物料属性只能为CP(成品)，WL（材料）和BCP（半成品）！");
                                                    haveErrors = true;
                                                    break;
                                            }
                                        } else {
                                            errorMessage.append(countErrorIndex++ + "：物料编号为" + wlBh + "的物料属性不能为空！");
                                            haveErrors = true;
                                        }
                                        //检验级别，InspectionLevel
                                        String InspectionLevel = newData.getStr("PRODUCTDATA_JYJB_CODE");
                                        if (StringUtil.isNotEmpty(InspectionLevel)) {
                                            switch (InspectionLevel.toLowerCase().replaceAll(" ", "")) {
                                                case "a":
                                                    newData.set("PRODUCTDATA_JYJB_CODE", "A");
                                                    newData.set("PRODUCTDATA_JYJB_NAME", "A类");
                                                    break;
                                                case "b":
                                                    newData.set("PRODUCTDATA_JYJB_CODE", "B");
                                                    newData.set("PRODUCTDATA_JYJB_NAME", "B类");
                                                    break;
                                                case "c":
                                                    newData.set("PRODUCTDATA_JYJB_CODE", "C");
                                                    newData.set("PRODUCTDATA_JYJB_NAME", "C类");
                                                    break;
                                                case "d":
                                                    newData.set("PRODUCTDATA_JYJB_CODE", "D");
                                                    newData.set("PRODUCTDATA_JYJB_NAME", "D类");
                                                    break;
                                                default:
                                                    errorMessage.append(countErrorIndex++ + "：物料编号为" + wlBh + "的检验级别只能为A,B,C,D ！");
                                                    haveErrors = true;
                                                    break;
                                            }
                                        }
                                        //检验类型，InspectionType
                                        String InspectionType = newData.getStr("PRODUCTDATA_JYLX_NAME");
                                        if (StringUtil.isNotEmpty(InspectionType)) {
                                            switch (InspectionType.replaceAll(" ", "")) {
                                                case "免检":
                                                    newData.set("PRODUCTDATA_JYLX_CODE", "MJ");
                                                    newData.set("PRODUCTDATA_JYLX_NAME", "免检");
                                                    break;
                                                case "抽检":
                                                    newData.set("PRODUCTDATA_JYLX_CODE", "CJ");
                                                    newData.set("PRODUCTDATA_JYLX_NAME", "抽检");
                                                    break;
                                                case "全检":
                                                    newData.set("PRODUCTDATA_JYLX_CODE", "QJ");
                                                    newData.set("PRODUCTDATA_JYLX_NAME", "全检");
                                                    break;
                                                case "首检":
                                                    newData.set("PRODUCTDATA_JYLX_CODE", "SJ");
                                                    newData.set("PRODUCTDATA_JYLX_NAME", "首检");
                                                    break;
                                                case "巡检":
                                                    newData.set("PRODUCTDATA_JYLX_CODE", "XJ");
                                                    newData.set("PRODUCTDATA_JYLX_NAME", "巡检");
                                                    break;
                                                default:
                                                    errorMessage.append(countErrorIndex++ + "：物料编号为" + wlBh + "的检验类型只能为免检，抽检，全检，首检，巡检 ！");
                                                    haveErrors = true;
                                                    break;
                                            }
                                        }
                                        //计量单位
                                        String PRODUCTDATA_JLDW_NAME = newData.getStr("PRODUCTDATA_JLDW_NAME");
                                        if (StringUtil.isEmpty(PRODUCTDATA_JLDW_NAME)) {
                                            errorMessage.append(countErrorIndex++ + "：物料编号为" + wlBh + "的计量单位不能为空 ！");
                                            haveErrors = true;
                                        }
                                        //ROUTING，工艺路线编号
                                        String ROUTING = newData.getStr("PRODUCTDATA_CPGYLX");
                                        if (StringUtil.isNotEmpty(ROUTING)) {
                                            List<DynaBean> jgmes_gygl_gylx = serviceTemplate.selectList("JGMES_GYGL_GYLX", "and GYLX_GYLXNUM='" + ROUTING + "' or GYLX_GYLXNAME='" + ROUTING + "'");
                                            if (jgmes_gygl_gylx.size() > 0) {
                                                for (DynaBean jgmesGyglGylx : jgmes_gygl_gylx) {
                                                    if (jgmesGyglGylx.getInt("GYLX_STATUS") == 1) {
                                                        newData.set("PRODUCTDATA_CPGYLXID", jgmesGyglGylx.getStr("GYLX_ID"));
                                                        newData.set("PRODUCTDATA_CPGYLX", jgmesGyglGylx.getStr("GYLX_GYLXNAME"));
                                                        break;
                                                    }
                                                }
                                            } else {
                                                errorMessage.append(countErrorIndex++ + "：物料编号为" + wlBh + "未找到对应的工艺路线！");
                                                haveErrors = true;
                                            }
                                        }
                                        //条码性质，BarcodeProperties
                                        String BarcodeProperties = newData.getStr("PRODUCTDATA_TMXZ_NAME");
                                        if (StringUtil.isNotEmpty(BarcodeProperties)) {
                                            switch (BarcodeProperties.replaceAll(" ", "")) {
                                                case "唯一码":
                                                    newData.set("PRODUCTDATA_TMXZ_CODE", "TMXZX02");
                                                    newData.set("PRODUCTDATA_TMXZ_NAME", "唯一码");
                                                    break;
                                                case "批次码":
                                                    newData.set("PRODUCTDATA_TMXZ_CODE", "TMXZX01");
                                                    newData.set("PRODUCTDATA_TMXZ_NAME", "批次码");
                                                    break;
                                                default:
                                                    errorMessage.append(countErrorIndex++ + "：物料编号为" + wlBh + "的条码性质只有唯一码和批次码！");
                                                    haveErrors = true;
                                                    break;
                                            }
                                        }

                                        //校验完毕
                                        if (!haveErrors) {
                                            //更新SQL语句
                                            if (IsUpdate) {//此处为更新操作
                                                updateSt.setString(1, newData.getStr("PRODUCTDATA_NAME") == null ? oldDataBean.getStr("PRODUCTDATA_NAME") : newData.getStr("PRODUCTDATA_NAME"));        //产品名称
                                                updateSt.setString(2, newData.getStr("PRODUCTDATA_STATUS_CODE") == null ? oldDataBean.getStr("PRODUCTDATA_STATUS_CODE") : newData.getStr("PRODUCTDATA_STATUS_CODE"));    //状态
                                                updateSt.setString(3, newData.getStr("PRODUCTDATA_WLTYPE_CODE") == null ? oldDataBean.getStr("PRODUCTDATA_WLTYPE_CODE") : newData.getStr("PRODUCTDATA_WLTYPE_CODE")); //物料属性code
                                                updateSt.setString(4, newData.getStr("PRODUCTDATA_WLTYPE_NAME") == null ? oldDataBean.getStr("PRODUCTDATA_WLTYPE_NAME") : newData.getStr("PRODUCTDATA_WLTYPE_NAME")); //物料属性name
                                                updateSt.setString(5, newData.getStr("PRODUCTDATA_CPFL_NAME") == null ? oldDataBean.getStr("PRODUCTDATA_CPFL_NAME") : newData.getStr("PRODUCTDATA_CPFL_NAME"));   //产品分类
                                                updateSt.setString(6, newData.getStr("PRODUCTDATA_JLDW_NAME") == null ? oldDataBean.getStr("PRODUCTDATA_JLDW_NAME") : newData.getStr("PRODUCTDATA_JLDW_NAME"));   //计量单位
                                                updateSt.setString(7, newData.getStr("PRODUCTDATA_GG") == null ? oldDataBean.getStr("PRODUCTDATA_GG") : newData.getStr("PRODUCTDATA_GG"));          //规格
                                                updateSt.setString(8, newData.getStr("PRODUCTDATA_MS") == null ? oldDataBean.getStr("PRODUCTDATA_MS") : newData.getStr("PRODUCTDATA_MS"));          //描述
                                                updateSt.setString(9, newData.getStr("PRODUCTDATA_TMYZGZ") == null ? oldDataBean.getStr("PRODUCTDATA_TMYZGZ") : newData.getStr("PRODUCTDATA_TMYZGZ"));      //条码验证规则
                                                updateSt.setString(10, newData.getStr("PRODUCTDATA_JYJB_CODE") == null ? oldDataBean.getStr("PRODUCTDATA_JYJB_CODE") : newData.getStr("PRODUCTDATA_JYJB_CODE"));   //检验级别code
                                                updateSt.setString(11, newData.getStr("PRODUCTDATA_JYJB_NAME") == null ? oldDataBean.getStr("PRODUCTDATA_JYJB_NAME") : newData.getStr("PRODUCTDATA_JYJB_NAME"));   //检验级别name
                                                updateSt.setString(12, newData.getStr("PRODUCTDATA_JYLX_CODE") == null ? oldDataBean.getStr("PRODUCTDATA_JYLX_CODE") : newData.getStr("PRODUCTDATA_JYLX_CODE"));   //检验类型code
                                                updateSt.setString(13, newData.getStr("PRODUCTDATA_JYLX_NAME") == null ? oldDataBean.getStr("PRODUCTDATA_JYLX_NAME") : newData.getStr("PRODUCTDATA_JYLX_NAME"));   //检验类型name
                                                updateSt.setString(14, newData.getStr("PRODUCTDATA_BZ") == null ? oldDataBean.getStr("PRODUCTDATA_BZ") : newData.getStr("PRODUCTDATA_BZ"));          //备注
                                                updateSt.setString(15, newData.getStr("PRODUCTDATA_ZJM") == null ? oldDataBean.getStr("PRODUCTDATA_ZJM") : newData.getStr("PRODUCTDATA_ZJM"));         //助记码
                                                updateSt.setString(16, newData.getStr("PRODUCTDATA_TMH") == null ? oldDataBean.getStr("PRODUCTDATA_TMH") : newData.getStr("PRODUCTDATA_TMH"));         //条码号
                                                updateSt.setString(17, newData.getStr("PRODUCTDATA_KHCPH") == null ? oldDataBean.getStr("PRODUCTDATA_KHCPH") : newData.getStr("PRODUCTDATA_KHCPH"));       //客户产品号
                                                updateSt.setString(18, newData.getStr("PRODUCTDATA_CPGYLX") == null ? oldDataBean.getStr("PRODUCTDATA_CPGYLX") : newData.getStr("PRODUCTDATA_CPGYLX"));      //工艺路线名称
                                                updateSt.setString(19, newData.getStr("PRODUCTDATA_CPGYLXID") == null ? oldDataBean.getStr("PRODUCTDATA_CPGYLXID") : newData.getStr("PRODUCTDATA_CPGYLXID"));    //工艺路线ID
                                                updateSt.setString(20, newData.getStr("PRODUCTDATA_TMXZ_CODE") == null ? oldDataBean.getStr("PRODUCTDATA_TMXZ_CODE") : newData.getStr("PRODUCTDATA_TMXZ_CODE"));   //条码性质code
                                                updateSt.setString(21, newData.getStr("PRODUCTDATA_TMXZ_NAME") == null ? oldDataBean.getStr("PRODUCTDATA_NAME") : newData.getStr("PRODUCTDATA_NAME"));   //条码性质name
                                                updateSt.setString(22, newData.getStr("PRODUCTDATA_XH") == null ? oldDataBean.getStr("PRODUCTDATA_XH") : newData.getStr("PRODUCTDATA_XH"));          //型号
                                                updateSt.setString(23, newData.getStr("PRODUCTDATA_BH") == null ? oldDataBean.getStr("PRODUCTDATA_BH") : newData.getStr("PRODUCTDATA_BH"));          //产品编号
                                                updateSt.addBatch();
                                                trueUpdate++;
                                            } else {    //此处为插入操作
                                                insertSt.setString(1, newData.getStr("PRODUCTDATA_NAME"));        //产品名称
                                                insertSt.setInt(2, newData.getInt("PRODUCTDATA_STATUS_CODE"));    //状态
                                                insertSt.setString(3, newData.getStr("PRODUCTDATA_WLTYPE_CODE")); //物料属性code
                                                insertSt.setString(4, newData.getStr("PRODUCTDATA_WLTYPE_NAME")); //物料属性name
                                                insertSt.setString(5, newData.getStr("PRODUCTDATA_CPFL_NAME"));   //产品分类
                                                insertSt.setString(6, newData.getStr("PRODUCTDATA_JLDW_NAME"));   //计量单位
                                                insertSt.setString(7, newData.getStr("PRODUCTDATA_GG"));          //规格
                                                insertSt.setString(8, newData.getStr("PRODUCTDATA_MS"));          //描述
                                                insertSt.setString(9, newData.getStr("PRODUCTDATA_TMYZGZ"));      //条码验证规则
                                                insertSt.setString(10, newData.getStr("PRODUCTDATA_JYJB_CODE"));   //检验级别code
                                                insertSt.setString(11, newData.getStr("PRODUCTDATA_JYJB_NAME"));   //检验级别name
                                                insertSt.setString(12, newData.getStr("PRODUCTDATA_JYLX_CODE"));   //检验类型code
                                                insertSt.setString(13, newData.getStr("PRODUCTDATA_JYLX_NAME"));   //检验类型name
                                                insertSt.setString(14, newData.getStr("PRODUCTDATA_BZ"));          //备注
                                                insertSt.setString(15, newData.getStr("PRODUCTDATA_ZJM"));         //助记码
                                                insertSt.setString(16, newData.getStr("PRODUCTDATA_TMH"));         //条码号
                                                insertSt.setString(17, newData.getStr("PRODUCTDATA_KHCPH"));       //客户产品号
                                                insertSt.setString(18, newData.getStr("PRODUCTDATA_CPGYLX"));      //工艺路线名称
                                                insertSt.setString(19, newData.getStr("PRODUCTDATA_CPGYLXID"));    //工艺路线ID
                                                insertSt.setString(20, newData.getStr("PRODUCTDATA_TMXZ_CODE"));   //条码性质code
                                                insertSt.setString(21, newData.getStr("PRODUCTDATA_TMXZ_NAME"));   //条码性质name
                                                insertSt.setString(22, newData.getStr("PRODUCTDATA_XH"));          //型号
                                                insertSt.setString(23, newData.getStr("PRODUCTDATA_BH"));          //产品编号
                                                insertSt.setString(24, UUID.randomUUID().toString().replaceAll("-", "").substring(0, 19));//主键
                                                insertSt.addBatch();
                                                trueImport++;
                                            }
                                            if (i % 1000 == 0) {
                                                if (updateSt != null) {
                                                    updateSt.executeBatch();
                                                    updateSt.clearBatch();
                                                }
                                                if (insertSt != null) {
                                                    insertSt.executeBatch();
                                                    insertSt.clearBatch();
                                                }
                                                conn.commit();
                                            }
                                        } else {
                                            falseImport++;
                                        }
                                    } else {
                                        errorMessage.append(countErrorIndex++ + "：第" + i + "个数据导入失败，原因：" + hashMap.get("errMsg"));
                                        falseImport++;
                                    }
                                    i++;
                                }
                                if (updateSt != null) {
                                    updateSt.executeBatch();
                                    updateSt.clearBatch();
                                }
                                if (insertSt != null) {
                                    insertSt.executeBatch();
                                    insertSt.clearBatch();
                                }
                                conn.commit();

                            } catch (Exception e) {
                                e.printStackTrace();
                                errorMessage.append(countErrorIndex++ + "：异常错误");
                            } finally {
                                if (rs != null) {
                                    try {
                                        rs.close();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (updateSt != null) {
                                    try {
                                        updateSt.close();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (insertSt != null) {
                                    try {
                                        insertSt.close();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (conn != null) {
                                    try {
                                        conn.close();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                                res.add(trueImport);
                                res.add(trueUpdate);
                                res.add(falseImport);
                            }
                            latch.countDown();
                        }
                    });
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!"".equals(errorMessage.toString()) && errorMessage.toString() != null) {
                        ret.setMessage("成功导入" + res.get(0) + "条数据,更新了" + res.get(1) + "条数据，操作失败" + res.get(2) + "条，错误原因是：" + errorMessage.toString());
                    } else {
                        ret.setMessage("成功导入" + res.get(0) + "条数据,更新了" + res.get(1) + "条数据!");
                        ret.IsSuccess = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonBuilder.toJson(ret);
    }

    /**
     * 根据工单号或者产品编码查找工单信息
     *
     * @param WorkOrderCode 工单编号
     * @param MaterialCode  产品编号
     * @return
     */
    @Override
    public String GetWorkOrderData(String WorkOrderCode, String MaterialCode) {
        if (serviceTemplate == null) {
            serviceTemplate = SpringContextHolder.getBean("PCDynaServiceTemplate");
        }
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        try {

            String whereSql = "";
            //工单编号
            if (StringUtil.isNotEmpty(WorkOrderCode)) {
                if (MaterialCode.indexOf(",") != -1) {
                    whereSql += " and GDLB_GDHM in ('" + WorkOrderCode.replaceAll(",", "','") + "')";
                } else {
                    whereSql += " and GDLB_GDHM = '" + WorkOrderCode + "'";
                }
            }
            //产品编码
            if (StringUtil.isNotEmpty(MaterialCode)) {
                if (MaterialCode.indexOf(",") != -1) {
                    whereSql += " and GDLB_CPBH in ('" + MaterialCode.replaceAll(",", "','") + "')";
                } else {
                    whereSql += " and GDLB_CPBH = '" + MaterialCode + "'";
                }
            }
            List<DynaBean> jgmes_plan_gdlb = serviceTemplate.selectList("JGMES_PLAN_GDLB", whereSql);
            ret.Data = ret.getValues(jgmes_plan_gdlb);
        } catch (Exception e) {
            e.printStackTrace();
            ret.setMessage("异常错误");
        }
        return jsonBuilder.toJson(ret);
    }

    /**
     * 更新或新增工单信息
     *
     * @param jsonList
     * @return
     */
    @Override
    public String UpdateOrAddOrderData(String jsonList) {
        if (serviceTemplate == null) {
            serviceTemplate = SpringContextHolder.getBean("PCDynaServiceTemplate");
        }
        Boolean haveError = false;
        StringBuilder errorMessage = new StringBuilder(2000);//错误信息记录
        HttpServletRequest request = null;
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        if (StringUtil.isNotEmpty(jsonList)) {
            List<DynaBean> list = new Vector<>();
            //转换成List数组
            try {
                String key = "";
                String value = "";
                net.sf.json.JSONArray ja1 = net.sf.json.JSONArray.fromObject(jsonList);
                JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, "");
                if (ja1.size() > 0) {
                    for (int i = 0; i < ja1.size(); i++) {
                        DynaBean bean = new DynaBean();
                        bean.setStr(beanUtils.KEY_TABLE_CODE, "JGMES_PLAN_GDLB");
                        JSONObject jb1 = ja1.getJSONObject(i);  // 遍历 jsonarray 数组，把每一个对象转成 json 对象
                        Iterator it = jb1.keys();
                        while (it.hasNext()) {
                            key = (String) it.next();
                            value = jb1.getString(key);
                            bean.setStr(key, value);
                        }
                        list.add(bean);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage.append("数据转换失败！");
                haveError = true;
            }
            if (!haveError) {
                //开启线程
                ExecutorService pool = Executors.newFixedThreadPool(10);
                CountDownLatch latch = new CountDownLatch(1);
                List<Integer> res = new Vector<>();
                try {
                    pool.submit(new Runnable() {
                        @Override
                        public void run() {
                            //开启JDBC批数据处理
                            Integer countErrorIndex = 1;
                            Integer trueImport = 0;
                            Integer trueUpdate = 0;
                            Integer falseImport = 0;
                            Connection conn = null;
                            PreparedStatement updateSt = null;
                            PreparedStatement insertSt = null;
                            ResultSet rs = null;
                            String updateSql = "update JGMES_PLAN_GDLB set GDLB_DDHM=?,GDLB_CPBH=?,GDLB_NAME=?,GDLB_CPGG=?,GDLB_CPXH=?,GDLB_GDSL=?,GDLB_DDSL=?,GDLB_XPCSL=?,GDLB_WPCSL=?,GDLB_QGRQ=?," +
                                    "GDLB_OTDRQ=?,GDLB_DDJHRQ=?,GDLB_JHKGSJ=?,GDLB_JHWGSJ=?,GDLB_KHMC=?,GDLB_KHBM=?,DEPTNAME=?,DEPTCODE=?,GDLB_BZ=?,GDLB_NO_CODE=?,GDLB_NO_NAME=?,GDLB_WLQTSJ=?,GDLB_GDZT_CODE=?,GDLB_GDZT_NAME=?" +
                                    " where GDLB_GDHM=?";
                            String insertSql = "insert into JGMES_PLAN_GDLB(GDLB_DDHM,GDLB_CPBH,GDLB_NAME,GDLB_CPGG,GDLB_CPXH,GDLB_GDSL,GDLB_DDSL,GDLB_XPCSL,GDLB_WPCSL,GDLB_QGRQ,GDLB_OTDRQ,GDLB_DDJHRQ,GDLB_JHKGSJ,GDLB_JHWGSJ,GDLB_KHMC,GDLB_KHBM,DEPTNAME,DEPTCODE,GDLB_BZ,GDLB_NO_CODE,GDLB_NO_NAME,GDLB_WLQTSJ,GDLB_GDHM,JGMES_PLAN_GDLB_ID,GDLB_RQ,GDLB_GDZT_CODE,GDLB_GDZT_NAME)" +
                                    "Values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,date_format(now(),'%Y-%m-%d'),?,?)";
                            try {
                                Properties prop = new Properties();
                                InputStream inputStream = LoadProperties.class.getClassLoader().getResourceAsStream("jdbc.properties");
                                prop.load(inputStream);
                                String driverClass = prop.getProperty("jdbc.Driver");
                                String url = prop.getProperty("jdbc.url");
                                String username = prop.getProperty("jdbc.username");
                                String password = prop.getProperty("jdbc.password");
                                conn = DriverManager.getConnection(url, username, password);
                                conn.setAutoCommit(false);
                                updateSt = conn.prepareStatement(updateSql);
                                insertSt = conn.prepareStatement(insertSql);
                                Integer i = 1;
                                for (DynaBean dynaBean : list) {
                                    //数据处理
                                    DynaBean clone = dynaBean.clone();
                                    DynaBean newData = dynaBean.clone();
                                    Hashtable hashMap = checkValuesLen(clone, "GDLB_GDHM");
                                    Boolean IsUpdate = false;
                                    DynaBean oldDataBean = new DynaBean();
                                    if ((Boolean) hashMap.get("IsSussess")) {
                                        //数据库校验长度成功，特别字段校验
                                        Boolean haveErrors = false;
                                        String gdBh = newData.getStr("GDLB_GDHM");
                                        if (StringUtil.isEmpty(gdBh)) {
                                            errorMessage.append(countErrorIndex++ + "：第" + i + "条数据导入失败，原因：工单编号为空！");
                                            haveErrors = true;
                                        } else {
                                            oldDataBean = serviceTemplate.selectOne("JGMES_PLAN_GDLB", "and GDLB_GDHM='" + gdBh + "'");
                                            if (oldDataBean != null) {
                                                newData.set("JGMES_PLAN_GDLB_ID", oldDataBean.getStr("JGMES_PLAN_GDLB_ID"));
                                                IsUpdate = true;
                                            }
                                        }
                                        //订单编号，必填，不能为空
                                        String ddhm = newData.getStr("GDLB_DDHM");
                                        if (StringUtil.isEmpty(ddhm)) {
                                            errorMessage.append(countErrorIndex++ + "：工单编号为" + gdBh + "的订单编号不能为空 ！");
                                            haveErrors = true;
                                        }
                                        //产品编号，产品名称不能为空
                                        String cpbh = newData.getStr("GDLB_CPBH");
                                        String cpmc = newData.getStr("GDLB_NAME");
                                        if (StringUtil.isNotEmpty(cpbh)) {
                                            DynaBean jgmes_base_productdata = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", "and PRODUCTDATA_BH='" + cpbh + "'");
                                            if (jgmes_base_productdata != null) {
                                                if (StringUtil.isNotEmpty(cpmc)) {
                                                    if (!cpmc.equals(jgmes_base_productdata.getStr("PRODUCTDATA_NAME"))) {
                                                        errorMessage.append(countErrorIndex++ + "：工单编号为" + gdBh + "的产品名称和产品编号不匹配 ！");
                                                        haveErrors = true;
                                                    } else {
                                                        newData.set("GDLB_CPGG", jgmes_base_productdata.getStr("PRODUCTDATA_GG"));//产品规格
                                                        newData.set("GDLB_CPXH", jgmes_base_productdata.getStr("PRODUCTDATA_XH"));//产品型号
                                                    }
                                                } else {
                                                    errorMessage.append(countErrorIndex++ + "：工单编号为" + gdBh + "的产品名称不能为空 ！");
                                                    haveErrors = true;
                                                }

                                            } else {
                                                errorMessage.append(countErrorIndex++ + "：工单编号为" + gdBh + "的产品编号不存在！");
                                                haveErrors = true;
                                            }

                                        } else {
                                            errorMessage.append(countErrorIndex++ + "：工单编号为" + gdBh + "的产品编号不能为空 ！");
                                            haveErrors = true;
                                        }
                                        //工单数量，必填，必须大于0
                                        String gdsl = newData.getStr("GDLB_GDSL");
                                        if (pec.checkPositiveInteger(gdsl, false)) {
                                            newData.set("GDLB_DDSL", gdsl);
                                            newData.set("GDLB_XPCSL", gdsl);
                                            newData.set("GDLB_GDSL", gdsl);
                                            int gdlb_ypcsl = newData.getInt("GDLB_YPCSL");
                                            newData.set("GDLB_WPCSL", Integer.parseInt(gdsl) - gdlb_ypcsl);
                                        } else {
                                            errorMessage.append(countErrorIndex++ + "：工单编号为" + gdBh + "的工单数量不合法 ！");
                                            haveErrors = true;
                                        }

                                        //请购日期,DateOfRequisition,必填
                                        String DateOfRequisition = newData.getStr("GDLB_QGRQ");
                                        if (StringUtil.isNotEmpty(DateOfRequisition)) {
                                            if (!pec.checkDate(DateOfRequisition, false)) {
                                                errorMessage.append(countErrorIndex++ + "：工单编号为" + gdBh + "的请购日期非法 ！");
                                                haveErrors = true;
                                            } else {
                                                newData.set("GDLB_QGRQ", DateOfRequisition);
                                            }
                                        } else {
                                            errorMessage.append(countErrorIndex++ + "：工单编号为" + gdBh + "的请购日期不能为空 ！");
                                            haveErrors = true;
                                        }

                                        //交货日期,DateOfDelivery，校验日期格式
                                        String DateOfDelivery = newData.getStr("GDLB_OTDRQ");
                                        if (StringUtil.isNotEmpty(DateOfDelivery)) {
                                            if (!pec.checkDate(DateOfDelivery, false)) {
                                                errorMessage.append(countErrorIndex++ + "：工单编号为" + gdBh + "的交货日期非法 ！");
                                                haveErrors = true;
                                            } else {
                                                newData.set("GDLB_OTDRQ", DateOfDelivery);
                                                newData.set("GDLB_DDJHRQ", DateOfDelivery);
                                            }
                                        } else {
                                            errorMessage.append(countErrorIndex++ + "：工单编号为" + gdBh + "的交货日期不能为空 ！");
                                            haveErrors = true;
                                        }

                                        //客户名称，CustomerName
                                        String CustomerName = newData.getStr("GDLB_KHMC");
                                        if (StringUtil.isNotEmpty(CustomerName)) {
                                            List<DynaBean> jgmes_base_cust = serviceTemplate.selectList("JGMES_BASE_CUST", "and CUST_NAME='" + CustomerName + "'");
                                            if (jgmes_base_cust.size() > 0) {
                                                newData.set("GDLB_KHMC", CustomerName);
                                                newData.set("GDLB_KHBM", jgmes_base_cust.get(0).getStr("CUST_CODE"));
                                            } else {
                                                errorMessage.append(countErrorIndex++ + "：工单编号为" + gdBh + "的该客户在客户列表中不存在！");
                                                haveErrors = true;
                                            }
                                        }

                                        //部门，DeptName
                                        String DeptName = newData.getStr("DEPTNAME");
                                        if (StringUtil.isNotEmpty(DeptName)) {
                                            List<DynaBean> je_core_department_ = serviceTemplate.selectList("JE_CORE_DEPARTMENT ", "and DEPTNAME ='" + DeptName + "'");
                                            if (je_core_department_.size() > 0) {
                                                newData.set("DEPTNAME", DeptName);
                                                newData.set("DEPTCODE", je_core_department_.get(0).getStr("DEPTCODE"));
                                            } else {
                                                errorMessage.append(countErrorIndex++ + "：工单编号为" + gdBh + "的部门不存在！");
                                                haveErrors = true;
                                            }
                                        }
                                        //物料是否齐套，MaterialReadiness
                                        String MaterialReadiness = newData.getStr("GDLB_NO_CODE");
                                        if (!pec.checkPositiveInteger(MaterialReadiness, true)) {
                                            errorMessage.append(countErrorIndex++ + "：工单编号为" + gdBh + "的物料是否齐套值非法！");
                                            haveErrors = true;
                                        } else {
                                            if (MaterialReadiness.equals("1")) {
                                                newData.set("GDLB_NO_CODE", 1);
                                                newData.set("GDLB_NO_NAME", "是");
                                            } else {
                                                newData.set("GDLB_NO_CODE", 2);
                                                newData.set("GDLB_NO_NAME", "否");
                                            }
                                        }

                                        //齐套时间，ReadinessTime
                                        String ReadinessTime = newData.getStr("GDLB_WLQTSJ");
                                        if (StringUtil.isNotEmpty(ReadinessTime)) {
                                            if (!pec.checkDate(ReadinessTime, false)) {
                                                errorMessage.append(countErrorIndex++ + "：工单编号为" + gdBh + "的齐套时间非法 ！");
                                                haveErrors = true;
                                            } else {
                                                newData.set("GDLB_WLQTSJ", ReadinessTime);
                                            }
                                        } else {
                                            if (MaterialReadiness.equals("1")) {
                                                errorMessage.append(countErrorIndex++ + "：工单编号为" + gdBh + "的齐套时间不能为空 ！");
                                                haveErrors = true;
                                            }
                                        }
                                        String GDLB_GDZT_CODE = newData.getStr("GDLB_GDZT_CODE");
                                        if (StringUtil.isEmpty(GDLB_GDZT_CODE)){
                                            newData.set("GDLB_GDZT_CODE",1);
                                            newData.set("GDLB_GDZT_NAME","未完");
                                        }

                                        //校验完毕
                                        if (!haveErrors) {
                                            //更新SQL语句
                                            if (IsUpdate) {//此处为更新操作
                                                updateSt.setString(1, newData.getStr("GDLB_DDHM") == null ? oldDataBean.getStr("GDLB_DDHM") : newData.getStr("GDLB_DDHM"));   //订单号
                                                updateSt.setString(2, newData.getStr("GDLB_CPBH") == null ? oldDataBean.getStr("GDLB_CPBH") : newData.getStr("GDLB_CPBH"));   //产品编号
                                                updateSt.setString(3, newData.getStr("GDLB_NAME") == null ? oldDataBean.getStr("GDLB_NAME") : newData.getStr("GDLB_NAME"));   //产品名称
                                                updateSt.setString(4, newData.getStr("GDLB_CPGG") == null ? oldDataBean.getStr("GDLB_CPGG") : newData.getStr("GDLB_CPGG"));   //产品规格
                                                updateSt.setString(5, newData.getStr("GDLB_CPXH") == null ? oldDataBean.getStr("GDLB_CPXH") : newData.getStr("GDLB_CPXH"));   //产品型号
                                                updateSt.setString(6, newData.getStr("GDLB_GDSL") == null ? oldDataBean.getStr("GDLB_GDSL") : newData.getStr("GDLB_GDSL"));      //工单数量
                                                updateSt.setString(7, newData.getStr("GDLB_DDSL") == null ? oldDataBean.getStr("GDLB_DDSL") : newData.getStr("GDLB_DDSL"));      //订单数量
                                                updateSt.setString(8, newData.getStr("GDLB_XPCSL") == null ? oldDataBean.getStr("GDLB_XPCSL") : newData.getStr("GDLB_XPCSL"));     //需排产数量
                                                updateSt.setString(9, newData.getStr("GDLB_WPCSL") == null ? oldDataBean.getStr("GDLB_WPCSL") : newData.getStr("GDLB_WPCSL"));     //未排产数量
                                                updateSt.setString(10, newData.getStr("GDLB_QGRQ") == null ? oldDataBean.getStr("GDLB_QGRQ") : newData.getStr("GDLB_QGRQ"));  //请购日期
                                                updateSt.setString(11, newData.getStr("GDLB_OTDRQ") == null ? oldDataBean.getStr("GDLB_OTDRQ") : newData.getStr("GDLB_OTDRQ")); //OTD日期
                                                updateSt.setString(12, newData.getStr("GDLB_DDJHRQ") == null ? oldDataBean.getStr("GDLB_DDJHRQ") : newData.getStr("GDLB_DDJHRQ"));//交货日期
                                                updateSt.setString(13, newData.getStr("GDLB_JHKGSJ") == null ? oldDataBean.getStr("GDLB_JHKGSJ") : newData.getStr("GDLB_JHKGSJ"));//计划开工时间
                                                updateSt.setString(14, newData.getStr("GDLB_JHWGSJ") == null ? oldDataBean.getStr("GDLB_JHWGSJ") : newData.getStr("GDLB_JHWGSJ"));//计划完工时间
                                                updateSt.setString(15, newData.getStr("GDLB_KHMC") == null ? oldDataBean.getStr("GDLB_KHMC") : newData.getStr("GDLB_KHMC"));  //客户名称
                                                updateSt.setString(16, newData.getStr("GDLB_KHBM") == null ? oldDataBean.getStr("GDLB_KHBM") : newData.getStr("GDLB_KHBM"));  //客户编码
                                                updateSt.setString(17, newData.getStr("DEPTNAME") == null ? oldDataBean.getStr("DEPTNAME") : newData.getStr("DEPTNAME"));   //部门名称
                                                updateSt.setString(18, newData.getStr("DEPTCODE") == null ? oldDataBean.getStr("DEPTCODE") : newData.getStr("DEPTCODE"));   //部门编码
                                                updateSt.setString(19, newData.getStr("GDLB_BZ") == null ? oldDataBean.getStr("GDLB_BZ") : newData.getStr("GDLB_BZ"));    //备注
                                                updateSt.setString(20, newData.getStr("GDLB_NO_CODE") == null ? oldDataBean.getStr("GDLB_NO_CODE") : newData.getStr("GDLB_NO_CODE"));  //物料是否齐套code
                                                updateSt.setString(21, newData.getStr("GDLB_NO_NAME") == null ? oldDataBean.getStr("GDLB_NO_NAME") : newData.getStr("GDLB_NO_NAME"));//物料是否齐套name
                                                updateSt.setString(22, newData.getStr("GDLB_WLQTSJ") == null ? oldDataBean.getStr("GDLB_WLQTSJ") : newData.getStr("GDLB_WLQTSJ")); //物料齐套时间
                                                updateSt.setString(23, newData.getStr("GDLB_GDZT_CODE") == null ? oldDataBean.getStr("GDLB_GDZT_CODE") : newData.getStr("GDLB_GDZT_CODE"));
                                                updateSt.setString(24, newData.getStr("GDLB_GDZT_NAME") == null ? oldDataBean.getStr("GDLB_GDZT_NAME") : newData.getStr("GDLB_GDZT_NAME"));
                                                updateSt.setString(25, newData.getStr("GDLB_GDHM") == null ? oldDataBean.getStr("GDLB_GDHM") : newData.getStr("GDLB_GDHM"));  //工单编码
                                                updateSt.addBatch();
                                                trueUpdate++;
                                            } else {    //此处为插入操作
                                                insertSt.setString(1, newData.getStr("GDLB_DDHM"));   //订单号
                                                insertSt.setString(2, newData.getStr("GDLB_CPBH"));   //产品编号
                                                insertSt.setString(3, newData.getStr("GDLB_NAME"));   //产品名称
                                                insertSt.setString(4, newData.getStr("GDLB_CPGG"));   //产品规格
                                                insertSt.setString(5, newData.getStr("GDLB_CPXH"));   //产品型号
                                                insertSt.setInt(6, newData.getInt("GDLB_GDSL"));      //工单数量
                                                insertSt.setInt(7, newData.getInt("GDLB_DDSL"));      //订单数量
                                                insertSt.setInt(8, newData.getInt("GDLB_XPCSL"));     //需排产数量
                                                insertSt.setInt(9, newData.getInt("GDLB_WPCSL"));     //未排产数量
                                                insertSt.setString(10, newData.getStr("GDLB_QGRQ"));  //请购日期
                                                insertSt.setString(11, newData.getStr("GDLB_OTDRQ")); //OTD日期
                                                insertSt.setString(12, newData.getStr("GDLB_DDJHRQ"));//交货日期
                                                insertSt.setString(13, newData.getStr("GDLB_JHKGSJ"));//计划开工时间
                                                insertSt.setString(14, newData.getStr("GDLB_JHWGSJ"));//计划完工时间
                                                insertSt.setString(15, newData.getStr("GDLB_KHMC"));  //客户名称
                                                insertSt.setString(16, newData.getStr("GDLB_KHBM"));  //客户编码
                                                insertSt.setString(17, newData.getStr("DEPTNAME"));   //部门名称
                                                insertSt.setString(18, newData.getStr("DEPTCODE"));   //部门编码
                                                insertSt.setString(19, newData.getStr("GDLB_BZ"));    //备注
                                                insertSt.setInt(20, newData.getInt("GDLB_NO_CODE"));  //物料是否齐套code
                                                insertSt.setString(21, newData.getStr("GDLB_NO_NAME"));//物料是否齐套name
                                                insertSt.setString(22, newData.getStr("GDLB_WLQTSJ")); //物料齐套时间
                                                insertSt.setString(23, newData.getStr("GDLB_GDHM"));  //工单编码
                                                insertSt.setString(24, UUID.randomUUID().toString().replaceAll("-", "").substring(0, 19));  //工单编码
                                                insertSt.setString(25, newData.getStr("GDLB_GDZT_CODE"));
                                                insertSt.setString(26, newData.getStr("GDLB_GDZT_NAME"));
                                                insertSt.addBatch();
                                                trueImport++;
                                            }
                                            if (i % 1000 == 0) {    //1000条处理一次
                                                try {
                                                    if (updateSt != null) {
                                                        updateSt.executeBatch();
                                                        updateSt.clearBatch();
                                                    }
                                                    if (insertSt != null) {
                                                        insertSt.executeBatch();
                                                        insertSt.clearBatch();
                                                    }
                                                    conn.commit();

                                                } catch (Exception e) {
                                                    errorMessage.append(e.toString());
                                                }

                                            }
                                        } else {
                                            falseImport++;
                                        }
                                    } else {
                                        errorMessage.append(countErrorIndex++ + "：第" + i + "个数据导入失败，原因：" + hashMap.get("errMsg"));
                                        falseImport++;
                                    }
                                    i++;
                                }
                                if (updateSt != null) {
                                    updateSt.executeBatch();
                                    updateSt.clearBatch();
                                }
                                if (insertSt != null) {
                                    insertSt.executeBatch();
                                    insertSt.clearBatch();
                                }
                                conn.commit();

                            } catch (Exception e) {
                                e.printStackTrace();
                                errorMessage.append(countErrorIndex++ + "：异常错误");
                            } finally {
                                if (rs != null) {
                                    try {
                                        rs.close();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (insertSt != null) {
                                    try {
                                        insertSt.close();
                                    } catch (SQLException e) {

                                        e.printStackTrace();
                                    }
                                }
                                if (updateSt != null) {
                                    try {
                                        updateSt.close();
                                    } catch (SQLException e) {

                                        e.printStackTrace();
                                    }
                                }
                                if (conn != null) {
                                    try {
                                        conn.close();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                                res.add(trueImport);
                                res.add(trueUpdate);
                                res.add(falseImport);
                            }

                            latch.countDown();
                        }
                    });

                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("主线程执行");
                    if (!"".equals(errorMessage.toString()) && errorMessage.toString() != null) {
                        ret.setMessage("成功导入" + res.get(0) + "条数据,更新了" + res.get(1) + "条数据，操作失败" + res.get(2) + "条，错误原因是：" + errorMessage.toString());
                    } else {
                        ret.setMessage("成功导入" + res.get(0) + "条数据,更新了" + res.get(1) + "条数据!");
                        ret.IsSuccess = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonBuilder.toJson(ret);
    }

    /**
     * 通过客户编号查询客户信息
     *
     * @param ClientCode 客户编码
     * @param ClientName 客户名称
     * @return
     */
    @Override
    public String GetClientData(String ClientCode, String ClientName) {
        if (serviceTemplate == null) {
            serviceTemplate = SpringContextHolder.getBean("PCDynaServiceTemplate");
        }
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();

        try {
            String whereSql = "";
            //工单编号
            if (StringUtil.isNotEmpty(ClientCode)) {
                if (ClientCode.indexOf(",") != -1) {
                    whereSql += " and CUST_CODE in ('" + ClientCode.replaceAll(",", "','") + "')";
                } else {
                    whereSql += " and CUST_CODE = '" + ClientCode + "'";
                }
            }
            //产品编码
            if (StringUtil.isNotEmpty(ClientName)) {
                if (ClientName.indexOf(",") != -1) {
                    whereSql += " and CUST_NAME in ('" + ClientName.replaceAll(",", "','") + "') or CUST_SNAME in ('" + ClientName.replaceAll(",", "','") + "') or CUST_SCODE in ('" + ClientName.replaceAll(",", "','") + "')";
                } else {
                    whereSql += " and CUST_NAME like '%" + ClientName + "%' or CUST_SNAME like '%" + ClientName + "%' or CUST_SCODE like '%" + ClientName + "%'";
                }
            }
            List<DynaBean> jgmes_base_cust = serviceTemplate.selectList("JGMES_BASE_CUST", whereSql);
            ret.Data = ret.getValues(jgmes_base_cust);
        } catch (Exception e) {
            e.printStackTrace();
            ret.setMessage("异常错误");
        }
        return jsonBuilder.toJson(ret);
    }

    @Override
    public String UpdateOrAddClientData(String jsonList) {
        if (serviceTemplate == null) {
            serviceTemplate = SpringContextHolder.getBean("PCDynaServiceTemplate");
        }
        Boolean haveError = false;
        StringBuffer errorMessage = new StringBuffer(2000);//错误信息记录
        HttpServletRequest request = null;
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        if (StringUtil.isNotEmpty(jsonList)) {
            List<DynaBean> list = new Vector<>();
            //转换成List数组
            try {
                String key = "";
                String value = "";
                net.sf.json.JSONArray ja1 = net.sf.json.JSONArray.fromObject(jsonList);
                JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, "");
                if (ja1.size() > 0) {
                    for (int i = 0; i < ja1.size(); i++) {
                        DynaBean bean = new DynaBean();
                        bean.setStr(beanUtils.KEY_TABLE_CODE, "JGMES_BASE_CUST");
                        JSONObject jb1 = ja1.getJSONObject(i);  // 遍历 jsonarray 数组，把每一个对象转成 json 对象
                        Iterator it = jb1.keys();
                        while (it.hasNext()) {
                            key = (String) it.next();
                            value = jb1.getString(key);
                            bean.setStr(key, value);
                        }
                        list.add(bean);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage.append("数据转换失败！");
                haveError = true;
            }
            if (!haveError) {
                //开启线程
                ExecutorService pool = Executors.newFixedThreadPool(10);
                CountDownLatch latch = new CountDownLatch(1);
                List<Integer> res = new Vector<>();
                try {
                    pool.submit(new Runnable() {
                        @Override
                        public void run() {
                            //开启JDBC批数据处理
                            Integer countErrorIndex = 1;
                            Integer trueImport = 0;
                            Integer trueUpdate = 0;
                            Integer falseImport = 0;
                            Connection conn = null;
                            PreparedStatement updateSt = null;
                            PreparedStatement insertSt = null;
                            ResultSet rs = null;
                            String updateSql = "update JGMES_BASE_CUST set CUST_NAME=?,CUST_SNAME=?,CUST_SCODE=? where CUST_CODE=?";
                            String insertSql = "insert into  JGMES_BASE_CUST(CUST_NAME,CUST_SNAME,CUST_SCODE,CUST_CODE,JGMES_BASE_CUST_ID) Values(?,?,?,?,?)";
                            try {
                                Properties prop = new Properties();
                                InputStream inputStream = LoadProperties.class.getClassLoader().getResourceAsStream("jdbc.properties");
                                prop.load(inputStream);
                                String driverClass = prop.getProperty("jdbc.Driver");
                                String url = prop.getProperty("jdbc.url");
                                String username = prop.getProperty("jdbc.username");
                                String password = prop.getProperty("jdbc.password");
                                conn = DriverManager.getConnection(url, username, password);
                                conn.setAutoCommit(false);
                                updateSt = conn.prepareStatement(updateSql);
                                insertSt = conn.prepareStatement(insertSql);
                                Integer i = 1;
                                for (DynaBean dynaBean : list) {
                                    //数据处理
                                    DynaBean clone = dynaBean.clone();
                                    DynaBean newData = dynaBean.clone();
                                    Hashtable hashMap = checkValuesLen(clone, "CUST_CODE");
                                    Boolean IsUpdate = false;
                                    DynaBean oldDataBean = new DynaBean();
                                    if ((Boolean) hashMap.get("IsSussess")) {
                                        //数据库校验长度成功，特别字段校验
                                        Boolean haveErrors = false;
                                        //客户编号，不能为空
                                        String custCode = newData.getStr("CUST_CODE");
                                        if (StringUtil.isEmpty(custCode)) {
                                            errorMessage.append(countErrorIndex++ + "：第" + i + "条数据导入失败，原因：客户编码为空！");
                                            haveErrors = true;
                                        } else {
                                            oldDataBean = serviceTemplate.selectOne("JGMES_BASE_CUST", "and CUST_CODE='" + custCode + "'");
                                            if (oldDataBean != null) {
                                                IsUpdate = true;
                                            }
                                        }
                                        //客户名称，不能为空
                                        String custName = newData.getStr("CUST_NAME");
                                        if (StringUtil.isEmpty(custName)) {
                                            errorMessage.append(countErrorIndex++ + "客户编码为" + custCode + "的客户名称不能为空 ！");
                                            haveErrors = true;
                                        }else{
                                            String IsCUST_NAMEStr = newData.getStr("IsCUST_NAME");
                                            Integer IsMaterialName = StringUtil.isEmpty(IsCUST_NAMEStr)||!IsCUST_NAMEStr.equals("0") ? 1 : 0;
                                            if (IsMaterialName == 0) {
                                                List<DynaBean> jgmes_base_cust = serviceTemplate.selectList("JGMES_BASE_CUST", "and CUST_NAME='" + custName + "' and CUST_CODE!='" + custCode + "'");
                                                if (jgmes_base_cust.size()>0){
                                                    errorMessage.append(countErrorIndex++ + "客户编码为" + custCode + "的客户名称已存在，不允许为空 ！");
                                                    haveErrors = true;
                                                }
                                            }
                                        }
                                        //客户简码，不能为空
                                        String custScode = newData.getStr("CUST_SCODE");
                                        if (StringUtil.isEmpty(custScode)) {
                                            errorMessage.append(countErrorIndex++ + "客户编码为" + custCode + "的客户简码不能为空 ！");
                                            haveErrors = true;
                                        }


                                        //校验完毕
                                        if (!haveErrors) {
                                            //更新SQL语句
                                            if (IsUpdate) {//此处为更新操作
                                                updateSt.setString(1, newData.getStr("CUST_NAME") == null ? oldDataBean.getStr("CUST_NAME") : newData.getStr("CUST_NAME"));
                                                updateSt.setString(2, newData.getStr("CUST_SNAME") == null ? oldDataBean.getStr("CUST_SNAME") : newData.getStr("CUST_SNAME"));
                                                updateSt.setString(3, newData.getStr("CUST_SCODE") == null ? oldDataBean.getStr("CUST_SCODE") : newData.getStr("CUST_SCODE"));
                                                updateSt.setString(4, newData.getStr("CUST_CODE") == null ? oldDataBean.getStr("CUST_CODE") : newData.getStr("CUST_CODE"));
                                                updateSt.addBatch();
                                                trueUpdate++;
                                            } else {    //此处为插入操作
                                                insertSt.setString(1, newData.getStr("CUST_NAME"));
                                                insertSt.setString(2, newData.getStr("CUST_SNAME"));
                                                insertSt.setString(3, newData.getStr("CUST_SCODE"));
                                                insertSt.setString(4, newData.getStr("CUST_CODE"));
                                                insertSt.setString(5, UUID.randomUUID().toString().replaceAll("-", "").substring(0, 19));
                                                insertSt.addBatch();
                                                trueImport++;
                                            }
                                            if (i % 1000 == 0) {    //1000条处理一次
                                                if (updateSt != null) {
                                                    updateSt.executeBatch();
                                                    updateSt.clearBatch();
                                                }
                                                if (insertSt != null) {
                                                    insertSt.executeBatch();
                                                    insertSt.clearBatch();
                                                }
                                                conn.commit();
                                            }
                                        } else {
                                            falseImport++;
                                        }
                                    } else {
                                        errorMessage.append(countErrorIndex++ + "：第" + i + "个数据导入失败，原因：" + hashMap.get("errMsg"));
                                        falseImport++;
                                    }
                                    i++;
                                }
                                if (updateSt != null) {
                                    updateSt.executeBatch();
                                    updateSt.clearBatch();
                                }
                                if (insertSt != null) {
                                    insertSt.executeBatch();
                                    insertSt.clearBatch();
                                }
                                conn.commit();
                            } catch (Exception e) {
                                e.printStackTrace();
                                errorMessage.append(countErrorIndex++ + "：异常错误");
                            } finally {
                                if (rs != null) {
                                    try {
                                        rs.close();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (updateSt != null) {
                                    try {
                                        updateSt.close();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (insertSt != null) {
                                    try {
                                        insertSt.close();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (conn != null) {
                                    try {
                                        conn.close();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                                res.add(trueImport);
                                res.add(trueUpdate);
                                res.add(falseImport);
                            }
                            latch.countDown();
                        }
                    });
                    try {
                        latch.await();//让主线程等待多个执行完后（即latch变为）再执行
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!"".equals(errorMessage.toString()) && errorMessage.toString() != null) {
                        ret.setMessage("成功导入" + res.get(0) + "条数据,更新了" + res.get(1) + "条数据，操作失败" + res.get(2) + "条，错误原因是：" + errorMessage.toString());
                    } else {
                        ret.setMessage("成功导入" + res.get(0) + "条数据,更新了" + res.get(1) + "条数据!");
                        ret.IsSuccess = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonBuilder.toJson(ret);
    }

    /**
     * 根据任务单号或者订单查找工单信息
     *
     * @param SCRW_RWDH
     * @param SCRW_DDHM
     * @return
     */
    @Override
    public String GetProductiveTaskData(String SCRW_RWDH, String SCRW_DDHM, String SCRW_GDHM) {
        if (serviceTemplate == null) {
            serviceTemplate = SpringContextHolder.getBean("PCDynaServiceTemplate");
        }
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();

        try {
            String whereSql = "";
            //任务单号
            if (StringUtil.isNotEmpty(SCRW_RWDH)) {
                if (SCRW_RWDH.indexOf(",") != -1) {
                    whereSql += " and SCRW_RWDH in ('" + SCRW_RWDH.replaceAll(",", "','") + "')";
                } else {
                    whereSql += " and SCRW_RWDH = '" + SCRW_RWDH + "'";
                }
            }
            //订单号
            if (StringUtil.isNotEmpty(SCRW_DDHM)) {
                if (SCRW_DDHM.indexOf(",") != -1) {
                    whereSql += " and SCRW_DDHM in ('" + SCRW_DDHM.replaceAll(",", "','") + "')";
                } else {
                    whereSql += " and SCRW_DDHM = '" + SCRW_DDHM + "'";
                }
            }
            //工单号
            if (StringUtil.isNotEmpty(SCRW_GDHM)) {
                if (SCRW_GDHM.indexOf(",") != -1) {
                    whereSql += " and SCRW_GDHM in ('" + SCRW_GDHM.replaceAll(",", "','") + "')";
                } else {
                    whereSql += " and SCRW_GDHM = '" + SCRW_GDHM + "'";
                }
            }
            List<DynaBean> bean = serviceTemplate.selectList("JGMES_PLAN_SCRW", whereSql);
            ret.Data = ret.getValues(bean);
        } catch (Exception e) {
            e.printStackTrace();
            ret.setMessage("异常错误");
        }
        return jsonBuilder.toJson(ret);
    }

    /**
     * 传入多条数据的Json字符串值以更新或新增生产任务单信息
     *
     * @param jsonList
     * @return
     */
    @Override
    public String UpdateOrAddProductiveTaskData(String jsonList) {
        if (serviceTemplate == null) {
            serviceTemplate = SpringContextHolder.getBean("PCDynaServiceTemplate");
        }
        Boolean haveError = false;
        StringBuffer errorMessage = new StringBuffer(2000);//错误信息记录
        HttpServletRequest request = null;
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        if (StringUtil.isNotEmpty(jsonList)) {
            List<DynaBean> list = new Vector<>();
            //转换成List数组
            try {
                String key = "";
                String value = "";
                net.sf.json.JSONArray ja1 = net.sf.json.JSONArray.fromObject(jsonList);
                JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, "");
                if (ja1.size() > 0) {
                    for (int i = 0; i < ja1.size(); i++) {
                        DynaBean bean = new DynaBean();
                        bean.setStr(beanUtils.KEY_TABLE_CODE, "JGMES_PLAN_SCRW");
                        JSONObject jb1 = ja1.getJSONObject(i);  // 遍历 jsonarray 数组，把每一个对象转成 json 对象
                        Iterator it = jb1.keys();
                        while (it.hasNext()) {
                            key = (String) it.next();
                            value = jb1.getString(key);
                            bean.setStr(key, value);
                        }
                        list.add(bean);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage.append("数据转换失败！");
                haveError = true;
            }
            if (!haveError) {
                //开启线程
                ExecutorService pool = Executors.newFixedThreadPool(10);
                CountDownLatch latch = new CountDownLatch(1);
                List<Integer> res = new Vector<>();
                try {
                    pool.submit(new Runnable() {
                        @Override
                        public void run() {
                            //开启JDBC批数据处理
                            Integer countErrorIndex = 1;
                            Integer trueImport = 0;
                            Integer trueUpdate = 0;
                            Integer falseImport = 0;
                            Connection conn = null;
                            PreparedStatement updateSt = null;
                            PreparedStatement insertSt = null;
                            ResultSet rs = null;
                            String updateSql = "update JGMES_PLAN_SCRW set SCRW_CPBH=?,SCRW_NAME=?,SCRW_CXBM=?,SCRW_CXMC=?,SCRW_JHKGSJ=?,SCRW_JHWGSJ=?,SCRW_QGRQ=?,SCRW_OTDRQ=?,SCRW_GDHM=?,SCRW_WCSL=?,SCRW_GDSL=?,SCRW_DDHM=?,SCRW_DDSL=?,\n" +
                                    "SCRW_KHBM=?,SCRW_KHMC=?,SCRW_BZ=?,SCRW_DEPTNAME=?,SCRW_YS=?,SCRW_SCSL=?,SCRW_SXSJ=?,SCRW_DBSL=?,SCRW_LPSL=?,SCRW_BLSL=?,SCRW_BFSL=?,SCRW_SJWGSJ=?,SCRW_SJKGSJ=?,SCRW_LCKH=?,SCRW_PGSL=?,SCRW_LB=?,SCRW_SX=?,SCRW_BZNAME=?,SCRW_ERPGX=?,\n" +
                                    "SCRW_SB=?,SCRW_HM=?,SCRW_RS=?,SCRW_BZCN=?,SCRW_ZBSC=?,SCRW_ZBSJSC=?,SCRW_ZBJHCL=?,SCRW_ZBSJCL=?,SCRW_JBCN=?,SCRW_JBSC=?,SCRW_JBSJSC=?,SCRW_JBJHCL=?,SCRW_JBSJCL=?,SCRW_SGZF=?,SCRW_BCSCMB=?,SCRW_MJBH=?,SCRW_MJMC=?,SCRW_SPM=?,SCRW_PS=?,\n" +
                                    "SCRW_LS=?,SCRW_SCYB_CODE=?,SCRW_SCYB_NAME=?,SCRW_CZSMS_CODE=?,SCRW_CZSMS_NAME=?,SCRW_SBZT_CODE=?,SCRW_SBZT_NAME=?,SCRW_CPGG=?,SCRW_CPXH=?,SCRW_PCSL=?,SCRW_RWZT_CODE=?,SCRW_RWZT_NAME=? where SCRW_RWDH=?";

                            String insertSql = "insert into JGMES_PLAN_SCRW(SCRW_SCRWDATASOURCE_CODE,SCRW_CPBH,SCRW_NAME,SCRW_CXBM,SCRW_CXMC,SCRW_JHKGSJ,SCRW_JHWGSJ,SCRW_QGRQ,SCRW_OTDRQ,SCRW_GDHM,SCRW_WCSL,SCRW_GDSL,SCRW_DDHM,SCRW_DDSL,\n" +
                                    "SCRW_KHBM,SCRW_KHMC,SCRW_BZ,SCRW_DEPTNAME,SCRW_YS,SCRW_SCSL,SCRW_SXSJ,SCRW_DBSL,SCRW_LPSL,SCRW_BLSL,SCRW_BFSL,SCRW_SJWGSJ,SCRW_SJKGSJ,SCRW_LCKH,SCRW_PGSL,SCRW_LB,SCRW_SX,SCRW_BZNAME,SCRW_ERPGX,\n" +
                                    "SCRW_SB,SCRW_HM,SCRW_RS,SCRW_BZCN,SCRW_ZBSC,SCRW_ZBSJSC,SCRW_ZBJHCL,SCRW_ZBSJCL,SCRW_JBCN,SCRW_JBSC,SCRW_JBSJSC,SCRW_JBJHCL,SCRW_JBSJCL,SCRW_SGZF,SCRW_BCSCMB,SCRW_MJBH,SCRW_MJMC,SCRW_SPM,SCRW_PS,\n" +
                                    "SCRW_LS,SCRW_SCYB_CODE,SCRW_SCYB_NAME,SCRW_CZSMS_CODE,SCRW_CZSMS_NAME,SCRW_SBZT_CODE,SCRW_SBZT_NAME,SCRW_RWDH,JGMES_PLAN_SCRW_ID,SCRW_CPGG,SCRW_CPXH,SCRW_PCSL,SCRW_RWZT_CODE,SCRW_RWZT_NAME) \n" +
                                    "Values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                            try {
                                Properties prop = new Properties();
                                InputStream inputStream = LoadProperties.class.getClassLoader().getResourceAsStream("jdbc.properties");
                                prop.load(inputStream);
                                String driverClass = prop.getProperty("jdbc.Driver");
                                String url = prop.getProperty("jdbc.url");
                                String username = prop.getProperty("jdbc.username");
                                String password = prop.getProperty("jdbc.password");
                                conn = DriverManager.getConnection(url, username, password);
                                conn.setAutoCommit(false);
                                updateSt = conn.prepareStatement(updateSql);
                                insertSt = conn.prepareStatement(insertSql);
                                Integer i = 1;
                                for (DynaBean dynaBean : list) {
                                    //数据处理
                                    DynaBean clone = dynaBean.clone();
                                    DynaBean newData = dynaBean.clone();
                                    Hashtable hashMap = checkValuesLen(clone, "SCRW_RWDH");
                                    Boolean IsUpdate = false;
                                    DynaBean oldDataBean = new DynaBean();
                                    if ((Boolean) hashMap.get("IsSussess")) {
                                        //数据库校验长度成功，特别字段校验
                                        Boolean haveErrors = false;
                                        //任务单号，不能为空
                                        String rwdh = newData.getStr("SCRW_RWDH");
                                        if (StringUtil.isEmpty(rwdh)) {
                                            errorMessage.append(countErrorIndex++ + "：第" + i + "条数据导入失败，原因：任务单号为空！");
                                            haveErrors = true;
                                        } else {
                                            oldDataBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", "and SCRW_RWDH='" + rwdh + "'");
                                            if (oldDataBean != null) {
                                                newData.set("JGMES_PLAN_SCRW_ID", oldDataBean.getStr("JGMES_PLAN_SCRW_ID"));
                                                IsUpdate = true;
                                            }
                                        }
                                        if (!haveErrors) {
                                            //产品编号+产品名称校验
                                            String cpbh = newData.getStr("SCRW_CPBH");
                                            String cpmc = newData.getStr("SCRW_NAME");
                                            if (StringUtil.isEmpty(cpbh)) {
                                                errorMessage.append(countErrorIndex++ + "：任务单号为" + rwdh + "的产品编号为空！");
                                                haveErrors = true;
                                            } else {
                                                DynaBean bean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", "and PRODUCTDATA_BH='" + cpbh + "' and PRODUCTDATA_STATUS_CODE=1");
                                                if (bean != null) {
                                                    if (StringUtil.isNotEmpty(cpmc)) {
                                                        if (!cpmc.equals(bean.getStr("PRODUCTDATA_NAME"))) {
                                                            errorMessage.append(countErrorIndex++ + "：任务单号为" + rwdh + "的产品编号和产品名称不匹配！");
                                                            haveErrors = true;
                                                        }
                                                        newData.set("SCRW_CPGG", bean.getStr("PRODUCTDATA_GG"));//产品规格
                                                        newData.set("SCRW_CPXH", bean.getStr("PRODUCTDATA_XH"));//客户型号
                                                    } else {
                                                        newData.set("SCRW_NAME", bean.getStr("PRODUCTDATA_NAME"));//产品名称
                                                    }
                                                } else {
                                                    errorMessage.append(countErrorIndex++ + "：任务单号为" + rwdh + "的产品编号不存在或未启用！");
                                                    haveErrors = true;
                                                }
                                            }
                                            //产线编码+产线名称
                                            String cxbh = newData.getStr("SCRW_CXBM");
                                            String cxmc = newData.getStr("SCRW_CXMC");
                                            if (StringUtil.isEmpty(cxbh)) {
                                                errorMessage.append(countErrorIndex++ + "：任务单号为" + rwdh + "的产线编码为空！");
                                                haveErrors = true;
                                            } else {
                                                DynaBean bean = serviceTemplate.selectOne("JGMES_BASE_CXSJ", "and CXSJ_CXBM='" + cxbh + "' and CXSJ_STATUS_CODE=1");
                                                if (bean != null) {
                                                    if (StringUtil.isNotEmpty(cxmc)) {
                                                        if (!cxmc.equals(bean.getStr("CXSJ_CXMC"))) {
                                                            errorMessage.append(countErrorIndex++ + "：任务单号为" + rwdh + "的产线编号和产线名称不匹配！");
                                                            haveErrors = true;
                                                        }
                                                    } else {
                                                        newData.set("SCRW_CXMC", bean.get("CXSJ_CXMC"));//产品名称
                                                    }
                                                } else {
                                                    errorMessage.append(countErrorIndex++ + "：任务单号为" + rwdh + "的产线编码不存在或未启用！");
                                                    haveErrors = true;
                                                }
                                            }
                                            //客户编码+客户名称
                                            String khbm = newData.getStr("SCRW_KHBM");
                                            String khmc = newData.getStr("SCRW_KHMC");
                                            if (StringUtil.isNotEmpty(khbm)) {
                                                DynaBean bean = serviceTemplate.selectOne("JGMES_BASE_CUST", "and CUST_CODE='" + khbm + "'");
                                                if (bean != null) {
                                                    if (StringUtil.isNotEmpty(khmc)) {
                                                        if (!khmc.equals(bean.getStr("CUST_NAME"))) {
                                                            errorMessage.append(countErrorIndex++ + "：任务单号为" + rwdh + "的客户编号和客户名称不匹配！");
                                                            haveErrors = true;
                                                        }
                                                    } else {
                                                        newData.set("SCRW_CXMC", bean.get("CUST_NAME"));//客户名称
                                                    }

                                                } else {
                                                    errorMessage.append(countErrorIndex++ + "：任务单号为" + rwdh + "的客户编码不存在！");
                                                    haveErrors = true;
                                                }
                                            }
                                            //计划开工时间
                                            String jhkgsj = newData.getStr("SCRW_JHKGSJ");
                                            if (!pec.checkDate(jhkgsj, false)) {
                                                errorMessage.append(countErrorIndex++ + "：任务单号为" + rwdh + "的计划开工时间非法！");
                                                haveErrors = true;
                                            }
                                            //计划完工时间
                                            String jhwgsj = newData.getStr("SCRW_JHWGSJ");
                                            if (!pec.checkDate(jhwgsj, false)) {
                                                errorMessage.append(countErrorIndex++ + "：任务单号为" + rwdh + "的计划开工时间非法！");
                                                haveErrors = true;
                                            }
                                            //部门，DeptName
                                            String DeptName = newData.getStr("SCRW_DEPTNAME");
                                            if (StringUtil.isNotEmpty(DeptName)) {
                                                List<DynaBean> je_core_department_ = serviceTemplate.selectList("JE_CORE_DEPARTMENT ", "and DEPTNAME ='" + DeptName + "'");
                                                if (je_core_department_.size() > 0) {
                                                    newData.set("DEPTNAME", DeptName);
                                                    newData.set("DEPTCODE", je_core_department_.get(0).getStr("DEPTCODE"));
                                                } else {
                                                    errorMessage.append(countErrorIndex++ + "：任务单号为" + rwdh + "的部门不存在！");
                                                    haveErrors = true;
                                                }
                                            }
                                            //请购日期
                                            String qgrq = newData.getStr("SCRW_QGRQ");
                                            if (StringUtil.isNotEmpty(qgrq)) {
                                                if (!pec.checkDate(qgrq, false)) {
                                                    errorMessage.append(countErrorIndex++ + "：任务单号为" + rwdh + "的请购日期非法！");
                                                    haveErrors = true;
                                                }
                                            } else {
                                                errorMessage.append(countErrorIndex++ + "：任务单号为" + rwdh + "的请购日期不能为空！");
                                                haveErrors = true;
                                            }

                                            //OTD日期
                                            String otdRq = newData.getStr("SCRW_OTDRQ");
                                            if (StringUtil.isNotEmpty(otdRq)) {
                                                if (!pec.checkDate(otdRq, false)) {
                                                    errorMessage.append(countErrorIndex++ + "：任务单号为" + rwdh + "的OTD日期非法！");
                                                    haveErrors = true;
                                                }
                                            } else {
                                                errorMessage.append(countErrorIndex++ + "：任务单号为" + rwdh + "的OTD日期不能为空！");
                                                haveErrors = true;
                                            }
                                            //实际完工时间，SCRW_SJWGSJ
                                            String sjwgsl = newData.getStr("SCRW_SJWGSJ");
                                            if (StringUtil.isNotEmpty(sjwgsl)) {
                                                if (!pec.checkDate(sjwgsl, false)) {
                                                    errorMessage.append(countErrorIndex++ + "：任务单号为" + rwdh + "的实际完工时间非法！");
                                                    haveErrors = true;
                                                }
                                            }
                                            //实际开工时间
                                            String sjkgsl = newData.getStr("SCRW_SJKGSJ");
                                            if (StringUtil.isNotEmpty(sjkgsl)) {
                                                if (!pec.checkDate(sjkgsl, false)) {
                                                    errorMessage.append(countErrorIndex++ + "：任务单号为" + rwdh + "的实际开工时间非法！");
                                                    haveErrors = true;
                                                }
                                            }
                                            //生产样板
                                            String scyb = newData.getStr("SCRW_SCYB_CODE");
                                            if (StringUtil.isNotEmpty(scyb)) {
                                                if (scyb.equals("1")) {
                                                    newData.set("SCRW_SCYB_CODE", 1);
                                                    newData.set("SCRW_SCYB_NAME", "是");
                                                } else {
                                                    newData.set("SCRW_SCYB_CODE", 0);
                                                    newData.set("SCRW_SCYB_NAME", "否");
                                                }
                                            }
                                            //操作说明书
                                            String czsms = newData.getStr("SCRW_CZSMS_CODE");
                                            if (StringUtil.isNotEmpty(czsms)) {
                                                if (czsms.equals("1")) {
                                                    newData.set("SCRW_CZSMS_CODE", 1);
                                                    newData.set("SCRW_CZSMS_NAME", "是");
                                                } else {
                                                    newData.set("SCRW_CZSMS_CODE", 0);
                                                    newData.set("SCRW_CZSMS_NAME", "否");
                                                }
                                            }
                                            //设备状态
                                            String sbzt = newData.getStr("SCRW_SBZT_CODE");
                                            if (StringUtil.isNotEmpty(sbzt)) {
                                                if (sbzt.equals("1")) {
                                                    newData.set("SCRW_SBZT_CODE", 1);
                                                    newData.set("SCRW_SBZT_NAME", "是");
                                                } else {
                                                    newData.set("SCRW_SBZT_CODE", 0);
                                                    newData.set("SCRW_SBZT_NAME", "否");
                                                }
                                            }
                                        }
                                        //若任务状态为空，则默认
                                        String rwzt = newData.getStr("SCRW_RWZT_CODE");
                                        if (StringUtil.isEmpty(rwzt)) {
                                            newData.set("SCRW_RWZT_CODE", "RWZT01");
                                            newData.set("SCRW_RWZT_NAME", "待生产");
                                        }
                                        //校验完毕
                                        if (!haveErrors) {
                                            //更新SQL语句
                                            if (IsUpdate) {//此处为更新操作
                                                updateSt.setString(1, newData.getStr("SCRW_CPBH") == null ? oldDataBean.getStr("SCRW_CPBH") : newData.getStr("SCRW_CPBH"));
                                                updateSt.setString(2, newData.getStr("SCRW_NAME") == null ? oldDataBean.getStr("SCRW_NAME") : newData.getStr("SCRW_NAME"));
                                                updateSt.setString(3, newData.getStr("SCRW_CXBM") == null ? oldDataBean.getStr("SCRW_CXBM") : newData.getStr("SCRW_CXBM"));
                                                updateSt.setString(4, newData.getStr("SCRW_CXMC") == null ? oldDataBean.getStr("SCRW_CXMC") : newData.getStr("SCRW_CXMC"));
                                                updateSt.setString(5, newData.getStr("SCRW_JHKGSJ") == null ? oldDataBean.getStr("SCRW_JHKGSJ") : newData.getStr("SCRW_JHKGSJ"));
                                                updateSt.setString(6, newData.getStr("SCRW_JHWGSJ") == null ? oldDataBean.getStr("SCRW_JHWGSJ") : newData.getStr("SCRW_JHWGSJ"));
                                                updateSt.setString(7, newData.getStr("SCRW_QGRQ") == null ? oldDataBean.getStr("SCRW_QGRQ") : newData.getStr("SCRW_QGRQ"));
                                                updateSt.setString(8, newData.getStr("SCRW_OTDRQ") == null ? oldDataBean.getStr("SCRW_OTDRQ") : newData.getStr("SCRW_OTDRQ"));
                                                updateSt.setString(9, newData.getStr("SCRW_GDHM") == null ? oldDataBean.getStr("SCRW_GDHM") : newData.getStr("SCRW_GDHM"));
                                                updateSt.setString(10, newData.getStr("SCRW_WCSL") == null ? oldDataBean.getStr("SCRW_WCSL") : newData.getStr("SCRW_WCSL"));
                                                updateSt.setString(11, newData.getStr("SCRW_GDSL") == null ? oldDataBean.getStr("SCRW_GDSL") : newData.getStr("SCRW_GDSL"));
                                                updateSt.setString(12, newData.getStr("SCRW_DDHM") == null ? oldDataBean.getStr("SCRW_DDHM") : newData.getStr("SCRW_DDHM"));
                                                updateSt.setString(13, newData.getStr("SCRW_DDSL") == null ? oldDataBean.getStr("SCRW_DDSL") : newData.getStr("SCRW_DDSL"));
                                                updateSt.setString(14, newData.getStr("SCRW_KHBM") == null ? oldDataBean.getStr("SCRW_KHBM") : newData.getStr("SCRW_KHBM"));
                                                updateSt.setString(15, newData.getStr("SCRW_KHMC") == null ? oldDataBean.getStr("SCRW_KHMC") : newData.getStr("SCRW_KHMC"));
                                                updateSt.setString(16, newData.getStr("SCRW_BZ") == null ? oldDataBean.getStr("SCRW_BZ") : newData.getStr("SCRW_BZ"));
                                                updateSt.setString(17, newData.getStr("SCRW_DEPTNAME") == null ? oldDataBean.getStr("SCRW_DEPTNAME") : newData.getStr("SCRW_DEPTNAME"));
                                                updateSt.setString(18, newData.getStr("SCRW_YS") == null ? oldDataBean.getStr("SCRW_YS") : newData.getStr("SCRW_YS"));
                                                updateSt.setString(19, newData.getStr("SCRW_SCSL") == null ? oldDataBean.getStr("SCRW_SCSL") : newData.getStr("SCRW_SCSL"));
                                                updateSt.setString(20, newData.getStr("SCRW_SXSJ") == null ? oldDataBean.getStr("SCRW_SXSJ") : newData.getStr("SCRW_SXSJ"));
                                                updateSt.setString(21, newData.getStr("SCRW_DBSL") == null ? oldDataBean.getStr("SCRW_DBSL") : newData.getStr("SCRW_DBSL"));
                                                updateSt.setString(22, newData.getStr("SCRW_LPSL") == null ? oldDataBean.getStr("SCRW_LPSL") : newData.getStr("SCRW_LPSL"));
                                                updateSt.setString(23, newData.getStr("SCRW_BLSL") == null ? oldDataBean.getStr("SCRW_BLSL") : newData.getStr("SCRW_BLSL"));
                                                updateSt.setString(24, newData.getStr("SCRW_BFSL") == null ? oldDataBean.getStr("SCRW_BFSL") : newData.getStr("SCRW_BFSL"));
                                                updateSt.setString(25, newData.getStr("SCRW_SJWGSJ") == null ? oldDataBean.getStr("SCRW_SJWGSJ") : newData.getStr("SCRW_SJWGSJ"));
                                                updateSt.setString(26, newData.getStr("SCRW_SJKGSJ") == null ? oldDataBean.getStr("SCRW_SJKGSJ") : newData.getStr("SCRW_SJKGSJ"));
                                                updateSt.setString(27, newData.getStr("SCRW_LCKH") == null ? oldDataBean.getStr("SCRW_LCKH") : newData.getStr("SCRW_LCKH"));
                                                updateSt.setString(28, newData.getStr("SCRW_PGSL") == null ? oldDataBean.getStr("SCRW_PGSL") : newData.getStr("SCRW_PGSL"));
                                                updateSt.setString(29, newData.getStr("SCRW_LB") == null ? oldDataBean.getStr("SCRW_LB") : newData.getStr("SCRW_LB"));
                                                updateSt.setString(30, newData.getStr("SCRW_SX") == null ? oldDataBean.getStr("SCRW_SX") : newData.getStr("SCRW_SX"));
                                                updateSt.setString(31, newData.getStr("SCRW_BZNAME") == null ? oldDataBean.getStr("SCRW_BZNAME") : newData.getStr("SCRW_BZNAME"));
                                                updateSt.setString(32, newData.getStr("SCRW_ERPGX") == null ? oldDataBean.getStr("SCRW_ERPGX") : newData.getStr("SCRW_ERPGX"));
                                                updateSt.setString(33, newData.getStr("SCRW_SB") == null ? oldDataBean.getStr("SCRW_SB") : newData.getStr("SCRW_SB"));
                                                updateSt.setString(34, newData.getStr("SCRW_HM") == null ? oldDataBean.getStr("SCRW_HM") : newData.getStr("SCRW_HM"));
                                                updateSt.setString(35, newData.getStr("SCRW_RS") == null ? oldDataBean.getStr("SCRW_RS") : newData.getStr("SCRW_RS"));
                                                updateSt.setString(36, newData.getStr("SCRW_BZCN") == null ? oldDataBean.getStr("SCRW_BZCN") : newData.getStr("SCRW_BZCN"));
                                                updateSt.setString(37, newData.getStr("SCRW_ZBSC") == null ? oldDataBean.getStr("SCRW_ZBSC") : newData.getStr("SCRW_ZBSC"));
                                                updateSt.setString(38, newData.getStr("SCRW_ZBSJSC") == null ? oldDataBean.getStr("SCRW_ZBSJSC") : newData.getStr("SCRW_ZBSJSC"));
                                                updateSt.setString(39, newData.getStr("SCRW_ZBJHCL") == null ? oldDataBean.getStr("SCRW_ZBJHCL") : newData.getStr("SCRW_ZBJHCL"));
                                                updateSt.setString(40, newData.getStr("SCRW_ZBSJCL") == null ? oldDataBean.getStr("SCRW_ZBSJCL") : newData.getStr("SCRW_ZBSJCL"));
                                                updateSt.setString(41, newData.getStr("SCRW_JBCN") == null ? oldDataBean.getStr("SCRW_JBCN") : newData.getStr("SCRW_JBCN"));
                                                updateSt.setString(42, newData.getStr("SCRW_JBSC") == null ? oldDataBean.getStr("SCRW_JBSC") : newData.getStr("SCRW_JBSC"));
                                                updateSt.setString(43, newData.getStr("SCRW_JBSJSC") == null ? oldDataBean.getStr("SCRW_JBSJSC") : newData.getStr("SCRW_JBSJSC"));
                                                updateSt.setString(44, newData.getStr("SCRW_JBJHCL") == null ? oldDataBean.getStr("SCRW_JBJHCL") : newData.getStr("SCRW_JBJHCL"));
                                                updateSt.setString(45, newData.getStr("SCRW_JBSJCL") == null ? oldDataBean.getStr("SCRW_JBSJCL") : newData.getStr("SCRW_JBSJCL"));
                                                updateSt.setString(46, newData.getStr("SCRW_SGZF") == null ? oldDataBean.getStr("SCRW_SGZF") : newData.getStr("SCRW_SGZF"));
                                                updateSt.setString(47, newData.getStr("SCRW_BCSCMB") == null ? oldDataBean.getStr("SCRW_BCSCMB") : newData.getStr("SCRW_BCSCMB"));
                                                updateSt.setString(48, newData.getStr("SCRW_MJBH") == null ? oldDataBean.getStr("SCRW_MJBH") : newData.getStr("SCRW_MJBH"));
                                                updateSt.setString(49, newData.getStr("SCRW_MJMC") == null ? oldDataBean.getStr("SCRW_MJMC") : newData.getStr("SCRW_MJMC"));
                                                updateSt.setString(50, newData.getStr("SCRW_SPM") == null ? oldDataBean.getStr("SCRW_SPM") : newData.getStr("SCRW_SPM"));
                                                updateSt.setString(51, newData.getStr("SCRW_PS") == null ? oldDataBean.getStr("SCRW_PS") : newData.getStr("SCRW_PS"));
                                                updateSt.setString(52, newData.getStr("SCRW_LS") == null ? oldDataBean.getStr("SCRW_LS") : newData.getStr("SCRW_LS"));
                                                updateSt.setString(53, newData.getStr("SCRW_SCYB_CODE ") == null ? oldDataBean.getStr("SCRW_SCYB_CODE") : newData.getStr("SCRW_SCYB_CODE"));
                                                updateSt.setString(54, newData.getStr("SCRW_SCYB_NAME ") == null ? oldDataBean.getStr("SCRW_SCYB_NAME") : newData.getStr("SCRW_SCYB_NAME"));
                                                updateSt.setString(55, newData.getStr("SCRW_CZSMS_CODE") == null ? oldDataBean.getStr("SCRW_CZSMS_CODE") : newData.getStr("SCRW_CZSMS_CODE"));
                                                updateSt.setString(56, newData.getStr("SCRW_CZSMS_NAME") == null ? oldDataBean.getStr("SCRW_CZSMS_NAME") : newData.getStr("SCRW_CZSMS_NAME"));
                                                updateSt.setString(57, newData.getStr("SCRW_SBZT_CODE ") == null ? oldDataBean.getStr("SCRW_SBZT_CODE") : newData.getStr("SCRW_SBZT_CODE"));
                                                updateSt.setString(58, newData.getStr("SCRW_SBZT_NAME ") == null ? oldDataBean.getStr("SCRW_SBZT_NAME") : newData.getStr("SCRW_SBZT_NAME"));
                                                updateSt.setString(59, newData.getStr("SCRW_CPGG") == null ? oldDataBean.getStr("SCRW_CPGG") : newData.getStr("SCRW_CPGG"));
                                                updateSt.setString(60, newData.getStr("SCRW_XH") == null ? oldDataBean.getStr("SCRW_XH") : newData.getStr("SCRW_XH"));
                                                updateSt.setString(61, newData.getStr("SCRW_PCSL") == null ? oldDataBean.getStr("SCRW_PCSL") : newData.getStr("SCRW_PCSL"));
                                                updateSt.setString(62, newData.getStr("SCRW_RWZT_CODE") == null ? oldDataBean.getStr("SCRW_RWZT_CODE") : newData.getStr("SCRW_RWZT_CODE"));
                                                updateSt.setString(63, newData.getStr("SCRW_RWZT_NAME") == null ? oldDataBean.getStr("SCRW_RWZT_NAME") : newData.getStr("SCRW_RWZT_NAME"));
                                                updateSt.setString(64, newData.getStr("SCRW_RWDH") == null ? oldDataBean.getStr("SCRW_RWDH") : newData.getStr("SCRW_RWDH"));
                                                updateSt.addBatch();
                                                trueUpdate++;
                                            } else {    //此处为插入操作
                                                insertSt.setString(1, "ERP");
                                                insertSt.setString(2, newData.getStr("SCRW_CPBH"));
                                                insertSt.setString(3, newData.getStr("SCRW_NAME"));
                                                insertSt.setString(4, newData.getStr("SCRW_CXBM"));
                                                insertSt.setString(5, newData.getStr("SCRW_CXMC"));
                                                insertSt.setString(6, newData.getStr("SCRW_JHKGSJ"));
                                                insertSt.setString(7, newData.getStr("SCRW_JHWGSJ"));
                                                insertSt.setString(8, newData.getStr("SCRW_QGRQ"));
                                                insertSt.setString(9, newData.getStr("SCRW_OTDRQ"));
                                                insertSt.setString(10, newData.getStr("SCRW_GDHM"));
                                                insertSt.setString(11, newData.getStr("SCRW_WCSL"));
                                                insertSt.setInt(12, newData.getInt("SCRW_GDSL"));
                                                insertSt.setString(13, newData.getStr("SCRW_DDHM"));
                                                insertSt.setInt(14, newData.getInt("SCRW_DDSL"));
                                                insertSt.setString(15, newData.getStr("SCRW_KHBM"));
                                                insertSt.setString(16, newData.getStr("SCRW_KHMC"));
                                                insertSt.setString(17, newData.getStr("SCRW_BZ"));
                                                insertSt.setString(18, newData.getStr("SCRW_DEPTNAME"));
                                                insertSt.setString(19, newData.getStr("SCRW_YS"));
                                                insertSt.setString(20, newData.getStr("SCRW_SCSL"));
                                                insertSt.setString(21, newData.getStr("SCRW_SXSJ"));
                                                insertSt.setString(22, newData.getStr("SCRW_DBSL"));
                                                insertSt.setString(23, newData.getStr("SCRW_LPSL"));
                                                insertSt.setString(24, newData.getStr("SCRW_BLSL"));
                                                insertSt.setString(25, newData.getStr("SCRW_BFSL"));
                                                insertSt.setString(26, newData.getStr("SCRW_SJWGSJ"));
                                                insertSt.setString(27, newData.getStr("SCRW_SJKGSJ"));
                                                insertSt.setString(28, newData.getStr("SCRW_LCKH"));
                                                insertSt.setInt(29, newData.getInt("SCRW_PGSL"));
                                                insertSt.setString(30, newData.getStr("SCRW_LB"));
                                                insertSt.setString(31, newData.getStr("SCRW_SX"));
                                                insertSt.setString(32, newData.getStr("SCRW_BZNAME"));
                                                insertSt.setString(33, newData.getStr("SCRW_ERPGX"));
                                                insertSt.setString(34, newData.getStr("SCRW_SB"));
                                                insertSt.setString(35, newData.getStr("SCRW_HM"));
                                                insertSt.setString(36, newData.getStr("SCRW_RS"));
                                                insertSt.setInt(37, newData.getInt("SCRW_BZCN"));
                                                insertSt.setString(38, newData.getStr("SCRW_ZBSC"));
                                                insertSt.setString(39, newData.getStr("SCRW_ZBSJSC"));
                                                insertSt.setString(40, newData.getStr("SCRW_ZBJHCL"));
                                                insertSt.setString(41, newData.getStr("SCRW_ZBSJCL"));
                                                insertSt.setString(42, newData.getStr("SCRW_JBCN"));
                                                insertSt.setString(43, newData.getStr("SCRW_JBSC"));
                                                insertSt.setString(44, newData.getStr("SCRW_JBSJSC"));
                                                insertSt.setString(45, newData.getStr("SCRW_JBJHCL"));
                                                insertSt.setString(46, newData.getStr("SCRW_JBSJCL"));
                                                insertSt.setString(47, newData.getStr("SCRW_SGZF"));
                                                insertSt.setString(48, newData.getStr("SCRW_BCSCMB"));
                                                insertSt.setString(49, newData.getStr("SCRW_MJBH"));
                                                insertSt.setString(50, newData.getStr("SCRW_MJMC"));
                                                insertSt.setString(51, newData.getStr("SCRW_SPM"));
                                                insertSt.setString(52, newData.getStr("SCRW_PS"));
                                                insertSt.setString(53, newData.getStr("SCRW_LS"));
                                                insertSt.setString(54, newData.getStr("SCRW_SCYB_CODE"));
                                                insertSt.setString(55, newData.getStr("SCRW_SCYB_NAME"));
                                                insertSt.setString(56, newData.getStr("SCRW_CZSMS_CODE"));
                                                insertSt.setString(57, newData.getStr("SCRW_CZSMS_NAME"));
                                                insertSt.setString(58, newData.getStr("SCRW_SBZT_CODE"));
                                                insertSt.setString(59, newData.getStr("SCRW_SBZT_NAME"));
                                                insertSt.setString(60, newData.getStr("SCRW_RWDH"));
                                                insertSt.setString(61, UUID.randomUUID().toString().replaceAll("-", "").substring(0, 19));
                                                insertSt.setString(62, newData.getStr("SCRW_CPGG"));
                                                insertSt.setString(63, newData.getStr("SCRW_XH"));
                                                insertSt.setString(64, newData.getStr("SCRW_PCSL"));
                                                insertSt.setString(65, newData.getStr("SCRW_RWZT_CODE"));
                                                insertSt.setString(66, newData.getStr("SCRW_RWZT_NAME"));
                                                insertSt.addBatch();
                                                trueImport++;
                                            }
                                            if (i % 1000 == 0) {    //1000条处理一次
                                                if (updateSt != null) {
                                                    updateSt.executeBatch();
                                                    updateSt.clearBatch();
                                                }
                                                if (insertSt != null) {
                                                    insertSt.executeBatch();
                                                    insertSt.clearBatch();
                                                }
                                                conn.commit();

                                            }
                                        } else {
                                            falseImport++;
                                        }
                                    } else {
                                        errorMessage.append("导入失败，原因：生产任务单号为" + hashMap.get("errMsg"));
                                        falseImport++;
                                    }
                                    i++;
                                }
                                if (updateSt != null) {
                                    updateSt.executeBatch();
                                    updateSt.clearBatch();
                                }
                                if (insertSt != null) {
                                    insertSt.executeBatch();
                                    insertSt.clearBatch();
                                }
                                conn.commit();
                            } catch (Exception e) {
                                e.printStackTrace();
                                errorMessage.append(countErrorIndex++ + "：异常错误");

                            } finally {
                                if (rs != null) {
                                    try {
                                        rs.close();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (insertSt != null) {
                                    try {
                                        insertSt.close();
                                    } catch (SQLException e) {

                                        e.printStackTrace();
                                    }
                                }
                                if (updateSt != null) {
                                    try {
                                        updateSt.close();
                                    } catch (SQLException e) {

                                        e.printStackTrace();
                                    }
                                }
                                if (conn != null) {
                                    try {
                                        conn.close();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                                res.add(trueImport);
                                res.add(trueUpdate);
                                res.add(falseImport);

                            }
                            latch.countDown();
                        }
                    });
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("主线程执行");
                    if (!"".equals(errorMessage.toString()) && errorMessage.toString() != null) {
                        ret.setMessage("成功导入" + res.get(0) + "条数据,更新了" + res.get(1) + "条数据，操作失败" + res.get(2) + "条，错误原因是：" + errorMessage.toString());
                    } else {
                        ret.setMessage("成功导入" + res.get(0) + "条数据,更新了" + res.get(1) + "条数据!");
                        ret.IsSuccess = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonBuilder.toJson(ret);
    }

    /**
     * 根据产线编码或者产线名称查找产线信息
     *
     * @param ProductionLineCode
     * @param ProductionLineName
     * @return
     */
    @Override
    public String GetProductionLineData(String ProductionLineCode, String ProductionLineName) {
        if (serviceTemplate == null) {
            serviceTemplate = SpringContextHolder.getBean("PCDynaServiceTemplate");
        }
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();

        try {
            String whereSql = "";
            //产线编码
            if (StringUtil.isNotEmpty(ProductionLineCode)) {
                if (ProductionLineCode.indexOf(",") != -1) {
                    whereSql += " and CXSJ_CXBM in ('" + ProductionLineCode.replaceAll(",", "','") + "')";
                } else {
                    whereSql += " and CXSJ_CXBM = '" + ProductionLineCode + "'";
                }
            }
            //产线名称
            if (StringUtil.isNotEmpty(ProductionLineName)) {
                if (ProductionLineName.indexOf(",") != -1) {
                    whereSql += " and CXSJ_CXMC in ('" + ProductionLineName.replaceAll(",", "','") + "')";
                } else {
                    whereSql += " and CXSJ_CXMC like '%" + ProductionLineName + "%'";
                }
            }
            List<DynaBean> bean = serviceTemplate.selectList("JGMES_BASE_CXSJ", whereSql);
            ret.Data = ret.getValues(bean);
        } catch (Exception e) {
            e.printStackTrace();
            ret.setMessage("异常错误");
        }
        return jsonBuilder.toJson(ret);
    }

    /**
     * 传入多条数据的Json字符串值以更新或新增产线信息
     *
     * @param jsonList
     * @return
     */
    @Override
    public String UpdateOrAddProductionLineData(String jsonList) {
        if (serviceTemplate == null) {
            serviceTemplate = SpringContextHolder.getBean("PCDynaServiceTemplate");
        }
        Boolean haveError = false;
        StringBuilder errorMessage = new StringBuilder(2000);//错误信息记录
        HttpServletRequest request = null;
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        if (StringUtil.isNotEmpty(jsonList)) {
            List<DynaBean> list = new Vector<>();
            //转换成List数组
            try {
                String key = "";
                String value = "";
                net.sf.json.JSONArray ja1 = net.sf.json.JSONArray.fromObject(jsonList);
                JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, "");
                if (ja1.size() > 0) {
                    for (int i = 0; i < ja1.size(); i++) {
                        DynaBean bean = new DynaBean();
                        bean.setStr(beanUtils.KEY_TABLE_CODE, "JGMES_BASE_CXSJ");
                        JSONObject jb1 = ja1.getJSONObject(i);
                        Iterator it = jb1.keys();
                        while (it.hasNext()) {
                            key = (String) it.next();
                            value = jb1.getString(key);
                            bean.setStr(key, value);
                        }
                        list.add(bean);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage.append("数据转换失败！");
                haveError = true;
            }
            if (!haveError) {
                //开启线程
                ExecutorService pool = Executors.newFixedThreadPool(10);
                CountDownLatch latch = new CountDownLatch(1);
                List<Integer> res = new Vector<>();
                try {
                    pool.submit(new Runnable() {
                        @Override
                        public void run() {
                            //开启JDBC批数据处理
                            Integer countErrorIndex = 1;
                            Integer trueImport = 0;
                            Integer trueUpdate = 0;
                            Integer falseImport = 0;
                            Connection conn = null;
                            PreparedStatement updateSt = null;
                            PreparedStatement insertSt = null;
                            ResultSet rs = null;
                            String updateSql = "update JGMES_BASE_CXSJ set CXSJ_CXMC=?,CXSJ_STATUS_CODE=?,CXSJ_STATUS_NAME=?,CXSJ_SZCJ=?,CXSJ_DZ=?,CXSJ_FZR=?,CXSJ_PHONE=? where CXSJ_CXBM=?";
                            String insertSql = "insert into JGMES_BASE_CXSJ (CXSJ_CXMC,CXSJ_STATUS_CODE,CXSJ_STATUS_NAME,CXSJ_SZCJ,CXSJ_DZ,CXSJ_FZR,CXSJ_PHONE,CXSJ_CXBM,JGMES_BASE_CXSJ_ID)\n" +
                                    "Values(?,?,?,?,?,?,?,?,?)";
                            try {
                                Properties prop = new Properties();
                                InputStream inputStream = LoadProperties.class.getClassLoader().getResourceAsStream("jdbc.properties");
                                prop.load(inputStream);
                                String driverClass = prop.getProperty("jdbc.Driver");
                                String url = prop.getProperty("jdbc.url");
                                String username = prop.getProperty("jdbc.username");
                                String password = prop.getProperty("jdbc.password");
                                conn = DriverManager.getConnection(url, username, password);
                                updateSt = conn.prepareStatement(updateSql);
                                insertSt = conn.prepareStatement(insertSql);
                                conn.setAutoCommit(false);
                                Integer i = 1;
                                for (DynaBean dynaBean : list) {
                                    //数据处理
                                    DynaBean clone = dynaBean.clone();
                                    DynaBean newData = dynaBean.clone();
                                    Hashtable hashMap = checkValuesLen(clone, "CXSJ_CXBM");
                                    Boolean IsUpdate = false;
                                    DynaBean oldDataBean = new DynaBean();
                                    if ((Boolean) hashMap.get("IsSussess")) {
                                        //数据库校验长度成功，特别字段校验
                                        Boolean haveErrors = false;
                                        //产线编号，不能为空
                                        String cxbh = newData.getStr("CXSJ_CXBM");
                                        if (StringUtil.isEmpty(cxbh)) {
                                            errorMessage.append(countErrorIndex++ + "：第" + i + "条数据导入失败，原因：产线编码为空！");
                                            haveErrors = true;
                                        } else {
                                            oldDataBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ", "and CXSJ_CXBM='" + cxbh + "'");
                                            if (oldDataBean != null) {
                                                newData.set("JGMES_BASE_CXSJ_ID", oldDataBean.getStr("JGMES_BASE_CXSJ_ID"));
                                                IsUpdate = true;
                                            }
                                        }
                                        //产线名称
                                        String CXSJ_CXMC = newData.getStr("CXSJ_CXMC");
                                        if(StringUtil.isEmpty(CXSJ_CXMC)){
                                            errorMessage.append(countErrorIndex++ + "：产线编号为" + cxbh + "的产线名称不能为空！");
                                            haveErrors = true;
                                        }else{
                                            String IsCXMCStr = newData.getStr("IsCXMC");
                                            Integer IsCXMC = StringUtil.isEmpty(IsCXMCStr)||!IsCXMCStr.equals("0") ? 1 : 0;
                                            if (IsCXMC == 0) {
                                                List<DynaBean> jgmes_base_cxsj = serviceTemplate.selectList("JGMES_BASE_CXSJ", "and CXSJ_CXMC='" + CXSJ_CXMC + "' and CXSJ_CXBM!='" + cxbh + "'");
                                                if (jgmes_base_cxsj.size()>0){
                                                    errorMessage.append(countErrorIndex++ + "：产线编号为" + cxbh + "的产线名称已存在，不允许重复 ！");
                                                    haveErrors = true;
                                                }
                                            }
                                        }
                                        //状态
                                        String STATUS = newData.getStr("CXSJ_STATUS_CODE");
                                        if (StringUtil.isEmpty(STATUS)) {
                                            errorMessage.append(countErrorIndex++ + "：产线编号为" + cxbh + "的原因：状态值为空！");
                                            haveErrors = true;
                                        } else {
                                            if (STATUS.equals("1")) {
                                                newData.set("CXSJ_STATUS_CODE", 1);
                                                newData.set("CXSJ_STATUS_NAME", "启用");
                                            } else {
                                                newData.set("CXSJ_STATUS_CODE", 2);
                                                newData.set("CXSJ_STATUS_NAME", "不启用");
                                            }
                                        }
                                        //所在车间
                                        String szcj = newData.getStr("CXSJ_SZCJ");
                                        if (StringUtil.isNotEmpty(szcj)) {
                                            List<DynaBean> je_core_department = serviceTemplate.selectList("JE_CORE_DEPARTMENT", "and rankcoDE='CEJIN' and DEPTNAME ='" + szcj + "'");
                                            if (je_core_department.size() == 0) {
                                                errorMessage.append(countErrorIndex++ + "：产线编号为" + cxbh + "的原因：所在车间不存在！");
                                                haveErrors = true;
                                            }
                                        }
                                        //负责人
                                        String fzr = newData.getStr("CXSJ_FZR");
                                        if (StringUtil.isNotEmpty(fzr)) {
                                            List<DynaBean> je_core_enduser = serviceTemplate.selectList("JE_CORE_ENDUSER", "and usercode='" + fzr + "'");
                                            if (je_core_enduser.size() == 0) {
                                                errorMessage.append(countErrorIndex++ + "：产线编号为" + cxbh + "的原因：负责人在人员表中不存在！");
                                                haveErrors = true;
                                            }
                                        }
                                        //联系电话
                                        String phone = newData.getStr("CXSJ_PHONE");
                                        if (StringUtil.isNotEmpty(phone)) {
                                            if (!pec.checkPhone(phone)) {
                                                errorMessage.append(countErrorIndex++ + "：产线编号为" + cxbh + "的原因：联系电话非法！");
                                                haveErrors = true;
                                            }
                                        }

                                        //校验完毕
                                        if (!haveErrors) {
                                            //更新SQL语句
                                            if (IsUpdate) {//此处为更新操作
                                                updateSt.setString(1, newData.getStr("CXSJ_CXMC") == null ? oldDataBean.getStr("CXSJ_CXMC") : newData.getStr("CXSJ_CXMC"));
                                                updateSt.setString(2, newData.getStr("CXSJ_STATUS_CODE") == null ? oldDataBean.getStr("CXSJ_STATUS_CODE") : newData.getStr("CXSJ_STATUS_CODE"));
                                                updateSt.setString(3, newData.getStr("CXSJ_STATUS_NAME") == null ? oldDataBean.getStr("CXSJ_STATUS_NAME") : newData.getStr("CXSJ_STATUS_NAME"));
                                                updateSt.setString(4, newData.getStr("CXSJ_SZCJ") == null ? oldDataBean.getStr("CXSJ_SZCJ") : newData.getStr("CXSJ_SZCJ"));
                                                updateSt.setString(5, newData.getStr("CXSJ_DZ") == null ? oldDataBean.getStr("CXSJ_DZ") : newData.getStr("CXSJ_DZ"));
                                                updateSt.setString(6, newData.getStr("CXSJ_FZR") == null ? oldDataBean.getStr("CXSJ_FZR") : newData.getStr("CXSJ_FZR"));
                                                updateSt.setString(7, newData.getStr("CXSJ_PHONE") == null ? oldDataBean.getStr("CXSJ_PHONE") : newData.getStr("CXSJ_PHONE"));
                                                updateSt.setString(8, newData.getStr("CXSJ_CXBM") == null ? oldDataBean.getStr("CXSJ_CXBM") : newData.getStr("CXSJ_CXBM"));
                                                updateSt.addBatch();
                                                trueUpdate++;
                                            } else {    //此处为插入操作
                                                insertSt.setString(1, newData.getStr("CXSJ_CXMC"));
                                                insertSt.setInt(2, newData.getInt("CXSJ_STATUS_CODE"));
                                                insertSt.setString(3, newData.getStr("CXSJ_STATUS_NAME"));
                                                insertSt.setString(4, newData.getStr("CXSJ_SZCJ"));
                                                insertSt.setString(5, newData.getStr("CXSJ_DZ"));
                                                insertSt.setString(6, newData.getStr("CXSJ_FZR"));
                                                insertSt.setString(7, newData.getStr("CXSJ_PHONE"));
                                                insertSt.setString(8, newData.getStr("CXSJ_CXBM"));
                                                insertSt.setString(9, UUID.randomUUID().toString().replaceAll("-", "").substring(0, 19));
                                                insertSt.addBatch();
                                                trueImport++;
                                            }
                                            if (i % 1000 == 0) {    //1000条处理一次
                                                if (updateSt != null) {
                                                    updateSt.executeBatch();
                                                    updateSt.clearBatch();
                                                }
                                                if (insertSt != null) {
                                                    insertSt.executeBatch();
                                                    insertSt.clearBatch();
                                                }
                                                conn.commit();
                                            }
                                        } else {
                                            falseImport++;
                                        }
                                    } else {
                                        errorMessage.append("产线编码为" + hashMap.get("errMsg"));
                                        falseImport++;
                                    }
                                    i++;
                                }
                                if (updateSt != null) {
                                    updateSt.executeBatch();
                                    updateSt.clearBatch();
                                }
                                if (insertSt != null) {
                                    insertSt.executeBatch();
                                    insertSt.clearBatch();
                                }
                                conn.commit();
                            } catch (Exception e) {
                                e.printStackTrace();
                                errorMessage.append(countErrorIndex++ + "：异常错误");
                            } finally {
                                if (rs != null) {
                                    try {
                                        rs.close();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (updateSt != null) {
                                    try {
                                        updateSt.close();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (insertSt != null) {
                                    try {
                                        insertSt.close();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (conn != null) {
                                    try {
                                        conn.close();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                                res.add(trueImport);
                                res.add(trueUpdate);
                                res.add(falseImport);
                            }
                            latch.countDown();
                        }
                    });
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!"".equals(errorMessage.toString()) && errorMessage.toString() != null) {
                        ret.setMessage("成功导入" + res.get(0) + "条数据,更新了" + res.get(1) + "条数据，操作失败" + res.get(2) + "条，错误原因是：" + errorMessage.toString());
                    } else {
                        ret.setMessage("成功导入" + res.get(0) + "条数据,更新了" + res.get(1) + "条数据!");
                        ret.IsSuccess = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonBuilder.toJson(ret);
    }

    @Override
    public String GetDepartmentData(String DeptCode, String deptName, String RankName, String ChargeUserName) {
        if (serviceTemplate == null) {
            serviceTemplate = SpringContextHolder.getBean("PCDynaServiceTemplate");
        }
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();

        try {
            String whereSql = "";
            //部门编号
            if (StringUtil.isNotEmpty(DeptCode)) {
                if (DeptCode.indexOf(",") != -1) {
                    whereSql += " and DEPTCODE in ('" + DeptCode.replaceAll(",", "','") + "')";
                } else {
                    whereSql += " and DEPTCODE = '" + DeptCode + "'";
                }
            }
            //部门名称
            if (StringUtil.isNotEmpty(deptName)) {
                if (deptName.indexOf(",") != -1) {
                    whereSql += " and DEPTNAME in ('" + deptName.replaceAll(",", "','") + "')";
                } else {
                    whereSql += " and DEPTNAME like '%" + deptName + "%'";
                }
            }
            //级别名称
            if (StringUtil.isNotEmpty(RankName)) {
                if (RankName.indexOf(",") != -1) {
                    whereSql += " and RANKNAME in ('" + RankName.replaceAll(",", "','") + "')";
                } else {
                    whereSql += " and RANKNAME like '%" + RankName + "%'";
                }
            }
            //负责人
            if (StringUtil.isNotEmpty(ChargeUserName)) {
                if (ChargeUserName.indexOf(",") != -1) {
                    whereSql += " and CHARGEUSERNAME in ('" + ChargeUserName.replaceAll(",", "','") + "')";
                } else {
                    whereSql += " and CHARGEUSERNAME like '%" + ChargeUserName + "%'";
                }
            }

            List<DynaBean> jgmes_base_cust = serviceTemplate.selectList("JE_CORE_DEPARTMENT", whereSql);
            ret.Data = ret.getValues(jgmes_base_cust);
        } catch (Exception e) {
            e.printStackTrace();
            ret.setMessage("异常错误");
        }
        return jsonBuilder.toJson(ret);
    }

    /**
     * 传入多条数据的Json字符串值以更新或新增部门信息
     *
     * @param jsonList
     * @return
     */
    @Override
    public String UpdateOrAddDepartmentData(String jsonList) {
        if (serviceTemplate == null) {
            serviceTemplate = SpringContextHolder.getBean("PCDynaServiceTemplate");
        }
        Boolean haveError = false;
        StringBuffer errorMessage = new StringBuffer(2000);//错误信息记录
        HttpServletRequest request = null;
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        if (StringUtil.isNotEmpty(jsonList)) {
            List<DynaBean> list = new Vector<>();
            //转换成List数组
            try {
                String key = "";
                String value = "";
                net.sf.json.JSONArray ja1 = net.sf.json.JSONArray.fromObject(jsonList);
                JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, "");
                if (ja1.size() > 0) {
                    for (int i = 0; i < ja1.size(); i++) {
                        DynaBean bean = new DynaBean();
                        bean.setStr(beanUtils.KEY_TABLE_CODE, "JE_CORE_DEPARTMENT");
                        JSONObject jb1 = ja1.getJSONObject(i);  // 遍历 jsonarray 数组，把每一个对象转成 json 对象
                        Iterator it = jb1.keys();
                        while (it.hasNext()) {
                            key = (String) it.next();
                            value = jb1.getString(key);
                            bean.setStr(key, value);
                        }
                        list.add(bean);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage.append("数据转换失败！");
                haveError = true;
            }
            if (!haveError) {
                //开启线程
                ExecutorService pool = Executors.newFixedThreadPool(10);
                CountDownLatch latch = new CountDownLatch(1);
                List<Integer> res = new Vector<>();
                try {
                    pool.submit(new Runnable() {
                        @Override
                        public void run() {
                            //开启JDBC批数据处理
                            Integer countErrorIndex = 1;
                            Integer trueImport = 0;
                            Integer trueUpdate = 0;
                            Integer falseImport = 0;
                            Connection conn = null;
                            PreparedStatement updateSt = null;
                            PreparedStatement insertSt = null;
                            PreparedStatement insertRoleSt = null;
                            ResultSet rs = null;
                            String updateSql = "update JE_CORE_DEPARTMENT set DEPTNAME=?,RANKNAME=?,RANKCODE=?,CHARGEUSER=?,CHARGEUSERNAME=?,PHONE=?,ADDRESS=?,ZNMS=?,PATH=?,PARENT=?,PARENTNAME=?,PARENTCODE=?,PARENTPATH=?,NODETYPE=?,TREEORDERINDEX=?" +
                                    "where DEPTCODE=? ";
                            String insertSql = "insert into JE_CORE_DEPARTMENT(DEPTNAME,RANKNAME,RANKCODE,CHARGEUSER,CHARGEUSERNAME,PHONE,ADDRESS,ZNMS,PATH,PARENT,PARENTNAME,PARENTCODE,PARENTPATH,DEPTCODE,DEPTID,STATUS,NODETYPE,TREEORDERINDEX) " +
                                    "Values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

                            String insertRoleSql = "INSERT INTO JE_CORE_ROLE(PATH,STATUS,PARENT,NODETYPE,ROLECODE,PARENTPATH,TREEORDERINDEX,PARENTCODE,ROLENAME,PARENTNAME,DEPTID,ROLETYPE,ROLERANK,ZHMC,ROLEID,LAYER,DEVELOP)" +
                                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                            try {
                                Properties prop = new Properties();
                                InputStream inputStream = LoadProperties.class.getClassLoader().getResourceAsStream("jdbc.properties");
                                prop.load(inputStream);
                                String driverClass = prop.getProperty("jdbc.Driver");
                                String url = prop.getProperty("jdbc.url");
                                String username = prop.getProperty("jdbc.username");
                                String password = prop.getProperty("jdbc.password");
                                conn = DriverManager.getConnection(url, username, password);
                                updateSt = conn.prepareStatement(updateSql);
                                insertSt = conn.prepareStatement(insertSql);
                                insertRoleSt = conn.prepareStatement(insertRoleSql);
                                conn.setAutoCommit(false);
                                DynaBean oldDataBean = new DynaBean();
                                Integer i = 1;
                                for (DynaBean dynaBean : list) {
                                    //数据处理
                                    DynaBean clone = dynaBean.clone();
                                    DynaBean newData = dynaBean.clone();
                                    Hashtable hashMap = checkValuesLen(clone, "DEPTCODE");
                                    Boolean IsUpdate = false;
                                    if ((Boolean) hashMap.get("IsSussess")) {
                                        //数据库校验长度成功，特别字段校验
                                        Boolean haveErrors = false;
                                        //部门编号，不能为空
                                        String deptCode = newData.getStr("DEPTCODE");
                                        System.out.println("deptCode" + deptCode);
                                        if (StringUtil.isEmpty(deptCode)) {
                                            errorMessage.append(countErrorIndex++ + "：第" + i + "条数据导入失败，原因：部门编码为空！");
                                            haveErrors = true;
                                        } else {
                                            oldDataBean = serviceTemplate.selectOne("JE_CORE_DEPARTMENT", "and DEPTCODE='" + deptCode + "'");
                                            if (oldDataBean != null) {
                                                IsUpdate = true;
                                            }
                                        }
                                        //部门名称
                                        String deptName = newData.getStr("DEPTNAME");
                                        if (StringUtil.isEmpty(deptName)) {
                                            errorMessage.append(countErrorIndex++ + "部门编号为" + deptCode + "的部门名称不能为空！");
                                            haveErrors = true;
                                        }else{
                                            String IsDEPTNAMEStr = newData.getStr("IsDEPTNAME");
                                            Integer IsDEPTNAME = StringUtil.isEmpty(IsDEPTNAMEStr)||!IsDEPTNAMEStr.equals("0") ? 1 : 0;
                                            if (IsDEPTNAME == 0) {
                                                List<DynaBean> je_core_department = serviceTemplate.selectList("JE_CORE_DEPARTMENT", "and DEPTNAME='" + deptName + "' and DEPTCODE!='" + deptCode + "'");
                                                if (je_core_department.size()>0){
                                                    errorMessage.append(countErrorIndex++ + "部门编号为" + deptCode + "的部门名称已存在，不允许重复！");
                                                    haveErrors = true;
                                                }
                                            }
                                        }
                                        //级别
                                        String jb = newData.getStr("RANKNAME");
                                        if (StringUtil.isNotEmpty(jb)) {
                                            //获取字典
                                            DynaBean dicItem = serviceTemplate.selectOne("je_core_dictionary", " AND DICTIONARY_DDCODE='JE_GSJB'");
                                            System.out.println(dicItem);
                                            if (dicItem != null) {
                                                List<DynaBean> dynaBeans = serviceTemplate.selectList("je_core_dictionaryitem", "and dictionaryitem_dictionary_id='" + dicItem.getStr("JE_CORE_DICTIONARY_ID") + "'");
                                                if (dynaBeans.size() > 0) {
                                                    Integer count = 0;
                                                    for (DynaBean bean : dynaBeans) {
                                                        if (jb.equals(bean.getStr("DICTIONARYITEM_ITEMNAME"))) {
                                                            newData.set("RANKNAME", bean.getStr("DICTIONARYITEM_ITEMNAME"));
                                                            newData.set("RANKCODE", bean.getStr("DICTIONARYITEM_ITEMCODE"));
                                                            count++;
                                                            break;
                                                        }
                                                    }
                                                    if (count == 0) {
                                                        errorMessage.append(countErrorIndex++ + "部门编号为" + deptCode + "的级别字段值不存在 ！");
                                                        haveErrors = true;
                                                    }
                                                } else {
                                                    errorMessage.append(countErrorIndex++ + "获取级别字典失败或字典为空！");
                                                    haveErrors = true;
                                                }

                                            } else {
                                                errorMessage.append(countErrorIndex++ + "获取级别字典失败 ！");
                                                haveErrors = true;
                                            }
                                        } else {
                                            errorMessage.append(countErrorIndex++ + "部门编号为" + deptCode + "的级别不能为空！");
                                            haveErrors = true;
                                        }
                                        //负责人编号
                                        String fzr = newData.getStr("CHARGEUSER");
                                        if (StringUtil.isNotEmpty(fzr)) {
                                            List<DynaBean> je_core_enduser = serviceTemplate.selectList("JE_CORE_ENDUSER", "and usercode='" + fzr + "'");
                                            if (je_core_enduser.size() == 0) {
                                                errorMessage.append(countErrorIndex++ + "部门编号为" + deptCode + "的原因：负责人在人员表中不存在！");
                                                haveErrors = true;
                                            } else {
                                                newData.set("CHARGEUSERNAME", je_core_enduser.get(0).getStr("USERNAME"));
                                            }
                                        }
                                        //联系电话
                                        String phone = newData.getStr("PHONE");
                                        if (StringUtil.isNotEmpty(phone)) {
                                            if (!pec.checkPhone(phone)) {
                                                errorMessage.append(countErrorIndex++ + "部门编号为" + deptCode + "的原因：联系电话非法！");
                                                haveErrors = true;
                                            }
                                        }
                                        //校验完毕
                                        if (!haveErrors) {
                                            //更新SQL语句
                                            if (IsUpdate) {//此处为更新操作
                                                updateSt.setString(1, newData.getStr("DEPTNAME") == null ? oldDataBean.getStr("DEPTNAME") : newData.getStr("DEPTNAME"));//DEPTNAME
                                                updateSt.setString(2, newData.getStr("RANKNAME") == null ? oldDataBean.getStr("RANKNAME") : newData.getStr("RANKNAME"));//RANKNAME
                                                updateSt.setString(3, newData.getStr("RANKCODE") == null ? oldDataBean.getStr("RANKCODE") : newData.getStr("RANKCODE"));//RANKCODE
                                                updateSt.setString(4, newData.getStr("CHARGEUSER") == null ? oldDataBean.getStr("CHARGEUSER") : newData.getStr("CHARGEUSER"));//CHARGEUSER
                                                updateSt.setString(5, newData.getStr("CHARGEUSERNAME") == null ? oldDataBean.getStr("CHARGEUSERNAME") : newData.getStr("CHARGEUSERNAME"));//CHARGEUSERNAME
                                                updateSt.setString(6, newData.getStr("PHONE") == null ? oldDataBean.getStr("PHONE") : newData.getStr("PHONE"));//PHONE
                                                updateSt.setString(7, newData.getStr("ADDRESS") == null ? oldDataBean.getStr("ADDRESS") : newData.getStr("ADDRESS"));//ADDRESS
                                                updateSt.setString(8, newData.getStr("ZNMS") == null ? oldDataBean.getStr("ZNMS") : newData.getStr("ZNMS"));//ZNMS
                                                updateSt.setString(9, newData.getStr("PATH") == null ? oldDataBean.getStr("PATH") : newData.getStr("PATH"));//PATH
                                                updateSt.setString(10, newData.getStr("PARENT") == null ? oldDataBean.getStr("PARENT") : newData.getStr("PARENT"));//PARENT
                                                updateSt.setString(11, newData.getStr("PARENTNAME") == null ? oldDataBean.getStr("PARENTNAME") : newData.getStr("PARENTNAME"));//PARENTNAME
                                                updateSt.setString(12, newData.getStr("PARENTCODE") == null ? oldDataBean.getStr("PARENTCODE") : newData.getStr("PARENTCODE"));//PARENTCODE
                                                updateSt.setString(13, newData.getStr("PARENTPATH") == null ? oldDataBean.getStr("PARENTPATH") : newData.getStr("PARENTPATH"));//PARENTPATH
                                                updateSt.setString(14, newData.getStr("NODETYPE") == null ? oldDataBean.getStr("NODETYPE") : "LEAF");//NODETYPE
                                                updateSt.setString(15, newData.getStr("TREEORDERINDEX") == null ? oldDataBean.getStr("TREEORDERINDEX") : "000001000002");//TREEORDERINDEX
                                                updateSt.setString(16, newData.getStr("DEPTCODE") == null ? oldDataBean.getStr("DEPTCODE") : newData.getStr("DEPTCODE"));

                                                updateSt.addBatch();
                                                trueUpdate++;
                                            } else {    //此处为插入操作
                                                //部门插入专用
                                                String pk = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 19);
                                                insertSt.setString(1, newData.getStr("DEPTNAME"));//DEPTNAME
                                                insertSt.setString(2, newData.getStr("RANKNAME"));//RANKNAME
                                                insertSt.setString(3, newData.getStr("RANKCODE"));//RANKCODE
                                                insertSt.setString(4, newData.getStr("CHARGEUSER"));//CHARGEUSER
                                                insertSt.setString(5, newData.getStr("CHARGEUSERNAME"));//CHARGEUSERNAME
                                                insertSt.setString(6, newData.getStr("PHONE"));//PHONE
                                                insertSt.setString(7, newData.getStr("ADDRESS"));//ADDRESS
                                                insertSt.setString(8, newData.getStr("ZNMS"));//ZNMS
                                                insertSt.setString(9, "/ROOT/" + pk);//PATH
                                                insertSt.setString(10, "ROOT");//PARENT
                                                insertSt.setString(11, "ROOT");//PARENTNAME
                                                insertSt.setString(12, "ROOT");//PARENTCODE
                                                insertSt.setString(13, "ROOT");//PARENTPATH
                                                insertSt.setString(14, newData.getStr("DEPTCODE"));//DEPTCODE
                                                insertSt.setString(15, pk);//DEPTID
                                                insertSt.setString(16, "1");//STATUS
                                                insertSt.setString(17, "LEAF");//NODETYPE
                                                insertSt.setString(18, "000001000002");//TREEORDERINDEX
                                                insertSt.addBatch();
                                                insertRoleSt.setString(1, "/ROOT/" + pk);//PATH
                                                insertRoleSt.setString(2, "1");//STATUS
                                                insertRoleSt.setString(3, "ROOT");//PARENT
                                                insertRoleSt.setString(4, "LEAF");//NODETYPE
                                                insertRoleSt.setString(5, newData.getStr("DEPTCODE"));//ROLECODE
                                                insertRoleSt.setString(6, "ROOT");//PARENTPATH
                                                insertRoleSt.setString(7, newData.getStr("TREEORDERINDEX"));//TREEORDERINDEX
                                                insertRoleSt.setString(8, "ROOT");//PARENTCODE
                                                insertRoleSt.setString(9, newData.getStr("DEPTNAME"));//ROLENAME
                                                insertRoleSt.setString(10, "ROOT");//PARENTNAME
                                                insertRoleSt.setString(11, pk);//DEPTID
                                                insertRoleSt.setString(12, "DEPT");//ROLETYPE
                                                insertRoleSt.setString(13, "SYS");//ROLERANK
                                                insertRoleSt.setString(14, "系统");//ZHMC
                                                insertRoleSt.setString(15, UUID.randomUUID().toString().replaceAll("-", "").substring(0, 19));//ROLEID
                                                insertRoleSt.setString(16, "1");//LAYER
                                                insertRoleSt.setString(17, "0");//DEVELOP
                                                insertRoleSt.addBatch();
                                                trueImport++;
                                            }
                                            if (i % 1000 == 0) {    //1000条处理一次
                                                if (updateSt != null) {
                                                    updateSt.executeBatch();
                                                    updateSt.clearBatch();
                                                }
                                                if (insertSt != null) {
                                                    insertSt.executeBatch();
                                                    insertSt.clearBatch();
                                                }
                                                if (insertRoleSt != null) {
                                                    insertRoleSt.executeBatch();
                                                    insertRoleSt.clearBatch();
                                                }
                                                conn.commit();
                                            }
                                        } else {
                                            falseImport++;
                                        }
                                    } else {
                                        errorMessage.append(countErrorIndex++ + "：第" + i + "个数据导入失败，原因：" + hashMap.get("errMsg"));
                                        falseImport++;
                                    }
                                    i++;
                                }
                                if (updateSt != null) {
                                    updateSt.executeBatch();
                                    updateSt.clearBatch();
                                }
                                if (insertSt != null) {
                                    insertSt.executeBatch();
                                    insertSt.clearBatch();
                                }
                                if (insertRoleSt != null) {
                                    insertRoleSt.executeBatch();
                                    insertRoleSt.clearBatch();
                                }
                                conn.commit();

                            } catch (Exception e) {
                                e.printStackTrace();
                                errorMessage.append(countErrorIndex++ + "：异常错误");
                            } finally {
                                if (rs != null) {
                                    try {
                                        rs.close();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (updateSt != null) {
                                    try {
                                        updateSt.close();
                                    } catch (SQLException e) {

                                        e.printStackTrace();
                                    }
                                }
                                if (insertSt != null) {
                                    try {
                                        insertSt.close();
                                    } catch (SQLException e) {

                                        e.printStackTrace();
                                    }
                                }
                                if (insertRoleSt != null) {
                                    try {
                                        insertRoleSt.close();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (conn != null) {
                                    try {
                                        conn.close();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                                res.add(trueImport);
                                res.add(trueUpdate);
                                res.add(falseImport);
                            }
                            latch.countDown();
                        }
                    });
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("主线程执行");
                    if (!"".equals(errorMessage.toString()) && errorMessage.toString() != null) {
                        ret.setMessage("成功导入" + res.get(0) + "条数据,更新了" + res.get(1) + "条数据，操作失败" + res.get(2) + "条，错误原因是：" + errorMessage.toString());
                    } else {
                        ret.setMessage("成功导入" + res.get(0) + "条数据,更新了" + res.get(1) + "条数据 !");
                        ret.IsSuccess = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonBuilder.toJson(ret);
    }

    @Override
    public String GetUserData(String USERCODE, String USERNAME, String GENDER) {
        if (serviceTemplate == null) {
            serviceTemplate = SpringContextHolder.getBean("PCDynaServiceTemplate");
        }
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();

        try {
            String whereSql = "";
            //部门编号
            if (StringUtil.isNotEmpty(USERCODE)) {
                if (USERCODE.indexOf(",") != -1) {
                    whereSql += " and USERCODE in ('" + USERCODE.replaceAll(",", "','") + "')";
                } else {
                    whereSql += " and USERCODE = '" + USERCODE + "'";
                }
            }
            //部门名称
            if (StringUtil.isNotEmpty(USERNAME)) {
                if (USERNAME.indexOf(",") != -1) {
                    whereSql += " and USERNAME in ('" + USERNAME.replaceAll(",", "','") + "')";
                } else {
                    whereSql += " and USERNAME like '%" + USERNAME + "%'";
                }
            }
            //级别名称
            if (StringUtil.isNotEmpty(GENDER)) {
                if (GENDER.indexOf(",") != -1) {
                    whereSql += " and GENDER in ('" + GENDER.replaceAll(",", "','") + "')";
                } else {
                    whereSql += " and GENDER = '" + GENDER + "'";
                }
            }

            List<DynaBean> peo = serviceTemplate.selectList("JE_CORE_ENDUSER", whereSql);
            ret.Data = ret.getValues(peo);
        } catch (Exception e) {
            e.printStackTrace();
            ret.setMessage("异常错误");
        }
        return jsonBuilder.toJson(ret);
    }

    @Override
    public String UpdateOrAddUserData(String jsonList) {
        if (serviceTemplate == null) {
            serviceTemplate = SpringContextHolder.getBean("PCDynaServiceTemplate");
        }
        Boolean haveError = false;
        StringBuffer errorMessage = new StringBuffer(2000);//错误信息记录
        HttpServletRequest request = null;
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        if (StringUtil.isNotEmpty(jsonList)) {
            List<DynaBean> list = new Vector<>();
            //转换成List数组
            try {
                String key = "";
                String value = "";
                net.sf.json.JSONArray ja1 = net.sf.json.JSONArray.fromObject(jsonList);
                if (ja1.size() > 0) {
                    for (int i = 0; i < ja1.size(); i++) {
                        DynaBean bean = new DynaBean();
                        bean.setStr(beanUtils.KEY_TABLE_CODE, "JE_CORE_ENDUSER");
                        JSONObject jb1 = ja1.getJSONObject(i);  // 遍历 jsonarray 数组，把每一个对象转成 json 对象
                        Iterator it = jb1.keys();
                        while (it.hasNext()) {
                            key = (String) it.next();
                            value = jb1.getString(key);
                            bean.setStr(key, value);
                        }
                        list.add(bean);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage.append("数据转换失败！");
                haveError = true;
            }
            if (!haveError) {
                //开启线程
                ExecutorService pool = Executors.newFixedThreadPool(10);
                CountDownLatch latch = new CountDownLatch(1);
                List<Integer> res = new Vector<>();
                try {
                    pool.submit(new Runnable() {
                        @Override
                        public void run() {
                            JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, "");
                            //开启JDBC批数据处理
                            Integer countErrorIndex = 1;
                            Integer trueImport = 0;
                            Integer trueUpdate = 0;
                            Integer falseImport = 0;
                            Connection conn = null;
                            PreparedStatement updateSt = null;
                            PreparedStatement insertSt = null;
                            PreparedStatement deleteRoleSt = null;
                            PreparedStatement insertRoleSt = null;
                            ResultSet rs = null;
                            String updateSql = "update JE_CORE_ENDUSER set USERNAME=?,GENDER=?,DEPTCODE=?,USERCARD=?,ISSYSUSER=?,ISMANAGER=?,JOBNUM=?,ROLENAMES=?,EXPIRYDATE=?,FAILURETIME=?,BIRTHDAY=?,NATION=?,IDCARD=?,MARRIED=?, " +
                                    " DEGREE=?,COMPANYEMAIL=?,NATIVEPLACE=?,PLUSUSERCODE=?,RTXID=?,CONTACTS=?,STATUS=?,SHADOW=?, " +
                                    " BACKUSERCODE=?,ICONCLS=?,AUDFLAG=?,PASSWORD=?,DEPTNAME=?,DEPTID=?,ROLECODES=?,ROLEIDS=?  where USERCODE=?";

                            String insertSql = "insert into JE_CORE_ENDUSER(USERNAME,GENDER,DEPTCODE,USERCARD,ISSYSUSER,ISMANAGER,JOBNUM,ROLENAMES,EXPIRYDATE,FAILURETIME,BIRTHDAY,NATION,IDCARD,MARRIED,DEGREE,COMPANYEMAIL,NATIVEPLACE,PLUSUSERCODE,RTXID,CONTACTS,STATUS,USERCODE,USERID,SHADOW," +
                                    "BACKUSERCODE,ICONCLS,AUDFLAG,PASSWORD,DEPTNAME,DEPTID,ROLECODES,ROLEIDS)\n" +
                                    "Values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                            String roleInsertSql = "insert into  JE_CORE_ROLE_USER (ROLEID,USERID)  Values(?,?)";
                            String roleDeleteSql = "delete from  JE_CORE_ROLE_USER where USERID=?";
                            try {
                                Properties prop = new Properties();
                                InputStream inputStream = LoadProperties.class.getClassLoader().getResourceAsStream("jdbc.properties");
                                prop.load(inputStream);
                                String driverClass = prop.getProperty("jdbc.Driver");
                                String url = prop.getProperty("jdbc.url");
                                String username = prop.getProperty("jdbc.username");
                                String password = prop.getProperty("jdbc.password");
                                conn = (Connection) DriverManager.getConnection(url, username, password);
                                updateSt = conn.prepareStatement(updateSql);
                                insertSt = conn.prepareStatement(insertSql);
                                deleteRoleSt = conn.prepareStatement(roleDeleteSql);
                                insertRoleSt = conn.prepareStatement(roleInsertSql);
                                conn.setAutoCommit(false);
                                Integer i = 1;
                                for (DynaBean dynaBean : list) {
                                    //数据处理
                                    DynaBean clone = dynaBean.clone();
                                    DynaBean newData = dynaBean.clone();
                                    Hashtable hashMap = checkValuesLen(clone, "USERCODE");
                                    Boolean IsUpdate = false;
                                    DynaBean oldDataBean = new DynaBean();
                                    if ((Boolean) hashMap.get("IsSussess")) {
                                        //数据库校验长度成功，特别字段校验
                                        Boolean haveErrors = false;
                                        //用户账号，不能为空
                                        String usercode = newData.getStr("USERCODE");
                                        if (StringUtil.isEmpty(usercode)) {
                                            errorMessage.append(countErrorIndex++ + "：第" + i + "条数据导入失败，原因：用户账号为空！");
                                            haveErrors = true;
                                        } else {
                                            oldDataBean = serviceTemplate.selectOne("JE_CORE_ENDUSER", "and USERCODE='" + usercode + "'");
                                            if (oldDataBean != null) {
                                                newData.set("USERID", oldDataBean.getStr("USERID"));
                                                IsUpdate = true;
                                            }
                                        }
                                        //用户名，不能为空，不能重复
                                        String USERNAME = newData.getStr("USERNAME");
                                        if (StringUtil.isEmpty(USERNAME)){
                                            errorMessage.append(countErrorIndex++ + "：用户账号为" + usercode + "的用户名不能为空 ！");
                                            haveErrors = true;
                                        }else{
                                            List<DynaBean> je_core_enduser = serviceTemplate.selectList("JE_CORE_ENDUSER", "and USERNAME='" + USERNAME + "' and USERCODE!='" + usercode + "'");
                                            if (je_core_enduser.size()>0){
                                                errorMessage.append(countErrorIndex++ + "：用户账号为" + usercode + "的用户名已存在，不允许重复 ！");
                                                haveErrors = true;
                                            }
                                        }
                                        //性别
                                        String GENDER = newData.getStr("GENDER");
                                        if (StringUtil.isNotEmpty(GENDER)) {
                                            if (!GENDER.equals("WOMAN")) {
                                                newData.set("GENDER", "MAN");
                                            }
                                        } else {
                                            newData.set("GENDER", "MAN");
                                        }
                                        //部门
                                        String DEPTCODE = newData.getStr("DEPTCODE");
                                        if (StringUtil.isNotEmpty(DEPTCODE)) {
                                            List<DynaBean> je_core_department = serviceTemplate.selectList("JE_CORE_DEPARTMENT", "and DEPTCODE='" + DEPTCODE + "'");
                                            if (je_core_department.size() > 0) {
                                                newData.set("DEPTNAME", je_core_department.get(0).get("DEPTNAME"));
                                                newData.set("DEPTCODE", je_core_department.get(0).get("DEPTCODE"));
                                                newData.set("DEPTID", je_core_department.get(0).get("DEPTID"));
                                            } else {
                                                errorMessage.append(countErrorIndex++ + "：用户账号为" + usercode + "的部门不存在 ！");
                                                haveErrors = true;
                                            }
                                        } else {
                                            errorMessage.append(countErrorIndex++ + "：用户账号为" + usercode + "的部门编号不能为空 ！");
                                            haveErrors = true;
                                        }
                                        //是否是系统用户
                                        String ISSYSUSER = newData.getStr("ISSYSUSER");
                                        if (StringUtil.isNotEmpty(ISSYSUSER)) {
                                            if (!ISSYSUSER.equals("0")) {
                                                newData.set("ISSYSUSER", "1");
                                            }
                                        } else {
                                            newData.set("ISSYSUSER", "1");
                                        }
                                        //是否是主管
                                        String ISMANAGER = newData.getStr("ISMANAGER");
                                        if (StringUtil.isNotEmpty(ISMANAGER)) {
                                            if (!ISSYSUSER.equals("1")) {
                                                newData.set("ISMANAGER", "0");
                                            }
                                        } else {
                                            newData.set("ISMANAGER", "0");
                                        }

                                        //有效期
                                        String EXPIRYDATE = newData.getStr("EXPIRYDATE");
                                        if (StringUtil.isNotEmpty(EXPIRYDATE)) {
                                            if (EXPIRYDATE.equals("SJD")) {
                                                String FAILURETIME = newData.getStr("FAILURETIME");
                                                if (StringUtil.isNotEmpty(FAILURETIME)) {
                                                    if (!pec.checkDate(FAILURETIME, true)) {
                                                        errorMessage.append(countErrorIndex++ + "：用户账号为" + usercode + "的失效时间非法 ！");
                                                        haveErrors = true;
                                                    }
                                                } else {
                                                    errorMessage.append(countErrorIndex++ + "：用户账号为" + usercode + "的有效期为时间段时，失效时间必填 ！");
                                                    haveErrors = true;
                                                }
                                            } else {
                                                newData.set("EXPIRYDATE", "YJ");
                                            }
                                        } else {
                                            newData.set("EXPIRYDATE", "YJ");
                                        }

                                        //身份证
                                        String IDCARD = newData.getStr("IDCARD");
                                        if (StringUtil.isNotEmpty(IDCARD)) {
                                            if (!pec.checkIDNumber(IDCARD)) {
                                                errorMessage.append(countErrorIndex++ + "：用户账号为" + usercode + "的身份证号非法 ！");
                                                haveErrors = true;
                                            }
                                        }
                                        //婚姻状况
                                        String MARRIED = newData.getStr("MARRIED");
                                        if (StringUtil.isNotEmpty(MARRIED)) {
                                            if (!MARRIED.equals("1")) {
                                                newData.set("MARRIED", "0");
                                            }
                                        }

                                        //手机
                                        String PLUSUSERCODE = newData.getStr("PLUSUSERCODE");
                                        if (StringUtil.isNotEmpty(PLUSUSERCODE)) {
                                            if (!pec.checkPhone(PLUSUSERCODE)) {
                                                errorMessage.append(countErrorIndex++ + "：用户账号为" + usercode + "的电话号码非法 ！");
                                                haveErrors = true;
                                            }
                                        }
                                        //紧急联系人
                                        String CONTACTS = newData.getStr("CONTACTS");
                                        if (StringUtil.isNotEmpty(CONTACTS)) {
                                            if (!pec.checkPhone(CONTACTS)) {
                                                errorMessage.append(countErrorIndex++ + "：用户账号为" + usercode + "的紧急联系人电话非法 ！");
                                                haveErrors = true;
                                            } else {
                                                if (CONTACTS.equals(PLUSUSERCODE)) {
                                                    errorMessage.append(countErrorIndex++ + "：用户账号为" + usercode + "的紧急联系人电话不能与手机相同 ！");
                                                    haveErrors = true;
                                                }
                                            }
                                        }
                                        //状态
                                        String STATUS = newData.getStr("STATUS");
                                        if (StringUtil.isNotEmpty(STATUS)) {
                                            if (!STATUS.equals("1")) {
                                                newData.set("STATUS", "0");
                                            }
                                        } else {
                                            newData.set("STATUS", "0");
                                        }

                                        //密码转码
                                        String PASSWORD = newData.getStr("PASSWORD");
                                        if (StringUtil.isNotEmpty(PASSWORD)) {
                                            String psw = jgmesCommon.StringToMd5(PASSWORD);
                                            if (StringUtil.isNotEmpty(psw)) {
                                                newData.set("PASSWORD", psw);
                                            } else {
                                                errorMessage.append(countErrorIndex++ + "：密码转换失败 ！");
                                                haveErrors = true;
                                            }
                                        } else {
                                            errorMessage.append(countErrorIndex++ + "：用户账号为" + usercode + "的密码不能为空 ！");
                                            haveErrors = true;
                                        }

                                        //角色校验
                                        String ROLECODEListStr = newData.getStr("ROLECODE");
                                        if (StringUtil.isNotEmpty(ROLECODEListStr)) {
                                            String[] roleList = ROLECODEListStr.split(",");
                                            StringBuffer RoleCode = new StringBuffer();
                                            StringBuffer RoleName = new StringBuffer();
                                            StringBuffer RoleID = new StringBuffer();
                                            for (String role : roleList) {
                                                List<DynaBean> je_core_role = serviceTemplate.selectList("JE_CORE_ROLE", "and STATUS =1 and ROLECODE ='" + role + "'and (PATH like '%ROOT%' and ROLETYPE='ROLE' AND ROLERANK='SYS' ) OR ROLEID = 'ROOT' and ROLEID!='ROOT' ORDER BY PARENT asc, ORDERINDEX asc ", "ROLECODE,ROLENAME,ROLEID");
                                                if (je_core_role.size() == 0) {
                                                    errorMessage.append(countErrorIndex++ + "：角色编码" + role + "不存在 ！");
                                                    haveErrors = true;
                                                    break;
                                                } else {
                                                    RoleCode.append(je_core_role.get(0).getStr("ROLECODE") + ",");
                                                    RoleName.append(je_core_role.get(0).getStr("ROLENAME") + ",");
                                                    RoleID.append(je_core_role.get(0).getStr("ROLEID") + ",");
                                                }
                                            }
                                            newData.set("ROLENAMES", RoleName.toString().substring(0, RoleName.length() - 1));
                                            newData.set("ROLECODES", RoleCode.toString().substring(0, RoleCode.length() - 1));
                                            newData.set("ROLEIDS", RoleID.toString().substring(0, RoleID.length() - 1));
                                        }

                                        //校验完毕
                                        if (!haveErrors) {
                                            //更新SQL语句
                                            if (IsUpdate) {//此处为更新操作
                                                updateSt.setString(1, newData.getStr("USERNAME")==null?oldDataBean.getStr("USERNAME"):newData.getStr("USERNAME"));
                                                updateSt.setString(2, newData.getStr("GENDER")==null?oldDataBean.getStr("GENDER"):newData.getStr("GENDER"));
                                                updateSt.setString(3, newData.getStr("DEPTCODE")==null?oldDataBean.getStr("DEPTCODE"):newData.getStr("DEPTCODE"));
                                                updateSt.setString(4, newData.getStr("USERCARD")==null?oldDataBean.getStr("USERCARD"):newData.getStr("USERCARD"));
                                                updateSt.setString(5, newData.getStr("ISSYSUSER")==null?oldDataBean.getStr("ISSYSUSER"):newData.getStr("ISSYSUSER"));
                                                updateSt.setString(6, newData.getStr("ISMANAGER")==null?oldDataBean.getStr("ISMANAGER"):newData.getStr("ISMANAGER"));
                                                updateSt.setString(7, newData.getStr("JOBNUM")==null?oldDataBean.getStr("JOBNUM"):newData.getStr("JOBNUM"));
                                                updateSt.setString(8, newData.getStr("ROLENAMES")==null?oldDataBean.getStr("ROLENAMES"):newData.getStr("ROLENAMES"));
                                                updateSt.setString(9, newData.getStr("EXPIRYDATE")==null?oldDataBean.getStr("EXPIRYDATE"):newData.getStr("EXPIRYDATE"));
                                                updateSt.setString(10, newData.getStr("FAILURETIME")==null?oldDataBean.getStr("FAILURETIME"):newData.getStr("FAILURETIME"));
                                                updateSt.setString(11, newData.getStr("BIRTHDAY")==null?oldDataBean.getStr("BIRTHDAY"):newData.getStr("BIRTHDAY"));
                                                updateSt.setString(12, newData.getStr("NATION")==null?oldDataBean.getStr("NATION"):newData.getStr("NATION"));
                                                updateSt.setString(13, newData.getStr("IDCARD")==null?oldDataBean.getStr("IDCARD"):newData.getStr("IDCARD"));
                                                updateSt.setString(14, newData.getStr("MARRIED")==null?oldDataBean.getStr("MARRIED"):newData.getStr("MARRIED"));
                                                updateSt.setString(15, newData.getStr("DEGREE")==null?oldDataBean.getStr("DEGREE"):newData.getStr("DEGREE"));
                                                updateSt.setString(16, newData.getStr("COMPANYEMAIL")==null?oldDataBean.getStr("COMPANYEMAIL"):newData.getStr("COMPANYEMAIL"));
                                                updateSt.setString(17, newData.getStr("NATIVEPLACE")==null?oldDataBean.getStr("NATIVEPLACE"):newData.getStr("NATIVEPLACE"));
                                                updateSt.setString(18, newData.getStr("PLUSUSERCODE")==null?oldDataBean.getStr("PLUSUSERCODE"):newData.getStr("PLUSUSERCODE"));
                                                updateSt.setString(19, newData.getStr("RTXID")==null?oldDataBean.getStr("RTXID"):newData.getStr("RTXID"));
                                                updateSt.setString(20, newData.getStr("CONTACTS")==null?oldDataBean.getStr("CONTACTS"):newData.getStr("CONTACTS"));
                                                updateSt.setString(21, newData.getStr("STATUS")==null?oldDataBean.getStr("STATUS"):newData.getStr("STATUS"));
                                                updateSt.setString(22, newData.getStr("SHADOW")==null?oldDataBean.getStr("USERNAME"):newData.getStr("SHADOW"));
                                                updateSt.setString(23, newData.getStr("BACKUSERCODE")==null?oldDataBean.getStr("BACKUSERCODE"):newData.getStr("BACKUSERCODE"));
                                                updateSt.setString(24, newData.getStr("ICONCLS")==null?oldDataBean.getStr("ICONCLS"):newData.getStr("ICONCLS"));
                                                updateSt.setString(25, newData.getStr("AUDFLAG")==null?oldDataBean.getStr("AUDFLAG"):newData.getStr("AUDFLAG"));
                                                updateSt.setString(26, newData.getStr("PASSWORD")==null?oldDataBean.getStr("PASSWORD"):newData.getStr("PASSWORD"));
                                                updateSt.setString(27, newData.getStr("DEPTNAME")==null?oldDataBean.getStr("DEPTNAME"):newData.getStr("DEPTNAME"));
                                                updateSt.setString(28, newData.getStr("DEPTID")==null?oldDataBean.getStr("DEPTID"):newData.getStr("DEPTID"));
                                                updateSt.setString(29, newData.getStr("ROLECODES")==null?oldDataBean.getStr("ROLECODES"):newData.getStr("ROLECODES"));
                                                updateSt.setString(30, newData.getStr("ROLEIDS")==null?oldDataBean.getStr("ROLEIDS"):newData.getStr("ROLEIDS"));
                                                updateSt.setString(31, newData.getStr("USERCODE")==null?oldDataBean.getStr("USERCODE"):newData.getStr("USERCODE"));
                                                updateSt.addBatch();
                                                //删除角色——人员对应表
                                                deleteRoleSt.setString(1, newData.getStr("USERID"));
                                                deleteRoleSt.addBatch();
                                                //插入角色——人员对应表
                                                String roleIDListStr = newData.getStr("ROLEIDS");
                                                    String[] roleIDList = roleIDListStr.split(",");
                                                    for (String roleID : roleIDList) {
                                                        insertRoleSt.setString(1, roleID);
                                                        insertRoleSt.setString(2, newData.getStr("USERID"));
                                                        insertRoleSt.addBatch();
                                                    }
                                                    trueUpdate++;
                                            } else {    //此处为插入操作
                                                String pk = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 19);
                                                insertSt.setString(1, newData.getStr("USERNAME"));
                                                insertSt.setString(2, newData.getStr("GENDER"));
                                                insertSt.setString(3, newData.getStr("DEPTCODE"));
                                                insertSt.setString(4, newData.getStr("USERCARD"));
                                                insertSt.setString(5, newData.getStr("ISSYSUSER"));
                                                insertSt.setString(6, newData.getStr("ISMANAGER"));
                                                insertSt.setString(7, newData.getStr("JOBNUM"));
                                                insertSt.setString(8, newData.getStr("ROLENAMES"));
                                                insertSt.setString(9, newData.getStr("EXPIRYDATE"));
                                                insertSt.setString(10, newData.getStr("FAILURETIME"));
                                                insertSt.setString(11, newData.getStr("BIRTHDAY"));
                                                insertSt.setString(12, newData.getStr("NATION"));
                                                insertSt.setString(13, newData.getStr("IDCARD"));
                                                insertSt.setString(14, newData.getStr("MARRIED"));
                                                insertSt.setString(15, newData.getStr("DEGREE"));
                                                insertSt.setString(16, newData.getStr("COMPANYEMAIL"));
                                                insertSt.setString(17, newData.getStr("NATIVEPLACE"));
                                                insertSt.setString(18, newData.getStr("PLUSUSERCODE"));
                                                insertSt.setString(19, newData.getStr("RTXID"));
                                                insertSt.setString(20, newData.getStr("CONTACTS"));
                                                insertSt.setString(21, newData.getStr("STATUS"));
                                                insertSt.setString(22, newData.getStr("USERCODE"));
                                                insertSt.setString(23, pk);
                                                insertSt.setString(24, "0");
                                                insertSt.setString(25, newData.getStr("USERCODE"));
                                                insertSt.setString(26, "je_shadow_user");
                                                insertSt.setString(27, "NOSTATUS");
                                                insertSt.setString(28, newData.getStr("PASSWORD"));
                                                insertSt.setString(29, newData.getStr("DEPTNAME"));
                                                insertSt.setString(30, newData.getStr("DEPTID"));
                                                insertSt.setString(31, newData.getStr("ROLECODES"));
                                                insertSt.setString(32, newData.getStr("ROLEIDS"));
                                                insertSt.addBatch();
                                                //插入角色——用户对应表
                                                String roleIDListStr = newData.getStr("ROLEIDS");
                                                if (StringUtil.isNotEmpty(roleIDListStr)){
                                                    String[] roleIDList = roleIDListStr.split(",");
                                                    for (String roleID : roleIDList) {
                                                        insertRoleSt.setString(1, roleID);
                                                        insertRoleSt.setString(2, pk);
                                                        insertRoleSt.addBatch();
                                                    }
                                                }
                                                trueImport++;
                                            }

                                            if (i % 1000 == 0) {    //1000条处理一次
                                                if (insertSt != null) {
                                                    insertSt.executeBatch();
                                                    insertSt.clearBatch();
                                                }
                                                if (updateSt != null) {
                                                    updateSt.executeBatch();
                                                    updateSt.clearBatch();
                                                }
                                                if (insertRoleSt != null) {
                                                    insertRoleSt.executeBatch();
                                                    insertRoleSt.clearBatch();
                                                }
                                                if (deleteRoleSt != null) {
                                                    deleteRoleSt.executeBatch();
                                                    deleteRoleSt.clearBatch();
                                                }
                                                conn.commit();
                                            }
                                        }else{
                                            falseImport++;
                                        }
                                    } else {
                                        errorMessage.append(countErrorIndex++ + "：人员编码为" + hashMap.get("errMsg"));
                                        falseImport++;
                                    }
                                    i++;
                                }

                                if (insertSt != null) {
                                    insertSt.executeBatch();
                                    insertSt.clearBatch();
                                }
                                if (updateSt != null) {
                                    updateSt.executeBatch();
                                    updateSt.clearBatch();
                                }
                                if (deleteRoleSt != null) {
                                    deleteRoleSt.executeBatch();
                                    deleteRoleSt.clearBatch();
                                }
                                if (insertRoleSt != null) {
                                    insertRoleSt.executeBatch();
                                    insertRoleSt.clearBatch();
                                }
                                conn.commit();
                            } catch (Exception e) {
                                e.printStackTrace();
                                errorMessage.append(countErrorIndex++ + "：异常错误");
                            } finally {
                                if (rs != null) {
                                    try {
                                        rs.close();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (insertSt != null) {
                                    try {
                                        insertSt.close();
                                    } catch (SQLException e) {

                                        e.printStackTrace();
                                    }
                                }
                                if (updateSt != null) {
                                    try {
                                        updateSt.close();
                                    } catch (SQLException e) {

                                        e.printStackTrace();
                                    }
                                }
                                if (insertRoleSt != null) {
                                    try {
                                        insertRoleSt.close();
                                    } catch (SQLException e) {

                                        e.printStackTrace();
                                    }
                                }
                                if (deleteRoleSt != null) {
                                    try {
                                        deleteRoleSt.close();
                                    } catch (SQLException e) {

                                        e.printStackTrace();
                                    }
                                }
                                if (conn != null) {
                                    try {
                                        conn.close();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                                res.add(trueImport);
                                res.add(trueUpdate);
                                res.add(falseImport);
                            }
                            latch.countDown();
                        }
                    });
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("主线程执行");
                    if (!"".equals(errorMessage.toString()) && errorMessage.toString() != null) {
                        ret.setMessage("成功导入"+res.get(0)+"条数据,更新了"+res.get(1)+"条数据，操作失败"+res.get(2)+"条，错误原因是："+errorMessage.toString());
                    } else {
                        ret.setMessage("成功导入"+res.get(0)+"条数据,更新了"+res.get(1)+"条数据 !");
                        ret.IsSuccess = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonBuilder.toJson(ret);
    }

    @Override
    public String GetRoleData() {
        if (serviceTemplate == null) {
            serviceTemplate = SpringContextHolder.getBean("PCDynaServiceTemplate");
        }
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();

        try {
            List<DynaBean> je_core_role = serviceTemplate.selectList("JE_CORE_ROLE", "and (PATH like '%ROOT%' and ROLETYPE='ROLE' AND ROLERANK='SYS' ) OR ROLEID = 'ROOT' and ROLEID!='ROOT' and STATUS =1  ORDER BY PARENT asc, ORDERINDEX asc ", "ROLECODE,ROLENAME");
            ret.Data = ret.getValues(je_core_role);
        } catch (Exception e) {
            e.printStackTrace();
            ret.setMessage("异常错误");
        }
        return jsonBuilder.toJson(ret);
    }

    /**
     * 校验DynaBean每个字段在对应的表中，在对应的编码下是否超出长度以及数据验证
     *
     * @param dynaBean    校验的数据
     * @param COLUMN_ONLY 唯一性字段，进行错误提示
     * @return
     */
    private Hashtable checkValuesLen(DynaBean dynaBean, String COLUMN_ONLY) {
        DynaBean copy = dynaBean.clone();
        Hashtable hashtable = new Hashtable();  //使用
        boolean IsSussess = true;
        try {
            String tableCode = (String) dynaBean.get(BeanUtils.KEY_TABLE_CODE);
            String errMsg = "";
            HashMap map = copy.getValues(); //JE平台中，使用getValues方法会影响DynaBean结构，必须先用clone方法进行复制
            Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
            String databaseName = null;//数据库名称
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                String key = entry.getKey();    //键
                Object data = entry.getValue();
                String value = data.toString() + "";    //值
                DruidDataSource druid = SpringContextHolder.getBean("dataSource"); //获取
                databaseName = druid.getUrl();
                String[] urls = databaseName.split("\\?")[0].split("\\/");
                databaseName = urls[urls.length - 1];
                String sql = "select * from information_schema.columns where table_name = '" + tableCode + "' and TABLE_SCHEMA='" + databaseName + "'";
                List<DynaBean> tbBeanList = serviceTemplate.selectListBySql(sql);
                for (DynaBean tbBean : tbBeanList) {
                    String column_name = tbBean.getStr("COLUMN_NAME");
                    if (column_name.equals(key)) {
                        String COLUMN_TYPE = tbBean.getStr("COLUMN_TYPE");
                        String CHARACTER_SET_NAME = tbBean.getStr("CHARACTER_SET_NAME");
                        Integer character_maximum_length = 0;
                        Integer value_length = 0;
                        String DATA_TYPE = tbBean.getStr("DATA_TYPE");
                        if (DATA_TYPE.equals("int")) {
                            if (pec.checkInteger(value)) {
                                character_maximum_length = 11;
                                value_length = value.length();
                                if (value_length > character_maximum_length) {
                                    errMsg += "字段" + column_name + "数值非法过长。";
                                    IsSussess = false;
                                }
                            } else {
                                errMsg += "字段" + column_name + "数值非法。";
                                IsSussess = false;
                            }

                        } else if (DATA_TYPE.equals("decimal")) {
                            Integer NUMERIC_PRECISION = tbBean.getInt("NUMERIC_PRECISION");
                            Integer NUMERIC_SCALE = tbBean.getInt("NUMERIC_SCALE");
                            if (!pec.CheckDcimal(NUMERIC_PRECISION, NUMERIC_SCALE, value)) {
                                errMsg += "字段" + column_name + "数值非法。";
                                IsSussess = false;
                            }
                        } else {
                            character_maximum_length = Integer.parseInt(tbBean.getStr("CHARACTER_MAXIMUM_LENGTH"));
                            value_length = value.getBytes(CHARACTER_SET_NAME).length;
                            if (value_length > character_maximum_length) {
                                errMsg += "字段" + column_name + "内容过长。";
                                IsSussess = false;
                            }
                        }
                    }
                }
            }
            String only = dynaBean.getStr(COLUMN_ONLY);
            if (StringUtil.isNotEmpty(only) && StringUtil.isNotEmpty(errMsg)) {
                errMsg = only + "的错误信息为：" + errMsg;
            }
            hashtable.put("errMsg", errMsg);
        } catch (Exception e) {
            e.printStackTrace();
            hashtable.put("errMsg", e.toString());
            IsSussess = false;
        }
        hashtable.put("IsSussess", IsSussess);
        return hashtable;
    }






    /**
     * ��ȡ��¼�û�
     *
     * @return
     */
    public EndUser getCurrentUser() {
        // TODO Auto-generated method stub
        return SecurityUserHolder.getCurrentUser();
    }

    /**
     * ��ȡ��¼�û����ڲ���
     *
     * @return
     */
    public Department getCurrentDept() {
        // TODO Auto-generated method stub
        return SecurityUserHolder.getCurrentUserDept();
    }

    @Resource(name = "PCDynaServiceTemplate")
    public void setServiceTemplate(PCDynaServiceTemplate serviceTemplate) {
        this.serviceTemplate = serviceTemplate;
    }

    @Resource(name = "PCServiceTemplateImpl")
    public void setPcServiceTemplate(PCServiceTemplate pcServiceTemplate) {
        this.pcServiceTemplate = pcServiceTemplate;
    }

    @Resource(name = "userManager")
    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }
}