package com.vrv.vap.apicasom.business.meeting.vo;

import lombok.Data;

import java.util.Date;

/**
 * 查询节点对象
 * @author liurz
 */
@Data
public class NodeVO {
    /**
     * 节点名称
     */
    private String name;
    /**
     * 组织机构名称
     */
    private String organizationName;
    /**
     * 会议开始时间
     */
    private Date startTime;
    /**
     * 会议结束时间
     */
    private Date endTime;
    /**
     * 会议结束时间
     */
    private String stage;

}
