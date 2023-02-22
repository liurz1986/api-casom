package com.vrv.vap.apicasom.business.meeting.vo;

import lombok.Data;

/**
 * 接入节点列表展示
 * @author vrv
 */

@Data
public class AccessNodeVO {

    /**
     * 节点名称
     */
    private String nodeName;
    /**
     * 会议次数
     */
    private String meetingCount;
    /**
     *  会议时长
     */
    private String meetingTimeTotal;
    /**
     *  所属分院/地区
     */
    private String region;
    /**
     *  设备型号
     */
    private String  assetType ;


}
