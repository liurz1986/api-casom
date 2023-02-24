package com.vrv.vap.apicasom.business.task.bean.hwmeetingbean;

import lombok.Data;

/**
 * @author: 梁国露
 * @since: 2023/2/20 11:08
 * @description:
 */
@Data
public class ScheduleConfBrief {
    private String id;
    private String subject;
    private String scheduleStartTime;
    private int duration;
    private boolean active;
    private String organizationName;
    private String type;
}
