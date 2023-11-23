package com.vrv.vap.apicasom.business.task.job;

import com.vrv.vap.apicasom.business.task.service.impl.HistoryHwMeetingDataServiceImpl;
import com.vrv.vap.apicasom.frameworks.util.CronUtil;
import com.vrv.vap.apicasom.frameworks.util.RedisUtils;
import com.vrv.vap.jpa.common.DateUtil;
import com.vrv.vap.jpa.quartz.QuartzFactory;
import com.vrv.vap.jpa.spring.SpringUtil;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

/**
 * 历史会议数据同步
 *
 * liurz
 * 2023-07-21
 */
@DisallowConcurrentExecution // 禁止同一个任务并发执行
public class HisMeetingSyncDataJob implements Job {
    private static Logger logger = LoggerFactory.getLogger(HisMeetingSyncDataJob.class);

    private HistoryHwMeetingDataServiceImpl historyHwMeetingDataService= SpringUtil.getBean(HistoryHwMeetingDataServiceImpl.class);
    private RedisUtils redisUtils= SpringUtil.getBean(RedisUtils.class);
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try{
            logger.info("历史会议数据同步任务执行开始");
            Map<String,String> cronmap = (Map<String,String>)jobExecutionContext.getJobDetail().getJobDataMap().get(QuartzFactory.CUSTOM_DATA_KEY);
            String cron = cronmap.get("cron");
            String startTime = null;
            Date date = new Date();
            Object hisMeetingTimeObj = redisUtils.get("hisMeetingTime");
            logger.warn("redis中记录上次历史数据同步的时间为："+hisMeetingTimeObj);
            if(null == hisMeetingTimeObj){// 初次的话，向前推一个星期
                startTime = getStartTime();
            }else{
                startTime = String.valueOf(hisMeetingTimeObj);
            }
            String endTime = DateUtil.format(date,DateUtil.DEFAULT_DATE_PATTERN);
            // 执行历史数据同步
            historyHwMeetingDataService.syncData(startTime,endTime);
            // redis保存同步时间，为了下次同步时开始时间
            redisUtils.set("hisMeetingTime",endTime);
            logger.info("历史会议数据同步任务执行完成");
        }catch (Exception e){
            logger.error("历史会议数据同步任务执行异常:{}",e);
        }

    }
    // 当前时间向前推一周
    private String getStartTime(){
        Date date = new Date();
        long time = date.getTime()-7*24*60*60*1000;
        String startTime = DateUtil.format(new Date(time),DateUtil.DEFAULT_DATE_PATTERN);
        return startTime;
    }

}
