package com.vrv.vap.apicasom.business.task.service.impl;

import com.vrv.vap.apicasom.business.task.bean.HwMeetingAlarm;
import com.vrv.vap.apicasom.business.task.bean.HwMeetingParticipant;
import com.vrv.vap.apicasom.business.task.service.HwMeetingAlarmService;
import com.vrv.vap.apicasom.business.task.service.HwMeetingParticipantService;
import com.vrv.vap.apicasom.business.task.service.repository.HwMeetingAlarmRespository;
import com.vrv.vap.apicasom.business.task.service.repository.HwMeetingParticipantRespository;
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
public class HwMeetingParticipantServiceImpl extends BaseServiceImpl<HwMeetingParticipant, String> implements HwMeetingParticipantService {
    @Autowired
    private HwMeetingParticipantRespository hwMeetingParticipantRespository;

    @Override
    public BaseRepository<HwMeetingParticipant, String> getRepository() {
        return hwMeetingParticipantRespository;
    }
}
