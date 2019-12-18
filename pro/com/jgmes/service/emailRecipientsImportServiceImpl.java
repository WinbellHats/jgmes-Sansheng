package com.jgmes.service;

import com.je.core.security.SecurityUserHolder;
import com.je.core.service.PCDynaServiceTemplate;
import com.je.core.service.PCServiceTemplate;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.DynaBean;
import com.je.rbac.model.Department;
import com.je.rbac.model.EndUser;
import com.je.rbac.service.UserManager;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * �����ռ��˶�Ӧ��
 *
 * @author admin
 * @version 2019-06-20 15:54:34
 */
@Component("emailRecipientsImportService")
public class emailRecipientsImportServiceImpl implements emailRecipientsImportService {

    /**
     * ��̬Bean(DynaBean)�ķ����
     */
    private PCDynaServiceTemplate serviceTemplate;
    /**
     * ʵ��Bean���������,��Ҫ����SQL
     */
    private PCServiceTemplate pcServiceTemplate;
    /**
     * �û������
     */
    private UserManager userManager;

    public void load() {
        System.out.println("hello serviceimpl");
    }

    @Override
    public DynaBean emailRecipientsImport(DynaBean order) {
        StringBuilder errorMessage = new StringBuilder(2000);//错误信息记录
        Boolean haveError = false;
        String Recipient = order.getStr("YXSJRDYB_SJR");//收件人
        String yjfslx = order.getStr("YXSJRDYB_YJFSLX_NAME");//邮件发送类型——文本：SendContextType.TEXT，HTML:SendContextType.HTML
        String yjfssxlx = order.getStr("YXSJRDYB_YJFSSXLX_NAME");//邮件发送事项类型——保养：1
        //收件人校验
        if (StringUtil.isNotEmpty(Recipient)) {
            List<DynaBean> je_core_enduser = serviceTemplate.selectList("JE_CORE_ENDUSER", "and USERCODE = '" + Recipient + "'");
            if (je_core_enduser.size() == 0) {
                errorMessage.append("该收件人在人员表中不存在！" + "</br>");
                haveError = true;
            }
            List<DynaBean> jgmes_mjmk_yxsjrdyb = serviceTemplate.selectList("JGMES_MJMK_YXSJRDYB", "and YXSJRDYB_SJR='" + Recipient + "'");
            if (jgmes_mjmk_yxsjrdyb.size()>0){
                errorMessage.append("该收件人在邮箱收件人对应表中已存在！" + "</br>");
                haveError = true;
            }

        } else {
            errorMessage.append("收件人不能为空" + "</br>");
            haveError = true;
        }
        //邮件发送类型校验
        if (StringUtil.isNotEmpty(yjfslx)){
            String sql = "select * from je_core_dictionaryitem where dictionaryitem_dictionary_ID = (select je_core_dictionary_ID from je_core_dictionary where dictionary_DDCODE = 'JGMES_DIC_YJFSLX')";
            List<DynaBean> DicList = serviceTemplate.selectListBySql(sql);
            Boolean error = false;
            for (DynaBean dicBean : DicList) {
                if (yjfslx.equals(dicBean.getStr("DICTIONARYITEM_ITEMNAME"))){
                    order.set("YXSJRDYB_YJFSLX_CODE",dicBean.getStr("DICTIONARYITEM_ITEMCODE"));
                    error =true;
                }
            }
            if (!error){
                errorMessage.append("邮件发送类型名称填写错误，请查看字典" + "</br>");
                haveError = true;
            }
        }else{
            errorMessage.append("邮件发送类型不能为空" + "</br>");
            haveError = true;
        }
        //邮件发送事项类型校验
        if (StringUtil.isNotEmpty(yjfssxlx)){
            String sql = "select * from je_core_dictionaryitem where dictionaryitem_dictionary_ID = (select je_core_dictionary_ID from je_core_dictionary where dictionary_DDCODE = 'JGMEST_DIC_YJFSSXLX')";
            List<DynaBean> DicList = serviceTemplate.selectListBySql(sql);
            Boolean error = false;
            for (DynaBean dicBean : DicList) {
                if (yjfssxlx.equals(dicBean.getStr("DICTIONARYITEM_ITEMNAME"))){
                    order.set("YXSJRDYB_YJFSSXLX_CODE",dicBean.getStr("DICTIONARYITEM_ITEMCODE"));
                    error =true;
                }
            }
            if (!error){
                errorMessage.append("邮件发送事项类型名称填写错误，请查看字典" + "</br>");
                haveError = true;
            }
        }else{
            errorMessage.append("邮件发送事项类型不能为空" + "</br>");
            haveError = true;
        }
        if (haveError) {
            order.set("error", "序号:" + order.getStr("rownumberer_1") + "的错误信息为：" + errorMessage.toString());
        }
        return order;
    }


    /**
     * ��ȡ��¼�û�
     *
     * @return
     */
    public EndUser getCurrentUser() {
        // TODO Auto-generated method stub
        return SecurityUserHolder.getCurrentUser();
    }

    /**
     * ��ȡ��¼�û����ڲ���
     *
     * @return
     */
    public Department getCurrentDept() {
        // TODO Auto-generated method stub
        return SecurityUserHolder.getCurrentUserDept();
    }

    @Resource(name = "PCDynaServiceTemplate")
    public void setServiceTemplate(PCDynaServiceTemplate serviceTemplate) {
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