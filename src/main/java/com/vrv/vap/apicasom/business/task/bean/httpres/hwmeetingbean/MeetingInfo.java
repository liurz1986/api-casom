package com.vrv.vap.apicasom.business.task.bean.httpres.hwmeetingbean;

import lombok.Data;

import java.util.List;

/**
 * @author: 梁国露
 * @since: 2023/2/16 17:51
 * @description:
 */
@Data
public class MeetingInfo {
    // 会议Id
    private String id;
    // 会议主题
    private String subject;
    // 创建者
    private String creatorName;
    // 用户名
    private String accountName;
    // 会议时长
    private int duration;
    private String chairmanPassword;
    private String guestPassword;
    private String accessCode;
    // 会议开始时间
    private String scheduleStartTime;
    // 会议结束时间
    private String scheduleEndTime;
    // 组织名称
    private String organizationName;
    private String mainServiceZoneName;
    // 会场列表
    private List<ParticipantRsp> participants;
    // 与会者列表
    private List<AttendeeRsp> attendees;
}
