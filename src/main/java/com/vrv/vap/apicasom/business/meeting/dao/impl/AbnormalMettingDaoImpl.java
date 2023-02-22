package com.vrv.vap.apicasom.business.meeting.dao.impl;

import com.vrv.vap.apicasom.business.meeting.dao.AbnormalMettingDao;
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
import java.text.DecimalFormat;
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
        String sql ="select * from (select count(*) as number ,name from hw_meeting_alarm where "+filterSql +" group by name)a ORDER BY a.name desc ";
        List<DistributionStatisticsVO> details = jdbcTemplate.query(sql,new DistributionStatisticsVOMapper());
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
        String sql="select a.severity as name,a.number as number  from (select count(*) as number, severity  from hw_meeting_alarm where "+filterSql +" group by severity)a ORDER BY a.severity desc ";
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

    public class DistributionStatisticsVOMapper implements RowMapper<DistributionStatisticsVO> {
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
                sql = "select date_format(alarm_time,'%Y-%m-%d') as dataX ,count(*) as dataY from hw_meeting_alarm where "+getFilterSql(statisticSearchVO)+"group by date_format(alarm_time,'%Y-%m-%d')";
                break;
            case "halfyear":
            case "year":
                sql = "select date_format(alarm_time,'%Y-%m') as dataX ,count(*) as dataY from hw_meeting_alarm where "+getFilterSql(statisticSearchVO)+"group by date_format(alarm_time,'%Y-%m')";
                break;
            case "none":
                sql = getNoneSql(statisticSearchVO);
                break;
            default:
                break;
        }
        List<AbnormalMettingTrendVO> details = jdbcTemplate.query(sql,new AbnormalMettingTrendVOMapper());
        return details;
    }
    public class AbnormalMettingTrendVOMapper implements RowMapper<AbnormalMettingTrendVO> {
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
        String sql= getPageSql(abnormalMettingSearchVO) +  " limit "+start+","+end;
        logger.debug("分页查询获取数据查询sql:"+sql);
        List<AbnormalMettingVO> details = jdbcTemplate.query(sql,new AbnormalMettingVOMapper());
        return details;
    }



    public class AbnormalMettingVOMapper implements RowMapper<AbnormalMettingVO> {
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
            detail.setStartTime(startTime);
            Date endTime = rs.getTimestamp("cleared_time");
            // 开始时间与结束时间计算时长,保留两位小数
            if(null == endTime|| null == startTime){
                detail.setAbnormalTime("");
            }else{
                long result = (endTime.getTime()-startTime.getTime())/(1000*60*60);
                DecimalFormat format = new DecimalFormat("#.00");
                String resultTime = result == 0?"0.00": format.format(result);
                detail.setAbnormalTime(resultTime);
            }
            return detail;
        }
    }
    private String getPageSql(AbnormalMettingSearchVO abnormalMettingSearchVO){
        String sql="select name,alarm_type,severity,alarm_time,cleared_time from hw_meeting_alarm where  1=1 ";
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
        return sql;
    }



    private String getNoneSql(StatisticSearchVO statisticSearchVO) {
         // 相差大于24H，按天统计
         if(MettingCommonUtil.isDay(statisticSearchVO.getEndDate(),statisticSearchVO.getStartDate())){
            return "select date_format(alarm_time,'%Y-%m-%d') as dataX ,count(*) as dataY from hw_meeting_alarm where "+getFilterSql(statisticSearchVO)+"group by date_format(alarm_time,'%Y-%m-%d')";
         }
         // 相差小于于24H，按小于统计
        return "select date_format(alarm_time,'%Y-%m-%d %H') as dataX ,count(*) as dataY from hw_meeting_alarm where "+getFilterSql(statisticSearchVO)+"group by date_format(alarm_time,'%Y-%m-%d %H')";
    }

    /**
     * 异常记录导出
     * @param abnormalMettingSearchVO
     * @return
     */
    @Override
    public List<AbnormalMettingExportExcelVO> exportData(AbnormalMettingSearchVO abnormalMettingSearchVO) {
        String sql= getPageSql(abnormalMettingSearchVO);
        logger.debug("分页查询获取数据查询sql:"+sql);
        List<AbnormalMettingExportExcelVO> details = jdbcTemplate.query(sql,new AbnormalMettingExportExcelVOMapper());
        return details;
    }

    public class AbnormalMettingExportExcelVOMapper implements RowMapper<AbnormalMettingExportExcelVO> {
        @Override
        public AbnormalMettingExportExcelVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            AbnormalMettingExportExcelVO data = new AbnormalMettingExportExcelVO();
            Date startTime = rs.getTimestamp("alarm_time");
            Date endTime = rs.getTimestamp("cleared_time");
            // 开始时间与结束时间计算时长
            if(null == endTime|| null == startTime){
                data.setAbnormalTime("");
            }else{
                long result = (endTime.getTime()-startTime.getTime())/(1000*60*60);
                DecimalFormat format = new DecimalFormat("#.00");
                String resultTime = format.format(result);
                data.setAbnormalTime(resultTime);
            }
            data.setStartTime(startTime);
            // 严重等级转换处理
            String grade = rs.getString("severity");
            String gradeName = MettingCommonUtil.getRangeName(grade);
            data.setGrade(gradeName);
            data.setName(rs.getString("name"));
            data.setAbnormalType(rs.getString("alarm_type"));
            return data;
        }
    }
}
