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

    private static final String CXID = "vrRGHFYotg35wRpSQjV";//Ĭ�ϵĲ���ID

    @Override
    public void load() {
        System.out.println("hello serviceimpl");
    }

    /**
     * Description: ��������ʤ�������ϵ���
     *
     * @Param: * @param null
     * @return:
     * @author: ljs
     * @date: 2019/11/15 15:57
     */
    @Override
    public DynaBean importProductData(DynaBean order) {
        Boolean haveError = false;
        StringBuilder errorMessage = new StringBuilder(2000);//������Ϣ��¼
        JgmesCommon jgmesCommon = new JgmesCommon(null, serviceTemplate);
        if (order != null) {
            //У��ò�Ʒ����Ƿ��Ѿ�����
            String productdata_bh = order.getStr("PRODUCTDATA_BH");
            if (StringUtil.isNotEmpty(productdata_bh)) {
                List<DynaBean> jgmes_base_productdata = serviceTemplate.selectList("JGMES_BASE_PRODUCTDATA", "and PRODUCTDATA_BH='" + productdata_bh + "'");
                if (jgmes_base_productdata.size() > 0) {
                    haveError = true;
                    errorMessage.append("�ò�Ʒ����Ѵ���");
                }
            } else {
                haveError = true;
                errorMessage.append("��Ʒ��Ų���Ϊ��");
            }

            //У��ò�Ʒ�����Ƿ����
            String productdata_name = order.getStr("PRODUCTDATA_NAME");
            if (StringUtil.isNotEmpty(productdata_name)) {
                List<DynaBean> jgmes_base_productdata1 = serviceTemplate.selectList("JGMES_BASE_PRODUCTDATA", "and PRODUCTDATA_NAME='" + productdata_name + "'");
                if (jgmes_base_productdata1.size() > 0) {
                    haveError = true;
                    errorMessage.append("�ò�Ʒ�����Ѵ���");
                }
            } else {
                haveError = true;
                errorMessage.append("��Ʒ���Ʋ���Ϊ��");
            }
            //У����������
            String productdata_wltype_code = order.getStr("PRODUCTDATA_WLTYPE_CODE");
            if (StringUtil.isNotEmpty(productdata_wltype_code)) {
                DynaBean jgmes_wltype = jgmesCommon.getDic("JGMES_WLTYPE", productdata_wltype_code);
                if (jgmes_wltype == null) {
                    haveError = true;
                    errorMessage.append("���������Բ�����");
                } else {
                    order.set("PRODUCTDATA_WLTYPE_CODE", jgmes_wltype.get("DICTIONARYITEM_ITEMCODE"));
                    order.set("PRODUCTDATA_WLTYPE_NAME", jgmes_wltype.get("DICTIONARYITEM_ITEMNAME"));
                }
            } else {
                haveError = true;
                errorMessage.append("�������Բ���Ϊ��");
            }

            //У�鹤��·��
            String productdata_cpgylx = order.getStr("PRODUCTDATA_CPGYLX");
            if (StringUtil.isNotEmpty(productdata_cpgylx)) {
                DynaBean jgmes_gygl_gylx = serviceTemplate.selectOne("JGMES_GYGL_GYLX", "and GYLX_GYLXNUM='" + productdata_cpgylx + "' and GYLX_STATUS=1");
                if (jgmes_gygl_gylx == null) {
                    haveError = true;
                    errorMessage.append("�ù���·�߲����ڻ�δ������");
                } else {
                    order.set("PRODUCTDATA_CPGYLX", jgmes_gygl_gylx.get("GYLX_GYLXNAME"));
                    order.set("PRODUCTDATA_CPGYLXID", jgmes_gygl_gylx.get("GYLX_ID"));
                }
            } else {
                haveError = true;
                errorMessage.append("����·�߱�Ų���Ϊ��");
            }
            order.set("PRODUCTDATA_STATUS_CODE", 1);
            order.set("PRODUCTDATA_STATUS_NAME", "����");
            String pk = JEUUID.uuid();
            order.set(BeanUtils.KEY_PK_CODE, pk);
            order.set("JGMES_BASE_PRODUCTDATA_ID", pk);
            if (haveError) {
                order.set("error", "���:" + order.getStr("rownumberer_1") + "�Ĵ�����ϢΪ��" + errorMessage.toString());
            } else {
                DynaBean jgmes_sys_tmbz = serviceTemplate.selectOne("JGMES_SYS_TMBZ", "and TMBZ_TMFL_CODE='CPM'");
                if (jgmes_sys_tmbz != null) {
                    //�ӱ��Զ�д��
                    DynaBean print = new DynaBean();
                    print.set(BeanUtils.KEY_TABLE_CODE, "JGMES_BASE_CPTMYYGG");
                    print.set("CPTMYYGG_CPBH", productdata_bh);//��Ʒ���
                    String uuid = JEUUID.uuid();
                    print.set("CPTMYYGG_YYGZBH", uuid);
                    print.set("CPTMYYGG_YYGGMC", "��Ʒ�������");//Ӧ�ù�������
                    print.set("CPTMYYGG_TMXZ_CODE", "TMXZX02");//�������ʣ�Ψһ��
                    print.set("CPTMYYGG_TMXZ_NAME", "Ψһ��");//�������ʣ�Ψһ��
                    print.set("CPTMYYGG_TMLX_CODE", "TMLX01");//��������
                    print.set("CPTMYYGG_TMLX_NAME", "��Ʒ����");//��������
                    print.set("CPTMYYGG_BQMB", jgmes_sys_tmbz.get("TMBZ_BQMBWJ"));//ģ���ļ�
                    print.set("CPTMYYGG_BQCS", jgmes_sys_tmbz.get("TMBZ_BQCSMB"));//ģ�����
                    print.set("CPTMYYGG_MTMSL", 1);//ÿ��������
                    print.set("CPTMYYGG_BARCODEMODEL_CODE", "BARCODEMODEL01");//�������ɷ�ʽ
                    print.set("CPTMYYGG_TMSCLX_CODE", "TMSCLX03");//�����������ͣ����չ�������
                    print.set("CPTMYYGG_TMSCLX_NAME", "����������");//�����������ͣ����չ�������
                    print.set("CPTMYYGG_TMGZBH", jgmes_sys_tmbz.get("TMBZ_YZZZ"));//��������ţ�ȡ��֤�����ֶ�
                    print.set("CPTMYYGG_STATUS_CODE", 1);//״̬
                    print.set("CPTMYYGG_STATUS_NAME", "����");//״̬
                    print.set("JGMES_BASE_CPTMYYGG_ID", uuid);
                    print.set(BeanUtils.KEY_PK_CODE, uuid);
                    print.set("JGMES_BASE_PRODUCTDATA_ID", pk);
                    order.set("child", print);
                } else {
                    order.set("error", "���:" + order.getStr("rownumberer_1") + "�Ĵ�����ϢΪ�������ʶ��û���趨��Ʒ�����");
                }
            }
        }
        return order;
    }

    /*
     * @Author Jiansong Lu
     * @Description ��ʤƽ����Ų������߼�����
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
            ret.setMessage("������Ϣ��ȡʧ�ܣ������µ�¼�󶨣�");
            return ret;
        }
        try {
            if (StringUtil.isNotEmpty(sumitList)) {
                JSONArray jsonArray = JSONArray.fromObject(sumitList);
                for (Object o : jsonArray) {
                    //��ȡ������Ϣ
                    JSONObject gdObject = JSONObject.parseObject(JSONObject.toJSONString(o));
                    String gdid = gdObject.getString("JGMES_PLAN_GDLB_ID");
                    if (StringUtil.isNotEmpty(gdid)) {
                        DynaBean gdBean = serviceTemplate.selectOne("JGMES_PLAN_GDLB", "and JGMES_PLAN_GDLB_ID='" + gdid + "'");
                        //���¹�����Ϣ
                        if (gdBean != null) {
                            gdBean.set("GDLB_YPCSL", gdBean.getInt("GDLB_YPCSL") + gdObject.getIntValue("PCQty"));//���Ų�����
                            gdBean.set("GDLB_WPCSL", gdBean.getInt("GDLB_WPCSL") - gdObject.getIntValue("PCQty"));//δ�Ų�����
                            //������������
                            DynaBean scrwBean = new DynaBean();
                            String uuid = JEUUID.uuid();
                            scrwBean.set(BeanUtils.KEY_TABLE_CODE, "JGMES_PLAN_SCRW");
                            scrwBean.set("SCRW_CPBH", gdBean.get("GDLB_CPBH"));//��Ʒ���
                            scrwBean.set("SCRW_NAME", gdBean.get("GDLB_NAME"));//��Ʒ����
                            scrwBean.set("SCRW_CPGG", gdBean.get("GDLB_CPGG"));//��Ʒ���
                            scrwBean.set("SCRW_JHKGSJ", dateFormat.format(new Date()));//�ƻ�����ʱ�䣬����
                            scrwBean.set("SCRW_JHWGSJ", dateFormat.format(new Date()));//�ƻ��깤ʱ�䣬����
                            scrwBean.set("SCRW_PCRQ", dateFormat.format(new Date()));//�Ų����ڣ�����
                            scrwBean.set("SCRW_DDHM", gdBean.get("GDLB_DDHM"));//��������
                            scrwBean.set("SCRW_GDHM", gdBean.get("GDLB_GDHM"));//��������
//							scrwBean.set("SCRW_RWDH", uuid);//���񵥺�
                            scrwBean.set("SCRW_RWDH", serviceTemplate.buildCode("SCRW_RWDH", "JGMES_PLAN_SCRW", scrwBean));//���񵥺�
                            scrwBean.set("JGMES_PLAN_SCRW_ID", uuid);//����id
                            scrwBean.set(BeanUtils.KEY_PK_CODE, uuid);//����id
                            scrwBean.set("SCRW_DDSL", gdBean.get("GDLB_DDSL"));//��������
                            scrwBean.set("SCRW_YPCSL", gdBean.get("GDLB_YPCSL"));//���Ų�����
                            scrwBean.set("SCRW_WPCSL", gdBean.get("GDLB_WPCSL"));//δ�Ų�����
                            scrwBean.set("SCRW_XPCSL", gdBean.get("GDLB_XPCSL"));//���Ų�����
                            scrwBean.set("SCRW_RWZT_CODE", "RWZT01");//��������״̬��������
                            scrwBean.set("SCRW_RWZT_NAMe", "������");//����������
                            scrwBean.set("SCRW_PCSL", gdObject.get("PCQty"));//�Ų�����
                            scrwBean.set("SCRW_CXBM", jgmes_base_cxsj.getStr("CXSJ_CXBM"));
                            scrwBean.set("SCRW_CXMC", jgmes_base_cxsj.getStr("CXSJ_CXMC"));
                            scrwBean.set("SCRW_XSGJ", gdBean.getStr("GDLB_CKGJ"));//���ڹ���
                            //������ţ�1.��ȡ�ù��������δ����������ID������ţ�����ˮ������ɸѡ���Ų�����������
//							List<DynaBean> dynaBeans = serviceTemplate.selectList("JGMES_BASE_GDCPTM",
//									"and GDCPTM_GDHM='" + gdBean.get("GDLB_GDHM") +
//											"' AND JGMES_PLAN_SCRW_ID IS NULL ORDER BY GDCPTM_LSH ASC LIMIT " + gdObject.getIntValue("PCQty"));
//							if (dynaBeans.size() < gdObject.getIntValue("PCQty")) {
//								ret.setMessage("�ù�������Ҫ�󶨵�����Ų��㣬��ʧ�ܣ��Ų�ʧ��");
//							} else {
//								for (DynaBean dynaBean : dynaBeans) {
//									dynaBean.set("JGMES_PLAN_SCRW_ID", scrwBean.get("JGMES_PLAN_SCRW_ID"));
//									dynaBean.set("GDCPTM_SCRWDH", scrwBean.get("SCRW_RWDH"));
//								}
//							}
                            //���в���
                            if (ret.IsSuccess) {
                                serviceTemplate.update(gdBean);
                                serviceTemplate.insert(scrwBean);
//								for (DynaBean dynaBean : dynaBeans) {
//									serviceTemplate.update(dynaBean);
                            }
                        } else {
                            ret.setMessage("������Ϣ��ȡʧ�ܣ�");
                        }

                    }

                }
            } else {
                ret.setMessage("���ݻ���ʼʧ��");
            }

        } catch (Exception e) {
            e.printStackTrace();
            ret.setMessage("�쳣����");
        }
        return ret;
    }

    @Override
    public JgmesResult<HashMap> startScrw(String taskcode) {
        JgmesResult<HashMap> ret = new JgmesResult<>();
        DynaBean scrw = serviceTemplate.selectOne("JGMES_PLAN_SCRW", "and SCRW_RWDH='" + taskcode + "'");
        if (scrw != null) {
            //������
            scrw.set("SCRW_RWZT_CODE", "RWZT02");
            scrw.set("SCRW_RWZT_NAME", "������");
            DynaBean update = serviceTemplate.update(scrw);
            if (update == null) {
                ret.setMessage("����ʧ��");
            }
        } else {
            ret.setMessage("���������񵥲����ڻ��ѱ�ɾ��");
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
                        //������
                        scrw.set("SCRW_RWZT_CODE", "RWZT02");
                        scrw.set("SCRW_RWZT_NAME", "������");
                        //���������񵥵�����
                        DynaBean update = serviceTemplate.update(scrw);
//                        serviceTemplate.update(jgmes_base_gdcptm);
                        if (update == null) {
                            ret.setMessage("�������񵥿���ʧ�ܣ�");
                        }

                    } else {
                        ret.setMessage("������󶨵��������񵥲����ڻ��ѱ�ɾ����");
                    }
                } else {
                    ret.setMessage("δ��ѯ�����������Ϣ");
                }

            } else {
                ret.setMessage("����Ų���Ϊ�գ�");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ret.setMessage("�쳣����");
        }
        return ret;
    }

    /*
     * @Author Jiansong Lu
     * @Description ������
     * @Date 10:44 2019/12/3
     * @Param [barCode, ProductionTaskCode]
     * @return com.jgmes.util.JgmesResult<java.util.HashMap>
     **/
    @Override
    public JgmesResult<HashMap> bindingBarCode(String barCode, String ProductionTaskCode) {
        JgmesResult<HashMap> ret = new JgmesResult<>();
        if (StringUtil.isNotEmpty(barCode)&&StringUtil.isNotEmpty(ProductionTaskCode)){
            //��ȡ�������Ϣ
            DynaBean jgmes_base_gdcptm = serviceTemplate.selectOne("JGMES_BASE_GDCPTM", "and GDCPTM_TMH='" + barCode + "'");
            //��ȡ����������Ϣ
            DynaBean jgmes_plan_scrw = serviceTemplate.selectOne("JGMES_PLAN_SCRW", "and SCRW_RWDH='" + ProductionTaskCode + "'");
            if (jgmes_plan_scrw!=null){
                //��У���������񵥵��Ų�������������д��ڵ������������������������ڻ�����Ų��������򲻽��а�����ʾ
                Integer scrw_pcsl = jgmes_plan_scrw.getInt("SCRW_PCSL");
                List<DynaBean> jgmes_base_gdcptm1 = serviceTemplate.selectList("JGMES_BASE_GDCPTM", "and GDCPTM_SCRWDH='" + ProductionTaskCode + "' and GDCPTM_TMH !='"+barCode+"'");
                if (scrw_pcsl<=jgmes_base_gdcptm1.size()){
                    ret.setMessage("������������󶨵������������������л�������������");
                    return ret;
                }

                if (jgmes_base_gdcptm!=null){
                    //У������ŵĹ����������������Ĺ��������Ƿ�һ��
                    if (!jgmes_plan_scrw.getStr("SCRW_GDHM").equals(jgmes_base_gdcptm.getStr("GDCPTM_GDHM"))) {
                        ret.setMessage("������󶨵Ĺ������������񵥵Ĺ�����һ�£��޷���");
                        return ret;
                    }
                    if (StringUtil.isEmpty(jgmes_base_gdcptm.getStr("GDCPTM_SCRWDH"))){
                        jgmes_base_gdcptm.set("JGMES_PLAN_SCRW_ID", jgmes_plan_scrw.get("JGMES_PLAN_SCRW_ID"));
                        jgmes_base_gdcptm.set("GDCPTM_SCRWDH", jgmes_plan_scrw.get("SCRW_RWDH"));
                        serviceTemplate.update(jgmes_base_gdcptm);
                    }
                }else{
                    ret.setMessage("������Ų����ڻ��ѱ�ɾ����");
                }
            }else {
                ret.setMessage("���������񵥲����ڻ��ѱ�ɾ����");
            }
        }else{
            ret.setMessage("��������������񵥺Ų���Ϊ�գ�");
        }
        return ret;
    }



    @Override
    public JgmesResult<HashMap> delScrw(String scrwId) {
        JgmesResult<HashMap> ret = new JgmesResult<>();
        try {
            if (StringUtil.isNotEmpty(scrwId)) {
                //�Ȼ�ȡ����������Ϣ
                DynaBean jgmes_plan_scrw = serviceTemplate.selectOneByPk("JGMES_PLAN_SCRW", scrwId);
                if (jgmes_plan_scrw != null) {
                    //��ʼУ������������Ƿ��ܱ�ɾ��������Ϊ������������û�и��������񵥺ŵ���Ϣ
                    List<DynaBean> jgmes_pb_bgsj = serviceTemplate.selectList("JGMES_PB_BGSJ", "and BGSJ_SCRWID='" + scrwId + "'");
                    if (jgmes_pb_bgsj.size() > 0) {
                        ret.setMessage("�������������б�����¼������ɾ��");
                    } else {
                        //���¹��������Ų�������δ�Ų�����
                        String scrw_gdhm = jgmes_plan_scrw.getStr("SCRW_GDHM");//�������룬��ʤ�����ж�������͹�������һ��
                        int scrw_pcsl = jgmes_plan_scrw.getInt("SCRW_PCSL");//�Ų�����
                        DynaBean jgmes_plan_gdlb = serviceTemplate.selectOne("JGMES_PLAN_GDLB", "and GDLB_GDHM='" + scrw_gdhm + "'");
                        if (jgmes_plan_gdlb != null) {
                            jgmes_plan_gdlb.set("GDLB_YPCSL", jgmes_plan_gdlb.getInt("GDLB_YPCSL") - scrw_pcsl);
                            jgmes_plan_gdlb.set("GDLB_WPCSL", jgmes_plan_gdlb.getInt("GDLB_WPCSL") + scrw_pcsl);
                            //��ȡ���������񵥰󶨵�����ţ����н�����
                            List<DynaBean> jgmes_base_gdcptm = serviceTemplate.selectList("JGMES_BASE_GDCPTM", "and JGMES_PLAN_SCRW_ID='" + scrwId + "'");
                            serviceTemplate.update(jgmes_plan_gdlb);
                            for (DynaBean dynaBean : jgmes_base_gdcptm) {
                                dynaBean.set("JGMES_PLAN_SCRW_ID", "");
                                dynaBean.set("GDCPTM_SCRWDH", "");
                                serviceTemplate.update(dynaBean);
                            }
                            int i = serviceTemplate.deleteByIds(scrwId, "JGMES_PLAN_SCRW", "JGMES_PLAN_SCRW_ID");
                            if (i == 0) {
                                ret.setMessage("ɾ��ʧ�ܣ�������");
                            }
                        }else{
                            ret.setMessage("��ȡ������Ϣʧ�ܣ�");
                        }
                    }
                } else {
                    ret.setMessage("��ȡ����������Ϣʧ�ܣ�");
                }
            } else {
                ret.setMessage("û�л�ȡ��Ҫɾ��������������Ϣ��");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ret.setMessage("�쳣����");
        }
        return ret;
    }

    /*
     * @Author Jiansong Lu
     * @Description ��ʤERP����ͬ��
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
            // �������ݿ�����
//            Class.forName(driver);
//            conn = DriverManager.getConnection(url, username, password);
            System.out.println(driver+"----"+url+"----"+username+"----"+password);
            System.out.println("���ݿ����ӳɹ�");
        } catch (Exception e) {
            System.out.println("���ݿ�����ʧ��");
            e.printStackTrace();

        }

//        JdbcUtil.
//
//
//        return conn;
    }
    /*
     * @Author Jiansong Lu
     * @Description ��ʤERP����ͬ���ӿ�
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
                logger.info("���ݿ�����ʧ��");
                System.out.println("���ݿ�����ʧ�ܣ�");
                return;
            }
            String SQL = "select * from k3_icmo";
//            String SQL = "select * from k3_icmo where ��������='20181114E112'";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(SQL);
            //��ȡϵͳ��������������ʤ����ͬ������·�߰���Ϣ�������룺SSGYLX
            String gylx_id = "";
            String gylx_gylxname="";
            DynaBean jgmes_xtgl_xtcs = serviceTemplate.selectOne("JGMES_XTGL_XTCS", "and XTCS_CXFL2_CODE='SSGYLX'");
            if (jgmes_xtgl_xtcs!=null){
                //����·�߱��
                String xtcs_csz = jgmes_xtgl_xtcs.getStr("XTCS_CSZ");
                DynaBean jgmes_gygl_gylx = serviceTemplate.selectOne("JGMES_GYGL_GYLX", "and GYLX_GYLXNUM='" + xtcs_csz + "'");
                if (jgmes_gygl_gylx!=null){
                    gylx_id = jgmes_gygl_gylx.getStr("GYLX_ID");
                    gylx_gylxname = jgmes_gygl_gylx.getStr("GYLX_GYLXNAME");
                }
            }else{
                logger.info("��ʤ����ͬ��ERP�󶨹���·��Ϊ�գ�");
                System.out.println("��ʤ����ͬ��ERP�󶨹���·��Ϊ�գ�");
            }
            DynaBean jgmes_sys_tmbz = serviceTemplate.selectOne("JGMES_SYS_TMBZ", "and TMBZ_TMFL_CODE='CPM'");
            DynaBean print = new DynaBean();
            if (jgmes_sys_tmbz != null) {
                /* �ӱ��Զ�д�� */
                print.set(BeanUtils.KEY_TABLE_CODE, "JGMES_BASE_CPTMYYGG");
                /* Ӧ�ù������� */
                print.set("CPTMYYGG_YYGGMC", "��Ʒ�������");
                //�������ʣ�Ψһ��
                print.set("CPTMYYGG_TMXZ_CODE", "TMXZX02");
                print.set("CPTMYYGG_TMXZ_NAME", "Ψһ��");//�������ʣ�Ψһ��
                print.set("CPTMYYGG_TMLX_CODE", "TMLX01");//��������
                print.set("CPTMYYGG_TMLX_NAME", "��Ʒ����");//��������
                print.set("CPTMYYGG_BQMB", jgmes_sys_tmbz.get("TMBZ_BQMBWJ"));//ģ���ļ�
                print.set("CPTMYYGG_BQCS", jgmes_sys_tmbz.get("TMBZ_BQCSMB"));//ģ�����
                print.set("CPTMYYGG_MTMSL", 1);//ÿ��������
                print.set("CPTMYYGG_BARCODEMODEL_CODE", "BARCODEMODEL01");//�������ɷ�ʽ
                print.set("CPTMYYGG_TMSCLX_CODE", "TMSCLX03");//�����������ͣ����չ�������
                print.set("CPTMYYGG_TMSCLX_NAME", "����������");//�����������ͣ����չ�������
                print.set("CPTMYYGG_TMGZBH", jgmes_sys_tmbz.get("TMBZ_YZZZ"));//��������ţ�ȡ��֤�����ֶ�
                print.set("CPTMYYGG_STATUS_CODE", 1);//״̬
                print.set("CPTMYYGG_STATUS_NAME", "����");//
            }
            while (rs.next()) {
                try {
                    String gd = rs.getString("��������");//������񵥵���mes��������
                    String ddh = rs.getString("���۶�����");//������۶����ŵ���mes������
                    String country = rs.getString("���ڹ���");//������ڹ��ҵ���mes���۹���
                    String cpCode = rs.getString("���ϳ�����");//������ϳ���������������ϲ�Ʒ���
                    String cpName = rs.getString("��������");//����������Ƶ����������ϲ�Ʒ����
                    String unit = rs.getString("��λ");//�����λ����mes�������ϼ�����λ
                    Integer quantity = rs.getInt("�ƻ���������");//����ƻ�������������mes�������������Ų�������δ�Ų���������������
                    Integer shortFallQuantity = rs.getInt("��װǷ����");//����ƻ�������������mes������װǷ��������ERP����
                    Integer incomingQuantity = rs.getInt("�������");//����ƻ������������mes�����������ERP����
                    String dateOfDelivery = rs.getString("������ŵ����");//���������ŵ���ڵ���mesOTD���ڣ�������������
                    String kingdeeStratDate = rs.getString("��װ��������");//�����װ�������ڵ���mes��װ��������
                    //�ռ�����������Ϣ
                    if (StringUtil.isNotEmpty(cpCode)){
                        //�Ȳ��Ҹ����������Ƿ����
                        DynaBean jgmes_base_productdata = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", "and PRODUCTDATA_BH='" + cpCode + "'");
                        if (jgmes_base_productdata==null){
                            DynaBean productdata = new DynaBean();
                            productdata.set(BeanUtils.KEY_TABLE_CODE, "JGMES_BASE_PRODUCTDATA");
                            productdata.set("PRODUCTDATA_BH",cpCode);
                            productdata.set("PRODUCTDATA_NAME",cpName);
                            productdata.set("PRODUCTDATA_JLDW_NAME",unit);
                            productdata.set("PRODUCTDATA_STATUS_CODE",1);
                            productdata.set("PRODUCTDATA_STATUS_NAME","����");
                            productdata.set("PRODUCTDATA_CPFL_NAME","���չʾ��");
                            productdata.set("PRODUCTDATA_WLTYPE_CODE","CP");
                            productdata.set("PRODUCTDATA_WLTYPE_NAME","��Ʒ");
                            productdata.set("PRODUCTDATA_CPGYLXID",gylx_id);
                            productdata.set("PRODUCTDATA_CPGYLX", gylx_gylxname);
                            String pk = JEUUID.uuid();
                            productdata.set(BeanUtils.KEY_PK_CODE, pk);
                            productdata.set("JGMES_BASE_PRODUCTDATA_ID", pk);
                            serviceTemplate.buildModelCreateInfo(productdata);
                            serviceTemplate.insert(productdata);
                            //�ӱ���Ϣ
                            if (print!=null){
                                DynaBean clone = print.clone();
                                clone.set("CPTMYYGG_CPBH", cpCode);//��Ʒ���
                                String uuid = JEUUID.uuid();
                                clone.set("CPTMYYGG_YYGZBH", uuid);
                                clone.set("JGMES_BASE_CPTMYYGG_ID", uuid);
                                clone.set(BeanUtils.KEY_PK_CODE, uuid);
                                clone.set("JGMES_BASE_PRODUCTDATA_ID", pk);
                                serviceTemplate.buildModelCreateInfo(clone);
                                serviceTemplate.insert(clone);
                            }

                            //����·�߹����Ӧ����Ϣ
                            if (StringUtil.isNotEmpty(gylx_id)){
                                List<DynaBean> jgmes_gygl_gylxgx = serviceTemplate.selectList("JGMES_GYGL_GYLXGX", "and GYLX_ID='"+gylx_id+"' order by SY_ORDERINDEX");
                                Integer index = 0;
                                for (DynaBean jgmesGyglGylxgx : jgmes_gygl_gylxgx) {
                                    //��ȡ��Ϣ
                                    String gygxid = jgmesGyglGylxgx.getStr("GYLXGX_ID");//���չ���ID
                                    String gxid = jgmesGyglGylxgx.getStr("GYLXGX_GXID");//����ID
                                    String gx = jgmesGyglGylxgx.getStr("GYLXGX_GXNAME");//����
                                    String pxzd = jgmesGyglGylxgx.getStr("SY_ORDERINDEX");//�����ֶ�
                                    String gxbm = jgmesGyglGylxgx.getStr("GYLXGX_GXNUM");//�������
                                    String gwbh = "";//��λ����
                                    String gwmc = "";//��λ����
                                    String gwid = "";//��λ����ID
                                    //�˴�д�����߱���,��ȡ���߹�λ
                                    List<DynaBean> jgmes_base_gw = serviceTemplate.selectList("JGMES_BASE_GW", "and JGMES_BASE_CXSJ_ID='"+CXID+"'  ORDER BY GW_GXSXH");
                                    if (jgmes_base_gw.size()<jgmes_gygl_gylxgx.size()){
                                        logger.error("����·�߹����������ڲ��߹�λ����,����������");
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
                            logger.info("��Ʒ"+cpCode+"�Ѵ��ڣ�");
                        }
                    }else{
                        logger.error("ERP��Ʒ���Ϊ�գ�");
                        return;
                    }
                    //�ռ�������Ϣ
                    DynaBean gdBean = new DynaBean();
                    //�жϹ����Ƿ����
                    DynaBean jgmes_plan_gdlb = serviceTemplate.selectOne("JGMES_PLAN_GDLB", "and GDLB_GDHM='" + gd + "'");
                    if (jgmes_plan_gdlb!=null){
                        //���������ERP��Ʒ���������ERP��װǷ��������ERP��װ��������
                        jgmes_plan_gdlb.set("GDLB_ERPCPRKSL",incomingQuantity);//ERP��Ʒ�������
                        jgmes_plan_gdlb.set("GDLB_ERPZZQCSL",shortFallQuantity);//ERP��װǷ������
                        if (StringUtil.isNotEmpty(kingdeeStratDate)){
                            gdBean.set("GDLB_ERPZZKGRQ",sdf.format(sdf.parse(kingdeeStratDate)));//ERP��װ��������
                        }else{
                            gdBean.set("GDLB_ERPZZKGRQ","");//ERP��װ��������
                        }
                        serviceTemplate.update(jgmes_plan_gdlb);
                    }else{
                        //��������������빤��
                        gdBean.set(BeanUtils.KEY_TABLE_CODE, "JGMES_PLAN_GDLB");
                        gdBean.set("GDLB_RQ",sdf.format(new Date()));//������������
                        gdBean.set("GDLB_DDHM",ddh);//������
                        gdBean.set("GDLB_GDHM",gd);//��������
                        gdBean.set("GDLB_QGRQ",sdf.format(new Date()));//�빺���ڣ��ݶ�����
                        if (StringUtil.isNotEmpty(dateOfDelivery)){
                            gdBean.set("GDLB_OTDRQ",sdf.format(sdf.parse(dateOfDelivery)));//OTD����
                            gdBean.set("GDLB_DDJHRQ",sdf.format(sdf.parse(dateOfDelivery)));//��������
                        }else{
                            gdBean.set("GDLB_OTDRQ","");//OTD����
                            gdBean.set("GDLB_DDJHRQ","");//��������
                        }
                        gdBean.set("GDLB_CPBH",cpCode);//��Ʒ����
                        gdBean.set("GDLB_NAME",cpName);//��Ʒ����
                        gdBean.set("GDLB_DDSL",quantity);//��������
                        gdBean.set("GDLB_XPCSL",quantity);//���Ų�����
                        gdBean.set("GDLB_WPCSL",quantity);//δ�Ų�����
                        gdBean.set("GDLB_GDSL",quantity);//��������
                        gdBean.set("GDLB_GDZT_CODE",1);//����״̬code
                        gdBean.set("GDLB_GDZT_NAME","δ��");//����״̬
                        gdBean.set("GDLB_CKGJ",country);//���۹���
                        gdBean.set("GDLB_ERPCPRKSL",incomingQuantity);//ERP��Ʒ�������
                        gdBean.set("GDLB_ERPZZQCSL",shortFallQuantity);//ERP��װǷ������
                        if (StringUtil.isNotEmpty(kingdeeStratDate)){
                            gdBean.set("GDLB_ERPZZKGRQ",sdf.format(sdf.parse(kingdeeStratDate)));//ERP��װ��������
                        }else{
                            gdBean.set("GDLB_ERPZZKGRQ","");//ERP��װ��������
                        }
                        gdBean.set("GDLB_BZ","ERPͬ������");//ERP��װ��������
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