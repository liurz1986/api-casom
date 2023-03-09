package com.vrv.vap.apicasom.business.task.bean.httpres.hwmeetingbean;

import lombok.Data;

/**
 * @author: 梁国露
 * @since: 2023/2/16 17:44
 * @description:
 */
@Data
public class Content {
    private String id;
    private String accessCode;
    private String subject;
    private String creatorName;
    private String category;
    private String scheduleStartTime;
    private String scheduleEndTime;
    private String mainServiceZoneName;
    private String organizationName;
    private String type;
    private String stage;
}
