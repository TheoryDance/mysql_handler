package com.grand.mysql_handler.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class TimeUtils {

	public enum TIME_PERIOD {
		START_AND_END_SECOND_BY_HOUR("yyyy-MM-dd HH"),
		START_AND_END_HOUR_BY_TODAY("yyyy-MM-dd"),
		START_AND_END_DAY_BY_THISWEEK("yyyy-wMM"),
		START_AND_END_DAY_BY_THISMONTH("yyyy-MM"),
		START_AND_END_DAY_BY_THISSEASON("yyyy-sM"),
		START_AND_END_DAY_BY_THISYEAR("yyyy");
		private String format;
		private TIME_PERIOD(String format) {
			this.format = format;
		}
		public String getFormat() {
			return this.format;
		}
	}
	
	/**
	 * 通过输入的字符串，获取对应的时间周期类型枚举
	 * @param type "h"|"d"|"w"|"m"|"s"|"y"
	 * @return
	 */
	public static TIME_PERIOD getPeriodByStr(String type) {
		if(StringUtils.isEmpty(type)) {
			return null;
		}
		switch(type.toLowerCase()) {
		case "h":
		case "hour":
			return TIME_PERIOD.START_AND_END_SECOND_BY_HOUR;
		case "d":
		case "day":
			return TIME_PERIOD.START_AND_END_HOUR_BY_TODAY;
		case "w":
		case "week":
			return TIME_PERIOD.START_AND_END_DAY_BY_THISWEEK;
		case "m":
		case "month":
			return TIME_PERIOD.START_AND_END_DAY_BY_THISMONTH;
		case "s":
		case "season":
			return TIME_PERIOD.START_AND_END_DAY_BY_THISSEASON;
		case "y":
		case "year":
			return TIME_PERIOD.START_AND_END_DAY_BY_THISYEAR;
		}
		return null;
	}
	
	/**
	 * 以当前时间作为参考值，确定起止时间<br>
	 *     小时表示当前小时排名(起止秒)<br>
	 *     日表示今日累计排名(起止小时)<br>
	 *     周表示今日累计排名(起止日期)<br>
	 *     月表示本月累计排名(起止日期)<br>
	 *     年表示本年累计排名(起止日期)<br>
	 *  示例：<br>
	 *  getPeriodByType(TIME_PERIOD.START_AND_END_SECOND_BY_HOUR, null)<br>
	 *  getPeriodByType(TIME_PERIOD.START_AND_END_HOUR_BY_TODAY, null)<br>
	 *	getPeriodByType(TIME_PERIOD.START_AND_END_DAY_BY_THISWEEK, null)<br>
	 *	getPeriodByType(TIME_PERIOD.START_AND_END_DAY_BY_THISMONTH, null)<br>
	 *	getPeriodByType(TIME_PERIOD.START_AND_END_DAY_BY_THISYEAR, null)<br>
	 *getPeriodByType(TIME_PERIOD.START_AND_END_SECOND_BY_HOUR, "2019-05-21 05")<br>
	 *	getPeriodByType(TIME_PERIOD.START_AND_END_HOUR_BY_TODAY, "2019-05-09")<br>
	 *	getPeriodByType(TIME_PERIOD.START_AND_END_DAY_BY_THISWEEK, "2019-w10")<br>
	 *	getPeriodByType(TIME_PERIOD.START_AND_END_DAY_BY_THISMONTH, "2019-04")<br>
	 *	getPeriodByType(TIME_PERIOD.START_AND_END_DAY_BY_THISYEAR, "2018")<br>
	 * @param data_type 数据类型：h小时、d日、w周、m月、y年
	 * @param time 可以为null，表示以当前时间为参考值，否则传递的时间必须与参数period的格式保持一致
	 * @return 在传递的参数和不出现异常的情况下，返回的结果是一个size=2的list集合，第一个表示时间段的开始时间，第二个数据表示结束时间
	 */
	public static List<String> getPeriodByType(TIME_PERIOD period, String time) {
		if(period == null) {
			return null;
		}
		Calendar c = Calendar.getInstance();
		// 验证传递的时间参数是否一致
		SimpleDateFormat sdf = new SimpleDateFormat(period.getFormat());
		if(StringUtils.isNotEmpty(time)) {
			try{
				if(period == TIME_PERIOD.START_AND_END_DAY_BY_THISWEEK) { // 周是自己定义的，特殊处理
					String[] strs = time.split("-w");
					c.set(Calendar.YEAR, Integer.parseInt(strs[0]));
					c.set(Calendar.WEEK_OF_YEAR, Integer.parseInt(strs[1]));
				}else if(period == TIME_PERIOD.START_AND_END_DAY_BY_THISSEASON){
					String[] strs = time.split("-s");
					c.set(Calendar.YEAR, Integer.parseInt(strs[0]));
					int season = Integer.parseInt(strs[1]);
					c.set(Calendar.DAY_OF_MONTH, 5);
					switch (season) {
					case 1:
						c.set(Calendar.MONTH, 1);
						break;
					case 2:
						c.set(Calendar.MONTH, 4);
						break;
					case 3:
						c.set(Calendar.MONTH, 7);
						break;
					case 4:
						c.set(Calendar.MONTH, 10);
						break;
					default:
						// 当前月
					}
				}else {
					c.setTime(sdf.parse(time));
				}
			} catch (ParseException e) {
				return null;
			}
		}
		
		List<String> list = new ArrayList<>();
		String start = null;
		String end = null;
		switch(period) {
		case START_AND_END_SECOND_BY_HOUR:
			start = sdf.format(c.getTime()) + ":00:00";
			end = sdf.format(c.getTime()) + ":59:59";
			break;
		case START_AND_END_HOUR_BY_TODAY:
			start = sdf.format(c.getTime()) + " 01";
			c.add(Calendar.DAY_OF_MONTH, 1);
			end = sdf.format(c.getTime()) + " 00";
			break;
		case START_AND_END_DAY_BY_THISWEEK:
			sdf = new SimpleDateFormat("yyyy-MM-dd");
			c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
			start = sdf.format(c.getTime());
			c.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
			end = sdf.format(c.getTime());
			break;
		case START_AND_END_DAY_BY_THISMONTH:
			start = sdf.format(c.getTime()) + "-01";
			end = sdf.format(c.getTime()) + "-31";
			break;
		case START_AND_END_DAY_BY_THISSEASON: 
			sdf = new SimpleDateFormat("yyyy");
			int season = c.get(Calendar.MONTH)/3 + 1; // 得到当前是那个季度
			switch (season) {
			case 1:
				start = sdf.format(c.getTime()) + "-01-01 00";
				end = sdf.format(c.getTime()) + "-03-31 23";
				break;
			case 2:
				start = sdf.format(c.getTime()) + "-04-01 00";
				end = sdf.format(c.getTime()) + "-06-30 23";
				break;
			case 3:
				start = sdf.format(c.getTime()) + "-07-01 00";
				end = sdf.format(c.getTime()) + "-09-30 23";
				break;
			case 4:
				start = sdf.format(c.getTime()) + "-10-01 00";
				end = sdf.format(c.getTime()) + "-12-31 23";
				break;
			default:
			}
			break;
		case START_AND_END_DAY_BY_THISYEAR:
			start = sdf.format(c.getTime()) + "-01-01";
			end = sdf.format(c.getTime()) + "-12-31";
			break;
		default:
			return null;
		}
		list.add(start);
		list.add(end);
		return list;
	}
	
	/**
	 * 获取当前时间
	 * @param type 当前时间的表现形式：h小时，d天，w周，m月，s季度，y年
	 * @return
	 */
	public static String getNow(String type) {
		return getTypeTime(type, null);
	}
	
	public static String getCompleteTimeType(String type) {
		if("h".equalsIgnoreCase(type)) {
			return "hour";
		}else if("d".equalsIgnoreCase(type)){
			return "day";
		}else if("w".equalsIgnoreCase(type)){
			return "week";
		}else if("m".equalsIgnoreCase(type)){
			return "month";
		}else if("y".equalsIgnoreCase(type)){
			return "year";
		}
		return null;
	}
	
	public static String getTypeTime(String type, Date date) {
		Calendar c = Calendar.getInstance();
		if(date != null) {
			c.setTime(date);
		}
		String res = null;
		switch(type) {
		case "h":
		case "hour":
			res = new SimpleDateFormat("yyyy-MM-dd HH").format(c.getTime());
			break;
		case "d":
		case "day":
			res = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
			break;
		case "m":
		case "month":
			res = new SimpleDateFormat("yyyy-MM").format(c.getTime());
			break;
		case "y":
		case "year":
			res = new SimpleDateFormat("yyyy").format(c.getTime());
			break;
		case "w":
		case "week":
			int week = c.get(Calendar.WEEK_OF_YEAR);
			res = new SimpleDateFormat("yyyy").format(c.getTime()) + "-w" + week;
			break;
		case "s":
		case "season":
			int season = c.get(Calendar.MONTH) / 3 + 1;
			res = new SimpleDateFormat("yyyy").format(c.getTime()) + "-s" + season;
			break;
		default:
			res = "";
		}
		return res;
	}
}
