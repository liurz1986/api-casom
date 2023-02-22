package com.vrv.vap.apicasom.business.meeting.dao;

import com.vrv.vap.apicasom.business.meeting.vo.*;

import java.util.List;

public interface VideoMettingDao {

    public List<DistributionStatisticsVO> queyMeetingDurationStatistics(StatisticSearchVO statisticSearchVO);

    public List<DistributionStatisticsVO> queyParticipantsStatistics(StatisticSearchVO statisticSearchVO);

    public long getPageTotal(VideoMettingSearchVO videoMettingSearchVO);

    public List<VideoMettingVO> getPageList(VideoMettingSearchVO videoMettingSearchVO);

    public List<VideoMettingExportExcelVO> exportData(VideoMettingSearchVO videoMettingSearchVO);
}
