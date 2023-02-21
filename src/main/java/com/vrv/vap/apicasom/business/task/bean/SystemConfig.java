package com.vrv.vap.apicasom.business.task.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @BelongsProject api-admin
 * @BelongsPackage com.vrv.vap.admin.model
 * @Author tongliang@VRV
 * @CreateTime 2019/03/11 14:16
 * @Description (密码复杂度及时效性等系统配置的映射实体类  系统配置)
 * @Version
 */
@ApiModel("系统配置信息")
@Getter
@Setter
@Table(name = "tb_conf")
public class SystemConfig {


    @ApiModelProperty("配置类型ID")
    @Id
    @Column(name = "conf_id")
    private String confId;


    @ApiModelProperty("配置是否开启标志")
    @Column(name = "conf_enable")
    private Byte confEnable;


    @ApiModelProperty("配置最近新时间")
    @Column(name = "conf_time")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",locale="zh",timezone="GMT+8")
    private Date confTime;


    @ApiModelProperty("配置值")
    @Column(name = "conf_value")
    private String confValue;

    @ApiModelProperty("更新状态标志")
    @Column(name = "status_Update")
    private Byte statusUpdate;

}
