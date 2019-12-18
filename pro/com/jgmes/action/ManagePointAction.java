package com.jgmes.action;

import javax.annotation.Resource;

import com.je.develop.action.DataImplAction;
import org.python.google.common.collect.Lists;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.je.core.action.DynaAction;
import com.je.core.util.bean.DynaBean;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
/**
 * 管理目标
 * @author trc
 * @version 2018-12-28 15:42:13
 * @see /jgmes/managePointAction!load.action
 */
/**
 * @author Administrator
 *
 */
@Component("managePointAction")
@Scope("prototype")
public class ManagePointAction extends DynaAction  {

	private static final long serialVersionUID = 1L;
	public static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	
	/* 
	 * 生成管理目标
	 */
	public void doSave() {
		try {
			System.out.println("pkValue:"+pkValue);
			DynaBean bean = serviceTemplate.selectOneByPk("GLMB_HEAD", pkValue);
			if (bean!=null) {
				String sjd=bean.getStr("HEAD_SJD");
				String type=bean.getStr("HEAD_SJLX_ID");
				String st=bean.getStr("HEAD_ST");
				String[] time = sjd.split(" 到  ");
				String[] sts = st.split(",");
				String start = time[0];
				String end = time[1];
				int syear = Integer.valueOf(start.substring(0, 4));
				//System.out.println("type:"+type);
				if (type.equals("1")) {
					// 年
					int eyear = Integer.valueOf(end.substring(0, 4));
					for (int i = syear; i <= eyear; i++) {
						//System.out.println(i);
						for (int j=0;j<sts.length;j++) {
							DynaBean point = new DynaBean("JGMES_MANAGE_POINT", true);
							serviceTemplate.buildModelCreateInfo(point);
							point.set("POINT_YEAR", syear + "");//年度
							point.set("POINT_SJD", i + "");//时间段
							point.set("POINT_ST", sts[j]);//实体
							point.set("POINT_ST_ID", bean.getStr("HEAD_ST"));//实体ID
							point.set("POINT_CPFL_NAME", bean.getStr("HEAD_CPFL_NAME"));//产品分类
							point.set("POINT_CPFL_ID", bean.getStr("HEAD_CPFL_ID"));//产品分类_ID
							point.set("POINT_KH_NAME", bean.getStr("HEAD_KH_NAME"));//客户
							point.set("POINT_KH_ID", bean.getStr("HEAD_KH_ID"));//客户ID
							point.set("POINT_MBZ", bean.getStr("HEAD_MBZ"));//目标值
							point.set("POINT_BZ", bean.getStr("HEAD_BZ"));//备注
							point.set("POINT_DW", bean.getStr("HEAD_DW"));//单位
							point.set("POINT_SJLX_ID", bean.getStr("HEAD_SJLX_ID"));//时间类型_ID
							point.set("POINT_SJLX_NAME", bean.getStr("HEAD_SJLX_NAME"));//时间类型
							point.set("TYPE_CODE", bean.getStr("TYPE_CODE"));//管理目标类型
							point.set("TYPE_NAME", bean.getStr("TYPE_NAME"));//管理目标类型_NAME
							point.set("GLMB_HEAD_ID", pkValue);//管理目标表头_外键ID
							serviceTemplate.insert(point);							
						}						
					}
				}
				if (type.equals("2")) {
					// 月
					List<String> months = getMonthBetween(start, end);
					for (String month : months) {
						for (int j=0;j<sts.length;j++) {
							DynaBean point = new DynaBean("JGMES_MANAGE_POINT", true);
							serviceTemplate.buildModelCreateInfo(point);
							point.set("POINT_YEAR", syear + "");//年度
							point.set("POINT_SJD", month);//时间段
							point.set("POINT_ST", sts[j]);//实体
							point.set("POINT_ST_ID", bean.getStr("HEAD_ST"));//实体ID
							point.set("POINT_CPFL_NAME", bean.getStr("HEAD_CPFL_NAME"));//产品分类
							point.set("POINT_CPFL_ID", bean.getStr("HEAD_CPFL_ID"));//产品分类_ID
							point.set("POINT_KH_NAME", bean.getStr("HEAD_KH_NAME"));//客户
							point.set("POINT_KH_ID", bean.getStr("HEAD_KH_ID"));//客户ID
							point.set("POINT_MBZ", bean.getStr("HEAD_MBZ"));//目标值
							point.set("POINT_BZ", bean.getStr("HEAD_BZ"));//备注
							point.set("POINT_DW", bean.getStr("HEAD_DW"));//单位
							point.set("POINT_SJLX_ID", bean.getStr("HEAD_SJLX_ID"));//时间类型_ID
							point.set("POINT_SJLX_NAME", bean.getStr("HEAD_SJLX_NAME"));//时间类型
							point.set("TYPE_CODE", bean.getStr("TYPE_CODE"));//管理目标类型
							point.set("TYPE_NAME", bean.getStr("TYPE_NAME"));//管理目标类型_NAME
							point.set("GLMB_HEAD_ID", pkValue);//管理目标表头_外键ID
							serviceTemplate.insert(point);
						}
					}
				}

				if (type.equals("3")) {
					//周
					List<String> weeks = getWeekBetween(start, end);
					System.out.println(weeks.toString());
					for (String week : weeks) {
						System.out.println(week);
						for (int j=0;j<sts.length;j++) {
							DynaBean point = new DynaBean("JGMES_MANAGE_POINT", true);
							serviceTemplate.buildModelCreateInfo(point);
							point.set("POINT_YEAR", syear + "");//年度
							point.set("POINT_SJD", week);//时间段
							point.set("POINT_ST", sts[j]);//实体
							point.set("POINT_ST_ID", bean.getStr("HEAD_ST"));//实体ID
							point.set("POINT_CPFL_NAME", bean.getStr("HEAD_CPFL_NAME"));//产品分类
							point.set("POINT_CPFL_ID", bean.getStr("HEAD_CPFL_ID"));//产品分类_ID
							point.set("POINT_KH_NAME", bean.getStr("HEAD_KH_NAME"));//客户
							point.set("POINT_KH_ID", bean.getStr("HEAD_KH_ID"));//客户ID
							point.set("POINT_MBZ", bean.getStr("HEAD_MBZ"));//目标值
							point.set("POINT_BZ", bean.getStr("HEAD_BZ"));//备注
							point.set("POINT_DW", bean.getStr("HEAD_DW"));//单位
							point.set("POINT_SJLX_ID", bean.getStr("HEAD_SJLX_ID"));//时间类型_ID
							point.set("POINT_SJLX_NAME", bean.getStr("HEAD_SJLX_NAME"));//时间类型
							point.set("TYPE_CODE", bean.getStr("TYPE_CODE"));//管理目标类型
							point.set("TYPE_NAME", bean.getStr("TYPE_NAME"));//管理目标类型_NAME
							point.set("GLMB_HEAD_ID", pkValue);//管理目标表头_外键ID
							serviceTemplate.insert(point);
						}
					}
				}
				if (type.equals("4")) {
					// 日
					List<String> days = getDayBetween(start, end);
					for (String day : days) {
						for (int j=0;j<sts.length;j++) {
							DynaBean point = new DynaBean("JGMES_MANAGE_POINT", true);
							serviceTemplate.buildModelCreateInfo(point);
							point.set("POINT_YEAR", syear + "");//年度
							point.set("POINT_SJD", day);//时间段
							point.set("POINT_ST", sts[j]);//实体
							point.set("POINT_ST_ID", bean.getStr("HEAD_ST"));//实体ID
							point.set("POINT_CPFL_NAME", bean.getStr("HEAD_CPFL_NAME"));//产品分类
							point.set("POINT_CPFL_ID", bean.getStr("HEAD_CPFL_ID"));//产品分类_ID
							point.set("POINT_KH_NAME", bean.getStr("HEAD_KH_NAME"));//客户
							point.set("POINT_KH_ID", bean.getStr("HEAD_KH_ID"));//客户ID
							point.set("POINT_MBZ", bean.getStr("HEAD_MBZ"));//目标值
							point.set("POINT_BZ", bean.getStr("HEAD_BZ"));//备注
							point.set("POINT_DW", bean.getStr("HEAD_DW"));//单位
							point.set("POINT_SJLX_ID", bean.getStr("HEAD_SJLX_ID"));//时间类型_ID
							point.set("POINT_SJLX_NAME", bean.getStr("HEAD_SJLX_NAME"));//时间类型
							point.set("TYPE_CODE", bean.getStr("TYPE_CODE"));//管理目标类型
							point.set("TYPE_NAME", bean.getStr("TYPE_NAME"));//管理目标类型_NAME
							point.set("GLMB_HEAD_ID", pkValue);//管理目标表头_外键ID
							serviceTemplate.insert(point);
						}
					}
				}
			}

			toWrite(jsonBuilder.returnSuccessJson("\"处理成功\""));
		} catch (Exception e) {
			e.printStackTrace();
			toWrite(jsonBuilder.returnFailureJson("\"+后台出现错误" + e.getMessage() + "\""));
		}
	}

	private static List<String> getMonthBetween(String minDate, String maxDate) throws ParseException {
		ArrayList<String> result = new ArrayList<String>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");// 格式化为年月
		Calendar min = Calendar.getInstance();
		Calendar max = Calendar.getInstance();
		min.setTime(sdf.parse(minDate));
		min.set(min.get(Calendar.YEAR), min.get(Calendar.MONTH), 1);
		max.setTime(sdf.parse(maxDate));
		max.set(max.get(Calendar.YEAR), max.get(Calendar.MONTH), 2);
		Calendar curr = min;
		while (curr.before(max)) {
			result.add(sdf.format(curr.getTime()));
			curr.add(Calendar.MONTH, 1);
		}
		return result;
	}

	private static List<String> getDayBetween(String minDate, String maxDate) throws ParseException {
		ArrayList<String> result = new ArrayList<String>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");// 格式化为年月
		Calendar min = Calendar.getInstance();
		Calendar max = Calendar.getInstance();
		min.setTime(sdf.parse(minDate));
		min.set(min.get(Calendar.YEAR), min.get(Calendar.MONTH),  min.get(Calendar.DATE));
		max.setTime(sdf.parse(maxDate));
		max.set(max.get(Calendar.YEAR), max.get(Calendar.MONTH), max.get(Calendar.DATE));
		max.add(Calendar.DATE, 1);
		Calendar curr = min;
		while (curr.before(max)) {
			result.add(sdf.format(curr.getTime()));
			curr.add(Calendar.DATE, 1);
		}
		return result;
	}
	private static List<String> getWeekBetween(String minDate, String maxDate) throws ParseException {
		ArrayList<String> result = new ArrayList<String>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");// 格式化为年月
		Calendar min = Calendar.getInstance();
		Calendar max = Calendar.getInstance();
		min.setTime(sdf.parse(minDate));
		min.set(min.get(Calendar.YEAR), min.get(Calendar.MONTH),  min.get(Calendar.DATE));
		max.setTime(sdf.parse(maxDate));
		//max.set(max.get(Calendar.YEAR), max.get(Calendar.MONTH), max.get(Calendar.DATE));
		//max.add(Calendar.DATE, 1);
		int startweek = min.get(Calendar.WEEK_OF_YEAR);
		int endweek = max.get(Calendar.WEEK_OF_YEAR);
		System.out.println(startweek+"/"+endweek);
		for (int i = startweek; i <= endweek; i++) {
			result.add(i+"");
		}
		return result;
	}

	public static void main(String[] args) throws ParseException {
		String start = "2018-05-05";
		String end = "2018-06-01";
		List<String> months = getWeekBetween(start, end);
		for (String month : months) {
			System.out.println(month);
		}
	}
	
}