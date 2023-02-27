package com.vrv.vap.apicasom.business.meeting.service;

import com.vrv.vap.apicasom.business.meeting.vo.*;

import java.util.List;

/**
 * 会议大屏
 *
 * @author liurz
 */
public interface LargeScreenService {

    public LargeScreenBaseMessageVO queryBaseMessage(String type);

    public List<LargeMapVO> queryMapMesage(String type);

    public LargeMapDetailVO queryCityDetail(CommonSearchVO commonSearchVO);

    public List<LargeBranchStatisticsVO> queryBranchStatistics(String type);

    public LargeBranchUseScaleStatisticsVO queryBranchScaleStatistics(String type);

    public List<LargeDeatailVO> queryBranchAbnormalStatistics(String type);

    public List<LargeDeatailVO> queryNodeMeetingCountStatistics(String type);

    public List<LargeDeatailVO> queryOutServiceStatistics(String type);

    public int getManyPoint(String type);
}
