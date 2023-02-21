package com.vrv.vap.apicasom.business.task.service.impl;

import com.vrv.vap.apicasom.business.task.bean.HwMeetingAlarm;
import com.vrv.vap.apicasom.business.task.bean.HwMeetingAttendee;
import com.vrv.vap.apicasom.business.task.service.HwMeetingAlarmService;
import com.vrv.vap.apicasom.business.task.service.HwMeetingAttendeeService;
import com.vrv.vap.apicasom.business.task.service.repository.HwMeetingAlarmRespository;
import com.vrv.vap.apicasom.business.task.service.repository.HwMeetingAttendeeRespository;
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
public class HwMeetingAttendeeServiceImpl extends BaseServiceImpl<HwMeetingAttendee, String> implements HwMeetingAttendeeService {
    @Autowired
    private HwMeetingAttendeeRespository hwMeetingAttendeeRespository;

    @Override
    public BaseRepository<HwMeetingAttendee, String> getRepository() {
        return hwMeetingAttendeeRespository;
    }
}
