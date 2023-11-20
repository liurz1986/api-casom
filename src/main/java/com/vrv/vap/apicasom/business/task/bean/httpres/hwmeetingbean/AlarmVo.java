package com.vrv.vap.apicasom.business.task.bean.httpres.hwmeetingbean;

import lombok.Data;

/**
 * @author: 梁国露
 * @since: 2023/2/20 10:32
 * @description:
 */
@Data
public class AlarmVo {

    private String id;
    // 告警标识
    private String alarmNo;
    // 告警名称
    private String name;
    // 告警级别
    private String severity;
    // 告警来源分类
    private String alarmType;
    // 告警时间
    private String alarmTime;
    // 告警确认或恢复时间
    private String clearedTime;
    // 会议Id
    private String confId;

}
