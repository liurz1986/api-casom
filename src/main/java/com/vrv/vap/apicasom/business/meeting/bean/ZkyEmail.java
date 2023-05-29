package com.vrv.vap.apicasom.business.meeting.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * 邮件收发
 */
@Data
@Entity
@Table(name = "zky_email")
public class ZkyEmail {
    @Id
    @Column(name="guid")
    @ApiModelProperty("记录ID")
    private String guid;

    @Column(name="org_name")
    @ApiModelProperty("部门名称")
    private String orgName;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @Column(name="email_time")
    @ApiModelProperty("时间")
    private Date emailTime;

    @Column(name="receive_num")
    @ApiModelProperty("收件数")
    private int receiveNum;

    @Column(name="send_num")
    @ApiModelProperty("发件数")
    private int sendNum;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name="import_time")
    @ApiModelProperty("导入时间")
    private Date importTime;
}
