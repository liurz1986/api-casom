package com.vrv.vap.apicasom.business.meeting.vo;

import lombok.Data;

/**
 * 公文交换箱系统情况 vo
 */
@Data
public class ExchangeBoxVO {

    private long userTotal; // 用户总数

    private long userLoginTotal; // 用户登录总数

    private ExchangeBoxExtendVO reviceFile; // 收文

    private ExchangeBoxExtendVO signFile; // 批件

    private ExchangeBoxExtendVO secrecyFile; // 密刊

}
