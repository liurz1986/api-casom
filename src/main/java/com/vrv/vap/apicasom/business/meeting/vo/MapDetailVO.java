package com.vrv.vap.apicasom.business.meeting.vo;

import lombok.Data;

/**
 * 态势应用地图详情VO
 */
@Data
public class MapDetailVO {
    private String name; //研究所

    private long localSendNum; // 本地发件数量

    private long localReceiveNum;  // 本地收件数量

    private long transRegionalSendNum; // 跨地区发件数量

    private long transRegionalReceiveNum;  //跨地区收件数量
}
