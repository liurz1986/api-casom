package com.vrv.vap.apicasom.business.task.bean.httpres.hwmeetingbean;

import lombok.Data;

/**
 * @author: 梁国露
 * @since: 2023/2/20 11:22
 * @description:
 */
@Data
public class ConferenceRsp {
    // 会议Id
    private String id;
    // 会议主题
    private String subject;
    // 会议开始时间
    private String scheduleStartTime;
    // 时长
    private int duration;
    // 会议所处阶段
    private String stage;
    // 组织名称
    private String organizationName;
}
