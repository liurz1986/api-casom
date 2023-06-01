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
 * 公文交换箱
 */
@Data
@Entity
@Table(name = "zky_exchange_box")
public class ZkyExchangeBox {
    @Id
    @Column(name="guid")
    @ApiModelProperty("记录ID")
    private String guid;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @Column(name="deadline")
    @ApiModelProperty("截至时间")
    private Date deadline;

    @Column(name="receive_total")
    @ApiModelProperty("收件总数")
    private long receiveTotal;

    @Column(name="receive_roam_total")
    @ApiModelProperty("收件流转总数")
    private long receiveRoamTotal;

    @Column(name="receive_register_total")
    @ApiModelProperty("收件登记总数")
    private long receiveRegisterTotal;

    @Column(name="sign_total")
    @ApiModelProperty("签批件总数")
    private long signTotal;

    @Column(name="sign_roam_total")
    @ApiModelProperty("签批件流转总数")
    private long signRoamTotal;

    @Column(name="sign_register_total")
    @ApiModelProperty("签批件登记总数")
    private long signRegisterTotal;

    @Column(name="secrecy_total")
    @ApiModelProperty("密刊总数")
    private long secrecyTotal;

    @Column(name="secrecy_roam_total")
    @ApiModelProperty("密刊流转总数")
    private long secrecyRoamTotal;

    @Column(name="secrecy_register_total")
    @ApiModelProperty("密刊登记总数")
    private long secrecyRegisterTotal;

    @Column(name="user_total")
    @ApiModelProperty("用户总数")
    private long userTotal;

    @Column(name="user_login_count")
    @ApiModelProperty("用户登录次数")
    private long userLoginCount;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name="import_time")
    @ApiModelProperty("导入时间")
    private Date importTime;
}
