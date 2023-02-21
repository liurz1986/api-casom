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
@Table(name = "hw_meeting_participant")
@Entity
public class HwMeetingParticipant {
    @Id
    @Column(name="id")
    @ApiModelProperty(value = "记录ID")
    private String id;

    @Column(name="meeting_id")
    @ApiModelProperty(value = "会议ID")
    private String meetingId;

    @Column(name="name")
    @ApiModelProperty(value = "会场名称")
    private String name;

    @Column(name="organization_name")
    @ApiModelProperty(value = "组织名称")
    private String organizationName;

    @Column(name="branch")
    @ApiModelProperty(value = "分院")
    private String branch;

    @Column(name="city")
    @ApiModelProperty(value = "城市")
    private String city;

    @Column(name="duration")
    @ApiModelProperty(value = "会议时长")
    private int duration;

    @Column(name="schedule_start_time")
    @ApiModelProperty(value = "会议开始时间")
    private Date scheduleStartTime;

    @Column(name="schedule_end_time")
    @ApiModelProperty(value = "会议结束时间")
    private Date scheduleEndTime;

    @Column(name="terminal_type")
    @ApiModelProperty(value = "设备型号")
    private String terminalType;

    @Column(name="stage")
    @ApiModelProperty(value = "会议类型")
    private String stage;
}
