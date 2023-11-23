package com.vrv.vap.apicasom.business.task.job;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vrv.vap.apicasom.business.task.bean.TimeBean;
import com.vrv.vap.apicasom.business.task.bean.ZkyUnitBean;
import com.vrv.vap.apicasom.business.task.service.HwMeetingDataService;
import com.vrv.vap.apicasom.business.task.service.HwMeetingService;
import com.vrv.vap.apicasom.business.task.service.MeetingHttpService;
import com.vrv.vap.apicasom.business.task.service.ZkyUnitService;
import com.vrv.vap.apicasom.business.task.service.impl.ReservationHwMeetingDataServiceImpl;
import com.vrv.vap.apicasom.frameworks.util.MeetingUtil;
import com.vrv.vap.apicasom.frameworks.util.RedisUtils;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

/**
 *  手动补全历史会议数据
 *  初始化会议室数量放在redis中
 *  初始城市信息本地缓存
 * @author: 梁国露
 * @since: 2023/2/16 17:04
 * @description:
 */
@Component
@Order(value = 2)
public class InitDataJob implements CommandLineRunner {
    // 日志
    private Logger logger = LoggerFactory.getLogger(InitDataJob.class);

    /**
     * gson对象
     */
    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();



    @Autowired
    private HwMeetingDataService historyHwMeetingDataService;

    @Autowired
    private MeetingHttpService meetingHttpService;
    @Autowired
    private HwMeetingService hwMeetingService;

    @Autowired
    private ZkyUnitService zkyUnitService;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private ReservationHwMeetingDataServiceImpl reservationHwMeetingDataService;

    /**
     * 是否执行手动历史数据补全
     */
    @Value("${hw.meeting.hiscompletion.status:false}")
    private Boolean hisStatus;
    /**
     * 手动历史数据补全开始时间
     */
    @Value("${hw.meeting.hiscompletion.starttime}")
    private String hisStarttime;
    /**
     * 手动历史数据补全结束时间
     */
    @Value("${hw.meeting.hiscompletion.endtime}")
    private String hisEndtime;

    public static boolean isToken=false;


    @Override
    public void run(String... args) throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                initData();
            }
        }).start();
    }

    /**
     * 初始化任务
     */
    public void initData(){
            // 历史会议数据补全
            hisMeetingHandle();
            // 初始化会议室数量放在redis中
            initMeetingRooms();
            // 初始城市信息本地缓存
            updateCity();

    }
    private void hisMeetingHandle() {
        try{
            if(!hisStatus ){
                logger.info("不执行手动历史数据补全");
                return;
            }
            if(StringUtils.isEmpty(hisStarttime)||StringUtils.isEmpty(hisEndtime)){
                logger.info("手动历史数据补全,开始时间或结束时间存在空的情况，不执行补全");
                return;
            }
            TimeBean time = new TimeBean();
            time.setStartTime(hisStarttime);
            time.setEndTime(hisEndtime);
            // 判断是不是重复补全
            Object obj =  redisUtils.get("hiscompletion");
            if(null != obj){
                String old = String.valueOf(obj);
                String newData = JSON.toJSONString(time);
                if(newData.equals(old)){
                    logger.info("同步时间与上次相同，不执行手动历史会议数据补全");
                    return;
                }
            }
            // 执行补全操作
            hisHandleData(time);
            // redis记录当前状态，避免重复补全
            redisUtils.set("hiscompletion", JSON.toJSONString(time));
        }catch (Exception e){
            logger.error("历史会议手动补全异常",e);
        }
    }


    /**
     * 历史会议补全
     * @param time
     */
    public void hisHandleData(TimeBean time){
        try{
            long stime = System.currentTimeMillis();
            // 处理数据查询时间
            String startTime = time.getStartTime();
            String endTime = time.getEndTime();
            logger.warn("手动补全历史数据开始!开始时间={}，结束时间={}",startTime,endTime);
            String token = meetingHttpService.getToken(0);
            if(StringUtils.isEmpty(token)){
                logger.error("获取token为空,请确认！");
                return;
            }
            logger.info("token的值："+token);
            MeetingUtil.token= token;
            // 异步循环调用获取token接口，防止token过期
            tokenHandle();
            // 处理数据
            List<String> ids = historyHwMeetingDataService.queryMeetingIds(startTime,endTime);
            logger.warn("补全历史数据 会议ID有{}个！",ids.size());
            if(CollectionUtils.isNotEmpty(ids)){
                historyHwMeetingDataService.handleMeetingInfo(ids);
                logger.warn("补全历史数据 会议详情 完成！");
                historyHwMeetingDataService.handleMeetingAlarm(ids);
                logger.warn("补全历史数据 会议告警 完成！");
            }
            logger.info("手动补全历史数据总时间："+(System.currentTimeMillis()-stime));
            isToken = false;
        }catch (Exception e){
            isToken = false;
            logger.error("手动补全历史数据异常",e);
            throw new RuntimeException("手动补全历史数据异常");
        }
    }

    private void tokenHandle() {
        isToken =true;
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                while(isToken){
                    try{
                        Thread.sleep(120000);
                        String token = meetingHttpService.getToken(0);
                        if(StringUtils.isEmpty(token)){
                            logger.error("获取token为空,请确认！");
                            return;
                        }
                        logger.info("token的值："+token);
                        MeetingUtil.token= token;
                    }catch (Exception e){
                        logger.error("手动补全历史数据时，定时获取token异常",e);
                    }
                }
            }
        }).start();
    }

    /**
     * 初始化会议室数量放在redis中
     */
    public void initMeetingRooms(){
        try{
            logger.info("初始会议室信息开始");
            int total = meetingHttpService.initMeetingRooms();
            logger.info("会议室数量："+total);
            redisUtils.set("MeetingRooms",total);
        }catch (Exception e){
            logger.error("获取会议室信息异常",e);
        }
    }
    /**
     * 初始城市信息本地缓存
     */
    public void updateCity(){
        try{
            logger.info("初始城市信息本地缓存");
            Map<String, ZkyUnitBean> zkyUnitBeanMap = zkyUnitService.initCity();
            hwMeetingService.updateCity(zkyUnitBeanMap);
        }catch (Exception e){
            logger.error("初始城市信息本地缓存异常",e);
        }

    }

}
