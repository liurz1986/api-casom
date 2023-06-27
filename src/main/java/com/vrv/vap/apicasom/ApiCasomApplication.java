package com.vrv.vap.apicasom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableFeignClients(basePackages = "com.vrv.vap")
@EnableHystrix
@EnableRedisHttpSession
@EnableDiscoveryClient
@SpringBootApplication
@ComponentScan({"com.vrv.vap.apicasom","com.vrv.vap.jpa.mapper","com.vrv.vap.jpa.quartz","com.vrv.vap.common","com.vrv.vap.swagger2","com.vrv.vap.jpa.req","com.vrv.vap.jpa.http","com.vrv.vap.syslog.*","com.vrv.vap.jpa.req","com.vrv.vap.jpa.spring","com.vrv.vap.es.*"})
public class ApiCasomApplication {
    private static Logger logger = LoggerFactory.getLogger(ApiCasomApplication.class);
    public static void main(String[] args) {
        logger.info("启动api-casom模块！！！");
        SpringApplication.run(ApiCasomApplication.class, args);
    }

}
