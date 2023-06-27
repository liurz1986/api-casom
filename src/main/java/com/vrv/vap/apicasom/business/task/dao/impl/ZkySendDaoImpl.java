package com.vrv.vap.apicasom.business.task.dao.impl;

import com.vrv.vap.apicasom.business.task.dao.ZkySendDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ZkySendDaoImpl implements ZkySendDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public boolean isRepeat(String time){
        String sql="select count(*) from zky_send where DATE_FORMAT(start_time,'%Y-%m-%d')='{0}'";
        sql = sql.replace("{0}",time);
        int count = jdbcTemplate.queryForObject(sql,Integer.class);
        if(count > 0){
            return true;
        }
        return false;
    }

    @Override
    public void deleteRepeatTimeData(String dateTime) {
        String sql="delete  from zky_send where DATE_FORMAT(start_time,'%Y-%m-%d')='{0}' ";
        sql = sql.replace("{0}",dateTime);
        jdbcTemplate.execute(sql);
    }
}
