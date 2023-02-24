package com.vrv.vap.apicasom.business.meeting.service;

import com.vrv.vap.apicasom.business.meeting.vo.AccessNodeSearchVO;
import com.vrv.vap.apicasom.business.meeting.vo.AccessNodeVO;
import com.vrv.vap.jpa.web.Result;
import com.vrv.vap.jpa.web.page.PageRes;

import javax.servlet.http.HttpServletResponse;
/**
 * 接入节点
 *
 * @author liurz
 */
public interface AccessNodeService {

    public PageRes<AccessNodeVO> getPage(AccessNodeSearchVO accessNodeSearchVO);

    public Result<String> exportData(AccessNodeSearchVO accessNodeSearchVO);

    public void exportAssetInfo(String fileName, HttpServletResponse response);
}
