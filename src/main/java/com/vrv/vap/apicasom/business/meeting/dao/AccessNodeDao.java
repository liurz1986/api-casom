package com.vrv.vap.apicasom.business.meeting.dao;

import com.vrv.vap.apicasom.business.meeting.vo.*;

import java.util.List;
import java.util.Map;

public interface AccessNodeDao {

    public long getPageTotal(AccessNodeSearchVO accessNodeSearchVO);

    public List<AccessNodeVO> getPageList(AccessNodeSearchVO accessNodeSearchVO);

    public List<AccessNodeExportExcelVO> exportData(AccessNodeSearchVO accessNodeSearchVO);

    public int getOnLineNodes(String status);

    public List<CommonQueryVO> queryNodesGroupByCity(String type);

    public List<String> getRunMettingCitys(String type);

    public  List<CommonQueryVO> queryNodeNamesByCity(String type, String cityName);

    public  List<NodeVO> queryRunNodesByCity(String type, String cityName);
}
