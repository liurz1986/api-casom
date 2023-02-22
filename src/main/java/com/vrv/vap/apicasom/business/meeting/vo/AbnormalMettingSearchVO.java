package com.vrv.vap.apicasom.business.meeting.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 异常会议列表
 * @author vrv
 */
@Data
public class AbnormalMettingSearchVO {
    /**
     * 异常名称
     */
    private String name;
    /**
     * 异常类型
     */
    private String abnormalType;
    /**
     * 严重等级
     */
    private String grade;

    /**
     * month(近一个月)、halfyear(半年)、year(一年)、none表示手动输入(startDate、endDate)
     */
    private String type;
    /**
     * 开始时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date startDate;
    /**
     * 截止时间间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date endDate;
    /**
     * 起始页
     */
    private Integer start_;
    /**
     * 每页行数
     */
    private Integer count_;

}
