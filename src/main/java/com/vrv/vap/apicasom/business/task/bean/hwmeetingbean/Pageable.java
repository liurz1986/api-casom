package com.vrv.vap.apicasom.business.task.bean.hwmeetingbean;

import lombok.Data;

/**
 * @author: 梁国露
 * @since: 2023/2/16 17:41
 * @description:
 */
@Data
public class Pageable {
    private Sort sort;
    private int offset;
    private int pageSize;
    private int pageNumber;
    private boolean unpaged;
    private boolean paged;

}
