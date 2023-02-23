package com.vrv.vap.apicasom.business.meeting.vo;

import lombok.Data;

import java.util.List;

/**
 * 地图城市详情
 * @author liurz
 */
@Data
public class LargeMapDetailVO {
    /**
     * 节点总数
     */
    private int nodeTotal;
    /**
     * 在线节点总数
     */
    private int runNodeTotal;

    /**
     * 组织结构
     */
    private List<LargeOrgVO> orgs;

}
