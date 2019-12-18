package com.jgmes.action;

import com.je.core.action.DynaAction;
import com.je.core.util.JEUUID;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.DynaBean;
import com.jgmes.service.FinalQualityAssureanceServices;
import com.jgmes.service.FinalQualityAssureanceServicesImpl;
import com.jgmes.util.JgmesCommon;
import com.jgmes.util.JgmesResult;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

/**
 * FQA Action
 *
 * @author ljs
 * @version 2019-06-04 11:58:00
 * @see /jgmes/finalQualityAssureanceAction!load.action
 */
@Component("finalQualityAssureanceAction")
@Scope("prototype")
public class FinalQualityAssureanceAction extends DynaAction {

    private static final long serialVersionUID = 1L;

    public void load() {
        toWrite("hello Action");
    }

    FinalQualityAssureanceServices fqaServices = new FinalQualityAssureanceServicesImpl();

    public void getBarcodeData() {
        String barCode = request.getParameter("Barcode");//SN��
        String OuterBarCode = request.getParameter("OuterBarCode");//������
        String powerLineCode = request.getParameter("powerLineCode");//��Դ����
        String BoxCode = request.getParameter("BoxCode");//������
        String ssh = request.getParameter("ssh");
        JgmesResult<List<HashMap>> ret = fqaServices.getBarcodeData(barCode, OuterBarCode, powerLineCode, BoxCode, ssh);
        toWrite(jsonBuilder.toJson(ret));
    }

    public void saveFQAData() {
        String barCode = request.getParameter("Barcode");//SN��
        String OuterBarCode = request.getParameter("OuterBarCode");//������
        String powerLineCode = request.getParameter("powerLineCode");//��Դ����
        String BoxCode = request.getParameter("BoxCode");//������
        JgmesResult<List<HashMap>> ret = fqaServices.saveFQAData(barCode, OuterBarCode, powerLineCode, BoxCode);
        toWrite(jsonBuilder.toJson(ret));
    }

    public void getSNBarCodeData() {
        String barCode = request.getParameter("Barcode");//SN��
        String ssh = request.getParameter("ssh");     //����˳���
        JgmesResult<List<HashMap>> ret = fqaServices.getSNBarcodeData(barCode, ssh);
        toWrite(jsonBuilder.toJson(ret));
    }

    public void save() {
        String jsonStr = request.getParameter("jsonStr");//����Json
        String jsonStrDetail = request.getParameter("jsonStrDetail");     //�ӱ�Json
        JgmesResult<List<HashMap>> ret = fqaServices.save(jsonStr, jsonStrDetail, request);
        toWrite(jsonBuilder.toJson(ret));
    }

    /*
     * @Author Jiansong Lu
     * @Description �������ϰ󶨣����޸�
     * @Date 14:23 2019/12/11
     * @Param []
     * @return void
     **/
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


}