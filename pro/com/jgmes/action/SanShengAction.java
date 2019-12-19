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
     * Description: 根据条件获取当前的生产任务列表
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
//        String IsFirstStation = request.getParameter("IsFirstStation");//是首工站的话，值为1
        StringBuilder selectSql = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //排产日期
        if (StringUtil.isNotEmpty(pcDate)) {
            try {
                pcDate = pcDate.replaceAll("/", "-");
                pcDate = sdf.format(sdf.parse(pcDate));
                selectSql.append(" and SCRW_PCRQ='" + pcDate + "'");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        //产品信息
        if (StringUtil.isNotEmpty(cpKeyWord)) {
            selectSql.append(" and (SCRW_CPBH like '%" + cpKeyWord + "%' or SCRW_NAME like '%" + cpKeyWord + "%' or SCRW_CPGG like '%" + cpKeyWord + "%') ");
        }
        //单据信息
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
//            /* 若为首工站，则查询该生产任务单是否已经绑定满生产任务单，若已满绑定，则不统计 */
//            if (IsFirstStation!=null&&IsFirstStation.equals("1")) {
//                List<DynaBean> newScrwBean1 = new ArrayList<>();
//                for (DynaBean dynaBean : jgmes_plan_scrw_limit) {
//                    /* 绑定的该生产任务的条码数 */
//                    long bingingNum = serviceTemplate.selectCount("JGMES_BASE_GDCPTM", "and GDCPTM_SCRWDH='" + dynaBean.getStr("SCRW_RWDH") + "'");
//                    if (dynaBean.getLong("SCRW_PCSL")>bingingNum){
//                        newScrwBean1.add(dynaBean);
//                    }
//                }
//                jgmes_plan_scrw_limit = newScrwBean1;
//            }

            //根据生产任务单ID获取上线数（投入数量），进线数量（第二个工序的数量）
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
     * @Description 排产页面——获取工单
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
        //只获取没有排产完成的工单，即已排产数量少于订单数量的工单
        selectSql.append("and IFNULL(GDLB_YPCSL,'0')+0<IFNULL(GDLB_DDSL,'0')+0 ");
        //产品信息
        if (StringUtil.isNotEmpty(cpKeyWord)) {
            selectSql.append(" and (GDLB_CPBH like '%" + cpKeyWord + "%' or GDLB_NAME like '%" + cpKeyWord + "%' or GDLB_CPGG like '%" + cpKeyWord + "%') ");
        }
        //单据信息
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
     * @Description 三胜排产功能逻辑处理
     * @Date 14:45 2019/11/19
     * @Param
     * @return void
     **/
    public void SumitScheduling() {
        String sumitList = request.getParameter("sumitList");//需要生成排产的数据
        String cxCode = request.getParameter("cxCode");//产线编码
        JgmesResult<HashMap> ret = sanshengService.SumitScheduling(sumitList,cxCode);
        toWrite(jsonBuilder.toJson(ret));
    }

    /*
     * @Author Jiansong Lu
     * @Description 三胜，删除生产任务单
     * @Date 9:09 2019/11/20
     * @Param []
     * @return void
     **/
    public void delScrw(){
        String scrwId = request.getParameter("scrwId");//需要删除的生产任务ID
        JgmesResult<HashMap> ret = sanshengService.delScrw(scrwId);
        toWrite(jsonBuilder.toJson(ret));
    }

    /*
     * @Author Jiansong Lu
     * @Description 根据生产任务单ID把生产任务单开工
     * @Date 15:24 2019/11/20
     * @Param []
     * @return void
     **/
    public void StartScrw(){
        String taskCode = request.getParameter("taskCode");
        // MAC地址
        String mac = request.getParameter("mac");
        // 用户编码  必填
        String userCode = request.getParameter("userCode");
        if (doCheck(userCode, mac).IsSuccess) {
            JgmesResult<HashMap> ret = sanshengService.startScrw(taskCode);
            toWrite(jsonBuilder.toJson(ret));
        }
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
     * @Author Jiansong Lu
     * @Description 根据条码号获取生产任务，并开工
     * @Date 17:54 2019/11/23
     * @Param []
     * @return void
     **/
    public void startScrwByBarCode(){
        String barCode = request.getParameter("barCode");//条码号
        String ProductionTaskCode = request.getParameter("ProductionTaskCode");//生产任务单号
        // MAC地址
        String mac = request.getParameter("mac");
        // 用户编码  必填
        String userCode = request.getParameter("userCode");
        if (doCheck(userCode, mac).IsSuccess) {
            JgmesResult<HashMap> ret = sanshengService.startScrwByBarCode(barCode,ProductionTaskCode);
            toWrite(jsonBuilder.toJson(ret));
        }

    }

    /*
     * @Author Jiansong Lu
     * @Description 获取所有产线信息,作废，不使用
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
            ret.setMessage("获取产线信息失败！");
        }
        toWrite(jsonBuilder.toJson(ret));
    }

    /*
     * @Author Jiansong Lu
     * @Description //根据产线编号获取该产线下的所有未完成的生产任务
     * @Date 14:40 2019/11/25
     * @Param []
     * @return void
     **/
    public void getProductionTaskByProductionLineCode(){
        JgmesResult<List<HashMap>> ret = new JgmesResult<>();
        /* 产线编码 */
        String cxCode = request.getParameter("cxCode");
        /* MAC地址 */
        String mac = request.getParameter("mac");
        /* 用户编码  必填 */
        String userCode = request.getParameter("userCode");
        String pageSize = request.getParameter("pageSize");
        String currPage = request.getParameter("currPage");
        String cpKeyWord = request.getParameter("cpKeyWord");
        String DH = request.getParameter("DH");

        StringBuilder selectSql = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                /* 产线信息 */
                if (StringUtil.isNotEmpty(cxCode)){
                    selectSql.append(" and SCRW_CXBM='" + cxCode + "'");
                }
                /* 产品信息 */
                if (StringUtil.isNotEmpty(cpKeyWord)) {
                    selectSql.append(" and (SCRW_CPBH like '%" + cpKeyWord + "%' or SCRW_NAME like '%" + cpKeyWord + "%' or SCRW_CPGG like '%" + cpKeyWord + "%') ");
                }
                /* 单据信息 */
                if (StringUtil.isNotEmpty(DH)) {
                    selectSql.append(" and (SCRW_DDHM like '%" + DH + "%' or SCRW_RWDH like '%" + DH + "%' or SCRW_GDHM like '%" + DH+ "%')");
                }
                /* 排除完成状态的生产任务单 */
                selectSql.append(" and SCRW_RWZT_CODE not in ('RWZT03','RWZT06') ");

                /* 排除已经绑定足够数量条码的生产任务单 */
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
     * @Description 把条码绑定生产任务单
     * @Date 14:28 2019/11/26
     * @Param []
     * @return void
     **/
    public void bindingBarCode(){
        /* 条码号 */
        String barCode = request.getParameter("barCode");
        /* 生产任务单号 */
        String ProductionTaskCode = request.getParameter("ProductionTaskCode");
        /* MAC地址 */
        String mac = request.getParameter("mac");
        /* 用户编码  必填 */
        String userCode = request.getParameter("userCode");
        if (doCheck(userCode, mac).IsSuccess) {
            JgmesResult<HashMap> ret = sanshengService.bindingBarCode(barCode, ProductionTaskCode);
            toWrite(jsonBuilder.toJson(ret));
        }

    }

    /*
     * @Author Jiansong Lu
     * @Description 检查SN码是否已经绑定生产任务单
     * @Date 10:27 2019/12/18
     * @Param []
     * @return void
     **/
    public void IsBindingBarCode(){
        /* 条码号 */
        String barCode = request.getParameter("barCode");
        /* MAC地址 */
        String mac = request.getParameter("mac");
        /* 用户编码  必填 */
        String userCode = request.getParameter("userCode");
        JgmesResult<List<HashMap>> ret = new JgmesResult<>();
        if (doCheck(userCode, mac).IsSuccess) {
            DynaBean jgmes_base_gdcptm = serviceTemplate.selectOne("JGMES_BASE_GDCPTM", "and GDCPTM_TMH='" + barCode + "'");
            if (jgmes_base_gdcptm!=null){
                /* 校验生产任务单号是否为空，空则未绑定 */
                String scrwdh = jgmes_base_gdcptm.getStr("GDCPTM_SCRWDH");
                if (StringUtil.isEmpty(scrwdh)){
                    ret.setMessage("该条码未排产！");
                }
            }else{
                ret.setMessage("未知条码号！");
            }
        }
        toWrite(jsonBuilder.toJson(ret));
    }

}