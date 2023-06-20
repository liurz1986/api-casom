package com.vrv.vap.apicasom.business.task.job;

import com.vrv.vap.apicasom.business.task.service.impl.ReservationHwMeetingDataServiceImpl;
import com.vrv.vap.apicasom.frameworks.util.CronUtil;
import com.vrv.vap.jpa.common.DateUtil;
import com.vrv.vap.jpa.spring.SpringUtil;
import org.apache.commons.collections.CollectionUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author: 梁国露
 * @since: 2023/2/16 17:04
 * @description:
 */
public class SyncDataJob implements Job {
    private static Logger logger = LoggerFactory.getLogger(SyncDataJob.class);
    private ReservationHwMeetingDataServiceImpl reservationHwMeetingDataService= SpringUtil.getBean(ReservationHwMeetingDataServiceImpl.class);
    private JdbcTemplate jdbcTemplate= SpringUtil.getBean(JdbcTemplate.class);
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try{
            logger.info("定时执行会议相关数据同步任务开始");
            syncMeetingData();
            logger.info("定时执行会议相关数据同步任务结束");
        }catch (Exception e){
            logger.error("定时执行会议相关数据同步任务异常:{}",e);
        }


    }
    /**
     * 同步会议数据
     */
    public void syncMeetingData(){
        Date date = new Date();
        String endTime = DateUtil.format(date,DateUtil.DEFAULT_DATE_PATTERN);
        String sql = "select job_cron from process_job where status = 1 and job_name = 'HwMeetingSync'";
        List<Map<String,Object>> list = jdbcTemplate.queryForList(sql);
        logger.warn("同步会议数据时，查询process_job表配置情况(status为1和job_name为HwMeetingSync)");
        if(CollectionUtils.isNotEmpty(list)){
            Map<String,Object> map1 = list.get(0);
            String cron = String.valueOf(map1.get("job_cron"));
            String startTime = DateUtil.format(CronUtil.getPreviousValidDate(cron,date),DateUtil.DEFAULT_DATE_PATTERN);
            logger.warn("预约会议调度，时间是{}~{}",startTime,endTime);
            List<String> ids = reservationHwMeetingDataService.queryMeetingIds(startTime,endTime);
            logger.warn("预约会议调度，会议有{}个！",ids.size());
            reservationHwMeetingDataService.handleMeetingInfo(ids);
            logger.warn("预约会议调度，会议详情保存成功");
            reservationHwMeetingDataService.handleMeetingAlarm(ids);
            logger.warn("预约会议调度，会议告警保存成功");
        }else{
            logger.warn("同步会议数据时，查询process_job表配置情况(status为1和job_name为HwMeetingSync)结果不存在，不执行数据同步操作");
        }
    }

}
