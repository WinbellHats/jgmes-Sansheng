package com.jgmes.action;

import com.je.core.action.DynaAction;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.DynaBean;
import com.jgmes.service.SanshengService;
import com.jgmes.util.JgmesCommon;
import com.jgmes.util.JgmesResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

/**
 * ?????????
 *
 * @author admin
 * @version 2019-11-13 20:45:29
 * @see /jgmes/sanShengAction!load.action
 */
@Component("sanShengAction")
@Scope("prototype")
public class SanShengAction extends DynaAction {

    private static final long serialVersionUID = 1L;

    @Autowired
    private SanshengService sanshengService;

    /**
     * Description: ����������ȡ��ǰ�����������б�
     *
     * @Param: * @param null
     * @return:
     * @author: ljs
     * @date: 2019/11/13 20:51
     */
    public void getProductionTaskData() {
        JgmesResult<List<HashMap>> ret = new JgmesResult<>();
        String pageSize = request.getParameter("pageSize");
        String currPage = request.getParameter("currPage");
        String pcDate = request.getParameter("pcDate");
        String cpKeyWord = request.getParameter("cpKeyWord");
        String DH = request.getParameter("DH");
//        String IsFirstStation = request.getParameter("IsFirstStation");//���׹�վ�Ļ���ֵΪ1
        StringBuilder selectSql = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //�Ų�����
        if (StringUtil.isNotEmpty(pcDate)) {
            try {
                pcDate = pcDate.replaceAll("/", "-");
                pcDate = sdf.format(sdf.parse(pcDate));
                selectSql.append(" and SCRW_PCRQ='" + pcDate + "'");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        //��Ʒ��Ϣ
        if (StringUtil.isNotEmpty(cpKeyWord)) {
            selectSql.append(" and (SCRW_CPBH like '%" + cpKeyWord + "%' or SCRW_NAME like '%" + cpKeyWord + "%' or SCRW_CPGG like '%" + cpKeyWord + "%') ");
        }
        //������Ϣ
        if (StringUtil.isNotEmpty(DH)) {
            selectSql.append(" and (SCRW_DDHM like '%" + DH + "%' or SCRW_RWDH like '%" + DH + "%' or SCRW_GDHM like '%" + DH+ "%')");
        }

        List<DynaBean> jgmes_plan_scrw = serviceTemplate.selectList("JGMES_PLAN_SCRW", selectSql.toString());
        ret.TotalCount = Long.valueOf(jgmes_plan_scrw.size());
        if (pageSize != null && !"".equals(pageSize) && currPage != null && !"".equals(currPage)) {
            int start = Integer.parseInt(pageSize) * (Integer.parseInt(currPage) - 1);
            int size = Integer.parseInt(pageSize);
            selectSql.append("  LIMIT " + start + "," + size + "");
            List<DynaBean> jgmes_plan_scrw_limit = serviceTemplate.selectList("JGMES_PLAN_SCRW", selectSql.toString());
//            /* ��Ϊ�׹�վ�����ѯ�����������Ƿ��Ѿ������������񵥣��������󶨣���ͳ�� */
//            if (IsFirstStation!=null&&IsFirstStation.equals("1")) {
//                List<DynaBean> newScrwBean1 = new ArrayList<>();
//                for (DynaBean dynaBean : jgmes_plan_scrw_limit) {
//                    /* �󶨵ĸ���������������� */
//                    long bingingNum = serviceTemplate.selectCount("JGMES_BASE_GDCPTM", "and GDCPTM_SCRWDH='" + dynaBean.getStr("SCRW_RWDH") + "'");
//                    if (dynaBean.getLong("SCRW_PCSL")>bingingNum){
//                        newScrwBean1.add(dynaBean);
//                    }
//                }
//                jgmes_plan_scrw_limit = newScrwBean1;
//            }

            //������������ID��ȡ��������Ͷ���������������������ڶ��������������
            for (DynaBean dynaBean : jgmes_plan_scrw_limit) {
                String id = dynaBean.getStr("JGMES_PLAN_SCRW_ID");
                long first = serviceTemplate.selectCount("JGMES_PB_BGSJ", "and BGSJ_SCRWID='" + id + "' and BGSJ_GXSXH=1 and BGSJ_STATUS_CODE!=2");
                long second = serviceTemplate.selectCount("JGMES_PB_BGSJ", "and BGSJ_SCRWID='" + id + "' and BGSJ_GXSXH=2 and BGSJ_STATUS_CODE!=2");
                dynaBean.setLong("first",first);
                dynaBean.setLong("second",second);
            }
            ret.Data = ret.getValues(jgmes_plan_scrw_limit);
        }
        toWrite(jsonBuilder.toJson(ret));
    }

    /*
     * @Author Jiansong Lu
     * @Description �Ų�ҳ�桪����ȡ����
     * @Date 9:11 2019/12/3
     * @Param []
     * @return void
     **/
    public void getGdData() {
        String pageSize = request.getParameter("pageSize");
        String currPage = request.getParameter("currPage");
        String cpKeyWord = request.getParameter("KeyWord");
        String DH = request.getParameter("DH");
        StringBuilder selectSql = new StringBuilder();
        JgmesResult<List<HashMap>> ret = new JgmesResult<>();
        //ֻ��ȡû���Ų���ɵĹ����������Ų��������ڶ��������Ĺ���
        selectSql.append("and IFNULL(GDLB_YPCSL,'0')+0<IFNULL(GDLB_DDSL,'0')+0 ");
        //��Ʒ��Ϣ
        if (StringUtil.isNotEmpty(cpKeyWord)) {
            selectSql.append(" and (GDLB_CPBH like '%" + cpKeyWord + "%' or GDLB_NAME like '%" + cpKeyWord + "%' or GDLB_CPGG like '%" + cpKeyWord + "%') ");
        }
        //������Ϣ
        if (StringUtil.isNotEmpty(DH)) {
            selectSql.append(" and (GDLB_DDHM like '%" + DH + "%' or GDLB_GDHM like '%" + DH + "%') ");
        }
        List<DynaBean> jgmes_plan_gdlb = serviceTemplate.selectList("JGMES_PLAN_GDLB", selectSql.toString());
        ret.TotalCount = Long.valueOf(jgmes_plan_gdlb.size());
        selectSql.append(" order by GDLB_RQ desc");
        if (pageSize != null && !"".equals(pageSize) && currPage != null && !"".equals(currPage)) {
            int start = Integer.parseInt(pageSize) * (Integer.parseInt(currPage) - 1);
            int size = Integer.parseInt(pageSize);
            selectSql.append("  LIMIT " + start + "," + size + "");
            List<DynaBean> jgmes_plan_gdlb1 = serviceTemplate.selectList("JGMES_PLAN_GDLB", selectSql.toString());
            ret.Data = ret.getValues(jgmes_plan_gdlb1);
        }
        toWrite(jsonBuilder.toJson(ret));
    }

    /*
     * @Author Jiansong Lu
     * @Description ��ʤ�Ų������߼�����
     * @Date 14:45 2019/11/19
     * @Param
     * @return void
     **/
    public void SumitScheduling() {
        String sumitList = request.getParameter("sumitList");//��Ҫ�����Ų�������
        String cxCode = request.getParameter("cxCode");//���߱���
        JgmesResult<HashMap> ret = sanshengService.SumitScheduling(sumitList,cxCode);
        toWrite(jsonBuilder.toJson(ret));
    }

    /*
     * @Author Jiansong Lu
     * @Description ��ʤ��ɾ����������
     * @Date 9:09 2019/11/20
     * @Param []
     * @return void
     **/
    public void delScrw(){
        String scrwId = request.getParameter("scrwId");//��Ҫɾ������������ID
        JgmesResult<HashMap> ret = sanshengService.delScrw(scrwId);
        toWrite(jsonBuilder.toJson(ret));
    }

    /*
     * @Author Jiansong Lu
     * @Description ������������ID���������񵥿���
     * @Date 15:24 2019/11/20
     * @Param []
     * @return void
     **/
    public void StartScrw(){
        String taskCode = request.getParameter("taskCode");
        // MAC��ַ
        String mac = request.getParameter("mac");
        // �û�����  ����
        String userCode = request.getParameter("userCode");
        if (doCheck(userCode, mac).IsSuccess) {
            JgmesResult<HashMap> ret = sanshengService.startScrw(taskCode);
            toWrite(jsonBuilder.toJson(ret));
        }
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
     * @Author Jiansong Lu
     * @Description ��������Ż�ȡ�������񣬲�����
     * @Date 17:54 2019/11/23
     * @Param []
     * @return void
     **/
    public void startScrwByBarCode(){
        String barCode = request.getParameter("barCode");//�����
        String ProductionTaskCode = request.getParameter("ProductionTaskCode");//�������񵥺�
        // MAC��ַ
        String mac = request.getParameter("mac");
        // �û�����  ����
        String userCode = request.getParameter("userCode");
        if (doCheck(userCode, mac).IsSuccess) {
            JgmesResult<HashMap> ret = sanshengService.startScrwByBarCode(barCode,ProductionTaskCode);
            toWrite(jsonBuilder.toJson(ret));
        }

    }

    /*
     * @Author Jiansong Lu
     * @Description ��ȡ���в�����Ϣ,���ϣ���ʹ��
     * @Date 10:17 2019/11/25
     * @Param []
     * @return void
     **/
    public void getAllCxData(){
        JgmesResult<List<HashMap>> ret = new JgmesResult<>();
        List<DynaBean> jgmes_base_cxsj = serviceTemplate.selectList("JGMES_BASE_CXSJ", " and CXSJ_STATUS_CODE=1","CXSJ_CXBM,CXSJ_CXMC,JGMES_BASE_CXSJ_ID");
        if (jgmes_base_cxsj.size()>0){
            ret.Data = ret.getValues(jgmes_base_cxsj);
        }else {
            ret.setMessage("��ȡ������Ϣʧ�ܣ�");
        }
        toWrite(jsonBuilder.toJson(ret));
    }

    /*
     * @Author Jiansong Lu
     * @Description //���ݲ��߱�Ż�ȡ�ò����µ�����δ��ɵ���������
     * @Date 14:40 2019/11/25
     * @Param []
     * @return void
     **/
    public void getProductionTaskByProductionLineCode(){
        JgmesResult<List<HashMap>> ret = new JgmesResult<>();
        /* ���߱��� */
        String cxCode = request.getParameter("cxCode");
        /* MAC��ַ */
        String mac = request.getParameter("mac");
        /* �û�����  ���� */
        String userCode = request.getParameter("userCode");
        String pageSize = request.getParameter("pageSize");
        String currPage = request.getParameter("currPage");
        String cpKeyWord = request.getParameter("cpKeyWord");
        String DH = request.getParameter("DH");

        StringBuilder selectSql = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                /* ������Ϣ */
                if (StringUtil.isNotEmpty(cxCode)){
                    selectSql.append(" and SCRW_CXBM='" + cxCode + "'");
                }
                /* ��Ʒ��Ϣ */
                if (StringUtil.isNotEmpty(cpKeyWord)) {
                    selectSql.append(" and (SCRW_CPBH like '%" + cpKeyWord + "%' or SCRW_NAME like '%" + cpKeyWord + "%' or SCRW_CPGG like '%" + cpKeyWord + "%') ");
                }
                /* ������Ϣ */
                if (StringUtil.isNotEmpty(DH)) {
                    selectSql.append(" and (SCRW_DDHM like '%" + DH + "%' or SCRW_RWDH like '%" + DH + "%' or SCRW_GDHM like '%" + DH+ "%')");
                }
                /* �ų����״̬���������� */
                selectSql.append(" and SCRW_RWZT_CODE not in ('RWZT03','RWZT06') ");

                /* �ų��Ѿ����㹻����������������� */
                selectSql.append(" and SCRW_PCSL>(SELECT COUNT(1) FROM JGMES_BASE_GDCPTM WHERE GDCPTM_SCRWDH=SCRW_RWDH) ");
                List<DynaBean> jgmes_plan_scrw = serviceTemplate.selectList("JGMES_PLAN_SCRW", selectSql.toString());
                ret.Data = ret.getValues(jgmes_plan_scrw);
                ret.TotalCount = Long.valueOf(jgmes_plan_scrw.size());
                if (pageSize != null && !"".equals(pageSize) && currPage != null && !"".equals(currPage)) {
                    int start = Integer.parseInt(pageSize) * (Integer.parseInt(currPage) - 1);
                    int size = Integer.parseInt(pageSize);
                    selectSql.append("  LIMIT " + start + "," + size + "");
                    List<DynaBean> jgmes_plan_scrw_limit = serviceTemplate.selectList("JGMES_PLAN_SCRW", selectSql.toString());
                    for (DynaBean dynaBean : jgmes_plan_scrw_limit) {
                        String id = dynaBean.getStr("JGMES_PLAN_SCRW_ID");
                        long first = serviceTemplate.selectCount("JGMES_PB_BGSJ", "and BGSJ_SCRWID='" + id + "' and BGSJ_GXSXH=1 and BGSJ_STATUS_CODE!=2");
                        long second = serviceTemplate.selectCount("JGMES_PB_BGSJ", "and BGSJ_SCRWID='" + id + "' and BGSJ_GXSXH=2 and BGSJ_STATUS_CODE!=2");
                        dynaBean.setLong("first",first);
                        dynaBean.setLong("second",second);
                    }
                    ret.Data = ret.getValues(jgmes_plan_scrw_limit);
                }
            }catch (Exception e){
                e.printStackTrace();
                ret.setMessage(e.toString());
            }

            toWrite(jsonBuilder.toJson(ret));
        }
    }

    /*
     * @Author Jiansong Lu
     * @Description ���������������
     * @Date 14:28 2019/11/26
     * @Param []
     * @return void
     **/
    public void bindingBarCode(){
        /* ����� */
        String barCode = request.getParameter("barCode");
        /* �������񵥺� */
        String ProductionTaskCode = request.getParameter("ProductionTaskCode");
        /* MAC��ַ */
        String mac = request.getParameter("mac");
        /* �û�����  ���� */
        String userCode = request.getParameter("userCode");
        if (doCheck(userCode, mac).IsSuccess) {
            JgmesResult<HashMap> ret = sanshengService.bindingBarCode(barCode, ProductionTaskCode);
            toWrite(jsonBuilder.toJson(ret));
        }

    }

    /*
     * @Author Jiansong Lu
     * @Description ���SN���Ƿ��Ѿ�����������
     * @Date 10:27 2019/12/18
     * @Param []
     * @return void
     **/
    public void IsBindingBarCode(){
        /* ����� */
        String barCode = request.getParameter("barCode");
        /* MAC��ַ */
        String mac = request.getParameter("mac");
        /* �û�����  ���� */
        String userCode = request.getParameter("userCode");
        JgmesResult<List<HashMap>> ret = new JgmesResult<>();
        if (doCheck(userCode, mac).IsSuccess) {
            DynaBean jgmes_base_gdcptm = serviceTemplate.selectOne("JGMES_BASE_GDCPTM", "and GDCPTM_TMH='" + barCode + "'");
            if (jgmes_base_gdcptm!=null){
                /* У���������񵥺��Ƿ�Ϊ�գ�����δ�� */
                String scrwdh = jgmes_base_gdcptm.getStr("GDCPTM_SCRWDH");
                if (StringUtil.isEmpty(scrwdh)){
                    ret.setMessage("������δ�Ų���");
                }
            }else{
                ret.setMessage("δ֪����ţ�");
            }
        }
        toWrite(jsonBuilder.toJson(ret));
    }

}