package com.vrv.vap.apicasom.business.task.service.impl;

import com.vrv.vap.apicasom.business.task.service.HwMeetingDataService;
import com.vrv.vap.apicasom.business.task.service.MeetingHttpService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: 梁国露
 * @since: 2023/2/16 17:15
 * @description:
 */
@Service("historyHwMeetingDataService")
public class HistoryHwMeetingDataServiceImpl implements HwMeetingDataService {

    @Autowired
    private MeetingHttpService meetingHttpService;

    @Override
    public List<String> queryMeetingIds(String startTime, String endTime) {
        List<String> ids = meetingHttpService.getHistoryMeetingList(startTime,endTime,0);
        return ids;
    }

    @Override
    public void handleMeetingInfo(List<String> ids) {
        if(CollectionUtils.isNotEmpty(ids)){
            // 删除历史数据
            // 删除会议详情信息
            meetingHttpService.deleteDbData("hw_meeting_info",ids);
            // 删除会议节点信息
            meetingHttpService.deleteDbData("hw_meeting_participant",ids);
            // 删除会议与会人信息
            meetingHttpService.deleteDbData("hw_meeting_attendee",ids);
        }
        for(String id : ids){
            meetingHttpService.getHistoryMeetingInfo(id,0);
        }
    }

    @Override
    public void handleMeetingAlarm(List<String> ids) {
        if(CollectionUtils.isNotEmpty(ids)){
            // 删除会议告警信息
            meetingHttpService.deleteDbData("hw_meeting_alarm",ids);
        }
        for(String id: ids){
            meetingHttpService.getHistoryMeetingAlarm(id,0);
        }
    }
}
