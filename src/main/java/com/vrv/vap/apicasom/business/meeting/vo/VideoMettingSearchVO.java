package com.vrv.vap.apicasom.business.meeting.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 视频会议查询
 * @author vrv
 */
@Data
public class VideoMettingSearchVO {
    /**
     * 开始时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date startTime;
    /**
     * 截止时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date endTime;
    /**
     * 起始页
     */
    private Integer start_;
    /**
     * 每页行数
     */
    private Integer count_;

    /**
     * 参会节点
     */
    private String nodeName;
    /**
     * 排序字段名称
     */
    private String order_;

    /**
     * 升序(asc)、降序(desc)
     */
    private String by_;
}
