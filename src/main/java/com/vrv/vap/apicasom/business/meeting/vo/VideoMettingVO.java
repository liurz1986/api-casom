package com.vrv.vap.apicasom.business.meeting.vo;

import lombok.Data;

import java.util.Date;

/**
 * 视屏会议列表
 * @author vrv
 */
@Data
public class VideoMettingVO {

    /**
     * 会议日期
     */
    private Date  meetingDate;
    /**
     * 会议时间
     */
    private String meetingTime;
    /**
     * 参会节点
     */
    private String nodeNames ;
    /**
     *  参会单位
     */
    private String  companys ;
    /**
     *  参会节点数
     */
    private String  nodeNumber ;
    /**
     *  参数人数
     */
    private String  peopleNumber ;
}
