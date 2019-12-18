package com.jgmes.action;

import com.je.core.action.DynaAction;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.DynaBean;
import com.jgmes.util.JgmesCommon;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author fm
 * @version 2018-12-15 00:04:12
 * @see /jgmes/btnMulityAction!addMulity.action
 */
@Component("btnMulityAction")
@Scope("prototype")
public class BtnMulityAction extends DynaAction {
    private static final long serialVersionUID = 1L;

    public void addMulity() {
        JgmesCommon jc = new JgmesCommon(request, serviceTemplate, null);

        String spIds = request.getParameter("spIds");
        String cxbm = request.getParameter("cxbm");
        String cxmc = request.getParameter("cxmc");
        //System.out.println("cxbm:"+cxbm);
        //System.out.println("cxmc:"+cxmc);

        List<DynaBean> sps = serviceTemplate.selectList("JGMES_PLAN_GDLB", " AND JGMES_PLAN_GDLB_ID in ("
                        + StringUtil.buildArrayToString(spIds.split(",")) + ")");
        //System.out.println("OK1");
        for (DynaBean sp : sps) {
            //System.out.println("pkValue："+pkValue);
            DynaBean pclb = new DynaBean("JGMES_PLAN_PCLB", true);
            pclb.set("PCLB_CPBH", sp.getStr("GDLB_CPBH"));
            pclb.set("PCLB_NAME", sp.getStr("GDLB_NAME"));
            pclb.set("PCLB_GDZT_CODE", sp.getStr("GDLB_GDZT_CODE"));
            pclb.set("PCLB_BZ", sp.getStr("GDLB_BZ"));
            pclb.set("PCLB_WCL", sp.getStr("GDLB_WCL"));
            pclb.set("PCLB_JHKGSJ", sp.getStr("GDLB_JHKGSJ"));
            pclb.set("PCLB_SJKGSJ", sp.getStr("GDLB_SJKGSJ"));
            pclb.set("PCLB_CPGG", sp.getStr("GDLB_CPGG"));
            pclb.set("PCLB_DDHM", sp.getStr("GDLB_DDHM"));
            pclb.set("PCLB_GDHM", sp.getStr("GDLB_GDHM"));
            pclb.set("PCLB_RWDH", sp.getStr("GDLB_RWDH"));
            pclb.set("PCLB_LCKH", sp.getStr("GDLB_LCKH"));
            pclb.set("PCLB_WCSL", sp.getStr("GDLB_WCSL"));
            pclb.set("PCLB_JHWGSJ", sp.getStr("GDLB_JHWGSJ"));
            pclb.set("PCLB_RQ", sp.getStr("GDLB_RQ"));
            pclb.set("PCLB_SJWGSJ", sp.getStr("GDLB_SJWGSJ"));
            pclb.set("PCLB_PGSL", sp.getStr("GDLB_PGSL"));
            pclb.set("PCLB_SJWGSJ", sp.getStr("GDLB_SJWGSJ"));
            pclb.set("PCLB_CXBM", cxbm);
            pclb.set("PCLB_CXMC", cxmc);
            pclb.set("JGMES_PLAN_GDLB_ID", sp.getStr("JGMES_PLAN_GDLB_ID"));//工单列表_外键ID
            pclb.set("JGMES_PLAN_MAIN_ID", pkValue);//排产主表外键ID

            DynaBean dic = jc.getDic("JGMES_DIC_PCZT", "PCZT01");//修改排产状态
            pclb.set("PCLBL_PCZT_CODE", dic.get("DICTIONARYITEM_ITEMCODE"));//编号
            pclb.set("PCLB_PCZT_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));//名称
            pclb.set("PCLB_PCZT_ID", dic.get("JE_CORE_DICTIONARYITEM_ID"));//id
            //pclb.set("PCLB_PCZT_CODE", "PCZT01");
            //pclb.set("PCLB_PCZT_NAME", "待生产");

            pclb.set("PCLB_QGRQ", sp.getStr("GDLB_QGRQ"));//请购日期
            pclb.set("PCLB_OTDRQ", sp.getStr("GDLB_OTDRQ"));//OTD日期
            pclb.set("PCLB_KHBM", sp.getStr("GDLB_KHBM"));//客户编码
            pclb.set("PCLB_KHMC", sp.getStr("GDLB_KHMC"));//客户名称
            pclb.set("PCLB_CPXH", sp.getStr("GDLB_CPXH"));//产品型号

            pclb.set("PCLB_DDSL", sp.getStr("GDLB_DDSL"));//订单数量
            pclb.set("PCLB_GDSL", sp.getStr("GDLB_GDSL"));//工单单数量

            String GDLB_WPCSL = sp.getStr("GDLB_WPCSL");//工单未排数量
            pclb.set("PCLB_YPCSL", 0);//已排产数量

            int GDLB_XPCSL = sp.getInt("GDLB_XPCSL");//订单需排产数量
            int GDLB_WCSL = sp.getInt("GDLB_WCSL");//订单完成数量
//            if (GDLB_WCSL > 0) {
//                //表示已经开始生产
//                pclb.set("PCLB_XPCSL", GDLB_XPCSL - GDLB_WCSL);//需排产数量
//                pclb.set("PCLB_WPCSL", GDLB_XPCSL - GDLB_WCSL);//未排产数量
//            } else {
                pclb.set("PCLB_XPCSL", GDLB_WPCSL);//需排产数量
                pclb.set("PCLB_WPCSL", GDLB_WPCSL);//未排产数量
//            }
			/*Date dt = new Date();
			String strDate=DateUtil.formatDate(dt, "yyyy-MM-dd");
			pclb.set("PCLB_PCRQ", strDate);*/

            serviceTemplate.buildModelCreateInfo(pclb);
            serviceTemplate.insert(pclb);

        }
        toWrite(jsonBuilder.returnSuccessJson("\"添加成功\""));
    }

    /**
     * 产品检验标准方案添加检验项目方法
     */
    public void addInspectionItem() {
        JgmesCommon jc = new JgmesCommon(request, serviceTemplate, null);
        String spIds = request.getParameter("spIds");
        String jgmes_zlgl_cpjybz_id = request.getParameter("JGMES_ZLGL_CPJYBZ_ID");//产品校验标准方案主键id

//		List<DynaBean> sps = serviceTemplate.selectList("V_JGMES_ZLGL_JYXMDA", " AND JGMES_ZLGL_JYXMDA_ID in ("
//						+ StringUtil.buildArrayToString(spIds.split(",")) + ")");
        try {
            String[] split = spIds.split(",");
            for (String id : split) {
                DynaBean sp = serviceTemplate.selectOne("V_JGMES_ZLGL_JYXMDA", "and JGMES_ZLGL_JYXMDA_ID='" + id + "'");
                if (sp != null) {
                    DynaBean bzfazb = new DynaBean("JGMES_ZLGL_CPJYBZZB", true);
                    bzfazb.set("CPJYBZZB_JYXMBM", sp.getStr("JYXMDA_XMBM"));//检验项目编码
//                    bzfazb.set("CPJYBZZB_ZDZ", 0);//最大值，默认为0
//                    bzfazb.set("CPJYBZZB_ZXZ", 0);//最小值，默认为0
                    bzfazb.set("CPJYBZZB_JYPDFL_CODE", 1);//检验判断分类
                    bzfazb.set("CPJYBZZB_JYPDFL_NAME", "按参考值（等于参考值为合格）");//检验判断分类名称
                    bzfazb.set("JGMES_ZLGL_CPJYBZ_ID", jgmes_zlgl_cpjybz_id);//产品检验标准方案_外键ID、
                    bzfazb.set("CPJYBZZB_FJ_CODE", 0);//是否附件
                    bzfazb.set("CPJYBZZB_FJ_NAME", "否");//是否附件
                    bzfazb.set("CPJYBZZB_BJ_CODE", 0);//是否必检
                    bzfazb.set("CPJYBZZB_BJ_NAME", "否");//是否必检
                    bzfazb.set("CPJYBZZB_BZ_CODE", 0);//是否填备注
                    bzfazb.set("CPJYBZZB_BZ_NAME", "否");//是否填备注
                    bzfazb.set("CPJYBZZB_NO_CODE", 0);//是否填检验值
                    bzfazb.set("CPJYBZZB_NO_NAME", 0);//是否填检验值
                    String orderIndexSql = "select max(SY_ORDERINDEX) SY_ORDERINDEX from V_JGMES_ZLGL_CPJYBZZB where JGMES_ZLGL_CPJYBZ_ID='" + jgmes_zlgl_cpjybz_id + "'";
                    List<DynaBean> dynaBeans = serviceTemplate.selectListBySql(orderIndexSql);
                    Integer orderIndex = 1;
                    if (dynaBeans.size() > 0) {
                        String index = dynaBeans.get(0).getStr("SY_ORDERINDEX");
                        if (StringUtil.isNotEmpty(index)) {
                            orderIndex = Integer.parseInt(index);
                            orderIndex += 1;
                        }
                    }
                    bzfazb.set("SY_ORDERINDEX", orderIndex);//是否填检验值
                    serviceTemplate.buildModelCreateInfo(bzfazb);
                    serviceTemplate.insert(bzfazb);
                }

            }


        } catch (Exception e) {
            e.printStackTrace();
            toWrite(jsonBuilder.returnFailureJson("\"添加失败\""));
            return;
        }
        toWrite(jsonBuilder.returnSuccessJson("\"添加成功\""));
    }
}
