package com.vrv.vap.apicasom.business.meeting.dao.impl;

import com.vrv.vap.apicasom.business.meeting.dao.SituationLargeScreenDao;
import com.vrv.vap.apicasom.business.meeting.util.SituationLargeScreenUtil;
import com.vrv.vap.apicasom.business.meeting.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *应用态势大屏查询
 *
 */
@Repository
public class SituationLargeScreenDaoImpl implements SituationLargeScreenDao {
    private static Logger logger = LoggerFactory.getLogger(SituationLargeScreenDaoImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     *公文及文件交换系统发件数量、收件数量TOP10
     * searchType : 1表示发件 2表示收件
     * @param searchType
     * @param timeType
     * @return
     */
    @Override
    public List<KeyValueQueryVO> fileSendAndReceiveNumTop10(String searchType,String timeType) {
        String sql="";
        if("1".equals(searchType)){
            sql="select * from(select org_name,sum(send_num) as totalNum from zky_send where "+ SituationLargeScreenUtil.typeSql(timeType,"start_time")+"  and send_scope='全院' and send_type='发件'  group by org_name)a order by totalNum desc limit 10";
        }else{
            sql="select * from(select org_name,sum(receive_num) as totalNum from zky_send where "+ SituationLargeScreenUtil.typeSql(timeType,"start_time")+" and  send_scope='全院' and send_type='收件' group by org_name)a order by totalNum desc limit  10";
        }
        List<KeyValueQueryVO> details = jdbcTemplate.query(sql,new KeyValueQueryVoMapper());
        return details;
    }

    public class KeyValueQueryVoMapper implements RowMapper<KeyValueQueryVO> {
        @Override
        public KeyValueQueryVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            KeyValueQueryVO data = new KeyValueQueryVO();
            data.setKey(rs.getString("org_name"));
            data.setValue(rs.getString("totalNum"));
            return data;
        }
    }

    /**
     * 发件和收件情况统计
     * filterType: 1: 本地收件 2：本地发件 3：跨地区收件 4：跨地区发件
     * @param xStrength
     * @param filterType
     * @return
     */
    public List<TreandVO> getFileSendAndReceiveTreandStatistics(String xStrength, String filterType, String type) {
        String sql ="";
        switch (xStrength) {
            case "0":  // 按天统计
                sql = "select date_format(start_time,'%Y-%m-%d') as dataX ,"+getFileSendAndReceiveTreandFilterSql(filterType,type)+"group by date_format(start_time,'%Y-%m-%d')";
                break;
            case "1":  // 按月统计
                sql = "select date_format(start_time,'%Y-%m') as dataX ,"+getFileSendAndReceiveTreandFilterSql(filterType,type)+"group by date_format(start_time,'%Y-%m')";
                break;
            case "2": // 按年统计
                sql = "select date_format(start_time,'%Y') as dataX ,"+getFileSendAndReceiveTreandFilterSql(filterType,type)+"group by date_format(start_time,'%Y')";
                break;
        }
        logger.debug("异常趋势统计查询sql:"+sql);
        List<TreandVO> details = jdbcTemplate.query(sql,new TreandVOVoMapper());
        return details;
    }
    public class TreandVOVoMapper implements RowMapper<TreandVO> {
        @Override
        public TreandVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            TreandVO data = new TreandVO();
            data.setDataX(rs.getString("dataX"));
            data.setDataY(rs.getInt("dataY"));
            return data;
        }
    }

    /**
     *
     * @param filterType
     * @return
     */
    private String getFileSendAndReceiveTreandFilterSql(String filterType,String type) {
        String sql="";
        switch (filterType){
            case "1":  //本地收件
                sql= "sum(receive_num) as dataY from zky_send  where send_region='0' and send_type='收件' "+CommonSql(type,"start_time");
                break;
            case "2":  //本地发件
                sql= "sum(send_num) as dataY from zky_send  where send_region='0' and send_type='发件' "+CommonSql(type,"start_time");
                break;
            case "3":  //跨地区收件
                sql= "sum(receive_num) as dataY from zky_send  where send_region='1' and send_type='收件' "+CommonSql(type,"start_time");
                break;
            case "4":  //跨地区发件
                sql= "sum(send_num) as dataY from zky_send  where send_region='1' and send_type='发件' "+CommonSql(type,"start_time");
                break;
        }
       return sql;

    }

    private String CommonSql(String type,String time){
        String sql ="";
        switch (type) {
            case "month":
                sql =" and DATE_SUB(CURDATE(), INTERVAL 1 MONTH) <= date_format("+time+",'%Y-%m-%d') and date_format("+time+",'%Y-%m-%d')<= date_format(CURDATE(),'%Y-%m-%d') ";
                break;
            case "halfyear":
                sql ="and DATE_SUB(CURDATE(), INTERVAL 6 MONTH) <= date_format("+time+",'%Y-%m-%d') and date_format("+time+",'%Y-%m-%d')<= date_format(CURDATE(),'%Y-%m-%d')";
                break;
            case "year":
                sql ="and DATE_SUB(CURDATE(), INTERVAL 1 YEAR) <= date_format("+time+",'%Y-%m-%d') and date_format("+time+",'%Y-%m-%d')<= date_format(CURDATE(),'%Y-%m-%d')";
                break;
            case "all":
                break;
        }
        return sql;
    }
    /**
     * 获取zky_send表中最大、最小时间
     * @return
     */
    @Override
    public Map<String, Object> getMaxAndMinStartTime() {
        // 判断有没有数据
        String sql="select count(*) as num from zky_send";
        Map<String,Object> count = jdbcTemplate.queryForMap(sql);
        int countNum = count.get("num")==null?0:Integer.parseInt(String.valueOf( count.get("num")));
        if(countNum == 0){
            return null;
        }
        sql="select date_format(min(start_time),'%Y-%m-%d %H:%i:%S') as minTime,date_format(max(start_time),'%Y-%m-%d %H:%i:%S') as maxTime from zky_send";
        return jdbcTemplate.queryForMap(sql);
    }

    /**
     * 院机关各部门邮件收发数量
     * @param s
     * @param type
     * @return
     */
    @Override
    public List<KeyValueQueryVO> emailSendAndReceiveNum(String s, String type) {
        String sql="";
        if("1".equals(s)){
            sql="select * from (select org_name as orgName,sum(receive_num) as totalNum from zky_email where 1=1 "+CommonSql(type,"email_time")+"group by org_name)a order by totalNum desc";
        }else{
            sql="select * from (select org_name as orgName,sum(send_num) as totalNum from zky_email where 1=1 "+CommonSql(type,"email_time")+"group by org_name)a order by totalNum desc";
        }
        List<KeyValueQueryVO> details = jdbcTemplate.query(sql,new KeyValueQueryVOMapper());
        return details;
    }



    public class KeyValueQueryVOMapper implements RowMapper<KeyValueQueryVO> {
        @Override
        public KeyValueQueryVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            KeyValueQueryVO data = new KeyValueQueryVO();
            data.setKey(rs.getString("orgName"));
            data.setValue(rs.getString("totalNum"));
            return data;
        }
    }

    @Override
    public Map<String, Object> emailSendAndReceiveTotal(String type) {
        String sql ="select sum(receive_num) as receiveNum,sum(send_num) as sendNum from zky_email where 1=1 "+CommonSql(type,"email_time");
        return jdbcTemplate.queryForMap(sql);

    }
}
