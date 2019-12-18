package com.jgmes.action;

import com.je.core.action.DynaAction;
import com.jgmes.service.JgmesFirstInspectionService;
import com.jgmes.util.JgmesCommon;
import com.jgmes.util.JgmesResult;
import com.jgmes.util.TreeDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * 
 * @author liuc
 * @version 2019-04-17 20:55:41
 * @see /jgmes/jgmesFirstInspectionAction!load.action
 */
@Component("jgmesFirstInspectionAction")
@Scope("prototype")
public class JgmesFirstInspectionAction extends DynaAction  {

    private JgmesFirstInspectionService jgmesFirstInspectionService;

	private static final Logger logger = LoggerFactory.getLogger(JgmesFirstInspectionAction.class);
	private static final long serialVersionUID = 1L;

	@Override
	public void load(){
		logger.debug("123123456123");
		toWrite("hello Action");
	}

	/**
	 * @author liuc
	 * 获取有效产品检验标准
	 * @see /jgmes/jgmesFirstInspectionAction!getEffectiveProductStandard.action
	 */
	public void getEffectiveProductStandard(){
		// MAC地址
		String mac = request.getParameter("mac");
		// 用户编码  必填
		String userCode = request.getParameter("userCode");
		// 产品编码
		String cpbm = request.getParameter("cpbm");
		// 检验类型
		String jylx = request.getParameter("jylx");

		JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
		String jsonStr = "";
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {

            ret = jgmesFirstInspectionService.getEffectiveProductStandard(cpbm,jylx);
		}
		jsonStr = jsonBuilder.toJson(ret);
		toWrite(jsonStr);
	}

	/**
	 * @author liuc
	 * 根据检测方案主键ID，获取检验项目
	 * @see /jgmes/jgmesFirstInspectionAction!getEffectiveProductStandardChild.action
	 */
	public void getEffectiveProductStandardChild(){
		// MAC地址
		String mac = request.getParameter("mac");
		// 用户编码  必填
		String userCode = request.getParameter("userCode");
		// 产品检验标准主表 ID
		String id = request.getParameter("jybzID");
		// 首件检验单主表ID
		String sjzbId = request.getParameter("sjzbId");
		// 巡检单主表ID
		String xjzbId = request.getParameter("xjzbId");
		// 项目分类根目录的主键ID
		String rootId = request.getParameter("rootId");

		JgmesResult<List<TreeDto>> ret = new JgmesResult<List<TreeDto>>();
		String jsonStr = "";
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {
			ret = jgmesFirstInspectionService.getEffectiveProductStandardChild(id,sjzbId,xjzbId,rootId);
		}
		jsonStr = jsonBuilder.toJson(ret);
		toWrite(jsonStr);
	}

	/**
	 * @author liuc
	 * 首件检验单存储数据库
	 * @see /jgmes/jgmesFirstInspectionAction!doSaveFirstInspection.action
	 */
	public void doSaveFirstInspection(){
        // MAC地址
        String mac = request.getParameter("mac");
        // 用户编码  必填
        String userCode = request.getParameter("userCode");
        // 首件检验主表数据
        String jsonStr = request.getParameter("jsonStr");
        // 首件检验子表数据集合
        String jsonStrDetail = request.getParameter("jsonStrDetail");
		// 巡检首检任务单关联表
		String jsonStrRwGl = request.getParameter("jsonStrRwGl");

        JgmesResult<String> ret = new JgmesResult<String>();
        // 校检合法性，是否多地登陆
        if (doCheck(userCode, mac).IsSuccess) {
            ret = jgmesFirstInspectionService.doSaveFirstInspection(jsonStr,jsonStrDetail,jsonStrRwGl,request,userCode);
        }
        jsonStr = jsonBuilder.toJson(ret);
        toWrite(jsonStr);
    }

	/**
	 * 获取检验项目分类的根目录
	 * id 检验方案主表主键ID
	 * userCode 用户编码
	 * mac mac地址
	 */
	public void getInspectionItemRootClassify(){
		// MAC地址
		String mac = request.getParameter("mac");
		// 用户编码  必填
		String userCode = request.getParameter("userCode");
		// 首件检验主表数据
		String id = request.getParameter("jybzID");

		JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {
			ret = jgmesFirstInspectionService.getInspectionItemRootClassify(id);
		}
		toWrite(jsonBuilder.toJson(ret));
	}

	/**
	 * 获取首件检验主表
	 * antistop 关键词模糊查询
	 * mac mac地址
	 * usercode 用户编码
	 */
	public void getFirstInspection(){
		// MAC地址
		String mac = request.getParameter("mac");
		// 用户编码  必填
		String userCode = request.getParameter("userCode");
		// 关键词
		String antistop = request.getParameter("antistop");

		JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {

			ret = jgmesFirstInspectionService.getFirstInspection(antistop);
		}
		toWrite(jsonBuilder.toJson(ret));
	}

	/**
	 * 获取巡检主表
	 * antistop 关键词模糊查询
	 * mac mac地址
	 * usercode 用户编码
	 */
	public void getRoutingInspection(){
		// MAC地址
		String mac = request.getParameter("mac");
		// 用户编码  必填
		String userCode = request.getParameter("userCode");
		// 关键词
		String antistop = request.getParameter("antistop");

		JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {
			ret = jgmesFirstInspectionService.getRoutingInspection(antistop);
		}
		toWrite(jsonBuilder.toJson(ret));
	}

	/**
	 * 获取首件检验主表
	 * antistop 关键词模糊查询
	 * mac mac地址
	 * usercode 用户编码
	 */
	public void getFirstInspectionChild(){
		// MAC地址
		String mac = request.getParameter("mac");
		// 用户编码  必填
		String userCode = request.getParameter("userCode");
		// 首件检验主表主键ID
		String id = request.getParameter("id");

		JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {

			ret = jgmesFirstInspectionService.getFirstInspectionChild(id);
		}
		toWrite(jsonBuilder.toJson(ret));
	}

	/**
	 * 获取巡检首检任务单关联表
	 * id 首检单或者巡检单的主键ID
	 * mac mac地址
	 * usercode 用户编码
	 */
	public void getRWDXXGLB(){
		// MAC地址
		String mac = request.getParameter("mac");
		// 用户编码  必填
		String userCode = request.getParameter("userCode");
		// 首检单或者巡检单的主键ID
		String id = request.getParameter("id");

		JgmesResult<List<HashMap>> ret = new JgmesResult<List<HashMap>>();
		// 校检合法性，是否多地登陆
		if (doCheck(userCode, mac).IsSuccess) {

			ret = jgmesFirstInspectionService.getRWDXXGLB(id);
		}
		toWrite(jsonBuilder.toJson(ret));
	}

	/**
	 * 校验用户合法性，不合法直接提示。
	 * @param userCode
	 * @param mac
	 * @return
	 */
	private JgmesResult<String> doCheck(String userCode,String mac) {
		JgmesResult<String> result = new JgmesCommon(request, serviceTemplate).doCheckRes(mac, userCode);
		if (!result.IsSuccess) {
			toWrite(jsonBuilder.toJson(result));
		}
		return result;
	}

    public JgmesFirstInspectionService getJgmesFirstInspectionService() {
        return jgmesFirstInspectionService;
    }

    @Resource(name="jgmesFirstInspectionService")
    public void setJgmesFirstInspectionService(JgmesFirstInspectionService jgmesFirstInspectionService) {
        this.jgmesFirstInspectionService = jgmesFirstInspectionService;
    }
}