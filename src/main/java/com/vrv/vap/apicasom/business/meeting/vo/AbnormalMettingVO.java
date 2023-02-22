package com.vrv.vap.apicasom.business.meeting.vo;

import lombok.Data;

import java.util.Date;

/**
 * 异常会议
 * @author vrv
 */
@Data
public class AbnormalMettingVO {
    /**
     * 异常名称
     */
    private String name;
    /**
     * 异常类型
     */
    private String type;
    /**
     * 严重等级
     */
    private String grade;
    /**
     * 故障开始时间
     */
    private Date startTime;
    /**
     * 故障持续时间长度
     */
    private String abnormalTime;
}
