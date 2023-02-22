package com.vrv.vap.apicasom.business.meeting.vo;

import lombok.Data;

/**
 * @author vrv
 */
@Data
public class AccessNodeSearchVO {

    /**
     * 起始页
     */
    private Integer start_;
    /**
     * 每页行数
     */
    private Integer count_;
    /**
     * 节点名称
     */
    private String nodeName;
    /**
     * 所属分院/地区
     */
    private String region;
}
