package com.vrv.vap.apicasom.business.meeting.vo;

import lombok.Data;

import java.util.List;

/**
 * 组织结构
 * @author liurz
 */
@Data
public class LargeOrgVO {
    /**
     * 组织结构名称
     */
    private String orgName;

    /**
     * 节点名称
     */
    private List<LargeNodeVO>  nodes;
}
