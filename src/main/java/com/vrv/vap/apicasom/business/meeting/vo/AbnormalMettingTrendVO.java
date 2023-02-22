package com.vrv.vap.apicasom.business.meeting.vo;

import lombok.Data;

/**
 * 异常会签
 * @author vrv
 */
@Data
public class AbnormalMettingTrendVO {
    /**
     * X轴数据
     */
    private String dataX;
    /**
     * Y轴数据
     */
    private int dataY;
}
