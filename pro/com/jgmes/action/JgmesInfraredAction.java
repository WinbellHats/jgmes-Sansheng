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
//		String realPath = req.getRealPath("/JE/data/upload/") + "/";// ȡϵͳ��ǰ·��
//		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
//		jgmesCommon.fileUplod(realPath);
//		String Password = req.getParameter("Password");
//		toWrite("nihao!");
    }

    //��ȡ��ǰ�������������б�
    public void getCxScrw() {
        String mac = request.getParameter("mac");// MAC��ַ
        String userCode = request.getParameter("userCode");// �û�����  ����
        String prodLineCode = request.getParameter("prodLineCode");// ���߱���
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        String d = request.getParameter("date");//����
        String jsonStr = "";
        // У��Ϸ��ԣ��Ƿ��ص�½
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
//					ret.setMessage("δ��ȡ�����߱��룡");
//				}

            } catch (Exception e) {
                logger.error(e.toString());
                e.printStackTrace();
                ret.setMessage("ϵͳ�����쳣������ϵ����Ա��");
            }
        } else {
            ret.setMessage("�û���֤�Ϸ���ʧ�ܣ�");
        }
        jsonStr = jsonBuilder.toJson(ret);
        toWrite(jsonStr);
    }


    //��������ҳ��----��ȡ��ǰ�������������б�,author:ljs
    public void getBatchCxScrw() {
        String mac = request.getParameter("mac");// MAC��ַ
        String userCode = request.getParameter("userCode");// �û�����  ����
        String prodLineCode = request.getParameter("prodLineCode");// ���߱���
        String noLike = request.getParameter("noLike");//������Ϣ
        String cpLike = request.getParameter("cpLike");//��Ʒ��Ϣ
        String zt = request.getParameter("zt");
        String teamOfTime = request.getParameter("teamOfTime");//���
        String bz = request.getParameter("BzData");//����
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        String d = request.getParameter("date");//����
        String jsonStr = "";

        // У��Ϸ��ԣ��Ƿ��ص�½
        if (doCheck(userCode, mac).IsSuccess) {
            List<DynaBean> scrwZtDynaBeanListforreturn = new ArrayList<DynaBean>();
            List<DynaBean> scrwZtDynaBeanList = new ArrayList<>();
//			String whereSql = "and SCRW_RWZT_CODE != 'RWZT03'";
            try {
                //����
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
                //�ƻ�����ʱ��
                if (StringUtil.isNotEmpty(d)) {
                    d = d.replaceAll("/", "-");
                    whereSql += "and SCRW_PCRQ='" + d + "'";
                }
                //������Ϣ
                if (StringUtil.isNotEmpty(noLike)) {
                    whereSql += " and (SCRW_GDHM like '%" + noLike + "%' or SCRW_RWDH like '%" + noLike + "%' or SCRW_DDHM like '%" + noLike + "%' or SCRW_GLDH like '%" + noLike + "%') ";
                }
                //��Ʒ��Ϣ
                if (StringUtil.isNotEmpty(cpLike)) {
                    whereSql += " and (SCRW_CPBH like '%" + cpLike + "%' or SCRW_NAME like '%" + cpLike + "%' or SCRW_CPGG like '%" + cpLike + "%') ";
                }
                //���
                if (StringUtil.isNotEmpty(teamOfTime)) {
                    if (teamOfTime.indexOf(",") != -1) {
                        whereSql += " and SCRW_BB in ('" + teamOfTime.replaceAll(",", "','") + "')";
                    } else {
                        whereSql += " and SCRW_BB like '%" + teamOfTime + "%'";
                    }
                }
                //����
                if (StringUtil.isNotEmpty(bz)) {
                    if (bz.indexOf(",") != -1) {
                        whereSql += " and SCRW_BZNAME in ('" + bz.replaceAll(",", "','") + "')";
                    } else {
                        whereSql += " and SCRW_BZNAME like '%" + bz + "%'";
                    }
                }
                //״̬
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
                ret.setMessage("ϵͳ�����쳣������ϵ����Ա��");
            }
        } else {
            ret.setMessage("�û���֤�Ϸ���ʧ�ܣ�");
        }
        jsonStr = jsonBuilder.toJson(ret);
        toWrite(jsonStr);
    }


    //�ļӻ����޸���������״̬��
    public void updateScrwZtB() {
        String mac = request.getParameter("mac");// MAC��ַ
        String userCode = request.getParameter("userCode");// �û�����  ����
        String prodLineCode = request.getParameter("prodLineCode");// ���߱���
        String taskCode = request.getParameter("taskCode");// �������񵥺�
        String startSection = request.getParameter("startSection");// ���߶�
        String taskStatus = request.getParameter("taskStatus");// ����״̬
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        String scrwId = request.getParameter("scrwId");// ����id
        String jsonStr = "";
        // У��Ϸ��ԣ��Ƿ��ص�½
        if (doCheck(userCode, mac).IsSuccess) {
            try {
                String startSectionName = "";
                //���߱���
                if ((prodLineCode == null || prodLineCode.isEmpty())) {
                    ret.setMessage("���߱��벻�ܶ�Ϊ�գ�");
                }
                //�������񵥺�
                if (taskCode == null || taskCode.isEmpty()) {
                    ret.setMessage("�������񵥺Ų��ܶ�Ϊ�գ�");
                }
                //���߶�
                if (startSection == null || startSection.isEmpty()) {
                    ret.setMessage("�����β��ܶ�Ϊ�գ�");
                } else {
                    if (JgmesEnumsDic.CXDQUD.getKey().equals(startSection)) {
                        startSectionName = JgmesEnumsDic.CXDQUD.getValue();
                    } else if (JgmesEnumsDic.CXDQD.getKey().equals(startSection)) {
                        startSectionName = JgmesEnumsDic.CXDQD.getValue();
                    } else if (JgmesEnumsDic.CXDHD.getKey().equals(startSection)) {
                        startSectionName = JgmesEnumsDic.CXDHD.getValue();
                    }
                }
                //����״̬
                if ((taskStatus == null || taskStatus.isEmpty())) {
                    ret.setMessage("����״̬���ܶ�Ϊ�գ�");
                }
                if ((prodLineCode != null && !prodLineCode.isEmpty()) &&
                        (taskCode != null && !taskCode.isEmpty()) &&
                        (startSection != null && !startSection.isEmpty()) &&
                        (taskStatus != null && !taskStatus.isEmpty())) {

//					//�����û�н����׼�����
//					String message=jgmesCommon.checkSJ(prodLineCode,taskCode);
//					if(message!=null&&!"".equals(message)){
//						ret.setMessage(message);
//						toWrite(jsonBuilder.toJson(ret));
//					}

                    //���ǿ����ģ��Ѹò��������������е����񵥶���ͣ
                    if (taskStatus.equals("RWZT02")) {
                        //��ȡ�ò�������״̬���������е����񵥣��ĳ���ͣ
                        List<DynaBean> jgmes_pb_scrwzt = serviceTemplate.selectList("JGMES_PB_SCRWZT", "and SCRWZT_SCZT_CODE='SCZT01' and SCRWZT_CXBM='" + prodLineCode + "'");
                        if (jgmes_pb_scrwzt.size() > 0) {
                            for (DynaBean bean : jgmes_pb_scrwzt) {
                                bean.set("SCRWZT_SCZT_CODE", "SCZT02");
                                bean.set("SCRWZT_SCZT_NAME", "��ͣ");
                                serviceTemplate.update(bean);
                                DynaBean scrwBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", "and SCRW_RWDH='" + bean.getStr("SCRWZT_RWDH") + "'");
                                scrwBean.set("SCRW_RWZT_CODE", "RWZT04");
                                scrwBean.set("SCRW_RWZT_NAME", "��ͣ");
                            }
                        }
                        //��ȡ�ò�������������������е����񵥣��ĳ���ͣ
                        List<DynaBean> jgmes_plan_scrw = serviceTemplate.selectList("JGMES_PLAN_SCRW", "and  SCRW_CXBM='" + prodLineCode + "' and SCRW_RWZT_CODE='RWZT02'");
                        if (jgmes_plan_scrw.size() > 0) {
                            for (DynaBean bean : jgmes_plan_scrw) {
                                bean.set("SCRW_RWZT_CODE", "RWZT04");
                                bean.set("SCRW_RWZT_NAME", "��ͣ");
                                serviceTemplate.update(bean);
                                List<DynaBean> dynaBeans = serviceTemplate.selectList("JGMES_PB_SCRWZT", "and SCRWZT_SCZT_CODE='SCZT01' and SCRWZT_RWDH='" + bean.getStr("SCRW_RWDH") + "'");
                                if (dynaBeans.size() > 0) {
                                    for (DynaBean dynaBean1 : dynaBeans) {
                                        dynaBean1.set("SCRWZT_SCZT_CODE", "SCZT02");
                                        dynaBean1.set("SCRWZT_SCZT_NAME", "��ͣ");
                                    }
                                }
                            }
                        }
                    }
                    JgmesEnumsDic startSectionDic = jgmesCommon.getJgmesEnumsDic(startSection);
                    DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and SCRW_RWDH  = '" + taskCode + "'");
                    //��ѯһ����������״̬��
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
                                        ret.setMessage("���ߣ�" + prodLineCode + "�в��߶�Ϊ" + startSectionName + "�����������Ѿ����ڣ�");
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
                                    ret.setMessage("��������ǰ���Ѿ������ˣ�");
                                    toWrite(jsonBuilder.toJson(ret));
                                    return;
                                }
                                if (isHD && ("CXD03".equals(startSection) || "CXD01".equals(startSection))) {
                                    ret.setMessage("�������ߺ���Ѿ������ˣ�");
                                    toWrite(jsonBuilder.toJson(ret));
                                    return;
                                }
                                if (isQUD && ("CXD03".equals(startSection) || "CXD02".equals(startSection))) {
                                    ret.setMessage("���������Ѿ���ȫ�ο���������,���Բ���ǰ�ο����ͺ�ο�����");
                                    toWrite(jsonBuilder.toJson(ret));
                                    return;
                                }
                                if (isQUD && ("CXD01".equals(startSection))) {
                                    ret.setMessage("��������ȫ�ο���ֻ����һ��!");
                                    toWrite(jsonBuilder.toJson(ret));
                                    return;
                                }

                            }
                            DynaBean scrwZtDynaBean = new DynaBean();
                            scrwZtDynaBean.setStr(beanUtils.KEY_TABLE_CODE, "JGMES_PB_SCRWZT");
                            scrwZtDynaBean.set("JGMES_PB_SCRWZT_ID", JEUUID.uuid());
                            scrwZtDynaBean.set("SCRWZT_CXBM", prodLineCode);//���߱���

                            DynaBean cxdDic = jgmesCommon.getDic("JGMES_DIC_CXD", startSection);
                            if (cxdDic != null) {
                                scrwZtDynaBean.set("SCRWZT_CXD", cxdDic.getStr("DICTIONARYITEM_ITEMCODE"));//���߶�
                                scrwZtDynaBean.set("SCRWZT_CXD_NAME", cxdDic.getStr("DICTIONARYITEM_ITEMNAME"));//��������
                            }
                            scrwZtDynaBean.set("SCRWZT_RWDH", taskCode);//���񵥺�
                            scrwZtDynaBean.set("SCRWZT_CPBM", scrwDynaBean.getStr("SCRW_CPBH"));//��Ʒ����
                            scrwZtDynaBean.set("SCRWZT_CPMC", scrwDynaBean.getStr("SCRW_NAME"));//��Ʒ����
                            scrwZtDynaBean.set("SCRWZT_CPGG", scrwDynaBean.getStr("SCRW_CPGG"));//��Ʒ���
                            scrwZtDynaBean.set("SCRWZT_KGSJ", jgmesCommon.getCurrentTime());//����ʱ��
                            scrwZtDynaBean.set("JGMES_PLAN_SCRW_ID", scrwId);//��д��������id

                            //���ö�������
                            scrwZtDynaBean.set("SCRWZT_DDHM", scrwDynaBean.getStr("SCRW_DDHM"));
                            DynaBean dic1 = jgmesCommon.getDic("JGMES_DIC_SCZT", "SCZT01");
                            if (dic1 != null) {
                                scrwZtDynaBean.set("SCRWZT_SCZT_CODE", dic1.get("DICTIONARYITEM_ITEMCODE"));//����״̬
                            }


                            List<DynaBean> scrwztDynaBean = serviceTemplate.selectList("JGMES_PB_SCRWZT", " and SCRWZT_RWDH = '" + taskCode + "' and SCRWZT_CXD = '" + startSection + "' and SCRWZT_CXBM='" + prodLineCode + "' and SCRWZT_SCZT_CODE = 'SCZT02'");
                            if (scrwztDynaBean == null || scrwztDynaBean.size() == 0) {
                                //������ʱ���������һ����ʼֵ
                                //��ȡ�����߻�̨������
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

                                //��д��ѹ�������е�����
                                DynaBean d = jgmesCommon.getDic("JGMES_YES_NO", "0");
                                if (dic1 != null) {
                                    cyjsDynaBean.set("CYJSB_NO_NAME", d.get("DICTIONARYITEM_ITEMNAME"));
                                    cyjsDynaBean.set("CYJSB_NO_CODE", d.get("DICTIONARYITEM_ITEMCODE"));
                                }
                                serviceTemplate.insert(cyjsDynaBean);
                            }

                            //��������
                            serviceTemplate.insert(scrwZtDynaBean);

                            //�޸���������ĵ�����״̬  1���޸ĸ��������������״̬Ϊ������
                            doSaveScrwZt(userCode, scrwDynaBean.getStr("JGMES_PLAN_SCRW_ID"), JgmesEnumsDic.ScDoing.getKey(), "");
                            //�Ѹò����е�������������״̬Ϊ�����еĸĳ�
                            //���ò���������������������ͣ��
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
                                    //������깤��
                                    if (JgmesEnumsDic.ScFinished.getKey().equals(taskStatus) && !"SCZT03".equals(scrwztDynaBean.getStr("SCRWZT_SCZT_CODE"))) {
                                        DynaBean dic1 = jgmesCommon.getDic("JGMES_DIC_SCZT", "SCZT03");
                                        if (dic1 != null) {
                                            scrwztDynaBean.set("SCRWZT_SCZT_CODE", dic1.get("DICTIONARYITEM_ITEMCODE"));//����״̬
                                        }
                                        scrwztDynaBean.set("SCRWZT_WGSJ", jgmesCommon.getCurrentTime());
                                    } else if (JgmesEnumsDic.ScPause.getKey().equals(taskStatus) && !"SCZT02".equals(scrwztDynaBean.getStr("SCRWZT_SCZT_CODE"))) {//�����
                                        DynaBean dic1 = jgmesCommon.getDic("JGMES_DIC_SCZT", "SCZT02");
                                        if (dic1 != null) {
                                            scrwztDynaBean.set("SCRWZT_SCZT_CODE", dic1.get("DICTIONARYITEM_ITEMCODE"));//����״̬
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
                                } else if (JgmesEnumsDic.ScPause.getKey().equals(taskStatus)) {//�����
                                    doSaveScrwZt(userCode, scrwDynaBean.getStr("JGMES_PLAN_SCRW_ID"), JgmesEnumsDic.ScPause.getKey(), "");
                                }
                            }
                        }
                    }
//                    //��д���������е�ģ����Ϣ
//                    String mjbh = "";//ģ�߱��
//                    if (scrwDynaBean != null) {
//                        mjbh = scrwDynaBean.getStr("SCRW_MJBH");
//                        DynaBean jgmes_base_mj = serviceTemplate.selectOne("JGMES_BASE_MJ", "and MJ_BH='" + mjbh + "'");
//                        String mjmc = "";//ģ������
//                        if (jgmes_base_mj != null) {
//                            mjmc = jgmes_base_mj.getStr("MJ_MC");//ģ������
//                        }
//                        DynaBean cxsjBean = serviceTemplate.selectOne("JGMES_BASE_CXSJ", "and CXSJ_CXBM = '" + prodLineCode + "'");
//                        if (cxsjBean != null) {
//                            cxsjBean.setStr("CXSJ_MJBH", mjbh);
//                            cxsjBean.setStr("CXSJ_MJMC", mjmc);
//                            serviceTemplate.update(cxsjBean);
//                        }
//                    }
//                    //��ͣ����ʱ���ѵ�ǰ�����������������д����������-��ͣʱ�������
//                    if (taskStatus.equals("RWZT04")) {
//                        DynaBean jgmes_plan_scrw = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and JGMES_PLAN_SCRW_ID='" + scrwId + "'");
//                        if (jgmes_plan_scrw != null) {
//                            String SCRW_WCSL = jgmes_plan_scrw.getStr("SCRW_WCSL");//��ǰ�������
//                            jgmes_plan_scrw.setStr("SCRW_ZTSWCS", SCRW_WCSL);
//                            serviceTemplate.update(jgmes_plan_scrw);
//                        }
//                    }
                }
            } catch (Exception e) {
                logger.error(e.toString());
                e.printStackTrace();
                ret.setMessage("ϵͳ�����쳣������ϵ����Ա��");
            }
            toWrite(jsonBuilder.toJson(ret));
        }
    }

    /**
     * �鿴��ǰ�����Ƿ���������е����񵥺�
     * ���£�
     */
    public void checkCXZT() {
        String rwdh = request.getParameter("rwdh");// ���񵥺�
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
                        bean.set("SCRWZT_SCZT_CODE", "SCZT02");//�޸ĳ���ͣ
                        serviceTemplate.update(bean);
                    }
                    DynaBean scrwZtDynaBean = new DynaBean();
                    scrwZtDynaBean.setStr(beanUtils.KEY_TABLE_CODE, "JGMES_PB_SCRWZT");
                    scrwZtDynaBean.set("JGMES_PB_SCRWZT_ID", JEUUID.uuid());
                    scrwZtDynaBean.set("SCRWZT_CXBM", cxbm);//���߱���
                    scrwZtDynaBean.set("SCRWZT_CXD", "CXD01");//���߶�
                    scrwZtDynaBean.set("SCRWZT_CXD_NAME", cxmc);//��������
                    scrwZtDynaBean.set("SCRWZT_RWDH", jgmes_plan_scrw1.get(0).getStr("SCRW_RWDH"));//���񵥺�
                    scrwZtDynaBean.set("SCRWZT_CPBM", jgmes_plan_scrw.get(0).getStr("SCRW_CPBH"));//��Ʒ����
                    scrwZtDynaBean.set("SCRWZT_CPMC", jgmes_plan_scrw.get(0).getStr("SCRW_NAME"));//��Ʒ����
                    scrwZtDynaBean.set("SCRWZT_CPGG", jgmes_plan_scrw.get(0).getStr("SCRW_CPGG"));//��Ʒ���
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//�������ڸ�ʽ
                    scrwZtDynaBean.set("SCRWZT_KGSJ", df.format(new Date()));//����ʱ��
                    scrwZtDynaBean.set("JGMES_PLAN_SCRW_ID", jgmes_plan_scrw.get(0).getStr("JGMES_PLAN_SCRW_ID"));//��д��������id
                    scrwZtDynaBean.set("SCRWZT_SCZT_CODE", "SCZT01");//����״̬
                    serviceTemplate.insert(scrwZtDynaBean);
                    ret.setMessage("�ò����Ѵ��������е�����");
                }
            } else {
                ret.setMessage("����������û�в��߱�����Ϣ");
            }
        } else {
            ret.setMessage("�����񵥺�û�ж�Ӧ������������Ϣ��");
        }
        toWrite(jsonBuilder.toJson(ret));
    }

    /**
     * ��ȡ��̨��
     */
    public void getJTH() {
        String sb = request.getParameter("sb");// ����״̬
        String mac = request.getParameter("mac");// MAC��ַ
        String userCode = request.getParameter("userCode");// �û�����  ����
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
        List<DynaBean> jgmes_admk_jtda = serviceTemplate.selectList("JGMES_ADMK_JTDA", "and JTDA_SCXBM='" + sb + "'");
        HashMap map = new HashMap();
        if (jgmes_admk_jtda.size() > 0) {
            String deviceID = jgmes_admk_jtda.get(0).getStr("JTDA_JTH");//��ȡ��̨��
            if (StringUtil.isNotEmpty(deviceID)) {
                map.put("DeviceID", deviceID);
                ret.Data = map;
            } else {
                ret.setMessage("��Ӧ�Ļ�̨�����л�̨��Ϊ��");
            }
        } else {
            ret.setMessage("û���ҵ���Ӧ�Ļ�̨������Ϣ");
        }
        toWrite(jsonBuilder.toJson(ret));
    }

    //��ȡ������Ϣ���ο�
    public void getGDData() {
        String mac = request.getParameter("mac");// MAC��ַ
        String userCode = request.getParameter("userCode");// �û�����  ����
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
        String gdId = request.getParameter("gdId");// MAC��ַ
        if (StringUtil.isNotEmpty(gdId)) {
            List<DynaBean> jgmes_plan_gdlb = serviceTemplate.selectList("JGMES_PLAN_GDLB", "and JGMES_PLAN_GDLB_ID='" + gdId + "'");
            if (jgmes_plan_gdlb.size() > 0) {
                ret.Data = ret.getValues(jgmes_plan_gdlb);
                ret.IsSuccess = true;
            } else {
                ret.setMessage("��ȡ������Ϣʧ�ܣ�");
            }
        } else {
            ret.setMessage("���ݻ�ȡʧ�ܣ�");
        }
        toWrite(jsonBuilder.toJson(ret));
    }

    /**
     * ��������������Ϣ
     */
    public void updateSCRWData() {
        String mac = request.getParameter("mac");// MAC��ַ
        String userCode = request.getParameter("userCode");// �û�����  ����
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
                        ret.setMessage("����ʧ�ܣ�");
                    }
                } else {
                    ret.setMessage("ת�����ݴ���");
                }
            } else {
                ret.setMessage("��ȡ������Ϣʧ�ܣ�");
            }

        } catch (Exception e) {
            e.printStackTrace();
            ret.setMessage("�쳣����");
        }
        toWrite(jsonBuilder.toJson(ret));

    }


    /**
     * �ֻ��ֶ��������淽��
     */
    public void saveLHBGSJ() {
        String mac = request.getParameter("mac");// MAC��ַ
        String userCode = request.getParameter("userCode");// �û�����  ����
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
        String jsonStr = request.getParameter("jsonStr");// MAC��ַ
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
                        jgmes_plan_scrw.set("SCRW_RWZT_NAME", "�������");
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
            ret.setMessage("�쳣����");
        }
        toWrite(jsonBuilder.toJson(ret));
    }


    /**
     * �����߶�ʱ����
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
                //��ȡʵ�ʿ���ʱ��
                float sl = 0;
                int kssl = 0;
                int jssl = 0;
                float mxsl = 0;
                List<DynaBean> cxhwxjsDynaBeanList = serviceTemplate.selectList("JGMES_SCGCGL_CXHWXJSB", " and CXHWXJSB_RWZTZJ = '" + scrwztDynaBean.getStr("JGMES_PB_SCRWZT_ID") + "' and CXHWXJSB_RWDH = '" + scrwztDynaBean.getStr("SCRWZT_RWDH") + "' order by CXHWXJSB_JSSJ");
                if (cxhwxjsDynaBeanList != null && cxhwxjsDynaBeanList.size() > 0) {
                    kssl = cxhwxjsDynaBeanList.get(0).getInt("CXHWXJSB_JSSL");
                    jssl = cxhwxjsDynaBeanList.get(cxhwxjsDynaBeanList.size() - 1).getInt("CXHWXJSB_JSSL");
                    sl = jssl - kssl;

                    //��ȡ��Ʒ��Ϣ�е�ÿ������
                    DynaBean cpDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " and PRODUCTDATA_BH = '" + scrwztDynaBean.getStr("SCRWZT_CPBM") + "'");
                    if (cpDynaBean != null) {
                        mxsl = cpDynaBean.getInt("PRODUCTDATA_MXSL");
                    }
                    if (mxsl == 0) {
                        mxsl = 1;
                    }

                    //��ȡ����������Ϣ
                    DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and SCRW_RWDH = '" + scrwztDynaBean.getStr("SCRWZT_RWDH") + "'");

                    //��ȡ������Ϣ  BGSJ_GXBH
                    DynaBean cxhwadDynaBean = serviceTemplate.selectOne("JGMES_SCGCGL_CXHWADAB", " and CXHWADAB_HWXBM = '" + cxhwxjsDynaBeanList.get(0).getStr("CXHWXJSB_HWXBM") + "' and CXHWADAB_USESTATUS_CODE = '0'");
                    if (cxhwadDynaBean != null) {
                        DynaBean bgsjDynaBean = serviceTemplate.selectOne("JGMES_PB_BGSJ", " and BGSJ_CXBM = '" + scrwztDynaBean.getStr("SCRWZT_CXBM") + "' and BGSJ_SCRW = '" + scrwztDynaBean.getStr("SCRWZT_RWDH") + "' and BGSJ_GXBH = '" + cxhwadDynaBean.getStr("CXHWADAB_GXBM") + "' and BGSJ_BGLX != '0'");
                        if (bgsjDynaBean != null) {//�����Ϊ�գ����޸ı�������
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
            //��д���������е�����
            for (DynaBean scrwztDynaBean : scrwztDynaBeanList) {
                //��ȡʵ�ʿ���ʱ��
                int sl = 0;
                int kssl = 0;
                int jssl = 0;
                int mxsl = 0;
                List<DynaBean> cxhwxjsDynaBeanList = serviceTemplate.selectList("JGMES_SCGCGL_CXHWXJSB", " and CXHWXJSB_RWZTZJ = '" + scrwztDynaBean.getStr("JGMES_PB_SCRWZT_ID") + "' and CXHWXJSB_RWDH = '" + scrwztDynaBean.getStr("SCRWZT_RWDH") + "' order by CXHWXJSB_JSSJ");
                if (cxhwxjsDynaBeanList != null && cxhwxjsDynaBeanList.size() > 0) {
                    kssl = cxhwxjsDynaBeanList.get(0).getInt("CXHWXJSB_JSSL");
                    jssl = cxhwxjsDynaBeanList.get(cxhwxjsDynaBeanList.size() - 1).getInt("CXHWXJSB_JSSL");
                    sl = jssl - kssl;

                    //��ȡ��Ʒ��Ϣ�е�ÿ������
                    DynaBean cpDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA", " and PRODUCTDATA_BH = '" + scrwztDynaBean.getStr("SCRWZT_CPBM") + "'");
                    if (cpDynaBean != null) {
                        mxsl = cpDynaBean.getInt("PRODUCTDATA_MXSL");
                    }
                    if (mxsl == 0) {
                        mxsl = 1;
                    }
                    //��ȡ����������Ϣ
                    DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and SCRW_RWDH = '" + scrwztDynaBean.getStr("SCRWZT_RWDH") + "'");
                    scrwDynaBean.set("SCRW_WCSL", scrwDynaBean.getInt("SCRW_WCSL") + sl / mxsl);
                    serviceTemplate.update(scrwDynaBean);
                }
            }
        }
    }


    /**
     * �ֻݶ�ʱ����
     *
     * @throws ParseException
     */
    public void doSaveLhBgsjTask() {
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
        List<DynaBean> scrwztDynaBeanList = serviceTemplate.selectList("JGMES_PB_SCRWZT", " and SCRWZT_SCZT_CODE = 'SCZT01'");
        if (scrwztDynaBeanList != null && scrwztDynaBeanList.size() > 0) {
            for (DynaBean scrwztDynaBean : scrwztDynaBeanList) {
                //��ȡʵ�ʿ���ʱ��
                int sl = 0;
                int kscs = 0;
                int jscs = 0;

                //��ȡ�����߻�̨������
                DynaBean cxjtglDynaBean = serviceTemplate.selectOne("JGMES_SCGCGL_CXJTGL", " and CXJTGL_CXBM = '" + scrwztDynaBean.getStr("SCRWZT_CXBM") + "'");
//				DynaBean cxjtglDynaBean = serviceTemplate.selectOne("JGMES_ADMK_JTDA","and JTDA_SCXBM='"+scrwztDynaBean.getStr("SCRWZT_CXBM")+"'");
                //��ȡ��ѹ������
                List<DynaBean> cyjsDynaBeanList = serviceTemplate.selectList("JGMES_SCGCGL_CYJSB", " and CYJSB_CXBM = '" + scrwztDynaBean.getStr("SCRWZT_CXBM") + "' and CYJSB_RWDH = '" + scrwztDynaBean.getStr("SCRWZT_RWDH") + "' and CYJSB_NO_CODE = '0' order by CYJSB_JLSJ");
                if (cyjsDynaBeanList != null && cyjsDynaBeanList.size() > 0) {
                    kscs = cyjsDynaBeanList.get(0).getInt("CYJSB_ZCCYCS");
                    jscs = cyjsDynaBeanList.get(cyjsDynaBeanList.size() - 1).getInt("CYJSB_ZCCYCS");
                    sl = jscs - kscs;
                    //��ȡ����������Ϣ
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
                    //��д��ѹ�������е�����
                    DynaBean dic1 = jgmesCommon.getDic("JGMES_YES_NO", "1");
                    if (dic1 != null) {
                        //�Ƿ񱨹�
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
			//��д���������е�����
			for(DynaBean scrwztDynaBean:scrwztDynaBeanList) {
				//��ȡʵ�ʿ���ʱ��
				int sl = 0;
				int kssl = 0;
				int jssl = 0;
				//��ȡ��ѹ������
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
//	 * �ֻݶ�ʱ����
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
//					//��ȡ�����߻�̨������
//					DynaBean cxjtglDynaBean = serviceTemplate.selectOne("JGMES_SCGCGL_CXJTGL"," and CXJTGL_CXBM = '"+scrwztDynaBeanList.get(0).getStr("SCRWZT_CXBM")+"'");
//
//					//��ȡ����������Ϣ
//					DynaBean scrwDynaBean = serviceTemplate.selectOne("JGMES_PLAN_SCRW", " and SCRW_RWDH = '"+scrwztDynaBeanList.get(0).getStr("SCRWZT_RWDH")+"'");
//					if(scrwDynaBean!=null){
//						scrwDynaBean.set("SCRW_WCSL", scrwDynaBean.getInt("SCRW_WCSL")+sl);
//						serviceTemplate.update(scrwDynaBean);
//					}
//
//					DynaBean bgslDynaBean= setBgsl(cxjtglDynaBean,scrwztDynaBeanList.get(0),Integer.toString(sl));
//					serviceTemplate.insert(bgslDynaBean);
//
//					//��ȡ��ѹ������
//					List<DynaBean> cyjsDynaBeanList = serviceTemplate.selectList("JGMES_SCGCGL_CYJSB", " and CYJSB_CXBM = '"+jsslDynaBean.getStr("CYJSB_CXBM")+"' and CYJSB_RWDH = '"+jsslDynaBean.getStr("CYJSB_RWDH")+"' and CYJSB_NO_CODE = '0' order by CYJSB_JLSJ");
//
//
//					//��д��ѹ�������е�����
//					DynaBean dic1 = jgmesCommon.getDic("JGMES_YES_NO","1");
//					if(dic1!=null) {
//						//�Ƿ񱨹�
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
                bgsj.set("BGSJ_CXMC", cxDynaBean.getStr("CXSJ_CXMC"));  //��������
            }
        }
        bgsj.set("BGSJ_CXBM", scrwztDynaBean.getStr("SCRWZT_CXBM"));  //���߱���
//		if(cxhwadDynaBean.getStr("CXJTGL_GXBM")!=null&&!"".equals(cxhwadDynaBean.getStr("CXJTGL_GXBM"))){
//			DynaBean gxDynaBean = serviceTemplate.selectOne("JGMES_GYGL_GXGL"," and GXGL_GXNUM = '"+cxhwadDynaBean.getStr("CXJTGL_GXBM")+"'");
//			if(gxDynaBean!=null){
//				//������
//				bgsj.set("BGSJ_GXBH", gxDynaBean.getStr("GXGL_GXNUM"));
//				//��������
//				bgsj.set("BGSJ_GXMC", gxDynaBean.getStr("GXGL_GXNAME"));
//			}
//		}else{
//			bgsj.set("BGSJ_GXBH", cxhwadDynaBean.getStr("CXHWADAB_GXBM"));  //������
//			bgsj.set("BGSJ_GXMC", cxhwadDynaBean.getStr("CXHWADAB_GXMC"));  //��������
//		}

        bgsj.set("BGSJ_CPBH", scrwztDynaBean.getStr("SCRWZT_CPBM"));  //��Ʒ���
        bgsj.set("BGSJ_CPMC", scrwztDynaBean.getStr("SCRWZT_CPMC"));  //��Ʒ����
        bgsj.set("BGSJ_CPGG", scrwztDynaBean.getStr("SCRWZT_CPGG"));  //��Ʒ���
        //bgsj.set("BGSJ_TMH", value);  //�����

//		bgsj.set("BGSJ_GWBH", value);  //��λ���
//		bgsj.set("BGSJ_GWMC", value);  //��λ����
        bgsj.set("BGSJ_SL", sl);  //����
        //bgsj.set("BGSJ_BZ", value);  //��ע

        //����״̬��Ĭ�Ͽ�����
        DynaBean dic = jgmesCommon.getDic("JGMES_STATUS", "1");
        if (dic != null) {
            bgsj.set("BGSJ_STATUS_NAME", dic.getStr("DICTIONARYITEM_ITEMNAME"));
            bgsj.set("BGSJ_STATUS_CODE", dic.getStr("DICTIONARYITEM_ITEMCODE"));
            bgsj.set("BGSJ_STATUS_ID", dic.getStr("JE_CORE_DICTIONARYITEM_ID"));
        }
        bgsj.set("BGSJ_SCRW", scrwztDynaBean.getStr("SCRWZT_RWDH"));  //��������

        return bgsj;
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
     * �����Ų���״̬
     */
    public void doSaveScrwZt(String userCode, String scrwId, String scrwZt, String ztyyZ) {
        JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate, userCode);
        JgmesResult<String> ret = new JgmesResult<String>();
        try {
            System.out.println("��������ID��" + scrwId);
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

                //�жϹ���������Ƿ����
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
                            //�޸������еĹ���״̬
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
				if (scrwZt.equalsIgnoreCase(JgmesEnumsDic.ScFinished.getKey()) || scrwZt.equalsIgnoreCase(JgmesEnumsDic.ScPause.getKey())) {//���ʱɾ������ָ��������
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
            toWrite("����״̬����" + jsonBuilder.toJson(ret));
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