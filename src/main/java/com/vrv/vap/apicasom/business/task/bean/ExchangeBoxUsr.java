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
 * @since: 2023/5/10 14:40
 * @description:
 */
@Data
@Entity
@Table(name = "exchange_box_usr")
public class ExchangeBoxUsr {
    @Id
    @Column(name="guid")
    @ApiModelProperty("记录ID")
    private String guid;

    @Column(name="total_user")
    @ApiModelProperty("总用户数")
    private int totalUsers;

    @Column(name="login_count")
    @ApiModelProperty("用户登录次数")
    private int loginCount;

    @Column(name="create_time")
    @ApiModelProperty("时间")
    private Date createTime;
}
