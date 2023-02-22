package com.vrv.vap.apicasom.business.meeting.service;

import com.vrv.vap.apicasom.business.meeting.vo.*;
import com.vrv.vap.jpa.web.Result;
import com.vrv.vap.jpa.web.page.PageRes;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.List;

public interface AbnormalMettingService {

   public List<DistributionStatisticsVO> typeStatistics(StatisticSearchVO statisticSearchVO);

   public List<DistributionStatisticsVO> gradeStatistics(StatisticSearchVO statisticSearchVO);

    public  List<AbnormalMettingTrendVO> trendStatistics(StatisticSearchVO statisticSearchVO) throws ParseException;

    public PageRes<AbnormalMettingVO> getPage(AbnormalMettingSearchVO abnormalMettingSearchVO);

    public Result<String> exportData(AbnormalMettingSearchVO abnormalMettingSearchVO);

    public void exportAssetInfo(String fileName, HttpServletResponse response);
}
