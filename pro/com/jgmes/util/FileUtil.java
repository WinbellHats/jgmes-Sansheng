package com.jgmes.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.*;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**文件工具类
 * @author lt
 * @since 20190328
 */
public class FileUtil {
	private static final Log log = LogFactory.getLog(FileUtil.class);
	private static void zipFileList(List<File> files,ZipOutputStream outputStream) {
		if(files==null && files.size()<=0) {
			return;
		}
		for (int i = 0; i < files.size(); i++) {
			zipFile(files.get(i),outputStream);
		}
	}
	private static void zipFileArray(File[] files,ZipOutputStream outputStream) {
		if(files==null && files.length<=0) {
			return;
		}
		for (int i = 0; i < files.length; i++) {
			zipFile(files[i],outputStream);
		}
	}
	/**创建Zip文件
	 * @param zipFullPath,如c:/a.zip
	 * @param files：导出的源数据(可能只导出里面的部分列)
	 * @return
	 */
	public static File createZip(String zipFullPath, List<File> files) {
		//
		File file = null;
		FileOutputStream fous = null;
		ZipOutputStream zipOut = null;
		try {
			if(files == null || files.size() < 1){
				return null;
			}
			if(zipFullPath == null || zipFullPath.trim().length() == 0) {
				zipFullPath = PubUtil.getProjectPath("")+"/data/jg/file_output";
			}
			log.debug("createZip path:"+zipFullPath);
			File filePath = new File(zipFullPath);
			if(!filePath.exists()) {
				filePath.mkdirs();
			}
			log.debug("createZip filePath="+filePath);
			file = new File(zipFullPath);
			if(!file.exists()) {
				file.createNewFile();
			}
			log.debug("createZip file="+file);
			fous = new FileOutputStream(file);   
	        zipOut = new ZipOutputStream(fous);
			zipFileList(files, zipOut);
			log.debug("createZip zipFile end");
		} catch (Exception e) {
			log.error("创建excel出错："+e.toString());
		}finally {
			try {
				if(zipOut!=null) {
					zipOut.close();
				}
				if(fous!=null) {
					fous.close();
				}
			} catch (IOException e) {
				log.error(e.toString());
			}
		}
		return file;
	}
	/**创建Zip文件
	 * @param zipFullPathFolder,如c:/a
	 * @param zipFileName:xxx.zip
	 * @return
	 */
	public static File createZip(String zipFullPathFolder,String zipFileName) {
		//
		File file = null;
		FileOutputStream fous = null;
		ZipOutputStream zipOut = null;
		try {
			File folderFile = new File(zipFullPathFolder);
			File[] files = null;
			if(folderFile != null) {
				files = folderFile.listFiles();
			}
			if(files == null || files.length < 1){
				return null;
			}
			if(zipFullPathFolder == null || zipFullPathFolder.trim().length() == 0) {
				zipFullPathFolder = PubUtil.getProjectPath("")+"/data/jg/file_output";
			}
			log.debug("createZip path:"+zipFullPathFolder);
			File filePath = new File(zipFullPathFolder);
			if(!filePath.exists()) {
				filePath.mkdirs();
			}
			log.debug("createZip filePath="+filePath);
			file = new File(zipFullPathFolder+"/"+zipFileName);
			if(!file.exists()) {
				file.createNewFile();
			}
			log.debug("createZip file="+file);
			fous = new FileOutputStream(file);
	        zipOut = new ZipOutputStream(fous);
			zipFileArray(files, zipOut);
			log.debug("createZip zipFile end");
		} catch (Exception e) {
			log.error("创建zip出错："+e.toString());
		}finally {
			try {
				if(zipOut!=null) {
					zipOut.close();
				}
				if(fous!=null) {
					fous.close();
				}
			} catch (IOException e) {
				log.error(e.toString());
			}
		}
		return file;
	}
	/**
	 * 根据输入的文件与输出流对文件进行打包
	 * @param inputFile
	 * @param ouputStream
	 */
	private static void zipFile(File inputFile,ZipOutputStream ouputStream) {
		if(inputFile==null){
			return ;
		}
//    	ZipOutputStream ouputStream  = new ZipOutputStream(new FileOutputStream(file));
		try {
			if(inputFile.exists()) {
				if (inputFile.isFile()) {
					FileInputStream IN = new FileInputStream(inputFile);
					BufferedInputStream bins = new BufferedInputStream(IN, 512);
					ZipEntry entry = new ZipEntry(inputFile.getName());
					ouputStream.putNextEntry(entry);
					// 向压缩文件中输出数据
					int nNumber;
					byte[] buffer = new byte[512];
					while ((nNumber = bins.read(buffer)) != -1) {
						ouputStream.write(buffer, 0, nNumber);
					}
					// 关闭创建的流对象
					bins.close();
					IN.close();
				} else {
					try {
						File[] files = inputFile.listFiles();
						for (int i = 0; i < files.length; i++) {
							zipFile(files[i], ouputStream);
						}
					} catch (Exception e) {
						log.error(e.toString());
					}
				}
			}
		} catch (Exception e) {
			log.error(e.toString());
		}
	}
	/**创建xml文件
	 * @param index:索引，用于批量生成文件时区分文件先后顺序
	 * @param path:为空时默认为项目根目录下面的strongit_file_output文件夹下
	 * @param data：导出的源数据(可能只导出里面的部分列)
	 * @return
	 * @throws Exception 
	 */
	/*public static File createXml(int index, String path, List<Map<String, Object>> data) throws Exception {
		if(PubUtil.isNull(path)) {
			path = PubUtil.getProjectPath("")+"/strongit_file_output";
		}
		File filePath = new File(path);
		if(!filePath.exists()) {
			filePath.mkdirs();
		}
		log.debug("createXml filePath:"+filePath);
		File file = new File(path+"/"+PubUtil.getDateTime("yyyy-MM-dd HH-mm-ss")+" "+(index>0?"-"+index:PubUtil.genValidateCode6())+".xml");
		String xml = VelocityUtil.createCodeByModelAndDataMap(PubUtil.createMap("tagName","resource","data",data),"velocity/model/resourceXml.vm");
		log.debug("createXml xml:\n"+xml);
		createFile(file,xml);
		return file;
	}*/
	/**
	 * 创建文件并写入内容 
	 * @since 20180817
	 * 
	 * @param path
	 *            ：真实路径,如http://localhost:8080/pl/WEB-INF/attach/20141104
	 * @param extName
	 *            ：保存的文件扩展名,如txt
	 * @param fileName
	 *            ：保存的文件名,若为空，则默认创建一个不重复的数字文件名，如：88691415850108181
	 * @param contents
	 *            ：保存的文件内容
	 * @return
	 * @throws IOException
	 */
	public static File createFile(String path, String fileName, String extName,
			String contents) throws IOException {
		// 为防止文件名重复，生成特殊的文件名称
		Random random = new Random();
		if (fileName == null || fileName.trim().length() == 0) {
			fileName = "" + random.nextInt(10000) + System.currentTimeMillis();
		}
		// 如果文件夹不存在则创建
		File fDir = new File(path);
		if (!fDir.isDirectory()) {
			fDir.mkdir();
		}
		File f = new File(path + "/" + fileName + "." + extName);
		if (!f.exists()) {
			// f.mkdirs();
			f.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(contents.getBytes("utf-8"));
		fos.flush();
		fos.close();
		return f;
	}
	public static void createFile(File f,String contents) throws IOException {
		if(f == null) {
			return ;
		}
		log.debug("createFile f="+f);
		if (!f.exists()) {
			// f.mkdirs();
			f.createNewFile();
		}
		log.debug("createFile begin write");
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(contents.getBytes("utf-8"));
		log.debug("createFile end write");
		fos.flush();
		fos.close();
	}
	/**
	 * 判断字符是否为""或null
	 *
	 * @param str
	 * @return
	 */
	public static boolean isNull(Object str) {
		if ("".equals(str+"")) {
			return true;
		} else {
			return false;
		}
	}
	/**
	 * 判断字符是否为""或null
	 *
	 * @param str
	 * @return
	 */
	public static boolean isNotNull(Object str) {
		return !isNull(str);
	}
	public static boolean toBoolean(Object obj) {
		if (obj == null) {
			return false;
		} else {
			if (obj.toString().toLowerCase().equals("true")
					|| obj.toString().toLowerCase().equals("1")
					|| obj.toString().toLowerCase().equals("Y")) {
				return true;
			} else {
				return false;
			}
		}
	}
	/**
	 * 写入文件
	 * @param data
	 * @param filePath
	 * @return
	 */
	public static boolean write2File(byte[] data, String filePath) {
		try {
			// String path=getProjectPath(filePath);
			File f = new File(filePath);
			if (!f.exists()) {
				f.createNewFile();
			}
			FileOutputStream out = new FileOutputStream(f);
			out.write(data);
			out.flush();
			out.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 重命名文件
	 * @param pathFolder：文件父级路径
	 * @param oldName
	 * @param newName
	 */
	public static void renameFile(String pathFolder,String oldName,String newName){
		//新的文件名和以前文件名不同时,才有必要进行重命名
		if(isNotNull(oldName) && !oldName.equals(newName)){
			File oldfile=new File(pathFolder+"/"+oldName);
			File newfile=new File(pathFolder+"/"+newName);
			if(!oldfile.exists()){
				return;//重命名文件不存在
			}
			//若在该目录下已经有一个文件和新文件名相同，则不允许重命名
			if(newfile.exists()) {
				System.out.println(newName + "已经存在！");
			}else{
				oldfile.renameTo(newfile);
			}
		}else{
			System.out.println("新文件名和旧文件名相同...");
		}
	}
	/**
	 * 复制文件
	 * @param fromPathFolder,如:d://fromPath
	 * @param fromFileName,如a.txt notNull
	 * @param toPathFolder,如:d://toPath
	 * @param toFileName 复制后的文件名
	 */
	public static void copyFile(String fromPathFolder,String fromFileName,String toPathFolder,String toFileName){
		if(isNull(fromPathFolder)||isNull(fromFileName)||isNull(toPathFolder)){
			return;
		}
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream(new File(fromPathFolder+"/"+fromFileName));
			File toFile = new File(toPathFolder+"/"+toFileName);
			out = new FileOutputStream(toFile);
			byte[] b = new byte[in.available()];
			int res = 0;
			while((res=in.read(b))!=-1){
				out.write(b,0,res);
			}
//			if(!isNull(toFileName)){
				//renameFile(toPathFolder,toFile.getName(),toFileName);
//			}
		}catch(Exception e){
			System.out.println("copyFile error:"+e);
		}finally {
			if(in != null){
				try {
					in.close();
					if(out != null){
						out.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
