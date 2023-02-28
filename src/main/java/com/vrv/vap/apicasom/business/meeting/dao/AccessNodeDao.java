package com.vrv.vap.apicasom.business.meeting.dao;

import com.vrv.vap.apicasom.business.meeting.vo.*;

import java.util.List;

public interface AccessNodeDao {

    public long getPageTotal(AccessNodeSearchVO accessNodeSearchVO);

    public List<AccessNodeVO> getPageList(AccessNodeSearchVO accessNodeSearchVO);

    public List<AccessNodeExportExcelVO> exportData(AccessNodeSearchVO accessNodeSearchVO);

    public List<CommonQueryVO> queryNodesGroupByCity();

    public List<String> getRunMettingCitys();

    public  List<KeyValueQueryVO> queryNodeNamesByCity(String cityName);

    public  List<NodeVO> queryRunNodesByCity(String cityName);

    public List<LargeBranchStatisticsVO> queryBranchStatistics(String type);

    public List<LargeDeatailVO> getUseStatisticsByBranch(String type);

    public int getUseStatisticsTotalCount(String type);

    public List<LargeDeatailVO> queryNodeMeetingCountStatistics(String type);

    public List<LargeDeatailVO> queryOutServiceStatistics(String type);
}
