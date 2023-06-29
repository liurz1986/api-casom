package com.vrv.vap.apicasom.business.task.job;
import com.vrv.vap.apicasom.business.task.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author: 梁国露
 * @since: 2023/2/17 15:12
 * @description: 收发件消息
 */
 @Component
 @EnableScheduling
 @EnableAsync
public class ZkySendJob {
    // 日志
    private Logger logger = LoggerFactory.getLogger(ZkySendJob.class);

    @Autowired
    private ZkySendDataService zkySendDataService;


    @Scheduled(cron = "${hw.send.time}")
    public void getZkySend(){
        try{
            logger.info("定时执行收发件任务开始");
            zkySendDataService.excZkySend();
        }catch (Exception e){
            logger.error("中科院文件信息同步失败：{}",e);
        }
    }

}
