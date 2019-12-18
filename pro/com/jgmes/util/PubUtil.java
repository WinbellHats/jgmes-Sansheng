package com.jgmes.util;

import com.alibaba.druid.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author kater
 */
public class PubUtil {
    protected static final Log logger = LogFactory.getLog(PubUtil.class);
    //项目根路径
    public static String rootPath = "";
    private static Random random;
    static {
        random = new Random();
        String classPath = PubUtil.class.getClassLoader().getResource("/")
                .getPath();
        // windows下
        if ("\\".equals(File.separator)) {
            rootPath = classPath.substring(1,
                    classPath.indexOf("/WEB-INF/classes"));
            rootPath = rootPath.replace("/", "\\");
        }
        // linux下
        if ("/".equals(File.separator)) {
            rootPath = classPath.substring(0,
                    classPath.indexOf("/WEB-INF/classes"));
            rootPath = rootPath.replace("\\", "/");
        }
    }
	/**
              * 获取当前系统日期，格式：yyyy-MM-dd HH:mm:ss
     *
     * @return
     */
    public static String getDateTime(String format) {
        Date d = new Date();
        if (format == null || format.length() == 0) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sFormat = new SimpleDateFormat(format);
        return sFormat.format(d);
    }
    /**
     * 获取当前系统日期，格式：yyyy-MM-dd HH:mm:ss
     *
     * @return
     */
    public static String getDateTime() {
        Date d = new Date();
        SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sFormat.format(d);
    }

    /**
     * 获取当前系统日期，格式：自定义 yyyy-MM-dd HH:mm:ss
     *
     * @return
     */
    public static String getDate(String format) {
        Date d = new Date();
        SimpleDateFormat sFormat = new SimpleDateFormat(format);
        return sFormat.format(d);
    }

    /**
     * 获取指定格式日期，格式：自定义
     *
     * @return
     */
    public static String getDate(Date d, String format) {
        SimpleDateFormat sFormat = new SimpleDateFormat(format);
        return sFormat.format(d);
    }
    /**
     *
     * @param d
     * @param orgiFormat：原始格式，必要与d一致
     * @param toFormat：转换后的格式
     * @returndataImplAction
     */
    public static String getDate(String d,String orgiFormat, String toFormat) {
        SimpleDateFormat sFormat = new SimpleDateFormat(orgiFormat);
        Date date = null;
        try {
            date = sFormat.parse(d);
        } catch (ParseException e) {
            System.out.println("convert date format error");
        }
        SimpleDateFormat toSdf = new SimpleDateFormat(toFormat);
        return toSdf.format(date);
    }
	public static String getIpAddr(HttpServletRequest request) {
		if (null == request) {
			return null;
		}
		String proxs[] = { "X-Forwarded-For", "Proxy-Client-IP",
				"WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR" ,"x-real-ip" };
		String ip = null;

		for (String prox : proxs) {
			ip = request.getHeader(prox);
			if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
				continue;
			} else {
				break;
			}
		}
		if (StringUtils.isEmpty(ip)) {
			return request.getRemoteAddr();
		}
		return ip;
	}
	/**
     * 构建过滤Map
     *
     * @param nvs 名称和值得组合
     * @return
     */
    public static Map<String, Object> createFilterMap(Object... nvs) {
        Map<String, Object> ex = new HashMap<String, Object>();
        if (nvs != null) {
            for (int i = 0; i < nvs.length; i += 2) {
                ex.put(nvs[i].toString(), nvs[i + 1]);
            }
        }
        return ex;
    }
    /**
     * 返回正确rstMap对象
     *
     * @return
     */
    public static Map<String, Object> addRst() {
        Map<String, Object> r = new HashMap<String, Object>();
        r.put("rst", "ok");
        return r;
    }
    /**
              * 返回错误rstMap对象
     *
     * @return
     */
    public static Map<String, Object> addErrRst(String errMsg) {
        Map<String, Object> r = new HashMap<String, Object>();
        r.put("errMsg", errMsg);
        return r;
    }
    public static boolean checkRst(Map<String, Object> rst) {
        if(rst == null || rst.size() == 0){
            return false;
        }
        return "OK".equalsIgnoreCase(rst.get("rst")+"");
    }
    public static String getRstErrMsg(Map<String, Object> rst) {
        if(rst == null || rst.size() == 0){
            return "";
        }
        return rst.get("errMsg")+"";
    }
    public static boolean isListNotNull(List<Map<String, Object>> list) {
        if (list != null && list.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isMapNotNull(Map<String, Object> map) {
        if (map != null && map.size() > 0) {
            return true;
        } else {
            return false;
        }
    }
    /**
     * 输出文件到浏览器
     * @param response
     * @param filePath:文件全路径,如：c:/a.txt
     * @param fileName:输出到浏览器下载的文件名
     */
    public static void outPutFile(HttpServletResponse response,
                                  String filePath, String fileName) {
        // 直接输出文件数据.
        InputStream fin = null;
        OutputStream outp = null;
        try {

            response.reset();// 可以加也可以不加
            response.setContentType("application/x-download");
            // 设置为下载application/force-download
            response.setContentType("application/force-download");
            response.addHeader("Content-Disposition", "attachment;filename="
                    + fileName);
            outp = response.getOutputStream();
            java.io.File f = new java.io.File(filePath);
            fin = new FileInputStream(f);

            byte[] b = new byte[1024];
            int i = 0;

            while ((i = fin.read(b)) > 0) {
                outp.write(b, 0, i);
            }
            //
            outp.flush();
            outp.close();
            outp = null;
            response.flushBuffer();
            fin.close();
            // 删除临时文件

        } catch (Exception e) {
            logger.debug(e);

        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    logger.debug(e);
                }
                fin = null;
            }
        }
    }
    /**
     * 获取项目路径,例如"c:\aaa"
     *
     * @param path
     * @return
     */
    public static String getProjectPath(String path) {
        String pp = "";
        // return projectPath + "\\" + path;
        if ("\\".equals(File.separator)) {
            pp = rootPath + "\\" + path;
        } else if ("/".equals(File.separator)) {
            pp = rootPath + "/" + path;
        }
        return pp;
    }
}
