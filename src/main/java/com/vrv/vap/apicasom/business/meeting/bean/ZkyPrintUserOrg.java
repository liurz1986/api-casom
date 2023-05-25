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
 * 用户打印机构数据
 */
@Data
@Entity
@Table(name = "zky_print_user_org")
public class ZkyPrintUserOrg {

    @Id
    @Column(name="guid")
    @ApiModelProperty("记录ID")
    private String guid;

    @Column(name="user_name")
    @ApiModelProperty("用户名称")
    private String userName;

    @Column(name="branch")
    @ApiModelProperty("分院(地区)")
    private String branch;

    @Column(name="organization_name")
    @ApiModelProperty("单位/部门")
    private String organizationName;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name="import_time")
    @ApiModelProperty("导入时间")
    private Date importTime;

}
