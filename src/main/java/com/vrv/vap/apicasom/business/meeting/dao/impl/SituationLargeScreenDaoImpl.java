package com.vrv.vap.apicasom.business.meeting.dao.impl;

import com.vrv.vap.apicasom.business.meeting.bean.ZkyExchangeBox;
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
     *  send_scope为全院
     * searchType : 1表示发件 2表示收件
     * @param searchType
     * @param timeType
     * @return
     */
    @Override
    public List<KeyValueQueryVO> fileSendAndReceiveNumTop10(String searchType,String timeType) {
        String sql="";
        if("1".equals(searchType)){
            sql="select * from(select org_name as orgName,sum(send_num) as totalNum from zky_send where "+ SituationLargeScreenUtil.typeSql(timeType,"start_time")+"  and send_scope='全院' and send_type='发件'  group by org_name)a order by totalNum desc limit 10";
        }else{
            sql="select * from(select  org_name as orgName,sum(receive_num) as totalNum from zky_send where "+ SituationLargeScreenUtil.typeSql(timeType,"start_time")+" and  send_scope='全院' and send_type='收件' group by org_name)a order by totalNum desc limit  10";
        }
        List<KeyValueQueryVO> details = jdbcTemplate.query(sql,new KeyValueQueryVoMapper());
        return details;
    }

    public class KeyValueQueryVoMapper implements RowMapper<KeyValueQueryVO> {
        @Override
        public KeyValueQueryVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            KeyValueQueryVO data = new KeyValueQueryVO();
            data.setKey(rs.getString("orgName"));
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
        List<KeyValueQueryVO> details = jdbcTemplate.query(sql,new KeyValueQueryVoMapper());
        return details;
    }

    @Override
    public Map<String, Object> emailSendAndReceiveTotal(String type) {
        String sql ="select sum(receive_num) as receiveNum,sum(send_num) as sendNum from zky_email where 1=1 "+CommonSql(type,"email_time");
        return jdbcTemplate.queryForMap(sql);

    }

    /**
     * 收发件数量： 院部机关下org_name、send_region、send_type分组统计
     * @param type
     * @return
     */
    @Override
    public List<Map<String, Object>> fileSendAndReceiveOrgName(String type) {
        String sql ="select org_name as orgName,send_region as sendRegion,send_type as sendType,sum(receive_num) as receiveNum ,sum(send_num) as sendNum from zky_send where send_scope='院部机关' "+CommonSql(type,"start_time") +"group by org_name,send_region,send_type";
        return jdbcTemplate.queryForList(sql);

    }

    /**
     * 收发件数量：按分院统计
     *          send_scope为'全院'
     * @param type
     * @return
     */
    @Override
    public List<Map<String, Object>> fileSendAndReceiveBranch(String type) {
        String sql ="select zky_unit.branch ,zky_send.send_region as sendRegion,zky_send.send_type as sendType,sum(zky_send.receive_num) as receiveNum ,sum(zky_send.send_num) as sendNum from zky_send  " +
                "inner join zky_unit on zky_send.org_name=zky_unit.name " +
                "where zky_send.send_scope='全院'"+CommonSql(type,"zky_send.start_time") +" group by zky_unit.branch,zky_send.send_region,zky_send.send_type";
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * send_region,send_type分组统计
     *
     * @param type
     * @return
     */
    @Override
    public List<Map<String, Object>> getGroupBySendRegionAndSendType(String type) {
        String sql ="select send_region as sendRegion,send_type as sendType,sum(receive_num) as receiveNum ,sum(send_num) as sendNum from zky_send where 1=1 "+CommonSql(type,"start_time") +"group by send_region,send_type";
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * 获取地图中城市
     * send_scope为全院城市统计
     * @param type
     * @return
     */
    @Override
    public List<String> branchMap(String type) {
        String sql="select DISTINCT city  from zky_send as send inner join zky_unit as unit on send.org_name=unit.name where send_scope='全院' "+CommonSql(type,"send.start_time") ;
        return jdbcTemplate.queryForList(sql,String.class);
    }

    /**
     * 地图详情
     * @param city
     * @param type
     * @return
     */
    @Override
    public List<MapDetailQueryVO> getGroupDeatailByCity(String city, String type) {
        List<String> params = new ArrayList<>();
        String sql ="select org_name as orgName,send_region as sendRegion,send_type as sendType,sum(receive_num) as receiveNum ,sum(send_num) as sendNum from(" +
                "select send.*  from zky_send as send inner join zky_unit as unit on send.org_name=unit.name where unit.city=? and send.send_scope='全院' "+CommonSql(type,"send.start_time")+")a group by org_name,send_region,send_type";
        params.add(city);
        return jdbcTemplate.query(sql,new MapDetailQueryVOMapper(),params.toArray());
    }


    public class MapDetailQueryVOMapper implements RowMapper<MapDetailQueryVO> {
        @Override
        public MapDetailQueryVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            MapDetailQueryVO data = new MapDetailQueryVO();
            data.setOrgName(rs.getString("orgName"));
            data.setSendRegion(rs.getString("sendRegion"));
            data.setSendType(rs.getString("sendType"));
            data.setReceiveNum(rs.getInt("receiveNum"));
            data.setSendNum(rs.getInt("sendNum"));
            return data;
        }
    }

    /**
     * 截至时间为最近的一次导入数据记录
     *
     * @return
     */
    @Override
    public ZkyExchangeBox getMaxDeadlineData() {
        String sql="select * from zky_exchange_box where deadline in(select max(deadline) from zky_exchange_box)";
        List<ZkyExchangeBox> list = jdbcTemplate.query(sql,new ZkyExchangeBoxVOMapper());
        if(null != list && list.size() > 0){
            return list.get(0);
        }
        return new ZkyExchangeBox();
    }

    @Override
    public List<ZkyExchangeBox> getMaxDeadlineZkyExchangeBox(String type, Date startTime, Date endTime) {
        List<Object> params = new ArrayList<>();
        String sql="select * from zky_exchange_box where deadline in(select max(deadline) from zky_exchange_box where date_format(deadline,'%Y-%m')>=date_format(?,'%Y-%m') and date_format(deadline,'%Y-%m')<=date_format(?,'%Y-%m')  group by date_format(deadline,'%Y-%m'))";
        params.add(startTime);
        params.add(endTime);
        return jdbcTemplate.query(sql,new ZkyExchangeBoxVOMapper(),params.toArray());
    }

    public class ZkyExchangeBoxVOMapper implements RowMapper<ZkyExchangeBox> {
        @Override
        public ZkyExchangeBox mapRow(ResultSet rs, int rowNum) throws SQLException {
            ZkyExchangeBox data = new ZkyExchangeBox();
            data.setGuid(rs.getString("guid"));
            data.setDeadline(rs.getTimestamp("deadline"));
            data.setUserTotal(rs.getInt("user_total"));
            data.setUserLoginCount(rs.getInt("user_login_count"));
            data.setSecrecyTotal(rs.getInt("secrecy_total"));
            data.setSecrecyRegisterTotal(rs.getInt("secrecy_register_total"));
            data.setSecrecyRoamTotal(rs.getInt("secrecy_roam_total"));
            data.setReceiveTotal(rs.getInt("receive_total"));
            data.setReceiveRegisterTotal(rs.getInt("receive_register_total"));
            data.setReceiveRoamTotal(rs.getInt("receive_roam_total"));
            data.setSignTotal(rs.getInt("sign_total"));
            data.setSignRegisterTotal(rs.getInt("sign_register_total"));
            data.setSignRoamTotal(rs.getInt("sign_roam_total"));
            return data;
        }
    }
}
