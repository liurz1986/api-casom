package com.vrv.vap.apicasom.business.task.bean.hwmeetingbean;

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
    private int totalElements;
    private int totalPages;
    private int number;
    private int size;
    private Sort sort;
    private int numberOfElements;
    private boolean first;
    private boolean empty;
}
