package com.vrv.vap.apicasom.business.meeting.service;

import com.vrv.vap.apicasom.business.meeting.vo.ZkyUnitSerachVO;
import com.vrv.vap.apicasom.business.meeting.vo.ZkyUnitVO;
import com.vrv.vap.apicasom.business.task.bean.ZkyUnitBean;
import com.vrv.vap.jpa.web.Result;
import com.vrv.vap.jpa.web.page.PageRes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ZkyUnitConfigService {

    PageRes<ZkyUnitBean> getPage(ZkyUnitSerachVO zkyUnitSerachVO);

    public Result<String> save(ZkyUnitVO zkyUnitVO);

    public Result<String> update(ZkyUnitVO zkyUnitVO);

    public void delete(String guid);

    public Result<String> exportDataTemplate();

    public Result<String> exportData(ZkyUnitSerachVO zkyUnitSerachVO);

    public void exportFileInfo(String fileName, HttpServletResponse response);

    public Result<String> importFileInfo(MultipartFile file) throws IOException;



}
