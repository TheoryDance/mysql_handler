package com.grand.mysql_handler.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;

import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @Description: (系统工具类)
 * @Author gaochao
 * @date   2018-4-8 上午10:26:39
 *	@version V1.0 
 */
@Slf4j
public class SystemUtils {

	public final static int MINUTE_MONITOR = 40; //判定条件
	public final static int DATA_PRODUCE_MINUTE = 10; // 数据生成时间

	/**
	 * 判断当前时间是否正在生产数据过程中
	 */
	public static boolean isMakingData() {
		return LocalTime.now().getMinute()<=DATA_PRODUCE_MINUTE ? true : false;
	}
	
	/**
	 * 
	* @Title: responseBody
	* @Description: (接口返回的统一格式)
	* @param   设定文件
	* @return JSONObject    返回类型
	 */
	public static JSONObject responseBody(int code,String msg,Object data){
		JSONObject resObj = new JSONObject();
		resObj.put("code", code);
		resObj.put("msg", msg);
		if((data+"").trim().equals("{}")){
			resObj.put("data", "{}");
			return resObj;
		}else if((data+"").trim().equals("null")){
			resObj.put("data", "{}");
			return resObj;
		}else if(JSONArray.fromObject(data).size()==0){
			resObj.put("data", "{}");
			return resObj;
		}else if((JSONArray.fromObject(data).get(0)+"").equals("null")){
			resObj.put("data", "{}");
			return resObj;
		}else {
			resObj.put("data", data);
		}
		return resObj;
	}
	public static JSONObject responseBody(int code,String msg,Object data,Map<String,Object> header){
		JSONObject resObj = new JSONObject();
		resObj.put("code", code);
		resObj.put("msg", msg);
		if(header != null) {
			resObj.putAll(header);
		}
		if((data+"").trim().equals("{}")){
			resObj.put("data", "{}");
			return resObj;
		}else if((data+"").trim().equals("null")){
			resObj.put("data", "{}");
			return resObj;
		}else if(JSONArray.fromObject(data).size()==0){
			resObj.put("data", "{}");
			return resObj;
		}else if((JSONArray.fromObject(data).get(0)+"").equals("null")){
			resObj.put("data", "{}");
			return resObj;
		}else {
			resObj.put("data", data);
		}
		return resObj;
	}
	
	/**
	 * 
	* @Title: getNowDate
	* @Description: (获取系统实时时间，格式由调用者传入)
	* @return String    返回类型
	 */
	public static String getNowDate(String time, String format,String type, int num) {
		String format2 = null;
		try {
			SimpleDateFormat sf = new SimpleDateFormat(format);
			Calendar cal = Calendar.getInstance();
			if(!"null".equals(time+"") && time!=""){
				 java.util.Date d = sf.parse(time);
				 cal.setTime(d);
			}
			if(type!=null){
			     //使用Calendar  进行时间的加减运算
				if (type.equals("SECOND")) {
					cal.add(Calendar.SECOND, num);
				} else if (type.equals("MINUTE")) {
					cal.add(Calendar.MINUTE, num);
				} else if (type.equals("HOUR")) {
					cal.add(Calendar.HOUR, num);
				} else if (type.equals("DAY")) {
					cal.add(Calendar.DAY_OF_MONTH, num);
				} else if (type.equals("MONTH")) {
					cal.add(Calendar.MONTH, num);
				} else if (type.equals("YEAR")) {
					cal.add(Calendar.YEAR, num);
				}
			}
            Date time2 = cal.getTime();
			format2 = sf.format(time2).toString();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
		return format2;
	}
	
	public static String getNowDate(String format) {
		return new SimpleDateFormat(format).format(new Date());
	}
	
	/**
	 * @Description: (设置系统实时时间，格式由调用者传入)
	 * @return String    返回类型
	 */
	public static String  setDate(String time, String format, String type, int num){
		String format2 = null;
		try {
			SimpleDateFormat sf = new SimpleDateFormat(format);
			Calendar cal = Calendar.getInstance();
			if(StringUtils.isNotEmpty(time)){
				 java.util.Date d = sf.parse(time);
				 cal.setTime(d);
			}/*else{
				String d2 = sf.format(new Date());
				cal.setTime(sf.parse(d2));
			}*/
			if(type!=null){
			     //使用Calendar  进行时间的加减运算
				if (type.equals("SECOND")) {
					cal.set(Calendar.SECOND, num);
				} else if (type.equals("MINUTE")) {
					cal.set(Calendar.MINUTE, num);
				} else if (type.equals("HOUR")) {
					cal.set(Calendar.HOUR_OF_DAY, num);
				} else if (type.equals("DATE")) {
					cal.set(Calendar.DAY_OF_MONTH, num);
				} else if (type.equals("MONTH")) {
					cal.set(Calendar.MONTH, num);
				} else if (type.equals("YEAR")) {
	            	cal.set(Calendar.YEAR, num);
	            }
			}
            Date time2 = cal.getTime();
			format2 = sf.format(time2).toString();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
		return format2;
	}
	
	
	/**
	 * 
	* @Title: getDataHour
	* @Description: (小时数据获取判定)
	* @param    设定文件
	* @return String    返回类型
	 */
	public static String getDataHour(){
		// 判断当前时间是否是前10分钟
		Calendar c = Calendar.getInstance();
		int minute = c.get(Calendar.MINUTE);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
		if(minute < SystemUtils.MINUTE_MONITOR){
			c.add(Calendar.HOUR_OF_DAY, -1);
		}
		return sdf.format(c.getTime());
	}

	/**
	 * 
	 * @Title: getTimeDifference
	 * @Description: (小时数据获取判定)
	 * @param    设定文件
	 * @return String    返回类型
	 */
	public static int getTimeDifference(String stime,String etime) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date date1=new Date();
		Date date2=new Date();
		try {
			date1 = format.parse(etime);
			date2 = format.parse(stime);
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		return (int) ((date1.getTime() - date2.getTime()) / (1000*3600*24));
	}
	/**
	 * 获取某一年的所有月份
	 * @param year
	 * @return
	 */
	public static List<String> getMonths(String year){
		 String[] months= {"01","02","03","04","05","06","07","08","09","10","11","12"};
		 List<String> list = Stream.of(months).map(x -> year+"-"+x).collect(Collectors.toList());
		 return list;
	}
	
    /**
     * 返回两个时间的之间的集合
     */
 	public static List<String> getDates(String type,String sdate,String edate) throws Exception{
		String timeFrom="";
		Long oneDay=null;
		if(type=="HOUR") {
			timeFrom="yyyy-MM-dd HH";
			oneDay= 1000 * 60 * 60 * 1l;  
		}
		if(type=="DAY") {
			timeFrom="yyyy-MM-dd";
			oneDay = 1000 * 60 * 60 * 24l;  
		}
		
		DateFormat df_ = new SimpleDateFormat(timeFrom);
		Date startDate = df_.parse(sdate);
		
		Date endDate = df_.parse(edate);
		
		Calendar start = Calendar.getInstance();  
	    start.setTime(startDate); 
	    Long startTIme = start.getTimeInMillis(); 
	    
	    List<String> list=new ArrayList<>();
	    
	    
	    Calendar end = Calendar.getInstance();  
	    end.setTime(endDate);  
	    Long endTime = end.getTimeInMillis();  
	   
	  
	    Long time = startTIme;  
	    while (time <= endTime) {  
	        Date d = new Date(time);  
	        DateFormat df = new SimpleDateFormat(timeFrom);  
	        list.add(df.format(d));
	        time += oneDay;  
	    }  
	    
	    return list;
	}
	
	
	
	public static List<Map<String, Object>> data(List<Map<String, Object>> list,String stime,String etime){
		
		return null;
	}
	
	public static String getWeekStartDay() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_WEEK, 1);
		c.add(Calendar.DAY_OF_MONTH, 1);
		return sdf.format(c.getTime());
	}

	public static boolean isLocalWeek(String time) {
		String stime = getWeekStartDay();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		boolean res = false;
		try {
			res = sdf.parse(time).getTime() >= sdf.parse(stime).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return res;
	}

	public static boolean isLocalMonth(String time) {
		String month = new SimpleDateFormat("yyyy-MM").format(new Date());
		return time.startsWith(month);
	}
	
	/**
	 * 给没有的属性进行补null
	 * @param list 需要被填充的集合
	 * @param attrs 属性集合
	 */
	public static void fillEmpty(Collection<? extends Map<String,Object>> list, Collection<String> attrs) {
		for(Map<String,Object> item: list) {
			for (String attr : attrs) {
				if(!item.keySet().contains(attr)) {
					item.put(attr, null);
				}
			}
		}
	}
	/**
	 * @Author JasonPeng
	 * @Description(给没有的属性进行补null)
	 * @Date 2019年5月17日下午5:22:01
	 * @param map
	 * @param attrs
	 */
	public static void fillEmptyByMap(Map<String,Object> map, Collection<String> attrs) {
			for (String attr : attrs) {
				if(!map.keySet().contains(attr)) {
					map.put(attr, null);
				}
			}
	}
	
	/**
	 * 在查询时，可能存在部分数据没有的情况，造成数据集合针对某个站点没有数据，这时需要使用基础信息填充到数据集合，
	 * 保证返回的记录条数正确，同时把没有的属性填充为null。<br>
	 * 注意：需要保证传递的参数primaryKey在dataList与baseList中的每个元素中都要有属性，否则匹配不成功，不能达到效果
	 * @param dataList 数据集合
	 * @param baseList 基础信息集合
	 * @param primaryKey 基础信息集合与数据集合的匹配条件
	 * @param attrs 需要填充的数据
	 */
	public static void fillBaseRecords(
			Collection<Map<String,Object>> dataList,
			Collection<Map<String,Object>> baseList,
			String primaryKey,List<String> attrs
	) {
		for(Map<String,Object> base: baseList) {
			String code = (String) base.get(primaryKey);
			// 判断在数据集合中是否已经含有了该基础信息的记录数据
			boolean has = false;
			for(Map<String,Object> item: dataList) {
				if(code==null||item.get(primaryKey)==null) {
					continue;
				}
				if(item.get(primaryKey).toString().equals(code)) {
					has = true;
					break;
				}
			}
			
			if(!has) {
				// 从基础数据中获取信息，然后向数据集合中添加一条记录
				Map<String,Object> item = new HashMap<>();
				attrs.forEach(x -> item.put(x, base.get(x)));
				dataList.add(item);
			}
		}
		// 给数据集合中没有的属性，填为null
		fillEmpty(dataList, attrs);
	}
	
	public static void fillBaseRecordsWithTime(
			Collection<Map<String,Object>> dataList,
			Collection<Map<String,Object>> baseList,
			String primaryKey,List<String> attrs,List<String> timeList,String timeField
	) {
		for(Map<String,Object> base: baseList) {
			String code = (String) base.get(primaryKey);
			for (String time : timeList) {
				// 判断在数据集合中是否已经含有了该基础信息的记录数据
				boolean has = false;
				for(Map<String,Object> item: dataList) {
					if(code==null||item.get(primaryKey)==null
							||time==null||item.get(timeField)==null) {
						continue;
					}
					if(item.get(primaryKey).toString().equals(code)
							||item.get(timeField).toString().equals(time)) {
						has = true;
						break;
					}
				}
				if(!has) {
					// 从基础数据中获取信息，然后向数据集合中添加一条记录
					Map<String,Object> item = new HashMap<>();
					attrs.forEach(x -> item.put(x, base.get(x)));
					item.put(timeField, time);
					dataList.add(item);
				}
			}
		}
		// 给数据集合中没有的属性，填为null
		fillEmpty(dataList, attrs);
	}

	/**
	 * 将一个集合中的数据，合并到另一个集合中
	 * @param dataList
	 * @param mergeList 需要将该集合中的数据合并到dataList中
	 * @param primaryKey 基础信息集合与数据集合的匹配条件
	 * @param attrs 需要合并的属性
	 */
	public static void mergeRecords(Collection<Map<String, Object>> dataList, List<Map<String, Object>> mergeList,
			String primaryKey, List<String> attrs) {
		for(Map<String,Object> merge: mergeList) {
			String code = (String) merge.get(primaryKey);
			for(Map<String,Object> item: dataList) {
				if(code==null||item.get(primaryKey)==null) {
					continue;
				}
				if(item.get(primaryKey).toString().equals(code)) {
					attrs.forEach(x -> item.put(x, merge.get(x)));
					break;
				}
			}
		}
	}
	
	/**
	 * 判断str字符串中是否包含数据库操作的一些关键字，调用该方法对sql语句近判断，可避免sql注入漏洞，可能会误判
	 * @return 包含关键字，返回true，否则返回false
	 */
	public static boolean containKeywords(String str) {
		// 对SQL语句进行安全判定
		List<String> list = Arrays.asList("insert","update","delete","drop","create","table",
				"auto_increment","varchar","database","admin","mysql","grant");
		return list.stream().anyMatch(str.toLowerCase()::contains);
	}

	/**
	 * 将小数保留指定的位数
	 * @param value 需要格式化的小数
	 * @param bit 需要保留的小数位数
	 * @return
	 */
	public static Double formatDouble(Double value, int bit) {
		if (value == null) {
			return null;
		}
		return (int)(value*Math.pow(10, bit))/Math.pow(10.0, bit);
	}
	
	public static <T> void push(Map<String,T> map,T value,String... keys) {
		for (String key : keys) {
			map.put(key, value);
		}
	}
	
	public static <T> void push(Map<String,T> map,T value,List<String> keys) {
		for (String key : keys) {
			map.put(key, value);
		}
	}

	/**
	 * 根据起止时间，填充缺失时间对应的数据
	 * @param list 需要填充的集合，要求list中至少有一个对象
	 * @param timeName 时间名称
	 * @param stime 开始时间（包含）
	 * @param etime 结束时间（包含）
	 * @param dim 时间维度，Calendar相关
	 * @return 填充的个数
	 */
	public static List<Map<String,Object>> fillListByTime(List<? extends Map<String, Object>> list, String timeName, String stime, String etime,
			int dim) {
		// 存放返回结果
		List<Map<String,Object>> resList = new ArrayList<>();
		// 1、获取需要填充的属性
		Set<String> attrs = new HashSet<>();
		list.stream().map(Map::keySet).forEach(attrs::addAll);
		
		// 2、遍历list，判断是否含有指定的时间，如果不含有就追加
		if(Calendar.HOUR_OF_DAY == dim) {
			try {
				List<String> times = getDates("HOUR", stime, etime);
				for (String time : times) {
					Map<String, Object> record = null;
					for (Map<String, Object> map : list) {
						// 遍历追加到resList中
						String timeV = (String) map.get(timeName);
						if(time.equals(timeV)) {
							record = map;
							break;
						}
					}
					if(record == null) {
						record = new HashMap<>();
						for (String attr : attrs) {
							record.put(attr, null);
						}
						record.put(timeName, time);
					}
					resList.add(record);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else if(Calendar.DAY_OF_MONTH == dim) {
			
		}
		return resList;
	}

	public static void fillZero(List<Map<String, Object>> dataList, List<String> attrs) {
		try {
			for (Map<String, Object> map : dataList) {
				for (String attr : attrs) {
					if(map.get(attr)==null) {
						map.put(attr, 0);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static <T extends Map<String,Object>> void fillEmptyStr(List<T> dataList, List<String> attrs) {
		try {
			for (Map<String, Object> map : dataList) {
				for (String attr : attrs) {
					if(map.get(attr)==null) {
						map.put(attr, "");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void fillZeroFromMap(Map<String, Object> map, List<String> attrs) {
		try {
			for (String attr : attrs) {
				if (map.get(attr) == null) {
					map.put(attr, 0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void fillEmptyStrFromMap(Map<String, Object> map, List<String> attrs) {
		try {
			for (String attr : attrs) {
				if (map.get(attr) == null) {
					map.put(attr, "");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
