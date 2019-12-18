package com.jgmes.action;

import com.je.core.action.DynaAction;
import com.je.core.constants.doc.JEFileType;
import com.je.core.security.SecurityUserHolder;
import com.je.core.util.StringUtil;
import com.je.core.util.WebUtils;
import com.je.core.util.bean.DynaBean;
import com.je.develop.service.ExcelManager;
import com.je.documentation.service.DocumentationManager;
import com.jgmes.service.IJdService;
import com.jgmes.util.JgmesResult;
import com.jgmes.util.PubUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * �޴������������
 * @author lt`
 * @since 20190327
 */
@Component("jdAction")
@Scope("prototype")
public class JdAction extends DynaAction {
    private static final long serialVersionUID = 6372590838276248902L;
    private String fieldCodes;
    private String fieldNames;
    private String ddObj = "{}";
    private ExcelManager excelManager;
    private IJdService jdService;
    private DocumentationManager documentationManager;

    public JdAction() {
    }

    public void exp() {
        String funcId = this.request.getParameter("funcId");
        String fileName = this.request.getParameter("fileName");
        String title = this.request.getParameter("title");
        String styleType = this.request.getParameter("styleType");
        String docFolderId = this.request.getParameter("docFolderId");
        String queryType = this.request.getParameter("queryType");
        String datasourceName = this.request.getParameter("datasourceName");
        String procedureName = this.request.getParameter("procedureName");
        String dbSql = this.request.getParameter("dbSql");
        String queryParamStr = this.request.getParameter("queryParamsStr");
        String queryParamValueStr = this.request.getParameter("dbQueryObj");
        if ("procedure".equals(queryType)) {
            if (StringUtil.isEmpty(procedureName) || StringUtil.isEmpty(funcId)) {
                this.toWrite(jsonBuilder.returnFailureJson("�洢������δ����"));
                return;
            }
        } else if ("sql".equals(queryType)) {
            if (StringUtil.isEmpty(dbSql) || StringUtil.isEmpty(funcId)) {
                this.toWrite(jsonBuilder.returnFailureJson("�Զ���sqlδ����"));
                return;
            }
        } else if (StringUtil.isEmpty(this.tableCode) || StringUtil.isEmpty(funcId)) {
            this.toWrite(jsonBuilder.returnFailureJson("���ܱ���δ����"));
            return;
        }

        try {
            String folderPath = (String) WebUtils.docVar.get("develop.excel.path") + SecurityUserHolder.getCurrentUser().getUserCode() + "/export/" + PubUtil.getDateTime("yyyyMMddHHmmss");
            this.jeFileManager.createFolder(folderPath, JEFileType.PLATFORM);
            //excelRelativePath=/JE/data/excel/null/export/201903281613/CLIO����������ϸ����ֶ���Ϣ_2019-03-28.zip
            String excelRelativePath = folderPath +"/"+ fileName+".xls";
            this.excelManager.exp(title, excelRelativePath, funcId, this.tableCode, styleType, this.whereSql, this.orderSql, queryType, datasourceName, procedureName, queryParamStr, queryParamValueStr, dbSql);

            this.fileSyncService.syncAddFile(excelRelativePath);
            if (StringUtil.isNotEmpty(docFolderId)) {
                DynaBean folder = this.serviceTemplate.selectOneByPk("JE_SYS_DOCUMENTATION", docFolderId);
                DynaBean file = new DynaBean("JE_SYS_DOCUMENTATION", false);
                file.set("$PK_CODE$", "JE_SYS_DOCUMENTATION_ID");
                file.set("SY_PARENT", folder.getStr("JE_SYS_DOCUMENTATION_ID"));
                file.set("SY_PATH", folder.getStr("SY_PATH"));
                file.set("SY_LAYER", folder.getInt("SY_LAYER") + 1);
                file.set("DOCUMENTATION_FILEMODE", folder.getStr("DOCUMENTATION_FILEMODE"));
                JSONArray arrays = new JSONArray();
                JSONObject fileObj = new JSONObject();
                fileObj.put("name", fileName);
                fileObj.put("path", excelRelativePath);
                arrays.add(fileObj);
                this.documentationManager.doSaveFile(file, arrays.toString());
            }
            //������������excelһ��ѹ�� 20190328
            String zipFileRelativePath = jdService.exportAttachAndZipExcelAndAttach(excelRelativePath, folderPath, this.whereSql, fileName+".zip");
            //���ѹ������ļ����·����ǰ��,��JE��װ�ķ����Ĵ�
//            PubUtil.outPutFile(this.response,zipFilePath,null);

            this.toWrite(jsonBuilder.returnSuccessJson("'" + zipFileRelativePath + "'"));
//            this.toWrite(zipFilePath);
        } catch (Exception var18) {
            var18.printStackTrace();
            this.toWrite(jsonBuilder.returnFailureJson("JE.local.lang.server.develop.excelExpFail+'" + var18.getMessage() + "'"));
        }

    }

    /**
    * Description: ͨ��SN��Ѱ������
    * @Param:  * @param null
    * @return:
    * @author: ljs
    * @date: 2019/10/29 14:13
    */
    public void SearchOldBarCodeData(){
        String barcode = this.request.getParameter("barcode");//SN��
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
        if (StringUtil.isNotEmpty(barcode)){
            List<DynaBean> dynaBeans = serviceTemplate.selectList("JGMES_PB_BGSJ", " and BGSJ_TMH='"+barcode+"'","BGSJ_SCRW,BGSJ_CPBH,BGSJ_CPMC,BGSJ_CPGG");//��ȡ����������б�����Ϣ
            if (dynaBeans.size()>0) {
                DynaBean dynaBean = dynaBeans.get(0);
                //�����������񵥺Ż�ȡ��Ϣ
                String scrw = dynaBean.getStr("BGSJ_SCRW");
                //���Ҷ������룬���̿��ţ��ڲ������ţ��������������Ų�����������������
                DynaBean jgmes_plan_scrw = serviceTemplate.selectOne("JGMES_PLAN_SCRW", "and SCRW_RWDH='" + scrw + "'", "SCRW_DDHM,SCRW_LCKH,SCRW_DDSL,SCRW_PCSL");
                if (jgmes_plan_scrw!=null){
                    dynaBean.set("SCRW_DDHM",jgmes_plan_scrw.get("SCRW_DDHM"));//��������
                    dynaBean.set("SCRW_LCKH",jgmes_plan_scrw.get("SCRW_LCKH"));//���̿��ţ��ڲ������ţ�
                    dynaBean.set("SCRW_DDSL",jgmes_plan_scrw.get("SCRW_DDSL"));//��������
                    dynaBean.set("SCRW_PCSL",jgmes_plan_scrw.get("SCRW_PCSL"));//�Ų�����������������
                    ret.Data=dynaBean.getValues();

                }else {
                    ret.setMessage("��SN��󶨵��������񵥲�����");
                }

            }else{
                ret.setMessage("�������û�н��й�����");
            }
        }else{
            ret.setMessage("SN�벻��Ϊ��");
        }

        toWrite(jsonBuilder.toJson(ret));
    }

    public void ReplaceTheBarCode(){
        String oldBarCode = this.request.getParameter("oldBarCode");//��SN��
        String newBarCode = this.request.getParameter("newBarCode");//��SN��
        JgmesResult<HashMap> ret = new JgmesResult<HashMap>();
        //��У���µ�SN���ڱ����������Ƿ����
        List<DynaBean> jgmes_pb_bgsj = serviceTemplate.selectList("JGMES_PB_BGSJ", "and BGSJ_TMH='" + newBarCode + "'");
        if (jgmes_pb_bgsj.size()==0) {
            //��鱨�����ݼ������Ƿ����14��ĩ����������������ʾ�����������
            List<DynaBean> checkLastGx = serviceTemplate.selectList("JGMES_PB_BGSJ", "and  BGSJ_TMH='" + oldBarCode + "' and BGSJ_GXSXH =14");
            if (checkLastGx.size()==0) {
                ret.setMessage("��SN��û�������ı�������!���ܽ����滻");
            }else{
                //������������������6�Ź��򣩣�װ�乤��13�Ź����⣩�ı�������
                List<DynaBean> bgsj = serviceTemplate.selectList("JGMES_PB_BGSJ", "and  BGSJ_TMH='" + oldBarCode + "' and BGSJ_GXSXH not in(6,13)");
                //�����������SN��

            }
        }else {
            ret.setMessage("����SN�����ڱ����������м�¼���������滻");
        }
        toWrite(jsonBuilder.toJson(ret));
    }







    public String getFieldCodes() {
        return this.fieldCodes;
    }

    public void setFieldCodes(String fieldCodes) {
        this.fieldCodes = fieldCodes;
    }

    public String getFieldNames() {
        return this.fieldNames;
    }

    public void setFieldNames(String fieldNames) {
        this.fieldNames = fieldNames;
    }

    public String getDdObj() {
        return this.ddObj;
    }

    public void setDdObj(String ddObj) {
        this.ddObj = ddObj;
    }

    @Resource(
            name = "excelManager"
    )
    public void setExcelManager(ExcelManager excelManager) {
        this.excelManager = excelManager;
    }

    @Resource(
            name = "documentationManager"
    )
    public void setDocumentationManager(DocumentationManager documentationManager) {
        this.documentationManager = documentationManager;
    }

    public IJdService getJdService() {
        return jdService;
    }

    @Resource(
            name = "jdService"
    )
    public void setJdService(IJdService jdService) {
        this.jdService = jdService;
    }
}
