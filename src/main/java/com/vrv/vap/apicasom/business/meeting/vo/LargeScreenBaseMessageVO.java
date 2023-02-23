package com.vrv.vap.apicasom.business.meeting.vo;

import lombok.Data;

/**
 * 大屏基本信息
 * 会议视屏节点总数、当前节点在线总数、举办会议次数、参会总人数、会议总时长
 * @author vrv
 */
@Data
public class LargeScreenBaseMessageVO {
    /**
     * 会议视屏节点总数
     */
    private int meetingTotal;

    /**
     * 会议视屏节点总数
     */
    private int onlineNodeTotal;

    /**
     * 举办会议次数
     */
    private int offlineMettingCount;

    /**
     * 参会总人数
     */
    private int  offlineMettingUserCount;

    /**
     * 会议总时长:小时，取整，四舍五入
     */
    private int meetingTimeTotal;
}
