package com.vrv.vap.apicasom.business.meeting.dao.impl;

import com.vrv.vap.apicasom.business.meeting.dao.AbnormalMettingDao;
import com.vrv.vap.apicasom.business.meeting.util.MettingCommonUtil;
import com.vrv.vap.apicasom.business.meeting.vo.*;
import com.vrv.vap.jpa.common.DateUtil;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 异常会议记录
 *
 * @author liurz
 */
@Repository
public class AbnormalMettingDaoImpl implements AbnormalMettingDao {
    private static Logger logger = LoggerFactory.getLogger(AbnormalMettingDaoImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 异常类型分布统计
     * @param statisticSearchVO
     * @return
     */
    @Override
    public List<DistributionStatisticsVO> typeStatistics(StatisticSearchVO statisticSearchVO) {
        String filterSql = getFilterSql(statisticSearchVO);
        String sql ="select * from (select count(*) as number,alarm_type as name from hw_meeting_alarm where alarm_status='history' and "+filterSql +" group by  alarm_type)a ORDER BY a.name desc ";
        logger.debug("异常类型分布统计查询sql:"+sql);
        List<DistributionStatisticsVO> details = jdbcTemplate.query(sql,new DistributionStatisticsVoMapper());
        return details;
    }

    /**
     * 异常严重等级分布统计
     *
     * @param statisticSearchVO
     * @return
     */
    @Override
    public List<DistributionStatisticsVO> gradeStatistics(StatisticSearchVO statisticSearchVO) {
        String filterSql = getFilterSql(statisticSearchVO);
        String sql="select a.severity as name,a.number as number  from (select count(*) as number, severity  from hw_meeting_alarm where alarm_status='history' and "+filterSql +" group by severity)a ORDER BY a.severity desc ";
        logger.debug("异常严重等级分布统计查询sql:"+sql);
        List<DistributionStatisticsVO> details = jdbcTemplate.query(sql,new DistributionSeverityVoMapper());
        return details;
    }
    public class DistributionSeverityVoMapper implements RowMapper<DistributionStatisticsVO> {
        @Override
        public DistributionStatisticsVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            DistributionStatisticsVO detail = new DistributionStatisticsVO();
            detail.setName(MettingCommonUtil.getRangeName(rs.getString("name")));
            detail.setNum(rs.getString("number")==null?0:Integer.parseInt(rs.getString("number")));
            return detail;
        }
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
                filterSql ="DATE_SUB(CURDATE(), INTERVAL 1 MONTH) <= date(alarm_time)";
                break;
            case "halfyear":
                filterSql ="DATE_SUB(CURDATE(), INTERVAL 6 MONTH) <= date(alarm_time)";
                break;
            case "year":
                filterSql ="DATE_SUB(CURDATE(), INTERVAL 1 YEAR) <= date(alarm_time)";
                break;
            case "none":
                // 时间范围
                if(null != statisticSearchVO.getStartDate() && null != statisticSearchVO.getEndDate()){
                    filterSql="date_format(alarm_time,'%Y-%m-%d') >= '"+ DateUtil.format(statisticSearchVO.getStartDate(),"yyyy-MM-dd") +"' and date_format(alarm_time,'%Y-%m-%d') <='"+DateUtil.format(statisticSearchVO.getEndDate(),"yyyy-MM-dd")+"'";
                }
                if(null != statisticSearchVO.getStartDate() && null == statisticSearchVO.getEndDate()){
                    filterSql="date_format(alarm_time,'%Y-%m-%d') >= '"+ DateUtil.format(statisticSearchVO.getStartDate(),"yyyy-MM-dd") +"'";
                }
                if(null == statisticSearchVO.getStartDate() && null != statisticSearchVO.getEndDate()){
                    filterSql="date_format(alarm_time,'%Y-%m-%d') <= '"+ DateUtil.format(statisticSearchVO.getEndDate(),"yyyy-MM-dd") +"'";
                }
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
            detail.setNum(rs.getString("number")==null?0:Integer.parseInt(rs.getString("number")));
            return detail;
        }
    }

    @Override
    public List<AbnormalMettingTrendVO> getTreandStatistics(StatisticSearchVO statisticSearchVO) {
        String type = statisticSearchVO.getType();
        String sql ="";
        switch (type) {
            case "month":
                sql = "select date_format(alarm_time,'%Y-%m-%d') as dataX ,count(*) as dataY from hw_meeting_alarm where alarm_status='history' and "+getFilterSql(statisticSearchVO)+"group by date_format(alarm_time,'%Y-%m-%d')";
                break;
            case "halfyear":
            case "year":
                sql = "select date_format(alarm_time,'%Y-%m') as dataX ,count(*) as dataY from hw_meeting_alarm where alarm_status='history' and "+getFilterSql(statisticSearchVO)+"group by date_format(alarm_time,'%Y-%m')";
                break;
            case "none":
                sql = getNoneSql(statisticSearchVO);
                break;
            default:
                break;
        }
        logger.debug("异常趋势统计查询sql:"+sql);
        List<AbnormalMettingTrendVO> details = jdbcTemplate.query(sql,new AbnormalMettingTrendVoMapper());
        return details;
    }
    public class AbnormalMettingTrendVoMapper implements RowMapper<AbnormalMettingTrendVO> {
        @Override
        public AbnormalMettingTrendVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            AbnormalMettingTrendVO detail = new AbnormalMettingTrendVO();
            detail.setDataX(rs.getString("dataX") );
            detail.setDataY(rs.getString("dataY")==null?0:Integer.parseInt(rs.getString("dataY")));
            return detail;
        }
    }

    @Override
    public long getPageTotal(AbnormalMettingSearchVO abnormalMettingSearchVO) {
        String sql ="select count(*) as number  from ("+getPageSql(abnormalMettingSearchVO)+") as a";
        logger.debug("分页查询获取总数查询sql:"+sql);
        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        if (null == result || result.size() == 0) {
            return 0;
        }
        return result.get("number")==null?0:Long.parseLong(String.valueOf(result.get("number")));
    }

    @Override
    public List<AbnormalMettingVO> getPageList(AbnormalMettingSearchVO abnormalMettingSearchVO) {
        int start =  abnormalMettingSearchVO.getStart_();
        int end = abnormalMettingSearchVO.getStart_() * abnormalMettingSearchVO.getCount_() + abnormalMettingSearchVO.getCount_();
        String sql= getPageSql(abnormalMettingSearchVO) +  "  limit "+start+","+end;
        logger.debug("分页查询获取数据查询sql:"+sql);
        List<AbnormalMettingVO> details = jdbcTemplate.query(sql,new AbnormalMettingVoMapper());
        return details;
    }



    public class AbnormalMettingVoMapper implements RowMapper<AbnormalMettingVO> {
        @SneakyThrows
        @Override
        public AbnormalMettingVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            AbnormalMettingVO detail = new AbnormalMettingVO();
            detail.setName(rs.getString("name"));
            detail.setAbnormalType(rs.getString("alarm_type"));
            // 严重等级转换处理
            String grade = rs.getString("severity");
            String gradeName = MettingCommonUtil.getRangeName(grade);
            detail.setGrade(gradeName);
            Date startTime = rs.getTimestamp("alarm_time");
            detail.setStartTime(startTime == null?null: DateUtil.parseDate(DateUtil.format(startTime),DateUtil.DEFAULT_DATE_PATTERN));
            Date endTime = rs.getTimestamp("cleared_time");
            // 开始时间与结束时间计算时长,保留两位小数,分种
            if(null == endTime|| null == startTime){
                detail.setAbnormalTime("");
            }else{
                long result = endTime.getTime()-startTime.getTime();
                detail.setAbnormalTime(MettingCommonUtil.transferMinutesAndSeconds(result));
            }
            return detail;
        }
    }
    private String getPageSql(AbnormalMettingSearchVO abnormalMettingSearchVO){
        String sql="select name,alarm_type,severity,alarm_time,cleared_time from hw_meeting_alarm where  alarm_status='history'  ";
        // 异常名称
        if(StringUtils.isNotEmpty(abnormalMettingSearchVO.getName())){
            sql = sql+" and name like '%" +abnormalMettingSearchVO.getName()+"%'";
        }
        // 异常类型
        if(StringUtils.isNotEmpty(abnormalMettingSearchVO.getAbnormalType())){
            sql = sql+" and alarm_type ='"+abnormalMettingSearchVO.getAbnormalType()+"'";
        }
        // 严重等级
        if(StringUtils.isNotEmpty(abnormalMettingSearchVO.getGrade())){
            sql = sql+" and severity ='"+abnormalMettingSearchVO.getGrade()+"'";
        }
        // 时间
        StatisticSearchVO statisticSearchVO = new StatisticSearchVO();
        statisticSearchVO.setType(abnormalMettingSearchVO.getType());
        statisticSearchVO.setStartDate(abnormalMettingSearchVO.getStartDate());
        statisticSearchVO.setEndDate(abnormalMettingSearchVO.getEndDate());
        String filterSql = getFilterSql(statisticSearchVO);
        if(StringUtils.isNotEmpty(filterSql)){
            sql = sql +"and "+ filterSql;
        }
        sql = sql +" order by alarm_time desc ";
        return sql;
    }



    private String getNoneSql(StatisticSearchVO statisticSearchVO) {
         // 相差大于24H，按天统计
         if(MettingCommonUtil.isDay(statisticSearchVO.getEndDate(),statisticSearchVO.getStartDate())){
            return "select date_format(alarm_time,'%Y-%m-%d') as dataX ,count(*) as dataY from hw_meeting_alarm where alarm_status='history' and "+getFilterSql(statisticSearchVO)+"group by date_format(alarm_time,'%Y-%m-%d')";
         }
         // 相差小于于24H，按小于统计
        return "select date_format(alarm_time,'%Y-%m-%d %H') as dataX ,count(*) as dataY from hw_meeting_alarm where alarm_status='history' and "+getFilterSql(statisticSearchVO)+"group by date_format(alarm_time,'%Y-%m-%d %H')";
    }

    /**
     * 异常记录导出
     * @param abnormalMettingSearchVO
     * @return
     */
    @Override
    public List<AbnormalMettingExportExcelVO> exportData(AbnormalMettingSearchVO abnormalMettingSearchVO) {
        String sql= getPageSql(abnormalMettingSearchVO);
        logger.debug("异常记录导出查询sql:"+sql);
        List<AbnormalMettingExportExcelVO> details = jdbcTemplate.query(sql,new AbnormalMettingExportExcelVoMapper());
        return details;
    }



    public class AbnormalMettingExportExcelVoMapper implements RowMapper<AbnormalMettingExportExcelVO> {
        @Override
        public AbnormalMettingExportExcelVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            AbnormalMettingExportExcelVO data = new AbnormalMettingExportExcelVO();
            Date startTime = rs.getTimestamp("alarm_time");
            Date endTime = rs.getTimestamp("cleared_time");
            // 开始时间与结束时间计算时长 换算成分钟
            if(null == endTime|| null == startTime){
                data.setAbnormalTime("");
            }else{
                long result = endTime.getTime()-startTime.getTime();
                data.setAbnormalTime(MettingCommonUtil.transferMinutesAndSeconds(result));
            }
            data.setStartTime(startTime == null?null: DateUtil.format(startTime));
            // 严重等级转换处理
            String grade = rs.getString("severity");
            String gradeName = MettingCommonUtil.getRangeName(grade);
            data.setGrade(gradeName);
            data.setName(rs.getString("name"));
            data.setAbnormalType(rs.getString("alarm_type"));
            return data;
        }
    }

    /**
     * 获取存在异常会议的城市：告警状态为current,进行中的告警
     * @param type
     * @return
     */
    @Override
    public List<CommonQueryVO> getAbnormalMettingCitys(String type) {
        String sql ="select node.city,node.organization_name,node.name from hw_meeting_participant as node inner join  " +
                " (select meeting_id from hw_meeting_info where meeting_id in (select meeting_id from hw_meeting_alarm where alarm_status='current' )" ;
        switch (type) {
            case "quarter":
                sql =sql + "and DATE_SUB(CURDATE(), INTERVAL 3 MONTH) <= date(schedule_start_time)";
                break;
            case "halfyear":
                sql =sql + "and DATE_SUB(CURDATE(), INTERVAL 6 MONTH) <= date(schedule_start_time)";
                break;
            case "year":
                sql =sql + "and DATE_SUB(CURDATE(), INTERVAL 1 YEAR) <= date(schedule_start_time)";
                break;
            default:
                break;
        }
        sql = sql+ ") meeting on node.meeting_id=meeting.meeting_id group by node.city,node.organization_name,node.name";
        List<CommonQueryVO> details = jdbcTemplate.query(sql,new CommonQueryVoMapper());
        return details;
    }
    public class CommonQueryVoMapper implements RowMapper<CommonQueryVO> {
        @Override
        public CommonQueryVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            CommonQueryVO data = new CommonQueryVO();
            data.setKey(rs.getString("city"));
            data.setValue(rs.getString("organization_name"));
            data.setExtend(rs.getString("name"));
            return data;
        }
    }

    /**
     * 异常名称分组，次数前五的数据，历史告警
     * @param type
     * @return
     */
    @Override
    public List<LargeDeatailVO> getStatisticsByName(String type) {
        String sql ="select * from(select name,count(*) as num from hw_meeting_alarm where alarm_status='history' "+largeScreenCommonSql(type)+" group by name)a order by a.num desc limit 0,5 ";
        List<LargeDeatailVO> details = jdbcTemplate.query(sql,new LargeBranchStatisticsVoMapper());
        logger.debug("异常名称分组，次数前五的数据，历史告警查询sql:"+sql);
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

    private String largeScreenCommonSql(String type){
        String sql ="";
        switch (type) {
            case "quarter":
                sql =" and DATE_SUB(CURDATE(), INTERVAL 3 MONTH) <= date(alarm_time)";
                break;
            case "halfyear":
                sql ="and DATE_SUB(CURDATE(), INTERVAL 6 MONTH) <= date(alarm_time)";
                break;
            case "year":
                sql ="and DATE_SUB(CURDATE(), INTERVAL 1 YEAR) <= date(alarm_time)";
                break;
            default:
                break;
        }
        return sql;
    }

    /**
     * 获取历史异常总数
     * @param type
     * @return
     */
    @Override
    public int getHistoryTotalCount(String type) {
        String sql = "select count(*)as num from hw_meeting_alarm where alarm_status='history'" + largeScreenCommonSql(type);
        logger.debug("获取历史异常总数查询sql:"+sql);
        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        if (null == result || result.size() == 0) {
            return 0;
        }
        int number = result.get("num") == null ? 0 : Integer.parseInt(String.valueOf(result.get("num")));
        return number;
    }

}
