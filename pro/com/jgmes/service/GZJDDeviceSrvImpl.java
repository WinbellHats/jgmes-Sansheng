package com.jgmes.service;

import com.je.core.security.SecurityUserHolder;
import com.je.core.service.PCDynaServiceTemplate;
import com.je.core.service.PCServiceTemplate;
import com.je.core.util.SpringContextHolder;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.DynaBean;
import com.je.rbac.model.Department;
import com.je.rbac.model.EndUser;
import com.jgmes.action.CommonAction;
import com.jgmes.util.JgmesEnumsBgsjLx;
import com.jgmes.util.JgmesResult;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * CLIO ��������
 * @author admin
 * @version 2019-05-18 09:28:41
 */
@Component("gZJDDeviceSrv")
public class GZJDDeviceSrvImpl implements GZJDDeviceSrv  {

	/**��̬Bean(DynaBean)�ķ����*/
	private PCDynaServiceTemplate serviceTemplate;
	/**ʵ��Bean���������,��Ҫ����SQL*/
	private PCServiceTemplate pcServiceTemplate;

	@Resource(name="PCDynaServiceTemplate")
	public void setServiceTemplate(PCDynaServiceTemplate serviceTemplate) {
		this.serviceTemplate = serviceTemplate;
	}
	@Resource(name="PCServiceTemplateImpl")
	public void setPcServiceTemplate(PCServiceTemplate pcServiceTemplate) {
		this.pcServiceTemplate = pcServiceTemplate;
	}
	public PCDynaServiceTemplate getServiceTemplate() {
		return serviceTemplate;
	}
	
	public void load(){
		System.out.println("hello serviceimpl");
	}


	@Override
	public ResultBoolean SaveClioTestData(String barCode, String TestResults) {
		ResultBoolean result = new ResultBoolean();
		result.Data = true;
		try {
			if(serviceTemplate==null){
				serviceTemplate = SpringContextHolder.getBean("PCDynaServiceTemplate");
			}
			if (StringUtil.isNotEmpty(barCode)) {
				List<DynaBean> bean = serviceTemplate.selectList("JGMES_PB_BGSJ", "and BGSJ_TMH='" + barCode + "'");
				if (bean != null && bean.size() > 0) {
					String cxbm = bean.get(0).getStr("BGSJ_CXBM");// 获取产线编码
					String cxmc = bean.get(0).getStr("BGSJ_CXMC");// 获取产线名称
					String cpbh = bean.get(0).getStr("BGSJ_CPBH");// 获取产品编号
					String cpmc = bean.get(0).getStr("BGSJ_CPMC");// 获取产品名称
					String cpgg = bean.get(0).getStr("BGSJ_CPGG");// 获取产品规格
					String macdz = bean.get(0).getStr("BGSJ_MACDZ");// 获取Mac地址
					String gwid = bean.get(0).getStr("BGSJ_GWID");// 获取工位id
					String rwid = bean.get(0).getStr("BGSJ_SCRWID");//任务单号id
					String rwdh = bean.get(0).getStr("BGSJ_SCRW");//任务单号
					String zjid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 19);// 主键id，JGMES_PB_BGSJ_ID
					// 获取当前时间
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String NowDate = df.format(new Date());// BGSJ_GZSJ

					String clioGxid = "Dn1yyUdryuvv2bC6vqy";
					List<DynaBean> jgmes_base_cpgwgx = serviceTemplate.selectList("JGMES_BASE_CPGWGX", "and CPGWGX_CPBH='" + cpbh + "' and CPGWGX_CXBM='" + cxbm + "' and CPGWGX_GXID = '" + clioGxid + "'");
					String gxid = "";
					String gwbh ="";
					String gxmc = "";
					String gwmc = "";
					if (jgmes_base_cpgwgx.size()>0){
						gxid = jgmes_base_cpgwgx.get(0).getStr("CPGWGX_GYGXID");//工艺路线工序ID
						gwbh = jgmes_base_cpgwgx.get(0).getStr("CPGWGX_GWBH");//工位编号
						gxmc = jgmes_base_cpgwgx.get(0).getStr("CPGWGX_GXNAME");//工序名称
						gwmc = jgmes_base_cpgwgx.get(0).getStr("CPGWGX_GWMC");//工位名称
					}else{
						result.setIsSuccess(false);
						result.setMessage("获取产品工位工序对应表失败");
						result.setData(false);
						return result;
					}
					String gxbh = "GX0032";// 工序编号，写死，BGSJ_GXBH    GX0098 jd:GX0032
//					String gxmc = "CLIO测试";// 工序名称，写死，BGSJ_GXMC
//					String gwbh = "GW000141";// 工位编号，写死，BGSJ_GWBH  GW000124 jd:GW000039
//					String gwmc = "CLIO测试";// 工位名称，写死，BGSJ_GWMC
					String zt = "1";// BGSJ_STATUS_CODE,默认启用
					String ztName = "启用";
					// String gxid = bean.get(0).getStr("BGSJ_GXID");// 工序id
//					String gxid = "83yt4G28J4KeHdVjFTN"; // xxp20190306 工艺路线工序ID     h97INZo39W2W9UiSPak jd:BxbcO21p9uCS3ka1vJC
					String sl = "1";// 数量，BGSJ_SL
					int gxsxh = 9;// 工序顺序号，写死，BGSJ_GXSXH   jd 9
					String BGSJ_PDJG_CODE = "PDJG02"; // 判定结果
					// String pdjg = TestResults;// 判定结果，BGSJ_PDJG_CODE
					Boolean re = false;
					if(StringUtil.isEmpty(TestResults)) {
						System.out.println("该测试结果为空，请检查输入");
						result.setErrorCode(2);
						result.setMessage("测试结果为空，请检查输入");
						result.setIsSuccess(false);
						result.setData(false);
						return result;
					}
					if ("pass".equalsIgnoreCase(TestResults.trim())) {
						re = true;
						BGSJ_PDJG_CODE = "PDJG01";
					}
					// 从表数据
					String wlbh="";// "CP20190220144";// BGSJZB_WLBH,物料编号，写死
					String wlmc="";// "蓝牙板";// BGSJZB_WLMC,物料名称，写死
					String jcbh = "XM000081";// 检测项目编号，待定,BGSJZB_JCXMBH     XM000080
					String jcmz = "CLIO测试";// 检测项目名称，待定,BGSJZB_JCXMMC
					//主表数据
					JSONObject mainJsonObject = new JSONObject();
					mainJsonObject.put("BGSJ_CXBM", cxbm);
					mainJsonObject.put("BGSJ_CXMC", cxmc);
					mainJsonObject.put("BGSJ_CPBH", cpbh);
					mainJsonObject.put("BGSJ_CPMC", cpmc);
					mainJsonObject.put("BGSJ_CPGG", cpgg);
					mainJsonObject.put("BGSJ_MACDZ", macdz);
					mainJsonObject.put("JGMES_PB_BGSJ_ID", zjid);
					mainJsonObject.put("BGSJ_GZSJ", NowDate);
					mainJsonObject.put("BGSJ_GXBH", gxbh);
					mainJsonObject.put("BGSJ_GXMC", gxmc);
					mainJsonObject.put("BGSJ_GWBH", gwbh);
					mainJsonObject.put("BGSJ_GWMC", gwmc);
					mainJsonObject.put("BGSJ_SL", sl);
					mainJsonObject.put("BGSJ_GXSXH", gxsxh);
					mainJsonObject.put("BGSJ_PDJG_CODE", BGSJ_PDJG_CODE);
					mainJsonObject.put("BGSJ_TMH", barCode);
					mainJsonObject.put("BGSJ_GWID", gwid);
					mainJsonObject.put("BGSJ_GXID", gxid);
					mainJsonObject.put("BGSJ_STATUS_CODE", zt);
					mainJsonObject.put("BGSJ_STATUS_NAME", ztName);
					mainJsonObject.put("BGSJ_SCRW",rwdh);
					mainJsonObject.put("BGSJ_SCRWID",rwid);
					String mainJson = mainJsonObject.toString();
//					System.out.println("主表数据-------"+mainJson);
					//从表数据
					JSONObject minorJsonObject = new JSONObject();
					minorJsonObject.put("BGSJZB_WLBH", wlbh);
					minorJsonObject.put("BGSJZB_WLMC", wlmc);
					minorJsonObject.put("BGSJZB_JCXMBH", jcbh);
					minorJsonObject.put("BGSJZB_JCXMMC", jcmz);
					minorJsonObject.put("BGSJZB_CPBH", cpbh);
					minorJsonObject.put("JGMES_PB_BGSJ_ID", zjid);
					minorJsonObject.put("BGSJZB_CPMC", cpmc);
					String minorJson = "["+minorJsonObject.toString()+"]";
//					System.out.println("从表数据----"+mainJson);
					CommonAction ca = new CommonAction();
					JgmesResult<HashMap> map =ca.doJsonSaveBgSjAll(mainJson,minorJson,"",null, JgmesEnumsBgsjLx.LySj,false);
					if(map.IsSuccess) {
						System.out.println("成功");
//						result.setErrorCode(0);
						result.setIsSuccess(true);
						result.setData(true);
						return result;
					}else {
						System.out.println("失败");
						result.setMessage(map.getMessage());
						result.setData(false);
						return result;
					}
				}else {
					result.setData(false);
					result.setMessage(String.format("条形码:%s无找到相关CLIO的数据",barCode));}
			}else{
				System.out.println("条码号为空，请检查输入");
				result.setErrorCode(1);
				result.setMessage("条码号为空，请检查输入");
				result.setData(false);
				return result;
			}
		}catch (Exception e){
			e.printStackTrace();
			System.out.println(e.toString());
			result.setMessage("系统异常错误！");
			result.setData(false);
		}
		return result;
	}

	/**
	 * 返回 字符串对象
	 *
	 * @author John
	 *
	 */
	public class ResultString {

		public ResultString() {
			this.IsSuccess = true;
		}

		private boolean IsSuccess;

		public boolean getIsSuccess() {
			return IsSuccess;
		}

		public void setIsSuccess(boolean IsSuccess) {
			this.IsSuccess = IsSuccess;
		}

		private String Message;

		public String getMessage() {
			return Message;
		}

		public void setMessage(String message) {
			this.Message = message;
			this.IsSuccess = false;
		}

		private String Data;

		public String getData() {
			return Data;
		}

		public void setData(String data) {
			Data = data;
		}

		private Integer ErrorCode;

		public Integer getErrorCode() {
			return ErrorCode;
		}

		public void setErrorCode(Integer errorCode) {
			ErrorCode = errorCode;
		}
	}

	/**
	 * 返回 布尔类型对象
	 *
	 * @author John
	 *
	 */
	public class ResultBoolean {

		public ResultBoolean() {
			this.IsSuccess = true;
		}

		private boolean IsSuccess;

		public boolean getIsSuccess() {
			return IsSuccess;
		}

		public void setIsSuccess(boolean IsSuccess) {
			this.IsSuccess = IsSuccess;
		}

		private String Message;

		public String getMessage() {
			return Message;
		}

		public void setMessage(String message) {
			this.Message = message;
			this.IsSuccess = false;
		}

		private boolean Data;

		public boolean getData() {
			return Data;
		}

		public void setData(boolean data) {
			Data = data;
		}

		private Integer ErrorCode;

		public Integer getErrorCode() {
			return ErrorCode;
		}

		public void setErrorCode(Integer errorCode) {
			ErrorCode = errorCode;
		}
	}
	
	/**
	 * ��ȡ��¼�û�
	 * @return
	 */
	public EndUser getCurrentUser() {
		// TODO Auto-generated method stub
		return SecurityUserHolder.getCurrentUser();
	}
	/**
	 * ��ȡ��¼�û����ڲ���
	 * @return
	 */
	public Department getCurrentDept() {
		// TODO Auto-generated method stub
		return SecurityUserHolder.getCurrentUserDept();
	}

}