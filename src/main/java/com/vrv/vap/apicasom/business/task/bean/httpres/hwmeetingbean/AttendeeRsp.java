package com.vrv.vap.apicasom.business.task.bean.httpres.hwmeetingbean;

import lombok.Data;

/**
 * @author: 梁国露
 * @since: 2023/2/16 18:12
 * @description:
 */
@Data
public class AttendeeRsp {
    //  与会人ID
    private String id;
    // 与会者名称
    private String name;
    // 与会者账号名称
    private String account;
    // 会场名称
    private String participantName;
    // 组织名称
    private String organizationName;
}
