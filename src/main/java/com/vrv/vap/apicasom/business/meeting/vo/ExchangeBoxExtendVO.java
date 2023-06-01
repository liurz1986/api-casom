package com.vrv.vap.apicasom.business.meeting.vo;

import lombok.Data;

/**
 *  公文交换箱系统情况中记录流转和登记总数
 */
@Data
public class ExchangeBoxExtendVO {
    private long roamTotal; // 流转总数

    private long registerTotal; // 登记总数
}
