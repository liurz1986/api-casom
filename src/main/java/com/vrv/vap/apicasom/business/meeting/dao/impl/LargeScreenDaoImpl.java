package com.vrv.vap.apicasom.business.meeting.dao.impl;
import com.vrv.vap.apicasom.business.meeting.dao.LargeScreenDao;
import com.vrv.vap.apicasom.business.meeting.util.MettingCommonUtil;
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
import java.util.List;
import java.util.Map;

/**
 * 大屏
 *
 * @author liurz
 */
@Repository
public class LargeScreenDaoImpl implements LargeScreenDao {
    private static Logger logger = LoggerFactory.getLogger(LargeScreenDaoImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // **************视频会议相关****************************//
    /**
     * 当前节点在线总数
     * @return
     */
    @Override
    public int getOnLineNodes() {
        String sql="select sum(participant_count) as number from hw_meeting_info where stage='ONLINE'";
        logger.debug("当前节点在线总数查询sql:"+sql);
        Map<String, Object> runNodes = jdbcTemplate.queryForMap(sql);
        if (null == runNodes || runNodes.size() == 0) {
            return 0;
        }
        return runNodes.get("number")==null?0:Integer.parseInt(String.valueOf(runNodes.get("number")));
    }



    /**
     * 举办会议次数 :状态为offline总数
     * type:quarter(季)，halfyear(半年)、year(一年)
     * @return
     */
    @Override
    public int getOffLineMettingTotal(String type) {
        String sql = "select count(*) as number from hw_meeting_info where "+MettingCommonUtil.largeScreenVideoAndNodeSql(type,"schedule_start_time")+" and stage='OFFLINE'";
        logger.debug("举办会议次数 :状态为offline总数查询sql:"+sql);
        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        if (null == result || result.size() == 0) {
            return 0;
        }
        return result.get("number")==null?0:Integer.parseInt(String.valueOf(result.get("number")));
    }

    /**
     * 参会总人数:状态为OFFLINE
     * type:quarter(季)，halfyear(半年)、year(一年)
     * @return
     */
    @Override
    public int getOfflineMettingUserCount(String type) {
        String sql = "select sum(attendee_count) as number from hw_meeting_info where "+MettingCommonUtil.largeScreenVideoAndNodeSql(type,"schedule_start_time")+" and stage='OFFLINE'";
        logger.debug("参会总人数:状态为OFFLINE查询sql:"+sql);
        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        if (null == result || result.size() == 0) {
            return 0;
        }
        return result.get("number")==null?0:Integer.parseInt(String.valueOf(result.get("number")));
    }

    /**
     * 会议总时长: 状态为OFFLINE
     * type:quarter(季)，halfyear(半年)、year(一年)
     * @param type
     * @return
     */
    @Override
    public int getOfflineMeetingTimeTotal(String type) {
        String sql = "select sum(duration) as number from hw_meeting_info where "+MettingCommonUtil.largeScreenVideoAndNodeSql(type,"schedule_start_time")+" and stage='OFFLINE'";
        logger.debug("会议总时长: 状态为OFFLINE查询sql:"+sql);
        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        if (null == result || result.size() == 0) {
            return 0;
        }
        long durations = result.get("number")==null?0:Long.parseLong(String.valueOf(result.get("number")));
        // 换算成小时：取整，四舍五入
        return MettingCommonUtil.divideUP(durations,60,0).intValue();
    }
    /**
     * 点对点会议次数: 会议记录表中参会节点数小于等于2,历史会议状态为OFFLINE
     * @param type
     * @return
     */
    @Override
    public int getPointToPoint(String type) {
        String sql = "select count(*) as number from hw_meeting_info where "+MettingCommonUtil.largeScreenVideoAndNodeSql(type,"schedule_start_time")+" and participant_count<= 2 and stage='OFFLINE'";
        logger.debug("点对点会议次数查询sql:"+sql);
        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        if (null == result || result.size() == 0) {
            return 0;
        }
        int number = result.get("number")==null?0:Integer.parseInt(String.valueOf(result.get("number")));
        return number;
    }

    /**
     * 多点会议次数: 会议记录表中参会节点数大于2,历史会议状态为OFFLINE
     * @param type
     * @return
     */
    @Override
    public int getManyPoint(String type) {
        String sql = "select count(*) as number from hw_meeting_info where "+MettingCommonUtil.largeScreenVideoAndNodeSql(type,"schedule_start_time")+" and participant_count > 2 and stage='OFFLINE'";
        logger.debug("多点会议次数查询sql:"+sql);
        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        if (null == result || result.size() == 0) {
            return 0;
        }
        int number = result.get("number")==null?0:Integer.parseInt(String.valueOf(result.get("number")));
        return number;
    }

    // **************接入节点相关****************************//

    /**
     * 按城市、组织机构、节点分组
     * @return
     */
    @Override
    public List<CommonQueryVO> queryNodesGroupByCity() {
        String sql ="select a.city as keyName,a.organization_name as value1,base.participant_name as value2  from(select city ,organization_name,participant_code from hw_meeting_participant  GROUP BY city,organization_name,participant_code) a " +
                "left join  zky_unit as base on a.participant_code=base.participant_code ";
        logger.debug("按城市、组织机构、节点code分组查询sql:"+sql);
        List<CommonQueryVO> details = jdbcTemplate.query(sql,new CommonQueryVoMapper());
        return details;
    }
    public class CommonQueryVoMapper implements RowMapper<CommonQueryVO> {
        @Override
        public CommonQueryVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            CommonQueryVO data = new CommonQueryVO();
            data.setKey(rs.getString("keyName"));
            data.setValue(rs.getString("value1"));
            data.setExtend(rs.getString("value2"));
            return data;
        }
    }
    /**
     * 获取正在开会的城市
     * @return
     */
    @Override
    public List<String> getRunMettingCitys() {
        String sql="select DISTINCT city from hw_meeting_participant  where  stage='ONLINE' ";
        logger.debug("获取正在开会的城市查询sql:"+sql);
        List<String> list = jdbcTemplate.queryForList(sql,String.class);
        return list;
    }

    /**
     * 城市所有节点名称
     * @param cityName
     * @return
     */
    @Override
    public List<KeyValueQueryVO> queryNodeNamesByCity(String cityName) {
        List<Object> params = new ArrayList<>();
        String sql ="select node.organization_name as keyName, base.participant_name as value1 from hw_meeting_participant as node " +
                "inner join zky_unit as base on node.participant_code=base.participant_code where node.city=?  GROUP BY keyName,value1";
        logger.debug("城市所有节点名称查询sql:"+sql);
        params.add(cityName);
        List<KeyValueQueryVO> details = jdbcTemplate.query(sql,new KeyValueQueryVoMapper(),params.toArray());
        return details;
    }
    public class KeyValueQueryVoMapper implements RowMapper<KeyValueQueryVO> {
        @Override
        public KeyValueQueryVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            KeyValueQueryVO data = new KeyValueQueryVO();
            data.setKey(rs.getString("keyName"));
            data.setValue(rs.getString("value1"));
            return data;
        }
    }



    /**
     * 当前城市正在开会的节点信息
     * @param cityName
     * @return
     */
    @Override
    public List<NodeVO> queryRunNodesByCity(String cityName) {
        List<Object> params = new ArrayList<>();
        String sql ="select base.participant_name as name ,node.organization_name,node.schedule_start_time,node.schedule_end_time,node.stage from hw_meeting_participant as node " +
                "left join zky_unit as base on base.participant_code=node.participant_code where node.stage='ONLINE' and node.city=? ";
        logger.debug("当前城市正在开会的节点信息查询sql:"+sql);
        params.add(cityName);
        List<NodeVO> details = jdbcTemplate.query(sql,new NodeVoMapper(),params.toArray());
        return details;
    }
    public class NodeVoMapper implements RowMapper<NodeVO> {
        @Override
        public NodeVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            NodeVO data = new NodeVO();
            data.setName(rs.getString("name"));
            data.setOrganizationName(rs.getString("organization_name"));
            data.setStartTime(rs.getTimestamp("schedule_start_time"));
            data.setEndTime(rs.getTimestamp("schedule_end_time"));
            data.setStage(rs.getString("stage"));
            return data;
        }
    }

    /**
     * 各地区系统使用统计
     * type:quarter(季)，halfyear(半年)、year(一年)
     * 1. 节点接入表和节点人数统计表 关联查询：hw_meeting_attendee、hw_meeting_participant
     * 2. 节点接入表状态为离线：OFFLINE
     * @return Result
     */
    @Override
    public List<LargeBranchStatisticsVO> queryBranchStatistics(String type) {
        String sql = "select a.branch,sum(a.user_count) as userCont,sum(a.duration) as durationTotal,count(a.name) as meetingCount from( " +
                " select node.name,node.branch,detail.user_count,detail.duration from hw_meeting_participant as node " +
                " left join " +
                " hw_meeting_attendee as detail on node.participant_code=detail.participant_code and node.meeting_id=detail.meeting_id " +
                " where node.stage='OFFLINE' and " + MettingCommonUtil.largeScreenVideoAndNodeSql(type,"node.schedule_start_time")+" )a group by a.branch ";
        logger.debug("各地区系统使用统计查询sql:"+sql);
        List<LargeBranchStatisticsVO> details = jdbcTemplate.query(sql,new BranchStatisticsVoMapper());
        return details;
    }


    public class BranchStatisticsVoMapper implements RowMapper<LargeBranchStatisticsVO> {
        @Override
        public LargeBranchStatisticsVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            LargeBranchStatisticsVO data = new LargeBranchStatisticsVO();
            data.setName(rs.getString("branch"));
            data.setUserNum(rs.getInt("userCont"));
            long meetingDur =rs.getLong("durationTotal");
            data.setMeetingDur(MettingCommonUtil.transferHourAndMinutes(meetingDur));
            data.setMeetingTimes(rs.getInt("meetingCount"));
            return data;
        }
    }

    /**
     *  各地区使用占比,历史数据
     *  1. 按分院统计开会的次数
     *  2. 会议为历史会议
     *  3. 查询开会次数前5天记录(开始次数降序)
     * @param type
     * @return
     */
    @Override
    public List<LargeDeatailVO> getUseStatisticsByBranch(String type) {
        String sql ="select * from(select branch as name,count(*)as num from hw_meeting_participant where stage='OFFLINE' and "+MettingCommonUtil.largeScreenVideoAndNodeSql(type,"schedule_start_time")+" group by branch)a order by a.num desc limit 0,5 ";
        logger.debug("各地区使用占比,历史数据查询sql:"+sql);
        List<LargeDeatailVO> details = jdbcTemplate.query(sql,new LargeBranchStatisticsVoMapper());
        return details;
    }



    public class LargeBranchStatisticsVoMapper implements RowMapper<LargeDeatailVO> {
        @Override
        public LargeDeatailVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            LargeDeatailVO data = new LargeDeatailVO();
            data.setName(rs.getString("name"));
            data.setCount(rs.getString("num")==null?0:Integer.parseInt(rs.getString("num")));
            return data;
        }
    }

    /**
     * 统计节点数：节点会议总次数
     * @param type
     * @return
     */
    @Override
    public int getUseStatisticsTotalCount(String type) {
        String sql="select count(*)as num from hw_meeting_participant where stage='OFFLINE' and  "+MettingCommonUtil.largeScreenVideoAndNodeSql(type,"schedule_start_time");
        logger.debug("节点会议总次数查询sql:"+sql);
        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        if (null == result || result.size() == 0) {
            return 0;
        }
        int number = result.get("num")==null?0:Integer.parseInt(String.valueOf(result.get("num")));
        return number;
    }

    /**
     * 开会次数
     * @param type
     * @return
     */
    @Override
    public List<LargeDeatailVO> queryNodeMeetingCountStatistics(String type) {
        String sql="select * from(select base.participant_name as name ,count(*) as num from hw_meeting_participant as node inner join zky_unit as base " +
                "on node.participant_code=base.participant_code " +
                "where node.stage='OFFLINE' and "+MettingCommonUtil.largeScreenVideoAndNodeSql(type,"node.schedule_start_time")+" GROUP BY base.participant_name)a order by a.num desc limit 0,5";
        logger.debug("开会次数查询sql:"+sql);
        List<LargeDeatailVO> details = jdbcTemplate.query(sql,new LargeBranchStatisticsVoMapper());
        return details;
    }

    /**
     * 对外提供服务
     * @param type
     * @return
     */
    @Override
    public List<LargeDeatailVO> queryOutServiceStatistics(String type) {
        String sql="select * from(select base.participant_name as name ,count(*) as num from hw_meeting_participant as node inner join zky_unit as base " +
                "on node.participant_code=base.participant_code " +
                "where node.stage='OFFLINE' and out_service='1' and "+MettingCommonUtil.largeScreenVideoAndNodeSql(type,"node.schedule_start_time")+" GROUP BY base.participant_name)a order by a.num desc limit 0,5";
        logger.debug("对外提供服务查询sql:"+sql);
        List<LargeDeatailVO> details = jdbcTemplate.query(sql,new LargeBranchStatisticsVoMapper());
        return details;
    }

    // **************异常会议相关****************************//

    /**
     * 获取存在异常会议的城市：告警状态为current,进行中的告警
     * @return
     */
    @Override
    public List<CommonQueryVO> getAbnormalMettingCitys() {
        String sql ="select a.city as keyName,a.organization_name as value1,base.participant_name as value2 from(" +
                "select city,organization_name ,participant_code  from hw_meeting_participant  where meeting_id in (select meeting_id from hw_meeting_alarm where alarm_status='current' ) " +
                "group by city,organization_name,participant_code)a left join zky_unit as base on base.participant_code=a.participant_code" ;
        logger.debug("获取存在异常会议的城市、组织机构、节点code分组查询sql:"+sql);
        List<CommonQueryVO> details = jdbcTemplate.query(sql,new CommonQueryVoMapper());
        return details;
    }
    /**
     * 异常名称分组，次数前五的数据，历史告警
     * @param type
     * @return
     */
    @Override
    public List<LargeDeatailVO> getStatisticsByName(String type) {
        String sql ="select * from(select name,count(*) as num from hw_meeting_alarm where alarm_status='history' "+abnormallargeScreenCommonSql(type)+" group by name)a order by a.num desc limit 0,5 ";
        List<LargeDeatailVO> details = jdbcTemplate.query(sql,new LargeBranchStatisticsVoMapper());
        logger.debug("异常名称分组，次数前五的数据，历史告警查询sql:"+sql);
        return details;
    }


    /**
     * 获取历史异常总数
     * @param type
     * @return
     */
    @Override
    public int getHistoryTotalCount(String type) {
        String sql = "select count(*)as num from hw_meeting_alarm where alarm_status='history'" + abnormallargeScreenCommonSql(type);
        logger.debug("获取历史异常总数查询sql:"+sql);
        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        if (null == result || result.size() == 0) {
            return 0;
        }
        int number = result.get("num") == null ? 0 : Integer.parseInt(String.valueOf(result.get("num")));
        return number;
    }


    private String abnormallargeScreenCommonSql(String type){
        String sql ="";
        switch (type) {
            case "quarter":
                sql =" and DATE_SUB(CURDATE(), INTERVAL 3 MONTH) <= date_format(alarm_time,'%Y-%m-%d') and date_format(alarm_time,'%Y-%m-%d')<= date_format(CURDATE(),'%Y-%m-%d') ";
                break;
            case "halfyear":
                sql ="and DATE_SUB(CURDATE(), INTERVAL 6 MONTH) <= date_format(alarm_time,'%Y-%m-%d') and date_format(alarm_time,'%Y-%m-%d')<= date_format(CURDATE(),'%Y-%m-%d')";
                break;
            case "year":
                sql ="and DATE_SUB(CURDATE(), INTERVAL 1 YEAR) <= date_format(alarm_time,'%Y-%m-%d') and date_format(alarm_time,'%Y-%m-%d')<= date_format(CURDATE(),'%Y-%m-%d')";
                break;
            default:
                break;
        }
        return sql;
    }

}
