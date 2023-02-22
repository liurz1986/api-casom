package com.vrv.vap.apicasom.business.meeting.service;

import com.vrv.vap.apicasom.business.meeting.vo.DistributionStatisticsVO;
import com.vrv.vap.apicasom.business.meeting.vo.StatisticSearchVO;
import com.vrv.vap.apicasom.business.meeting.vo.VideoMettingSearchVO;
import com.vrv.vap.apicasom.business.meeting.vo.VideoMettingVO;
import com.vrv.vap.jpa.web.Result;
import com.vrv.vap.jpa.web.page.PageRes;
import org.springframework.web.bind.annotation.PathVariable;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface VideoMettingService {

    /**
     * 会议时长分布统计
     * @param statisticSearchVO
     * @return
     */
    public List<DistributionStatisticsVO> queyMeetingDurationStatistics(StatisticSearchVO statisticSearchVO);

    /**
     * 参会人数分布统计
     * @param statisticSearchVO
     * @return
     */
    public List<DistributionStatisticsVO> queyParticipantsStatistics(StatisticSearchVO statisticSearchVO);

    /**
     * 历史会议列表
     * @param videoMettingSearchVO
     * @return
     */
    public PageRes<VideoMettingVO> getPage(VideoMettingSearchVO videoMettingSearchVO);

    /**
     *  历史会议导出
     * @param videoMettingSearchVO
     * @return
     */
    public Result<String> exportData(VideoMettingSearchVO videoMettingSearchVO);

    /**
     * 下载历史会议导出文件
     * @param fileName
     * @param response
     */
    public void exportAssetInfo(@PathVariable String fileName, HttpServletResponse response);
}
