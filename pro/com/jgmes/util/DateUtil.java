package com.jgmes.util;

import java.util.Calendar;

/**
 * 与日期相关操作工具类
 * @author cj
 *
 */

public class DateUtil {

	/**
	 * 得到指定月的天数
	 * @param year 年
	 * @param month 月
	 * @return 当月天数
	 */
	public  int getMonthLastDay(int year, int month)
	{
		Calendar a = Calendar.getInstance();
		a.set(Calendar.YEAR, year);
		a.set(Calendar.MONTH, month - 1);
		a.set(Calendar.DATE, 1);//把日期设置为当月第一天
		a.roll(Calendar.DATE, -1);//日期回滚一天，也就是最后一天
		int maxDate = a.get(Calendar.DATE);
		return maxDate;
	}
}
