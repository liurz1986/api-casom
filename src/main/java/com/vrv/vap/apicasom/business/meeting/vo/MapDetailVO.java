package com.vrv.vap.apicasom.business.meeting.vo;

import lombok.Data;

/**
 * 态势应用地图详情VO
 */
@Data
public class MapDetailVO {
    private String name; //研究所

    private int localSendNum; // 本地发件数量

    private int localReceiveNum;  // 本地收件数量

    private int transRegionalSendNum; // 跨地区发件数量

    private int transRegionalReceiveNum;  //跨地区收件数量
}
