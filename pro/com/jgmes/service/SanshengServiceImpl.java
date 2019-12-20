package com.jgmes.service;

import com.gexin.fastjson.JSONObject;
import com.je.core.security.SecurityUserHolder;
import com.je.core.service.PCDynaServiceTemplate;
import com.je.core.service.PCServiceTemplate;
import com.je.core.util.JEUUID;
import com.je.core.util.JdbcUtil;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.BeanUtils;
import com.je.core.util.bean.DynaBean;
import com.je.rbac.model.Department;
import com.je.rbac.model.EndUser;
import com.je.rbac.service.UserManager;
import com.jgmes.util.JgmesCommon;
import com.jgmes.util.JgmesResult;
import com.jgmes.util.PropertyUtil;
import net.sf.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * sanshengService
 *
 * @author admin
 * @version 2019-11-13 20:53:43
 */
@SuppressWarnings("ALL")
@Component("sanshengService")
public class SanshengServiceImpl implements SanshengService {

    private static final Logger logger = LoggerFactory.getLogger(SanshengServiceImpl.class);
    /**
     * ???Bean(DynaBean)??????
     */
    private PCDynaServiceTemplate serviceTemplate;
    /**
     * ???Bean?????????,???????SQL
     */
    private PCServiceTemplate pcServiceTemplate;
    /**
     * ????????
     */
    private UserManager userManager;

    private static final String CXID = "vrRGHFYotg35wRpSQjV";//默认的产线ID

    @Override
    public void load() {
        System.out.println("hello serviceimpl");
    }

    /**
     * Description: 仅用于三胜物料资料导入
     *
     * @Param: * @param null
     * @return:
     * @author: ljs
     * @date: 2019/11/15 15:57
     */
    @Override
    public DynaBean importProductData(DynaBean order) {
        Boolean haveError = false;
        StringBuilder errorMessage = new StringBuilder(2000);//错误信息记录
        JgmesCommon jgmesCommon = new JgmesCommon(null, serviceTemplate);
        if (order != null) {
            //校验该产品编号是否已经存在
            String productdata_bh = order.getStr("PRODUCTDATA_BH");
            if (StringUtil.isNotEmpty(productdata_bh)) {
                List<DynaBean> jgmes_base_productdata = serviceTemplate.selectList("JGMES_BASE_PRODUCTDATA", "and PRODUCTDATA_BH='" + productdata_bh + "'");
                if (jgmes_base_productdata.size() > 0) {
                    haveError = true;
                    errorMessage.append("该产品编号已存在");
                }
            } else {
                haveError = true;
                errorMessage.append("产品编号不能为空");
            }

            //校验该产品名称是否存在
            String productdata_name = order.getStr("PRODUCTDATA_NAME");
            if (StringUtil.isNotEmpty(productdata_name)) {
                List<DynaBean> jgmes_base_productdata1 = serviceTemplate.selectList("JGMES_BASE_PRODUCTDATA", "and PRODUCTDATA_NAME='" + productdata_name + "'");
                if (jgmes_base_productdata1.size() > 0) {
                    haveError = true;
                    errorMessage.append("该产品名称已存在");
                }
            } else {
                haveError = true;
                errorMessage.append("产品名称不能为空");
            }
            //校验物料属性
            String productdata_wltype_code = order.getStr("PRODUCTDATA_WLTYPE_CODE");
            if (StringUtil.isNotEmpty(productdata_wltype_code)) {
                DynaBean jgmes_wltype = jgmesCommon.getDic("JGMES_WLTYPE", productdata_wltype_code);
                if (jgmes_wltype == null) {
                    haveError = true;
                    errorMessage.append("该物料属性不存在");
                } else {
                    order.set("PRODUCTDATA_WLTYPE_CODE", jgmes_wltype.get("DICTIONARYITEM_ITEMCODE"));
                    order.set("PRODUCTDATA_WLTYPE_NAME", jgmes_wltype.get("DICTIONARYITEM_ITEMNAME"));
                }
            } else {
                haveError = true;
                errorMessage.append("物料属性不能为空");
            }

            //校验工艺路线
            String productdata_cpgylx = order.getStr("PRODUCTDATA_CPGYLX");
            if (StringUtil.isNotEmpty(productdata_cpgylx)) {
                DynaBean jgmes_gygl_gylx = serviceTemplate.selectOne("JGMES_GYGL_GYLX", "and GYLX_GYLXNUM='" + productdata_cpgylx + "' and GYLX_STATUS=1");
                if (jgmes_gygl_gylx == null) {
                    haveError = true;
                    errorMessage.append("该工艺路线不存在或未被启用");
                } else {
                    order.set("PRODUCTDATA_CPGYLX", jgmes_gygl_gylx.get("GYLX_GYLXNAME"));
                    order.set("PRODUCTDATA_CPGYLXID", jgmes_gygl_gylx.get("GYLX_ID"));
                }
            } else {
                haveError = true;
                errorMessage.append("工艺路线编号不能为空");
            }
            order.set("PRODUCTDATA_STATUS_CODE", 1);
            order.set("PRODUCTDATA_STATUS_NAME", "启用");
            String pk = JEUUID.uuid();
            order.set(BeanUtils.KEY_PK_CODE, pk);
            order.set("JGMES_BASE_PRODUCTDATA_ID", pk);
            if (haveError) {
                order.set("error", "序号:" + order.getStr("rownumberer_1") + "的错误信息为：" + errorMessage.toString());
            } else {
                DynaBean jgmes_sys_tmbz = serviceTemplate.selectOne("JGMES_SYS_TMBZ", "and TMBZ_TMFL_CODE='CPM'");
                if (jgmes_sys_tmbz != null) {
                    //子表自动写入
                    DynaBean print = new DynaBean();
                    print.set(BeanUtils.KEY_TABLE_CODE, "JGMES_BASE_CPTMYYGG");
                    print.set("CPTMYYGG_CPBH", productdata_bh);//产品编号
                    String uuid = JEUUID.uuid();
                    print.set("CPTMYYGG_YYGZBH", uuid);
                    print.set("CPTMYYGG_YYGGMC", "产品条码规则");//应用规则名称
                    print.set("CPTMYYGG_TMXZ_CODE", "TMXZX02");//条码性质，唯一码
                    print.set("CPTMYYGG_TMXZ_NAME", "唯一码");//条码性质，唯一码
                    print.set("CPTMYYGG_TMLX_CODE", "TMLX01");//条码类型
                    print.set("CPTMYYGG_TMLX_NAME", "产品条码");//条码类型
                    print.set("CPTMYYGG_BQMB", jgmes_sys_tmbz.get("TMBZ_BQMBWJ"));//模板文件
                    print.set("CPTMYYGG_BQCS", jgmes_sys_tmbz.get("TMBZ_BQCSMB"));//模板参数
                    print.set("CPTMYYGG_MTMSL", 1);//每条码用量
                    print.set("CPTMYYGG_BARCODEMODEL_CODE", "BARCODEMODEL01");//条码生成方式
                    print.set("CPTMYYGG_TMSCLX_CODE", "TMSCLX03");//条码生成类型，按照工单生成
                    print.set("CPTMYYGG_TMSCLX_NAME", "按工单生成");//条码生成类型，按照工单生成
                    print.set("CPTMYYGG_TMGZBH", jgmes_sys_tmbz.get("TMBZ_YZZZ"));//条码规则编号，取验证正则字段
                    print.set("CPTMYYGG_STATUS_CODE", 1);//状态
                    print.set("CPTMYYGG_STATUS_NAME", "启用");//状态
                    print.set("JGMES_BASE_CPTMYYGG_ID", uuid);
                    print.set(BeanUtils.KEY_PK_CODE, uuid);
                    print.set("JGMES_BASE_PRODUCTDATA_ID", pk);
                    order.set("child", print);
                } else {
                    order.set("error", "序号:" + order.getStr("rownumberer_1") + "的错误信息为：条码标识中没有设定产品码规则");
                }
            }
        }
        return order;
    }

    /*
     * @Author Jiansong Lu
     * @Description 三胜平板端排产功能逻辑处理
     * @Date 17:34 2019/11/19
     * @Param [sumitList]
     * @return com.jgmes.util.JgmesResult<java.util.HashMap>
     **/
    @Override
    public JgmesResult<HashMap> SumitScheduling(String sumitList, String cxCode) {
        JgmesResult<HashMap> ret = new JgmesResult<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
        DynaBean jgmes_base_cxsj = serviceTemplate.selectOne("JGMES_BASE_CXSJ", "and CXSJ_CXBM ='" + cxCode + "'");
        if (jgmes_base_cxsj == null) {
            ret.setMessage("产线信息获取失败，请重新登录绑定！");
            return ret;
        }
        try {
            if (StringUtil.isNotEmpty(sumitList)) {
                JSONArray jsonArray = JSONArray.fromObject(sumitList);
                for (Object o : jsonArray) {
                    //获取工单信息
                    JSONObject gdObject = JSONObject.parseObject(JSONObject.toJSONString(o));
                    String gdid = gdObject.getString("JGMES_PLAN_GDLB_ID");
                    if (StringUtil.isNotEmpty(gdid)) {
                        DynaBean gdBean = serviceTemplate.selectOne("JGMES_PLAN_GDLB", "and JGMES_PLAN_GDLB_ID='" + gdid + "'");
                        //更新工单信息
                        if (gdBean != null) {
                            gdBean.set("GDLB_YPCSL", gdBean.getInt("GDLB_YPCSL") + gdObject.getIntValue("PCQty"));//已排产数量
                            gdBean.set("GDLB_WPCSL", gdBean.getInt("GDLB_WPCSL") - gdObject.getIntValue("PCQty"));//未排产数量
                            //生成生产任务单
                            DynaBean scrwBean = new DynaBean();
                            String uuid = JEUUID.uuid();
                            scrwBean.set(BeanUtils.KEY_TABLE_CODE, "JGMES_PLAN_SCRW");
                            scrwBean.set("SCRW_CPBH", gdBean.get("GDLB_CPBH"));//产品编号
                            scrwBean.set("SCRW_NAME", gdBean.get("GDLB_NAME"));//产品名称
                            scrwBean.set("SCRW_CPGG", gdBean.get("GDLB_CPGG"));//产品规格
                            scrwBean.set("SCRW_JHKGSJ", dateFormat.format(new Date()));//计划开工时间，今天
                            scrwBean.set("SCRW_JHWGSJ", dateFormat.format(new Date()));//计划完工时间，今天
                            scrwBean.set("SCRW_PCRQ", dateFormat.format(new Date()));//排产日期，今天
                            scrwBean.set("SCRW_DDHM", gdBean.get("GDLB_DDHM"));//订单号码
                            scrwBean.set("SCRW_GDHM", gdBean.get("GDLB_GDHM"));//工单号码
//							scrwBean.set("SCRW_RWDH", uuid);//任务单号
                            scrwBean.set("SCRW_RWDH", serviceTemplate.buildCode("SCRW_RWDH", "JGMES_PLAN_SCRW", scrwBean));//任务单号
                            scrwBean.set("JGMES_PLAN_SCRW_ID", uuid);//主键id
                            scrwBean.set(BeanUtils.KEY_PK_CODE, uuid);//主键id
                            scrwBean.set("SCRW_DDSL", gdBean.get("GDLB_DDSL"));//订单数量
                            scrwBean.set("SCRW_YPCSL", gdBean.get("GDLB_YPCSL"));//已排产数量
                            scrwBean.set("SCRW_WPCSL", gdBean.get("GDLB_WPCSL"));//未排产数量
                            scrwBean.set("SCRW_XPCSL", gdBean.get("GDLB_XPCSL"));//需排产数量
                            scrwBean.set("SCRW_RWZT_CODE", "RWZT01");//生产任务状态，待生产
                            scrwBean.set("SCRW_RWZT_NAMe", "待生产");//待生产名称
                            scrwBean.set("SCRW_PCSL", gdObject.get("PCQty"));//排产数量
                            scrwBean.set("SCRW_CXBM", jgmes_base_cxsj.getStr("CXSJ_CXBM"));
                            scrwBean.set("SCRW_CXMC", jgmes_base_cxsj.getStr("CXSJ_CXMC"));
                            scrwBean.set("SCRW_XSGJ", gdBean.getStr("GDLB_CKGJ"));//出口国家
                            //绑定条码号：1.获取该工单下面的未绑定生产任务ID的条码号，以流水号排序，筛选出排产数量条数据
//							List<DynaBean> dynaBeans = serviceTemplate.selectList("JGMES_BASE_GDCPTM",
//									"and GDCPTM_GDHM='" + gdBean.get("GDLB_GDHM") +
//											"' AND JGMES_PLAN_SCRW_ID IS NULL ORDER BY GDCPTM_LSH ASC LIMIT " + gdObject.getIntValue("PCQty"));
//							if (dynaBeans.size() < gdObject.getIntValue("PCQty")) {
//								ret.setMessage("该工单下需要绑定的条码号不足，绑定失败，排产失败");
//							} else {
//								for (DynaBean dynaBean : dynaBeans) {
//									dynaBean.set("JGMES_PLAN_SCRW_ID", scrwBean.get("JGMES_PLAN_SCRW_ID"));
//									dynaBean.set("GDCPTM_SCRWDH", scrwBean.get("SCRW_RWDH"));
//								}
//							}
                            //集中操作
                            if (ret.IsSuccess) {
                                serviceTemplate.update(gdBean);
                                serviceTemplate.insert(scrwBean);
//								for (DynaBean dynaBean : dynaBeans) {
//									serviceTemplate.update(dynaBean);
                            }
                        } else {
                            ret.setMessage("工单信息获取失败！");
                        }

                    }

                }
            } else {
                ret.setMessage("数据化初始失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
            ret.setMessage("异常错误！");
        }
        return ret;
    }

    @Override
    public JgmesResult<HashMap> startScrw(String taskcode) {
        JgmesResult<HashMap> ret = new JgmesResult<>();
        DynaBean scrw = serviceTemplate.selectOne("JGMES_PLAN_SCRW", "and SCRW_RWDH='" + taskcode + "'");
        if (scrw != null) {
            //生产中
            scrw.set("SCRW_RWZT_CODE", "RWZT02");
            scrw.set("SCRW_RWZT_NAME", "生产中");
            DynaBean update = serviceTemplate.update(scrw);
            if (update == null) {
                ret.setMessage("更新失败");
            }
        } else {
            ret.setMessage("该生产任务单不存在或已被删除");
        }
        return ret;
    }

    @Override
    public JgmesResult<HashMap> startScrwByBarCode(String barCode,String ProductionTaskCode) {
        JgmesResult<HashMap> ret = new JgmesResult<>();
        try {
                if (StringUtil.isNotEmpty(barCode)&&StringUtil.isNotEmpty(ProductionTaskCode)) {
                DynaBean jgmes_base_gdcptm = serviceTemplate.selectOne("JGMES_BASE_GDCPTM", "and GDCPTM_TMH='" + barCode + "'");
                if (jgmes_base_gdcptm != null &&StringUtil.isEmpty(jgmes_base_gdcptm.getStr("SCRW_RWDH"))) {
                    DynaBean scrw = serviceTemplate.selectOne("JGMES_PLAN_SCRW", "and SCRW_RWDH='"+ProductionTaskCode+"'");
                    if (scrw != null) {
                        //生产中
                        scrw.set("SCRW_RWZT_CODE", "RWZT02");
                        scrw.set("SCRW_RWZT_NAME", "生产中");
                        //绑定生产任务单到条码
                        DynaBean update = serviceTemplate.update(scrw);
//                        serviceTemplate.update(jgmes_base_gdcptm);
                        if (update == null) {
                            ret.setMessage("生产任务单开工失败！");
                        }

                    } else {
                        ret.setMessage("该条码绑定的生产任务单不存在或已被删除！");
                    }
                } else {
                    ret.setMessage("未查询到该条码的信息");
                }

            } else {
                ret.setMessage("条码号不能为空！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ret.setMessage("异常错误");
        }
        return ret;
    }

    /*
     * @Author Jiansong Lu
     * @Description 绑定条码
     * @Date 10:44 2019/12/3
     * @Param [barCode, ProductionTaskCode]
     * @return com.jgmes.util.JgmesResult<java.util.HashMap>
     **/
    @Override
    public JgmesResult<HashMap> bindingBarCode(String barCode, String ProductionTaskCode) {
        JgmesResult<HashMap> ret = new JgmesResult<>();
        if (StringUtil.isNotEmpty(barCode)&&StringUtil.isNotEmpty(ProductionTaskCode)){
            //获取条码号信息
            DynaBean jgmes_base_gdcptm = serviceTemplate.selectOne("JGMES_BASE_GDCPTM", "and GDCPTM_TMH='" + barCode + "'");
            //获取生产任务单信息
            DynaBean jgmes_plan_scrw = serviceTemplate.selectOne("JGMES_PLAN_SCRW", "and SCRW_RWDH='" + ProductionTaskCode + "'");
            if (jgmes_plan_scrw!=null){
                //先校验生产任务单的排产数量和条码表中存在的条码数量，若条码数量大于或等于排产数量，则不进行绑定且提示
                Integer scrw_pcsl = jgmes_plan_scrw.getInt("SCRW_PCSL");
                List<DynaBean> jgmes_base_gdcptm1 = serviceTemplate.selectList("JGMES_BASE_GDCPTM", "and GDCPTM_SCRWDH='" + ProductionTaskCode + "' and GDCPTM_TMH !='"+barCode+"'");
                if (scrw_pcsl<=jgmes_base_gdcptm1.size()){
                    ret.setMessage("该生产任务单需绑定的生产任务单已满，请切换其他生产任务单");
                    return ret;
                }

                if (jgmes_base_gdcptm!=null){
                    //校验条码号的工单号码和生产任务的工单号码是否一致
                    if (!jgmes_plan_scrw.getStr("SCRW_GDHM").equals(jgmes_base_gdcptm.getStr("GDCPTM_GDHM"))) {
                        ret.setMessage("该条码绑定的工单和生产任务单的工单不一致，无法绑定");
                        return ret;
                    }
                    if (StringUtil.isEmpty(jgmes_base_gdcptm.getStr("GDCPTM_SCRWDH"))){
                        jgmes_base_gdcptm.set("JGMES_PLAN_SCRW_ID", jgmes_plan_scrw.get("JGMES_PLAN_SCRW_ID"));
                        jgmes_base_gdcptm.set("GDCPTM_SCRWDH", jgmes_plan_scrw.get("SCRW_RWDH"));
                        serviceTemplate.update(jgmes_base_gdcptm);
                    }
                }else{
                    ret.setMessage("该条码号不存在或已被删除！");
                }
            }else {
                ret.setMessage("该生产任务单不存在或已被删除！");
            }
        }else{
            ret.setMessage("条码号与生产任务单号不能为空！");
        }
        return ret;
    }



    @Override
    public JgmesResult<HashMap> delScrw(String scrwId) {
        JgmesResult<HashMap> ret = new JgmesResult<>();
        try {
            if (StringUtil.isNotEmpty(scrwId)) {
                //先获取生产任务单信息
                DynaBean jgmes_plan_scrw = serviceTemplate.selectOneByPk("JGMES_PLAN_SCRW", scrwId);
                if (jgmes_plan_scrw != null) {
                    //开始校验该生产任务单是否能被删除，条件为：报工数据中没有该生产任务单号的信息
                    List<DynaBean> jgmes_pb_bgsj = serviceTemplate.selectList("JGMES_PB_BGSJ", "and BGSJ_SCRWID='" + scrwId + "'");
                    if (jgmes_pb_bgsj.size() > 0) {
                        ret.setMessage("该生产任务单已有报工记录，不能删除");
                    } else {
                        //更新工单的已排产数量和未排产数量
                        String scrw_gdhm = jgmes_plan_scrw.getStr("SCRW_GDHM");//订单号码，三胜工单中订单号码和工单号码一致
                        int scrw_pcsl = jgmes_plan_scrw.getInt("SCRW_PCSL");//排产数量
                        DynaBean jgmes_plan_gdlb = serviceTemplate.selectOne("JGMES_PLAN_GDLB", "and GDLB_GDHM='" + scrw_gdhm + "'");
                        if (jgmes_plan_gdlb != null) {
                            jgmes_plan_gdlb.set("GDLB_YPCSL", jgmes_plan_gdlb.getInt("GDLB_YPCSL") - scrw_pcsl);
                            jgmes_plan_gdlb.set("GDLB_WPCSL", jgmes_plan_gdlb.getInt("GDLB_WPCSL") + scrw_pcsl);
                            //获取该生产任务单绑定的条码号，进行解绑操作
                            List<DynaBean> jgmes_base_gdcptm = serviceTemplate.selectList("JGMES_BASE_GDCPTM", "and JGMES_PLAN_SCRW_ID='" + scrwId + "'");
                            serviceTemplate.update(jgmes_plan_gdlb);
                            for (DynaBean dynaBean : jgmes_base_gdcptm) {
                                dynaBean.set("JGMES_PLAN_SCRW_ID", "");
                                dynaBean.set("GDCPTM_SCRWDH", "");
                                serviceTemplate.update(dynaBean);
                            }
                            int i = serviceTemplate.deleteByIds(scrwId, "JGMES_PLAN_SCRW", "JGMES_PLAN_SCRW_ID");
                            if (i == 0) {
                                ret.setMessage("删除失败！请重试");
                            }
                        }else{
                            ret.setMessage("获取工单信息失败！");
                        }
                    }
                } else {
                    ret.setMessage("获取生产任务单信息失败！");
                }
            } else {
                ret.setMessage("没有获取到要删除的生产任务单信息！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ret.setMessage("异常错误");
        }
        return ret;
    }

    /*
     * @Author Jiansong Lu
     * @Description 三胜ERP工单同步
     * @Date 15:13 2019/11/28
     * @Param []
     * @return void
     **/

    public void getConnection() {
        System.out.println(111);
        Properties properties = new Properties();
        Connection conn = null;
        try {
            properties.load(this.getClass().getResourceAsStream("/pro-config/dbServer.properties"));
            String driver = properties.getProperty("driver");
            String url = properties.getProperty("url");
            String username = properties.getProperty("username");
            String password = properties.getProperty("password");
            // 加载数据库驱动
//            Class.forName(driver);
//            conn = DriverManager.getConnection(url, username, password);
            System.out.println(driver+"----"+url+"----"+username+"----"+password);
            System.out.println("数据库连接成功");
        } catch (Exception e) {
            System.out.println("数据库连接失败");
            e.printStackTrace();

        }

//        JdbcUtil.
//
//
//        return conn;
    }
    /*
     * @Author Jiansong Lu
     * @Description 三胜ERP工单同步接口
     * @Date 10:37 2019/11/30
     * @Param []
     * @return void
     **/
    @Override
    public void synchronizationOrder() {
        Properties properties = new Properties();
        Connection conn = null;
        JdbcUtil instance=null;
        Statement stmt = null;
        ResultSet rs = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Properties prop = new Properties();
            InputStream resourceAsStream = PropertyUtil.class.getClassLoader().getResourceAsStream("dbServer.properties");
            prop.load(resourceAsStream);
            String driver = prop.getProperty("SQLServer.Driver");
            String url = prop.getProperty("SQLServer.url");
            String username = prop.getProperty("SQLServer.username");
            String password = prop.getProperty("SQLServer.password");
            instance = JdbcUtil.getInstance(driver, url, username, password);
            conn = instance.getConnection();
            if (conn==null){
                logger.info("数据库连接失败");
                System.out.println("数据库连接失败！");
                return;
            }
            String SQL = "select * from k3_icmo";
//            String SQL = "select * from k3_icmo where 生产任务单='20181114E112'";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(SQL);
            //获取系统参数————三胜工单同步工艺路线绑定信息，助记码：SSGYLX
            String gylx_id = "";
            String gylx_gylxname="";
            DynaBean jgmes_xtgl_xtcs = serviceTemplate.selectOne("JGMES_XTGL_XTCS", "and XTCS_CXFL2_CODE='SSGYLX'");
            if (jgmes_xtgl_xtcs!=null){
                //工艺路线编号
                String xtcs_csz = jgmes_xtgl_xtcs.getStr("XTCS_CSZ");
                DynaBean jgmes_gygl_gylx = serviceTemplate.selectOne("JGMES_GYGL_GYLX", "and GYLX_GYLXNUM='" + xtcs_csz + "'");
                if (jgmes_gygl_gylx!=null){
                    gylx_id = jgmes_gygl_gylx.getStr("GYLX_ID");
                    gylx_gylxname = jgmes_gygl_gylx.getStr("GYLX_GYLXNAME");
                }
            }else{
                logger.info("三胜工单同步ERP绑定工艺路线为空！");
                System.out.println("三胜工单同步ERP绑定工艺路线为空！");
            }
            DynaBean jgmes_sys_tmbz = serviceTemplate.selectOne("JGMES_SYS_TMBZ", "and TMBZ_TMFL_CODE='CPM'");
            DynaBean print = new DynaBean();
            if (jgmes_sys_tmbz != null) {
                /* 子表自动写入 */
                print.set(BeanUtils.KEY_TABLE_CODE, "JGMES_BASE_CPTMYYGG");
                /* 应用规则名称 */
                print.set("CPTMYYGG_YYGGMC", "产品条码规则");
                //条码性质，唯一码
                print.set("CPTMYYGG_TMXZ_CODE", "TMXZX02");
                print.set("CPTMYYGG_TMXZ_NAME", "唯一码");//条码性质，唯一码
                print.set("CPTMYYGG_TMLX_CODE", "TMLX01");//条码类型
                print.set("CPTMYYGG_TMLX_NAME", "产品条码");//条码类型
                print.set("CPTMYYGG_BQMB", jgmes_sys_tmbz.get("TMBZ_BQMBWJ"));//模板文件
                print.set("CPTMYYGG_BQCS", jgmes_sys_tmbz.get("TMBZ_BQCSMB"));//模板参数
                print.set("CPTMYYGG_MTMSL", 1);//每条码用量
                print.set("CPTMYYGG_BARCODEMODEL_CODE", "BARCODEMODEL01");//条码生成方式
                print.set("CPTMYYGG_TMSCLX_CODE", "TMSCLX03");//条码生成类型，按照工单生成
                print.set("CPTMYYGG_TMSCLX_NAME", "按工单生成");//条码生成类型，按照工单生成
                print.set("CPTMYYGG_TMGZBH", jgmes_sys_tmbz.get("TMBZ_YZZZ"));//条码规则编号，取验证正则字段
                print.set("CPTMYYGG_STATUS_CODE", 1);//状态
                print.set("CPTMYYGG_STATUS_NAME", "启用");//
            }
            while (rs.next()) {
                try {
                    String gd = rs.getString("生产任务单");//金蝶任务单等于mes工单号码
                    String ddh = rs.getString("销售订单号");//金蝶销售订单号等于mes订单号
                    String country = rs.getString("出口国家");//金蝶出口国家等于mes销售国家
                    String cpCode = rs.getString("物料长代码");//金蝶物料长代码等于物料资料产品编号
                    String cpName = rs.getString("物料名称");//金蝶物料名称等于物料资料产品名称
                    String unit = rs.getString("单位");//金蝶单位等于mes物料资料计量单位
                    Integer quantity = rs.getInt("计划生产数量");//金蝶计划生产数量等于mes订单数量，需排产数量，未排产数量，工单数量
                    Integer shortFallQuantity = rs.getInt("总装欠产数");//金蝶计划生产数量等于mes工单总装欠产数，随ERP更新
                    Integer incomingQuantity = rs.getInt("入库数量");//金蝶计划入库数量等于mes入库数量，随ERP更新
                    String dateOfDelivery = rs.getString("生产承诺日期");//金蝶生产承诺日期等于mesOTD日期，订单交货日期
                    String kingdeeStratDate = rs.getString("总装开工日期");//金蝶总装开工日期等于mes总装开工日期
                    //收集物料资料信息
                    if (StringUtil.isNotEmpty(cpCode)){
                        //先查找该物料资料是否存在
                        DynaBean jgmes_base_productdata = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", "and PRODUCTDATA_BH='" + cpCode + "'");
                        if (jgmes_base_productdata==null){
                            DynaBean productdata = new DynaBean();
                            productdata.set(BeanUtils.KEY_TABLE_CODE, "JGMES_BASE_PRODUCTDATA");
                            productdata.set("PRODUCTDATA_BH",cpCode);
                            productdata.set("PRODUCTDATA_NAME",cpName);
                            productdata.set("PRODUCTDATA_JLDW_NAME",unit);
                            productdata.set("PRODUCTDATA_STATUS_CODE",1);
                            productdata.set("PRODUCTDATA_STATUS_NAME","启用");
                            productdata.set("PRODUCTDATA_CPFL_NAME","冷藏展示柜");
                            productdata.set("PRODUCTDATA_WLTYPE_CODE","CP");
                            productdata.set("PRODUCTDATA_WLTYPE_NAME","成品");
                            productdata.set("PRODUCTDATA_CPGYLXID",gylx_id);
                            productdata.set("PRODUCTDATA_CPGYLX", gylx_gylxname);
                            String pk = JEUUID.uuid();
                            productdata.set(BeanUtils.KEY_PK_CODE, pk);
                            productdata.set("JGMES_BASE_PRODUCTDATA_ID", pk);
                            serviceTemplate.buildModelCreateInfo(productdata);
                            serviceTemplate.insert(productdata);
                            //子表信息
                            if (print!=null){
                                DynaBean clone = print.clone();
                                clone.set("CPTMYYGG_CPBH", cpCode);//产品编号
                                String uuid = JEUUID.uuid();
                                clone.set("CPTMYYGG_YYGZBH", uuid);
                                clone.set("JGMES_BASE_CPTMYYGG_ID", uuid);
                                clone.set(BeanUtils.KEY_PK_CODE, uuid);
                                clone.set("JGMES_BASE_PRODUCTDATA_ID", pk);
                                serviceTemplate.buildModelCreateInfo(clone);
                                serviceTemplate.insert(clone);
                            }

                            //工艺路线工序对应表信息
                            if (StringUtil.isNotEmpty(gylx_id)){
                                List<DynaBean> jgmes_gygl_gylxgx = serviceTemplate.selectList("JGMES_GYGL_GYLXGX", "and GYLX_ID='"+gylx_id+"' order by SY_ORDERINDEX");
                                Integer index = 0;
                                for (DynaBean jgmesGyglGylxgx : jgmes_gygl_gylxgx) {
                                    //获取信息
                                    String gygxid = jgmesGyglGylxgx.getStr("GYLXGX_ID");//工艺工序ID
                                    String gxid = jgmesGyglGylxgx.getStr("GYLXGX_GXID");//工序ID
                                    String gx = jgmesGyglGylxgx.getStr("GYLXGX_GXNAME");//工序
                                    String pxzd = jgmesGyglGylxgx.getStr("SY_ORDERINDEX");//排序字段
                                    String gxbm = jgmesGyglGylxgx.getStr("GYLXGX_GXNUM");//工序编码
                                    String gwbh = "";//工位编码
                                    String gwmc = "";//工位名称
                                    String gwid = "";//工位主键ID
                                    //此处写死产线编码,获取产线工位
                                    List<DynaBean> jgmes_base_gw = serviceTemplate.selectList("JGMES_BASE_GW", "and JGMES_BASE_CXSJ_ID='"+CXID+"'  ORDER BY GW_GXSXH");
                                    if (jgmes_base_gw.size()<jgmes_gygl_gylxgx.size()){
                                        logger.error("工艺路线工序数量大于产线工位数量,不进行生成");
                                    }else{
                                        gwbh = jgmes_base_gw.get(index).getStr("GW_GWBH");
                                        gwmc = jgmes_base_gw.get(index).getStr("GW_GWMC");
                                        gwid = jgmes_base_gw.get(index).getStr("JGMES_BASE_GW_ID");
                                        DynaBean cx = serviceTemplate.selectOneByPk("JGMES_BASE_CXSJ", CXID);
                                        if (cx!=null){
                                            String sql= "insert into JGMES_BASE_CPGWGX(SY_ORDERINDEX,CPGWGX_STATUS_CODE,JGMES_BASE_CPGWGX_ID,CPGWGX_CPBH,CPGWGX_CPMC,CPGWGX_GG,CPGWGX_CPGYLXID,CPGWGX_GXID,CPGWGX_GXNAME,CPGWGX_GYGXID,JGMES_BASE_CXSJ_ID,CPGWGX_CXBM,CPGWGX_CXMC,CPGWGX_GWBH,CPGWGX_GWMC,JGMES_BASE_GW_ID,CPGWGX_HWJSBS)";
                                            sql=sql+"values('"+pxzd+"',"+1+",'"+JEUUID.uuid()+"','"+cpCode+"','"+cpName+"','"+""+"','"+gylx_id+"','"+gxid+"','"+gx+"','"+gygxid+"','"+cx.getStr("JGMES_BASE_CXSJ_ID")+"','"+cx.getStr("CXSJ_CXBM")+"','"+cx.getStr("CXSJ_CXMC")+"','"+gwbh+"','"+gwmc+"','"+gwid+"','"+1+"')";
                                            pcServiceTemplate.executeSql(sql);
                                        }
                                        index++;
                                    }
                                }
                            }

                        }else{
                            logger.info("产品"+cpCode+"已存在！");
                        }
                    }else{
                        logger.error("ERP产品编号为空！");
                        return;
                    }
                    //收集工单信息
                    DynaBean gdBean = new DynaBean();
                    //判断工单是否存在
                    DynaBean jgmes_plan_gdlb = serviceTemplate.selectOne("JGMES_PLAN_GDLB", "and GDLB_GDHM='" + gd + "'");
                    if (jgmes_plan_gdlb!=null){
                        //存在则更新ERP产品入库数量，ERP总装欠产数量，ERP总装开工日期
                        jgmes_plan_gdlb.set("GDLB_ERPCPRKSL",incomingQuantity);//ERP产品入库数量
                        jgmes_plan_gdlb.set("GDLB_ERPZZQCSL",shortFallQuantity);//ERP总装欠产数量
                        if (StringUtil.isNotEmpty(kingdeeStratDate)){
                            gdBean.set("GDLB_ERPZZKGRQ",sdf.format(sdf.parse(kingdeeStratDate)));//ERP总装开工日期
                        }else{
                            gdBean.set("GDLB_ERPZZKGRQ","");//ERP总装开工日期
                        }
                        serviceTemplate.update(jgmes_plan_gdlb);
                    }else{
                        //工单不存在则插入工单
                        gdBean.set(BeanUtils.KEY_TABLE_CODE, "JGMES_PLAN_GDLB");
                        gdBean.set("GDLB_RQ",sdf.format(new Date()));//工单生成日期
                        gdBean.set("GDLB_DDHM",ddh);//订单号
                        gdBean.set("GDLB_GDHM",gd);//工单号码
                        gdBean.set("GDLB_QGRQ",sdf.format(new Date()));//请购日期，暂定当天
                        if (StringUtil.isNotEmpty(dateOfDelivery)){
                            gdBean.set("GDLB_OTDRQ",sdf.format(sdf.parse(dateOfDelivery)));//OTD日期
                            gdBean.set("GDLB_DDJHRQ",sdf.format(sdf.parse(dateOfDelivery)));//交货日期
                        }else{
                            gdBean.set("GDLB_OTDRQ","");//OTD日期
                            gdBean.set("GDLB_DDJHRQ","");//交货日期
                        }
                        gdBean.set("GDLB_CPBH",cpCode);//产品编码
                        gdBean.set("GDLB_NAME",cpName);//产品名称
                        gdBean.set("GDLB_DDSL",quantity);//订单数量
                        gdBean.set("GDLB_XPCSL",quantity);//需排产数量
                        gdBean.set("GDLB_WPCSL",quantity);//未排产数量
                        gdBean.set("GDLB_GDSL",quantity);//工单数量
                        gdBean.set("GDLB_GDZT_CODE",1);//工单状态code
                        gdBean.set("GDLB_GDZT_NAME","未完");//工单状态
                        gdBean.set("GDLB_CKGJ",country);//销售国家
                        gdBean.set("GDLB_ERPCPRKSL",incomingQuantity);//ERP产品入库数量
                        gdBean.set("GDLB_ERPZZQCSL",shortFallQuantity);//ERP总装欠产数量
                        if (StringUtil.isNotEmpty(kingdeeStratDate)){
                            gdBean.set("GDLB_ERPZZKGRQ",sdf.format(sdf.parse(kingdeeStratDate)));//ERP总装开工日期
                        }else{
                            gdBean.set("GDLB_ERPZZKGRQ","");//ERP总装开工日期
                        }
                        gdBean.set("GDLB_BZ","ERP同步所得");//ERP总装开工日期
                        serviceTemplate.insert(gdBean);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    logger.error(e.toString());
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }finally {
            instance.close();
            try {
                if (conn!=null){
                    conn.close();
                }
                if (stmt!=null){
                    stmt.close();
                }
                if (rs!=null){
                    rs.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }




    /**
     * ?????????
     *
     * @return
     */
    public EndUser getCurrentUser() {
        // TODO Auto-generated method stub
        return SecurityUserHolder.getCurrentUser();
    }

    /**
     * ????????????????
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