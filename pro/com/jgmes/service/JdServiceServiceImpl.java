package com.jgmes.service;

import com.je.core.service.PCDynaServiceTemplate;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.DynaBean;
import com.jgmes.util.FileUtil;
import com.jgmes.util.PubUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component("jdService")
public class JdServiceServiceImpl implements IJdService {

    private PCDynaServiceTemplate serviceTemplate;
    private final String TABLECODE = "CLIO_TEST_FILE";
    /**
     * 导出附件并将附件和前一步导出的excel一起压缩
     * @author lt
     * @since 20190328
     * @param excelRelativePath：要压缩的excel相对路径:/je/data/..../a.xls
     * @param zipParentFolder：放压缩文件的文件夹路径
     * @param whereSql：前端选择导出的数据类别(当前页、选中行或全部)
     * @param zipFileName：压缩后zip名称
     * @return 返回压缩包的全路径
     */
    @Override
    public String exportAttachAndZipExcelAndAttach(String excelRelativePath, String zipParentFolder, String whereSql,String zipFileName) {
        List<DynaBean> list=serviceTemplate.selectList(TABLECODE, whereSql);
        String projectRootPath = PubUtil.rootPath;
        String zipParentFolderRealPath = projectRootPath + "/" + zipParentFolder;
        for(int i=0;i<list.size();i++){
            DynaBean bean = list.get(i);
            if(bean != null){
                String fileNameAndPath = bean.getStr("FILE_SEVICE_FILE_ADRESS");
                String fileUploadTime = bean.getStr("FILE_UPLOAD_TIME");
                if(StringUtil.isNotEmpty(fileNameAndPath)) {
                    //jgmes_xtcs.sql
                    String fileName = fileNameAndPath.split("\\*/")[0];
                    //filePath=/JE/data/upload/201903/vKBcFbfdHDatiZkoVM2.sql
                    String filePath = fileNameAndPath.split(fileName + "\\*/")[1];
                   
                    //filePathFolder=D:/tomcat/webapp/root/JE/data/upload/201903
                    String filePathFolder = projectRootPath+"/"+filePath.substring(0,filePath.lastIndexOf("/"));
                    //filePathFileName=vKBcFbfdHDatiZkoVM2.sql
                    String filePathFileName = filePath.substring(filePath.lastIndexOf("/")+1,filePath.length());
                    //拷贝附件到zip压缩文件目录下
                    if(StringUtil.isNotEmpty(fileName) && StringUtil.isNotEmpty(filePath)){
                        int dIndex = fileName.lastIndexOf(".");
                        String lastFileName = "";
                        if(dIndex > 0) {
                            lastFileName = fileName.substring(0, dIndex) + "_" + PubUtil.getDate(fileUploadTime, "yyyy-MM-dd HH:mm:ss","yyyyMMddHHmmss") + fileName.substring(dIndex, fileName.length());
                        }else{
                            //无扩展名情况
                            lastFileName = fileName + "_" + PubUtil.getDate(fileUploadTime, "yyyy-MM-dd HH:mm:ss","yyyyMMddHHmmss");
                        }
                        FileUtil.copyFile(filePathFolder, filePathFileName, zipParentFolderRealPath, lastFileName);
                    }
                }
            }
        }
        //压缩zipParentFolderRealPath下面所有文件
        FileUtil.createZip(zipParentFolderRealPath, zipFileName);
        return zipParentFolder+"/"+zipFileName;
    }








    /*
    public void exp(String title,String expPath,String funcId,String tableCode,String styleType,String whereSql,String orderSql,
                    String queryType, String datasourceName, String procedureName, String queryParamStr,
                    String queryParamValueStr, String dbSql) throws Exception{
        List<DynaBean> datas= expData( tableCode, whereSql, orderSql,
                queryType,  datasourceName,  procedureName,  queryParamStr,
                queryParamValueStr, dbSql);
        if(datas.size()>65500){
            throw new Exception("数据条数大于65500,无法导出!");
        }

        //获取功能字段列配置
        List<DynaBean> columns=null;
        if("REPORT".equals(styleType)){
            columns=getReportColumns(funcId);
        }else if("TEMPLATE".equals(styleType)){
            List<DynaBean> template=serviceTemplate.selectList("JE_SYS_DATATEMPLATE", " AND DATATEMPLATE_FUNCCODE IN (SELECT FUNCINFO_FUNCCODE FROM JE_CORE_FUNCINFO WHERE JE_CORE_FUNCINFO_ID='"+funcId+"')");
            if(template!=null && template.size()>0){
                JSONObject obj=new JSONObject();
//				obj.put("datas", datas);
                obj.put("title", title);
                obj.put("expPath", expPath);
                obj.put("expData", true);
                dataImplManager.generateTemplate(template.get(0).getStr("JE_SYS_DATATEMPLATE_ID"),datas, obj);
            }else{
                throw new Exception("未找到导入模版，请查看!");
            }
            return;
        }else{
            columns=serviceTemplate.selectList("JE_CORE_VCOLUMNANDFIELD",
                    " AND ( RESOURCEFIELD_XTYPE NOT IN ( 'ckeditor', 'uxfilefield', 'uxfilesfield' ) OR RESOURCEFIELD_XTYPE IS NULL) " +
                            "AND RESOURCECOLUMN_FUNCINFO_ID='"+funcId+"' AND RESOURCECOLUMN_HIDDEN='0' AND RESOURCECOLUMN_XTYPE NOT IN ('actioncolumn') ORDER BY SY_ORDERINDEX","RESOURCECOLUMN_MORECOLUMNNAME,RESOURCECOLUMN_WIDTH,RESOURCECOLUMN_CODE,RESOURCECOLUMN_XTYPE,RESOURCECOLUMN_NAME,RESOURCEFIELD_XTYPE,RESOURCEFIELD_CONFIGINFO");
        }
        //封装字典数据
        Map<String, Map<String,String>> ddInfos=buildDicDatas(columns);
        //创建Excel操作对象
        FileOperate.delFile(jeFileManager.getBasePath(JEFileType.PLATFORM)+expPath);
        ExcelWriter writer = ExcelUtil.getWriter(jeFileManager.getBasePath(JEFileType.PLATFORM)+expPath);
        //样式
        StyleSet style = writer.getStyleSet();
        //大标题样式
        CellStyle titleCellStyle = writer.getWorkbook().createCellStyle();
        Font tFont = writer.createFont();
        tFont.setFontName("宋体");
        tFont.setFontHeightInPoints((short) 18);//字号
        titleCellStyle.setFont(tFont);
        titleCellStyle.setAlignment(HorizontalAlignment.CENTER);
        titleCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleCellStyle.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
        //设置列名样式
        CellStyle headCellStyle = style.getHeadCellStyle();
        Font headFont = writer.createFont();
        headFont.setFontName("宋体");
        headFont.setFontHeightInPoints((short) 12);//字号
        headCellStyle.setFont(headFont);
        headCellStyle.setWrapText(true);
        headCellStyle.setAlignment(HorizontalAlignment.CENTER);
        headCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headCellStyle.setBorderTop(BorderStyle.THIN);
        headCellStyle.setBorderLeft(BorderStyle.THIN);
        headCellStyle.setBorderRight(BorderStyle.THIN);
        headCellStyle.setBorderBottom(BorderStyle.THIN);
        //设置数据样式
        CellStyle cellStyle = style.getCellStyle();
        Font font = writer.createFont();
        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 10);//字号
        cellStyle.setFont(font);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        DataFormat format = writer.getWorkbook().createDataFormat();
        cellStyle.setDataFormat(format.getFormat("@"));//单元格默认设置文本
        //处理多表头
        Boolean haveMore=false;
        List<DynaBean> dataColumns=new ArrayList<DynaBean>();
        for(DynaBean moreColumn:columns){
            if(!"morecolumn".equals(moreColumn.getStr("RESOURCECOLUMN_XTYPE")) && !StringUtil.isNotEmpty(moreColumn.getStr("RESOURCECOLUMN_MORECOLUMNNAME"))){
                dataColumns.add(moreColumn);
                continue;
            }
            haveMore=true;
            String code=moreColumn.getStr("RESOURCECOLUMN_CODE");
            List<DynaBean> childrens=new ArrayList<DynaBean>();
            for(DynaBean dataColumn:columns){
                if(code.equals(dataColumn.getStr("RESOURCECOLUMN_MORECOLUMNNAME"))){
                    childrens.add(dataColumn);
                    dataColumns.add(dataColumn);
                }
            }
            moreColumn.set("children", childrens);
        }
        //大标题
        writer.merge(0, 0, 0, dataColumns.size()-1, title, true);
        Cell titleCell = writer.getOrCreateCell(0 , 0);
        titleCell.setCellStyle(titleCellStyle);
        writer.setRowHeight(0, 30);
        Integer startRow=2;
        //列名标题
        if(!haveMore){
            for(Integer i=0;i<dataColumns.size();i++){
                DynaBean column=dataColumns.get(i);
                //设置宽度
                Integer width = columnWidth(column);
                writer.setColumnWidth(i, width);
                writer.writeCellValue(i, 1, column.getStr("RESOURCECOLUMN_NAME"));

                //设置列名标题样式
                Cell c = writer.getOrCreateCell(i , 1);
                c.setCellStyle(headCellStyle);
            }
            //设置标题高度
            writer.setRowHeight(1, 18);
        }else{
            startRow=3;
            //设置标题
            Integer nowColumnIndex=0;
            Set<String> columnCodes=new HashSet<String>();
            for(Integer i=0;i<columns.size();i++){
                DynaBean moreColumn=columns.get(i);
                String columnCode=moreColumn.getStr("RESOURCECOLUMN_CODE");
                if(columnCodes.contains(columnCode))continue;
                if("morecolumn".equals(moreColumn.getStr("RESOURCECOLUMN_XTYPE"))){
                    List<DynaBean> childrens=(List<DynaBean>) moreColumn.get("children");
                    if(childrens.size()<=0){
                        continue;
                    }
                    writer.merge(1, 1, nowColumnIndex, nowColumnIndex+childrens.size()-1, moreColumn.getStr("RESOURCECOLUMN_NAME"), true);
                    //处理多表头下的列
                    for(Integer j=0;j<childrens.size();j++){
                        DynaBean column=childrens.get(j);
                        //设置宽度
                        Integer width = columnWidth(column);
                        writer.setColumnWidth(nowColumnIndex+j, width);
                        writer.writeCellValue(nowColumnIndex+j, 2, column.getStr("RESOURCECOLUMN_NAME"));

                        //设置列名标题样式
                        Cell c = writer.getOrCreateCell(nowColumnIndex+j , 2);
                        c.setCellStyle(headCellStyle);

                        columnCodes.add(column.getStr("RESOURCECOLUMN_CODE"));
                    }
                    nowColumnIndex+=childrens.size();
                }else{
                    if(StringUtil.isNotEmpty(moreColumn.getStr("RESOURCECOLUMN_MORECOLUMNNAME")))continue;
                    //设置宽度
                    Integer moreWidth = columnWidth(moreColumn);
                    writer.setColumnWidth(nowColumnIndex, moreWidth);
                    writer.merge(1, 2, nowColumnIndex, nowColumnIndex, moreColumn.getStr("RESOURCECOLUMN_NAME"), true);
                    nowColumnIndex++;
                }
                columnCodes.add(columnCode);
            }
            //标题高度
            writer.setRowHeight(1, 18);
            writer.setRowHeight(2, 18);
        }
        //填充数据
        for(Integer i=0;i<datas.size();i++){
            DynaBean dynaBean=datas.get(i);
            for(Integer j=0;j<dataColumns.size();j++){
                DynaBean column=dataColumns.get(j);
                if("rownumberer".equals(column.getStr("RESOURCECOLUMN_XTYPE"))){
                    writer.writeCellValue(j, startRow+i, i+1);
                    continue;
                }
                String xtype=column.getStr("RESOURCEFIELD_XTYPE");
                if(ArrayUtils.contains(new String[]{"rgroup","cgroup","cbbfield"}, xtype)){
                    writer.writeCellValue(j, startRow+i, getDdValue(ddInfos, column, dynaBean.getStr(column.getStr("RESOURCECOLUMN_CODE",""))));
                }else{
                    if(ArrayUtils.contains(new String[]{"numberfield"}, xtype)){
                        writer.writeCellValue(j, startRow+i, dynaBean.getDouble(column.getStr("RESOURCECOLUMN_CODE"),0));
                    }else{
                        writer.writeCellValue(j, startRow+i, dynaBean.getStr(column.getStr("RESOURCECOLUMN_CODE","")));
                    }
                }
            }
        }
        writer.close();
    }*/
    @Resource(
            name = "PCDynaServiceTemplate"
    )
    public void setServiceTemplate(PCDynaServiceTemplate serviceTemplate) {
        this.serviceTemplate = serviceTemplate;
    }

}
