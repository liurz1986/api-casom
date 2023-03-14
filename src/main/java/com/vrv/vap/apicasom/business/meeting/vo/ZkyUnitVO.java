package com.vrv.vap.apicasom.business.meeting.vo;

import lombok.Data;


/**
 * 节点接触数据配置VO
 */
@Data
public class ZkyUnitVO {
    /**
     * id
     */
    private String id;
    /**
     * 节点code
     */
    private String participantCode;
    /**
     * 节点名称
     */
    private String participantName;
    /**
     * 城市
     */
    private String city;
    /**
     * 分院
     */
    private String branch;
}
