package com.vrv.vap.apicasom.business.meeting.vo;

import lombok.Data;

/**
 * 综合大屏基础信息
 * 视频会议系统节点数量、当前在线节点数、当前开会节点数、参会总人数、会议总时长、开会次数
 * @author liurz
 */
@Data
public class IntegratedlsBaseVO {
    /**
     * 视频会议系统节点数量
     */
    private int videoNodeCount ;

    /**
     * 当前在线节点数
     */
    private int onlineNodeTotal ;

    /**
     * 当前开会节点数
     */
    private int currMettingCount ;

    /**
     * 参会总人数
     */
    private int mettingUserCount ;

    /**
     * 会议总时长:小时，取整，四舍五入
     */
    private int meetingTimeTotal ;

    /**
     * 开会次数
     */
    private int meetingTimes ;

    /**
     * 发送文件件数
     */
    private int sendFiles ;

    /**
     * 接收文件件数
     */
    private int receiveFiles ;
}
