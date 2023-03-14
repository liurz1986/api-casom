package com.vrv.vap.apicasom.business.meeting.vo;

import lombok.Data;

@Data
public class ZkyUnitSerachVO extends ZkyUnitVO{
    /**
     * 起始页
     */
    private Integer start_;
    /**
     * 每页行数
     */
    private Integer count_;

    /**
     * 排序字段名称
     */
    private String order_;

    /**
     * 升序(asc)、降序(desc)
     */
    private String by_;
}
