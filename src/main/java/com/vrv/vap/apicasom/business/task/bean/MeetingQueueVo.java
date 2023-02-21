package com.vrv.vap.apicasom.business.task.bean;

import lombok.Data;

/**
 * @author: 梁国露
 * @since: 2023/2/20 14:26
 * @description:
 */
@Data
public class MeetingQueueVo {
    private String method;
    private String param;
    private int failNum;
}
