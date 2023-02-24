package com.vrv.vap.apicasom.business.meeting.dao;

import com.vrv.vap.apicasom.business.meeting.vo.*;

import java.util.List;

public interface AbnormalMettingDao {

    public List<DistributionStatisticsVO> typeStatistics(StatisticSearchVO statisticSearchVO);

    public List<DistributionStatisticsVO> gradeStatistics(StatisticSearchVO statisticSearchVO);

    public List<AbnormalMettingTrendVO> getTreandStatistics(StatisticSearchVO statisticSearchVO);

    public long getPageTotal(AbnormalMettingSearchVO abnormalMettingSearchVO);

    public List<AbnormalMettingVO> getPageList(AbnormalMettingSearchVO abnormalMettingSearchVO);

    public List<AbnormalMettingExportExcelVO> exportData(AbnormalMettingSearchVO abnormalMettingSearchVO);

    public List<CommonQueryVO> getAbnormalMettingCitys(String type);

    public  List<LargeDeatailVO> getStatisticsByName(String type);

    public int getHistoryTotalCount(String type);
}
