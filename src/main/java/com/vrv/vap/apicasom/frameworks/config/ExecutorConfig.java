package com.vrv.vap.apicasom.frameworks.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

public class ExecutorConfig {
    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger(ExecutorConfig.class);

    public static Executor asyncServiceExecutor(){
        logger.info("ExecutorConfig start");
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(4);
        threadPoolTaskExecutor.setMaxPoolSize(8);
        threadPoolTaskExecutor.setKeepAliveSeconds(60);
         threadPoolTaskExecutor.setQueueCapacity(32);
        threadPoolTaskExecutor.setThreadNamePrefix("async-have-log-data-Executor-");
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }

    public static Executor historyAlarmMeetingExecutor(){
        logger.info("ExecutorConfig start");
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(5);
        threadPoolTaskExecutor.setMaxPoolSize(8);
        threadPoolTaskExecutor.setKeepAliveSeconds(60);
        threadPoolTaskExecutor.setQueueCapacity(32);
        threadPoolTaskExecutor.setThreadNamePrefix("risk-event-rule-Executor-");
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }

}
