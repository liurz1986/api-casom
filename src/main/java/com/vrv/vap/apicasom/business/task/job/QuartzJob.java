package com.vrv.vap.apicasom.business.task.job;

import com.vrv.vap.jpa.common.UUIDUtils;
import com.vrv.vap.jpa.quartz.QuartzFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Order(value = 3)
public class QuartzJob implements CommandLineRunner {
    // 日志
    private Logger logger = LoggerFactory.getLogger(QuartzJob.class);

    @Autowired
    private QuartzFactory quartzFactory;

    @Value("${hw.send.time:0 0 1 * * ?}")
    private String sendTime;

    @Value("${hw.meeting.token: 0 */3 * * * ?}")
    private String meetingToken;

    @Value("${hw.meeting.time:0 */9 * * * ?}")
    private String curMeetingCron;

    @Value("${hw.meeting.histime:0 */10 * * * ?}")
    private String hisMeetingCron;


    private String zkySendJobName="ZkySendDataJobName";

    private String meetingJobName="MeetingDataJobName";

    private String commonMeetingJobName="CommonMeetingDataJobName";

    private String hisMeetingSyncDataJobName="HisMeetingSyncDataJobName";

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
            logger.info("会议数据同步任务频次:"+curMeetingCron);
            Map<String,String> params = new HashMap<>();
            params.put("cron",curMeetingCron);
            quartzFactory.addJob(meetingJobName+jobGuid, SyncDataJob.class, curMeetingCron, params);
            //初始化公共信息（分院/城市信息,会议室数量信息）
            logger.info("初始化公共信息（分院/城市信息,会议室数量信息）任务频次:"+meetingToken);
            jobGuid =  UUIDUtils.get32UUID();
            quartzFactory.addJob(commonMeetingJobName+jobGuid, InitCommonDataJob.class, meetingToken, jobGuid);
            // 历史会议数据同步
            params = new HashMap<>();
            params.put("cron",hisMeetingCron);
            logger.info(" 历史会议数据同步任务频次:"+hisMeetingCron);
            quartzFactory.addJob(hisMeetingSyncDataJobName+jobGuid, HisMeetingSyncDataJob.class, hisMeetingCron, params);
        }catch (Exception e){
            logger.error("项目启动开启收发件，会议内容定时任务异常",e);
        }
    }
}
