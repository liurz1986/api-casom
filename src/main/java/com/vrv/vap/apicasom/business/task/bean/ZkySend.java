package com.vrv.vap.apicasom.business.task.bean;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author: 梁国露
 * @since: 2023/3/8 18:12
 * @description:
 */
@Data
@Entity
@Table(name = "zky_send")
public class ZkySend {
    @Id
    @Column(name="id")
    @ApiModelProperty("记录ID")
    private String id;

    @Column(name="org_name")
    @ApiModelProperty("组织机构名称")
    private String orgName;

    @Column(name="org_code")
    @ApiModelProperty("组织机构编码")
    private String orgCode;

    @Column(name="start_time")
    @ApiModelProperty("开始时间")
    private String startDate;

    @Column(name="end_time")
    @ApiModelProperty("结束时间")
    private String endDate;

    @Column(name="send_type")
    @ApiModelProperty("类型")
    private String sendType;

    @Column(name="send_scope")
    @ApiModelProperty("单位")
    private String sendScope;

    @Column(name="receive_num")
    @ApiModelProperty("接收数量")
    private Integer receiveNum;

    @Column(name="send_num")
    @ApiModelProperty("发送数量")
    private Integer sendNum;

    @Column(name="send_region")
    @ApiModelProperty("发送区域")
    private Integer sendRegion;
}
