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

/**
 * 视频会议
 * @author liurz
 */
@Repository
public class VideoMettingDaoImpl implements VideoMettingDao {
    private static Logger logger = LoggerFactory.getLogger(VideoMettingDaoImpl.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String meetingDate="meetingDate";
    /**
     * 会议时长分布统计:duration
     * @return
     */
    @Override
    public List<DistributionStatisticsVO> queyMeetingDurationStatistics(StatisticSearchVO statisticSearchVO) {
        String filterSql =  getFilterSql(statisticSearchVO);
        filterSql ="select * from hw_meeting_info where 1=1"+ filterSql+" and stage='OFFLINE'";;
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
        List<DistributionStatisticsVO> details = jdbcTemplate.query(sql.toString(),new DistributionStatisticsVoMapper());
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
        baseSql ="select * from hw_meeting_info where 1=1"+ baseSql + " and stage='OFFLINE'";
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
        sql.append("select '大于40人' as name,count(*) as num  from (").append(baseSql).append(")as a where  a.attendee_count >40");
        logger.debug("会议时长分布统计查询sql:"+sql.toString());
        List<DistributionStatisticsVO> details = jdbcTemplate.query(sql.toString(),new DistributionStatisticsVoMapper());
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
                filterSql =" and DATE_SUB(CURDATE(), INTERVAL 1 MONTH) <= date(schedule_start_time)";
                break;
            case "halfyear":
                filterSql =" and DATE_SUB(CURDATE(), INTERVAL 6 MONTH) <= date(schedule_start_time)";
                break;
            case "year":
                filterSql =" and  DATE_SUB(CURDATE(), INTERVAL 1 YEAR) <= date(schedule_start_time)";
                break;
            case "none":
                filterSql=" and date_format(schedule_start_time,'%Y-%m-%d') >= '"+ DateUtil.format(statisticSearchVO.getStartDate(),"yyyy-MM-dd") +"' and date_format(schedule_start_time,'%Y-%m-%d')  <='"+DateUtil.format(statisticSearchVO.getEndDate(),"yyyy-MM-dd")+"'";
                break;
            default:
                break;
        }
        return filterSql;
    }
    public class DistributionStatisticsVoMapper implements RowMapper<DistributionStatisticsVO> {
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
        int end =  videoMettingSearchVO.getCount_();
        String sql= getPageSql(videoMettingSearchVO) +  " limit "+start+","+end;
        logger.debug("分页查询获取数据查询sql:"+sql);
        List<VideoMettingVO> details = jdbcTemplate.query(sql,new VideoMettingVoMapper());
        return details;
    }

    public class VideoMettingVoMapper implements RowMapper<VideoMettingVO> {
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
        // 排序处理：
        String by= videoMettingSearchVO.getBy_();
        String order = videoMettingSearchVO.getOrder_();
        String defaultOrder = "schedule_start_time" ;
        String defaultBy="desc";
        // 目前只按会议日期schedule_start_time
        if(meetingDate.equals(order)){
            defaultOrder = "schedule_start_time" ;
        }
        if(StringUtils.isNotEmpty(by)){
            defaultBy= by;
        }
        sql = sql+" and stage='OFFLINE' order by "+ defaultOrder +" " + defaultBy;
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
        List<VideoMettingExportExcelVO> details = jdbcTemplate.query(sql,new VideoMettingExportExcelVoMapper());
        return details;
    }

    public class VideoMettingExportExcelVoMapper implements RowMapper<VideoMettingExportExcelVO> {
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

}
