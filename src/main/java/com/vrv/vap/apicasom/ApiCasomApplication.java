package com.vrv.vap.apicasom;

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
@ComponentScan({"com.vrv.vap.apicasom","com.vrv.vap.jpa.mapper","com.vrv.vap.common","com.vrv.vap.swagger2","com.vrv.vap.jpa.req","com.vrv.vap.jpa.http","com.vrv.vap.syslog.*","com.vrv.vap.jpa.req","com.vrv.vap.jpa.spring"})
public class ApiCasomApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiCasomApplication.class, args);
    }

}
