package com.vrv.vap.apicasom.business.task.job;

import com.vrv.vap.apicasom.business.task.bean.ZkyUnitBean;
import com.vrv.vap.apicasom.business.task.service.HwMeetingService;
import com.vrv.vap.apicasom.business.task.service.MeetingHttpService;
import com.vrv.vap.apicasom.business.task.service.ZkyUnitService;
import com.vrv.vap.apicasom.frameworks.util.RedisUtils;
import com.vrv.vap.jpa.spring.SpringUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
/**
 * @author: 梁国露
 * @since: 2023/2/17 15:12
 * @description: 初始化公共信息（token,分院/城市信息,会议室数量信息）
 */
public class InitCommonDataJob implements Job {
    private static Logger logger = LoggerFactory.getLogger(InitCommonDataJob.class);

    private MeetingHttpService meetingHttpService= SpringUtil.getBean(MeetingHttpService.class);
    private HwMeetingService hwMeetingService= SpringUtil.getBean(HwMeetingService.class);
    private ZkyUnitService zkyUnitService= SpringUtil.getBean(ZkyUnitService.class);
    private RedisUtils redisUtils= SpringUtil.getBean(RedisUtils.class);
    public static String token = null;
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try{
            logger.info("初始化公共信息（token,分院/城市信息,会议室数量信息）任务执行");
            token = meetingHttpService.getToken(0);
            hwMeetingService.updateToken(token);
            updateCity();
            initMeetingRooms();
        }catch (Exception e){
            logger.error("初始化公共信息（token,分院/城市信息,会议室数量信息）任务执行异常:{}",e);
        }

    }
    /**
     * 更新城市信息
     */
    public void updateCity(){
        Map<String,ZkyUnitBean> zkyUnitBeanMap = zkyUnitService.initCity();
        hwMeetingService.updateCity(zkyUnitBeanMap);
    }

    /**
     * 会议室信息同步
     */
    public void initMeetingRooms(){
        int total = meetingHttpService.initMeetingRooms();
        redisUtils.set("MeetingRooms",total);
    }


}
