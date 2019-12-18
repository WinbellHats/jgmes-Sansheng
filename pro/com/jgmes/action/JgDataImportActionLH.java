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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
/**
 * 
 * @author cj
 * @version 2019-05-27 09:08:45
 * @see /jgmes/jgDataImportActionLH!load.action
 */
@Component("jgDataImportActionLH")
@Scope("prototype")
public class JgDataImportActionLH extends DynaAction  {
	
	private static final long serialVersionUID = 1L;
	
	public void load(){
		toWrite("hello Action");
	}

	private PCDynaServiceTemplate serviceTemplate;
	private PCServiceTemplate pcServiceTemplate;
	private DictionaryManager dictionaryManager;



	private DataImplManager dataImplManager;
	/**
	 * ����ģ�沢�ҷ��ص�ַ
	 */
	public void generateTemplate(){
		JSONObject returnObj=new JSONObject();
		try {
			dataImplManager.generateTemplate(pkValue,new ArrayList<DynaBean>(), returnObj);
			toWrite(jsonBuilder.returnSuccessJson("\""+returnObj.getString("templatePath")+"\""));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			toWrite(jsonBuilder.returnFailureJson("\"����ģ�����!\""));
		}
	}
	/**
	 * �����ֶ�
	 */
	public void implFields(){
		String funcId=request.getParameter("funcId");
		pcServiceTemplate.executeSql(" DELETE FROM JE_SYS_DATAFIELD WHERE JE_SYS_DATATEMPLATE_ID='"+pkValue+"'");
		//AND RESOURCECOLUMN_XTYPE!='morecolumn'
		List<DynaBean> columnFields=serviceTemplate.selectList("JE_CORE_VCOLUMNANDFIELD"," AND RESOURCECOLUMN_FUNCINFO_ID='"+funcId+"'  ORDER BY SY_ORDERINDEX");
		for(DynaBean columnField:columnFields){
			DynaBean dataField=new DynaBean("JE_SYS_DATAFIELD",false);
			dataField.set(BeanUtils.KEY_PK_CODE, "JE_SYS_DATAFIELD_ID");
			dataField.set("SY_ORDERINDEX", columnField.getStr("SY_ORDERINDEX"));
			dataField.set("DATAFIELD_CODE", columnField.getStr("RESOURCECOLUMN_CODE"));
			dataField.set("DATAFIELD_NAME", columnField.getStr("RESOURCECOLUMN_NAME"));
			//������   RESOURCEFIELD_XTYPE     RESOURCECOLUMN_XTYPE
			if("morecolumn".equals(columnField.getStr("RESOURCECOLUMN_XTYPE"))){
				dataField.set("DATAFIELD_XTYPE", "morecolumn");
			}else if("rownumberer".equals(columnField.getStr("RESOURCECOLUMN_XTYPE"))){
				dataField.set("DATAFIELD_XTYPE", "rownumberer");
			}else if("uxcheckcolumn".equals(columnField.getStr("RESOURCECOLUMN_XTYPE"))){
				dataField.set("DATAFIELD_XTYPE", "cbbfield");
				dataField.set("DATAFIELD_CONFIGINFO", columnField.getStr("RESOURCEFIELD_CONFIGINFO"));
			}else if("datefield".equals(columnField.getStr("RESOURCECOLUMN_XTYPE")) || ("datetimefield".equals(columnField.getStr("RESOURCECOLUMN_XTYPE")))){
				dataField.set("DATAFIELD_XTYPE", columnField.getStr("RESOURCEFIELD_XTYPE"));
			}else{
				String xtype=columnField.getStr("RESOURCEFIELD_XTYPE");
				if(ArrayUtils.contains(new String[]{"cbbfield","rgroup","cgroup"},xtype)){
					dataField.set("DATAFIELD_XTYPE", "cbbfield");
					dataField.set("DATAFIELD_CONFIGINFO", columnField.getStr("RESOURCEFIELD_CONFIGINFO"));
				}else if(ArrayUtils.contains(new String[]{"treessfield","treessareafield"},xtype)){
					String[] configs=columnField.getStr("RESOURCEFIELD_CONFIGINFO","").split(",");
					if(configs.length>0){
						String ddCode=configs[0];
						DynaBean dic=serviceTemplate.selectOne("JE_CORE_DICTIONARY"," and DICTIONARY_DDTYPE IN ('LIST','TREE') AND DICTIONARY_DDCODE='"+ddCode+"'");
						if(dic!=null){
							dataField.set("DATAFIELD_XTYPE", "cbbfield");
							dataField.set("DATAFIELD_CONFIGINFO", columnField.getStr("RESOURCEFIELD_CONFIGINFO"));
						}else{
							dataField.set("DATAFIELD_XTYPE", "textfield");
						}
					}else{
						dataField.set("DATAFIELD_XTYPE", "textfield");
					}
				}else{
					dataField.set("DATAFIELD_XTYPE", "textfield");
				}
			}
			dataField.set("DATAFIELD_WIDTH", columnField.getStr("RESOURCECOLUMN_WIDTH"));
			dataField.set("DATAFIELD_VALUE", columnField.getStr("RESOURCEFIELD_VALUE"));
			dataField.set("DATAFIELD_SSDBT", columnField.getStr("RESOURCECOLUMN_MORECOLUMNNAME"));
			dataField.set("DATAFIELD_XLK", "0");
			dataField.set("DATAFIELD_EMPTY", "0");
			if("0".equals(columnField.getStr("RESOURCEFIELD_ALLOWBLANK"))){
				dataField.set("DATAFIELD_EMPTY", "1");
			}
			dataField.set("DATAFIELD_HIDDEN", columnField.getStr("RESOURCECOLUMN_HIDDEN"));
			dataField.set("JE_SYS_DATATEMPLATE_ID", pkValue);
			serviceTemplate.buildModelCreateInfo(dataField);
			serviceTemplate.insert(dataField);
		}
		toWrite(jsonBuilder.returnSuccessJson("'����ɹ�!'"));
	}
	public void clearHidden(){
		if(StringUtil.isNotEmpty(pkValue)){
			pcServiceTemplate.executeSql(" DELETE FROM JE_SYS_DATAFIELD WHERE JE_SYS_DATATEMPLATE_ID='"+pkValue+"' AND DATAFIELD_HIDDEN='1'");
			toWrite(jsonBuilder.returnSuccessJson("'�����ɹ�!'"));
		}else{
			toWrite(jsonBuilder.returnSuccessJson("'ɾ����������ϵ����Ա!'"));
		}
	}
	public void uploadFile(){
		System.out.println("uploadFile����");
		List<DynaBean> docs = (List<DynaBean>) dynaBean.get(BeanUtils.KEY_DOC_INFO);
		if(null != docs && 0 != docs.size()) {
			DynaBean doc = docs.iterator().next();
			dynaBeanTemplate.doSaveDocumentInfo(doc);
			String filePath=doc.getStr("DOCUMENT_ADDRESS");
			JSONObject returnObj=new JSONObject();
			JSONObject params=new JSONObject();
			String paramStr=request.getParameter("params");
			if(StringUtil.isNotEmpty(paramStr)){
				params=JSONObject.fromObject(paramStr);
			}
			returnObj.put("params", params);
			returnObj.put("pkValue", pkValue);
//			dataImplManager.implData(BaseAction.webrootAbsPath+filePath, returnObj);
			implData(BaseAction.webrootAbsPath+filePath, returnObj);
			FileOperate.delFile(BaseAction.webrootAbsPath+filePath);
			if(returnObj.containsKey("error")){
				toWrite(jsonBuilder.returnFailureJson("'"+returnObj.getString("error")+"'"));
			}else{
				toWrite(jsonBuilder.returnSuccessJson(returnObj.toString()));
			}
		}else{
			toWrite(jsonBuilder.returnFailureJson("'�ļ��ϴ�ʧ��!'"));
		}
	}
	public void applyFunc(){
		DynaBean funcInfo=serviceTemplate.selectOne("JE_CORE_FUNCINFO"," AND FUNCINFO_FUNCCODE='JE_SYS_DATATEMPLATE' AND FUNCINFO_NODEINFOTYPE='FUNC'","JE_CORE_FUNCINFO_ID,FUNCINFO_FUNCNAME,FUNCINFO_FUNCCODE");
		List<DynaBean> buttons=serviceTemplate.selectList("JE_CORE_RESOURCEBUTTON", " AND RESOURCEBUTTON_FUNCINFO_ID='"+funcInfo.getStr("JE_CORE_FUNCINFO_ID")+"' AND RESOURCEBUTTON_CODE IN ('downloadDataBtn','implDataBtn')");
		String funcId=request.getParameter("funcId");
		String templateId=request.getParameter("templateId");
		Long count=serviceTemplate.selectCount("JE_CORE_RESOURCEBUTTON", " AND RESOURCEBUTTON_FUNCINFO_ID='"+funcId+"' AND RESOURCEBUTTON_CODE IN ('downloadDataBtn','implDataBtn')");
//		if(count>0){
//			toWrite(jsonBuilder.returnFailureJson("'��������ģ�水ť������ɾ����Ӧ��!'"));
//			return;
//		}
		for(DynaBean button:buttons){

			button.set("RESOURCEBUTTON_FUNCINFO_ID", funcId);
			button.set("JE_CORE_RESOURCEBUTTON_ID", null);
			String jsListener=button.getStr("RESOURCEBUTTON_JSLISTENER","");
			jsListener=jsListener.replace("_JEREPLACEPKVALUE_", templateId);
			button.set("RESOURCEBUTTON_JSLISTENER", jsListener);
			button.set("RESOURCEBUTTON_DISABLED", "0");
			button.set("SY_ORDERINDEX", "0");
			serviceTemplate.buildModelCreateInfo(button);
			serviceTemplate.insert(button);
		}
		toWrite(jsonBuilder.returnSuccessJson("'Ӧ�óɹ�,��鿴���ܰ�ť��Ϣ!'"));
	}
	@Resource(name="dataImplManager")
	public void setDataImplManager(DataImplManager dataImplManager) {
		this.dataImplManager = dataImplManager;
	}

	//*****

	public void implData(String filePath,JSONObject returnObj) {
		System.out.println("implData������");
		// TODO Auto-generated method stub
		FileInputStream fis=null;
		POIFSFileSystem fs = null;
		HSSFWorkbook wb = null;
		JSONObject params=new JSONObject();
		int sheetIndex=0;
		if(returnObj.containsKey("sheet")){
			if(StringUtil.isNotEmpty(returnObj.get("sheet")+"")){
				sheetIndex=returnObj.getInt("sheet");
			}
		}
		if(returnObj.containsKey("params")){
			params=returnObj.getJSONObject("params");
		}
		try {
			// 1.�õ��ļ�����
			fis=new FileInputStream(filePath);
			fs = new POIFSFileSystem(fis);
			wb = new HSSFWorkbook(fs);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			returnObj.put("error", "�ļ���ȡ����!");
			return;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			returnObj.put("error", "�ļ���ȡ����!");
			return;
		}
		// �������
		HSSFSheet sheet = wb.getSheetAt(sheetIndex);
		HSSFCell nowCell=null;
		EndUser currentUser= SecurityUserHolder.getCurrentUser();
		Integer success=0;
		Integer failure=0;
		List<String> failures=new ArrayList<String>();
		//��ȡģ������
		nowCell=getCell(sheet, 1, 1);
		String templateId=getCellStringValue(nowCell);
		if(StringUtil.isEmpty(templateId)){
			returnObj.put("error", "δ����ģ����Ϣ������������ģ��...");
			return;
		}
		DynaBean template=serviceTemplate.selectOneByPk("JE_SYS_DATATEMPLATE", templateId);
		if(template==null){
			returnObj.put("error", "δ��ϵͳ�鵽ģ����Ϣ����鿴ϵͳģ������...");
			return;
		}
		if(!templateId.equals(returnObj.getString("pkValue"))){
			returnObj.put("error", "������Ϣ�뵱ǰģ�治��Ӧ������������ģ��...");
			return;
		}
		List<DynaBean> allFields=serviceTemplate.selectList("JE_SYS_DATAFIELD", " AND JE_SYS_DATATEMPLATE_ID='"+templateId+"' ORDER BY DATAFIELD_HIDDEN ASC,SY_ORDERINDEX ASC");
		List<DynaBean> fields=new ArrayList<DynaBean>();
		Map<String,DynaBean> fieldInfos=new HashMap<String,DynaBean>();
		Map<String,List<DictionaryItemVo>> dicInfos=new HashMap<String,List<DictionaryItemVo>>();
		for(DynaBean field:allFields){
			if("cbbfield".equals(field.getStr("DATAFIELD_XTYPE"))){
				String configInfo=field.getStr("DATAFIELD_CONFIGINFO","");
				if(StringUtil.isNotEmpty(configInfo)){
					//��ȡ�ֵ�����
					String ddCode=configInfo.split(",")[0];
					List<DictionaryItemVo> dicItems=dictionaryManager.getDicList(ddCode, new QueryInfo(), false);
					dicInfos.put(ddCode, dicItems);
				}
			}
			if(!"1".equals(field.getStr("DATAFIELD_HIDDEN"))){// &&
				fields.add(field);
				fieldInfos.put(field.getStr("DATAFIELD_NAME"), field);
			}
		}

		//���������ģ���еĵ�˳�򼯺�
		List<DynaBean> columns=new ArrayList<DynaBean>();
		for(int i=0;i<fields.size();i++){
			nowCell=getCell(sheet, 2+i, 3);
			if(nowCell==null){
				break;
			}
			String fieldName=getCellStringValue(nowCell);
			if(StringUtil.isEmpty(fieldName)){
				nowCell=getCell(sheet, 2+i, 2);
				fieldName=getCellStringValue(nowCell);
			}
			DynaBean field=fieldInfos.get(fieldName);
			if(field==null){
				returnObj.put("error", "ģ����⡾"+fieldName+"����ϵͳδ�ҵ�������������ģ��!");
				return;
			}
			columns.add(field);
		}
		if(columns.size()<=0){
			returnObj.put("error", "ģ����ⲻ���ϸ�ʽ������������ģ��!");
			return;
		}
		//ִ��ǰ��sql
		String sqlStr=template.getStr("DATATEMPLATE_BEFORESQL","");
		if(StringUtil.isNotEmpty(sqlStr)){
			String[] sqls=sqlStr.split(";");
			for(String sql:sqls){
				if(StringUtil.isNotEmpty(sql)){
					try{
						pcServiceTemplate.executeSql(sql);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		}
		int startRow=4;
		if("1".equals(template.getStr("DATATEMPLATE_SM_CODE"))){
			startRow=5;
		}
		if (sheet.getLastRowNum()+1<startRow){
			failure++;
			failures.add("�˱���޵������ݣ�������");
		}
		try {
			for(int row=startRow;row<=(sheet.getLastRowNum()+1);row++){
				HSSFRow rowObj = sheet.getRow(row - 1);
				DynaBean dynaBean=new DynaBean(template.getStr("DATATEMPLATE_TABLECODE"),true);
				if(rowObj==null)continue;
				boolean btError=false;
				for(int col=0;col<columns.size();col++){
					DynaBean column=columns.get(col);
					String fieldCode=column.getStr("DATAFIELD_CODE");
					String xtype=column.getStr("DATAFIELD_XTYPE");
					nowCell=getCell(sheet, col+2, row);
					String defaultValue=column.getStr("DATAFIELD_VALUE");
					String val="";
//				if(nowCell==null){
//					if(StringUtil.isNotEmpty(defaultValue)){
//						dynaBean.set(fieldCode, StringUtil.codeToValue(defaultValue));
//					}
//					continue;
//				}
					if(nowCell!=null){
						val=getCellStringValue(nowCell);
						if("datetimefield".equals(xtype) || "datefield".equals(xtype)){
							if(StringUtil.isNotEmpty(val)){
								Date d= DateUtils.getDate(val, DateUtils.DAFAULT_DATETIME_FORMAT);
								if(d==null){
									DateUtils.getDate(val, DateUtils.DAFAULT_DATE_FORMAT);
								}
								if(d!=null){
									if("datetimefield".equals(xtype)){
										val=DateUtils.formatDateTime(d);
									}else if("datefield".equals(xtype)){
										val=DateUtils.formatDate(d);
									}
								}
							}
						}
					}
					if(StringUtil.isNotEmpty(defaultValue)){
						defaultValue=StringUtil.codeToValue(defaultValue);
					}
					if(StringUtil.isEmpty(val)){
						val=defaultValue;
					}
					if("1".equals(column.getStr("DATAFIELD_EMPTY"))){
						if(StringUtil.isEmpty(val)){
							btError=true;
							failure++;
							failures.add("��"+row+"�У���"+column.getStr("DATAFIELD_NAME")+"����Ϊ�����");
							break;
						}
					}
					if(StringUtil.isNotEmpty(val) && "1".equals(column.getStr("DATAFIELD_QCKXJS"))){
						if(val.indexOf("E")!=1){
//						 DecimalFormat df = new DecimalFormat("0");
//						 val = df.format(val);
							BigDecimal b = new BigDecimal(val);
							val=b.toPlainString();
						}
					}
					if("textfield".equals(xtype)){
						dynaBean.set(fieldCode, val);
						//������
					}else if("cbbfield".equals(xtype)){
						String configInfo=column.getStr("DATAFIELD_CONFIGINFO","");
						if(StringUtil.isNotEmpty(configInfo)){
							//��ȡ�ֵ�����
							String[] configs=configInfo.split(",");
							String ddCode=configs[0];
							List<DictionaryItemVo> dicItems=dicInfos.get(ddCode);
							if(dicItems!=null && StringUtil.isNotEmpty(val)){
								//�ҵ��ֵ���
								DictionaryItemVo dicItem=null;
								for(DictionaryItemVo item:dicItems){
									if(val.equals(item.getText())){
										dicItem=item;
										break;
									}
								}
								if(dicItem!=null){
									String[] configFs=null;
									String[] configDs=null;
									if(configs.length>2){
										configFs=configs[1].split("~");
										configDs=configs[2].split("~");
										for(int i=0;i<configFs.length;i++){
											String fc=configFs[i];
											String dc=configDs[i];
											if("text".equals(dc)){
												dynaBean.set(fc,dicItem.getText());
											}else if("code".equals(dc)){
												dynaBean.set(fc, dicItem.getCode());
											}else if("id".equals(dc)){
												dynaBean.set(fc, dicItem.getId());
											}
										}
									}else{
										dynaBean.set(fieldCode, dicItem.getCode());
									}
								}else{
									dynaBean.set(fieldCode, val);
								}
							}else{
								dynaBean.set(fieldCode, val);
							}
						}
					}else{
						dynaBean.set(fieldCode, val);
					}
				}
				for(DynaBean field:allFields){
					if("1".equals(field.getStr("DATAFIELD_HIDDEN"))){
						String fieldCode=field.getStr("DATAFIELD_CODE");
						String xtype=field.getStr("DATAFIELD_XTYPE");
						String defaultValue=field.getStr("DATAFIELD_VALUE");
						if(StringUtil.isNotEmpty(dynaBean.getStr(fieldCode)))continue;
						if(ArrayUtils.contains(new String[]{"textfield","cbbfield"}, xtype)){
							if(StringUtil.isNotEmpty(defaultValue)){
								field.set(fieldCode, StringUtil.codeToValue(defaultValue));
							}
						}
					}
				}
				if(btError){
					continue;
				}
				if("1".equals(template.getStr("DATATEMPLATE_ZDXTFIELD"))){
					serviceTemplate.buildModelCreateInfo(dynaBean);
				}
				String beforeClass=template.getStr("DATATEMPLATE_CZQLM");
				String beforeMethod=template.getStr("DATATEMPLATE_CZQFF");
				dynaBean.set("NOW_ROW", row);
				dynaBean.set("params", params);
				if(StringUtil.isNotEmpty(beforeClass) && StringUtil.isNotEmpty(beforeMethod)){
					//����ǰִ����
					Object bean = SpringContextHolder.getBean(beforeClass);
					//��������Ƿ񳬹����ݿⳤ��
					try {
						HashMap checkMap = checkValuesLength(dynaBean);
						Boolean isSussess = (Boolean) checkMap.get("IsSussess");
						if (!isSussess){
							failures.add((String)checkMap.get("errMsg"));
						}
					} catch (Exception e) {
						e.printStackTrace();
						failures.add("δ֪����");
					}
					dynaBean=(DynaBean) ReflectionUtils.getInstance().invokeMethod(bean, beforeMethod, new Object[]{dynaBean});
//					if(dynaBean==null){
////						returnObj.put("error", "����ǰ�������뷵��DynaBean���ݶ���");
//						returnObj.put("error", "��ȷ�ϵ���ģ�������Ƿ���ȷ��");
//						return;
//					}else{
						if(dynaBean.containsKey("error")){
							System.out.println("error:"+dynaBean.getStr("error",""));//
							//returnObj.put("error", dynaBean.getStr("error",""));
							failures.add(dynaBean.getStr("error","��"+row+"�У�����;"));//
							failure++;
							//return;
						}
						else {
							if("1".equals(template.getStr("DATATEMPLATE_XGCZ"))){
								String fieldCodes=template.getStr("DATATEMPLATE_WYZDBM","");
								if(StringUtil.isNotEmpty(fieldCodes)){
									String[] fieldArray=fieldCodes.split(",");
									String[] unSqls=new String[fieldArray.length];
									boolean yxdata=false;
									for(int i=0;i<fieldArray.length;i++){
										unSqls[i]=" "+fieldArray[i]+"='"+dynaBean.getStr(fieldArray[i])+"' ";
										if(StringUtil.isNotEmpty(dynaBean.getStr(fieldArray[i]))){
											yxdata=true;
										}
									}
									if(!yxdata){
//								failure++;
//								failures.add("��"+row+"�У�Ψһ�ֶε�ֵΪ�ա�");
										continue;
									}
									List<DynaBean> lists=serviceTemplate.selectList(template.getStr("DATATEMPLATE_TABLECODE")," AND "+StringUtil.buildSplitString(unSqls," AND "));
									if(lists.size()==1){
										dynaBean.set(dynaBean.getStr(BeanUtils.KEY_PK_CODE), lists.get(0).getStr(dynaBean.getStr(BeanUtils.KEY_PK_CODE)));
										serviceTemplate.buildModelModifyInfo(dynaBean);
										serviceTemplate.update(dynaBean);
									}else if(lists.size()>1){
										failure++;
										failures.add("��"+row+"�У����ݸ���Ψһ�ֶβ�ѯ���ظ����ݡ�");
										continue;
									}else{
										serviceTemplate.insert(dynaBean);
								}
								}else{
									serviceTemplate.insert(dynaBean);
								}
							}else{
								serviceTemplate.insert(dynaBean);
							}
							//����ʱ�������child�����ӱ����ݣ�����е���
							DynaBean child = (DynaBean)dynaBean.get("child");
							if (child!=null){
								serviceTemplate.insert(child);
							}
							success++;
						}
//					}
				}else{
					serviceTemplate.insert(dynaBean);//��û��ִ��ǰ������Ҳ���в���
				}
				String afterClass=template.getStr("DATATEMPLATE_CZHLM");
				String afterMethod=template.getStr("DATATEMPLATE_CZHFF");
				if(StringUtil.isNotEmpty(afterClass) && StringUtil.isNotEmpty(afterMethod)){
					Object bean = SpringContextHolder.getBean(afterClass);
					DynaBean result=(DynaBean) ReflectionUtils.getInstance().invokeMethod(bean, afterMethod, new Object[]{dynaBean});
					if(result==null){
						returnObj.put("error", "�����󷽷����뷵��DynaBean���ݶ���");
						return;
					}else{
						if(dynaBean.containsKey("error")){
							returnObj.put("error", dynaBean.getStr("error",""));
							return;
						}
					}
				}
			}
		} catch (PCExcuteException e) {
			e.printStackTrace();
		}

		//ִ�к���sql
		sqlStr=template.getStr("DATATEMPLATE_AFTERSQL","");
		if(StringUtil.isNotEmpty(sqlStr)){
			String[] sqls=sqlStr.split(";");
			for(String sql:sqls){
				if(StringUtil.isNotEmpty(sql)){
					try{
						pcServiceTemplate.executeSql(sql);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		}
//		if("1".equals(template.getStr("DATATEMPLATE_BZCZ"))){
//			returnObj.put("msg", "���ݵ���ɹ�!");
//			returnObj.put("msgType", "0");
//		}else{
			if(failure>0){
				String errors="<p>����ɹ�"+success+"�����ݡ�</p><p>"+"����ʧ��"+failure+"�����ݡ�</p>";
				errors+=("<p>"+StringUtil.buildSplitString(ArrayUtils.getArray(failures), "</p><p>")+"</p>");
				returnObj.put("msg",errors);
				returnObj.put("msgType", "1");
			}else{
				returnObj.put("msgType", "0");
				returnObj.put("msg", "���ݵ���ɹ�!");
			}
//		}
		if(fis!=null){
			try {
				fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * ���DynaBean��Valuesֵ�Ƿ���ϳ���
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
            String databaseName = null;//���ݿ�����
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                String key = entry.getKey();
                Object data = (Object) entry.getValue();
                //����Ϊ��ֱ������
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
                        } else if(DATA_TYPE.equals("decimal")){
                        	character_maximum_length = 20;
                            value_length = value.length();
                        }else {
                            character_maximum_length = Integer.parseInt(tbBean.getStr("CHARACTER_MAXIMUM_LENGTH"));
                            value_length = value.getBytes(CHARACTER_SET_NAME).length;
                        }
                        String COLUMN_COMMENT = tbBean.getStr("COLUMN_COMMENT");
                        String NOW_ROW = ((Object) map.get("NOW_ROW")).toString();
//						System.out.println(NOW_ROW);
                        if (value_length > character_maximum_length) {
                            s += "����" + NOW_ROW + "�е�" + COLUMN_COMMENT + "���ݹ�����" + "</br>";
                            IsSussess = false;
                        }
                        break;
                    }
                }
            }
            System.out.println("���ݿ���" + databaseName);
            logger.error("���ݵ��롪" + databaseName + ";tiem:" + new Date());
            rMap.put("errMsg", s);
        } catch (Exception e) {
            e.printStackTrace();
            rMap.put("errMsg", e.toString());
            IsSussess = false;
        }
        rMap.put("IsSussess", IsSussess);
        return rMap;
    }

	public HSSFCell getCell(HSSFSheet sheet, Integer colnumber,Integer rownumber) {
		HSSFRow row = sheet.getRow(rownumber - 1);
		if(row==null){
			return null;
		}
		HSSFCell cell = row.getCell(Short.parseShort((colnumber - 1) + ""));
		return cell;
	}
	public String getCellStringValue(HSSFCell cell) {
		String cellValue = "";
		if(cell==null) return "";
		switch (cell.getCellType()) {
			case HSSFCell.CELL_TYPE_STRING:// �ַ�������
				cellValue = cell.getStringCellValue();
				if (cellValue.trim().equals("") || cellValue.trim().length() <= 0)
					cellValue = " ";
				break;
			case HSSFCell.CELL_TYPE_NUMERIC: // ��ֵ����
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
					if(cellValue.lastIndexOf(".0")!=-1){
						cellValue=cellValue.substring(0,cellValue.length()-2);
					}
				}
				break;
			case HSSFCell.CELL_TYPE_FORMULA: // ��ʽ
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);
//			cellValue = String.valueOf(MathExtend.divide(
//					cell.getNumericCellValue(), 1, 2));
				cellValue=cell.getStringCellValue();
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

	@Resource(name="PCDynaServiceTemplate")
	public void setServiceTemplate(PCDynaServiceTemplate serviceTemplate) {
		this.serviceTemplate = serviceTemplate;
	}
	@Resource(name="PCServiceTemplateImpl")
	public void setPcServiceTemplate(PCServiceTemplate pcServiceTemplate) {
		this.pcServiceTemplate = pcServiceTemplate;
	}
	@Resource(name="dictionaryManager")
	public void setDictionaryManager(DictionaryManager dictionaryManager) {
		this.dictionaryManager = dictionaryManager;
	}
	

	
}