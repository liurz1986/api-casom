package com.vrv.vap.apicasom.business.task.job;

import com.vrv.vap.apicasom.business.task.bean.ZkyUnitBean;
import com.vrv.vap.apicasom.business.task.service.HwMeetingService;
import com.vrv.vap.apicasom.business.task.service.MeetingHttpService;
import com.vrv.vap.apicasom.business.task.service.ZkyUnitService;
import com.vrv.vap.apicasom.frameworks.util.RedisUtils;
import com.vrv.vap.jpa.spring.SpringUtil;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author: 梁国露
 * @since: 2023/2/17 15:12
 * @description: 初始化公共信息（分院/城市信息,会议室数量信息）
 */
@DisallowConcurrentExecution // 禁止同一个任务并发执行
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
            logger.info("初始化公共信息（分院/城市信息,会议室数量信息）任务执行开始");
            updateCity();
            initMeetingRooms();
            logger.info("初始化公共信息（分院/城市信息,会议室数量信息）任务执行完成");
        }catch (Exception e){
            logger.error("初始化公共信息（分院/城市信息,会议室数量信息）任务执行异常:{}",e);
        }

    }
    /**
     * 更新城市信息
     */
    public void updateCity(){
        logger.info("更新城市信息");
        Map<String,ZkyUnitBean> zkyUnitBeanMap = zkyUnitService.initCity();
        hwMeetingService.updateCity(zkyUnitBeanMap);
    }

    /**
     * 会议室信息同步
     */
    public void initMeetingRooms(){
        logger.info("初始会议室信息开始");
        int total = meetingHttpService.initMeetingRooms();
        logger.info("会议室数量："+total);
        redisUtils.set("MeetingRooms",total);
    }


}
