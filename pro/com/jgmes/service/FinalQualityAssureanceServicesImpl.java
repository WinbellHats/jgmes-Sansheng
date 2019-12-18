package com.jgmes.service;

import com.je.core.security.SecurityUserHolder;
import com.je.core.service.PCDynaServiceTemplate;
import com.je.core.service.PCServiceTemplate;
import com.je.core.util.JEUUID;
import com.je.core.util.SpringContextHolder;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.BeanUtils;
import com.je.core.util.bean.DynaBean;
import com.je.rbac.model.Department;
import com.je.rbac.model.EndUser;
import com.je.rbac.service.UserManager;
import com.jgmes.util.JgmesCommon;
import com.jgmes.util.JgmesResult;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * FQA services
 *
 * @author admin
 * @version 2019-06-04 11:44:52
 */
@Component("finalQualityAssureanceServices")
public class FinalQualityAssureanceServicesImpl implements FinalQualityAssureanceServices {

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

    public void load() {
        System.out.println("hello serviceimpl");
    }

    @Override
    public JgmesResult<List<HashMap>> saveFQAData(String barCode, String OuterBarCode, String powerLineCode, String BoxCode) {
        if (serviceTemplate == null) {
            serviceTemplate = SpringContextHolder.getBean("PCDynaServiceTemplate");
        }
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        List<DynaBean> cpBeanList = serviceTemplate.selectList("JGMES_BASE_GDCPTM", "and GDCPTM_TMH='" + barCode + "'");
        String ddhm = "";
        if (cpBeanList.size() > 0) {
            ddhm = cpBeanList.get(0).getStr("GDCPTM_DDHM");//��������
        }
        DynaBean FQABean = new DynaBean();
        FQABean.set(BeanUtils.KEY_TABLE_CODE, "JGMES_ZLGL_FQAJLB");
        FQABean.set("FQAJLB_DDHM", ddhm);
        FQABean.set("FQAJLB_SNM", barCode);
        FQABean.set("FQAJLB_WXTM", OuterBarCode);
        FQABean.set("FQAJLB_DYX", powerLineCode);
        FQABean.set("FQAJLB_FJH", BoxCode);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String NowDate = df.format(new Date());
        FQABean.set("FQAJLB_RQ", NowDate);
        DynaBean insert = serviceTemplate.insert(FQABean);
        if (insert != null) {
            ret.IsSuccess = true;
        } else {
            ret.setMessage("��������ʧ��");
        }
        return ret;
    }

    @Override
    public JgmesResult<List<HashMap>> getBarcodeData(String barCode, String OuterBarCode, String powerLineCode, String BoxCode, String ssh) {
        if (serviceTemplate == null) {
            serviceTemplate = SpringContextHolder.getBean("PCDynaServiceTemplate");
        }
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        if (StringUtil.isNotEmpty(barCode)) {
            String sql = "select * from JGMES_PB_BGSJ where BGSJ_TMH='" + barCode + "' and BGSJ_GXSXH ='" + ssh + "'";
            if (StringUtil.isNotEmpty(OuterBarCode)) {
                if (!barCode.equals(OuterBarCode)) {
                    ret.setMessage("��������SN�벻��");
                    return ret;
                }
            }
            //��Դ������͸��������붼��Ϊ��
            if (StringUtil.isNotEmpty(powerLineCode) && StringUtil.isNotEmpty(BoxCode)) {
                String fullSql = "select * from JGMES_PB_BGSJ z " +
                        "left join JGMES_PB_BGSJZB b1 on z.JGMES_PB_BGSJ_ID = b1.JGMES_PB_BGSJ_ID " +
                        "left join JGMES_PB_BGSJZB b2 on z.JGMES_PB_BGSJ_ID = b2.JGMES_PB_BGSJ_ID " +
                        " where z.BGSJ_TMH='" + barCode + "' and b1.BGSJZB_TMH='" + powerLineCode + "' and b2.BGSJZB_TMH='" + BoxCode + "'and z.BGSJ_GXSXH ='" + ssh + "'";
                List<DynaBean> fullBeans = serviceTemplate.selectListBySql(fullSql);
                if (fullBeans.size() > 0) {
                    ret.IsSuccess = true;
                } else {
                    ret.setMessage("��Դ�߻򸽼���������SN�벻ƥ��");
                    String OneSql = "select * from JGMES_PB_BGSJ z " +
                            "left join JGMES_PB_BGSJZB b1 on z.JGMES_PB_BGSJ_ID = b1.JGMES_PB_BGSJ_ID" +
                            " where z.BGSJ_TMH='" + barCode + "' and b1.BGSJZB_TMH='" + powerLineCode + "'and z.BGSJ_GXSXH ='" + ssh + "'";
                    List<DynaBean> oneBeans = serviceTemplate.selectListBySql(OneSql);
                    if (oneBeans.size() > 0) {
                        ret.setMessage("������������SN�벻ƥ��");
                    } else {
                        ret.setMessage("��Դ����SN�벻ƥ��");
                    }
                }
            } else {
                String tm = "";
                //��Դ������͸�������������һ��Ϊ��
                if (StringUtil.isNotEmpty(BoxCode)) {
                    tm = BoxCode;
                    ret.setMessage("��������SN���벻��");//�������ϲ���Ч
                }
                if (StringUtil.isNotEmpty(powerLineCode)) {
                    tm = powerLineCode;
                    ret.setMessage("��Դ����SN���벻��");//�������ϲ���Ч
                }
                if (StringUtil.isNotEmpty(tm)) {
                    String OneSql = "select * from JGMES_PB_BGSJ z " +
                            "left join JGMES_PB_BGSJZB b1 on z.JGMES_PB_BGSJ_ID = b1.JGMES_PB_BGSJ_ID" +
                            " where z.BGSJ_TMH='" + barCode + "' and b1.BGSJZB_TMH='" + tm + "'and z.BGSJ_GXSXH ='" + ssh + "'";
                    System.out.println(OneSql);
                    List<DynaBean> oneBeans = serviceTemplate.selectListBySql(OneSql);
                    if (oneBeans.size() > 0) {
                        ret.IsSuccess = true;
                    }
                } else {
                    //���ǿ�
                    List<DynaBean> firstBeans = serviceTemplate.selectListBySql(sql);
                    ret.setMessage("��SN��û�������ù����޷�У��");//�������ϲ���Ч
                    if (firstBeans.size() > 0) {
                        ret.IsSuccess = true;
                    }
                }
            }
        } else {
            ret.setMessage("����ɨSN��");
        }
        return ret;
    }

    @Override
    public JgmesResult<List<HashMap>> getSNBarcodeData(String barCode, String ssh) {
        if (serviceTemplate == null) {
            serviceTemplate = SpringContextHolder.getBean("PCDynaServiceTemplate");
        }
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        if (StringUtil.isNotEmpty(barCode) && StringUtil.isNotEmpty(ssh)) {
            List<DynaBean> dynaBeans = serviceTemplate.selectList("JGMES_PB_BGSJ", "and BGSJ_TMH='" + barCode + "' and BGSJ_GXSXH ='" + ssh + "'");
            if (dynaBeans.size() > 0) {
                String bgsjId = dynaBeans.get(0).getStr("JGMES_PB_BGSJ_ID");
                if (StringUtil.isNotEmpty(bgsjId)) {
                    List<DynaBean> jgmes_pb_bgsjzb = serviceTemplate.selectList("JGMES_PB_BGSJZB", "and JGMES_PB_BGSJ_ID='" + bgsjId + "'");
                    if (jgmes_pb_bgsjzb.size() > 0) {
                        for (DynaBean dynaBean : jgmes_pb_bgsjzb) {

                            dynaBean.set("same", 0);
                        }
                        ret.Data = ret.getValues(jgmes_pb_bgsjzb);
                        ret.IsSuccess = true;
                    } else {
                        ret.setMessage("�����뱨��������û��FQA�����");
                    }
                } else {
                    ret.setMessage("��ȡ��������ʧ��");
                }
            } else {
                ret.setMessage("��SN��û�������ù����޷�У��");
            }
        }
        return ret;


    }

    @Override
    public JgmesResult<List<HashMap>> save(String jsonStr, String jsonStrDetail,HttpServletRequest request) {
        if (serviceTemplate == null) {
            serviceTemplate = SpringContextHolder.getBean("PCDynaServiceTemplate");
        }
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        try {
            if (StringUtil.isNotEmpty(jsonStr)) {
                DynaBean mainBean = jgmesCommon.getDynaBeanByJsonStr("JGMES_ZLGL_FQAJLB", jsonStr);
                if (mainBean != null) {
                    DynaBean mainBean1 = serviceTemplate.selectOne("JGMES_ZLGL_FQAJLB", " and JGMES_ZLGL_FQAJLB_ID = '" + mainBean.getStr("JGMES_ZLGL_FQAJLB_ID") + "'");
                    if (mainBean1 != null) {
                        serviceTemplate.update(mainBean1);
                    } else {
                        mainBean.set("JGMES_ZLGL_FQAJLB_ID", JEUUID.uuid());
                        mainBean.set("FQAJLB_RQ", jgmesCommon.getCurrentTime());
                        String barCode = mainBean.getStr("FQAJLB_SNM");
                        List<DynaBean> cpBeanList = serviceTemplate.selectList("JGMES_BASE_GDCPTM", "and GDCPTM_TMH='" + barCode + "'");
                        if (cpBeanList.size()>0){
                            mainBean.set("FQAJLB_DDHM",cpBeanList.get(0).getStr("GDCPTM_DDHM"));//��������
                        }
                        serviceTemplate.buildModelCreateInfo(mainBean);
                        serviceTemplate.insert(mainBean);
                    }
                }
                if (StringUtil.isNotEmpty(jsonStrDetail)) {
                    List<DynaBean> zbBean = jgmesCommon.getListDynaBeanByJsonStr("JGMES_ZLGL_FQAJLBZB", jsonStrDetail);
                    for (DynaBean dynaBean : zbBean) {
                        dynaBean.set("JGMES_ZLGL_FQAJLB_ID", mainBean.getStr("JGMES_ZLGL_FQAJLB_ID"));
//                        dynaBean.set("JGMES_ZLGL_FQAJLBZB_ID", JEUUID.uuid());
                        serviceTemplate.buildModelCreateInfo(dynaBean);
                    }
                    serviceTemplate.insert(zbBean);
                }
                ret.IsSuccess=true;
            }else {
                ret.setMessage("δ��ȡ����������");
            }

        } catch (Exception e) {
            e.printStackTrace();
            ret.setMessage("ϵͳ�����쳣������ϵ����Ա��");
        }
        return ret;
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
        if (serviceTemplate == null) {
            serviceTemplate = SpringContextHolder.getBean("PCDynaServiceTemplate");
        }
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