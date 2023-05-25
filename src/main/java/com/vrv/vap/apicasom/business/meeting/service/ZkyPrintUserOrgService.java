package com.vrv.vap.apicasom.business.meeting.service;

import com.vrv.vap.apicasom.business.meeting.bean.ZkyPrintUserOrg;
import com.vrv.vap.apicasom.business.meeting.vo.PrintUserOrgSerachVO;
import com.vrv.vap.jpa.baseservice.BaseService;
import com.vrv.vap.jpa.web.Result;
import com.vrv.vap.jpa.web.page.PageRes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ZkyPrintUserOrgService  extends BaseService<ZkyPrintUserOrg, String> {
    public Result<String> printUserOrgTemplate();

    public Result<String> importprintUserOrgFile(MultipartFile file) throws IOException;

    public PageRes<ZkyPrintUserOrg> printUserOrgGetPage(PrintUserOrgSerachVO printUserOrgSerachVO);

    public void printUserOrgDel(String guid);

    public Result<String> printUserOrgEdit(ZkyPrintUserOrg printUserOrg);



}
