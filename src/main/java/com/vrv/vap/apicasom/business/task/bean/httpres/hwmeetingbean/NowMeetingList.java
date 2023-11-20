package com.vrv.vap.apicasom.business.task.bean.httpres.hwmeetingbean;

import lombok.Data;

import java.util.List;

/**
 * @author: 梁国露
 * @since: 2023/2/20 11:09
 * @description:
 */
@Data
public class NowMeetingList {
    private List<ScheduleConfBrief> content;
    private Pageable pageable;
    private boolean last;
    // 总条数
    private int totalElements;
    // 总页数
    private int totalPages;
    // 当前页码
    private int number;
    private int size;
    private Sort sort;
    // 当前页条数
    private int numberOfElements;
    private boolean first;
    private boolean empty;
}
