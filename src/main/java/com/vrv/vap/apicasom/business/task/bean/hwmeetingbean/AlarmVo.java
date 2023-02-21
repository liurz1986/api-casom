package com.vrv.vap.apicasom.business.task.bean.hwmeetingbean;

import lombok.Data;

/**
 * @author: 梁国露
 * @since: 2023/2/20 10:32
 * @description:
 */
@Data
public class AlarmVo {
    private String id;
    private String alarmNo;
    private String name;
    private String severity;
    private String alarmType;
    private String alarmTime;
    private String clearedTime;
    private String confId;

}
