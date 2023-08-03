package com.vrv.vap.apicasom.business.task.constant;

/**
 * @author: 梁国露
 * @since: 2023/2/17 14:18
 * @description:
 */
public class MeetingUrlConstant {
    public static int size= 1000;
    // token
    public static String TOKEN_URL = "/tokens";

    // 历史会议列表
    public static String HISTORY_LIST_URL = "/conferences/history/conditions?page={page}&size="+size+"&sortOrder=scheduleEndTime,desc";

    // 历史会议详情
    public static String HISTORY_INFO_URL = "/conferences/history/{0}";

    // 历史会议告警
    public static String HISTORY_ALARM_URL = "/conferences/history/{0}/alarms/conditions?page={page}&size="+size+"&sort=alarmTime";

    // 预约会议列表
    public static String NOW_LIST_URL = "/conferences/conditions?page={page}&size="+size;

    // 预约会议详情
    public static String NOW_INFO_URL="/conferences/{0}";

    // 预约会议告警
    public static String NOW_ALARM_URL="/conferences/{0}/currentalarms?page={page}&size="+size+"&sort=alarmTime";

    // 预约会议会场
    public static String NOW_PARTICIPANT_URL="/online/conferences/{0}/participants/conditions?page={page}&size="+size;

    // 会议室
    public static String MEETING_ROOMS_URL = "/meetingrooms/conditions";

    // 获取会议室前注册
    public static String REGISTER = "/noauth/deviceinfo";

}
