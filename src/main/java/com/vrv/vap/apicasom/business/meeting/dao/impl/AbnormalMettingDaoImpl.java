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
import java.util.ArrayList;
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
     * 异常类型分布统计:按照异常名称分组统计
     * @param statisticSearchVO
     * @return
     */
    @Override
    public List<DistributionStatisticsVO> typeStatistics(StatisticSearchVO statisticSearchVO) {
        List<Object> params = new ArrayList<>();
        String filterSql = getFilterSql(statisticSearchVO,params);
        String sql ="select * from (select count(*) as number,name  from hw_meeting_alarm where alarm_status='history'  "+filterSql +" group by  name)a ORDER BY a.name desc ";
        logger.debug("异常类型分布统计查询sql:"+sql);
        List<DistributionStatisticsVO> details = jdbcTemplate.query(sql,new DistributionStatisticsVoMapper(),params.toArray());
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
        List<Object> params = new ArrayList<>();
        String filterSql = getFilterSql(statisticSearchVO,params);
        String sql="select a.severity as name,a.number as number  from (select count(*) as number, severity  from hw_meeting_alarm where alarm_status='history'  "+filterSql +" group by severity)a ORDER BY a.severity desc ";
        logger.debug("异常严重等级分布统计查询sql:"+sql);
        List<DistributionStatisticsVO> details = jdbcTemplate.query(sql,new DistributionSeverityVoMapper(),params.toArray());
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
    private  String getFilterSql(StatisticSearchVO statisticSearchVO,List<Object> params) {
        String type = statisticSearchVO.getType();
        String filterSql ="";
        switch (type) {
            case "month":
                filterSql =" and DATE_SUB(CURDATE(), INTERVAL 1 MONTH) <= date(alarm_time)";
                break;
            case "halfyear":
                filterSql =" and DATE_SUB(CURDATE(), INTERVAL 6 MONTH) <= date(alarm_time)";
                break;
            case "year":
                filterSql =" and DATE_SUB(CURDATE(), INTERVAL 1 YEAR) <= date(alarm_time)";
                break;
            case "none":
                // 时间范围
                if(null != statisticSearchVO.getStartDate() && null != statisticSearchVO.getEndDate()){
                    filterSql=" and date_format(alarm_time,'%Y-%m-%d') >= ? and date_format(alarm_time,'%Y-%m-%d') <=? ";
                    params.add(DateUtil.format(statisticSearchVO.getStartDate(),"yyyy-MM-dd"));
                    params.add(DateUtil.format(statisticSearchVO.getEndDate(),"yyyy-MM-dd"));
                }
                if(null != statisticSearchVO.getStartDate() && null == statisticSearchVO.getEndDate()){
                    filterSql=" and date_format(alarm_time,'%Y-%m-%d') >= ? ";
                    params.add(DateUtil.format(statisticSearchVO.getStartDate(),"yyyy-MM-dd"));
                }
                if(null == statisticSearchVO.getStartDate() && null != statisticSearchVO.getEndDate()){
                    filterSql=" and date_format(alarm_time,'%Y-%m-%d') <= ? ";
                    params.add(DateUtil.format(statisticSearchVO.getEndDate(),"yyyy-MM-dd"));
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
        List<Object> params = new ArrayList<>();
        String type = statisticSearchVO.getType();
        String sql ="";
        switch (type) {
            case "month":
                sql = "select date_format(alarm_time,'%Y-%m-%d') as dataX ,count(*) as dataY from hw_meeting_alarm where alarm_status='history' "+getFilterSql(statisticSearchVO,params)+"group by date_format(alarm_time,'%Y-%m-%d')";
                break;
            case "halfyear":
            case "year":
                sql = "select date_format(alarm_time,'%Y-%m') as dataX ,count(*) as dataY from hw_meeting_alarm where alarm_status='history' "+getFilterSql(statisticSearchVO,params)+"group by date_format(alarm_time,'%Y-%m')";
                break;
            case "none":
                sql = getNoneSql(statisticSearchVO,params);
                break;
            default:
                break;
        }
        logger.debug("异常趋势统计查询sql:"+sql);
        List<AbnormalMettingTrendVO> details = jdbcTemplate.query(sql,new AbnormalMettingTrendVoMapper(),params.toArray());
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
        List<Object> params = new ArrayList<>();
        String sql ="select count(*) as number  from ("+getPageSql(abnormalMettingSearchVO,params)+") as a";
        logger.debug("分页查询获取总数查询sql:"+sql);
        Map<String, Object> result = jdbcTemplate.queryForMap(sql,params.toArray());
        if (null == result || result.size() == 0) {
            return 0;
        }
        return result.get("number")==null?0:Long.parseLong(String.valueOf(result.get("number")));
    }

    @Override
    public List<AbnormalMettingVO> getPageList(AbnormalMettingSearchVO abnormalMettingSearchVO) {
        int start =  abnormalMettingSearchVO.getStart_();
        int end = abnormalMettingSearchVO.getCount_();
        List<Object> params = new ArrayList<>();
        String sql= getPageSql(abnormalMettingSearchVO,params) +  "  limit "+start+","+end;
        logger.debug("分页查询获取数据查询sql:"+sql);
        List<AbnormalMettingVO> details = jdbcTemplate.query(sql,new AbnormalMettingVoMapper(),params.toArray());
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
    private String getPageSql(AbnormalMettingSearchVO abnormalMettingSearchVO,List<Object> params){
        String sql="select name,alarm_type,severity,alarm_time,cleared_time from hw_meeting_alarm where  alarm_status='history'  ";
        // 异常名称
        if(StringUtils.isNotEmpty(abnormalMettingSearchVO.getName())){
            sql = sql+" and name like ? ";
            params.add("%"+abnormalMettingSearchVO.getName()+"%");
        }
        // 异常类型
        if(StringUtils.isNotEmpty(abnormalMettingSearchVO.getAbnormalType())){
            sql = sql+" and alarm_type like ? ";
            params.add("%"+abnormalMettingSearchVO.getAbnormalType()+"%");
        }
        // 严重等级
        if(StringUtils.isNotEmpty(abnormalMettingSearchVO.getGrade())){
            sql = sql+" and severity =? ";
            params.add(abnormalMettingSearchVO.getGrade());
        }
        // 时间
        StatisticSearchVO statisticSearchVO = new StatisticSearchVO();
        statisticSearchVO.setType(abnormalMettingSearchVO.getType());
        statisticSearchVO.setStartDate(abnormalMettingSearchVO.getStartDate());
        statisticSearchVO.setEndDate(abnormalMettingSearchVO.getEndDate());
        String filterSql = getFilterSql(statisticSearchVO,params);
        if(StringUtils.isNotEmpty(filterSql)){
            sql = sql + filterSql;
        }
        sql = sql +" order by alarm_time desc ";
        return sql;
    }



    private String getNoneSql(StatisticSearchVO statisticSearchVO,List<Object> params) {
         // 相差大于24H，按天统计
         if(MettingCommonUtil.isDay(statisticSearchVO.getEndDate(),statisticSearchVO.getStartDate())){
            return "select date_format(alarm_time,'%Y-%m-%d') as dataX ,count(*) as dataY from hw_meeting_alarm where alarm_status='history'  "+getFilterSql(statisticSearchVO,params)+"group by date_format(alarm_time,'%Y-%m-%d')";
         }
         // 相差小于于24H，按小于统计
        return "select date_format(alarm_time,'%Y-%m-%d %H') as dataX ,count(*) as dataY from hw_meeting_alarm where alarm_status='history'  "+getFilterSql(statisticSearchVO,params)+"group by date_format(alarm_time,'%Y-%m-%d %H')";
    }

    /**
     * 异常记录导出
     * @param abnormalMettingSearchVO
     * @return
     */
    @Override
    public List<AbnormalMettingExportExcelVO> exportData(AbnormalMettingSearchVO abnormalMettingSearchVO) {
        List<Object> params = new ArrayList<>();
        String sql= getPageSql(abnormalMettingSearchVO,params);
        logger.debug("异常记录导出查询sql:"+sql);
        List<AbnormalMettingExportExcelVO> details = jdbcTemplate.query(sql,new AbnormalMettingExportExcelVoMapper(),params.toArray());
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

}
