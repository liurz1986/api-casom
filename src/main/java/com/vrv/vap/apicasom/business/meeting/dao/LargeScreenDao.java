package com.vrv.vap.apicasom.business.meeting.dao;

import com.vrv.vap.apicasom.business.meeting.vo.*;

import java.util.List;

public interface LargeScreenDao {
    public int getOnLineNodes();

    public int getOffLineMettingTotal(String type);

    public int getOfflineMettingUserCount(String type);

    public int getOfflineMeetingTimeTotal(String type);

    public  int getPointToPoint(String type);

    public int getManyPoint(String type);

    public List<CommonQueryVO> queryNodesGroupByCity();

    public List<String> getRunMettingCitys();

    public  List<KeyValueQueryVO> queryNodeNamesByCity(String cityName);

    public  List<NodeVO> queryRunNodesByCity(String cityName);

    public List<LargeBranchStatisticsVO> queryBranchStatistics(String type);

    public List<LargeDeatailVO> getUseStatisticsByBranch(String type);

    public int getUseStatisticsTotalCount(String type);

    public List<LargeDeatailVO> queryNodeMeetingCountStatistics(String type);

    public List<LargeDeatailVO> queryOutServiceStatistics(String type);

    public List<CommonQueryVO> getAbnormalMettingCitys();

    public  List<LargeDeatailVO> getStatisticsByName(String type);

    public int getHistoryTotalCount(String type);
}
