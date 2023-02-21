package com.vrv.vap.apicasom.business.task.bean;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author: 梁国露
 * @since: 2023/2/16 17:10
 * @description:
 */
@Data
@Table(name = "hw_meeting_attendee")
@Entity
public class HwMeetingAttendee {

    @Id
    @Column(name="id")
    @ApiModelProperty(value = "记录ID")
    private String id;

    @Column(name="meeting_id")
    @ApiModelProperty(value = "会议ID")
    private String meetingId;

    @Column(name="participant_name")
    @ApiModelProperty(value = "会场名称")
    private String participantName;

    @Column(name="user_count")
    @ApiModelProperty(value = "参会人数")
    private Integer userCount;

    @Column(name="city")
    @ApiModelProperty(value = "城市")
    private String city;

    @Column(name="branch")
    @ApiModelProperty(value = "分院")
    private String branch;

    @Column(name="duration")
    @ApiModelProperty(value = "会议时长")
    private Integer duration;
}
