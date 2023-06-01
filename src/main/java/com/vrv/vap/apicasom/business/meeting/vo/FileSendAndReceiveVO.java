package com.vrv.vap.apicasom.business.meeting.vo;

import lombok.Data;

/**
 * 收发件数量vo
 */
@Data
public class FileSendAndReceiveVO {

    private String name; // X轴名称

    private long localSendNum; // 本地发数量

    private long localReceiveNum;  // 本地收数量

    private long transRegionalSendNum; // 跨地区发数量

    private long transRegionalReceiveNum;  //跨地区收数量
}
