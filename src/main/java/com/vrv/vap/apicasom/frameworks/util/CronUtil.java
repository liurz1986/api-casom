package com.vrv.vap.apicasom.frameworks.util;

import org.quartz.CronExpression;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author: 梁国露
 * @since: 2023/2/20 14:03
 * @description:
 */
public class CronUtil {
    /**
     * 当前周期的上一周期
     * @param cron
     * @param nextValidTime 当前周期时间
     * @return
     */
    public static Date getPreviousValidDate(String cron, Date nextValidTime) {
        try {
            CronExpression cronExpression = new CronExpression(cron);
            Date subsequentNextValidTime = cronExpression.getNextValidTimeAfter(nextValidTime);
            long interval = subsequentNextValidTime.getTime() - nextValidTime.getTime();
            return new Date(nextValidTime.getTime() - interval);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unsupported cron or date", e);
        }
    }

    /**
     * utc时间转成local时间
     * @param utcTime
     * @return
     */
    public static Date utcToLocal(String utcTime){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date utcDate = null;
        try {
            utcDate = sdf.parse(utcTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        sdf.setTimeZone(TimeZone.getDefault());
        Date locatlDate = null;
        String localTime = sdf.format(utcDate.getTime());
        try {
            locatlDate = sdf.parse(localTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return locatlDate;
    }

}
