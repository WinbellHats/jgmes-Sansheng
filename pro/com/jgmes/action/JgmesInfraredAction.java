package com.jgmes.action;

import com.je.core.action.DynaAction;
import com.je.core.util.JEUUID;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.BeanUtils;
import com.je.core.util.bean.DynaBean;
import com.jgmes.util.JgmesCommon;
import com.jgmes.util.JgmesEnumsDic;
import com.jgmes.util.JgmesResult;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * ????????
 *
 * @author liuc
 * @version 2019-03-25 17:10:30
 * @see /jgmes/jgmesInfraredAction!load.action
 */
@Component("jgmesInfraredAction")
@Scope("prototype")
public class JgmesInfraredAction extends DynaAction {

    //private JgmesInfraredService jgmesInfraredService;

    public void load() {
//		String file = request.getParameter("form");
//		MultiPartRequestWrapper req = (MultiPartRequestWrapper) request;
//		String realPath = req.getRealPath("/JE/data/upload/") + "/";// 取系统当前路径
//		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
//		jgmesCommon.fileUplod(realPath);
//		String Password = req.getParameter("Password");
//		toWrite("nihao!");
    }

    //获取当前产线生产任务单列表
    public void getCxScrw() {
        String mac = request.getParameter("mac");// MAC地址
        String userCode = request.getParameter("userCode");// 用户编码  必填
        String prodLineCode = request.getParameter("prodLineCode");// 产线编码
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        String d = request.getParameter("date");//日期
        String jsonStr = "";
        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            List<DynaBean> scrwZtDynaBeanListforreturn = new ArrayList<DynaBean>();
            List<DynaBean> scrwZtDynaBeanList = new ArrayList<>();
            try {
                if (prodLineCode != null && !"".equals(prodLineCode)) {
                    if (StringUtil.isNotEmpty(d)) {
                        d = d.replaceAll("/", "-");
                        scrwZtDynaBeanList = serviceTemplate.selectList("JGMES_PLAN_SCRW", " and SCRW_CXBM  = '" + prodLineCode + "' and SCRW_RWZT_CODE != 'RWZT03' and SCRW_JHKGSJ='" + d + "'");
                    } else {
                        scrwZtDynaBeanList = serviceTemplate.selectList("JGMES_PLAN_SCRW", " and SCRW_CXBM  = '" + prodLineCode + "' and SCRW_RWZT_CODE != 'RWZT03'");
                    }
                } else {
                    if (StringUtil.isNotEmpty(d)) {
                        d = d.replaceAll("/", "-");
                        scrwZtDynaBeanList = serviceTemplate.selectList("JGMES_PLAN_SCRW", " and SCRW_RWZT_CODE != 'RWZT03' and SCRW_JHKGSJ='" + d + "'");
                    } else {
                        scrwZtDynaBeanList = serviceTemplate.selectList("JGMES_PLAN_SCRW", " and SCRW_RWZT_CODE != 'RWZT03'");
                    }
                }
                if (scrwZtDynaBeanList != null && scrwZtDynaBeanList.size() > 0) {
                    for (DynaBean scrwZtDynaBean : scrwZtDynaBeanList) {
                        List<DynaBean> scrwztDynaBeanList = serviceTemplate.selectList("JGMES_PB_SCRWZT", " and SCRWZT_RWDH = '" + scrwZtDynaBean.getStr("SCRW_RWDH") + "' and SCRWZT_SCZT_CODE = 'SCZT01'");
                        if (scrwztDynaBeanList != null && scrwztDynaBeanList.size() > 0) {
                            for (DynaBean scrwztDynaBean : scrwztDynaBeanList) {
                                scrwZtDynaBean.set("SCRWZT_CXD", scrwztDynaBean.getStr("SCRWZT_CXD"));
                                scrwZtDynaBeanListforreturn.add(scrwZtDynaBean);
                            }
                        } else {
                            if (!"RWZT02".equals(scrwZtDynaBean.getStr("SCRW_RWZT_CODE"))) {
                                scrwZtDynaBean.set("SCRWZT_CXD", "");
                                scrwZtDynaBeanListforreturn.add(scrwZtDynaBean);
                            }
                        }
                    }
                    ret.Data = ret.getValues(scrwZtDynaBeanListforreturn);
                    ret.TotalCount = (long) scrwZtDynaBeanList.size();
                }
//				}
//				else {
//					ret.setMessage("未获取到产线编码！");
//				}

            } catch (Exception e) {
                logger.error(e.toString());
                e.printStackTrace();
                ret.setMessage("系统发生异常，请联系管理员！");
            }
        } else {
            ret.setMessage("用户验证合法性失败！");
        }
        jsonStr = jsonBuilder.toJson(ret);
        toWrite(jsonStr);
    }


    //批量开工页面----获取当前产线生产任务单列表,author:ljs
    public void getBatchCxScrw() {
        String mac = request.getParameter("mac");// MAC地址
        String userCode = request.getParameter("userCode");// 用户编码  必填
        String prodLineCode = request.getParameter("prodLineCode");// 产线编码
        String noLike = request.getParameter("noLike");//单号信息
        String cpLike = request.getParameter("cpLike");//产品信息
        String zt = request.getParameter("zt");
        String teamOfTime = request.getParameter("teamOfTime");//班别
        String bz = request.getParameter("BzData");//班组
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        String d = request.getParameter("date");//日期
        String jsonStr = "";

        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            List<DynaBean> scrwZtDynaBeanListforreturn = new ArrayList<DynaBean>();
            List<DynaBean> scrwZtDynaBeanList = new ArrayList<>();
//			String whereSql = "and SCRW_RWZT_CODE != 'RWZT03'";
            try {
                //产线
                if (StringUtil.isNotEmpty(prodLineCode)) {
//					whereSql+=" and SCRW_CXBM  = '" + prodLineCode+"'";
                    if (prodLineCode.indexOf(",") != -1) {
                        //sql += jgmesCommon.getSqlWhere("SCRW_PCZT_CODE", "'"+zt.replaceAll(",", "','")+"'");
                        whereSql += " and SCRW_CXBM in ('" + prodLineCode.replaceAll(",", "','") + "')";
                    } else {
                        //sql += jgmesCommon.getSqlWhere("SCRW_PCZT_CODE", zt);
                        whereSql += jgmesCommon.getSqlWhere("SCRW_CXBM", prodLineCode);
                    }
                }
                //计划开工时间
                if (StringUtil.isNotEmpty(d)) {
                    d = d.replaceAll("/", "-");
                    whereSql += "and SCRW_PCRQ='" + d + "'";
                }
                //单据信息
                if (StringUtil.isNotEmpty(noLike)) {
                    whereSql += " and (SCRW_GDHM like '%" + noLike + "%' or SCRW_RWDH like '%" + noLike + "%' or SCRW_DDHM like '%" + noLike + "%' or SCRW_GLDH like '%" + noLike + "%') ";
                }
                //产品信息
                if (StringUtil.isNotEmpty(cpLike)) {
                    whereSql += " and (SCRW_CPBH like '%" + cpLike + "%' or SCRW_NAME like '%" + cpLike + "%' or SCRW_CPGG like '%" + cpLike + "%') ";
                }
                //班别
                if (StringUtil.isNotEmpty(teamOfTime)) {
                    if (teamOfTime.indexOf(",") != -1) {
                        whereSql += " and SCRW_BB in ('" + teamOfTime.replaceAll(",", "','") + "')";
                    } else {
                        whereSql += " and SCRW_BB like '%" + teamOfTime + "%'";
                    }
                }
                //班组
                if (StringUtil.isNotEmpty(bz)) {
                    if (bz.indexOf(",") != -1) {
                        whereSql += " and SCRW_BZNAME in ('" + bz.replaceAll(",", "','") + "')";
                    } else {
                        whereSql += " and SCRW_BZNAME like '%" + bz + "%'";
                    }
                }
                //状态
                if (StringUtil.isNotEmpty(zt)) {
                    if (zt.indexOf(",") != -1) {
                        //sql += jgmesCommon.getSqlWhere("SCRW_PCZT_CODE", "'"+zt.replaceAll(",", "','")+"'");
                        whereSql += " and SCRW_RWZT_CODE in ('" + zt.replaceAll(",", "','") + "')";
                    } else {
                        //sql += jgmesCommon.getSqlWhere("SCRW_PCZT_CODE", zt);
                        whereSql += jgmesCommon.getSqlWhere("SCRW_RWZT_CODE", zt);
                    }
                }
                scrwZtDynaBeanList = serviceTemplate.selectList("JGMES_PLAN_SCRW", whereSql);
                if (scrwZtDynaBeanList != null && scrwZtDynaBeanList.size() > 0) {
                    for (DynaBean scrwZtDynaBean : scrwZtDynaBeanList) {
                        List<DynaBean> scrwztDynaBeanList = serviceTemplate.selectList("JGMES_PB_SCRWZT", " and SCRWZT_RWDH = '" + scrwZtDynaBean.getStr("SCRW_RWDH") + "' and SCRWZT_SCZT_CODE = 'SCZT01'");
                        if (scrwztDynaBeanList != null && scrwztDynaBeanList.size() > 0) {
                            for (DynaBean scrwztDynaBean : scrwztDynaBeanList) {
                                scrwZtDynaBean.set("SCRWZT_CXD", scrwztDynaBean.getStr("SCRWZT_CXD"));
                                scrwZtDynaBeanListforreturn.add(scrwZtDynaBean);
                            }
                        } else {
                            if (!"RWZT02".equals(scrwZtDynaBean.getStr("SCRW_RWZT_CODE"))) {
                                scrwZtDynaBean.set("SCRWZT_CXD", "");
                                scrwZtDynaBeanListforreturn.add(scrwZtDynaBean);
                            }
                        }
                    }
                    ret.Data = ret.getValues(scrwZtDynaBeanListforreturn);
                    ret.TotalCount = (long) scrwZtDynaBeanList.size();
                }
            } catch (Exception e) {
                logger.error(e.toString());
                e.printStackTrace();
                ret.setMessage("系统发生异常，请联系管理员！");
            }
        } else {
            ret.setMessage("用户验证合法性失败！");
        }
        jsonStr = jsonBuilder.toJson(ret);
        toWrite(jsonStr);
    }


    //心加或者修改生产任务状态表
    public void updateScrwZtB() {
        String mac = request.getParameter("mac");// MAC地址
        String userCode = request.getParameter("userCode");// 用户编码  必填
        String prodLineCode = request.getParameter("prodLineCode");// 产线编码
        String taskCode = request.getParameter("taskCode");// 生产任务单号
        String startSection = request.getParameter("startSection");// 产线段
        String taskStatus = request.getParameter("taskStatus");// 任务状态
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        String scrwId = request.getParameter("scrwId");// 任务单id
        String jsonStr = "";
        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                String startSectionName = "";
                //产线编码
                if ((prodLineCode == null || prodLineCode.isEmpty())) {
                    ret.setMessage("产线编码不能都为空！");
                }
                //生产任务单号
                if (taskCode == null || taskCode.isEmpty()) {
                    ret.setMessage("生产任务单号不能都为空！");
                }
                //产线段
                if (startSection == null || startSection.isEmpty()) {
                    ret.setMessage("生产段不能都为空！");
                } else {
                    if (JgmesEnumsDic.CXDQUD.getKey().equals(startSection)) {
                        startSectionName = JgmesEnumsDic.CXDQUD.getValue();
                    } else if (JgmesEnumsDic.CXDQD.getKey().equals(startSection)) {
                        startSectionName = JgmesEnumsDic.CXDQD.getValue();
                    } else if (JgmesEnumsDic.CXDHD.getKey().equals(startSection)) {
                        startSectionName = JgmesEnumsDic.CXDHD.getValue();
                    }
                }
                //任务状态
                if ((taskStatus == null || taskStatus.isEmpty())) {
                    ret.setMessage("任务状态不能都为空！");
                }
                if ((prodLineCode != null && !prodLineCode.isEmpty()) &&
                        (taskCode != null && !taskCode.isEmpty()) &&
                        (startSection != null && !startSection.isEmpty()) &&
                        (taskStatus != null && !taskStatus.isEmpty())) {

//					//检查有没有进行首件检验
//					String message=jgmesCommon.checkSJ(prodLineCode,taskCode);
//					if(message!=null&&!"".equals(message)){
//						ret.setMessage(message);
//						toWrite(jsonBuilder.toJson(ret));
//					}

                    //若是开工的，把该产线下所有生产中的任务单都暂停
                    if (taskStatus.equals("RWZT02")) {
                        //获取该产线生产状态表中生产中的任务单，改成暂停
                        List<DynaBean> jgmes_pb_scrwzt = serviceTemplate.selectList("JGMES_PB_SCRWZT", "and SCRWZT_SCZT_CODE='SCZT01' and SCRWZT_CXBM='" + prodLineCode + "'");
                        if (jgmes_pb_scrwzt.size() > 0) {
                            for (DynaBean bean : jgmes_pb_scrwzt) {
                                bean.set("SCRWZT_SCZT_CODE", "SCZT02");
                                bean.set("SCRWZT_SCZT_NAME", "暂停");
                                serviceTemplate.update(bean);
                                DynaBean scrwBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", "and SCRW_RWDH='" + bean.getStr("SCRWZT_RWDH") + "'");
                                scrwBean.set("SCRW_RWZT_CODE", "RWZT04");
                                scrwBean.set("SCRW_RWZT_NAME", "暂停");
                            }
                        }
                        //获取该产线生产任务表中生产中的任务单，改成暂停
                        List<DynaBean> jgmes_plan_scrw = serviceTemplate.selectList("JGMES_PLAN_SCRW", "and  SCRW_CXBM='" + prodLineCode + "' and SCRW_RWZT_CODE='RWZT02'");
                        if (jgmes_plan_scrw.size() > 0) {
                            for (DynaBean bean : jgmes_plan_scrw) {
                                bean.set("SCRW_RWZT_CODE", "RWZT04");
                                bean.set("SCRW_RWZT_NAME", "暂停");
                                serviceTemplate.update(bean);
                                List<DynaBean> dynaBeans = serviceTemplate.selectList("JGMES_PB_SCRWZT", "and SCRWZT_SCZT_CODE='SCZT01' and SCRWZT_RWDH='" + bean.getStr("SCRW_RWDH") + "'");
                                if (dynaBeans.size() > 0) {
                                    for (DynaBean dynaBean1 : dynaBeans) {
                                        dynaBean1.set("SCRWZT_SCZT_CODE", "SCZT02");
                                        dynaBean1.set("SCRWZT_SCZT_NAME", "暂停");
                                    }
                                }
                            }
                        }
                    }
                    JgmesEnumsDic startSectionDic = jgmesCommon.getJgmesEnumsDic(startSection);
                    DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and SCRW_RWDH  = '" + taskCode + "'");
                    //查询一下生产任务状态表
                    List<DynaBean> scrwZTDynaBeanList = serviceTemplate.selectList("JGMES_PB_SCRWZT", " and SCRWZT_CXBM  = '" + prodLineCode + "' and SCRWZT_SCZT_CODE = 'SCZT01'");

                    if (scrwDynaBean != null) {
                        boolean isQUD = false;
                        boolean isQD = false;
                        boolean isHD = false;
                        if (JgmesEnumsDic.ScDoing.getKey().equals(taskStatus)) {
                            if (scrwZTDynaBeanList != null && scrwZTDynaBeanList.size() > 0) {
                                for (DynaBean scrwZTDynaBean : scrwZTDynaBeanList) {
                                    if (startSection.equals(scrwZTDynaBean.getStr("SCRWZT_CXD"))) {
                                        if (!"RWZT02".equals(scrwDynaBean.getStr("SCRW_RWZT_CODE"))) {
                                            doSaveScrwZt(userCode, scrwDynaBean.getStr("JGMES_PLAN_SCRW_ID"), JgmesEnumsDic.ScDoing.getKey(), "");
                                        }
                                        ret.setMessage("产线：" + prodLineCode + "中产线段为" + startSectionName + "的生产任务已经存在！");
                                        toWrite(jsonBuilder.toJson(ret));
                                        return;
                                    }
                                    if (scrwZTDynaBean.getStr("SCRWZT_CXD") != null &&
                                            JgmesEnumsDic.CXDQD.getKey().equals(scrwZTDynaBean.getStr("SCRWZT_CXD"))
                                    ) {
                                        isQD = true;
                                    } else if (scrwZTDynaBean.getStr("SCRWZT_CXD") != null &&
                                            JgmesEnumsDic.CXDHD.getKey().equals(scrwZTDynaBean.getStr("SCRWZT_CXD"))) {
                                        isHD = true;
                                    } else if (scrwZTDynaBean.getStr("SCRWZT_CXD") != null &&
                                            JgmesEnumsDic.CXDQUD.getKey().equals(scrwZTDynaBean.getStr("SCRWZT_CXD"))) {
                                        isQUD = true;
                                    }
                                }
                                if (isQD && ("CXD02".equals(startSection) || "CXD01".equals(startSection))) {
                                    ret.setMessage("这条产线前段已经开工了！");
                                    toWrite(jsonBuilder.toJson(ret));
                                    return;
                                }
                                if (isHD && ("CXD03".equals(startSection) || "CXD01".equals(startSection))) {
                                    ret.setMessage("这条产线后段已经开工了！");
                                    toWrite(jsonBuilder.toJson(ret));
                                    return;
                                }
                                if (isQUD && ("CXD03".equals(startSection) || "CXD02".equals(startSection))) {
                                    ret.setMessage("该条产线已经有全段开工的任务,所以不能前段开工和后段开工！");
                                    toWrite(jsonBuilder.toJson(ret));
                                    return;
                                }
                                if (isQUD && ("CXD01".equals(startSection))) {
                                    ret.setMessage("整条产线全段开工只能有一条!");
                                    toWrite(jsonBuilder.toJson(ret));
                                    return;
                                }

                            }
                            DynaBean scrwZtDynaBean = new DynaBean();
                            scrwZtDynaBean.setStr(beanUtils.KEY_TABLE_CODE, "JGMES_PB_SCRWZT");
                            scrwZtDynaBean.set("JGMES_PB_SCRWZT_ID", JEUUID.uuid());
                            scrwZtDynaBean.set("SCRWZT_CXBM", prodLineCode);//产线编码

                            DynaBean cxdDic = jgmesCommon.getDic("JGMES_DIC_CXD", startSection);
                            if (cxdDic != null) {
                                scrwZtDynaBean.set("SCRWZT_CXD", cxdDic.getStr("DICTIONARYITEM_ITEMCODE"));//产线段
                                scrwZtDynaBean.set("SCRWZT_CXD_NAME", cxdDic.getStr("DICTIONARYITEM_ITEMNAME"));//产线名称
                            }
                            scrwZtDynaBean.set("SCRWZT_RWDH", taskCode);//任务单号
                            scrwZtDynaBean.set("SCRWZT_CPBM", scrwDynaBean.getStr("SCRW_CPBH"));//产品编码
                            scrwZtDynaBean.set("SCRWZT_CPMC", scrwDynaBean.getStr("SCRW_NAME"));//产品名称
                            scrwZtDynaBean.set("SCRWZT_CPGG", scrwDynaBean.getStr("SCRW_CPGG"));//产品规格
                            scrwZtDynaBean.set("SCRWZT_KGSJ", jgmesCommon.getCurrentTime());//开工时间
                            scrwZtDynaBean.set("JGMES_PLAN_SCRW_ID", scrwId);//回写生产任务id

                            //设置订单号码
                            scrwZtDynaBean.set("SCRWZT_DDHM", scrwDynaBean.getStr("SCRW_DDHM"));
                            DynaBean dic1 = jgmesCommon.getDic("JGMES_DIC_SCZT", "SCZT01");
                            if (dic1 != null) {
                                scrwZtDynaBean.set("SCRWZT_SCZT_CODE", dic1.get("DICTIONARYITEM_ITEMCODE"));//生产状态
                            }


                            List<DynaBean> scrwztDynaBean = serviceTemplate.selectList("JGMES_PB_SCRWZT", " and SCRWZT_RWDH = '" + taskCode + "' and SCRWZT_CXD = '" + startSection + "' and SCRWZT_CXBM='" + prodLineCode + "' and SCRWZT_SCZT_CODE = 'SCZT02'");
                            if (scrwztDynaBean == null || scrwztDynaBean.size() == 0) {
                                //开工的时候给计数表一个初始值
                                //获取生产线机台关联表
                                DynaBean cyjsDynaBean = new DynaBean();
                                cyjsDynaBean.set("JGMES_SCGCGL_CYJSB_ID", JEUUID.uuid());
                                cyjsDynaBean.setStr(BeanUtils.KEY_TABLE_CODE, "JGMES_SCGCGL_CYJSB");
                                cyjsDynaBean.set("CYJSB_CXBM", prodLineCode);
                                cyjsDynaBean.set("CYJSB_RWDH", scrwZtDynaBean.getStr("SCRWZT_RWDH"));
                                cyjsDynaBean.set("CYJSB_DDHM", scrwZtDynaBean.getStr("SCRWZT_DDHM"));
                                cyjsDynaBean.set("CYJSB_CYCS", 0);
                                cyjsDynaBean.set("CYJSB_ZCCYCS", 0);
                                cyjsDynaBean.set("CYJSB_JLSJ", jgmesCommon.getCurrentTime());
                                cyjsDynaBean.set("CYJSB_JLSJ", jgmesCommon.getCurrentTime());

                                //回写冲压计数表中的数据
                                DynaBean d = jgmesCommon.getDic("JGMES_YES_NO", "0");
                                if (dic1 != null) {
                                    cyjsDynaBean.set("CYJSB_NO_NAME", d.get("DICTIONARYITEM_ITEMNAME"));
                                    cyjsDynaBean.set("CYJSB_NO_CODE", d.get("DICTIONARYITEM_ITEMCODE"));
                                }
                                serviceTemplate.insert(cyjsDynaBean);
                            }

                            //插入数据
                            serviceTemplate.insert(scrwZtDynaBean);

                            //修改生产任务的的生产状态  1、修改改生产任务的任务状态为生产中
                            doSaveScrwZt(userCode, scrwDynaBean.getStr("JGMES_PLAN_SCRW_ID"), JgmesEnumsDic.ScDoing.getKey(), "");
                            //把该产线中的其他生产任务状态为生产中的改成
                            //将该产线上其它的生产任务单暂停掉
							/*List<DynaBean> list = serviceTemplate.selectList("JGMES_PLAN_SCRW", " and SCRW_CXBM='"+prodLineCode+"' and SCRW_RWDH<>'"+taskCode+"' and SCRW_RWZT_CODE='"+ JgmesEnumsDic.ScDoing.getKey()+"'");
							if (list != null && list.size()>0) {
								for (int i = 0; i < list.size(); i++) {
									DynaBean scrwDynaBean1 = list.get(0);
									doSaveScrwZt(userCode, scrwDynaBean1.getStr("JGMES_PLAN_SCRW_ID"), JgmesEnumsDic.ScPause.getKey(), "");
								}
							}*/
                        } else {
                            List<DynaBean> scrwztDynaBeanList = serviceTemplate.selectList("JGMES_PB_SCRWZT", " and SCRWZT_RWDH = '" + taskCode + "' and SCRWZT_CXD = '" + startSection + "' and SCRWZT_CXBM='" + prodLineCode + "'");
                            if (scrwztDynaBeanList != null && scrwztDynaBeanList.size() > 0) {
                                for (DynaBean scrwztDynaBean : scrwztDynaBeanList) {
                                    //如果是完工的
                                    if (JgmesEnumsDic.ScFinished.getKey().equals(taskStatus) && !"SCZT03".equals(scrwztDynaBean.getStr("SCRWZT_SCZT_CODE"))) {
                                        DynaBean dic1 = jgmesCommon.getDic("JGMES_DIC_SCZT", "SCZT03");
                                        if (dic1 != null) {
                                            scrwztDynaBean.set("SCRWZT_SCZT_CODE", dic1.get("DICTIONARYITEM_ITEMCODE"));//生产状态
                                        }
                                        scrwztDynaBean.set("SCRWZT_WGSJ", jgmesCommon.getCurrentTime());
                                    } else if (JgmesEnumsDic.ScPause.getKey().equals(taskStatus) && !"SCZT02".equals(scrwztDynaBean.getStr("SCRWZT_SCZT_CODE"))) {//如果是
                                        DynaBean dic1 = jgmesCommon.getDic("JGMES_DIC_SCZT", "SCZT02");
                                        if (dic1 != null) {
                                            scrwztDynaBean.set("SCRWZT_SCZT_CODE", dic1.get("DICTIONARYITEM_ITEMCODE"));//生产状态
                                        }
                                        scrwztDynaBean.set("SCRWZT_ZTSJ", jgmesCommon.getCurrentTime());
                                    }
                                    serviceTemplate.update(scrwztDynaBean);
                                }
								/*
								List<DynaBean> scrwztDynaBeanList2 = serviceTemplate.selectList("JGMES_PB_SCRWZT", " and SCRWZT_RWDH = '"+taskCode+"' and SCRWZT_CXBM='"+prodLineCode+"'");
								boolean isFinished = true;
								boolean isPause = true;
								for(DynaBean scrwztDynaBean:scrwztDynaBeanList2) {
									if(!"CXD01".equals(scrwztDynaBean.getStr("SCRWZT_CXD"))&&!"SCZT03".equals(scrwztDynaBean.getStr("SCRWZT_SCZT_CODE"))) {
										isFinished = false;
									}else if(!"CXD01".equals(scrwztDynaBean.getStr("SCRWZT_CXD"))&&!"SCZT02".equals(scrwztDynaBean.getStr("SCRWZT_SCZT_CODE"))) {
										isPause = false;
									}
								}*/
                                if (JgmesEnumsDic.ScFinished.getKey().equals(taskStatus)) {
                                    doSaveScrwZt(userCode, scrwDynaBean.getStr("JGMES_PLAN_SCRW_ID"), JgmesEnumsDic.ScFinished.getKey(), "");
                                } else if (JgmesEnumsDic.ScPause.getKey().equals(taskStatus)) {//如果是
                                    doSaveScrwZt(userCode, scrwDynaBean.getStr("JGMES_PLAN_SCRW_ID"), JgmesEnumsDic.ScPause.getKey(), "");
                                }
                            }
                        }
                    }
//                    //回写产线资料中的模具信息
//                    String mjbh = "";//模具编号
//                    if (scrwDynaBean != null) {
//                        mjbh = scrwDynaBean.getStr("SCRW_MJBH");
//                        DynaBean jgmes_base_mj = serviceTemplate.selectOne("JGMES_BASE_MJ", "and MJ_BH='" + mjbh + "'");
//                        String mjmc = "";//模具名称
//                        if (jgmes_base_mj != null) {
//                            mjmc = jgmes_base_mj.getStr("MJ_MC");//模具名称
//                        }
//                        DynaBean cxsjBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ", "and CXSJ_CXBM = '" + prodLineCode + "'");
//                        if (cxsjBean != null) {
//                            cxsjBean.setStr("CXSJ_MJBH", mjbh);
//                            cxsjBean.setStr("CXSJ_MJMC", mjmc);
//                            serviceTemplate.update(cxsjBean);
//                        }
//                    }
//                    //暂停任务时，把当前生产任务完成数量回写到生产任务-暂停时完成数中
//                    if (taskStatus.equals("RWZT04")) {
//                        DynaBean jgmes_plan_scrw = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and JGMES_PLAN_SCRW_ID='" + scrwId + "'");
//                        if (jgmes_plan_scrw != null) {
//                            String SCRW_WCSL = jgmes_plan_scrw.getStr("SCRW_WCSL");//当前完成数量
//                            jgmes_plan_scrw.setStr("SCRW_ZTSWCS", SCRW_WCSL);
//                            serviceTemplate.update(jgmes_plan_scrw);
//                        }
//                    }
                }
            } catch (Exception e) {
                logger.error(e.toString());
                e.printStackTrace();
                ret.setMessage("系统发生异常，请联系管理员！");
            }
            toWrite(jsonBuilder.toJson(ret));
        }
    }

    /**
     * 查看当前产线是否存在生产中的任务单号
     * 更新：
     */
    public void checkCXZT() {
        String rwdh = request.getParameter("rwdh");// 任务单号
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
        List<DynaBean> jgmes_plan_scrw = serviceTemplate.selectList("JGMES_PLAN_SCRW", "and SCRW_RWDH='" + rwdh + "'");
        if (jgmes_plan_scrw.size() > 0) {
            String cxbm = jgmes_plan_scrw.get(0).getStr("SCRW_CXBM");
            String cxmc = jgmes_plan_scrw.get(0).getStr("SCRW_CXMC");
            if (StringUtil.isNotEmpty(cxbm)) {
                List<DynaBean> jgmes_plan_scrw1 = serviceTemplate.selectList("JGMES_PLAN_SCRW", "and SCRW_CXBM ='" + cxbm + "' and SCRW_RWZT_CODE='RWZT02'");
                List<DynaBean> jgmes_pb_scrwzt = serviceTemplate.selectList("JGMES_PB_SCRWZT", "and SCRWZT_CXBM='" + cxbm + "' and SCRWZT_SCZT_CODE = 'SCZT01'");
                if (jgmes_plan_scrw1.size() > 0) {
                    for (DynaBean bean : jgmes_pb_scrwzt) {
                        bean.set("SCRWZT_SCZT_CODE", "SCZT02");//修改成暂停
                        serviceTemplate.update(bean);
                    }
                    DynaBean scrwZtDynaBean = new DynaBean();
                    scrwZtDynaBean.setStr(beanUtils.KEY_TABLE_CODE, "JGMES_PB_SCRWZT");
                    scrwZtDynaBean.set("JGMES_PB_SCRWZT_ID", JEUUID.uuid());
                    scrwZtDynaBean.set("SCRWZT_CXBM", cxbm);//产线编码
                    scrwZtDynaBean.set("SCRWZT_CXD", "CXD01");//产线段
                    scrwZtDynaBean.set("SCRWZT_CXD_NAME", cxmc);//产线名称
                    scrwZtDynaBean.set("SCRWZT_RWDH", jgmes_plan_scrw1.get(0).getStr("SCRW_RWDH"));//任务单号
                    scrwZtDynaBean.set("SCRWZT_CPBM", jgmes_plan_scrw.get(0).getStr("SCRW_CPBH"));//产品编码
                    scrwZtDynaBean.set("SCRWZT_CPMC", jgmes_plan_scrw.get(0).getStr("SCRW_NAME"));//产品名称
                    scrwZtDynaBean.set("SCRWZT_CPGG", jgmes_plan_scrw.get(0).getStr("SCRW_CPGG"));//产品规格
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                    scrwZtDynaBean.set("SCRWZT_KGSJ", df.format(new Date()));//开工时间
                    scrwZtDynaBean.set("JGMES_PLAN_SCRW_ID", jgmes_plan_scrw.get(0).getStr("JGMES_PLAN_SCRW_ID"));//回写生产任务id
                    scrwZtDynaBean.set("SCRWZT_SCZT_CODE", "SCZT01");//生产状态
                    serviceTemplate.insert(scrwZtDynaBean);
                    ret.setMessage("该产线已存在生产中的任务");
                }
            } else {
                ret.setMessage("该生产任务单没有产线编码信息");
            }
        } else {
            ret.setMessage("该任务单号没有对应的生产任务信息！");
        }
        toWrite(jsonBuilder.toJson(ret));
    }

    /**
     * 获取机台号
     */
    public void getJTH() {
        String sb = request.getParameter("sb");// 任务状态
        String mac = request.getParameter("mac");// MAC地址
        String userCode = request.getParameter("userCode");// 用户编码  必填
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
        List<DynaBean> jgmes_admk_jtda = serviceTemplate.selectList("JGMES_ADMK_JTDA", "and JTDA_SCXBM='" + sb + "'");
        HashMap map = new HashMap();
        if (jgmes_admk_jtda.size() > 0) {
            String deviceID = jgmes_admk_jtda.get(0).getStr("JTDA_JTH");//获取机台号
            if (StringUtil.isNotEmpty(deviceID)) {
                map.put("DeviceID", deviceID);
                ret.Data = map;
            } else {
                ret.setMessage("对应的机台档案中机台号为空");
            }
        } else {
            ret.setMessage("没有找到对应的机台档案信息");
        }
        toWrite(jsonBuilder.toJson(ret));
    }

    //获取工单信息，嘉俊
    public void getGDData() {
        String mac = request.getParameter("mac");// MAC地址
        String userCode = request.getParameter("userCode");// 用户编码  必填
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        String gdId = request.getParameter("gdId");// MAC地址
        if (StringUtil.isNotEmpty(gdId)) {
            List<DynaBean> jgmes_plan_gdlb = serviceTemplate.selectList("JGMES_PLAN_GDLB", "and JGMES_PLAN_GDLB_ID='" + gdId + "'");
            if (jgmes_plan_gdlb.size() > 0) {
                ret.Data = ret.getValues(jgmes_plan_gdlb);
                ret.IsSuccess = true;
            } else {
                ret.setMessage("获取工单信息失败！");
            }
        } else {
            ret.setMessage("数据获取失败！");
        }
        toWrite(jsonBuilder.toJson(ret));
    }

    /**
     * 更新生产任务信息
     */
    public void updateSCRWData() {
        String mac = request.getParameter("mac");// MAC地址
        String userCode = request.getParameter("userCode");// 用户编码  必填
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        String jsonStr = request.getParameter("jsonStr");
        try {
            if (StringUtil.isNotEmpty(jsonStr)) {
                DynaBean gdlbDynaBean = jgmesCommon.getDynaBeanByJsonStr("JGMES_PLAN_SCRW", jsonStr);
                if (gdlbDynaBean != null) {
                    DynaBean update = serviceTemplate.update(gdlbDynaBean);
                    if (update != null) {
                        ret.IsSuccess = true;
                    } else {
                        ret.setMessage("更新失败！");
                    }
                } else {
                    ret.setMessage("转换数据错误！");
                }
            } else {
                ret.setMessage("获取保存信息失败！");
            }

        } catch (Exception e) {
            e.printStackTrace();
            ret.setMessage("异常错误！");
        }
        toWrite(jsonBuilder.toJson(ret));

    }


    /**
     * 乐惠手动报工储存方法
     */
    public void saveLHBGSJ() {
        String mac = request.getParameter("mac");// MAC地址
        String userCode = request.getParameter("userCode");// 用户编码  必填
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
        String jsonStr = request.getParameter("jsonStr");// MAC地址
        try {
            if (jsonStr != null && !"".equals(jsonStr)) {
                DynaBean bgsjDynaBean = jgmesCommon.getDynaBeanByJsonStr("JGMES_PB_BGSJ", jsonStr);
                if (bgsjDynaBean != null) {
                    bgsjDynaBean.set("BGSJ_GZSJ", jgmesCommon.getCurrentTime());
                    serviceTemplate.buildModelCreateInfo(bgsjDynaBean);
                    serviceTemplate.insert(bgsjDynaBean);
                }
                String zt = request.getParameter("zt");
                DynaBean jgmes_plan_scrw = serviceTemplate.selectOne("JGMES_PLAN_SCRW", "and SCRW_RWDH='" + bgsjDynaBean.getStr("BGSJ_SCRW") + "'");
                if (jgmes_plan_scrw != null) {
                    if (zt.equals("RWZT03")) {
                        jgmes_plan_scrw.set("SCRW_RWZT_CODE", zt);
                        jgmes_plan_scrw.set("SCRW_RWZT_NAME", "完成生产");
                    }
                }
                int wcsl = jgmes_plan_scrw.getInt("SCRW_WCSL");
                int sl = 0;
                if (StringUtil.isNotEmpty(bgsjDynaBean.getStr("BGSJ_SL"))) {
                    sl = Integer.parseInt(bgsjDynaBean.getStr("BGSJ_SL"));
                }
                jgmes_plan_scrw.set("SCRW_WCSL", wcsl + sl);
                serviceTemplate.update(jgmes_plan_scrw);
                ret.IsSuccess = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ret.setMessage("异常错误！");
        }
        toWrite(jsonBuilder.toJson(ret));
    }


    /**
     * 红外线定时任务
     *
     * @throws ParseException
     */
    public void doSaveBgsjTask() {
        DecimalFormat df = new DecimalFormat("#.00");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        List<DynaBean> scrwztDynaBeanList = serviceTemplate.selectList("JGMES_PB_SCRWZT", " and SCRWZT_SCZT_CODE = 'SCZT01'");
        if (scrwztDynaBeanList != null && scrwztDynaBeanList.size() > 0) {
            for (DynaBean scrwztDynaBean : scrwztDynaBeanList) {
                //获取实际开工时间
                float sl = 0;
                int kssl = 0;
                int jssl = 0;
                float mxsl = 0;
                List<DynaBean> cxhwxjsDynaBeanList = serviceTemplate.selectList("JGMES_SCGCGL_CXHWXJSB", " and CXHWXJSB_RWZTZJ = '" + scrwztDynaBean.getStr("JGMES_PB_SCRWZT_ID") + "' and CXHWXJSB_RWDH = '" + scrwztDynaBean.getStr("SCRWZT_RWDH") + "' order by CXHWXJSB_JSSJ");
                if (cxhwxjsDynaBeanList != null && cxhwxjsDynaBeanList.size() > 0) {
                    kssl = cxhwxjsDynaBeanList.get(0).getInt("CXHWXJSB_JSSL");
                    jssl = cxhwxjsDynaBeanList.get(cxhwxjsDynaBeanList.size() - 1).getInt("CXHWXJSB_JSSL");
                    sl = jssl - kssl;

                    //获取产品信息中的每箱数量
                    DynaBean cpDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " and PRODUCTDATA_BH = '" + scrwztDynaBean.getStr("SCRWZT_CPBM") + "'");
                    if (cpDynaBean != null) {
                        mxsl = cpDynaBean.getInt("PRODUCTDATA_MXSL");
                    }
                    if (mxsl == 0) {
                        mxsl = 1;
                    }

                    //获取生产任务单信息
                    DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and SCRW_RWDH = '" + scrwztDynaBean.getStr("SCRWZT_RWDH") + "'");

                    //获取工序信息  BGSJ_GXBH
                    DynaBean cxhwadDynaBean = serviceTemplate.selectOne("JGMES_SCGCGL_CXHWADAB", " and CXHWADAB_HWXBM = '" + cxhwxjsDynaBeanList.get(0).getStr("CXHWXJSB_HWXBM") + "' and CXHWADAB_USESTATUS_CODE = '0'");
                    if (cxhwadDynaBean != null) {
                        DynaBean bgsjDynaBean = serviceTemplate.selectOne("JGMES_PB_BGSJ", " and BGSJ_CXBM = '" + scrwztDynaBean.getStr("SCRWZT_CXBM") + "' and BGSJ_SCRW = '" + scrwztDynaBean.getStr("SCRWZT_RWDH") + "' and BGSJ_GXBH = '" + cxhwadDynaBean.getStr("CXHWADAB_GXBM") + "' and BGSJ_BGLX != '0'");
                        if (bgsjDynaBean != null) {//如过不为空，就修改报工数据
                            bgsjDynaBean.set("BGSJ_SL", df.format(sl / mxsl));
                            bgsjDynaBean.set("BGSJ_GZSJ", jgmesCommon.getCurrentTime());
                            serviceTemplate.update(bgsjDynaBean);
                            scrwDynaBean.set("SCRW_WCSL", 0);
                            serviceTemplate.update(scrwDynaBean);
                        } else {
                            DynaBean bgslDynaBean = setBgsl(cxhwadDynaBean, scrwztDynaBean, df.format(sl / mxsl));
                            serviceTemplate.insert(bgslDynaBean);
                            scrwDynaBean.set("SCRW_WCSL", 0);
                            serviceTemplate.update(scrwDynaBean);
                        }
                    }
                }
            }
            //回写生产任务单中的数量
            for (DynaBean scrwztDynaBean : scrwztDynaBeanList) {
                //获取实际开工时间
                int sl = 0;
                int kssl = 0;
                int jssl = 0;
                int mxsl = 0;
                List<DynaBean> cxhwxjsDynaBeanList = serviceTemplate.selectList("JGMES_SCGCGL_CXHWXJSB", " and CXHWXJSB_RWZTZJ = '" + scrwztDynaBean.getStr("JGMES_PB_SCRWZT_ID") + "' and CXHWXJSB_RWDH = '" + scrwztDynaBean.getStr("SCRWZT_RWDH") + "' order by CXHWXJSB_JSSJ");
                if (cxhwxjsDynaBeanList != null && cxhwxjsDynaBeanList.size() > 0) {
                    kssl = cxhwxjsDynaBeanList.get(0).getInt("CXHWXJSB_JSSL");
                    jssl = cxhwxjsDynaBeanList.get(cxhwxjsDynaBeanList.size() - 1).getInt("CXHWXJSB_JSSL");
                    sl = jssl - kssl;

                    //获取产品信息中的每箱数量
                    DynaBean cpDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " and PRODUCTDATA_BH = '" + scrwztDynaBean.getStr("SCRWZT_CPBM") + "'");
                    if (cpDynaBean != null) {
                        mxsl = cpDynaBean.getInt("PRODUCTDATA_MXSL");
                    }
                    if (mxsl == 0) {
                        mxsl = 1;
                    }
                    //获取生产任务单信息
                    DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and SCRW_RWDH = '" + scrwztDynaBean.getStr("SCRWZT_RWDH") + "'");
                    scrwDynaBean.set("SCRW_WCSL", scrwDynaBean.getInt("SCRW_WCSL") + sl / mxsl);
                    serviceTemplate.update(scrwDynaBean);
                }
            }
        }
    }


    /**
     * 乐惠定时任务
     *
     * @throws ParseException
     */
    public void doSaveLhBgsjTask() {
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        List<DynaBean> scrwztDynaBeanList = serviceTemplate.selectList("JGMES_PB_SCRWZT", " and SCRWZT_SCZT_CODE = 'SCZT01'");
        if (scrwztDynaBeanList != null && scrwztDynaBeanList.size() > 0) {
            for (DynaBean scrwztDynaBean : scrwztDynaBeanList) {
                //获取实际开工时间
                int sl = 0;
                int kscs = 0;
                int jscs = 0;

                //获取生产线机台关联表
                DynaBean cxjtglDynaBean = serviceTemplate.selectOne("JGMES_SCGCGL_CXJTGL", " and CXJTGL_CXBM = '" + scrwztDynaBean.getStr("SCRWZT_CXBM") + "'");
//				DynaBean cxjtglDynaBean = serviceTemplate.selectOne("JGMES_ADMK_JTDA","and JTDA_SCXBM='"+scrwztDynaBean.getStr("SCRWZT_CXBM")+"'");
                //获取冲压计数表
                List<DynaBean> cyjsDynaBeanList = serviceTemplate.selectList("JGMES_SCGCGL_CYJSB", " and CYJSB_CXBM = '" + scrwztDynaBean.getStr("SCRWZT_CXBM") + "' and CYJSB_RWDH = '" + scrwztDynaBean.getStr("SCRWZT_RWDH") + "' and CYJSB_NO_CODE = '0' order by CYJSB_JLSJ");
                if (cyjsDynaBeanList != null && cyjsDynaBeanList.size() > 0) {
                    kscs = cyjsDynaBeanList.get(0).getInt("CYJSB_ZCCYCS");
                    jscs = cyjsDynaBeanList.get(cyjsDynaBeanList.size() - 1).getInt("CYJSB_ZCCYCS");
                    sl = jscs - kscs;
                    //获取生产任务单信息
                    try {
                        DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and SCRW_RWDH = '" + scrwztDynaBean.getStr("SCRWZT_RWDH") + "'");
                        if (scrwDynaBean != null) {

                            scrwDynaBean.set("SCRW_WCSL", scrwDynaBean.getInt("SCRW_WCSL") + sl);
                            serviceTemplate.update(scrwDynaBean);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    DynaBean bgslDynaBean = setBgsl(cxjtglDynaBean, scrwztDynaBean, Integer.toString(sl));
                    if (bgslDynaBean != null) {
                        serviceTemplate.insert(bgslDynaBean);
                    }
                    //回写冲压计数表中的数据
                    DynaBean dic1 = jgmesCommon.getDic("JGMES_YES_NO", "1");
                    if (dic1 != null) {
                        //是否报工
                        for (int i = 0; i < cyjsDynaBeanList.size(); i++) {
                            if (i != cyjsDynaBeanList.size() - 1) {
                                DynaBean cyjsDynaBean = cyjsDynaBeanList.get(i);
                                cyjsDynaBean.set("CYJSB_NO_NAME", dic1.get("DICTIONARYITEM_ITEMNAME"));
                                cyjsDynaBean.set("CYJSB_NO_CODE", dic1.get("DICTIONARYITEM_ITEMCODE"));
                                serviceTemplate.update(cyjsDynaBean);
                            }
                        }
                    }
                }
            }

			/*
			//回写生产任务单中的数量
			for(DynaBean scrwztDynaBean:scrwztDynaBeanList) {
				//获取实际开工时间
				int sl = 0;
				int kssl = 0;
				int jssl = 0;
				//获取冲压计数表
				List<DynaBean> cyjsDynaBeanList = serviceTemplate.selectList("JGMES_SCGCGL_CYJSB", " and CYJSB_CXBM = '"+scrwztDynaBean.getStr("SCRWZT_CXBM")+"' and CYJSB_RWDH = '"+scrwztDynaBean.getStr("SCRWZT_RWDH")+"' and CYJSB_NO_CODE = '0' order by CYJSB_JLSJ");
				if(cyjsDynaBeanList!=null&&cyjsDynaBeanList.size()>0) {
					kssl = cyjsDynaBeanList.get(0).getInt("CYJSB_ZCCYCS");
					jssl = cyjsDynaBeanList.get(cyjsDynaBeanList.size()-1).getInt("CYJSB_ZCCYCS");
					sl = jssl - kssl;


				}
			}
			*/
        }
    }


//	/**
//	 * 乐惠定时任务
//	 * @throws ParseException
//	 */
//	public void doSaveLhBgsjTask(){
//		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
//
//		String sql = "SELECT MAX(CYJSB_CYCS)-MIN(CYJSB_CYCS) as SL,CYJSB_CXBM,CYJSB_RWDH from JGMES_SCGCGL_CYJSB where CYJSB_NO_CODE = 0  GROUP BY CYJSB_CXBM,CYJSB_RWDH";
//		List<DynaBean> jsslDynaBeanList = serviceTemplate.selectListBySql(sql);
//		if(jsslDynaBeanList!=null&&jsslDynaBeanList.size()>0){
//			for(DynaBean jsslDynaBean:jsslDynaBeanList){
//				int sl = jsslDynaBean.getInt("SL");
//				List<DynaBean> scrwztDynaBeanList = serviceTemplate.selectList("JGMES_PB_SCRWZT", " and SCRWZT_CXBM = '"+jsslDynaBean.getStr("CYJSB_CXBM")+"' and SCRWZT_RWDH = '"+jsslDynaBean.getStr("CYJSB_RWDH")+"' and  SCRWZT_SCZT_CODE in ('SCZT01','SCZT03')");
//				if(scrwztDynaBeanList!=null&&scrwztDynaBeanList.size()>0){
//					//获取生产线机台关联表
//					DynaBean cxjtglDynaBean = serviceTemplate.selectOne("JGMES_SCGCGL_CXJTGL"," and CXJTGL_CXBM = '"+scrwztDynaBeanList.get(0).getStr("SCRWZT_CXBM")+"'");
//
//					//获取生产任务单信息
//					DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and SCRW_RWDH = '"+scrwztDynaBeanList.get(0).getStr("SCRWZT_RWDH")+"'");
//					if(scrwDynaBean!=null){
//						scrwDynaBean.set("SCRW_WCSL", scrwDynaBean.getInt("SCRW_WCSL")+sl);
//						serviceTemplate.update(scrwDynaBean);
//					}
//
//					DynaBean bgslDynaBean= setBgsl(cxjtglDynaBean,scrwztDynaBeanList.get(0),Integer.toString(sl));
//					serviceTemplate.insert(bgslDynaBean);
//
//					//获取冲压计数表
//					List<DynaBean> cyjsDynaBeanList = serviceTemplate.selectList("JGMES_SCGCGL_CYJSB", " and CYJSB_CXBM = '"+jsslDynaBean.getStr("CYJSB_CXBM")+"' and CYJSB_RWDH = '"+jsslDynaBean.getStr("CYJSB_RWDH")+"' and CYJSB_NO_CODE = '0' order by CYJSB_JLSJ");
//
//
//					//回写冲压计数表中的数据
//					DynaBean dic1 = jgmesCommon.getDic("JGMES_YES_NO","1");
//					if(dic1!=null) {
//						//是否报工
//						for (int i = 0; i < cyjsDynaBeanList.size(); i++) {
//							if(i!=cyjsDynaBeanList.size()-1){
//								DynaBean cyjsDynaBean = cyjsDynaBeanList.get(i);
//								cyjsDynaBean.set("CYJSB_NO_NAME",dic1.get("DICTIONARYITEM_ITEMNAME"));
//								cyjsDynaBean.set("CYJSB_NO_CODE",dic1.get("DICTIONARYITEM_ITEMCODE"));
//								serviceTemplate.update(cyjsDynaBean);
//							}
//						}
//					}
//				}
//			}
//		}
//	}


    public DynaBean setBgsl(DynaBean cxhwadDynaBean, DynaBean scrwztDynaBean, String sl) {
        if (cxhwadDynaBean == null || scrwztDynaBean == null) {
            return null;
        }
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        DynaBean bgsj = new DynaBean();
        bgsj.setStr(beanUtils.KEY_TABLE_CODE, "JGMES_PB_BGSJ");
        bgsj.set("JGMES_PB_BGSJ_ID", JEUUID.uuid());
        bgsj.set("BGSJ_GZSJ", jgmesCommon.getCurrentTime());
        if (scrwztDynaBean != null) {
            DynaBean cxDynaBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ", " and CXSJ_CXBM = '" + scrwztDynaBean.getStr("SCRWZT_CXBM") + "'");
            if (cxDynaBean != null) {
                bgsj.set("BGSJ_CXMC", cxDynaBean.getStr("CXSJ_CXMC"));  //产线名称
            }
        }
        bgsj.set("BGSJ_CXBM", scrwztDynaBean.getStr("SCRWZT_CXBM"));  //产线编码
//		if(cxhwadDynaBean.getStr("CXJTGL_GXBM")!=null&&!"".equals(cxhwadDynaBean.getStr("CXJTGL_GXBM"))){
//			DynaBean gxDynaBean = serviceTemplate.selectOne("JGMES_GYGL_GXGL"," and GXGL_GXNUM = '"+cxhwadDynaBean.getStr("CXJTGL_GXBM")+"'");
//			if(gxDynaBean!=null){
//				//工序编号
//				bgsj.set("BGSJ_GXBH", gxDynaBean.getStr("GXGL_GXNUM"));
//				//工序名称
//				bgsj.set("BGSJ_GXMC", gxDynaBean.getStr("GXGL_GXNAME"));
//			}
//		}else{
//			bgsj.set("BGSJ_GXBH", cxhwadDynaBean.getStr("CXHWADAB_GXBM"));  //工序编号
//			bgsj.set("BGSJ_GXMC", cxhwadDynaBean.getStr("CXHWADAB_GXMC"));  //工序名称
//		}

        bgsj.set("BGSJ_CPBH", scrwztDynaBean.getStr("SCRWZT_CPBM"));  //产品编号
        bgsj.set("BGSJ_CPMC", scrwztDynaBean.getStr("SCRWZT_CPMC"));  //产品名称
        bgsj.set("BGSJ_CPGG", scrwztDynaBean.getStr("SCRWZT_CPGG"));  //产品规格
        //bgsj.set("BGSJ_TMH", value);  //条码号

//		bgsj.set("BGSJ_GWBH", value);  //工位编号
//		bgsj.set("BGSJ_GWMC", value);  //工位名称
        bgsj.set("BGSJ_SL", sl);  //数量
        //bgsj.set("BGSJ_BZ", value);  //备注

        //设置状态（默认开启）
        DynaBean dic = jgmesCommon.getDic("JGMES_STATUS", "1");
        if (dic != null) {
            bgsj.set("BGSJ_STATUS_NAME", dic.getStr("DICTIONARYITEM_ITEMNAME"));
            bgsj.set("BGSJ_STATUS_CODE", dic.getStr("DICTIONARYITEM_ITEMCODE"));
            bgsj.set("BGSJ_STATUS_ID", dic.getStr("JE_CORE_DICTIONARYITEM_ID"));
        }
        bgsj.set("BGSJ_SCRW", scrwztDynaBean.getStr("SCRWZT_RWDH"));  //生产任务单

        return bgsj;
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
     * 保存排产单状态
     */
    public void doSaveScrwZt(String userCode, String scrwId, String scrwZt, String ztyyZ) {
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<String> ret = new JgmesResult<String>();
        try {
            System.out.println("生产任务单ID：" + scrwId);
            DynaBean scrwDynaBean = serviceTemplate.selectOneByPk("JGMES_PLAN_SCRW", scrwId);
            if (scrwDynaBean != null) {
                if (JgmesEnumsDic.ScDoing.getKey().equals(scrwZt)) {
                    if (!JgmesEnumsDic.ScPause.getKey().equals(scrwDynaBean.getStr("SCRW_RWZT_CODE"))) {
                        scrwDynaBean.set("SCRW_SJKGSJ", jgmesCommon.getCurrentTime());

                        DynaBean gdDynaBean = serviceTemplate.selectOne("JGMES_PLAN_GDLB", " and JGMES_PLAN_GDLB_ID = '" + scrwDynaBean.getStr("JGMES_PLAN_GDLB_ID") + "'");///
                        if (gdDynaBean != null) {
                            if (gdDynaBean.getStr("GDLB_SJKGSJ") == null || "".equals(gdDynaBean.getStr("GDLB_SJKGSJ"))) {
                                gdDynaBean.set("GDLB_SJKGSJ", jgmesCommon.getCurrentDate());
                                serviceTemplate.update(gdDynaBean);
                            }
                        }
                    }
                }
                jgmesCommon.setDynaBeanDicScrwZt(scrwDynaBean, scrwZt, "SCRW_RWZT_ID", "SCRW_RWZT_CODE",
                        "SCRW_RWZT_NAME");
                if (JgmesEnumsDic.ScFinished.getKey().equals(scrwZt)) {
                    scrwDynaBean.set("SCRW_SJWGSJ", jgmesCommon.getCurrentTime());
                }
                serviceTemplate.update(scrwDynaBean);

                //判断工单下面的是否都完成
                if (JgmesEnumsDic.ScFinished.getKey().equals(scrwZt)) {
                    List<DynaBean> scrwDynaBeanList = serviceTemplate.selectList("JGMES_PLAN_SCRW", " and SCRW_GDHM = '" + scrwDynaBean.getStr("SCRW_GDHM") + "'");
                    boolean isComplete = true;
                    int wcsl = 0;
                    if (scrwDynaBeanList != null && scrwDynaBeanList.size() > 0) {
                        for (DynaBean scrwDynaBean1 : scrwDynaBeanList) {
                            if ("RWZT01".equals(scrwDynaBean1.getStr("SCRW_RWZT_CODE")) ||
                                    "RWZT02".equals(scrwDynaBean1.getStr("SCRW_RWZT_CODE")) ||
                                    "RWZT04".equals(scrwDynaBean1.getStr("SCRW_RWZT_CODE"))) {
                                isComplete = false;
                            }
                            wcsl += scrwDynaBean1.getInt("SCRW_WCSL");
                        }
                        DynaBean gdDynaBean = serviceTemplate.selectOne("JGMES_PLAN_GDLB", " and JGMES_PLAN_GDLB_ID = '" + scrwDynaBean.getStr("JGMES_PLAN_GDLB_ID") + "'");///
                        //DynaBean padDynaBean = serviceTemplate.selectOne("JGMES_PLAN_PCLB", " and PCLB_DDHM = '"+gdDynaBean.getStr("GDLB_DDHM")+"' and JGMES_PLAN_GDLB_ID = '"+gdDynaBean.getStr("JGMES_PLAN_GDLB_ID")+"'");
                        DynaBean dic = jgmesCommon.getDic("JGMES_DIC_GDZT", "2");
                        if (isComplete && gdDynaBean != null && wcsl >= gdDynaBean.getInt("GDLB_GDSL")) {
                            if (dic != null && gdDynaBean != null) {
                                gdDynaBean.set("GDLB_GDZT_NAME", dic.getStr("DICTIONARYITEM_ITEMNAME"));
                                gdDynaBean.set("GDLB_GDZT_CODE", dic.getStr("DICTIONARYITEM_ITEMCODE"));
                                gdDynaBean.set("GDLB_SJWGSJ", jgmesCommon.getCurrentDate());
                            }
                            serviceTemplate.update(gdDynaBean);
                            //修改任务单中的工单状态
                            for (DynaBean scrwDynaBean1 : scrwDynaBeanList) {
                                if (dic != null) {
                                    scrwDynaBean1.set("SCRW_GDZT_NAME", dic.getStr("DICTIONARYITEM_ITEMNAME"));
                                    scrwDynaBean1.set("SCRW_GDZT_CODE", dic.getStr("DICTIONARYITEM_ITEMCODE"));
                                    serviceTemplate.update(scrwDynaBean1);
                                }
                            }
                        }
                    }
                }
				/*
				if (scrwZt.equalsIgnoreCase(JgmesEnumsDic.ScFinished.getKey()) || scrwZt.equalsIgnoreCase(JgmesEnumsDic.ScPause.getKey())) {//完成时删除产线指定的任务
					List<DynaBean> cxList = serviceTemplate.selectList("JGMES_BASE_CXSJ", " and JGMES_PLAN_SCRW_ID='"+scrwId+"'");
					DynaBean cxDynaBean =new DynaBean();
					if (cxList != null && cxList.size()>0) {
						//
						cxDynaBean = cxList.get(0);
						cxDynaBean.set("JGMES_PLAN_SCRW_ID", "");
						serviceTemplate.update(cxDynaBean);
					}
				}*/


            }
        } catch (Exception e) {
            logger.error(e.toString());
            // TODO Auto-generated catch block
//			e.printStackTrace();
            ret.setMessage(e.toString());
            toWrite("保存状态出错：" + jsonBuilder.toJson(ret));
        }
    }


//	public JgmesInfraredService getJgmesInfraredService() {
//		return jgmesInfraredService;
//	}
//
//	@Resource(name = "jgmesInfraredService")
//	public void setJgmesInfraredService(JgmesInfraredService jgmesInfraredService) {
//		this.jgmesInfraredService = jgmesInfraredService;
//	}

}