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
    private String id;
    private String subject;
    private String creatorName;
    private String accountName;
    private int duration;
    private String chairmanPassword;
    private String guestPassword;
    private String accessCode;
    private String scheduleStartTime;
    private String scheduleEndTime;
    private String organizationName;
    private String mainServiceZoneName;
    private List<ParticipantRsp> participants;
    private List<AttendeeRsp> attendees;
}
