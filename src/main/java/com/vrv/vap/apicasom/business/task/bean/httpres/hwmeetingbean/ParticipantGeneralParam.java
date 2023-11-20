package com.vrv.vap.apicasom.business.task.bean.httpres.hwmeetingbean;

import lombok.Data;

/**
 * @author: 梁国露
 * @since: 2023/2/20 11:45
 * @description:
 */
@Data
public class ParticipantGeneralParam {
    // 会场Id
    private String id;
    // 会场名称
    private String name;
    // 会场设备类型
    private String model;
}
