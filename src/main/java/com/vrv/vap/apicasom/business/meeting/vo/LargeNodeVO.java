package com.vrv.vap.apicasom.business.meeting.vo;

import lombok.Data;

/**
 * 节点
 * @author liurz
 */
@Data
public class LargeNodeVO {
    /**
     * 节点名称
     */
    private String nodeName;
    /**
     * 会议时间:只记录正在进行的，这个时间是预约时间
     */
    private String meetingTime;
}
