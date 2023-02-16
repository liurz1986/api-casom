package com.vrv.vap.apicasom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession
@SpringBootApplication
@ComponentScan({"com.vrv.vap.apicasom","com.vrv.vap.jpa.mapper","com.vrv.vap.common","com.vrv.vap.swagger2","com.vrv.vap.es","com.vrv.vap.jpa.req","com.vrv.vap.jpa.http","com.vrv.vap.syslog.*","com.vrv.vap.jpa.req"})
public class ApiCasomApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiCasomApplication.class, args);
    }

}
