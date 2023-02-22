package com.vrv.vap.apicasom.business.task.job;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vrv.vap.apicasom.business.task.bean.ZkyUnitBean;
import com.vrv.vap.apicasom.business.task.bean.hwmeetingbean.Token;
import com.vrv.vap.apicasom.business.task.constant.MeetingUrlConstant;
import com.vrv.vap.apicasom.business.task.service.MeetingHttpService;
import com.vrv.vap.apicasom.business.task.service.ZkyUnitService;
import com.vrv.vap.apicasom.frameworks.util.Base64Utils;
import com.vrv.vap.apicasom.frameworks.util.HttpClientUtils;
import com.vrv.vap.apicasom.frameworks.util.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author: 梁国露
 * @since: 2023/2/17 15:12
 * @description:
 */
@Component
@EnableScheduling
public class InitTokenJob{

    public static String token = null;

    @Autowired
    private MeetingHttpService meetingHttpService;

    @Autowired
    private ZkyUnitService zkyUnitService;

    @Autowired
    private RedisUtils redisUtils;

//    @Scheduled(cron = "59 59 23 * * ?")
    @Scheduled(cron = "0 */1 * * * ?")
    public void getToken(){
        token = meetingHttpService.getToken(0);
        meetingHttpService.updateToken(token);
        updateCity();
        initMeetingRooms();
    }

    public void updateCity(){
        Map<String,ZkyUnitBean> zkyUnitBeanMap = zkyUnitService.initCity();
        meetingHttpService.updateCity(zkyUnitBeanMap);
    }

    public void initMeetingRooms(){
        int total = meetingHttpService.initMeetingRooms();
        redisUtils.set("MeetingRooms",total);
    }
}
