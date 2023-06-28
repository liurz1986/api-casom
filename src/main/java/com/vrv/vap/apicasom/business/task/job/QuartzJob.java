package com.vrv.vap.apicasom.business.task.job;

import com.vrv.vap.apicasom.business.task.bean.ProcessJob;
import com.vrv.vap.jpa.common.UUIDUtils;
import com.vrv.vap.jpa.quartz.QuartzFactory;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 项目启动时触发quartz任务
 * 2023-06-20
 * @author vrv
 */
// @Component
// @Order(value = 3)
public class QuartzJob implements CommandLineRunner {
    // 日志
    private Logger logger = LoggerFactory.getLogger(QuartzJob.class);

    @Autowired
    private QuartzFactory quartzFactory;

    @Value("${hw.send.time}")
    private String sendTime;

    @Value("${hw.meeting.token}")
    private String meetingToken;

    private String zkySendJobName="ZkySendDataJobName";

    private String meetingJobName="MeetingDataJobName";

    private String commonMeetingJobName="CommonMeetingDataJobName";
    @Autowired
    private JdbcTemplate jdbcTemplate;
    /**
     * 触发定时任务
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args)  {
        try{
            logger.info("项目启动开启收发件，会议内容定时任务");
            String jobGuid =  UUIDUtils.get32UUID();
            // 收发件任务
            logger.info("收发件任务频次:"+sendTime);
            quartzFactory.addJob(zkySendJobName+jobGuid, ZkySendJob.class, sendTime, jobGuid);
            jobGuid =  UUIDUtils.get32UUID();
            // 会议相关数据同步
            // 从数据库获取执行周期
            String cron = getProcessJob("HwMeetingSync");
            logger.info("会议数据同步任务频次:"+cron);
            quartzFactory.addJob(meetingJobName+jobGuid, SyncDataJob.class, cron, jobGuid);
            //初始化公共信息（token,分院/城市信息,会议室数量信息）
            logger.info("初始化公共信息（token,分院/城市信息,会议室数量信息）任务频次:"+meetingToken);
            jobGuid =  UUIDUtils.get32UUID();
            quartzFactory.addJob(commonMeetingJobName+jobGuid, InitCommonDataJob.class, cron, jobGuid);
        }catch (Exception e){
            logger.error("项目启动开启收发件，会议内容定时任务异常",e);
        }
    }

    public String getProcessJob(String jobName){
        String sql = "select job_name as jobName,job_cron as jobCron from process_job where status = 1 and job_name = '{0}';";
        sql = sql.replace("{0}",jobName);
        List<ProcessJob> list = jdbcTemplate.query(sql,new BeanPropertyRowMapper<ProcessJob>(ProcessJob.class));
        if(CollectionUtils.isNotEmpty(list)){
            return list.get(0).getJobCron();
        }
        return "0 */2 * * * ?";
    }
}
