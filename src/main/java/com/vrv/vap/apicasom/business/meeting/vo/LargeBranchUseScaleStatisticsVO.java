package com.vrv.vap.apicasom.business.meeting.vo;

import lombok.Data;

import java.util.List;

/**
 * 点对点会议次数、各地区使用占比VO
 * 多点会议次数、故障情况分析VO
 * @author vrv
 */
@Data
public class LargeBranchUseScaleStatisticsVO {
    /**
     * 点对点会议次数 /多点会议
     */
    private int pointNum;

    /**
     * 各地区使用占比 /故障情况分析
     */
    private List<LargeDeatailVO> detail;


}
