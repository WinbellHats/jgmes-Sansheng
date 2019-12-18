package com.jgmes.action;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.je.core.action.DynaAction;

import java.io.Serializable;
/**
 * 
 * @author liuc
 * @version 2019-09-14 13:54:10
 * @see /jgmes/studentAction!load.action
 */
@Component("studentAction")
@Scope("prototype")
public class StudentAction extends DynaAction  {
	
	private static final long serialVersionUID = 1L;
	
	public void load(){
		String id=request.getParameter("id");
		toWrite("hello Action");
	}
	
}