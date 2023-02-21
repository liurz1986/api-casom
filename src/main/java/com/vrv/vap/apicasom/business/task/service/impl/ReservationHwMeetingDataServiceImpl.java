package com.vrv.vap.apicasom.business.task.service.impl;

import com.vrv.vap.apicasom.business.task.bean.HwMeetingAlarm;
import com.vrv.vap.apicasom.business.task.service.HwMeetingDataService;
import com.vrv.vap.apicasom.business.task.service.MeetingHttpService;
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

    @Override
    public List<String> queryMeetingIds(String startTime, String endTime) {
        List<String> ids = meetingHttpService.getNowMeetingList(startTime,endTime);
        return ids;
    }

    @Override
    public void handleMeetingInfo(List<String> ids) {
        for(String id:ids){
            meetingHttpService.getNowMeetingInfo(id);
        }
    }

    @Override
    public void handleMeetingAlarm(List<String> ids) {
    }
}
