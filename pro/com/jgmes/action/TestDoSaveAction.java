package com.jgmes.action;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.codehaus.jettison.json.JSONArray;
import org.python.antlr.PythonParser.return_stmt_return;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.je.core.action.DynaAction;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.DynaBean;
import com.je.phone.vo.DsInfoVo;
import com.je.rbac.model.EndUser;
import com.sun.org.apache.bcel.internal.generic.NEW;
import com.zeroturnaround.javarebel.iF;
import com.je.core.facade.extjs.JsonBuilder;
import com.je.core.security.SecurityUserHolder;


/**
 * 
 * @author fm
 * @version 2018-12-17 21:20:50
 * @see /jgmes/testDoSaveAction!doSaveLogin.action
 */
@Component("testDoSaveAction")
@Scope("prototype")
public class TestDoSaveAction  extends DynaAction{
	/**
     * 将cookie封装到Map里面
     * @param request
     * @return
     */
    private static Map<String, Cookie> ReadCookieMap(HttpServletRequest request) {
        Map<String, Cookie> cookieMap = new HashMap<String, Cookie>();
        Cookie[] cookies = request.getCookies();
        if (null != cookies) {
            for (Cookie cookie : cookies) {
                cookieMap.put(cookie.getName(), cookie);
            }
        }
        return cookieMap;
    }
    /**
     * 根据名字获取cookie
     *
     * @param request
     * @param name
     *            cookie名字
     * @return
     */
    private static String getCookieByName(HttpServletRequest request, String name) {
        Map<String, Cookie> cookieMap = ReadCookieMap(request);
        if (cookieMap.containsKey(name)) {
            Cookie cookie = (Cookie) cookieMap.get(name);
            return cookie.getValue();
        } else {
            return null;
        }
    }

	
	public void doSaveLogin() {
		String mac = request.getParameter("mac");
		String ip = request.getParameter("ip");
		String userCode = request.getParameter("userCode");//非必填，为空则获取当前登陆用户
		String userName = request.getParameter("userName");//非必填，为空则获取当前登陆用户
		
		if (userCode==null || userCode.isEmpty()) {
			EndUser user = loginUser;//SecurityUserHolder.getCurrentUser();
			userCode =getCookieByName(request,"loginUserCode");//user.getUserCode();
			userName = user.getUsername();
		}
		if (userName==null || userName.isEmpty()) {
			EndUser user = loginUser;//SecurityUserHolder.getUserByCode(userCode);
			userName = user.getUsername();
		}
		if (mac==null || mac.isEmpty()) {
			toWrite(jsonBuilder.returnFailureJson("MAC地址不能为空！"));
//			return;
		}
		DynaBean gwj = serviceTemplate.selectOne("JGMES_EQ_STATIONMACHINE", " and STATIONMACHINE_MACDZ='"+mac+"'");
		DynaBean dlgl = new DynaBean("JGMES_PB_DLGL", true);
		dlgl.set("DLGL_GWJBM", gwj.getStr("STATIONMACHINE_GWJBM"));
		dlgl.set("DLGL_GWJMC", gwj.getStr("STATIONMACHINE_GWJMC"));
		dlgl.set("DLGL_MACDZ", gwj.getStr("STATIONMACHINE_MACDZ"));
		dlgl.set("DLGL_IPDZ", ip);
		dlgl.set("DLGL_GWBH", gwj.getStr("STATIONMACHINE_GWBH"));
		dlgl.set("DLGL_GWMC", gwj.getStr("STATIONMACHINE_GWMC"));
		dlgl.set("DLGL_YHBM", userCode);
		dlgl.set("DLGL_YHMC", userName);
		dlgl.set("DLGL_DLSJ", new java.sql.Date(new Date().getTime()));
//		dlgl.set("DLGL_NO_CODE", gwj.getStr("BOM_MM_MC"));
//		dlgl.set("DLGL_NO_NAME", gwj.getStr("BOM_MM_MC"));
//		dlgl.set("DLGL_NO_ID", gwj.getStr("BOM_MM_MC"));
				
		
		
		serviceTemplate.buildModelCreateInfo(dlgl);
		serviceTemplate.insert(dlgl);
		toWrite(jsonBuilder.returnSuccessJson("\"添加成功\""));
	}
}
