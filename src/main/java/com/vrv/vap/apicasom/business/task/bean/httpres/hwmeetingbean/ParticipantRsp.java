package com.vrv.vap.apicasom.business.task.bean.httpres.hwmeetingbean;

import lombok.Data;

/**
 * @author: 梁国露
 * @since: 2023/2/16 18:10
 * @description:
 */
@Data
public class ParticipantRsp {
    // 会场索引
    private String id;
    // 与会者名称
    private String name;
    // 终端类型
    private String terminalType;
    // 组织名称
    private String organizationName;
}
