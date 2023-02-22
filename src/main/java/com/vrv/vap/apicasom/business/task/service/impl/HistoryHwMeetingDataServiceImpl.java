package com.vrv.vap.apicasom.business.task.service.impl;

import com.vrv.vap.apicasom.business.task.bean.HwMeetingAlarm;
import com.vrv.vap.apicasom.business.task.service.HwMeetingDataService;
import com.vrv.vap.apicasom.business.task.service.MeetingHttpService;
import com.vrv.vap.apicasom.frameworks.config.ExecutorConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Executor;

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
        for(String id : ids){
                meetingHttpService.getHistoryMeetingInfo(id,0);
        }
    }

    @Override
    public void handleMeetingAlarm(List<String> ids) {
        for(String id: ids){
//            alarmExecutor.execute(()->{
                meetingHttpService.getHistoryMeetingAlarm(id,0);
//            });
        }
    }
}
