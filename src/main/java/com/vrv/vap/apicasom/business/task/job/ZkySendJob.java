package com.vrv.vap.apicasom.business.task.job;

import com.vrv.vap.apicasom.business.task.service.ZkySendDataService;
import com.vrv.vap.jpa.spring.SpringUtil;
import lombok.SneakyThrows;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 收发件任务
 * 2023-06-20
 * @author vrv
 */
@DisallowConcurrentExecution // 禁止同一个任务并发执行
public class ZkySendJob implements Job {
    private static Logger logger = LoggerFactory.getLogger(ZkySendJob.class);
    private ZkySendDataService zkySendDataService= SpringUtil.getBean(ZkySendDataService.class);
    @SneakyThrows
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info("定时执行收发件任务开始");
        try{
            zkySendDataService.excZkySend();
            logger.info("定时执行收发件任务完成");
        }catch (Exception e){
            logger.error("定时执行收发件任务异常：{}",e);
        }


    }
}
