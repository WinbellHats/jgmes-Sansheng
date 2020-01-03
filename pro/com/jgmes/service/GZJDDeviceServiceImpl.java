package com.jgmes.service;

import com.je.core.service.PCDynaServiceTemplate;
import com.je.core.service.PCServiceTemplate;
import com.je.core.util.StringUtil;
import com.je.core.util.bean.DynaBean;
import com.jgmes.action.CommonAction;
import com.jgmes.util.JgmesEnumsBgsjLx;
import com.jgmes.util.JgmesResult;
import net.sf.json.JSONObject;
import org.codehaus.xfire.util.Base64;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * 
 * @author xxp
 * @version 2019-02-28 16:15:04
 */
@Component("GZJDDeviceService")
public class GZJDDeviceServiceImpl implements GZJDDeviceService {

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

	public PCDynaServiceTemplate getServiceTemplate() {
		return serviceTemplate;
	}
	
	
	@Override
	public ResultString GetBluetoothAddressBySN(String barCode) {
		// TODO Auto-generated method stub
		ResultString result = new ResultString();
		String data = "";
		if (barCode != "" || barCode.isEmpty()) {
			// GX0027:ɨ������ά��/BT���ά��, CP20190120012:������
			String sql = "SELECT b.BGSJZB_TMH FROM JGMES_PB_BGSJ a,JGMES_PB_BGSJZB b WHERE a.JGMES_PB_BGSJ_ID=b.JGMES_PB_BGSJ_ID and BGSJ_GXBH='GX0027' and b.BGSJZB_WLBH='CP20190120012' and BGSJ_TMH='"
					+ barCode + "'";
			List<?> list = pcServiceTemplate.queryBySql(sql);
			if (list != null && list.size() > 0)
				data = list.get(0).toString();
			
		}
		result.setData(data);
		return result;
	}

	@Override
	public ResultBoolean SaveBluetoothTestData(String barCode, String BTTestData, String TestResults) {
		// TODO Auto-generated method stub
		ResultBoolean result = new ResultBoolean();
		result.Data = true;
		try {
//			Connection conn = null;
//			PreparedStatement st = null;
//			ResultSet rs = null;
			if (barCode != "" || !barCode.isEmpty()) {
				List<DynaBean> bean = serviceTemplate.selectList("JGMES_PB_BGSJ", "and BGSJ_TMH='" + barCode + "'");
				if (bean.size() > 0) {
					String cxbm = bean.get(0).getStr("BGSJ_CXBM");// ��ȡ���߱���
					String cxmc = bean.get(0).getStr("BGSJ_CXMC");// ��ȡ��������
					String cpbh = bean.get(0).getStr("BGSJ_CPBH");// ��ȡ��Ʒ���
					String cpmc = bean.get(0).getStr("BGSJ_CPMC");// ��ȡ��Ʒ����
					String cpgg = bean.get(0).getStr("BGSJ_CPGG");// ��ȡ��Ʒ���
					String macdz = bean.get(0).getStr("BGSJ_MACDZ");// ��ȡMac��ַ
					String gwid = bean.get(0).getStr("BGSJ_GWID");// ��ȡ��λid
					String rwid = bean.get(0).getStr("BGSJ_SCRWID");//���񵥺�id
					String rwdh = bean.get(0).getStr("BGSJ_SCRW");//���񵥺�
					String zjid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 19);// ����id��JGMES_PB_BGSJ_ID
					// ��ȡ��ǰʱ��
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String NowDate = df.format(new Date());// BGSJ_GZSJ
					/*ͨ��ϵͳ������ȡ��ǰ����������ı��*/
					DynaBean jgmes_xtgl_xtcs = serviceTemplate.selectOne("JGMES_XTGL_XTCS", "and XTCS_CXFL2_CODE='LYGX'");
					if (jgmes_xtgl_xtcs==null){
						result.setIsSuccess(false);
						result.setMessage("��ȡϵͳ���������������Ա��ʧ�ܣ�");
						result.setData(false);
						return result;
					}
					/*����������*/
					String lyGx = jgmes_xtgl_xtcs.getStr("XTCS_CSZ");
					/*�������������Ż�ȡ����ID*/
					DynaBean jgmes_gygl_gxgl = serviceTemplate.selectOne("JGMES_GYGL_GXGL", " and GXGL_GXNUM='" + lyGx + "'");
					String lygxid = jgmes_gygl_gxgl.getStr("GXGL_ID");
					if (StringUtil.isEmpty(lygxid)) {
						result.setIsSuccess(false);
						result.setMessage("δ�ҵ��ù�������Ӧ�Ĺ������ݣ�");
						result.setData(false);
						return result;
					}
					// String lygxid = "b9zRoTeAqkH2PJXJpZr";  // b9zRoTeAqkH2PJXJpZr
					List<DynaBean> jgmes_base_cpgwgx = serviceTemplate.selectList("JGMES_BASE_CPGWGX", "and CPGWGX_CPBH='" + cpbh + "' and CPGWGX_CXBM='" + cxbm + "' and CPGWGX_GXID = '" + lygxid + "'");
					String gxid = "";
					String gwbh ="";
					String gxmc = "";
					String gwmc = "";
					if (jgmes_base_cpgwgx.size()>0){
						gxid = jgmes_base_cpgwgx.get(0).getStr("CPGWGX_GYGXID");//����·�߹���ID
						gwbh = jgmes_base_cpgwgx.get(0).getStr("CPGWGX_GWBH");//��λ���
						gxmc = jgmes_base_cpgwgx.get(0).getStr("CPGWGX_GXNAME");//��������
						gwmc = jgmes_base_cpgwgx.get(0).getStr("CPGWGX_GWMC");//��λ����
					}else{
						result.setIsSuccess(false);
						result.setMessage("��ȡ��Ʒ��λ�����Ӧ��ʧ��");
						result.setData(false);
						return result;
					}
					String gxbh = "GX0031";// �����ţ�д����BGSJ_GXBH
//					String gxmc = "��������";// �������ƣ�д����BGSJ_GXMC
//					String gwbh = "GW000038";// ��λ��ţ�д����BGSJ_GWBH    GW000124
//					String gwmc = "��������";// ��λ���ƣ�д����BGSJ_GWMC    ��������
					String zt = "1";// BGSJ_STATUS_CODE,Ĭ������
					String ztName = "����";
					// String gxid = bean.get(0).getStr("BGSJ_GXID");// ����id
//					String gxid = "zswfeRI8x4PqI3jqcPZ"; // xxp20190306 ����·�߹���ID    b9KB9mNigBPjFqrjoSZ
					String sl = "1";// ������BGSJ_SL
					int gxsxh = 6;// ����˳��ţ�д����BGSJ_GXSXH    //2
					String BGSJ_PDJG_CODE = "PDJG02"; // �ж����
					// String pdjg = TestResults;// �ж������BGSJ_PDJG_CODE
					Boolean re = false;
					if(StringUtil.isEmpty(TestResults)) {
						System.out.println("�ò���ֵ���Ϊ�գ���������");
						result.setErrorCode(2);
						result.setMessage("����ֵ���Ϊ�գ���������");
						result.setIsSuccess(false);
						result.setData(false);
						return result;
					}
					if ("pass".equalsIgnoreCase(TestResults.trim())) {
						re = true;
						BGSJ_PDJG_CODE = "PDJG01";
					}
					// �ӱ�����
					String wlbh="";// "CP20190220144";// BGSJZB_WLBH,���ϱ�ţ�д��
					String wlmc="";// "������";// BGSJZB_WLMC,�������ƣ�д��
					String jcbh = "XM000076";// ���������Ŀ��ţ�����,BGSJZB_JCXMBH     XM000080
					String jcmz = "RSSI����";// ���������Ŀ���ƣ�����,BGSJZB_JCXMMC
					String jcz = BTTestData;// ���������Ŀ���ֵ,BGSJZB_JCZ
					ResultString res = GetBluetoothAddressBySN(barCode);
					String tmh = res.getData();// ������ַ
//				// ά�ޱ���������
//				String fxzmc = "��������վ";// ������***************
//				String gdzt = "��Ч��";// ����״̬��ʼĬ����Ч�У�FXD_GDZT_CODE
//				String wxzt = "��ά��";// FXD_WXZT_NAME��Ĭ�ϴ�ά��
//				String wxztCode = "WXZT01";// FXD_WXZT_CODE,ά��״̬����
//				String wxztid = "O3Az0ws8IcbTVTCfsuv";// FXD_WXZT_ID��ά��״̬id
//				String fhgxid = bean.get(0).getStr("BGSJ_GXID");// ���ع���id
//				String cxid = bean.get(0).getStr("BGSJ_GXID");// ����id
					//��������
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
					mainJsonObject.put("BGSJ_SCRW",rwdh);
					mainJsonObject.put("BGSJ_SCRWID",rwid);
					mainJsonObject.put("BGSJ_STATUS_CODE", zt);
					mainJsonObject.put("BGSJ_STATUS_NAME", ztName);
					String mainJson = mainJsonObject.toString();
					//�ӱ�����
					JSONObject minorJsonObject = new JSONObject();
					minorJsonObject.put("BGSJZB_WLBH", wlbh);
					minorJsonObject.put("BGSJZB_WLMC", wlmc);
					minorJsonObject.put("BGSJZB_JCXMBH", jcbh);
					minorJsonObject.put("BGSJZB_JCXMMC", jcmz);
					minorJsonObject.put("BGSJZB_JCZ", jcz);
					minorJsonObject.put("BGSJZB_CPBH", cpbh);
					minorJsonObject.put("JGMES_PB_BGSJ_ID", zjid);
					minorJsonObject.put("BGSJZB_CPMC", cpmc);
					minorJsonObject.put("BGSJZB_TMH", tmh);
					String minorJson = "["+minorJsonObject.toString()+"]";
					CommonAction ca = new CommonAction();
						JgmesResult<HashMap> map =ca.doJsonSaveBgSjAll(mainJson,minorJson,"",null,JgmesEnumsBgsjLx.LySj,false);
						if(map.IsSuccess) {
							result.setIsSuccess(true);
							result.setData(true);
							return result;
						}else {
							result.setMessage(map.getMessage());
							result.setData(false);
							return result;
						}
				}else {
					result.setData(false);
					result.setMessage(String.format("������:%s���ҵ��������������",barCode));}
			}else{
				System.out.println("�����Ϊ�գ���������");
				result.setErrorCode(1);
				result.setMessage("�����Ϊ�գ���������");
				result.setData(false);
				return result;
			}
		}catch (Exception e){
			e.printStackTrace();
			result.setMessage("ϵͳ�쳣����");
			result.setData(false);
		}
		return result;
	}

	@Override
	public ResultBoolean SaveCLIOTestData(String fileName, String file) {
		// TODO Auto-generated method stub
		ResultBoolean resultBoolean = new ResultBoolean();
		Date nowDate = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyyMM");
		SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat t = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String NowDate = df.format(nowDate);
		String date = d.format(nowDate);
		String time = t.format(nowDate);
		String name = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 19);
		String prefix = fileName.substring(fileName.lastIndexOf(".") + 1);
		String FileUrl = "JE/data/upload/" + NowDate;
		String Furl = GZJDDeviceServiceImpl.class.getResource("/").getPath();
		String[] rootPath = Furl.split("WEB-INF/", 0);
		String fileDic = rootPath[0] + FileUrl + "/";
		String newFileName = name + "." + prefix;
		// String path =fileDic+newFileName;

		String url = fileName + "*/" + FileUrl + "/" + name + "." + prefix;
		File loadFile = new File(fileDic, newFileName); // ���ļ�
		InputStream in = new ByteArrayInputStream(Base64.decode(file));
		byte[] buffer = new byte[1024 * 1024];
		int len = -1;
		boolean b = false;
		try {
			FileOutputStream out = new FileOutputStream(loadFile);
			while (-1 != (len = in.read(buffer, 0, buffer.length))) {
				out.write(buffer, 0, len);
			}
			in.close();
			out.close();
			b = true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			resultBoolean.setMessage("�ϴ��ļ������쳣");
		}
		if (b) {
			String strSql = "INSERT INTO CLIO_TEST_FILE (CLIO_TEST_FILE_ID,FILE_NAME,FILE_UPLOAD_DATE,FILE_UPLOAD_TIME,FILE_SEVICE_FILE_ADRESS) "
					+ "VALUES('" + name + "','" + fileName + "','" + date + "','" + time + "','" + url + "');";
			Long res = pcServiceTemplate.executeSql(strSql);
		}
		return resultBoolean;
	}

	/**
	 * ���� �ַ�������
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
	 * ���� �������Ͷ���
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

}