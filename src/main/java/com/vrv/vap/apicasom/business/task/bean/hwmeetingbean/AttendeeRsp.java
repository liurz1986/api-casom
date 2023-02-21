package com.vrv.vap.apicasom.business.task.bean.hwmeetingbean;

import lombok.Data;

/**
 * @author: 梁国露
 * @since: 2023/2/16 18:12
 * @description:
 */
@Data
public class AttendeeRsp {
    private String id;
    private String name;
    private String account;
    private String participantName;
    private String organizationName;
}
