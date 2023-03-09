package com.vrv.vap.apicasom.business.meeting.dao.impl;

import com.vrv.vap.apicasom.business.meeting.dao.IntegratedLargeScreenDao;
import com.vrv.vap.apicasom.business.meeting.util.MettingCommonUtil;
import com.vrv.vap.apicasom.business.meeting.vo.IntegratedLargeSearchVO;
import com.vrv.vap.apicasom.business.meeting.vo.KeyValueQueryVO;
import com.vrv.vap.jpa.common.DateUtil;
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
 * 综合大屏
 * @author liurz
 */
@Repository
public class IntegratedLargeScreenDaoimpl implements IntegratedLargeScreenDao {
    private static Logger logger = LoggerFactory.getLogger(IntegratedLargeScreenDaoimpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 当前开会节点数:状态为在线的会议记录数ONLINE
     * @return
     */
    @Override
    public int onLineMettingCount() {
        String sql = "select count(*) as number from hw_meeting_info where stage='ONLINE'";
        logger.debug("状态为在线的会议记录数查询sql:"+sql);
        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        if (null == result || result.size() == 0) {
            return 0;
        }
        return result.get("number")==null?0:Integer.parseInt(String.valueOf(result.get("number")));
    }

    /**
     *  参会总人数:状态为OFFLINE
     * @return
     */
    @Override
    public int getOfflineMettingUserCount(IntegratedLargeSearchVO searchVO) {
        String sql = "select sum(attendee_count) as number from hw_meeting_info where stage='OFFLINE'";
        List<Object> params = new ArrayList<>();
        sql=sql+ " and "+ filterSql(searchVO,params);
        logger.debug("参会总人数:状态为OFFLINE查询sql:"+sql);
        Map<String, Object> result = null;
        if(params.size()> 0){
            result = jdbcTemplate.queryForMap(sql,params.toArray());
        }else{
            result = jdbcTemplate.queryForMap(sql);
        }
        if (null == result || result.size() == 0) {
            return 0;
        }
        return result.get("number")==null?0:Integer.parseInt(String.valueOf(result.get("number")));
    }

    /**
     * 会议总时长: 状态为OFFLINE
     * @return
     */
    @Override
    public int getOfflineMeetingTimeTotal(IntegratedLargeSearchVO searchVO) {
        String sql = "select sum(duration) as number from hw_meeting_info where  stage='OFFLINE'";
        List<Object> params = new ArrayList<>();
        sql=sql+ " and  "+ filterSql(searchVO,params);
        logger.debug("会议总时长: 状态为OFFLINE查询sql:"+sql);
        Map<String, Object> result = null;
        if(params.size()> 0){
            result = jdbcTemplate.queryForMap(sql,params.toArray());
        }else{
            result = jdbcTemplate.queryForMap(sql);
        }
        if (null == result || result.size() == 0) {
            return 0;
        }
        long durations = result.get("number")==null?0:Long.parseLong(String.valueOf(result.get("number")));
        // 换算成小时：取整，四舍五入
        return MettingCommonUtil.divideUP(durations,60,0).intValue();
    }

    /**
     * 各地区接入节点分布
     *
     * 接入节点表状态为历史，按分院分组统计数量
     * @return
     */
    @Override
    public List<KeyValueQueryVO> queryBranchNodeStatistics() {
        String sql ="select branch as name,count(*) as value  from hw_meeting_participant where stage='OFFLINE' group by branch  ";
        logger.debug("各地区接入节点分布查询sql:"+sql);
        List<KeyValueQueryVO> details = jdbcTemplate.query(sql,new KeyValueQueryVoMapper());
        return details;
    }

    /**
     * 应用使用态势
     * @param endDate
     * @param beginDate
     * @param type
     * @return
     */
    @Override
    public List<KeyValueQueryVO> getTreandStatistics(Date endDate, Date beginDate, String type) {
        List<Object> params = new ArrayList<>();
        String sql = getQuerySql(endDate,beginDate,type,params);
        logger.debug("应用使用态势查询sql:"+sql);
        List<KeyValueQueryVO> details = jdbcTemplate.query(sql,new KeyValueQueryVoMapper(),params.toArray());
        return details;
    }


    private String getQuerySql(Date endDate, Date beginDate, String type , List<Object> params) {
        String sql="select @#@ as name ,count(*) as value  from hw_meeting_info  " +
                "where stage='OFFLINE' and date_format(schedule_start_time,'%Y-%m-%d') >= ? and date_format(schedule_start_time,'%Y-%m-%d') <=?  " +
                "group by @#@ ";
        switch (type) {
            case "1":
                sql = sql.replace("@#@","date_format(schedule_start_time,'%Y-%m-%d %H')");
                break;
            case "2":
                sql = sql.replace("@#@","date_format(schedule_start_time,'%Y-%m-%d')");
                break;
            case "3":
                sql = sql.replace("@#@","date_format(schedule_start_time,'%Y-%m')");
                break;
            default:
                break;
        }
        params.add(DateUtil.format(beginDate,"yyyy-MM-dd"));
        params.add(DateUtil.format(endDate,"yyyy-MM-dd"));
        return sql;
    }

    public class KeyValueQueryVoMapper implements RowMapper<KeyValueQueryVO> {
        @Override
        public KeyValueQueryVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            KeyValueQueryVO data = new KeyValueQueryVO();
            data.setKey(rs.getString("name"));
            data.setValue(rs.getString("value"));
            return data;
        }
    }

    /**
     * 开会次数: 状态为OFFLINE
     * @param searchVO
     * @return
     */
    @Override
    public int getOffLineMettingTotal(IntegratedLargeSearchVO searchVO) {
        String sql = "select count(*) as number from hw_meeting_info where  stage='OFFLINE'";
        List<Object> params = new ArrayList<>();
        sql=sql+ " and "+ filterSql(searchVO,params);
        logger.debug("举办会议次数 :状态为offline总数查询sql:"+sql);
        Map<String, Object> result = null;
        if(params.size()> 0){
            result = jdbcTemplate.queryForMap(sql,params.toArray());
        }else{
            result = jdbcTemplate.queryForMap(sql);
        }
        if (null == result || result.size() == 0) {
            return 0;
        }
        return result.get("number")==null?0:Integer.parseInt(String.valueOf(result.get("number")));

    }

    private String filterSql(IntegratedLargeSearchVO searchVO,List<Object> params) {
        String sql = "";
        if(null != searchVO.getBeginTime() && null != searchVO.getEndTime()){
            sql="  date_format(schedule_start_time,'%Y-%m-%d') >= ? and date_format(schedule_start_time,'%Y-%m-%d') <=? ";
            params.add(DateUtil.format(searchVO.getBeginTime(),"yyyy-MM-dd"));
            params.add(DateUtil.format(searchVO.getEndTime(),"yyyy-MM-dd"));
        }
        if(null !=  searchVO.getBeginTime()  && null == searchVO.getEndTime()){
            sql="  date_format(schedule_start_time,'%Y-%m-%d') >= ? ";
            params.add(DateUtil.format(searchVO.getBeginTime(),"yyyy-MM-dd"));
        }
        if(null ==  searchVO.getBeginTime()  && null != searchVO.getEndTime()){
            sql="  date_format(schedule_start_time,'%Y-%m-%d') <= ? ";
            params.add(DateUtil.format(searchVO.getEndTime(),"yyyy-MM-dd"));
        }
        return sql;
    }

    @Override
    public int getFiles(IntegratedLargeSearchVO searchVO, String type) {
        String sql="";
        String message="";
        if("send".equals(type)){
            sql = "select sum(send_num) as number from zky_send ";
            message ="发送文件";
        }else{
            sql = "select sum(receive_num) as number from zky_send ";
            message ="接收文件";
        }
        List<Object> params = new ArrayList<>();
        if(null != searchVO.getBeginTime() && null != searchVO.getEndTime()){
            sql=sql +" where date_format(start_time,'%Y-%m-%d') >= ? and date_format(start_time,'%Y-%m-%d') <=? ";
            params.add(DateUtil.format(searchVO.getBeginTime(),"yyyy-MM-dd"));
            params.add(DateUtil.format(searchVO.getEndTime(),"yyyy-MM-dd"));
        }
        if(null !=  searchVO.getBeginTime()  && null == searchVO.getEndTime()){
            sql=sql +" where date_format(start_time,'%Y-%m-%d') >= ? ";
            params.add(DateUtil.format(searchVO.getBeginTime(),"yyyy-MM-dd"));
        }
        if(null ==  searchVO.getBeginTime()  && null != searchVO.getEndTime()){
            sql=sql +" where date_format(start_time,'%Y-%m-%d') <= ? ";
            params.add(DateUtil.format(searchVO.getEndTime(),"yyyy-MM-dd"));
        }
        logger.debug(message+"总数查询sql:"+sql);
        Map<String, Object> result = null;
        if(params.size()> 0){
            result = jdbcTemplate.queryForMap(sql,params.toArray());
        }else{
            result = jdbcTemplate.queryForMap(sql);
        }
        if (null == result || result.size() == 0) {
            return 0;
        }
        return result.get("number")==null?0:Integer.parseInt(String.valueOf(result.get("number")));
    }
}
