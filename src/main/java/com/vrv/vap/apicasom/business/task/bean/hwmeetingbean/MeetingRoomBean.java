package com.vrv.vap.apicasom.business.task.bean.hwmeetingbean;

import lombok.Data;

/**
 * @author: 梁国露
 * @since: 2023/2/22 14:32
 * @description:
 */
@Data
public class MeetingRoomBean {
    private String id;
    private String name;
    private String organizationId;
    private String organizationName;
    private String areaId;
    private String areaName;
    private String serviceZoneId;
    private String serviceZoneName;
    private Boolean provisionEua;
    private String email;
    private String remarks;
}
