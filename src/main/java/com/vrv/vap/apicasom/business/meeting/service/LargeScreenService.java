package com.vrv.vap.apicasom.business.meeting.service;

import com.vrv.vap.apicasom.business.meeting.vo.CommonSearchVO;
import com.vrv.vap.apicasom.business.meeting.vo.LargeMapDetailVO;
import com.vrv.vap.apicasom.business.meeting.vo.LargeMapVO;
import com.vrv.vap.apicasom.business.meeting.vo.LargeScreenBaseMessageVO;

import java.util.List;

public interface LargeScreenService {

    public LargeScreenBaseMessageVO queryBaseMessage(String type);

    public List<LargeMapVO> queryMapMesage(String type);

    public LargeMapDetailVO queryCityDetail(CommonSearchVO commonSearchVO);
}
