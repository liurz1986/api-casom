package com.vrv.vap.apicasom.business.meeting.util;

import lombok.Data;

/**
 * @author vrv
 */
@Data
public class MeetingConstrant {
    /**
     * 近一个月
     */
    public static String MONTH="month";
    /**
     * 半年
     */
    public static String HALFWAY ="halfyear";
    /**
     * 一年
     */
    public static String YEAR="year";
    /**
     * none表示手动输入(startDate、endDate)
     */
    public static String NONE="none";

    /**
     *  会议状态：已结束的会议(历史）
     */
    public static String OFFLINE="OFFLINE";


    /**
     *  议状态：在线
     */
    public static String ONLINE="ONLINE";

    /**
     *  大屏城市状态：存在异常会议
     */
    public static String CITY_STATUS_ABNORMAL="abnormal";
    /**
     *  大屏城市状态：存在在线会议
     */
    public static String CITY_STATUS_RUN="run";
    /**
     *  大屏城市状态：不存在异常会议和正在进行的会议
     */
    public static String CITY_STATUS_NONE="none";
}
