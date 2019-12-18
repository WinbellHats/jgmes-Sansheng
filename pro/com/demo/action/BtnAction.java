package com.demo.action;

import com.je.core.action.DynaAction;
import com.je.phone.vo.DsInfoVo;
import net.sf.json.JSONObject;
import org.codehaus.jettison.json.JSONArray;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * ????????antion
 * 
 * @author admin
 * @version 2017-05-23 12:31:39
 * @see /DEMO/btnAction!load.action
 */
@Component("btnAction")
@Scope("prototype")
public class BtnAction extends DynaAction {

	private static final long serialVersionUID = 1L;

	/**
	 * ?????????????
	 */
	public void welcomespeech() {
		toWrite("??????????JEPLUTS?????????????????");
	}

	public String actionDemo(DsInfoVo infoVo) {
		JSONObject params = infoVo.getParams();
		String A = params.getString("A");
		JSONArray list = new JSONArray();
		JSONObject map = new JSONObject();
		map.put("A", "a");
		map.put("B", "b");
		list.put(map);
		System.out.println(A);
		return list.toString();
	}

	public int badgeDemo() {
		java.util.Random random = new java.util.Random();
		return random.nextInt(100);
	};
}