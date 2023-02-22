package com.vrv.vap.apicasom.business.task.service;


import com.vrv.vap.apicasom.business.task.bean.ZkyUnitBean;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author: 梁国露
 * @since: 2023/2/17 14:04
 * @description:
 */
public interface MeetingHttpService {
    /**
     * 获取token
     * @return
     */
    public String getToken(Integer errorNum);

    /**
     * 更新token
     * @param newToken
     */
    public void updateToken(String newToken);

    /**
     * 查询会议列表
     */
    public List<String> getHistoryMeetingList(String startTime, String endTime,Integer errorNum);

    /**
     * 查询历史会议详情
     */
    public void getHistoryMeetingInfo(String id,Integer errorNum);

    /**
     * 查询历史会议告警
     * @param id
     */
    public void getHistoryMeetingAlarm(String id,Integer errorNum);

    /**
     * 查询预约会议列表
     */
    public List<String> getNowMeetingList(String startTime, String endTime,Integer errorNum);

    /**
     * 查询预约会议详情
     * @param id
     */
    public void getNowMeetingInfo(String id,Integer errorNum);

    public void getNowMeetingParticipants(String id,String organizationName,int duration, Date scheduleStartTime);

    /**
     * 更新城市信息
     * @param map
     */
    public void updateCity(Map<String, ZkyUnitBean> map);


}
