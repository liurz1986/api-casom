package com.vrv.vap.apicasom.business.task.service.impl;

import com.vrv.vap.apicasom.business.task.bean.HwMeetingAlarm;
import com.vrv.vap.apicasom.business.task.bean.HwMeetingInfo;
import com.vrv.vap.apicasom.business.task.service.HwMeetingAlarmService;
import com.vrv.vap.apicasom.business.task.service.HwMeetingInfoService;
import com.vrv.vap.apicasom.business.task.service.repository.HwMeetingAlarmRespository;
import com.vrv.vap.apicasom.business.task.service.repository.HwMeetingInfoRespository;
import com.vrv.vap.jpa.basedao.BaseRepository;
import com.vrv.vap.jpa.baseservice.impl.BaseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: 梁国露
 * @since: 2023/2/17 09:46
 * @description:
 */
@Service
public class HwMeetingAlarmServiceImpl extends BaseServiceImpl<HwMeetingAlarm, String> implements HwMeetingAlarmService {
    @Autowired
    private HwMeetingAlarmRespository hwMeetingAlarmRespository;

    @Override
    public BaseRepository<HwMeetingAlarm, String> getRepository() {
        return hwMeetingAlarmRespository;
    }
}
