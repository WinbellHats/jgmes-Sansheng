package com.jgmes.service;

import com.je.core.security.SecurityUserHolder;
import com.je.core.service.PCDynaServiceTemplate;
import com.je.core.service.PCServiceTemplate;
import com.je.core.util.JEUUID;
import com.je.core.util.bean.BeanUtils;
import com.je.core.util.bean.DynaBean;
import com.je.rbac.model.Department;
import com.je.rbac.model.EndUser;
import com.je.rbac.service.UserManager;
import com.jgmes.util.JgmesCommon;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * ר�Ŵ���ť��service
 * @author liuc
 * @version 2019-05-05 10:47:02
 */
@Component("jgmesButtonService")
public class JgmesButtonServiceImpl implements JgmesButtonService  {

	/**��̬Bean(DynaBean)�ķ����*/
	private PCDynaServiceTemplate serviceTemplate;
	/**ʵ��Bean���������,��Ҫ����SQL*/
	private PCServiceTemplate pcServiceTemplate;
	/**�û������*/
	private UserManager userManager;

	@Override
	public void load(){
		System.out.println("hello serviceimpl");
	}

	@Override
	public String doSaveCPGZTemplate(String[] ids) {
		if (ids != null && ids.length > 0) {
			JgmesCommon jgmesCommon = new JgmesCommon(null, serviceTemplate);
			for (int i = 0; i < ids.length; i++) {
				DynaBean cptmyyggDynaBean = serviceTemplate.selectOne("JGMES_BASE_CPTMYYGG", " and JGMES_BASE_CPTMYYGG_ID = '" + ids[i] + "'");
				if(cptmyyggDynaBean!=null){

					//��ѯһ�£����������Ƿ��Ѿ�����Ϊģ��
					List<DynaBean> cptmyyggDynaBeanList = serviceTemplate.selectList("JGMES_BASE_CPTMYYGG"," and CPTMYYGG_YYGGMC = '"+cptmyyggDynaBean.getStr("CPTMYYGG_YYGGMC")+"' and CPTMYYGG_NO_CODE = '1' ");
					if(cptmyyggDynaBeanList!=null&&cptmyyggDynaBeanList.size()>0){
						return "\""+cptmyyggDynaBean.getStr("CPTMYYGG_YYGGMC")+":�ò�Ʒ����Ӧ�ù����Ѿ���ģ����\"";
					}else{
						cptmyyggDynaBean.set("JGMES_BASE_CPTMYYGG_ID", JEUUID.uuid());
						cptmyyggDynaBean.set("CPTMYYGG_YYGGMC", cptmyyggDynaBean.getStr("CPTMYYGG_YYGGMC"));
						cptmyyggDynaBean.set("CPTMYYGG_YYGZBH", serviceTemplate.buildCode("CPTMYYGG_YYGZBH", "JGMES_BASE_CPTMYYGG", cptmyyggDynaBean));
						cptmyyggDynaBean.set("CPTMYYGG_STATUS_CODE",2);//��Ϊģ��ʱ����Ϊ������
						cptmyyggDynaBean.set("CPTMYYGG_STATUS_NAME","������");//��Ϊģ��ʱ����Ϊ������
						//ģ������Ӧ�ù����в�Ʒ����Ӧ���ǿյ�
						cptmyyggDynaBean.remove("CPTMYYGG_CPBH");
						cptmyyggDynaBean.remove("CPTMYYGG_CPNAME");
						cptmyyggDynaBean.remove("JGMES_BASE_PRODUCTDATA_ID");
						//����ģ��
						DynaBean dic = jgmesCommon.getDic("JGMES_YES_NO","1");
						if(dic!=null){
							cptmyyggDynaBean.set("CPTMYYGG_NO_CODE",dic.get("DICTIONARYITEM_ITEMCODE"));
							cptmyyggDynaBean.set("CPTMYYGG_NO_NAME",dic.get("DICTIONARYITEM_ITEMNAME"));
						}
						serviceTemplate.insert(cptmyyggDynaBean);
					}
				}

			}
		}
		return "\"����ģ��ɹ���\"";
	}

	@Override
	public String doSaveCpTmYYGG(String[] ids, String cpbm) {
		if(ids!=null&&ids.length>0&&cpbm!=null&&!"".equals(cpbm)){
			JgmesCommon jgmesCommon = new JgmesCommon(null, serviceTemplate);

			//��ȡ��Ʒ����
			DynaBean cpDynaBean = serviceTemplate.selectOne("JGMES_BASE_PRODUCTDATA"," and PRODUCTDATA_BH = '"+cpbm+"'");
			if(cpDynaBean!=null){
				StringBuffer yyggmc = new StringBuffer();
				for (int i = 0; i < ids.length; i++) {
					DynaBean cptmyyggDynaBean = serviceTemplate.selectOne("JGMES_BASE_CPTMYYGG", " and JGMES_BASE_CPTMYYGG_ID = '" + ids[i] + "'");
					List<DynaBean> cptmyyggDynaBeanList = serviceTemplate.selectList("JGMES_BASE_CPTMYYGG", " and CPTMYYGG_CPBH = '"+cpbm+"' and CPTMYYGG_YYGGMC = '"+cptmyyggDynaBean.getStr("CPTMYYGG_YYGGMC")+"'");
					if(cptmyyggDynaBeanList!=null&&cptmyyggDynaBeanList.size()>0){
						yyggmc.append(cptmyyggDynaBean.getStr("CPTMYYGG_YYGGMC")+"��");
					}else{
						if(cptmyyggDynaBean!=null){
							cptmyyggDynaBean.set("JGMES_BASE_CPTMYYGG_ID", JEUUID.uuid());
							cptmyyggDynaBean.set("CPTMYYGG_YYGZBH", serviceTemplate.buildCode("CPTMYYGG_YYGZBH", "JGMES_BASE_CPTMYYGG", cptmyyggDynaBean));

							//ģ������Ӧ�ù����в�Ʒ����Ӧ���ǿյ�
							cptmyyggDynaBean.set("CPTMYYGG_CPBH",cpDynaBean.getStr("PRODUCTDATA_BH"));
							cptmyyggDynaBean.set("CPTMYYGG_CPNAME",cpDynaBean.getStr("PRODUCTDATA_NAME"));
							cptmyyggDynaBean.set("JGMES_BASE_PRODUCTDATA_ID",cpDynaBean.getStr("JGMES_BASE_PRODUCTDATA_ID"));

							//����ģ��
							DynaBean dic = jgmesCommon.getDic("JGMES_YES_NO","0");
							if(dic!=null){
								cptmyyggDynaBean.set("CPTMYYGG_NO_CODE",dic.get("DICTIONARYITEM_ITEMCODE"));
								cptmyyggDynaBean.set("CPTMYYGG_NO_NAME",dic.get("DICTIONARYITEM_ITEMNAME"));
							}

							serviceTemplate.insert(cptmyyggDynaBean);
						}
					}
				}
				if(yyggmc.length()>0){
					return "\""+yyggmc.toString()+":�ò�Ʒ����Ӧ�ù����Ѿ�����\"";
				}
			}else{
				return "\"�����Ʒ�����Ƿ񱣴棡\"";
			}
		}else{
			return "\"��ѡ��һ�����ݣ�\"";
		}
		return "\"��Ʒ����Ӧ�ù�����ɹ���\"";
	}

	@Override
	public String doSavehgxq(HttpServletRequest request,String id,String[] zbidList) {
		JgmesCommon jgmesCommon = new JgmesCommon(request, serviceTemplate);
		SimpleDateFormat sdf =   new SimpleDateFormat( "yyyy-MM-dd" );
		DynaBean hgapzbDynaBean = null;
		String ddhm = null;
		if(id!=null&&!"".equals(id)){
			hgapzbDynaBean = serviceTemplate.selectOne("JGMES_PB_HGAPZB"," and JGMES_PB_HGAPZB_ID = '"+id+"'");
		}else{
			List<DynaBean> list = serviceTemplate.selectList("JGMES_PB_HGAPZB"," order by SY_CREATETIME desc",0,1);
			if(list!=null&&list.size()>0){
				hgapzbDynaBean = list.get(0);
			}
		}
		String message = "";
		if(hgapzbDynaBean!=null){
			ddhm = hgapzbDynaBean.getStr("HGAPZB_DDHM");
			int sl = hgapzbDynaBean.getInt("HGAPZB_ZZSL");
			if(sl==0) {
				return "\"�����װջ����Ϊ0\"";
			}
			List<DynaBean> hgapzbxqDynaBeanList = serviceTemplate.selectList("JGMES_PB_HGAPZBXQ", " and HGAPZBXQ_HGAPZBZJID = '"+hgapzbDynaBean.getStr("JGMES_PB_HGAPZB_ID")+"'");
			if(hgapzbxqDynaBeanList!=null&&hgapzbxqDynaBeanList.size()>0) {
				sl = sl-hgapzbxqDynaBeanList.size();
			}

			//��ȡջ������
			List<DynaBean> zbtmList = null;
			if(zbidList!=null&&zbidList.length>0){
				for(int i=0;i<zbidList.length;i++) {
					if(i>=sl) {
						message =  "\"ջ���������ܳ���װջ������\"";
						break;
					}
					DynaBean zbtm = serviceTemplate.selectOne("JGMES_BASE_GDCPTM"," and JGMES_BASE_GDCPTM_ID = '"+zbidList[i]+"'");
					DynaBean hgapzbxqDynaBean = new DynaBean();
					hgapzbxqDynaBean.set("JGMES_PB_HGAPZBXQ_ID",JEUUID.uuid());
					hgapzbxqDynaBean.set(BeanUtils.KEY_TABLE_CODE,"JGMES_PB_HGAPZBXQ");
					//����
					hgapzbxqDynaBean.set("HGAPZBXQ_RQ",hgapzbDynaBean.getStr("HGAPZB_RQ"));
					//�����
					hgapzbxqDynaBean.set("HGAPZBXQ_FXD",hgapzbDynaBean.getStr("HGAPZB_FXD"));
					//������
					hgapzbxqDynaBean.set("HGAPZBXQ_HGBH",hgapzbDynaBean.getStr("HGAPZB_HGBH"));
					//�������ӱ�����ID
					hgapzbxqDynaBean.set("HGAPZBXQ_HGAPZBZJID",hgapzbDynaBean.getStr("JGMES_PB_HGAPZB_ID"));
					//ջ����
					hgapzbxqDynaBean.set("HGAPZBXQ_ZBTMH",zbtm.getStr("GDCPTM_TMH"));
					//������
					hgapzbxqDynaBean.set("HGAPZBXQ_DDHM",zbtm.getStr("GDCPTM_DDHM"));
					//�ڲ�������
					hgapzbxqDynaBean.set("HGAPZBXQ_LCKH", zbtm.getStr("GDCPTM_LCKH"));

					jgmesCommon.setDynaBeanInfo(hgapzbxqDynaBean);

					serviceTemplate.insert(hgapzbxqDynaBean);
				}
			}else{

				//�������񵥹��˵�
				String sql = "SELECT * FROM JGMES_BASE_GDCPTM " +
						"WHERE GDCPTM_TMLX_CODE = 'TMLX05' " +
						"AND GDCPTM_DDHM = '"+ddhm+"'  " +
						"AND ( " +
						"SELECT " +
						"count(*) " +
						"FROM " +
						"JGMES_PB_HGAPZBXQ " +
						"WHERE " +
						"JGMES_BASE_GDCPTM.GDCPTM_TMH = JGMES_PB_HGAPZBXQ.HGAPZBXQ_ZBTMH " +
						") = 0 " +
						"and " +
						"(" +
						"select (case JGMES_XTGL_XTCS.XTCS_CSZ " +
						"when  1  then (select ZBBD_NO_CODE from GMES_PB_ZBBD where JGMES_BASE_GDCPTM.GDCPTM_TMH = GMES_PB_ZBBD.ZBBD_ZBTMH limit 1) " +
						"when  2  then 1 " +
						"else  1 " +
						"end) from JGMES_XTGL_XTCS where XTCS_CXFL1_CODE = 'CXFL06' and XTCS_CXFL2_CODE = 'HGAPZBLX' " +
						")=1   order by GDCPTM_TMH";


				zbtmList = serviceTemplate.selectListBySql(sql);

				//���������񵥹��˵�
				String sql2 = "SELECT * FROM JGMES_BASE_GDCPTM " +
						"WHERE GDCPTM_TMLX_CODE = 'TMLX05' " +
						"AND ( " +
						"SELECT " +
						"count(*) " +
						"FROM " +
						"JGMES_PB_HGAPZBXQ " +
						"WHERE " +
						"JGMES_BASE_GDCPTM.GDCPTM_TMH = JGMES_PB_HGAPZBXQ.HGAPZBXQ_ZBTMH " +
						") = 0 " +
						"and " +
						"(" +
						"select (case JGMES_XTGL_XTCS.XTCS_CSZ " +
						"when  1  then (select ZBBD_NO_CODE from GMES_PB_ZBBD where JGMES_BASE_GDCPTM.GDCPTM_TMH = GMES_PB_ZBBD.ZBBD_ZBTMH limit 1) " +
						"when  2  then 1 " +
						"else  1 " +
						"end) from JGMES_XTGL_XTCS where XTCS_CXFL1_CODE = 'CXFL06' and XTCS_CXFL2_CODE = 'HGAPZBLX' " +
						")=1   order by GDCPTM_TMH";


				List<DynaBean> zbtmList1 = serviceTemplate.selectListBySql(sql2);
				int j = 0;
				if(zbtmList!=null&&zbtmList.size()>0){
					for(int i=0;i<sl;i++){
						if(i>=sl) {
							message =  "\"ջ���������ܳ���װջ������\"";
							break;
						}
						DynaBean zbtm = null;
						if(i<zbtmList.size()){
							zbtm = zbtmList.get(i);
						}else{
							if(zbtmList1!=null&&zbtmList1.size()>0){
								zbtm  = zbtmList1.get(j);
								j++;
							}else{
								break;
							}

						}
						DynaBean hgapzbxqDynaBean = new DynaBean();
						hgapzbxqDynaBean.set("JGMES_PB_HGAPZBXQ_ID",JEUUID.uuid());
						hgapzbxqDynaBean.set(BeanUtils.KEY_TABLE_CODE,"JGMES_PB_HGAPZBXQ");
						//����
						hgapzbxqDynaBean.set("HGAPZBXQ_RQ",hgapzbDynaBean.getStr("HGAPZB_RQ"));
						//�����
						hgapzbxqDynaBean.set("HGAPZBXQ_FXD",hgapzbDynaBean.getStr("HGAPZB_FXD"));
						//������
						hgapzbxqDynaBean.set("HGAPZBXQ_HGBH",hgapzbDynaBean.getStr("HGAPZB_HGBH"));
						//�������ӱ�����ID
						hgapzbxqDynaBean.set("HGAPZBXQ_HGAPZBZJID",hgapzbDynaBean.getStr("JGMES_PB_HGAPZB_ID"));
						//ջ����
						hgapzbxqDynaBean.set("HGAPZBXQ_ZBTMH",zbtm.getStr("GDCPTM_TMH"));
						//������
						hgapzbxqDynaBean.set("HGAPZBXQ_DDHM",zbtm.getStr("GDCPTM_DDHM"));
						//�ڲ�������
						hgapzbxqDynaBean.set("HGAPZBXQ_LCKH", zbtm.getStr("GDCPTM_LCKH"));

						jgmesCommon.setDynaBeanInfo(hgapzbxqDynaBean);

						serviceTemplate.insert(hgapzbxqDynaBean);
					}
				}
			}



			
			//��д�������ӱ���Ѱ�������
			hgapzbxqDynaBeanList = serviceTemplate.selectList("JGMES_PB_HGAPZBXQ", " and HGAPZBXQ_HGAPZBZJID = '"+hgapzbDynaBean.getStr("JGMES_PB_HGAPZB_ID")+"'");
			if(hgapzbxqDynaBeanList!=null&&hgapzbxqDynaBeanList.size()>0) {
				hgapzbDynaBean.set("HGAPZB_YAPSL", hgapzbxqDynaBeanList.size());
			}else {
				hgapzbDynaBean.set("HGAPZB_YAPSL", 0);
			}
			serviceTemplate.update(hgapzbDynaBean);
		}
		if(message!=null&&!"".equals(message)) {
			return message;
		}else {
			return "\"ջ����ӳɹ���\"";
		}
	}

	@Override
	public String doDeletehgxq(String[] id) {
		if(id!=null&&id.length>0){
			String ids = String.join("','",id);
			serviceTemplate.deleteByWehreSql("JGMES_PB_HGAPZBXQ"," and HGAPZBXQ_HGAPZBZJID = ('"+ids+"')");
		}
		return "\"ɾ���ɹ���\"";
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
	@Resource(name="PCDynaServiceTemplate")
	public void setServiceTemplate(PCDynaServiceTemplate serviceTemplate) {
		this.serviceTemplate = serviceTemplate;
	}
	@Resource(name="PCServiceTemplateImpl")
	public void setPcServiceTemplate(PCServiceTemplate pcServiceTemplate) {
		this.pcServiceTemplate = pcServiceTemplate;
	}
	@Resource(name="userManager")
	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}
}