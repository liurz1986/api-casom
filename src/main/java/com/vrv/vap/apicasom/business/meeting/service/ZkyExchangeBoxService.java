package com.vrv.vap.apicasom.business.meeting.service;

import com.vrv.vap.apicasom.business.meeting.bean.ZkyExchangeBox;
import com.vrv.vap.apicasom.business.meeting.vo.ZkyExchangeBoxSearchVO;
import com.vrv.vap.jpa.baseservice.BaseService;
import com.vrv.vap.jpa.web.Result;
import com.vrv.vap.jpa.web.page.PageRes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ZkyExchangeBoxService extends BaseService<ZkyExchangeBox, String> {
    public Result<String> documentTemplate();

    Result<String> importDocumentFile(MultipartFile file) throws IOException;

    public PageRes<ZkyExchangeBox> documentGetPage(ZkyExchangeBoxSearchVO searchVO);

    public void documentDelete(String guid);
}
