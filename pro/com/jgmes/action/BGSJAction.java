package com.jgmes.action;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.codehaus.jettison.json.JSONArray;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.je.core.action.DynaAction;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.DynaBean;
import com.je.phone.vo.DsInfoVo;

import com.je.core.facade.extjs.JsonBuilder;


/**
 * 
 * @author fm
 * @version 2018-12-17 00:04:12
 * @see /jgmes/bGSJAction!doJsonSave.action
 */
@Component("bGSJAction")
@Scope("prototype")
public class BGSJAction extends DynaAction {

	@Override
	public void doSave() {		
		// TODO Auto-generated method stub
		
		toWrite(jsonBuilder.returnSuccessJson("\"添加成功\""));
		super.doSave();
	}
	
	public void doJsonSave(String jsonString) {
		
		DynaBean bgsj=(DynaBean) new JsonBuilder().fromJson(jsonString,new DynaBean("JGMES_PB_BGSJ",true).getClass());
		
		serviceTemplate.buildModelCreateInfo(bgsj);
		serviceTemplate.insert(bgsj);
		
		toWrite(jsonBuilder.returnSuccessJson("\"添加成功\""));
	}
	
	public void doJsonSave() {
		
		
		String jsonString = ((String[])requestParams.get("jsonString"))[0];
		
		DynaBean bgsj = new DynaBean();
		bgsj.setStr(beanUtils.KEY_TABLE_CODE, "JGMES_PB_BGSJ");
		serviceTemplate.buildModelCreateInfo(bgsj);
		serviceTemplate.insert(bgsj);
		
		toWrite(jsonBuilder.returnSuccessJson("\"添加成功\""+jsonString));
	}
	
public void doJsonSave2()  throws ParseException
{
		

	String key ="";
	String value ="";
	
		String jsonString = ((String[])requestParams.get("jsonString"))[0];
		DynaBean bgsj = new DynaBean();
		JSONObject  jb1=JSONObject.fromObject(jsonString);
		Iterator it = jb1.keys();  
        List<String> keyListstr = new ArrayList<String>();  
        while(it.hasNext()){  
            key = (String) it.next();
            value = jb1.getString(key);
            bgsj.setStr(key,value);
        }  
        bgsj.setStr(beanUtils.KEY_TABLE_CODE, "JGMES_PB_BGSJ");
//		dd.setStr(beanUtils.KEY_PK_CODE, "JGMES_PB_BGSJ_ID");
		
//		DynaBean dd = new DynaBean("JGMES_PB_BGSJ",true);

//		DynaBean bgsj=(DynaBean) new JsonBuilder().fromJson(jsonString,dynaBeanTemplate.getClass());
		
//		System.out.println(bgsj);
		
		serviceTemplate.buildModelCreateInfo(bgsj);
		serviceTemplate.insert(bgsj);
		
		toWrite(jsonBuilder.returnSuccessJson("\"添加成功\""));
		
	}
	
}
