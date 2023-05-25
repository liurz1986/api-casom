package com.vrv.vap.apicasom.business.meeting.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vrv.vap.apicasom.business.meeting.bean.ZkyExchangeBox;
import lombok.Data;

import java.util.Date;

@Data
public class ZkyExchangeBoxSearchVO extends ZkyExchangeBox {
    /**
     * 起始页
     */
    private Integer start_;
    /**
     * 每页行数
     */
    private Integer count_;

    /**
     * 截至时间-开始时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date deadlineStart;

    /**
     * 截至时间-结束时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date deadlineEnd;

    /**
     * 导入时间-开始时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date importTimeStart;

    /**
     * 导入时间-结束时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date importTimeEnd;
}
