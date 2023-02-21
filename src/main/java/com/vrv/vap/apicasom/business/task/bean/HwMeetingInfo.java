package com.vrv.vap.apicasom.business.task.bean;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author: 梁国露
 * @since: 2023/2/16 17:09
 * @description:
 */
@Data
@Table(name = "hw_meeting_info")
@Entity
public class HwMeetingInfo {

    @Id
    @Column(name="meeting_id")
    @ApiModelProperty(value = "会议ID")
    private String meetingId;

    @Column(name="duration")
    @ApiModelProperty(value = "会议时长")
    private int duration;

    @Column(name="schedule_start_time")
    @ApiModelProperty(value = "会议开始时间")
    private Date scheduleStartTime;

    @Column(name="schedule_end_time")
    @ApiModelProperty(value = "会议结束时间")
    private Date scheduleEndTime;

    @Column(name="organization_name")
    @ApiModelProperty(value = "组织名称")
    private String organizationName;

    @Column(name="stage")
    @ApiModelProperty(value = "会议状态")
    private String stage;

    @Column(name="attendee_count")
    @ApiModelProperty(value = "会议人数")
    private int attendeeCount;

    @Column(name="participant_count")
    @ApiModelProperty(value = "会议节点数")
    private int participantCount;

    @Column(name="participant_unity")
    @ApiModelProperty(value = "会议节点")
    private String participantUnity;
}
