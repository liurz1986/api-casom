package com.vrv.vap.apicasom.business.task.job;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vrv.vap.apicasom.business.task.bean.ZkyUnitBean;
import com.vrv.vap.apicasom.business.task.bean.hwmeetingbean.Token;
import com.vrv.vap.apicasom.business.task.constant.MeetingUrlConstant;
import com.vrv.vap.apicasom.business.task.service.HwMeetingService;
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
 * @description: 初始化公共信息（token,分院/城市信息,会议室数量信息）
 */
@Component
@EnableScheduling
public class InitCommonDataJob {

    public static String token = null;

    @Autowired
    private MeetingHttpService meetingHttpService;

    @Autowired
    private HwMeetingService hwMeetingService;

    @Autowired
    private ZkyUnitService zkyUnitService;

    @Autowired
    private RedisUtils redisUtils;

    @Scheduled(cron = "${hw.meeting.token}")
    public void getToken(){
        token = meetingHttpService.getToken(0);
        hwMeetingService.updateToken(token);
        updateCity();
        initMeetingRooms();
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
