package com.vrv.vap.apicasom.business.meeting.dao.impl;

import com.vrv.vap.apicasom.business.meeting.dao.VideoMettingDao;
import com.vrv.vap.apicasom.business.meeting.util.MettingCommonUtil;
import com.vrv.vap.apicasom.business.meeting.vo.*;
import com.vrv.vap.jpa.common.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
public class VideoMettingDaoImpl implements VideoMettingDao {
    private static Logger logger = LoggerFactory.getLogger(VideoMettingDaoImpl.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;
    /**
     * 会议时长分布统计:duration
     * @return
     */
    @Override
    public List<DistributionStatisticsVO> queyMeetingDurationStatistics(StatisticSearchVO statisticSearchVO) {
        String filterSql =  getFilterSql(statisticSearchVO);
        filterSql ="select * from hw_meeting_info where "+ filterSql+" and stage='OFFLINE'";;
        StringBuffer sql = new StringBuffer();
        sql.append("select '0-60分钟' as name,count(*) as num  from (").append(filterSql).append(")as a where a.duration BETWEEN 0 and 60");
        sql.append(" union ");
        sql.append("select '60-90分钟' as name,count(*) as num  from (").append(filterSql).append(")as a where a.duration >60 and a.duration<=90");
        sql.append(" union ");
        sql.append("select '90-120分钟' as name,count(*) as num  from (").append(filterSql).append(")as a where a.duration >90 and a.duration<=120");
        sql.append(" union ");
        sql.append("select '120-150分钟' as name,count(*) as num  from (").append(filterSql).append(")as a where a.duration >120 and a.duration<=150");
        sql.append(" union ");
        sql.append("select '150-180分钟' as name,count(*) as num  from (").append(filterSql).append(")as a where a.duration >150 and a.duration<=180");
        sql.append(" union ");
        sql.append("select '大于180分钟' as name,count(*) as num  from (").append(filterSql).append(")as a where  a.duration >180");
        logger.debug("会议时长分布统计查询sql:"+sql.toString());
        List<DistributionStatisticsVO> details = jdbcTemplate.query(sql.toString(),new DistributionStatisticsVOMapper());
        return details;
    }

    /**
     * 参会人数分布统计:
     * @param statisticSearchVO
     * @return
     */
    @Override
    public List<DistributionStatisticsVO> queyParticipantsStatistics(StatisticSearchVO statisticSearchVO) {
        String baseSql =  getFilterSql(statisticSearchVO);
        baseSql ="select * from hw_meeting_info where "+ baseSql + " and stage='OFFLINE'";
        StringBuffer sql = new StringBuffer();
        sql.append("select '2-5人' as name,count(*) as num  from (").append(baseSql).append(")as a where a.attendee_count BETWEEN 2 and 5");
        sql.append(" union ");
        sql.append("select '6-10人' as name,count(*) as num  from (").append(baseSql).append(")as a where a.attendee_count BETWEEN 6 and 10");
        sql.append(" union ");
        sql.append("select '11-20人' as name,count(*) as num  from (").append(baseSql).append(")as a where a.attendee_count BETWEEN 11 and 20");
        sql.append(" union ");
        sql.append("select '21-30人' as name,count(*) as num  from (").append(baseSql).append(")as a where a.attendee_count BETWEEN 21 and 30");
        sql.append(" union ");
        sql.append("select '31-40人' as name,count(*) as num  from (").append(baseSql).append(")as a where a.attendee_count BETWEEN 31 and 40");
        sql.append(" union ");
        sql.append("select '大于40人' as name,count(*) as num  from (").append(baseSql).append(")as a where  a.duration >40");
        logger.debug("会议时长分布统计查询sql:"+sql.toString());
        List<DistributionStatisticsVO> details = jdbcTemplate.query(sql.toString(),new DistributionStatisticsVOMapper());
        return details;
    }


    /**
     * 统计时段处理
     * month：近一月
     * halfyear：近半年
     * year：近一年
     * none： 手动输入开始日期和结束日期
     * @param statisticSearchVO
     * @return
     */
    private  String getFilterSql(StatisticSearchVO statisticSearchVO) {
        String type = statisticSearchVO.getType();
        String filterSql ="";
        switch (type) {
            case "month":
                filterSql ="DATE_SUB(CURDATE(), INTERVAL 1 MONTH) <= date(schedule_start_time)";
                break;
            case "halfyear":
                filterSql ="DATE_SUB(CURDATE(), INTERVAL 6 MONTH) <= date(schedule_start_time)";
                break;
            case "year":
                filterSql ="DATE_SUB(CURDATE(), INTERVAL 1 YEAR) <= date(schedule_start_time)";
                break;
            case "none":
                filterSql="date_format(schedule_start_time,'%Y-%m-%d') >= '"+ DateUtil.format(statisticSearchVO.getStartDate(),"yyyy-MM-dd") +"' and date_format(schedule_start_time,'%Y-%m-%d')  <='"+DateUtil.format(statisticSearchVO.getEndDate(),"yyyy-MM-dd")+"'";
                break;
            default:
                break;
        }
        return filterSql;
    }
    public class DistributionStatisticsVOMapper implements RowMapper<DistributionStatisticsVO> {
        @Override
        public DistributionStatisticsVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            DistributionStatisticsVO detail = new DistributionStatisticsVO();
            detail.setName(rs.getString("name") );
            detail.setNum(rs.getString("num")==null?0:Integer.parseInt(rs.getString("num")));
            return detail;
        }
    }

    /**
     * 分页查询获取总数
     * @param videoMettingSearchVO
     * @return
     */
    @Override
    public long getPageTotal(VideoMettingSearchVO videoMettingSearchVO) {
        String sql ="select count(*) as number  from ("+getPageSql(videoMettingSearchVO)+") as a";
        logger.debug("分页查询获取总数查询sql:"+sql);
        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        if (null == result || result.size() == 0) {
            return 0;
        }
        return result.get("number")==null?0:Long.parseLong(String.valueOf(result.get("number")));
    }

    /**
     * 分页查询获取数据
     * @param videoMettingSearchVO
     * @return
     */
    @Override
    public List<VideoMettingVO> getPageList(VideoMettingSearchVO videoMettingSearchVO) {
        int start =  videoMettingSearchVO.getStart_();
        int end = videoMettingSearchVO.getStart_() * videoMettingSearchVO.getCount_() + videoMettingSearchVO.getCount_();
        String sql= getPageSql(videoMettingSearchVO) +  " limit "+start+","+end;
        logger.debug("分页查询获取数据查询sql:"+sql);
        List<VideoMettingVO> details = jdbcTemplate.query(sql,new VideoMettingVOMapper());
        return details;
    }

    public class VideoMettingVOMapper implements RowMapper<VideoMettingVO> {
        @Override
        public VideoMettingVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            VideoMettingVO detail = new VideoMettingVO();
            detail.setMeetingDate(rs.getDate("meetingDate"));
            detail.setMeetingTime(rs.getString("meetingTime"));
            detail.setNodeNames(rs.getString("nodeNames"));
            detail.setCompanys(rs.getString("companys"));
            detail.setPeopleNumber(rs.getString("peopleNumber"));
            detail.setNodeNumber(rs.getString("nodeNumber"));
            return detail;
        }
    }

    private String getPageSql(VideoMettingSearchVO videoMettingSearchVO){
        String sql="select DATE_FORMAT(schedule_start_time,'%Y-%m-%d') as meetingDate,CONCAT(DATE_FORMAT(schedule_start_time,'%H:%i'),'-'," +
                " DATE_FORMAT(schedule_end_time,'%H:%i')) as meetingTime,participant_unity as nodeNames,organization_name as companys,attendee_count as peopleNumber , participant_count as nodeNumber" +
                " from hw_meeting_info where  1 = 1 ";
        // 参会节点
        if(StringUtils.isNotEmpty(videoMettingSearchVO.getNodeName())){
            sql = sql+" and participant_unity like '%" +videoMettingSearchVO.getNodeName()+"%'";
        }
        // 时间范围
        if(null != videoMettingSearchVO.getStartTime() && null != videoMettingSearchVO.getEndTime()){
            sql = sql+" and date_format(schedule_start_time,'%Y-%m-%d') BETWEEN '" +  DateUtil.format(videoMettingSearchVO.getStartTime(),"yyyy-MM-dd") + "' and '"+ DateUtil.format( videoMettingSearchVO.getEndTime(),"yyyy-MM-dd")+"'";
        }
        if(null != videoMettingSearchVO.getStartTime() && null == videoMettingSearchVO.getEndTime()){
            sql = sql+" and date_format(schedule_start_time,'%Y-%m-%d') >= '" +  DateUtil.format(videoMettingSearchVO.getStartTime(),"yyyy-MM-dd") +"'";
        }
        if(null == videoMettingSearchVO.getStartTime() && null != videoMettingSearchVO.getEndTime()){
            sql = sql+" and date_format(schedule_start_time,'%Y-%m-%d') <= '" +  DateUtil.format(videoMettingSearchVO.getEndTime(),"yyyy-MM-dd") +"'";
        }
        sql = sql+" and stage='OFFLINE' order by schedule_start_time desc" ;
        return sql;
    }

    /**
     * 获取导出数据
     * @param videoMettingSearchVO
     * @return
     */
    @Override
    public List<VideoMettingExportExcelVO> exportData(VideoMettingSearchVO videoMettingSearchVO) {
        String sql= getPageSql(videoMettingSearchVO);
        logger.debug("获取导出数据查询sql:"+sql);
        List<VideoMettingExportExcelVO> details = jdbcTemplate.query(sql,new VVideoMettingExportExcelVOMapper());
        return details;
    }

    public class VVideoMettingExportExcelVOMapper implements RowMapper<VideoMettingExportExcelVO> {
        @Override
        public VideoMettingExportExcelVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            VideoMettingExportExcelVO excelVO = new VideoMettingExportExcelVO();
            excelVO.setMeetingTime(rs.getString("meetingTime"));
            excelVO.setNodeNames(rs.getString("nodeNames"));
            excelVO.setCompanys(rs.getString("companys"));
            excelVO.setPeopleNumber(rs.getString("peopleNumber"));
            excelVO.setNodeNumber(rs.getString("nodeNumber"));
            excelVO.setMeetingDate(rs.getDate("meetingDate"));
            return excelVO;
        }
    }
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
        String sql = "select count(*) as number from hw_meeting_info where "+largeScreenCommonSql(type)+" and stage='OFFLINE'";
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
        String sql = "select sum(attendee_count) as number from hw_meeting_info where "+largeScreenCommonSql(type)+" and stage='OFFLINE'";
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
        String sql = "select sum(duration) as number from hw_meeting_info where "+largeScreenCommonSql(type)+" and stage='OFFLINE'";
        logger.debug("会议总时长: 状态为OFFLINE查询sql:"+sql);
        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        if (null == result || result.size() == 0) {
            return 0;
        }
        long durations = result.get("number")==null?0:Long.parseLong(String.valueOf(result.get("number")));
        // 换算成小时：取整，四舍五入
        return MettingCommonUtil.divideUP(durations,60,0).intValue();
    }


    private String largeScreenCommonSql(String type){
        String sql ="";
        switch (type) {
            case "quarter":
                sql ="DATE_SUB(CURDATE(), INTERVAL 3 MONTH) <= date(schedule_start_time)";
                break;
            case "halfyear":
                sql ="DATE_SUB(CURDATE(), INTERVAL 6 MONTH) <= date(schedule_start_time)";
                break;
            case "year":
                sql ="DATE_SUB(CURDATE(), INTERVAL 1 YEAR) <= date(schedule_start_time)";
                break;
            default:
                break;
        }
        return sql;
    }

    /**
     * 点对点会议次数: 会议记录表中参会节点数小于等于2,历史会议状态为OFFLINE
     * @param type
     * @return
     */
    @Override
    public int getPointToPoint(String type) {
        String sql = "select count(*) as number from hw_meeting_info where "+largeScreenCommonSql(type)+" and participant_count<= 2 and stage='OFFLINE'";
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
        String sql = "select count(*) as number from hw_meeting_info where "+largeScreenCommonSql(type)+" and participant_count > 2 and stage='OFFLINE'";
        logger.debug("多点会议次数查询sql:"+sql);
        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        if (null == result || result.size() == 0) {
            return 0;
        }
        int number = result.get("number")==null?0:Integer.parseInt(String.valueOf(result.get("number")));
        return number;
    }

}
