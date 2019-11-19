package com.grand.mysql_handler.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * 日常工具方法
 */
public class CommonTools {
    private Calendar calendar = null;
    private int minus = 0;

    public CommonTools() {
    }

    public static Double objToDouble(Object obj) {
        return obj != null && !"".equals(obj) ? Double.parseDouble(obj.toString()) : null;
    }

    public static double objTodouble(Object obj) {
        return obj != null && !"".equals(obj) ? Double.parseDouble(obj.toString()) : 0.0;
    }

    /**
     * 转换成INT
     *
     * @param obj
     * @return
     */
    public static Integer objToInt(Object obj) {
        return obj != null && !"".equals(obj) ? Integer.parseInt(new DecimalFormat("0").format(objToDouble(obj.toString()))) : null;
    }

    public static int objToint(Object obj) {
        return obj != null && !"".equals(obj) ? Integer.parseInt(new DecimalFormat("0").format(objToDouble(obj.toString()))) : 0;
    }

    /**
     * 根据输入的两个集合，补齐缺失的天数数据，以0进行补充，并按照时间顺序进行升序返回
     *
     * @param lastYear
     * @param thisYear
     * @param thisYearNum
     * @return
     */
    public static Map<String, Object> yearCompare(List<Map<String, Object>> lastYear,
                                                  List<Map<String, Object>> thisYear, int thisYearNum, String minDate, String maxDate, String timeAttrName,
                                                  Set<String> keysName, String... str) throws Exception {
        // 储存返回的结果使用
        Map<String, Object> resultMap = new HashMap<String, Object>();

        String timeAttr = null; // 对象的时间字段名
        Set<String> keys = null; // 每个对象中的属性
        if (timeAttrName != null && keysName != null && keysName.size() > 0) {
            timeAttr = timeAttrName;
            keys = keysName;
        } else {
            // 根据输入确定的时间字段及所有的属性名
            if (thisYear.size() > 0) {
                Map<String, Object> item = thisYear.get(0);
                for (String key : item.keySet()) {
                    if (key != null && key.contains("time")) {
                        keys = new HashSet<String>(item.keySet());
                        timeAttr = key;
                        break;
                    }
                }
            }
            if (keys == null && lastYear.size() > 0) {
                Map<String, Object> item = lastYear.get(0);
                for (String key : item.keySet()) {
                    if (key != null && key.contains("time")) {
                        keys = new HashSet<String>(item.keySet());
                        timeAttr = key;
                        break;
                    }
                }
            }
        }

        boolean has4Year = false;
        // 判断今年和去年是否有闰年
        if (thisYearNum % 4 == 0 || (thisYearNum - 1) % 4 == 0) {
            has4Year = true;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Map<String, Map<String, Object>> lastYearMap = new HashMap<String, Map<String, Object>>();
        Map<String, Map<String, Object>> thisYearMap = new HashMap<String, Map<String, Object>>();
        // 得到最小的时间与最大的时间

        String min = null;
        String max = null;

        for (Map<String, Object> item : lastYear) {
            String time = (String) item.get(timeAttr);
            lastYearMap.put(time, item);
        }
        for (Map<String, Object> item : thisYear) {
            String time = (String) item.get(timeAttr);
            thisYearMap.put(time, item);
        }
        if (minDate == null || maxDate == null) {
            for (Map<String, Object> item : lastYear) {
                String time = (String) item.get(timeAttr);
                if (min == null) {
                    min = time;
                } else {
                    if (sdf.parse(time).getTime() < sdf.parse(min).getTime()) {
                        min = time;
                    }
                }
                if (max == null) {
                    max = time;
                } else {
                    if (sdf.parse(time).getTime() > sdf.parse(max).getTime()) {
                        max = time;
                    }
                }
            }
            for (Map<String, Object> item : thisYear) {
                String time = (String) item.get(timeAttr);
                thisYearMap.put(time, item);
                time = time.replace(thisYearNum + "", thisYearNum - 1 + "");
                if (min == null) {
                    min = time;
                } else {
                    if (sdf.parse(time).getTime() < sdf.parse(min).getTime()) {
                        min = time;
                    }
                }
                if (max == null) {
                    max = time;
                } else {
                    if (sdf.parse(time).getTime() > sdf.parse(max).getTime()) {
                        max = time;
                    }
                }
            }
        } else {
            min = minDate.replace(thisYearNum + "", thisYearNum - 1 + "");
            max = maxDate.replace(thisYearNum + "", thisYearNum - 1 + "");
        }

        // 得到最小日期和最大日期之间的日期集合
        Calendar cursor = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        List<String> timeList = new ArrayList<String>();
        if (str.length > 0 && "month".equals(str[0])) {
            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM");
            cursor.setTime(sd.parse(min));
            end.setTime(sd.parse(max));
            while (cursor.getTimeInMillis() <= end.getTimeInMillis()) {
                String tt = sd.format(cursor.getTime());
                timeList.add(tt);
                cursor.add(Calendar.MONTH, 1);
            }
        } else if (str.length > 0 && "hour".equals(str[0])) {
            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH");
            cursor.setTime(sd.parse(min));
            end.setTime(sd.parse(max));
            while (cursor.getTimeInMillis() <= end.getTimeInMillis()) {
                String tt = sd.format(cursor.getTime());
                timeList.add(tt);
                // 此处存在一个BUG，当选择的开始时间是闰年的02-29日时
                if (tt.contains("02-28") && has4Year) {
                    timeList.add(tt.replace("02-28", "02-29"));
                }
                cursor.add(Calendar.HOUR_OF_DAY, 1);
            }
        } else {
            cursor.setTime(sdf.parse(min));
            end.setTime(sdf.parse(max));
            while (cursor.getTimeInMillis() <= end.getTimeInMillis()) {
                String tt = sdf.format(cursor.getTime());
                timeList.add(tt);
                // 此处存在一个BUG，当选择的开始时间是闰年的02-29日时
                if (tt.contains("02-28") && has4Year) {
                    timeList.add(tt.replace("02-28", "02-29"));
                }
                cursor.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        List<Map<String, Object>> lastYearList = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> thisYearList = new ArrayList<Map<String, Object>>();
        for (String date : timeList) {
            Map<String, Object> item1 = lastYearMap.get(date);
            if (item1 == null) {
                item1 = new HashMap<String, Object>();
                for (String key : keys) {
                    item1.put(key, "");
                }
                item1.put(timeAttr, date);
            }
            lastYearList.add(item1);

            date = date.replace(thisYearNum - 1 + "", thisYearNum + "");
            Map<String, Object> item2 = thisYearMap.get(date);
            if (item2 == null) {
                item2 = new HashMap<String, Object>();
                for (String key : keys) {
                    item2.put(key, "");
                }
                item2.put(timeAttr, date);
            } else {
                System.out.println();
            }
            thisYearList.add(item2);
        }
        resultMap.put("lastYear", lastYearList);
        resultMap.put("thisYear", thisYearList);
        return resultMap;
    }

    public static Map<String, Object> monthCompare(List<Map<String, Object>> lastmonth,
                                                   List<Map<String, Object>> thisYear, int thisYearNum, String sdate, String edate, String timeAttrName,
                                                   Set<String> keysName, String... str) throws Exception {
        // 储存返回的结果使用
        Map<String, Object> resultMap = new HashMap<String, Object>();

        String timeAttr = null; // 对象的时间字段名
        Set<String> keys = null; // 每个对象中的属性
        if (timeAttrName != null && keysName != null && keysName.size() > 0) {
            timeAttr = timeAttrName;
            keys = keysName;
        } else {
            // 根据输入确定的时间字段及所有的属性名
            if (thisYear.size() > 0) {
                Map<String, Object> item = thisYear.get(0);
                for (String key : item.keySet()) {
                    if (key != null && key.contains("time")) {
                        keys = new HashSet<String>(item.keySet());
                        timeAttr = key;
                        break;
                    }
                }
            }
            if (keys == null && lastmonth.size() > 0) {
                Map<String, Object> item = lastmonth.get(0);
                for (String key : item.keySet()) {
                    if (key != null && key.contains("time")) {
                        keys = new HashSet<String>(item.keySet());
                        timeAttr = key;
                        break;
                    }
                }
            }
        }

        boolean has4Year = false;
        // 判断今年和去年是否有闰年
        if (thisYearNum % 4 == 0 || (thisYearNum - 1) % 4 == 0) {
            has4Year = true;
        }

        SimpleDateFormat sdh = new SimpleDateFormat("yyyy-MM-dd HH");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM");

        Map<String, Map<String, Object>> lastMonthMap = new HashMap<String, Map<String, Object>>();
        Map<String, Map<String, Object>> thisYearMap = new HashMap<String, Map<String, Object>>();
        // 得到最小的时间与最大的时间

        String min = null;
        String max = null;

        for (Map<String, Object> item : lastmonth) {
            String time = (String) item.get(timeAttr);
            lastMonthMap.put(time, item);
        }
        for (Map<String, Object> item : thisYear) {
            String time = (String) item.get(timeAttr);
            thisYearMap.put(time, item);
        }
        if (sdate == null || edate == null) {
            for (Map<String, Object> item : lastmonth) {
                String time = (String) item.get(timeAttr);
                if (min == null) {
                    min = time;
                } else {
                    if (sdf.parse(time).getTime() < sdf.parse(min).getTime()) {
                        min = time;
                    }
                }
                if (max == null) {
                    max = time;
                } else {
                    if (sdf.parse(time).getTime() > sdf.parse(max).getTime()) {
                        max = time;
                    }
                }
            }
            for (Map<String, Object> item : thisYear) {
                String time = (String) item.get(timeAttr);
                thisYearMap.put(time, item);
                time = LocalDate.parse(time).plusMonths(-1).toString();
                if (min == null) {
                    min = time;
                } else {
                    if (sdf.parse(time).getTime() < sdf.parse(min).getTime()) {
                        min = time;
                    }
                }
                if (max == null) {
                    max = time;
                } else {
                    if (sdf.parse(time).getTime() > sdf.parse(max).getTime()) {
                        max = time;
                    }
                }
            }
        } else {
            min = sdate;
            max = edate;
        }

        // 得到最小日期和最大日期之间的日期集合
        Calendar cursor = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        List<String> timeList = new ArrayList<String>();
        if (str.length > 0 && "month".equals(str[0])) {
            cursor.setTime(sd.parse(min));
            end.setTime(sd.parse(max));
            while (cursor.getTimeInMillis() <= end.getTimeInMillis()) {
                String tt = sd.format(cursor.getTime());
                timeList.add(tt);
                cursor.add(Calendar.MONTH, 1);
            }
        } else if (str.length > 0 && "hour".equals(str[0])) {
            cursor.setTime(sdh.parse(min));
            end.setTime(sdh.parse(max));
            while (cursor.getTimeInMillis() <= end.getTimeInMillis()) {
                String tt = sdh.format(cursor.getTime());
                timeList.add(tt);
                // 此处存在一个BUG，当选择的开始时间是闰年的02-29日时
                if (tt.contains("02-28") && has4Year) {
                    timeList.add(tt.replace("02-28", "02-29"));
                }
                cursor.add(Calendar.HOUR_OF_DAY, 1);
            }
        } else {
            cursor.setTime(sdf.parse(min));
            end.setTime(sdf.parse(max));
            while (cursor.getTimeInMillis() <= end.getTimeInMillis()) {
                String tt = sdf.format(cursor.getTime());
                timeList.add(tt);
                // 此处存在一个BUG，当选择的开始时间是闰年的02-29日时
                if (tt.contains("02-28") && has4Year) {
                    timeList.add(tt.replace("02-28", "02-29"));
                }
                cursor.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        List<Map<String, Object>> lastMonthList = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> thisYearList = new ArrayList<Map<String, Object>>();
        for (String date : timeList) {
            Map<String, Object> item1 = lastMonthMap.get(date);
            if (item1 == null) {
                item1 = new HashMap<String, Object>();
                for (String key : keys) {
                    item1.put(key, "");
                }
                item1.put(timeAttr, date);
            }
            lastMonthList.add(item1);

            if (str.length > 0 && "month".equals(str[0])) {
                cursor.setTime(sd.parse(date));
                cursor.add(Calendar.MONTH, 1);
                date = sd.format(cursor.getTime());
            } else if (str.length > 0 && "hour".equals(str[0])) {
                cursor.setTime(sdh.parse(date));
                cursor.add(Calendar.MONTH, 1);
                date = sdh.format(cursor.getTime());
            } else
                date = LocalDate.parse(date).plusMonths(1).toString();
            Map<String, Object> item2 = thisYearMap.get(date);
            if (item2 == null) {
                item2 = new HashMap<String, Object>();
                for (String key : keys) {
                    item2.put(key, "");
                }
                item2.put(timeAttr, date);
            } else {
                System.out.println();
            }
            thisYearList.add(item2);
        }
        resultMap.put("lastMonth", lastMonthList);
        resultMap.put("thisYear", thisYearList);
        return resultMap;
    }

    public static Map<String, Object> hoursCompare(List<Map<String, Object>> lastlist,
                                                   List<Map<String, Object>> thisYear, String timeAttrName, Set<String> keysName) throws Exception {
        // 储存返回的结果使用
        Map<String, Object> resultMap = new HashMap<String, Object>();

        String timeAttr = null; // 对象的时间字段名
        Set<String> keys = null; // 每个对象中的属性
        if (timeAttrName != null && keysName != null && keysName.size() > 0) {
            timeAttr = timeAttrName;
            keys = keysName;
        } else {
            // 根据输入确定的时间字段及所有的属性名
            if (thisYear.size() > 0) {
                Map<String, Object> item = thisYear.get(0);
                for (String key : item.keySet()) {
                    if (key != null && key.contains("time")) {
                        keys = new HashSet<String>(item.keySet());
                        timeAttr = key;
                        break;
                    }
                }
            }
            if (keys == null && lastlist.size() > 0) {
                Map<String, Object> item = lastlist.get(0);
                for (String key : item.keySet()) {
                    if (key != null && key.contains("time")) {
                        keys = new HashSet<String>(item.keySet());
                        timeAttr = key;
                        break;
                    }
                }
            }
        }

        Map<String, Map<String, Object>> lastMap = new HashMap<String, Map<String, Object>>();
        Map<String, Map<String, Object>> thisYearMap = new HashMap<String, Map<String, Object>>();
        for (Map<String, Object> item : lastlist) {
            String time = (String) item.get(timeAttr);
            lastMap.put(time, item);
        }
        for (Map<String, Object> item : thisYear) {
            String time = (String) item.get(timeAttr);
            thisYearMap.put(time, item);
        }

        // 得到24小时集合
        List<String> timeList = new ArrayList<String>();
        for (int i = 0; i < 24; i++) {
            String tt = null;
            if (i < 10) {
                tt = "0" + i;
            } else {
                tt = i + "";
            }
            timeList.add(tt);
        }
        List<Map<String, Object>> lastList = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> thisYearList = new ArrayList<Map<String, Object>>();
        for (String date : timeList) {
            Map<String, Object> item1 = lastMap.get(date);
            if (item1 == null) {
                item1 = new HashMap<String, Object>();
                for (String key : keys) {
                    item1.put(key, "");
                }
                item1.put(timeAttr, date);
            }
            lastList.add(item1);
            Map<String, Object> item2 = thisYearMap.get(date);
            if (item2 == null) {
                item2 = new HashMap<String, Object>();
                for (String key : keys) {
                    item2.put(key, "");
                }
                item2.put(timeAttr, date);
            } else {
                System.out.println();
            }
            thisYearList.add(item2);
        }
        resultMap.put("lastlist", lastList);
        resultMap.put("thisYear", thisYearList);
        return resultMap;
    }

    public static List<String> getDays(String sdate, String edate) {
        List<String> list = new ArrayList<String>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cursor = Calendar.getInstance();
            cursor.setTime(sdf.parse(sdate));
            Calendar end = Calendar.getInstance();
            end.setTime(sdf.parse(edate));
            while (cursor.getTimeInMillis() <= end.getTimeInMillis()) {
                list.add(sdf.format(cursor.getTime()));
                cursor.add(Calendar.DAY_OF_MONTH, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static <T> T transferBaseObject(Object obj, Class<T> clazz) throws Exception {
        if (obj == null) {
            throw new ClassCastException();
        }
        Class tempClazz = obj.getClass();
        if (tempClazz == clazz) {
            return (T) obj;
        }
        String objStr = obj.toString();
        if (clazz == String.class) {
            return (T) objStr;
        } else if (clazz == Integer.class) {
            return (T) Integer.valueOf(objStr);
        } else if (clazz == Long.class) {
            return (T) Long.valueOf(objStr);
        } else if (clazz == Float.class) {
            return (T) Float.valueOf(objStr);
        } else if (clazz == Double.class) {
            return (T) Double.valueOf(objStr);
        } else {
            throw new Exception("is not base wrapper.");
        }
    }

    public static String fillEmpty(String str) {
        if (str == null || str.equals("")) {
            return "";
        }
        return str;
    }

    public static int fillEmpty(Integer obj) {
        if (obj == null) {
            return 0;
        }
        return obj;
    }

    public static long fillEmpty(Long obj) {
        if (obj == null) {
            return 0L;
        }
        return obj;
    }

    public static double fillEmpty(Double obj) {
        if (obj == null) {
            return 0.0;
        }
        return obj;
    }

    public CommonTools(int t) {
        this.minus = t;
        this.calendar = new GregorianCalendar();
    }

    public static String getYesterday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(calendar.getTime());
    }

    public static String formatDate(java.util.Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    public static String formatDate(java.util.Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    /**
     * 通过1年中的周数，获取指定周的起止时间（包含起，不包含止）
     *
     * @param week
     * @return
     */
    public static Map<String, String> getStartAndEndTimeByWeek(int week, int year) {
        Map<String, String> map = new HashMap<String, String>();
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        c.set(Calendar.YEAR, year);
        c.set(Calendar.WEEK_OF_YEAR, week);
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        String startTIme = sdf.format(c.getTime());
        c.add(Calendar.DAY_OF_YEAR, 7);
        String endTime = sdf.format(c.getTime());
        map.put("startTime", startTIme);
        map.put("endTime", endTime);
        return map;
    }

    /**
     * 获取当前周的起止时间（包含起，不包含止）
     *
     * @return
     */
    public static Map<String, String> getStartAndEndTimeByWeek() {
        Map<String, String> map = new HashMap<String, String>();
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // c.set(Calendar.YEAR, year);
        // c.set(Calendar.WEEK_OF_YEAR, week);
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        String startTIme = sdf.format(c.getTime());
        c.add(Calendar.DAY_OF_YEAR, 7);
        String endTime = sdf.format(c.getTime());
        map.put("startTime", startTIme);
        map.put("endTime", endTime);
        return map;
    }

    /**
     * 获取时间的分段区间 （按天）
     *
     * @param beginTime
     * @param endTime
     * @param num       份数
     * @return
     */
    public static String[] getdates(String beginTime, String endTime, int num) {
        String[] dates = new String[num];

        long days = getDiffDay(beginTime, endTime);
        int fen = (int) days / num;
        int lastFen = (int) (fen + days % num);

        String curtime = CommonTools.getNowDate();
        for (int i = 0; i < num; i++) {
            if (i == num - 1) {
                dates[i] = getDate(curtime, 1, lastFen);
            } else {
                dates[i] = getDate(curtime, 1, fen);
            }
            curtime = dates[i];
        }
        return dates;
    }

    public static String getBeforeOneHour() {
        String ddd = getDate(getNowDate(), 4, -1);
        return ddd.substring(0, 13);
    }

    /**
     * 时间的加减运算
     *
     * @param time 起始时间
     * @param type 类型
     * @param num  数量
     * @return
     */
    public static String getDate(String time, int type, int num) {
        String resTime = "";
        try {
            // 字符串时间转Date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date d = sdf.parse(time);

            Calendar c = Calendar.getInstance();

            // Date 转Calendar
            c.setTime(d);

            // 使用Calendar 进行时间的加减运算
            if (type == 2) {
                c.add(Calendar.MONTH, num);
            } else if (type == 1) {
                c.add(Calendar.DATE, num);
            } else if (type == 3) {
                c.add(Calendar.YEAR, num);
            } else if (type == 4) {
                c.add(Calendar.HOUR, num);
            } else if (type == 5) {
                c.add(Calendar.MINUTE, num);
            } else if (type == 6) {
                c.add(Calendar.SECOND, num);
            }

            // 最终把Calendar 转Date 再转 String 输出
            resTime = sdf.format(c.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resTime;
    }

    public static String getDate(String time, String format, int type, int num) {
        String resTime = "";
        try {
            // 字符串时间转Date
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            java.util.Date d = sdf.parse(time);

            Calendar c = Calendar.getInstance();

            // Date 转Calendar
            c.setTime(d);

            // 使用Calendar 进行时间的加减运算
            if (type == 2) {
                c.add(Calendar.MONTH, num);
            } else if (type == 1) {
                c.add(Calendar.DATE, num);
            } else if (type == 3) {
                c.add(Calendar.YEAR, num);
            } else if (type == 4) {
                c.add(Calendar.HOUR_OF_DAY, num);
            }

            // 最终把Calendar 转Date 再转 String 输出
            resTime = sdf.format(c.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resTime;
    }

    /**
     * 获取日期相差天数
     *
     * @param
     * @return 日期类型时间
     * @throws java.text.ParseException
     */
    public static int getDiffDay(String beginDate, String endDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int checkday = 0; // 开始结束相差天数
        try {
            checkday = (int) ((formatter.parse(endDate).getTime() - formatter.parse(beginDate).getTime())
                    / (1000 * 24 * 60 * 60));
            Long temp = (formatter.parse(endDate).getTime() - formatter.parse(beginDate).getTime())
                    % (1000 * 24 * 60 * 60);
            if (temp > 0) {
                checkday += 1;
            } else if (temp < 0) {
                checkday -= 1;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            checkday = 0;
        }
        return checkday;
    }

    public static int getDiffDay(java.util.Date beginDate, java.util.Date endDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strBeginDate = format.format(beginDate);
        String strEndDate = format.format(endDate);
        return getDiffDay(strBeginDate, strEndDate);
    }

    public static Long getDiffMinutes(String beginDate, String endDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Long check = 0l; // 开始结束相差天数
        try {
            check = (formatter.parse(endDate).getTime() - formatter.parse(beginDate).getTime()) / (1000 * 60);
        } catch (ParseException e) {
            e.printStackTrace();
            check = null;
        }
        return check;
    }

    /**
     * 获取字符串 对应的 毫秒时间
     *
     * @param time
     * @return
     */
    public static Long dateStrToLong(String time) {
        if (time.length() == 10) {
            time = time + " 00:00:00";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Long check = 0l;
        try {
            check = formatter.parse(time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            check = null;
        }
        return check;
    }

    public static Long getDiffMinutes(java.util.Date beginDate, java.util.Date endDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strBeginDate = format.format(beginDate);
        String strEndDate = format.format(endDate);
        return getDiffMinutes(strBeginDate, strEndDate);
    }

    /**
     * 获得指定之前时间的时间戳(秒级别)
     *
     * @return
     */
    public long getBeforeTimeStamp() {
        StringBuffer buf = new StringBuffer();
        buf.append(calendar.get(Calendar.YEAR));
        buf.append(this.addZero(calendar.get(Calendar.MONTH) + 1, 2));
        buf.append(this.addZero(calendar.get(Calendar.DAY_OF_MONTH), 2));
        buf.append(this.addZero(calendar.get(Calendar.HOUR_OF_DAY), 2));
        calendar.add(Calendar.MINUTE, this.minus);
        buf.append(this.addZero(calendar.get(Calendar.MINUTE), 2));
        buf.append(this.addZero(calendar.get(Calendar.SECOND), 2));
//	  buf.append(this.addZero(calendar.get(Calendar.MILLISECOND), 3));

        return Long.parseLong(buf.toString());
    }

    /**
     * 获得系统的时间戳 (毫秒级)
     *
     * @return
     */
    public long getTimeStamp() {
        StringBuffer buf = new StringBuffer();
        buf.append(calendar.get(Calendar.YEAR));
        buf.append(this.addZero(calendar.get(Calendar.MONTH) + 1, 2));
        buf.append(this.addZero(calendar.get(Calendar.DAY_OF_MONTH), 2));
        buf.append(this.addZero(calendar.get(Calendar.HOUR_OF_DAY), 2));
        calendar.add(Calendar.MINUTE, this.minus);
        buf.append(this.addZero(calendar.get(Calendar.MINUTE), 2));
        buf.append(this.addZero(calendar.get(Calendar.SECOND), 2));
        buf.append(this.addZero(calendar.get(Calendar.MILLISECOND), 3));

        return Long.parseLong(buf.toString());
    }

    private String addZero(int num, int len) {
        StringBuffer s = new StringBuffer();
        s.append(num);
        while (s.length() < len) {
            s.insert(0, "0");
        }
        return s.toString();
    }

    /**
     * 返回普通sql日期
     *
     * @param dates
     * @return
     */
    public static Date getSqlDate(String dates) {
        Date sqlDate = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            java.util.Date date = sdf.parse(dates);
            sqlDate = new Date(date.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sqlDate;
    }

    /**
     * 返回当前精确sql日期
     *
     * @return
     */
    public static Timestamp getSqlTime() {
        Timestamp sqlDate = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            java.util.Date date = sdf.parse(getNowDate("yyyy-MM-dd hh:mm:ss"));
            sqlDate = new Timestamp(date.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sqlDate;
    }

    public static String getChnMoney(String bigdMoneyNumber) {
        return getChnMoney(new BigDecimal(bigdMoneyNumber));
    }

    public static String getChnMoney(BigDecimal bigdMoneyNumber) {
        String[] straChineseUnit = new String[]{"分", "角", "圆", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿", "拾", "佰",
                "仟"};
        String[] straChineseNumber = new String[]{"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};// 中文数字字符数组
        String strChineseCurrency = "";
        boolean bZero = true;// 零数位标记
        int ChineseUnitIndex = 0;// 中文金额单位下标
        try {
            if (bigdMoneyNumber.intValue() == 0)
                return "零圆整";
            double doubMoneyNumber = Math.round(bigdMoneyNumber.doubleValue() * 100);// 处理小数部分，四舍五入
            boolean bNegative = doubMoneyNumber < 0;// 是否负数
            doubMoneyNumber = Math.abs(doubMoneyNumber);// 取绝对值
            while (doubMoneyNumber > 0)// 循环处理转换操作
            {
                if (ChineseUnitIndex == 2 && strChineseCurrency.length() == 0)
                    strChineseCurrency = strChineseCurrency + "整";// 整的处理(无小数位)
                if (doubMoneyNumber % 10 > 0)// 非零数位的处理
                {
                    strChineseCurrency = straChineseNumber[(int) doubMoneyNumber % 10]
                            + straChineseUnit[ChineseUnitIndex] + strChineseCurrency;
                    bZero = false;
                } else {// 零数位的处理
                    if (ChineseUnitIndex == 2)// 元的处理(个位)
                    {
                        if (doubMoneyNumber > 0)// 段中有数字
                        {
                            strChineseCurrency = straChineseUnit[ChineseUnitIndex] + strChineseCurrency;
                            bZero = true;
                        }
                    } else if (ChineseUnitIndex == 6 || ChineseUnitIndex == 10)// 万、亿数位的处理
                    {
                        if (doubMoneyNumber % 1000 > 0)
                            strChineseCurrency = straChineseUnit[ChineseUnitIndex] + strChineseCurrency;// 段中有数字
                    }
                    if (!bZero)
                        strChineseCurrency = straChineseNumber[0] + strChineseCurrency; // 前一数位非零的处理
                    bZero = true;
                }
                doubMoneyNumber = Math.floor(doubMoneyNumber / 10);
                ChineseUnitIndex++;
            }
            if (bNegative)
                strChineseCurrency = "负" + strChineseCurrency;// 负数的处理
        } catch (Exception e) {
            return "";
        }
        return strChineseCurrency;
    }

    public static String getChinese(String s) {
        if (s == null)
            return "";
        try {
            String convert = new String(s.getBytes("ISO8859_1"), "GB2312");
            return convert;
        } catch (Exception e) {
        }
        return s;
    }

    public static String getCharset(String s, String fromCharset1, String toCharset2) {
        if (s == null)
            return "";
        try {
            String convert = new String(s.getBytes(fromCharset1), toCharset2);
            return convert;
        } catch (Exception e) {
        }
        return s;
    }

    public static String getSQLLike(String SQL) {
        if (!SQL.equals("")) {
            SQL = SQL.replace("Like '~!", "Like '%");
            SQL = SQL.replace("~!')", "%')");
        }
        return SQL;
    }

    // 返回给定的字符长度
    // s:要截取的字符串
    // LimitStrlen:长度
    // IsReturnSpace:当s为null时是否返回" ";为true:要返回；false：不返回
    // IsDouHao:返回值中是否将字符串中的上逗号'去掉 为true:要去掉；false：不去掉
//    public static String getSaveStr(String s){boolean m;if(DBConnect.Ver==0)m=false;else m=true;return getLimitLenStr(s,0,m,true);}
//    public static String getSaveStr(String s,int LimitStrlen){boolean m;if(DBConnect.Ver==0)m=false;else m=true;return getLimitLenStr(s,LimitStrlen,m,true);}
    public static String getLimitLenStr(String s, int LimitStrlen) {
        return getLimitLenStr(s, LimitStrlen, false);
    }

    public static String getLimitLenStr(String s, int LimitStrlen, boolean IsReturnSpace) {
        return getLimitLenStr(s, LimitStrlen, IsReturnSpace, false);
    }

    public static String getLimitLenStr(String s, int LimitStrlen, boolean IsReturnSpace, boolean IsDouHao) {
        if (s == null)
            if (IsReturnSpace == true)
                return " ";
            else
                return "";
        s = s.replace("'", "");
        if (LimitStrlen != 0) {
            char[] cc = s.toCharArray();
            int intLen = 0;
            int i;
            // if("中国".length()==4){return s.substring(Maxlen/2);}
            for (i = 0; i < cc.length; i++) {
                if ((int) cc[i] > 255) {
                    intLen = intLen + 2;
                } else {
                    intLen++;
                }
                if (intLen >= LimitStrlen) {
                    break;
                }
            }
            if (intLen == LimitStrlen)
                i++;
            s = s.substring(0, i);
        }
        if (s.equals(""))
            if (IsReturnSpace == true)
                return s = " ";
        if (IsDouHao == true)
            s = s.replace("'", "");
        return s;
    }

    public static String getLimitChinese(String s, int MaxLen) {
        if (s == null)
            return "";
        try {
            String convert = new String(s.getBytes("ISO8859_1"), "GB2312");
            convert = getLimitLenStr(convert, MaxLen);
            // String convert=getLimitLenStr(s,MaxLen);
            return convert;
        } catch (Exception e) {
        }
        return "";
    }

    public static String getNumber(String m) {
        if (m == null)
            return "0";
        m = m.trim();
        if (m.equals(""))
            return "0";
        return m;
    }

    public static String getNowDate() {
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar now = Calendar.getInstance();
        return s.format(now.getTime()).toString();
    }

    public static String getNowDate(String format) {
        SimpleDateFormat s = new SimpleDateFormat(format);
        Calendar now = Calendar.getInstance();
        return s.format(now.getTime()).toString();
    }

    public static String getDate(String m) {
        if (m == null)
            return getNowDate();
        m = m.trim();
        if (m.equals(""))
            return getNowDate();
        try {
            SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
            m = s.format(s.parse(m)).toString();
        } catch (Exception e) {
            m = getNowDate();
        }
        return m;
    }

    /**
     * date 转换为时间串
     */
    public static String getDateString(java.util.Date date) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String str = format.format(date);
        return str;
    }

    public static String getDateString(java.util.Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String str = sdf.format(date);
        return str;
    }

    public static String timeOperate(String time, String format, int type, int num, int position, int length) {
        String res = null;
        String outFormat = "yyyy-MM-dd HH:mm:ss:SSS";
        try {
            SimpleDateFormat inSdf = new SimpleDateFormat(format);
            SimpleDateFormat outSdf = new SimpleDateFormat(outFormat);
            Calendar c = Calendar.getInstance();
            c.setTime(inSdf.parse(time));
            c.add(type, num);
            res = outSdf.format(c.getTime()).substring(position, length);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * 提供判断并返回值。
     *
     * @param w 源固定值
     * @param m 变量值
     */
    public static String getSwitch(String Rstr, String w, String m) {
        if (w.equals(m))
            return Rstr;
        else
            return "";
    }

    /**
     * 提供精确的加法运算。
     *
     * @param v1 被加数
     * @param v2 加数
     * @return 两个参数的和
     */
    public static double add(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).doubleValue();
    }

    /**
     * 提供精确的减法运算。
     *
     * @param v1 被减数
     * @param v2 减数
     * @return 两个参数的差
     */
    public static double sub(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2).doubleValue();
    }

    /**
     * 提供精确的乘法运算。
     *
     * @param v1 被乘数
     * @param v2 乘数
     * @return 两个参数的积
     */
    public static double mul(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2).doubleValue();
    }

    /**
     * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到 小数点以后10位，以后的数字四舍五入。
     *
     * @param v1 被除数
     * @param v2 除数
     * @return 两个参数的商
     */
    public static double div(double v1, double v2) {
        return div(v1, v2, 10);
    }

    /**
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指 定精度，以后的数字四舍五入。
     *
     * @param v1    被除数
     * @param v2    除数
     * @param scale 表示表示需要精确到小数点以后几位。
     * @return 两个参数的商
     */
    public static double div(double v1, double v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 提供精确的小数位四舍五入处理。
     *
     * @param v     需要四舍五入的数字
     * @param scale 小数点后保留几位
     * @return 四舍五入后的结果
     */
    public static double round(double v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b = new BigDecimal(Double.toString(v));
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 提供精确的格式化小数位四舍五入处理。
     *
     * @param v      需要四舍五入的数字
     * @param scale  小数点后保留几位
     * @param Format 输出的格式 如保留小数点后2位"#.00"
     * @return 四舍五入后的格式化结果
     */
    public static String round(double v, int scale, String Format) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b = new BigDecimal(Double.toString(v));
        BigDecimal one = new BigDecimal("1");
        DecimalFormat df1 = new DecimalFormat(Format);
        return df1.format(b.divide(one, scale, BigDecimal.ROUND_HALF_UP).doubleValue());
    }

    /**
     * 提供精确的格式化小数位处理。
     *
     * @param v      需要四舍五入的数字
     * @param Format 输出的格式 如保留小数点后2位"#.00"
     * @return 四舍五入后的格式化结果
     */
    public static String round(double v, String Format) {
        DecimalFormat df1 = new DecimalFormat(Format);
        return df1.format(v);
    }

    public static String getName(Connection con, String sql) {
        String name = "";
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs != null) {
                if (rs.next()) {
                    name = rs.getString(1);
                }
                rs.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            stmt.close();
        } catch (Exception ww) {
        }
        return name;
    }

    public static void deleteDirectory(File dir) throws IOException {
        if ((dir == null) || !dir.isDirectory()) {
            return;
        }
        File[] entries = dir.listFiles();
        int sz = entries.length;
        for (int i = 0; i < sz; i++) {
            if (entries[i].isDirectory()) {
                deleteDirectory(entries[i]);
            } else {
                entries[i].delete();
            }
        }
        dir.delete();
    }

    public static String getAsstring(String str, int n) {
        if (str.length() > n)
            return str.substring(0, n);
        else
            return str;
    }

    public static String escape(String src) {
        int i;
        char j;
        StringBuffer tmp = new StringBuffer();
        tmp.ensureCapacity(src.length() * 6);
        for (i = 0; i < src.length(); i++) {
            j = src.charAt(i);
            if (Character.isDigit(j) || Character.isLowerCase(j) || Character.isUpperCase(j))
                tmp.append(j);
            else if (j < 256) {
                tmp.append("%");
                if (j < 16)
                    tmp.append("0");
                tmp.append(Integer.toString(j, 16));
            } else {
                tmp.append("%u");
                tmp.append(Integer.toString(j, 16));
            }
        }
        return tmp.toString();
    }

    public static String unescape(String src) {
        StringBuffer tmp = new StringBuffer();
        tmp.ensureCapacity(src.length());
        int lastPos = 0, pos = 0;
        char ch;
        while (lastPos < src.length()) {
            pos = src.indexOf("%", lastPos);
            if (pos == lastPos) {
                if (src.charAt(pos + 1) == 'u') {
                    ch = (char) Integer.parseInt(src.substring(pos + 2, pos + 6), 16);
                    tmp.append(ch);
                    lastPos = pos + 6;
                } else {
                    ch = (char) Integer.parseInt(src.substring(pos + 1, pos + 3), 16);
                    tmp.append(ch);
                    lastPos = pos + 3;
                }
            } else {
                if (pos == -1) {
                    tmp.append(src.substring(lastPos));
                    lastPos = src.length();
                } else {
                    tmp.append(src.substring(lastPos, pos));
                    lastPos = pos;
                }
            }
        }
        return tmp.toString();
    }

    /**
     * 隐藏字符串的字符
     *
     * @param str
     * @param begin
     * @param end
     * @return
     */
    public static String getHiddenStr(String str, int begin, int end) {
        String newStr = "";
        String temp = str.substring(end);
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < end; i++) {
            stringBuffer.append("*");
        }
        stringBuffer.append(temp);
        newStr = stringBuffer.toString();
        return newStr;
    }

    /**
     * 创建一个时间，这个时间根据 cf : Calendar的格式 n ： 创建的时间的增量，正数加，负数减
     */
    public static String createTime(int cf, int n) {
        Calendar c = Calendar.getInstance();
        c.add(cf, n);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String mDateTime = formatter.format(c.getTime());
        return mDateTime;
    }

    /**
     * @param formatstr    需要转换的格式
     * @param sdataeformat 日期本来的格式
     * @param sdate        日期字符串
     * @return
     * @throws java.text.ParseException
     */
    public static String formatedate(String formatstr, String sdataeformat, String sdate) throws ParseException {
        java.util.Date date = null;
        SimpleDateFormat bartDateFormat = new SimpleDateFormat(sdataeformat);
        date = bartDateFormat.parse(sdate);
        SimpleDateFormat formatter1 = new SimpleDateFormat(formatstr);
        String timems = formatter1.format(date);
        return timems;
    }

    /**
     * 得到去年和今年的所有月份
     *
     * @return
     */
    public static List<String> getMonths() {
        List<String> list = new ArrayList<String>();
        try {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            String nowMonth = sdf.format(c.getTime());

            int lastYear = c.get(Calendar.YEAR) - 1;
            nowMonth = (lastYear + 1) + "-12";
            java.util.Date date = sdf.parse(lastYear + "-01");
            c.setTime(date);
            int num = 30;
            while (num-- > 0) {
                String cursor = sdf.format(c.getTime());
                list.add(cursor);
                if (cursor.equals(nowMonth)) {
                    break;
                }
                c.add(Calendar.MONTH, 1);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<String> getMonths(String start, String end) {
        List<String> list = new ArrayList<String>();
        Calendar cursor = Calendar.getInstance();
        Calendar endPoint = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        int n = 30; // 避免循环次数过多陷入死循环
        try {
            String startMonth = start.substring(0, 7);
            String endMonth = end.substring(0, 7);
            cursor.setTime(sdf.parse(startMonth));
            endPoint.setTime(sdf.parse(endMonth));
            do {
                list.add(sdf.format(cursor.getTime()));
                cursor.add(Calendar.MONTH, 1);
                if (cursor.getTimeInMillis() <= endPoint.getTimeInMillis()
                        || sdf.format(cursor.getTime()).equals(sdf.format(endPoint.getTime()))) {

                } else {
                    break;
                }
                if (--n < 0) {
                    break;
                }
            } while (true);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 通过输入的时间类型和时间，将其解析为起止时间段(注意：在数据库中时间字段存放的是字符串)
     *
     * @param type 4选1，week | month | season | year
     * @param time 对应于类型："yyyy年xx周" | "yyyy年xx月" | "yyyy年01季度" | "yyyy年"
     * @return 返回数组长度为2，第一个为开始时间，第二个为结束时间，都包含
     */
    public static String[] parseTimeTypeToHour(String type, String time) throws Exception {
        if (StringUtils.isEmpty(type) || StringUtils.isEmpty(time)) {
            throw new Exception("传入的参数为空");
        }
        String[] array = new String[2];
        switch (type) {
            case "week": {
                // 传递的格式要求为 yyyy年MM周
                String temp = time.replace("周", "").replace("年", ",");
                String[] arr = temp.split(",");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Calendar c = Calendar.getInstance();
                c.setFirstDayOfWeek(Calendar.MONDAY);
                int year = Integer.parseInt(arr[0]);
                int week = Integer.parseInt(arr[1]);
                c.set(Calendar.YEAR, year);
                c.set(Calendar.WEEK_OF_YEAR, week + 1);
                c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                array[0] = sdf.format(c.getTime()) + " 00";
                c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                array[1] = sdf.format(c.getTime()) + " 23";
                break;
            }
            case "month": {
                // 传递的格式要求为 yyyy年MM月 转为 "yyyy-MM"
                String prex = time.replace("月", "").replace("年", "-");
                array[0] = prex + "-01 00";
                array[1] = prex + "-31 23";
                break;
            }
            case "season": {
                // 传递的格式为yyyy年xx季度
                String temp = time.replace("季度", "").replace("年", ",");
                String[] arr = temp.split(",");
                String year = arr[0];
                String season = arr[1];
                if (season.equals("01")) {
                    array[0] = year + "-01-01 00";
                    array[1] = year + "-03-31 23";
                } else if (season.equals("02")) {
                    array[0] = year + "-04-01 00";
                    array[1] = year + "-06-30 23";
                } else if (season.equals("03")) {
                    array[0] = year + "-07-01 00";
                    array[1] = year + "-09-30 23";
                } else if (season.equals("04")) {
                    array[0] = year + "-10-01 00";
                    array[1] = year + "-12-31 23";
                }
                break;
            }
            case "year": {
                // 传递的格式为yyyy年
                String prex = time.replace("年", "");
                array[0] = prex + "-01-01 00";
                array[1] = prex + "-12-31 23";
                break;
            }
            default:
                throw new Exception("不存在传入的时间类型");
        }
        if (StringUtils.isEmpty(array[0]) || StringUtils.isEmpty(array[1])) {
            throw new Exception("输入参数时间与格式不匹配");
        }
        return array;
    }

    public static java.util.Date parseTimeStr(String time, String format) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.parse(time);
    }

    /**
     * 获取分钟数整的当前时间
     *
     * @param date
     * @param string
     */
    public static String getDataMinute(java.util.Date date, String string) {
        SimpleDateFormat sdf = new SimpleDateFormat(string);
        String format = sdf.format(date);
        String substring = format.substring(0, 15);
        substring = substring + "0";
        return substring;
    }

    /**
     * 获取百分数
     *
     * @param size
     * @param count
     * @return
     */
    public static String getPercent(int size, int count) {
        int num1 = size;
        int num2 = count;
        // 创建一个数值格式化对象
        NumberFormat numberFormat = NumberFormat.getInstance();
        // 设置精确到小数点后2位
        numberFormat.setMaximumFractionDigits(2);
        String result = numberFormat.format((float) num1 / (float) num2 * 100);
        System.out.println("num1和num2的百分比为:" + result + "%");
        return result + "%";
    }

    /**
     * 根据起止时间，获取在时间段内的时间集合
     *
     * @param start    开始时间（包含）
     * @param end      截止（包含）
     * @param timeType 使用Calendar中的值
     * @return
     */
    public static List<String> getLostTime(String start, String end, int timeType) {
        List<String> list = new ArrayList<String>();
        String format = null;
        switch (timeType) {
            case Calendar.HOUR_OF_DAY:
                format = "yyyy-MM-dd HH";
                break;
            case Calendar.DAY_OF_MONTH:
                format = "yyyy-MM-dd";
                break;
            case Calendar.MONTH:
                format = "yyyy-MM";
                break;
            default:
                return list;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            Calendar cursor = Calendar.getInstance();
            Calendar cEnd = Calendar.getInstance();
            cEnd.setTime(sdf.parse(end));
            cursor.setTime(sdf.parse(start));
            while (cursor.getTimeInMillis() <= cEnd.getTimeInMillis()) {
                list.add(sdf.format(cursor.getTime()));
                cursor.add(timeType, 1);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Double windDirToAngle(String windDir) {
        final String[] directions = new String[]{"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW",
                "WSW", "W", "WNW", "NW", "NNW"};
        final Double[] angles = new Double[]{0.0, 22.5, 45.0, 67.5, 90.0, 112.5, 135.0, 157.5, 180.0, 202.5, 225.0,
                247.5, 270.0, 295.5, 315.0, 337.5};
        for (int i = 0; i < directions.length; i++) {
            if (directions[i].equals(windDir)) {
                return angles[i];
            }
        }
        return 0.0;
    }

    public static String getWindDirByAngle(Double angle) {
        String direction = "不详";
        if (angle == null) {
            return direction;
        }
        // 将角度转为文字描述的风向
        if (angle <= 11.25) {
            direction = "北";
        } else if (angle <= 22.5 + 11.25) {
            direction = "北东北";
        } else if (angle <= 45.0 + 11.25) {
            direction = "东北";
        } else if (angle <= 67.5 + 11.25) {
            direction = "东东北";
        } else if (angle <= 90.0 + 11.25) {
            direction = "东";
        } else if (angle <= 112.5 + 11.25) {
            direction = "东东南";
        } else if (angle <= 135 + 11.25) {
            direction = "东南";
        } else if (angle <= 157.5 + 11.25) {
            direction = "南东南";
        } else if (angle <= 180 + 11.25) {
            direction = "南";
        } else if (angle <= 202.5 + 11.25) {
            direction = "南西南";
        } else if (angle <= 225 + 11.25) {
            direction = "西南";
        } else if (angle <= 247.5 + 11.25) {
            direction = "西西南";
        } else if (angle <= 270 + 11.25) {
            direction = "西";
        } else if (angle <= 292.5 + 11.25) {
            direction = "西西北";
        } else if (angle <= 315 + 11.25) {
            direction = "西北";
        } else if (angle <= 337.5 + 11.25) {
            direction = "北西北";
        } else {
            direction = "北";
        }
        return direction;
    }

    public static String getPropertisUrl(String name) throws IOException {
        Properties properties = new Properties();
        InputStream in = CommonTools.class.getResourceAsStream("/data-center-url.properties");
        properties.load(in);
        String url = properties.getProperty("ip") + properties.getProperty(name);
        return url;
    }

    /**
     * 获取小数后进行截取位数
     *
     * @param val 值
     * @param num 小数点后保留的位数
     * @return
     */
    public static double getDecimal(Double val, int num) {
        if (val == null) {
            return 0;
        }
        BigDecimal b = new BigDecimal(val);
        return b.setScale(num, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * @param value 需要科学计算的数据
     * @param digit 保留的小数位
     * @return 功能：四舍六入五成双计算法
     */
    public static double sciCal(double value, int digit) {
        String result = "-999";
        try {
            double ratio = Math.pow(10, digit);
            double _num = value * ratio;
            double mod = _num % 1;
            double integer = Math.floor(_num);
            double returnNum;
            if (mod > 0.5) {
                returnNum = (integer + 1) / ratio;
            } else if (mod < 0.5) {
                returnNum = integer / ratio;
            } else {
                returnNum = (integer % 2 == 0 ? integer : integer + 1) / ratio;
            }
            BigDecimal bg = new BigDecimal(returnNum);
            result = bg.setScale((int) digit, BigDecimal.ROUND_HALF_UP).toString();
        } catch (RuntimeException e) {
            throw e;
        }
        double dou = Double.parseDouble(result);
        return dou;
    }

    public static void main(String[] args) {
        System.out.println(sciCal(18.51, 0));
    }
}
