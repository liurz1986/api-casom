package com.vrv.vap.apicasom.business.meeting.vo;

import com.vrv.vap.apicasom.business.meeting.bean.ZkyPrintUserOrg;
import lombok.Data;

@Data
public class PrintUserOrgSerachVO extends ZkyPrintUserOrg {
    /**
     * 起始页
     */
    private Integer start_;
    /**
     * 每页行数
     */
    private Integer count_;
}
