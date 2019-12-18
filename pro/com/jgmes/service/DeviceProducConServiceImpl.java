package com.jgmes.service;

import com.je.core.service.PCDynaServiceTemplate;
import com.je.core.util.JEUUID;
import com.je.core.util.SpringContextHolder;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.BeanUtils;
import com.je.core.util.bean.DynaBean;
import com.je.message.service.EmailManager;
import com.je.message.service.EmailManagerImpl;
import com.jgmes.util.DataCollection;
import com.jgmes.util.JgmesCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * �豸�Խ�webservice������
 *
 * @author liuc
 * @version 2019-04-23 11:44:16
 */
public class DeviceProducConServiceImpl implements DeviceProducConService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceProducConServiceImpl.class);

    private static PCDynaServiceTemplate serviceTemplate = SpringContextHolder.getBean("PCDynaServiceTemplate");

    @Override
    public void load() {
        System.out.println("hello serviceimpl");
    }

    EmailManager emailManager = new EmailManagerImpl();

    @Override
    public ResultBoolean SetDeviceQuantity(String DeviceID, Long Quantity) {
        JgmesCommon jgmesCommon = new JgmesCommon(null, serviceTemplate);
        ResultBoolean re = new ResultBoolean();
        if (DeviceID == null || DeviceID.isEmpty() || Quantity == null) {
            re.IsSuccess = false;
            re.ErrorCode = 0;
            re.setMessage("未获取到机台号或者数量！");
            return re;
        }
        String message = "";
        //获取产线机台关联
        DynaBean cxjtglDynaBean = serviceTemplate.selectOne("JGMES_SCGCGL_CXJTGL", " and CXJTGL_JTH = '" + DeviceID + "'");

        if (cxjtglDynaBean != null) {
            //获取该产线中处于生产中的生产任务
            DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PB_SCRWZT", " and SCRWZT_CXBM = '" + cxjtglDynaBean.getStr("CXJTGL_CXBM") + "' and SCRWZT_SCZT_CODE = 'SCZT01'");
            if (scrwDynaBean != null) {
                DynaBean cyjsDynaBean = new DynaBean();
                cyjsDynaBean.set("JGMES_SCGCGL_CYJSB_ID", JEUUID.uuid());
                cyjsDynaBean.setStr(BeanUtils.KEY_TABLE_CODE, "JGMES_SCGCGL_CYJSB");
                cyjsDynaBean.set("CYJSB_CXBM", cxjtglDynaBean.getStr("CXJTGL_CXBM"));
                cyjsDynaBean.set("CYJSB_RWDH", scrwDynaBean.getStr("SCRWZT_RWDH"));
                cyjsDynaBean.set("CYJSB_DDHM", scrwDynaBean.getStr("SCRWZT_DDHM"));
                cyjsDynaBean.set("CYJSB_CYCS", Quantity);
                cyjsDynaBean.set("CYJSB_ZCCYCS", Quantity);
                cyjsDynaBean.set("CYJSB_JLSJ", jgmesCommon.getCurrentTime());

                //回写冲压计数表中的数据
                DynaBean dic1 = jgmesCommon.getDic("JGMES_YES_NO", "0");
                if (dic1 != null) {
                    cyjsDynaBean.set("CYJSB_NO_NAME", dic1.get("DICTIONARYITEM_ITEMNAME"));
                    cyjsDynaBean.set("CYJSB_NO_CODE", dic1.get("DICTIONARYITEM_ITEMCODE"));
                }
                serviceTemplate.insert(cyjsDynaBean);

                //回写产线机台关联表
                //累计冲压次数
                cxjtglDynaBean.set("CXJTGL_LJCYCS", cxjtglDynaBean.getInt("CXJTGL_LJCYCS") + Quantity);
                //累计正常冲压次数
                cxjtglDynaBean.set("CXJTGL_LJZCCYCS", cxjtglDynaBean.getInt("CXJTGL_LJZCCYCS") + Quantity);

                serviceTemplate.update(cxjtglDynaBean);
            } else {
                message = "未获取到处于生产中的生产任务状态数据，请检查！";
            }

        } else {
            message = "未获取到产线机台关联数据，请检查！";
        }

        if (message != null && !"".equals(message)) {
            re.IsSuccess = false;
            re.ErrorCode = 0;
            re.setMessage(message);
            return re;
        } else {
            re.IsSuccess = true;
            return re;
        }
    }

    @Override
    public ResultBoolean SetInfraredQuantity(String cxCode, Long Quantity, String comk) {
        JgmesCommon jgmesCommon = new JgmesCommon(null, serviceTemplate);
        DynaBean scrwZTDynaBean = serviceTemplate.selectOne("JGMES_PB_SCRWZT", " and SCRWZT_CXBM = '" + cxCode + "' and SCRWZT_SCZT_CODE = 'SCZT01' and SCRWZT_CXD = 'CXD01'");
        String message = "";
        if (scrwZTDynaBean != null) {
            //获取红外线编码
            List<DynaBean> hwdaDynaBeanList = serviceTemplate.selectList("JGMES_SCGCGL_CXHWADAB", " and CXHWADAB_CXBM = '" + cxCode + "' and CXHWADAB_HWXCOMK = '" + comk + "'");
            if (hwdaDynaBeanList != null && hwdaDynaBeanList.size() > 0) {
                //产线红外线计数表
                DynaBean cxjsDynaBean = new DynaBean();
                cxjsDynaBean.set("JGMES_SCGCGL_CXHWXJSB_ID", JEUUID.uuid());
                cxjsDynaBean.setStr(BeanUtils.KEY_TABLE_CODE, "JGMES_SCGCGL_CXHWXJSB");
                //产线编码
                cxjsDynaBean.set("CXHWXJSB_CXBM", cxCode);
                //任务状态主键
                cxjsDynaBean.set("CXHWXJSB_RWZTZJ", scrwZTDynaBean.getStr("JGMES_PB_SCRWZT_ID"));
                //任务单号
                cxjsDynaBean.set("CXHWXJSB_RWDH", scrwZTDynaBean.getStr("SCRWZT_RWDH"));
                //计数时间
                cxjsDynaBean.set("CXHWXJSB_JSSJ", jgmesCommon.getCurrentTime());
                //计数数量
                cxjsDynaBean.set("CXHWXJSB_JSSL", Quantity);
                //红外线编码
                cxjsDynaBean.set("CXHWXJSB_HWXBM", hwdaDynaBeanList.get(0).getStr("CXHWADAB_HWXBM"));

                serviceTemplate.insert(cxjsDynaBean);
            } else {
                message = "未设置产线红外线编码数据！";
            }
        } else {
            message = "改产线没有已经开工的生产任务单！";
        }
        ResultBoolean re = new ResultBoolean();
        if (message != null && !"".equals(message)) {
            re.IsSuccess = false;
            re.ErrorCode = 0;
            re.setMessage(message);
            return re;
        } else {
            re.IsSuccess = true;
            return re;
        }
    }

    @Override
    public ResultBoolean setZKSJCJB(List<DataCollection> dataCollectionList) {
        ResultBoolean ret = new ResultBoolean();
        ret.IsSuccess = true;
        if (dataCollectionList != null && dataCollectionList.size() > 0) {
            for (DataCollection dataCollection : dataCollectionList) {
                DynaBean scDynaBean = new DynaBean();
                scDynaBean.set(BeanUtils.KEY_TABLE_CODE, "jgmes_zk_sjb");
                scDynaBean.set("jgmes_zk_sjb_ID", JEUUID.uuid());
                scDynaBean.set("sjb_xb", dataCollection.getDeviceName());
                scDynaBean.set("sjb_qsdz", dataCollection.getFetchID());
                scDynaBean.set("sbb_zwmc", dataCollection.getDeviceNameDes());
                scDynaBean.set("sjb_sjz", dataCollection.getDataValue());
                scDynaBean.set("sjb_rq", dataCollection.getFetchDate());
                scDynaBean.set("sjb_bz", dataCollection.getDeviceRemark());
                serviceTemplate.insert(scDynaBean);
            }
        } else {
            ret.IsSuccess = false;
            ret.ErrorCode = 0;
            ret.setMessage("传输数据不能为空！");
            return ret;
        }
        return ret;
    }

    @Override
    public ResultBoolean upKeep(String id, HttpServletRequest request) {
        ResultBoolean re = new ResultBoolean();
        JgmesCommon jgmesCommon = new JgmesCommon(request,serviceTemplate,"");
        String idListStr = id.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\"", "");
        String[] split = idListStr.split(",");
        try {
            for (String pkId : split) {
                DynaBean mjBean = serviceTemplate.selectOne("JGMES_BASE_MJ", "and JGMES_BASE_MJ_ID='" + pkId + "'");
                if (mjBean!=null){
                    Integer change = 0;//中转数据
                    change = mjBean.getInt("MJ_LJSCSL");
                    Long ljsl = change.longValue();//累计数量
                    mjBean.setLong("MJ_LSSCSL", ljsl);//历史生产数量
                    String mjbh = mjBean.getStr("MJ_BH");//模具编号
                    //获取模具冲次记录表数据
                    DynaBean jgmes_mjmk_mjccjlb = serviceTemplate.selectOne("JGMES_MJMK_MJCCJLB", "and MJCCJLB_MJBH='" + mjbh + "'");
                    if (jgmes_mjmk_mjccjlb != null) {
                        jgmes_mjmk_mjccjlb.setLong("MJCCJLB_CCSL", 0);
                        serviceTemplate.update(jgmes_mjmk_mjccjlb);
                    }
                    serviceTemplate.update(mjBean);
                    //回写模具记录表
                    DynaBean mjbyjlb = new DynaBean("JGMES_MJMK_MJBYJLB",true);
                    mjbyjlb.set("MJBYJLB_MJBH",mjBean.getStr("MJ_BH"));
                    mjbyjlb.set("MJBYJLB_MJLX",mjBean.getStr("MJ_MJLX_NAME"));
                    mjbyjlb.set("MJBYJLB_MJMC",mjBean.getStr("MJ_MC"));
                    mjbyjlb.set("MJBYJLB_LJSCSL",mjBean.getStr("MJ_LJSCSL"));
                    mjbyjlb.set("MJBYJLB_BYR",jgmesCommon.jgmesUser.getCurrentUserCode());
                    jgmesCommon.setDynaBeanInfo(mjbyjlb);
                    serviceTemplate.insert(mjbyjlb);
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
            re.setMessage(e.toString());
        }
        if (StringUtil.isEmpty(re.getMessage())) {
            re.IsSuccess = true;
        }
        return re;
    }

    /**
     * 设备计数与安灯异常服务接口
     *
     * @param DeviceID                设备唯一ID,不能为空
     * @param DeviceTime              格式:年-月-日 时:分:秒（2019-04-26 20:20:00）
     * @param Admin                   超级现场管理功能：1表示启用，0表示正常（整形）
     * @param TurnOffLight            服务器灭灯功能：1表示启用，0表示正常（整形）
     * @param Lighting                服务器控灯功能：1表示启用，0表示正常（整形）
     * @param AllTotalQuantity        所有累计数量（长整形）
     * @param NormalQuantity          正常数量（长整形）
     * @param AbnormalState           异常状态:1表示有超过5分钟机台没有动作，需要报警处理（呼叫主管），0表示没有异常（整形）
     * @param MoldException           模具异常:1表示有按下，0表示没有按下，2表示超过时间需要处理（整形）
     * @param DeviceException         设备异常:1表示有按下，0表示没有按下，2表示超过时间需要处理（整形）
     * @param QualityException        品质异常:1表示有按下，0表示没有按下，2表示超过时间需要处理（整形）
     * @param MaterialsException      物料异常:1表示有按下，0表示没有按下，2表示超过时间需要处理（整形）
     * @param CallSquadLeader         呼叫主管：1表示有按下，0表示没有按下，2表示超过时间需要处理（整形）
     * @param MoldCumulativeTime      模具按下累计时间:秒（整形）
     * @param DeviceCumulativeTime    设备按下累计时间:秒（整形）
     * @param QualityCumulativeTime   品质按下累计时间:秒（整形）
     * @param MaterialsCumulativeTime 物料按下累计时间：秒（整形）
     * @param CallCumulativeTime      呼叫班长按下累计时间：秒（整形）
     * @return
     */
    @Override
    public ResultBoolean SetDeviceCountAnlamp(String DeviceID, String DeviceTime, int Admin, int TurnOffLight, int Lighting, Long AllTotalQuantity, Long NormalQuantity, int AbnormalState, int MoldException, int DeviceException, int QualityException, int MaterialsException, int CallSquadLeader, int MoldCumulativeTime, int DeviceCumulativeTime, int QualityCumulativeTime, int MaterialsCumulativeTime, int CallCumulativeTime) {
        JgmesCommon jgmesCommon = new JgmesCommon(null, serviceTemplate);

        ResultBoolean re = new ResultBoolean();
        if (DeviceID == null || DeviceID.isEmpty() || NormalQuantity == null) {
            re.IsSuccess = false;
            re.ErrorCode = 0;
            re.setMessage("未获取到机台号或者数量！");
            return re;
        }

        String result = DeviceID + "," + DeviceTime + "," + Admin + "," + TurnOffLight + "," + Lighting + "," + AllTotalQuantity + "," + NormalQuantity + "," + AbnormalState + "," + MoldException + "," + DeviceException + "," + QualityException + "," + MaterialsException + "," + CallSquadLeader + "," + MoldCumulativeTime + "," + DeviceCumulativeTime + "," + QualityCumulativeTime + "," + MaterialsCumulativeTime + "," + CallCumulativeTime;
		/*// 1：利用File类找到要操作的对象
		File file = new File("D:\\TEST\\DeviceCountAnlamp.txt");
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }

        //2：准备输出流
        try {
        	Writer out = new FileWriter(file);
			out.write(result);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
        String message = "";
        String JTYCLX = "", JTYCLXMC = "";

        //获取机台档案
        DynaBean jtdaDynaBean = serviceTemplate.selectOne("JGMES_ADMK_JTDA", " and JTDA_JTH = '" + DeviceID.trim() + "'");

        if (jtdaDynaBean != null) {
            String SCRWZT_RWDH = "";
            String SCRWZT_DDHM = "";
            String SCRWZT_BZBM = "";
            String SCRWZT_SCRWID = "";//生产任务id
            String SCRW_PS = "";
            String SCRW_LS = "";
            //获取该产线中处于生产中的生产任务
            DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PB_SCRWZT", " and SCRWZT_CXBM = '" + jtdaDynaBean.getStr("JTDA_SCXBM") + "' and SCRWZT_SCZT_CODE = 'SCZT01'");
            if (scrwDynaBean != null) {
                SCRWZT_RWDH = scrwDynaBean.getStr("SCRWZT_RWDH");
                SCRWZT_DDHM = scrwDynaBean.getStr("SCRWZT_DDHM");
                SCRWZT_BZBM = scrwDynaBean.getStr("SCRWZT_BZBM");
                SCRWZT_SCRWID = scrwDynaBean.getStr("JGMES_PLAN_SCRW_ID");
            } /*else{
				message= "未获取到处于生产中的生产任务状态数据，请检查！";
			}*/

            //新增冲压计数表中的数据
            DynaBean cyjsDynaBean = new DynaBean();
            cyjsDynaBean.set("JGMES_ADMK_JTCYJSB_ID", JEUUID.uuid());
            cyjsDynaBean.setStr(BeanUtils.KEY_TABLE_CODE, "JGMES_ADMK_JTCYJSB");
            cyjsDynaBean.set("JTCYJSB_JTH", DeviceID);//机台号
            cyjsDynaBean.set("JTCYJSB_SCXBM", jtdaDynaBean.getStr("JTDA_SCXBM"));//生产线编码
            cyjsDynaBean.set("JTCYJSB_RWDH", SCRWZT_RWDH);//任务单号
            cyjsDynaBean.set("JTCYJSB_DDH", SCRWZT_DDHM);//订单号
            cyjsDynaBean.set("JTCYJSB_BZ", SCRWZT_BZBM);//班组
            cyjsDynaBean.set("JTCYJSB_CYCS", AllTotalQuantity);//冲压次数
            cyjsDynaBean.set("JTCYJSB_ZCCYCS", NormalQuantity);//正常冲压次数
            cyjsDynaBean.set("JTCYJSB_JSSJ", DeviceTime);//计数时间jgmesCommon.getCurrentTime()
            cyjsDynaBean.set("JTCYJSB_CRCSZ", result);//记录传入的参数值
            DynaBean dic1 = jgmesCommon.getDic("JGMES_YES_NO", "0");
            if (dic1 != null) {
                cyjsDynaBean.set("JTCYJSB_ADSFBG_NAME", dic1.get("DICTIONARYITEM_ITEMNAME"));//安灯是否报工_NAME
                cyjsDynaBean.set("JTCYJSB_ADSFBG_CODE", dic1.get("DICTIONARYITEM_ITEMCODE"));//安灯是否报工
            }

            try {
                //更新机台模具记录表
                DynaBean jtmjjlBean = new DynaBean();
                jtmjjlBean.setStr(BeanUtils.KEY_TABLE_CODE, "JGMES_MJMK_JTMJCCJLB");//设置表名
                String cxbm = jtdaDynaBean.getStr("JTDA_SCXBM");
                jtmjjlBean.setStr("JTMJCCJLB_CXBH", cxbm);//产线编码
                jtmjjlBean.setStr("JTMJCCJLB_CXMC", jtdaDynaBean.getStr("JTDA_JTMC"));//产线名称
                //获取产线模具以获得模具信息
                DynaBean cxsjBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ", "and CXSJ_CXBM='" + cxbm + "'");
                if (cxsjBean != null) {
                    String cxsj_mjbh = cxsjBean.getStr("CXSJ_MJBH");
                    if (StringUtil.isNotEmpty(cxsj_mjbh)) {//有绑定模具时
                        jtmjjlBean.setStr("JTMJCCJLB_MJBH", cxsjBean.getStr("CXSJ_MJBH"));//模具编号
                        jtmjjlBean.setStr("JTMJCCJLB_MJMC", cxsjBean.getStr("CXSJ_MJMC"));//模具名称
                        String lastDataSql = "select * from JGMES_ADMK_JTCYJSB where JTCYJSB_JTH='" + DeviceID + "' and JTCYJSB_JSSJ=(select max(JTCYJSB_JSSJ) from JGMES_ADMK_JTCYJSB where JTCYJSB_JTH='" + DeviceID + "')";
                        List<DynaBean> lastDataBeans = serviceTemplate.selectListBySql(lastDataSql);//读取插入前最后一条的数据
                        if (lastDataBeans.size() > 0) {
                            Integer l = lastDataBeans.get(0).getInt("JTCYJSB_CYCS");
                            Long lastAllTotalQuantity = l.longValue();//读取冲压次数
                            Long cycs = Long.valueOf(0);//模具当前冲压次数
                            if (lastAllTotalQuantity <= AllTotalQuantity) {//当前冲压次数比之前的大（即没有被清零）
                                cycs = AllTotalQuantity - lastAllTotalQuantity;
                            } else {//被清零了
                                cycs = AllTotalQuantity;
                            }
                            jtmjjlBean.setLong("JTMJCCJLB_CCSL", cycs);//冲次数量，现在这条数据和冲压表中的最后一条记录的冲压次数的差值
                            List<DynaBean> jtmjccjlbList = serviceTemplate.selectList("JGMES_MJMK_JTMJCCJLB", "and JTMJCCJLB_CXBH='" + cxbm + "' and JTMJCCJLB_MJBH='" + cxsjBean.getStr("CXSJ_MJBH") + "'");
                            Long nowCCSL = Long.valueOf(0);//当前模具的冲次数量
                            if (jtmjccjlbList.size() > 0) {
                                long lastCCSL = jtmjccjlbList.get(0).getLong("JTMJCCJLB_CCSL");//最后的冲次数量
                                nowCCSL = cycs + lastCCSL;
                                jtmjccjlbList.get(0).setLong("JTMJCCJLB_CCSL", nowCCSL);
                                serviceTemplate.update(jtmjccjlbList.get(0));
                            } else {
                                nowCCSL = cycs;
                                serviceTemplate.buildModelCreateInfo(jtmjjlBean);
                                serviceTemplate.insert(jtmjjlBean);
                            }
                            //更新模具记录表
                            DynaBean mjjlBean = new DynaBean();
                            mjjlBean.setStr(BeanUtils.KEY_TABLE_CODE, "JGMES_MJMK_MJCCJLB");//设置表名
                            mjjlBean.setStr("MJCCJLB_MJBH", cxsjBean.getStr("CXSJ_MJBH"));//模具编号
                            mjjlBean.setStr("MJCCJLB_MJMC", cxsjBean.getStr("CXSJ_MJMC"));//模具名称
                            mjjlBean.setLong("MJCCJLB_CCSL", cycs);//冲次次数
                            List<DynaBean> dynaBeans = serviceTemplate.selectList("JGMES_MJMK_MJCCJLB", "and MJCCJLB_MJBH='" + cxsjBean.getStr("CXSJ_MJBH") + "'");//读取记录表中是否很有这个模具的数据
                            if (dynaBeans.size() > 0) {//若含有
                                long lastCCSL = dynaBeans.get(0).getLong("MJCCJLB_CCSL");//最后的冲次数量
                                dynaBeans.get(0).setLong("MJCCJLB_CCSL", cycs + lastCCSL);
                                serviceTemplate.update(dynaBeans.get(0));
                            } else {
                                serviceTemplate.buildModelCreateInfo(mjjlBean);
                                serviceTemplate.insert(mjjlBean);
                            }
                            //回写模具资料，保养次数-保养提醒数量（正负值）=需要的提醒数量
                            DynaBean mjBean = serviceTemplate.selectOne("JGMES_BASE_MJ", "and MJ_BH='" + cxsjBean.getStr("CXSJ_MJBH") + "'");
                            if (mjBean != null) {
                                Integer mjbysl = mjBean.getInt("MJ_MJBYSL");
                                long bytxsl = mjbysl.longValue();//保养提醒数量
                                Integer bycs = mjBean.getInt("MJ_BYCS");
                                long bysl = bycs.longValue();//保养次数（运行多少次之后要保养的数量）
                                long txsl = bysl - bytxsl;//需要的提醒数量
                                Integer lssl = mjBean.getInt("MJ_LJSCSL");
                                long lastLssl = lssl.longValue();//累计生产数量
                                Long sl = cycs + lastLssl;//累计生产数量=历史数量+当前冲压数量
                                mjBean.setLong("MJ_LJSCSL", sl);
                                serviceTemplate.update(mjBean);
                                //读取冲压记录表中的数据中的当前数据
                                List<DynaBean> cyjlb = serviceTemplate.selectList("JGMES_MJMK_MJCCJLB", "and MJCCJLB_MJBH='" + cxsjBean.getStr("CXSJ_MJBH") + "'");
                                int dqcycs = 0;//当前冲压次数
                                if (cyjlb.size()>0){
                                    dqcycs = cyjlb.get(0).getInt("MJCCJLB_CCSL");
                                }
                                if (dqcycs > txsl) {
//                                if (nowCCSL > txsl) {//当前冲压次数大于当前提醒数量时
                                    //获取邮箱收件人对应表信息,1为保养
                                    List<DynaBean> jgmes_mjmk_yxsjrdyb = serviceTemplate.selectList("JGMES_MJMK_YXSJRDYB", "and YXSJRDYB_YJFSSXLX_CODE='1'");
                                    for (DynaBean dynaBean : jgmes_mjmk_yxsjrdyb) {
                                        //发送邮件，通知保养
                                        String userCode = dynaBean.getStr("YXSJRDYB_SJR");//收件人
                                        //一天发一次
                                        List<DynaBean> jgmes_mjmk_emailrecord = serviceTemplate.selectList("JGMES_MJMK_EMAILRECORD", "and EMAILRECORD_SJRBM='" + userCode + "' and TO_DAYS(NOW())- TO_DAYS(SY_CREATETIME)<1");
                                        if (jgmes_mjmk_emailrecord.size() == 0) {//一天内没有发送过
                                            DynaBean sendPeopleBean = serviceTemplate.selectOne("JE_CORE_ENDUSER", "and usercode='" + userCode + "'");
                                            if (sendPeopleBean != null) {
//                                                String emailJsonStr = sendPeopleBean.getStr("OTHEREMAIL");//获取邮箱
                                                String email = sendPeopleBean.getStr("COMPANYEMAIL");//直接获取公司邮箱
//                                                if (StringUtil.isNotEmpty(emailJsonStr) && !emailJsonStr.equals("[]")) { //邮箱不为空时
                                                if (StringUtil.isNotEmpty(email)) {
                                                    String mjbh = cxsjBean.getStr("CXSJ_MJBH");
                                                    DynaBean jgmes_base_mj = serviceTemplate.selectOne("JGMES_BASE_MJ", "and MJ_BH='" + mjbh + "'");
                                                    String mjmc = "";
                                                    String mjlx = "";
                                                    if (jgmes_base_mj!=null){
                                                        mjmc = cxsjBean.getStr("CXSJ_MJBH");
                                                        mjlx = jgmes_base_mj.getStr("MJ_MJLX_NAME");
                                                        if (StringUtil.isEmpty(mjmc)){
                                                            mjmc="";
                                                        }
                                                        if (StringUtil.isEmpty(mjlx)){
                                                            mjlx="";
                                                        }
                                                    }

//                                                    JSONObject jsonObject = JSONObject.fromObject(emailJsonStr.replaceAll("\\[", "").replaceAll("\\]", ""));
//                                                    String email = jsonObject.getString("value") + jsonObject.getString("text");
//                                                    String title = dynaBean.getStr("YXSJRDYB_YJBT");//邮件标题
                                                    String title = dynaBean.getStr("YXSJRDYB_YJBT");
                                                    if (StringUtil.isNotEmpty(title)){
                                                        if(title.indexOf("%MJBH%") != -1){
                                                            title=title.replaceAll("%MJBH%",mjbh);
                                                        }
                                                        if(title.indexOf("%MJLX%") != -1){
                                                            title=title.replaceAll("%MJLX%",mjlx);
                                                        }
                                                        if(title.indexOf("%MJMC%") != -1){
                                                            title=title.replaceAll("%MJMC%",mjmc);
                                                        }
                                                    }
                                                    String conText = dynaBean.getStr("YXSJRDYB_YJNR");//内容
                                                    if (StringUtil.isNotEmpty(conText)){
                                                        if(conText.indexOf("%MJBH%") != -1){
                                                            conText=conText.replaceAll("%MJBH%",mjbh);
                                                        }
                                                        if(conText.indexOf("%MJLX%") != -1){
                                                            conText=conText.replaceAll("%MJLX%",mjlx);
                                                        }
                                                        if(conText.indexOf("%MJMC%") != -1){
                                                            conText=conText.replaceAll("%MJMC%",mjmc);
                                                        }
                                                    }else{
                                                        conText = "模具编号为" + mjbh + ",类型为" + mjlx + "的模具需要保养，请注意！！";
                                                    }
                                                    String lx = dynaBean.getStr("YXSJRDYB_YJFSLX_CODE");//发送类型
                                                    emailManager.send(email, title, lx, conText);
                                                    //回写邮件发送记录表
                                                    DynaBean emailRecordBean = new DynaBean();
                                                    emailRecordBean.setStr(BeanUtils.KEY_TABLE_CODE, "JGMES_MJMK_EMAILRECORD");
                                                    emailRecordBean.setStr("EMAILRECORD_SJRBM", userCode);
                                                    emailRecordBean.setStr("EMAILRECORD_SJRYX", email);
                                                    emailRecordBean.setStr("EMAILRECORD_XYBYDJTH", cxbm);
                                                    emailRecordBean.setStr("EMAILRECORD_FSLX", "SendContextType.TEXT");
                                                    emailRecordBean.setStr("EMAILRECORD_YJBT", title);
                                                    emailRecordBean.setStr("EMAILRECORD_YJNR", conText);
                                                    serviceTemplate.buildModelCreateInfo(emailRecordBean);//插入系统参数
                                                    serviceTemplate.insert(emailRecordBean);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    message = "未获取到产线数据，请检查！";
                }
            } catch (Exception e) {
                e.printStackTrace();
                message = e.toString();
            }
            //插入冲压计数表
            serviceTemplate.buildModelCreateInfo(cyjsDynaBean);
            serviceTemplate.insert(cyjsDynaBean);
            try {
                //更新生产任务的完成数量
                if (StringUtil.isNotEmpty(SCRWZT_SCRWID)){
                    DynaBean bean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and JGMES_PLAN_SCRW_ID = '" + SCRWZT_SCRWID + "'");
                    Integer pcsl = bean.getInt("SCRW_PCSL");//排产数量
                    SCRW_PS = bean.getStr("SCRW_PS");
                    SCRW_LS = bean.getStr("SCRW_LS");
                    String SCRW_ZTSWCS = bean.getStr("SCRW_ZTSWCS");//暂停时完成数量
                    int ztswcsl = 0;
                    if (StringUtil.isNotEmpty(SCRW_ZTSWCS)){
                        ztswcsl = Integer.parseInt(SCRW_ZTSWCS);
                    }

                    Long ps = Long.valueOf(0);//片数
                    Long ls = Long.valueOf(0);//列数
                    try {
                        if (StringUtil.isEmpty(SCRW_PS)) {
                            ps = Long.valueOf(1);
                        } else {
                            ps = Long.valueOf(SCRW_PS);
                        }
                        if (StringUtil.isEmpty(SCRW_LS)) {
                            ls = Long.valueOf(1);
                        } else {
                            ls = Long.valueOf(SCRW_LS);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        ps = Long.valueOf(1);
                        ls = Long.valueOf(1);
                    }
                    if (bean != null) {
                        Long wcsl = NormalQuantity / ps * ls+ztswcsl;
                        bean.set("SCRW_WCSL", wcsl);
//                        String wcl = String.format("%.2f", (float)wcsl / (float)pcsl);//完成率
//                        String wcl = String.format("%.2f",(float) wcsl/(float)pcsl*100)+"%";
//                        bean.set("SCRW_WCL", wcl);
                        serviceTemplate.update(bean);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //异常状态:1表示有超过5分钟机台没有动作，需要报警处理（呼叫主管），0表示没有异常
            if (AbnormalState == 1) {
                JTYCLX = "JTYCLX06";
                //机台异常类型
                DynaBean dic = jgmesCommon.getDic("JGMES_DIC_JTYCLX", JTYCLX);
                if (dic != null) {
                    JTYCLXMC = dic.get("DICTIONARYITEM_ITEMNAME") + "";
                }
                AbnormalWarning(DeviceID, jtdaDynaBean.getStr("JTDA_JTMC"), JTYCLX, JTYCLXMC);
            }

            //机台异常:1表示有按下，0表示没有按下，2表示超过时间需要处理
            //模具异常
            JTYCLX = "JTYCLX01";
            JtycTrans(jtdaDynaBean, scrwDynaBean, DeviceID, MoldException, DeviceTime, JTYCLX, jgmesCommon);

            //设备异常
            JTYCLX = "JTYCLX02";
            JtycTrans(jtdaDynaBean, scrwDynaBean, DeviceID, DeviceException, DeviceTime, JTYCLX, jgmesCommon);

            //品质异常
            JTYCLX = "JTYCLX03";
            JtycTrans(jtdaDynaBean, scrwDynaBean, DeviceID, QualityException, DeviceTime, JTYCLX, jgmesCommon);

            //物料异常
            JTYCLX = "JTYCLX04";
            JtycTrans(jtdaDynaBean, scrwDynaBean, DeviceID, MaterialsException, DeviceTime, JTYCLX, jgmesCommon);

            //呼叫主管
            JTYCLX = "JTYCLX05";
            JtycTrans(jtdaDynaBean, scrwDynaBean, DeviceID, CallSquadLeader, DeviceTime, JTYCLX, jgmesCommon);

            //回写产线机台关联表
            jtdaDynaBean.set("JTDA_LJCYCS", AllTotalQuantity);//累计冲压次数
            jtdaDynaBean.set("JTDA_LJZCCYCS", NormalQuantity);//累计正常冲压次数
            serviceTemplate.update(jtdaDynaBean);
        } else {
            message = "未获取到产线机台关联数据，请检查！";
        }

        if (message != null && !"".equals(message)) {
            re.IsSuccess = false;
            re.ErrorCode = 0;
            re.setMessage(message);
            return re;
        } else {
            re.IsSuccess = true;
            return re;
        }
    }

    /**
     * 机台异常处理
     *
     * @param jtdaDynaBean 机台档案bean
     * @param scrwDynaBean 产线中处于生产中的生产任务bean
     * @param DeviceID     机台
     * @param Exception    异常
     * @param DeviceTime   设备时间
     * @param JTYCLX       机台异常类型
     * @param jgmesCommon
     * @return
     */
    public static String JtycTrans(DynaBean jtdaDynaBean, DynaBean scrwDynaBean, String DeviceID, int Exception, String DeviceTime, String JTYCLX, JgmesCommon jgmesCommon) {
        //  0--检查该机台 是否有状态为未确认/已确认/已预警的记录，如果没有则不予处理，如果有则将状态改为已取消
        //	1--检查该机台 是否有状态为未确认/已确认/已预警的记录，如果已有则不予处理，如果没有则新增一条异常记录(状态为未确认)
        //  2--检查该机台 是否有状态为未确认/已确认/已预警的记录，如果没有则新增一条异常记录(状态为未确认)并调用报警接口，如果已有则直接调用报警接口
        //机台异常处理状态：CLZT01--未确认	CLZT02--已确认	CLZT03--已处理	CLZT04--不予处理	CLZT05--已预警	CLZT06--已取消
        String message = "";
        List<DynaBean> jtycjlDynaBeanList = serviceTemplate.selectList("JGMES_ADMK_JTYCJL", " and JTYCJL_JTH = '" + DeviceID + "' and JTYCJL_JTYCLX_CODE='" + JTYCLX + "' and JTYCJL_JTYCCLZT_CODE in('CLZT01','CLZT02','CLZT05','CLZT07')");
        //System.out.println(" and JTYCJL_JTH = '"+DeviceID+"' and JTYCJL_JTYCLX_CODE='"+JTYCLX+"' and JTYCJL_JTYCCLZT_CODE in('CLZT01','CLZT02')");
        if (jtycjlDynaBeanList == null) {
            message = "未设置机台异常记录表(JGMES_ADMK_JTYCJL)，请检查！";
        } else if (jtycjlDynaBeanList.size() > 1) {
            message = "机台【" + DeviceID + "】存在多条未处理的异常记录，请检查！";
        } else {
            String JTMC = jtdaDynaBean.getStr("JTDA_JTMC");
            String SCXBM = "";
            String RWDH = "";
            String CPBM = "";
            String GXBM = "";
            if (scrwDynaBean != null) {
                SCXBM = scrwDynaBean.getStr("SCRWZT_CXBM");
                RWDH = scrwDynaBean.getStr("SCRWZT_RWDH");
                CPBM = scrwDynaBean.getStr("SCRWZT_CPBM");
                GXBM = scrwDynaBean.getStr("SCRWZT_CXD_NAME");
            }
            DynaBean dic = null;
            String JTYCJL_ID = JEUUID.uuid();
            if (Exception == 0) {
                if (jtycjlDynaBeanList.size() == 1) {
                    DynaBean jtycjlDynaBean = jtycjlDynaBeanList.get(0);
                    //机台异常处理状态
                    dic = jgmesCommon.getDic("JGMES_DIC_JTYCCLZT", "CLZT06");//已取消
                    if (dic != null) {
                        jtycjlDynaBean.set("JTYCJL_JTYCCLZT_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));//机台异常处理状态_NAME
                        jtycjlDynaBean.set("JTYCJL_JTYCCLZT_CODE", dic.get("DICTIONARYITEM_ITEMCODE"));//机台异常处理状态
                    }
                    serviceTemplate.update(jtycjlDynaBean);
                }
            } else if (Exception == 1) {
                if (jtycjlDynaBeanList.size() == 0) {
                    //新增机台异常记录
                    InsertJtycjl(DeviceID, JTMC, SCXBM, RWDH, CPBM, GXBM, DeviceTime, JTYCJL_ID, JTYCLX, jgmesCommon);
                }
            } else if (Exception == 2) {
                if (jtycjlDynaBeanList.size() == 0) {
                    //新增机台异常记录
                    InsertJtycjl(DeviceID, JTMC, SCXBM, RWDH, CPBM, GXBM, DeviceTime, JTYCJL_ID, JTYCLX, jgmesCommon);
                } else {
                    JTYCJL_ID = jtycjlDynaBeanList.get(0).getStr("JGMES_ADMK_JTYCJL_ID");
                }

                //异常通知
                message = AbnormalInform(JTYCJL_ID);
            }

        }
        return message;
    }

    /**
     * 新增机台异常记录
     *
     * @param DeviceID    机台
     * @param JTMC        机台名称
     * @param SCXBM       生产线编码
     * @param RWDH        任务单号
     * @param CPBM        产品编码
     * @param GXBM        工序编码
     * @param JTYCJL_ID   异常记录id
     * @param DeviceTime  设备时间
     * @param JTYCLX      机台异常类型
     * @param jgmesCommon
     * @return
     */
    public static void InsertJtycjl(String DeviceID, String JTMC, String SCXBM, String RWDH, String CPBM, String GXBM, String DeviceTime, String JTYCJL_ID, String JTYCLX, JgmesCommon jgmesCommon) {
        DynaBean jtycDynaBean = new DynaBean();
        jtycDynaBean.set("JGMES_ADMK_JTYCJL_ID", JTYCJL_ID);
        jtycDynaBean.setStr(BeanUtils.KEY_TABLE_CODE, "JGMES_ADMK_JTYCJL");
        jtycDynaBean.set("JTYCJL_JTH", DeviceID);//机台号
        jtycDynaBean.set("JTYCJL_JTMC", JTMC);//机台名称
        jtycDynaBean.set("JTYCJL_SCX", SCXBM);//生产线
        jtycDynaBean.set("JTYCJL_RWDH", RWDH);//任务单号
        jtycDynaBean.set("JTYCJL_CPBM", CPBM);//产品编码
        jtycDynaBean.set("JTYCJL_GXBM", GXBM);//工序编码
        jtycDynaBean.set("JTYCJL_YCSJ", DeviceTime);//异常时间

        //机台异常类型
        DynaBean dic = jgmesCommon.getDic("JGMES_DIC_JTYCLX", JTYCLX);
        if (dic != null) {
            jtycDynaBean.set("JTYCJL_JTYCLX_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));//机台异常类型_NAME
            jtycDynaBean.set("JTYCJL_JTYCLX_CODE", dic.get("DICTIONARYITEM_ITEMCODE"));//机台异常类型
        }

        //机台异常处理状态
        dic = jgmesCommon.getDic("JGMES_DIC_JTYCCLZT", "CLZT01");//未确认
        if (dic != null) {
            jtycDynaBean.set("JTYCJL_JTYCCLZT_NAME", dic.get("DICTIONARYITEM_ITEMNAME"));//机台异常类型_NAME
            jtycDynaBean.set("JTYCJL_JTYCCLZT_CODE", dic.get("DICTIONARYITEM_ITEMCODE"));//机台异常类型
        }

        serviceTemplate.buildModelCreateInfo(jtycDynaBean);
        serviceTemplate.insert(jtycDynaBean);
    }

    /**
     * 异常通知
     *
     * @param JTYCJL_ID 异常记录ID
     * @return
     */
    public static String AbnormalInform(String JTYCJL_ID) {
        String message = "";

        //获取机台异常记录信息
        DynaBean ycjlDynaBean = serviceTemplate.selectOne("JGMES_ADMK_JTYCJL", " and JGMES_ADMK_JTYCJL_ID = '" + JTYCJL_ID + "'");
        if (ycjlDynaBean == null) {
            message = "未获取到机台异常记录信息(ID:" + JTYCJL_ID + ")，请检查！";
        } else {
            //获取异常处理人（异常类型分配）
            DynaBean ycclrDynaBean = serviceTemplate.selectOne("JGMES_ADMK_YCLXFP", " and YCLXFP_JTYCLX_CODE = '" + ycjlDynaBean.getStr("JTYCJL_JTYCLX_CODE") + "'");
            if (ycclrDynaBean == null) {
                message = "未获取到异常处理人配置信息(异常类型:" + ycjlDynaBean.getStr("JTYCJL_JTYCLX_NAME") + ")，请检查！";
            } else {
                String JTYCJL_JTH = ycjlDynaBean.getStr("JTYCJL_JTH");
                String JTYCJL_JTMC = ycjlDynaBean.getStr("JTYCJL_JTMC");
                String JTYCJL_JTYCLX_CODE = ycjlDynaBean.getStr("JTYCJL_JTYCLX_CODE");
                String JTYCJL_JTYCLX_NAME = ycjlDynaBean.getStr("JTYCJL_JTYCLX_NAME");

//                //获取异常通知，如果已存在未读的异常则不处理
//                DynaBean yctzDynaBean = serviceTemplate.selectOne("JGMES_ADMK_JTYCTZ", "and JTYCTZ_JTH='" + JTYCJL_JTH + "' and JTYCTZ_JTYCLX_CODE='" + JTYCJL_JTYCLX_CODE + "' and JTYCTZ_YDZT_CODE='1'");
//                if (yctzDynaBean == null) {
//                    //异常通知
//                    DynaBean jtyctzDynaBean = new DynaBean();
//
//                    jtyctzDynaBean.set("JGMES_ADMK_JTYCTZ_ID", JEUUID.uuid());
//                    jtyctzDynaBean.setStr(BeanUtils.KEY_TABLE_CODE, "JGMES_ADMK_JTYCTZ");
//                    jtyctzDynaBean.set("JTYCTZ_JTH", JTYCJL_JTH);//机台号
//                    jtyctzDynaBean.set("JTYCTZ_JTMC", JTYCJL_JTMC);//机台名称
//                    jtyctzDynaBean.set("JTYCTZ_YJDJ", "");//预警等级
//                    jtyctzDynaBean.set("JTYCTZ_TZNR", JTYCJL_JTMC + " " + JTYCJL_JTYCLX_NAME);//通知内容:机台名称+机台异常类型_NAME
//                    jtyctzDynaBean.set("JTYCTZ_TZR", ycclrDynaBean.getStr("YCLXFP_YCCLRBM"));//通知人(异常处理人编码)
//                    //jtyctzDynaBean.set("JTYCTZ_JTMC",ycjlDynaBean.getStr("JTYCJL_JTH"));//机台名称
//                    jtyctzDynaBean.set("JTYCTZ_JTYCLX_CODE", JTYCJL_JTYCLX_CODE);//机台异常类型
//                    jtyctzDynaBean.set("JTYCTZ_JTYCLX_NAME", JTYCJL_JTYCLX_NAME);//机台异常类型_NAME
//
//                    serviceTemplate.buildModelCreateInfo(jtyctzDynaBean);
//                    serviceTemplate.insert(jtyctzDynaBean);

                    //更新机台异常记录的状态为已预警
					/*JgmesCommon jgmesCommon = new JgmesCommon(null, serviceTemplate);
					DynaBean dic = jgmesCommon.getDic("JGMES_DIC_JTYCCLZT","CLZT05");//已预警
					if(dic!=null) {
						ycjlDynaBean.set("JTYCJL_JTYCCLZT_NAME",dic.get("DICTIONARYITEM_ITEMNAME"));//机台异常类型_NAME
						ycjlDynaBean.set("JTYCJL_JTYCCLZT_CODE",dic.get("DICTIONARYITEM_ITEMCODE"));//机台异常类型
					}
					serviceTemplate.update(ycjlDynaBean);*/
//                }
            }
        }

        return message;
    }

    /**
     * 异常预警
     *
     * @param JTH    机台号
     * @param JTH    机台名称
     * @param JTYCLX 机台异常类型
     * @param JTYCLX 机台异常类型名称
     * @return
     */
    public static String AbnormalWarning(String JTH, String JTMC, String JTYCLX, String JTYCLXMC) {
        String message = "";
        //获取异常处理人（异常类型分配）
        DynaBean ycclrDynaBean = serviceTemplate.selectOne("JGMES_ADMK_YCLXFP", " and YCLXFP_JTYCLX_CODE = '" + JTYCLX + "'");
        if (ycclrDynaBean == null) {
            message = "未获取到异常处理人配置信息(异常类型:" + JTYCLXMC + ")，请检查！";
        } else {
            //获取机台未读异常通知信息
            //DynaBean ycyjDynaBean = serviceTemplate.selectOne("JGMES_ADMK_JTYCYJB"," and JTYCYJB_JTYCYJSYZT_CODE=1 and JTYCYJB_JTYCLX_CODE = '"+JTYCLX+"' and JTYCYJB_JTH='"+JTH+"'");
            DynaBean yctzDynaBean = serviceTemplate.selectOne("JGMES_ADMK_JTYCTZ", " and JTYCTZ_YDR='' and JTYCTZ_JTYCLX_CODE = '" + JTYCLX + "' and JTYCTZ_JTH='" + JTH + "'");
//            if (yctzDynaBean == null) {
//                //message= "未获取到机台异常通知信息("+JTMC+":"+JTYCLXMC+")，请检查！";
//                DynaBean jtyctzDynaBean = new DynaBean();
//                jtyctzDynaBean.set("JGMES_ADMK_JTYCTZ_ID", JEUUID.uuid());
//                jtyctzDynaBean.setStr(BeanUtils.KEY_TABLE_CODE, "JGMES_ADMK_JTYCTZ");
//                jtyctzDynaBean.set("JTYCTZ_JTH", JTH);//机台号
//                jtyctzDynaBean.set("JTYCTZ_JTMC", JTMC);//机台名称
//                jtyctzDynaBean.set("JTYCTZ_YJDJ", "");//预警等级
//                jtyctzDynaBean.set("JTYCTZ_TZNR", JTMC + " " + JTYCLXMC);//通知内容:机台名称+机台异常类型_NAME
//                jtyctzDynaBean.set("JTYCTZ_TZR", ycclrDynaBean.getStr("YCLXFP_YCCLRBM"));//通知人(异常处理人编码)
//                jtyctzDynaBean.set("JTYCTZ_JTYCLX_CODE", JTYCLX);//机台异常类型
//                jtyctzDynaBean.set("JTYCTZ_JTYCLX_NAME", JTYCLXMC);//机台异常类型_NAME
//
//                serviceTemplate.buildModelCreateInfo(jtyctzDynaBean);
//                serviceTemplate.insert(jtyctzDynaBean);
//            }
        }
        return message;
    }


    /**
     * 返回 布尔类型对象
     *
     * @author John
     */
    public class ResultBoolean {

        public ResultBoolean() {
            this.IsSuccess = true;
        }

        private boolean IsSuccess;

        public boolean getIsSuccess() {
            return IsSuccess;
        }

        public void setIsSuccess(boolean IsSuccess) {
            this.IsSuccess = IsSuccess;
        }

        private String Message;

        public String getMessage() {
            return Message;
        }

        public void setMessage(String message) {
            this.Message = message;
            this.IsSuccess = false;
        }

        private boolean Data;

        public boolean getData() {
            return Data;
        }

        public void setData(boolean data) {
            Data = data;
        }

        private Integer ErrorCode;

        public Integer getErrorCode() {
            return ErrorCode;
        }

        public void setErrorCode(Integer errorCode) {
            ErrorCode = errorCode;
        }
    }

}