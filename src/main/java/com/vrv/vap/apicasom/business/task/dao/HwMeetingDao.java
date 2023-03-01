package com.vrv.vap.apicasom.business.task.dao;

import java.util.List;

/**
 * @author: 梁国露
 * @since: 2023/3/1 10:25
 * @description:
 */
public interface HwMeetingDao {
    /**
     * 删除数据
     * @param tableName
     * @param ids
     */
    public void deleteDbData(String tableName, List<String> ids);
}
