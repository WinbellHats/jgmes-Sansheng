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
        String barCode = request.getParameter("Barcode");//SN码
        String OuterBarCode = request.getParameter("OuterBarCode");//外箱码
        String powerLineCode = request.getParameter("powerLineCode");//电源线码
        String BoxCode = request.getParameter("BoxCode");//附件码
        String ssh = request.getParameter("ssh");
        JgmesResult<List<HashMap>> ret = fqaServices.getBarcodeData(barCode, OuterBarCode, powerLineCode, BoxCode, ssh);
        toWrite(jsonBuilder.toJson(ret));
    }

    public void saveFQAData() {
        String barCode = request.getParameter("Barcode");//SN码
        String OuterBarCode = request.getParameter("OuterBarCode");//外箱码
        String powerLineCode = request.getParameter("powerLineCode");//电源线码
        String BoxCode = request.getParameter("BoxCode");//附件码
        JgmesResult<List<HashMap>> ret = fqaServices.saveFQAData(barCode, OuterBarCode, powerLineCode, BoxCode);
        toWrite(jsonBuilder.toJson(ret));
    }

    public void getSNBarCodeData() {
        String barCode = request.getParameter("Barcode");//SN码
        String ssh = request.getParameter("ssh");     //工序顺序号
        JgmesResult<List<HashMap>> ret = fqaServices.getSNBarcodeData(barCode, ssh);
        toWrite(jsonBuilder.toJson(ret));
    }

    public void save() {
        String jsonStr = request.getParameter("jsonStr");//主表Json
        String jsonStrDetail = request.getParameter("jsonStrDetail");     //从表Json
        JgmesResult<List<HashMap>> ret = fqaServices.save(jsonStr, jsonStrDetail, request);
        toWrite(jsonBuilder.toJson(ret));
    }

    /*
     * @Author Jiansong Lu
     * @Description 更换物料绑定，新修改
     * @Date 14:23 2019/12/11
     * @Param []
     * @return void
     **/
    public void getOldCriticalComponent() {
        // MAC地址
        String mac = request.getParameter("mac");
        // 用户编码  必填
        String userCode = request.getParameter("userCode");
        //任务单号
        String rwNo = request.getParameter("rwNo");
        //产品条码号
        String cpCode = request.getParameter("cpCode");
        // 物料条码号
        String wlBarCode = request.getParameter("wlBarCode");

        JgmesResult<HashMap> ret = new JgmesResult<>();

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            if (wlBarCode == null || wlBarCode.isEmpty()) {
                ret.setMessage("未获取到物料条码号！");
            }
            if (cpCode == null || cpCode.isEmpty()) {
                ret.setMessage("未获取到产品编码！");
            }
            if (rwNo == null || rwNo.isEmpty()) {
                ret.setMessage("未获取到任务单号！");
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
                                ret.setMessage("物料条码与产品条码不匹配！");
                            }
                        } else {
                            ret.setMessage("物料条码号已经在别的任务单中！");
                        }
                    } else {
                        /* 规避蓝牙数据蓝牙版影响，并且修改时同步把蓝牙数据的蓝牙版条码更新 */
                        if (bgslzbDynaBeanList.size()==2) {
                            DynaBean bgslzbDynaBean = new DynaBean();
                            /* 工序号4扫解码板二维码/BT板二维码与蓝牙测试=2条数据 */
                            /* 校验另一条是否是蓝牙数据,把不是蓝牙测试的数据提取出来 */
                            StringBuilder testData = new StringBuilder();
                            for (DynaBean bean : bgslzbDynaBeanList) {
                                String jcz = bean.getStr("BGSJZB_JCZ");
                                testData.append(jcz);
                                if (StringUtil.isEmpty(jcz)) {
                                    bgslzbDynaBean = bean;
                                }
                            }
                            /* 其中一个为蓝牙数据 */
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
                                        ret.setMessage("物料条码与产品条码不匹配！");
                                    }
                                } else {
                                    ret.setMessage("物料条码号已经在别的任务单中！");
                                }
                            }
                        }else{
                            ret.setMessage("请确认扫描的是物料唯一码，有多处使用该条码！");
                        }
                    }
                }
            }
        }

        toWrite(jsonBuilder.toJson(ret));
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

    /**
     * 保存物料更换信息
     */
    public void doSaveCriticalComponent() {
        // MAC地址
        String mac = request.getParameter("mac");
        // 用户编码  必填
        String userCode = request.getParameter("userCode");
        //产品编码
        String cpCode = request.getParameter("cpCode");
        //产品条码号
        String barCode = request.getParameter("barCode");
        // 新物料条码号
        String newWlBarCode = request.getParameter("newWlBarCode");
        // 旧物料条码号
        String oldWlBarCode = request.getParameter("oldWlBarCode");

        //报工数据子表数据
        String bgsjzbId = request.getParameter("bgsjzbId");

        JgmesResult<List<HashMap>> ret = new JgmesResult<>();

        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            if (newWlBarCode == null || newWlBarCode.isEmpty()) {
                ret.setMessage("未获取到新物料条码号！");
            }
            if (oldWlBarCode == null || oldWlBarCode.isEmpty()) {
                ret.setMessage("未获取到旧物料条码号！");
            }
            if (cpCode == null || cpCode.isEmpty()) {
                ret.setMessage("未获取到产品编码！");
            }
            if (bgsjzbId == null || bgsjzbId.isEmpty()) {
                ret.setMessage("未获取到报工数据子表主键ID！");
            }
            if (ret.IsSuccess) {
                //获取报工数据子表
                DynaBean bgsjzbDynaBean = serviceTemplate.selectOne("JGMES_PB_BGSJZB", " and JGMES_PB_BGSJZB_ID = '" + bgsjzbId + "'");
                if (bgsjzbDynaBean != null) {
                    if (oldWlBarCode.equals(bgsjzbDynaBean.getStr("BGSJZB_WLBH"))) {
                        ret.setMessage("旧物料条码输入错误！");
                        toWrite(jsonBuilder.toJson(ret));
                        return;
                    }
                    //获取报工数据主表
                    DynaBean bgsjDynaBean = serviceTemplate.selectOne("JGMES_PB_BGSJ", " and JGMES_PB_BGSJ_ID = '" + bgsjzbDynaBean.getStr("JGMES_PB_BGSJ_ID") + "'");
                    if (bgsjDynaBean != null && cpCode.equals(bgsjDynaBean.getStr("BGSJ_CPBH"))) {
                        //记录物料更换记录表
                        DynaBean wlghDynaBean = new DynaBean();
                        wlghDynaBean.set("JGMES_SCGCGL_WLGHJLB_ID", JEUUID.uuid());
                        wlghDynaBean.set(beanUtils.KEY_TABLE_CODE, "JGMES_SCGCGL_WLGHJLB");
                        //产品
                        wlghDynaBean.set("WLGHJLB_CPMC", bgsjDynaBean.getStr("BGSJ_CPMC"));
                        wlghDynaBean.set("WLGHJLB_CPBM", bgsjDynaBean.getStr("BGSJ_CPBH"));
                        //物料
                        wlghDynaBean.set("WLGHJLB_WLBM", bgsjzbDynaBean.getStr("BGSJZB_WLBH"));
                        wlghDynaBean.set("WLGHJLB_WLMC", bgsjzbDynaBean.getStr("BGSJZB_WLMC"));
                        //工序
                        wlghDynaBean.set("WLGHJLB_GXBM", bgsjDynaBean.getStr("BGSJ_GXBH"));
                        wlghDynaBean.set("WLGHJLB_GXMC", bgsjDynaBean.getStr("BGSJ_GXMC"));

                        //产品条码号
                        wlghDynaBean.set("WLGHJLB_CPTMH", barCode);
                        //旧物料条码号
                        wlghDynaBean.set("WLGHJLB_JWLTMH", oldWlBarCode);
                        //新物料条码号
                        wlghDynaBean.set("WLGHJLB_XWLTMH", newWlBarCode);
                        //任务单号
                        wlghDynaBean.set("WLGHJLB_RWDH", bgsjDynaBean.getStr("BGSJ_SCRW"));
                        //获取订单号
                        DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and SCRW_RWDH = '" + bgsjDynaBean.getStr("BGSJ_SCRW") + "'");
                        //工单号码
                        wlghDynaBean.set("WLGHJLB_GDHM", bgsjDynaBean.getStr("BGSJ_GDHM"));
                        //订单号码
                        if (scrwDynaBean != null) {
                            wlghDynaBean.set("WLGHJLB_DDHM", scrwDynaBean.getStr("SCRW_DDHM"));
                            //内部订单号
                            wlghDynaBean.set("WLGHJLB_LCKH", scrwDynaBean.getStr("SCRW_LCKH"));
                            if (wlghDynaBean.getStr("WLGHJLB_GDHM") == null || "".equals(wlghDynaBean.getStr("WLGHJLB_GDHM"))) {
                                wlghDynaBean.set("WLGHJLB_GDHM", scrwDynaBean.getStr("SCRW_GDHM"));
                            }
                        }
                        //更换时间
                        wlghDynaBean.set("WLGHJLB_GHSJ", jgmesCommon.getCurrentTime());
                        //报工数据主键ID
                        wlghDynaBean.set("WLGHJLB_BGSJZJID", bgsjDynaBean.getStr("JGMES_PB_BGSJ_ID"));

                        //插入物料更换记录
                        serviceTemplate.insert(wlghDynaBean);
                        bgsjzbDynaBean.set("BGSJZB_TMH", newWlBarCode);
                        serviceTemplate.update(bgsjzbDynaBean);
                    }

                    //更換其他的物料（例如藍牙版的物料）
                    List<DynaBean> bgslzbDynaBeanList = serviceTemplate.selectList("JGMES_PB_BGSJZB", " and BGSJZB_TMH = '" + oldWlBarCode + "' and JGMES_PB_BGSJ_ID != '" + bgsjzbDynaBean.getStr("JGMES_PB_BGSJ_ID") + "'");
                    if (bgslzbDynaBeanList != null && bgslzbDynaBeanList.size() > 0) {
                        for (DynaBean bgslzb : bgslzbDynaBeanList) {
                            //获取报工数据主表
                            bgsjDynaBean = serviceTemplate.selectOne("JGMES_PB_BGSJ", " and JGMES_PB_BGSJ_ID = '" + bgslzb.getStr("JGMES_PB_BGSJ_ID") + "'");
                            if (bgsjDynaBean != null && cpCode.equals(bgsjDynaBean.getStr("BGSJ_CPBH"))) {
                                //记录物料更换记录表
                                DynaBean wlghDynaBean = new DynaBean();
                                wlghDynaBean.set("JGMES_SCGCGL_WLGHJLB_ID", JEUUID.uuid());
                                wlghDynaBean.set(beanUtils.KEY_TABLE_CODE, "JGMES_SCGCGL_WLGHJLB");
                                //产品
                                wlghDynaBean.set("WLGHJLB_CPMC", bgsjDynaBean.getStr("BGSJ_CPMC"));
                                wlghDynaBean.set("WLGHJLB_CPBM", bgsjDynaBean.getStr("BGSJ_CPBH"));
                                //物料
                                wlghDynaBean.set("WLGHJLB_WLBM", bgslzb.getStr("BGSJZB_WLBH"));
                                wlghDynaBean.set("WLGHJLB_WLMC", bgslzb.getStr("BGSJZB_WLMC"));
                                //工序
                                wlghDynaBean.set("WLGHJLB_GXBM", bgsjDynaBean.getStr("BGSJ_GXBH"));
                                wlghDynaBean.set("WLGHJLB_GXMC", bgsjDynaBean.getStr("BGSJ_GXMC"));

                                //产品条码号
                                wlghDynaBean.set("WLGHJLB_CPTMH", barCode);
                                //旧物料条码号
                                wlghDynaBean.set("WLGHJLB_JWLTMH", oldWlBarCode);
                                //新物料条码号
                                wlghDynaBean.set("WLGHJLB_XWLTMH", newWlBarCode);
                                //任务单号
                                wlghDynaBean.set("WLGHJLB_RWDH", bgsjDynaBean.getStr("BGSJ_SCRW"));
                                //获取订单号
                                DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and SCRW_RWDH = '" + bgsjDynaBean.getStr("BGSJ_SCRW") + "'");
                                //工单号码
                                wlghDynaBean.set("WLGHJLB_GDHM", bgsjDynaBean.getStr("BGSJ_GDHM"));
                                //订单号码
                                if (scrwDynaBean != null) {
                                    wlghDynaBean.set("WLGHJLB_DDHM", scrwDynaBean.getStr("SCRW_DDHM"));
                                    //内部订单号
                                    wlghDynaBean.set("WLGHJLB_LCKH", scrwDynaBean.getStr("SCRW_LCKH"));
                                    if (wlghDynaBean.getStr("WLGHJLB_GDHM") == null || "".equals(wlghDynaBean.getStr("WLGHJLB_GDHM"))) {
                                        wlghDynaBean.set("WLGHJLB_GDHM", scrwDynaBean.getStr("SCRW_GDHM"));
                                    }
                                }
                                //更换时间
                                wlghDynaBean.set("WLGHJLB_GHSJ", jgmesCommon.getCurrentTime());
                                //报工数据主键ID
                                wlghDynaBean.set("WLGHJLB_BGSJZJID", bgsjDynaBean.getStr("JGMES_PB_BGSJ_ID"));

                                //插入物料更换记录
                                serviceTemplate.insert(wlghDynaBean);
                                bgslzb.set("BGSJZB_TMH", newWlBarCode);
                                serviceTemplate.update(bgslzb);
                            }
                        }
                    }
                } else {
                    ret.setMessage("未获取到报工数据！");
                }
            }
        }
        toWrite(jsonBuilder.toJson(ret));
    }


}