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
}
