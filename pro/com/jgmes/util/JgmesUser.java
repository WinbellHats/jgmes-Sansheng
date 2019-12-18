package com.jgmes.util;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.je.core.action.DynaAction;
import com.je.core.service.PCDynaBeanTemplate;
import com.je.core.service.PCDynaServiceTemplate;
import com.je.core.util.bean.DynaBean;
import com.zeroturnaround.javarebel.iF;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @author fm
 * @version 2018-12-21 11:20:50
 * @see /jgmes/jgmesUser!load.action
 */
@Component("jgmesUser")
@Scope("prototype")
public class JgmesUser  extends DynaAction {


	private HttpServletRequest REQUEST=null;
	private PCDynaServiceTemplate SERVICETEMPLATE=null;
	private String USERCODE =null;
	public JgmesUser(HttpServletRequest request,PCDynaServiceTemplate servicetemplate) {
		REQUEST = request;
		SERVICETEMPLATE = servicetemplate;
	}
	public JgmesUser(HttpServletRequest request,PCDynaServiceTemplate servicetemplate,String userCode) {
		REQUEST = request;
		SERVICETEMPLATE = servicetemplate;
		USERCODE =userCode;
	}
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

	/*
	 * 获取当前用户
	 */
	public String getCurrentUserCode() {
		if (USERCODE == null || USERCODE.isEmpty()) {
			USERCODE = getCookieByName(REQUEST,"loginUserCode");
		}
		return USERCODE;
	}
	
	/*
	 * 获取当前用户名称
	 */
	public String getCurrentUserName() {
		String userName="";
		DynaBean userDynaBean = getCurrentUser();
		if (userDynaBean != null) {
			userName = userDynaBean.getStr("USERNAME");
		}
		
		return userName;
	}
	/*
	 * 获取当前用户ID
	 */
	public String getCurrentUserID() {
		String userId="";
		DynaBean userDynaBean = getCurrentUser();
		if (userDynaBean != null) {
			userId = userDynaBean.getStr("USERID");
		}
		
		return userId;
	}
	
	/*
	 * 获取当前部门ID
	 */
	public String getCurrentDeptID() {
		String deptId="";
		DynaBean userDynaBean = getCurrentUser();
		if (userDynaBean != null) {
			deptId = userDynaBean.getStr("DEPTID");
		}
		
		return deptId;
	}
	
	/*
	 * 获取当前部门Code
	 */
	public String getCurrentDeptCode() {
		String res="";
		DynaBean userDynaBean = getCurrentUser();
		if (userDynaBean != null) {
			res = userDynaBean.getStr("DEPTCODE");
		}
		
		return res;
	}
	
	/*
	 * 获取当前部门名称
	 */
	public String getCurrentDeptName() {
		String res="";
		DynaBean userDynaBean = getCurrentUser();
		if (userDynaBean != null) {
			res = userDynaBean.getStr("DEPTNAME");
		}
		
		return res;
	}
	
	
	/*
	 * 获取当前集团公司名称
	 */
	public String getCurrentJTGSMC() {
		String res="";
		DynaBean userDynaBean = getCurrentUser();
		if (userDynaBean != null) {
			res = userDynaBean.getStr("JTGSMC");
		}
		
		return res;
	}
	
	/*
	 * 获取当前集团公司ID
	 */
	public String getCurrentJTGSID() {
		String res="";
		DynaBean userDynaBean = getCurrentUser();
		if (userDynaBean != null) {
			res = userDynaBean.getStr("JTGSID");
		}
		
		return res;
	}
	
	/*
	 * 获取当前用户实体
	 */
	public DynaBean getCurrentUser() {
		String userCode=getCurrentUserCode();
		DynaBean userDynaBean = new DynaBean();
		if (userCode != null &&!userCode.isEmpty()) {
			userDynaBean = SERVICETEMPLATE.selectOne("JE_CORE_ENDUSER", " and USERCODE='"+userCode+"'");
		}		
		return userDynaBean;
	}
	
	

}
