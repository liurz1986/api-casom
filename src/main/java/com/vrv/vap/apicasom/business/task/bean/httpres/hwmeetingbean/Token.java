package com.vrv.vap.apicasom.business.task.bean.httpres.hwmeetingbean;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author: 梁国露
 * @since: 2023/2/16 17:34
 * @description:
 */
@Data
public class Token {
    @ApiModelProperty("token")
    private String uuid;

    @ApiModelProperty("用户类型")
    private String userType;

    @ApiModelProperty("过期时间")
    private Long expire;

    @ApiModelProperty("剩余过期天数")
    private String passwordExpireAfter;
}
