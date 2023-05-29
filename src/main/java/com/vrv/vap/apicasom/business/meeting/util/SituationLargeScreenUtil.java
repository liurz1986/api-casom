package com.vrv.vap.apicasom.business.meeting.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SituationLargeScreenUtil {

    /**
     * month(近一个月)、halfyear(半年)、year(一年)、all(全部)
     * @param type
     * @param filterColum
     * @return
     */
    public static String typeSql(String type,String filterColum) {
        String sql = "";
        switch (type) {
            case "month":
                sql = "DATE_SUB(CURDATE(), INTERVAL 1 MONTH) <= date_format(" + filterColum + ",'%Y-%m-%d') and date_format(" + filterColum + ",'%Y-%m-%d')<= date_format(CURDATE(),'%Y-%m-%d')";
                break;
            case "halfyear":
                sql = "DATE_SUB(CURDATE(), INTERVAL 6 MONTH) <= date_format(" + filterColum + ",'%Y-%m-%d') and date_format(" + filterColum + ",'%Y-%m-%d')<= date_format(CURDATE(),'%Y-%m-%d')";
                break;
            case "year":
                sql = "DATE_SUB(CURDATE(), INTERVAL 1 YEAR) <= date_format(" + filterColum + ",'%Y-%m-%d') and date_format(" + filterColum + ",'%Y-%m-%d')<= date_format(CURDATE(),'%Y-%m-%d')";
                break;
            default:
                sql= "1=1";
                break;
        }
        return sql;
    }

    /**
     * 判断两个时间相差几个月
     * @param endDate
     * @param startDate
     * @return
     */
    public static int monthDiff(Date startDate,Date endDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();
        startCalendar.setTime(startDate);
        endCalendar.setTime(endDate);
        int count = endCalendar.get(Calendar.YEAR)-startCalendar.get(Calendar.YEAR);
        int month =0;
        if(count > 0){
            month = count*12;
        }
        if(count < 0){
            month = -count*12;
        }
        count = endCalendar.get(Calendar.MONTH)-startCalendar.get(Calendar.MONTH)+month;
        return count;
    }
}
