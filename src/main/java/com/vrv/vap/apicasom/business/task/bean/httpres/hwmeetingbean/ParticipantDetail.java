package com.vrv.vap.apicasom.business.task.bean.httpres.hwmeetingbean;

import lombok.Data;

/**
 * @author: 梁国露
 * @since: 2023/2/20 11:49
 * @description:
 */
@Data
public class ParticipantDetail {
    // 会场基本参数
    private ParticipantGeneralParam generalParam;
    // 会场状态
    private ParticipantState state;
}
