package com.vrv.vap.apicasom.business.task.service.impl;

import com.vrv.vap.apicasom.business.task.bean.HwMeetingInfo;
import com.vrv.vap.apicasom.business.task.bean.ZkyUnitBean;
import com.vrv.vap.apicasom.business.task.service.HwMeetingInfoService;
import com.vrv.vap.apicasom.business.task.service.ZkyUnitService;
import com.vrv.vap.apicasom.business.task.service.repository.HwMeetingInfoRespository;
import com.vrv.vap.apicasom.business.task.service.repository.ZkyUnitRespository;
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
public class HwMeetingInfoServiceImpl extends BaseServiceImpl<HwMeetingInfo, String> implements HwMeetingInfoService {

    @Autowired
    private HwMeetingInfoRespository hwMeetingInfoRespository;

    @Override
    public BaseRepository<HwMeetingInfo, String> getRepository() {
        return hwMeetingInfoRespository;
    }
}
