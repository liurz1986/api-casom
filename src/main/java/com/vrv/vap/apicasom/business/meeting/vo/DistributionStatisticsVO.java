package com.vrv.vap.apicasom.business.meeting.vo;

import lombok.Data;

/**
 * 分布统计：会议时长分布、参会人数分布、异常类型分布、严重等级分布
 * @author vrv
 */
@Data
public class DistributionStatisticsVO {

    /**
     * 名称
     */
    private String name;

    /**
     * 数量
     */
    private int num;

}
