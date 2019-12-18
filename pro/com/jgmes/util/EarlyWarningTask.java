package com.jgmes.util;

import com.je.core.util.JEUUID;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.BeanUtils;
import com.je.core.util.bean.DynaBean;
import com.je.message.service.EmailManager;
import com.je.message.service.EmailManagerImpl;
import com.je.task.service.PcTimedTaskTemplate;
import com.je.task.vo.TimedTaskParamsVo;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 定时检查异常记录，如果达到相应的预警等级，则发出预警通知！
 *
 * @author trc
 * @version 2019-05-24 15:53:23
 */
@Component("earlyWarningTask")
public class EarlyWarningTask extends PcTimedTaskTemplate {

    EmailManager emailManager = new EmailManagerImpl();

    public void load(TimedTaskParamsVo vo) {
        System.out.println("hello TASK");
    }

    public void DeviceWarning() {
        //获取发生异常多少分钟发送异常的系统参数
        DynaBean ycsjXtcsBean = serviceTemplate.selectOne("JGMES_XTGL_XTCS", " and XTCS_CXFL2_CODE='YCFSXXSJ'");//获取异常发送的最大时间，系统参数
        Integer time= 5;//初始是5分钟，默认
        if (ycsjXtcsBean != null && ycsjXtcsBean.getStr("XTCS_NO_CODE").equals("1")) {//不等于空且启用状态
            time = ycsjXtcsBean.getInt("XTCS_CSZ");//获取参数值
        }

        DynaBean ycclsjXtcsBean = serviceTemplate.selectOne("JGMES_XTGL_XTCS", " and XTCS_CXFL2_CODE='YCCLZDSJ'");//获取异常发送的最大时间，系统参数
        Integer ycclTime= 5;//初始是5分钟，默认
        if (ycclsjXtcsBean != null && ycclsjXtcsBean.getStr("XTCS_NO_CODE").equals("1")) {//不等于空且启用状态
            ycclTime = ycsjXtcsBean.getInt("XTCS_CSZ");//获取参数值
        }
        //获取超过5分钟未处理的异常记录
                String strSql = "select TIMESTAMPDIFF(MINUTE,JTYCJL_YCSJ,now()) as MTime,a.JTYCJL_JTH,a.JTYCJL_JTMC,a.JTYCJL_JTYCLX_CODE,a.JTYCJL_JTYCLX_NAME,"
                        + "a.JTYCJL_SCX,a.JTYCJL_RWDH,a.JTYCJL_CPBM,a.JTYCJL_YCSJ,a.JTYCJL_JTYCCLZT_CODE,a.JTYCJL_JTYCCLZT_NAME,a.JGMES_ADMK_JTYCJL_ID "
                        + "from JGMES_ADMK_JTYCJL a where a.JTYCJL_JTYCCLZT_CODE in('CLZT01') and TIMESTAMPDIFF(MINUTE,JTYCJL_YCSJ,now())>"+time+" or (a.JTYCJL_JTYCCLZT_CODE in('CLZT07') and TIMESTAMPDIFF(MINUTE,JTYCJL_KSCLSJ,now())>"+ycclTime+")";
                List<DynaBean> jtycjlDynaBeanList = serviceTemplate.selectListBySql(strSql);
                if (jtycjlDynaBeanList != null) {
                    String mtime = "", jth = "", jtmc = "", jtyclx = "", jtyclxname = "", scx = "", rwdh = "", cpbm = "", ycsj = "", jtycclzt = "", jtycclztname = "", id = "";
                    String yjdj = "", yctzr = "", yctznr = "", yctzdh = "";
                    for (DynaBean bean : jtycjlDynaBeanList) {
                        mtime = bean.get("MTime") + "";//异常时间(分钟)
                        jth = bean.get("JTYCJL_JTH") + "";//机台号
                        jtmc = bean.get("JTYCJL_JTMC") + "";//机台名称
                        jtyclx = bean.get("JTYCJL_JTYCLX_CODE") + "";//异常类型代码
                        jtyclxname = bean.get("JTYCJL_JTYCLX_NAME") + "";//异常类型名称
                        scx = bean.get("JTYCJL_SCX") + "";//生产线编码
                        rwdh = bean.get("JTYCJL_RWDH") + "";//任务单号
                        cpbm = bean.get("JTYCJL_CPBM") + "";//产品编码
                        ycsj = bean.get("JTYCJL_YCSJ") + "";//异常发生时间
                        jtycclzt = bean.get("JTYCJL_JTYCCLZT_CODE") + "";//异常处理状态代码
                        jtycclztname = bean.get("JTYCJL_JTYCCLZT_NAME") + "";//异常处理状态名称
                        id = bean.get("JGMES_ADMK_JTYCJL_ID") + "";//主键

                //获取同机台、同异常类型、在时间范围内的预警级别
                strSql = "select * from JGMES_ADMK_JTYCYJB "
                        + "where JTYCYJB_JTH like '%" + jth + "%' and JTYCYJB_JTYCLX_CODE='" + jtyclx + "' and JTYCYJB_YJJGSJ<=" + mtime + " "
                        + "order by JTYCYJB_YJJGSJ desc "
                        + "limit 0,1";
                List<DynaBean> jtycyjbDynaBeanList = serviceTemplate.selectListBySql(strSql);
                if (jtycyjbDynaBeanList != null && jtycyjbDynaBeanList.size() > 0) {
                    DynaBean yjbean = jtycyjbDynaBeanList.get(0);
                    yjdj = yjbean.get("JTYCYJB_YJDJ") + "";//预警等级
                    yctzr = yjbean.get("JTYCYJB_YCTZR") + "";//异常通知人
                    yctznr = yjbean.get("JTYCYJB_YCTZNR") + "";//异常通知内容
                    yctzdh = yjbean.get("JTYCYJB_YCTZDH") + "";//异常通知电话
                    //获取该异常是否已经发送过通知
                    List<DynaBean> jgmes_admk_jtyctz = serviceTemplate.selectList("JGMES_ADMK_JTYCTZ", "and JGMES_ADMK_JTYCJL_ID='" + id + "'");
                    if (jgmes_admk_jtyctz.size()==0){
                        DynaBean jtyctzDynaBean = new DynaBean();
                        jtyctzDynaBean.set("JGMES_ADMK_JTYCTZ_ID", JEUUID.uuid());
                        jtyctzDynaBean.setStr(BeanUtils.KEY_TABLE_CODE, "JGMES_ADMK_JTYCTZ");
                        jtyctzDynaBean.set("JTYCTZ_JTH", jth);//机台号
                        jtyctzDynaBean.set("JTYCTZ_JTMC", jtmc);//机台名称
                        jtyctzDynaBean.set("JTYCTZ_YJDJ", yjdj);//预警等级
                        jtyctzDynaBean.set("JTYCTZ_TZNR",jtmc+"发生" +jtyclxname);//通知内容
                        jtyctzDynaBean.set("JTYCTZ_TZR", yctzr);//通知人
                        jtyctzDynaBean.set("JTYCTZ_JTYCLX_CODE", jtyclx);//机台异常类型
                        jtyctzDynaBean.set("JTYCTZ_JTYCLX_NAME", jtyclxname);//机台异常类型_NAME
                        jtyctzDynaBean.set("JGMES_ADMK_JTYCJL_ID", id);//机台异常类型_NAME
                        jtyctzDynaBean.set("JTYCTZ_YDZT_CODE", 1);//机台异常类型_NAME
                        jtyctzDynaBean.set("JTYCTZ_YDZT_NAME", "未读");//机台异常类型_NAME
                        serviceTemplate.buildModelCreateInfo(jtyctzDynaBean);
                        serviceTemplate.insert(jtyctzDynaBean);
                    }
                    //发送邮件
                    String notifierStr = yctzr;//通知人
                    String[] tzrArray = notifierStr.split(",");
                    for (String notifier : tzrArray) {
                        if (StringUtil.isNotEmpty(notifier)) {
                            DynaBean sendPeopleBean = serviceTemplate.selectOne("JE_CORE_ENDUSER", "and usercode='" + notifier + "'");
                            if (sendPeopleBean != null) {
//                            String emailJsonStr = sendPeopleBean.getStr("OTHEREMAIL");//获取邮箱
                                String email = sendPeopleBean.getStr("COMPANYEMAIL");//直接获取公司邮箱
//                            if (StringUtil.isNotEmpty(emailJsonStr) && !emailJsonStr.equals("[]")) { //邮箱不为空时
                                if (StringUtil.isNotEmpty(email)) {
//                                JSONObject jsonObject = JSONObject.fromObject(emailJsonStr.replaceAll("\\[", "").replaceAll("\\]", ""));
//                                String email = jsonObject.getString("value") + jsonObject.getString("text");//获取邮箱
                                    String title = "";//标题

                                    /**
                                     *  模具异常：机台编号+机台+模具编号+模具+发生模具异常，请处理，如，2#机台MJ00027模具发生模具异常，请处理
                                     *  品质异常：机台编号+机台+物料编号+发生品质异常，请处理，如，77#机台32216-521-00发生品质异常，请处理
                                     *  物料异常：机台编号+机台+物料编号+发生物料异常，请处理,如，77#机台32216-521-01发生物料异常，请处理
                                     *  超时无动作：机台编号+机台+停机未确认异常情况，呼叫主管处理，如，2#机台停机未确认异常情况，呼叫主管处理
                                     *  设备异常：机台编号+机台+发生设备异常，请处理，如，2#机台发生设备异常，请处理
                                     *  呼叫主管：机台编号+机台+呼叫主管，如，2#机台呼叫主管
                                     */
                                    String cpbh = "";
                                    String mjbh="";
                                    int count = 1;
                                    DynaBean xtcsBean = serviceTemplate.selectOne("JGMES_XTGL_XTCS", " and XTCS_CXFL2_CODE='YCYJTJFSCS'");//获取最大邮件发送次数，若无系统参数，默认为一次
                                    if (xtcsBean != null && xtcsBean.getStr("XTCS_NO_CODE").equals("1")) {//不等于空且启用状态
                                        count = xtcsBean.getInt("XTCS_CSZ");//获取参数值
                                    }
                                    //获取异常发送记录表，同预警等级，收件人，异常类型相同，机台号相同，异常外键id一样的记录
                                    List<DynaBean> jgmes_admk_ycyjfsjlb = serviceTemplate.selectList("JGMES_ADMK_YCYJFSJLB", "and YCYJFSJLB_SJRYX='" + email + "' and YCYJFSJLB_YJDJ='" + yjdj + "' and YCYJFSJLB_JTYCLX='" + jtyclx + "' and YCYJFSJLB_JTH='" + jth + "' and JGMES_ADMK_JTYCJL_ID='" + id + "'");
                                    if (jgmes_admk_ycyjfsjlb.size()<count){
                                        switch (jtyclx){
                                            case "JTYCLX01":
                                                List<DynaBean> jgmes_admk_jtda = serviceTemplate.selectList("JGMES_ADMK_JTDA", "and JTDA_JTH='" + jth + "'");
                                                if (jgmes_admk_jtda.size()>0){
                                                    String cxbm = jgmes_admk_jtda.get(0).getStr("JTDA_SCXBM");
                                                    if (StringUtil.isNotEmpty(cxbm)){
                                                        List<DynaBean> jgmes_base_cxsj = serviceTemplate.selectList("JGMES_BASE_CXSJ", "and CXSJ_CXBM='" + cxbm + "'");
                                                        if (jgmes_base_cxsj.size()>0){
                                                            mjbh = jgmes_base_cxsj.get(0).getStr("CXSJ_MJBH");//模具编号
                                                        }
                                                    }
                                                    if (StringUtil.isNotEmpty(rwdh)){
                                                        DynaBean jgmes_lhplan_scrw = serviceTemplate.selectOne("JGMES_PLAN_SCRW", "and SCRW_RWDH='" + rwdh + "'");
                                                        cpbh = jgmes_lhplan_scrw.getStr("SCRW_CPGG");
                                                    }
                                                }
                                                title = jth+":"+jtmc+cpbh+"模具发生模具异常，请处理";
                                                break;
                                            case "JTYCLX03":
                                                if (StringUtil.isNotEmpty(rwdh)){
                                                    DynaBean jgmes_lhplan_scrw = serviceTemplate.selectOne("JGMES_PLAN_SCRW", "and SCRW_RWDH='" + rwdh + "'");
                                                    cpbh = jgmes_lhplan_scrw.getStr("SCRW_CPGG");
                                                }
                                                title = jth+":"+jtmc+cpbh+"发生品质异常，请处理";
                                                break;
                                            case "JTYCLX04":
                                                if (StringUtil.isNotEmpty(rwdh)){
                                                    DynaBean jgmes_lhplan_scrw = serviceTemplate.selectOne("JGMES_PLAN_SCRW", "and SCRW_RWDH='" + rwdh + "'");
                                                    cpbh = jgmes_lhplan_scrw.getStr("SCRW_CPGG");
                                                }
                                                title = jth+":"+jtmc+cpbh+"发生物料异常，请处理";
                                                break;
                                            case "JTYCLX02":
                                                title = jth+":"+jtmc+"发生设备异常，请处理";
                                                break;
                                            case "JTYCLX05":
                                                title = jth+":"+jtmc+"呼叫主管";
                                                break;
                                            case "JTYCLX06":
                                                title = jth+":"+jtmc+"停机未确认异常情况，呼叫主管处理";
                                                break;
                                        }
                                        if (StringUtil.isNotEmpty(title)){
                                            String conText = title;//内容
                                            emailManager.send(email, title, "SendContextType.TEXT", conText);
                                            //回写异常邮件发送记录表
                                            DynaBean ycyjfsjlb = new DynaBean();
                                            ycyjfsjlb.setStr(BeanUtils.KEY_TABLE_CODE, "JGMES_ADMK_YCYJFSJLB");
                                            serviceTemplate.buildModelCreateInfo(ycyjfsjlb);//插入系统参数
                                            ycyjfsjlb.setStr("YCYJFSJLB_JTH", jth);//机台号
                                            ycyjfsjlb.setStr("YCYJFSJLB_JTMC", jtmc);//机台号
                                            ycyjfsjlb.setStr("YCYJFSJLB_YJBT", title);//邮件标题
                                            ycyjfsjlb.setStr("YCYJFSJLB_YJNR", conText);//邮件标题
                                            ycyjfsjlb.setStr("YCYJFSJLB_SJRYX", email);//收件人邮箱
                                            ycyjfsjlb.setStr("YCYJFSJLB_SJRXTBM", yctzr);//通知人
                                            ycyjfsjlb.setStr("YCYJFSJLB_YJDJ", yjdj);//预警等级
                                            ycyjfsjlb.setStr("YCYJFSJLB_JTYCLX", jtyclx);//机台异常类型
                                            ycyjfsjlb.setStr("JGMES_ADMK_JTYCJL_ID", id);//机台异常类型
                                            serviceTemplate.insert(ycyjfsjlb);
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
    }


}