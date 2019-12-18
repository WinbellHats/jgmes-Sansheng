package com.jgmes.util;

 

import java.net.URL;

import org.codehaus.xfire.client.Client;
import org.springframework.stereotype.Component;

import com.je.task.service.PcTimedTaskTemplate;
import com.je.task.vo.TimedTaskParamsVo;

/**
 * ��ʱ����saas  Services ������
 * @author liuc
 * @version 2019-02-27 13:55:05
 */
@Component("saasTimingTask")
public class SaasTimingTask extends PcTimedTaskTemplate {

	
	public void load(TimedTaskParamsVo vo){
		System.out.println("hello TASK");
	}
	
	public void getService() {
		try {	
			Client client = new Client(new URL("http://localhost:8088/services/demo?wsdl"));
			Object[] res = client.invoke("test", new Object[]{"admin"});
			//invoke方法有两个参数，一个是接口的方法名，一个是接口方法的参数列表
			String re = (String) res[0];
			System.out.println(re);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}