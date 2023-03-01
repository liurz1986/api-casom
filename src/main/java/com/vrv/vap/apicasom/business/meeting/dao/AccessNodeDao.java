package com.vrv.vap.apicasom.business.meeting.dao;

import com.vrv.vap.apicasom.business.meeting.vo.*;

import java.util.List;

public interface AccessNodeDao {

    public long getPageTotal(AccessNodeSearchVO accessNodeSearchVO);

    public List<AccessNodeVO> getPageList(AccessNodeSearchVO accessNodeSearchVO);

    public List<AccessNodeExportExcelVO> exportData(AccessNodeSearchVO accessNodeSearchVO);

}
