package com.jgmes.action;

import com.alibaba.druid.pool.DruidDataSource;
import com.je.core.action.BaseAction;
import com.je.core.action.DynaAction;
import com.je.core.entity.QueryInfo;
import com.je.core.security.SecurityUserHolder;
import com.je.core.service.PCDynaServiceTemplate;
import com.je.core.service.PCServiceTemplate;
import com.je.core.util.*;
import com.je.core.util.bean.BeanUtils;
import com.je.core.util.bean.DynaBean;
import com.je.dd.service.DictionaryManager;
import com.je.dd.vo.DictionaryItemVo;
import com.je.develop.service.DataImplManager;
import com.je.rbac.model.EndUser;
import com.je.table.exception.PCExcuteException;
import net.sf.json.JSONObject;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * ���ݵ���
 *
 * @author ljs
 * @version 2019-04-12 21:03:07
 * @see /jgmes/jgDataImportAction!load.action
 */
@Component("jgDataImportAction")
@Scope("prototype")
public class JgDataImportAction extends DynaAction {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(JgDataImportAction.class);

    private PCDynaServiceTemplate serviceTemplate;
    private PCServiceTemplate pcServiceTemplate;
    private DictionaryManager dictionaryManager;


    public void load() {
        toWrite("hello Action");
    }

    private DataImplManager dataImplManager;

    /**
     * 生成模版并且返回地址
     */
    public void generateTemplate() {
        JSONObject returnObj = new JSONObject();
        try {
            dataImplManager.generateTemplate(pkValue, new ArrayList<DynaBean>(), returnObj);
            toWrite(jsonBuilder.returnSuccessJson("\"" + returnObj.getString("templatePath") + "\""));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            toWrite(jsonBuilder.returnFailureJson("\"导出模版出错!\""));
        }
    }

    /**
     * 导入字段
     */
    public void implFields() {
        String funcId = request.getParameter("funcId");
        pcServiceTemplate.executeSql(" DELETE FROM JE_SYS_DATAFIELD WHERE JE_SYS_DATATEMPLATE_ID='" + pkValue + "'");
        //AND RESOURCECOLUMN_XTYPE!='morecolumn'
        List<DynaBean> columnFields = serviceTemplate.selectList("JE_CORE_VCOLUMNANDFIELD", " AND RESOURCECOLUMN_FUNCINFO_ID='" + funcId + "'  ORDER BY SY_ORDERINDEX");
        for (DynaBean columnField : columnFields) {
            DynaBean dataField = new DynaBean("JE_SYS_DATAFIELD", false);
            dataField.set(BeanUtils.KEY_PK_CODE, "JE_SYS_DATAFIELD_ID");
            dataField.set("SY_ORDERINDEX", columnField.getStr("SY_ORDERINDEX"));
            dataField.set("DATAFIELD_CODE", columnField.getStr("RESOURCECOLUMN_CODE"));
            dataField.set("DATAFIELD_NAME", columnField.getStr("RESOURCECOLUMN_NAME"));
            //表单类型   RESOURCEFIELD_XTYPE     RESOURCECOLUMN_XTYPE
            if ("morecolumn".equals(columnField.getStr("RESOURCECOLUMN_XTYPE"))) {
                dataField.set("DATAFIELD_XTYPE", "morecolumn");
            } else if ("rownumberer".equals(columnField.getStr("RESOURCECOLUMN_XTYPE"))) {
                dataField.set("DATAFIELD_XTYPE", "rownumberer");
            } else if ("uxcheckcolumn".equals(columnField.getStr("RESOURCECOLUMN_XTYPE"))) {
                dataField.set("DATAFIELD_XTYPE", "cbbfield");
                dataField.set("DATAFIELD_CONFIGINFO", columnField.getStr("RESOURCEFIELD_CONFIGINFO"));
            } else if ("datefield".equals(columnField.getStr("RESOURCECOLUMN_XTYPE")) || ("datetimefield".equals(columnField.getStr("RESOURCECOLUMN_XTYPE")))) {
                dataField.set("DATAFIELD_XTYPE", columnField.getStr("RESOURCEFIELD_XTYPE"));
            } else {
                String xtype = columnField.getStr("RESOURCEFIELD_XTYPE");
                if (ArrayUtils.contains(new String[]{"cbbfield", "rgroup", "cgroup"}, xtype)) {
                    dataField.set("DATAFIELD_XTYPE", "cbbfield");
                    dataField.set("DATAFIELD_CONFIGINFO", columnField.getStr("RESOURCEFIELD_CONFIGINFO"));
                } else if (ArrayUtils.contains(new String[]{"treessfield", "treessareafield"}, xtype)) {
                    String[] configs = columnField.getStr("RESOURCEFIELD_CONFIGINFO", "").split(",");
                    if (configs.length > 0) {
                        String ddCode = configs[0];
                        DynaBean dic = serviceTemplate.selectOne("JE_CORE_DICTIONARY", " and DICTIONARY_DDTYPE IN ('LIST','TREE') AND DICTIONARY_DDCODE='" + ddCode + "'");
                        if (dic != null) {
                            dataField.set("DATAFIELD_XTYPE", "cbbfield");
                            dataField.set("DATAFIELD_CONFIGINFO", columnField.getStr("RESOURCEFIELD_CONFIGINFO"));
                        } else {
                            dataField.set("DATAFIELD_XTYPE", "textfield");
                        }
                    } else {
                        dataField.set("DATAFIELD_XTYPE", "textfield");
                    }
                } else {
                    dataField.set("DATAFIELD_XTYPE", "textfield");
                }
            }
            dataField.set("DATAFIELD_WIDTH", columnField.getStr("RESOURCECOLUMN_WIDTH"));
            dataField.set("DATAFIELD_VALUE", columnField.getStr("RESOURCEFIELD_VALUE"));
            dataField.set("DATAFIELD_SSDBT", columnField.getStr("RESOURCECOLUMN_MORECOLUMNNAME"));
            dataField.set("DATAFIELD_XLK", "0");
            dataField.set("DATAFIELD_EMPTY", "0");
            if ("0".equals(columnField.getStr("RESOURCEFIELD_ALLOWBLANK"))) {
                dataField.set("DATAFIELD_EMPTY", "1");
            }
            dataField.set("DATAFIELD_HIDDEN", columnField.getStr("RESOURCECOLUMN_HIDDEN"));
            dataField.set("JE_SYS_DATATEMPLATE_ID", pkValue);
            serviceTemplate.buildModelCreateInfo(dataField);
            serviceTemplate.insert(dataField);
        }
        toWrite(jsonBuilder.returnSuccessJson("'导入成功!'"));
    }

    public void clearHidden() {
        if (StringUtil.isNotEmpty(pkValue)) {
            pcServiceTemplate.executeSql(" DELETE FROM JE_SYS_DATAFIELD WHERE JE_SYS_DATATEMPLATE_ID='" + pkValue + "' AND DATAFIELD_HIDDEN='1'");
            toWrite(jsonBuilder.returnSuccessJson("'操作成功!'"));
        } else {
            toWrite(jsonBuilder.returnSuccessJson("'删除出错，请联系管理员!'"));
        }
    }

    public void uploadFile() {
        System.out.println("uploadFile启动");
        List<DynaBean> docs = (List<DynaBean>) dynaBean.get(BeanUtils.KEY_DOC_INFO);
        if (null != docs && 0 != docs.size()) {
            DynaBean doc = docs.iterator().next();
            dynaBeanTemplate.doSaveDocumentInfo(doc);
            String filePath = doc.getStr("DOCUMENT_ADDRESS");
            JSONObject returnObj = new JSONObject();
            JSONObject params = new JSONObject();
            String paramStr = request.getParameter("params");
            if (StringUtil.isNotEmpty(paramStr)) {
                params = JSONObject.fromObject(paramStr);
            }
            returnObj.put("params", params);
            returnObj.put("pkValue", pkValue);
//			dataImplManager.implData(BaseAction.webrootAbsPath+filePath, returnObj);
            implData(BaseAction.webrootAbsPath + filePath, returnObj);
            FileOperate.delFile(BaseAction.webrootAbsPath + filePath);
            if (returnObj.containsKey("error")) {
                toWrite(jsonBuilder.returnFailureJson("'" + returnObj.getString("error") + "'"));
            } else {
                toWrite(jsonBuilder.returnSuccessJson(returnObj.toString()));
            }
        } else {
            toWrite(jsonBuilder.returnFailureJson("'文件上传失败!'"));
        }
    }

    public void applyFunc() {
        DynaBean funcInfo = serviceTemplate.selectOne("JE_CORE_FUNCINFO", " AND FUNCINFO_FUNCCODE='JE_SYS_DATATEMPLATE' AND FUNCINFO_NODEINFOTYPE='FUNC'", "JE_CORE_FUNCINFO_ID,FUNCINFO_FUNCNAME,FUNCINFO_FUNCCODE");
        List<DynaBean> buttons = serviceTemplate.selectList("JE_CORE_RESOURCEBUTTON", " AND RESOURCEBUTTON_FUNCINFO_ID='" + funcInfo.getStr("JE_CORE_FUNCINFO_ID") + "' AND RESOURCEBUTTON_CODE IN ('downloadDataBtn','implDataBtn')");
        String funcId = request.getParameter("funcId");
        String templateId = request.getParameter("templateId");
        Long count = serviceTemplate.selectCount("JE_CORE_RESOURCEBUTTON", " AND RESOURCEBUTTON_FUNCINFO_ID='" + funcId + "' AND RESOURCEBUTTON_CODE IN ('downloadDataBtn','implDataBtn')");
//		if(count>0){
//			toWrite(jsonBuilder.returnFailureJson("'功能已有模版按钮，请先删除后应用!'"));
//			return;
//		}
        for (DynaBean button : buttons) {

            button.set("RESOURCEBUTTON_FUNCINFO_ID", funcId);
            button.set("JE_CORE_RESOURCEBUTTON_ID", null);
            String jsListener = button.getStr("RESOURCEBUTTON_JSLISTENER", "");
            jsListener = jsListener.replace("_JEREPLACEPKVALUE_", templateId);
            button.set("RESOURCEBUTTON_JSLISTENER", jsListener);
            button.set("RESOURCEBUTTON_DISABLED", "0");
            button.set("SY_ORDERINDEX", "0");
            serviceTemplate.buildModelCreateInfo(button);
            serviceTemplate.insert(button);
        }
        toWrite(jsonBuilder.returnSuccessJson("'应用成功,请查看功能按钮信息!'"));
    }

    @Resource(name = "dataImplManager")
    public void setDataImplManager(DataImplManager dataImplManager) {
        this.dataImplManager = dataImplManager;
    }

    //*****

    public void implData(String filePath, JSONObject returnObj) {
        System.out.println("implData启动！");
        // TODO Auto-generated method stub
        FileInputStream fis = null;
        POIFSFileSystem fs = null;
        HSSFWorkbook wb = null;
        JSONObject params = new JSONObject();
        Boolean isInsert = false;
        int sheetIndex = 0;
        if (returnObj.containsKey("sheet")) {
            if (StringUtil.isNotEmpty(returnObj.get("sheet") + "")) {
                sheetIndex = returnObj.getInt("sheet");
            }
        }
        if (returnObj.containsKey("params")) {
            params = returnObj.getJSONObject("params");
        }
        try {
            // 1.得到文件对象
            fis = new FileInputStream(filePath);
            fs = new POIFSFileSystem(fis);
            wb = new HSSFWorkbook(fs);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            returnObj.put("error", "文件读取出错!");
            return;
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            returnObj.put("error", "文件读取出错!");
            return;
        }
        // 报表封面
        HSSFSheet sheet = wb.getSheetAt(sheetIndex);
        HSSFCell nowCell = null;
        EndUser currentUser = SecurityUserHolder.getCurrentUser();
        Integer success = 0;
        Integer failure = 0;
        List<String> failures = new ArrayList<String>();
        //获取模版主键
        nowCell = getCell(sheet, 1, 1);
        String templateId = getCellStringValue(nowCell);
        if (StringUtil.isEmpty(templateId)) {
            returnObj.put("error", "未发现模版信息，请重新下载模版...");
            return;
        }
        DynaBean template = serviceTemplate.selectOneByPk("JE_SYS_DATATEMPLATE", templateId);
        if (template == null) {
            returnObj.put("error", "未在系统查到模版信息，请查看系统模版内容...");
            return;
        }
        if (!templateId.equals(returnObj.getString("pkValue"))) {
            returnObj.put("error", "数据信息与当前模版不对应，请重新下载模版...");
            return;
        }
        List<DynaBean> allFields = serviceTemplate.selectList("JE_SYS_DATAFIELD", " AND JE_SYS_DATATEMPLATE_ID='" + templateId + "' ORDER BY DATAFIELD_HIDDEN ASC,SY_ORDERINDEX ASC");
        List<DynaBean> fields = new ArrayList<DynaBean>();
        Map<String, DynaBean> fieldInfos = new HashMap<String, DynaBean>();
        Map<String, List<DictionaryItemVo>> dicInfos = new HashMap<String, List<DictionaryItemVo>>();
        for (DynaBean field : allFields) {
            if ("cbbfield".equals(field.getStr("DATAFIELD_XTYPE"))) {
                String configInfo = field.getStr("DATAFIELD_CONFIGINFO", "");
                if (StringUtil.isNotEmpty(configInfo)) {
                    //获取字典数据
                    String ddCode = configInfo.split(",")[0];
                    List<DictionaryItemVo> dicItems = dictionaryManager.getDicList(ddCode, new QueryInfo(), false);
                    dicInfos.put(ddCode, dicItems);
                }
            }
            if (!"1".equals(field.getStr("DATAFIELD_HIDDEN"))) {// &&
                fields.add(field);
                fieldInfos.put(field.getStr("DATAFIELD_NAME"), field);
            }
        }

        //计算出数据模版列的的顺序集合
        List<DynaBean> columns = new ArrayList<DynaBean>();
        for (int i = 0; i < fields.size(); i++) {
            nowCell = getCell(sheet, 2 + i, 3);
            if (nowCell == null) {
                break;
            }
            String fieldName = getCellStringValue(nowCell);
            if (StringUtil.isEmpty(fieldName)) {
                nowCell = getCell(sheet, 2 + i, 2);
                fieldName = getCellStringValue(nowCell);
            }
            DynaBean field = fieldInfos.get(fieldName);
            if (field == null) {
                returnObj.put("error", "模版标题【" + fieldName + "】在系统未找到，请重新下载模版!");
                return;
            }
            columns.add(field);
        }
        if (columns.size() <= 0) {
            returnObj.put("error", "模版标题不符合格式，请重新下载模版!");
            return;
        }
        //执行前置sql
        String sqlStr = template.getStr("DATATEMPLATE_BEFORESQL", "");
        if (StringUtil.isNotEmpty(sqlStr)) {
            String[] sqls = sqlStr.split(";");
            for (String sql : sqls) {
                if (StringUtil.isNotEmpty(sql)) {
                    try {
                        pcServiceTemplate.executeSql(sql);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        int startRow = 4;
        if ("1".equals(template.getStr("DATATEMPLATE_SM_CODE"))) {
            startRow = 5;
        }
        if (sheet.getLastRowNum() + 1 < startRow) {
            failure++;
            failures.add("此表格无导入数据！请检查表格");
        }

        List<DynaBean> importDataList = new ArrayList<>();
        List<DynaBean> updateDataList = new ArrayList<>();
        //loop start
        try {
            for (int row = startRow; row <= (sheet.getLastRowNum() + 1); row++) {
                HSSFRow rowObj = sheet.getRow(row - 1);
                DynaBean dynaBean = new DynaBean(template.getStr("DATATEMPLATE_TABLECODE"), true);
                if (rowObj == null) continue;
                boolean btError = false;
                for (int col = 0; col < columns.size(); col++) {
                    DynaBean column = columns.get(col);
                    String fieldCode = column.getStr("DATAFIELD_CODE");
                    String xtype = column.getStr("DATAFIELD_XTYPE");
                    nowCell = getCell(sheet, col + 2, row);
                    String defaultValue = column.getStr("DATAFIELD_VALUE");
                    String val = "";
//				if(nowCell==null){
//					if(StringUtil.isNotEmpty(defaultValue)){
//						dynaBean.set(fieldCode, StringUtil.codeToValue(defaultValue));
//					}
//					continue;
//				}
                    if (nowCell != null) {
                        val = getCellStringValue(nowCell);
                        if ("datetimefield".equals(xtype) || "datefield".equals(xtype)) {
                            if (StringUtil.isNotEmpty(val)) {
                                Date d = DateUtils.getDate(val, DateUtils.DAFAULT_DATETIME_FORMAT);
                                if (d == null) {
                                    DateUtils.getDate(val, DateUtils.DAFAULT_DATE_FORMAT);
                                }
                                if (d != null) {
                                    if ("datetimefield".equals(xtype)) {
                                        val = DateUtils.formatDateTime(d);
                                    } else if ("datefield".equals(xtype)) {
                                        val = DateUtils.formatDate(d);
                                    }
                                }
                            }
                        }
                    }
                    if (StringUtil.isNotEmpty(defaultValue)) {
                        defaultValue = StringUtil.codeToValue(defaultValue);
                    }
                    if (StringUtil.isEmpty(val)) {
                        val = defaultValue;
                    }
                    if ("1".equals(column.getStr("DATAFIELD_EMPTY"))) {
                        if (StringUtil.isEmpty(val)) {
                            btError = true;
                            failure++;
                            failures.add("第" + row + "行：【" + column.getStr("DATAFIELD_NAME") + "】列为必填项。");
                            break;
                        }
                    }
                    if (StringUtil.isNotEmpty(val) && "1".equals(column.getStr("DATAFIELD_QCKXJS"))) {
                        if (val.indexOf("E") != 1) {
//						 DecimalFormat df = new DecimalFormat("0");
//						 val = df.format(val);
                            BigDecimal b = new BigDecimal(val);
                            val = b.toPlainString();
                        }
                    }
                    if ("textfield".equals(xtype)) {
                        dynaBean.set(fieldCode, val);
                        //下拉框
                    } else if ("cbbfield".equals(xtype)) {
                        String configInfo = column.getStr("DATAFIELD_CONFIGINFO", "");
                        if (StringUtil.isNotEmpty(configInfo)) {
                            //获取字典数据
                            String[] configs = configInfo.split(",");
                            String ddCode = configs[0];
                            List<DictionaryItemVo> dicItems = dicInfos.get(ddCode);
                            if (dicItems != null && StringUtil.isNotEmpty(val)) {
                                //找到字典项
                                DictionaryItemVo dicItem = null;
                                for (DictionaryItemVo item : dicItems) {
                                    if (val.equals(item.getText())) {
                                        dicItem = item;
                                        break;
                                    }
                                }
                                if (dicItem != null) {
                                    String[] configFs = null;
                                    String[] configDs = null;
                                    if (configs.length > 2) {
                                        configFs = configs[1].split("~");
                                        configDs = configs[2].split("~");
                                        for (int i = 0; i < configFs.length; i++) {
                                            String fc = configFs[i];
                                            String dc = configDs[i];
                                            if ("text".equals(dc)) {
                                                dynaBean.set(fc, dicItem.getText());
                                            } else if ("code".equals(dc)) {
                                                dynaBean.set(fc, dicItem.getCode());
                                            } else if ("id".equals(dc)) {
                                                dynaBean.set(fc, dicItem.getId());
                                            }
                                        }
                                    } else {
                                        dynaBean.set(fieldCode, dicItem.getCode());
                                    }
                                } else {
                                    dynaBean.set(fieldCode, val);
                                }
                            } else {
                                dynaBean.set(fieldCode, val);
                            }
                        }
                    } else {
                        dynaBean.set(fieldCode, val);
                    }
                }
                for (DynaBean field : allFields) {
                    if ("1".equals(field.getStr("DATAFIELD_HIDDEN"))) {
                        String fieldCode = field.getStr("DATAFIELD_CODE");
                        String xtype = field.getStr("DATAFIELD_XTYPE");
                        String defaultValue = field.getStr("DATAFIELD_VALUE");
                        if (StringUtil.isNotEmpty(dynaBean.getStr(fieldCode))) continue;
                        if (ArrayUtils.contains(new String[]{"textfield", "cbbfield"}, xtype)) {
                            if (StringUtil.isNotEmpty(defaultValue)) {
                                field.set(fieldCode, StringUtil.codeToValue(defaultValue));
                            }
                        }
                    }
                }
                if (btError) {
                    continue;
                }
                if ("1".equals(template.getStr("DATATEMPLATE_ZDXTFIELD"))) {
                    serviceTemplate.buildModelCreateInfo(dynaBean);
                }
                String beforeClass = template.getStr("DATATEMPLATE_CZQLM");
                String beforeMethod = template.getStr("DATATEMPLATE_CZQFF");
                dynaBean.set("NOW_ROW", row);
                dynaBean.set("params", params);

                //检测数据是否超过数据库长度
                try {
                    HashMap checkMap = checkValuesLength(dynaBean);
                    Boolean isSussess = (Boolean) checkMap.get("IsSussess");
                    if (!isSussess) {
                        failures.add((String) checkMap.get("errMsg"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    failures.add("未知错误！");
                }

                if (StringUtil.isNotEmpty(beforeClass) && StringUtil.isNotEmpty(beforeMethod)) {
                    //处理前执行类
                    Object bean = SpringContextHolder.getBean(beforeClass);
                    dynaBean.set("importDataList", importDataList);
                    dynaBean.set("updateDataList", updateDataList);
                    dynaBean = (DynaBean) ReflectionUtils.getInstance().invokeMethod(bean, beforeMethod, new Object[]{dynaBean});
                    if (dynaBean == null) {
//						returnObj.put("error", "操作前方法必须返回DynaBean数据对象");
                        returnObj.put("error", "请确认导入模板主题是否正确。");
                        return;
                    } else {
                        if (dynaBean.containsKey("error")) {
                            System.out.println("error:" + dynaBean.getStr("error", ""));//
                            failures.add(dynaBean.getStr("error", "第" + row + "行：错误;"));//
                            failure++;
                        } else {
                            if ("1".equals(template.getStr("DATATEMPLATE_XGCZ"))) {//有修改操作时
                                String fieldCodes = template.getStr("DATATEMPLATE_WYZDBM", "");
                                if (StringUtil.isNotEmpty(fieldCodes)) {
                                    String[] fieldArray = fieldCodes.split(",");
                                    String[] unSqls = new String[fieldArray.length];
                                    boolean yxdata = false;
                                    for (int i = 0; i < fieldArray.length; i++) {
                                        unSqls[i] = " " + fieldArray[i] + "='" + dynaBean.getStr(fieldArray[i]) + "' ";
                                        if (StringUtil.isNotEmpty(dynaBean.getStr(fieldArray[i]))) {
                                            yxdata = true;
                                        }
                                    }
                                    if (!yxdata) {
//								failure++;
//								failures.add("第"+row+"行：唯一字段的值为空。");
                                        continue;
                                    }
                                    List<DynaBean> lists = serviceTemplate.selectList(template.getStr("DATATEMPLATE_TABLECODE"), " AND " + StringUtil.buildSplitString(unSqls, " AND "));
                                    if (lists.size() == 1) {
                                        dynaBean.set(dynaBean.getStr(BeanUtils.KEY_PK_CODE), lists.get(0).getStr(dynaBean.getStr(BeanUtils.KEY_PK_CODE)));
                                        serviceTemplate.buildModelModifyInfo(dynaBean);
                                        updateDataList.add(dynaBean);
//										serviceTemplate.update(dynaBean);
                                    } else if (lists.size() > 1) {
                                        failure++;
                                        failures.add("第" + row + "行：数据根据唯一字段查询出重复数据。");
                                        continue;
                                    } else {
                                        importDataList.add(dynaBean);
                                    }
                                }
                            } else {//无修改操作时直接添加
                                importDataList.add(dynaBean);
                            }
                        }
                    }
                }
                String afterClass = template.getStr("DATATEMPLATE_CZHLM");
                String afterMethod = template.getStr("DATATEMPLATE_CZHFF");
                if (StringUtil.isNotEmpty(afterClass) && StringUtil.isNotEmpty(afterMethod)) {
                    Object bean = SpringContextHolder.getBean(afterClass);
                    DynaBean result = (DynaBean) ReflectionUtils.getInstance().invokeMethod(bean, afterMethod, new Object[]{dynaBean});
                    if (result == null) {
                        returnObj.put("error", "操作后方法必须返回DynaBean数据对象");
//								returnObj.put("error", "请确认导入模板主题是否正确。");
                        return;
                    } else {
                        if (dynaBean.containsKey("error")) {
                            returnObj.put("error", dynaBean.getStr("error", ""));
                            return;
                        }
                    }
                }
            }
        } catch (PCExcuteException e) {
            e.printStackTrace();
            failure++;
            failures.add("未知错误");
        }
        //loop ending

        //重复性校验
//		importDataList


        if (failure == 0) {
            isInsert = true;
        }
        //校验没有问题，启动更新操作
        if (isInsert) {
            try {
                lxCallBack(importDataList);//类型回调方法

                //批量插入数据
                serviceTemplate.insert(importDataList);
                //批量更新数据
                for (DynaBean dynaBean : updateDataList) {
                    serviceTemplate.update(dynaBean);
                }
            } catch (Exception e) {
                e.printStackTrace();
                returnObj.put("msg", "插入失败，请重新操作");
                return;
            }
            returnObj.put("msgType", "0");
            returnObj.put("msg", "数据导入成功!");
//			returnObj.put("num", num);
        } else {
            String errors = "<p>导入失败，以下是导入错误信息。</p>";
            errors += ("<p>" + StringUtil.buildSplitString(ArrayUtils.getArray(failures), "</p><p>") + "</p>");
            returnObj.put("msg", errors);
            returnObj.put("msgType", "1");
        }

        //执行后置sql
        sqlStr = template.getStr("DATATEMPLATE_AFTERSQL", "");
        if (StringUtil.isNotEmpty(sqlStr)) {
            String[] sqls = sqlStr.split(";");
            for (String sql : sqls) {
                if (StringUtil.isNotEmpty(sql)) {
                    try {
                        pcServiceTemplate.executeSql(sql);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * 多字段组合重复校验
     *
     * @param importDataList
     */
    private void checkRepetitiveData(List<DynaBean> importDataList) {
        for (int i = 0; i < importDataList.size(); i++) {
            HashMap map = new HashMap();
            String columnList = importDataList.get(i).getStr("BS");//获取列集合
            String[] column = columnList.split(",");//获取需要校验的列名
            List<String> values = new ArrayList();
            List<String> littleValues = new ArrayList();
            for (String c : column) {
//                map.put(c,importDataList.get(i).getStr(c));
                values.add(importDataList.get(i).getStr(c));
            }
            for (int j = 0; j + 1 < importDataList.size(); j++) {
                for (String s : column) {
                    littleValues.add(importDataList.get(j).getStr(s));
                }
                if (values.size() != littleValues.size()) {
                    //集合数量不相等
                    continue;
                } else if (values.containsAll(littleValues)) {
                    //相同
//                    map.put("msg","")
                }

            }
        }
    }


    /**
     * 数据类型回调
     *
     * @param importDataList
     */
    private void lxCallBack(List<DynaBean> importDataList) {
        HashMap<String, Integer> map = new HashMap();
        //产品条码导入，已生成数量回调
        for (DynaBean cpInsertBean : importDataList) {
            if (cpInsertBean.containsKey("LX")) {
                //若为空，则不进行操作
                if (cpInsertBean.getStr("LX").equals("CP")) {//获取带有产品条码标识的数据
                    String id = cpInsertBean.getStr("JGMES_BARCODE_TMSCJL_ID");//获取id
                    if (map.containsKey(id)) {
                        map.put(id, map.get(id).intValue() + 1);//若id存在，则加1
                    } else {
                        map.put(id, new Integer(1));//若不存在，则设置1
                    }
                }
            }
        }
        if (map.size() != 0) {//若不为空，则进行数量回写操作
            Iterator<String> keys = map.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                Integer num = map.get(key).intValue();
                System.out.print(key + ":" + map.get(key).intValue() + ", ");
                String yscslSql = "update JGMES_BARCODE_TMSCJL set TMSCJL_YSCSL=TMSCJL_YSCSL+" + num + " where JGMES_BARCODE_TMSCJL_ID='" + key + "'";
                pcServiceTemplate.executeSql(yscslSql);
            }
        }
    }


    /**
     * 检查DynaBean中Values值在数据库中是否符合长度
     *
     * @param dynaBean
     * @param
     * @return
     */
    private HashMap checkValuesLength(DynaBean dynaBean) {
        DynaBean copy = dynaBean.clone();
        HashMap rMap = new HashMap();
        boolean IsSussess = true;
        try {
            String tableCode = (String) dynaBean.get(BeanUtils.KEY_TABLE_CODE);
            String s = "";
            HashMap map = copy.getValues();
            Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
            String databaseName = null;//数据库名称
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                String key = entry.getKey();
                Object data = (Object) entry.getValue();
                //内容为空直接跳过
                if (data == null) {
                    continue;
                }
                String value = data.toString() + "";
                DruidDataSource druid = (DruidDataSource) SpringContextHolder.getBean("dataSource");
                databaseName = druid.getUrl();
                String[] urls = databaseName.split("\\?")[0].split("\\/");
                databaseName = urls[urls.length - 1];
                String sql = "select * from information_schema.columns where table_name = '" + tableCode + "' and TABLE_SCHEMA='" + databaseName + "'";
                List<DynaBean> tbBeanList = serviceTemplate.selectListBySql(sql);
                for (DynaBean tbBean : tbBeanList) {
                    String column_name = tbBean.getStr("COLUMN_NAME");
                    if (column_name.equals(key)) {
                        String COLUMN_TYPE = tbBean.getStr("COLUMN_TYPE");
                        String CHARACTER_SET_NAME = tbBean.getStr("CHARACTER_SET_NAME");
                        Integer character_maximum_length = 0;
                        Integer value_length = 0;
                        String DATA_TYPE = tbBean.getStr("DATA_TYPE");
                        if (DATA_TYPE.equals("int")) {
                            character_maximum_length = 11;
                            value_length = value.length();
                        } else {
                            character_maximum_length = Integer.parseInt(tbBean.getStr("CHARACTER_MAXIMUM_LENGTH"));
                            value_length = value.getBytes(CHARACTER_SET_NAME).length;
                        }
                        String COLUMN_COMMENT = tbBean.getStr("COLUMN_COMMENT");
                        String NOW_ROW = ((Object) map.get("NOW_ROW")).toString();
//						System.out.println(NOW_ROW);
                        if (value_length > character_maximum_length) {
                            s += "表格第" + NOW_ROW + "行的" + COLUMN_COMMENT + "内容过长。" + "</br>";
                            IsSussess = false;
                        }
                        break;
                    }
                }
            }
            rMap.put("errMsg", s);
        } catch (Exception e) {
            e.printStackTrace();
            rMap.put("errMsg", e.toString());
            IsSussess = false;
        }
        rMap.put("IsSussess", IsSussess);
        return rMap;
    }

    public HSSFCell getCell(HSSFSheet sheet, Integer colnumber, Integer rownumber) {
        HSSFRow row = sheet.getRow(rownumber - 1);
        if (row == null) {
            return null;
        }
        HSSFCell cell = row.getCell(Short.parseShort((colnumber - 1) + ""));
        return cell;
    }

    public String getCellStringValue(HSSFCell cell) {
        String cellValue = "";
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case HSSFCell.CELL_TYPE_STRING:// 字符串类型
                cellValue = cell.getStringCellValue();
                if (cellValue.trim().equals("") || cellValue.trim().length() <= 0)
                    cellValue = " ";
                break;
            case HSSFCell.CELL_TYPE_NUMERIC: // 数值类型
                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    Date d = cell.getDateCellValue();
                    if (d != null) {
                        cellValue = DateUtils.formatDate(d, DateUtils.DAFAULT_DATETIME_FORMAT);
                    } else {
                        cellValue = "";
                    }
                } else {
                    cellValue = String.valueOf(MathExtend.divide(
                            cell.getNumericCellValue(), 1, 2));
//				cellValue=cell.getNumericCellValue()+"";
                    if (cellValue.lastIndexOf(".0") != -1) {
                        cellValue = cellValue.substring(0, cellValue.length() - 2);
                    }
                }
                break;
            case HSSFCell.CELL_TYPE_FORMULA: // 公式
                cell.setCellType(HSSFCell.CELL_TYPE_STRING);
//			cellValue = String.valueOf(MathExtend.divide(
//					cell.getNumericCellValue(), 1, 2));
                cellValue = cell.getStringCellValue();
                break;
            case HSSFCell.CELL_TYPE_BLANK:
                cellValue = " ";
                break;
            case HSSFCell.CELL_TYPE_BOOLEAN:
                break;
            case HSSFCell.CELL_TYPE_ERROR:
                break;
            default:
                break;
        }
        return cellValue;
    }

    @Resource(name = "PCDynaServiceTemplate")
    public void setServiceTemplate(PCDynaServiceTemplate serviceTemplate) {
        this.serviceTemplate = serviceTemplate;
    }

    @Resource(name = "PCServiceTemplateImpl")
    public void setPcServiceTemplate(PCServiceTemplate pcServiceTemplate) {
        this.pcServiceTemplate = pcServiceTemplate;
    }

    @Resource(name = "dictionaryManager")
    public void setDictionaryManager(DictionaryManager dictionaryManager) {
        this.dictionaryManager = dictionaryManager;
    }

}