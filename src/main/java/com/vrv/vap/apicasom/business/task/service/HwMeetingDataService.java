package com.vrv.vap.apicasom.business.task.service;

import com.vrv.vap.apicasom.business.task.bean.HwMeetingAlarm;

import java.util.List;

/**
 * @author: 梁国露
 * @since: 2023/2/16 17:15
 * @description:
 */
public interface HwMeetingDataService {
    /**
     * 通过时间  查询会议ID
     * @param startTime
     * @param endTime
     * @return
     */
    List<String> queryMeetingIds(String startTime,String endTime);

    /**
     * 处理会议信息
     * @param ids
     */
    void handleMeetingInfo(List<String> ids);


    /**
     * 处理会议告警信息
     * @param ids
     */
    void handleMeetingAlarm(List<String> ids);
}
