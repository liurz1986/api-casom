package com.vrv.vap.apicasom.business.task.bean.hwmeetingbean;

import lombok.Data;

import java.util.List;

/**
 * @author: 梁国露
 * @since: 2023/2/22 14:30
 * @description:
 */
@Data
public class MeetingRoomsRes {
    private List<MeetingRoomBean> content;
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
