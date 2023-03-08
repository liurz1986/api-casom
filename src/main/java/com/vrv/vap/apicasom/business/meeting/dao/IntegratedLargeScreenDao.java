package com.vrv.vap.apicasom.business.meeting.dao;

import com.vrv.vap.apicasom.business.meeting.vo.IntegratedLargeSearchVO;
import com.vrv.vap.apicasom.business.meeting.vo.KeyValueQueryVO;

import java.util.Date;
import java.util.List;

public interface IntegratedLargeScreenDao {
    public int onLineMettingCount();

    public int getOfflineMettingUserCount(IntegratedLargeSearchVO searchVO);

    public int getOfflineMeetingTimeTotal(IntegratedLargeSearchVO searchVO);

    public List<KeyValueQueryVO> queryBranchNodeStatistics();

    public List<KeyValueQueryVO> getTreandStatistics(Date endDate, Date beginDate, String hourY);

    public  int getOffLineMettingTotal(IntegratedLargeSearchVO searchVO);
}
