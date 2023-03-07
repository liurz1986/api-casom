package com.vrv.vap.apicasom.business.meeting.service;

import com.vrv.vap.apicasom.business.meeting.vo.IntegratedLargeSearchVO;
import com.vrv.vap.apicasom.business.meeting.vo.IntegratedlsBaseVO;
import com.vrv.vap.apicasom.business.meeting.vo.KeyValueQueryVO;

import java.text.ParseException;
import java.util.List;

public interface IntegratedLargeScreenService {

    public IntegratedlsBaseVO queryBaseMessage(IntegratedLargeSearchVO searchVO);

    public  long  queryNodes();

    public List<KeyValueQueryVO>  queryBranchNodeStatistics();

    public List<KeyValueQueryVO>  queryMettingsStatistics(IntegratedLargeSearchVO searchVO) throws ParseException;
}
