package com.demo.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.je.core.constants.tree.NodeType;
import com.je.core.entity.QueryInfo;
import com.je.core.entity.extjs.JSONTreeNode;
import com.je.core.service.PCDynaServiceTemplate;
import com.je.core.service.PCServiceTemplate;
import com.je.core.util.JEUUID;
import com.je.core.util.StringUtil;
import com.je.core.util.TreeUtil;
import com.je.core.util.bean.BeanUtils;
import com.je.core.util.bean.DynaBean;
import com.je.dd.vo.DicInfoVo;
import com.je.develop.vo.ExcelParamVo;
import com.je.develop.vo.ExcelReturnVo;
import com.je.wf.processVo.WfAssgineSubmitInfo;
import com.je.wf.processVo.WfEventSubmitInfo;

@Component("demoService")
@Scope("prototype")
public class DemoServiceImpl implements DemoService {

	private static PCDynaServiceTemplate serviceTemplate;
	private static PCServiceTemplate pcServiceTemplate; 
	
	@Resource(name = "PCDynaServiceTemplate")
	public void setServiceTemplate(PCDynaServiceTemplate serviceTemplate) {
		this.serviceTemplate = serviceTemplate;
	}

	@Resource(name = "PCServiceTemplateImpl")
	public void setPcServiceTemplate(PCServiceTemplate pcServiceTemplate) {
		this.pcServiceTemplate = pcServiceTemplate;
	}

	@Override
	public void doWfProcessEvent(WfEventSubmitInfo eventInfo) {
		// TODO Auto-generated method stub
		// 获取当前业务表数据
		DynaBean dynaBean = eventInfo.getDynaBean();
		System.out.println("流程事件已执行..");
	}

	@Override
	public void doWfTaskEvent(WfEventSubmitInfo eventInfo) {
		// TODO Auto-generated method stub
		// 获取当前业务表数据
		DynaBean dynaBean = eventInfo.getDynaBean();
		System.out.println("任务动作事件已执行..");
	}
	
	@Override
	public List<DynaBean> doWfAssigneEvent1() {
		// TODO Auto-generated method stub
		// 指定的用户
		List<DynaBean> users = serviceTemplate.selectList("JE_CORE_ENDUSER",
				" AND USERCODE='admin'");
		// 如果不查询则指定 用户三个属性即可 USERNAME USERCODE USERID
		List<DynaBean> us = new ArrayList<DynaBean>();
		DynaBean u = new DynaBean("JE_CORE_ENDUSER", true);
		u.set("USERID", "");
		u.set("USERCODE", "");
		u.set("USERNAME", "");
		us.add(u);// 返回us也可以实现
		System.out.println("动态获取人员时间已执行..");
		return users;
	}
	

	@Override
	public List<DynaBean> doWfAssigneEvent(WfAssgineSubmitInfo eventInfo) {
		// TODO Auto-generated method stub
		// 指定的用户
		List<DynaBean> users = serviceTemplate.selectList("JE_CORE_ENDUSER",
				" AND USERCODE='admin'");
		// 如果不查询则指定 用户三个属性即可 USERNAME USERCODE USERID
		List<DynaBean> us = new ArrayList<DynaBean>();
		DynaBean u = new DynaBean("JE_CORE_ENDUSER", true);
		u.set("USERID", "");
		u.set("USERCODE", "");
		u.set("USERNAME", "");
		us.add(u);// 返回us也可以实现
		System.out.println("动态获取人员时间已执行..");
		return users;
	}
	@Override
	public JSONTreeNode getProjectTree(DicInfoVo dicInfoVo) {
		// TODO Auto-generated method stub
		BeanUtils beanUtils=BeanUtils.getInstance();
		DynaBean resTable=beanUtils.getResourceTable("MR_PRODUCT_RES");
		List<DynaBean> rescolumns=(List<DynaBean>) resTable.get(BeanUtils.KEY_TABLE_COLUMNS);
		JSONTreeNode restemplate = beanUtils.buildJSONTreeNodeTemplate(rescolumns);
		QueryInfo resqueryInfo=new QueryInfo();
		List<JSONTreeNode> reses=pcServiceTemplate.getJsonTreeNodeList(NodeType.ROOT, "MR_PRODUCT_RES", restemplate, resqueryInfo);
		String resRootId="ROOT_RES";
		String nodeInfo=dicInfoVo.getFieldName();
		for(JSONTreeNode node:reses){
			if(node.getId().equals(NodeType.ROOT)){
				node.setId(resRootId);
				node.setParent("ROOT");
				node.setDisabled("1");
				if(StringUtil.isNotEmpty(nodeInfo)){
					node.setNodeInfo(nodeInfo);
					node.setId(node.getId()+"_"+nodeInfo);
					node.setParent(node.getParent()+"_"+nodeInfo);
				}
			}else if(NodeType.ROOT.equals(node.getParent())){
				node.setParent(resRootId);
				if(StringUtil.isNotEmpty(nodeInfo)){
					node.setNodeInfo(nodeInfo);
					node.setId(node.getId()+"_"+nodeInfo);
					node.setParent(node.getParent()+"_"+nodeInfo);
				}
			}else{
				if(StringUtil.isNotEmpty(nodeInfo)){
					node.setNodeInfo(nodeInfo);
					node.setId(node.getId()+"_"+nodeInfo);
					node.setParent(node.getParent()+"_"+nodeInfo);
				}
			}
			node.setNodeInfoType("0");
		}
		
		JSONTreeNode rootNode=TreeUtil.buildRootNode();
		if(StringUtil.isNotEmpty(nodeInfo)){
			resRootId=resRootId+"_"+nodeInfo;
			rootNode.setId(rootNode.getId()+"_"+nodeInfo);
		}
		JSONTreeNode resNode=pcServiceTemplate.buildJSONNewTree(reses, resRootId);
		resNode.setText("根节点");
		rootNode.getChildren().add(resNode);
		rootNode.setIconCls("JE_TREEQUERY");
		return rootNode;
	}
	@Override
	public ExcelReturnVo impDicData(ExcelParamVo paramVo) {
		// TODO Auto-generated method stub
		List<DynaBean> lists=paramVo.getSheetValues();
		//数据字典编码
		String dicCode="字典编码";
		DynaBean dictionary=serviceTemplate.selectOne("JE_CORE_DICTIONARY", " AND DICTIONARY_DDCODE='"+dicCode+"'");
		String dicId=dictionary.getStr("JE_CORE_DICTIONARY_ID");
		DynaBean rootBean=serviceTemplate.selectOne("JE_CORE_DICTIONARYITEM", " AND SY_NODETYPE='"+NodeType.ROOT+"' AND DICTIONARYITEM_DICTIONARY_ID='"+dictionary.getStr("JE_CORE_DICTIONARY_ID")+"'");
		pcServiceTemplate.executeSql(" DELETE FROM JE_CORE_DICTIONARYITEM WHERE  SY_NODETYPE!='"+NodeType.ROOT+"' AND DICTIONARYITEM_DICTIONARY_ID='"+dictionary.getStr("JE_CORE_DICTIONARY_ID")+"'");
		saveDicItems(rootBean, dicId, lists);
		ExcelReturnVo returnVo=new ExcelReturnVo(1, "成功");
		return returnVo;
	}
	private void saveDicItems(DynaBean parent,String dicId,List<DynaBean> lists){
		String parentCode=parent.getStr("DICTIONARYITEM_ITEMCODE");
		if("ROOT".equals(parentCode) || StringUtil.isEmpty(parentCode)){
			parentCode="1000";
		}
		int length=parentCode.length();
		int childLength=length+4;
		int order=1;
		boolean haveChild=false;
		for(DynaBean obj:lists){
			String code=obj.getStr("DICTIONARYITEM_ITEMCODE");
			if(code.length()==childLength && code.startsWith(parentCode) && !code.equals(parentCode)){
				String uuid=JEUUID.uuid();
				DynaBean item=new DynaBean("JE_CORE_DICTIONARYITEM",true);
				item.set("JE_CORE_DICTIONARYITEM_ID", uuid);
				item.set("DICTIONARYITEM_ITEMNAME", obj.getStr("DICTIONARYITEM_ITEMNAME"));
				item.set("DICTIONARYITEM_ITEMCODE",code);
				item.setStr("DICTIONARYITEM_DICTIONARY_ID", dicId);
				item.set("SY_PARENT", parent.getStr("JE_CORE_DICTIONARYITEM_ID"));
				item.set("SY_NODETYPE",NodeType.LEAF);
				item.set("SY_PARENTPATH", parent.getStr("SY_PATH"));
				item.set("SY_LAYER", parent.getInt("SY_LAYER", 0)+1);
				item.set("SY_PATH", parent.getStr("SY_PATH")+"/"+uuid);
				item.set("SY_ORDERINDEX", obj.getStr("SY_STATUS"));
				item.set("SY_FLAG", "1");
				item.set("SY_TREEORDERINDEX", parent.getStr("SY_TREEORDERINDEX")+StringUtil.preFillUp(order+"", 6, '0'));
				serviceTemplate.buildModelCreateInfo(item);
				item=serviceTemplate.insert(item);
				saveDicItems(item, dicId, lists);
				order++;
				haveChild=true;
			}
		}
		if(haveChild && !NodeType.ROOT.equals(parent.getStr("SY_NODETYPE"))){
			parent.set("SY_NODETYPE", NodeType.GENERAL);
			serviceTemplate.update(parent);
		}
	}
	
	

}
