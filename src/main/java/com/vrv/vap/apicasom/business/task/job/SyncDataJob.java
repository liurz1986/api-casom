package com.vrv.vap.apicasom.business.task.job;

import com.alibaba.fastjson.JSON;
import com.vrv.vap.apicasom.business.task.service.HwMeetingService;
import com.vrv.vap.apicasom.business.task.service.MeetingHttpService;
import com.vrv.vap.apicasom.business.task.service.impl.ReservationHwMeetingDataServiceImpl;
import com.vrv.vap.apicasom.frameworks.util.CronUtil;
import com.vrv.vap.apicasom.frameworks.util.RedisUtils;
import com.vrv.vap.jpa.common.DateUtil;
import com.vrv.vap.jpa.quartz.QuartzFactory;
import com.vrv.vap.jpa.spring.SpringUtil;
import lombok.SneakyThrows;
import org.apache.commons.collections.CollectionUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author: 梁国露
 * @since: 2023/2/16 17:04
 * @description:
 */
@DisallowConcurrentExecution  // 禁止同一个任务并发执行
public class SyncDataJob implements Job {
    private static Logger logger = LoggerFactory.getLogger(SyncDataJob.class);
    private HwMeetingService hwMeetingService= SpringUtil.getBean(HwMeetingService.class);
    private MeetingHttpService meetingHttpService= SpringUtil.getBean(MeetingHttpService.class);
    private RedisUtils redisUtils= SpringUtil.getBean(RedisUtils.class);
    private ReservationHwMeetingDataServiceImpl reservationHwMeetingDataService= SpringUtil.getBean(ReservationHwMeetingDataServiceImpl.class);
    private JdbcTemplate jdbcTemplate= SpringUtil.getBean(JdbcTemplate.class);
    @SneakyThrows
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try{
            long startTime = System.currentTimeMillis();
            Map<String,String> cronmap = (Map<String,String>)jobExecutionContext.getJobDetail().getJobDataMap().get(QuartzFactory.CUSTOM_DATA_KEY);
            String cron = cronmap.get("cron");
            logger.info("定时执行会议相关数据同步任务开始.cron:"+cron);
            syncMeetingData(cron);
            logger.info("定时执行会议相关数据同步任务结束,总花费的时间："+(System.currentTimeMillis()-startTime)/1000);
        }catch (Exception e){
            logger.error("定时执行会议相关数据同步任务异常:{}",e);
        }


    }
    /**
     * 同步会议数据
     */
    public void syncMeetingData(String cron){
        String token = meetingHttpService.getToken(0);
        hwMeetingService.updateToken(token);
        Date date = new Date();
        String endTime = DateUtil.format(date,DateUtil.DEFAULT_DATE_PATTERN);
        Object meetingTimeObj = redisUtils.get("meetingTime");
        logger.warn("redis中上次同步的时间为："+meetingTimeObj);
        String startTime = null;
        if(null == meetingTimeObj){
            startTime = DateUtil.format(CronUtil.getPreviousValidDate(cron,date),DateUtil.DEFAULT_DATE_PATTERN);
        }else{
            startTime = String.valueOf(meetingTimeObj);
        }
        logger.warn("预约会议调度，时间是{}~{}",startTime,endTime);
        List<String> ids = reservationHwMeetingDataService.queryMeetingIds(startTime,endTime);
        if(CollectionUtils.isEmpty(ids)){
            logger.warn("获取预约会议调度id为空,不处理");
            return;
        }
        logger.warn("预约会议调度，会议有{}个！",ids.size());
        logger.info("预约会议调度所有会议id："+(JSON.toJSONString(ids)));
        reservationHwMeetingDataService.handleMeetingInfo(ids);
        logger.warn("预约会议调度，会议详情保存成功");
        reservationHwMeetingDataService.handleMeetingAlarm(ids);
        logger.warn("预约会议调度，会议告警保存成功");
        // redis保存同步时间，为了下次同步时开始时间
        redisUtils.set("meetingTime",endTime);
    }

}
