package com.vrv.vap.apicasom.business.task.service.impl;

import com.vrv.vap.apicasom.business.task.bean.HwMeetingAlarm;
import com.vrv.vap.apicasom.business.task.dao.HwMeetingDao;
import com.vrv.vap.apicasom.business.task.service.HwMeetingDataService;
import com.vrv.vap.apicasom.business.task.service.MeetingHttpService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: 梁国露
 * @since: 2023/2/16 17:16
 * @description:
 */
@Service("reservationHwMeetingDataService")
public class ReservationHwMeetingDataServiceImpl implements HwMeetingDataService {
    @Autowired
    private MeetingHttpService meetingHttpService;

    @Autowired
    private HwMeetingDao hwMeetingDao;

    @Override
    public List<String> queryMeetingIds(String startTime, String endTime) {
        List<String> ids = meetingHttpService.getNowMeetingList(startTime,endTime,0);
        return ids;
    }

    @Override
    public void handleMeetingInfo(List<String> ids) {
        if(CollectionUtils.isNotEmpty(ids)){
            // 删除历史数据
            // 删除会议详情信息
            hwMeetingDao.deleteDbData("hw_meeting_info",ids);
            // 删除会议节点信息
            hwMeetingDao.deleteDbData("hw_meeting_participant",ids);
            // 删除会议与会人信息
            hwMeetingDao.deleteDbData("hw_meeting_attendee",ids);
        }
        for(String id:ids){
            meetingHttpService.getNowMeetingInfo(id,0);
        }
    }

    @Override
    public void handleMeetingAlarm(List<String> ids) {
        if(CollectionUtils.isNotEmpty(ids)){
            // 删除会议告警信息
            hwMeetingDao.deleteDbData("hw_meeting_alarm",ids);
        }
        for(String id:ids){
            meetingHttpService.getNowMeetingAlarm(id,0);
        }
    }
}
