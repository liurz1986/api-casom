package com.vrv.vap.apicasom.business.task.job;

import com.vrv.vap.apicasom.business.task.bean.ProcessJob;
import com.vrv.vap.apicasom.business.task.service.HwMeetingDataService;
import com.vrv.vap.apicasom.business.task.service.impl.MeetingHttpServiceImpl;
import com.vrv.vap.apicasom.frameworks.util.CronUtil;
import com.vrv.vap.jpa.common.DateUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author: 梁国露
 * @since: 2023/2/16 17:04
 * @description:
 */
@Component
public class SyncDataJob implements SchedulingConfigurer {
    // 日志
    private Logger logger = LoggerFactory.getLogger(SyncDataJob.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private HwMeetingDataService reservationHwMeetingDataService;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(
                //1.添加任务内容(Runnable)
                () -> syncMeetingData(),
                //2.设置执行周期(Trigger)
                triggerContext -> {
                    //2.1 从数据库获取执行周期
                    String cron = getProcessJob("HwMeetingSync");
//                    String cron = "0 */2 * * * ?";
                    //2.3 返回执行周期(Date)
                    return new CronTrigger(cron).nextExecutionTime(triggerContext);
                }
        );
    }

    /**
     * 同步会议数据
     */
    public void syncMeetingData(){
        Date date = new Date();
        String endTime = DateUtil.format(date,DateUtil.DEFAULT_DATE_PATTERN);
        String sql = "select job_cron from process_job where status = 1 and job_name = 'HwMeetingSync'";
        List<Map<String,Object>> list = jdbcTemplate.queryForList(sql);
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
        }
    }

    public String getProcessJob(String jobName){
        String sql = "select job_name as jobName,job_cron as jobCron from process_job where status = 1 and job_name = '{0}';";
        sql = sql.replace("{0}",jobName);
        List<ProcessJob> list = jdbcTemplate.query(sql,new BeanPropertyRowMapper<ProcessJob>(ProcessJob.class));
        if(CollectionUtils.isNotEmpty(list)){
            return list.get(0).getJobCron();
        }
        return "0 0/10 * * * ?";
    }
}
