package com.vrv.vap.apicasom.business.task.job;

import com.alibaba.fastjson.JSON;
import com.vrv.vap.apicasom.business.task.service.HwMeetingService;
import com.vrv.vap.apicasom.business.task.service.MeetingHttpService;
import com.vrv.vap.apicasom.business.task.service.impl.ReservationHwMeetingDataServiceImpl;
import com.vrv.vap.apicasom.frameworks.util.CronUtil;
import com.vrv.vap.apicasom.frameworks.util.MeetingUtil;
import com.vrv.vap.apicasom.frameworks.util.RedisUtils;
import com.vrv.vap.jpa.common.DateUtil;
import com.vrv.vap.jpa.quartz.QuartzFactory;
import com.vrv.vap.jpa.spring.SpringUtil;
import lombok.SneakyThrows;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.Date;
import java.util.Map;

/**
 * @author: 梁国露
 * @since: 2023/2/16 17:04
 * @description:
 */
@DisallowConcurrentExecution  // 禁止同一个任务并发执行
public class SyncDataJob implements Job {
    private static Logger logger = LoggerFactory.getLogger(SyncDataJob.class);
    private RedisUtils redisUtils= SpringUtil.getBean(RedisUtils.class);
    private ReservationHwMeetingDataServiceImpl reservationHwMeetingDataService= SpringUtil.getBean(ReservationHwMeetingDataServiceImpl.class);
    private JdbcTemplate jdbcTemplate= SpringUtil.getBean(JdbcTemplate.class);
    @SneakyThrows
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try{
            long startTimeM = System.currentTimeMillis();
            Map<String,String> cronmap = (Map<String,String>)jobExecutionContext.getJobDetail().getJobDataMap().get(QuartzFactory.CUSTOM_DATA_KEY);
            String cron = cronmap.get("cron");
            logger.info("定时执行预约会议相关数据同步任务开始.cron:"+cron);
            Date date = new Date();
            String endTime = DateUtil.format(date,DateUtil.DEFAULT_DATE_PATTERN);
            String startTime = null;
            Object meetingTimeObj = redisUtils.get("meetingTime");
            logger.warn("redis中上次同步的时间为："+meetingTimeObj);
            if(null == meetingTimeObj){ // 初次的话，向前推一天
                startTime = getStartTime();
            }else{
                startTime = String.valueOf(meetingTimeObj);
            }
            logger.warn("预约会议调度，时间是{}~{}",startTime,endTime);
            reservationHwMeetingDataService.syncData(startTime,endTime);
            // redis保存同步时间，为了下次同步时开始时间
            redisUtils.set("meetingTime",endTime);
            logger.info("定时执行预约会议相关数据同步任务结束,总花费的时间："+(System.currentTimeMillis()-startTimeM)/1000);
        }catch (Exception e){
            logger.error("定时执行预约会议相关数据同步任务异常:{}",e);
        }

    }

    // 当前时间向前推一天
    private String getStartTime(){
        Date date = new Date();
        long time = date.getTime()-24*60*60*1000;
        String startTime = DateUtil.format(new Date(time),DateUtil.DEFAULT_DATE_PATTERN);
        return startTime;
    }

}
