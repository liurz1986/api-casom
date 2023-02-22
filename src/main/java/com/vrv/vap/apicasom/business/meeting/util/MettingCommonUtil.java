package com.vrv.vap.apicasom.business.meeting.util;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MettingCommonUtil {
    /**
     * 告警级别
     */
    private static Map<String,String> ranges = new HashMap<>();

    private static List<String> timeTypes = new ArrayList<>();

    /**
     * 时间段为手动输入
     */
    public final static String TIMETYPE="none";

    public final static long DAYTIME=86400000;

      static{
          ranges.put("MAJOR","重要");
          ranges.put("MINOR","次要");
          ranges.put("NONE","无告警，用于设备级别的汇总告警状态");

          timeTypes.add("month");
          timeTypes.add("halfyear");
          timeTypes.add("year");
          timeTypes.add("none");
      }

    /**
     * 获取告警对应的中文名称
     * @param code
     * @return
     */
    public static String getRangeName(String code){
        if(StringUtils.isEmpty(code)){
            return "未知";
        }
        String data = ranges.get(code);
        if(StringUtils.isEmpty(data)){
            return "未知";
        }
        return data;
    }


    /**
     * 时间段统计类型判断
     * @param timeType
     * @return
     */
    public static boolean isExistTimeType(String timeType){
        return  timeTypes.contains(timeType);
    }

    /**
     * 判断开始时间与结束时间之间是否大于24小时
     * @param endDate
     * @param startDate
     * @return
     */
    public static boolean isDay(Date endDate,Date startDate) {
        long endtime = endDate.getTime();
        long starttime= startDate.getTime();
        long result = endtime-starttime;
        // 大于24小时按天
        if(result > DAYTIME){
            return true;
        }
        return false;
    }

    /**
     * 月：
     * X轴数据：近一个月按天处理
     * @return
     * @throws ParseException
     */
    public static List<String> getMonthDataX() throws ParseException {
        List<String> dataXS= new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  // 具体天
        String endTimeStr = sdf.format(new Date());
        Date endTime = sdf.parse(endTimeStr);
        long endTimes = endTime.getTime();
        Date startTime =  DateUtils.addMonths(endTime, -1);
        long startTimes = startTime.getTime();
        while(startTimes <= endTimes){
            Date date = new Date(startTimes);
            dataXS.add(sdf.format(date));
            startTimes = startTimes+1000 * 60 * 60 * 24;
        }
        return dataXS;
    }

    /**
     * 半年：
     * X轴数据：半年按月处理
     * @return
     * @throws ParseException
     */
    public static List<String> getHalfyearDataX(){
        List<String> dataXS= new ArrayList<>();
        // 获取当前日历对象
        Calendar calendar = Calendar.getInstance();
        // 6个月前
        calendar.add(Calendar.MONTH,-5);
        for(int i= 0;i< 6;i++){
            // 日历转为date对象
            Date date= calendar.getTime();
            // 将date转化为字符串
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            String dataStr = sdf.format(date);
            dataXS.add(dataStr);
            // 每次月份加1
            calendar.add(Calendar.MONTH,1);
        }
        return dataXS;
    }

    /**
     * 年：
     * X轴数据：年按月处理
     * @return
     * @throws ParseException
     */
    public static List<String> getYearDataX() {
        List<String> dataXS= new ArrayList<>();
        // 获取当前日历对象
        Calendar calendar = Calendar.getInstance();
        // 12个月前
        calendar.add(Calendar.MONTH,-11);
        for(int i= 0;i< 12;i++){
            // 日历转为date对象
           Date date= calendar.getTime();
           // 将date转化为字符串
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            String dataStr = sdf.format(date);
            dataXS.add(dataStr);
            // 每次月份加1
            calendar.add(Calendar.MONTH,1);
        }
       return dataXS;
    }

    /**
     *  根据开始时间和结束，按小时处理
     * @return
     * @throws ParseException
     */
    public static List<String> getDataXByHour(Date endDate,Date startDate) throws ParseException {
        List<String> dataXS = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
        long startTimes = startDate.getTime();
        long endTimes = endDate.getTime();
        // 如果开始日期和结束日期同一天情况
        if(startTimes == endTimes){
            return getDataXByDay24(startDate);
        }
        while(startTimes <= endTimes){
            Date date = new Date(startTimes);
            dataXS.add(sdf.format(date));
            startTimes = startTimes+1000 * 60 * 60;
        }
        return dataXS;
    }

    /**
     *  根据开始时间和结束，按天处理
     * @return
     * @throws ParseException
     */
    public static List<String> getDataXByDay(Date endDate,Date startDate) {
        List<String> dataXS = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        long startTimes = startDate.getTime();
        long endTimes = endDate.getTime();
        while(startTimes <= endTimes){
            Date date = new Date(startTimes);
            dataXS.add(sdf.format(date));
            startTimes = startTimes+1000 * 60 * 60 * 24;
        }
        return dataXS;
    }

    private static List<String> getDataXByDay24(Date startDate) throws ParseException {
        List<String> dataXS = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String startTimeStr = sdf.format(startDate);
        Date startTimeNew = sdf.parse(startTimeStr);
        long startTimes = startTimeNew.getTime();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH");
        for(int i= 0;i<24;i++){
            Date date = new Date(startTimes);
            dataXS.add(sdf.format(date));
            startTimes = startTimes+1000 * 60 * 60;
        }
        return dataXS;
    }
}