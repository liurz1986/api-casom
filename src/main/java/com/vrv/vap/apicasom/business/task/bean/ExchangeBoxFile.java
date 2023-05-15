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
 * @since: 2023/5/10 14:36
 * @description:
 */
@Data
@Entity
@Table(name = "exchange_box_file")
public class ExchangeBoxFile {
    @Id
    @Column(name="guid")
    @ApiModelProperty("记录ID")
    private String guid;

    @Column(name="file_type")
    @ApiModelProperty("文件类型")
    private String fileType;

    @Column(name="total")
    @ApiModelProperty("总数")
    private int total;

    @Column(name="flowingTotal")
    @ApiModelProperty("流转总数")
    private int flowingTotal;

    @Column(name="registrationTotal")
    @ApiModelProperty("登记总数")
    private int registrationTotal;

    @Column(name="create_time")
    @ApiModelProperty("时间")
    private Date createTime;
}
