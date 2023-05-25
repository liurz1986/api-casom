package com.vrv.vap.apicasom.business.meeting.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 时间段查询
 * @author
 */
@Data
public class SituationLargeSearchVO {

    /**
     * 收发文件数量用到 tab页："1":各分院(地区)  "2":院机关各部门
     */
    private String tabName;

    /**
     * 时间类型区分
     * month(近一个月)、halfyear(半年)、year(一年)、all(全部)
     */
    private String type;

    /**
     * 分院名称，地图详情会用到
     */
    private String city;
}
