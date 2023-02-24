package com.vrv.vap.apicasom.business.meeting.vo;

import lombok.Data;

/**
 * 大屏地图信息VO
 * @author liurz
 */
@Data
public class LargeMapVO {
    /**
     * 城市
     */
    private String city;

    /**
     * 节点数
     */
    private int nodes;

    /**
     * 状态：abnormal（存在异常会议），run(存在在线会议)，none(不存在异常会议和正在进行的会议)
     */
    private String status;
    /**
     * 告警信息描述:组织机构+节点名称+"异常告警"
     *
     */
    private String abnormalMessge;

}
