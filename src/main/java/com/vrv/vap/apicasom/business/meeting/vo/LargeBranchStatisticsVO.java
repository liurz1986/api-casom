package com.vrv.vap.apicasom.business.meeting.vo;

import lombok.Data;

/**
 * 各地区系统使用统计
 * @author vrv
 */
@Data
public class LargeBranchStatisticsVO {
    /**
     * 分院名称
     */
    private String name;

    /**
     * 参数人数
     */
    private int userNum;;

    /**
     * 参数次数
     */
    private int meetingTimes;;
    /**
     * 会议时长
     */
    private int meetingDur;
}
