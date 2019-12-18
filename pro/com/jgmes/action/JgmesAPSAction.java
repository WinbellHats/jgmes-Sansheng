package com.jgmes.action;

import com.je.core.action.DynaAction;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

//import org.apache.log4j.Logger;
/**
 * 
 * @author cj
 * @version 2019-04-26 21:06:54
 * @see /jgmes/jgmesAPSAction!load.action
 */
@Component("jgmesAPSAction")
@Scope("prototype")
public class JgmesAPSAction extends DynaAction  {
	
	private static final long serialVersionUID = 1L;
	
	public void load(){
		toWrite("hello Action");
	}
	
}