package com.vrv.vap.apicasom.business.task.dao.impl;

import com.vrv.vap.apicasom.business.task.dao.HwMeetingDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author: 梁国露
 * @since: 2023/3/1 10:26
 * @description:
 */
@Repository
public class HwMeetingDaoImpl implements HwMeetingDao {


    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 删除数据
     * @param tableName
     * @param ids
     */
    @Override
    public void deleteDbData(String tableName, List<String> ids){
        String idStr = String.join("','",ids);
        String sql = "delete from {0} where meeting_id in ('{1}');";
        sql = sql.replace("{0}",tableName).replace("{1}",idStr);
        jdbcTemplate.execute(sql);
    }
}
