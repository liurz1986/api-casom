package com.vrv.vap.apicasom.business.meeting.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 *
 * @author vrv
 */
@Data
public class StatisticSearchVO {
    /**
     * month(近一个月)、halfyear(半年)、year(一年)、none表示手动输入(startDate、endDate)
     */
    private String type;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date startDate;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date endDate;
}
