package com.vrv.vap.apicasom.business.meeting.service;


import com.vrv.vap.apicasom.business.meeting.bean.ZkyEmail;
import com.vrv.vap.apicasom.business.meeting.vo.ZkyEmailSearchVO;
import com.vrv.vap.jpa.baseservice.BaseService;
import com.vrv.vap.jpa.web.Result;
import com.vrv.vap.jpa.web.page.PageRes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ZkyEmailService extends BaseService<ZkyEmail, String> {
    public Result<String> emailTemplate();

    public Result<String> importEmailFile(MultipartFile file) throws IOException;

    public PageRes<ZkyEmail> emailGetPage(ZkyEmailSearchVO searchVO);

    public  void emailDelete(String guid);
}
