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
@Table(name = "hw_meeting_alarm")
@Entity
public class HwMeetingAlarm {

    @Id
    @Column(name="id")
    @ApiModelProperty(value = "记录ID")
    private String id;

    @Column(name="meeting_id")
    @ApiModelProperty(value = "会议ID")
    private String meetingId;

    @Column(name="alarm_no")
    @ApiModelProperty(value = "告警标识")
    private String alarmNo;

    @Column(name="name")
    @ApiModelProperty(value = "异常名称")
    private String name;

    @Column(name="severity")
    @ApiModelProperty(value = "严重级别")
    private String severity;

    @Column(name="alarm_type")
    @ApiModelProperty(value = "异常类型")
    private String alarmType;

    @Column(name="alarm_time")
    @ApiModelProperty(value = "告警时间")
    private Date alarmTime;

    @Column(name="cleared_time")
    @ApiModelProperty(value = "告警确认或恢复时间")
    private Date clearedTime;

    @Column(name="alarm_status")
    @ApiModelProperty(value = "告警状态")
    private String alarmStatus;

}
